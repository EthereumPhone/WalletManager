package com.core.data.swap

import android.content.Context
import android.util.Log
import com.core.data.BuildConfig
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainIdToRPC
import com.core.data.utils.GasEstimationHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
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
 * Minimal 0x-based swap handler mirroring TokenLauncher behavior.
 */
class SwapHandler(private val context: Context) {

    companion object {
        private const val TAG = "WM-SwapHandler"
        private const val ZEROX_API_BASE_URL = "https://api.0x.org"
        private const val ZEROX_API_VERSION = "v2"
        private const val ETH_TOKEN_ADDRESS = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"
        private const val SWAP_FEE_BPS = 15 // 0.15%
        private const val SWAP_FEE_RECIPIENT = "0xF1F39090D2bE5010Cc1Dd633b6dCe476A38b5675"
        private val ZEROX_SUPPORTED_CHAIN_IDS = setOf(1, 10, 137, 42161, 8453)
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

    private fun isZeroXSupported(chainId: Int): Boolean =
        ZEROX_SUPPORTED_CHAIN_IDS.contains(chainId)

    private fun resolveRpcUrl(chainId: Int): String? = try {
        chainIdToRPC(chainId)
    } catch (t: Throwable) {
        Log.e(TAG, "Unable to resolve RPC for chainId=$chainId", t)
        null
    }

    private suspend fun getWalletSdkForChain(chainId: Int): WalletSDK? {
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
                context = context,
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

    /**
     * Execute swap via 0x. Returns tx hash on success, or error keyword.
     */
    suspend fun executeSwap(
        fromAddress: String,
        toAddress: String,
        fromDecimals: Int,
        toDecimals: Int,
        chainId: Int,
        fromSymbol: String,
        toSymbol: String,
        fromAmount: BigDecimal
    ): String = withContext(Dispatchers.IO) {
        try {
            val walletSDK = getWalletSdkForChain(chainId)
                ?: return@withContext "ERROR_UNSUPPORTED_CHAIN"

            val isSellingETH = isEthLike(fromAddress, fromSymbol, chainId)
            val isBuyingETH = isEthLike(toAddress, toSymbol, chainId)

            val sellAmount = if (isSellingETH) {
                Convert.toWei(fromAmount, Convert.Unit.ETHER).toBigInteger()
            } else {
                fromAmount.multiply(BigDecimal.TEN.pow(fromDecimals)).toBigInteger()
            }

            val sellToken = if (isSellingETH) ETH_TOKEN_ADDRESS else fromAddress
            val buyToken = if (isBuyingETH) ETH_TOKEN_ADDRESS else toAddress

            val taker = walletSDK.getAddress()

            val url = "$ZEROX_API_BASE_URL/swap/allowance-holder/quote" +
                "?chainId=$chainId" +
                "&sellToken=$sellToken" +
                "&buyToken=$buyToken" +
                "&sellAmount=$sellAmount" +
                "&taker=$taker" +
                "&swapFeeRecipient=$SWAP_FEE_RECIPIENT" +
                "&swapFeeBps=$SWAP_FEE_BPS" +
                "&swapFeeToken=$sellToken"

            val request = Request.Builder()
                .url(url)
                .addHeader("0x-api-key", BuildConfig.ZEROX_API_KEY)
                .addHeader("0x-version", ZEROX_API_VERSION)
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()
            if (!response.isSuccessful || body == null) {
                Log.e(TAG, "0x quote failed: ${response.code} - $body")
                return@withContext "ERROR"
            }

            val adapter = moshi.adapter(ZeroXSwapQuoteResponse::class.java)
            val quote = adapter.fromJson(body) ?: return@withContext "ERROR"

            if (quote.liquidityAvailable == false) {
                Log.e(TAG, "No liquidity for swap")
                return@withContext "ERROR"
            }

            if (!isSellingETH) {
                quote.issues?.allowance?.let { allowance ->
                    quote.allowanceTransaction = buildApprovalTransaction(
                        tokenAddress = sellToken,
                        spenderAddress = allowance.spender
                    )
                }
            }

            val txList = if (quote.allowanceTransaction != null) {
                listOf(
                    WalletSDK.TxParams(
                        to = quote.allowanceTransaction!!.to,
                        value = "0",
                        data = quote.allowanceTransaction!!.data
                    ),
                    WalletSDK.TxParams(
                        to = quote.transaction.to,
                        value = quote.transaction.value,
                        data = quote.transaction.data
                    )
                )
            } else {
                listOf(
                    WalletSDK.TxParams(
                        to = quote.transaction.to,
                        value = quote.transaction.value,
                        data = quote.transaction.data
                    )
                )
            }

            val txHash = walletSDK.sendTransaction(
                txParamsList = txList,
                callGas = null,
                chainId = chainId,
                gasProvider = { userOp -> gasProvider(chainId, userOp) }
            )

            return@withContext when {
                txHash.startsWith("0x") -> txHash
                txHash.equals("decline", ignoreCase = true) -> "DECLINE"
                txHash.contains("AA21") -> "NOT_ENOUGH_GAS"
                else -> "ERROR"
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Swap failed", t)
            return@withContext "ERROR"
        }
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

    private suspend fun gasProvider(chainId: Int, userOp: WalletSDK.UserOperation): WalletSDK.GasEstimation {
        val rpcUrl = resolveRpcUrl(chainId) ?: chainIdToRPC(8453)
        return GasEstimationHelper.estimateGas(userOp, rpcUrl)
    }

    /**
     * Get a swap quote from 0x API to display expected output amount.
     * Does NOT execute the swap - only returns quote information.
     * 
     * @param fromAddress Token address to swap from
     * @param toAddress Token address to swap to
     * @param fromDecimals Decimals of the from token
     * @param toDecimals Decimals of the to token (used for formatting output)
     * @param chainId Chain ID for the swap
     * @param fromSymbol Symbol of from token (for ETH detection)
     * @param toSymbol Symbol of to token (for ETH detection)
     * @param fromAmount Amount to swap (human-readable format)
     * @return ZeroXSwapQuoteResponse with buyAmount and other details, or null if failed
     */
    suspend fun getSwapQuote(
        fromAddress: String,
        toAddress: String,
        fromDecimals: Int,
        toDecimals: Int,
        chainId: Int,
        fromSymbol: String,
        toSymbol: String,
        fromAmount: BigDecimal
    ): ZeroXSwapQuoteResponse? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== Getting Swap Quote ===")
            Log.d(TAG, "From: $fromAddress ($fromSymbol, decimals: $fromDecimals)")
            Log.d(TAG, "To: $toAddress ($toSymbol, decimals: $toDecimals)")
            Log.d(TAG, "Amount: $fromAmount")
            Log.d(TAG, "Chain ID: $chainId")
            
            val walletSDK = getWalletSdkForChain(chainId)
                ?: return@withContext null
            
            // Validate inputs
            if (fromAmount <= BigDecimal.ZERO) {
                Log.w(TAG, "Invalid amount: $fromAmount")
                return@withContext null
            }
            
            val isSellingETH = isEthLike(fromAddress, fromSymbol, chainId)
            val isBuyingETH = isEthLike(toAddress, toSymbol, chainId)
            
            Log.d(TAG, "ETH Detection:")
            Log.d(TAG, "  isSellingETH: $isSellingETH (fromAddress=$fromAddress, fromSymbol=$fromSymbol)")
            Log.d(TAG, "  isBuyingETH: $isBuyingETH (toAddress=$toAddress, toSymbol=$toSymbol)")
            
            // Convert amount to smallest unit
            val sellAmount = if (isSellingETH) {
                Convert.toWei(fromAmount, Convert.Unit.ETHER).toBigInteger()
            } else {
                fromAmount.multiply(BigDecimal.TEN.pow(fromDecimals)).toBigInteger()
            }
            
            Log.d(TAG, "Sell amount (smallest unit): $sellAmount")
            
            // Normalize token addresses for 0x API
            val sellToken = if (isSellingETH) ETH_TOKEN_ADDRESS else fromAddress
            val buyToken = if (isBuyingETH) ETH_TOKEN_ADDRESS else toAddress
            
            Log.d(TAG, "Normalized addresses for 0x:")
            Log.d(TAG, "  sellToken: $sellToken")
            Log.d(TAG, "  buyToken: $buyToken")
            
            val taker = walletSDK.getAddress()
            
            // Build the API URL with fee parameters
            val url = "$ZEROX_API_BASE_URL/swap/allowance-holder/quote" +
                "?chainId=$chainId" +
                "&sellToken=$sellToken" +
                "&buyToken=$buyToken" +
                "&sellAmount=$sellAmount" +
                "&taker=$taker" +
                "&swapFeeRecipient=$SWAP_FEE_RECIPIENT" +
                "&swapFeeBps=$SWAP_FEE_BPS" +
                "&swapFeeToken=$sellToken"
            
            Log.d(TAG, "Calling 0x API for quote...")
            Log.d(TAG, "URL: $url")
            
            val request = Request.Builder()
                .url(url)
                .addHeader("0x-api-key", BuildConfig.ZEROX_API_KEY)
                .addHeader("0x-version", ZEROX_API_VERSION)
                .get()
                .build()
            
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()
            
            Log.d(TAG, "0x API Response:")
            Log.d(TAG, "  Status code: ${response.code}")
            Log.d(TAG, "  Is successful: ${response.isSuccessful}")
            Log.d(TAG, "  Body length: ${body?.length ?: 0}")
            
            if (!response.isSuccessful || body == null) {
                Log.e(TAG, "❌ 0x API quote request FAILED")
                Log.e(TAG, "  Status: ${response.code}")
                Log.e(TAG, "  Response body: $body")
                return@withContext null
            }
            
            Log.d(TAG, "0x API response received (${body.length} bytes)")
            Log.d(TAG, "Response body: $body")
            
            val adapter = moshi.adapter(ZeroXSwapQuoteResponse::class.java)
            val quote = adapter.fromJson(body)
            
            if (quote == null) {
                Log.e(TAG, "Failed to parse 0x quote response")
                return@withContext null
            }
            
            // Check for issues
            quote.issues?.let { issues ->
                Log.d(TAG, "Checking quote issues...")
                
                issues.balance?.let { balanceIssue ->
                    Log.e(TAG, "❌ BALANCE ISSUE:")
                    Log.e(TAG, "  Token: ${balanceIssue.token}")
                    Log.e(TAG, "  Expected: ${balanceIssue.expected}")
                    Log.e(TAG, "  Actual: ${balanceIssue.actual}")
                    return@withContext null
                }
                
                issues.allowance?.let { allowanceIssue ->
                    Log.w(TAG, "⚠️ ALLOWANCE ISSUE (will handle with approval):")
                    Log.w(TAG, "  Spender: ${allowanceIssue.spender}")
                    Log.w(TAG, "  Token: ${allowanceIssue.token}")
                    Log.w(TAG, "  Expected: ${allowanceIssue.expected}")
                    Log.w(TAG, "  Actual: ${allowanceIssue.actual}")
                }
                
                if (issues.simulationIncomplete == true) {
                    Log.w(TAG, "⚠️ Simulation incomplete")
                }
                
                issues.invalidSourcesPassed?.let { invalidSources ->
                    Log.w(TAG, "⚠️ Invalid sources: ${invalidSources.joinToString(", ")}")
                }
            }
            
            if (quote.liquidityAvailable == false) {
                Log.e(TAG, "❌ NO LIQUIDITY AVAILABLE for this swap pair")
                return@withContext null
            }
            
            Log.d(TAG, "✅ Quote received successfully:")
            Log.d(TAG, "  Buy amount (smallest unit): ${quote.buyAmount}")
            Log.d(TAG, "  Price: ${quote.price ?: "N/A"}")
            Log.d(TAG, "  Estimated price impact: ${quote.estimatedPriceImpact ?: "N/A"}")
            
            // Log route information
            quote.route?.fills?.let { fills ->
                if (fills.isNotEmpty()) {
                    val sources = fills.joinToString(" -> ") { it.source }
                    Log.d(TAG, "  Route: $sources")
                }
            }
            
            return@withContext quote
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching quote", e)
            return@withContext null
        }
    }

    private fun isEthLike(address: String?, symbol: String?, chainId: Int): Boolean {
        if (address == null) return true
        return address == "0x0000000000000000000000000000000000000000" ||
                address.equals(ETH_TOKEN_ADDRESS, ignoreCase = true) ||
                address == chainId.toString() ||
                (symbol != null && symbol.equals("ETH", ignoreCase = true))
    }
}


