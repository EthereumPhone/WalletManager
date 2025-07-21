package com.core.data.remote

import androidx.tracing.trace
import com.core.data.BuildConfig
import com.core.data.model.dto.NetworkTokenExchange
import com.core.data.model.dto.TokenAddress
import com.core.data.model.dto.TokenPriceAddressesRequest
import com.core.data.model.dto.TokenPricesResponse
import com.core.data.util.chainIdToName
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

private interface TokenPriceApi {
    // e.g. GET https://api.g.alchemy.com/prices/v1/{apiKey}/tokens/by-symbol?symbols=ETH
    @GET("/prices/v1/{apiKey}/tokens/by-symbol")
    suspend fun getTokenPrice(
        @Path("apiKey") apiKey: String,
        @Query("symbols") symbols: List<String>
    ): NetworkResponse<List<NetworkTokenExchange>>

    @POST("/prices/v1/{apiKey}/tokens/by-address")
    suspend fun getTokenPriceByAddress(
        @Path("apiKey") apiKey: String,
        @Body body: TokenPriceAddressesRequest
    ): TokenPricesResponse
}

@JsonClass(generateAdapter = true)
private data class NetworkResponse<T>(
    val data: T
)

private const val BASE_URL = "https://api.g.alchemy.com/"


@Singleton
class RetrofitTokenPrice @Inject constructor(
    okHttpClientFactory: OkHttpClient,
    moshi: Moshi
): TokenPriceDataSource {
    private val networkApi = trace("getTokenPricesWmNetwork") {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClientFactory)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TokenPriceApi::class.java)
    }

    override suspend fun fetchTokenPriceByAddresses(addresses: List<TokenAddress>)
    : TokenPricesResponse {
        val fixRequest = addresses.map { it.copy(network = chainIdToName(it.network.toInt())) }

        val request = TokenPriceAddressesRequest(addresses = fixRequest)
        return networkApi.getTokenPriceByAddress(BuildConfig.ALCHEMY_API, request)
    }

    override suspend fun fetchTokenPriceBySymbols(symbols: List<String>): List<NetworkTokenExchange> =
        networkApi.getTokenPrice(BuildConfig.ALCHEMY_API, symbols).data



}