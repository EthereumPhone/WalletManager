package com.core.data.remote

import com.core.data.model.dto.TokenMetadataDto
import com.core.data.model.dto.TokenMetadataJsonResponse
import com.core.data.model.requestBody.TokenMetadataRequestBody
import com.core.database.model.erc20.TokenMetadataEntity
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

interface TokenMetadataApi {
    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST
    suspend fun getTokenMetadata(
        @Url url: String,
        @Body requestBody: TokenMetadataRequestBody
    ): TokenMetadataJsonResponse
}