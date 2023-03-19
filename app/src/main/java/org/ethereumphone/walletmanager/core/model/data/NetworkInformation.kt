package org.ethereumphone.walletmanager.core.model.data

data class NetworkInformation(
    val chainID: Int,
    val chainName: String,
    val networkBalance: Float,
    val fiatBalance: Float
)
