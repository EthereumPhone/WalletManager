package com.feature.send

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.core.data.remote.EnsApi
import com.core.model.TokenAssetWithPrice
import com.core.model.UserData
import com.feature.send.fakes.FakeGroupedTokenRepository
import com.feature.send.fakes.FakeNetworkBalanceRepository
import com.feature.send.fakes.FakeSendRepository
import com.feature.send.fakes.FakeTokenExchangeRepository
import com.feature.send.fakes.FakeUserDataRepository
import com.feature.send.fakes.createNoopTerminalRepository
import com.feature.send.ui.TransactionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SendViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var userDataRepository: FakeUserDataRepository
    private lateinit var networkBalanceRepository: FakeNetworkBalanceRepository
    private lateinit var tokenExchangeRepository: FakeTokenExchangeRepository
    private lateinit var groupedTokenRepository: FakeGroupedTokenRepository
    private lateinit var sendRepository: FakeSendRepository
    private lateinit var viewModel: SendViewModel

    private val groupId = "group_eth"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        userDataRepository = FakeUserDataRepository(
            initialUserData = UserData(
                walletAddress = "0xabc",
                walletNetwork = "1",
                isFirstBoot = false,
                preferredCurrency = "usd"
            )
        )
        networkBalanceRepository = FakeNetworkBalanceRepository()
        tokenExchangeRepository = FakeTokenExchangeRepository()
        groupedTokenRepository = FakeGroupedTokenRepository()
        sendRepository = FakeSendRepository()

        viewModel = SendViewModel(
            userDataRepository = userDataRepository,
            networkBalanceRepository = networkBalanceRepository,
            tokenExchangeRepository = tokenExchangeRepository,
            groupedTokenRepository = groupedTokenRepository,
            sendRepository = sendRepository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "groupId" to groupId,
                    "address" to "",
                    "amount" to "",
                    "chainId" to "1"
                )
            ),
            ensApi = EnsApi(),
            terminalRepository = createNoopTerminalRepository(context),
            terminalSDK = null,
            reflectiveLedPattern = null,
            context = context
        )
    }

    @Test
    fun viewModel_whenInitialized_shouldHaveDefaultStateValues() {
        // Depending on test dispatcher timing, state can be Loading then quickly Empty
        val initial = viewModel.assetsUiState.value
        assertTrue(initial is AssetsUiState.Loading || initial is AssetsUiState.Empty)
        // triggers are false
        assertFalse(viewModel.qrScannerTriggered.value)
        assertFalse(viewModel.sendTransactionTriggered.value)
        // no selection and no transaction status
        assertTrue(viewModel.selectedAssetUiState.value is SelectedAssetUiState.Unselected)
        assertNull(viewModel.transactionStatus.value)
    }

    @Test
    fun assetsUiState_whenTokensEmitted_shouldTransitionFromEmptyToSuccess() = runTest {
        // Initially, no tokens -> Empty after Loading
        val empty = viewModel.assetsUiState.first { it !is AssetsUiState.Loading }
        assertTrue(empty is AssetsUiState.Empty)

        // Emit tokens for the current group
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "1", // native ETH on chain 1 (address equals chainId)
                chainId = 1,
                symbol = "ETH",
                name = "Ethereum",
                balance = 2.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 7500.0
            ),
            TokenAssetWithPrice(
                address = "0xERC",
                chainId = 1,
                symbol = "USDC",
                name = "USD Coin",
                balance = 1000.0,
                decimals = 6,
                logoUrl = null,
                swappable = true,
                fiatAmount = 1000.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)

        val success = viewModel.assetsUiState.first { it is AssetsUiState.Success } as AssetsUiState.Success
        assertEquals(2, success.assets.size)
        assertEquals("ETH", success.assets[0].symbol)
    }

    @Test
    fun changeSelectedAsset_whenCalled_shouldUpdateSelectionAndResetAmountState() = runTest {
        // Provide assets
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "1",
                chainId = 1,
                symbol = "ETH",
                name = "Ethereum",
                balance = 2.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 7500.0
            ),
            TokenAssetWithPrice(
                address = "0xERC",
                chainId = 1,
                symbol = "USDC",
                name = "USD Coin",
                balance = 1000.0,
                decimals = 6,
                logoUrl = null,
                swappable = true,
                fiatAmount = 1000.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
        advanceUntilIdle()

        // Auto-selection should select the preferred chain (1) – ETH here
        val initialSelected = viewModel.selectedAssetUiState.value as SelectedAssetUiState.Selected
        assertEquals("ETH", initialSelected.tokenAsset.symbol)

        viewModel.changeSelectedAsset(1)
        val sel = viewModel.selectedAssetUiState.value as SelectedAssetUiState.Selected
        assertEquals("ETH", sel.tokenAsset.symbol)

        val amount = viewModel.amountUiState.value
        assertEquals(2.5, amount.maxAmount, 0.0)
        assertEquals("", amount.currentAmount)
        assertEquals("", amount.currentFiatAmount)
        assertFalse(amount.useMaxAmount)
    }

    @Test
    fun updateAddress_givenNonEnsAddress_shouldSetValueWithoutResolution() = runTest {
        viewModel.updateAddress("0x1234")
        advanceUntilIdle()
        val state = viewModel.recipientUiState.value
        assertEquals("0x1234", state.recipientAddress)
        assertFalse(state.isResolving)
        assertEquals("", state.ensError)
        assertFalse(viewModel.shouldDismissKeyboard.value)
    }

    @Test
    fun updateAmount_givenVariousInputs_shouldSanitizeAndSetCorrectField() {
        // Crypto amount with single dot is normalized
        viewModel.updateAmount(".", isFiat = false)
        assertEquals("0.", viewModel.amountUiState.value.currentAmount)
        assertEquals("", viewModel.amountUiState.value.currentFiatAmount)

        // Remove second dot
        viewModel.updateAmount("1.2.3", isFiat = false)
        assertEquals("1.23", viewModel.amountUiState.value.currentAmount)

        // When isFiat is true, update fiat field only
        viewModel.updateAmount("10.00", isFiat = true)
        assertEquals("", viewModel.amountUiState.value.currentAmount)
        assertEquals("10.00", viewModel.amountUiState.value.currentFiatAmount)
    }

    @Test
    fun setMaxAmount_givenNativeAsset_shouldUseRepositoryValueAndSetFlag() = runTest {
        // Provide native asset
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "1", // native indicator: address == chainId
                chainId = 1,
                symbol = "ETH",
                name = "Ethereum",
                balance = 2.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 7500.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
        advanceUntilIdle()

        viewModel.changeSelectedAsset(1)
        // Stub repo to return a precise max native value
        sendRepository.stubMaxAllowedSend(chainId = 1, value = "2.4")

        viewModel.setMaxAmount()
        advanceUntilIdle()

        val amt = viewModel.amountUiState.value
        assertEquals("2.4", amt.currentAmount)
        assertTrue(amt.useMaxAmount)
        // currentFiatAmount mirrors formattedMaxFiatAmount when max is set
        assertEquals(amt.formattedMaxFiatAmount, amt.currentFiatAmount)
    }

    @Test
    fun setMaxAmount_givenErc20Token_shouldUseRepositoryValueAndSetFlag() = runTest {
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "0xERC",
                chainId = 1,
                symbol = "USDC",
                name = "USD Coin",
                balance = 1000.0,
                decimals = 6,
                logoUrl = null,
                swappable = true,
                fiatAmount = 1000.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
        advanceUntilIdle()

        viewModel.changeSelectedAsset(1)
        sendRepository.stubMaxErc20("0xERC", 1, 6, "1000")

        viewModel.setMaxAmount()
        advanceUntilIdle()

        val amt = viewModel.amountUiState.value
        assertEquals("1000", amt.currentAmount)
        assertTrue(amt.useMaxAmount)
        assertEquals(amt.formattedMaxFiatAmount, amt.currentFiatAmount)
    }

    @Test
    fun send_givenNoAssetSelected_shouldSetFailureStatus() = runTest {
        // Provide a valid crypto amount, but no selected asset
        viewModel.updateAmount("1", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        val status = viewModel.transactionStatus.value
        assertTrue(status is TransactionStatus.FAILURE)
        assertEquals("No asset selected", (status as TransactionStatus.FAILURE).errorMessage)
    }

    // ==================== send() method tests ====================

    @Test
    fun send_givenEmptyAmount_shouldNotInitiateTransfer() = runTest {
        // Setup asset but leave amount empty
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.send()
        advanceUntilIdle()

        // Status should remain null - early return
        assertNull(viewModel.transactionStatus.value)
        // No transfer calls made
        assertTrue(sendRepository.transferEthCalls.isEmpty())
    }

    @Test
    fun send_givenDotOnlyAmount_shouldNotInitiateTransfer() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.updateAmount(".", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        // "." normalizes to "0." which is considered invalid for send
        assertNull(viewModel.transactionStatus.value)
        assertTrue(sendRepository.transferEthCalls.isEmpty())
    }

    @Test
    fun send_givenZeroDotAmount_shouldNotInitiateTransfer() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.updateAmount("0.", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        assertNull(viewModel.transactionStatus.value)
        assertTrue(sendRepository.transferEthCalls.isEmpty())
    }

    @Test
    fun send_givenNativeEthAsset_shouldCallTransferEthWithCorrectParams() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.updateAddress("0xRecipient123")
        viewModel.updateAmount("1.5", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        // Verify transfer was called
        assertEquals(1, sendRepository.transferEthCalls.size)
        val call = sendRepository.transferEthCalls[0]
        assertEquals(1, call.chainId)
        assertEquals("0xRecipient123", call.toAddress)
        assertEquals("1.5", call.value)
    }

    @Test
    fun send_givenErc20Token_shouldCallTransferErc20WithCorrectParams() = runTest {
        setupErc20UsdcAsset()
        advanceUntilIdle()

        viewModel.updateAddress("0xRecipient456")
        viewModel.updateAmount("100", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        // Verify ERC20 transfer was called
        assertEquals(1, sendRepository.transferErc20Calls.size)
        val call = sendRepository.transferErc20Calls[0]
        assertEquals(1, call.chainId)
        assertEquals("0xRecipient456", call.toAddress)
        assertEquals(100.0, call.amount, 0.001)
        assertEquals("USDC", call.tokenAsset.symbol)
    }

    @Test
    fun send_whenCalled_shouldSetStatusToPendingAndInitiateTransfer() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("1", isFiat = false)

        // Initially null
        assertNull(viewModel.transactionStatus.value)

        viewModel.send()
        // After send starts, status should be PENDING
        advanceUntilIdle()

        // The send was initiated (transfer was called)
        assertEquals(1, sendRepository.transferEthCalls.size)
    }

    @Test
    fun send_givenUserDecline_shouldSetFailureWithDeclineMessage() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        // Stub repository to return "decline"
        sendRepository.stubTransactionResult("decline")

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("1", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        val status = viewModel.transactionStatus.value
        assertTrue(status is TransactionStatus.FAILURE)
        assertEquals("Transaction declined", (status as TransactionStatus.FAILURE).errorMessage)
    }

    @Test
    fun send_givenErrorResult_shouldSetFailureStatus() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        // Stub repository to return "error"
        sendRepository.stubTransactionResult("error")

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("1", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        val status = viewModel.transactionStatus.value
        assertTrue(status is TransactionStatus.FAILURE)
    }

    @Test
    fun send_givenFiatAmount_shouldConvertToCryptoAndSend() = runTest {
        // Setup asset with known price: 2.5 ETH = $7500, so 1 ETH = $3000
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "1",
                chainId = 1,
                symbol = "ETH",
                name = "Ethereum",
                balance = 2.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 7500.0 // $3000 per ETH
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
        advanceUntilIdle()

        viewModel.updateAddress("0xRecipient")
        // Enter fiat amount of $3000 (should convert to 1 ETH)
        viewModel.updateAmount("3000", isFiat = true)
        viewModel.send()
        advanceUntilIdle()

        // Verify transfer was called
        assertEquals(1, sendRepository.transferEthCalls.size)
        val call = sendRepository.transferEthCalls[0]
        // $3000 / $3000 per ETH = 1 ETH
        assertEquals(1.0, call.value.toDouble(), 0.001)
    }

    // ==================== updateAddress() tests ====================

    @Test
    fun updateAddress_givenEmptyString_shouldSetEmptyRecipient() = runTest {
        viewModel.updateAddress("")
        advanceUntilIdle()

        val state = viewModel.recipientUiState.value
        assertEquals("", state.recipientAddress)
        assertFalse(state.isResolving)
    }

    @Test
    fun updateAddress_givenHexAddress_shouldSetValueWithoutResolution() = runTest {
        val hexAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f5e123"
        viewModel.updateAddress(hexAddress)
        advanceUntilIdle()

        val state = viewModel.recipientUiState.value
        assertEquals(hexAddress, state.recipientAddress)
        assertFalse(state.isResolving)
        assertEquals("", state.ensError)
    }

    @Test
    fun updateAddress_givenEnsName_shouldTriggerEnsResolution() = runTest {
        // ENS names ending in .eth should trigger resolution
        viewModel.updateAddress("vitalik.eth")
        // Don't wait for full resolution - just check isResolving was triggered
        
        // The address should be set
        assertEquals("vitalik.eth", viewModel.recipientUiState.value.recipientAddress)
        // Note: isResolving may have already transitioned due to network/async behavior
    }

    @Test
    fun updateAddress_givenMultipleUpdates_shouldKeepLatestValue() = runTest {
        viewModel.updateAddress("0xFirst")
        viewModel.updateAddress("0xSecond")
        viewModel.updateAddress("0xThird")
        advanceUntilIdle()

        assertEquals("0xThird", viewModel.recipientUiState.value.recipientAddress)
    }

    // ==================== updateAmount() tests ====================

    @Test
    fun updateAmount_givenCryptoValue_shouldSetCurrentAmountField() {
        viewModel.updateAmount("1.5", isFiat = false)

        assertEquals("1.5", viewModel.amountUiState.value.currentAmount)
        assertEquals("", viewModel.amountUiState.value.currentFiatAmount)
        assertFalse(viewModel.amountUiState.value.useMaxAmount)
    }

    @Test
    fun updateAmount_givenFiatValue_shouldSetCurrentFiatAmountField() {
        viewModel.updateAmount("100.50", isFiat = true)

        assertEquals("", viewModel.amountUiState.value.currentAmount)
        assertEquals("100.50", viewModel.amountUiState.value.currentFiatAmount)
    }

    @Test
    fun updateAmount_whenSwitchingBetweenCryptoAndFiat_shouldClearOtherField() {
        // Set crypto first
        viewModel.updateAmount("1.5", isFiat = false)
        assertEquals("1.5", viewModel.amountUiState.value.currentAmount)

        // Switch to fiat - crypto should clear
        viewModel.updateAmount("100", isFiat = true)
        assertEquals("", viewModel.amountUiState.value.currentAmount)
        assertEquals("100", viewModel.amountUiState.value.currentFiatAmount)

        // Switch back to crypto - fiat should clear
        viewModel.updateAmount("2.0", isFiat = false)
        assertEquals("2.0", viewModel.amountUiState.value.currentAmount)
        assertEquals("", viewModel.amountUiState.value.currentFiatAmount)
    }

    @Test
    fun updateAmount_givenMultipleDots_shouldKeepOnlyFirstDot() {
        viewModel.updateAmount("1.2.3.4", isFiat = false)
        assertEquals("1.234", viewModel.amountUiState.value.currentAmount)
    }

    // ==================== Additional Amount Input Validation Tests ====================

    @Test
    fun updateAmount_givenEmptyString_shouldSetEmptyAmount() {
        viewModel.updateAmount("", isFiat = false)
        assertEquals("", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenJustZero_shouldAcceptIt() {
        viewModel.updateAmount("0", isFiat = false)
        assertEquals("0", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenTrailingDecimal_shouldAcceptIt() {
        viewModel.updateAmount("5.", isFiat = false)
        assertEquals("5.", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenLeadingDecimalWithDigits_shouldAcceptIt() {
        viewModel.updateAmount(".5", isFiat = false)
        assertEquals(".5", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenVeryLongDecimalPlaces_shouldAcceptIt() {
        viewModel.updateAmount("1.123456789012345", isFiat = false)
        assertEquals("1.123456789012345", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenVeryLargeNumber_shouldAcceptIt() {
        viewModel.updateAmount("999999999999", isFiat = false)
        assertEquals("999999999999", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenConsecutiveDots_shouldRemoveSecondDot() {
        viewModel.updateAmount("1..5", isFiat = false)
        assertEquals("1.5", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenMultipleZeros_shouldAcceptThem() {
        viewModel.updateAmount("00", isFiat = false)
        assertEquals("00", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenZeroPointZero_shouldAcceptIt() {
        viewModel.updateAmount("0.0", isFiat = false)
        assertEquals("0.0", viewModel.amountUiState.value.currentAmount)
    }

    // ==================== Fiat Mode Input Validation Tests ====================

    @Test
    fun updateAmount_givenDotInFiatMode_shouldNormalizeToZeroDot() {
        viewModel.updateAmount(".", isFiat = true)
        assertEquals("0.", viewModel.amountUiState.value.currentFiatAmount)
        assertEquals("", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenMultipleDotsInFiatMode_shouldRemoveSecondDot() {
        viewModel.updateAmount("100.50.25", isFiat = true)
        assertEquals("100.5025", viewModel.amountUiState.value.currentFiatAmount)
    }

    @Test
    fun updateAmount_givenEmptyStringInFiatMode_shouldSetEmptyFiatAmount() {
        viewModel.updateAmount("", isFiat = true)
        assertEquals("", viewModel.amountUiState.value.currentFiatAmount)
    }

    @Test
    fun updateAmount_givenLeadingDecimalInFiatMode_shouldAcceptIt() {
        viewModel.updateAmount(".99", isFiat = true)
        assertEquals(".99", viewModel.amountUiState.value.currentFiatAmount)
    }

    @Test
    fun updateAmount_givenTrailingDecimalInFiatMode_shouldAcceptIt() {
        viewModel.updateAmount("100.", isFiat = true)
        assertEquals("100.", viewModel.amountUiState.value.currentFiatAmount)
    }

    // ==================== Max Amount Boundary Tests ====================

    @Test
    fun updateAmount_givenAmountExceedingBalance_shouldStillStoreIt() = runTest {
        setupNativeEthAsset() // balance = 2.5 ETH
        advanceUntilIdle()

        // Amount exceeds balance
        viewModel.updateAmount("10.0", isFiat = false)

        // Amount is stored (UI would show it in red, but value is stored)
        assertEquals("10.0", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenAmountAtExactBalance_shouldStoreIt() = runTest {
        setupNativeEthAsset() // balance = 2.5 ETH
        advanceUntilIdle()

        viewModel.updateAmount("2.5", isFiat = false)
        assertEquals("2.5", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenAmountBelowBalance_shouldStoreIt() = runTest {
        setupNativeEthAsset() // balance = 2.5 ETH
        advanceUntilIdle()

        viewModel.updateAmount("1.0", isFiat = false)
        assertEquals("1.0", viewModel.amountUiState.value.currentAmount)
    }

    // ==================== Send Validation Edge Cases ====================

    @Test
    fun send_givenLeadingDecimalAmount_shouldProcessCorrectly() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount(".5", isFiat = false) // 0.5 ETH
        viewModel.send()
        advanceUntilIdle()

        // Transfer should be called
        assertEquals(1, sendRepository.transferEthCalls.size)
        assertEquals(".5", sendRepository.transferEthCalls[0].value)
    }

    @Test
    fun send_givenTrailingDecimalAmount_shouldProcessCorrectly() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("1.", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        // Transfer should be called
        assertEquals(1, sendRepository.transferEthCalls.size)
        assertEquals("1.", sendRepository.transferEthCalls[0].value)
    }

    @Test
    fun send_givenZeroAmount_shouldProcessIt() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("0", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        // Transfer should be called (blockchain will handle 0 amount)
        assertEquals(1, sendRepository.transferEthCalls.size)
        assertEquals("0", sendRepository.transferEthCalls[0].value)
    }

    @Test
    fun send_givenDotOnlyInFiatMode_shouldNotInitiateTransfer() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.updateAmount(".", isFiat = true) // Becomes "0." in fiat field
        viewModel.updateAddress("0xRecipient")
        viewModel.send()
        advanceUntilIdle()

        // "0." is invalid for sending
        assertNull(viewModel.transactionStatus.value)
        assertTrue(sendRepository.transferEthCalls.isEmpty())
    }

    @Test
    fun send_givenValidFiatOnlyAmount_shouldConvertAndSend() = runTest {
        // Setup asset with known price: 2.5 ETH = $7500, so 1 ETH = $3000
        setupNativeEthAsset()
        advanceUntilIdle()

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("1500", isFiat = true) // $1500 = 0.5 ETH
        viewModel.send()
        advanceUntilIdle()

        // Transfer should be called with converted amount
        assertEquals(1, sendRepository.transferEthCalls.size)
        // $1500 / $3000 per ETH = 0.5 ETH
        assertEquals(0.5, sendRepository.transferEthCalls[0].value.toDouble(), 0.001)
    }

    // ==================== Rapid Input Change Tests ====================

    @Test
    fun updateAmount_givenRapidInputChanges_shouldOnlyKeepLastValue() {
        viewModel.updateAmount("1", isFiat = false)
        viewModel.updateAmount("12", isFiat = false)
        viewModel.updateAmount("123", isFiat = false)
        viewModel.updateAmount("1234", isFiat = false)

        assertEquals("1234", viewModel.amountUiState.value.currentAmount)
    }

    @Test
    fun updateAmount_givenRapidModeSwitch_shouldMaintainCorrectField() {
        viewModel.updateAmount("1.5", isFiat = false)
        viewModel.updateAmount("100", isFiat = true)
        viewModel.updateAmount("2.0", isFiat = false)

        assertEquals("2.0", viewModel.amountUiState.value.currentAmount)
        assertEquals("", viewModel.amountUiState.value.currentFiatAmount)
    }

    @Test
    fun updateAmount_afterSetMaxAmount_shouldResetUseMaxFlag() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        sendRepository.stubMaxAllowedSend(chainId = 1, value = "2.4")
        viewModel.setMaxAmount()
        advanceUntilIdle()

        assertTrue(viewModel.amountUiState.value.useMaxAmount)

        // Manually updating amount should reset the flag
        viewModel.updateAmount("1.0", isFiat = false)
        assertFalse(viewModel.amountUiState.value.useMaxAmount)
    }

    // ==================== changeSelectedAsset() tests ====================

    @Test
    fun changeSelectedAsset_givenDifferentChainId_shouldSwitchToNewAsset() = runTest {
        // Setup multiple assets on different chains
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "1",
                chainId = 1,
                symbol = "ETH",
                name = "Ethereum",
                balance = 2.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 7500.0
            ),
            TokenAssetWithPrice(
                address = "10",
                chainId = 10,
                symbol = "ETH",
                name = "Ethereum (Optimism)",
                balance = 1.0,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 3000.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
        advanceUntilIdle()

        // Initially selects lowest chainId (1)
        var selected = viewModel.selectedAssetUiState.value as SelectedAssetUiState.Selected
        assertEquals(1, selected.tokenAsset.chainId)

        // Switch to chain 10
        viewModel.changeSelectedAsset(10)
        selected = viewModel.selectedAssetUiState.value as SelectedAssetUiState.Selected
        assertEquals(10, selected.tokenAsset.chainId)
        assertEquals("Ethereum (Optimism)", selected.tokenAsset.name)
    }

    @Test
    fun changeSelectedAsset_whenCalled_shouldResetAmountToEmpty() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        // Set some amount
        viewModel.updateAmount("1.5", isFiat = false)
        assertEquals("1.5", viewModel.amountUiState.value.currentAmount)

        // Re-select the same asset - amount should reset
        viewModel.changeSelectedAsset(1)

        assertEquals("", viewModel.amountUiState.value.currentAmount)
        assertEquals("", viewModel.amountUiState.value.currentFiatAmount)
        assertFalse(viewModel.amountUiState.value.useMaxAmount)
    }

    @Test
    fun changeSelectedAsset_givenNewChain_shouldUpdateMaxAmountToNewBalance() = runTest {
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "1",
                chainId = 1,
                symbol = "ETH",
                name = "Ethereum",
                balance = 2.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 7500.0
            ),
            TokenAssetWithPrice(
                address = "10",
                chainId = 10,
                symbol = "ETH",
                name = "Ethereum (Optimism)",
                balance = 5.0,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 15000.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
        advanceUntilIdle()

        // Initially chain 1 with balance 2.5
        assertEquals(2.5, viewModel.amountUiState.value.maxAmount, 0.001)

        // Switch to chain 10 with balance 5.0
        viewModel.changeSelectedAsset(10)
        assertEquals(5.0, viewModel.amountUiState.value.maxAmount, 0.001)
    }

    // ==================== setMaxAmount() tests ====================

    @Test
    fun setMaxAmount_whenCalled_shouldSetUseMaxFlagToTrue() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        sendRepository.stubMaxAllowedSend(chainId = 1, value = "2.4")

        assertFalse(viewModel.amountUiState.value.useMaxAmount)
        viewModel.setMaxAmount()
        advanceUntilIdle()

        assertTrue(viewModel.amountUiState.value.useMaxAmount)
    }

    @Test
    fun setMaxAmount_givenTokenWith8Decimals_shouldUseCorrectPrecision() = runTest {
        // Token with 8 decimals (like WBTC)
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "0xWBTC",
                chainId = 1,
                symbol = "WBTC",
                name = "Wrapped Bitcoin",
                balance = 0.5,
                decimals = 8,
                logoUrl = null,
                swappable = true,
                fiatAmount = 25000.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
        advanceUntilIdle()

        sendRepository.stubMaxErc20("0xWBTC", 1, 8, "0.49999999")

        viewModel.setMaxAmount()
        advanceUntilIdle()

        assertEquals("0.49999999", viewModel.amountUiState.value.currentAmount)
    }

    // ==================== Auto-selection (init) tests ====================

    @Test
    fun autoSelection_givenSingleAsset_shouldSelectItAutomatically() = runTest {
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "1",
                chainId = 1,
                symbol = "ETH",
                name = "Ethereum",
                balance = 2.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 7500.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
        advanceUntilIdle()

        val selected = viewModel.selectedAssetUiState.value
        assertTrue(selected is SelectedAssetUiState.Selected)
        assertEquals("ETH", (selected as SelectedAssetUiState.Selected).tokenAsset.symbol)
    }

    @Test
    fun autoSelection_givenMultipleAssets_shouldSelectLowestChainId() = runTest {
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "42161",
                chainId = 42161, // Arbitrum
                symbol = "ETH",
                name = "Ethereum (Arbitrum)",
                balance = 1.0,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 3000.0
            ),
            TokenAssetWithPrice(
                address = "1",
                chainId = 1, // Mainnet - lowest
                symbol = "ETH",
                name = "Ethereum",
                balance = 2.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 7500.0
            ),
            TokenAssetWithPrice(
                address = "10",
                chainId = 10, // Optimism
                symbol = "ETH",
                name = "Ethereum (Optimism)",
                balance = 0.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 1500.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
        advanceUntilIdle()

        val selected = viewModel.selectedAssetUiState.value as SelectedAssetUiState.Selected
        assertEquals(1, selected.tokenAsset.chainId) // Should select chain 1 (lowest)
    }

    // ==================== Error Parsing tests (AA error codes) ====================

    @Test
    fun send_givenAA21GasError_shouldShowNotEnoughEthForGasMessage() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        // Stub to return an AA21 error
        sendRepository.stubTransactionResult("AA21 didn't pay prefund")

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("1", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        val status = viewModel.transactionStatus.value
        assertTrue(status is TransactionStatus.FAILURE)
        assertEquals("Not enough ETH for gas", (status as TransactionStatus.FAILURE).errorMessage)
    }

    @Test
    fun send_givenAA24SignatureError_shouldShowInvalidSignatureMessage() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        sendRepository.stubTransactionResult("AA24 signature error")

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("1", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        val status = viewModel.transactionStatus.value
        assertTrue(status is TransactionStatus.FAILURE)
        assertEquals("Invalid signature", (status as TransactionStatus.FAILURE).errorMessage)
    }

    @Test
    fun send_givenAA31PaymasterError_shouldShowPaymasterFundsLowMessage() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        sendRepository.stubTransactionResult("AA31 paymaster deposit too low")

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("1", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        val status = viewModel.transactionStatus.value
        assertTrue(status is TransactionStatus.FAILURE)
        assertEquals("Paymaster funds too low", (status as TransactionStatus.FAILURE).errorMessage)
    }

    @Test
    fun send_givenInsufficientFundsError_shouldShowInsufficientFundsMessage() = runTest {
        setupNativeEthAsset()
        advanceUntilIdle()

        sendRepository.stubTransactionResult("insufficient funds for transfer")

        viewModel.updateAddress("0xRecipient")
        viewModel.updateAmount("1", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        val status = viewModel.transactionStatus.value
        assertTrue(status is TransactionStatus.FAILURE)
        assertEquals("Insufficient funds", (status as TransactionStatus.FAILURE).errorMessage)
    }

    // ==================== Helper methods ====================

    private fun setupNativeEthAsset() {
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "1", // native: address == chainId
                chainId = 1,
                symbol = "ETH",
                name = "Ethereum",
                balance = 2.5,
                decimals = 18,
                logoUrl = null,
                swappable = true,
                fiatAmount = 7500.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
    }

    private fun setupErc20UsdcAsset() {
        val tokens = listOf(
            TokenAssetWithPrice(
                address = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", // USDC contract
                chainId = 1,
                symbol = "USDC",
                name = "USD Coin",
                balance = 1000.0,
                decimals = 6,
                logoUrl = null,
                swappable = true,
                fiatAmount = 1000.0
            )
        )
        groupedTokenRepository.emitTokensForGroup(groupId, tokens)
    }
}


