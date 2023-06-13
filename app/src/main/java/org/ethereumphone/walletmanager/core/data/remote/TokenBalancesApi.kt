package org.ethereumphone.walletmanager.core.data.remote

import org.ethereumphone.walletmanager.core.data.remote.dto.TokenBalanceJsonResponse
import org.ethereumphone.walletmanager.core.data.remote.dto.request.TokenBalanceRequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface TokenBalancesApi {

    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST("{network}.g.alchemy.com/v2/{apiKey}")
    suspend fun getTokenBalances(
        @Path("network") network: String,
        @Path("apiKey") apiKey: String,
        @Body requestBody: TokenBalanceRequestBody
    ): List<TokenBalanceJsonResponse>

    companion object {
        const val BASE_URL = "https://"
    }


}