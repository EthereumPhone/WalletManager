package com.core.domain

import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.math.pow

class GetSwapTokens @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val tokenBalanceRepository: TokenBalanceRepository
) {

    operator fun invoke(
        query: String
    ): Flow<List<TokenAsset>> =
        combine(
            tokenMetadataRepository.getTokensMetadata(1),
            tokenBalanceRepository.getTokensBalances(1),
            networkBalanceRepository.getNetworkBalance(1)
        ) { metadata, erc20Amount, networkAmount ->
            val networkAsset = TokenAsset(
                address = networkAmount.contractAddress,
                chainId = networkAmount.chainId,
                symbol = "ETH",
                name = "Ether",
                balance = networkAmount.tokenBalance.toDouble()
            )

            val erc20Assets = metadata.map { tokenMetadata ->
                val tokenBalance = erc20Amount.find { tokenMetadata.contractAddress.lowercase() == it.contractAddress.lowercase() }
                val scale = 10.0.pow(tokenMetadata.decimals).toBigDecimal()
                val truncatedValue = tokenBalance?.tokenBalance?.setScale(tokenMetadata.decimals, BigDecimal.ROUND_DOWN)

                TokenAsset(
                    address = tokenMetadata.contractAddress,
                    chainId = tokenMetadata.chainId,
                    symbol = tokenMetadata.symbol,
                    name = tokenMetadata.name,
                    balance = (truncatedValue?.div(scale))?.toDouble() ?: 0.0
                )
            }
            if(query.isEmpty()) {
                listOf(networkAsset) + erc20Assets
            } else {
                (listOf(networkAsset) + erc20Assets).filter { it.name.contains(query, ignoreCase = true) || it.symbol.contains(query, ignoreCase = true) }
            }
        }
}
