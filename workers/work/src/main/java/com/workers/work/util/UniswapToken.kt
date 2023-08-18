package com.workers.work.util

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UniswapToken(
    val chainId: Int,
    val address: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logoURI: String,
    val extensions: Extensions? = null
)

data class Extensions(
    val bridgeInfo: Map<String, BridgeInfo>
)

data class BridgeInfo(
    val tokenAddress: String
)