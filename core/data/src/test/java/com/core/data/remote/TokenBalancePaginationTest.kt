package com.core.data.remote

import com.core.data.model.dto.TokenBalanceJsonResponse
import com.core.data.model.requestBody.TokenBalanceRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for alchemy_getTokenBalances pagination support.
 *
 * The endpoint returns at most 100 balances per page plus a `pageKey` when more remain.
 * Before this fix only the first page was read, so wallets with >100 ERC20 tokens lost the
 * rest. These tests pin the request wire-format (the options object that carries the pageKey)
 * and that the response model parses `pageKey`.
 */
class TokenBalancePaginationTest {

    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val address = "0x31145208a6b6323f71fd66e63191a8933385c4be"

    @Test
    fun firstPageRequest_hasNoPageKeyOption() {
        val body = TokenBalanceRequestBody.allErc20Tokens(address)
        assertEquals(listOf(address, "erc20"), body.params)
    }

    @Test
    fun nextPageRequest_carriesPageKeyAsOptionsObject() {
        val body = TokenBalanceRequestBody.allErc20Tokens(address, pageKey = "0xabc123")
        assertEquals(listOf(address, "erc20", mapOf("pageKey" to "0xabc123")), body.params)
    }

    /**
     * The pageKey option must serialize as a nested JSON object, matching what Alchemy expects:
     * params = [address, "erc20", { "pageKey": ... }]. This guards the Moshi Any/Map handling.
     */
    @Test
    fun nextPageRequest_serializesToExpectedJson() {
        val body = TokenBalanceRequestBody.allErc20Tokens(address, pageKey = "0xabc123")
        val json = moshi.adapter(TokenBalanceRequestBody::class.java).toJson(body)

        assertEquals(
            """{"id":1,"jsonrpc":"2.0","method":"alchemy_getTokenBalances",""" +
                """"params":["$address","erc20",{"pageKey":"0xabc123"}]}""",
            json
        )
    }

    @Test
    fun response_parsesPageKeyWhenPresent() {
        val json = """
            {"id":1,"jsonrpc":"2.0","result":{"address":"$address",
            "tokenBalances":[{"contractAddress":"0x833589fcd6edb6e08f4c7c32d4f71b54bda02913",
            "tokenBalance":"0x9cc9ab"}],"pageKey":"0xnextpage"}}
        """.trimIndent()

        val parsed = moshi.adapter(TokenBalanceJsonResponse::class.java).fromJson(json)!!
        assertEquals("0xnextpage", parsed.result.pageKey)
        assertEquals(1, parsed.result.tokenBalances.size)
    }

    @Test
    fun response_pageKeyIsNullOnLastPage() {
        val json = """
            {"id":1,"jsonrpc":"2.0","result":{"address":"$address","tokenBalances":[]}}
        """.trimIndent()

        val parsed = moshi.adapter(TokenBalanceJsonResponse::class.java).fromJson(json)!!
        assertNull(parsed.result.pageKey)
    }
}
