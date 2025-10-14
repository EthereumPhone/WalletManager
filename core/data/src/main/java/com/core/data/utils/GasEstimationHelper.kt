package com.core.data.utils

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
import java.math.BigInteger

/**
 * Helper class for estimating gas for UserOperations.
 * Uses exact values from API but always sets verificationGasLimit to 800k.
 * 
 * NOTE: WalletSDK internally doubles preVerificationGas and verificationGasLimit,
 * so we return half of the desired final values for these fields.
 */
object GasEstimationHelper {
    
    // EntryPoint v0.6 address
    private const val ENTRY_POINT = "0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789"
    
    // Default fallback values if API fails
    private val DEFAULT_PRE_VERIFICATION_GAS = BigInteger.valueOf(70000) // 70k
    private val DEFAULT_CALL_GAS_LIMIT = BigInteger.valueOf(200000) // 200k
    
    /**
     * Estimates gas for a UserOperation by calling eth_estimateUserOperationGas RPC.
     * Uses exact API values except for verificationGasLimit which is hardcoded to result in 800k after doubling.
     * 
     * Note: WalletSDK doubles preVerificationGas and verificationGasLimit internally,
     * so we return 400k for verificationGasLimit to achieve a final value of 800k.
     */
    suspend fun estimateGas(
        userOp: WalletSDK.UserOperation,
        rpcUrl: String
    ): WalletSDK.GasEstimation = withContext(Dispatchers.IO) {
        
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
        
        // Create JSON-RPC request
        val requestJson = JsonObject().apply {
            addProperty("jsonrpc", "2.0")
            addProperty("method", "eth_estimateUserOperationGas")
            add("params", JsonArray().apply {
                add(userOpJson)
                add(ENTRY_POINT)
            })
            addProperty("id", 1)
        }
        
        // Initialize with defaults
        var preVerificationGas = DEFAULT_PRE_VERIFICATION_GAS
        var callGasLimit = DEFAULT_CALL_GAS_LIMIT
        
        try {
            // Make HTTP request to RPC endpoint
            val client = OkHttpClient()
            val contentType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(rpcUrl)
                .post(requestJson.toString().toRequestBody(contentType))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                
                if (!responseBody.isNullOrEmpty()) {
                    try {
                        // Parse response
                        val responseJson = JsonParser.parseString(responseBody).asJsonObject
                        
                        if (responseJson.has("error")) {
                            // Log error but use default values
                            val error = responseJson.getAsJsonObject("error")
                            val errorMessage = error.get("message")?.asString ?: "Unknown error"
                            println("GasEstimationHelper: API error: $errorMessage, using default values")
                        } else if (responseJson.has("result")) {
                            val result = responseJson.getAsJsonObject("result")
                            
                            // Extract exact values from API (no buffer applied)
                            result.get("preVerificationGas")?.asString?.let { 
                                try {
                                    preVerificationGas = BigInteger(it.removePrefix("0x"), 16)
                                    println("GasEstimationHelper: Using exact preVerificationGas from API: $preVerificationGas")
                                } catch (e: Exception) {
                                    println("GasEstimationHelper: Failed to parse preVerificationGas: ${e.message}")
                                }
                            }
                            
                            result.get("callGasLimit")?.asString?.let {
                                try {
                                    callGasLimit = BigInteger(it.removePrefix("0x"), 16)
                                    println("GasEstimationHelper: Using exact callGasLimit from API: $callGasLimit")
                                } catch (e: Exception) {
                                    println("GasEstimationHelper: Failed to parse callGasLimit: ${e.message}")
                                }
                            }
                            
                            // Note: We ignore verificationGasLimit from API and use fixed value
                            result.get("verificationGasLimit")?.asString?.let {
                                println("GasEstimationHelper: API returned verificationGasLimit but using fixed 800k instead")
                            }
                        }
                    } catch (e: Exception) {
                        println("GasEstimationHelper: Failed to parse response: ${e.message}")
                    }
                }
            } else {
                println("GasEstimationHelper: HTTP request failed with code ${response.code}, using defaults")
            }
        } catch (e: Exception) {
            println("GasEstimationHelper: Exception during gas estimation: ${e.message}, using defaults")
        }
        
        // WalletSDK internally doubles preVerificationGas and verificationGasLimit
        // So we need to return half of what we want the final values to be
        
        // We want final verificationGasLimit to be 800k, so return 400k
        val verificationGasLimitValue = BigInteger.valueOf(400000) // Will be doubled to 800k by WalletSDK
        
        // PreVerificationGas will also be doubled, so keep it as-is from API (it will be doubled)
        // The API already returns the base value which will be doubled
        
        println("GasEstimationHelper: Final values (will be doubled by WalletSDK):")
        println("  - preVerificationGas: $preVerificationGas (will become ${preVerificationGas.multiply(BigInteger.valueOf(2))})")
        println("  - verificationGasLimit: $verificationGasLimitValue (will become ${verificationGasLimitValue.multiply(BigInteger.valueOf(2))}) - HARDCODED to become 800k")
        println("  - callGasLimit: $callGasLimit (will NOT be doubled)")
        
        val gasEstimation = WalletSDK.GasEstimation(
            preVerificationGas = preVerificationGas,
            verificationGasLimit = verificationGasLimitValue, // 400k -> will be doubled to 800k
            callGasLimit = callGasLimit
        )
        
        // Log what we're actually returning
        println("GasEstimationHelper: Returning GasEstimation with verificationGasLimit = ${gasEstimation.verificationGasLimit} (will be doubled to ${gasEstimation.verificationGasLimit.multiply(BigInteger.valueOf(2))})")
        
        gasEstimation
    }
}
