package com.core.data.repository

import com.core.data.remote.CoinbaseTokenExchangeApi
import com.core.model.Exchange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class CoinbaseExchangeRepository @Inject constructor(
    private val coinbaseTokenExchangeApi: CoinbaseTokenExchangeApi
): ExchangeRepository {
    override suspend fun getExchange(pair: String): Exchange = withContext(Dispatchers.IO) {
        val response = coinbaseTokenExchangeApi.getExchangeRate(pair).data
        Exchange(
            response.amount,
            response.base,
            response.currency
        )
    }
}