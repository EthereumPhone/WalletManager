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
        try {
            tokenBalanceRepository.getTokensBalances()
                .collectLatest { tokens ->
                    val filteredTokens = tokens
                        .filter { it.tokenBalance.compareTo(BigDecimal.ZERO) != 0 }

                    val (addresses, networks) = filteredTokens.partition { it.contractAddress.startsWith("0x") }


                    val symbols = tokenMetadataRepository.getTokensMetadata(addresses.map { it.contractAddress })
                        .first()
                        .map { it.symbol } + networks.map { network ->
                        if (network.chainId == 137) "MATIC" else "ETH"
                    }.distinct() // only fetch eth one time


                    val data = tokenPriceDataSource.fetchTokenPriceBySymbols(symbols)

                    val entities = data.flatMap { response ->
                        response.prices.map { price ->
                            TokenExchangeEntity(
                                symbol = response.symbol,
                                currency = price.currency,
                                value = price.value.toDouble(),
                                timestamp = Instant.parse(price.lastUpdatedAt)
                            )
                        }
                    }

                    exchangeDao.insertAllExchanges(entities)
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override suspend fun fetchExchangeByAddress(address: String) {
        TODO("Not yet implemented")
    }

    override fun getExchanges(): Flow<List<TokenExchange>> {
        Log.d("DBSTUFF","getExchange executed")
        return exchangeDao.getExchanges()
    }


}