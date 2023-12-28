package com.core.data.remote


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
    private val walletSDK: WalletSDK,
    private val web3j: Web3j
) {

    suspend fun sendErc20Token(
        toAddress: String,
        erc20ContractAddress: String,
        amount: Double,
        decimals: Int,
    ): String {
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
            realAmount.toBigIntegerExact()
        ).encodeFunctionCall()

        return walletSDK.sendTransaction(
            to = erc20ContractAddress,
            value = "0",
            data = data,
            gasAmount = estimateGas(
                erc20ContractAddress,
                data,
                "0x0",
                walletSDK,
                web3j
            ).toString()
        )
    }

    private fun estimateGas(
        to: String,
        data: String,
        value: String = "0x0",
        walletSDK: WalletSDK,
        web3j: Web3j
    ): BigInteger {
        val gas = web3j.ethEstimateGas(
            org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
                walletSDK.getAddress(),
                null,
                null,
                null,
                to,
                BigInteger(Long.parseLong(value.substring(2), 16).toString()),
                data
            )
        )?.sendAsync()?.get()
        if (gas?.hasError() == true) {
            println("Error: ${gas.error.message}")
            //throw Exception(gas.error.message)
            return BigInteger.valueOf(240000)
        }
        val gasResult = gas?.amountUsed!!
        println("Gas estimation: $gasResult")
        return gasResult
    }
}