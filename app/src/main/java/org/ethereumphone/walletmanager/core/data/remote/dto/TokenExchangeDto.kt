package org.ethereumphone.walletmanager.core.data.remote.dto

import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenExchangeEntity

data class TokenExchangeDto(
    val symbol: String,
    val price: String
) {
    fun toTokenExchangeEntity() : TokenExchangeEntity {
        return TokenExchangeEntity(
            symbol,
            price.toDouble()
        )
    }
}