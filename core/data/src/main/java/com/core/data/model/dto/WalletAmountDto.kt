package com.core.data.model.dto

data class WalletAmountDto(
    var ethAmount: Double = Double.MAX_VALUE,
    var fiatAmount: Double = Double.MAX_VALUE,
)