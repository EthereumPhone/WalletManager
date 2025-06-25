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
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TransferDao
import com.core.database.model.Erc1155MetadataObject
import com.core.database.model.RawContract
import com.core.database.model.TransferEntity
import com.core.terminalsdk.TerminalSDK
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

class SendRepositoryImp @Inject constructor(
    private val web3j: Web3j,
    private val erc20TransferApi: Erc20TransferApi,
    private val mContext: Context,
    private val tokenBalanceDao: TokenBalanceDao,
    private val transferDao: TransferDao,
    private val transferRepository: TransferRepository,
    private val terminalSDK: TerminalSDK?
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


            terminalSDK?.finishScreen()

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

            terminalSDK?.finishScreen()

            val res = try {
                val txHash = erc20TransferApi.sendErc20Token(
                    toAddress,
                    tokenAsset.address,
                    amount,
                    tokenAsset.decimals,
                    chainId
                )
                
                // If the transaction was successful (we got a valid transaction hash)
                if (txHash.isNotEmpty() && txHash != "error" && txHash != "decline") {
                    // Get the current balance from the database
                    val currentBalances = tokenBalanceDao.getTokenBalances(listOf(tokenAsset.address)).first()
                    val currentBalance = currentBalances.firstOrNull { it.contractAddress == tokenAsset.address && it.chainId == chainId }
                    
                    if (currentBalance != null) {
                        // Convert the amount to the smallest unit (e.g., wei) using the token's decimals
                        val amountInSmallestUnit = BigDecimal(amount).multiply(BigDecimal.TEN.pow(tokenAsset.decimals))
                        
                        // Calculate the new balance by subtracting the sent amount
                        val newBalance = currentBalance.tokenBalance - amountInSmallestUnit
                        
                        // Update the balance in the database
                        val updatedBalance = currentBalance.copy(tokenBalance = newBalance)
                        tokenBalanceDao.upsertTokenBalances(listOf(updatedBalance))
                    }

                    val fromAddress = walletSDK.getAddress()
                    val transferEntity = TransferEntity(
                        uniqueId = txHash,
                        asset = tokenAsset.symbol,
                        chainId = chainId,
                        blockNum = "",
                        category = "erc20",
                        erc1155Metadata = emptyList(),
                        erc721TokenId = "",
                        fromaddress = fromAddress,
                        hash = txHash,
                        rawContract = RawContract(
                            address = tokenAsset.address,
                            decimal = tokenAsset.decimals.toString(),
                            value = BigDecimal(amount).multiply(BigDecimal.TEN.pow(tokenAsset.decimals)).toBigInteger().toString()
                        ),
                        toaddress = toAddress,
                        tokenId = tokenAsset.address,
                        value = amount,
                        blockTimestamp = Clock.System.now(),
                        userIsSender = true
                    )
                    transferDao.insertTransfer(transferEntity)
                }
                
                txHash
            } catch (exception: Exception) {
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

    override fun restoreState() {
        currentTransactionHash.value = ""
        currentTransactionChainId.value = 0
    }
}