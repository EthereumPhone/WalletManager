package com.core.data.model.dto

import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenMetadataEntity

data class TokenMetadataJsonResponse(
    val jsonrpc: String,
    val id: Int,
    val result: TokenMetadataDto
)


data class TokenMetadataDto(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logo: String
)

fun TokenMetadataDto.asEntity(
    contractAddress: String,
    chainId: Int
): TokenMetadataEntity {
    return TokenMetadataEntity(
        contractAddress = contractAddress,
        name = name,
        decimals = decimals,
        symbol = symbol,
        chainId = chainId,
        logo = logo
    )
}