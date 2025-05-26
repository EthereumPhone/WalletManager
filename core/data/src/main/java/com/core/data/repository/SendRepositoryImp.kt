package com.core.data.repository

import android.content.Context
import com.core.data.remote.Erc20TransferApi
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainToApiKey
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.lang.NullPointerException
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import com.core.database.dao.TokenBalanceDao
import com.core.database.model.erc20.TokenBalanceEntity
import kotlinx.coroutines.flow.first

class SendRepositoryImp @Inject constructor(
    private val web3j: Web3j,
    private val erc20TransferApi: Erc20TransferApi,
    private val mContext: Context,
    private val tokenBalanceDao: TokenBalanceDao
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
            val rpc = NetworkChain.getNetworkByChainId(chainId)
            val walletSDK = if (rpc != null) {
                WalletSDK(
                    context = mContext,
                    web3jInstance = Web3j.build(HttpService("https://${NetworkChain.getNetworkByChainId(chainId)?.chainName!!}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.getNetworkByChainId(chainId)?.chainName!!)}")),
                    bundlerRPCUrl = chainIdToBundler(chainId)
                )
            } else {
                WalletSDK(mContext, bundlerRPCUrl = chainIdToBundler(chainId))
            }

            if(chainId != walletSDK.getChainId()) {
                rpc?.let {
                    walletSDK.changeChain(
                        chainId,
                        "https://${NetworkChain.getNetworkByChainId(chainId)?.chainName!!}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.getNetworkByChainId(chainId)?.chainName!!)}",
                        chainIdToBundler(chainId)
                    )
                }
            }

            val decimalValue = BigDecimal(value.replace(",",".")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString()
            val amountDouble = value.replace(",",".").toDouble()


            var ethGasPrice = web3j.ethGasPrice().send().gasPrice
            ethGasPrice = ethGasPrice.add(ethGasPrice.multiply(BigInteger.valueOf(4)).divide(BigInteger.valueOf(100)))



            val res = try {
                walletSDK.sendTransaction(
                    toAddress,
                    decimalValue,
                    data?: "",
                    null,
                    chainId
                )
            } catch (exception: Exception) {
                "error"
            }
            currentTransactionHash.value = res
            currentTransactionChainId.value = chainId

            if (res.lowercase().contains("error")) {
                // The address for native ETH is stored as its chainId in string format.
                val currentBalanceEntity = tokenBalanceDao.getTokenBalances(listOf(chainId.toString())).first().firstOrNull()
                if (currentBalanceEntity != null) {
                    val amountInWei = BigDecimal(value.replace(",",".")).multiply(BigDecimal.TEN.pow(18))
                    val newBalance = currentBalanceEntity.tokenBalance.subtract(amountInWei)
                    tokenBalanceDao.upsertTokenBalances(
                        listOf(
                            TokenBalanceEntity(
                                contractAddress = chainId.toString(),
                                chainId = chainId,
                                tokenBalance = newBalance
                            )
                        )
                    )
                }
            }
        }
    }

    override suspend fun transferErc20(
        chainId: Int,
        tokenAsset: TokenAsset,
        amount: Double,
        toAddress: String
    ) {
        withContext(Dispatchers.IO) {

            val rpc = NetworkChain.getNetworkByChainId(chainId)
            val walletSDK = if (rpc != null) {
                WalletSDK(
                    context = mContext,
                    web3jInstance = Web3j.build(HttpService("https://${NetworkChain.getNetworkByChainId(chainId)?.chainName!!}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.getNetworkByChainId(chainId)?.chainName!!)}")),
                    bundlerRPCUrl = chainIdToBundler(chainId)
                )
            } else {
                WalletSDK(mContext, bundlerRPCUrl = chainIdToBundler(chainId))
            }

            if(chainId != walletSDK.getChainId()) {
                rpc?.let {
                    walletSDK.changeChain(
                        chainId,
                        "https://${NetworkChain.getNetworkByChainId(chainId)?.chainName!!}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.getNetworkByChainId(chainId)?.chainName!!)}",
                        chainIdToBundler(chainId)
                    )
                }
            }

            val res = try {
                erc20TransferApi.sendErc20Token(
                    toAddress,
                    tokenAsset.address,
                    amount,
                    tokenAsset.decimals,
                    chainId
                )
            } catch (exception: NullPointerException) {
                "error"
            }
            currentTransactionHash.value = res
            currentTransactionChainId.value = chainId

            if (res.lowercase().contains("error")) {
                val currentBalanceEntity = tokenBalanceDao.getTokenBalances(listOf(tokenAsset.address)).first().firstOrNull()
                if (currentBalanceEntity != null) {
                    val amountInSmallestUnit = BigDecimal(amount).multiply(BigDecimal.TEN.pow(tokenAsset.decimals))
                    val newBalance = currentBalanceEntity.tokenBalance.subtract(amountInSmallestUnit)
                    tokenBalanceDao.upsertTokenBalances(
                        listOf(
                            TokenBalanceEntity(
                                contractAddress = tokenAsset.address,
                                chainId = tokenAsset.chainId,
                                tokenBalance = newBalance
                            )
                        )
                    )
                }
            }
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

    override fun restoreState() {
        currentTransactionHash.value = ""
        currentTransactionChainId.value = 0
    }
}