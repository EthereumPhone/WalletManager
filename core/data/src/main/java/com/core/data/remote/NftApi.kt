package com.core.data.remote

import com.core.data.model.dto.NftResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Retrofit API interface for Alchemy NFT endpoints
 * Uses the NFT API v3: https://docs.alchemy.com/reference/getnftsforowner-v3
 */
interface NftApi {
    
    /**
     * Get all NFTs owned by an address
     * 
     * @param url The full Alchemy NFT API URL (e.g., https://eth-mainnet.g.alchemy.com/nft/v3/{apiKey}/getNFTsForOwner)
     * @param owner The wallet address to get NFTs for
     * @param withMetadata Include NFT metadata in response
     * @param pageSize Number of NFTs to return per page (max 100)
     * @param pageKey Pagination key for fetching next page
     * @param excludeFilters Filters to exclude spam NFTs
     */
    @GET
    suspend fun getNFTsForOwner(
        @Url url: String,
        @Query("owner") owner: String,
        @Query("withMetadata") withMetadata: Boolean = true,
        @Query("pageSize") pageSize: Int = 100,
        @Query("pageKey") pageKey: String? = null,
        @Query("excludeFilters[]") excludeFilters: List<String> = listOf("SPAM", "AIRDROPS")
    ): NftResponse
}
