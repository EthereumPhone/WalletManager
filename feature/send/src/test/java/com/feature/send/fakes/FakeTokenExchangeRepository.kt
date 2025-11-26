package com.feature.send.fakes

import com.core.data.repository.TokenExchangeRepository
import com.core.model.TokenExchange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeTokenExchangeRepository : TokenExchangeRepository {

    private val latestByAddress = MutableStateFlow<Map<String, TokenExchange?>>(emptyMap())
    private val latestByAddressAndChain =
        MutableStateFlow<Map<Pair<String, Int>, TokenExchange?>>(emptyMap())
    private val historicalBySymbol = MutableStateFlow<Map<String, List<TokenExchange>>>(emptyMap())

    override fun observeLatestExchangeByAddress(address: String): Flow<TokenExchange?> =
        latestByAddress.asStateFlow().map { it[address] }

    override fun observeLatestExchangeByAddressAndChain(
        address: String,
        chainId: Int
    ): Flow<TokenExchange?> =
        latestByAddressAndChain.asStateFlow().map { it[address to chainId] }

    override fun getHistoricalExchanges(symbol: String): Flow<List<TokenExchange>> =
        historicalBySymbol.asStateFlow().map { it[symbol].orEmpty() }

    override suspend fun fetchExchangeBySymbols(symbols: List<String>) {
        // no-op in tests
    }

    override suspend fun fetchExchangeByAddress(address: String) {
        // no-op in tests
    }

    override suspend fun fetchAllExchanges() {
        // no-op in tests
    }

    fun emitLatestForAddress(address: String, exchange: TokenExchange?) {
        latestByAddress.value = latestByAddress.value.toMutableMap().apply {
            put(address, exchange)
        }
    }

    fun emitLatestForAddressAndChain(address: String, chainId: Int, exchange: TokenExchange?) {
        latestByAddressAndChain.value = latestByAddressAndChain.value.toMutableMap().apply {
            put(address to chainId, exchange)
        }
    }

    fun emitHistorical(symbol: String, exchanges: List<TokenExchange>) {
        historicalBySymbol.value = historicalBySymbol.value.toMutableMap().apply {
            put(symbol, exchanges)
        }
    }
}


