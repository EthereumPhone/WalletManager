package com.core.data.util

import com.core.data.BuildConfig

fun chainToApiKey(networkName: String): String = when(networkName) {
    "eth-mainnet" -> BuildConfig.ETHEREUM_API
    "eth-goerli" -> BuildConfig.GOERLI_API
    "opt-mainnet" -> BuildConfig.OPTIMISM_API
    "arb-mainnet" -> BuildConfig.ARBITRUM_API
    "polygon-mainnet" -> BuildConfig.POLYGON_API
    "base-mainnet" -> BuildConfig.BASE_API
    else -> ""
}

fun chainIdToName(chainId: Int): String = when(chainId) {
    1 -> "eth-mainnet"
    5 -> "eth-goerli"
    10 -> "opt-mainnet"
    42161 -> "arb-mainnet"
    137 -> "polygon-mainnet"
    8453 -> "base-mainnet"
    else -> ""
}

fun chainIdToRPC(chainId: Int): String {
    return "https://${chainIdToName(chainId)}.g.alchemy.com/v2/${chainToApiKey(chainIdToName(chainId))}"
}