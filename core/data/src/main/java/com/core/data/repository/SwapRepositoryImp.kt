package com.core.data.repository

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
        ): Double {
        val metadataList = tokenMetadataRepository
            .getTokensMetadata(listOf(inputTokenAddress, outputTokenAddress)).single()

        if (metadataList.size < 2) {
            throw IllegalArgumentException("Token metadata could not be retrieved")
        }

        val inputToken = toToken(metadataList[0])
        val outputToken = toToken(metadataList[1])

        return uniswapApi.getQuote(
            inputToken,
            outputToken,
            amount,
            receiverAddress
        )
    }

    override suspend fun swap(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double
    ): String {
        val metadataList = tokenMetadataRepository
            .getTokensMetadata(listOf(inputTokenAddress, outputTokenAddress)).single()

        if (metadataList.size < 2) {
            throw IllegalArgumentException("Token metadata could not be retrieved")
        }

        val inputToken = toToken(metadataList[0])
        val outputToken = toToken(metadataList[1])

        return withContext(Dispatchers.IO) {
            val inputAddress = if (inputToken.address == "1") UniswapRoutingSDK.ETH_MAINNET else inputToken
            val outputAddress = if (outputToken.address == "1") UniswapRoutingSDK.ETH_MAINNET else outputToken

            uniswapApi.swap(inputAddress, outputAddress, amount)
        }
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
