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
    }

    override suspend fun queryTokens(q: String) = coroutineScope {
        val results = api.searchTokens(q).groupBy { it.chainId }

        Log.d(TAG, "FETCHED ${results.toString()}")

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