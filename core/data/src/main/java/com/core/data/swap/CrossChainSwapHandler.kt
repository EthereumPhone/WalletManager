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
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
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
 * Handler for cross-chain swaps using Socket API
 * Documentation: https://docs.socket.tech/socket-api/v2
 * 
 * Socket aggregates multiple bridges (Hop, Across, Stargate, etc.) to find the best
 * route for cross-chain token transfers.
 */
class CrossChainSwapHandler(private val context: Context) {

    companion object {
        private const val TAG = "WM-CrossChainSwap"
        private const val SOCKET_API_BASE_URL = "https://api.socket.tech/v2"
        private const val ETH_TOKEN_ADDRESS = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"
        private const val NATIVE_TOKEN_ADDRESS = "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
        
        // Socket API key (stored in BuildConfig for security)
        // Note: Socket provides free tier, get key at https://sockettech.notion.site/
        private const val SOCKET_API_KEY = "72a5b4b0-e727-48be-8aa1-5da9d62fe635" // Public demo key
        
        // Fee configuration (0.15% to match 0x fee)
        private const val INTEGRATOR_FEE_BPS = 15 // 0.15%
        private const val FEE_RECIPIENT = "0xF1F39090D2bE5010Cc1Dd633b6dCe476A38b5675"
        
        // Supported chains for cross-chain swaps
        val SUPPORTED_CHAIN_IDS = setOf(1, 10, 137, 42161, 8453, 43114, 56)
        
        // Chain names for logging
        private val CHAIN_NAMES = mapOf(
            1 to "Ethereum",
            10 to "Optimism",
            137 to "Polygon",
            42161 to "Arbitrum",
            8453 to "Base",
            43114 to "Avalanche",
            56 to "BSC"
        )
    }

    private val web3jByChain = ConcurrentHashMap<Int, Web3j>()
    private val walletSdkByChain = ConcurrentHashMap<Int, WalletSDK>()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun isSupported(chainId: Int): Boolean = SUPPORTED_CHAIN_IDS.contains(chainId)

    private fun getChainName(chainId: Int): String = CHAIN_NAMES[chainId] ?: "Chain $chainId"

    private fun resolveRpcUrl(chainId: Int): String? = try {
        chainIdToRPC(chainId)
    } catch (t: Throwable) {
        Log.e(TAG, "Unable to resolve RPC for chainId=$chainId", t)
        null
    }

