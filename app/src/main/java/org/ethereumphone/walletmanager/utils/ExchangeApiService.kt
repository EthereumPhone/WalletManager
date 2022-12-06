package org.ethereumphone.walletmanager.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.ethereumphone.walletmanager.models.Exchange
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://data.binance.com"
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ExchangeApiService {
    @GET("/api/v3/ticker/price")
    suspend fun getExchange(
        @Query("symbol") symbol: String? = null
    ): List<Exchange>
}

// Singleton
object ExchangeApi {
    val retrofitService: ExchangeApiService by lazy {
        retrofit.create(ExchangeApiService::class.java)
    }
}