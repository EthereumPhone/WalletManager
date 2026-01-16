package com.core.domain

import android.util.Log
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
    companion object {
        private const val TAG = "GetSwapTokens"
    }

    operator fun invoke(
        query: String,
        chainId: Int
    ): Flow<List<TokenAsset>> =
        combine(
            tokenMetadataRepository.getTokensMetadata(chainId),
            tokenBalanceRepository.getTokensBalances(chainId),
            networkBalanceRepository.getNetworkBalance(chainId)
        ) { metadata, erc20Amount, networkAmount ->
            Log.d(TAG, "=== GetSwapTokens combine triggered ===")
            Log.d(TAG, "ChainId: $chainId, Query: '$query'")
            Log.d(TAG, "Metadata count from DB: ${metadata.size}")
            Log.d(TAG, "Swappable tokens in metadata: ${metadata.count { it.swappable }}")
            
            // Log first few metadata entries
            metadata.filter { it.swappable }.take(5).forEach { m ->
                Log.d(TAG, "  DB Token: ${m.symbol} (${m.name}) swappable=${m.swappable}")
            }
            
            // Check specifically for common tokens like USDC
            val usdc = metadata.find { it.symbol.equals("USDC", ignoreCase = true) }
            if (usdc != null) {
                Log.d(TAG, "  USDC found in DB: ${usdc.symbol} (${usdc.name}) swappable=${usdc.swappable}")
            } else {
                Log.w(TAG, "  USDC NOT FOUND in DB for chain $chainId!")
            }
            
            val networkAsset = TokenAsset(
                address = networkAmount.contractAddress,
                chainId = networkAmount.chainId,
                symbol = if (chainId == 137) "MATIC" else "ETH" ,
                name = if (chainId == 137) "Matic" else "Ether",
                balance = networkAmount.tokenBalance.toDouble(),
                decimals = 18,
                logoUrl = null
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
                    balance = (truncatedValue?.div(scale))?.toDouble() ?: 0.0,
                    decimals = tokenMetadata.decimals, 
                    swappable = true, // All tokens in the swap selector should be swappable
                    logoUrl = tokenMetadata.logo
                )
            }
                .distinctBy { it.address }
            
            val result = if(query.isEmpty()) {
                listOf(networkAsset) + erc20Assets
            } else {
                (listOf(networkAsset) + erc20Assets).filter { it.name.contains(query, ignoreCase = true) || it.symbol.contains(query, ignoreCase = true) }
            }
            
            Log.d(TAG, "Returning ${result.size} tokens (query='$query')")
            result
        }
}
