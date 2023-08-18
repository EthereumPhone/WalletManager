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
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateTokensUseCase @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenBalanceRepository: TokenBalanceRepository,
    private val networkBalanceRepository: NetworkBalanceRepository
) {
    suspend operator fun invoke(
        toAddress: String
    ) {
        Log.d("UpdateUseCase", toAddress)
        tokenBalanceRepository.refreshTokensBalances(toAddress)
        networkBalanceRepository.refreshNetworkBalance(toAddress)

        val balancesAddresses = tokenBalanceRepository.getTokensBalances()
        val metadataAddresses = tokenMetadataRepository.getTokensMetadata()

        /*
        This portion filters out TokenMetadata that are already present in the db,
        as they don't need to be updated
        */
        val metadataToFetch = combine(
            balancesAddresses,
            metadataAddresses.map { metadataList -> metadataList.map { it.contractAddress } }
        ) { balances, metadata ->
            balances.filter {
                !metadata.contains(it.contractAddress)
            }.groupBy { it.chainId }
        }

        /*
         The Alchemy metadata api has different endpoints for each network.
         If one tries to pass an erc20 address to the wrong endpoint,
         it will return empty tokenMetadata DAOs.
         For this reason, I need to group the addresses to the right chainID first,
         so that the repository can map them to the right alchemy endpoint.
        */
        coroutineScope {
            metadataToFetch.map { fetchGroups ->
                fetchGroups.entries.map { (chainId, metadata) ->
                    async {
                        tokenMetadataRepository.refreshTokensMetadata(
                            metadata.map { it.contractAddress },
                            chainId
                        )
                    }
                }
            }
        }
    }
}