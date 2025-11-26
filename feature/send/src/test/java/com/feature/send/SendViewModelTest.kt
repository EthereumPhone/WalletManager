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
import com.feature.send.fakes.SendUsecaseFactory
import com.feature.send.fakes.createNoopTerminalRepository
import com.feature.send.ui.TransactionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun initial_states_defaults() {
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
    fun assetsUiState_emitsEmpty_thenSuccess_whenTokensAppear() = runTest {
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
    fun changeSelectedAsset_updatesSelection_andAmountUiState() = runTest {
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
    fun updateAddress_setsValue_withoutEnsResolution_forNonEns() = runTest {
        viewModel.updateAddress("0x1234")
        advanceUntilIdle()
        val state = viewModel.recipientUiState.value
        assertEquals("0x1234", state.recipientAddress)
        assertFalse(state.isResolving)
        assertEquals("", state.ensError)
        assertFalse(viewModel.shouldDismissKeyboard.value)
    }

    @Test
    fun updateAmount_sanitization_and_targetFields() {
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
    fun setMaxAmount_nativeAsset_usesRepositoryValue_and_setsFlag() = runTest {
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
    fun setMaxAmount_erc20_usesRepositoryValue_and_setsFlag() = runTest {
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
    fun onKeyboardDismissed_resetsFlagToFalse() {
        // Initially false; calling it should keep false and not crash
        viewModel.onKeyboardDismissed()
        assertFalse(viewModel.shouldDismissKeyboard.value)
    }

    @Test
    fun qrScannerTrigger_toggle() {
        assertFalse(viewModel.qrScannerTriggered.value)
        viewModel.triggerQrScanner()
        assertTrue(viewModel.qrScannerTriggered.value)
        viewModel.resetQrScannerTrigger()
        assertFalse(viewModel.qrScannerTriggered.value)
    }

    @Test
    fun sendTransactionTrigger_toggle() {
        assertFalse(viewModel.sendTransactionTriggered.value)
        viewModel.triggerSendTransaction()
        assertTrue(viewModel.sendTransactionTriggered.value)
        viewModel.resetSendTransactionTrigger()
        assertFalse(viewModel.sendTransactionTriggered.value)
    }

    @Test
    fun send_withoutSelectedAsset_setsFailureStatus() = runTest {
        // Provide a valid crypto amount, but no selected asset
        viewModel.updateAmount("1", isFiat = false)
        viewModel.send()
        advanceUntilIdle()

        val status = viewModel.transactionStatus.value
        assertTrue(status is TransactionStatus.FAILURE)
        assertEquals("No asset selected", (status as TransactionStatus.FAILURE).errorMessage)
    }

    @Test
    fun clearTransactionStatus_setsNull() {
        // Manually put a failure state by calling send as above
        viewModel.updateAmount("1", isFiat = false)
        viewModel.send()

        // Clear status
        viewModel.clearTransactionStatus()
        assertNull(viewModel.transactionStatus.value)
    }

    @Test
    fun txComplete_reset_setsUnComplete() {
        // Default is UnComplete; calling reset should keep it UnComplete (idempotent)
        viewModel.resetTxComplete()
        assertTrue(viewModel.txComplete.value is TxCompleteUiState.UnComplete)
    }

    @Test
    fun terminal_calls_doNotCrash() = runTest {
        viewModel.onScreenOpened()
        viewModel.onScreenOpenedAfterResume()
        viewModel.onSendClosed()
        viewModel.showFailedMatrix()
        viewModel.showWarningMatrix()
        viewModel.showSuccessMatrix()
        // No state change expected; just ensure no exceptions
        assertNull(viewModel.transactionStatus.value)
    }

    @Test
    fun getContacts_returnsEmptyList_inDefaultRobolectricEnv() = runTest {
        viewModel.getContacts(context)
        val list = viewModel.contacts.first()
        assertTrue(list.isEmpty())
    }
}


