package com.core.model

import java.math.BigDecimal

data class TokenBalance(
    val contractAddress: String,
    val chainId: Int,
    val tokenBalance: BigDecimal,
)