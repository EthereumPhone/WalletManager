package org.ethereumphone.walletmanager.core.model

import androidx.compose.ui.graphics.Color
import org.ethereumphone.walletmanager.theme.goerliNetwork
import org.ethereumphone.walletmanager.theme.mainNetwork
import org.ethereumphone.walletmanager.theme.optimismNetwork
import org.ethereumphone.walletmanager.theme.polygonNetwork

enum class NetworkStyle(
    val color: Color,
    val networkName: String
) {
    MAIN(
        color = mainNetwork,
        networkName = "Ethereum Mainnet"
    ),
    GOERLI(
        color = goerliNetwork,
        networkName = "Goerli Testnet"
    ),
    ARBITRUM(
        color = Color.White,
        networkName = "Arbitrum Network"
    ),
    OPTIMISM(
        color = optimismNetwork,
        networkName = "Optimism Network"
    ),
    POLYGON(
        color = polygonNetwork,
        networkName = "Polygon Network"
    )
}