package com.feature.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenScreenshotRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun snapshot_home_default_robo() {
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

        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(filePath = "screenshot/snapshot_home_default_robo.png")
    }
}


