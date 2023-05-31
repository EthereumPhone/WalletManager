package org.ethereumphone.walletmanager.core.domain.model

data class NetworkWalletData(
    val chainId: Int,
    val address: String,
    val networkBalance: Double
)