package com.core.data.repository

import android.util.Log
import com.core.data.remote.DexScreenerDataSource
import com.core.database.model.erc20.TokenMetadataEntity
import javax.inject.Inject

class DexScreenerSearchRepository @Inject constructor(
    val api: DexScreenerDataSource,
    val tokenMetadataRepository: TokenMetadataRepository,
): SearchRepository {

    companion object {
        private const val TAG = "DexScreenerSearchRepo"
        
        // Supported chain IDs that have RPC endpoints configured
        private val SUPPORTED_CHAIN_IDS = setOf(1, 10, 137, 42161, 8453, 56, 43114)
    }

    override suspend fun queryTokens(q: String) {
        // Default implementation without chain filter
        queryTokens(q, null)
    }
    
    /**
     * Search for tokens by name, symbol, or contract address.
     * 
     * Strategy:
     * 1. If query looks like a contract address AND chainId is provided → direct lookup first
     * 2. Always do a general search to find by name/symbol
     * 3. Insert ALL results from supported chains (UI will filter by selected chain)
     */
    suspend fun queryTokens(q: String, chainId: Int?) {
        Log.d(TAG, "=== Searching DexScreener ===")
        Log.d(TAG, "Query: '$q', Target chain: $chainId")
        
        val metadataList = mutableListOf<TokenMetadataEntity>()
        
        // Check if query looks like a contract address
        val isContractAddress = q.startsWith("0x", ignoreCase = true) && q.length >= 40
        
        // Strategy 1: If it's a contract address and we have a chain, try direct lookup first
        if (isContractAddress && chainId != null && chainId in SUPPORTED_CHAIN_IDS) {
            Log.d(TAG, "Query looks like contract address, trying direct lookup on chain $chainId")
            try {
                val token = api.getTokenByAddress(q, chainId)
                if (token != null) {
                    Log.d(TAG, "Direct lookup found: ${token.symbol} (${token.name})")
                    metadataList.add(
                        TokenMetadataEntity(
                            contractAddress = token.address.lowercase(),
                            chainId = token.chainId,
                            decimals = 18,
                            name = token.name,
                            symbol = token.symbol,
                            logo = null,
                            swappable = true,
                            groupId = null
                        )
                    )
                } else {
                    Log.d(TAG, "Direct lookup returned no results")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Direct lookup failed: ${e.message}")
            }
        }
        
        // Strategy 2: Always do general search (for name/symbol matches)
        // This helps find tokens even if direct lookup fails or query is not an address
        Log.d(TAG, "Performing general search for: '$q'")
        try {
            val allResults = api.searchTokens(q)
            
            // Filter to only supported EVM chains
            val supportedResults = allResults.filter { it.chainId in SUPPORTED_CHAIN_IDS }
            
            Log.d(TAG, "Search returned ${allResults.size} total, ${supportedResults.size} on supported chains")
            
            // Log breakdown by chain
            allResults.groupBy { it.chainId }.forEach { (chain, tokens) ->
                val supported = if (chain in SUPPORTED_CHAIN_IDS) "✓" else "✗"
                Log.d(TAG, "  Chain $chain $supported: ${tokens.size} tokens")
            }

            // Add all supported chain results (avoid duplicates by address+chain)
            val existingKeys = metadataList.map { "${it.contractAddress}_${it.chainId}" }.toSet()
            
            supportedResults.forEach { token ->
                val key = "${token.address.lowercase()}_${token.chainId}"
                if (key !in existingKeys) {
                    metadataList.add(
                        TokenMetadataEntity(
                            contractAddress = token.address.lowercase(),
                            chainId = token.chainId,
                            decimals = 18,
                            name = token.name,
                            symbol = token.symbol,
                            logo = null,
                            swappable = true,
                            groupId = null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "General search failed: ${e.message}")
        }

        Log.d(TAG, "=== Results Summary ===")
        Log.d(TAG, "Total tokens to insert: ${metadataList.size}")
        
        // Log tokens by chain
        metadataList.groupBy { it.chainId }.forEach { (chain, tokens) ->
            Log.d(TAG, "  Chain $chain: ${tokens.size} tokens")
            tokens.take(3).forEach { token ->
                Log.d(TAG, "    - ${token.symbol} (${token.name})")
            }
        }
        
        if (metadataList.isNotEmpty()) {
            tokenMetadataRepository.insertTokenMetadata(metadataList)
            Log.d(TAG, "Successfully inserted ${metadataList.size} tokens to database")
        } else {
            Log.w(TAG, "No tokens found for query: '$q'")
        }
    }
}