package com.core.data.remote

import androidx.tracing.trace
import com.core.data.BuildConfig
import com.core.data.model.dto.NetworkTokenExchange
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

private interface TokenPriceApi {
    // e.g. GET https://api.g.alchemy.com/prices/v1/{apiKey}/tokens/by-symbol?symbols=ETH
    @GET("{apiKey}/tokens/by-symbol")
    suspend fun getTokenPrice(
        @Query("apiKey") apiKey: String,
        @Query("symbols") symbols: List<String>
    ): NetworkResponse<List<NetworkTokenExchange>>

    //TODO: add headers and post request for get by address
}

@Serializable
private data class NetworkResponse<T>(
    val data: T
)

private const val BASE_URL = "https://getapps"


@Singleton
class RetrofitTokenPrice @Inject constructor(
    okHttpClientFactory: OkHttpClient
    //json: Json
): TokenPriceDataSource {
    private val networkApi = trace("getTokenPricesWmNetwork") {
        Retrofit.Builder()
            .baseUrl("https://api.g.alchemy.com/prices/v1/")
            //.addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TokenPriceApi::class.java)
    }

    override suspend fun fetchTokenPriceByAddresses(addresses: List<String>): List<NetworkTokenExchange> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchTokenPriceBySymbols(symbols: List<String>): List<NetworkTokenExchange> =
        networkApi.getTokenPrice(BuildConfig.ALCHEMY_API, symbols).data



}