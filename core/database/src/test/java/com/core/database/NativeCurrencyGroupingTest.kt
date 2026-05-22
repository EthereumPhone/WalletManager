package com.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.database.util.BigDecimalTypeConverter
import com.core.database.util.Erc1155MetadataConverter
import com.core.database.util.MoshiJsonConverter
import com.core.database.util.RawContractConverter
import com.core.model.NetworkChain
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal

/**
 * Validates the per-chain native-currency model added for Monad/ApeChain support
 * (which also fixes the old binary "every non-Polygon chain is ETH" bug for BNB/AVAX).
 *
 * Mirrors the refactored Web3jNetworkBalanceRepository.refreshNetworkBalance seeding loop and
 * checks: (a) each native currency gets its own group with the right ticker, and (b) an
 * EXISTING user whose chain-56 native balance was mislabeled ETH (groupId network_eth) is
 * auto-healed to network_bnb when balances next refresh — no clear-data needed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class NativeCurrencyGroupingTest {

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

    /** Mirrors Web3jNetworkBalanceRepository.refreshNetworkBalance's native group/metadata seeding. */
    private suspend fun seedNativeGroups(chainIds: List<Int>) {
        val networks = chainIds.mapNotNull { NetworkChain.getNetworkByChainId(it) }
        val groups = mutableListOf<TokenGroupEntity>()
        val metas = mutableListOf<TokenMetadataEntity>()
        networks.groupBy { it.nativeSymbol }.forEach { (symbol, chains) ->
            val canonical = chains.minByOrNull { it.chainId } ?: return@forEach
            groups += TokenGroupEntity(canonical.nativeGroupId, canonical.chainId, canonical.chainId.toString(), symbol, canonical.nativeName)
            chains.forEach { net ->
                metas += TokenMetadataEntity(net.chainId.toString(), net.chainId, net.nativeDecimals, net.nativeName, symbol, symbol, true, canonical.nativeGroupId)
            }
        }
        db.tokenGroupDao.upsertTokenGroups(groups)
        db.tokenMetadataDao.upsertTokensMetadata(metas)
    }

    @Test
    fun eachNativeCurrencyGetsItsOwnGroup_includingMonAndApe() = runBlocking {
        seedNativeGroups(listOf(1, 8453, 137, 56, 43114, 143, 33139)) // ETH chains, MATIC, BNB, AVAX, MON, APE

        suspend fun groupOf(chainId: Int) =
            db.tokenMetadataDao.getTokenMetadataByAddressAndChainId(chainId.toString(), chainId)!!.groupId

        assertEquals("network_eth", groupOf(1))
        assertEquals("network_eth", groupOf(8453))   // Base ETH shares the ETH group
        assertEquals("network_matic", groupOf(137))
        assertEquals("network_bnb", groupOf(56))
        assertEquals("network_avax", groupOf(43114))
        assertEquals("network_mon", groupOf(143))     // Monad
        assertEquals("network_ape", groupOf(33139))   // ApeChain
        // Tickers are correct, not all "ETH".
        assertEquals("MON", db.tokenGroupDao.getGroupedToken("network_mon")!!.symbol)
        assertEquals("APE", db.tokenGroupDao.getGroupedToken("network_ape")!!.symbol)
    }

    @Test
    fun existingBnbBalanceMislabeledEth_isHealedOnRefresh() = runBlocking {
        // OLD buggy state: chain-56 native balance stored as ETH under network_eth.
        db.tokenGroupDao.upsertTokenGroups(
            listOf(TokenGroupEntity("network_eth", 1, "1", "ETH", "Ethereum"))
        )
        db.tokenMetadataDao.upsertTokensMetadata(
            listOf(TokenMetadataEntity("56", 56, 18, "ETH", "ETH", "ETH", true, "network_eth"))
        )
        db.tokenBalanceDao.upsertTokenBalances(
            listOf(TokenBalanceEntity("56", 56, BigDecimal("2000000000000000000")))
        )
        assertEquals("network_eth", db.tokenMetadataDao.getTokenMetadataByAddressAndChainId("56", 56)!!.groupId)

        // Next balance refresh re-seeds native groups (upsert) for the held chains.
        seedNativeGroups(listOf(1, 56))

        // Healed: chain-56 native now belongs to the BNB group, and the active-group query
        // surfaces a BNB group on chain 56 (no longer hidden inside ETH).
        val meta = db.tokenMetadataDao.getTokenMetadataByAddressAndChainId("56", 56)!!
        assertEquals("network_bnb", meta.groupId)
        assertEquals("BNB", meta.symbol)

        val active = db.tokenGroupDao.observeAllActiveTokenGroupsWithExchange().first()
        val bnb = active.firstOrNull { it.tokenGroup.groupId == "network_bnb" }
        assertTrue("BNB native group must be active", bnb != null)
        assertTrue("BNB group must be active on chain 56", bnb!!.activeChainIds.contains(56))
    }
}
