package com.core.data.repository

import android.util.Log
import com.core.data.remote.UniswapApi
import com.core.model.TokenMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
import org.ethosmobile.uniswap_routing_sdk.Token
import org.ethosmobile.uniswap_routing_sdk.UniswapRoutingSDK
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SwapRepositoryImp @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val uniswapApi: UniswapApi,
): SwapRepository {

    override suspend fun getQuote(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double,
        receiverAddress: String,
        chainId: Int
        ): Double {
        val tokenMetadataList = tokenMetadataRepository.getTokensMetadata(listOf(inputTokenAddress, outputTokenAddress))
            .first()


        val (inputToken, outputToken) = when {
            tokenMetadataList.size < 2 -> {
                when {
                    (inputTokenAddress == "1" || inputTokenAddress == "10") -> UniswapRoutingSDK.ETH_MAINNET to toToken(tokenMetadataList[0])
                    (outputTokenAddress == "1" || outputTokenAddress == "10") -> toToken(tokenMetadataList[0]) to UniswapRoutingSDK.ETH_MAINNET
                    else -> throw IllegalArgumentException("Token metadata could not be retrieved")
                }
            }
            else -> toToken(tokenMetadataList[0]) to toToken(tokenMetadataList[1])
        }

        return withContext(Dispatchers.IO) {
            uniswapApi.getQuote(
                inputToken,
                outputToken,
                amount,
                receiverAddress,
                chainId
            )
        }
    }

    override suspend fun swap(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double
    ): String = withContext(Dispatchers.IO) {
        val tokenMetadataList = tokenMetadataRepository.getTokensMetadata(listOf(inputTokenAddress, outputTokenAddress))
            .first()
        println("TokenMetadatalist: $tokenMetadataList")
        val inputToken = when (inputTokenAddress) {
            "1" -> {
                UniswapRoutingSDK.ETH_MAINNET
            }
            "10" -> {
                UniswapRoutingSDK.ETH_MAINNET
            }
            else -> {
                val inputTokenMetadata = tokenMetadataList.find { it.contractAddress == inputTokenAddress }
                Token(
                    chainId = inputTokenMetadata?.chainId!!,
                    address = inputTokenMetadata.contractAddress,
                    decimals = inputTokenMetadata.decimals,
                    name = inputTokenMetadata.name,
                    symbol = inputTokenMetadata.symbol
                )
            }
        }
        val outputToken = when (outputTokenAddress) {
            "1" -> {
                UniswapRoutingSDK.ETH_MAINNET
            }
            "10" -> {
                UniswapRoutingSDK.ETH_MAINNET
            }
            else -> {
                val outputTokenMetadata = tokenMetadataList.find { it.contractAddress == outputTokenAddress }
                Token(
                    chainId = outputTokenMetadata?.chainId!!,
                    address = outputTokenMetadata.contractAddress,
                    decimals = outputTokenMetadata.decimals,
                    name = outputTokenMetadata.name,
                    symbol = outputTokenMetadata.symbol
                )
            }
        }

        uniswapApi.swap(
            fromToken = inputToken,
            toToken = outputToken,
            amount = amount
        )
    }
}


private fun toToken(
    tokenMetadata: TokenMetadata
) = Token(
    chainId = tokenMetadata.chainId,
    address = tokenMetadata.contractAddress,
    decimals = tokenMetadata.decimals,
    symbol = tokenMetadata.symbol,
    name = tokenMetadata.name
)
