package com.example.transactions

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.core.data.repository.EnsRepository
import com.core.model.Transfer
import com.core.model.Transfer.EntryCategory
import com.core.model.UserData
import com.example.transactions.fakes.FakeEnsRepository
import com.example.transactions.fakes.FakeTokenMetadataRepository
import com.example.transactions.fakes.FakeTransferRepository
import com.example.transactions.fakes.FakeUserDataRepository
import com.example.transactions.fakes.createNoopTerminalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for TransactionViewModel.
 *
 * These tests mirror the style of Home/Send/Swap ViewModel tests and can be
 * run as plain JVM tests (no device/emulator required).
 *
 * What we test:
 * - transferState wiring from GetTransfersUseCase → TransfersUiState
 * - isRefreshing toggle behavior during refreshData()
 * - interaction with TransferRepository.refreshTransfers()
 * - basic safety of terminal-related methods (no crashes)
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var userDataRepository: FakeUserDataRepository
    private lateinit var transferRepository: FakeTransferRepository
    private lateinit var ensRepository: EnsRepository
    private lateinit var tokenMetadataRepository: FakeTokenMetadataRepository
    private lateinit var viewModel: TransactionViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        userDataRepository = FakeUserDataRepository(
            initialUserData = UserData(
                walletAddress = "0xTestWallet",
                walletNetwork = "1",
                isFirstBoot = false,
                preferredCurrency = "usd"
            )
        )
        transferRepository = FakeTransferRepository()
        ensRepository = FakeEnsRepository()
        tokenMetadataRepository = FakeTokenMetadataRepository()

        // Use real GetTransfersUseCase wired to fakes so we test real mapping logic
        val getTransfersUseCase = com.core.domain.GetTransfersUseCase(
            transferRepository = transferRepository,
            ensRepository = ensRepository
        )

        viewModel = TransactionViewModel(
            getTransfersUseCase = getTransfersUseCase,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            tokenMetadataRepository = tokenMetadataRepository,
            terminalRepository = createNoopTerminalRepository(context),
            reflectiveLedPattern = null,
            appContext = context
        )
    }

    // =============================================
    // INITIAL STATE TESTS
    // =============================================

    @Test
    fun `transferState starts as Loading`() {
        val state = viewModel.transferState.value
        assertTrue(state is TransfersUiState.Loading)
    }

    @Test
    fun `isRefreshing defaults to false`() {
        assertFalse(viewModel.isRefreshing.value)
    }

    // =============================================
    // TRANSFER FLOW TESTS
    // =============================================

    @Test
    fun `transferState emits Success when transfers are available`() = runTest {
        // Arrange: emit some transfers through the fake repository
        val nowEpochMillis = Clock.System.now().toEpochMilliseconds()
        val transfers = listOf(
            createTransfer(
                txHash = "0xhash1",
                from = "0xAlice",
                to = "0xBob",
                asset = "ETH",
                value = 1.234,
                blockTimestampMillis = nowEpochMillis
            ),
            createTransfer(
                txHash = "0xhash2",
                from = "0xCarol",
                to = "0xDave",
                asset = "USDC",
                value = 100.0,
                blockTimestampMillis = nowEpochMillis - 60_000
            )
        )

        transferRepository.emitTransfers(transfers)

        // Act: wait for mapping in GetTransfersUseCase → ViewModel
        val state = viewModel.transferState.first { it is TransfersUiState.Success }
                as TransfersUiState.Success

        // Assert
        assertEquals(2, state.transfers.size)
        val first = state.transfers[0]
        val second = state.transfers[1]

        // Transfers should be sorted by timestamp ascending (older first)
        assertEquals("0xhash2", first.txHash)
        assertEquals("0xhash1", second.txHash)
    }

    // =============================================
    // REFRESH BEHAVIOR TESTS
    // =============================================

    @Test
    fun `refreshData toggles isRefreshing and calls refreshTransfers with wallet address`() = runTest {
        // Precondition
        assertFalse(viewModel.isRefreshing.value)

        // Act
        viewModel.refreshData()
        advanceUntilIdle()

        // Assert
        assertFalse("isRefreshing should be false after refresh completes", viewModel.isRefreshing.value)
        assertEquals("0xTestWallet", transferRepository.lastRefreshedAddress)
    }

    // =============================================
    // TERMINAL INTERACTION SAFETY TESTS
    // =============================================

    @Test
    fun `resumeLogOpened does not crash`() = runTest {
        viewModel.resumeLogOpened()
        // Allow delayed job to run
        advanceUntilIdle()
        // No assertion here; just ensuring no exceptions
    }

    @Test
    fun `onLogClosed does not crash`() = runTest {
        viewModel.onLogClosed()
        advanceUntilIdle()
    }

    @Test
    fun `onDetailLogOpened does not crash`() = runTest {
        viewModel.onDetailLogOpened("0xhash1")
        advanceUntilIdle()
    }

    @Test
    fun `onDetailLogResume and cancelPendingOperations do not crash`() = runTest {
        viewModel.onDetailLogResume("0xhash1")
        advanceUntilIdle()

        viewModel.cancelPendingOperations()
        advanceUntilIdle()
    }

    // =============================================
    // HELPERS
    // =============================================

    private fun createTransfer(
        txHash: String,
        from: String,
        to: String,
        asset: String,
        value: Double,
        blockTimestampMillis: Long,
        chainId: Int = 1
    ): Transfer {
        return Transfer(
            asset = asset,
            chainId = chainId,
            category = EntryCategory.EXTERNAL,
            erc1155Metadata = emptyList(),
            erc721TokenId = "",
            from = from,
            rawContract = Transfer.RawContract(
                address = null,
                decimal = null,
                value = null
            ),
            to = to,
            tokenId = "",
            value = value,
            blockTimestamp = Instant.fromEpochMilliseconds(blockTimestampMillis),
            userIsSender = true,
            txHash = txHash
        )
    }
}


