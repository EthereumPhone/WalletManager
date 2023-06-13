package org.ethereumphone.walletmanager.core.domain.model

data class WalletData(
    val chainId: Int,
    val name: String, // alchemy api network name
    val rpc: String,
    val address: String,
    val networkBalance: Double
)