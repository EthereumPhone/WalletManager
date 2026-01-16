package com.core.data.repository

import android.content.Context
import com.core.data.remote.Erc20TransferApi
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainToApiKey
import com.core.data.utils.GasEstimationHelper
import com.core.model.NetworkChain
import com.core.model.NftTokenType
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
import java.math.RoundingMode
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

    // Store chainId for use in gasProvider
    private var currentChainId: Int = 1

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
            // Store chainId for gas provider
            currentChainId = chainId
            
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

            // Use precise conversion: ETH (string) -> WEI (BigDecimal) without floating imprecision
            val valueNormalized = value.replace(",", ".")
            val amountWeiNoFraction = BigDecimal(valueNormalized)
                .movePointRight(18)
                .setScale(0, RoundingMode.DOWN)

            // If the requested amount exceeds the DB balance, clamp to the DB balance
            val finalWei: BigDecimal = if (currentBalance != null) {
                // DB stores native token balance in ETHER → convert to WEI for comparison
                val dbWei = currentBalance.tokenBalance
                    .movePointRight(18)
                    .setScale(0, RoundingMode.DOWN)
                if (amountWeiNoFraction.compareTo(dbWei) > 0) dbWei else amountWeiNoFraction
            } else {
                amountWeiNoFraction
            }

            val decimalValue = finalWei.toPlainString()


            var ethGasPrice = web3j.ethGasPrice().send().gasPrice
            ethGasPrice = ethGasPrice.add(ethGasPrice.multiply(BigInteger.valueOf(4)).divide(BigInteger.valueOf(100)))


            terminalSDK?.finishScreen()
            reflectiveLedPattern?.displayArrowUp()

            val res = try {
                walletSDK.sendTransaction(
                    to = toAddress,
                    value = decimalValue,
                    data = "",
                    callGas = null,
                    chainId = chainId,
                    gasProvider = ::gasProvider
                )
            } catch (exception: Exception) {
                "error"
            }
            
            // If the transaction was successful (we got a valid bundler tx hash)
            if (res.isNotEmpty() && res != "error" && res != "decline") {
                if (currentBalance != null) {
                    // Convert the final amount back to ETHER to update the DB (DB stores ETHER for native tokens)
                    val finalAmountEther = finalWei.movePointLeft(18)
                    val newBalanceEther = currentBalance.tokenBalance.subtract(finalAmountEther)

                    val updatedBalance = currentBalance.copy(tokenBalance = newBalanceEther)
                    tokenBalanceDao.upsertTokenBalances(listOf(updatedBalance))
                }

                // Insert provisional transfer entry so the UI can display it immediately
                val fromAddress = walletSDK.getAddress()
                val finalAmountEther = finalWei.movePointLeft(18)
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
                    value = finalAmountEther.toDouble(),
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
                // Convert the UI amount to the smallest unit (e.g., wei) using precise BigDecimal
                val amountInSmallestUnit = BigDecimal.valueOf(amount).multiply(BigDecimal.TEN.pow(tokenAsset.decimals))
                
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
                        // Convert the final amount to the smallest unit (e.g., wei) using the token's decimals (precise)
                        val amountInSmallestUnit = BigDecimal.valueOf(finalAmount).multiply(BigDecimal.TEN.pow(tokenAsset.decimals))
                        
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
                            value = BigDecimal.valueOf(amount).multiply(BigDecimal.TEN.pow(tokenAsset.decimals)).toBigInteger().toString()
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

    override suspend fun getMaxErc20AmountString(
        contractAddress: String,
        chainId: Int,
        decimals: Int
    ): String = withContext(Dispatchers.IO) {
        val entity = tokenBalanceDao.getTokenBalanceEntity(contractAddress, chainId)
        val raw = entity?.tokenBalance ?: BigDecimal.ZERO
        raw
            .movePointLeft(decimals)
            .stripTrailingZeros()
            .toPlainString()
    }

    override fun restoreState() {
        currentTransactionHash.value = ""
        currentTransactionChainId.value = 0
    }

    override suspend fun transferNft(
        chainId: Int,
        contractAddress: String,
        tokenId: String,
        toAddress: String,
        tokenType: NftTokenType,
        amount: Int
    ) {
        withContext(Dispatchers.IO) {
            currentChainId = chainId

            val rpc = NetworkChain.getNetworkByChainId(chainId)
            val walletSDK = if (rpc != null) {
                WalletSDK(
                    context = mContext,
                    web3jInstance = Web3j.build(HttpService("https://${rpc.chainName}.g.alchemy.com/v2/${chainToApiKey(rpc.chainName)}")),
                    bundlerRPCUrl = chainIdToBundler(chainId)
                )
            } else {
                WalletSDK(mContext, bundlerRPCUrl = chainIdToBundler(chainId))
            }

            if (chainId != walletSDK.getChainId()) {
                rpc?.let {
                    walletSDK.changeChain(
                        chainId,
                        "https://${rpc.chainName}.g.alchemy.com/v2/${chainToApiKey(rpc.chainName)}",
                        chainIdToBundler(chainId)
                    )
                }
            }

            terminalSDK?.finishScreen()
            reflectiveLedPattern?.displayArrowUp()

            val fromAddress = walletSDK.getAddress()
            val tokenIdBigInt = BigInteger(tokenId)
            
            // Encode the appropriate function call based on token type
            val data = when (tokenType) {
                NftTokenType.ERC721 -> {
                    // ERC721 safeTransferFrom(address from, address to, uint256 tokenId)
                    // Function selector: 0x42842e0e
                    val methodId = "42842e0e"
                    val fromAddressPadded = fromAddress.removePrefix("0x").lowercase().padStart(64, '0')
                    val toAddressPadded = toAddress.removePrefix("0x").lowercase().padStart(64, '0')
                    val tokenIdHex = tokenIdBigInt.toString(16).padStart(64, '0')
                    "0x$methodId$fromAddressPadded$toAddressPadded$tokenIdHex"
                }
                NftTokenType.ERC1155 -> {
                    // ERC1155 safeTransferFrom(address from, address to, uint256 id, uint256 amount, bytes data)
                    // Function selector: 0xf242432a
                    val methodId = "f242432a"
                    val fromAddressPadded = fromAddress.removePrefix("0x").lowercase().padStart(64, '0')
                    val toAddressPadded = toAddress.removePrefix("0x").lowercase().padStart(64, '0')
                    val tokenIdHex = tokenIdBigInt.toString(16).padStart(64, '0')
                    val amountHex = BigInteger.valueOf(amount.toLong()).toString(16).padStart(64, '0')
                    // bytes data offset (points to position 160 = 0xa0, which is after the 5 fixed params)
                    val dataOffset = "00000000000000000000000000000000000000000000000000000000000000a0"
                    // bytes data length (0 = empty bytes)
                    val dataLength = "0000000000000000000000000000000000000000000000000000000000000000"
                    "0x$methodId$fromAddressPadded$toAddressPadded$tokenIdHex$amountHex$dataOffset$dataLength"
                }
                NftTokenType.ERC404 -> {
                    // ERC404 is a hybrid ERC-20/ERC-721 token. For NFT transfers, it uses ERC-721's safeTransferFrom
                    // safeTransferFrom(address from, address to, uint256 tokenId)
                    // Function selector: 0x42842e0e
                    val methodId = "42842e0e"
                    val fromAddressPadded = fromAddress.removePrefix("0x").lowercase().padStart(64, '0')
                    val toAddressPadded = toAddress.removePrefix("0x").lowercase().padStart(64, '0')
                    val tokenIdHex = tokenIdBigInt.toString(16).padStart(64, '0')
                    "0x$methodId$fromAddressPadded$toAddressPadded$tokenIdHex"
                }
                NftTokenType.UNKNOWN -> {
                    // Default to ERC721 behavior for unknown types
                    android.util.Log.w("SendRepository", "Unknown NFT token type, defaulting to ERC721")
                    val methodId = "42842e0e"
                    val fromAddressPadded = fromAddress.removePrefix("0x").lowercase().padStart(64, '0')
                    val toAddressPadded = toAddress.removePrefix("0x").lowercase().padStart(64, '0')
                    val tokenIdHex = tokenIdBigInt.toString(16).padStart(64, '0')
                    "0x$methodId$fromAddressPadded$toAddressPadded$tokenIdHex"
                }
            }

            val res = try {
                walletSDK.sendTransaction(
                    to = contractAddress,
                    value = "0",
                    data = data,
                    callGas = null,
                    chainId = chainId,
                    gasProvider = ::gasProvider
                )
            } catch (exception: Exception) {
                android.util.Log.e("SendRepository", "NFT transfer failed", exception)
                "error"
            }

            // If successful, insert provisional transfer entry
            if (res.isNotEmpty() && res != "error" && res != "decline") {
                val category = when (tokenType) {
                    NftTokenType.ERC721 -> "erc721"
                    NftTokenType.ERC1155 -> "erc1155"
                    NftTokenType.ERC404 -> "erc721" // ERC404 NFT transfers are recorded as ERC721
                    NftTokenType.UNKNOWN -> "erc721"
                }
                
                val erc1155Metadata = if (tokenType == NftTokenType.ERC1155) {
                    listOf(Erc1155MetadataObject(tokenId = tokenId, value = amount.toString()))
                } else {
                    emptyList()
                }
                
                val transferEntity = TransferEntity(
                    uniqueId = "temp_${res}",
                    asset = "NFT",
                    chainId = chainId,
                    blockNum = "",
                    category = category,
                    erc1155Metadata = erc1155Metadata,
                    erc721TokenId = if (tokenType == NftTokenType.ERC721 || tokenType == NftTokenType.ERC404) tokenId else "",
                    fromaddress = fromAddress,
                    hash = res,
                    rawContract = RawContract(
                        address = contractAddress,
                        decimal = "0",
                        value = amount.toString()
                    ),
                    toaddress = toAddress,
                    tokenId = tokenId,
                    value = amount.toDouble(),
                    blockTimestamp = Clock.System.now(),
                    userIsSender = true
                )
                transferDao.insertTransfer(transferEntity)
            }

            currentTransactionHash.value = res
            currentTransactionChainId.value = chainId
        }
    }
    
    /**
     * Gas provider for ETH transfers using the shared GasEstimationHelper
     */
    private suspend fun gasProvider(userOp: WalletSDK.UserOperation): WalletSDK.GasEstimation {
        // Get Alchemy RPC URL for the chain
        val rpcUrl = "https://${NetworkChain.getNetworkByChainId(currentChainId)?.chainName}.g.alchemy.com/v2/${chainToApiKey(NetworkChain.getNetworkByChainId(currentChainId)?.chainName!!)}"
        
        // Use the shared gas estimation helper
        return GasEstimationHelper.estimateGas(userOp, rpcUrl)
    }
}