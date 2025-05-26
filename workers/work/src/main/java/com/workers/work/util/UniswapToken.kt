package com.workers.work.util

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class UniswapToken(
    val chainId: Int,
    val symbol: String,
    val name: String,
    val address: String,
    val decimals: Int,
    val logoURI: String? = null,
    val tags: List<String>? = null,
    val extensions: Extensions? = null
)

@Serializable
data class Extensions(
    val bridgeInfo: Map<String, BridgeData>? = null
)

@Serializable
data class BridgeData(
    val tokenAddress: String? = null,
    val originBridgeAddress: String? = null
)


@Serializable
data class TokenList(
    val name: String,
    val timestamp: String,
    val version: Version,
    val tags: Map<String, String> = emptyMap(),
    val logoURI: String,
    val keywords: List<String>,
    val uniswapTokens: List<UniswapToken>
)

@Serializable
data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int
)
