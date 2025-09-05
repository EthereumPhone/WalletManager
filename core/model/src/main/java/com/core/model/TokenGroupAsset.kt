package com.core.model





data class TokenGroupAssetOverview(
    val groupId: String,
    val symbol: String,
    val name: String,
    val logoUrl: String?,
    val totalBalance: Double,
    val formattedBalance: String,
    val totalFiatBalance: Double? = 0.0,
    val formattedFiatBalance: String? = "0.00",
    val exchangeCurrency: String? = "usd",
)


data class TokenGroupAsset(
    val groupId: String,
    val symbol: String,
    val name: String,
    val totalBalance: Double,
    val formattedBalance: String,
    val activeChains: Int,
    val totalChains: Int,
    val logoUrl: String?,
    val tokens: List<TokenAsset>,
    val chainIds: List<Int>,
    val activeChainIds: List<Int>,
)

/**
 * External model with price information for UI layer consumption.
 */
data class TokenGroupAssetWithExchange(
    val groupId: String,
    val symbol: String,
    val name: String,
    val totalBalance: Double,
    val formattedBalance: String,
    val totalBalanceUsd: Double,
    val formattedBalanceUsd: String,
    val activeChains: Int,
    val totalChains: Int,
    val logoUrl: String?,
    val tokens: List<TokenAssetWithPrice>,
    val chainIds: List<Int>,
    val activeChainIds: List<Int>,
    val exchangeRate: Double,
    val exchangeCurrency: String
)




