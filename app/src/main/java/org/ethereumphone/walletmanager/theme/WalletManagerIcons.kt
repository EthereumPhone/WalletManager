package org.ethereumphone.walletmanager.theme

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.VerticalAlignBottom
import androidx.compose.ui.graphics.vector.ImageVector

object WalletManagerIcons {
    val Asset = Icons.Filled.AccountBalanceWallet
    val Home = Icons.Filled.Home
    val Swap = Icons.Rounded.SwapHoriz
    val Send = Icons.Rounded.NorthEast
    val Buy = Icons.Default.CreditCard
    val Receive = Icons.Rounded.VerticalAlignBottom
}

sealed class Icon {
    data class ImageVectorIcon(val imageVector: ImageVector): Icon()
    data class DrawableResourceIcon(@DrawableRes val id: Int): Icon()
}