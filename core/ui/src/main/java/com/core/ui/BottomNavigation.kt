package com.core.ui

import android.graphics.drawable.Icon
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.DensityMedium
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector


data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)

@Composable
fun WmBottomNavigation(
    navController: NavHostController,
) {

    val list = listOf(
        BottomNavigationItem(
            title = "Overview",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            hasNews = false,
        ),
        BottomNavigationItem(
            title = "Assets",
            selectedIcon = Icons.Filled.AttachMoney,
            unselectedIcon = Icons.Outlined.AttachMoney,
            hasNews = false,
        ),
        BottomNavigationItem(
            title = "Transaction",
            selectedIcon = Icons.Filled.DensityMedium,
            unselectedIcon = Icons.Outlined.DensityMedium,
            hasNews = false,
        )
    )

    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }
    NavigationBar {
        list.forEachIndexed{ index, item ->

            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = { /*TODO*/ },
                icon = {
                    Icon(
                        imageVector = if (index == selectedItemIndex) {
                            item.selectedIcon
                        } else item.unselectedIcon,
                        contentDescription = item.title
                    )

                    /*
                    BadgedBox(
                                                   badge = {
                                                       if(item.badgeCount != null) {
                                                           Badge {
                                                               Text(text = item.badgeCount.toString())
                                                           }
                                                       } else if(item.hasNews) {
                                                           Badge()
                                                       }
                                                   }
                                               ) {
                                                   Icon(
                                                       imageVector = if (index == selectedItemIndex) {
                                                           item.selectedIcon
                                                       } else item.unselectedIcon,
                                                       contentDescription = item.title
                                                   )
                                               }
                     */
                }
            )
        }
    }
}


