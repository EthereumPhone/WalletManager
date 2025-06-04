package com.core.domain

import android.util.Log
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateTokensUseCase @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenBalanceRepository: TokenBalanceRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
) {
    companion object {
        private const val TAG = "UpdateTokensUseCase"
    }

    suspend operator fun invoke(
        toAddress: String
    ) {

        tokenBalanceRepository.refreshTokensBalances(toAddress)
        networkBalanceRepository.refreshNetworkBalance(toAddress)

        // Collect the first emission from each flow
        val currentBalances = tokenBalanceRepository.getTokensBalances().first()
            .filter { it.contractAddress.contains("0x") } // skips network currency

        val currentMetadataContractAddresses = tokenMetadataRepository.getTokensMetadata().first()
            .map { it.contractAddress.lowercase() }

        Log.d(TAG, "Collected balances count: ${currentBalances.size}, metadata addresses count: ${currentMetadataContractAddresses.size}")

        val metadataToFetch = currentBalances.filter { balanceItem ->
            !currentMetadataContractAddresses.contains(balanceItem.contractAddress.lowercase())
        }.groupBy { it.chainId }

        Log.d(TAG, "Metadata to fetch by chainId: $metadataToFetch")

        if (metadataToFetch.isEmpty()) {
            Log.d(TAG, "No new metadata to fetch.")
            return // Early exit if nothing to do
        }

        /*
        The Alchemy metadata api has different endpoints for each network.
        If one tries to pass an erc20 address to the wrong endpoint,
        it will return empty tokenMetadata DAOs.
        For this reason, I need to group the addresses to the right chainID first,
        so that the repository can map them to the right alchemy endpoint.
        */

        coroutineScope {
            metadataToFetch.entries.forEach { (chainId, tokensForChain) ->

                async(Dispatchers.IO) { // Explicitly use Dispatchers.IO for network calls
                    val addressesToFetch = tokensForChain.map { it.contractAddress }
                    try {
                        Log.d(TAG, "Refreshing metadata for chainId: $chainId, addresses: $addressesToFetch")
                        tokenMetadataRepository.refreshTokensMetadata(
                            addressesToFetch,
                            chainId
                        )
                        Log.i(TAG, "Successfully refreshed metadata for chainId: $chainId, addresses: $addressesToFetch")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to refresh metadata for chainId: $chainId, addresses: $addressesToFetch", e)
                        // Optionally, collect/report these errors
                    }
                }
            }
        }
    }
}