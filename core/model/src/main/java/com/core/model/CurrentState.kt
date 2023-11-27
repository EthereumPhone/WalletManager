package com.core.model

data class CurrentState(
    var address: String,
    var symbol: String,
    var name: String,
    var balance: Double,
    var assets: List<TokenAsset>
)
