package com.core.database.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.core.database.WmDatabase
import com.core.database.dao.TokenMetadataDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * ContentProvider to share TokenMetadata with other apps.
 * 
 * URI patterns:
 * - content://[authority]/token/[chainId]/[contractAddress] - Get specific token metadata
 * - content://[authority]/tokens/[chainId] - Get all tokens for a chain
 * 
 * Example usage from other app:
 * ```
 * val uri = Uri.parse("content://com.walletmanager.tokenmetadata.provider/token/1/0x123...")
 * val cursor = contentResolver.query(uri, null, null, null, null)
 * ```
 */
class TokenMetadataContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.walletmanager.tokenmetadata.provider"
        
        // URI codes
        private const val TOKEN_BY_CHAIN_AND_ADDRESS = 1
        private const val TOKENS_BY_CHAIN = 2
        
        // Column names
        const val COLUMN_CONTRACT_ADDRESS = "contract_address"
        const val COLUMN_DECIMALS = "decimals"
        const val COLUMN_NAME = "name"
        const val COLUMN_SYMBOL = "symbol"
        const val COLUMN_LOGO = "logo"
        const val COLUMN_CHAIN_ID = "chain_id"
        const val COLUMN_SWAPPABLE = "swappable"
        
        private val COLUMNS = arrayOf(
            COLUMN_CONTRACT_ADDRESS,
            COLUMN_DECIMALS,
            COLUMN_NAME,
            COLUMN_SYMBOL,
            COLUMN_LOGO,
            COLUMN_CHAIN_ID,
            COLUMN_SWAPPABLE
        )
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            // Match: token/[chainId]/[contractAddress]
            addURI(AUTHORITY, "token/#/*", TOKEN_BY_CHAIN_AND_ADDRESS)
            // Match: tokens/[chainId]
            addURI(AUTHORITY, "tokens/#", TOKENS_BY_CHAIN)
        }
    }
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TokenMetadataContentProviderEntryPoint {
        fun tokenMetadataDao(): TokenMetadataDao
    }
    
    private lateinit var tokenMetadataDao: TokenMetadataDao
    
    override fun onCreate(): Boolean {
        val context = context ?: return false
        
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TokenMetadataContentProviderEntryPoint::class.java
        )
        tokenMetadataDao = entryPoint.tokenMetadataDao()
        
        return true
    }
    
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor = MatrixCursor(projection ?: COLUMNS)
        
        when (uriMatcher.match(uri)) {
            TOKEN_BY_CHAIN_AND_ADDRESS -> {
                val chainId = uri.pathSegments[1].toIntOrNull() ?: return null
                val contractAddress = uri.pathSegments[2]
                
                // Query specific token
                runBlocking {
                    val tokens = tokenMetadataDao.getTokenMetadata(listOf(contractAddress)).first()
                    tokens.filter { it.chainId == chainId }.forEach { token ->
                        cursor.addRow(
                            arrayOf(
                                token.contractAddress,
                                token.decimals,
                                token.name,
                                token.symbol,
                                token.logo,
                                token.chainId,
                                if (token.swappable) 1 else 0
                            )
                        )
                    }
                }
            }
            
            TOKENS_BY_CHAIN -> {
                val chainId = uri.pathSegments[1].toIntOrNull() ?: return null
                
                // Query all tokens for a chain
                runBlocking {
                    val tokens = tokenMetadataDao.getTokenMetadata(chainId).first()
                    tokens.forEach { token ->
                        cursor.addRow(
                            arrayOf(
                                token.contractAddress,
                                token.decimals,
                                token.name,
                                token.symbol,
                                token.logo,
                                token.chainId,
                                if (token.swappable) 1 else 0
                            )
                        )
                    }
                }
            }
            
            else -> return null
        }
        
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }
    
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            TOKEN_BY_CHAIN_AND_ADDRESS -> "vnd.android.cursor.item/vnd.$AUTHORITY.token"
            TOKENS_BY_CHAIN -> "vnd.android.cursor.dir/vnd.$AUTHORITY.token"
            else -> null
        }
    }
    
    // We don't support these operations as this is a read-only provider
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
} 