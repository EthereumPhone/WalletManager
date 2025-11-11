package com.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.core.ui.UiTestTags
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

        composeRule.onNodeWithTag(UiTestTags.SWAP_BUTTON, useUnmergedTree = true)
            .assertIsDisplayed()
    }
}


