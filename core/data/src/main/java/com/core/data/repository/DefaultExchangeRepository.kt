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

    /**
     * Fetches token prices by their addresses with proper batching.
     * Batches are limited to max 25 addresses across max 3 networks per request.
     */
    private suspend fun fetchExchangeByAddressesBatch(addressesWithMetadata: List<TokenAddressWithMetadata>) {
        if (addressesWithMetadata.isEmpty()) return

        // Group addresses by network
        val addressesByNetwork = addressesWithMetadata.groupBy { it.chainId }
        
        // Create batches respecting both constraints
        val batches = mutableListOf<List<TokenAddressWithMetadata>>()
        var currentBatch = mutableListOf<TokenAddressWithMetadata>()
        var currentNetworks = mutableSetOf<Int>()
        var currentAddressCount = 0

        for ((chainId, addresses) in addressesByNetwork) {
            for (address in addresses) {
                // Check if adding this address would exceed our constraints
                val wouldExceedNetworkLimit = !currentNetworks.contains(chainId) && currentNetworks.size >= 3
                val wouldExceedAddressLimit = currentAddressCount >= 25

                if (wouldExceedNetworkLimit || wouldExceedAddressLimit) {
                    // Save current batch and start a new one
                    if (currentBatch.isNotEmpty()) {
                        batches.add(currentBatch.toList())
                    }
                    currentBatch = mutableListOf()
                    currentNetworks = mutableSetOf()
                    currentAddressCount = 0
                }

                currentBatch.add(address)
                currentNetworks.add(chainId)
                currentAddressCount++
            }
        }

        // Don't forget the last batch
        if (currentBatch.isNotEmpty()) {
            batches.add(currentBatch)
        }

        // Process each batch
        for (batch in batches) {
            try {
                val tokenAddresses = batch.map { 
                    TokenAddress(network = it.chainId.toString(), address = it.address)
                }

                val response = tokenPriceDataSource.fetchTokenPriceByAddresses(tokenAddresses)

                if (response.error != null) {
                    Log.e("DefaultExchangeRepository", "Error from API for batch: ${response.error.message}")
                    continue
                }

                val entities = response.data.flatMap { tokenPriceInfo ->
                    val metadata = batch.find { it.address == tokenPriceInfo.address }
                    if (metadata == null) {
                        Log.w("DefaultExchangeRepository", "No metadata found for address: ${tokenPriceInfo.address}")
                        return@flatMap emptyList()
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
                }

                if (entities.isNotEmpty()) {
                    exchangeDao.insertAllExchanges(entities)
                }

            } catch (e: Exception) {
                Log.e("DefaultExchangeRepository", "Error fetching exchange data for batch", e)
            }
        }
    }

    // Data class to hold address with its metadata
    private data class TokenAddressWithMetadata(
        val address: String,
        val chainId: Int,
        val symbol: String
    )

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

        val addressesWithMetadata = metadataList.map {
            TokenAddressWithMetadata(
                address = it.contractAddress,
                chainId = it.chainId,
                symbol = it.symbol
            )
        }

        fetchExchangeByAddressesBatch(addressesWithMetadata)
    }




    override suspend fun fetchAllExchanges() {
        // Get the latest token balances
        val allBalances = tokenBalanceRepository.getTokensBalances().first()

        // Filter for balances > 0
        val balancesWithSufficientAmount = allBalances
            .filter { it.tokenBalance > BigDecimal.ZERO }

        if (balancesWithSufficientAmount.isEmpty()) {
            Log.d("fetchAllExchanges", "No token balances greater than zero found.")
            return
        }

        // Separate network currencies from ERC20 tokens
        val (networkCurrencies, erc20Tokens) = balancesWithSufficientAmount.partition {
            !it.contractAddress.startsWith("0x")
        }

        // Handle network currencies using symbol-based fetching
        val networkSymbols = networkCurrencies.mapNotNull { balance ->
            val chainId = balance.contractAddress.toIntOrNull()
            when (chainId) {
                137 -> "MATIC"    // Polygon
                else -> "ETH"      // All other chains use ETH
            }
        }.distinct()

        // Handle ERC20 tokens using address-based fetching
        val addressesWithMetadata = mutableListOf<TokenAddressWithMetadata>()
        
        if (erc20Tokens.isNotEmpty()) {
            // Refresh metadata for all chains
            val chainIds = erc20Tokens.map { it.chainId }.distinct()
            chainIds.forEach { chainId ->
                val addressesForChain = erc20Tokens.filter { it.chainId == chainId }.map { it.contractAddress }
                tokenMetadataRepository.refreshTokensMetadata(addressesForChain, chainId)
            }

            // Get metadata for all ERC20 tokens
            val contractAddresses = erc20Tokens.map { it.contractAddress }
            val metadataList = tokenMetadataRepository.getTokensMetadata(contractAddresses).first()

            // Create address entries with metadata
            for (metadata in metadataList) {
                addressesWithMetadata.add(
                    TokenAddressWithMetadata(
                        address = metadata.contractAddress,
                        chainId = metadata.chainId,
                        symbol = metadata.symbol
                    )
                )
            }
        }

        // Fetch prices for native tokens using symbol-based API
        if (networkSymbols.isNotEmpty()) {
            Log.d("fetchAllExchanges", "Fetching native token prices for symbols: ${networkSymbols.joinToString()}")
            
            // API has a 25 symbol limit per call, so chunk if necessary
            networkSymbols.chunked(25).forEach { chunk ->
                try {
                    fetchExchangeBySymbols(chunk)
                } catch (e: IOException) {
                    Log.e("fetchAllExchanges", "Error fetching exchange data for native tokens ${chunk.joinToString()}", e)
                }
            }
        }

        // Fetch prices for ERC20 tokens using address-based API
        if (addressesWithMetadata.isNotEmpty()) {
            Log.d("fetchAllExchanges", "Fetching ERC20 token prices for ${addressesWithMetadata.size} addresses")
            fetchExchangeByAddressesBatch(addressesWithMetadata)
        }
    }

    override suspend fun fetchExchangeByAddress(address: String) {
        // First, try to find metadata for this address
        val metadata = tokenMetadataRepository.getTokensMetadata(listOf(address)).first().firstOrNull()
        
        if (metadata != null) {
            // We found metadata, use it to fetch the price
            val addressWithMetadata = TokenAddressWithMetadata(
                address = metadata.contractAddress,
                chainId = metadata.chainId,
                symbol = metadata.symbol
            )
            fetchExchangeByAddressesBatch(listOf(addressWithMetadata))
        } else {
            // No metadata found, this could be a native token or unknown token
            // Try common native token addresses
            val nativeAddresses = mutableListOf<TokenAddressWithMetadata>()
            
            // Check if it's a known native token address
            when (address) {
                "0x0000000000000000000000000000000000001010" -> {
                    // MATIC on Polygon
                    nativeAddresses.add(
                        TokenAddressWithMetadata(
                            address = address,
                            chainId = 137,
                            symbol = "MATIC"
                        )
                    )
                }
                "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE" -> {
                    // ETH on various chains - try common ones
                    listOf(1, 10, 42161, 8453).forEach { chainId ->
                        nativeAddresses.add(
                            TokenAddressWithMetadata(
                                address = address,
                                chainId = chainId,
                                symbol = "ETH"
                            )
                        )
                    }
                }
                else -> {
                    // Unknown address, try to fetch from common chains
                    // This is a best-effort approach
                    Log.w("fetchExchangeByAddress", "No metadata found for address: $address. Attempting common chains.")
                    
                    listOf(1, 137, 10, 42161, 8453).forEach { chainId ->
                        nativeAddresses.add(
                            TokenAddressWithMetadata(
                                address = address,
                                chainId = chainId,
                                symbol = "UNKNOWN"
                            )
                        )
                    }
                }
            }
            
            if (nativeAddresses.isNotEmpty()) {
                fetchExchangeByAddressesBatch(nativeAddresses)
            } else {
                Log.e("fetchExchangeByAddress", "Unable to determine chain for address: $address")
            }
        }
    }

    override fun getExchanges(): Flow<List<TokenExchange>> {
        Log.d("DBSTUFF","getExchange executed")
        return exchangeDao.getExchanges()
    }


}