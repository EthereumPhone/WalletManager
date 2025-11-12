package com.core.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.core.data.utils.GasEstimationHelper
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ethereumphone.walletsdk.WalletSDK
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
import javax.inject.Inject
import com.reown.walletkit.client.Wallet
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Foreground service that manages WalletConnect sessions.
 * Handles automatic connection approval and request processing.
 * Shows a persistent notification when connected to dApps.
 */
@AndroidEntryPoint
class WalletConnectService : Service() {
    
    @Inject
    @JvmField
    var walletSDK: WalletSDK? = null
    
    private lateinit var walletConnectManager: WalletConnectManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    private var walletAddress: String = ""
    private var isWalletReady = false
    private var hasSessions = false // Track if we've ever had sessions
    private var serviceStartTime = 0L // Track when service started
    
    // Jobs for flow collection
    private var connectionStateJob: Job? = null
    private var sessionRequestsJob: Job? = null
    private var activeSessionsJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        serviceStartTime = System.currentTimeMillis()
        Log.d(TAG, "WalletConnectService onCreate")
        
        try {
            // CRITICAL: Start foreground IMMEDIATELY - before anything else!
            // Don't wait for notification channels, create a basic notification first
            val initialNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android O+, we need a channel, but we'll create it inline for speed
                try {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "WalletConnect Sessions",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        setShowBadge(false)
                        enableVibration(false)
                        enableLights(false)
                    }
                    notificationManager.createNotificationChannel(channel)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create channel, notification may not work", e)
                }
                
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("WalletConnect")
                    .setContentText("Initializing...")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()
            } else {
                // For pre-O, no channel needed
                @Suppress("DEPRECATION")
                NotificationCompat.Builder(this)
                    .setContentTitle("WalletConnect")
                    .setContentText("Initializing...")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()
            }
            
            // Call startForeground IMMEDIATELY
            startForeground(NOTIFICATION_ID, initialNotification)
            Log.d(TAG, "Started foreground service (${System.currentTimeMillis() - serviceStartTime}ms)")
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to start foreground service", e)
            // Even if we fail, try to call startForeground with a minimal notification
            try {
                @Suppress("DEPRECATION")
                val emergencyNotification = NotificationCompat.Builder(this)
                    .setContentTitle("WalletConnect")
                    .setContentText("Starting...")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build()
                startForeground(NOTIFICATION_ID, emergencyNotification)
            } catch (e2: Exception) {
                Log.e(TAG, "Even emergency startForeground failed", e2)
            }
            stopSelf()
            return
        }
        
        // Now that we're safely in foreground, create the error notification channel
        try {
            createErrorNotificationChannel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create error notification channel", e)
        }
        
        // Now do the heavy initialization work in a try-catch to not crash the service
        try {
            // Initialize WalletConnect manager
            // CoreClient is already initialized by WmApplication and had time to connect
            Log.d(TAG, "Creating WalletConnectManager...")
            walletConnectManager = WalletConnectManager(application)
        
        // Wait for WalletConnectManager to initialize (optimized)
        serviceScope.launch {
            Log.d(TAG, "Waiting for WalletConnectManager to initialize...")
            var attempts = 0
            // Reduced polling interval from 500ms to 100ms and max wait from 20s to 5s
            while (!walletConnectManager.isReady() && attempts < 50) {
                kotlinx.coroutines.delay(100)
                attempts++
                if (attempts % 10 == 0) {
                    Log.d(TAG, "Still waiting for WalletConnectManager... (${attempts * 100}ms elapsed)")
                }
            }
            if (walletConnectManager.isReady()) {
                Log.d(TAG, "WalletConnectManager is ready after ${attempts * 100}ms")
            } else {
                Log.w(TAG, "WalletConnectManager not ready after ${attempts * 100}ms")
            }
        }
        
        // Initialize wallet address
        serviceScope.launch(Dispatchers.IO) {
            try {
                walletSDK?.let { sdk ->
                    walletAddress = sdk.getAddress()
                    isWalletReady = true
                    Log.d(TAG, "Wallet address initialized: $walletAddress")
                } ?: run {
                    Log.w(TAG, "WalletSDK is null (probably running on emulator)")
                    isWalletReady = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get wallet address", e)
            }
        }
        
        // Observe connection state
        observeConnectionState()
        
        // Observe session requests
        observeSessionRequests()
        
        // Observe active sessions
        observeActiveSessions()
        Log.d(TAG, "All observers set up")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WalletConnectService", e)
            // Service is already in foreground, so it won't crash
            // But we should handle the error gracefully
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WalletConnectService onStartCommand")
        
        intent?.let { handleIntent(it) }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null // This is a started service, not a bound service
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "WalletConnectService onDestroy")
        
        // Cancel all flow collection jobs
        connectionStateJob?.cancel()
        sessionRequestsJob?.cancel()
        activeSessionsJob?.cancel()
        
        // Cancel the service scope
        serviceScope.cancel()
        
        // Optionally disconnect all sessions when service is destroyed
        // walletConnectManager.disconnectAllSessions()
    }
    
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PAIR -> {
                val uri = intent.getStringExtra(EXTRA_URI)
                if (uri != null) {
                    Log.d(TAG, "Handling pair action with URI: $uri")
                    pairWithUri(uri)
                }
            }
            ACTION_DISCONNECT -> {
                val topic = intent.getStringExtra(EXTRA_TOPIC)
                Log.d(TAG, "Handling disconnect action for topic: $topic")
                if (topic != null) {
                    walletConnectManager.disconnect(topic)
                } else {
                    walletConnectManager.disconnectAllSessions()
                }
            }
            ACTION_DISCONNECT_ALL -> {
                Log.d(TAG, "Handling disconnect all action")
                walletConnectManager.disconnectAllSessions()
            }
        }
    }
    
    private fun pairWithUri(uri: String) {
        serviceScope.launch {
            // Wait for wallet to be ready (optimized)
            var attempts = 0
            while (!isWalletReady && attempts < 30) {
                kotlinx.coroutines.delay(100)
                attempts++
            }
            
            if (!isWalletReady) {
                Log.w(TAG, "Wallet not ready after ${attempts * 100}ms, but proceeding with pairing")
            }
            
            // Wait for WalletKit to be ready (optimized - reduced from 500ms to 100ms intervals)
            attempts = 0
            while (!walletConnectManager.isReady() && attempts < 50) {
                kotlinx.coroutines.delay(100)
                attempts++
            }
            
            if (!walletConnectManager.isReady()) {
                Log.w(TAG, "WalletKit may not be fully ready after ${attempts * 100}ms, but proceeding with pairing")
            }
            
            Log.d(TAG, "Initiating WalletConnect pairing")
            walletConnectManager.pair(uri)
        }
    }
    
    private fun observeConnectionState() {
        connectionStateJob = walletConnectManager.connectionState
            .onEach { state ->
                Log.d(TAG, "Connection state changed: $state")
                
                when (state) {
                    is ConnectionState.ProposalReceived -> {
                        handleSessionProposal(state.proposal)
                    }
                    is ConnectionState.Connected -> {
                        // Session connected, notification will be updated by observeActiveSessions
                        Log.d(TAG, "Session connected: ${state.topic}")
                    }
                    is ConnectionState.Disconnected -> {
                        // Notification will be handled by observeActiveSessions based on remaining sessions
                        Log.d(TAG, "Session disconnected")
                    }
                    is ConnectionState.Error -> {
                        Log.e(TAG, "Connection error: ${state.message}")
                        showErrorNotification("WalletConnect Error", state.message)
                    }
                }
            }
            .launchIn(serviceScope)
    }
    
    private fun observeSessionRequests() {
        sessionRequestsJob = walletConnectManager.sessionRequests
            .onEach { requests ->
                Log.d(TAG, "Session requests updated: ${requests.size} request(s)")
                
                // Automatically handle each request
                requests.forEach { request ->
                    handleSessionRequest(request)
                }
            }
            .launchIn(serviceScope)
    }
    
    private fun observeActiveSessions() {
        activeSessionsJob = walletConnectManager.activeSessions
            .onEach { sessions ->
                Log.d(TAG, "Active sessions flow update: ${sessions.size} session(s)")
                sessions.forEach { session ->
                    Log.d(TAG, "  - Session: ${session.peerName} (${session.topic})")
                }
                
                updateSessionNotifications(sessions)
            }
            .launchIn(serviceScope)
    }

    suspend fun getTxHashForUserOp(bundlerRPC: String, userOpHash: String, chainId: Int): String {
        val client = OkHttpClient()
        var attempts = 0

        while (attempts < 10) {
            try {
                val requestBody = """
                {
                    "jsonrpc": "2.0",
                    "method": "pimlico_getUserOperationStatus",
                    "params": ["$userOpHash"],
                    "id": 1
                }
                """.trimIndent()

                val url = bundlerRPC

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("API request failed with response code: ${response.code}")
                    }

                    val jsonResponse = response.body?.string() ?: throw Exception("Empty response body")
                    val jsonElement = JsonParser.parseString(jsonResponse)

                    // Check for JSON-RPC error
                    val error = jsonElement.asJsonObject.get("error")
                    if (error != null && !error.isJsonNull) {
                        throw Exception("JSON-RPC error: ${error.asJsonObject.get("message")?.asString ?: "Unknown error"}")
                    }

                    val result = jsonElement.asJsonObject.get("result")
                    if (result == null || result.isJsonNull) {
                        throw Exception("No result in response")
                    }

                    val resultObj = result.asJsonObject
                    val status = resultObj.get("status")?.asString ?: throw Exception("No status in response")

                    when (status) {
                        "submitted", "included" -> {
                            val transactionHash = resultObj.get("transactionHash")?.asString
                            if (transactionHash != null) {
                                return transactionHash
                            } else {
                                throw Exception("Transaction hash not found in response despite status: $status")
                            }
                        }
                        "not_found" -> {
                            attempts++
                            if (attempts >= 3) {
                                throw Exception("UserOp not found after 3 attempts")
                            }
                            // Wait for 3 seconds before retrying
                            withContext(Dispatchers.IO) {
                                Thread.sleep(3000)
                            }
                        }
                        "failed", "rejected" -> {
                            throw Exception("UserOp failed with status: $status")
                        }
                        else -> {
                            throw Exception("Unknown or unsupported status: $status")
                        }
                    }
                }
            } catch (e: Exception) {
                if (attempts >= 10) {  // On the third attempt, throw the error
                    throw Exception("Failed to get transaction hash for UserOp: ${e.message}", e)
                }
                attempts++
                // Wait before retrying on error
                withContext(Dispatchers.IO) {
                    Thread.sleep(3000)
                }
            }
        }

        throw Exception("Failed to get transaction hash after maximum attempts")
    }
    
    private fun handleSessionProposal(proposal: Wallet.Model.SessionProposal) {
        serviceScope.launch {
            val displayName = proposal.name.takeIf { it.isNotBlank() } ?: proposal.url
            Log.d(TAG, "Auto-approving session from $displayName")
            
            // Format accounts with chain ID in CAIP-10 format
            val accounts = mutableListOf<String>()
            val normalizedAddress = walletAddress.lowercase()
            
            proposal.requiredNamespaces.forEach { (namespace, proposalNamespace) ->
                val chains = proposalNamespace.chains
                if (chains != null && chains.isNotEmpty()) {
                    chains.forEach { chain ->
                        accounts.add("$chain:$normalizedAddress")
                    }
                } else {
                    when (namespace) {
                        "eip155" -> {
                            accounts.add("eip155:1:$normalizedAddress")
                            accounts.add("eip155:5:$normalizedAddress")
                            accounts.add("eip155:137:$normalizedAddress")
                            accounts.add("eip155:42161:$normalizedAddress")
                        }
                        else -> accounts.add("$namespace:1:$normalizedAddress")
                    }
                }
            }
            
            proposal.optionalNamespaces?.forEach { (namespace, proposalNamespace) ->
                val chains = proposalNamespace.chains
                if (chains != null && chains.isNotEmpty()) {
                    chains.forEach { chain ->
                        val account = "$chain:$normalizedAddress"
                        if (!accounts.contains(account)) {
                            accounts.add(account)
                        }
                    }
                }
            }
            
            Log.d(TAG, "Auto-approving with accounts: ${accounts.distinct()}")
            walletConnectManager.approveSession(proposal, accounts.distinct())
        }
    }
    
    private fun handleSessionRequest(request: SessionRequest) {
        serviceScope.launch {
            try {
                // Check if WalletSDK is available
                val sdk = walletSDK
                if (sdk == null) {
                    val errorMsg = "WalletSDK is not available (probably running on emulator)"
                    Log.e(TAG, errorMsg)
                    showErrorNotification("Request Failed", errorMsg)
                    walletConnectManager.rejectRequest(request.topic, request.requestId, "WalletSDK not available")
                    return@launch
                }
                
                val chainId = request.chainId.removePrefix("eip155:").toIntOrNull() ?: 1
                Log.d(TAG, "Processing request: ${request.method} on chain $chainId")
                
                // Run all WalletSDK operations on IO dispatcher
                val result = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    // Ensure WalletSDK is on the correct chain
                    Log.d(TAG, "Switching to $chainId")
                    val rpcUrl = getChainRpcUrl(chainId)
                    val bundlerUrl = getBundlerUrl(chainId)

                    val changeResult = sdk.changeChain(chainId, rpcUrl, bundlerUrl)
                    if (changeResult == WalletSDK.DECLINE) {
                        val errorMsg = "User declined chain switch to chain $chainId"
                        Log.w(TAG, errorMsg)
                        showErrorNotification("Chain Switch Declined", errorMsg)
                        walletConnectManager.rejectRequest(request.topic, request.requestId, "User declined chain switch")
                        return@withContext null
                    }
                    when (request.method) {
                        "personal_sign" -> {
                            val message = parsePersonalSignParams(request.params)
                            val signature = sdk.signMessage(message, chainId)
                            signature
                        }
                        "eth_sign" -> {
                            val message = parseEthSignParams(request.params)
                            val signature = sdk.signMessage(message, chainId)
                            signature
                        }
                        "eth_signTypedData", "eth_signTypedData_v4" -> {
                            val typedData = parseTypedDataParams(request.params)
                            val signature = sdk.signMessage(typedData, chainId)
                            signature
                        }
                        "eth_sendTransaction" -> {
                            val tx = parseTransactionParams(request.params)
                            Log.d(TAG, "Sending transaction: to=${tx.to}, value=${tx.value}, data=${tx.data}")
                            
                            val valueInWei = tx.value?.let { hexValue ->
                                try {
                                    if (hexValue.startsWith("0x")) {
                                        BigInteger(hexValue.removePrefix("0x"), 16).toString()
                                    } else {
                                        hexValue
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Failed to parse value: $hexValue", e)
                                    "0"
                                }
                            } ?: "0"
                            
                            val callGas = tx.gas?.let { gasHex ->
                                try {
                                    BigInteger(gasHex.removePrefix("0x"), 16)
                                } catch (e: Exception) {
                                    Log.w(TAG, "Failed to parse gas: $gasHex", e)
                                    null
                                }
                            }
                            
                            val gasProvider: suspend (WalletSDK.UserOperation) -> WalletSDK.GasEstimation = { userOp ->
                                GasEstimationHelper.estimateGas(userOp, rpcUrl)
                            }
                            
                            val userOpHash = sdk.sendTransaction(
                                to = tx.to,
                                value = valueInWei,
                                data = tx.data ?: "0x",
                                callGas = callGas,
                                chainId = chainId,
                                gasProvider = gasProvider
                            )
                            
                            Log.d(TAG, "UserOp submitted with hash: $userOpHash")

                            Log.d(TAG, "Getting tx hash now")
                            // Return plain string; SDK will serialize correctly.
                            val txHash = getTxHashForUserOp(
                                bundlerRPC = bundlerUrl,
                                chainId = chainId,
                                userOpHash = userOpHash
                            )
                            Log.d(TAG, "Got tx hash $txHash")
                            txHash
                        }
                        "wallet_getCapabilities" -> {
                            val capabilities = JSONObject().apply {
                                val smartWalletCapabilities = JSONObject().apply {
                                    put("atomic", JSONObject().apply {
                                        put("supported", true)
                                    })
                                    put("paymasterService", JSONObject().apply {
                                        put("supported", false)
                                    })
                                }
                                
                                // Add capabilities for supported chains
                                put("0x1", JSONObject(smartWalletCapabilities.toString()))
                                put("0xaa36a7", JSONObject(smartWalletCapabilities.toString()))
                                put("0xa", JSONObject(smartWalletCapabilities.toString()))
                                put("0x89", JSONObject(smartWalletCapabilities.toString()))
                                put("0xa4b1", JSONObject(smartWalletCapabilities.toString()))
                                put("0x2105", JSONObject(smartWalletCapabilities.toString()))
                                put("0x14a34", JSONObject(smartWalletCapabilities.toString()))
                            }
                            Log.d(TAG, "Returning smart wallet capabilities")
                            capabilities.toString()
                        }
                        else -> {
                            val errorMsg = "Unsupported method: ${request.method}"
                            Log.w(TAG, errorMsg)
                            showErrorNotification("Unsupported Request", errorMsg)
                            walletConnectManager.rejectRequest(request.topic, request.requestId, "Unsupported method")
                            return@withContext null
                        }
                    }
                }
                
                // Only respond if we have a result
                if (result != null) {
                    walletConnectManager.respondToRequest(request.topic, request.requestId, result)
                    Log.d(TAG, "Successfully processed and responded to ${request.method} with result: $result")
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to process ${request.method}: ${e.message}"
                Log.e(TAG, errorMsg, e)
                showErrorNotification("Request Processing Failed", errorMsg)
                walletConnectManager.rejectRequest(request.topic, request.requestId, "Processing failed: ${e.message}")
            }
        }
    }
    
    private fun createErrorNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID_ERRORS,
                    "WalletConnect Errors",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "WalletConnect connection and operation errors"
                    setShowBadge(true)
                    enableVibration(true)
                    enableLights(true)
                }
                
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Error notification channel created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create error notification channel", e)
        }
    }
    
    private fun updateSessionNotifications(sessions: List<ActiveSession>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Always clear all existing notifications first to avoid orphaned notifications
        notificationManager.cancelAll()
        
        if (sessions.isEmpty()) {
            // Don't stop foreground immediately after starting - Android requires we stay
            // in foreground for a minimum time after calling startForegroundService()
            val timeSinceStart = System.currentTimeMillis() - serviceStartTime
            val minForegroundTime = 5000L // Stay in foreground for at least 5 seconds
            
            if (hasSessions || timeSinceStart > minForegroundTime) {
                // We've had sessions before or enough time has passed, safe to stop foreground
                Log.d(TAG, "No active sessions, stopping foreground and clearing notifications")
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                // Too soon after startup, keep showing initializing notification
                Log.d(TAG, "No active sessions yet, but keeping foreground (${timeSinceStart}ms since start)")
                val initNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("WalletConnect")
                    .setContentText("Ready for connections...")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setOngoing(true)
                    .build()
                startForeground(NOTIFICATION_ID, initNotification)
            }
            return
        }
        
        // We have sessions, remember this
        hasSessions = true
        
        if (sessions.size == 1) {
            // Single session - show simple notification with dApp name
            val session = sessions.first()
            Log.d(TAG, "Single session active: ${session.peerName}")
            val notification = createSessionNotification(session, false)
            startForeground(NOTIFICATION_ID, notification)
        } else {
            // Multiple sessions - use grouped notifications
            Log.d(TAG, "Multiple sessions active (${sessions.size}), creating grouped notifications")
            
            // Create individual notifications for each session
            sessions.forEachIndexed { index, session ->
                val sessionNotification = createSessionNotification(session, true)
                // Use unique ID for each session (starting from 1002 to avoid conflicts)
                notificationManager.notify(1002 + index, sessionNotification)
            }
            
            // Create summary notification
            val summaryNotification = createSummaryNotification(sessions.size)
            startForeground(SUMMARY_NOTIFICATION_ID, summaryNotification)
        }
    }
    
    private fun createSessionNotification(session: ActiveSession, isGrouped: Boolean): Notification {
        // Create intent to open the app
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Create disconnect action for this specific session
        val disconnectIntent = Intent(this, WalletConnectService::class.java).apply {
            action = ACTION_DISCONNECT
            putExtra(EXTRA_TOPIC, session.topic)
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            session.topic.hashCode(), // Use topic hash as unique request code
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(session.peerName)
            .setContentText("Connected to ${session.peerName}")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disconnect",
                disconnectPendingIntent
            )
            .setOngoing(true)
        
        // Add to group if this is part of a grouped notification
        if (isGrouped) {
            builder.setGroup(GROUP_KEY_WALLETCONNECT)
        }
        
        return builder.build()
    }
    
    private fun createSummaryNotification(sessionCount: Int): Notification {
        // Create intent to open the app
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Create disconnect all action
        val disconnectIntent = Intent(this, WalletConnectService::class.java).apply {
            action = ACTION_DISCONNECT_ALL
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val title = if (sessionCount > 0) {
            "Connected to $sessionCount dApp${if (sessionCount > 1) "s" else ""}"
        } else {
            "WalletConnect"
        }
        
        val content = if (sessionCount > 0) {
            "$sessionCount active connection${if (sessionCount > 1) "s" else ""}"
        } else {
            "Starting..."
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY_WALLETCONNECT)
            .setGroupSummary(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disconnect All",
                disconnectPendingIntent
            )
            .setOngoing(true)
            .build()
    }
    
    private fun showErrorNotification(title: String, message: String) {
        Log.d(TAG, "Showing error notification: $title - $message")
        
        // Create intent to open the app
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ERRORS)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
    }
    
    // Helper functions
    private fun getChainRpcUrl(chainId: Int): String {
        val alchemyKey = com.core.data.BuildConfig.ALCHEMY_API
        
        return when (chainId) {
            1 -> "https://eth-mainnet.g.alchemy.com/v2/$alchemyKey"
            5 -> "https://eth-goerli.g.alchemy.com/v2/$alchemyKey"
            11155111 -> "https://eth-sepolia.g.alchemy.com/v2/$alchemyKey"
            10 -> "https://opt-mainnet.g.alchemy.com/v2/$alchemyKey"
            137 -> "https://polygon-mainnet.g.alchemy.com/v2/$alchemyKey"
            42161 -> "https://arb-mainnet.g.alchemy.com/v2/$alchemyKey"
            8453 -> "https://base-mainnet.g.alchemy.com/v2/$alchemyKey"
            84532 -> "https://base-sepolia.g.alchemy.com/v2/$alchemyKey"
            56 -> "https://bnb-mainnet.g.alchemy.com/v2/$alchemyKey"
            43114 -> "https://avax-mainnet.g.alchemy.com/v2/$alchemyKey"
            else -> {
                Log.w(TAG, "Unknown chainId $chainId, using public RPC")
                when (chainId) {
                    8453 -> "https://mainnet.base.org"
                    84532 -> "https://sepolia.base.org"
                    else -> "https://cloudflare-eth.com"
                }
            }
        }
    }
    
    private fun getBundlerUrl(chainId: Int): String {
        val apiKey = com.core.data.BuildConfig.BUNDLER_API
        return "https://api.pimlico.io/v2/$chainId/rpc?apikey=$apiKey"
    }
    
    private fun parsePersonalSignParams(params: String): String {
        return try {
            val regex = """"(0x[a-fA-F0-9]+)"""".toRegex()
            val matches = regex.findAll(params).map { it.groupValues[1] }.toList()
            matches.firstOrNull() ?: params
        } catch (e: Exception) {
            params
        }
    }
    
    private fun parseEthSignParams(params: String): String {
        return parsePersonalSignParams(params)
    }
    
    private fun parseTypedDataParams(params: String): String {
        return try {
            val regex = """"(0x[a-fA-F0-9]+)"""".toRegex()
            val matches = regex.findAll(params).map { it.groupValues[1] }.toList()
            if (matches.size >= 2) {
                matches[1]
            } else {
                params
            }
        } catch (e: Exception) {
            params
        }
    }
    
    data class TransactionParams(
        val to: String,
        val value: String?,
        val data: String?,
        val gas: String?,
        val gasPrice: String?,
        val from: String?
    )
    
    private fun parseTransactionParams(params: String): TransactionParams {
        return try {
            val jsonArray = JSONArray(params)
            val txObject = jsonArray.getJSONObject(0)
            
            TransactionParams(
                to = txObject.optString("to", ""),
                value = if (txObject.has("value")) txObject.getString("value") else "0x0",
                data = if (txObject.has("data")) txObject.getString("data") else "0x",
                gas = if (txObject.has("gas")) txObject.getString("gas") else null,
                gasPrice = if (txObject.has("gasPrice")) txObject.getString("gasPrice") else null,
                from = if (txObject.has("from")) txObject.getString("from") else null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse transaction params: $params", e)
            TransactionParams(
                to = "0x0000000000000000000000000000000000000000",
                value = "0x0",
                data = "0x",
                gas = null,
                gasPrice = null,
                from = null
            )
        }
    }
    
    companion object {
        private const val TAG = "WalletConnectService"
        private const val CHANNEL_ID = "walletconnect_service"
        private const val CHANNEL_ID_ERRORS = "walletconnect_errors"
        private const val NOTIFICATION_ID = 1001
        private const val GROUP_KEY_WALLETCONNECT = "com.walletmanager.WALLETCONNECT_SESSIONS"
        private const val SUMMARY_NOTIFICATION_ID = 1000
        private const val ERROR_NOTIFICATION_ID = 1099
        
        const val ACTION_PAIR = "com.core.data.service.ACTION_PAIR"
        const val ACTION_DISCONNECT = "com.core.data.service.ACTION_DISCONNECT"
        const val ACTION_DISCONNECT_ALL = "com.core.data.service.ACTION_DISCONNECT_ALL"
        const val EXTRA_URI = "extra_uri"
        const val EXTRA_TOPIC = "extra_topic"
        
        /**
         * Start the WalletConnect service
         */
        fun start(context: Context) {
            val intent = Intent(context, WalletConnectService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * Pair with a WalletConnect URI
         */
        fun pair(context: Context, uri: String) {
            val intent = Intent(context, WalletConnectService::class.java).apply {
                action = ACTION_PAIR
                putExtra(EXTRA_URI, uri)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * Disconnect from a specific session
         */
        fun disconnect(context: Context, topic: String) {
            val intent = Intent(context, WalletConnectService::class.java).apply {
                action = ACTION_DISCONNECT
                putExtra(EXTRA_TOPIC, topic)
            }
            context.startService(intent)
        }
        
        /**
         * Disconnect from all sessions
         */
        fun disconnectAll(context: Context) {
            val intent = Intent(context, WalletConnectService::class.java).apply {
                action = ACTION_DISCONNECT_ALL
            }
            context.startService(intent)
        }
    }
}
