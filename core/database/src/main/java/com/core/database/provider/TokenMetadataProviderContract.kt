package com.core.database.provider

import android.net.Uri

/**
 * Contract class for TokenMetadataContentProvider.
 * 
 * This class should be copied to the consuming app to easily query and update token metadata.
 * 
 * Example usage:
 * ```
 * // Query token metadata:
 * val tokenData = TokenMetadataProviderContract.getTokenMetadata(
 *     context.contentResolver,
 *     chainId = 1,
 *     contractAddress = "0x123..."
 * )
 * 
 * if (tokenData != null) {
 *     Log.d("Token", "Name: ${tokenData.name}, Symbol: ${tokenData.symbol}")
 * }
 * 
 * // Upsert token metadata (automatically creates/updates TokenGroup):
 * val success = TokenMetadataProviderContract.upsertTokenMetadata(
 *     context.contentResolver,
 *     contractAddress = "0x123...",
 *     chainId = 1,
 *     decimals = 18,
 *     name = "My Token",
 *     symbol = "MTK",
 *     logo = "https://...",
 *     swappable = true
 * )
 * ```
 */
object TokenMetadataProviderContract {
    
    const val AUTHORITY = "com.walletmanager.tokenmetadata.provider"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
    
    // Column names
    const val COLUMN_CONTRACT_ADDRESS = "contract_address"
    const val COLUMN_DECIMALS = "decimals"
    const val COLUMN_NAME = "name"
    const val COLUMN_SYMBOL = "symbol"
    const val COLUMN_LOGO = "logo"
    const val COLUMN_CHAIN_ID = "chain_id"
    const val COLUMN_SWAPPABLE = "swappable"
    const val COLUMN_PRICE = "price"
    
    /**
     * Data class representing token metadata
     */
    data class TokenMetadataData(
        val contractAddress: String,
        val decimals: Int,
        val name: String,
        val symbol: String,
        val logo: String?,
        val chainId: Int,
        val swappable: Boolean,
        val price: Double
    )
    
    /**
     * Build URI for querying a specific token
     */
    fun buildTokenUri(chainId: Int, contractAddress: String): Uri {
        return CONTENT_URI.buildUpon()
            .appendPath("token")
            .appendPath(chainId.toString())
            .appendPath(contractAddress)
            .build()
    }
    
    /**
     * Build URI for querying all tokens of a specific chain
     */
    fun buildTokensByChainUri(chainId: Int): Uri {
        return CONTENT_URI.buildUpon()
            .appendPath("tokens")
            .appendPath(chainId.toString())
            .build()
    }
    
    /**
     * Build URI for upsert operations
     */
    fun buildUpsertUri(): Uri {
        return CONTENT_URI.buildUpon()
            .appendPath("token")
            .build()
    }
    
    /**
     * Helper method to query token metadata from another app
     */
    fun getTokenMetadata(
        contentResolver: android.content.ContentResolver,
        chainId: Int,
        contractAddress: String
    ): TokenMetadataData? {
        val uri = buildTokenUri(chainId, contractAddress)
        val cursor = contentResolver.query(uri, null, null, null, null)
        
        return cursor?.use {
            if (it.moveToFirst()) {
                TokenMetadataData(
                    contractAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTRACT_ADDRESS)),
                    decimals = it.getInt(it.getColumnIndexOrThrow(COLUMN_DECIMALS)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    symbol = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMBOL)),
                    logo = it.getString(it.getColumnIndexOrThrow(COLUMN_LOGO)),
                    chainId = it.getInt(it.getColumnIndexOrThrow(COLUMN_CHAIN_ID)),
                    swappable = it.getInt(it.getColumnIndexOrThrow(COLUMN_SWAPPABLE)) == 1,
                    price = it.getDouble(it.getColumnIndexOrThrow(COLUMN_PRICE))
                )
            } else {
                null
            }
        }
    }
    
    /**
     * Helper method to query all tokens for a chain from another app
     */
    fun getTokensByChain(
        contentResolver: android.content.ContentResolver,
        chainId: Int
    ): List<TokenMetadataData> {
        val uri = buildTokensByChainUri(chainId)
        val cursor = contentResolver.query(uri, null, null, null, null)
        
        return cursor?.use {
            val tokens = mutableListOf<TokenMetadataData>()
            while (it.moveToNext()) {
                tokens.add(
                    TokenMetadataData(
                        contractAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTRACT_ADDRESS)),
                        decimals = it.getInt(it.getColumnIndexOrThrow(COLUMN_DECIMALS)),
                        name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                        symbol = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMBOL)),
                        logo = it.getString(it.getColumnIndexOrThrow(COLUMN_LOGO)),
                        chainId = it.getInt(it.getColumnIndexOrThrow(COLUMN_CHAIN_ID)),
                        swappable = it.getInt(it.getColumnIndexOrThrow(COLUMN_SWAPPABLE)) == 1,
                        price = it.getDouble(it.getColumnIndexOrThrow(COLUMN_PRICE))
                    )
                )
            }
            tokens
        } ?: emptyList()
    }
    
    /**
     * Helper method to upsert token metadata from another app.
     * This will also automatically create or update the associated TokenGroup.
     * Returns true if successful, false otherwise.
     * 
     * @param contentResolver The ContentResolver to use
     * @param contractAddress The token's contract address
     * @param chainId The chain ID where the token exists
     * @param decimals Number of decimal places
     * @param name Token name
     * @param symbol Token symbol (e.g., "ETH", "USDC")
     * @param logo Optional logo URL
     * @param swappable Whether the token is swappable (default: false)
     */
    fun upsertTokenMetadata(
        contentResolver: android.content.ContentResolver,
        contractAddress: String,
        chainId: Int,
        decimals: Int,
        name: String,
        symbol: String,
        logo: String? = null,
        swappable: Boolean = false
    ): Boolean {
        val uri = buildUpsertUri()
        val values = android.content.ContentValues().apply {
            put(COLUMN_CONTRACT_ADDRESS, contractAddress)
            put(COLUMN_CHAIN_ID, chainId)
            put(COLUMN_DECIMALS, decimals)
            put(COLUMN_NAME, name)
            put(COLUMN_SYMBOL, symbol)
            put(COLUMN_LOGO, logo)
            put(COLUMN_SWAPPABLE, if (swappable) 1 else 0)
        }
        
        val resultUri = contentResolver.insert(uri, values)
        return resultUri != null
    }
} 