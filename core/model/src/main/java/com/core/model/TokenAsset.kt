package com.core.model
import kotlinx.serialization.Serializable

@Serializable
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


data class TokenAssetWithPrice(
    val address: String,
    val chainId: Int,
    val symbol: String,
    val name: String,
    val balance: Double,
    val decimals: Int = 0,
    val logoUrl: String? = "",
    val swappable: Boolean = false,
    val fiatAmount: Double = 0.0
)
