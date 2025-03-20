package com.core.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class NetworkTokenExchange(
    val symbol: String,
    val prices: List<PriceResponse>,
    val error: String?
)

@Serializable
data class PriceResponse(
    val currency: String,
    val value: String,
    val lastUpdatedAt: String
)