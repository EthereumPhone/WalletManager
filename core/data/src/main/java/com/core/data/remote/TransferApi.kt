package com.core.data.remote

import com.core.data.model.dto.TransferDto
import com.core.data.model.requestBody.NetworkTransferRequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface TransfersApi {
    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST("{network}.g.alchemy.com/v2/{apiKey}")
    suspend fun getTransfers(
        @Path("network") network: String,
        @Path("apiKey") apiKey: String,
        @Body requestBody: NetworkTransferRequestBody
    ): List<TransferDto>

    companion object {
        const val BASE_URL = "https://"
    }
}