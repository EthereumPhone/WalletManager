package org.ethereumphone.walletmanager.core.data.remote.dto

import org.ethereumphone.walletmanager.core.domain.model.TokenExchange

data class TokenExchangeDto(
    val symbol: String,
    val price: String
)