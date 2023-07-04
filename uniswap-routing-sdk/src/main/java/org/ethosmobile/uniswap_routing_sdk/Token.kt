package org.ethosmobile.uniswap_routing_sdk

data class Token(
    val chainId: Int,
    val address: String,
    val decimals: Int,
    val symbol: String,
    val name: String
)
