package com.feature.swap

import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.core.model.SwapToken
import com.core.model.SwapUIState
import com.core.model.TokenAsset
import com.core.ui.util.dgenBurgendy
import com.core.ui.util.dgenRed
import com.feature.swap.ui.SwapInterface
import com.feature.swap.ui.SwapTransactionStatus
import com.feature.swap.ui.SwapTransactionStatusOverlay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for the SwapScreen.
 * 
 * These tests simulate user interactions with the Swap screen's textfields
 * and verify that the correct values are passed to the callbacks.
 * 
 * Tests use fake data to ensure fast and efficient execution without network calls.
 * 
 * Test scenarios include:
 * - Amount input in FROM field
 * - MAX button click functionality
 * - Token selector display
 * - TO field read-only behavior
 * - Transaction status overlay states
 */
@RunWith(AndroidJUnit4::class)
class SwapScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // Test fixture: ETH asset on Base with 2.5 balance
    private val ethToken = SwapToken(
        token = TokenAsset(
            address = "1",
            chainId = 8453,
            symbol = "ETH",
            name = "Ethereum",
            balance = 2.5,
            decimals = 18,
            logoUrl = null,
            swappable = true
        ),
        balance = "2.5",
        fiatBalance = "7500.00",
        formattedMaxAmount = "2.5",
        formattedMaxFiatAmount = "7500.00"
    )

    // Test fixture: USDC asset on Base
    private val usdcToken = SwapToken(
        token = TokenAsset(
            address = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
            chainId = 8453,
            symbol = "USDC",
            name = "USD Coin",
            balance = 1000.0,
            decimals = 6,
            logoUrl = null,
            swappable = true
        ),
        balance = "1000.0",
        fiatBalance = "1000.00",
        formattedMaxAmount = "1000.0",
        formattedMaxFiatAmount = "1000.00"
    )


    // =====================================================================
    // TEST 1: FROM TEXTFIELD INPUT - Amount capture in swap FROM field
    // =====================================================================

    @Test
    fun swapInterface_fromTextField_capturesAmountInput() {
        var capturedAmount = ""
        var capturedIsFiat = false
        
        var uiState by mutableStateOf(
            SwapUIState(
                fromToken = ethToken,
                fromOnAmountChange = { amount, isFiat ->
                    capturedAmount = amount
                    capturedIsFiat = isFiat
                }
            )
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Click on the FROM amount placeholder and enter amount
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("1.25")

        // Verify the amount was captured correctly
        assertEquals("1.25", capturedAmount)
        assertFalse("Should not be fiat mode", capturedIsFiat)
    }

    @Test
    fun swapInterface_fromTextField_rejectsInvalidCharacters() {
        var capturedAmount = ""
        
        val uiState = SwapUIState(
            fromToken = ethToken,
            fromOnAmountChange = { amount, _ -> capturedAmount = amount }
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Try to enter invalid characters (letters)
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("abc")

        // The input should be empty or unchanged since letters are filtered
        assertTrue("Invalid characters should be rejected", capturedAmount.isEmpty() || !capturedAmount.contains("abc"))
    }

    @Test
    fun swapInterface_fromTextField_acceptsDecimalNumbers() {
        var capturedAmount = ""
        
        var uiState by mutableStateOf(
            SwapUIState(
                fromToken = ethToken,
                fromOnAmountChange = { amount, _ -> capturedAmount = amount }
            )
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Enter a valid decimal number
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("0.123456")

        assertEquals("0.123456", capturedAmount)
    }

    // =====================================================================
    // TEST 2: MAX BUTTON - Click sets max amount
    // =====================================================================

    @Test
    fun swapInterface_maxButton_setsMaxAmount() {
        var maxClicked = false
        var capturedAmount = ""
        
        val uiState = SwapUIState(
            fromToken = ethToken,
            fromOnAmountChange = { amount, _ -> capturedAmount = amount },
            fromOnMaxClick = { maxClicked = true }
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Click the MAX button (contains "MAX 2.5")
        composeRule.onNodeWithText("MAX 2.5", substring = true).performClick()

        // Verify MAX was clicked
        assertTrue("MAX button should trigger callback", maxClicked)
        // Verify the max amount was set
        assertEquals("2.5", capturedAmount)
    }

    @Test
    fun swapInterface_fromTitle_clickAlsoTriggersMax() {
        var maxClicked = false
        var capturedAmount = ""
        
        val uiState = SwapUIState(
            fromToken = ethToken,
            fromTitle = "FROM",
            fromOnAmountChange = { amount, _ -> capturedAmount = amount },
            fromOnMaxClick = { maxClicked = true }
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Click the FROM title (should also trigger MAX)
        composeRule.onNodeWithText("FROM").performClick()

        assertTrue("Clicking FROM title should trigger max", maxClicked)
        assertEquals("2.5", capturedAmount)
    }

    // =====================================================================
    // TEST 3: TOKEN SELECTOR - Click triggers token selection
    // =====================================================================

    @Test
    fun swapInterface_fromTokenClick_triggersCallback() {
        var tokenClicked = false
        
        val uiState = SwapUIState(
            fromToken = ethToken,
            fromOnTokenClick = { tokenClicked = true }
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Click on the token symbol to open selector
        composeRule.onNodeWithText("ETH").performClick()

        assertTrue("Token click should trigger callback", tokenClicked)
    }

    @Test
    fun swapInterface_toTokenClick_triggersCallback() {
        var tokenClicked = false
        
        val uiState = SwapUIState(
            fromToken = ethToken,
            toToken = usdcToken,
            toOnTokenClick = { tokenClicked = true }
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Click on the TO token symbol
        composeRule.onNodeWithText("USDC").performClick()

        assertTrue("TO token click should trigger callback", tokenClicked)
    }

    // =====================================================================
    // TEST 4: TO FIELD - Should be read-only by default
    // =====================================================================

    @Test
    fun swapInterface_toField_displaysQuoteAmount() {
        val uiState = SwapUIState(
            fromToken = ethToken,
            fromCurrentAmount = "1.0",
            toToken = usdcToken,
            toCurrentAmount = "2500.00",
            toReadOnly = true
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Verify the TO amount is displayed
        composeRule.onNodeWithText("2500.00").assertIsDisplayed()
    }

    @Test
    fun swapInterface_displaysFromAndToTitles() {
        val uiState = SwapUIState(
            fromToken = ethToken,
            fromTitle = "FROM",
            toToken = usdcToken,
            toTitle = "TO"
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Verify both titles are displayed
        composeRule.onNodeWithText("FROM").assertIsDisplayed()
        composeRule.onNodeWithText("TO").assertIsDisplayed()
    }

    // =====================================================================
    // TEST 5: TOKEN DISPLAY - Shows correct token info
    // =====================================================================

    @Test
    fun swapInterface_displaysTokenSymbols() {
        val uiState = SwapUIState(
            fromToken = ethToken,
            toToken = usdcToken
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Verify both token symbols are displayed
        composeRule.onNodeWithText("ETH").assertIsDisplayed()
        composeRule.onNodeWithText("USDC").assertIsDisplayed()
    }

    @Test
    fun swapInterface_noToken_showsPlaceholder() {
        val uiState = SwapUIState(
            fromToken = null,
            toToken = null
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Verify placeholders are shown (0.0 for amounts)
        composeRule.onNodeWithText("0.0").assertIsDisplayed()
    }

    // =====================================================================
    // TEST 6: TRANSACTION STATUS OVERLAY - Different states
    // =====================================================================

    @Test
    fun swapTransactionOverlay_pending_showsPendingText() {
        composeRule.setContent {


            SwapTransactionStatusOverlay(
                status = SwapTransactionStatus.PENDING,
                gifLoader = null,
                onDismiss = {},
                primaryColor = dgenRed,
                secondaryColor = dgenBurgendy
            )
        }

        composeRule.waitForIdle()

        // Should display pending text
        composeRule.onNodeWithText("SWAP PENDING...", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun swapTransactionOverlay_success_showsSuccessText() {
        composeRule.setContent {
            SwapTransactionStatusOverlay(
                status = SwapTransactionStatus.SUCCESS,
                gifLoader = null,
                onDismiss = {},
                primaryColor = dgenRed,
                secondaryColor = dgenBurgendy
            )
        }

        composeRule.waitForIdle()

        // Should display success text
        composeRule.onNodeWithText("SWAP COMPLETE", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun swapTransactionOverlay_failure_showsErrorMessage() {
        val errorMessage = "Insufficient balance"

        composeRule.setContent {
            SwapTransactionStatusOverlay(
                status = SwapTransactionStatus.FAILURE(errorMessage),
                gifLoader = null,
                onDismiss = {},
                primaryColor = dgenRed,
                secondaryColor = dgenBurgendy
            )
        }

        composeRule.waitForIdle()

        // Should display the error message
        composeRule.onNodeWithText(errorMessage.uppercase(), substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun swapTransactionOverlay_null_notDisplayed() {
        composeRule.setContent {
            SwapTransactionStatusOverlay(
                status = null,
                gifLoader = null,
                onDismiss = {},
                primaryColor = dgenRed,
                secondaryColor = dgenBurgendy
            )
        }

        composeRule.waitForIdle()

        // Nothing should be displayed when status is null
        // We verify by checking common overlay texts don't exist
        composeRule.onNode(hasText("SWAP", substring = true, ignoreCase = true))
            .assertDoesNotExist()
    }

    // =====================================================================
    // TEST 7: COMBINED FLOW - Full swap input capture
    // =====================================================================

    @Test
    fun swapInterface_fullFlow_capturesAllInputs() {
        var fromAmount = ""
        var fromMaxClicked = false
        var fromTokenClicked = false
        var toTokenClicked = false
        
        var uiState by mutableStateOf(
            SwapUIState(
                fromToken = ethToken,
                toToken = usdcToken,
                fromOnAmountChange = { amount, _ -> fromAmount = amount },
                fromOnMaxClick = { fromMaxClicked = true },
                fromOnTokenClick = { fromTokenClicked = true },
                toOnTokenClick = { toTokenClicked = true }
            )
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Step 1: Click MAX to set amount
        composeRule.onNodeWithText("MAX 2.5", substring = true).performClick()
        assertTrue("MAX should be clicked", fromMaxClicked)
        assertEquals("2.5", fromAmount)

        // Step 2: Click on FROM token selector
        composeRule.onNodeWithText("ETH").performClick()
        assertTrue("FROM token should be clicked", fromTokenClicked)

        // Step 3: Click on TO token selector
        composeRule.onNodeWithText("USDC").performClick()
        assertTrue("TO token should be clicked", toTokenClicked)

        // Print captured values for verification
        println("=== SWAP INTERFACE CAPTURED VALUES ===")
        println("FROM Amount: $fromAmount")
        println("MAX Clicked: $fromMaxClicked")
        println("FROM Token Clicked: $fromTokenClicked")
        println("TO Token Clicked: $toTokenClicked")
        println("FROM Token: ${ethToken.token.symbol} on chain ${ethToken.token.chainId}")
        println("TO Token: ${usdcToken.token.symbol} on chain ${usdcToken.token.chainId}")
        println("=======================================")
    }

    // =====================================================================
    // TEST 8: EDGE CASES - Amount validation
    // =====================================================================

    @Test
    fun swapInterface_verySmallAmount_accepted() {
        var capturedAmount = ""
        
        var uiState by mutableStateOf(
            SwapUIState(
                fromToken = ethToken,
                fromOnAmountChange = { amount, _ -> capturedAmount = amount }
            )
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Enter a very small amount (wei-level)
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("0.000000001")

        assertEquals("0.000000001", capturedAmount)
    }

    @Test
    fun swapInterface_zeroAmount_accepted() {
        var capturedAmount = ""
        
        var uiState by mutableStateOf(
            SwapUIState(
                fromToken = ethToken,
                fromOnAmountChange = { amount, _ -> capturedAmount = amount }
            )
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Enter zero
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("0")

        assertEquals("0", capturedAmount)
    }

    @Test
    fun swapInterface_leadingDecimal_accepted() {
        var capturedAmount = ""
        
        var uiState by mutableStateOf(
            SwapUIState(
                fromToken = ethToken,
                fromOnAmountChange = { amount, _ -> capturedAmount = amount }
            )
        )

        composeRule.setContent {
            SwapInterface(uiState = uiState)
        }

        composeRule.waitForIdle()

        // Enter leading decimal
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput(".5")

        assertEquals(".5", capturedAmount)
    }
}


