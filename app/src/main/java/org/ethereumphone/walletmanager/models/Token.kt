package org.ethereumphone.walletmanager.models

data class Token(
    val address: String,
    val longName: String,
    val ticker: String,
    val network: Network
)
