package com.core.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.core.data.BuildConfig
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainIdToRPC
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.core.data.swap.ZeroXSwapQuoteResponse
import com.core.data.swap.ZeroXTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * ContentProvider to get swap transaction data (to, data, value) for other apps to execute swaps.
 * Uses the 0x API to get the transaction parameters needed to execute a swap.
 * 
 * This provider returns a list of TxParams (to, value, data) that should be executed in order.
 * If an approval is needed (for ERC20 tokens), the approval transaction will be the first row.
 * 
 * URI patterns:
 * - content://[authority]/swapdata?sellToken=...&buyToken=...&sellAmount=...&chainId=...&sellDecimals=...&buyDecimals=...&sellSymbol=...&buySymbol=...
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
 * Returns cursor with rows representing TxParams to execute in order:
 * - to: Target address for the transaction
 * - value: ETH value to send (in wei, as string)
 * - data: The calldata for the transaction
 * - tx_type: "approval" or "swap" - indicates the transaction type
 * - tx_index: 0-based index of this transaction in the list
 * - tx_count: Total number of transactions to execute
 * - buy_amount: Expected amount to receive (only populated on swap row)
 * - sell_amount: Amount being sold (only populated on swap row)
 * - error: Error message if any, empty string on success
 * 
 * Example usage from other app:
 * ```
 * val uri = Uri.parse("content://com.walletmanager.swapdata.provider/swapdata")
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
 * val txList = mutableListOf<TxParams>()
 * cursor?.use {
 *     while (it.moveToNext()) {
 *         val error = it.getString(it.getColumnIndexOrThrow("error"))
 *         if (error.isEmpty()) {
 *             txList.add(TxParams(
 *                 to = it.getString(it.getColumnIndexOrThrow("to")),
 *                 value = it.getString(it.getColumnIndexOrThrow("value")),
 *                 data = it.getString(it.getColumnIndexOrThrow("data"))
 *             ))
 *         }
 *     }
 * }
 * // Execute all transactions in order
 * for (tx in txList) {
 *     wallet.sendTransaction(to = tx.to, value = tx.value, data = tx.data)
 * }
 * ```
 */
class SwapDataContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "SwapDataProvider"
        const val AUTHORITY = "com.walletmanager.swapdata.provider"
        
        private const val ZEROX_API_BASE_URL = "https://api.0x.org"
        private const val ZEROX_API_VERSION = "v2"
        private const val ETH_TOKEN_ADDRESS = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"
        private const val SWAP_FEE_BPS = 15 // 0.15%
        private const val SWAP_FEE_RECIPIENT = "0xF1F39090D2bE5010Cc1Dd633b6dCe476A38b5675"
        private val ZEROX_SUPPORTED_CHAIN_IDS = setOf(1, 10, 137, 42161, 8453)
        
        // URI codes
        private const val SWAP_DATA = 1
        
        // Column names - matches WalletSDK.TxParams structure
        const val COLUMN_TO = "to"
        const val COLUMN_VALUE = "value"
        const val COLUMN_DATA = "data"
        const val COLUMN_TX_TYPE = "tx_type" // "approval" or "swap"
        const val COLUMN_TX_INDEX = "tx_index"
        const val COLUMN_TX_COUNT = "tx_count"
        const val COLUMN_BUY_AMOUNT = "buy_amount"
        const val COLUMN_SELL_AMOUNT = "sell_amount"
        const val COLUMN_ERROR = "error"
        
        val COLUMNS = arrayOf(
            COLUMN_TO,
            COLUMN_VALUE,
            COLUMN_DATA,
            COLUMN_TX_TYPE,
            COLUMN_TX_INDEX,
            COLUMN_TX_COUNT,
            COLUMN_BUY_AMOUNT,
            COLUMN_SELL_AMOUNT,
            COLUMN_ERROR
        )
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "swapdata", SWAP_DATA)
        }
    }
    
    private val web3jByChain = ConcurrentHashMap<Int, Web3j>()
    private val walletSdkByChain = ConcurrentHashMap<Int, WalletSDK>()
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        Log.d(TAG, "SwapDataContentProvider initialized")
        return true
    }
    
    private fun isZeroXSupported(chainId: Int): Boolean =
        ZEROX_SUPPORTED_CHAIN_IDS.contains(chainId)
    
    private fun resolveRpcUrl(chainId: Int): String? = try {
        chainIdToRPC(chainId)
    } catch (t: Throwable) {
        Log.e(TAG, "Unable to resolve RPC for chainId=$chainId", t)
        null
    }
    
    private suspend fun getWalletSdkForChain(chainId: Int): WalletSDK? {
        val ctx = context ?: return null
        
        if (!isZeroXSupported(chainId)) {
            Log.e(TAG, "0x swap is not available on chainId=$chainId")
            return null
        }

        val rpcUrl = resolveRpcUrl(chainId)
        if (rpcUrl == null) {
            Log.e(TAG, "RPC URL resolution failed for chainId=$chainId")
            return null
        }

        val bundlerUrl = chainIdToBundler(chainId)
        val web3j = web3jByChain.getOrPut(chainId) {
            Web3j.build(HttpService(rpcUrl))
        }

        val sdk = walletSdkByChain.getOrPut(chainId) {
            WalletSDK(
                context = ctx,
                web3jInstance = web3j,
                bundlerRPCUrl = bundlerUrl
            )
        }

        try {
            val currentChainId = sdk.getChainId()
            if (currentChainId != chainId) {
                sdk.changeChain(chainId, rpcUrl, bundlerUrl)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to update WalletSDK chain to $chainId", t)
        }

        return sdk
    }
    
    private fun isEthLike(address: String?, symbol: String?, chainId: Int): Boolean {
        if (address == null) return true
        return address == "0x0000000000000000000000000000000000000000" ||
                address.equals(ETH_TOKEN_ADDRESS, ignoreCase = true) ||
                address == chainId.toString() ||
                (symbol != null && symbol.equals("ETH", ignoreCase = true))
    }
    
    private fun buildApprovalTransaction(
        tokenAddress: String,
        spenderAddress: String
    ): ZeroXTransaction {
        val approvalAmount = BigInteger("2").pow(256).subtract(BigInteger.ONE)
        val function = Function(
            "approve",
            listOf(Address(spenderAddress), Uint256(approvalAmount)),
            emptyList<TypeReference<*>>()
        )
        val encodedFunction = FunctionEncoder.encode(function)
        return ZeroXTransaction(
            to = tokenAddress,
            data = encodedFunction,
            value = "0",
            gas = "100000",
            gasPrice = null,
            from = null
        )
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
            SWAP_DATA -> handleSwapDataQuery(uri, cursor)
            else -> {
                Log.w(TAG, "Unknown URI: $uri")
                return null
            }
        }
        
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }
    
    private fun handleSwapDataQuery(uri: Uri, cursor: MatrixCursor) {
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
        
        Log.d(TAG, "Getting swap data: sellToken=$sellToken, buyToken=$buyToken, amount=$sellAmount, chainId=$chainId")
        
        runBlocking {
            try {
                val walletSDK = getWalletSdkForChain(chainId)
                if (walletSDK == null) {
                    addErrorRow(cursor, "Unsupported chain: $chainId")
                    return@runBlocking
                }
                
                val isSellingETH = isEthLike(sellToken, sellSymbol, chainId)
                val isBuyingETH = isEthLike(buyToken, buySymbol, chainId)
                
                // Convert amount to smallest unit
                val sellAmountWei = if (isSellingETH) {
                    Convert.toWei(sellAmount, Convert.Unit.ETHER).toBigInteger()
                } else {
                    sellAmount.multiply(BigDecimal.TEN.pow(sellDecimals)).toBigInteger()
                }
                
                // Normalize token addresses for 0x API
                val normalizedSellToken = if (isSellingETH) ETH_TOKEN_ADDRESS else sellToken
                val normalizedBuyToken = if (isBuyingETH) ETH_TOKEN_ADDRESS else buyToken
                
                val taker = walletSDK.getAddress()
                
                // Build the API URL
                val url = "$ZEROX_API_BASE_URL/swap/allowance-holder/quote" +
                    "?chainId=$chainId" +
                    "&sellToken=$normalizedSellToken" +
                    "&buyToken=$normalizedBuyToken" +
                    "&sellAmount=$sellAmountWei" +
                    "&taker=$taker" +
                    "&swapFeeRecipient=$SWAP_FEE_RECIPIENT" +
                    "&swapFeeBps=$SWAP_FEE_BPS" +
                    "&swapFeeToken=$normalizedSellToken"
                
                Log.d(TAG, "Calling 0x API: $url")
                
                val quote = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("0x-api-key", BuildConfig.ZEROX_API_KEY)
                        .addHeader("0x-version", ZEROX_API_VERSION)
                        .get()
                        .build()
                    
                    val response = httpClient.newCall(request).execute()
                    val body = response.body?.string()
                    
                    if (!response.isSuccessful || body == null) {
                        Log.e(TAG, "0x API quote failed: ${response.code} - $body")
                        return@withContext null
                    }
                    
                    val adapter = moshi.adapter(ZeroXSwapQuoteResponse::class.java)
                    adapter.fromJson(body)
                }
                
                if (quote == null) {
                    addErrorRow(cursor, "Failed to get quote from 0x API")
                    return@runBlocking
                }
                
                if (quote.liquidityAvailable == false) {
                    addErrorRow(cursor, "No liquidity available for this swap pair")
                    return@runBlocking
                }
                
                // Check for balance issues
                quote.issues?.balance?.let { balanceIssue ->
                    Log.e(TAG, "Balance issue: expected=${balanceIssue.expected}, actual=${balanceIssue.actual}")
                    addErrorRow(cursor, "Insufficient balance: expected ${balanceIssue.expected}, actual ${balanceIssue.actual}")
                    return@runBlocking
                }
                
                // Build transaction list like SwapHandler.executeSwap does
                val txList = mutableListOf<WalletSDK.TxParams>()
                
                // Check if approval is needed (for non-ETH tokens with allowance issues)
                if (!isSellingETH) {
                    quote.issues?.allowance?.let { allowance ->
                        Log.d(TAG, "Allowance issue detected, building approval transaction")
                        val approvalTx = buildApprovalTransaction(
                            tokenAddress = normalizedSellToken,
                            spenderAddress = allowance.spender
                        )
                        txList.add(
                            WalletSDK.TxParams(
                                to = approvalTx.to,
                                value = "0",
                                data = approvalTx.data
                            )
                        )
                    }
                }
                
                // Add the swap transaction
                txList.add(
                    WalletSDK.TxParams(
                        to = quote.transaction.to,
                        value = quote.transaction.value,
                        data = quote.transaction.data
                    )
                )
                
                Log.d(TAG, "Built ${txList.size} transaction(s)")
                
                // Add rows to cursor
                val txCount = txList.size
                txList.forEachIndexed { index, txParams ->
                    val isSwapTx = index == txList.lastIndex
                    val txType = if (isSwapTx) "swap" else "approval"
                    
                    cursor.addRow(
                        arrayOf<Any?>(
                            txParams.to,
                            txParams.value,
                            txParams.data,
                            txType,
                            index,
                            txCount,
                            if (isSwapTx) quote.buyAmount else "",
                            if (isSwapTx) quote.sellAmount else "",
                            "" // No error
                        )
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting swap data", e)
                addErrorRow(cursor, "Error: ${e.message}")
            }
        }
    }
    
    private fun addErrorRow(cursor: MatrixCursor, error: String) {
        cursor.addRow(
            arrayOf<Any?>(
                "", "", "", "", 0, 0, "", "", error
            )
        )
    }
    
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            SWAP_DATA -> "vnd.android.cursor.dir/vnd.$AUTHORITY.swapdata"
            else -> null
        }
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

