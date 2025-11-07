package com.core.data.remote

import androidx.tracing.trace
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

private interface SponsorshipApiService {
    @GET("/api/token-prices")
    suspend fun getPrices(
        @Query("addresses") addresses: List<String>
    ): SponsorshipPricesResponse
}

private const val DEFAULT_SPONSORSHIP_BASE_URL = "https://api.markushaas.com/"

@Singleton
class RetrofitSponsorshipPrice @Inject constructor(
    okHttpClient: OkHttpClient,
    moshi: Moshi
): SponsorshipPriceDataSource {
    private val api = trace("getTokenPricesSponsorshipApi") {
        Retrofit.Builder()
            .baseUrl(DEFAULT_SPONSORSHIP_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SponsorshipApiService::class.java)
    }

    override suspend fun fetchPricesByAddresses(addresses: List<String>): SponsorshipPricesResponse =
        api.getPrices(addresses)
}


