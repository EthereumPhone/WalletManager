package org.ethereumphone.walletmanager.core.data.remote.dto.request

data class TokenBalanceRequestBody(
    val id: Int = 1,
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenBalances",
    val params: List<String>
)