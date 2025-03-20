package com.core.model

import kotlinx.datetime.Instant

data class TokenExchange(
    val symbol: String,
    val currency: String,
    val value: Double,
    val timestamp: Instant
)
