package com.feature.send

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.model.dto.Contact
import com.core.data.repository.AlchemyTransferRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.SendRepository
import com.core.data.repository.UserDataRepository
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val sendRepository: SendRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val alchemyTransferRepository: AlchemyTransferRepository // for pending transfers
): ViewModel() {

    val userAddress: StateFlow<String> =
        userDataRepository.userData.map {
            it.walletAddress
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val userNetwork: StateFlow<String> =
        userDataRepository.userData.map {
            it.walletNetwork
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val networkBalanceState: StateFlow<AssetUiState> =
        networkBalanceRepository.getNetworksBalance()
            .map { balances  ->
                val assets = balances.map {
                    val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""

                    TokenAsset(
                        it.contractAddress,
                        it.chainId,
                        if(name == "GOERLI") "GÖRLI" else name,
                        if(name == "GOERLI") "GÖRLI" else name,
                        it.tokenBalance.toDouble())
                }
                AssetUiState.Success(assets)
            }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssetUiState.Loading
        )

    private val _maxAmount = MutableStateFlow("")
    val maxAmount: Flow<String> = _maxAmount .asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: Flow<String> = _amount .asStateFlow()

    private val _toAddress = MutableStateFlow("")
    val toAddress: Flow<String> = _toAddress.asStateFlow()

    private val _selectedAsset = MutableStateFlow<SelectedTokenUiState>(SelectedTokenUiState.Unselected)
    val selectedAsset: StateFlow<SelectedTokenUiState> = _selectedAsset.asStateFlow()

    private val _txComplete = MutableStateFlow<TxCompleteUiState>(TxCompleteUiState.UnComplete)
    val txComplete: StateFlow<TxCompleteUiState> = _txComplete.asStateFlow()

    private val _exchange = MutableStateFlow("")
    val exchange: Flow<String> = _exchange


    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: Flow<List<Contact>> = _contacts



    fun send(
        to: String,
        chainId: Int,
        amount: String
    ){
        viewModelScope.launch {
            sendRepository.transferEth(
                chainId = chainId,
                toAddress = to,
                data = "",
                value = amount
            )
        }
    }


    fun changeToAddress(address: String) {
        Log.d("Wowser", address)
        _toAddress.value = address
    }

    fun changeTxComplete() {
        when(_txComplete.value) {
            is TxCompleteUiState.UnComplete -> {
                _txComplete.value = TxCompleteUiState.Complete

            }
            is TxCompleteUiState.Complete -> {
                _txComplete.value = TxCompleteUiState.UnComplete
            }
        }
    }

    fun changeSelectedAsset(tokenAsset: TokenAsset) {
        when(_selectedAsset.value) {
            is SelectedTokenUiState.Selected -> {
                _selectedAsset.value = SelectedTokenUiState.Selected(tokenAsset)

            }
            is SelectedTokenUiState.Unselected -> {
                _selectedAsset.value = SelectedTokenUiState.Selected(tokenAsset)
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



