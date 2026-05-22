package com.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.util.BigDecimalTypeConverter
import com.core.database.util.Erc1155MetadataConverter
import com.core.database.util.MoshiJsonConverter
import com.core.database.util.RawContractConverter
import com.core.database.util.TokenSeedingHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal

/**
 * End-to-end reproduction for the report:
 *   "user 0x31145208... cannot see their USDC balance on Base, even after clearing app data."
 *
 * This runs the REAL pipeline against a real (in-memory) Room DB:
 *   1. TokenSeedingHelper.seedTokensDirectly() with the real tokens_uniswap_org.json
 *   2. insert the user's REAL on-chain balances (fetched via Alchemy)
 *   3. query the REAL home query: TokenGroupDao.getActiveCompositeTokenGroups()
 *
 * Clearing app data wipes the DB and forces a fresh reseed, so if the bug reproduces it must
 * live in this deterministic path. The test asserts a USDC group is present (i.e. the home
 * carousel would render a USDC card).
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class UsdcBaseSeedingReproTest {

    private lateinit var db: WmDatabase

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonConverter = MoshiJsonConverter(moshi)
        db = Room.inMemoryDatabaseBuilder(ctx, WmDatabase::class.java)
            .addTypeConverter(Erc1155MetadataConverter(jsonConverter))
            .addTypeConverter(RawContractConverter(jsonConverter))
            .addTypeConverter(BigDecimalTypeConverter())
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun usdcOnBase_isVisibleAfterRealSeeding() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()

        // 1. Real seeding (this is exactly what the Room onCreate/onOpen callback runs).
        TokenSeedingHelper.seedTokensDirectly(ctx, db.openHelper.writableDatabase)

        // 2. The user's real holdings (raw integer balances, exactly as Alchemy returns them).
        db.tokenBalanceDao.upsertTokenBalances(
            listOf(
                TokenBalanceEntity("0x833589fcd6edb6e08f4c7c32d4f71b54bda02913", 8453, BigDecimal("10275243")),     // USDC @ Base
                TokenBalanceEntity("0xaf88d065e77c8cc2239327c5edb3a432268e5831", 42161, BigDecimal("20000")),       // USDC @ Arbitrum
                TokenBalanceEntity("0x912ce59144191c1204e64559fe8253a0e49e6548", 42161, BigDecimal("1100000000000000000")), // ARB
                TokenBalanceEntity("0x4ed4e862860bed51a9570b96d89af5e1b0efefed", 8453, BigDecimal("130550128132129186"))    // DEGEN @ Base
            )
        )

        // 3. The real home query that feeds the token carousel.
        val activeGroups = db.tokenGroupDao.getActiveCompositeTokenGroups().first()
        val symbols = activeGroups.map { it.tokenGroup.symbol }
        println("ACTIVE GROUPS (home cards) = $symbols")

        // Diagnostics if it fails:
        val usdcMeta = db.tokenMetadataDao.getTokenMetadataByAddressAndChainId(
            "0x833589fcd6edb6e08f4c7c32d4f71b54bda02913", 8453
        )
        println("USDC@Base metadata = $usdcMeta")

        assertTrue(
            "Expected a USDC group in the active (home) groups but got $symbols",
            symbols.any { it.equals("USDC", ignoreCase = true) }
        )
    }

    /**
     * The carousel does NOT render `getActiveCompositeTokenGroups` directly — it renders
     * `observeAllActiveTokenGroupsWithExchange()` mapped to overviews, then filters
     * `totalBalance > 0.0` (HomeCardCarousel.kt). totalBalance comes from a DIFFERENT query
     * (getTokensInGroupWithLatestExchange) whose balance is mapped via @Embedded(prefix="balance_").
     * This test exercises that exact path — the one the user actually sees.
     */
    @Test
    fun usdcOnBase_survivesTheRealCarouselPath() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        TokenSeedingHelper.seedTokensDirectly(ctx, db.openHelper.writableDatabase)
        db.tokenBalanceDao.upsertTokenBalances(
            listOf(
                TokenBalanceEntity("0x833589fcd6edb6e08f4c7c32d4f71b54bda02913", 8453, BigDecimal("10275243")),
                TokenBalanceEntity("0xaf88d065e77c8cc2239327c5edb3a432268e5831", 42161, BigDecimal("20000")),
                TokenBalanceEntity("0x912ce59144191c1204e64559fe8253a0e49e6548", 42161, BigDecimal("1100000000000000000")),
                TokenBalanceEntity("0x4ed4e862860bed51a9570b96d89af5e1b0efefed", 8453, BigDecimal("130550128132129186"))
            )
        )

        // The exact flow the home screen collects.
        val groups = db.tokenGroupDao.observeAllActiveTokenGroupsWithExchange().first()
        groups.forEach { g ->
            println("group=${g.tokenGroup.symbol} totalBalance=${g.totalBalance} activeChains=${g.activeChainIds} members=${g.tokensWithExchange.size}")
        }

        // Replicate the carousel filter exactly (HomeCardCarousel.kt:232).
        val visibleSymbols = groups
            .filter { it.totalBalance.toDouble() > 0.0 }
            .map { it.tokenGroup.symbol }
        println("VISIBLE CAROUSEL CARDS = $visibleSymbols")

        val usdc = groups.firstOrNull { it.tokenGroup.symbol.equals("USDC", ignoreCase = true) }
        assertTrue("USDC group missing from observeAllActiveTokenGroupsWithExchange()", usdc != null)
        assertTrue(
            "USDC totalBalance should be ~10.295243 but was ${usdc?.totalBalance}",
            usdc!!.totalBalance.toDouble() > 0.0
        )
        assertTrue(
            "USDC filtered out of carousel (totalBalance > 0 filter). Visible: $visibleSymbols",
            visibleSymbols.any { it.equals("USDC", ignoreCase = true) }
        )
    }
}
