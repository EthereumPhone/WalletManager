package com.core.data.remote

import com.core.data.util.chainToApiKey
import com.core.model.NetworkChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.model.Address
import org.kethereum.rpc.HttpEthereumRPC

class EnsApi {

    suspend fun resolveEns(
        ensAddress: String,
        chainId: Int
    ): String {
        val address = ensAddress.lowercase()
        if(!address.endsWith(".eth")) {
            return ensAddress
        }
        if (ENSName(address).isPotentialENSDomain()) {
            return try {
                withContext(Dispatchers.IO) {
                    val network = NetworkChain.getNetworkByChainId(chainId)
                    val apiKey = network?.let { chainToApiKey(it.chainName) }
                    val ens = ENS(HttpEthereumRPC("https://${network!!.chainName}.g.alchemy.com/v2/$apiKey"))
                    val ensAddr = ens.getAddress(ENSName(address))
                    ensAddr?.hex.toString() ?: address
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return ensAddress
            }
        } else {
            return ensAddress
        }
    }
    
    /**
     * Resolves an Ethereum address to its ENS name using reverse resolution
     * @param ethereumAddress The Ethereum address to resolve (e.g., "0x123...")
     * @param chainId The chain ID to use for resolution (default: 1 for mainnet)
     * @return The ENS name if found, null otherwise
     */
    suspend fun resolveAddressToEns(
        ethereumAddress: String,
        chainId: Int = 1
    ): String? {
        return try {
            withContext(Dispatchers.IO) {
                val network = NetworkChain.getNetworkByChainId(chainId) ?: NetworkChain.MAINNET
                val apiKey = chainToApiKey(network.chainName)
                val rpcUrl = "https://${network.chainName}.g.alchemy.com/v2/$apiKey"
                
                val ens = ENS(HttpEthereumRPC(rpcUrl))
                
                // Create Address object from the ethereum address string
                val address = Address(ethereumAddress)
                
                // Perform reverse resolution
                val ensName = ens.reverseResolve(address)
                
                // Return the ENS name as string if found
                ensName?.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}