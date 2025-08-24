package com.core.data.repository

import com.core.model.TokenExchange
import kotlinx.coroutines.flow.Flow

interface TokenExchangeRepository {
    fun getLatestExchange(symbol: String): Flow<TokenExchange?>
    fun getLatestExchangeByAddress(address: String): Flow<TokenExchange?>
    fun getHistoricalExchanges(symbol: String): Flow<List<TokenExchange>>
    suspend fun fetchExchangeBySymbols(symbols: List<String>)
    suspend fun fetchExchangeByAddress(address: String)
    suspend fun fetchAllExchanges()
    fun getExchanges(): Flow<List<TokenExchange>>


}