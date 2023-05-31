package org.ethereumphone.walletmanager.core.domain.model

data class TokenMetadata(
    val chainId: Int,
    val address: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logoURI: String
)