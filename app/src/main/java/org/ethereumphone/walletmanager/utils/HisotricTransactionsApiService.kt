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

const val MAINNET = "https://api.etherscan.io/api?module=account&action=txlist&address=<replace>&startblock=0&endblock=99999999&page=1&offset=7&sort=desc&apikey=${BuildConfig.ETHSCAN_API}"
const val GOERLI = "https://api-goerli.etherscan.io/api?module=account&action=txlist&address=<replace>&startblock=0&endblock=99999999&page=1&offset=7&sort=desc&apikey=${BuildConfig.ETHSCAN_API}"
const val OPTIMISM = "https://api-optimistic.etherscan.io/api?module=account&action=txlist&address=$<replace>&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=${BuildConfig.OPTISCAN_API}"
const val POLYGON = "https://api.polygonscan.com/api?module=account&action=txlist&address=$<replace>&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=${BuildConfig.POLYSCAN_API}"
const val ARBITRUM = "https://api.arbiscan.io/api?module=account&action=txlistinternal&address=$<replace>&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=${BuildConfig.ARBISCAN_API}"

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