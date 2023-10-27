package com.core.data.repository

import com.core.data.util.chainToApiKey
import com.core.model.NetworkChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import javax.inject.Inject

class SendRepositoryWalletSDK @Inject constructor(
    private val walletSDK: WalletSDK
): SendRepository {
    override suspend fun sendTo(
        chainId: Int,
        toAddress: String,
        value: String,
        data: String?,
        gasPrice: String?,
        gasAmount: String
    ) {
        withContext(Dispatchers.IO) {
            val decimalValue = BigDecimal(value.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString()
            if(chainId != walletSDK.getChainId()) {
                val rpc = "https://${NetworkChain.getNetworkByChainId(chainId)?.chainName}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.getNetworkByChainId(chainId)?.chainName!!)}"
                rpc?.let {
                    walletSDK.changeChain(
                        chainId,
                        it
                    )
                }
            }

            walletSDK.sendTransaction(
                toAddress,
                decimalValue,
                data?: "",
                gasPrice,
                gasAmount
            )
        }
    }

    override suspend fun maxAllowedSend(
        amount: BigDecimal,
        chainId: Int
    ): String = withContext(Dispatchers.IO) {
        val rpc =  "https://${NetworkChain.getNetworkByChainId(chainId)?.chainName}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.getNetworkByChainId(chainId)?.chainName!!)}"
        val web3j = Web3j.build(HttpService(rpc))
        val gas = web3j.ethGasPrice().sendAsync().get().gasPrice
        val gasEther = Convert.fromWei(gas.toString(), Convert.Unit.ETHER)
        amount.minus(gasEther).toString()
    }
}