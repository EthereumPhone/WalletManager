package com.core.data.service

import android.util.Log
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.model.NetworkChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Uint
import org.web3j.abi.datatypes.Utf8String
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for fetching ERC20 token metadata directly from the blockchain
 */
@Singleton
class OnChainTokenMetadataFetcher @Inject constructor() : TokenMetadataFetcher {
    
    companion object {
        private const val TAG = "OnChainTokenMetadataFetcher"
        
        // ERC20 function signatures
        private const val FUNC_NAME = "name"
        private const val FUNC_SYMBOL = "symbol"
        private const val FUNC_DECIMALS = "decimals"
    }
    
    /**
     * Fetches token metadata from the blockchain for a given contract address and chain
     * Returns null if the contract is not an ERC20 token or if fetching fails
     */
    override suspend fun fetchTokenMetadata(
        contractAddress: String,
        chainId: Int,
        rpcUrl: String
    ): TokenMetadataEntity? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching on-chain metadata for token: $contractAddress on chain: $chainId")
            
            val web3j = Web3j.build(HttpService(rpcUrl))
            
            try {
                // Fetch name
                val name = fetchTokenName(web3j, contractAddress)
                    ?.takeIf { it.isNotBlank() }
                    ?: return@withContext null
                
                // Fetch symbol
                val symbol = fetchTokenSymbol(web3j, contractAddress)
                    ?.takeIf { it.isNotBlank() }
                    ?: return@withContext null
                
                // Fetch decimals
                val decimals = fetchTokenDecimals(web3j, contractAddress)
                    ?: 18 // Default to 18 if decimals call fails
                
                Log.d(TAG, "Successfully fetched on-chain metadata - Name: $name, Symbol: $symbol, Decimals: $decimals")
                
                TokenMetadataEntity(
                    contractAddress = contractAddress.lowercase(),
                    chainId = chainId,
                    decimals = decimals,
                    name = name,
                    symbol = symbol,
                    logo = null, // Logo cannot be fetched on-chain
                    swappable = false, // Default to false for on-chain fetched tokens
                    groupId = null // Don't set groupId here, let the repository handle it
                )
            } finally {
                web3j.shutdown()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch on-chain metadata for $contractAddress: ${e.message}")
            null
        }
    }
    
    private suspend fun fetchTokenName(web3j: Web3j, contractAddress: String): String? {
        return try {
            val function = Function(
                FUNC_NAME,
                emptyList(),
                listOf(object : TypeReference<Utf8String>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val transaction = Transaction.createEthCallTransaction(
                null, // from address not required for call
                contractAddress,
                encodedFunction
            )
            
            val response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send()
            
            if (response.hasError()) {
                Log.w(TAG, "Error fetching name for $contractAddress: ${response.error.message}")
                return null
            }
            
            val result = FunctionReturnDecoder.decode(
                response.value,
                function.outputParameters
            )
            
            (result.firstOrNull() as? Utf8String)?.value
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching name for $contractAddress", e)
            null
        }
    }
    
    private suspend fun fetchTokenSymbol(web3j: Web3j, contractAddress: String): String? {
        return try {
            val function = Function(
                FUNC_SYMBOL,
                emptyList(),
                listOf(object : TypeReference<Utf8String>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val transaction = Transaction.createEthCallTransaction(
                null,
                contractAddress,
                encodedFunction
            )
            
            val response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send()
            
            if (response.hasError()) {
                Log.w(TAG, "Error fetching symbol for $contractAddress: ${response.error.message}")
                return null
            }
            
            val result = FunctionReturnDecoder.decode(
                response.value,
                function.outputParameters
            )
            
            (result.firstOrNull() as? Utf8String)?.value
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching symbol for $contractAddress", e)
            null
        }
    }
    
    private suspend fun fetchTokenDecimals(web3j: Web3j, contractAddress: String): Int? {
        return try {
            val function = Function(
                FUNC_DECIMALS,
                emptyList(),
                listOf(object : TypeReference<Uint>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val transaction = Transaction.createEthCallTransaction(
                null,
                contractAddress,
                encodedFunction
            )
            
            val response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send()
            
            if (response.hasError()) {
                Log.w(TAG, "Error fetching decimals for $contractAddress: ${response.error.message}")
                return null
            }
            
            val result = FunctionReturnDecoder.decode(
                response.value,
                function.outputParameters
            )
            
            (result.firstOrNull() as? Uint)?.value?.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching decimals for $contractAddress", e)
            null
        }
    }
}
