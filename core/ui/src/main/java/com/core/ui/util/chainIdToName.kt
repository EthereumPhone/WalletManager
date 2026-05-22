package com.core.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun chainIdToName(name: String): String = when(name) {
    "1" -> "Mainnet"
    "11155111" -> "Sepola"
    "137" -> "Polygon" // Polygon
    "10" -> "Optimism" // Optimum
    "42161" -> "Arbitrum" // Arbitrum
    "8453" -> "Base" // Base
    "7777777" -> "Zora"
    "56" -> "BNB Chain"
    "43114" -> "Avalanche"
    "143" -> "Monad"
    "33139" -> "ApeChain"
    else -> {
        "N/A"
    }
}


