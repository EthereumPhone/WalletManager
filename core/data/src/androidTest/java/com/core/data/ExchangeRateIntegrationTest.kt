package com.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.data.remote.RetrofitTokenPrice
import com.core.data.remote.SponsorshipPriceDataSource
import com.core.data.remote.TokenPriceDataSource
import com.core.data.repository.DefaultExchangeRepository
import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.Web3jNetworkBalanceRepository
import com.core.database.WmDatabase
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenExchangeDao
import com.core.database.dao.TokenGroupDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenExchangeEntity
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenBalance
import com.core.model.TokenGroupAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.TokenMetadata
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

/**
 * Integration-style tests against the real token price API to verify
 * we can fetch latest USD prices and compute fiat amounts end-to-end.
 *
 * Notes:
 * - Requires valid API keys in local.properties (ALCHEMY_API, etc.)
 * - Requires network access during instrumentation tests.
 */
@RunWith(AndroidJUnit4::class)
class ExchangeRateIntegrationTest {

    private lateinit var db: WmDatabase
    private lateinit var tokenBalanceDao: TokenBalanceDao
    private lateinit var tokenExchangeDao: TokenExchangeDao
    private lateinit var tokenGroupDao: TokenGroupDao
    private lateinit var tokenMetadataDao: TokenMetadataDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, WmDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tokenBalanceDao = db.tokenBalanceDao
        tokenExchangeDao = db.tokenExchangeDao
        tokenGroupDao = db.tokenGroupDao
        tokenMetadataDao = db.tokenMetadataDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun liveAlchemyEthUsd_isPositive() = runBlocking {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val http = OkHttpClient()
        val priceApi = RetrofitTokenPrice(http, moshi)

        val prices = priceApi.fetchTokenPriceBySymbols(listOf("ETH"))
        val usd = prices.first().prices.first { it.currency.equals("usd", ignoreCase = true) }.value.toDouble()

        assertTrue("ETH/USD should be > 0", usd > 0.0)
    }

