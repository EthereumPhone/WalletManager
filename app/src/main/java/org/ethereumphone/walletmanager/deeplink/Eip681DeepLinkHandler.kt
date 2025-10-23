package org.ethereumphone.walletmanager.deeplink

import android.content.Context
import android.content.Intent
import android.util.Log
import com.core.data.util.Eip681
import com.core.data.util.Eip681ParseException
import com.core.data.util.parseEip681
import com.core.database.dao.TokenMetadataDao
import javax.inject.Inject

data class Eip681DeepLinkResult(
    val success: Boolean,
    val recipientAddress: String? = null,
    val amount: String? = null,
    val groupId: String? = null,
    val chainId: Int? = null,
    val errorMessage: String? = null
)

class Eip681DeepLinkHandler @Inject constructor(
    private val tokenMetadataDao: TokenMetadataDao
) {
    
    companion object {
        private const val TAG = "Eip681DeepLinkHandler"
        private const val DEFAULT_CHAIN_ID = 1 // Ethereum mainnet
    }
    
    suspend fun handleIntent(intent: Intent, context: Context): Eip681DeepLinkResult {
        try {
            val data = intent.data ?: return Eip681DeepLinkResult(
                success = false,
                errorMessage = "No data in intent"
            )
            
            val uriString = data.toString()
            Log.d(TAG, "Processing EIP-681 URI: $uriString")
            
            // Parse the EIP-681 URI
            val eip681: Eip681
            try {
                eip681 = parseEip681(uriString)
                Log.d(TAG, "Parsed EIP-681: target=${eip681.target}, chainId=${eip681.chainId}, functionName=${eip681.functionName}")
                Log.d(TAG, "Parsed params (${eip681.params.size}): ${eip681.params.joinToString { "(key=${it.key}, value=${it.rawValue})" }}")
            } catch (e: Eip681ParseException) {
                Log.e(TAG, "Failed to parse EIP-681 URI: ${e.message}")
                return Eip681DeepLinkResult(
                    success = false,
                    errorMessage = "Invalid EIP-681 URI: ${e.message}"
                )
            }
            
            // Extract recipient address
            val recipientAddress = when (val target = eip681.target) {
                is Eip681.HexAddress -> target.value
                is Eip681.EnsName -> target.value // Will be resolved by SendViewModel
            }
            
            // Extract chain ID (default to mainnet if not provided)
            val chainId = eip681.chainId?.toInt() ?: DEFAULT_CHAIN_ID
            
            // Extract amount from parameters
            // For native transfers: look for "value" parameter
            // For ERC20 transfers: look for "uint256" parameter (the amount in transfer function)
            val amountParam = eip681.params.find { it.key == "value" || it.key == "uint256" }
            val amount = amountParam?.rawValue
            Log.d(TAG, "Amount extraction - found param: ${amountParam?.key}, value: $amount")
            
            // Extract contract address if this is an ERC20 transfer
            // If functionName is "transfer", the recipient in the URI is actually the contract address
            val contractAddress: String? = if (eip681.functionName == "transfer") {
                recipientAddress
            } else {
                null
            }
            
            // Determine if this is a native token or ERC20 token transfer
            val groupId: String?
            val finalRecipientAddress: String
            
            if (contractAddress != null) {
                // ERC20 token transfer - look up the actual groupId from database
                val tokenMetadata = tokenMetadataDao.getTokenMetadataByAddressAndChainId(
                    address = contractAddress.lowercase(),
                    chainIdInt = chainId
                )
                
                if (tokenMetadata == null) {
                    Log.w(TAG, "Token not found in database: chainId=$chainId, address=$contractAddress")
                    return Eip681DeepLinkResult(
                        success = false,
                        errorMessage = "Token not found in wallet"
                    )
                }
                
                groupId = tokenMetadata.groupId
                
                if (groupId == null) {
                    Log.w(TAG, "Token has no groupId: chainId=$chainId, address=$contractAddress")
                    return Eip681DeepLinkResult(
                        success = false,
                        errorMessage = "Token configuration error"
                    )
                }
                
                // For ERC20 transfers, the actual recipient is in the parameters
                val toParam = eip681.params.find { it.key == "address" || it.key == "to" }
                finalRecipientAddress = toParam?.rawValue ?: recipientAddress
            } else {
                // Native token transfer
                groupId = if (chainId == 137) "network_matic" else "network_eth"
                finalRecipientAddress = recipientAddress
            }
            
            Log.d(TAG, "Successfully processed EIP-681: recipient=$finalRecipientAddress, amount=$amount, groupId=$groupId, chainId=$chainId")
            
            return Eip681DeepLinkResult(
                success = true,
                recipientAddress = finalRecipientAddress,
                amount = amount,
                groupId = groupId,
                chainId = chainId
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling EIP-681 intent", e)
            return Eip681DeepLinkResult(
                success = false,
                errorMessage = "Error processing request: ${e.message}"
            )
        }
    }
}

