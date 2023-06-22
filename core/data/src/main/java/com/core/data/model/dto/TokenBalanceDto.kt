package com.core.data.model.dto

import com.core.database.model.erc20.TokenBalanceEntity

data class TokenBalanceJsonResponse(
    val id: Int,
    val jsonrpc: String,
    val result: Result
) {
    data class Result(
        val address: String,
        val tokenBalances: List<TokenBalanceDto>
    )

}

data class TokenBalanceDto(
    val contractAddress: String,
    val tokenBalance: String // Balance is hex
)

fun TokenBalanceDto.asEntity(chainId: Int): TokenBalanceEntity {
    return TokenBalanceEntity(
        contractAddress = contractAddress,
        chainId = chainId,
        tokenBalance = tokenBalance.toInt()

    )
}