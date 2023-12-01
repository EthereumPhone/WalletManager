package com.core.data.repository

import com.core.model.Exchange

interface ExchangeRepository {
    suspend fun getExchange(pair: String): Exchange
}