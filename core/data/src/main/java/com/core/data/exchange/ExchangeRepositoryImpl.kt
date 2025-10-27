package com.core.data.exchange

import com.core.data.repository.SwapRepository
import com.core.data.repository.UserDataRepository
import com.core.model.exchange.ExchangeQuote
import com.core.model.exchange.ExchangeResult
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class ExchangeRepositoryImpl @Inject constructor(
    private val swapRepository: SwapRepository,
    private val userDataRepository: UserDataRepository
) : ExchangeRepository {

    override suspend fun getQuote(
        tokenInAddr: String,
        tokenOutAddr: String,
        amountIn: BigDecimal,
        decimalsIn: Int,
        chainId: Int,
        slippageBps: Int
    ): ExchangeQuote {
        val addr = userDataRepository.userData.first().walletAddress
        val normalizedAmount = amountIn.movePointLeft(decimalsIn).toDouble()
        val expectedOut = swapRepository.getQuote(
            inputTokenAddress = tokenInAddr,
            outputTokenAddress = tokenOutAddr,
            amount = normalizedAmount,
            receiverAddress = addr,
            chainId = chainId
        )
        val expectedOutBd = BigDecimal.valueOf(expectedOut)
        return ExchangeQuote(
            tokenIn = tokenInAddr,
            tokenOut = tokenOutAddr,
            amountIn = amountIn,
            expectedOut = expectedOutBd,
            minOut = expectedOutBd, // TODO: apply slippage
            pricePerToken = if (amountIn.signum() == 0) BigDecimal.ZERO else expectedOutBd.divide(amountIn, 18, java.math.RoundingMode.HALF_UP),
            slippage = slippageBps,
            priceImpact = 0.0,
            gasEstimate = null,
            chainId = chainId,
            isNativeIn = tokenInAddr == "1" || tokenInAddr == "10",
            requiresApproval = tokenInAddr != "1" && tokenInAddr != "10"
        )
    }

    override suspend fun swap(quote: ExchangeQuote): ExchangeResult {
        return try {
            val tx = swapRepository.swap(
                inputTokenAddress = quote.tokenIn,
                outputTokenAddress = quote.tokenOut,
                amount = quote.amountIn.movePointLeft(18).toDouble() // Using 18 as a safe default
            )
            if (tx == org.ethereumphone.walletsdk.WalletSDK.DECLINE) {
                ExchangeResult.UserDeclined
            } else {
                ExchangeResult.Success(txHash = tx, amountOut = quote.expectedOut, gasUsed = null)
            }
        } catch (t: Throwable) {
            ExchangeResult.Error(t.message ?: "Swap failed", t)
        }
    }
}


