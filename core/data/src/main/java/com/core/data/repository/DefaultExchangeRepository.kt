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
        // Get the latest token balances.
        // TokenBalanceEntity has 'contractAddress' and 'tokenBalance'
        val allBalances = tokenBalanceRepository.getTokensBalances().first()

        // Filter for balances > 0
        val balancesWithSufficientAmount = allBalances
            .filter { it.tokenBalance > BigDecimal.ZERO }

        if (balancesWithSufficientAmount.isEmpty()) {
            Log.d("fetchAllExchanges", "No token balances greater than zero found.")
            return // No balances to process
        }

        // Get all contract addresses for tokens with balance > 0
        val contractAddresses = balancesWithSufficientAmount.map { it.contractAddress }

        // Fetch metadata for all these addresses at once
        val metadataList = tokenMetadataRepository.getTokensMetadata(contractAddresses).first()

        // Extract unique symbols from the metadata
        val symbolsToFetch = metadataList.map { it.symbol }.distinct()

        if (symbolsToFetch.isEmpty()) {
            Log.d("fetchAllExchanges", "No symbols could be determined for tokens with balance > 0.")
            return // No symbols to fetch
        }

        Log.d("fetchAllExchanges", "Symbols to fetch: ${symbolsToFetch.joinToString()}")

        // API has a 25 symbol limit per call, so chunk the list of symbols
        val chunkSize = 25
        symbolsToFetch.chunked(chunkSize).forEach { chunk ->
            try {
                fetchExchangeBySymbols(chunk)
            } catch (e: IOException) {
                // The fetchExchangeBySymbols method already has its own try-catch.
                // Log error for this specific chunk.
                Log.e("fetchAllExchanges", "Error fetching exchange data for chunk ${chunk.joinToString()}", e)
            }
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