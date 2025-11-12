package com.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.core.ui.R
import com.feature.home.GroupedAssetsUiState
import com.feature.home.HomeScreen2
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class HomeScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    /**
    Check if the swap button is visible on the home screen
     */
    @Test
    fun homeScreen_showsSwapButton() {
        composeRule.setContent {
            HomeScreen2(
                groupedAssetsUiState = GroupedAssetsUiState.Loading,
                navigateToSwap = {},
                navigateToSend = {},
                navigateToLog = {},
                navigateToReceive = {},
                navigateToPayMaster = {},
                isOffline = false,
                hasTransfer = false
            )
        }

        val cdSwap = composeRule.activity.getString(R.string.cd_swap)
        composeRule.onNodeWithContentDescription(cdSwap).assertIsDisplayed()
    }


    /**
    Check if the offline is visible on the home screen
     */
    @Test
    fun homeScreen_showsOffline() {
        composeRule.setContent {
            HomeScreen2(
                groupedAssetsUiState = GroupedAssetsUiState.Loading,
                navigateToSwap = {},
                navigateToSend = {},
                navigateToLog = {},
                navigateToReceive = {},
                navigateToPayMaster = {},
                isOffline = true,
                hasTransfer = false
            )
        }

        val cdGlobe = composeRule.activity.getString(R.string.cd_wireframe_globe)
        composeRule.onNodeWithContentDescription(cdGlobe).assertIsDisplayed()

        val cdOfflineText = composeRule.activity.getString(R.string.offline_connect_internet)
        composeRule.onNodeWithText(cdOfflineText).assertIsDisplayed()
    }


}


