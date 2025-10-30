package com.core.data.repository

import android.util.Log
import com.core.data.remote.UniswapApi
import com.core.model.TokenMetadata
import com.core.data.swap.SwapHandler
import com.core.data.swap.ZeroXSwapQuoteResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
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
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SwapRepositoryImp @Inject constructor(
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val uniswapApi: UniswapApi?,
    @ApplicationContext private val context: Context,
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
            uniswapApi?.getQuote(
                inputToken,
                outputToken,
                amount,
                receiverAddress,
                chainId
            ) ?: 0.0
        }
    }

    override suspend fun getSwapQuote(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: BigDecimal,
        inputTokenDecimals: Int,
        outputTokenDecimals: Int,
        chainId: Int,
        inputTokenSymbol: String,
        outputTokenSymbol: String
    ): ZeroXSwapQuoteResponse? = withContext(Dispatchers.IO) {
        try {
            Log.d("SwapRepositoryImp", "=== getSwapQuote called ===")
            Log.d("SwapRepositoryImp", "Input: $inputTokenAddress ($inputTokenSymbol)")
            Log.d("SwapRepositoryImp", "Output: $outputTokenAddress ($outputTokenSymbol)")
            Log.d("SwapRepositoryImp", "Amount: $amount")
            Log.d("SwapRepositoryImp", "Chain ID: $chainId")
            
            val handler = SwapHandler(context)
            val quote = handler.getSwapQuote(
                fromAddress = inputTokenAddress,
                toAddress = outputTokenAddress,
                fromDecimals = inputTokenDecimals,
                toDecimals = outputTokenDecimals,
                chainId = chainId,
                fromSymbol = inputTokenSymbol,
                toSymbol = outputTokenSymbol,
                fromAmount = amount
            )
            
            if (quote != null) {
                Log.d("SwapRepositoryImp", "✅ Quote fetched successfully: buyAmount=${quote.buyAmount}")
            } else {
                Log.w("SwapRepositoryImp", "⚠️ Quote returned null")
            }
            
            quote
        } catch (e: Exception) {
            Log.e("SwapRepositoryImp", "❌ Failed to get swap quote", e)
            null
        }
    }

    override suspend fun swap(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double
    ): String = withContext(Dispatchers.IO) {
        try {
            val tokenMetadataList = tokenMetadataRepository
                .getTokensMetadata(listOf(inputTokenAddress, outputTokenAddress))
                .first()

            val fromMeta = tokenMetadataList.find { it.contractAddress == inputTokenAddress }
            val toMeta = tokenMetadataList.find { it.contractAddress == outputTokenAddress }

            val isFromEthAlias = (inputTokenAddress == "1" || inputTokenAddress == "10")
            val isToEthAlias = (outputTokenAddress == "1" || outputTokenAddress == "10")

            val fromAddress = if (isFromEthAlias) "0x0000000000000000000000000000000000000000" else (fromMeta?.contractAddress ?: inputTokenAddress)
            val toAddress = if (isToEthAlias) "0x0000000000000000000000000000000000000000" else (toMeta?.contractAddress ?: outputTokenAddress)

            val fromDecimals = fromMeta?.decimals ?: 18
            val toDecimals = toMeta?.decimals ?: 18
            val chainId = (fromMeta?.chainId ?: toMeta?.chainId) ?: 8453

            val fromSymbol = fromMeta?.symbol ?: if (isFromEthAlias) "ETH" else ""
            val toSymbol = toMeta?.symbol ?: if (isToEthAlias) "ETH" else ""

            val handler = SwapHandler(context)
            handler.executeSwap(
                fromAddress = fromAddress,
                toAddress = toAddress,
                fromDecimals = fromDecimals,
                toDecimals = toDecimals,
                chainId = chainId,
                fromSymbol = fromSymbol,
                toSymbol = toSymbol,
                fromAmount = amount.toBigDecimal()
            )
        } catch (e: Exception) {
            Log.e("SwapRepositoryImp", "0x swap failed, returning empty string", e)
            ""
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
