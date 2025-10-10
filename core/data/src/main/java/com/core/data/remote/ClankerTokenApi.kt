package com.core.data.remote

import androidx.tracing.trace
import com.core.data.model.ClankerDataSource
import com.core.data.model.dto.ClankerToken
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

private interface ClankerTokenApi {


    @GET("https://www.clanker.world/api/tokens")
    suspend fun getClankerTokens(
        @Query("q") q: String? = null, // symbol, name or token address
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null
    ): ClankerNetworkResponse<List<ClankerToken>>
}



@Serializable
data class ClankerNetworkResponse<T>(
    val data: T,
    val total: Int
)


@Singleton
class RetrofitClankerTokenApi @Inject constructor(
    json: Json,
    okHttpClientFactory: dagger.Lazy<Call.Factory>
): ClankerDataSource {

    private val networkApi = trace("getAppsDasNetwork") {
        Retrofit.Builder()
            .baseUrl("http://localhost/") // Using firebase functions, so cant use base url
            //avoid main thread initialisation trick
            .callFactory { okHttpClientFactory.get().newCall(it) }
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ClankerTokenApi::class.java)
    }

    override suspend fun getClankerTokens(
        query: String,
        limit: Int,
        pageIndex: Int,
        startAfter: String
    ): List<ClankerToken> = networkApi.getClankerTokens(query, limit , "").data



}