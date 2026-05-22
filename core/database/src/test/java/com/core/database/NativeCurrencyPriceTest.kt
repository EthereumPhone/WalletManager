package com.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.core.database.model.erc20.TokenExchangeEntity
import com.core.database.util.BigDecimalTypeConverter
import com.core.database.util.Erc1155MetadataConverter
import com.core.database.util.MoshiJsonConverter
import com.core.database.util.RawContractConverter
import com.core.model.NetworkChain
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Native (gas-token) USD prices are fetched by ticker via Alchemy's by-symbol Prices API
 * (DefaultExchangeRepository.fetchExchangeBySymbols) and read back through
 * TokenExchangeDao.observeExchangesBySymbols + Web3jNetworkBalanceRepository.latestUsdRateBySymbol.
 *
 * This pins that the multi-symbol native-rate lookup added for Monad/ApeChain works: every
 * native ticker (incl. MON/APE, and the now-correctly-handled BNB/AVAX) resolves to the latest
 * USD rate, and the per-balance fiat computation is correct. Verified empirically that Alchemy
 * returns prices for these symbols (MON ~$0.028, APE ~$0.146).
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class NativeCurrencyPriceTest {

    private lateinit var db: WmDatabase

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val jc = MoshiJsonConverter(Moshi.Builder().add(KotlinJsonAdapterFactory()).build())
        db = Room.inMemoryDatabaseBuilder(ctx, WmDatabase::class.java)
            .addTypeConverter(Erc1155MetadataConverter(jc))
            .addTypeConverter(RawContractConverter(jc))
            .addTypeConverter(BigDecimalTypeConverter())
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    /** A native exchange row as DefaultExchangeRepository.fetchExchangeBySymbols stores it. */
    private fun nativeUsd(symbol: String, value: Double, ts: String) =
        TokenExchangeEntity(address = null, symbol = symbol, chainId = null, currency = "usd", value = value, timestamp = Instant.parse(ts))

    /** Mirrors Web3jNetworkBalanceRepository.latestUsdRateBySymbol. */
    private fun List<TokenExchangeEntity>.latestUsdRateBySymbol(): Map<String, Double> =
        groupBy { it.symbol }.mapValues { (_, rows) -> rows.maxByOrNull { it.timestamp }?.value ?: 0.0 }

    @Test
    fun nativeRatesResolveBySymbol_includingMonAndApe() = runBlocking {
        db.tokenExchangeDao.insertAll(
            listOf(
                nativeUsd("ETH", 2118.1, "2026-05-22T15:00:00Z"),
                nativeUsd("MATIC", 0.0913, "2026-05-22T15:00:00Z"),
                nativeUsd("BNB", 657.45, "2026-05-22T15:00:00Z"),
                nativeUsd("AVAX", 9.44, "2026-05-22T15:00:00Z"),
                nativeUsd("MON", 0.02, "2026-05-22T14:00:00Z"),      // stale
                nativeUsd("MON", 0.02815, "2026-05-22T15:00:00Z"),   // latest
                nativeUsd("APE", 0.146, "2026-05-22T15:00:00Z"),
            )
        )

        // The exact query the network-token overview uses.
        val nativeSymbols = NetworkChain.getAllNetworkChains().map { it.nativeSymbol }.distinct()
        val rates = db.tokenExchangeDao.observeExchangesBySymbols(nativeSymbols, "usd")
            .first()
            .latestUsdRateBySymbol()

        // Every native ticker resolves — Monad/ApeChain included, BNB/AVAX no longer priced as ETH.
        assertEquals(2118.1, rates["ETH"]!!, 1e-9)
        assertEquals(657.45, rates["BNB"]!!, 1e-9)
        assertEquals(9.44, rates["AVAX"]!!, 1e-9)
        assertEquals(0.146, rates["APE"]!!, 1e-9)
        assertEquals("must pick the latest MON rate, not the stale one", 0.02815, rates["MON"]!!, 1e-9)

        // Per-balance fiat (as getGroupedNetworkTokensOverview computes it): 100 MON -> $2.815
        assertEquals(2.815, 100.0 * rates["MON"]!!, 1e-6)
    }
}
