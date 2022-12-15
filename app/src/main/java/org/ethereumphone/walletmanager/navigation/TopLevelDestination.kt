package org.ethereumphone.walletmanager.navigation

import org.ethereumphone.walletmanager.R
import org.ethereumphone.walletmanager.theme.Icon
import org.ethereumphone.walletmanager.theme.WalletManagerIcons


enum class TopLevelDestination(
    val icon: Icon,
    val iconTextId: Int,
    val titleTextId: Int
    ) {
    HOME(
        icon = Icon.ImageVectorIcon(WalletManagerIcons.Home),
        iconTextId = R.string.home_screen,
        titleTextId = R.string.home_screen
    ),
    ASSET(
        icon = Icon.ImageVectorIcon(WalletManagerIcons.Asset),
        iconTextId = R.string.asset_screen,
        titleTextId = R.string.asset_screen
    ),
    SWAP(
        icon = Icon.ImageVectorIcon(WalletManagerIcons.Swap),
        iconTextId = R.string.swap_screen,
        titleTextId = R.string.swap_screen
    )
}