    private suspend fun getWalletSdkForChain(chainId: Int): WalletSDK? {
        if (!isSupported(chainId)) {
            Log.e(TAG, "Cross-chain swap not available on chainId=$chainId")
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
     * Get a cross-chain swap quote from Socket API
     * 
     * @param fromChainId Source chain ID
     * @param toChainId Destination chain ID
     * @param fromAddress Token address on source chain
     * @param toAddress Token address on destination chain
     * @param fromDecimals Decimals of source token
     * @param toDecimals Decimals of destination token
     * @param fromSymbol Symbol of source token
     * @param toSymbol Symbol of destination token
     * @param fromAmount Amount to swap (human-readable)
     * @return SocketQuoteResponse or null if failed
     */
    suspend fun getCrossChainQuote(
        fromChainId: Int,
        toChainId: Int,
        fromAddress: String,
        toAddress: String,
        fromDecimals: Int,
        toDecimals: Int,
        fromSymbol: String,
        toSymbol: String,
        fromAmount: BigDecimal
    ): SocketQuoteResponse? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== Getting Cross-Chain Quote ===")
            Log.d(TAG, "From: ${getChainName(fromChainId)} -> ${getChainName(toChainId)}")
            Log.d(TAG, "Token: $fromSymbol ($fromAddress) -> $toSymbol ($toAddress)")
            Log.d(TAG, "Amount: $fromAmount")

            val walletSDK = getWalletSdkForChain(fromChainId)
                ?: return@withContext null

            val userAddress = walletSDK.getAddress()

            // Normalize addresses for native tokens
            val normalizedFromAddress = normalizeTokenAddress(fromAddress, fromSymbol, fromChainId)
            val normalizedToAddress = normalizeTokenAddress(toAddress, toSymbol, toChainId)

            // Convert to smallest unit
            val sellAmount = if (isNativeToken(fromAddress, fromSymbol, fromChainId)) {
                Convert.toWei(fromAmount, Convert.Unit.ETHER).toBigInteger()
            } else {
                fromAmount.multiply(BigDecimal.TEN.pow(fromDecimals)).toBigInteger()
            }

            Log.d(TAG, "User address: $userAddress")
            Log.d(TAG, "Sell amount (wei): $sellAmount")
            Log.d(TAG, "Normalized from: $normalizedFromAddress")
            Log.d(TAG, "Normalized to: $normalizedToAddress")

            // Build Socket API URL
            val url = buildString {
                append("$SOCKET_API_BASE_URL/quote")
                append("?fromChainId=$fromChainId")
                append("&toChainId=$toChainId")
                append("&fromTokenAddress=$normalizedFromAddress")
                append("&toTokenAddress=$normalizedToAddress")
                append("&fromAmount=$sellAmount")
                append("&userAddress=$userAddress")
                append("&uniqueRoutesPerBridge=true")
                append("&sort=output") // Best output amount
                append("&singleTxOnly=true") // Single transaction for simplicity
                // Add integrator fee
                append("&feePercent=$INTEGRATOR_FEE_BPS")
                append("&feeTakerAddress=$FEE_RECIPIENT")
            }

            Log.d(TAG, "Socket API URL: $url")

            val request = Request.Builder()
                .url(url)
                .addHeader("API-KEY", SOCKET_API_KEY)
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()

            Log.d(TAG, "Socket API Response: ${response.code}")

            if (!response.isSuccessful || body == null) {
                Log.e(TAG, "Socket quote failed: ${response.code} - $body")
                return@withContext null
            }

            Log.d(TAG, "Response body: ${body.take(500)}...")

            val adapter = moshi.adapter(SocketQuoteResponse::class.java)
            val quoteResponse = adapter.fromJson(body)

            if (quoteResponse == null || !quoteResponse.success) {
                Log.e(TAG, "Failed to parse Socket quote response")
                return@withContext null
            }

            val routes = quoteResponse.result?.routes ?: emptyList()
            if (routes.isEmpty()) {
                Log.w(TAG, "No routes available for this cross-chain swap")
                return@withContext null
            }

            val bestRoute = routes.first()
            Log.d(TAG, "✅ Cross-chain quote received:")
            Log.d(TAG, "  Route ID: ${bestRoute.routeId}")
            Log.d(TAG, "  Bridges: ${bestRoute.usedBridgeNames.joinToString(", ")}")
            Log.d(TAG, "  Output amount: ${bestRoute.toAmount}")
            Log.d(TAG, "  Gas fees (USD): ${bestRoute.totalGasFeesInUsd}")
            Log.d(TAG, "  Service time: ${bestRoute.serviceTime}s")

            return@withContext quoteResponse

        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching cross-chain quote", e)
            return@withContext null
        }
    }

    /**
     * Execute a cross-chain swap using the best route from Socket
     * 
     * @return Transaction hash on success, or error string
     */
    suspend fun executeCrossChainSwap(
        fromChainId: Int,
        toChainId: Int,
        fromAddress: String,
        toAddress: String,
        fromDecimals: Int,
        toDecimals: Int,
        fromSymbol: String,
        toSymbol: String,
        fromAmount: BigDecimal
    ): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== Executing Cross-Chain Swap ===")
            Log.d(TAG, "${getChainName(fromChainId)} -> ${getChainName(toChainId)}")
            Log.d(TAG, "$fromSymbol -> $toSymbol")
            Log.d(TAG, "Amount: $fromAmount")

