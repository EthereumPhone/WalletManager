package com.feature.swap

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.core.model.TokenAssetWithPrice
import com.core.model.TokenGroupAssetOverview
import com.core.model.UserData
import com.feature.swap.fakes.FakeSwapRepository
import com.feature.swap.fakes.FakeUserDataRepository
import com.feature.swap.fakes.SwapUsecaseFactory
import com.feature.swap.fakes.createNoopTerminalRepository
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
class SwapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var userDataRepository: FakeUserDataRepository
    private lateinit var swapRepository: FakeSwapRepository

    private lateinit var viewModel: SwapViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        userDataRepository = FakeUserDataRepository(
            initialUserData = UserData(
                walletAddress = "0xabc",
                walletNetwork = "8453",
                isFirstBoot = false,
                preferredCurrency = "usd"
            )
        )
        swapRepository = FakeSwapRepository()

        // Build use cases and repositories for token/grouped flows
        val tokensWithExchange = SwapUsecaseFactory.createGetAllTokensWithExchangeUsecase(
            erc20Groups = emptyList(),
            networkGroups = emptyList()
        )
        val allTokens = SwapUsecaseFactory.createGetAllTokensUsecase()
        val swapTokens = SwapUsecaseFactory.createGetSwapTokensUsecase()
        val queryByNetwork = SwapUsecaseFactory.createQueryTokenAssetsByNetworkUsecase()

        // Construct GetSwappableTokensForSelection manually to share the same grouped repo instance
        val getSwappableTokensForSelection = com.core.domain.GetSwappableTokensForSelection(
            getAllGroupedTokensUsecase = tokensWithExchange.getAllGroupedTokensUsecase,
            getSwapTokens = swapTokens.usecase,
            groupedTokenRepository = tokensWithExchange.groupedTokenRepository,
            userDataRepository = userDataRepository
        )

        viewModel = SwapViewModel(
            userDataRepository = userDataRepository,
            getSwapTokens = swapTokens.usecase,
            queryTokenAssetsByNetwork = queryByNetwork.usecase,
            swapRepository = swapRepository,
            savedStateHandle = SavedStateHandle(),
            getAllGroupedTokensUsecase = tokensWithExchange.getAllGroupedTokensUsecase,
            getAllTokensUsecase = allTokens.usecase,
            getAllTokensWithExchangeUsecase = tokensWithExchange.usecase,
            groupedTokenRepository = tokensWithExchange.groupedTokenRepository,
            getSwappableTokensForSelection = getSwappableTokensForSelection,
            terminalRepository = createNoopTerminalRepository(context)
        )
    }

    @Test
    fun initial_states_defaults() {
        // Token overlay defaults
        assertFalse(viewModel.isTokenOverlayVisible.value)
        assertEquals(TokenSelectionMode.None, viewModel.tokenSelectionMode.value)
        // Default chain selection is Base (8453)
        assertEquals(8453, viewModel.selectedTokenChainId.value)
        // SwapUI defaults
        val ui = viewModel.swapUIState.value
        assertEquals("FROM", ui.fromTitle)
        assertEquals("TO", ui.toTitle)
        assertFalse(ui.fromReadOnly)
        assertTrue(ui.toReadOnly)
        // High-level lists can be Loading or quickly become Empty depending on dispatcher timing
        val grouped = viewModel.groupedTokenAssetState.value
        assertTrue(grouped is GroupedAssetsUiState.Loading || grouped is GroupedAssetsUiState.Empty)
        val fromTokens = viewModel.fromTokensState.value
        assertTrue(fromTokens is FromTokensUiState.Loading || fromTokens is FromTokensUiState.Empty)
    }

    @Test
    fun setTokenSelectorChain_unsupported_showsToast_and_doesNotChangeSelection() {
        // 7777777 is Zora and not in supportedSwapChainIds
        viewModel.setTokenSelectorChain(7777777)
        assertEquals("Swaps on Zora are not supported yet.", viewModel.toastMessage.value)
        // chainId remains unchanged
        assertEquals(8453, viewModel.selectedTokenChainId.value)
    }

    @Test
    fun show_and_hide_token_overlay() {
        viewModel.showTokenOverlay(TokenSelectionMode.To)
        assertTrue(viewModel.isTokenOverlayVisible.value)
        assertEquals(TokenSelectionMode.To, viewModel.tokenSelectionMode.value)
        viewModel.hideTokenOverlay()
        assertFalse(viewModel.isTokenOverlayVisible.value)
        assertEquals(TokenSelectionMode.None, viewModel.tokenSelectionMode.value)
    }

    @Test
    fun auto_selects_default_from_token_when_grouped_assets_present() = runTest {
        // Rebuild viewModel with one grouped asset and tokens inside the group
        val groupId = "group_eth"
        val groups = listOf(
            TokenGroupAssetOverview(
                groupId = groupId,
                symbol = "ETH",
                name = "Ethereum",
                logoUrl = null,
                totalBalance = 1.0,
                formattedBalance = "1.0",
                totalFiatBalance = 5500.0,
                formattedFiatBalance = "5500.00",
                exchangeCurrency = "usd"
            )
        )
        val tokensWithExchange = SwapUsecaseFactory.createGetAllTokensWithExchangeUsecase(
            erc20Groups = groups,
            networkGroups = emptyList()
        )
        val allTokens = SwapUsecaseFactory.createGetAllTokensUsecase()
        val swapTokens = SwapUsecaseFactory.createGetSwapTokensUsecase()
        val queryByNetwork = SwapUsecaseFactory.createQueryTokenAssetsByNetworkUsecase()

        val getSwappableTokensForSelection = com.core.domain.GetSwappableTokensForSelection(
            getAllGroupedTokensUsecase = tokensWithExchange.getAllGroupedTokensUsecase,
            getSwapTokens = swapTokens.usecase,
            groupedTokenRepository = tokensWithExchange.groupedTokenRepository,
            userDataRepository = userDataRepository
        )

        // Emit tokens before creating VM so default selection can use real data (avoids fallback)
        tokensWithExchange.groupedTokenRepository.emitTokensForGroup(
            groupId,
            listOf(
                TokenAssetWithPrice(
                    address = "1", // native Base ETH encoded as "1"? we keep actual address irrelevant here
                    chainId = 8453,
                    symbol = "ETH",
                    name = "Ethereum",
                    balance = 1.0,
                    decimals = 18,
                    logoUrl = null,
                    swappable = true,
                    fiatAmount = 5500.0
                ),
                TokenAssetWithPrice(
                    address = "0xEth",
                    chainId = 1,
                    symbol = "ETH",
                    name = "Ethereum",
                    balance = 1.0,
                    decimals = 18,
                    logoUrl = null,
                    swappable = true,
                    fiatAmount = 5000.0
                )
            )
        )

        val vm = SwapViewModel(
            userDataRepository = userDataRepository,
            getSwapTokens = swapTokens.usecase,
            queryTokenAssetsByNetwork = queryByNetwork.usecase,
            swapRepository = swapRepository,
            savedStateHandle = SavedStateHandle(),
            getAllGroupedTokensUsecase = tokensWithExchange.getAllGroupedTokensUsecase,
            getAllTokensUsecase = allTokens.usecase,
            getAllTokensWithExchangeUsecase = tokensWithExchange.usecase,
            groupedTokenRepository = tokensWithExchange.groupedTokenRepository,
            getSwappableTokensForSelection = getSwappableTokensForSelection,
            terminalRepository = createNoopTerminalRepository(context)
        )

        // Wait for default selection to happen
        advanceUntilIdle()

        val from = vm.swapUIState.value.fromToken
        assertNotNull(from)
        assertEquals("ETH", from!!.token.symbol)
        // Should pick the highest fiat amount token (Base in this setup)
        assertEquals(8453, from.token.chainId)
    }

    @Test
    fun selectToTokenAsset_rejects_invalid_cross_chain_pair() = runTest {
        // Prepare a VM with default FROM set to ETH on Base
        val groupId = "group_eth"
        val groups = listOf(
            TokenGroupAssetOverview(
                groupId = groupId,
                symbol = "ETH",
                name = "Ethereum",
                logoUrl = null,
                totalBalance = 1.0,
                formattedBalance = "1.0",
                totalFiatBalance = 5500.0,
                formattedFiatBalance = "5500.00",
                exchangeCurrency = "usd"
            )
        )
        val tokensWithExchange = SwapUsecaseFactory.createGetAllTokensWithExchangeUsecase(
            erc20Groups = groups,
            networkGroups = emptyList()
        )
        val allTokens = SwapUsecaseFactory.createGetAllTokensUsecase()
        val swapTokens = SwapUsecaseFactory.createGetSwapTokensUsecase()
        val queryByNetwork = SwapUsecaseFactory.createQueryTokenAssetsByNetworkUsecase()

        val getSwappableTokensForSelection = com.core.domain.GetSwappableTokensForSelection(
            getAllGroupedTokensUsecase = tokensWithExchange.getAllGroupedTokensUsecase,
            getSwapTokens = swapTokens.usecase,
            groupedTokenRepository = tokensWithExchange.groupedTokenRepository,
            userDataRepository = userDataRepository
        )

        // Emit tokens before creating VM so default selection can use real data
        tokensWithExchange.groupedTokenRepository.emitTokensForGroup(
            groupId,
            listOf(
                TokenAssetWithPrice(
                    address = "1",
                    chainId = 8453,
                    symbol = "ETH",
                    name = "Ethereum",
                    balance = 1.0,
                    decimals = 18,
                    logoUrl = null,
                    swappable = true,
                    fiatAmount = 5500.0
                )
            )
        )

        val vm = SwapViewModel(
            userDataRepository = userDataRepository,
            getSwapTokens = swapTokens.usecase,
            queryTokenAssetsByNetwork = queryByNetwork.usecase,
            swapRepository = swapRepository,
            savedStateHandle = SavedStateHandle(),
            getAllGroupedTokensUsecase = tokensWithExchange.getAllGroupedTokensUsecase,
            getAllTokensUsecase = allTokens.usecase,
            getAllTokensWithExchangeUsecase = tokensWithExchange.usecase,
            groupedTokenRepository = tokensWithExchange.groupedTokenRepository,
            getSwappableTokensForSelection = getSwappableTokensForSelection,
            terminalRepository = createNoopTerminalRepository(context)
        )

        advanceUntilIdle()
        assertNotNull(vm.swapUIState.value.fromToken)

        // Try to select a different token on a different chain (invalid cross-chain pair)
        vm.showTokenOverlay(TokenSelectionMode.To)
        vm.selectToTokenAsset(
            TokenAssetWithPrice(
                address = "0xUSDC",
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
        advanceUntilIdle()

        // Should reject and keep TO unselected
        assertNull(vm.swapUIState.value.toToken)
        assertEquals("Cross-chain swaps not supported", vm.toastMessage.value)
        assertFalse(vm.isTokenOverlayVisible.value)
    }
}
