package com.core.model.exchange

import com.core.model.TokenAsset
import java.math.BigDecimal

data class ExchangeUiState(
    val isLoading: Boolean = false,
    val isSwapping: Boolean = false,
    val error: String? = null,
    val fromAsset: TokenAsset? = null,
    val toAsset: TokenAsset? = null,
    val availableAssets: List<TokenAsset> = emptyList(),
    val balances: Map<String, BigDecimal> = emptyMap(),
    val prices: Map<String, Double> = emptyMap(),
    val fromAmount: String = "",
    val toAmount: String = "",
    val selectedChain: Int = 1,
    val availableChains: List<Int> = listOf(1, 10),
    val showFromPicker: Boolean = false,
    val showToPicker: Boolean = false,
    val exchangeRate: String = "",
    val priceImpact: String = "",
    val needsApproval: Boolean = false,
    val isApproving: Boolean = false,
    val gasEstimation: GasEstimation? = null,
    val selectedGasTier: GasTier = GasTier.MEDIUM,
    val isLoadingGasEstimate: Boolean = false,
    val fiatPrice: Double? = null
)

data class ExchangeQuote(
    val tokenIn: String,
    val tokenOut: String,
    val amountIn: BigDecimal,
    val expectedOut: BigDecimal,
    val minOut: BigDecimal,
    val pricePerToken: BigDecimal,
    val slippage: Int,
    val priceImpact: Double,
    val gasEstimate: GasEstimation?,
    val chainId: Int,
    val isNativeIn: Boolean,
    val requiresApproval: Boolean
)

sealed class ExchangeResult {
    data class Success(val txHash: String, val amountOut: BigDecimal, val gasUsed: BigDecimal? = null) : ExchangeResult()
    data class Error(val message: String, val cause: Throwable? = null) : ExchangeResult()
    data object UserDeclined : ExchangeResult()
}

data class ExchangeAsset(
    val token: TokenAsset,
    val balance: BigDecimal,
    val price: Double?,
    val usdValue: BigDecimal
)

data class GasEstimation(
    val gasLimit: BigDecimal,
    val gasPrice: BigDecimal,
    val maxFee: BigDecimal? = null
)

enum class GasTier { LOW, MEDIUM, HIGH }

data class ChainInfo(
    val chainId: Int,
    val name: String,
    val rpcUrl: String
)


