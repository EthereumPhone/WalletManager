package com.core.data.repository

import com.core.Resource.Resource
import com.core.model.TokenBalance
import kotlinx.coroutines.flow.Flow

interface NetworkBalanceRepository {
    fun getNetworkBalanceByChainId(
        fetchRemote: Boolean,
        toAddress: String,
        chainId: Int
    ): Flow<Resource<TokenBalance>>
    fun getAllNetworkBalance(
        fetchRemote: Boolean,
        toAddress: String
    ): Flow<Resource<List<TokenBalance>>>
}