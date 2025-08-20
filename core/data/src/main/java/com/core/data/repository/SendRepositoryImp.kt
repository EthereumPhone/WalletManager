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
import com.core.terminalsdk.ReflectiveLedPattern
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
    private val terminalSDK: TerminalSDK?,
    private val reflectiveLedPattern: ReflectiveLedPattern?
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

            // Check if the amount exceeds the actual database balance
            val currentBalances = tokenBalanceDao.getTokenBalances(listOf(chainId.toString())).first()
            val currentBalance = currentBalances.firstOrNull { it.chainId == chainId }
            
            val amountDouble = value.replace(",",".").toDouble()
            
            val finalAmountDouble = if (currentBalance != null) {
                // Convert the UI amount to wei
                val amountInWei = BigDecimal(amountDouble).multiply(BigDecimal.TEN.pow(18))
                
                // If the amount exceeds the database balance, use the exact database balance
                if (amountInWei > currentBalance.tokenBalance) {
                    // Convert the exact database balance back to ETH
                    currentBalance.tokenBalance.divide(BigDecimal.TEN.pow(18)).toDouble()
                } else {
                    amountDouble
                }
            } else {
                amountDouble
            }
            
            val decimalValue = BigDecimal(finalAmountDouble).times(BigDecimal.TEN.pow(18)).toBigInteger().toString()


            var ethGasPrice = web3j.ethGasPrice().send().gasPrice
            ethGasPrice = ethGasPrice.add(ethGasPrice.multiply(BigInteger.valueOf(4)).divide(BigInteger.valueOf(100)))


            terminalSDK?.finishScreen()
            reflectiveLedPattern?.displayArrowUp()

            val res = try {
                walletSDK.sendTransaction(
                    toAddress,
                    decimalValue,
                    data?: "",
                    BigInteger("120000"),
                    chainId
                )
            } catch (exception: Exception) {
                "error"
            }
            
            // If the transaction was successful (we got a valid bundler tx hash)
            if (res.isNotEmpty() && res != "error" && res != "decline") {
                if (currentBalance != null) {
                    // Use the final amount in wei for balance update
                    val finalAmountInWei = BigDecimal(finalAmountDouble).multiply(BigDecimal.TEN.pow(18))
                    val newBalance = currentBalance.tokenBalance - finalAmountInWei

                    val updatedBalance = currentBalance.copy(tokenBalance = newBalance)
                    tokenBalanceDao.upsertTokenBalances(listOf(updatedBalance))
                }

                // Insert provisional transfer entry so the UI can display it immediately
                val fromAddress = walletSDK.getAddress()
                val transferEntity = TransferEntity(
                    uniqueId = "temp_${res}",
                    asset = "ETH",
                    chainId = chainId,
                    blockNum = "",
                    category = "external",
                    erc1155Metadata = emptyList(),
                    erc721TokenId = "",
                    fromaddress = fromAddress,
                    hash = res,
                    rawContract = RawContract(
                        address = "",
                        decimal = "18",
                        value = decimalValue
                    ),
                    toaddress = toAddress,
                    tokenId = chainId.toString(),
                    value = amountDouble,
                    blockTimestamp = Clock.System.now(),
                    userIsSender = true
                )
                transferDao.insertTransfer(transferEntity)
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

            reflectiveLedPattern?.displayArrowUp()

            // Check if the amount exceeds the actual database balance
            val currentBalances = tokenBalanceDao.getTokenBalances(listOf(tokenAsset.address)).first()
            val currentBalance = currentBalances.firstOrNull { it.contractAddress == tokenAsset.address && it.chainId == chainId }
            
            val finalAmount = if (currentBalance != null) {
                // Convert the UI amount to the smallest unit (e.g., wei)
                val amountInSmallestUnit = BigDecimal(amount).multiply(BigDecimal.TEN.pow(tokenAsset.decimals))
                
                // If the amount exceeds the database balance, use the exact database balance
                if (amountInSmallestUnit > currentBalance.tokenBalance) {
                    // Convert the exact database balance back to human-readable format
                    currentBalance.tokenBalance.divide(BigDecimal.TEN.pow(tokenAsset.decimals)).toDouble()
                } else {
                    amount
                }
            } else {
                amount
            }

            val res = try {
                val txHash = erc20TransferApi.sendErc20Token(
                    toAddress,
                    tokenAsset.address,
                    finalAmount,
                    tokenAsset.decimals,
                    chainId
                )
                
                // If the transaction was successful (we got a valid transaction hash)
                if (txHash.isNotEmpty() && txHash != "error" && txHash != "decline") {
                    if (currentBalance != null) {
                        // Convert the final amount to the smallest unit (e.g., wei) using the token's decimals
                        val amountInSmallestUnit = BigDecimal(finalAmount).multiply(BigDecimal.TEN.pow(tokenAsset.decimals))
                        
                        // Calculate the new balance by subtracting the sent amount
                        val newBalance = currentBalance.tokenBalance - amountInSmallestUnit
                        
                        // Update the balance in the database
                        val updatedBalance = currentBalance.copy(tokenBalance = newBalance)
                        tokenBalanceDao.upsertTokenBalances(listOf(updatedBalance))
                    }

                    val fromAddress = walletSDK.getAddress()
                    val transferEntity = TransferEntity(
                        uniqueId = "temp_${txHash}",
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
        val gasPriceWei = web3j.ethGasPrice().sendAsync().get().gasPrice // current gas price per unit in wei
        
        // Log the gas price for debugging
        val gasPriceGwei = Convert.fromWei(gasPriceWei.toString(), Convert.Unit.GWEI)
        android.util.Log.d("SendRepository", "Chain ID: $chainId")
        android.util.Log.d("SendRepository", "Gas price: ${gasPriceGwei} GWEI")
        
        // For a smart wallet transaction, assume a gas limit of 120,000 units
        val gasLimit = java.math.BigInteger.valueOf(120_000L)
        val totalGasCostWei = gasPriceWei.multiply(gasLimit)
        val totalGasCostEther = Convert.fromWei(totalGasCostWei.toString(), Convert.Unit.ETHER)
        
        // Log the calculation details
        android.util.Log.d("SendRepository", "Gas limit: $gasLimit units")
        android.util.Log.d("SendRepository", "Total gas cost: $totalGasCostEther ETH")
        android.util.Log.d("SendRepository", "Original amount: $amount ETH")
        
        val maxSend = amount.subtract(totalGasCostEther)
        
        // Log the result
        android.util.Log.d("SendRepository", "Max send amount after gas: $maxSend ETH")
        
        // Ensure we never return a negative value
        if (maxSend.signum() <= 0) {
            android.util.Log.d("SendRepository", "Max send is negative or zero, returning 0")
            "0"
        } else {
            maxSend.toPlainString()
        }
    }

    override fun restoreState() {
        currentTransactionHash.value = ""
        currentTransactionChainId.value = 0
    }
}