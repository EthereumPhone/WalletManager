package com.core.data.repository

import com.core.model.TokenBalance
import kotlinx.coroutines.flow.Flow

interface TokenBalanceRepository {

    fun getTokensBalances(): Flow<List<TokenBalance>>
    fun getTokensBalances(contractAddresses: List<String>): Flow<List<TokenBalance>>
    suspend fun refreshTokensBalances(toAddress: String)

}