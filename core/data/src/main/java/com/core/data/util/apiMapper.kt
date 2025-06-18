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
    5 -> "eth-goerli"
    else -> ""
}

fun chainIdToRPC(chainId: Int): String {
    return "https://${chainIdToName(chainId)}.g.alchemy.com/v2/${chainToApiKey(chainIdToName(chainId))}"
}

fun chainIdToBundler(chainId: Int): String {
    return "https://api.pimlico.io/v2/$chainId/rpc?apikey=${BuildConfig.BUNDLER_API}"
}