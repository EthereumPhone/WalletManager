package com.core.model

data class TokenAsset(
    val address: String,
    val chainId: Int,
    val symbol: String,
    val name: String,
    val balance: Double,
    val decimals: Int = 0,
    val logoUrl: String? = "",
    val swappable: Boolean = false
)
