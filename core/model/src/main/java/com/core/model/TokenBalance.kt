package com.core.model

data class TokenBalance(
    val contractAddress: String,
    val chainId: Int,
    val tokenBalance: Double,
)