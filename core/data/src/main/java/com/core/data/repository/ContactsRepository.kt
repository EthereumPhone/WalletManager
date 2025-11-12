package com.core.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.core.data.model.dto.Contact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Check if contacts permission is granted
     */
    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Gets all contacts from the device that have an Ethereum address or ENS name
     * stored in DATA15 field
     */
    @SuppressLint("Range")
    fun getContactsWithEthAddress(): Flow<List<Contact>> = flow {
        // Check permission first
        if (!hasContactsPermission()) {
            emit(emptyList())
            return@flow
        }
        val contactsList = mutableListOf<Contact>()
        val contentResolver: ContentResolver = context.contentResolver
        
        // Map to avoid duplicates (a contact can have multiple phone numbers)
        val contactsMap = mutableMapOf<String, Contact>()
        
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )
        
        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val contactName = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) ?: "Unknown"
                
                // Get eth address for the contact from DATA15
                val ethAddress = getData15ForContact(contactId, contentResolver)
                
                // Only include contacts that have an Ethereum address or ENS name
                if (!ethAddress.isNullOrBlank() && (isValidEthAddress(ethAddress) || isValidEns(ethAddress))) {
                    val contact = Contact(
                        id = contactId,
                        name = contactName,
                        phone = getPhoneNumber(contentResolver, contactId),
                        address = ethAddress,
                        ens = if (isValidEns(ethAddress)) ethAddress else "",
                        image = getPhotoUriForContact(contactId, contentResolver) ?: ""
                    )
                    contactsMap[contactId] = contact
                }
            }
        }
        
        contactsList.addAll(contactsMap.values)
        emit(contactsList)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Search contacts by name or address
     */
    fun searchContacts(query: String): Flow<List<Contact>> =
        getContactsWithEthAddress().map { allContacts ->
            if (query.isBlank()) {
                allContacts
            } else {
                allContacts.filter { contact ->
                    contact.name.contains(query, ignoreCase = true) ||
                    contact.address.contains(query, ignoreCase = true) ||
                    contact.ens.contains(query, ignoreCase = true)
                }
            }
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
        
        cursor?.use {
            if (it.moveToFirst()) {
                phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }
        
        return phoneNumber
    }
    
    @SuppressLint("Range")
    private fun getPhotoUriForContact(contactId: String, contentResolver: ContentResolver): String? {
        val photoCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
            null,
            ContactsContract.Data.CONTACT_ID + " = ?",
            arrayOf(contactId), 
            null
        )
        
        photoCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI)
                if (columnIndex != -1) {
                    val photoUri = cursor.getString(columnIndex)
                    if (photoUri != null) {
                        return photoUri
                    }
                }
            }
        }
        
        return null
    }
    
    @SuppressLint("Range")
    private fun getData15ForContact(contactId: String, contentResolver: ContentResolver): String? {
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(ContactsContract.Data.DATA15)
        val selection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        
        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(ContactsContract.Data.DATA15)
                if (columnIndex != -1) {
                    return cursor.getString(columnIndex)
                }
            }
        }
        return null
    }
    
    private fun isValidEthAddress(address: String): Boolean {
        return address.matches(Regex("^0x[a-fA-F0-9]{40}$"))
    }
    
    private fun isValidEns(address: String): Boolean {
        return address.endsWith(".eth", ignoreCase = true) || 
               address.endsWith(".eth.eth", ignoreCase = true) ||
               address.contains(".base.eth", ignoreCase = true)
    }
}
