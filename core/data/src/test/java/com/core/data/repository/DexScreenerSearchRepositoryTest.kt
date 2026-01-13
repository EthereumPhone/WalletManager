package com.core.data.repository

import com.core.data.remote.DexScreenerDataSource
import com.core.data.remote.DexScreenerSearchResult
import com.core.data.service.TokenMetadataFetcher
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
    private lateinit var fakeTokenMetadataFetcher: FakeTokenMetadataFetcher
    private lateinit var fakeTokenMetadataRepository: FakeTokenMetadataRepository
    private lateinit var repository: DexScreenerSearchRepository

    @Before
    fun setUp() {
        fakeApi = FakeDexScreenerDataSource()
        fakeTokenMetadataFetcher = FakeTokenMetadataFetcher()
        fakeTokenMetadataRepository = FakeTokenMetadataRepository()
        repository = DexScreenerSearchRepository(
            api = fakeApi,
            tokenMetadataFetcher = fakeTokenMetadataFetcher,
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
    fun `queryTokens fetches on-chain metadata for each token`() = runBlocking {
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

        assertEquals(2, fakeTokenMetadataFetcher.fetchedTokens.size)
        assertTrue(fakeTokenMetadataFetcher.fetchedTokens.any { it.first == "0xtoken1" && it.second == 1 })
        assertTrue(fakeTokenMetadataFetcher.fetchedTokens.any { it.first == "0xtoken2" && it.second == 1 })
    }

    @Test
    fun `queryTokens groups tokens by chainId and processes each chain`() = runBlocking {
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

        // Verify tokens from different chains were fetched
        val chainIds = fakeTokenMetadataFetcher.fetchedTokens.map { it.second }.distinct()
        assertEquals(2, chainIds.size)
        assertTrue(chainIds.contains(1))
        assertTrue(chainIds.contains(42161))
    }

    @Test
    fun `queryTokens inserts fetched metadata into repository`() = runBlocking {
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
        fakeTokenMetadataFetcher.metadataToReturn = TokenMetadataEntity(
            contractAddress = "0xtoken1",
            chainId = 1,
            decimals = 18,
            name = "Token One",
            symbol = "TKN1",
            logo = null,
            swappable = false,
            groupId = null
        )

        repository.queryTokens("test")

        assertEquals(1, fakeTokenMetadataRepository.insertedMetadata.size)
        assertEquals("0xtoken1", fakeTokenMetadataRepository.insertedMetadata[0].contractAddress)
        assertEquals(18, fakeTokenMetadataRepository.insertedMetadata[0].decimals)
    }

    @Test
    fun `queryTokens handles empty search results`() = runBlocking {
        fakeApi.searchResults = emptyList()

        repository.queryTokens("nonexistent")

        assertTrue(fakeTokenMetadataFetcher.fetchedTokens.isEmpty())
        assertTrue(fakeTokenMetadataRepository.insertedMetadata.isEmpty())
    }

    @Test
    fun `queryTokens filters out tokens when on-chain fetch returns null`() = runBlocking {

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
        fakeTokenMetadataFetcher.metadataToReturn = null // Simulate fetch failure

        repository.queryTokens("test")

        // Should still call fetch but no metadata inserted
        assertEquals(1, fakeTokenMetadataFetcher.fetchedTokens.size)
        assertTrue(fakeTokenMetadataRepository.insertedMetadata.isEmpty())
    }

    @Test
    fun `queryTokens processes multiple tokens on same chain`() = runBlocking {
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
            ),
            DexScreenerSearchResult(
                address = "0xtoken3",
                chainId = 1,
                symbol = "TKN3",
                name = "Token Three",
                priceUsd = 3.0,
                liquidity = 3000.0,
                volume24h = 900.0
            )
        )
        fakeApi.searchResults = tokens
        fakeTokenMetadataFetcher.returnDynamicMetadata = true

        repository.queryTokens("test")

        assertEquals(3, fakeTokenMetadataFetcher.fetchedTokens.size)
        assertEquals(3, fakeTokenMetadataRepository.insertedMetadata.size)
    }

    // Fake implementations

    private class FakeDexScreenerDataSource : DexScreenerDataSource {
        var searchResults: List<DexScreenerSearchResult> = emptyList()
        var lastQuery: String? = null

        override suspend fun searchTokens(query: String): List<DexScreenerSearchResult> {
            lastQuery = query
            return searchResults
        }
    }

    private class FakeTokenMetadataFetcher : TokenMetadataFetcher {
        val fetchedTokens = mutableListOf<Triple<String, Int, String>>()
        var metadataToReturn: TokenMetadataEntity? = null
        var returnDynamicMetadata = false

        override suspend fun fetchTokenMetadata(
            contractAddress: String,
            chainId: Int,
            rpcUrl: String
        ): TokenMetadataEntity? {
            fetchedTokens.add(Triple(contractAddress, chainId, rpcUrl))
            return if (returnDynamicMetadata) {
                TokenMetadataEntity(
                    contractAddress = contractAddress,
                    chainId = chainId,
                    decimals = 18,
                    name = "Token $contractAddress",
                    symbol = "TKN",
                    logo = null,
                    swappable = false,
                    groupId = null
                )
            } else {
                metadataToReturn
            }
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
