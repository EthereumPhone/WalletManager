package com.core.data.model.requestBody

import com.core.database.model.erc20.TokenBalanceEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenBalanceRequestBody(
    val id: Int = 1,
    val jsonrpc: String = "2.0",
    val method: String = "alchemy_getTokenBalances",
    val params: List<Any>
) {
    companion object {
        fun allErc20Tokens(toAddress: String, pageKey: String? = null): TokenBalanceRequestBody {
            // alchemy_getTokenBalances returns at most 100 balances per page. When more exist,
            // the response carries a pageKey that must be passed back as an options object to
            // retrieve the next page: [address, "erc20", { "pageKey": ... }].
            val params = if (pageKey == null) {
                listOf(toAddress, "erc20")
            } else {
                listOf(toAddress, "erc20", mapOf("pageKey" to pageKey))
            }
            return TokenBalanceRequestBody(params = params)
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