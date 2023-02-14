package org.ethereumphone.walletmanager.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.ethereumphone.walletmanager.models.TransactionList
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

private const val BASE_URL = "http://localhost/"
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface HistoricTransactionsApiService {
    @GET
    suspend fun getHistoricTransactions(@Url url: String): TransactionList
}


// Singleton
object HistoricTransactionsApi {
    val retrofitService: HistoricTransactionsApiService by lazy {
        retrofit.create(HistoricTransactionsApiService::class.java)
    }
}