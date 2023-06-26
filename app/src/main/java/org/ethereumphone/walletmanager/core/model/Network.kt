package org.ethereumphone.walletmanager.core.model

import androidx.compose.ui.graphics.Color
import java.io.Serializable

data class Network(
    val chainId: Int,
    val chainRPC: String,
    val chainCurrency: String,
    val chainName: String,
    val chainExplorer: String,
    var color: Color = Color.Unspecified
) : Serializable

