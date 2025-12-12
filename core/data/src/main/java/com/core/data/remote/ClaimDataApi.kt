package com.core.data.remote

import com.core.data.model.dto.ClaimDataResponse
import retrofit2.http.GET

/**
 * API interface for fetching claim data containing token metadata
 */
interface ClaimDataApi {
    
    @GET("https://api.markushaas.com/api/claim-data")
    suspend fun getClaimData(): ClaimDataResponse
}

