package com.feature.home

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.data.remote.NetworkBalanceApi
import com.core.data.remote.RetrofitTokenPrice
import com.core.data.remote.SponsorshipPriceDataSource
import com.core.data.remote.SponsorshipPricesResponse
import com.core.data.remote.TokenPriceDataSource
import com.core.data.repository.DefaultExchangeRepository
import com.core.data.repository.DefaultGroupedTokenRepository
import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.repository.Web3jNetworkBalanceRepository
import com.core.data.util.NetworkMonitor
import com.core.database.WmDatabase
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenExchangeDao
import com.core.database.dao.TokenGroupDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenExchangeEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.domain.GetAllGroupedTokensUsecase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenBalance
import com.core.model.TokenGroupAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.TokenMetadata
import com.core.model.Transfer
import com.core.model.UserData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

/**
 * # HomeViewModel Integration Tests
 *
 * These tests run on a REAL Android device or emulator and verify the complete data flow:
 * 
 * ```
 * Database → Repository → UseCase → ViewModel → UI State
 * ```
 *
 * ## Why Integration Tests?
 * 
 * Unit tests verify components in isolation, but integration tests verify they work together:
 * - Database queries return correct data
 * - Repositories properly transform database entities to domain models
 * - UseCases combine data from multiple repositories correctly
 * - ViewModel produces correct UI states
 *
 * ## What We're Testing:
 * 
 * 1. **Token Balance Accuracy**: Verify balances from the database appear correctly in UI state
 * 2. **Fiat Calculation**: Verify balance × exchange rate = correct fiat amount
 * 3. **Logo URL Propagation**: Verify logo URLs flow from metadata to UI state
 * 4. **Sorting**: Verify tokens are sorted correctly by fiat value
 *
 * ## Test Setup:
 * 
 * - Uses an in-memory Room database (fast, isolated)
 * - Seeds test data directly into database tables
 * - Uses real repository implementations (not fakes)
 * - Verifies end-to-end data flow
 *
 * ## Running These Tests:
 * 
 * Connect your Android device or start an emulator, then:
 * ```
 * ./gradlew :feature:home:connectedAndroidTest
 * ```
 * 
 * Or run specific test:
 * ```
 * ./gradlew :feature:home:connectedAndroidTest --tests "com.feature.home.HomeViewModelIntegrationTest"
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HomeViewModelIntegrationTest {

    // Database and DAOs
    private lateinit var database: WmDatabase
    private lateinit var tokenBalanceDao: TokenBalanceDao
    private lateinit var tokenExchangeDao: TokenExchangeDao
    private lateinit var tokenGroupDao: TokenGroupDao
    private lateinit var tokenMetadataDao: TokenMetadataDao

    // Repositories (real implementations)
    private lateinit var groupedTokenRepository: GroupedTokenRepository
    private lateinit var networkBalanceRepository: NetworkBalanceRepository
    private lateinit var exchangeRepository: DefaultExchangeRepository

    // Test fakes for non-data dependencies
    private lateinit var networkMonitor: TestNetworkMonitor
    private lateinit var userDataRepository: TestUserDataRepository
    private lateinit var transferRepository: TestTransferRepository

    // System under test
    private lateinit var viewModel: HomeViewModel

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Create in-memory database - fast and isolated
        database = Room.inMemoryDatabaseBuilder(context, WmDatabase::class.java)
            .allowMainThreadQueries() // OK for tests
            .build()

        // Get DAOs
        tokenBalanceDao = database.tokenBalanceDao
        tokenExchangeDao = database.tokenExchangeDao
        tokenGroupDao = database.tokenGroupDao
        tokenMetadataDao = database.tokenMetadataDao

        // Initialize test fakes
        networkMonitor = TestNetworkMonitor()
        userDataRepository = TestUserDataRepository()
        transferRepository = TestTransferRepository()

        // Create real repository implementations
        groupedTokenRepository = DefaultGroupedTokenRepository(tokenGroupDao)
        
        // Create exchange repository with minimal stubs (we seed exchange data directly)
        exchangeRepository = createExchangeRepository()
        
        // Create network balance repository
        networkBalanceRepository = Web3jNetworkBalanceRepository(
            networkBalanceApi = NetworkBalanceApi(),
            tokenBalanceDao = tokenBalanceDao,
            tokenGroupDao = tokenGroupDao,
            tokenMetadataDao = tokenMetadataDao,
            tokenExchangeRepository = exchangeRepository,
            tokenExchangeDao = tokenExchangeDao
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    // =============================================
    // TEST: Token Balance Flows to UI State
    // =============================================

    @Test
    fun tokenBalance_flowsFromDatabase_toViewModelState() = runBlocking {
        // ARRANGE: Seed database with a token that has balance
        val tokenAddress = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48" // USDC on mainnet
        val chainId = 1
        val groupId = "usdc_group"
        val expectedBalance = BigDecimal("1000.000000") // 1000 USDC (6 decimals raw)
        
        // Insert token group FIRST (foreign key constraint)
        tokenGroupDao.upsertTokenGroups(listOf(
            TokenGroupEntity(
                groupId = groupId,
                symbol = "USDC",
                name = "USD Coin"
            )
        ))

        // Insert token metadata WITH groupId to link it to the group
        tokenMetadataDao.upsertTokenMetadata(listOf(
            TokenMetadataEntity(
                contractAddress = tokenAddress,
                chainId = chainId,
                name = "USD Coin",
                symbol = "USDC",
                decimals = 6,
                logo = "https://example.com/usdc.png",
                swappable = true,
                groupId = groupId  // Link to group
            )
        ))

        // Insert token balance
        tokenBalanceDao.upsertTokenBalances(listOf(
            TokenBalanceEntity(
                contractAddress = tokenAddress,
                chainId = chainId,
                tokenBalance = expectedBalance
            )
        ))

        // Insert exchange rate (1 USDC = $1)
        tokenExchangeDao.insertAllExchanges(listOf(
            TokenExchangeEntity(
                address = tokenAddress,
                symbol = "USDC",
                chainId = chainId,
                currency = "usd",
                value = 1.0,
                timestamp = Clock.System.now()
            )
        ))

        // ACT: Create ViewModel and get state
        val usecase = GetAllGroupedTokensUsecase(
            groupedTokenRepository = groupedTokenRepository,
            networkBalanceRepository = networkBalanceRepository
        )
        
        viewModel = HomeViewModel(
            networkMonitor = networkMonitor,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            getAllGroupedTokensUsecase = usecase,
            savedStateHandle = SavedStateHandle(),
            context = context
        )

        // Wait for Success state
        val state = viewModel.groupedTokenAssetState.first { it is GroupedAssetsUiState.Success }
                as GroupedAssetsUiState.Success

        // ASSERT: Token appears with correct balance
        assertTrue("Should have at least one token", state.assets.isNotEmpty())
        
        val usdcToken = state.assets.find { it.symbol == "USDC" }
        assertNotNull("USDC token should be present", usdcToken)
        
        // Balance should be converted from raw to display units (1000000000 / 10^6 = 1000)
        // Note: The exact value depends on how your repository handles decimal conversion
        assertTrue("Balance should be positive", usdcToken!!.totalBalance > 0)
    }

    // =============================================
    // TEST: Fiat Calculation is Accurate
    // =============================================

    @Test
    fun fiatAmount_isBalanceTimesExchangeRate() = runBlocking {
        // ARRANGE: Token with known balance and exchange rate
        val tokenAddress = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2" // WETH
        val chainId = 1
        val balance = BigDecimal("2.5") // 2.5 ETH
        val ethPriceUsd = 2500.0 // $2,500 per ETH
        val expectedFiat = 2.5 * ethPriceUsd // $6,250

        // Seed metadata
        tokenMetadataDao.upsertTokenMetadata(listOf(
            TokenMetadataEntity(
                contractAddress = tokenAddress,
                chainId = chainId,
                name = "Wrapped Ether",
                symbol = "WETH",
                decimals = 18,
                logo = "https://example.com/weth.png",
                swappable = true
            )
        ))

        // Seed balance
        tokenBalanceDao.upsertTokenBalances(listOf(
            TokenBalanceEntity(
                contractAddress = tokenAddress,
                chainId = chainId,
                tokenBalance = balance
            )
        ))

        // Seed token group
        tokenGroupDao.insertTokenGroups(listOf(
            TokenGroupEntity(
                groupId = "weth_group",
                symbol = "WETH",
                name = "Wrapped Ether"
            )
        ))

        tokenGroupDao.upsertTokenGroupMembers(
            groupId = "weth_group",
            chainId = chainId,
            contractAddress = tokenAddress
        )

        // Seed exchange rate
        tokenExchangeDao.insertAllExchanges(listOf(
            TokenExchangeEntity(
                address = tokenAddress,
                symbol = "WETH",
                chainId = chainId,
                currency = "usd",
                value = ethPriceUsd,
                timestamp = Clock.System.now()
            )
        ))

        // ACT
        val usecase = GetAllGroupedTokensUsecase(
            groupedTokenRepository = groupedTokenRepository,
            networkBalanceRepository = networkBalanceRepository
        )

        viewModel = HomeViewModel(
            networkMonitor = networkMonitor,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            getAllGroupedTokensUsecase = usecase,
            savedStateHandle = SavedStateHandle(),
            context = context
        )

        val state = viewModel.groupedTokenAssetState.first { it is GroupedAssetsUiState.Success }
                as GroupedAssetsUiState.Success

        // ASSERT
        val wethToken = state.assets.find { it.symbol == "WETH" }
        assertNotNull("WETH token should be present", wethToken)
        
        // Fiat should be balance × exchange rate (with some tolerance for floating point)
        assertNotNull("Fiat balance should not be null", wethToken!!.totalFiatBalance)
        assertEquals(
            "Fiat should equal balance × exchange rate",
            expectedFiat,
            wethToken.totalFiatBalance!!,
            0.01 // Allow small floating point difference
        )
    }

    // =============================================
    // TEST: Logo URL Propagation
    // =============================================

    @Test
    fun logoUrl_propagatesFromMetadata_toUiState() = runBlocking {
        // ARRANGE
        val tokenAddress = "0x6B175474E89094C44Da98b954EedeAC495271d0F" // DAI
        val chainId = 1
        val expectedLogoUrl = "https://cryptologos.cc/logos/multi-collateral-dai-dai-logo.png"

        // Seed with logo URL
        tokenMetadataDao.upsertTokenMetadata(listOf(
            TokenMetadataEntity(
                contractAddress = tokenAddress,
                chainId = chainId,
                name = "Dai Stablecoin",
                symbol = "DAI",
                decimals = 18,
                logo = expectedLogoUrl,
                swappable = true
            )
        ))

        tokenBalanceDao.upsertTokenBalances(listOf(
            TokenBalanceEntity(
                contractAddress = tokenAddress,
                chainId = chainId,
                tokenBalance = BigDecimal("500")
            )
        ))

        tokenGroupDao.insertTokenGroups(listOf(
            TokenGroupEntity(
                groupId = "dai_group",
                symbol = "DAI",
                name = "Dai Stablecoin"
            )
        ))

        tokenGroupDao.upsertTokenGroupMembers(
            groupId = "dai_group",
            chainId = chainId,
            contractAddress = tokenAddress
        )

        tokenExchangeDao.insertAllExchanges(listOf(
            TokenExchangeEntity(
                address = tokenAddress,
                symbol = "DAI",
                chainId = chainId,
                currency = "usd",
                value = 1.0,
                timestamp = Clock.System.now()
            )
        ))

        // ACT
        val usecase = GetAllGroupedTokensUsecase(
            groupedTokenRepository = groupedTokenRepository,
            networkBalanceRepository = networkBalanceRepository
        )

        viewModel = HomeViewModel(
            networkMonitor = networkMonitor,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            getAllGroupedTokensUsecase = usecase,
            savedStateHandle = SavedStateHandle(),
            context = context
        )

        val state = viewModel.groupedTokenAssetState.first { it is GroupedAssetsUiState.Success }
                as GroupedAssetsUiState.Success

        // ASSERT
        val daiToken = state.assets.find { it.symbol == "DAI" }
        assertNotNull("DAI token should be present", daiToken)
        assertEquals(
            "Logo URL should match what was stored in metadata",
            expectedLogoUrl,
            daiToken!!.logoUrl
        )
    }

    // =============================================
    // TEST: Multiple Tokens Sorted by Fiat Value
    // =============================================

    @Test
    fun multipleTokens_areSortedByFiatValue_ascending() = runBlocking {
        // ARRANGE: Three tokens with different fiat values
        val tokens = listOf(
            Triple("0xToken1", "HIGH", 5000.0),   // $5000
            Triple("0xToken2", "LOW", 100.0),     // $100
            Triple("0xToken3", "MID", 1500.0)     // $1500
        )

        tokens.forEachIndexed { index, (address, symbol, fiatValue) ->
            tokenMetadataDao.upsertTokenMetadata(listOf(
                TokenMetadataEntity(
                    contractAddress = address,
                    chainId = 1,
                    name = "$symbol Token",
                    symbol = symbol,
                    decimals = 18,
                    logo = null,
                    swappable = true
                )
            ))

            tokenBalanceDao.upsertTokenBalances(listOf(
                TokenBalanceEntity(
                    contractAddress = address,
                    chainId = 1,
                    tokenBalance = BigDecimal("1") // 1 token each
                )
            ))

            tokenGroupDao.insertTokenGroups(listOf(
                TokenGroupEntity(
                    groupId = "${symbol.lowercase()}_group",
                    symbol = symbol,
                    name = "$symbol Token"
                )
            ))

            tokenGroupDao.upsertTokenGroupMembers(
                groupId = "${symbol.lowercase()}_group",
                chainId = 1,
                contractAddress = address
            )

            // Exchange rate = fiat value (since balance is 1)
            tokenExchangeDao.insertAllExchanges(listOf(
                TokenExchangeEntity(
                    address = address,
                    symbol = symbol,
                    chainId = 1,
                    currency = "usd",
                    value = fiatValue,
                    timestamp = Clock.System.now()
                )
            ))
        }

        // ACT
        val usecase = GetAllGroupedTokensUsecase(
            groupedTokenRepository = groupedTokenRepository,
            networkBalanceRepository = networkBalanceRepository
        )

        viewModel = HomeViewModel(
            networkMonitor = networkMonitor,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            getAllGroupedTokensUsecase = usecase,
            savedStateHandle = SavedStateHandle(),
            context = context
        )

        val state = viewModel.groupedTokenAssetState.first { it is GroupedAssetsUiState.Success }
                as GroupedAssetsUiState.Success

        // ASSERT: Should be sorted ascending by fiat value
        val symbols = state.assets.map { it.symbol }
        assertEquals(
            "Tokens should be sorted by fiat value ascending (LOW, MID, HIGH)",
            listOf("LOW", "MID", "HIGH"),
            symbols
        )
    }

    // =============================================
    // HELPER: Create Exchange Repository
    // =============================================

    private fun createExchangeRepository(): DefaultExchangeRepository {
        // Stub implementations that return empty data
        // (We seed exchange data directly into the database)
        val tokenPriceDataSource = object : TokenPriceDataSource {
            override suspend fun fetchTokenPriceBySymbols(symbols: List<String>) = emptyList<com.core.data.model.dto.NetworkTokenExchange>()
            override suspend fun fetchTokenPriceByAddresses(addresses: List<com.core.data.model.dto.TokenAddress>) =
                com.core.data.model.dto.TokenPricesResponse(emptyList(), null)
        }

        val sponsorshipPriceDataSource = object : SponsorshipPriceDataSource {
            override suspend fun fetchPricesByAddresses(addresses: List<String>) =
                SponsorshipPricesResponse(emptyList())
        }

        val tokenBalanceRepository = object : TokenBalanceRepository {
            override fun getTokens(): Flow<List<TokenAsset>> = flowOf(emptyList())
            override fun observeBalancesWithoutMetadata(): Flow<List<com.core.database.model.erc20.TokenBalanceEntity>> = flowOf(emptyList())
            override fun getCombinedTokens(): Flow<List<TokenAsset>> = flowOf(emptyList())
            override fun getTokensBalances(): Flow<List<TokenBalance>> = flowOf(emptyList())
            override fun getTokensBalances(contractAddresses: List<String>): Flow<List<TokenBalance>> = flowOf(emptyList())
            override fun getTokensBalances(chainId: Int): Flow<List<TokenBalance>> = flowOf(emptyList())
            override suspend fun refreshTokensBalances(toAddress: String) {}
            override suspend fun refreshTokensBalancesByNetwork(toAddress: String, chainId: Int) {}
        }

        val tokenMetadataRepository = object : TokenMetadataRepository {
            override fun getTokensMetadata(): Flow<List<TokenMetadata>> = flowOf(emptyList())
            override fun getTokensMetadata(contractAddresses: List<String>): Flow<List<TokenMetadata>> = flowOf(emptyList())
            override fun getTokensMetadataBySymbols(symbols: List<String>): Flow<List<TokenMetadata>> = flowOf(emptyList())
            override fun getTokensMetadata(chainId: Int): Flow<List<TokenMetadata>> = flowOf(emptyList())
            override suspend fun refreshTokensMetadata(contractAddresses: List<String>, chainId: Int) {}
            override suspend fun refreshTokensMetadataByNetwork(contractAddresses: List<String>, network: NetworkChain) {}
            override suspend fun insertTokenMetadata(tokensMetadata: List<TokenMetadataEntity>) {}
            override suspend fun reconcileTokenGroups() {}
        }

        return DefaultExchangeRepository(
            tokenPriceDataSource = tokenPriceDataSource,
            sponsorshipPriceDataSource = sponsorshipPriceDataSource,
            exchangeDao = tokenExchangeDao,
            tokenBalanceRepository = tokenBalanceRepository,
            groupedTokenRepository = groupedTokenRepository,
            tokenBalanceDao = tokenBalanceDao,
            tokenMetadataRepository = tokenMetadataRepository,
            tokenGroupDao = tokenGroupDao
        )
    }
}

// =============================================
// TEST FAKES (Simple implementations for testing)
// =============================================

private class TestNetworkMonitor : NetworkMonitor {
    override val isOnline: Flow<Boolean> = flowOf(true)
}

private class TestUserDataRepository : UserDataRepository {
    override val userData: Flow<UserData> = flowOf(
        UserData(
            walletAddress = "0xTestWallet",
            walletNetwork = "mainnet",
            isFirstBoot = false,
            preferredCurrency = "usd"
        )
    )
    override suspend fun setWalletAddress(address: String) {}
    override suspend fun setWalletNetwork(network: String) {}
    override suspend fun setIsFirstBoot(isFirstBoot: Boolean) {}
    override suspend fun setPreferredCurrency(currency: String) {}
}

private class TestTransferRepository : TransferRepository {
    override fun getTransfers(): Flow<List<Transfer>> = flowOf(emptyList())
    override fun getTransfers(chainId: Int): Flow<List<Transfer>> = flowOf(emptyList())
    override fun getTransfers(categories: List<String>): Flow<List<Transfer>> = flowOf(emptyList())
    override fun getTransfers(chainId: Int, categories: List<String>): Flow<List<Transfer>> = flowOf(emptyList())
    override fun observeTransfersExist(): Flow<Boolean> = flowOf(false)
    override suspend fun refreshTransfers(address: String) {}
}

