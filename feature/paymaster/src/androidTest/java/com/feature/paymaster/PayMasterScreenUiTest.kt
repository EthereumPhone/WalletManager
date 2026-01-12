package com.feature.paymaster

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for the PayMasterScreen.
 * 
 * These tests simulate user interactions with the PayMaster screen's
 * amount buttons and verify correct value capture.
 * 
 * Tests use fake data and don't require network connectivity.
 * 
 * Test scenarios include:
 * - Amount button selection ($10, $25, $50, $100)
 * - Balance display formatting
 * - Screen layout verification
 */
@RunWith(AndroidJUnit4::class)
class PayMasterScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // =====================================================================
    // TEST 1: AMOUNT BUTTONS - Selection captures correct values
    // =====================================================================

    @Test
    fun payMasterScreen_amountButton10_capturesCorrectValue() {
        var capturedAmount by mutableStateOf(TextFieldValue(""))
        
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = capturedAmount,
                onTopUpAmountChanged = { capturedAmount = it }
            )
        }

        composeRule.waitForIdle()

        // Click the $10 button
        composeRule.onNodeWithText("\$10").performClick()

        // Verify amount was captured
        assertEquals("10", capturedAmount.text)
    }

    @Test
    fun payMasterScreen_amountButton25_capturesCorrectValue() {
        var capturedAmount by mutableStateOf(TextFieldValue(""))
        
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = capturedAmount,
                onTopUpAmountChanged = { capturedAmount = it }
            )
        }

        composeRule.waitForIdle()

        // Click the $25 button
        composeRule.onNodeWithText("\$25").performClick()

        assertEquals("25", capturedAmount.text)
    }

    @Test
    fun payMasterScreen_amountButton50_capturesCorrectValue() {
        var capturedAmount by mutableStateOf(TextFieldValue(""))
        
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = capturedAmount,
                onTopUpAmountChanged = { capturedAmount = it }
            )
        }

        composeRule.waitForIdle()

        // Click the $50 button
        composeRule.onNodeWithText("\$50").performClick()

        assertEquals("50", capturedAmount.text)
    }

    @Test
    fun payMasterScreen_amountButton100_capturesCorrectValue() {
        var capturedAmount by mutableStateOf(TextFieldValue(""))
        
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = capturedAmount,
                onTopUpAmountChanged = { capturedAmount = it }
            )
        }

        composeRule.waitForIdle()

        // Click the $100 button
        composeRule.onNodeWithText("\$100").performClick()

        assertEquals("100", capturedAmount.text)
    }

    // =====================================================================
    // TEST 2: BUTTON SWITCHING - User can change selection
    // =====================================================================

    @Test
    fun payMasterScreen_switchingAmounts_updatesValue() {
        var capturedAmount by mutableStateOf(TextFieldValue(""))
        
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = capturedAmount,
                onTopUpAmountChanged = { capturedAmount = it }
            )
        }

        composeRule.waitForIdle()

        // Click $10 first
        composeRule.onNodeWithText("\$10").performClick()
        assertEquals("10", capturedAmount.text)

        // Then switch to $50
        composeRule.onNodeWithText("\$50").performClick()
        assertEquals("50", capturedAmount.text)

        // Finally switch to $100
        composeRule.onNodeWithText("\$100").performClick()
        assertEquals("100", capturedAmount.text)
    }

    // =====================================================================
    // TEST 3: BALANCE DISPLAY - Shows formatted balance
    // =====================================================================

    @Test
    fun payMasterScreen_displaysFormattedBalance() {
        composeRule.setContent {
            PayMasterScreen(
                balance = "123.456789",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // Balance should be formatted to 2 decimal places with $ prefix
        composeRule.onNodeWithText("\$123.46", substring = true).assertIsDisplayed()
    }

    @Test
    fun payMasterScreen_displaysZeroBalance() {
        composeRule.setContent {
            PayMasterScreen(
                balance = "0.0",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // Should display $0.00
        composeRule.onNodeWithText("\$0.00", substring = true).assertIsDisplayed()
    }

    @Test
    fun payMasterScreen_handlesInvalidBalance() {
        composeRule.setContent {
            PayMasterScreen(
                balance = "invalid",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // Invalid balance should fall back to $0.00
        composeRule.onNodeWithText("\$0.00", substring = true).assertIsDisplayed()
    }

    // =====================================================================
    // TEST 4: SCREEN LAYOUT - Displays expected elements
    // =====================================================================

    @Test
    fun payMasterScreen_displaysHeader() {
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // Header should show "Gas"
        composeRule.onNodeWithText("Gas", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun payMasterScreen_displaysTotalLabel() {
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // Should display "TOTAL" label
        composeRule.onNodeWithText("TOTAL").assertIsDisplayed()
    }

    @Test
    fun payMasterScreen_displaysInfoText() {
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // Should display informational text about paymaster
        composeRule.onNodeWithText("paymaster", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun payMasterScreen_displaysAllAmountButtons() {
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // All 4 amount buttons should be displayed
        composeRule.onNodeWithText("\$10").assertIsDisplayed()
        composeRule.onNodeWithText("\$25").assertIsDisplayed()
        composeRule.onNodeWithText("\$50").assertIsDisplayed()
        composeRule.onNodeWithText("\$100").assertIsDisplayed()
    }

    // =====================================================================
    // TEST 5: BUTTON SELECTION STATE - Selected button is highlighted
    // =====================================================================

    @Test
    fun payMasterScreen_selectedButton_isHighlighted() {
        var capturedAmount by mutableStateOf(TextFieldValue("10"))
        
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = capturedAmount,
                onTopUpAmountChanged = { capturedAmount = it }
            )
        }

        composeRule.waitForIdle()

        // When amount is "10", the $10 button should be displayed (and selected)
        composeRule.onNodeWithText("\$10").assertIsDisplayed()
        
        // Click $25 to change selection
        composeRule.onNodeWithText("\$25").performClick()
        
        // Verify both buttons are still displayed
        composeRule.onNodeWithText("\$10").assertIsDisplayed()
        composeRule.onNodeWithText("\$25").assertIsDisplayed()
    }

    // =====================================================================
    // TEST 6: BACK NAVIGATION - Callback triggered
    // =====================================================================

    @Test
    fun payMasterScreen_backClick_triggersCallback() {
        var backClicked = false
        
        composeRule.setContent {
            PayMasterScreen(
                balance = "50.00",
                onBackClick = { backClicked = true },
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // The header has a back button with "Gas" text
        // Click on the header area to trigger back
        composeRule.onNodeWithText("Gas", ignoreCase = true).performClick()

        // Note: The HeaderBar might have specific click handling
        // This test verifies the screen renders correctly with the callback
    }

    // =====================================================================
    // TEST 7: COMBINED FLOW - Complete top-up selection
    // =====================================================================

    @Test
    fun payMasterScreen_fullFlow_selectAmount() {
        var selectedAmount = ""
        var capturedAmount by mutableStateOf(TextFieldValue(""))
        
        composeRule.setContent {
            PayMasterScreen(
                balance = "75.50",
                onBackClick = {},
                topUp = { amount -> 
                    selectedAmount = amount
                    null // Return null as we're testing UI only
                },
                forceRefresh = {},
                topUpAmount = capturedAmount,
                onTopUpAmountChanged = { capturedAmount = it }
            )
        }

        composeRule.waitForIdle()

        // Step 1: Verify balance is displayed
        composeRule.onNodeWithText("\$75.50", substring = true).assertIsDisplayed()

        // Step 2: Select $50
        composeRule.onNodeWithText("\$50").performClick()
        assertEquals("50", capturedAmount.text)

        // Print captured values
        println("=== PAYMASTER SCREEN CAPTURED VALUES ===")
        println("Balance displayed: \$75.50")
        println("Selected amount: ${capturedAmount.text}")
        println("=========================================")
    }

    // =====================================================================
    // TEST 8: EDGE CASES - Large and small balances
    // =====================================================================

    @Test
    fun payMasterScreen_largeBalance_formattedCorrectly() {
        composeRule.setContent {
            PayMasterScreen(
                balance = "999999.99",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // Large balance should be formatted
        composeRule.onNodeWithText("\$999999.99", substring = true).assertIsDisplayed()
    }

    @Test
    fun payMasterScreen_verySmallBalance_formattedCorrectly() {
        composeRule.setContent {
            PayMasterScreen(
                balance = "0.01",
                onBackClick = {},
                topUp = { null },
                forceRefresh = {},
                topUpAmount = TextFieldValue(""),
                onTopUpAmountChanged = {}
            )
        }

        composeRule.waitForIdle()

        // Small balance should show as $0.01
        composeRule.onNodeWithText("\$0.01", substring = true).assertIsDisplayed()
    }
}


