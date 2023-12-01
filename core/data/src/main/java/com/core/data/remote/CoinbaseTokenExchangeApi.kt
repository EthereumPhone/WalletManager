package com.core.data.remote

import com.core.data.model.dto.ExchangeJsonResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinbaseTokenExchangeApi {
    @GET("/v2/prices/{pair}")
    fun getExchangeRate(@Path("pair") pair: String): ExchangeJsonResponse
}