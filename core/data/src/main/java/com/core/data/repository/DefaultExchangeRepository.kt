package com.core.data.repository

import android.util.Log
import com.core.data.model.dto.TokenAddress
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

            val entities = data
                .filter { response -> 
                    // Only process tokens that don't have errors and have price data
                    response.error == null && response.prices.isNotEmpty()
                }
                .flatMap { response ->
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

            if (entities.isNotEmpty()) {
                exchangeDao.insertAllExchanges(entities)
            }

            val failedSymbols = data.filter { it.error != null }
            if (failedSymbols.isNotEmpty()) {
                Log.w("DefaultExchangeRepository", "No valid price data found for symbols: $failedSymbols. Attempting fallback.")
                fetchExchangeByAddressForFailedSymbols(failedSymbols.map { it.symbol })
            }
        } catch (e: Exception) {
            Log.e("DefaultExchangeRepository", "Error fetching exchange data for symbols: $symbols", e)
            e.printStackTrace()
        }
    }

    private suspend fun fetchExchangeByAddressForFailedSymbols(symbols: List<String>) {
        val metadataList = tokenMetadataRepository.getTokensMetadataBySymbols(symbols).first()

        if (metadataList.isEmpty()) {
            Log.w("fetchExchangeByAddressForFailedSymbols", "No metadata found for failed symbols: $symbols")
            return
        }

        val addressesToFetch = metadataList.map {
            TokenAddress(network = it.chainId.toString(), address = it.contractAddress)
        }

        try {
            val response = tokenPriceDataSource.fetchTokenPriceByAddresses(addressesToFetch)

            if (response.error != null) {
                Log.e("fetchExchangeByAddressForFailedSymbols", "Error from by-address API: ${response.error.message}")
                return
            }

            val entities = response.data.mapNotNull { tokenPriceInfo ->
                val metadata = metadataList.find { it.contractAddress == tokenPriceInfo.address }
                if (metadata == null) {
                    Log.w("fetchExchangeByAddressForFailedSymbols", "No metadata found for address: ${tokenPriceInfo.address}")
                    return@mapNotNull null
                }

                tokenPriceInfo.prices.map { price ->
                    TokenExchangeEntity(
                        symbol = metadata.symbol,
                        address = tokenPriceInfo.address,
                        chainId = metadata.chainId,
                        currency = price.currency,
                        value = price.value.toDouble(),
                        timestamp = Instant.parse(price.lastUpdatedAt)
                    )
                }
            }.flatten()

            if (entities.isNotEmpty()) {
                exchangeDao.insertAllExchanges(entities)
            }

        } catch (e: Exception) {
            Log.e("fetchExchangeByAddressForFailedSymbols", "Error fetching exchange data for addresses", e)
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

        // Separate network currencies from ERC20 tokens
        val (networkCurrencies, erc20Tokens) = balancesWithSufficientAmount.partition {
            !it.contractAddress.startsWith("0x")
        }

        // Handle network currencies
        val networkSymbols = networkCurrencies.mapNotNull { balance ->
            val chainId = balance.contractAddress.toIntOrNull()
            when (chainId) {
                137 -> "MATIC"    // Polygon
                else -> "ETH"      // Unknown chain, skip
            }
        }.distinct()

        // Get all contract addresses for ERC20 tokens
        val contractAddresses = erc20Tokens.map { it.contractAddress }
        val chainIds = erc20Tokens.map { it.chainId }.distinct()

        // Fetch metadata for ERC20 tokens
        if (contractAddresses.isNotEmpty()) {
            chainIds.forEach { chainId ->
                val addressesForChain = erc20Tokens.filter { it.chainId == chainId }.map { it.contractAddress }
                tokenMetadataRepository.refreshTokensMetadata(addressesForChain, chainId)
            }
        }
        val metadataList = if (contractAddresses.isNotEmpty()) {
            tokenMetadataRepository.getTokensMetadata(contractAddresses).first()
        } else {
            emptyList()
        }

        // Extract unique symbols from the metadata
        val erc20Symbols = metadataList.map { it.symbol }.distinct()

        // Combine network symbols and ERC20 symbols
        val symbolsToFetch = (networkSymbols + erc20Symbols).distinct()

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