package com.core.model



data class TokenData(
    val symbol: String?,
    val prices: List<Price>?
)

data class Price(
    val currency: String?,
    val value: String?,
    val lastUpdatedAt: String?
)
