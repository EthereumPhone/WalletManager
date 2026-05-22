package com.core.data.util

import com.core.data.BuildConfig
//TODO: Redundant but too lazy to refactor everything right now
fun chainToApiKey(networkName: String): String = BuildConfig.ALCHEMY_API

fun chainIdToName(chainId: Int): String = when(chainId) {
    1 -> "eth-mainnet"
    11155111 -> "eth-sepolia"
    10 -> "opt-mainnet"
    42161 -> "arb-mainnet"
    137 -> "polygon-mainnet"
    8453 -> "base-mainnet"
    7777777 -> "zora-mainnet"
    5 -> "eth-goerli"
    56 -> "bnb-mainnet"
    43114 -> "avax-mainnet"
    143 -> "monad-mainnet"
    33139 -> "apechain-mainnet"
    else -> ""
}

fun chainIdToRPC(chainId: Int): String {
    val networkName = chainIdToName(chainId)
    require(networkName.isNotBlank()) { "Unsupported chainId: $chainId" }
    return "https://${networkName}.g.alchemy.com/v2/${chainToApiKey(networkName)}"
}

fun chainIdToBundler(chainId: Int): String {
    return "https://api.pimlico.io/v2/$chainId/rpc?apikey=${BuildConfig.BUNDLER_API}"
}