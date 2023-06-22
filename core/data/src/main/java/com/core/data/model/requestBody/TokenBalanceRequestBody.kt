package com.core.data.model.requestBody

import com.core.database.model.erc20.TokenBalanceEntity

data class TokenBalanceRequestBody(
    val id: Int = 1,
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenBalances",
    val params: List<Any>
) {
    companion object {
        fun allErc20Tokens(toAddress: String): TokenBalanceRequestBody {
            return TokenBalanceRequestBody(
                params = listOf(
                    toAddress,
                    "erc20"
                )
            )
        }

        fun forContract(toAddress: String, contractAddress: List<String>): TokenBalanceRequestBody {
            return TokenBalanceRequestBody(
                params = listOf(
                    toAddress,
                    contractAddress
                )
            )
        }
    }
}