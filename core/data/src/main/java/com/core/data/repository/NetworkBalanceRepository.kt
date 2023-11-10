package com.core.data.repository

import com.core.model.NetworkChain
import com.core.model.TokenBalance
import kotlinx.coroutines.flow.Flow

interface NetworkBalanceRepository {
    fun getNetworksBalance(): Flow<List<TokenBalance>>
    fun getNetworkBalance(chainId: Int): Flow<TokenBalance>
    suspend fun refreshNetworkBalance(
        toAddress: String,
        chainIds: List<Int> = NetworkChain.getAllNetworkChains().map { it.chainId }
    )

    suspend fun refreshNetworkBalanceByNetwork(
        toAddress: String,
        networkChain: NetworkChain
    )
}