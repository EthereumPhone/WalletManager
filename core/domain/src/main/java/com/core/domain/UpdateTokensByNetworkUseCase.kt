package com.core.domain

import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateTokensByNetworkUseCase @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenBalanceRepository: TokenBalanceRepository,
    private val networkBalanceRepository: NetworkBalanceRepository
) {
    suspend operator fun invoke(address: String, chainId: Int) {
        networkBalanceRepository.refreshNetworkBalanceByNetwork(address, chainId)
        tokenBalanceRepository.refreshTokensBalancesByNetwork(address, chainId)

        val balancesAddresses = tokenBalanceRepository.getTokensBalances(chainId)
            .map { balances -> balances.filter { it.contractAddress.contains("0x") }  }
        val metadataAddresses = tokenMetadataRepository.getTokensMetadata(chainId)
            .map { metadataList -> metadataList.map { it.contractAddress } }

        val metadataToFetch = combine(
            balancesAddresses,
            metadataAddresses
        ) { balances, metadata ->
            balances.filter {
                !metadata.contains(it.contractAddress)
            }
        }.first()

        withContext(Dispatchers.IO) {
            async {
                tokenMetadataRepository.refreshTokensMetadata(
                    metadataToFetch.map { it.contractAddress },
                    chainId
                )
            }
        }
    }
}