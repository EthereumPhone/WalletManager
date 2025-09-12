package com.feature.send

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.model.dto.Contact
import com.core.data.repository.SendRepository
import com.core.data.repository.UserDataRepository
import com.core.model.TokenAsset
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import com.core.data.remote.EnsApi
import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenExchangeRepository
import com.core.model.UserData
import com.core.terminalsdk.TerminalSDK
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.feature.send.ui.TransactionStatus
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainToApiKey
import com.core.model.TokenAssetWithPrice
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.ui.util.formatWithSuffix
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import kotlin.String
import kotlin.collections.first

enum class TransactionStatus {
    PENDING,
    SUCCESS,
    FAILURE
}

sealed interface TxCompleteUiState {
    object UnComplete: TxCompleteUiState
    object Complete: TxCompleteUiState
}


private const val GROUP_NAV_ARGUMENT = "groupId"
private const val ADDRESS_NAV_ARGUMENT = "address"

@HiltViewModel
class SendViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val tokenExchangeRepository: TokenExchangeRepository,
    private val groupedTokenRepository: GroupedTokenRepository,
    private val sendRepository: SendRepository,
    private val savedStateHandle: SavedStateHandle,
    private val ensApi: EnsApi,
    private val terminalSDK: TerminalSDK?,
    private val reflectiveLedPattern: ReflectiveLedPattern?,
    @ApplicationContext private val context: Context
): ViewModel() {
    val groupId: String = savedStateHandle[GROUP_NAV_ARGUMENT] ?: ""
    val address: String = savedStateHandle[ADDRESS_NAV_ARGUMENT] ?: ""


    val assetsUiState: StateFlow<AssetsUiState> =
        groupedTokenRepository.observeAllTokensWithPriceInGroup(groupId).map {
            if (it.isEmpty()) AssetsUiState.Empty
            else AssetsUiState.Success(it)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AssetsUiState.Loading
            )

    init {
        viewModelScope.launch {
            val assetState = assetsUiState.first { it !is AssetsUiState.Loading }
            if (assetState is AssetsUiState.Success) {
                val assets = assetState.assets

                when {
                    assets.size == 1 -> {
                        val asset = assets.first()

                        _selectedAssetUiState.value = SelectedAssetUiState.Selected(asset)
                        _amountUiState.update { it.copy(
                            maxAmount = asset.balance,
                            currentAmount = "",
                            formattedMaxAmount = asset.balance.formatWithSuffix(),
                            maxFiatAmount = asset.fiatAmount,
                            currentFiatAmount = "",
                            formattedMaxFiatAmount = asset.fiatAmount.formatWithSuffix(2)
                        ) }
                    }
                    assets.size > 1 -> {
                        val sortedByChainId = assets.minByOrNull { it.chainId }!!
                        _selectedAssetUiState.value = SelectedAssetUiState.Selected(sortedByChainId)

                        _amountUiState.update { it.copy(
                            maxAmount = sortedByChainId.balance,
                            formattedMaxAmount = sortedByChainId.balance.formatWithSuffix(),
                            maxFiatAmount = sortedByChainId.fiatAmount,
                            formattedMaxFiatAmount = sortedByChainId.fiatAmount.formatWithSuffix(2)

                        ) }
                    }
                    else -> _selectedAssetUiState.value = SelectedAssetUiState.Unselected
                }
            }
        }
    }




    private val _recipientUiState = MutableStateFlow<RecipientUiState>(RecipientUiState(recipientAddress = address))
    val recipientUiState: StateFlow<RecipientUiState> = _recipientUiState
    
    // Keyboard dismissal state - triggered when ENS resolution succeeds
    private val _shouldDismissKeyboard = MutableStateFlow(false)
    val shouldDismissKeyboard: StateFlow<Boolean> = _shouldDismissKeyboard.asStateFlow()



    private val _amountUiState = MutableStateFlow<AmountUiState>(AmountUiState(
        0.0,
        "0.0",
        currentAmount = "0.0",
        maxFiatAmount = 0.0,
        formattedMaxFiatAmount = "0.0",
        currentFiatAmount = "0.0",
        useMaxAmount = false
    ))
    val amountUiState = _amountUiState




    private val _selectedAssetUiState = MutableStateFlow<SelectedAssetUiState>(SelectedAssetUiState.Unselected)
    val selectedAssetUiState: StateFlow<SelectedAssetUiState> = _selectedAssetUiState.asStateFlow()


    fun changeSelectedAsset(chainId: Int) {

        val selected = (assetsUiState.value as AssetsUiState.Success)
            .assets.first { it.chainId == chainId }

        val current = _selectedAssetUiState.value
        if (current is SelectedAssetUiState.Selected &&
            current.tokenAsset.chainId == selected.chainId
        ) {
            // disable de-selection for now
            //_selectedAssetUiState.value = SelectedAssetUiState.Unselected
        } else {
            _selectedAssetUiState.value = SelectedAssetUiState.Selected(selected)

            _amountUiState.update { it.copy(
                maxAmount = selected.balance,
                formattedMaxAmount = selected.balance.formatWithSuffix(),
                maxFiatAmount = selected.fiatAmount,
                formattedMaxFiatAmount = selected.fiatAmount.formatWithSuffix(2),
                currentAmount = "",
                currentFiatAmount = "",
                useMaxAmount = false
            ) }
        }
    }



    private val _txComplete = MutableStateFlow<TxCompleteUiState>(TxCompleteUiState.UnComplete)
    val txComplete: StateFlow<TxCompleteUiState> = _txComplete.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: Flow<List<Contact>> = _contacts

    // Add QR scanner trigger state
    private val _qrScannerTriggered = MutableStateFlow(false)
    val qrScannerTriggered: StateFlow<Boolean> = _qrScannerTriggered.asStateFlow()

    // Add send transaction trigger state
    private val _sendTransactionTriggered = MutableStateFlow(false)
    val sendTransactionTriggered: StateFlow<Boolean> = _sendTransactionTriggered.asStateFlow()

    private val _transactionStatus = MutableStateFlow<TransactionStatus?>(null)
    val transactionStatus: StateFlow<TransactionStatus?> = _transactionStatus.asStateFlow()


    fun send(callback: () -> Unit) {
        viewModelScope.launch {
            val selectedAsset = selectedAssetUiState.value
            Log.d("SendViewModel", "=== SEND TRANSACTION STARTED ===")
            Log.d("SendViewModel", "Selected asset: $selectedAsset")
            Log.d("SendViewModel", "Amount: ${amountUiState.value}")
            Log.d("SendViewModel", "To address: ${recipientUiState.value}")
            
            _transactionStatus.value = TransactionStatus.PENDING
            Log.d("SendViewModel", "Status set to PENDING")

            if(selectedAsset is SelectedAssetUiState.Selected) {
                try {
                    val asset = TokenAsset(
                        address = selectedAsset.tokenAsset.address,
                        chainId = selectedAsset.tokenAsset.chainId,
                        symbol = selectedAsset.tokenAsset.symbol,
                        name = selectedAsset.tokenAsset.name,
                        balance = selectedAsset.tokenAsset.balance,
                        decimals = selectedAsset.tokenAsset.decimals,
                        logoUrl = "",
                        swappable = false
                    )
                    
                    // Calculate the actual amount to send
                    val amountToSend: String
                    val amountDouble: Double
                    
                    if (amountUiState.value.currentAmount.isNotEmpty()) {
                        // User entered crypto amount directly
                        amountToSend = amountUiState.value.currentAmount
                        amountDouble = amountToSend.toDouble()
                        Log.d("SendViewModel", "Using crypto amount: $amountToSend")
                    } else if (amountUiState.value.currentFiatAmount.isNotEmpty()) {
                        // User entered fiat amount, need to convert to crypto
                        val fiatAmount = amountUiState.value.currentFiatAmount.toDouble()
                        
                        // Calculate price per token
                        val pricePerToken = if (selectedAsset.tokenAsset.balance > 0) {
                            selectedAsset.tokenAsset.fiatAmount / selectedAsset.tokenAsset.balance
                        } else {
                            0.0
                        }
                        
                        // Convert fiat to crypto amount
                        amountDouble = if (pricePerToken > 0) {
                            fiatAmount / pricePerToken
                        } else {
                            0.0
                        }
                        amountToSend = amountDouble.toString()
                        
                        Log.d("SendViewModel", "Converting fiat amount: $$fiatAmount to crypto: $amountToSend (price per token: $$pricePerToken)")
                    } else {
                        // No amount entered
                        Log.e("SendViewModel", "No amount entered for transaction")
                        throw IllegalArgumentException("No amount specified for transaction")
                    }
                    
                    Log.d("SendViewModel", "Processing transaction for ${asset.symbol} on chain ${asset.chainId}")
                    
                    // Clear previous transaction hash
                    sendRepository.restoreState()
                    
                    if(asset.address.contains("0x")) {
                        Log.d("SendViewModel", "Sending ERC20 token: ${asset.address}")
                        sendRepository.transferErc20(
                            selectedAsset.tokenAsset.chainId,
                            asset,
                            amountDouble,
                            recipientUiState.value.recipientAddress
                        )
                        Log.d("SendViewModel", "ERC20 transfer method completed")
                    } else {
                        Log.d("SendViewModel", "Sending native ETH")
                        sendRepository.transferEth(
                            chainId = selectedAsset.tokenAsset.chainId,
                            toAddress = recipientUiState.value.recipientAddress,
                            data = "",
                            value = amountToSend
                        )
                        Log.d("SendViewModel", "ETH transfer method completed")
                    }

                    // Observe the transaction result
                    val txResult = sendRepository.currentTransactionHash.first()
                    if (txResult.lowercase() != "decline" && txResult.lowercase() != "error") {
                        // Now check the transaction result from the repository's flow
                        val transactionResult = sendRepository.currentTransactionHash.first()
                        Log.d("SendViewModel", "Transaction result from repository: '$transactionResult'")
                        
                        // Check if the transaction was successful by examining the result
                        if (transactionResult.isEmpty() || transactionResult == "error" || transactionResult == "decline" || transactionResult.contains("error", ignoreCase = true)) {
                            Log.e("SendViewModel", "🔴 TRANSACTION FAILED - Repository returned: '$transactionResult'")

                            reflectiveLedPattern?.displayError()

                            _transactionStatus.value = TransactionStatus.FAILURE
                        } else {
                            terminalSDK?.displayBlackText("TXN IN ORBIT...")
                            checkTransactionInclusion(
                                txResult
                            ) { hasBeenIncluded ->
                                if (hasBeenIncluded) {
                                    Log.d("SendViewModel", "🟢 TRANSACTION SUCCESS - Repository returned valid hash: '$transactionResult'")
                                    reflectiveLedPattern?.displaySuccess()
                                    _transactionStatus.value = TransactionStatus.SUCCESS
                                    viewModelScope.launch {
                                        terminalSDK?.displayBlackText("TXN SUCCESS!")
                                    }
                                } else {
                                    Log.e("SendViewModel", "🔴 TRANSACTION FAILED - Not included in the blockchain")
                                    reflectiveLedPattern?.displayError()
                                    _transactionStatus.value = TransactionStatus.FAILURE
                                }
                            }

                        }
                    } else {
                        Log.e("SendViewModel", "🔴 TRANSACTION FAILED - User declined or error before sending")
                        reflectiveLedPattern?.displayError()
                        _transactionStatus.value = TransactionStatus.FAILURE
                    }
                } catch (e: Exception) {
                    Log.e("SendViewModel", "🔴 TRANSACTION FAILED - Exception caught: ${e.message}", e)
                    Log.e("SendViewModel", "Exception type: ${e.javaClass.simpleName}")
                    reflectiveLedPattern?.displayError()
                    e.printStackTrace()
                    _transactionStatus.value = TransactionStatus.FAILURE
                }
            } else {
                Log.e("SendViewModel", "🔴 NO ASSET SELECTED - Transaction failed")
                reflectiveLedPattern?.displayError()
                _transactionStatus.value = TransactionStatus.FAILURE
            }
            
            Log.d("SendViewModel", "=== SEND TRANSACTION ENDED ===")
            Log.d("SendViewModel", "Final status - transactionStatus: ${_transactionStatus.value}")
            callback()
        }
    }


    fun updateAddress(address: String) {
        _recipientUiState.update { it.copy(address) }
        resolveEns()
    }
    
    fun onKeyboardDismissed() {
        // Reset the keyboard dismissal state after it has been consumed
        _shouldDismissKeyboard.value = false
    }

    fun updateAmount(amount: String, isFiat: Boolean) {
        _amountUiState.update {
            it.copy(
                currentAmount = if (isFiat) "" else amount,
                currentFiatAmount = if (isFiat) amount else "",
                useMaxAmount = false
            )
        }
    }

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
                        // Trigger keyboard dismissal on successful ENS resolution
                        _shouldDismissKeyboard.value = true
                    } else {
                        _recipientUiState.update { it.copy(ensError = "ENS name not found") }
                    }
                } catch (e: Exception) {
                    Log.e("SendViewModel", "Failed to resolve ENS", e)
                    _recipientUiState.update { it.copy(ensError = "Failed to resolve ENS") }
                } finally {
                    _recipientUiState.update { it.copy(isResolving = false) }
                }
            }
        }
    }

    fun setMaxAmount() {
        viewModelScope.launch {

            val maxAmount = _amountUiState.value.maxAmount.toBigDecimal()
            val chainId = (_selectedAssetUiState.value as SelectedAssetUiState.Selected).tokenAsset.chainId

            val amount = sendRepository.maxAllowedSend(maxAmount,chainId)

            _amountUiState.update {
                it.copy(
                    currentAmount = amount.toDouble().formatWithSuffix(),
                    currentFiatAmount = it.formattedMaxFiatAmount,
                    useMaxAmount = true
                )
            }
        }
    }

    
    /**
     * Function to trigger QR scanner from secondary screen
     */
    fun triggerQrScanner() {
        _qrScannerTriggered.value = true
    }
    
    /**
     * Reset QR scanner trigger state after handling
     */
    fun resetQrScannerTrigger() {
        _qrScannerTriggered.value = false
    }

    /**
     * Function to trigger send transaction from secondary screen
     */
    fun triggerSendTransaction() {
        _sendTransactionTriggered.value = true
    }
    
    /**
     * Reset send transaction trigger state after handling
     */
    fun resetSendTransactionTrigger() {
        _sendTransactionTriggered.value = false
    }

    /**
     * Call this function when the send screen is opened to display QR code on secondary screen
     */
    fun onScreenOpened() {
        viewModelScope.launch(Dispatchers.Main) {
            try {

                reflectiveLedPattern?.displayArrowUp()
                val result = terminalSDK?.isAvailable() == false
                println("TerminalSDK isAvailable: $result")

                terminalSDK?.displayQRCode(
                    onQrCode = {
                        Log.d("SendViewModel", "QR code touched on secondary screen - triggering QR scanner")
                        triggerQrScanner()
                    },
                    sendTx = {
                        Log.d("SendViewModel", "Send transaction touched on secondary screen - triggering send transaction")
                        send() {}
                    }
                )
                Log.d("SendViewModel", "QR code displayed on secondary screen")
            } catch (e: Exception) {
                Log.e("SendViewModel", "Error displaying QR code", e)
            }
        }
    }

    fun onScreenOpenedAfterResume() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                delay(500)
                if (terminalSDK?.isAvailable() == true) {
                    while(!terminalSDK.isScreenOn()) {
                        Log.d("SendViewModel", "ETHOSDEBUG: Waiting for secondary screen to be on...")
                        delay(200)
                    }

                    terminalSDK.displayQRCode(
                        onQrCode = {
                            Log.d("SendViewModel", "QR code touched on secondary screen - triggering QR scanner")
                            triggerQrScanner()
                        },
                        sendTx = {
                            Log.d("SendViewModel", "Send transaction touched on secondary screen - triggering send transaction")
                            send() {}
                        }
                    )
                    reflectiveLedPattern?.displayArrowUp()
                    Log.d("SendViewModel", "QR code displayed on secondary screen")
                } else {
                    Log.w("SendViewModel", "TerminalSDK not available")
                }
            } catch (e: Exception) {
                Log.e("SendViewModel", "Error displaying QR code", e)
            }
        }
    }

    /**
     * Call this function when the send screen is closed/navigated away to remove QR code from secondary screen
     */
    fun onScreenClosed(clearLed: Boolean = true) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (terminalSDK?.isAvailable() == true) {
                    terminalSDK.removeQRCode()
                    // Only clear LED if explicitly requested
                    if (clearLed) {
                        reflectiveLedPattern?.clear()
                    }

                    Log.d("SendViewModel", "QR code removed from secondary screen")
                } else {
                    Log.w("SendViewModel", "TerminalSDK not available")
                }
            } catch (e: Exception) {
                Log.e("SendViewModel", "Error removing QR code", e)
            }
        }
    }

    fun showFailedMatrix(){
        viewModelScope.launch {
            reflectiveLedPattern?.displayError()
            delay(2000)
            reflectiveLedPattern?.clear()
            reflectiveLedPattern?.displayArrowUp()
        }
    }

    fun showWarningMatrix(){
        viewModelScope.launch {
            reflectiveLedPattern?.displayWarning()
            delay(2000)
            reflectiveLedPattern?.clear()
            reflectiveLedPattern?.displayArrowUp()
        }
    }

    fun showSuccessMatrix(){
        viewModelScope.launch {
            reflectiveLedPattern?.displayError()
            delay(2000)
            reflectiveLedPattern?.clear()
            reflectiveLedPattern?.displayArrowUp()
        }
    }

    //Contacts
    @SuppressLint("Range")
    fun getContacts(context: Context) {
        val contactsList = mutableListOf<Contact>()

        val contentResolver: ContentResolver = context.contentResolver // Obtain the ContentResolver

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                // Get the phone number for the contact
                val phoneNumber = getPhoneNumber(contentResolver, contactId)

                // Get eth address for the contact
                val res = getData15ForContact(contactId,contentResolver)
                val address = if(res?.isNotEmpty() == true) res else ""

                // Get Image for the contact


                val contact = Contact(
                    id = contactId,
                    name = contactName,
                    phone = phoneNumber,
                    address = address,
                    image = getPhotoUriForContact(contactId, contentResolver)
                        ?: ""
                )
                contactsList.add(contact)
            } while (cursor.moveToNext())
            cursor.close()
        }

        _contacts.value = contactsList
    }

    @SuppressLint("Range")
    private fun getPhoneNumber(contentResolver: ContentResolver, contactId: String): String {
        var phoneNumber = ""

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            cursor.close()
        }

        return phoneNumber
    }

    @SuppressLint("Range")
    private fun getPhotoUriForContact(contactId: String,contentResolver: ContentResolver): String? {
        val photoCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.Data.CONTACT_ID + " = ?",
            arrayOf(contactId), null
        )

        photoCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))
                if (photoUri != null) {
                    return photoUri
                }
            }
        }

        return null
    }

    @SuppressLint("Range")
    fun getData15ForContact(contactId: String,contentResolver: ContentResolver): String? {
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Data.DATA15)
        val selection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA15))
            }
        }
        return null
    }

    private fun formatSmallBalance(balance: Double): Double {
        if (balance == 0.0) return 0.0

        val precision = 6
        val minDisplayableValue = 1.0 / Math.pow(10.0, precision.toDouble())

        // For very small values (less than minDisplayableValue), return the minimum displayable value
        if (balance > 0 && balance < minDisplayableValue) {
            return minDisplayableValue
        }

        // Otherwise, round to 6 decimal places
        val bd = BigDecimal(balance)
        val rounded = bd.setScale(precision, BigDecimal.ROUND_HALF_UP)
        return rounded.toDouble()
    }

    /**
     * Call this function to clear the transaction status, e.g., when the overlay is dismissed.
     */
    fun clearTransactionStatus() {
        _transactionStatus.value = null
    }

    /**
     * Reset txComplete state back to UnComplete, useful for starting fresh transactions
     */
    fun resetTxComplete() {
        _txComplete.value = TxCompleteUiState.UnComplete
    }

    fun checkTransactionInclusion(txHash: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Resolve correct chain id (same logic as before)
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
            val timeoutMillis = 60000 // 1 minute timeout

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

                // Wait 4 seconds before next poll
                delay(4000)
            }

            withContext(Dispatchers.Main) {
                callback(false)
            }
        }
    }

}


sealed interface SelectedAssetUiState {
    object Unselected: SelectedAssetUiState
    data class Selected(val tokenAsset: TokenAssetWithPrice): SelectedAssetUiState
}

sealed interface AssetsUiState {
    object Loading : AssetsUiState
    object Error : AssetsUiState
    object Empty : AssetsUiState
    data class Success(
        val assets: List<TokenAssetWithPrice>
    ) : AssetsUiState
}

sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}

data class RecipientUiState(
    val recipientAddress: String = "",
    val isResolving: Boolean = false,
    val ensError: String = "",
)

data class AmountUiState(
    val maxAmount: Double,
    val formattedMaxAmount: String,
    val currentAmount: String = "",
    val maxFiatAmount: Double,
    val formattedMaxFiatAmount: String,
    val currentFiatAmount: String = "",
    val useMaxAmount: Boolean = false,
)


private const val SEARCH_QUERY = "searchQuery"
private const val ADDRESS_QUERY = "addressQuery"
private const val AMOUNT = "amount"