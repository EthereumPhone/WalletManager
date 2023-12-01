package com.core.data.repository

import com.core.data.remote.CoinbaseTokenExchangeApi
import com.core.database.dao.TokenExchangeDao
import com.core.database.model.ExchangeEntity
import com.core.database.model.asExternalModel
import com.core.model.Exchange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class CoinbaseExchangeRepository @Inject constructor(
    private val coinbaseTokenExchangeApi: CoinbaseTokenExchangeApi,
    private val exchangeDao: TokenExchangeDao
): ExchangeRepository {
    override suspend fun getExchange(base: String, currency: String): Flow<Exchange?> =
        exchangeDao.getExchange(base, currency)
            .map{ it?.asExternalModel() }

    override suspend fun getExchangesBySymbol(base: String): Flow<List<Exchange>> =
        exchangeDao.getExchangesByBase(base)
            .map{ it.map (ExchangeEntity::asExternalModel) }

    override suspend fun getExchangesByCurrency(currency: String): Flow<List<Exchange>> {
        TODO("Not yet implemented")
    }


    override suspend fun refreshExchange(pair: String) {
        val response = coinbaseTokenExchangeApi.getExchangeRate(pair).data
        Exchange(
            response.amount,
            response.base,
            response.currency
        )
    }
}