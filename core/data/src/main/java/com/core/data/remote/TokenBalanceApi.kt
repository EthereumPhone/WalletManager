package com.core.data.remote

import com.core.data.model.dto.TokenBalanceDto
import com.core.data.model.dto.TokenBalanceJsonResponse
import com.core.data.model.requestBody.TokenBalanceRequestBody
import com.core.database.model.erc20.TokenBalanceEntity
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface TokenBalanceApi {
    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST("{network}.g.alchemy.com/v2/{apiKey}")
    suspend fun getTokenBalances(
        @Path("network") network: String,
        @Path("apiKey") apiKey: String,
        @Body requestBody: TokenBalanceRequestBody
    ): List<TokenBalanceDto>

    companion object {
        const val BASE_URL = "https://"
    }
}