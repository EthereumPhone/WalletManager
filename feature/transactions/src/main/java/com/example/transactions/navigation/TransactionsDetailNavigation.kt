package com.example.transactions.navigation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.transactions.TransactionRoute
import com.example.transactions.TransctionDetailRoute
import java.net.URLDecoder
import java.net.URLEncoder


private val URL_CHARACTER_ENCODING = Charsets.UTF_8.name()

@VisibleForTesting
internal const val txArg = "tx"

const val transactionDetailRoute = "transactiondetail_route"


internal class TxArgs(val tx: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(URLDecoder.decode(checkNotNull(savedStateHandle[txArg]), URL_CHARACTER_ENCODING))
}

fun NavController.navigateToTransactionDetail(tx: String) {
    val encodedtx = URLEncoder.encode(tx, URL_CHARACTER_ENCODING)
    this.navigate("$transactionDetailRoute/$encodedtx") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.transactionDetailScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = "$transactionDetailRoute/{$txArg}",
        arguments = listOf(
            navArgument(txArg) { type = NavType.StringType },
        ),
    ) {
        TransctionDetailRoute(
            navigateToTransaction = onBackClick
        )
    }
}