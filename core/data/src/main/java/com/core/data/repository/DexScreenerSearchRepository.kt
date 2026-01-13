package com.core.data.repository

import android.util.Log
import com.core.data.remote.DexScreenerDataSource
import com.core.data.service.TokenMetadataFetcher
import com.core.data.util.chainIdToRPC
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class DexScreenerSearchRepository @Inject constructor(
    val api: DexScreenerDataSource,
    val tokenMetadataFetcher: TokenMetadataFetcher,
    val tokenMetadataRepository: TokenMetadataRepository,
): SearchRepository {

    companion object {
        private const val TAG = "DexScreenerSearchRepo"
        
        // Supported chain IDs that have RPC endpoints configured
        private val SUPPORTED_CHAIN_IDS = setOf(1, 10, 137, 42161, 8453, 56, 43114)
    }

    override suspend fun queryTokens(q: String) = coroutineScope {
        val allResults = api.searchTokens(q)
        
        // Filter to only supported chains
        val results = allResults
            .filter { it.chainId in SUPPORTED_CHAIN_IDS }
            .groupBy { it.chainId }

        Log.d(TAG, "FETCHED ${allResults.size} tokens, ${results.values.flatten().size} on supported chains")

        results.forEach { group ->
            async {
                val rpc = chainIdToRPC(group.key)

                val metadataList = group.value.mapNotNull { token ->
                    tokenMetadataFetcher.fetchTokenMetadata(
                        token.address,
                        token.chainId,
                        rpc
                    )?.copy(swappable = true) // Mark as swappable since found on DEX
                }

                Log.d(TAG, "ADDING TO LIST ${metadataList.toString()}")

                tokenMetadataRepository.insertTokenMetadata(metadataList)
            }
        }
    }
}