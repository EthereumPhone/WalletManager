package com.core.data.provider

import android.content.ContentResolver
import android.net.Uri
import java.math.BigDecimal

/**
 * Contract class for SwapDataContentProvider.
 * 
 * This class can be copied to consuming apps to easily get swap transaction data.
 * Returns a list of TxParams (to, value, data) that should be executed in order.
 * If an approval is needed, the first transaction will be the approval.
 * 
 * Example usage:
 * ```
 * val result = SwapDataProviderContract.getSwapTransactions(
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
 * if (result.isSuccess) {
 *     // Execute all transactions in order
 *     for (tx in result.transactions) {
 *         wallet.sendTransaction(
 *             to = tx.to,
 *             value = tx.value,
 *             data = tx.data
 *         )
 *     }
 *     Log.d("Swap", "Expected output: ${result.buyAmount}")
 * } else {
 *     Log.e("Swap", "Error: ${result.error}")
 * }
 * ```
 */
object SwapDataProviderContract {
    
    const val AUTHORITY = "com.walletmanager.swapdata.provider"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")
    
    // Column names - matches WalletSDK.TxParams structure
    const val COLUMN_TO = "to"
    const val COLUMN_VALUE = "value"
    const val COLUMN_DATA = "data"
    const val COLUMN_TX_TYPE = "tx_type"
    const val COLUMN_TX_INDEX = "tx_index"
    const val COLUMN_TX_COUNT = "tx_count"
    const val COLUMN_BUY_AMOUNT = "buy_amount"
    const val COLUMN_SELL_AMOUNT = "sell_amount"
    const val COLUMN_ERROR = "error"
    
    /**
     * Transaction parameters matching WalletSDK.TxParams
     * Can be directly used with WalletSDK.sendTransaction
     */
    data class TxParams(
        val to: String,
        val value: String,
        val data: String
    )
    
    /**
     * Result of a swap data query containing all transactions to execute
     */
    data class SwapTransactionsResult(
        /** List of transactions to execute in order (approval first if needed, then swap) */
        val transactions: List<TxParams>,
        /** Expected amount to receive (in smallest unit) */
        val buyAmount: String,
        /** Amount being sold (in smallest unit) */
        val sellAmount: String,
        /** Error message if any, empty string on success */
        val error: String
    ) {
        /** Returns true if the query was successful (no error) */
        val isSuccess: Boolean get() = error.isEmpty() && transactions.isNotEmpty()
        
        /** Returns true if an approval transaction is included */
        val hasApproval: Boolean get() = transactions.size > 1
    }
    
    /**
     * Build URI for querying swap data
     */
    fun buildSwapDataUri(
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
            .appendPath("swapdata")
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
     * Get swap transactions from WalletManager.
     * Returns a list of TxParams that should be executed in order.
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
     * @return SwapTransactionsResult containing all TxParams to execute
     */
    fun getSwapTransactions(
        contentResolver: ContentResolver,
        sellToken: String,
        buyToken: String,
        sellAmount: BigDecimal,
        chainId: Int,
        sellDecimals: Int,
        buyDecimals: Int,
        sellSymbol: String = "",
        buySymbol: String = ""
    ): SwapTransactionsResult {
        val uri = buildSwapDataUri(
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
            val transactions = mutableListOf<TxParams>()
            var buyAmountResult = ""
            var sellAmountResult = ""
            var errorResult = ""
            
            while (it.moveToNext()) {
                val error = it.getString(it.getColumnIndexOrThrow(COLUMN_ERROR))
                if (error.isNotEmpty()) {
                    errorResult = error
                    break
                }
                
                val to = it.getString(it.getColumnIndexOrThrow(COLUMN_TO))
                val value = it.getString(it.getColumnIndexOrThrow(COLUMN_VALUE))
                val data = it.getString(it.getColumnIndexOrThrow(COLUMN_DATA))
                val txType = it.getString(it.getColumnIndexOrThrow(COLUMN_TX_TYPE))
                
                transactions.add(TxParams(to = to, value = value, data = data))
                
                // Get buyAmount and sellAmount from the swap transaction (last one)
                if (txType == "swap") {
                    buyAmountResult = it.getString(it.getColumnIndexOrThrow(COLUMN_BUY_AMOUNT))
                    sellAmountResult = it.getString(it.getColumnIndexOrThrow(COLUMN_SELL_AMOUNT))
                }
            }
            
            SwapTransactionsResult(
                transactions = transactions,
                buyAmount = buyAmountResult,
                sellAmount = sellAmountResult,
                error = errorResult
            )
        } ?: SwapTransactionsResult(
            transactions = emptyList(),
            buyAmount = "",
            sellAmount = "",
            error = "Failed to query content provider"
        )
    }
}

