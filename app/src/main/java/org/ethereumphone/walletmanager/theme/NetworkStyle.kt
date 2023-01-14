package org.ethereumphone.walletmanager.theme

import androidx.compose.ui.graphics.Color

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