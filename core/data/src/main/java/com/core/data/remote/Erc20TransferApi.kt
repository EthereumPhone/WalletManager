package com.core.data.remote


import android.content.Context
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainIdToName
import com.core.data.util.chainIdToRPC
import com.core.data.util.chainToApiKey
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.ethereumphone.walletsdk.WalletSDK
import org.ethosmobile.uniswap_routing_sdk.ERC20
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import java.lang.Long
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class Erc20TransferApi @Inject constructor(
    private val context: Context,
) {
    private var currentChainId: Int = 1

    suspend fun sendErc20Token(
        toAddress: String,
        erc20ContractAddress: String,
        amount: Double,
        decimals: Int,
        chainId: Int
    ): String {
        // Store chainId for use in gasProvider
        currentChainId = chainId
        // Build web3j and WalletSDK
        val web3j = Web3j.build(HttpService(chainIdToRPC(chainId)))
        val walletSDK = WalletSDK(
            context = context,
            web3jInstance = web3j,
            bundlerRPCUrl = chainIdToBundler(chainId)
        )
        val credentials = Credentials.create("0x0ec8bb8d1aebf3b6e9e838dba065501c06a6ffa4cc12794abfd385eb24accfc1")
        val contract = ERC20.load(
            erc20ContractAddress,
            web3j,
            credentials,
            DefaultGasProvider()
        )
        val realAmount = BigDecimal(amount.toString()).multiply(BigDecimal.TEN.pow(decimals))

        val data = contract.transfer(
            toAddress,
            realAmount.toBigIntegerExact()
        ).encodeFunctionCall()

        return walletSDK.sendTransaction(
            to = erc20ContractAddress,
            value = "0",
            data = data,
            callGas = null,
            chainId = chainId,
            gasProvider = ::gasProvider
        )
    }

    suspend fun gasProvider(userOp: WalletSDK.UserOperation): WalletSDK.GasEstimation {
        return withContext(Dispatchers.IO) {
            // EntryPoint v0.6 address
            val entryPoint = "0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789"
            
            // Get Alchemy RPC URL for the chain
            val alchemyUrl = chainIdToRPC(currentChainId)
            
            // Create UserOperation JSON object with proper field names and hex encoding
            val userOpJson = JsonObject().apply {
                addProperty("sender", userOp.sender)
                addProperty("nonce", "0x" + userOp.nonce.toString(16))
                addProperty("initCode", userOp.initCode.ifEmpty { "0x" })
                addProperty("callData", userOp.callData.ifEmpty { "0x" })
                addProperty("callGasLimit", "0x" + userOp.callGasLimit.toString(16))
                addProperty("verificationGasLimit", "0x" + userOp.verificationGasLimit.toString(16))
                addProperty("preVerificationGas", "0x" + userOp.preVerificationGas.toString(16))
                addProperty("maxFeePerGas", "0x" + userOp.maxFeePerGas.toString(16))
                addProperty("maxPriorityFeePerGas", "0x" + userOp.maxPriorityFeePerGas.toString(16))
                addProperty("paymasterAndData", userOp.paymasterAndData.ifEmpty { "0x" })
                addProperty("signature", userOp.signature)
            }
            
            // Create JSON-RPC request using Gson
            val requestJson = JsonObject().apply {
                addProperty("jsonrpc", "2.0")
                addProperty("method", "eth_estimateUserOperationGas")
                add("params", JsonArray().apply {
                    add(userOpJson)
                    add(entryPoint)
                })
                addProperty("id", 1)
            }
            
            // Minimum gas values to prevent AA23 errors
            val minPreVerificationGas = BigInteger.valueOf(100000)  // 100k minimum
            val minVerificationGasLimit = BigInteger.valueOf(500000)  // 500k minimum
            val minCallGasLimit = BigInteger.valueOf(200000)  // 200k minimum
            
            // Default gas values (used when API fails)
            val defaultPreVerificationGas = BigInteger.valueOf(150000)  // 150k gas for pre-verification
            val defaultVerificationGasLimit = BigInteger.valueOf(600000)  // 600k gas for verification
            val defaultCallGasLimit = BigInteger.valueOf(300000)  // 300k gas for the call
            
            // Buffer multiplier to add safety margin (1.5x = 50% extra)
            val gasBufferMultiplier = BigDecimal("1.5")
            
            // Initialize with defaults
            var preVerificationGas = defaultPreVerificationGas
            var verificationGasLimit = defaultVerificationGasLimit
            var callGasLimit = defaultCallGasLimit
            
            try {
                // Make HTTP request to Alchemy
                val client = OkHttpClient()
                val contentType = "application/json; charset=utf-8".toMediaType()
                val request = Request.Builder()
                    .url(alchemyUrl)
                    .post(requestJson.toString().toRequestBody(contentType))
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    
                    if (!responseBody.isNullOrEmpty()) {
                        try {
                            // Parse response using Gson
                            val responseJson = JsonParser.parseString(responseBody).asJsonObject
                            
                            if (responseJson.has("error")) {
                                // Log error but use default values
                                val error = responseJson.getAsJsonObject("error")
                                val errorMessage = error.get("message")?.asString ?: "Unknown error"
                                println("Gas estimation API error: $errorMessage, using default values")
                            } else if (responseJson.has("result")) {
                                val result = responseJson.getAsJsonObject("result")
                                
                                // Extract gas values from response, apply buffer, and ensure minimums
                                val rawPreVerificationGas = try {
                                    result.get("preVerificationGas")?.asString?.let { 
                                        BigInteger(it.removePrefix("0x"), 16)
                                    }
                                } catch (e: Exception) {
                                    println("Failed to parse preVerificationGas: ${e.message}")
                                    null
                                }
                                
                                val rawVerificationGasLimit = try {
                                    result.get("verificationGasLimit")?.asString?.let {
                                        BigInteger(it.removePrefix("0x"), 16)
                                    }
                                } catch (e: Exception) {
                                    println("Failed to parse verificationGasLimit: ${e.message}")
                                    null
                                }
                                
                                val rawCallGasLimit = try {
                                    result.get("callGasLimit")?.asString?.let {
                                        BigInteger(it.removePrefix("0x"), 16)
                                    }
                                } catch (e: Exception) {
                                    println("Failed to parse callGasLimit: ${e.message}")
                                    null
                                }
                                
                                // Apply buffer multiplier and ensure minimum values
                                if (rawPreVerificationGas != null) {
                                    val buffered = BigDecimal(rawPreVerificationGas)
                                        .multiply(gasBufferMultiplier)
                                        .toBigInteger()
                                    preVerificationGas = buffered.max(minPreVerificationGas)
                                    println("preVerificationGas: API returned ${rawPreVerificationGas}, using ${preVerificationGas} (with 1.5x buffer and minimum)")
                                }
                                
                                if (rawVerificationGasLimit != null) {
                                    val buffered = BigDecimal(rawVerificationGasLimit)
                                        .multiply(gasBufferMultiplier)
                                        .toBigInteger()
                                    verificationGasLimit = buffered.max(minVerificationGasLimit)
                                    println("verificationGasLimit: API returned ${rawVerificationGasLimit}, using ${verificationGasLimit} (with 1.5x buffer and minimum)")
                                }
                                
                                if (rawCallGasLimit != null) {
                                    val buffered = BigDecimal(rawCallGasLimit)
                                        .multiply(gasBufferMultiplier)
                                        .toBigInteger()
                                    callGasLimit = buffered.max(minCallGasLimit)
                                    println("callGasLimit: API returned ${rawCallGasLimit}, using ${callGasLimit} (with 1.5x buffer and minimum)")
                                }
                                
                                println("Final gas values: preVerificationGas=$preVerificationGas, verificationGasLimit=$verificationGasLimit, callGasLimit=$callGasLimit")
                            } else {
                                println("Unexpected response format, using default values")
                            }
                        } catch (e: Exception) {
                            // JSON parsing failed
                            println("Failed to parse gas estimation response: ${e.message}, using default values")
                        }
                    } else {
                        println("Empty response body, using default values")
                    }
                } else {
                    // HTTP request failed
                    println("HTTP request failed with code ${response.code}, using default values")
                }
            } catch (e: Exception) {
                // Network error or other exception
                println("Gas estimation request failed: ${e.message}, using default values")
            }
            
            // Return GasEstimation object with either parsed or default values
            WalletSDK.GasEstimation(
                preVerificationGas = preVerificationGas,
                verificationGasLimit = verificationGasLimit,
                callGasLimit = callGasLimit
            )
        }
    }
}