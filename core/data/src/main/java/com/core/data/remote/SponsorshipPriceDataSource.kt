package com.core.data.remote

import com.squareup.moshi.JsonClass

interface SponsorshipPriceDataSource {
    suspend fun fetchPricesByAddresses(addresses: List<String>): SponsorshipPricesResponse
}

@JsonClass(generateAdapter = true)
data class SponsorshipPricesResponse(
    val data: List<SponsorshipTokenPrice>
)

@JsonClass(generateAdapter = true)
data class SponsorshipTokenPrice(
    val address: String,
    val symbol: String?,
    val price_usd: Double?
)