            // Step 1: Get the quote
            val quoteResponse = getCrossChainQuote(
                fromChainId, toChainId,
                fromAddress, toAddress,
                fromDecimals, toDecimals,
                fromSymbol, toSymbol,
                fromAmount
            )

            if (quoteResponse == null || quoteResponse.result?.routes.isNullOrEmpty()) {
                Log.e(TAG, "No routes available for cross-chain swap")
                return@withContext "ERROR_NO_ROUTES"
            }

            val bestRoute = quoteResponse.result!!.routes.first()
            Log.d(TAG, "Using route: ${bestRoute.routeId}")
            Log.d(TAG, "Bridges: ${bestRoute.usedBridgeNames}")

            // Step 2: Build the transaction
            val buildTxResponse = buildTransaction(bestRoute)
            if (buildTxResponse == null || !buildTxResponse.success || buildTxResponse.result == null) {
                Log.e(TAG, "Failed to build cross-chain transaction")
                return@withContext "ERROR_BUILD_TX"
            }

            val txData = buildTxResponse.result

            // Step 3: Get wallet SDK for source chain
            val walletSDK = getWalletSdkForChain(fromChainId)
                ?: return@withContext "ERROR_WALLET_SDK"

            // Step 4: Handle approval if needed (for ERC20 tokens)
            val isNative = isNativeToken(fromAddress, fromSymbol, fromChainId)
            if (!isNative && txData.approvalData != null) {
                Log.d(TAG, "Approval required for ${txData.approvalData.approvalTokenAddress}")
                
                val approvalTx = buildApprovalTransaction(
                    tokenAddress = txData.approvalData.approvalTokenAddress,
                    spenderAddress = txData.approvalData.allowanceTarget,
                    amount = txData.approvalData.minimumApprovalAmount
                )
                
                // Execute approval first
                val approvalHash = walletSDK.sendTransaction(
                    txParamsList = listOf(approvalTx),
                    callGas = null,
                    chainId = fromChainId,
                    gasProvider = { userOp -> gasProvider(fromChainId, userOp) }
                )
                
                if (!approvalHash.startsWith("0x")) {
                    Log.e(TAG, "Approval transaction failed: $approvalHash")
                    return@withContext when {
                        approvalHash.equals("decline", ignoreCase = true) -> "DECLINE"
                        approvalHash.contains("AA21") -> "NOT_ENOUGH_GAS"
                        else -> "ERROR_APPROVAL"
                    }
                }
                
                Log.d(TAG, "Approval successful: $approvalHash")
            }

            // Step 5: Execute the bridge/swap transaction
            val swapTx = WalletSDK.TxParams(
                to = txData.txTarget,
                value = txData.value,
                data = txData.txData
            )

            Log.d(TAG, "Executing bridge transaction...")
            Log.d(TAG, "  To: ${txData.txTarget}")
            Log.d(TAG, "  Value: ${txData.value}")
            
            val txHash = walletSDK.sendTransaction(
                txParamsList = listOf(swapTx),
                callGas = null,
                chainId = fromChainId,
                gasProvider = { userOp -> gasProvider(fromChainId, userOp) }
            )

