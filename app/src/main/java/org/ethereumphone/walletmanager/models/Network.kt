package org.ethereumphone.walletmanager.models

import java.io.Serializable

data class Network(
    val chainId: Int,
    val chainRPC: String,
    val chainCurrency: String,
    val chainName: String,
    val chainExplorer: String
) : Serializable
