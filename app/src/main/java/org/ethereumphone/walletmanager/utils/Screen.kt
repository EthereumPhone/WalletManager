package org.ethereumphone.walletmanager.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Dataset
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.assets.navigation.assetRoute
import com.example.transactions.navigation.transactionRoute
import com.feature.home.navigation.homeRoute

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen(homeRoute, "Overview", Icons.Rounded.Home)
    object Assets : Screen(assetRoute, "Assets", Icons.Rounded.Dataset)
    object Transaction : Screen(transactionRoute, "Transactions", Icons.Rounded.Article)
}