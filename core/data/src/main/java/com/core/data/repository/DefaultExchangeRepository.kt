package com.core.data.repository

import android.util.Log
import com.core.data.model.dto.TokenAddress
import com.core.data.remote.TokenPriceDataSource
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenExchangeDao
import com.core.database.dao.TokenGroupDao
import com.core.database.model.erc20.TokenExchangeEntity
import com.core.database.model.erc20.asExternalModel
import com.core.database.model.erc20.asExternalModule
import com.core.database.model.erc20.toExternalModel
import com.core.model.NetworkChain
import com.core.model.TokenExchange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import okio.IOException
import java.math.BigDecimal
import javax.inject.Inject


class DefaultExchangeRepository @Inject constructor(
    private val tokenPriceDataSource: TokenPriceDataSource,
    private val exchangeDao: TokenExchangeDao,
    private val tokenBalanceRepository: TokenBalanceRepository,
    private val groupedTokenRepository: GroupedTokenRepository,
    private val tokenBalanceDao: TokenBalanceDao,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenGroupDao: TokenGroupDao
): TokenExchangeRepository {

    override fun observeLatestExchangeByAddressAndChain(
        address: String,
        chainId: Int
    ): Flow<TokenExchange?> = exchangeDao.observeLatestExchangeByAddressAndChain(address, chainId)
        .map { it?.asExternalModel() }


    override fun observeLatestExchangeByAddress(address: String): Flow<TokenExchange?> =
        exchangeDao.observeLatestExchangeByAddress(address)
            .map { it?.asExternalModel() }



    override fun getHistoricalExchanges(symbol: String): Flow<List<TokenExchange>> {
        TODO()
    }

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
        try {
            // Get all active token groups (groups with balance > 0)
            val activeTokenGroups = tokenGroupDao.getActiveTokenGroupsWithExchange()
                .filterNot { item ->
                    DEFAULT_EXCLUDE_LIST.any { snippet ->
                        item.tokenGroup.name.contains(snippet, ignoreCase = true) ||
                                item.tokenGroup.symbol.contains(snippet, ignoreCase = true)
                    }
                }

            val tokenAddressesWithMetadata = mutableListOf<TokenAddressWithMetadata>()
            
            for (tokenGroup in activeTokenGroups) {
                // Skip native tokens (ETH and MATIC) - we'll handle them separately
                if (tokenGroup.tokenGroup.symbol == "ETH" || tokenGroup.tokenGroup.symbol == "MATIC") {
                    continue
                }
                
                // For each token group, we only need to fetch the price once
                // Since all tokens in a group share the same price, we can use any representative token
                // Choose the token with the highest balance or the canonical one
                val representativeToken = tokenGroup.tokensWithExchange
                    .filter { it.tokenBalanceEntity?.tokenBalance?.compareTo(BigDecimal.ZERO) == 1 }
                    .maxByOrNull { it.tokenBalanceEntity?.tokenBalance ?: BigDecimal.ZERO }
                    ?: tokenGroup.tokensWithExchange.firstOrNull { 
                        it.tokenMetadataEntity.chainId == tokenGroup.tokenGroup.canonicalChainId 
                    }
                    ?: tokenGroup.tokensWithExchange.firstOrNull()
                
                representativeToken?.let { token ->
                    tokenAddressesWithMetadata.add(
                        TokenAddressWithMetadata(
                            address = token.tokenMetadataEntity.contractAddress,
                            chainId = token.tokenMetadataEntity.chainId,
                            symbol = token.tokenMetadataEntity.symbol
                        )
                    )
                }
            }
            
            // Get network currencies that have balance
            val networkBalances = tokenBalanceDao.getTokensWithBalance()
                .filter { balance ->
                    // Check if this is a native token address
                    NetworkChain.getAllNetworkChains().any { chain ->
                        balance.contractAddress == chain.chainId.toString() ||
                        balance.contractAddress == "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE" ||
                        (balance.contractAddress == "0x0000000000000000000000000000000000001010" && chain.chainId == 137)
                    }
                }
            
            // Determine which native currencies to fetch by symbol
            val nativeCurrencySymbols = mutableSetOf<String>()
            
            for (balance in networkBalances) {
                when {
                    balance.chainId == 137 || balance.contractAddress == "0x0000000000000000000000000000000000001010" -> {
                        nativeCurrencySymbols.add("MATIC")
                    }
                    else -> {
                        nativeCurrencySymbols.add("ETH")
                    }
                }
            }
            
            // Fetch native currencies by symbol
            if (nativeCurrencySymbols.isNotEmpty()) {
                Log.d("DefaultExchangeRepository", "Fetching native currency exchanges for: $nativeCurrencySymbols")
                fetchExchangeBySymbols(nativeCurrencySymbols.toList())
            }
            
            // Fetch ERC20 tokens by address/chainId with proper batching
            if (tokenAddressesWithMetadata.isNotEmpty()) {
                Log.d("DefaultExchangeRepository", "Fetching exchanges for ${tokenAddressesWithMetadata.size} ERC20 tokens by address")
                fetchExchangeByAddressesBatch(tokenAddressesWithMetadata)
            }
            
            if (nativeCurrencySymbols.isEmpty() && tokenAddressesWithMetadata.isEmpty()) {
                Log.d("DefaultExchangeRepository", "No tokens with balance found, skipping exchange fetch")
            }
            
        } catch (e: Exception) {
            Log.e("DefaultExchangeRepository", "Error in fetchAllExchanges", e)
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
}