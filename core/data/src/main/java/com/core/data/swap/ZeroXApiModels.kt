package com.core.data.swap

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from 0x Swap API /swap/allowance-holder/quote endpoint
 * Documentation: https://0x.org/docs/api
 */
@JsonClass(generateAdapter = true)
data class ZeroXSwapQuoteResponse(
    @Json(name = "transaction") val transaction: ZeroXTransaction,
    @Json(name = "buyAmount") val buyAmount: String,
    @Json(name = "sellAmount") val sellAmount: String,
    @Json(name = "minBuyAmount") val minBuyAmount: String? = null,
    @Json(name = "buyToken") val buyToken: String,
    @Json(name = "sellToken") val sellToken: String,
    @Json(name = "allowanceTarget") val allowanceTarget: String,
    @Json(name = "blockNumber") val blockNumber: String,
    @Json(name = "price") val price: String? = null,
    @Json(name = "guaranteedPrice") val guaranteedPrice: String? = null,
    @Json(name = "estimatedPriceImpact") val estimatedPriceImpact: String? = null,
    @Json(name = "liquidityAvailable") val liquidityAvailable: Boolean? = null,
    @Json(name = "route") val route: ZeroXRoute? = null,
    @Json(name = "issues") val issues: ZeroXIssues? = null,
    @Json(name = "fees") val fees: ZeroXFees? = null,
    @Json(name = "tokenMetadata") val tokenMetadata: ZeroXTokenMetadata? = null,
    @Json(name = "totalNetworkFee") val totalNetworkFee: String? = null,
    @Json(name = "zid") val zid: String? = null,
    @Json(name = "decodedUniqueId") val decodedUniqueId: String? = null,
    @Json(name = "gas") val gas: String? = null,
    @Json(name = "gasPrice") val gasPrice: String? = null,
    @Json(name = "estimatedGas") val estimatedGas: String? = null
) {
    @Transient
    var allowanceTransaction: ZeroXTransaction? = null
}

@JsonClass(generateAdapter = true)
data class ZeroXTransaction(
    @Json(name = "to") val to: String,
    @Json(name = "data") val data: String,
    @Json(name = "value") val value: String,
    @Json(name = "gas") val gas: String? = null,
    @Json(name = "gasPrice") val gasPrice: String? = null,
    @Json(name = "from") val from: String? = null
)

@JsonClass(generateAdapter = true)
data class ZeroXRoute(
    @Json(name = "fills") val fills: List<ZeroXFill> = emptyList(),
    @Json(name = "tokens") val tokens: List<ZeroXRouteToken> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ZeroXFill(
    @Json(name = "from") val from: String,
    @Json(name = "to") val to: String,
    @Json(name = "source") val source: String,
    @Json(name = "proportionBps") val proportionBps: String
)

@JsonClass(generateAdapter = true)
data class ZeroXRouteToken(
    @Json(name = "address") val address: String,
    @Json(name = "symbol") val symbol: String? = null
)

@JsonClass(generateAdapter = true)
data class ZeroXIssues(
    @Json(name = "balance") val balance: ZeroXBalanceIssue? = null,
    @Json(name = "allowance") val allowance: ZeroXAllowanceIssue? = null,
    @Json(name = "simulationIncomplete") val simulationIncomplete: Boolean? = null,
    @Json(name = "invalidSourcesPassed") val invalidSourcesPassed: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ZeroXBalanceIssue(
    @Json(name = "token") val token: String,
    @Json(name = "expected") val expected: String,
    @Json(name = "actual") val actual: String
)

@JsonClass(generateAdapter = true)
data class ZeroXAllowanceIssue(
    @Json(name = "spender") val spender: String,
    @Json(name = "token") val token: String? = null,
    @Json(name = "expected") val expected: String? = null,
    @Json(name = "actual") val actual: String
)

@JsonClass(generateAdapter = true)
data class ZeroXFees(
    @Json(name = "integratorFee") val integratorFee: ZeroXFeeDetails? = null,
    @Json(name = "zeroExFee") val zeroExFee: ZeroXFeeDetails? = null,
    @Json(name = "gasFee") val gasFee: ZeroXFeeDetails? = null
)

@JsonClass(generateAdapter = true)
data class ZeroXFeeDetails(
    @Json(name = "amount") val amount: String,
    @Json(name = "token") val token: String,
    @Json(name = "type") val type: String
)

@JsonClass(generateAdapter = true)
data class ZeroXTokenMetadata(
    @Json(name = "buyToken") val buyToken: ZeroXTokenTaxInfo,
    @Json(name = "sellToken") val sellToken: ZeroXTokenTaxInfo
)

@JsonClass(generateAdapter = true)
data class ZeroXTokenTaxInfo(
    @Json(name = "buyTaxBps") val buyTaxBps: String,
    @Json(name = "sellTaxBps") val sellTaxBps: String
)


