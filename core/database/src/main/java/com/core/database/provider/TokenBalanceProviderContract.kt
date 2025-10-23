package com.core.database.provider

import android.content.ContentValues
import android.net.Uri
import java.math.BigDecimal

/**
 * Contract class for TokenBalanceContentProvider.
 * 
 * This class should be copied to the consuming app to easily query and update token balances.
 * 
 * Example usage:
 * ```
 * // In your other app, query token balance:
 * val balanceData = TokenBalanceProviderContract.getTokenBalance(
 *     context.contentResolver,
 *     chainId = 1,
 *     contractAddress = "0x123..."
 * )
 * 
 * if (balanceData != null) {
 *     Log.d("Balance", "Balance: ${balanceData.tokenBalance}")
 * }
 * 
 * // Upsert a token balance:
 * val inserted = TokenBalanceProviderContract.upsertTokenBalance(
 *     context.contentResolver,
 *     chainId = 1,
 *     contractAddress = "0x123...",
 *     balance = BigDecimal("1000.5")
 * )
 * ```
 */
object TokenBalanceProviderContract {
    
    const val AUTHORITY = "com.walletmanager.tokenbalance.provider"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
    
    // Column names
    const val COLUMN_CONTRACT_ADDRESS = "contract_address"
    const val COLUMN_CHAIN_ID = "chain_id"
    const val COLUMN_TOKEN_BALANCE = "token_balance"
    
    /**
     * Data class representing token balance
     */
    data class TokenBalanceData(
        val contractAddress: String,
        val chainId: Int,
        val tokenBalance: BigDecimal
    )
    
    /**
     * Build URI for querying a specific token balance
     */
    fun buildTokenBalanceUri(chainId: Int, contractAddress: String): Uri {
        return CONTENT_URI.buildUpon()
            .appendPath("balance")
            .appendPath(chainId.toString())
            .appendPath(contractAddress)
            .build()
    }
    
    /**
     * Build URI for querying all token balances of a specific chain
     */
    fun buildBalancesByChainUri(chainId: Int): Uri {
        return CONTENT_URI.buildUpon()
            .appendPath("balances")
            .appendPath(chainId.toString())
            .build()
    }
    
    /**
     * Build URI for querying all token balances with balance > 0
     */
    fun buildBalancesWithPositiveBalanceUri(): Uri {
        return CONTENT_URI.buildUpon()
            .appendPath("balances")
            .appendPath("positive")
            .build()
    }
    
    /**
     * Build URI for upsert operations
     */
    fun buildUpsertUri(): Uri {
        return CONTENT_URI.buildUpon()
            .appendPath("balance")
            .build()
    }

    fun buildAdjustDeductUri(chainId: Int, contractAddress: String): Uri {
        return CONTENT_URI.buildUpon()
            .appendPath("adjust")
            .appendPath("deduct")
            .appendPath(chainId.toString())
            .appendPath(contractAddress)
            .build()
    }
    
    /**
     * Helper method to query token balance from another app
     */
    fun getTokenBalance(
        contentResolver: android.content.ContentResolver,
        chainId: Int,
        contractAddress: String
    ): TokenBalanceData? {
        val uri = buildTokenBalanceUri(chainId, contractAddress)
        val cursor = contentResolver.query(uri, null, null, null, null)
        
        return cursor?.use {
            if (it.moveToFirst()) {
                TokenBalanceData(
                    contractAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTRACT_ADDRESS)),
                    chainId = it.getInt(it.getColumnIndexOrThrow(COLUMN_CHAIN_ID)),
                    tokenBalance = it.getString(it.getColumnIndexOrThrow(COLUMN_TOKEN_BALANCE)).toBigDecimal()
                )
            } else {
                null
            }
        }
    }
    
    /**
     * Helper method to query all token balances for a chain from another app
     */
    fun getBalancesByChain(
        contentResolver: android.content.ContentResolver,
        chainId: Int
    ): List<TokenBalanceData> {
        val uri = buildBalancesByChainUri(chainId)
        val cursor = contentResolver.query(uri, null, null, null, null)
        
        return cursor?.use {
            val balances = mutableListOf<TokenBalanceData>()
            while (it.moveToNext()) {
                balances.add(
                    TokenBalanceData(
                        contractAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTRACT_ADDRESS)),
                        chainId = it.getInt(it.getColumnIndexOrThrow(COLUMN_CHAIN_ID)),
                        tokenBalance = it.getString(it.getColumnIndexOrThrow(COLUMN_TOKEN_BALANCE)).toBigDecimal()
                    )
                )
            }
            balances
        } ?: emptyList()
    }
    
    /**
     * Helper method to query all token balances with positive balance
     */
    fun getBalancesWithPositiveBalance(
        contentResolver: android.content.ContentResolver
    ): List<TokenBalanceData> {
        val uri = buildBalancesWithPositiveBalanceUri()
        val cursor = contentResolver.query(uri, null, null, null, null)
        
        return cursor?.use {
            val balances = mutableListOf<TokenBalanceData>()
            while (it.moveToNext()) {
                balances.add(
                    TokenBalanceData(
                        contractAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTRACT_ADDRESS)),
                        chainId = it.getInt(it.getColumnIndexOrThrow(COLUMN_CHAIN_ID)),
                        tokenBalance = it.getString(it.getColumnIndexOrThrow(COLUMN_TOKEN_BALANCE)).toBigDecimal()
                    )
                )
            }
            balances
        } ?: emptyList()
    }
    
    /**
     * Helper method to upsert token balance from another app
     * Returns true if successful, false otherwise
     */
    fun upsertTokenBalance(
        contentResolver: android.content.ContentResolver,
        chainId: Int,
        contractAddress: String,
        balance: BigDecimal
    ): Boolean {
        val uri = buildUpsertUri()
        val values = ContentValues().apply {
            put(COLUMN_CONTRACT_ADDRESS, contractAddress)
            put(COLUMN_CHAIN_ID, chainId)
            put(COLUMN_TOKEN_BALANCE, balance.toString())
        }
        
        val resultUri = contentResolver.insert(uri, values)
        return resultUri != null
    }
} 
