package com.core.data.swap

import android.content.Context
import android.util.Log
import com.core.data.BuildConfig
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
import java.util.concurrent.TimeUnit

/**
 * Minimal 0x-based swap handler mirroring TokenLauncher behavior.
 */
class SwapHandler(private val context: Context) {

    private val walletSDK = WalletSDK(
        context = context,
        web3jInstance = Web3j.build(HttpService("https://base-mainnet.g.alchemy.com/v2/${BuildConfig.ALCHEMY_API}")),
        bundlerRPCUrl = "https://api.pimlico.io/v2/8453/rpc?apikey=${BuildConfig.BUNDLER_API}"
    )

    companion object {
        private const val TAG = "WM-SwapHandler"
        private const val ZEROX_API_BASE_URL = "https://api.0x.org"
        private const val ZEROX_API_VERSION = "v2"
        private const val ETH_TOKEN_ADDRESS = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"
        private const val SWAP_FEE_BPS = 15 // 0.15%
        private const val SWAP_FEE_RECIPIENT = "0xF1F39090D2bE5010Cc1Dd633b6dCe476A38b5675"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

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
                gasProvider = ::gasProvider
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

    private suspend fun gasProvider(userOp: WalletSDK.UserOperation): WalletSDK.GasEstimation {
        val rpcUrl = "https://base-mainnet.g.alchemy.com/v2/${BuildConfig.ALCHEMY_API}"
        return GasEstimationHelper.estimateGas(userOp, rpcUrl)
    }

    private fun isEthLike(address: String?, symbol: String?, chainId: Int): Boolean {
        if (address == null) return true
        return address == "0x0000000000000000000000000000000000000000" ||
                address.equals(ETH_TOKEN_ADDRESS, ignoreCase = true) ||
                address == chainId.toString() ||
                (symbol != null && symbol.equals("ETH", ignoreCase = true))
    }
}


