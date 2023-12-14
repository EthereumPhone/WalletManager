package com.feature.send

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.model.dto.Contact
import com.core.data.repository.SendRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.QueryTokenAssetsByNetwork
import com.core.model.TokenAsset
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
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
import com.core.model.UserData
import com.core.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@HiltViewModel
class SendViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val sendRepository: SendRepository,
    queryTokenAssetsByNetwork: QueryTokenAssetsByNetwork,
    private val savedStateHandle: SavedStateHandle,
    private val ensApi: EnsApi
): ViewModel() {

    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val toAddress = savedStateHandle.getStateFlow(ADDRESS_QUERY, "")
        .mapLatest {
            ensApi.resolveEns(
                it,
                userDataRepository.userData.first().walletNetwork.toInt()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )
    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, "")
    val amount = savedStateHandle.getStateFlow(AMOUNT, "")


    private val _selectedAssetUiState = MutableStateFlow<SelectedTokenUiState>(SelectedTokenUiState.Unselected)
    val selectedAssetUiState = _selectedAssetUiState.asStateFlow()

    val tokensAssetState: StateFlow<AssetUiState> =
        assetUiState(
            userDataRepository,
            queryTokenAssetsByNetwork,
            searchQuery
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssetUiState.Loading
        )

    private val _selectedAsset = MutableStateFlow<SelectedTokenUiState>(SelectedTokenUiState.Unselected)
    val selectedAsset: StateFlow<SelectedTokenUiState> = _selectedAsset.asStateFlow()

    private val _txComplete = MutableStateFlow<TxCompleteUiState>(TxCompleteUiState.UnComplete)
    val txComplete: StateFlow<TxCompleteUiState> = _txComplete.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: Flow<List<Contact>> = _contacts


    fun send() {
        viewModelScope.launch {
            val selectedAsset = _selectedAssetUiState.value

            if(selectedAsset is SelectedTokenUiState.Selected) {
                val asset = selectedAsset.tokenAsset
                if(asset.address.contains("0x")) {
                    sendRepository.transferErc20(
                        asset,
                        amount.value.toDouble(),
                        toAddress.value
                    )
                } else {
                    sendRepository.transferEth(
                        chainId = userDataRepository.userData.first().walletNetwork.toInt(),
                        toAddress = toAddress.value,
                        data = "",
                        value = amount.value
                    )
                }
            }
        }
    }


    fun updateToAddress(address: String) {
        savedStateHandle[ADDRESS_QUERY] = address
    }

    fun updateAmount(amount: String) {
        savedStateHandle[AMOUNT] = amount
    }

    fun updateSelectedAsset(tokenAsset: TokenAsset) {
        _selectedAssetUiState.update {
            SelectedTokenUiState.Selected(tokenAsset)
        }
    }

    fun updateQuery(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
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
}



@OptIn(ExperimentalCoroutinesApi::class)
fun assetUiState(
    userDataRepository: UserDataRepository,
    queryTokenAssetsByNetwork: QueryTokenAssetsByNetwork,
    searchQuery: Flow<String>
): Flow<AssetUiState> {
    return searchQuery.flatMapLatest { query ->
        val userData = userDataRepository.userData.first()
        queryTokenAssetsByNetwork(
            userData.walletNetwork.toInt(),
            query
        )
            .asResult()
            .mapLatest { result ->
                when(result) {
                    is Result.Success -> {
                        if (result.data.isEmpty()) {
                            AssetUiState.Empty
                        } else {
                            AssetUiState.Success(result.data)
                        }
                    }
                    is Result.Loading -> AssetUiState.Loading
                    is Result.Error -> AssetUiState.Error
                }
            }
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

sealed interface AssetUiState {
    object Loading: AssetUiState
    object Error: AssetUiState
    object Empty: AssetUiState
    data class Success(
        val assets: List<TokenAsset>
    ): AssetUiState
}

sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}

private const val SEARCH_QUERY = "searchQuery"
private const val ADDRESS_QUERY = "addressQuery"
private const val AMOUNT = "amount"