            return@withContext when {
                txHash.startsWith("0x") -> {
                    Log.d(TAG, "✅ Cross-chain swap initiated: $txHash")
                    Log.d(TAG, "Note: Destination funds will arrive in ~${bestRoute.serviceTime}s")
                    txHash
                }
                txHash.equals("decline", ignoreCase = true) -> "DECLINE"
                txHash.contains("AA21") -> "NOT_ENOUGH_GAS"
                else -> "ERROR"
            }

        } catch (e: Exception) {
            Log.e(TAG, "Cross-chain swap failed", e)
            return@withContext "ERROR"
        }
    }

    /**
     * Build transaction data from Socket API
     */
    private suspend fun buildTransaction(route: SocketRoute): SocketBuildTxResponse? {
        return try {
            val url = "$SOCKET_API_BASE_URL/build-tx"

            // Create request body with route
            val requestBody = """
                {
                    "route": ${moshi.adapter(SocketRoute::class.java).toJson(route)}
                }
            """.trimIndent()

            Log.d(TAG, "Building transaction for route: ${route.routeId}")

            val request = Request.Builder()
                .url(url)
                .addHeader("API-KEY", SOCKET_API_KEY)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body == null) {
                Log.e(TAG, "Build TX failed: ${response.code} - $body")
                return null
            }

            val adapter = moshi.adapter(SocketBuildTxResponse::class.java)
            adapter.fromJson(body)

        } catch (e: Exception) {
            Log.e(TAG, "Exception building transaction", e)
            null
        }
    }

    private fun buildApprovalTransaction(
        tokenAddress: String,
        spenderAddress: String,
        amount: String
    ): WalletSDK.TxParams {
        // Use max approval for convenience
        val approvalAmount = BigInteger("2").pow(256).subtract(BigInteger.ONE)
        
        val function = Function(
            "approve",
            listOf(Address(spenderAddress), Uint256(approvalAmount)),
            emptyList<TypeReference<*>>()
        )
        val encodedFunction = FunctionEncoder.encode(function)
        
        return WalletSDK.TxParams(
            to = tokenAddress,
            value = "0",
            data = encodedFunction
        )
    }

    private suspend fun gasProvider(chainId: Int, userOp: WalletSDK.UserOperation): WalletSDK.GasEstimation {
        val rpcUrl = resolveRpcUrl(chainId) ?: chainIdToRPC(8453)
        return GasEstimationHelper.estimateGas(userOp, rpcUrl)
    }

    private fun normalizeTokenAddress(address: String?, symbol: String?, chainId: Int): String {
        if (isNativeToken(address, symbol, chainId)) {
            return NATIVE_TOKEN_ADDRESS
        }
        return address ?: NATIVE_TOKEN_ADDRESS
    }

    private fun isNativeToken(address: String?, symbol: String?, chainId: Int): Boolean {
        if (address == null) return true
        return address == "0x0000000000000000000000000000000000000000" ||
                address.equals(ETH_TOKEN_ADDRESS, ignoreCase = true) ||
                address.equals(NATIVE_TOKEN_ADDRESS, ignoreCase = true) ||
                address == chainId.toString() ||
                (symbol != null && isNativeSymbol(symbol, chainId))
    }

    private fun isNativeSymbol(symbol: String, chainId: Int): Boolean {
        val nativeSymbols = when (chainId) {
            56 -> setOf("BNB", "WBNB")
            137 -> setOf("MATIC", "POL", "WMATIC")
            43114 -> setOf("AVAX", "WAVAX")
            else -> setOf("ETH", "WETH")
        }
        return nativeSymbols.any { it.equals(symbol, ignoreCase = true) }
    }

    /**
     * Check the status of a cross-chain transaction
     */
    suspend fun checkTransactionStatus(
        transactionHash: String,
        fromChainId: Int,
        toChainId: Int
    ): SocketRouteStatusResponse? = withContext(Dispatchers.IO) {
        try {
            val url = buildString {
                append("$SOCKET_API_BASE_URL/bridge-status")
                append("?transactionHash=$transactionHash")
                append("&fromChainId=$fromChainId")
                append("&toChainId=$toChainId")
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("API-KEY", SOCKET_API_KEY)
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string()

            if (!response.isSuccessful || body == null) {
                Log.e(TAG, "Status check failed: ${response.code}")
                return@withContext null
            }

            val adapter = moshi.adapter(SocketRouteStatusResponse::class.java)
            adapter.fromJson(body)

        } catch (e: Exception) {
            Log.e(TAG, "Exception checking status", e)
            null
        }
    }
}


