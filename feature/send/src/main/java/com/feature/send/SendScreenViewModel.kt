package com.feature.send

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
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
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenExchangeRepository
import com.core.domain.GetSwapTokens
import com.core.domain.GetAllTokensUsecase
import com.core.model.Price
import com.core.model.TokenData
import com.core.model.UserData
import com.core.terminalsdk.TerminalSDK
import com.core.ui.showCustomToast
import com.core.ui.util.dgenOcean
import com.core.ui.util.dgenTurqoise
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlin.collections.filter
import kotlin.collections.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.feature.send.ui.TransactionStatus
import com.core.data.util.chainIdToBundler
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.ui.showDgenToast
import com.core.ui.util.PitagonsSans
import com.core.ui.util.extraLargeExitDuration
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.delay

enum class TransactionStatus {
    PENDING,
    SUCCESS,
    FAILURE
}

sealed interface TxCompleteUiState {
    object UnComplete: TxCompleteUiState
    object Complete: TxCompleteUiState
}

@HiltViewModel
class SendViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val getAllTokensUsecase: GetAllTokensUsecase,
    private val tokenExchangeRepository: TokenExchangeRepository,
    private val sendRepository: SendRepository,
    private val getSwapTokens: GetSwapTokens,
    private val savedStateHandle: SavedStateHandle,
    private val ensApi: EnsApi,
    private val terminalSDK: TerminalSDK?,
    private val reflectiveLedPattern: ReflectiveLedPattern?,
    @ApplicationContext private val context: Context
): ViewModel()
{


    val currentChain: Flow<String> = userDataRepository.userData.map { it.walletNetwork }

    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )


    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, "")

    private val _toAddress = MutableStateFlow(
        savedStateHandle.get<String>(ADDRESS_QUERY) ?: ""
    )
    val toAddress: StateFlow<String> = _toAddress

    private val _amount = MutableStateFlow(
        savedStateHandle.get<String>(AMOUNT) ?: ""
    )
    val amount: StateFlow<String> = _amount

    private val _selectedAssetUiState = MutableStateFlow<SelectedTokenUiState>(SelectedTokenUiState.Unselected)
    val selectedAssetUiState = _selectedAssetUiState.asStateFlow()

    /*val tokenAssetState: StateFlow<AssetUiState> =
        networkBalanceRepository.getNetworksBalance()
            .map { balances ->
                val netWorkAssets = balances.map {
                    val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""
                    TokenAsset(
                        address = it.contractAddress,
                        chainId = it.chainId,
                        symbol = name.lowercase(),
                        name = name.lowercase(),
                        balance = it.tokenBalance.toDouble(),
                        decimals = 18
                    )
                }
                .sortedByDescending { it.balance }

                AssetUiState.Success(netWorkAssets)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AssetUiState.Loading
            )*/
    val tokenAssetState: StateFlow<AssetsUiState> = getAllTokensUsecase()
        .map { tokens ->
            val filteredTokens = tokens
                .filter { it.balance > 0 }
                .filter { token -> // Filter out tokens with URLs in their names or symbols
                    val name = token.name.lowercase()
                    val symbol = token.symbol.lowercase()

                    val urlPatterns = listOf(
                        "http://", "https://", "www.",
                        ".com", ".io", ".org", ".net", ".xyz",
                        "/", "t.me", "telegram", "twitter", "discord", "t.ly"
                    )

                    val containsNoUrlPatterns = urlPatterns.none { pattern ->
                        name.contains(pattern) || symbol.contains(pattern)
                    }
                    containsNoUrlPatterns
                }

            if (filteredTokens.isEmpty()) {
                AssetsUiState.Empty
            } else {
                AssetsUiState.Success(filteredTokens)
            }

        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssetsUiState.Loading
        )



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

    companion object {
        private const val SELECTED_TOKEN_ID = "selected_token"
    }

    // Use a StateFlow, MutableStateFlow, LiveData, or mutableStateOf as desired
    private val _selectedTokenIdFlow = MutableStateFlow(
        // Retrieve the initial value from the SavedStateHandle (or default)
        savedStateHandle.get<String>(SELECTED_TOKEN_ID) ?: ""
    )
    val selectedTokenIdFlow = _selectedTokenIdFlow.asStateFlow()

    /**
     * Update the user input in both the in-memory Flow and the SavedStateHandle
     */
    fun updateSelectedTokenId(newValue: String) {
        _selectedTokenIdFlow.value = newValue
        savedStateHandle[SELECTED_TOKEN_ID] = newValue
    }


    fun send(callback: () -> Unit) {
        viewModelScope.launch {
            val selectedAsset = _selectedAssetUiState.value
            Log.d("SendViewModel", "=== SEND TRANSACTION STARTED ===")
            Log.d("SendViewModel", "Selected asset: $selectedAsset")
            Log.d("SendViewModel", "Amount: ${amount.value}")
            Log.d("SendViewModel", "To address: ${toAddress.value}")
            
            _transactionStatus.value = TransactionStatus.PENDING
            Log.d("SendViewModel", "Status set to PENDING")

            if(selectedAsset is SelectedTokenUiState.Selected) {
                try {
                    val asset = selectedAsset.tokenAsset
                    val amountDouble = amount.value.toDouble()
                    Log.d("SendViewModel", "Processing transaction for ${asset.symbol} on chain ${asset.chainId}")
                    
                    // Clear previous transaction hash
                    sendRepository.restoreState()
                    
                    if(asset.address.contains("0x")) {
                        Log.d("SendViewModel", "Sending ERC20 token: ${asset.address}")
                        sendRepository.transferErc20(
                            selectedAsset.tokenAsset.chainId,
                            asset,
                            amountDouble,
                            toAddress.value
                        )
                        Log.d("SendViewModel", "ERC20 transfer method completed")
                    } else {
                        Log.d("SendViewModel", "Sending native ETH")
                        sendRepository.transferEth(
                            chainId = selectedAsset.tokenAsset.chainId,
                            toAddress = toAddress.value,
                            data = "",
                            value = amount.value
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


    fun updateToAddress(address: String) {
        _toAddress.value = address
    }

    fun updateAmount(amount: String) {
        _amount.value = amount
    }

    fun updateSelectedAsset(tokenAsset: TokenAsset) {
        _selectedAssetUiState.update {
            SelectedTokenUiState.Selected(tokenAsset)
        }
    }

    fun updateQuery(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    fun setMaxAmount(maxamount: BigDecimal, chainId: Int){
        viewModelScope.launch {

            val decimalFormat = DecimalFormat("#.#####")
            val test = sendRepository.maxAllowedSend(maxamount,chainId)

            updateAmount(decimalFormat.format(test.toDouble()))
        }
    }

    val tokenData = tokenExchangeRepository.getExchanges()
        .map { exchanges ->
            exchanges.groupBy { it.symbol }
                .map { (symbol, exchangeList) ->
                    // Get the most recent exchange rate for each symbol
                    val latestExchange = exchangeList.maxByOrNull { it.timestamp }
                    TokenData(
                        symbol = symbol,
                        prices = listOf(
                            Price(
                                currency = latestExchange?.currency ?: "",
                                value = latestExchange?.value?.toString() ?: "0.0",
                                lastUpdatedAt = latestExchange?.timestamp?.toString() ?: ""
                            )
                        )
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun loadSymbol(symbol: List<String>) {
        viewModelScope.launch {
            try {
                tokenExchangeRepository.fetchExchangeBySymbols(symbol)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    // 1) Get the itemId directly as a value:
//    val tokenId: String = savedStateHandle["tokenId"] ?: ""

    // OR 2) Expose it as a StateFlow:
     val tokenIdFlow: StateFlow<String> =
         savedStateHandle.getStateFlow("tokenId", "")

    init {
        // Initialize selected asset if tokenId is available
        viewModelScope.launch {
            /*
            tokenIdFlow.collect { tokenId ->
                Log.d("SendViewModel", "TokenId from navigation: $tokenId")
                if (tokenId.isNotEmpty()) {
                    tokenAssetState.collect { assetState ->
                        if (assetState is AssetUiState.Success) {
                            Log.d("SendViewModel", "Assets available: ${assetState.assets.size}")
                            val token = assetState.assets.find { it.address == tokenId }
                            Log.d("SendViewModel", "Found token: ${token?.symbol}")
                            token?.let {
                                updateSelectedAsset(it)
                                Log.d("SendViewModel", "Updated selected asset to: ${it.symbol}")
                            }
                        }
                    }
                }
            }
             */


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

                if (terminalSDK?.isAvailable() == true) {
                    terminalSDK.displayQRCode(
                        onQrCode = {
                            Log.d("SendViewModel", "QR code touched on secondary screen - triggering QR scanner")
                            triggerQrScanner()
                        },
                        sendTx = {
                            Log.d("SendViewModel", "Send transaction touched on secondary screen - triggering send transaction")
                            triggerSendTransaction()
                        }
                    )
                    Log.d("SendViewModel", "QR code displayed on secondary screen")
                } else {
                    Log.w("SendViewModel", "TerminalSDK not available")
                }
            } catch (e: Exception) {
                Log.e("SendViewModel", "Error displaying QR code", e)
            }
        }
    }

    fun onScreenOpenedAfterResume() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                delay(2000)
                if (terminalSDK?.isAvailable() == true) {
                    while(terminalSDK.isScreenOn() != true) {
                        Log.d("SendViewModel", "ETHOSDEBUG: Waiting for secondary screen to be on...")
                        delay(500)
                    }

                    reflectiveLedPattern?.displayArrowUp()
                    terminalSDK.displayQRCode(
                        onQrCode = {
                            Log.d("SendViewModel", "QR code touched on secondary screen - triggering QR scanner")
                            triggerQrScanner()
                        },
                        sendTx = {
                            Log.d("SendViewModel", "Send transaction touched on secondary screen - triggering send transaction")
                            triggerSendTransaction()
                        }
                    )
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

    fun convertDollarToToken(dollarAmount: String, tokenSymbol: String) {
        viewModelScope.launch {
            try {
                val dollarValue = dollarAmount.toDoubleOrNull() ?: return@launch
                
                // Determine the correct symbol that has a price feed. For network-coins the on-chain "symbol" may be the
                // network name (e.g. "BASE") rather than the underlying currency symbol (ETH). We remap those cases here
                // so that we always query the price API with a symbol that exists.
                val lookupSymbol = when(tokenSymbol.uppercase()) {
                    "MAINNET", "OPTIMISM", "ARBITRUM", "SEPOLIA", "BASE", "ZORA" -> "ETH"
                    "POLYGON" -> "MATIC" // Polygon network gas token is MATIC
                    else -> tokenSymbol.uppercase()
                }

                // Fetch the latest USD price for the token
                tokenExchangeRepository.getLatestExchange(lookupSymbol)
                    .first()
                    ?.let { exchange ->
                        // exchange.value should represent the USD price for ONE unit of the given token. 
                        // On some chains the Alchemy price API returns the price for 1 **gwei** (1e-9 of the token) instead of one full token,
                        // which leads to extremely small USD values (and therefore an unrealistically large token amount).
                        // If we detect such a case (price < 10 USD for typical network coins like ETH) we correct it by scaling with 1e9.
                        val networkCoinsNeedingFix = setOf("ETH", "MATIC", "OP", "ARB", "BNB", "AVAX")
                        val correctedPrice = if (exchange.value < 10 && tokenSymbol.uppercase() in networkCoinsNeedingFix) {
                            // The API probably returned price for 1 gwei – convert to full-token price
                            exchange.value * 1_000_000_000
                        } else {
                            exchange.value
                        }

                        val tokenAmount = dollarValue / correctedPrice
                        
                        // Formatiere das Ergebnis
                        val decimalFormat = DecimalFormat("#.######")
                        val formattedAmount = decimalFormat.format(tokenAmount)

                        val realTokenLogo = when(tokenSymbol.uppercase()) {
                            "MAINNET", "OPTIMISM", "ARBITRUM", "SEPOLIA", "BASE", "ZORA" -> "ETH"
                            "POLYGON" -> "MATIC"
                            else -> tokenSymbol
                        }
                        
                        // Update den amount Wert
                        updateAmount(formattedAmount)
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showDgenToast(
                        context,
                        "Error at calculation: ${e.message}",
                    )
                }
            }
        }
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


sealed interface SelectedTokenUiState {
    object Unselected: SelectedTokenUiState
    data class Selected(val tokenAsset: TokenAsset): SelectedTokenUiState
}

sealed interface AssetsUiState {
    object Loading : AssetsUiState
    object Error : AssetsUiState
    object Empty : AssetsUiState
    data class Success(
        val assets: List<TokenAsset>
    ) : AssetsUiState
}

sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}

private const val SEARCH_QUERY = "searchQuery"
private const val ADDRESS_QUERY = "addressQuery"
private const val AMOUNT = "amount"