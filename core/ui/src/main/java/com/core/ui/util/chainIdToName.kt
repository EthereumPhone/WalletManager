package com.core.ui.util

fun chainIdToName(name: String): String = when(name) {
    "1" -> "Mainnet"
    "5" -> "Goerli"
    "137" -> "Polygon" // Polygon
    "10" -> "Optimism" // Optimum
    "42161" -> "Arbitrum" // Arbitrum
    "8453" -> "Base" // Base
    else -> {
        "N/A"
    }
}