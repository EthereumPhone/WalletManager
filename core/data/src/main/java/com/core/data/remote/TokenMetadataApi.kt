package com.core.data.remote

import com.core.data.model.dto.TokenMetadataDto
import com.core.data.model.requestBody.TokenMetadataRequestBody
import com.core.database.model.erc20.TokenMetadataEntity
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface TokenMetadataApi {
    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST("{network}.g.alchemy.com/v2/{apiKey}")
    suspend fun getTokenMetadata(
        @Path("network") network: String,
        @Path("apiKey") apiKey: String,
        @Body requestBody: TokenMetadataRequestBody
    ): List<TokenMetadataDto>

    companion object {
        const val BASE_URL = "https://"
    }
}