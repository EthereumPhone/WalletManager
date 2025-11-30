package com.core.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.core.data.swap.SwapHandler
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

/**
 * ContentProvider to share swap quotes with other apps.
 * Uses the 0x API to get swap quotes without executing the swap.
 * 
 * URI patterns:
 * - content://[authority]/quote?sellToken=...&buyToken=...&sellAmount=...&chainId=...&sellDecimals=...&buyDecimals=...&sellSymbol=...&buySymbol=...
 * 
 * Query parameters:
 * - sellToken: Token address to sell (required)
 * - buyToken: Token address to buy (required)
 * - sellAmount: Amount to sell in human-readable format, e.g. "1.5" (required)
 * - chainId: Chain ID for the swap (required)
 * - sellDecimals: Decimals of the sell token (required)
 * - buyDecimals: Decimals of the buy token (required)
 * - sellSymbol: Symbol of sell token, used for ETH detection (optional, defaults to "")
 * - buySymbol: Symbol of buy token, used for ETH detection (optional, defaults to "")
 * 
 * Example usage from other app:
 * ```
 * val uri = Uri.parse("content://com.walletmanager.swapquote.provider/quote")
 *     .buildUpon()
 *     .appendQueryParameter("sellToken", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48")
 *     .appendQueryParameter("buyToken", "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2")
 *     .appendQueryParameter("sellAmount", "100")
 *     .appendQueryParameter("chainId", "1")
 *     .appendQueryParameter("sellDecimals", "6")
 *     .appendQueryParameter("buyDecimals", "18")
 *     .appendQueryParameter("sellSymbol", "USDC")
 *     .appendQueryParameter("buySymbol", "WETH")
 *     .build()
 * 
 * val cursor = contentResolver.query(uri, null, null, null, null)
 * cursor?.use {
 *     if (it.moveToFirst()) {
 *         val buyAmount = it.getString(it.getColumnIndexOrThrow("buy_amount"))
 *         val price = it.getString(it.getColumnIndexOrThrow("price"))
 *         val estimatedPriceImpact = it.getString(it.getColumnIndexOrThrow("estimated_price_impact"))
 *     }
 * }
 * ```
 */
class SwapQuoteContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "SwapQuoteProvider"
        const val AUTHORITY = "com.walletmanager.swapquote.provider"
        
        // URI codes
        private const val QUOTE = 1
        
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
        
        val COLUMNS = arrayOf(
            COLUMN_SELL_TOKEN,
            COLUMN_BUY_TOKEN,
            COLUMN_SELL_AMOUNT,
            COLUMN_BUY_AMOUNT,
            COLUMN_MIN_BUY_AMOUNT,
            COLUMN_PRICE,
            COLUMN_GUARANTEED_PRICE,
            COLUMN_ESTIMATED_PRICE_IMPACT,
            COLUMN_LIQUIDITY_AVAILABLE,
            COLUMN_GAS,
            COLUMN_GAS_PRICE,
            COLUMN_TOTAL_NETWORK_FEE,
            COLUMN_ALLOWANCE_TARGET,
            COLUMN_CHAIN_ID,
            COLUMN_ERROR
        )
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "quote", QUOTE)
        }
    }
    
    private lateinit var swapHandler: SwapHandler
    
    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        
        // Initialize SwapHandler directly with context
        swapHandler = SwapHandler(ctx)
        
        Log.d(TAG, "SwapQuoteContentProvider initialized")
        return true
    }
    
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor = MatrixCursor(projection ?: COLUMNS)
        
        when (uriMatcher.match(uri)) {
            QUOTE -> handleQuoteQuery(uri, cursor)
            else -> {
                Log.w(TAG, "Unknown URI: $uri")
                return null
            }
        }
        
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }
    
    private fun handleQuoteQuery(uri: Uri, cursor: MatrixCursor) {
        // Extract query parameters
        val sellToken = uri.getQueryParameter("sellToken")
        val buyToken = uri.getQueryParameter("buyToken")
        val sellAmountStr = uri.getQueryParameter("sellAmount")
        val chainIdStr = uri.getQueryParameter("chainId")
        val sellDecimalsStr = uri.getQueryParameter("sellDecimals")
        val buyDecimalsStr = uri.getQueryParameter("buyDecimals")
        val sellSymbol = uri.getQueryParameter("sellSymbol") ?: ""
        val buySymbol = uri.getQueryParameter("buySymbol") ?: ""
        
        // Validate required parameters
        if (sellToken.isNullOrEmpty() || buyToken.isNullOrEmpty() || 
            sellAmountStr.isNullOrEmpty() || chainIdStr.isNullOrEmpty() ||
            sellDecimalsStr.isNullOrEmpty() || buyDecimalsStr.isNullOrEmpty()) {
            Log.e(TAG, "Missing required query parameters")
            addErrorRow(cursor, "Missing required parameters: sellToken, buyToken, sellAmount, chainId, sellDecimals, buyDecimals")
            return
        }
        
        val sellAmount = try {
            BigDecimal(sellAmountStr)
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid sellAmount: $sellAmountStr", e)
            addErrorRow(cursor, "Invalid sellAmount format")
            return
        }
        
        val chainId = chainIdStr.toIntOrNull()
        if (chainId == null) {
            Log.e(TAG, "Invalid chainId: $chainIdStr")
            addErrorRow(cursor, "Invalid chainId format")
            return
        }
        
        val sellDecimals = sellDecimalsStr.toIntOrNull()
        if (sellDecimals == null) {
            Log.e(TAG, "Invalid sellDecimals: $sellDecimalsStr")
            addErrorRow(cursor, "Invalid sellDecimals format")
            return
        }
        
        val buyDecimals = buyDecimalsStr.toIntOrNull()
        if (buyDecimals == null) {
            Log.e(TAG, "Invalid buyDecimals: $buyDecimalsStr")
            addErrorRow(cursor, "Invalid buyDecimals format")
            return
        }
        
        Log.d(TAG, "Getting quote: sellToken=$sellToken, buyToken=$buyToken, amount=$sellAmount, chainId=$chainId")
        
        runBlocking {
            try {
                val quote = swapHandler.getSwapQuote(
                    fromAddress = sellToken,
                    toAddress = buyToken,
                    fromDecimals = sellDecimals,
                    toDecimals = buyDecimals,
                    chainId = chainId,
                    fromSymbol = sellSymbol,
                    toSymbol = buySymbol,
                    fromAmount = sellAmount
                )
                
                if (quote != null) {
                    Log.d(TAG, "Quote received: buyAmount=${quote.buyAmount}")
                    cursor.addRow(
                        arrayOf<Any?>(
                            quote.sellToken,
                            quote.buyToken,
                            quote.sellAmount,
                            quote.buyAmount,
                            quote.minBuyAmount ?: "",
                            quote.price ?: "",
                            quote.guaranteedPrice ?: "",
                            quote.estimatedPriceImpact ?: "",
                            if (quote.liquidityAvailable == true) 1 else 0,
                            quote.gas ?: "",
                            quote.gasPrice ?: "",
                            quote.totalNetworkFee ?: "",
                            quote.allowanceTarget,
                            chainId,
                            "" // No error
                        )
                    )
                } else {
                    Log.w(TAG, "Quote returned null")
                    addErrorRow(cursor, "Failed to get quote - no liquidity or unsupported pair")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting quote", e)
                addErrorRow(cursor, "Error: ${e.message}")
            }
        }
    }
    
    private fun addErrorRow(cursor: MatrixCursor, error: String) {
        cursor.addRow(
            arrayOf<Any?>(
                "", "", "", "", "", "", "", "", 0, "", "", "", "", 0, error
            )
        )
    }
    
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            QUOTE -> "vnd.android.cursor.item/vnd.$AUTHORITY.quote"
            else -> null
        }
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

