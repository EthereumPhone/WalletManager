package com.core.data.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkTokenExchange(
    val symbol: String,
    val prices: List<PriceResponse>,
    val error: TokenPriceError? = null
)


@JsonClass(generateAdapter = true)
data class TokenPriceError(
    val message: String
)

@JsonClass(generateAdapter = true)
data class PriceResponse(
    val currency: String,
    val value: String,
    val lastUpdatedAt: String
)

@JsonClass(generateAdapter = true)
data class TokenPricesResponse(
    val data: List<TokenPriceInfo>,
    val error: TokenPriceError? = null
)

@JsonClass(generateAdapter = true)
data class TokenPriceInfo(
    val network: String,
    val address: String,
    val prices: List<PriceResponse>
)

@JsonClass(generateAdapter = true)
data class TokenPriceAddressesRequest(
    val addresses: List<TokenAddress>
)

@JsonClass(generateAdapter = true)
data class TokenAddress(
    val network: String,
    val address: String
)