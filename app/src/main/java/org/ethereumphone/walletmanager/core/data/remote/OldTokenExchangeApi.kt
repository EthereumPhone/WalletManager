package org.ethereumphone.walletmanager.core.data.remote

import org.ethereumphone.walletmanager.core.data.remote.dto.TokenExchangeDto
import retrofit2.http.GET
import retrofit2.http.Query


interface OldExchangeApi {
    @GET("/api/v3/ticker/price")
    suspend fun getExchange(
        @Query("symbol") symbol: String
    ): TokenExchangeDto

    @GET("/api/v3/ticker/price")
    suspend fun getAllExchanges(): List<TokenExchangeDto>


    companion object {
        const val BASE_URL = "https://data.binance.com"
    }
}