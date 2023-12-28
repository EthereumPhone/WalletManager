package com.core.data.repository

import com.core.data.remote.Erc20TransferApi
import com.core.data.util.chainToApiKey
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.lang.NullPointerException
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class SendRepositoryImp @Inject constructor(
    private val walletSDK: WalletSDK,
    private val web3j: Web3j,
    private val erc20TransferApi: Erc20TransferApi

): SendRepository {

    override val currentTransactionHash = MutableStateFlow("")
    override val currentTransactionChainId = MutableStateFlow(0)


    override suspend fun transferEth(
        chainId: Int,
        toAddress: String,
        value: String,
        data: String?,
        gasPrice: String?,
        gasAmount: String
    ) {
        withContext(Dispatchers.IO) {
            val decimalValue = BigDecimal(value.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString()

            var gasPrice = web3j.ethGasPrice().send().gasPrice
            gasPrice = gasPrice.add(gasPrice.multiply(BigInteger.valueOf(4)).divide(BigInteger.valueOf(100)))

            if(chainId != walletSDK.getChainId()) {
                val rpc = NetworkChain.getNetworkByChainId(chainId)
                rpc?.let {
                    walletSDK.changeChain(
                        chainId,
                        "https://${NetworkChain.getNetworkByChainId(chainId)?.chainName}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.getNetworkByChainId(chainId)?.chainName!!)}"
                    )
                }
            }

            val res = try {
                walletSDK.sendTransaction(
                    toAddress,
                    decimalValue,
                    data?: "",
                    gasPrice.toString(),
                )
            } catch (exception: NullPointerException) {
                "error"
            }
            currentTransactionHash.value = res
            currentTransactionChainId.value = chainId
        }
    }

    override suspend fun transferErc20(
        chainId: Int,
        tokenAsset: TokenAsset,
        amount: Double,
        toAddress: String
    ) {
        withContext(Dispatchers.IO) {

            if(chainId != walletSDK.getChainId()) {
                val rpc = NetworkChain.getNetworkByChainId(chainId)
                rpc?.let {
                    walletSDK.changeChain(
                        chainId,
                        "https://${NetworkChain.getNetworkByChainId(chainId)?.chainName}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.getNetworkByChainId(chainId)?.chainName!!)}"
                    )
                }
            }

            val res = try {
                erc20TransferApi.sendErc20Token(
                    toAddress,
                    tokenAsset.address,
                    amount,
                    tokenAsset.decimals
                )
            } catch (exception: NullPointerException) {
                "error"
            }
            currentTransactionHash.value = res
            currentTransactionChainId.value = chainId
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