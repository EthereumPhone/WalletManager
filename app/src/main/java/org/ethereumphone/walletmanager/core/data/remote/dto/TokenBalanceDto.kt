package org.ethereumphone.walletmanager.core.data.remote.dto

data class TokenBalanceDto(
    val id: Int,
    val jsonrpc: String,
    val result: Result
) {
    data class Result(
        val address: String,
        val tokenBalances: List<TokenBalance>
    )
    data class TokenBalance(
        val contractAddress: String,
        val tokenBalance: String // Balance is hex
    )
}