package com.core.data.repository

import com.core.data.remote.DexScreenerDataSource
import com.core.data.remote.DexScreenerSearchResult
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.model.NetworkChain
import com.core.model.TokenMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DexScreenerSearchRepositoryTest {

    private lateinit var fakeApi: FakeDexScreenerDataSource
    private lateinit var fakeTokenMetadataRepository: FakeTokenMetadataRepository
    private lateinit var repository: DexScreenerSearchRepository

    @Before
    fun setUp() {
        fakeApi = FakeDexScreenerDataSource()
        fakeTokenMetadataRepository = FakeTokenMetadataRepository()
        repository = DexScreenerSearchRepository(
            api = fakeApi,
            tokenMetadataRepository = fakeTokenMetadataRepository
        )
    }

    @Test
    fun `queryTokens calls api with correct query`() = runBlocking {
        val query = "USDC"
        
        repository.queryTokens(query)
        
        assertEquals(query, fakeApi.lastQuery)
    }

    @Test
    fun `queryTokens inserts tokens directly from DexScreener results`() = runBlocking {
        val tokens = listOf(
            DexScreenerSearchResult(
                address = "0xtoken1",
                chainId = 1,
                symbol = "TKN1",
                name = "Token One",
                priceUsd = 1.0,
                liquidity = 1000.0,
                volume24h = 500.0
            ),
            DexScreenerSearchResult(
                address = "0xtoken2",
                chainId = 1,
                symbol = "TKN2",
                name = "Token Two",
                priceUsd = 2.0,
                liquidity = 2000.0,
                volume24h = 800.0
            )
        )
        fakeApi.searchResults = tokens

        repository.queryTokens("test")

        assertEquals(2, fakeTokenMetadataRepository.insertedMetadata.size)
        assertTrue(fakeTokenMetadataRepository.insertedMetadata.any { 
            it.contractAddress == "0xtoken1" && it.symbol == "TKN1" && it.name == "Token One"
        })
        assertTrue(fakeTokenMetadataRepository.insertedMetadata.any { 
            it.contractAddress == "0xtoken2" && it.symbol == "TKN2" && it.name == "Token Two"
        })
    }

    @Test
    fun `queryTokens inserts tokens from multiple chains`() = runBlocking {
        val tokens = listOf(
            DexScreenerSearchResult(
                address = "0xeth_token",
                chainId = 1,
                symbol = "ETH_TKN",
                name = "Ethereum Token",
                priceUsd = 1.0,
                liquidity = 1000.0,
                volume24h = 500.0
            ),
            DexScreenerSearchResult(
                address = "0xarb_token",
                chainId = 42161,
                symbol = "ARB_TKN",
                name = "Arbitrum Token",
                priceUsd = 2.0,
                liquidity = 2000.0,
                volume24h = 800.0
            )
        )
        fakeApi.searchResults = tokens

        repository.queryTokens("test")

        // Verify tokens from different chains were inserted
        val chainIds = fakeTokenMetadataRepository.insertedMetadata.map { it.chainId }.distinct()
        assertEquals(2, chainIds.size)
        assertTrue(chainIds.contains(1))
        assertTrue(chainIds.contains(42161))
    }

    @Test
    fun `queryTokens marks all tokens as swappable`() = runBlocking {
        val tokens = listOf(
            DexScreenerSearchResult(
                address = "0xtoken1",
                chainId = 1,
                symbol = "TKN1",
                name = "Token One",
                priceUsd = 1.0,
                liquidity = 1000.0,
                volume24h = 500.0
            )
        )
        fakeApi.searchResults = tokens

        repository.queryTokens("test")

        assertEquals(1, fakeTokenMetadataRepository.insertedMetadata.size)
        assertTrue(fakeTokenMetadataRepository.insertedMetadata[0].swappable)
    }

    @Test
    fun `queryTokens handles empty search results`() = runBlocking {
        fakeApi.searchResults = emptyList()

        repository.queryTokens("nonexistent")

        assertTrue(fakeTokenMetadataRepository.insertedMetadata.isEmpty())
    }

    @Test
    fun `queryTokens filters unsupported chains`() = runBlocking {
        val tokens = listOf(
            DexScreenerSearchResult(
                address = "0xsupported",
                chainId = 8453, // Base - supported
                symbol = "BASE",
                name = "Base Token",
                priceUsd = 1.0,
                liquidity = 1000.0,
                volume24h = 500.0
            ),
            DexScreenerSearchResult(
                address = "0xunsupported",
                chainId = 999999, // Unsupported chain
                symbol = "UNSUP",
                name = "Unsupported Token",
                priceUsd = 2.0,
                liquidity = 2000.0,
                volume24h = 800.0
            )
        )
        fakeApi.searchResults = tokens

        repository.queryTokens("test")

        // Only supported chain token should be inserted
        assertEquals(1, fakeTokenMetadataRepository.insertedMetadata.size)
        assertEquals(8453, fakeTokenMetadataRepository.insertedMetadata[0].chainId)
    }

    @Test
    fun `queryTokens lowercases contract addresses`() = runBlocking {
        val tokens = listOf(
            DexScreenerSearchResult(
                address = "0xABCDEF123456",
                chainId = 1,
                symbol = "TKN",
                name = "Token",
                priceUsd = 1.0,
                liquidity = 1000.0,
                volume24h = 500.0
            )
        )
        fakeApi.searchResults = tokens

        repository.queryTokens("test")

        assertEquals("0xabcdef123456", fakeTokenMetadataRepository.insertedMetadata[0].contractAddress)
    }

    // Fake implementations

    private class FakeDexScreenerDataSource : DexScreenerDataSource {
        var searchResults: List<DexScreenerSearchResult> = emptyList()
        var lastQuery: String? = null
        var tokenByAddressResult: DexScreenerSearchResult? = null

        override suspend fun searchTokens(query: String): List<DexScreenerSearchResult> {
            lastQuery = query
            return searchResults
        }
        
        override suspend fun getTokenByAddress(address: String, chainId: Int): DexScreenerSearchResult? {
            return tokenByAddressResult
        }
    }

    private class FakeTokenMetadataRepository : TokenMetadataRepository {
        val insertedMetadata = mutableListOf<TokenMetadataEntity>()

        override fun getTokensMetadata(): Flow<List<TokenMetadata>> = flowOf(emptyList())

        override fun getTokensMetadata(contractAddresses: List<String>): Flow<List<TokenMetadata>> = flowOf(emptyList())

        override fun getTokensMetadataBySymbols(symbols: List<String>): Flow<List<TokenMetadata>> = flowOf(emptyList())

        override fun getTokensMetadata(chainId: Int): Flow<List<TokenMetadata>> = flowOf(emptyList())

        override suspend fun refreshTokensMetadata(contractAddresses: List<String>, chainId: Int) {}

        override suspend fun refreshTokensMetadataByNetwork(contractAddresses: List<String>, network: NetworkChain) {}

        override suspend fun insertTokenMetadata(tokensMetadata: List<TokenMetadataEntity>) {
            insertedMetadata.addAll(tokensMetadata)
        }

        override suspend fun reconcileTokenGroups() {}
    }
}
