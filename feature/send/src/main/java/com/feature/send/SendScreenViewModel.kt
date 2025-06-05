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
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import com.core.data.remote.EnsApi
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenExchangeRepository
import com.core.domain.GetSwapTokens
import com.core.domain.GetAllTokensUsecase
import com.core.model.NetworkChain
import com.core.model.Price
import com.core.model.TokenData
import com.core.model.UserData
import com.core.result.Result
import com.core.terminalsdk.TerminalSDK
import com.core.ui.showCustomToast
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlin.collections.filter
import kotlin.collections.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.feature.send.ui.TransactionStatus

enum class TransactionStatus {
    PENDING,
    SUCCESS,
    FAILURE
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
            _txComplete.value = TxCompleteUiState.UnComplete // Reset to UnComplete when starting
            Log.d("SendViewModel", "Set status to PENDING and txComplete to UnComplete")

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
                        callback()
                    } else {
                        // Optionally, handle the "decline" or "error" case, e.g., show a message
                        Log.d("SendViewModel", "Transaction declined or failed: $txResult")
                        onScreenOpened()
                    }
                    
                    // Now check the transaction result from the repository's flow
                    val transactionResult = sendRepository.currentTransactionHash.first()
                    Log.d("SendViewModel", "Transaction result from repository: '$transactionResult'")
                    
                    // Check if the transaction was successful by examining the result
                    if (transactionResult.isEmpty() || transactionResult == "error" || transactionResult == "decline" || transactionResult.contains("error", ignoreCase = true)) {
                        Log.e("SendViewModel", "🔴 TRANSACTION FAILED - Repository returned: '$transactionResult'")
                        _transactionStatus.value = TransactionStatus.FAILURE
                        _txComplete.value = TxCompleteUiState.UnComplete // Keep as UnComplete on failure
                        Log.d("SendViewModel", "❌ Status set: transactionStatus=FAILURE, txComplete=UnComplete")
                    } else {
                        Log.d("SendViewModel", "🟢 TRANSACTION SUCCESS - Repository returned valid hash: '$transactionResult'")
                        _transactionStatus.value = TransactionStatus.SUCCESS
                        _txComplete.value = TxCompleteUiState.Complete // ✅ Set txComplete to Complete on success!
                        Log.d("SendViewModel", "✅ Status set: transactionStatus=SUCCESS, txComplete=Complete")
                    }
                    //onTransactionFinalized(true)
                } catch (e: Exception) {
                    Log.e("SendViewModel", "🔴 TRANSACTION FAILED - Exception caught: ${e.message}", e)
                    Log.e("SendViewModel", "Exception type: ${e.javaClass.simpleName}")
                    e.printStackTrace()
                    _transactionStatus.value = TransactionStatus.FAILURE
                    _txComplete.value = TxCompleteUiState.UnComplete // Keep as UnComplete on failure
                    Log.d("SendViewModel", "❌ Status set: transactionStatus=FAILURE, txComplete=UnComplete")
                    //onTransactionFinalized(false)
                }
            } else {
                Log.e("SendViewModel", "🔴 NO ASSET SELECTED - Transaction failed")
                // Handle case where no asset is selected, though UI should prevent this
                _transactionStatus.value = TransactionStatus.FAILURE
                _txComplete.value = TxCompleteUiState.UnComplete // Keep as UnComplete on failure
                Log.d("SendViewModel", "❌ Status set: transactionStatus=FAILURE, txComplete=UnComplete (no asset)")
                //onTransactionFinalized(false)
            }
            
            Log.d("SendViewModel", "=== SEND TRANSACTION ENDED ===")
            Log.d("SendViewModel", "Final status - transactionStatus: ${_transactionStatus.value}, txComplete: ${_txComplete.value}")
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

    /**
     * Call this function when the send screen is closed/navigated away to remove QR code from secondary screen
     */
    fun onScreenClosed() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (terminalSDK?.isAvailable() == true) {
                    terminalSDK.removeQRCode()
                    Log.d("SendViewModel", "QR code removed from secondary screen")
                } else {
                    Log.w("SendViewModel", "TerminalSDK not available")
                }
            } catch (e: Exception) {
                Log.e("SendViewModel", "Error removing QR code", e)
            }
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
                
                // Hole den aktuellen Wechselkurs für das Token
                tokenExchangeRepository.getLatestExchange(tokenSymbol)
                    .first()
                    ?.let { exchange ->
                        // exchange.value ist der Preis für 1 Token in USD
                        val tokenAmount = dollarValue / exchange.value
                        
                        // Formatiere das Ergebnis
                        val decimalFormat = DecimalFormat("#.######")
                        val formattedAmount = decimalFormat.format(tokenAmount)
                        
                        // show Toast
                        withContext(Dispatchers.Main) {
                            context.showCustomToast(
                                "$${dollarAmount} = $formattedAmount $tokenSymbol",
                                Toast.LENGTH_SHORT,
                                fontFamily = PitagonsSans,
                                fontWeight = FontWeight.SemiBold,
                                backgroundColor = dgenOcean,
                                textColor = dgenTurqoise
                            )
                        }
                        
                        // Update den amount Wert
                        updateAmount(formattedAmount)
                    } ?: run {
                        // If no exchange has been found
                        withContext(Dispatchers.Main) {

                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    context.showCustomToast(
                        "Error at calculation: ${e.message}",
                        Toast.LENGTH_SHORT,
                        fontFamily = PitagonsSans,
                        fontWeight = FontWeight.SemiBold,
                        backgroundColor = dgenOcean,
                        textColor = dgenTurqoise
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

}


sealed interface TxCompleteUiState {
    object UnComplete: TxCompleteUiState
    object Complete: TxCompleteUiState
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