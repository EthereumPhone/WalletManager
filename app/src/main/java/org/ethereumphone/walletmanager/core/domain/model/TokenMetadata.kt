package org.ethereumphone.walletmanager.core.domain.model

import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenMetadataEntity

data class TokenMetadata(
    val chainId: Int,
    val address: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logoURI: String
) {
    fun toTokenMetadataEntity(): TokenMetadataEntity {
        return TokenMetadataEntity(
            chainId = chainId,
            address = address,
            name = name,
            symbol = symbol,
            decimals = decimals,
            logoURI = logoURI
        )
    }
}