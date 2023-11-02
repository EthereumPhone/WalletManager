package com.core.model

data class TokenMetadata(
    val contractAddress: String,
    val decimals: Int,
    val name: String,
    val symbol: String,
    val logo: String?,
    val chainId: Int,
    val swappable: Boolean = false
)