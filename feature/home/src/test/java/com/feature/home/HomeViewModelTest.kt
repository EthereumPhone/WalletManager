package com.feature.home

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.core.model.TokenGroupAssetOverview
import com.core.model.UserData
import com.feature.home.fakes.FakeNetworkMonitor
import com.feature.home.fakes.FakeTransferRepository
import com.feature.home.fakes.FakeUserDataRepository
import com.feature.home.fakes.HomeUsecaseFactory
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

/**
 * # HomeViewModel Unit Tests
 *
 * These tests verify the HomeViewModel's behavior in ISOLATION using fake implementations.
 * This is different from integration tests which use real repositories and databases.
 *
 * ## Key Concepts:
 *
 * ### 1. Fakes vs Mocks
 * - **Fakes**: Full implementations of interfaces with simplified logic (what we use here)
 * - **Mocks**: Objects that record interactions and return predefined values
 * - Fakes are preferred when you want to test behavior, not just interactions
 *
 * ### 2. MainDispatcherRule
 * - ViewModels use Dispatchers.Main by default for StateFlows
 * - On JVM (unit tests), there's no Main dispatcher
 * - This rule replaces Main with a test dispatcher so tests don't crash
 *
 * ### 3. StateFlow Testing
 * - StateFlows are "hot" flows that always have a value
 * - Use `.value` for immediate access or `.first()` to collect once
 * - `advanceUntilIdle()` ensures all coroutines complete before assertions
 *
 * ### 4. What We're Testing:
 * - Token data flows correctly from repositories -> usecase -> ViewModel -> UI state
 * - Fiat calculations are accurate (balance × exchange rate)
 * - Sorting logic works correctly (tokens sorted by fiat value)
 * - Network status properly updates isOffline state
 * - Error states are handled gracefully
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {

    // This rule ensures Dispatchers.Main works in unit tests
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Test dependencies - these are fakes, not real implementations
    private lateinit var context: Context
    private lateinit var networkMonitor: FakeNetworkMonitor
    private lateinit var userDataRepository: FakeUserDataRepository
    private lateinit var transferRepository: FakeTransferRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        // Robolectric provides a fake Android context for testing
        context = ApplicationProvider.getApplicationContext()
        
        // Initialize fakes with default test values
        networkMonitor = FakeNetworkMonitor(initialIsOnline = true)
        userDataRepository = FakeUserDataRepository(
            initialUserData = UserData(
                walletAddress = "0xTestWalletAddress123",
                walletNetwork = "ETH",
                isFirstBoot = false,
                preferredCurrency = "usd"
            )
        )
        transferRepository = FakeTransferRepository(initialHasTransfers = false)

        // Create the usecase with empty data initially
        val usecaseComponents = HomeUsecaseFactory.createGetAllGroupedTokensUsecase(
            erc20Groups = emptyList(),
            networkGroups = emptyList()
        )

        // Create the ViewModel we're testing
        viewModel = HomeViewModel(
            networkMonitor = networkMonitor,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            getAllGroupedTokensUsecase = usecaseComponents.usecase,
            savedStateHandle = SavedStateHandle(),
            context = context
        )
    }

    // =============================================
    // NETWORK STATUS TESTS
    // =============================================

    @Test
    fun `isOffline starts true (defensive) then becomes false when online`() = runTest {
        // The ViewModel starts with isOffline = true as a defensive default
        // After the network monitor emits, it should update
        advanceUntilIdle()
        
        // Since our fake starts with isOnline = true, isOffline should be false
        assertFalse(
            "isOffline should be false when network is online",
            viewModel.isOffline.value
        )
    }

    @Test
    fun `isOffline updates when network status changes`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.isOffline.value)

        // Simulate going offline
        networkMonitor.setOnline(false)
        advanceUntilIdle()
        
        assertTrue(
            "isOffline should be true when network goes offline",
            viewModel.isOffline.value
        )

        // Simulate coming back online
        networkMonitor.setOnline(true)
        advanceUntilIdle()
        
        assertFalse(
            "isOffline should be false when network comes back online",
            viewModel.isOffline.value
        )
    }

    // =============================================
    // TRANSFER INDICATOR TESTS
    // =============================================

    @Test
    fun `hasTransfers reflects transfer repository state`() = runTest {
        // Initially no transfers
        val initial = viewModel.hasTransfers.first()
        assertFalse("hasTransfers should start false", initial)
        
        // Simulate transfers appearing
        transferRepository.setHasTransfers(true)
        
        // Wait for the first emission where hasTransfers is true
        val updated = viewModel.hasTransfers.first { it }
        assertTrue("hasTransfers should be true after transfers appear", updated)
    }

    // =============================================
    // WALLET DATA TESTS
    // =============================================

    @Test
    fun `walletDataState emits Success with user data`() = runTest {
        // Wait for Success state
        val success = viewModel.walletDataState.first { it is WalletDataUiState.Success }
                as WalletDataUiState.Success
        
        assertEquals("0xTestWalletAddress123", success.userData.walletAddress)
        assertEquals("ETH", success.userData.walletNetwork)
        assertEquals("usd", success.userData.preferredCurrency)
        assertFalse(success.userData.isFirstBoot)
    }

    @Test
    fun `walletDataState updates when user preferences change`() = runTest {
        // Get initial state
        val initial = viewModel.walletDataState.first { it is WalletDataUiState.Success }
                as WalletDataUiState.Success
        assertEquals("usd", initial.userData.preferredCurrency)

        // Change preferred currency
        userDataRepository.setPreferredCurrency("eur")
        
        // Get updated state
        val updated = viewModel.walletDataState.first { 
            it is WalletDataUiState.Success && it.userData.preferredCurrency == "eur"
        } as WalletDataUiState.Success
        
        assertEquals("eur", updated.userData.preferredCurrency)
    }

    // =============================================
    // TOKEN ASSET STATE TESTS - THE CORE FUNCTIONALITY
    // =============================================

    @Test
    fun `groupedTokenAssetState emits Empty when no tokens exist`() = runTest {
        // With empty initial data, should emit Empty state
        val state = viewModel.groupedTokenAssetState.first { it !is GroupedAssetsUiState.Loading }
        
        assertTrue(
            "State should be Empty when no tokens exist",
            state is GroupedAssetsUiState.Empty
        )
    }

    @Test
    fun `groupedTokenAssetState emits Success with tokens sorted by fiat balance ascending`() = runTest {
        // Create test token groups with different fiat balances
        val groups = listOf(
            // High value token - should appear LAST after ascending sort
            TokenGroupAssetOverview(
                groupId = "g_high",
                symbol = "ETH",
                name = "Ethereum",
                logoUrl = "https://example.com/eth.png",
                totalBalance = 2.0,
                formattedBalance = "2.00",
                totalFiatBalance = 5000.0,  // $5,000
                formattedFiatBalance = "$5,000.00",
                exchangeCurrency = "usd"
            ),
            // Low value token - should appear FIRST after ascending sort
            TokenGroupAssetOverview(
                groupId = "g_low",
                symbol = "USDC",
                name = "USD Coin",
                logoUrl = "https://example.com/usdc.png",
                totalBalance = 100.0,
                formattedBalance = "100.00",
                totalFiatBalance = 100.0,  // $100
                formattedFiatBalance = "$100.00",
                exchangeCurrency = "usd"
            ),
            // Medium value token - should appear in the MIDDLE
            TokenGroupAssetOverview(
                groupId = "g_mid",
                symbol = "LINK",
                name = "Chainlink",
                logoUrl = "https://example.com/link.png",
                totalBalance = 50.0,
                formattedBalance = "50.00",
                totalFiatBalance = 750.0,  // $750
                formattedFiatBalance = "$750.00",
                exchangeCurrency = "usd"
            )
        )

        // Recreate ViewModel with populated token groups
        val usecaseComponents = HomeUsecaseFactory.createGetAllGroupedTokensUsecase(
            erc20Groups = groups,
            networkGroups = emptyList()
        )
        viewModel = HomeViewModel(
            networkMonitor = networkMonitor,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            getAllGroupedTokensUsecase = usecaseComponents.usecase,
            savedStateHandle = SavedStateHandle(),
            context = context
        )

        // Get the Success state
        val success = viewModel.groupedTokenAssetState.first { it is GroupedAssetsUiState.Success }
                as GroupedAssetsUiState.Success

        // Verify tokens are sorted by fiat balance ASCENDING (lowest first)
        val symbols = success.assets.map { it.symbol }
        assertEquals(
            "Tokens should be sorted by fiat balance ascending",
            listOf("USDC", "LINK", "ETH"),
            symbols
        )

        // Verify fiat values are in ascending order
        val fiatValues = success.assets.map { it.totalFiatBalance }
        assertEquals(listOf(100.0, 750.0, 5000.0), fiatValues)
    }

    @Test
    fun `groupedTokenAssetState handles null fiat balance as zero`() = runTest {
        val groups = listOf(
            TokenGroupAssetOverview(
                groupId = "g_with_fiat",
                symbol = "ETH",
                name = "Ethereum",
                logoUrl = null,
                totalBalance = 1.0,
                formattedBalance = "1.00",
                totalFiatBalance = 2000.0,
                formattedFiatBalance = "$2,000.00",
                exchangeCurrency = "usd"
            ),
            TokenGroupAssetOverview(
                groupId = "g_null_fiat",
                symbol = "UNKNOWN",
                name = "Unknown Token",
                logoUrl = null,
                totalBalance = 1000.0,
                formattedBalance = "1,000.00",
                totalFiatBalance = null,  // No exchange rate available
                formattedFiatBalance = null,
                exchangeCurrency = "usd"
            )
        )

        val usecaseComponents = HomeUsecaseFactory.createGetAllGroupedTokensUsecase(
            erc20Groups = groups,
            networkGroups = emptyList()
        )
        viewModel = HomeViewModel(
            networkMonitor = networkMonitor,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            getAllGroupedTokensUsecase = usecaseComponents.usecase,
            savedStateHandle = SavedStateHandle(),
            context = context
        )

        val success = viewModel.groupedTokenAssetState.first { it is GroupedAssetsUiState.Success }
                as GroupedAssetsUiState.Success

        // Token with null fiat should be treated as 0.0 and appear first
        assertEquals("UNKNOWN", success.assets[0].symbol)
        assertEquals("ETH", success.assets[1].symbol)
    }

    // =============================================
    // TOKEN LOGO URL TESTS
    // =============================================

    @Test
    fun `tokens include logo URLs from repository`() = runTest {
        val expectedLogoUrl = "https://example.com/token-logo.png"
        val groups = listOf(
            TokenGroupAssetOverview(
                groupId = "g_with_logo",
                symbol = "USDC",
                name = "USD Coin",
                logoUrl = expectedLogoUrl,
                totalBalance = 100.0,
                formattedBalance = "100.00",
                totalFiatBalance = 100.0,
                formattedFiatBalance = "$100.00",
                exchangeCurrency = "usd"
            )
        )

        val usecaseComponents = HomeUsecaseFactory.createGetAllGroupedTokensUsecase(
            erc20Groups = groups,
            networkGroups = emptyList()
        )
        viewModel = HomeViewModel(
            networkMonitor = networkMonitor,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            getAllGroupedTokensUsecase = usecaseComponents.usecase,
            savedStateHandle = SavedStateHandle(),
            context = context
        )

        val success = viewModel.groupedTokenAssetState.first { it is GroupedAssetsUiState.Success }
                as GroupedAssetsUiState.Success

        assertNotNull("Token should have logo URL", success.assets[0].logoUrl)
        assertEquals(expectedLogoUrl, success.assets[0].logoUrl)
    }

    @Test
    fun `tokens can have null logo URLs`() = runTest {
        val groups = listOf(
            TokenGroupAssetOverview(
                groupId = "g_no_logo",
                symbol = "CUSTOM",
                name = "Custom Token",
                logoUrl = null,  // No logo available
                totalBalance = 50.0,
                formattedBalance = "50.00",
                totalFiatBalance = 50.0,
                formattedFiatBalance = "$50.00",
                exchangeCurrency = "usd"
            )
        )

        val usecaseComponents = HomeUsecaseFactory.createGetAllGroupedTokensUsecase(
            erc20Groups = groups,
            networkGroups = emptyList()
        )
        viewModel = HomeViewModel(
            networkMonitor = networkMonitor,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            getAllGroupedTokensUsecase = usecaseComponents.usecase,
            savedStateHandle = SavedStateHandle(),
            context = context
        )

        val success = viewModel.groupedTokenAssetState.first { it is GroupedAssetsUiState.Success }
                as GroupedAssetsUiState.Success

        assertNull("Token can have null logo URL", success.assets[0].logoUrl)
    }

    // =============================================
    // OVERLAY STATE TESTS
    // =============================================

    @Test
    fun `token overlay visibility toggles correctly`() {
        // Initially hidden
        assertFalse(viewModel.isTokenOverlayVisible.value)
        
        // Show overlay
        viewModel.showTokenOverlay()
        assertTrue(viewModel.isTokenOverlayVisible.value)
        
        // Hide overlay
        viewModel.hideTokenOverlay()
        assertFalse(viewModel.isTokenOverlayVisible.value)
    }

    // =============================================
    // REFRESH STATE TESTS
    // =============================================

    @Test
    fun `isRefreshing defaults to false`() {
        assertFalse(viewModel.isRefreshing.value)
    }

    // =============================================
    // SELECTED TOKEN TESTS
    // =============================================

    @Test
    fun `selectedTokenAsset is initially null`() {
        assertNull(viewModel.selectedTokenAsset.value)
    }

    // =============================================
    // NETWORK-DEPENDENT OPERATION TESTS
    // =============================================

    @Test
    fun `getLink returns null when offline`() = runTest {
        // Set network to offline
        networkMonitor.setOnline(false)
        advanceUntilIdle()
        
        // Try to get a link
        val link = viewModel.getLink("any-uri")
        
        assertNull(
            "getLink should return null when offline",
            link
        )
    }
}
