package com.core.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DexScreener API for searching tokens by name, symbol, or contract address.
 * Free tier rate limit: 60 requests per minute.
 */
private interface DexScreenerApiService {
    @GET("/latest/dex/search")
    suspend fun searchPairs(
        @Query("q") query: String
    ): DexScreenerSearchResponse
}

@JsonClass(generateAdapter = true)
data class DexScreenerSearchResponse(
    @Json(name = "pairs") val pairs: List<DexScreenerPair>?
)

@JsonClass(generateAdapter = true)
data class DexScreenerPair(
    @Json(name = "chainId") val chainId: String,
    @Json(name = "dexId") val dexId: String,
    @Json(name = "pairAddress") val pairAddress: String,
    @Json(name = "baseToken") val baseToken: DexScreenerToken,
    @Json(name = "quoteToken") val quoteToken: DexScreenerToken,
    @Json(name = "priceUsd") val priceUsd: String?,
    @Json(name = "liquidity") val liquidity: DexScreenerLiquidity?,
    @Json(name = "volume") val volume: DexScreenerVolume?,
    @Json(name = "fdv") val fdv: Double?
)

@JsonClass(generateAdapter = true)
data class DexScreenerToken(
    @Json(name = "address") val address: String,
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String
)

@JsonClass(generateAdapter = true)
data class DexScreenerLiquidity(
    @Json(name = "usd") val usd: Double?,
    @Json(name = "base") val base: Double?,
    @Json(name = "quote") val quote: Double?
)

@JsonClass(generateAdapter = true)
data class DexScreenerVolume(
    @Json(name = "h24") val h24: Double?,
    @Json(name = "h6") val h6: Double?,
    @Json(name = "h1") val h1: Double?,
    @Json(name = "m5") val m5: Double?
)

private const val DEXSCREENER_BASE_URL = "https://api.dexscreener.com/"

// Map DexScreener chain IDs to EVM chain IDs
private val chainIdMapping = mapOf(
    "ethereum" to 1,
    "optimism" to 10,
    "polygon" to 137,
    "arbitrum" to 42161,
    "base" to 8453,
    "avalanche" to 43114,
    "bsc" to 56,
    "fantom" to 250,
    "cronos" to 25,
    "gnosis" to 100,
    "celo" to 42220,
    "moonbeam" to 1284,
    "moonriver" to 1285,
    "metis" to 1088,
    "aurora" to 1313161554,
    "harmony" to 1666600000,
    "boba" to 288,
    "zksync" to 324,
    "linea" to 59144,
    "scroll" to 534352,
    "mantle" to 5000,
    "blast" to 81457
)

interface DexScreenerDataSource {
    suspend fun searchTokens(query: String): List<DexScreenerSearchResult>
}

/**
 * Simplified search result that can be easily converted to TokenAssetWithPrice
 */
data class DexScreenerSearchResult(
    val address: String,
    val chainId: Int,
    val symbol: String,
    val name: String,
    val priceUsd: Double,
    val liquidity: Double,
    val volume24h: Double
)

@Singleton
class DexScreenerApiClient @Inject constructor(
    okHttpClient: OkHttpClient,
    moshi: Moshi
) : DexScreenerDataSource {
    
    private val api: DexScreenerApiService = Retrofit.Builder()
        .baseUrl(DEXSCREENER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(DexScreenerApiService::class.java)
    
    override suspend fun searchTokens(query: String): List<DexScreenerSearchResult> {
        if (query.isBlank() || query.length < 2) return emptyList()
        
        return try {
            val response = api.searchPairs(query)
            val pairs = response.pairs ?: return emptyList()
            
            // Group by token address and chain, keeping highest liquidity pair
            val tokenMap = mutableMapOf<String, DexScreenerSearchResult>()
            
            for (pair in pairs) {
                val evmChainId = chainIdMapping[pair.chainId] ?: continue
                val token = pair.baseToken
                val key = "${token.address.lowercase()}_$evmChainId"
                
                val priceUsd = pair.priceUsd?.toDoubleOrNull() ?: 0.0
                val liquidity = pair.liquidity?.usd ?: 0.0
                val volume24h = pair.volume?.h24 ?: 0.0
                
                val existing = tokenMap[key]
                if (existing == null || liquidity > existing.liquidity) {
                    tokenMap[key] = DexScreenerSearchResult(
                        address = token.address,
                        chainId = evmChainId,
                        symbol = token.symbol,
                        name = token.name,
                        priceUsd = priceUsd,
                        liquidity = liquidity,
                        volume24h = volume24h
                    )
                }
            }
            
            // Sort by liquidity descending to show most liquid tokens first
            tokenMap.values.sortedByDescending { it.liquidity }
        } catch (e: Exception) {
            android.util.Log.e("DexScreenerApi", "Search failed for query: $query", e)
            emptyList()
        }
    }
}
