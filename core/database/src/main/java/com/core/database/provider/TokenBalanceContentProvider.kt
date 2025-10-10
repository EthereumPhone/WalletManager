package com.core.database.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.core.database.dao.TokenBalanceDao
import com.core.database.model.erc20.TokenBalanceEntity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

/**
 * ContentProvider to share TokenBalance with other apps and allow upsert operations.
 * 
 * URI patterns:
 * - content://[authority]/balance/[chainId]/[contractAddress] - Get specific token balance
 * - content://[authority]/balances/[chainId] - Get all balances for a chain
 * - content://[authority]/balances/positive - Get all balances with balance > 0
 * - content://[authority]/balance - Insert/upsert token balance
 * 
 * Example usage from other app:
 * ```
 * // Query specific balance
 * val uri = Uri.parse("content://com.walletmanager.tokenbalance.provider/balance/1/0x123...")
 * val cursor = contentResolver.query(uri, null, null, null, null)
 * 
 * // Upsert balance
 * val values = ContentValues().apply {
 *     put("contract_address", "0x123...")
 *     put("chain_id", 1)
 *     put("token_balance", "1000.5")
 * }
 * contentResolver.insert(Uri.parse("content://com.walletmanager.tokenbalance.provider/balance"), values)
 * ```
 */
class TokenBalanceContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.walletmanager.tokenbalance.provider"
        
        // URI codes
        private const val BALANCE_BY_CHAIN_AND_ADDRESS = 1
        private const val BALANCES_BY_CHAIN = 2
        private const val BALANCES_POSITIVE = 3
        private const val BALANCE_INSERT = 4
        
        // Column names
        const val COLUMN_CONTRACT_ADDRESS = "contract_address"
        const val COLUMN_CHAIN_ID = "chain_id"
        const val COLUMN_TOKEN_BALANCE = "token_balance"
        
        private val COLUMNS = arrayOf(
            COLUMN_CONTRACT_ADDRESS,
            COLUMN_CHAIN_ID,
            COLUMN_TOKEN_BALANCE
        )
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            // Match: balance/[chainId]/[contractAddress]
            addURI(AUTHORITY, "balance/#/*", BALANCE_BY_CHAIN_AND_ADDRESS)
            // Match: balances/[chainId]
            addURI(AUTHORITY, "balances/#", BALANCES_BY_CHAIN)
            // Match: balances/positive
            addURI(AUTHORITY, "balances/positive", BALANCES_POSITIVE)
            // Match: balance (for insert/upsert)
            addURI(AUTHORITY, "balance", BALANCE_INSERT)
        }
    }
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TokenBalanceContentProviderEntryPoint {
        fun tokenBalanceDao(): TokenBalanceDao
    }
    
    private lateinit var tokenBalanceDao: TokenBalanceDao
    
    override fun onCreate(): Boolean {
        val context = context ?: return false
        
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TokenBalanceContentProviderEntryPoint::class.java
        )
        tokenBalanceDao = entryPoint.tokenBalanceDao()
        
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
            BALANCE_BY_CHAIN_AND_ADDRESS -> {
                val chainId = uri.pathSegments[1].toIntOrNull() ?: return null
                val contractAddress = uri.pathSegments[2]
                
                // Query specific token balance
                runBlocking {
                    val balances = tokenBalanceDao.getTokenBalances(listOf(contractAddress)).first()
                    balances.filter { it.chainId == chainId }.forEach { balance ->
                        cursor.addRow(
                            arrayOf<Any?>(
                                balance.contractAddress,
                                balance.chainId,
                                balance.tokenBalance.toString()
                            )
                        )
                    }
                }
            }
            
            BALANCES_BY_CHAIN -> {
                val chainId = uri.pathSegments[1].toIntOrNull() ?: return null
                
                // Query all balances for a chain
                runBlocking {
                    val balances = tokenBalanceDao.getTokenBalances(chainId).first()
                    balances.forEach { balance ->
                        cursor.addRow(
                            arrayOf<Any?>(
                                balance.contractAddress,
                                balance.chainId,
                                balance.tokenBalance.toString()
                            )
                        )
                    }
                }
            }
            
            BALANCES_POSITIVE -> {
                // Query all balances with balance > 0
                runBlocking {
                    val balances = tokenBalanceDao.getTokensWithBalance()
                    balances.forEach { balance ->
                        cursor.addRow(
                            arrayOf<Any?>(
                                balance.contractAddress,
                                balance.chainId,
                                balance.tokenBalance.toString()
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
            BALANCE_BY_CHAIN_AND_ADDRESS -> "vnd.android.cursor.item/vnd.$AUTHORITY.balance"
            BALANCES_BY_CHAIN, BALANCES_POSITIVE -> "vnd.android.cursor.dir/vnd.$AUTHORITY.balance"
            BALANCE_INSERT -> "vnd.android.cursor.item/vnd.$AUTHORITY.balance"
            else -> null
        }
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uriMatcher.match(uri) != BALANCE_INSERT) {
            return null
        }
        
        values ?: return null
        
        try {
            val contractAddress = values.getAsString(COLUMN_CONTRACT_ADDRESS) ?: return null
            val chainId = values.getAsInteger(COLUMN_CHAIN_ID) ?: return null
            val tokenBalance = values.getAsString(COLUMN_TOKEN_BALANCE)?.let { 
                BigDecimal(it) 
            } ?: return null
            
            val entity = TokenBalanceEntity(
                contractAddress = contractAddress,
                chainId = chainId,
                tokenBalance = tokenBalance
            )
            
            // Perform upsert operation
            runBlocking {
                tokenBalanceDao.upsertTokenBalances(listOf(entity))
            }
            
            // Notify observers
            context?.contentResolver?.notifyChange(uri, null)
            
            // Return URI for the inserted/updated item
            return Uri.parse("$AUTHORITY/balance/$chainId/$contractAddress")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    // We don't support delete and update operations
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
} 
