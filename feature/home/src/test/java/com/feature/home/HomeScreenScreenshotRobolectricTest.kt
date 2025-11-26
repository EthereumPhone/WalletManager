package com.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    qualifiers = "w480dp-h480dp-port-hdpi"
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenScreenshotRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun snapshot_home_default_robo() {
        val widthPx = 720
        val heightPx = 720
        val dpi = 240
        val density = dpi / 160f

        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = density, fontScale = 1f)) {
                Box(
                    Modifier
                        .size((widthPx / density).dp, (heightPx / density).dp)
                        .semantics { testTag = "screenshot" }
                ) {
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
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("screenshot").captureRoboImage(filePath = "screenshot/snapshot_home_default_robo.png")
    }
}


