package com.core.data.remote

import com.core.data.util.chainToApiKey
import com.core.model.NetworkChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
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
}