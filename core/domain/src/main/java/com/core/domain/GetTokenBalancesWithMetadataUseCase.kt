package com.core.domain

import android.util.Log
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.pow

class GetTokenBalancesWithMetadataUseCase @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenBalanceRepository: TokenBalanceRepository
) {

    operator fun invoke(): Flow<List<TokenAsset>> =
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
            pairedList.map { (tokenBalance, tokenMetadata) ->
                Log.d("testyTest", tokenMetadata.name)
                Log.d("testyTest", tokenMetadata.contractAddress)
                Log.d("testyTest", tokenMetadata.logo?: "not found")
                Log.d("testyTest", tokenMetadata.swappable.toString())

                TokenAsset(
                    address = tokenBalance.contractAddress,
                    chainId = tokenBalance.chainId,
                    symbol = tokenMetadata.symbol,
                    name = tokenMetadata.name,
                    balance = tokenBalance.tokenBalance.divide(
                        (10.0.pow(tokenMetadata.decimals)).toBigDecimal()).toDouble(),
                    decimals = tokenMetadata.decimals,
                    logoUrl = tokenMetadata.logo,
                    swappable = tokenMetadata.swappable
                )
            }
        }
}