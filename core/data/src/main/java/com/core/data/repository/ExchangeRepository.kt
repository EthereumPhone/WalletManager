package com.core.data.repository

import com.core.model.Exchange
import kotlinx.coroutines.flow.Flow

interface ExchangeRepository {
    suspend fun getExchange(base: String, currency: String): Flow<Exchange?>
    suspend fun getExchangesBySymbol(base: String): Flow<List<Exchange>>
    suspend fun getExchangesByCurrency(currency: String): Flow<List<Exchange>>
    suspend fun refreshExchange(pair: String)
}