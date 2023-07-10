package com.core.data.remote

import com.core.data.model.dto.TransferDto
import com.core.data.model.requestBody.NetworkTransferRequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

interface TransfersApi {
    @Headers(
        "accept: application/json",
        "Content-Type: application/json"
    )
    @POST
    suspend fun getTransfers(
        @Url url: String,
        @Body requestBody: NetworkTransferRequestBody
    ): List<TransferDto>
}