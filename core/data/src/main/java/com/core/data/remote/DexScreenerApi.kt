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
    
    @GET("/token-pairs/v1/{chainId}/{tokenAddress}")
    suspend fun getTokenPairs(
        @retrofit2.http.Path("chainId") chainId: String,
        @retrofit2.http.Path("tokenAddress") tokenAddress: String
    ): DexScreenerTokenPairsResponse
}

@JsonClass(generateAdapter = true)
data class DexScreenerSearchResponse(
    @Json(name = "pairs") val pairs: List<DexScreenerPair>?
)

@JsonClass(generateAdapter = true)
data class DexScreenerTokenPairsResponse(
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

// Reverse mapping: EVM chain ID to DexScreener chain name
private val evmToChainName = mapOf(
    1 to "ethereum",
    10 to "optimism",
    137 to "polygon",
    42161 to "arbitrum",
    8453 to "base",
    43114 to "avalanche",
    56 to "bsc"
)

interface DexScreenerDataSource {
    suspend fun searchTokens(query: String): List<DexScreenerSearchResult>
    suspend fun getTokenByAddress(address: String, chainId: Int): DexScreenerSearchResult?
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
        android.util.Log.d("DexScreenerApi", "=== searchTokens called ===")
        android.util.Log.d("DexScreenerApi", "Query: '$query'")
        
        if (query.isBlank() || query.length < 2) {
            android.util.Log.d("DexScreenerApi", "Query too short, returning empty")
            return emptyList()
        }
        
        return try {
            android.util.Log.d("DexScreenerApi", "Calling API: /latest/dex/search?q=$query")
            val response = api.searchPairs(query)
            val pairs = response.pairs
            
            android.util.Log.d("DexScreenerApi", "API returned ${pairs?.size ?: 0} pairs")
            
            if (pairs == null || pairs.isEmpty()) {
                android.util.Log.w("DexScreenerApi", "No pairs returned from API")
                return emptyList()
            }
            
            // Log first few pairs for debugging
            pairs.take(3).forEach { pair ->
                android.util.Log.d("DexScreenerApi", "  Pair: ${pair.baseToken.symbol}/${pair.quoteToken.symbol} on ${pair.chainId} - liquidity: ${pair.liquidity?.usd}")
            }
            
            // Group by token address and chain, keeping highest liquidity pair
            // Extract BOTH baseToken and quoteToken from each pair to capture all matching tokens
            val tokenMap = mutableMapOf<String, DexScreenerSearchResult>()
            
            for (pair in pairs) {
                val evmChainId = chainIdMapping[pair.chainId]
                if (evmChainId == null) {
                    android.util.Log.d("DexScreenerApi", "  Skipping unknown chain: ${pair.chainId}")
                    continue
                }
                
                val priceUsd = pair.priceUsd?.toDoubleOrNull() ?: 0.0
                val liquidity = pair.liquidity?.usd ?: 0.0
                val volume24h = pair.volume?.h24 ?: 0.0
                
                // Process baseToken
                val baseToken = pair.baseToken
                val baseKey = "${baseToken.address.lowercase()}_$evmChainId"
                val existingBase = tokenMap[baseKey]
                if (existingBase == null || liquidity > existingBase.liquidity) {
                    tokenMap[baseKey] = DexScreenerSearchResult(
                        address = baseToken.address,
                        chainId = evmChainId,
                        symbol = baseToken.symbol,
                        name = baseToken.name,
                        priceUsd = priceUsd,
                        liquidity = liquidity,
                        volume24h = volume24h
                    )
                }
                
                // Process quoteToken (also add to results if it matches the search)
                val quoteToken = pair.quoteToken
                val quoteKey = "${quoteToken.address.lowercase()}_$evmChainId"
                val existingQuote = tokenMap[quoteKey]
                // Only add quoteToken if it matches the search query (name or symbol)
                val quoteMatchesQuery = quoteToken.name.contains(query, ignoreCase = true) ||
                    quoteToken.symbol.contains(query, ignoreCase = true) ||
                    quoteToken.address.contains(query, ignoreCase = true)
                if (quoteMatchesQuery && (existingQuote == null || liquidity > existingQuote.liquidity)) {
                    tokenMap[quoteKey] = DexScreenerSearchResult(
                        address = quoteToken.address,
                        chainId = evmChainId,
                        symbol = quoteToken.symbol,
                        name = quoteToken.name,
                        priceUsd = 0.0, // Quote token price not directly available
                        liquidity = liquidity,
                        volume24h = volume24h
                    )
                }
            }
            
            android.util.Log.d("DexScreenerApi", "Mapped to ${tokenMap.size} unique tokens (including quote tokens)")
            tokenMap.values.take(5).forEach { token ->
                android.util.Log.d("DexScreenerApi", "  Token: ${token.symbol} (${token.name}) on chain ${token.chainId}")
            }
            
            // Sort by liquidity descending to show most liquid tokens first
            tokenMap.values.sortedByDescending { it.liquidity }
        } catch (e: Exception) {
            android.util.Log.e("DexScreenerApi", "Search failed for query: $query", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Get token info by contract address on a specific chain.
     * Uses the /token-pairs/v1/{chainId}/{tokenAddress} endpoint.
     */
    override suspend fun getTokenByAddress(address: String, chainId: Int): DexScreenerSearchResult? {
        val chainName = evmToChainName[chainId]
        if (chainName == null) {
            android.util.Log.w("DexScreenerApi", "Unsupported chainId for token lookup: $chainId")
            return null
        }
        
        android.util.Log.d("DexScreenerApi", "=== getTokenByAddress called ===")
        android.util.Log.d("DexScreenerApi", "Address: $address, ChainId: $chainId ($chainName)")
        
        return try {
            val response = api.getTokenPairs(chainName, address)
            val pairs = response.pairs
            
            android.util.Log.d("DexScreenerApi", "Token pairs returned: ${pairs?.size ?: 0}")
            
            if (pairs.isNullOrEmpty()) {
                android.util.Log.w("DexScreenerApi", "No pairs found for token $address on $chainName")
                return null
            }
            
            // Get the pair with highest liquidity
            val bestPair = pairs.maxByOrNull { it.liquidity?.usd ?: 0.0 }
            if (bestPair == null) {
                android.util.Log.w("DexScreenerApi", "No valid pair found")
                return null
            }
            
            val token = bestPair.baseToken
            android.util.Log.d("DexScreenerApi", "Found token: ${token.symbol} (${token.name})")
            
            DexScreenerSearchResult(
                address = token.address,
                chainId = chainId,
                symbol = token.symbol,
                name = token.name,
                priceUsd = bestPair.priceUsd?.toDoubleOrNull() ?: 0.0,
                liquidity = bestPair.liquidity?.usd ?: 0.0,
                volume24h = bestPair.volume?.h24 ?: 0.0
            )
        } catch (e: Exception) {
            android.util.Log.e("DexScreenerApi", "Token lookup failed for $address on chain $chainId", e)
            null
        }
    }
}
