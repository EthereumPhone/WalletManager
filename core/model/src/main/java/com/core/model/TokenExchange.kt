package com.core.model

import kotlinx.datetime.Instant

data class TokenExchange(
    val address: String? = "",
    val symbol: String,
    val chainId: Int? = -1,
    val currency: String,
    val value: Double, // this value always relates to 1 unit of the token. FI: 1 eth = x usd
    val timestamp: Instant
)