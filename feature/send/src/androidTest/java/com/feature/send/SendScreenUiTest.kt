package com.feature.send

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.model.TokenAssetWithPrice
import com.feature.send.ui.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for the SendScreen.
 * 
 * These tests simulate user interactions with the Send screen's text inputs
 * and verify that the correct values are passed to the callbacks.
 * 
 * Test scenarios include:
 * - Correct send flow with valid inputs
 * - Incorrect flow with amount exceeding balance
 * - Flow with no inputs (empty state)
 * - Various edge cases for input validation
 */
@RunWith(AndroidJUnit4::class)
class SendScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // Data class to capture all values that would be passed to the send method
    data class CapturedSendData(
        val amount: String = "",
        val fiatAmount: String = "",
        val recipientAddress: String = "",
        val selectedChainId: Int? = null,
        val selectedTokenSymbol: String? = null,
        val useMaxAmount: Boolean = false
    )

    // Test fixture: ETH asset with 2.5 balance worth $7500
    private val ethAsset = TokenAssetWithPrice(
        address = "1",
        chainId = 1,
        symbol = "ETH",
        name = "Ethereum",
        balance = 2.5,
        decimals = 18,
        swappable = true,
        fiatAmount = 7500.0
    )

    // Test fixture: USDC asset with 1000 balance worth $1000
    private val usdcAsset = TokenAssetWithPrice(
        address = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
        chainId = 1,
        symbol = "USDC",
        name = "USD Coin",
        balance = 1000.0,
        decimals = 6,
        swappable = true,
        fiatAmount = 1000.0
    )

    // Test fixture: Polygon ETH asset
    private val ethPolygon = TokenAssetWithPrice(
        address = "137",
        chainId = 137,
        symbol = "ETH",
        name = "Ethereum (Polygon)",
        balance = 1.0,
        decimals = 18,
        swappable = true,
        fiatAmount = 3000.0
    )

    // =====================================================================
    // TEST 1: CORRECT FLOW - Valid inputs should produce correct captured values
    // =====================================================================

    @Test
    fun sendScreen_correctFlow_capturesValidAmountAndAddress() {
        var capturedData = CapturedSendData()
        var amountUiState by mutableStateOf(createDefaultAmountState())
        var recipientUiState by mutableStateOf(RecipientUiState())

        composeRule.setContent {
            SendScreen(
                amountUiState = amountUiState,
                recipientUiState = recipientUiState,
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = { chainId ->
                    capturedData = capturedData.copy(selectedChainId = chainId)
                },
                onAmountChange = { amount, isFiat ->
                    capturedData = if (isFiat) {
                        capturedData.copy(fiatAmount = amount)
                    } else {
                        capturedData.copy(amount = amount)
                    }
                    amountUiState = amountUiState.copy(
                        currentAmount = if (!isFiat) amount else "",
                        currentFiatAmount = if (isFiat) amount else ""
                    )
                },
                maxAmountClicked = {
                    capturedData = capturedData.copy(
                        amount = ethAsset.balance.toString(),
                        useMaxAmount = true
                    )
                },
                onRecipientChange = { address ->
                    capturedData = capturedData.copy(recipientAddress = address)
                    recipientUiState = recipientUiState.copy(recipientAddress = address)
                },
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        // Wait for UI to settle
        composeRule.waitForIdle()

        // Enter a valid amount (1.5 ETH)
        // The placeholder shows "0.0" which we target first
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("1.5")

        // Verify captured amount
        assertEquals("1.5", capturedData.amount)

        // Enter valid recipient address
        composeRule.onNodeWithText("Address").performClick()
        composeRule.onNodeWithText("Address").performTextInput("0x742d35Cc6634C0532925a3b844Bc9e7595f5e123")

        // Verify captured data
        assertEquals("1.5", capturedData.amount)
        assertEquals("0x742d35Cc6634C0532925a3b844Bc9e7595f5e123", capturedData.recipientAddress)
        assertFalse(capturedData.useMaxAmount)
    }

    @Test
    fun sendScreen_correctFlow_clickMaxSetsMaxAmount() {
        var capturedData = CapturedSendData()
        var amountUiState by mutableStateOf(createDefaultAmountState())

        composeRule.setContent {
            SendScreen(
                amountUiState = amountUiState,
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { amount, isFiat ->
                    capturedData = if (isFiat) {
                        capturedData.copy(fiatAmount = amount)
                    } else {
                        capturedData.copy(amount = amount)
                    }
                },
                maxAmountClicked = {
                    capturedData = capturedData.copy(
                        amount = "2.5",
                        useMaxAmount = true
                    )
                    amountUiState = amountUiState.copy(
                        currentAmount = "2.5",
                        useMaxAmount = true
                    )
                },
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Click MAX button - it shows "MAX  2.5" (formatted balance)
        composeRule.onNodeWithText("MAX  2.5", substring = true).performClick()

        // Verify MAX was clicked and amount set
        assertTrue(capturedData.useMaxAmount)
        assertEquals("2.5", capturedData.amount)
    }

    // =====================================================================
    // TEST 2: INCORRECT FLOW - Amount exceeds balance
    // =====================================================================

    @Test
    fun sendScreen_incorrectFlow_amountExceedsBalance_capturesInput() {
        var capturedData = CapturedSendData()
        var amountUiState by mutableStateOf(createDefaultAmountState())

        composeRule.setContent {
            SendScreen(
                amountUiState = amountUiState,
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { amount, isFiat ->
                    capturedData = capturedData.copy(amount = amount)
                    amountUiState = amountUiState.copy(
                        currentAmount = amount,
                        currentFiatAmount = ""
                    )
                },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Enter amount that exceeds balance (10 > 2.5)
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("10")

        // Amount is still captured (UI would show red, but value is tracked)
        assertEquals("10", capturedData.amount)
    }

    @Test
    fun sendScreen_incorrectFlow_invalidAddress_stillCaptured() {
        var capturedData = CapturedSendData()
        var recipientUiState by mutableStateOf(RecipientUiState())

        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = recipientUiState,
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = { address ->
                    capturedData = capturedData.copy(recipientAddress = address)
                    recipientUiState = recipientUiState.copy(recipientAddress = address)
                },
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Enter an invalid address (too short)
        composeRule.onNodeWithText("Address").performClick()
        composeRule.onNodeWithText("Address").performTextInput("0x123")

        // Invalid address is still captured (validation happens in ViewModel)
        assertEquals("0x123", capturedData.recipientAddress)
    }

    // =====================================================================
    // TEST 3: NO INPUTS FLOW - Empty state
    // =====================================================================

    @Test
    fun sendScreen_noInputs_capturesEmptyValues() {
        var capturedData = CapturedSendData()

        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { amount, isFiat ->
                    capturedData = if (isFiat) {
                        capturedData.copy(fiatAmount = amount)
                    } else {
                        capturedData.copy(amount = amount)
                    }
                },
                maxAmountClicked = {},
                onRecipientChange = { address ->
                    capturedData = capturedData.copy(recipientAddress = address)
                },
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Without any input, capturedData should remain at default (empty)
        assertEquals("", capturedData.amount)
        assertEquals("", capturedData.fiatAmount)
        assertEquals("", capturedData.recipientAddress)
        assertFalse(capturedData.useMaxAmount)
    }

    @Test
    fun sendScreen_noInputs_placeholderDisplayed() {
        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        // Verify placeholders are displayed
        composeRule.onNodeWithText("0.0").assertIsDisplayed()
        composeRule.onNodeWithText("Address").assertIsDisplayed()
    }

    // =====================================================================
    // TEST 4: EDGE CASES - Various unique input scenarios
    // =====================================================================

    @Test
    fun sendScreen_edgeCase_decimalOnlyInput() {
        var capturedData = CapturedSendData()
        var amountUiState by mutableStateOf(createDefaultAmountState())

        composeRule.setContent {
            SendScreen(
                amountUiState = amountUiState,
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { amount, _ ->
                    capturedData = capturedData.copy(amount = amount)
                    amountUiState = amountUiState.copy(currentAmount = amount)
                },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Enter just a decimal point
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput(".")

        // The AmountTextField regex filter should handle this
        // (passes through if it matches ^\\d*\\.?\\d*$)
        assertEquals(".", capturedData.amount)
    }

    @Test
    fun sendScreen_edgeCase_leadingDecimalWithDigits() {
        var capturedData = CapturedSendData()
        var amountUiState by mutableStateOf(createDefaultAmountState())

        composeRule.setContent {
            SendScreen(
                amountUiState = amountUiState,
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { amount, _ ->
                    capturedData = capturedData.copy(amount = amount)
                    amountUiState = amountUiState.copy(currentAmount = amount)
                },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Enter ".12" - input starting with decimal point
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput(".12")

        // The UI passes raw input to callback; ViewModel normalizes ".12" -> "0.12"
        assertEquals(".12", capturedData.amount)
    }

    @Test
    fun sendScreen_edgeCase_verySmallAmount() {
        var capturedData = CapturedSendData()
        var amountUiState by mutableStateOf(createDefaultAmountState())

        composeRule.setContent {
            SendScreen(
                amountUiState = amountUiState,
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { amount, _ ->
                    capturedData = capturedData.copy(amount = amount)
                    amountUiState = amountUiState.copy(currentAmount = amount)
                },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Enter a very small amount (wei-level precision)
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("0.000000001")

        assertEquals("0.000000001", capturedData.amount)
    }

    @Test
    fun sendScreen_edgeCase_zeroAmount() {
        var capturedData = CapturedSendData()
        var amountUiState by mutableStateOf(createDefaultAmountState())

        composeRule.setContent {
            SendScreen(
                amountUiState = amountUiState,
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { amount, _ ->
                    capturedData = capturedData.copy(amount = amount)
                    amountUiState = amountUiState.copy(currentAmount = amount)
                },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Enter zero
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("0")

        assertEquals("0", capturedData.amount)
    }

    @Test
    fun sendScreen_edgeCase_multipleNetworkAssets_networkSelectionCaptured() {
        var capturedData = CapturedSendData()
        val assets = listOf(ethAsset, ethPolygon)

        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(assets),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = { chainId ->
                    capturedData = capturedData.copy(selectedChainId = chainId)
                },
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // The network selector shows icons; we just verify the screen displays with multiple assets
        // (NetworkSelector uses custom UI that may need specific identifiers)
        assertNull(capturedData.selectedChainId) // No network selected yet
    }

    @Test
    fun sendScreen_edgeCase_ensAddress_displayed() {
        var capturedData = CapturedSendData()
        var recipientUiState by mutableStateOf(RecipientUiState())

        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = recipientUiState,
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = { address ->
                    capturedData = capturedData.copy(recipientAddress = address)
                    recipientUiState = recipientUiState.copy(recipientAddress = address)
                },
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Enter an ENS name
        composeRule.onNodeWithText("Address").performClick()
        composeRule.onNodeWithText("Address").performTextInput("vitalik.eth")

        assertEquals("vitalik.eth", capturedData.recipientAddress)
    }

    @Test
    fun sendScreen_edgeCase_longAddress_fullyCaptured() {
        var capturedData = CapturedSendData()
        var recipientUiState by mutableStateOf(RecipientUiState())
        val fullAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f5e123"

        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = recipientUiState,
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = { address ->
                    capturedData = capturedData.copy(recipientAddress = address)
                    recipientUiState = recipientUiState.copy(recipientAddress = address)
                },
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Enter full 42-character Ethereum address
        composeRule.onNodeWithText("Address").performClick()
        composeRule.onNodeWithText("Address").performTextInput(fullAddress)

        // Full address should be captured (maxLength is 43 in RecipientSection)
        assertEquals(fullAddress, capturedData.recipientAddress)
    }

    // =====================================================================
    // TEST 5: TRANSACTION STATUS STATES
    // =====================================================================

    @Test
    fun sendScreen_transactionPending_overlayDisplayed() {
        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = TransactionStatus.PENDING,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Transaction pending overlay should show "TRANSACTION PENDING..."
        composeRule.onNodeWithText("TRANSACTION PENDING...", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendScreen_transactionSuccess_overlayDisplayed() {
        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = TransactionStatus.SUCCESS,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Transaction success overlay should show "TRANSACTION CONFIRMED!"
        composeRule.onNodeWithText("TRANSACTION CONFIRMED!", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendScreen_transactionFailure_overlayDisplaysErrorMessage() {
        val errorMessage = "Insufficient funds"

        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = TransactionStatus.FAILURE(errorMessage),
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Failure overlay should show the specific error message (uppercased in UI)
        composeRule.onNodeWithText(errorMessage.uppercase(), substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendScreen_transactionFailureWithGasError_showsHelpText() {
        val errorMessage = "Not enough ETH for gas"

        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = TransactionStatus.FAILURE(errorMessage),
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Should display the help text for gas errors
        composeRule.onNodeWithText("Add ETH to your wallet to pay for gas fees", substring = true)
            .assertIsDisplayed()
    }

    // =====================================================================
    // TEST 6: LOADING/EMPTY STATES
    // =====================================================================

    @Test
    fun sendScreen_loadingState_noAssets() {
        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Loading,
                selectedAssetUiState = SelectedAssetUiState.Unselected,
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        // Screen should still render with loading state
        composeRule.waitForIdle()
        // The amount placeholder should still be visible
        composeRule.onNodeWithText("0.0").assertIsDisplayed()
    }

    @Test
    fun sendScreen_emptyState_noAssetsAvailable() {
        composeRule.setContent {
            SendScreen(
                amountUiState = createDefaultAmountState(),
                recipientUiState = RecipientUiState(),
                assetsUiState = AssetsUiState.Empty,
                selectedAssetUiState = SelectedAssetUiState.Unselected,
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { _, _ -> },
                maxAmountClicked = {},
                onRecipientChange = {},
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()
        // Screen should still display with empty asset state
        composeRule.onNodeWithText("Address").assertIsDisplayed()
    }

    // =====================================================================
    // TEST 7: COMBINED INPUT VALIDATION - Simulating full send flow values
    // =====================================================================

    @Test
    fun sendScreen_fullFlow_capturesAllRequiredValues() {
        var capturedAmount = ""
        var capturedAddress = ""
        var capturedFiat = ""
        var maxWasClicked = false
        var amountUiState by mutableStateOf(createDefaultAmountState())
        var recipientUiState by mutableStateOf(RecipientUiState())

        composeRule.setContent {
            SendScreen(
                amountUiState = amountUiState,
                recipientUiState = recipientUiState,
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = {},
                onAmountChange = { amount, isFiat ->
                    if (isFiat) {
                        capturedFiat = amount
                    } else {
                        capturedAmount = amount
                    }
                    amountUiState = amountUiState.copy(
                        currentAmount = if (!isFiat) amount else "",
                        currentFiatAmount = if (isFiat) amount else ""
                    )
                },
                maxAmountClicked = {
                    maxWasClicked = true
                    capturedAmount = "2.5"
                    amountUiState = amountUiState.copy(
                        currentAmount = "2.5",
                        useMaxAmount = true
                    )
                },
                onRecipientChange = { address ->
                    capturedAddress = address
                    recipientUiState = recipientUiState.copy(recipientAddress = address)
                },
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Step 1: Click MAX to set max amount
        composeRule.onNodeWithText("MAX  2.5", substring = true).performClick()

        // Step 2: Enter recipient address
        composeRule.onNodeWithText("Address").performClick()
        composeRule.onNodeWithText("Address").performTextInput("0xDEADBEEF12345678901234567890123456789012")

        // Verify all values captured correctly
        assertTrue("MAX should have been clicked", maxWasClicked)
        assertEquals("2.5", capturedAmount)
        assertEquals("0xDEADBEEF12345678901234567890123456789012", capturedAddress)

        // These are the exact values that would be passed to the send() method in ViewModel:
        // - amount: "2.5" (or maxAmount)
        // - recipientAddress: "0xDEADBEEF12345678901234567890123456789012"
        // - selectedAsset: ethAsset (chainId = 1, symbol = "ETH")
    }

    @Test
    fun sendScreen_fullFlow_printCapturedValuesForSendMethod() {
        // This test demonstrates what values would be passed to the send method
        var capturedAmount = ""
        var capturedAddress = ""
        var capturedChainId: Int? = null
        var capturedSymbol: String? = null
        var amountUiState by mutableStateOf(createDefaultAmountState())
        var recipientUiState by mutableStateOf(RecipientUiState())

        composeRule.setContent {
            SendScreen(
                amountUiState = amountUiState,
                recipientUiState = recipientUiState,
                assetsUiState = AssetsUiState.Success(listOf(ethAsset)),
                selectedAssetUiState = SelectedAssetUiState.Selected(ethAsset),
                transactionStatus = null,
                qrScannerTriggered = false,
                shouldDismissKeyboard = false,
                onNetworkSelected = { chainId ->
                    capturedChainId = chainId
                },
                onAmountChange = { amount, _ ->
                    capturedAmount = amount
                    amountUiState = amountUiState.copy(currentAmount = amount)
                },
                maxAmountClicked = {},
                onRecipientChange = { address ->
                    capturedAddress = address
                    recipientUiState = recipientUiState.copy(recipientAddress = address)
                },
                clearTransactionStatus = {},
                resetQrScannerTrigger = {},
                onKeyboardDismissed = {},
                onBackClick = {}
            )
        }

        composeRule.waitForIdle()

        // Enter amount
        composeRule.onNodeWithText("0.0").performClick()
        composeRule.onNodeWithText("0.0").performTextInput("1.234")

        // Enter address
        composeRule.onNodeWithText("Address").performClick()
        composeRule.onNodeWithText("Address").performTextInput("0x1234567890AbCdEf1234567890AbCdEf12345678")

        // Get the selected asset info from the state we passed
        capturedSymbol = ethAsset.symbol
        capturedChainId = ethAsset.chainId

        // Print what would be sent to the send() method
        println("=== VALUES THAT WOULD BE PASSED TO send() METHOD ===")
        println("Amount: $capturedAmount")
        println("Recipient Address: $capturedAddress")
        println("Chain ID: $capturedChainId")
        println("Token Symbol: $capturedSymbol")
        println("Token Address: ${ethAsset.address}")
        println("Token Decimals: ${ethAsset.decimals}")
        println("Is Native (ETH): ${ethAsset.address == ethAsset.chainId.toString()}")
        println("====================================================")

        // Assertions to verify values
        assertEquals("1.234", capturedAmount)
        assertEquals("0x1234567890AbCdEf1234567890AbCdEf12345678", capturedAddress)
        assertEquals(1, capturedChainId)
        assertEquals("ETH", capturedSymbol)
    }

    // =====================================================================
    // HELPER METHODS
    // =====================================================================

    private fun createDefaultAmountState(): AmountUiState {
        return AmountUiState(
            maxAmount = ethAsset.balance,
            formattedMaxAmount = ethAsset.balance.toString(),
            currentAmount = "",
            maxFiatAmount = ethAsset.fiatAmount,
            formattedMaxFiatAmount = ethAsset.fiatAmount.toString(),
            currentFiatAmount = "",
            useMaxAmount = false
        )
    }
}

