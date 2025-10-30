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
    
    // Jobs for flow collection
    private var connectionStateJob: Job? = null
    private var sessionRequestsJob: Job? = null
    private var activeSessionsJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WalletConnectService onCreate")
        
        // Initialize WalletConnect manager
        // CoreClient is already initialized by WmApplication and had time to connect
        Log.d(TAG, "Creating WalletConnectManager...")
        walletConnectManager = WalletConnectManager(application)
        
        // Wait for WalletConnectManager to initialize
        serviceScope.launch {
            Log.d(TAG, "Waiting for WalletConnectManager to initialize...")
            var attempts = 0
            while (!walletConnectManager.isReady() && attempts < 40) {
                kotlinx.coroutines.delay(500)
                attempts++
                if (attempts % 6 == 0) {
                    Log.d(TAG, "Still waiting for WalletConnectManager... (${attempts * 500}ms elapsed)")
                }
            }
            if (walletConnectManager.isReady()) {
                Log.d(TAG, "WalletConnectManager is ready after ${attempts * 500}ms")
            } else {
                Log.w(TAG, "WalletConnectManager not ready after ${attempts * 500}ms")
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
        
        // Create notification channel
        createNotificationChannel()
        
        // Start foreground service with initial notification
        startForeground(NOTIFICATION_ID, createNotification("WalletConnect", "Ready to connect"))
        
        // Observe connection state
        observeConnectionState()
        
        // Observe session requests
        observeSessionRequests()
        
        // Observe active sessions
        observeActiveSessions()
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
            // Wait for wallet to be ready
            var attempts = 0
            while (!isWalletReady && attempts < 30) {
                kotlinx.coroutines.delay(100)
                attempts++
            }
            
            if (!isWalletReady) {
                Log.w(TAG, "Wallet not ready after ${attempts * 100}ms, but proceeding with pairing")
            }
            
            // Wait for WalletKit to be ready
            attempts = 0
            while (!walletConnectManager.isReady() && attempts < 40) {
                kotlinx.coroutines.delay(500)
                attempts++
            }
            
            if (!walletConnectManager.isReady()) {
                Log.w(TAG, "WalletKit may not be fully ready after ${attempts * 500}ms, but proceeding with pairing")
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
                        updateNotification("WalletConnect", "Ready to connect")
                    }
                    is ConnectionState.Error -> {
                        Log.e(TAG, "Connection error: ${state.message}")
                        updateNotification("WalletConnect", "Error: ${state.message}")
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
                Log.d(TAG, "Active sessions updated: ${sessions.size} session(s)")
                
                if (sessions.isNotEmpty()) {
                    // Update notification to show connected dApps
                    val sessionNames = sessions.joinToString(", ") { it.peerName }
                    updateNotification(
                        "Connected to ${sessions.size} dApp(s)",
                        sessionNames
                    )
                } else {
                    updateNotification("WalletConnect", "Ready to connect")
                }
            }
            .launchIn(serviceScope)
    }
    
    private fun handleSessionProposal(proposal: Wallet.Model.SessionProposal) {
        serviceScope.launch {
            Log.d(TAG, "Auto-approving session from ${proposal.name}")
            
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
                    Log.e(TAG, "WalletSDK is not available (probably running on emulator)")
                    walletConnectManager.rejectRequest(request.topic, request.requestId, "WalletSDK not available")
                    return@launch
                }
                
                val chainId = request.chainId.removePrefix("eip155:").toIntOrNull() ?: 1
                Log.d(TAG, "Processing request: ${request.method} on chain $chainId")
                
                // Run all WalletSDK operations on IO dispatcher
                val result = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    // Ensure WalletSDK is on the correct chain
                    val currentChainId = sdk.getChainId()
                    if (currentChainId != chainId) {
                        Log.d(TAG, "Switching from chain $currentChainId to $chainId")
                        val rpcUrl = getChainRpcUrl(chainId)
                        val bundlerUrl = getBundlerUrl(chainId)
                        
                        val changeResult = sdk.changeChain(chainId, rpcUrl, bundlerUrl)
                        if (changeResult == WalletSDK.DECLINE) {
                            Log.w(TAG, "User declined chain switch")
                            walletConnectManager.rejectRequest(request.topic, request.requestId, "User declined chain switch")
                            return@withContext null
                        }
                        
                        Log.d(TAG, "Switched to chain $chainId")
                    }
                    
                    when (request.method) {
                        "personal_sign" -> {
                            val message = parsePersonalSignParams(request.params)
                            sdk.signMessage(message, chainId)
                        }
                        "eth_sign" -> {
                            val message = parseEthSignParams(request.params)
                            sdk.signMessage(message, chainId)
                        }
                        "eth_signTypedData", "eth_signTypedData_v4" -> {
                            val typedData = parseTypedDataParams(request.params)
                            sdk.signMessage(typedData, chainId)
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
                            
                            val gasProvider: suspend (WalletSDK.UserOperation) -> WalletSDK.GasEstimation = { _ ->
                                WalletSDK.GasEstimation(
                                    preVerificationGas = BigInteger.valueOf(70000),
                                    verificationGasLimit = BigInteger.valueOf(400000),
                                    callGasLimit = BigInteger.valueOf(200000)
                                )
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
                            userOpHash
                        }
                        "wallet_getCapabilities" -> {
                            val capabilities = JSONObject().apply {
                                val smartWalletCapabilities = JSONObject().apply {
                                    put("atomic", JSONObject().apply {
                                        put("supported", "supported")
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
                            Log.w(TAG, "Unsupported method: ${request.method}")
                            walletConnectManager.rejectRequest(request.topic, request.requestId, "Unsupported method")
                            return@withContext null
                        }
                    }
                }
                
                // Only respond if we have a result
                if (result != null) {
                    walletConnectManager.respondToRequest(request.topic, request.requestId, result)
                    Log.d(TAG, "Successfully processed and responded to ${request.method}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process request ${request.method}: ${e.message}", e)
                walletConnectManager.rejectRequest(request.topic, request.requestId, "Processing failed: ${e.message}")
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WalletConnect",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "WalletConnect session status"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(title: String, content: String): Notification {
        // Create intent to open the app
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Create disconnect action
        val disconnectIntent = Intent(this, WalletConnectService::class.java).apply {
            action = ACTION_DISCONNECT_ALL
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with proper icon
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disconnect",
                disconnectPendingIntent
            )
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(title: String, content: String) {
        val notification = createNotification(title, content)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
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
        private const val NOTIFICATION_ID = 1001
        
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
