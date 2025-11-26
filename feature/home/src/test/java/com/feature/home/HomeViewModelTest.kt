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
import com.feature.home.GroupedAssetsUiState
import com.feature.home.WalletDataUiState
import com.feature.home.HomeViewModel
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
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var networkMonitor: FakeNetworkMonitor
    private lateinit var userDataRepository: FakeUserDataRepository
    private lateinit var transferRepository: FakeTransferRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        networkMonitor = FakeNetworkMonitor(initialIsOnline = true)
        userDataRepository = FakeUserDataRepository(
            initialUserData = UserData(
                walletAddress = "0xabc",
                walletNetwork = "ETH",
                isFirstBoot = false,
                preferredCurrency = "usd"
            )
        )
        transferRepository = FakeTransferRepository(initialHasTransfers = false)

        val usecaseComponents = HomeUsecaseFactory.createGetAllGroupedTokensUsecase(
            erc20Groups = emptyList(),
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
    }

    @Test
    fun isOffline_initiallyReflectsOnlineState_andUpdates() = runTest {
        // Initially online -> isOffline should become false after collection starts
        advanceUntilIdle()
        assertFalse(viewModel.isOffline.value)

        // Flip to offline -> isOffline true
        networkMonitor.setOnline(false)
        advanceUntilIdle()
        assertTrue(viewModel.isOffline.value)

        // Back online -> isOffline false
        networkMonitor.setOnline(true)
        advanceUntilIdle()
        assertFalse(viewModel.isOffline.value)
    }

    @Test
    fun hasTransfers_reflectsRepositoryFlow() = runTest {
        // WhileSubscribed flow: must collect to activate upstream
        val initial = viewModel.hasTransfers.first()
        assertFalse(initial)
        transferRepository.setHasTransfers(true)
        val updated = viewModel.hasTransfers.first { it }
        assertTrue(updated)
    }

    @Test
    fun walletDataState_emitsSuccessWithInitialUserData_andUpdatesOnChange() = runTest {
        val success = viewModel.walletDataState.first { it is WalletDataUiState.Success } as WalletDataUiState.Success
        assertEquals("0xabc", success.userData.walletAddress)
        assertEquals("ETH", success.userData.walletNetwork)
        assertEquals("usd", success.userData.preferredCurrency)
        assertFalse(success.userData.isFirstBoot)

        userDataRepository.setPreferredCurrency("eur")
        val updated = viewModel.walletDataState.first { it is WalletDataUiState.Success } as WalletDataUiState.Success
        assertEquals("eur", updated.userData.preferredCurrency)
    }

    @Test
    fun groupedTokenAssetState_emitsEmptyWhenUsecaseReturnsEmpty() = runTest {
        val state = viewModel.groupedTokenAssetState.first { it !is GroupedAssetsUiState.Loading }
        assertTrue(state is GroupedAssetsUiState.Empty)
    }

    @Test
    fun groupedTokenAssetState_emitsSuccess_sortedByTotalFiatBalanceAscending() = runTest {
        // Recreate VM with pre-populated groups
        val groups = listOf(
            // totalFiatBalance: 10.0
            TokenGroupAssetOverview(
                groupId = "g1",
                symbol = "AAA",
                name = "Group A",
                logoUrl = null,
                totalBalance = 1.0,
                formattedBalance = "1",
                totalFiatBalance = 10.0,
                formattedFiatBalance = "10.00",
                exchangeCurrency = "usd"
            ),
            // totalFiatBalance: 5.0
            TokenGroupAssetOverview(
                groupId = "g2",
                symbol = "BBB",
                name = "Group B",
                logoUrl = null,
                totalBalance = 1.0,
                formattedBalance = "1",
                totalFiatBalance = 5.0,
                formattedFiatBalance = "5.00",
                exchangeCurrency = "usd"
            ),
            // totalFiatBalance: null -> treated as 0.0 in sort key
            TokenGroupAssetOverview(
                groupId = "g3",
                symbol = "CCC",
                name = "Group C",
                logoUrl = null,
                totalBalance = 1.0,
                formattedBalance = "1",
                totalFiatBalance = null,
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

        val success = viewModel.groupedTokenAssetState.first { it is GroupedAssetsUiState.Success } as GroupedAssetsUiState.Success
        val namesInOrder = success.assets.map { it.name }
        // Sorted ascending by (totalFiatBalance ?: 0.0) => C (0.0), B (5.0), A (10.0)
        assertEquals(listOf("Group C", "Group B", "Group A"), namesInOrder)
    }

    @Test
    fun overlayVisibility_toggleWorks() {
        assertFalse(viewModel.isTokenOverlayVisible.value)
        viewModel.showTokenOverlay()
        assertTrue(viewModel.isTokenOverlayVisible.value)
        viewModel.hideTokenOverlay()
        assertFalse(viewModel.isTokenOverlayVisible.value)
    }

    @Test
    fun refreshing_defaultFalse() {
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun selectedTokenAsset_initiallyNull() {
        assertNull(viewModel.selectedTokenAsset.value)
    }

    @Test
    fun getLink_offline_returnsNull() = runTest {
        networkMonitor.setOnline(false)
        val link = viewModel.getLink("any-uri")
        assertNull(link)
    }
}


