package com.feature.send.fakes

import com.core.data.repository.SendRepository
import com.core.model.TokenAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal

class FakeSendRepository : SendRepository {

    private val txHashState = MutableStateFlow("")
    private val txChainIdState = MutableStateFlow(0)

    var transferEthCalls = mutableListOf<TransferEthCall>()
    var transferErc20Calls = mutableListOf<TransferErc20Call>()

    private val maxNativeByChainId = mutableMapOf<Int, String>()
    private val maxErc20ByKey = mutableMapOf<Triple<String, Int, Int>, String>()
    
    // Stubbed transaction result - if set, overrides the default behavior
    private var stubbedTransactionResult: String? = null

    override val currentTransactionHash: Flow<String> = txHashState.asStateFlow()
    override val currentTransactionChainId: Flow<Int> = txChainIdState.asStateFlow()

    override suspend fun transferEth(
        chainId: Int,
        toAddress: String,
        value: String,
        data: String?,
        gasPrice: String?,
        gasAmount: String
    ) {
        transferEthCalls.add(
            TransferEthCall(chainId, toAddress, value, data, gasPrice, gasAmount)
        )
        txChainIdState.value = chainId
        txHashState.value = stubbedTransactionResult ?: "0xFAKE_ETH_TX_${transferEthCalls.size}"
    }

    override suspend fun transferErc20(
        chainId: Int,
        tokenAsset: TokenAsset,
        amount: Double,
        toAddress: String
    ) {
        transferErc20Calls.add(
            TransferErc20Call(chainId, tokenAsset, amount, toAddress)
        )
        txChainIdState.value = chainId
        txHashState.value = stubbedTransactionResult ?: "0xFAKE_ERC20_TX_${transferErc20Calls.size}"
    }
    
    /**
     * Stub the transaction result that will be returned after transfer calls.
     * Use "decline" to simulate user declining, "error" for errors, or a valid hash for success.
     */
    fun stubTransactionResult(result: String) {
        stubbedTransactionResult = result
    }
    
    /**
     * Clear the stubbed transaction result to restore default behavior.
     */
    fun clearTransactionResultStub() {
        stubbedTransactionResult = null
    }
    
    /**
     * Reset all recorded calls and stubs for a fresh test state.
     */
    fun reset() {
        transferEthCalls.clear()
        transferErc20Calls.clear()
        stubbedTransactionResult = null
        maxNativeByChainId.clear()
        maxErc20ByKey.clear()
        restoreState()
    }

    override suspend fun maxAllowedSend(
        amount: BigDecimal,
        chainId: Int
    ): String {
        return maxNativeByChainId[chainId] ?: amount.toPlainString()
    }

    override fun restoreState() {
        txHashState.value = ""
        txChainIdState.value = 0
    }

    override suspend fun getMaxErc20AmountString(
        contractAddress: String,
        chainId: Int,
        decimals: Int
    ): String {
        val key = Triple(contractAddress, chainId, decimals)
        return maxErc20ByKey[key] ?: "0"
    }

    fun stubMaxAllowedSend(chainId: Int, value: String) {
        maxNativeByChainId[chainId] = value
    }

    fun stubMaxErc20(contractAddress: String, chainId: Int, decimals: Int, value: String) {
        maxErc20ByKey[Triple(contractAddress, chainId, decimals)] = value
    }

    data class TransferEthCall(
        val chainId: Int,
        val toAddress: String,
        val value: String,
        val data: String?,
        val gasPrice: String?,
        val gasAmount: String
    )

    data class TransferErc20Call(
        val chainId: Int,
        val tokenAsset: TokenAsset,
        val amount: Double,
        val toAddress: String
    )
}


