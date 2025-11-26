package com.feature.send.fakes

import com.core.data.repository.NetworkBalanceRepository
import com.core.model.TokenAsset
import com.core.model.TokenBalance
import com.core.model.TokenGroupAssetOverview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal

class FakeNetworkBalanceRepository(
    initialNetworkGroups: List<TokenGroupAssetOverview> = emptyList()
) : NetworkBalanceRepository {

    private val networkTokensState = MutableStateFlow<List<TokenAsset>>(emptyList())
    private val groupedNetworkTokensState = MutableStateFlow<List<TokenAsset>>(emptyList())
    private val groupedNetworkOverviewState = MutableStateFlow(initialNetworkGroups)
    private val networkBalanceStateByChainId = mutableMapOf<Int, MutableStateFlow<TokenBalance>>()

    override fun getNetworkTokens(): Flow<List<TokenAsset>> = networkTokensState.asStateFlow()

    override fun getGroupedNetworkTokens(): Flow<List<TokenAsset>> = groupedNetworkTokensState.asStateFlow()

    override fun getGroupedNetworkTokensOverview(): Flow<List<TokenGroupAssetOverview>> =
        groupedNetworkOverviewState.asStateFlow()

    override fun getNetworkBalance(chainId: Int): Flow<TokenBalance> =
        networkBalanceStateByChainId.getOrPut(chainId) {
            MutableStateFlow(
                TokenBalance(
                    contractAddress = "",
                    chainId = chainId,
                    tokenBalance = BigDecimal.ZERO
                )
            )
        }.asStateFlow()

    override suspend fun refreshNetworkBalance(
        toAddress: String,
        chainIds: List<Int>
    ) {
        // no-op for tests
    }

    override suspend fun refreshNetworkBalanceByNetwork(toAddress: String, chainId: Int) {
        // no-op for tests
    }

    fun emitNetworkTokens(tokens: List<TokenAsset>) {
        networkTokensState.value = tokens
    }

    fun emitGroupedNetworkTokens(tokens: List<TokenAsset>) {
        groupedNetworkTokensState.value = tokens
    }

    fun emitGroupedNetworkOverview(groups: List<TokenGroupAssetOverview>) {
        groupedNetworkOverviewState.value = groups
    }

    fun emitNetworkBalance(balance: TokenBalance) {
        val state = networkBalanceStateByChainId.getOrPut(balance.chainId) {
            MutableStateFlow(balance)
        }
        state.value = balance
    }
}


