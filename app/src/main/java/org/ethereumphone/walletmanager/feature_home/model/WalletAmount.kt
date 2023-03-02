package org.ethereumphone.walletmanager.feature_home.model

data class WalletAmount(
    var ethAmount: Double = Double.MAX_VALUE,
    var fiatAmount: Double = Double.MAX_VALUE,
)
