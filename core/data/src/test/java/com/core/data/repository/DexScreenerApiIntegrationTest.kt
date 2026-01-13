package com.core.data.repository

import com.core.data.remote.DexScreenerSearchResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Integration test that makes REAL API calls to DexScreener.
 * Run this test to verify the API is working and see actual responses.
 * 
 * Note: These tests depend on external API availability and may fail
 * if the API is down or rate limited.
 */
class DexScreenerApiIntegrationTest {

    private lateinit var api: DexScreenerTestApiService

    @Before
    fun setUp() {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            println("[HTTP] $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        api = Retrofit.Builder()
            .baseUrl("https://api.dexscreener.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DexScreenerTestApiService::class.java)
    }

    @Test
    fun `search for USDC tokens`() = runBlocking {
        println("\n========== REAL API TEST: Search for USDC ==========\n")
        
        val response = api.searchPairs("USDC")
        val pairs = response.pairs ?: emptyList()
        
        println("Found ${pairs.size} pairs for 'USDC'\n")
        
        pairs.take(10).forEachIndexed { index, pair ->
            println("[$index] ${pair.baseToken.symbol} (${pair.baseToken.name})")
            println("    Chain: ${pair.chainId}")
            println("    Address: ${pair.baseToken.address}")
            println("    Price USD: ${pair.priceUsd ?: "N/A"}")
            println("    Liquidity: \$${pair.liquidity?.usd ?: "N/A"}")
            println("    Volume 24h: \$${pair.volume?.h24 ?: "N/A"}")
            println()
        }
        
        println("========== END ==========\n")
    }

    @Test
    fun `search for ETH tokens`() = runBlocking {
        println("\n========== REAL API TEST: Search for ETH ==========\n")
        
        val response = api.searchPairs("ETH")
        val pairs = response.pairs ?: emptyList()
        
        println("Found ${pairs.size} pairs for 'ETH'\n")
        
        pairs.take(10).forEachIndexed { index, pair ->
            println("[$index] ${pair.baseToken.symbol} (${pair.baseToken.name})")
            println("    Chain: ${pair.chainId}")
            println("    Address: ${pair.baseToken.address}")
            println("    Price USD: ${pair.priceUsd ?: "N/A"}")
            println("    Liquidity: \$${pair.liquidity?.usd ?: "N/A"}")
            println()
        }
        
        println("========== END ==========\n")
    }

    @Test
    fun `search for specific contract address`() = runBlocking {
        println("\n========== REAL API TEST: Search by Contract Address ==========\n")
        
        // USDC on Ethereum mainnet
        val usdcAddress = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"
        
        val response = api.searchPairs(usdcAddress)
        val pairs = response.pairs ?: emptyList()
        
        println("Found ${pairs.size} pairs for USDC contract\n")
        
        pairs.take(5).forEachIndexed { index, pair ->
            println("[$index] ${pair.baseToken.symbol}/${pair.quoteToken.symbol}")
            println("    DEX: ${pair.dexId}")
            println("    Chain: ${pair.chainId}")
            println("    Pair Address: ${pair.pairAddress}")
            println("    Price USD: ${pair.priceUsd ?: "N/A"}")
            println("    Liquidity: \$${pair.liquidity?.usd ?: "N/A"}")
            println()
        }
        
        println("========== END ==========\n")
    }

    @Test
    fun `search for a random token`() = runBlocking {
        println("\n========== REAL API TEST: Search for PEPE ==========\n")
        
        val response = api.searchPairs("PEPE")
        val pairs = response.pairs ?: emptyList()
        
        println("Found ${pairs.size} pairs for 'PEPE'\n")
        
        // Group by chain to see distribution
        val byChain = pairs.groupBy { it.chainId }
        println("Distribution by chain:")
        byChain.forEach { (chain, chainPairs) ->
            println("  $chain: ${chainPairs.size} pairs")
        }
        println()
        
        // Show top 5 by liquidity
        val topByLiquidity = pairs
            .filter { it.liquidity?.usd != null }
            .sortedByDescending { it.liquidity?.usd ?: 0.0 }
            .take(5)
        
        println("Top 5 by liquidity:")
        topByLiquidity.forEachIndexed { index, pair ->
            println("[$index] ${pair.baseToken.symbol} on ${pair.chainId}")
            println("    Address: ${pair.baseToken.address}")
            println("    Price USD: ${pair.priceUsd ?: "N/A"}")
            println("    Liquidity: \$${pair.liquidity?.usd}")
            println()
        }
        
        println("========== END ==========\n")
    }
}

// Test-only API service interface (mirrors the production one)
private interface DexScreenerTestApiService {
    @GET("/latest/dex/search")
    suspend fun searchPairs(@Query("q") query: String): DexScreenerSearchResponse
}
