package org.ethereumphone.walletmanager.core.data.remote

import org.ethereumphone.walletmanager.core.data.remote.dto.TransferDto
import org.ethereumphone.walletmanager.core.data.remote.dto.request.NetworkTransferRequestBody
import org.ethereumphone.walletmanager.core.data.remote.dto.TransferJsonResponse
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