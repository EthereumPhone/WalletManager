package com.core.data.service

import android.app.Application
import android.util.Log
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages WalletConnect sessions and requests.
 * Handles pairing, session proposals, and sign requests from dApps.
 */
class WalletConnectManager(private val application: Application) {
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _sessionRequests = MutableStateFlow<List<SessionRequest>>(emptyList())
    val sessionRequests: StateFlow<List<SessionRequest>> = _sessionRequests.asStateFlow()
    
    private val _activeSessions = MutableStateFlow<List<ActiveSession>>(emptyList())
    val activeSessions: StateFlow<List<ActiveSession>> = _activeSessions.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    private var currentTopic: String? = null
    private var isInitialized = false
    private var isWalletKitReady = false
    private var lastActivityTime: Long = 0L
    private var consecutiveFailedHealthChecks = 0
    private var lastProposalPublicKey: String? = null
    
    // Configuration: Inactivity timeout (0 = disabled)
    private val inactivityTimeoutMinutes = 0 // Disabled by default
    
    init {
        Log.d(TAG, "WalletConnectManager init block starting")
        
        // Use GlobalScope to ensure this runs even if something cancels our scope
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG, "Starting WalletKit initialization coroutine in GlobalScope")
            try {
                // Small delay to ensure CoreClient is ready - reduced from 2000ms to 800ms
                // This is a safer middle ground that still provides significant speedup
                kotlinx.coroutines.delay(800)
                Log.d(TAG, "About to call initializeWalletKit()")
                initializeWalletKit()
            } catch (e: Exception) {
                Log.e(TAG, "Exception in init coroutine", e)
            }
        }
        
        Log.d(TAG, "WalletConnectManager init block completed")
    }
    
    private fun initializeWalletKit() {
        try {
            Log.d(TAG, "initializeWalletKit() called")
            if (isInitialized) {
                Log.d(TAG, "WalletKit already initialized")
                return
            }
            
            Log.d(TAG, "Initializing WalletKit (CoreClient already initialized by Application)")
            
            // CoreClient is already initialized by WmApplication, just initialize WalletKit
            val initParams = Wallet.Params.Init(core = CoreClient)
            WalletKit.initialize(initParams) { error ->
                Log.e(TAG, "WalletKit initialization error: ${error.throwable.message}", error.throwable)
            }
            
            // Set up WalletKit delegates
            setupWalletKitDelegates()
            
            isInitialized = true
            
            // Mark as ready immediately - WalletKit will handle connection state internally
            isWalletKitReady = true
            Log.d(TAG, "WalletKit initialized and ready")
            
            // Load existing sessions
            scope.launch {
                loadActiveSessions()
            }
            
            Log.d(TAG, "WalletKit initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WalletConnect", e)
        }
    }
    
    private fun setupWalletKitDelegates() {
        val walletDelegate = object : WalletKit.WalletDelegate {
            override fun onSessionProposal(sessionProposal: Wallet.Model.SessionProposal, verifyContext: Wallet.Model.VerifyContext) {
                Log.d(TAG, "Session proposal received from: ${sessionProposal.name}")
                Log.d(TAG, "Proposal details - URL: ${sessionProposal.url}, Chains: ${sessionProposal.requiredNamespaces}")
                
                // Check if this is a duplicate of the same proposal
                if (lastProposalPublicKey == sessionProposal.proposerPublicKey) {
                    Log.d(TAG, "Duplicate proposal detected, ignoring")
                    return
                }
                
                // Check if we already have a different proposal in progress
                val currentState = _connectionState.value
                if (currentState is ConnectionState.ProposalReceived && 
                    currentState.proposal.proposerPublicKey != sessionProposal.proposerPublicKey) {
                    Log.w(TAG, "Already have a different proposal in progress, rejecting new one")
                    rejectSession(sessionProposal, "Another proposal is being processed")
                    return
                }
                
                // Store this proposal's public key to detect duplicates
                lastProposalPublicKey = sessionProposal.proposerPublicKey
                _connectionState.value = ConnectionState.ProposalReceived(sessionProposal)
            }
            
            override fun onSessionRequest(sessionRequest: Wallet.Model.SessionRequest, verifyContext: Wallet.Model.VerifyContext) {
                Log.d(TAG, "Session request received: ${sessionRequest.request.method}")
                
                // Update activity timestamp and reset failed checks counter
                lastActivityTime = System.currentTimeMillis()
                consecutiveFailedHealthChecks = 0
                
                val request = SessionRequest(
                    topic = sessionRequest.topic,
                    requestId = sessionRequest.request.id,
                    method = sessionRequest.request.method,
                    params = sessionRequest.request.params,
                    chainId = sessionRequest.chainId ?: ""
                )
                
                _sessionRequests.value = _sessionRequests.value + request
            }
            
            override fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete) {
                Log.d(TAG, "Session deleted")
                
                // The SessionDelete doesn't provide topic directly, so we need to reload active sessions
                // and check if our current session still exists
                scope.launch(Dispatchers.IO) {
                    try {
                        val activeSessions = WalletKit.getListOfActiveSessions()
                        val activeTopics = activeSessions.map { it.topic }
                        
                        // Remove deleted sessions from our state
                        _activeSessions.value = _activeSessions.value.filter { it.topic in activeTopics }
                        
                        // If current session was deleted, update connection state
                        if (currentTopic != null && currentTopic !in activeTopics) {
                            _connectionState.value = ConnectionState.Disconnected
                            currentTopic = null
                            lastProposalPublicKey = null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update sessions after delete", e)
                    }
                }
            }
            
            override fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse) {
                Log.d(TAG, "Session settled")
                when (settleSessionResponse) {
                    is Wallet.Model.SettledSessionResponse.Result -> {
                        val session = settleSessionResponse.session
                        currentTopic = session.topic
                        lastActivityTime = System.currentTimeMillis()
                        lastProposalPublicKey = null
                        _connectionState.value = ConnectionState.Connected(session.topic)
                        
                        // Add to active sessions
                        val peerName = session.metaData?.name?.takeIf { it.isNotBlank() }
                            ?: extractDomainFromUrl(session.metaData?.url ?: "")
                            ?: "Unknown dApp"
                        
                        val activeSession = ActiveSession(
                            topic = session.topic,
                            peerName = peerName,
                            peerUrl = session.metaData?.url ?: "",
                            peerIcon = session.metaData?.icons?.firstOrNull() ?: "",
                            accounts = session.namespaces.values.flatMap { it.accounts }
                        )
                        Log.d(TAG, "Adding session to active sessions: ${activeSession.peerName}")
                        _activeSessions.value = _activeSessions.value + activeSession
                        Log.d(TAG, "Active sessions count after adding: ${_activeSessions.value.size}")
                    }
                    is Wallet.Model.SettledSessionResponse.Error -> {
                        Log.e(TAG, "Session settlement error: ${settleSessionResponse.errorMessage}")
                        lastProposalPublicKey = null
                        _connectionState.value = ConnectionState.Error(settleSessionResponse.errorMessage)
                    }
                }
            }
            
            override fun onSessionExtend(session: Wallet.Model.Session) {
                Log.d(TAG, "Session extended: ${session.topic}")
            }
            
            override fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse) {
                Log.d(TAG, "Session updated")
            }
            
            override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {
                Log.d(TAG, "Connection state changed: ${state.isAvailable}")
                
                // If connection is lost and we have an active session, verify it's still valid
                if (!state.isAvailable && currentTopic != null) {
                    Log.w(TAG, "Connection lost while session was active - verifying session")
                    scope.launch(Dispatchers.IO) {
                        forceDisconnectIfStale()
                    }
                }
            }
            
            override fun onError(error: Wallet.Model.Error) {
                Log.e(TAG, "WalletKit error: ${error.throwable.message}", error.throwable)
                // Don't show connectivity errors, database errors, or unsupported flow errors in UI
                val errorMsg = error.throwable.message ?: "Unknown error"
                if (!errorMsg.contains("Connectivity error", ignoreCase = true) &&
                    !errorMsg.contains("Batch subscribe", ignoreCase = true) &&
                    !errorMsg.contains("UNIQUE constraint", ignoreCase = true) &&
                    !errorMsg.contains("SQLiteConstraintException", ignoreCase = true) &&
                    !errorMsg.contains("No proposal or pending session authenticate", ignoreCase = true) &&
                    !errorMsg.contains("NullPointerException", ignoreCase = true)) {
                    _connectionState.value = ConnectionState.Error(errorMsg)
                }
            }
        }
        
        WalletKit.setWalletDelegate(walletDelegate)
    }
    
    fun isReady(): Boolean = isWalletKitReady
    
    fun pair(uri: String) {
        try {
            if (!isWalletKitReady) {
                Log.w(TAG, "WalletKit not fully ready yet, pairing may fail")
            }
            Log.d(TAG, "Attempting to pair with URI: $uri")
            val pairingParams = Core.Params.Pair(uri)
            
            CoreClient.Pairing.pair(pairingParams) { error ->
                Log.e(TAG, "Pairing error: ${error.throwable.message}")
                _connectionState.value = ConnectionState.Error(error.throwable.message ?: "Pairing failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pair", e)
            _connectionState.value = ConnectionState.Error(e.message ?: "Failed to pair")
        }
    }
    
    fun approveSession(proposal: Wallet.Model.SessionProposal, accounts: List<String>) {
        try {
            Log.d(TAG, "Approving session with accounts: $accounts")
            
            // Build session namespaces from required namespaces
            val sessionNamespaces = mutableMapOf<String, Wallet.Model.Namespace.Session>()
            
            // Handle required namespaces
            proposal.requiredNamespaces.forEach { (key, namespace) ->
                val chains = namespace.chains ?: listOf()
                sessionNamespaces[key] = Wallet.Model.Namespace.Session(
                    chains = chains,
                    accounts = accounts,
                    methods = namespace.methods,
                    events = namespace.events
                )
            }
            
            // Optionally handle optional namespaces
            proposal.optionalNamespaces?.forEach { (key, namespace) ->
                if (!sessionNamespaces.containsKey(key)) {
                    val chains = namespace.chains ?: listOf()
                    sessionNamespaces[key] = Wallet.Model.Namespace.Session(
                        chains = chains,
                        accounts = accounts,
                        methods = namespace.methods,
                        events = namespace.events
                    )
                }
            }
            
            if (sessionNamespaces.isEmpty()) {
                Log.e(TAG, "Session namespaces are empty - cannot approve")
                _connectionState.value = ConnectionState.Error("No namespaces to approve")
                return
            }
            
            val approveParams = Wallet.Params.SessionApprove(
                proposerPublicKey = proposal.proposerPublicKey,
                namespaces = sessionNamespaces
            )
            
            WalletKit.approveSession(approveParams) { error ->
                Log.e(TAG, "Session approval error: ${error.throwable.message}", error.throwable)
                _connectionState.value = ConnectionState.Error(error.throwable.message ?: "Approval failed")
            }
            
            Log.d(TAG, "Session approval sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to approve session", e)
            _connectionState.value = ConnectionState.Error(e.message ?: "Failed to approve session")
        }
    }
    
    fun rejectSession(proposal: Wallet.Model.SessionProposal, reason: String = "User rejected") {
        try {
            val rejectParams = Wallet.Params.SessionReject(
                proposerPublicKey = proposal.proposerPublicKey,
                reason = reason
            )
            
            WalletKit.rejectSession(rejectParams) { error ->
                Log.e(TAG, "Session rejection error: ${error.throwable.message}")
            }
            
            lastProposalPublicKey = null
            _connectionState.value = ConnectionState.Disconnected
            Log.d(TAG, "Session rejected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject session", e)
        }
    }
    
    fun respondToRequest(topic: String, requestId: Long, result: String) {
        try {
            val response = Wallet.Params.SessionRequestResponse(
                sessionTopic = topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                    id = requestId,
                    result = result
                )
            )
            
            WalletKit.respondSessionRequest(response) { error ->
                Log.e(TAG, "Request response error: ${error.throwable.message}")
            }
            
            // Remove the request from the list
            _sessionRequests.value = _sessionRequests.value.filter { 
                !(it.topic == topic && it.requestId == requestId)
            }
            
            Log.d(TAG, "Request response sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to respond to request", e)
        }
    }
    
    fun rejectRequest(topic: String, requestId: Long, message: String = "User rejected") {
        try {
            val response = Wallet.Params.SessionRequestResponse(
                sessionTopic = topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcError(
                    id = requestId,
                    code = 5000,
                    message = message
                )
            )
            
            WalletKit.respondSessionRequest(response) { error ->
                Log.e(TAG, "Request rejection error: ${error.throwable.message}")
            }
            
            // Remove the request from the list
            _sessionRequests.value = _sessionRequests.value.filter { 
                !(it.topic == topic && it.requestId == requestId)
            }
            
            Log.d(TAG, "Request rejected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject request", e)
        }
    }
    
    fun disconnect(topic: String? = null) {
        try {
            val targetTopic = topic ?: currentTopic
            targetTopic?.let { topicValue ->
                val disconnectParams = Wallet.Params.SessionDisconnect(topicValue)
                WalletKit.disconnectSession(disconnectParams) { error ->
                    Log.e(TAG, "Disconnect error: ${error.throwable.message}")
                }
                
                // Remove from active sessions
                _activeSessions.value = _activeSessions.value.filter { it.topic != topicValue }
                
                // Update state if this was the current session
                if (currentTopic == topicValue) {
                    _connectionState.value = ConnectionState.Disconnected
                    currentTopic = null
                    lastProposalPublicKey = null
                }
            }
            
            Log.d(TAG, "Disconnected from topic: $targetTopic")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect", e)
        }
    }
    
    /**
     * Disconnect all active sessions
     */
    fun disconnectAllSessions() {
        try {
            val activeSessions = WalletKit.getListOfActiveSessions()
            Log.d(TAG, "Disconnecting ${activeSessions.size} active session(s)")
            
            activeSessions.forEach { session ->
                try {
                    val disconnectParams = Wallet.Params.SessionDisconnect(session.topic)
                    WalletKit.disconnectSession(disconnectParams) { error ->
                        Log.e(TAG, "Error disconnecting session ${session.topic}: ${error.throwable.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to disconnect session ${session.topic}", e)
                }
            }
            
            _connectionState.value = ConnectionState.Disconnected
            _activeSessions.value = emptyList()
            currentTopic = null
            _sessionRequests.value = emptyList()
            Log.d(TAG, "All sessions disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect all sessions", e)
        }
    }
    
    /**
     * Get the count of active sessions
     */
    fun getActiveSessionCount(): Int {
        return try {
            WalletKit.getListOfActiveSessions().size
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get active sessions", e)
            0
        }
    }
    
    /**
     * Load existing active sessions into state
     */
    private fun loadActiveSessions() {
        try {
            val sessions = WalletKit.getListOfActiveSessions()
            Log.d(TAG, "Loaded ${sessions.size} active session(s)")
            
            val activeSessions = sessions.map { session ->
                val peerName = session.metaData?.name?.takeIf { it.isNotBlank() }
                    ?: extractDomainFromUrl(session.metaData?.url ?: "")
                    ?: "Unknown dApp"
                
                ActiveSession(
                    topic = session.topic,
                    peerName = peerName,
                    peerUrl = session.metaData?.url ?: "",
                    peerIcon = session.metaData?.icons?.firstOrNull() ?: "",
                    accounts = session.namespaces.values.flatMap { it.accounts }
                )
            }
            
            _activeSessions.value = activeSessions
            
            // If we have sessions but no current topic, set the first one as current
            if (activeSessions.isNotEmpty() && currentTopic == null) {
                currentTopic = activeSessions.first().topic
                _connectionState.value = ConnectionState.Connected(activeSessions.first().topic)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load active sessions", e)
        }
    }
    
    /**
     * Extract a friendly domain name from a URL
     * E.g., "https://zapper.xyz" -> "zapper.xyz"
     */
    private fun extractDomainFromUrl(url: String): String? {
        return try {
            if (url.isBlank()) return null
            
            // Remove protocol
            val withoutProtocol = url.replace(Regex("^https?://"), "")
            
            // Remove path and query params
            val domain = withoutProtocol.split("/", "?", "#").firstOrNull() ?: return null
            
            // Return the domain
            domain.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract domain from URL: $url", e)
            null
        }
    }
    
    /**
     * Check if the current session is still active
     */
    fun checkSessionHealth() {
        scope.launch(Dispatchers.IO) {
            try {
                val topic = currentTopic ?: return@launch
                
                // Check if the session exists in the active sessions list
                val activeSessions = WalletKit.getListOfActiveSessions()
                val sessionExists = activeSessions.any { it.topic == topic }
                
                if (!sessionExists) {
                    Log.w(TAG, "Current session no longer exists - cleaning up")
                    _connectionState.value = ConnectionState.Disconnected
                    currentTopic = null
                    _sessionRequests.value = emptyList()
                    lastActivityTime = 0L
                    consecutiveFailedHealthChecks = 0
                    return@launch
                }
                
                Log.d(TAG, "Session health check passed - session active")
                consecutiveFailedHealthChecks = 0
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check session health", e)
                consecutiveFailedHealthChecks++
            }
        }
    }
    
    /**
     * Force disconnect the current session if it appears stale
     */
    private fun forceDisconnectIfStale() {
        try {
            val topic = currentTopic ?: return
            
            Log.d(TAG, "Force checking session staleness")
            
            // Get all active sessions
            val activeSessions = WalletKit.getListOfActiveSessions()
            
            // If no active sessions at all, definitely disconnect
            if (activeSessions.isEmpty()) {
                Log.w(TAG, "No active sessions found - force disconnecting")
                _connectionState.value = ConnectionState.Disconnected
                currentTopic = null
                _sessionRequests.value = emptyList()
                lastActivityTime = 0L
                return
            }
            
            // Check if our session is in the list
            val ourSession = activeSessions.find { it.topic == topic }
            if (ourSession == null) {
                Log.w(TAG, "Current session not in active list - force disconnecting")
                _connectionState.value = ConnectionState.Disconnected
                currentTopic = null
                _sessionRequests.value = emptyList()
                lastActivityTime = 0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to force check staleness", e)
        }
    }
    
    companion object {
        private const val TAG = "WalletConnectManager"
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    data class ProposalReceived(val proposal: Wallet.Model.SessionProposal) : ConnectionState()
    data class Connected(val topic: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

data class SessionRequest(
    val topic: String,
    val requestId: Long,
    val method: String,
    val params: String,
    val chainId: String
)

data class ActiveSession(
    val topic: String,
    val peerName: String,
    val peerUrl: String,
    val peerIcon: String,
    val accounts: List<String>
)

