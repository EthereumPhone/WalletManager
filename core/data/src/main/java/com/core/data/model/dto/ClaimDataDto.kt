package com.core.data.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from the claim-data API containing chain and token information
 */
@JsonClass(generateAdapter = true)
data class ClaimDataResponse(
    @Json(name = "chains") val chains: List<ClaimChain>
)

@JsonClass(generateAdapter = true)
data class ClaimChain(
    @Json(name = "name") val name: String,
    @Json(name = "chainId") val chainId: Int,
    @Json(name = "epoch") val epoch: Int,
    @Json(name = "rpcUrl") val rpcUrl: String,
    @Json(name = "bundlerRpcUrl") val bundlerRpcUrl: String,
    @Json(name = "alchemyApiKey") val alchemyApiKey: String,
    @Json(name = "distributorAddress") val distributorAddress: String,
    @Json(name = "tokens") val tokens: List<ClaimToken>
)

@JsonClass(generateAdapter = true)
data class ClaimToken(
    @Json(name = "name") val name: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "address") val address: String,
    @Json(name = "decimals") val decimals: Int,
    @Json(name = "claimable") val claimable: String
)

