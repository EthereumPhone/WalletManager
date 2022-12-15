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
    SEND(
        icon = Icon.ImageVectorIcon(WalletManagerIcons.Send),
        iconTextId = R.string.send_screen,
        titleTextId = R.string.send_screen
    ),
    RECEIVE(
        icon = Icon.ImageVectorIcon(WalletManagerIcons.Receive),
        iconTextId = R.string.receive_action,
        titleTextId = R.string.receive_action
    )
}

