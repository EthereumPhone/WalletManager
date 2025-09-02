package com.core.data.repository

import com.core.model.TokenExchange
import kotlinx.coroutines.flow.Flow

interface TokenExchangeRepository {
    fun observeLatestExchangeByAddress(address: String): Flow<TokenExchange?>
    fun observeLatestExchangeByAddressAndChain(address: String, chainId: Int): Flow<TokenExchange?>
    fun getHistoricalExchanges(symbol: String): Flow<List<TokenExchange>>
    suspend fun fetchExchangeBySymbols(symbols: List<String>)
    suspend fun fetchExchangeByAddress(address: String)
    suspend fun fetchAllExchanges()

}