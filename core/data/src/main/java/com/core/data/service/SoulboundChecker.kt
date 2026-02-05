package com.core.data.service

import android.util.Log
import com.core.data.util.chainIdToRPC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for checking if an NFT is soulbound (non-transferable) using EIP-5192.
 * 
 * EIP-5192 defines the "Minimal Soulbound NFT" standard with a `locked(uint256 tokenId)` function
 * that returns true if the token is locked (soulbound/non-transferable).
 * 
 * @see <a href="https://eips.ethereum.org/EIPS/eip-5192">EIP-5192</a>
 */
@Singleton
class SoulboundChecker @Inject constructor() {
    
    companion object {
        private const val TAG = "SoulboundChecker"
        
        // EIP-5192 function name
        private const val FUNC_LOCKED = "locked"
        
        // EIP-5192 interface ID: 0xb45a3c0e
        // This can be used to check if contract supports EIP-5192 via ERC-165
        private const val EIP5192_INTERFACE_ID = "0xb45a3c0e"
    }
    
    /**
     * Check if an NFT is soulbound (non-transferable) using EIP-5192.
     * 
     * @param chainId The chain ID where the NFT contract is deployed
     * @param contractAddress The NFT contract address
     * @param tokenId The token ID to check
     * @return true if the NFT is soulbound (locked), false otherwise or if the contract
     *         doesn't implement EIP-5192
     */
    suspend fun isNftSoulbound(
        chainId: Int,
        contractAddress: String,
        tokenId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val rpcUrl = try {
                chainIdToRPC(chainId)
            } catch (e: Exception) {
                Log.w(TAG, "Unsupported chain ID: $chainId")
                return@withContext false
            }
            
            Log.d(TAG, "Checking soulbound status for token $tokenId at $contractAddress on chain $chainId")
            
            val web3j = Web3j.build(HttpService(rpcUrl))
            
            try {
                val isLocked = checkLocked(web3j, contractAddress, tokenId)
                Log.d(TAG, "Soulbound check result for $contractAddress#$tokenId: $isLocked")
                isLocked
            } finally {
                web3j.shutdown()
            }
        } catch (e: Exception) {
            // If the call fails, the contract likely doesn't implement EIP-5192
            // In this case, we assume the NFT is transferable
            Log.d(TAG, "Contract $contractAddress doesn't implement EIP-5192 or call failed: ${e.message}")
            false
        }
    }
    
    /**
     * Call the locked(uint256 tokenId) function on the contract.
     * Returns true if the token is locked (soulbound), false otherwise.
     */
    private suspend fun checkLocked(
        web3j: Web3j,
        contractAddress: String,
        tokenId: String
    ): Boolean {
        return try {
            // Parse tokenId - handle both decimal and hex formats
            val tokenIdBigInt = if (tokenId.startsWith("0x", ignoreCase = true)) {
                BigInteger(tokenId.removePrefix("0x").removePrefix("0X"), 16)
            } else {
                BigInteger(tokenId)
            }
            
            // Create the function call for locked(uint256 tokenId) -> bool
            val function = Function(
                FUNC_LOCKED,
                listOf(Uint256(tokenIdBigInt)),
                listOf(object : TypeReference<Bool>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val transaction = Transaction.createEthCallTransaction(
                null, // from address not required for call
                contractAddress,
                encodedFunction
            )
            
            val response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send()
            
            if (response.hasError()) {
                Log.w(TAG, "Error calling locked() for $contractAddress: ${response.error.message}")
                return false
            }
            
            // Empty response means contract doesn't implement this function
            if (response.value == null || response.value == "0x") {
                Log.d(TAG, "Contract $contractAddress returned empty response - likely doesn't implement EIP-5192")
                return false
            }
            
            val result = FunctionReturnDecoder.decode(
                response.value,
                function.outputParameters
            )
            
            (result.firstOrNull() as? Bool)?.value ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Exception checking locked() for $contractAddress#$tokenId", e)
            false
        }
    }
    
    /**
     * Check if a contract supports EIP-5192 using ERC-165 supportsInterface.
     * This is optional - some soulbound NFTs may implement locked() without ERC-165.
     */
    suspend fun supportsEip5192(
        chainId: Int,
        contractAddress: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val rpcUrl = try {
                chainIdToRPC(chainId)
            } catch (e: Exception) {
                return@withContext false
            }
            
            val web3j = Web3j.build(HttpService(rpcUrl))
            
            try {
                checkSupportsInterface(web3j, contractAddress, EIP5192_INTERFACE_ID)
            } finally {
                web3j.shutdown()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failed to check ERC-165 support for $contractAddress: ${e.message}")
            false
        }
    }
    
    /**
     * Call supportsInterface(bytes4 interfaceId) on the contract (ERC-165).
     */
    private suspend fun checkSupportsInterface(
        web3j: Web3j,
        contractAddress: String,
        interfaceId: String
    ): Boolean {
        return try {
            // Create the function call for supportsInterface(bytes4) -> bool
            val function = Function(
                "supportsInterface",
                listOf(org.web3j.abi.datatypes.generated.Bytes4(
                    hexStringToByteArray(interfaceId.removePrefix("0x"))
                )),
                listOf(object : TypeReference<Bool>() {})
            )
            
            val encodedFunction = FunctionEncoder.encode(function)
            val transaction = Transaction.createEthCallTransaction(
                null,
                contractAddress,
                encodedFunction
            )
            
            val response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send()
            
            if (response.hasError() || response.value == null || response.value == "0x") {
                return false
            }
            
            val result = FunctionReturnDecoder.decode(
                response.value,
                function.outputParameters
            )
            
            (result.firstOrNull() as? Bool)?.value ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Exception checking supportsInterface for $contractAddress", e)
            false
        }
    }
    
    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
