package com.core.domain

import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.pow

class GetTokenAssetsBySymbolUseCase @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenBalanceRepository: TokenBalanceRepository
) {
    operator fun invoke(symbol: String = ""): Flow<List<TokenAsset>> =
        combine(
            tokenBalanceRepository.getTokensBalances(),
            tokenMetadataRepository.getTokensMetadata()
        ) { balance, metadata ->
            val metadataMap = metadata.associateBy { it.contractAddress.lowercase() }
            val pairedList = balance.mapNotNull { tokenBalance ->
                metadataMap[tokenBalance.contractAddress.lowercase()]?.let { tokenMetadata ->
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

            if(symbol != "") {
                tokenAssets.filter {
                    it.symbol == symbol
                }
            } else {
                tokenAssets
            }
        }
}