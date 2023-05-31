package org.ethereumphone.walletmanager.core.domain.model

data class ERCToken internal constructor(
    val symbol: String,
    val price: Double,
    val chainId: Int,
    val address: String,
    val name: String,
    val decimals: Int,
    val logoURI: String
) {
    constructor(exchange: TokenExchange, metadata: TokenMetadata): this(
        symbol = metadata.symbol,
        price = exchange.price,
        chainId = metadata.chainId,
        address = metadata.address,
        name = metadata.name,
        decimals = metadata.decimals,
        logoURI = metadata.logoURI
    )
}

fun List<TokenMetadata>.mapToErcTokens(exchange: TokenExchange): List<ERCToken> {
    return map { ERCToken(exchange = exchange, metadata = it) }
}

