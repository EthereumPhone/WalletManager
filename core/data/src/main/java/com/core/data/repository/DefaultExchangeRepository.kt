package com.core.data.repository

import android.util.Log
import com.core.data.remote.TokenPriceDataSource
import com.core.database.dao.TokenExchangeDao
import com.core.database.model.erc20.TokenExchangeEntity
import com.core.model.TokenExchange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import okio.IOException
import java.math.BigDecimal
import javax.inject.Inject


class DefaultExchangeRepository @Inject constructor(
    private val tokenPriceDataSource: TokenPriceDataSource,
    private val exchangeDao: TokenExchangeDao,
    private val tokenBalanceRepository: TokenBalanceRepository,
    private val tokenMetadataRepository: TokenMetadataRepository
): TokenExchangeRepository {
    override fun getLatestExchange(symbol: String): Flow<TokenExchange?> =
        exchangeDao.getLatestExchange(symbol)

    override fun getHistoricalExchanges(symbol: String): Flow<List<TokenExchange>> =
        exchangeDao.getHistoricalExchange(symbol)

    override suspend fun fetchExchangeBySymbols(symbols: List<String>) {
        try {
            val data = tokenPriceDataSource.fetchTokenPriceBySymbols(symbols)

            val entities = data.flatMap { response ->
                response.prices.map { price ->
                    TokenExchangeEntity(
                        symbol = response.symbol,
                        address = null,
                        chainId = null,
                        currency = price.currency,
                        value = price.value.toDouble(),
                        timestamp = Instant.parse(price.lastUpdatedAt)
                    )
                }
            }

            exchangeDao.insertAllExchanges(entities)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override suspend fun fetchAllExchanges() {

    }

    override suspend fun fetchExchangeByAddress(address: String) {
        TODO("Not yet implemented")
    }

    override fun getExchanges(): Flow<List<TokenExchange>> {
        Log.d("DBSTUFF","getExchange executed")
        return exchangeDao.getExchanges()
    }


}