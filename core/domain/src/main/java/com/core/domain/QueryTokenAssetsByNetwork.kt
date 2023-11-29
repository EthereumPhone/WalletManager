package com.core.domain

import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.pow

class QueryTokenAssetsByNetwork @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenBalanceRepository: TokenBalanceRepository
) {

    operator fun invoke(
        chainId: Int = 0,
        query: String
    ): Flow<List<TokenAsset>> =
        combine(
            tokenBalanceRepository.getTokensBalances(),
            tokenMetadataRepository.getTokensMetadata()
        ) { balance, metadata ->
            val pairedList = balance.mapNotNull { tokenBalance ->
                metadata.find {
                    it.contractAddress.lowercase() == tokenBalance.contractAddress.lowercase()
                }?.let { tokenMetadata ->
                    Pair(tokenBalance, tokenMetadata)
                }
            }
            val tokenAssets = pairedList
                .map { (tokenBalance, tokenMetadata) ->
                    TokenAsset(
                        address = tokenBalance.contractAddress,
                        chainId = tokenBalance.chainId,
                        symbol = tokenMetadata.symbol,
                        name = tokenMetadata.name,
                        balance = tokenBalance.tokenBalance.divide(
                            (10.0.pow(tokenMetadata.decimals)).toBigDecimal()).toDouble(),
                        decimals = tokenMetadata.decimals
                    )
                }

            if(chainId != 0) {
                tokenAssets.filter {
                    it.chainId == chainId && it.name.contains(query)
                }
            } else {
                tokenAssets.filter { it.name.contains(query) }
            }
        }
}