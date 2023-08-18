package com.feature.send.ui

data class MockNetworkData(
    var name: String,
    var chainId: Int,
    var ethAmount: Double = Double.MAX_VALUE,
    var fiatAmount: Double = Double.MAX_VALUE,
)