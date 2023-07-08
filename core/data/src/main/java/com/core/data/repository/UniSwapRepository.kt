package com.core.data.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.ethereumphone.swaptestapp.RealEncoder
import org.ethereumphone.walletsdk.WalletSDK
import org.ethosmobile.uniswap_routing_sdk.ERC20
import org.ethosmobile.uniswap_routing_sdk.Permit2Contract
import org.ethosmobile.uniswap_routing_sdk.Token
import org.ethosmobile.uniswap_routing_sdk.UniswapRoutingSDK
import org.ethosmobile.uniswap_routing_sdk.UniversalRouter
import org.json.JSONObject
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Numeric
import java.lang.Long.parseLong
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class UniSwapRepository @Inject constructor(
    private val walletSDK: WalletSDK,
    private val web3j: Web3j,
    private val uniswapRouterSDK: UniswapRoutingSDK
): SwapRepository {


    companion object {
        val UNISWAP_V3_ADDRESS = "0x3fC91A3afd70395Cd496C647d5a6CC9D4B2b7FAD"
        val UNISWAP_PERMIT2 = "0x000000000022d473030f116ddee9f6b43ac78ba3"
        val WETH_ADDRESS = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
    }

    // Fee amount
    val FEE_SIZE = 5

    // Uniswap commands
    val permit2PermitCommand: Byte = 0x0a.toByte()
    val exactInSwapCommand: Byte = 0x00.toByte()
    val permitTransferCommand: Byte = 0x02.toByte()
    val wrapEthCommand: Byte = 0x0b.toByte()
    val unwrapWethCommand: Byte = 0x0c.toByte()

    // Random credentials to make web3j happy
    val credentials = Credentials.create("0x0ec8bb8d1aebf3b6e9e838dba065501c06a6ffa4cc12794abfd385eb24accfc1")

    val universalRouter = UniversalRouter.load(UNISWAP_V3_ADDRESS, web3j, credentials, DefaultGasProvider())

    override fun getBestRoute(fromToken: Token, toToken: Token, amount: Double): List<String> {
        return uniswapRouterSDK.getAllRouter(
            inputToken = fromToken,
            outputToken = toToken,
            amountIn = amount,
            receiverAddress = walletSDK.getAddress()
        ).get()
    }

    override suspend fun swap(
        fromToken: Token,
        toToken: Token,
        amount: Double
    ): String {
        if (fromToken == UniswapRoutingSDK.ETH_MAINNET) {
            return swapEthToToken(toToken, amount)
        } else if (toToken == UniswapRoutingSDK.ETH_MAINNET) {
            return swapTokenToEth(fromToken, amount)
        }
        // Get the optimal route for the swap
        val route = getBestRoute(fromToken, toToken, amount)

        // Create the input token contract
        val inputTokenContract =
            ERC20.load(fromToken.address, web3j, credentials, DefaultGasProvider())

        // Calculate the amount to swap
        val fullAmountToSwap =
            BigDecimal(amount).multiply(BigDecimal(10).pow(fromToken.decimals)).toBigInteger()

        // Calculate the fee amount in percent
        val feePercentage =
            BigDecimal(10000 - 9975).divide(BigDecimal(10000), MathContext.DECIMAL128)

        // Calculate the fee amount
        val feeAmount = fullAmountToSwap.multiply(feePercentage.toBigInteger())

        // Calculate the amount to swap without fee
        val amountToSwapWithoutFee = fullAmountToSwap - feeAmount

        // Create Permit2 contract
        val permit2contract =
            Permit2Contract.load(UNISWAP_PERMIT2, web3j, credentials, DefaultGasProvider())

        // Get permit2 nonce
        val nonce = (permit2contract.allowance(
            walletSDK.getAddress(),
            fromToken.address,
            UNISWAP_V3_ADDRESS
        ).get(2) as org.web3j.abi.datatypes.generated.Uint48).value

        // Encode path
        val encodedPath = Numeric.hexStringToByteArray(encodePathExactInput(route))

        // Check if input token is allowed to be spent
        val inputAllowance =
            inputTokenContract.allowance(walletSDK.getAddress(), UNISWAP_PERMIT2).sendAsync().get()
        if (inputAllowance < fullAmountToSwap) {
            // Approve input token
            val approveTokenData =
                inputTokenContract.approve(UNISWAP_PERMIT2, fullAmountToSwap).encodeFunctionCall()
            val approveTxId = walletSDK.sendTransaction(
                to = fromToken.address,
                value = "0x0",
                data = approveTokenData,
                gasAmount = estimateGas(
                    to = fromToken.address,
                    data = approveTokenData,
                    walletSDK = walletSDK,
                    web3j = web3j
                ).toString()
            )
            waitForTxToBeValidated(
                txHash = approveTxId,
                web3j = web3j
            ).get()
        }
        val sigDeadlineAndExpiration =
            BigInteger.valueOf((System.currentTimeMillis() / 1000) + 1500)
        val permit2string = generatePermit2Json(
            Token = fromToken.address,
            Amount = fullAmountToSwap,
            Expiration = sigDeadlineAndExpiration,
            Nonce = nonce,
            Spender = UNISWAP_V3_ADDRESS,
            SigDeadline = sigDeadlineAndExpiration,
            walletSDK = walletSDK
        )
        val signature = walletSDK.signMessage(
            message = permit2string,
            type = "eth_signTypedData"
        )
        val realEncoder = RealEncoder()
        val permitData = realEncoder.encodePermit(
            tokenIn = fromToken.address,
            amount = fullAmountToSwap,
            expiration = sigDeadlineAndExpiration,
            nonce = nonce,
            spender = UNISWAP_V3_ADDRESS,
            sigDeadline = sigDeadlineAndExpiration,
            signature = Numeric.hexStringToByteArray(signature)
        )
        val swapData = realEncoder.encodeSwap(
            receipient = walletSDK.getAddress(),
            amountIn = amountToSwapWithoutFee,
            minOut = BigInteger.ZERO,
            path = encodedPath,
            flag = true
        )
        val permit2TransferData = realEncoder.encodePermitTransfer(
            token = fromToken.address,
            fee = feeAmount
        )


        val universalData = universalRouter.execute(
            byteArrayOf(permit2PermitCommand, permitTransferCommand, exactInSwapCommand),
            mutableListOf<ByteArray>(
                Numeric.hexStringToByteArray(permitData),
                Numeric.hexStringToByteArray(permit2TransferData),
                Numeric.hexStringToByteArray(swapData)
            ),
            BigInteger.ZERO
        ).encodeFunctionCall()
        return walletSDK.sendTransaction(
            to = UNISWAP_V3_ADDRESS,
            value = "0x0",
            data = universalData,
            gasAmount = estimateGas(
                to = UNISWAP_V3_ADDRESS,
                data = universalData,
                walletSDK = walletSDK,
                web3j = web3j
            ).toString()
        )
    }

    private suspend fun swapTokenToEth(fromToken: Token, amount: Double): String {
        // Add a unwrap command at the end
        val toToken: Token = Token(
            address = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
            decimals = 18,
            symbol = "WETH",
            name = "Wrapped Ether",
            chainId = 1
        )
        // Get the optimal route for the swap
        val route = getBestRoute(fromToken, toToken, amount)

        // Create the input token contract
        val inputTokenContract =
            ERC20.load(fromToken.address, web3j, credentials, DefaultGasProvider())

        // Calculate the amount to swap
        val fullAmountToSwap =
            BigDecimal(amount).multiply(BigDecimal(10).pow(fromToken.decimals)).toBigInteger()

        // Calculate the fee amount in percent
        val feePercentage =
            BigDecimal(10000 - 9975).divide(BigDecimal(10000), MathContext.DECIMAL128)

        // Calculate the fee amount
        val feeAmount = fullAmountToSwap.multiply(feePercentage.toBigInteger())

        // Calculate the amount to swap without fee
        val amountToSwapWithoutFee = fullAmountToSwap - feeAmount

        // Create Permit2 contract
        val permit2contract =
            Permit2Contract.load(UNISWAP_PERMIT2, web3j, credentials, DefaultGasProvider())

        // Get permit2 nonce
        val nonce = (permit2contract.allowance(
            walletSDK.getAddress(),
            fromToken.address,
            UNISWAP_V3_ADDRESS
        ).get(2) as org.web3j.abi.datatypes.generated.Uint48).value

        // Encode path
        val encodedPath = Numeric.hexStringToByteArray(encodePathExactInput(route))

        // Check if input token is allowed to be spent
        val inputAllowance =
            inputTokenContract.allowance(walletSDK.getAddress(), UNISWAP_PERMIT2).sendAsync().get()
        if (inputAllowance < fullAmountToSwap) {
            // Approve input token
            val approveTokenData =
                inputTokenContract.approve(UNISWAP_PERMIT2, fullAmountToSwap).encodeFunctionCall()
            val approveTxId = walletSDK.sendTransaction(
                to = fromToken.address,
                value = "0x0",
                data = approveTokenData,
                gasAmount = estimateGas(
                    to = fromToken.address,
                    data = approveTokenData,
                    walletSDK = walletSDK,
                    web3j = web3j
                ).toString()
            )
            waitForTxToBeValidated(
                txHash = approveTxId,
                web3j = web3j
            ).get()
        }
        val sigDeadlineAndExpiration =
            BigInteger.valueOf((System.currentTimeMillis() / 1000) + 1500)
        val permit2string = generatePermit2Json(
            Token = fromToken.address,
            Amount = fullAmountToSwap,
            Expiration = sigDeadlineAndExpiration,
            Nonce = nonce,
            Spender = UNISWAP_V3_ADDRESS,
            SigDeadline = sigDeadlineAndExpiration,
            walletSDK = walletSDK
        )
        val signature = walletSDK.signMessage(
            message = permit2string,
            type = "eth_signTypedData"
        )
        val realEncoder = RealEncoder()
        val permitData = realEncoder.encodePermit(
            tokenIn = fromToken.address,
            amount = fullAmountToSwap,
            expiration = sigDeadlineAndExpiration,
            nonce = nonce,
            spender = UNISWAP_V3_ADDRESS,
            sigDeadline = sigDeadlineAndExpiration,
            signature = Numeric.hexStringToByteArray(signature)
        )
        val swapData = realEncoder.encodeSwap(
            receipient = walletSDK.getAddress(),
            amountIn = amountToSwapWithoutFee,
            minOut = BigInteger.ZERO,
            path = encodedPath,
            flag = true
        )
        val permit2TransferData = realEncoder.encodePermitTransfer(
            token = fromToken.address,
            fee = feeAmount
        )

        val wrapEthData = realEncoder.encodeWEthCommand(
            address = walletSDK.getAddress(),
            amount = fullAmountToSwap
        )


        val universalData = universalRouter.execute(
            byteArrayOf(wrapEthCommand, permit2PermitCommand, permitTransferCommand, exactInSwapCommand),
            mutableListOf<ByteArray>(
                Numeric.hexStringToByteArray(wrapEthData),
                realEncoder.setFirstFourBitsToZero(Numeric.hexStringToByteArray(permitData)),
                Numeric.hexStringToByteArray(permit2TransferData),
                Numeric.hexStringToByteArray(swapData)
            ),
            BigInteger.ZERO
        ).encodeFunctionCall()
        return walletSDK.sendTransaction(
            to = UNISWAP_V3_ADDRESS,
            value = fullAmountToSwap.toString(),
            data = universalData,
            gasAmount = estimateGas(
                to = UNISWAP_V3_ADDRESS,
                data = universalData,
                walletSDK = walletSDK,
                web3j = web3j
            ).toString()
        )
    }

    private suspend fun swapEthToToken(toToken: Token, amount: Double): String {
        // Add a wrap eth command to the commands, before the permit2 command
        val fromToken: Token = Token(
            address = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
            decimals = 18,
            symbol = "WETH",
            name = "Wrapped Ether",
            chainId = 1
        )
        // Get the optimal route for the swap
        val route = getBestRoute(fromToken, toToken, amount)

        // Create the input token contract
        val inputTokenContract =
            ERC20.load(fromToken.address, web3j, credentials, DefaultGasProvider())

        // Calculate the amount to swap
        val fullAmountToSwap =
            BigDecimal(amount).multiply(BigDecimal(10).pow(fromToken.decimals)).toBigInteger()

        // Calculate the fee amount in percent
        val feePercentage =
            BigDecimal(10000 - 9975).divide(BigDecimal(10000), MathContext.DECIMAL128)

        // Calculate the fee amount
        val feeAmount = fullAmountToSwap.multiply(feePercentage.toBigInteger())

        // Calculate the amount to swap without fee
        val amountToSwapWithoutFee = fullAmountToSwap - feeAmount

        // Create Permit2 contract
        val permit2contract =
            Permit2Contract.load(UNISWAP_PERMIT2, web3j, credentials, DefaultGasProvider())

        // Get permit2 nonce
        val nonce = (permit2contract.allowance(
            walletSDK.getAddress(),
            fromToken.address,
            UNISWAP_V3_ADDRESS
        ).get(2) as org.web3j.abi.datatypes.generated.Uint48).value

        // Encode path
        val encodedPath = Numeric.hexStringToByteArray(encodePathExactInput(route))

        // Check if input token is allowed to be spent
        val inputAllowance =
            inputTokenContract.allowance(walletSDK.getAddress(), UNISWAP_PERMIT2).sendAsync().get()
        if (inputAllowance < fullAmountToSwap) {
            // Approve input token
            val approveTokenData =
                inputTokenContract.approve(UNISWAP_PERMIT2, fullAmountToSwap).encodeFunctionCall()
            val approveTxId = walletSDK.sendTransaction(
                to = fromToken.address,
                value = "0x0",
                data = approveTokenData,
                gasAmount = estimateGas(
                    to = fromToken.address,
                    data = approveTokenData,
                    walletSDK = walletSDK,
                    web3j = web3j
                ).toString()
            )
            waitForTxToBeValidated(
                txHash = approveTxId,
                web3j = web3j
            ).get()
        }
        val sigDeadlineAndExpiration =
            BigInteger.valueOf((System.currentTimeMillis() / 1000) + 1500)
        val permit2string = generatePermit2Json(
            Token = fromToken.address,
            Amount = fullAmountToSwap,
            Expiration = sigDeadlineAndExpiration,
            Nonce = nonce,
            Spender = UNISWAP_V3_ADDRESS,
            SigDeadline = sigDeadlineAndExpiration,
            walletSDK = walletSDK
        )
        val signature = walletSDK.signMessage(
            message = permit2string,
            type = "eth_signTypedData"
        )
        val realEncoder = RealEncoder()
        val permitData = realEncoder.encodePermit(
            tokenIn = fromToken.address,
            amount = fullAmountToSwap,
            expiration = sigDeadlineAndExpiration,
            nonce = nonce,
            spender = UNISWAP_V3_ADDRESS,
            sigDeadline = sigDeadlineAndExpiration,
            signature = Numeric.hexStringToByteArray(signature)
        )
        val swapData = realEncoder.encodeSwap(
            receipient = UNISWAP_V3_ADDRESS,
            amountIn = amountToSwapWithoutFee,
            minOut = BigInteger.ZERO,
            path = encodedPath,
            flag = true
        )
        val permit2TransferData = realEncoder.encodePermitTransfer(
            token = fromToken.address,
            fee = feeAmount
        )

        val unwrapEthData = realEncoder.encodeWEthCommand(
            address = walletSDK.getAddress(),
            amount = BigInteger.ZERO
        )

        val universalData = universalRouter.execute(
            byteArrayOf(permit2PermitCommand, permitTransferCommand, exactInSwapCommand, unwrapWethCommand),
            mutableListOf<ByteArray>(
                Numeric.hexStringToByteArray(permitData),
                Numeric.hexStringToByteArray(permit2TransferData),
                Numeric.hexStringToByteArray(swapData),
                realEncoder.setFirstFourBitsToZero(Numeric.hexStringToByteArray(unwrapEthData))
            ),
            BigInteger.ZERO
        ).encodeFunctionCall()
        return walletSDK.sendTransaction(
            to = UNISWAP_V3_ADDRESS,
            value = fullAmountToSwap.toString(),
            data = universalData,
            gasAmount = estimateGas(
                to = UNISWAP_V3_ADDRESS,
                data = universalData,
                walletSDK = walletSDK,
                web3j = web3j
            ).toString()
        )
    }

    fun estimateGas(
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
                BigInteger(parseLong(value.substring(2), 16).toString()),
                data
            )
        )?.sendAsync()?.get()
        if (gas?.hasError() == true) {
            println("Error: ${gas.error.message}")
            throw Exception(gas.error.message)
        }
        return gas?.amountUsed!!
    }


    fun waitForTxToBeValidated(txHash: String, web3j: Web3j): CompletableFuture<Unit> {
        val completableFuture = CompletableFuture<Unit>()
        CompletableFuture.runAsync {
            while (true) {
                val receipt = web3j.ethGetTransactionReceipt(txHash).sendAsync().get()
                if (receipt.hasError()) {
                    println("Error: ${receipt.error.message}")
                    completableFuture.completeExceptionally(Exception(receipt.error.message))
                    return@runAsync
                }
                if (receipt.result != null) {
                    println("Transaction validated!")
                    completableFuture.complete(Unit)
                    return@runAsync
                }
                Thread.sleep(1000)
            }
        }

        return completableFuture
    }

    suspend fun generatePermit2Json(
        Token: String,
        Amount: BigInteger,
        Expiration: BigInteger,
        Nonce: BigInteger,
        Spender: String,
        SigDeadline: BigInteger,
        walletSDK: WalletSDK
    ): String {
        val permit = Permit2(
            Details = Details(
                Token = Token,
                Amount = Amount,
                Expiration = Expiration,
                Nonce = Nonce
            ),
            Spender = Spender,
            SigDeadline = SigDeadline
        )
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val entireTree = "{\"types\":{\"PermitSingle\":[{\"name\":\"details\",\"type\":\"PermitDetails\"},{\"name\":\"spender\",\"type\":\"address\"},{\"name\":\"sigDeadline\",\"type\":\"uint256\"}],\"PermitDetails\":[{\"name\":\"token\",\"type\":\"address\"},{\"name\":\"amount\",\"type\":\"uint160\"},{\"name\":\"expiration\",\"type\":\"uint48\"},{\"name\":\"nonce\",\"type\":\"uint48\"}],\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}]},\"domain\":{\"name\":\"Permit2\",\"chainId\":\"${walletSDK.getChainId()}\",\"verifyingContract\":\"0x000000000022d473030f116ddee9f6b43ac78ba3\"},\"primaryType\":\"PermitSingle\",\"message\":{\"details\":{\"token\":\"0x1f9840a85d5af5bf1d1762f925bdaddc4201f984\",\"amount\":\"1461501637330902918203684832716283019655932542975\",\"expiration\":\"1684404044\",\"nonce\":\"0\"},\"spender\":\"$Spender\",\"sigDeadline\":\"1781813844\"}}"
        val tree = JSONObject(entireTree)
        val message = mapper.writeValueAsString(permit)
        tree.remove("message")
        tree.put("message", JSONObject(message))
        println(tree.toString())
        return tree.toString()
    }

    fun encodePathExactInput(tokens: List<String>): String {
        return encodePath(tokens, List(tokens.size - 1) { FeeAmount.MEDIUM })
    }

    data class Details(
        val Token: String,
        val Amount: BigInteger,
        val Expiration: BigInteger,
        val Nonce: BigInteger
    )

    data class Permit2(
        val Details: Details,
        val Spender: String,
        val SigDeadline: BigInteger
    )

    fun encodePath(path: List<String>, fees: List<FeeAmount>): String {
        if (path.size != fees.size + 1) {
            throw Error("path/fee lengths do not match")
        }

        var encoded = "0x"
        for (i in fees.indices) {
            // 20 byte encoding of the address
            encoded += path[i].substring(2)
            // 3 byte encoding of the fee
            encoded += fees[i].value.toString(16).padStart(2 * FEE_SIZE, '0')
        }
        // encode the final token
        encoded += path.last().substring(2)

        return encoded.toLowerCase()
    }

    enum class FeeAmount(val value: Int) {
        LOWEST(100),
        LOW(500),
        MEDIUM(3000),
        HIGH(10000)
    }

}