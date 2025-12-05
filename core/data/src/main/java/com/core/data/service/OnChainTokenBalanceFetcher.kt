package com.core.data.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for fetching ERC20 token balances directly from the blockchain
 */
@Singleton
class OnChainTokenBalanceFetcher @Inject constructor() {
    
    companion object {
        private const val TAG = "OnChainTokenBalanceFetcher"
        private const val FUNC_BALANCE_OF = "balanceOf"
    }
    
    /**
     * Fetches the token balance for a given wallet address and token contract
     * Returns the balance in the token's smallest unit (e.g., wei for 18 decimal tokens)
     * Returns null if the balance check fails
     */
    suspend fun fetchTokenBalance(
        walletAddress: String,
        contractAddress: String,
        rpcUrl: String
    ): BigDecimal? = withContext(Dispatchers.IO) {
        try {
            val web3j = Web3j.build(HttpService(rpcUrl))
            
            try {
                val balance = fetchBalanceOf(web3j, contractAddress, walletAddress)
                
                if (balance != null && balance > BigInteger.ZERO) {
                    BigDecimal(balance)
                } else {
                    BigDecimal.ZERO
                }
            } finally {
                web3j.shutdown()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch balance for $contractAddress: ${e.message}")
            null
        }
    }
    
    /**
     * Fetches balances for multiple tokens in a single batch
     * Returns a map of contract address to balance (in smallest unit)
     * Tokens that fail to fetch will have null values
     */
    suspend fun fetchTokenBalances(
        walletAddress: String,
        contractAddresses: List<String>,
        rpcUrl: String
    ): Map<String, BigDecimal?> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, BigDecimal?>()
        
        if (contractAddresses.isEmpty()) {
            return@withContext results
        }
        
        try {
            val web3j = Web3j.build(HttpService(rpcUrl))
            
            try {
                for (contractAddress in contractAddresses) {
                    try {
                        val balance = fetchBalanceOf(web3j, contractAddress, walletAddress)
                        results[contractAddress.lowercase()] = if (balance != null) {
                            BigDecimal(balance)
                        } else {
                            BigDecimal.ZERO
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching balance for $contractAddress: ${e.message}")
                        results[contractAddress.lowercase()] = null
                    }
                }
            } finally {
                web3j.shutdown()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create web3j instance: ${e.message}")
            contractAddresses.forEach { results[it.lowercase()] = null }
        }
        
        results
    }
    
    private fun fetchBalanceOf(web3j: Web3j, contractAddress: String, walletAddress: String): BigInteger? {
        return try {
            val function = Function(
                FUNC_BALANCE_OF,
                listOf(Address(walletAddress)),
                listOf(object : TypeReference<Uint256>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val transaction = Transaction.createEthCallTransaction(
                null,
                contractAddress,
                encodedFunction
            )
            
            val response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send()
            
            if (response.hasError()) {
                Log.w(TAG, "Error fetching balanceOf for $contractAddress: ${response.error.message}")
                return null
            }
            
            if (response.value == null || response.value == "0x" || response.value.isEmpty()) {
                return BigInteger.ZERO
            }
            
            val result = FunctionReturnDecoder.decode(
                response.value,
                function.outputParameters
            )
            
            (result.firstOrNull() as? Uint256)?.value
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching balanceOf for $contractAddress", e)
            null
        }
    }
}
