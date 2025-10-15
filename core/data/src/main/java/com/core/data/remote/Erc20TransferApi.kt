package com.core.data.remote


import android.content.Context
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainIdToName
import com.core.data.util.chainIdToRPC
import com.core.data.util.chainToApiKey
import com.core.data.utils.GasEstimationHelper
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.ethereumphone.walletsdk.WalletSDK
import org.ethosmobile.uniswap_routing_sdk.ERC20
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import java.lang.Long
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class Erc20TransferApi @Inject constructor(
    private val context: Context,
) {
    private var currentChainId: Int = 1

    suspend fun sendErc20Token(
        toAddress: String,
        erc20ContractAddress: String,
        amount: Double,
        decimals: Int,
        chainId: Int
    ): String {
        // Store chainId for use in gasProvider
        currentChainId = chainId
        // Build web3j and WalletSDK
        val web3j = Web3j.build(HttpService(chainIdToRPC(chainId)))
        val walletSDK = WalletSDK(
            context = context,
            web3jInstance = web3j,
            bundlerRPCUrl = chainIdToBundler(chainId)
        )
        val credentials = Credentials.create("0x0ec8bb8d1aebf3b6e9e838dba065501c06a6ffa4cc12794abfd385eb24accfc1")
        val contract = ERC20.load(
            erc20ContractAddress,
            web3j,
            credentials,
            DefaultGasProvider()
        )
        val realAmount = BigDecimal(amount.toString()).multiply(BigDecimal.TEN.pow(decimals))

        val data = contract.transfer(
            toAddress,
            realAmount.toBigInteger()  // Truncates decimal places instead of throwing exception
        ).encodeFunctionCall()

        return walletSDK.sendTransaction(
            to = erc20ContractAddress,
            value = "0",
            data = data,
            callGas = null,
            chainId = chainId,
            gasProvider = ::gasProvider
        )
    }

    suspend fun gasProvider(userOp: WalletSDK.UserOperation): WalletSDK.GasEstimation {
        // Get Alchemy RPC URL for the chain
        val alchemyUrl = chainIdToRPC(currentChainId)
        
        // Use the shared gas estimation helper
        return GasEstimationHelper.estimateGas(userOp, alchemyUrl)
    }
}