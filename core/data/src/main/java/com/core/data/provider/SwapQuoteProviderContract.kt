package com.core.data.provider

import android.content.ContentResolver
import android.net.Uri
import java.math.BigDecimal

/**
 * Contract class for SwapQuoteContentProvider.
 * 
 * This class can be copied to consuming apps to easily query swap quotes.
 * 
 * Example usage:
 * ```
 * val quote = SwapQuoteProviderContract.getSwapQuote(
 *     contentResolver = context.contentResolver,
 *     sellToken = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", // USDC
 *     buyToken = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",  // WETH
 *     sellAmount = BigDecimal("100"),
 *     chainId = 1,
 *     sellDecimals = 6,
 *     buyDecimals = 18,
 *     sellSymbol = "USDC",
 *     buySymbol = "WETH"
 * )
 * 
 * if (quote != null && quote.error.isEmpty()) {
 *     Log.d("Swap", "Expected output: ${quote.buyAmount}")
 *     Log.d("Swap", "Price: ${quote.price}")
 * }
 * ```
 */
object SwapQuoteProviderContract {
    
    const val AUTHORITY = "com.walletmanager.swapquote.provider"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
    
    // Column names
    const val COLUMN_SELL_TOKEN = "sell_token"
    const val COLUMN_BUY_TOKEN = "buy_token"
    const val COLUMN_SELL_AMOUNT = "sell_amount"
    const val COLUMN_BUY_AMOUNT = "buy_amount"
    const val COLUMN_MIN_BUY_AMOUNT = "min_buy_amount"
    const val COLUMN_PRICE = "price"
    const val COLUMN_GUARANTEED_PRICE = "guaranteed_price"
    const val COLUMN_ESTIMATED_PRICE_IMPACT = "estimated_price_impact"
    const val COLUMN_LIQUIDITY_AVAILABLE = "liquidity_available"
    const val COLUMN_GAS = "gas"
    const val COLUMN_GAS_PRICE = "gas_price"
    const val COLUMN_TOTAL_NETWORK_FEE = "total_network_fee"
    const val COLUMN_ALLOWANCE_TARGET = "allowance_target"
    const val COLUMN_CHAIN_ID = "chain_id"
    const val COLUMN_ERROR = "error"
    
    /**
     * Data class representing a swap quote
     */
    data class SwapQuoteData(
        val sellToken: String,
        val buyToken: String,
        val sellAmount: String,
        val buyAmount: String,
        val minBuyAmount: String,
        val price: String,
        val guaranteedPrice: String,
        val estimatedPriceImpact: String,
        val liquidityAvailable: Boolean,
        val gas: String,
        val gasPrice: String,
        val totalNetworkFee: String,
        val allowanceTarget: String,
        val chainId: Int,
        val error: String
    ) {
        val isSuccess: Boolean get() = error.isEmpty()
    }
    
    /**
     * Build URI for querying a swap quote
     */
    fun buildQuoteUri(
        sellToken: String,
        buyToken: String,
        sellAmount: BigDecimal,
        chainId: Int,
        sellDecimals: Int,
        buyDecimals: Int,
        sellSymbol: String = "",
        buySymbol: String = ""
    ): Uri {
        return CONTENT_URI.buildUpon()
            .appendPath("quote")
            .appendQueryParameter("sellToken", sellToken)
            .appendQueryParameter("buyToken", buyToken)
            .appendQueryParameter("sellAmount", sellAmount.toPlainString())
            .appendQueryParameter("chainId", chainId.toString())
            .appendQueryParameter("sellDecimals", sellDecimals.toString())
            .appendQueryParameter("buyDecimals", buyDecimals.toString())
            .appendQueryParameter("sellSymbol", sellSymbol)
            .appendQueryParameter("buySymbol", buySymbol)
            .build()
    }
    
    /**
     * Get a swap quote from WalletManager
     * 
     * @param contentResolver The content resolver to use
     * @param sellToken Token address to sell
     * @param buyToken Token address to buy
     * @param sellAmount Amount to sell (human-readable, e.g. "100" for 100 USDC)
     * @param chainId Chain ID (1 for Ethereum mainnet, 10 for Optimism, etc.)
     * @param sellDecimals Decimals of the sell token
     * @param buyDecimals Decimals of the buy token
     * @param sellSymbol Symbol of sell token (helps with ETH detection)
     * @param buySymbol Symbol of buy token (helps with ETH detection)
     * @return SwapQuoteData or null if query failed
     */
    fun getSwapQuote(
        contentResolver: ContentResolver,
        sellToken: String,
        buyToken: String,
        sellAmount: BigDecimal,
        chainId: Int,
        sellDecimals: Int,
        buyDecimals: Int,
        sellSymbol: String = "",
        buySymbol: String = ""
    ): SwapQuoteData? {
        val uri = buildQuoteUri(
            sellToken = sellToken,
            buyToken = buyToken,
            sellAmount = sellAmount,
            chainId = chainId,
            sellDecimals = sellDecimals,
            buyDecimals = buyDecimals,
            sellSymbol = sellSymbol,
            buySymbol = buySymbol
        )
        
        val cursor = contentResolver.query(uri, null, null, null, null)
        
        return cursor?.use {
            if (it.moveToFirst()) {
                SwapQuoteData(
                    sellToken = it.getString(it.getColumnIndexOrThrow(COLUMN_SELL_TOKEN)),
                    buyToken = it.getString(it.getColumnIndexOrThrow(COLUMN_BUY_TOKEN)),
                    sellAmount = it.getString(it.getColumnIndexOrThrow(COLUMN_SELL_AMOUNT)),
                    buyAmount = it.getString(it.getColumnIndexOrThrow(COLUMN_BUY_AMOUNT)),
                    minBuyAmount = it.getString(it.getColumnIndexOrThrow(COLUMN_MIN_BUY_AMOUNT)),
                    price = it.getString(it.getColumnIndexOrThrow(COLUMN_PRICE)),
                    guaranteedPrice = it.getString(it.getColumnIndexOrThrow(COLUMN_GUARANTEED_PRICE)),
                    estimatedPriceImpact = it.getString(it.getColumnIndexOrThrow(COLUMN_ESTIMATED_PRICE_IMPACT)),
                    liquidityAvailable = it.getInt(it.getColumnIndexOrThrow(COLUMN_LIQUIDITY_AVAILABLE)) == 1,
                    gas = it.getString(it.getColumnIndexOrThrow(COLUMN_GAS)),
                    gasPrice = it.getString(it.getColumnIndexOrThrow(COLUMN_GAS_PRICE)),
                    totalNetworkFee = it.getString(it.getColumnIndexOrThrow(COLUMN_TOTAL_NETWORK_FEE)),
                    allowanceTarget = it.getString(it.getColumnIndexOrThrow(COLUMN_ALLOWANCE_TARGET)),
                    chainId = it.getInt(it.getColumnIndexOrThrow(COLUMN_CHAIN_ID)),
                    error = it.getString(it.getColumnIndexOrThrow(COLUMN_ERROR))
                )
            } else {
                null
            }
        }
    }
}

