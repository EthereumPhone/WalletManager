package com.feature.swap.fakes

import com.core.data.repository.TokenBalanceRepository
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.model.TokenAsset
import com.core.model.TokenBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeTokenBalanceRepository : TokenBalanceRepository {

    private val tokensState = MutableStateFlow<List<TokenAsset>>(emptyList())
    private val balancesWithoutMetadataState = MutableStateFlow<List<TokenBalanceEntity>>(emptyList())
    private val tokenBalancesState = MutableStateFlow<List<TokenBalance>>(emptyList())

    override fun getTokens(): Flow<List<TokenAsset>> = tokensState.asStateFlow()

    override fun observeBalancesWithoutMetadata(): Flow<List<TokenBalanceEntity>> =
        balancesWithoutMetadataState.asStateFlow()

    override fun getCombinedTokens(): Flow<List<TokenAsset>> = tokensState.asStateFlow()

    override fun getTokensBalances(): Flow<List<TokenBalance>> = tokenBalancesState.asStateFlow()

    override fun getTokensBalances(contractAddresses: List<String>): Flow<List<TokenBalance>> =
        tokenBalancesState.asStateFlow().map { balances ->
            val set = contractAddresses.map { it.lowercase() }.toSet()
            balances.filter { set.contains(it.contractAddress.lowercase()) }
        }

    override fun getTokensBalances(chainId: Int): Flow<List<TokenBalance>> =
        tokenBalancesState.asStateFlow().map { balances ->
            balances.filter { it.chainId == chainId }
        }

    override suspend fun refreshTokensBalances(toAddress: String) {
        // no-op for tests
    }

    override suspend fun refreshTokensBalancesByNetwork(toAddress: String, chainId: Int) {
        // no-op for tests
    }

    fun emitTokens(tokens: List<TokenAsset>) {
        tokensState.value = tokens
    }

    fun emitBalancesWithoutMetadata(balances: List<TokenBalanceEntity>) {
        balancesWithoutMetadataState.value = balances
    }

    fun emitTokenBalances(balances: List<TokenBalance>) {
        tokenBalancesState.value = balances
    }
}



