package com.feature.send

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.NftRepository
import com.core.data.repository.SendRepository
import com.core.data.repository.TerminalEvent
import com.core.data.repository.TerminalRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainToApiKey
import com.core.model.NFT
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalSDK
import com.feature.send.ui.TransactionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import javax.inject.Inject

/**
 * ViewModel for the Send NFT Screen
 */
@HiltViewModel
class SendNftViewModel @Inject constructor(
    private val nftRepository: NftRepository,
    private val sendRepository: SendRepository,
    private val userDataRepository: UserDataRepository,
    private val terminalRepository: TerminalRepository,
    private val terminalSDK: TerminalSDK?,
    private val reflectiveLedPattern: ReflectiveLedPattern?,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _nft = MutableStateFlow<NFT?>(null)
    val nft: StateFlow<NFT?> = _nft.asStateFlow()

    private val _recipientUiState = MutableStateFlow(RecipientUiState())
    val recipientUiState: StateFlow<RecipientUiState> = _recipientUiState.asStateFlow()

    private val _qrScannerTriggered = MutableStateFlow(false)
    val qrScannerTriggered: StateFlow<Boolean> = _qrScannerTriggered.asStateFlow()

    private val _transactionStatus = MutableStateFlow<TransactionStatus?>(null)
    val transactionStatus: StateFlow<TransactionStatus?> = _transactionStatus.asStateFlow()

    private val _shouldDismissKeyboard = MutableStateFlow(false)
    val shouldDismissKeyboard: StateFlow<Boolean> = _shouldDismissKeyboard.asStateFlow()

    init {
        // Observe terminal events
        viewModelScope.launch {
            terminalRepository.events.collect { event ->
                when (event) {
                    TerminalEvent.SendTapped -> sendNft()
                    else -> triggerQrScanner()
                }
            }
        }
    }

    /**
     * Load NFT details from repository
     */
    fun loadNft(contractAddress: String, tokenId: String, chainId: Int) {
        viewModelScope.launch {
            val loadedNft = nftRepository.getNft(contractAddress, tokenId, chainId)
            _nft.value = loadedNft
            Log.d("SendNftViewModel", "Loaded NFT: $loadedNft")
        }
    }

    /**
     * Update recipient address
     */
    fun updateAddress(address: String) {
        _recipientUiState.update { it.copy(recipientAddress = address, ensError = "") }
        resolveEns()
    }

    /**
     * Resolve ENS name to address
     */
    private fun resolveEns() {
        viewModelScope.launch {
            val address = recipientUiState.value.recipientAddress.lowercase()

            if (address.endsWith(".eth") && ENSName(address).isPotentialENSDomain()) {
                _recipientUiState.update { it.copy(isResolving = true, ensError = "") }

                try {
                    val resolvedAddress = withContext(Dispatchers.IO) {
                        val ens = ENS(
                            HttpEthereumRPC(
                                "https://eth-mainnet.g.alchemy.com/v2/${chainToApiKey("eth-mainnet")}"
                            )
                        )
                        ens.getAddress(ENSName(address))
                    }

                    if (resolvedAddress != null) {
                        _recipientUiState.update { it.copy(recipientAddress = resolvedAddress.hex) }
                        _shouldDismissKeyboard.value = true
                    } else {
                        _recipientUiState.update { it.copy(ensError = "ENS name not found") }
                    }
                } catch (e: Exception) {
                    Log.e("SendNftViewModel", "Failed to resolve ENS", e)
                    _recipientUiState.update { it.copy(ensError = "Failed to resolve ENS") }
                } finally {
                    _recipientUiState.update { it.copy(isResolving = false) }
                }
            }
        }
    }

    /**
     * Send the NFT
     */
    fun sendNft() {
        val currentNft = _nft.value ?: return
        val toAddress = _recipientUiState.value.recipientAddress

        // Validate address
        if (!toAddress.matches(Regex("^0x[a-fA-F0-9]{40}$"))) {
            _transactionStatus.value = TransactionStatus.FAILURE("Invalid recipient address")
            return
        }

        viewModelScope.launch {
            Log.d("SendNftViewModel", "=== SEND NFT TRANSACTION STARTED ===")
            Log.d("SendNftViewModel", "NFT: ${currentNft.name} (${currentNft.contractAddress})")
            Log.d("SendNftViewModel", "Token ID: ${currentNft.tokenId}")
            Log.d("SendNftViewModel", "To address: $toAddress")

            _transactionStatus.value = TransactionStatus.PENDING

            try {
                // Clear previous transaction state
                sendRepository.restoreState()

                // Transfer the NFT using ERC721 safeTransferFrom
                sendRepository.transferNft(
                    chainId = currentNft.chainId,
                    contractAddress = currentNft.contractAddress,
                    tokenId = currentNft.tokenId,
                    toAddress = toAddress
                )

                // Observe transaction result
                val txResult = sendRepository.currentTransactionHash.first()
                
                if (txResult.lowercase() != "decline" && txResult.lowercase() != "error") {
                    val transactionResult = sendRepository.currentTransactionHash.first()
                    Log.d("SendNftViewModel", "Transaction result: '$transactionResult'")

                    if (transactionResult.isEmpty() || 
                        transactionResult == "error" || 
                        transactionResult == "decline" || 
                        transactionResult.contains("error", ignoreCase = true)) {
                        
                        Log.e("SendNftViewModel", "🔴 TRANSACTION FAILED - $transactionResult")
                        reflectiveLedPattern?.displayError()
                        _transactionStatus.value = TransactionStatus.FAILURE(parseNftErrorCode(transactionResult))
                    } else {
                        terminalSDK?.displayBlackText("NFT TRANSFER IN ORBIT...")
                        
                        checkTransactionInclusion(txResult) { hasBeenIncluded ->
                            if (hasBeenIncluded) {
                                Log.d("SendNftViewModel", "🟢 NFT TRANSFER SUCCESS")
                                reflectiveLedPattern?.displaySuccess()
                                _transactionStatus.value = TransactionStatus.SUCCESS
                                viewModelScope.launch {
                                    terminalSDK?.displayBlackText("NFT SENT!")
                                }
                            } else {
                                Log.e("SendNftViewModel", "🔴 NFT TRANSFER FAILED - Not included")
                                reflectiveLedPattern?.displayError()
                                _transactionStatus.value = TransactionStatus.FAILURE("Transaction not confirmed")
                            }
                        }
                    }
                } else {
                    Log.e("SendNftViewModel", "🔴 TRANSACTION FAILED - User declined or error")
                    reflectiveLedPattern?.displayError()
                    _transactionStatus.value = TransactionStatus.FAILURE(
                        if (txResult == "decline") "Transaction declined" else null
                    )
                }
            } catch (e: Exception) {
                Log.e("SendNftViewModel", "🔴 TRANSACTION FAILED - Exception: ${e.message}", e)
                reflectiveLedPattern?.displayError()
                _transactionStatus.value = TransactionStatus.FAILURE(parseNftErrorCode(e.message ?: ""))
            }

            Log.d("SendNftViewModel", "=== SEND NFT TRANSACTION ENDED ===")
        }
    }

    /**
     * Clear transaction status
     */
    fun clearTransactionStatus() {
        _transactionStatus.value = null
    }

    /**
     * Trigger QR scanner
     */
    fun triggerQrScanner() {
        _qrScannerTriggered.value = true
    }

    /**
     * Reset QR scanner trigger
     */
    fun resetQrScannerTrigger() {
        _qrScannerTriggered.value = false
    }

    /**
     * Keyboard dismissed callback
     */
    fun onKeyboardDismissed() {
        _shouldDismissKeyboard.value = false
    }

    /**
     * Check if transaction was included in blockchain
     */
    private fun checkTransactionInclusion(txHash: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val chainId = try {
                val repoChainId = sendRepository.currentTransactionChainId.first()
                if (repoChainId != 0) repoChainId else {
                    val chainIdStr = userDataRepository.userData.first().walletNetwork
                    if (chainIdStr.startsWith("0x", ignoreCase = true)) {
                        chainIdStr.removePrefix("0x").toInt(16)
                    } else {
                        chainIdStr.toIntOrNull() ?: 1
                    }
                }
            } catch (e: Exception) {
                1
            }

            val client = OkHttpClient()
            val contentType = "application/json; charset=utf-8".toMediaType()

            val startTime = System.currentTimeMillis()
            val timeoutMillis = 60000

            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                val bodyJson = """
                    {
                        "jsonrpc": "2.0",
                        "method": "pimlico_getUserOperationStatus",
                        "params": ["$txHash"],
                        "id": 1
                    }
                """.trimIndent()

                val request = Request.Builder()
                    .url(chainIdToBundler(chainId))
                    .post(bodyJson.toRequestBody(contentType))
                    .build()

                val status = try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            null
                        } else {
                            val jsonString = response.body?.string() ?: return@use null
                            val json = JSONObject(jsonString)
                            val resultObj = json.optJSONObject("result")
                            resultObj?.optString("status")
                        }
                    }
                } catch (e: Exception) {
                    null
                }

                when (status) {
                    "included" -> {
                        withContext(Dispatchers.Main) { callback(true) }
                        return@launch
                    }
                    "failed", "rejected" -> {
                        withContext(Dispatchers.Main) { callback(false) }
                        return@launch
                    }
                }

                delay(4000)
            }

            withContext(Dispatchers.Main) {
                callback(false)
            }
        }
    }

    /**
     * Parse NFT-specific error codes
     */
    private fun parseNftErrorCode(errorString: String): String? {
        return when {
            errorString.contains("not owner", ignoreCase = true) -> 
                "You don't own this NFT"
            errorString.contains("not approved", ignoreCase = true) -> 
                "NFT transfer not approved"
            errorString.contains("invalid token", ignoreCase = true) -> 
                "Invalid NFT token"
            errorString.contains("insufficient funds", ignoreCase = true) -> 
                "Insufficient funds for gas"
            errorString.contains("decline", ignoreCase = true) -> 
                "Transaction declined"
            errorString.contains("reverted", ignoreCase = true) -> 
                "Transaction reverted"
            else -> null
        }
    }
}
