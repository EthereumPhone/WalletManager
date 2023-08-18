package com.core.data.util

import com.core.data.BuildConfig

fun chainToApiKey(networkName: String): String = when(networkName) {
    "eth-mainnet" -> BuildConfig.ETHEREUM_API
    "eth-goerli" -> BuildConfig.GOERLI_API
    "opt-mainnet" -> BuildConfig.OPTIMISM_API
    "arb-mainnet" -> BuildConfig.ARBITRUM_API
    "polygon-mainnet" -> BuildConfig.POLYGON_API
    else -> ""
}