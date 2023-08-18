package org.ethereumphone.walletmanager.core.model

import org.ethereumphone.walletmanager.core.model.Network

data class Token(
    val address: String,
    val longName: String,
    val ticker: String,
    val network: Network
)
