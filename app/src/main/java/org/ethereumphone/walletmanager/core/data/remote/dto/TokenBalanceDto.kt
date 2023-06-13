package org.ethereumphone.walletmanager.core.data.remote.dto

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