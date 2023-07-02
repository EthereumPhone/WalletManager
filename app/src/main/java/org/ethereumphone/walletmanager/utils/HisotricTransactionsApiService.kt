package org.ethereumphone.walletmanager.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.ethereumphone.walletmanager.BuildConfig
import org.ethereumphone.walletmanager.models.Exchange
import org.ethereumphone.walletmanager.models.Transaction
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()


interface HistoricTransactionsApiService {
    @GET("/api/v3/ticker/price")
    suspend fun getTransactions(
        @Url url: String? = null,
        @Query("symbol") symbol: String? = null
    ): ArrayList<Transaction>
}

// Singleton
object HistoricTransactionsApi {
    val retrofitService: ExchangeApiService by lazy {
        retrofit.create(ExchangeApiService::class.java)
    }
}