    @Test
    fun ethFiat_isBalanceTimesLatestUsd_fromLivePrice() = runBlocking {
        // 1) Seed a known ETH network balance (2.0 ETH) into Room.
        tokenBalanceDao.upsertTokenBalances(
            listOf(
                TokenBalanceEntity(
                    contractAddress = "1", // network token stored under chainId string
                    chainId = 1,
                    tokenBalance = BigDecimal("2.0") // already in ETH units for network balances
                )
            )
        )

        // 2) Fetch latest ETH USD price from Alchemy API.
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val http = OkHttpClient()
        val priceApi = RetrofitTokenPrice(http, moshi)
        val prices = priceApi.fetchTokenPriceBySymbols(listOf("ETH"))
        val usd = prices.first().prices.first { it.currency.equals("usd", ignoreCase = true) }.value.toDouble()
        assertTrue("ETH/USD should be > 0", usd > 0.0)

        // 3) Insert fetched price as the latest exchange into Room.
        tokenExchangeDao.insertAllExchanges(
            listOf(
                TokenExchangeEntity(
                    address = null,
                    symbol = "ETH",
                    chainId = null,
                    currency = "usd",
                    value = usd,
                    timestamp = Clock.System.now()
                )
            )
        )

        // 4) Use the repository that multiplies total network balance by the latest USD price.
        val dummyExchangeRepo = DefaultExchangeRepository(
            tokenPriceDataSource = object : TokenPriceDataSource {
                override suspend fun fetchTokenPriceBySymbols(symbols: List<String>) =
                    emptyList<com.core.data.model.dto.NetworkTokenExchange>()

                override suspend fun fetchTokenPriceByAddresses(addresses: List<com.core.data.model.dto.TokenAddress>) =
                    com.core.data.model.dto.TokenPricesResponse(emptyList(), null)
            },
            sponsorshipPriceDataSource = object : SponsorshipPriceDataSource {
                override suspend fun fetchPricesByAddresses(addresses: List<String>) =
                    com.core.data.remote.SponsorshipPricesResponse(emptyList())
            },
            exchangeDao = tokenExchangeDao,
            tokenBalanceRepository = object : TokenBalanceRepository {
                override fun getTokens(): Flow<List<TokenAsset>> = kotlinx.coroutines.flow.flowOf(emptyList())
                override fun observeBalancesWithoutMetadata(): Flow<List<com.core.database.model.erc20.TokenBalanceEntity>> =
                    kotlinx.coroutines.flow.flowOf(emptyList())
                override fun getCombinedTokens(): Flow<List<TokenAsset>> = kotlinx.coroutines.flow.flowOf(emptyList())
                override fun getTokensBalances(): Flow<List<TokenBalance>> = kotlinx.coroutines.flow.flowOf(emptyList())
                override fun getTokensBalances(contractAddresses: List<String>): Flow<List<TokenBalance>> =
                    kotlinx.coroutines.flow.flowOf(emptyList())
                override fun getTokensBalances(chainId: Int): Flow<List<TokenBalance>> =
                    kotlinx.coroutines.flow.flowOf(emptyList())
                override suspend fun refreshTokensBalances(toAddress: String) { }
                override suspend fun refreshTokensBalancesByNetwork(toAddress: String, chainId: Int) { }
            },
            groupedTokenRepository = object : GroupedTokenRepository {
                override fun observeGroupedTokensOverview(filterList: List<String>?): Flow<List<TokenGroupAssetOverview>> =
                    kotlinx.coroutines.flow.flowOf(emptyList())
                override fun observeGroupedTokens(): Flow<List<TokenGroupAsset>> =
                    kotlinx.coroutines.flow.flowOf(emptyList())
                override fun observeAllTokensWithPriceInGroup(groupId: String, filterZeroBalance: Boolean) =
                    kotlinx.coroutines.flow.flowOf(emptyList<com.core.model.TokenAssetWithPrice>())
            },
            tokenBalanceDao = tokenBalanceDao,
            tokenMetadataRepository = object : com.core.data.repository.TokenMetadataRepository {
                override fun getTokensMetadata(): Flow<List<TokenMetadata>> = kotlinx.coroutines.flow.flowOf(emptyList())
                override fun getTokensMetadata(contractAddresses: List<String>): Flow<List<TokenMetadata>> =
                    kotlinx.coroutines.flow.flowOf(emptyList())
                override fun getTokensMetadataBySymbols(symbols: List<String>): Flow<List<TokenMetadata>> =
                    kotlinx.coroutines.flow.flowOf(emptyList())
                override fun getTokensMetadata(chainId: Int): Flow<List<TokenMetadata>> =
                    kotlinx.coroutines.flow.flowOf(emptyList())
                override suspend fun refreshTokensMetadata(contractAddresses: List<String>, chainId: Int) { }
                override suspend fun refreshTokensMetadataByNetwork(contractAddresses: List<String>, network: NetworkChain) { }
                override suspend fun insertTokenMetadata(tokensMetadata: List<com.core.database.model.erc20.TokenMetadataEntity>) { }
                override suspend fun reconcileTokenGroups() { }
            },
            tokenGroupDao = tokenGroupDao
        )

        val networkRepo = Web3jNetworkBalanceRepository(
            networkBalanceApi = com.core.data.remote.NetworkBalanceApi(),
            tokenBalanceDao = tokenBalanceDao,
            tokenGroupDao = tokenGroupDao,
            tokenMetadataDao = tokenMetadataDao,
            tokenExchangeRepository = dummyExchangeRepo,
            tokenExchangeDao = tokenExchangeDao
        )

        val groups = networkRepo.getGroupedNetworkTokensOverview().first()
        val ethGroup = groups.first { it.groupId == "network_eth" }

        val expectedFiat = 2.0 * usd
        assertEquals(expectedFiat, ethGroup.totalFiatBalance!!, 0.000001)
    }
}





