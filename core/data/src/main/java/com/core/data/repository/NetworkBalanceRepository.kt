package com.core.data.repository

import com.core.database.model.erc20.CompositeToken
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenBalance
import kotlinx.coroutines.flow.Flow

interface NetworkBalanceRepository {
    fun getNetworkTokens(): Flow<List<TokenAsset>>
    fun getGroupedNetworkTokens(): Flow<List<TokenAsset>>
    fun getNetworkBalance(chainId: Int): Flow<TokenBalance>
    suspend fun refreshNetworkBalance(
        toAddress: String,
        chainIds: List<Int> = NetworkChain.getAllNetworkChains().map { it.chainId }
    )

    suspend fun refreshNetworkBalanceByNetwork(toAddress: String, chainId: Int)
}