package com.core.data.remote

import com.core.data.model.dto.TokenBalanceDto
import com.core.data.model.dto.TokenBalanceJsonResponse
import com.core.data.model.requestBody.TokenBalanceRequestBody
import com.core.database.model.erc20.TokenBalanceEntity
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

interface TokenBalanceApi {
    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST
    suspend fun getTokenBalances(
        @Url url: String,
        @Body requestBody: TokenBalanceRequestBody
    ): List<TokenBalanceDto>
}