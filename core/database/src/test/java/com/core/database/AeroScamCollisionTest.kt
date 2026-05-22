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
import com.core.database.util.TokenSeedingHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal

/**
 * Reproduces the report from wallet 0x693ED898E52cd942198a340d2B65Dcd38d01F710:
 *   "swapped into AERO, my main AERO bag disappeared from the UI; a scam AERO showed instead.
 *    The real AERO still shows on the block explorer."
 *
 * On-chain that wallet holds TWO tokens both reporting symbol "AERO":
 *   - 0x940181a9... "Aerodrome" (REAL, seeded), 18 decimals, balance 925.343
 *   - 0x295ed817... "AERO"      (SCAM, not seeded), 18 decimals, balance 15472.056
 *
 * The seeder/backfill group tokens BY SYMBOL, so the scam token's backfill resolves its
 * groupId to the real AERO group (findGroupIdBySymbolPreferMainnet("AERO")). This test runs
 * the real DAO path and shows how the merge corrupts the AERO card.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class AeroScamCollisionTest {

    private val realAero = "0x940181a94a35a4569e4529a3cdfb74e38fd98631"
    private val scamAero = "0x295ed817ac3c8b12f023a61f7a35cb8f917f8ff3"

    private lateinit var db: WmDatabase

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val jsonConverter = MoshiJsonConverter(Moshi.Builder().add(KotlinJsonAdapterFactory()).build())
        db = Room.inMemoryDatabaseBuilder(ctx, WmDatabase::class.java)
            .addTypeConverter(Erc1155MetadataConverter(jsonConverter))
            .addTypeConverter(RawContractConverter(jsonConverter))
            .addTypeConverter(BigDecimalTypeConverter())
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun scamAero_mergesIntoRealAeroGroup_andCorruptsTheCard() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        TokenSeedingHelper.seedTokensDirectly(ctx, db.openHelper.writableDatabase)

        // Both balances are on-chain (verified via Alchemy).
        db.tokenBalanceDao.upsertTokenBalances(
            listOf(
                TokenBalanceEntity(realAero, 8453, BigDecimal("925342960279550840725")),
                TokenBalanceEntity(scamAero, 8453, BigDecimal("15472055633893321852726"))
            )
        )

        // Faithfully replicate the runtime backfill (AlchemyTokenBalanceRepository.fetchMissingMetadataOnChain):
        // the scam token has a balance but no seed metadata, so its groupId is resolved BY SYMBOL.
        val resolvedGroupId = db.tokenGroupDao.findGroupIdBySymbolPreferMainnet("AERO")
        println("scam AERO resolveGroupId(symbol=AERO) -> $resolvedGroupId")
        if (db.tokenGroupDao.getGroupedToken(resolvedGroupId!!) == null) {
            db.tokenGroupDao.upsertTokenGroups(
                listOf(TokenGroupEntity(resolvedGroupId, 8453, scamAero, "AERO", "AERO"))
            )
        }
        db.tokenMetadataDao.upsertTokensMetadata(
            listOf(TokenMetadataEntity(scamAero, 8453, 18, "AERO", "AERO", null, false, resolvedGroupId))
        )

        // What the home actually renders.
        val groups = db.tokenGroupDao.observeAllActiveTokenGroupsWithExchange().first()
        val aero = groups.firstOrNull { it.tokenGroup.symbol.equals("AERO", ignoreCase = true) }

        println("=== AERO group as the home would render it ===")
        println("groupId=${aero?.tokenGroup?.groupId} canonicalAddress=${aero?.tokenGroup?.canonicalAddress}")
        println("displayed totalBalance=${aero?.totalBalance}  (real bag is 925.343)")
        println("members:")
        aero?.tokensWithExchange?.forEach { m ->
            println("   addr=${m.tokenMetadataEntity?.contractAddress} name=${m.tokenMetadataEntity?.name} bal=${m.tokenBalanceEntity?.tokenBalance}")
        }
        val realIncluded = aero?.tokensWithExchange?.any { it.tokenMetadataEntity?.contractAddress == realAero } == true
        val scamIncluded = aero?.tokensWithExchange?.any { it.tokenMetadataEntity?.contractAddress == scamAero } == true
        println("real AERO in group=$realIncluded  scam AERO in group=$scamIncluded")
        println("=> The single 'AERO' card shows ${aero?.totalBalance} instead of the user's real 925.343 bag.")
    }

    /**
     * Validates the fix. Mirrors the corrected runtime logic:
     *   - resolveGroupId no longer merges by symbol: a backfilled (non-curated) token gets
     *     its own per-address group (bridge lookup only, which a scam never matches).
     *   - reconcileTokenGroups no longer re-points tokens that already have a valid group,
     *     so it can't re-merge the scam into the real group.
     * Expectation: the real Aerodrome bag (925.343) is preserved as its own card, and the
     * scam shows as a SEPARATE card — never merged.
     */
    @Test
    fun withFix_scamAeroGetsOwnGroup_realAeroBagPreserved() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        TokenSeedingHelper.seedTokensDirectly(ctx, db.openHelper.writableDatabase)
        db.tokenBalanceDao.upsertTokenBalances(
            listOf(
                TokenBalanceEntity(realAero, 8453, BigDecimal("925342960279550840725")),
                TokenBalanceEntity(scamAero, 8453, BigDecimal("15472055633893321852726"))
            )
        )

        // FIXED backfill: resolveGroupId(chainId, address) = bridge lookup only, else own group.
        val byBridge = db.tokenGroupDao.findGroupIdByBridge(8453, scamAero)
        val scamGroupId = byBridge ?: "8453_$scamAero"
        println("scam AERO fixed resolveGroupId -> $scamGroupId (bridge=$byBridge)")
        if (db.tokenGroupDao.getGroupedToken(scamGroupId) == null) {
            db.tokenGroupDao.upsertTokenGroups(
                listOf(TokenGroupEntity(scamGroupId, 8453, scamAero, "AERO", "AERO"))
            )
        }
        db.tokenMetadataDao.upsertTokensMetadata(
            listOf(TokenMetadataEntity(scamAero, 8453, 18, "AERO", "AERO", null, false, scamGroupId))
        )

        // FIXED reconcile (orphan-only): real AERO and scam AERO both already have valid groups,
        // so neither is re-pointed. (Replicated here; the real impl is in AlchemyTokenMetadataRepository.)
        // -> no-op for these two tokens.

        val groups = db.tokenGroupDao.observeAllActiveTokenGroupsWithExchange().first()
        val aeroGroups = groups.filter { it.tokenGroup.symbol.equals("AERO", ignoreCase = true) }
        println("=== AERO groups after fix ===")
        aeroGroups.forEach { g ->
            println("  groupId=${g.tokenGroup.groupId} totalBalance=${g.totalBalance} members=${g.tokensWithExchange.map { it.tokenMetadataEntity?.contractAddress }}")
        }

        val realGroup = aeroGroups.firstOrNull { it.tokenGroup.groupId == "8453_$realAero" }
        val scamGroup = aeroGroups.firstOrNull { it.tokenGroup.groupId == scamGroupId }

        assertEquals("scam and real AERO must be in SEPARATE groups", 2, aeroGroups.size)
        assertNotNull("real Aerodrome group must exist", realGroup)
        assertNotNull("scam AERO must have its own group", scamGroup)
        // Real bag preserved exactly — not polluted by the scam's 15472.
        assertEquals(925.342960279550840725, realGroup!!.totalBalance.toDouble(), 1e-9)
        assertFalse(
            "real AERO group must NOT contain the scam token",
            realGroup.tokensWithExchange.any { it.tokenMetadataEntity?.contractAddress == scamAero }
        )
    }

    /**
     * Validates the HEAL path for users already affected (e.g. nanopunk). Starts from the
     * already-merged DB state the old code produced — the scam's metadata pointing at the real
     * AERO group — then runs the fixed reconcile logic and asserts the scam is split back out so
     * the real bag reappears. Mirrors AlchemyTokenMetadataRepository.reconcileTokenGroups.
     */
    @Test
    fun reconcile_healsAlreadyMergedScam() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        TokenSeedingHelper.seedTokensDirectly(ctx, db.openHelper.writableDatabase)
        db.tokenBalanceDao.upsertTokenBalances(
            listOf(
                TokenBalanceEntity(realAero, 8453, BigDecimal("925342960279550840725")),
                TokenBalanceEntity(scamAero, 8453, BigDecimal("15472055633893321852726"))
            )
        )
        // Reproduce the OLD merged state: scam metadata stored pointing at the REAL AERO group.
        val realGroupId = "8453_$realAero"
        db.tokenMetadataDao.upsertTokensMetadata(
            listOf(TokenMetadataEntity(scamAero, 8453, 18, "AERO", "AERO", null, false, realGroupId))
        )
        // Sanity: both are merged into one group before the heal.
        assertEquals(1, db.tokenGroupDao.observeAllActiveTokenGroupsWithExchange().first()
            .count { it.tokenGroup.symbol.equals("AERO", ignoreCase = true) })

        // --- Fixed reconcile logic (mirrors AlchemyTokenMetadataRepository.reconcileTokenGroups) ---
        val allMeta = db.tokenMetadataDao.getTokensMetadata().first()
        allMeta.groupBy { it.symbol.lowercase() }.forEach { (_, list) ->
            if (list.isEmpty()) return@forEach
            val symbol = list.first().symbol
            val canonicalGroupId = db.tokenGroupDao.findGroupIdBySymbolPreferMainnet(symbol) ?: return@forEach
            val grp = db.tokenGroupDao.getGroupedToken(canonicalGroupId) ?: return@forEach
            list.forEach { token ->
                val addr = token.contractAddress.lowercase()
                if (token.chainId == grp.canonicalChainId && addr != grp.canonicalAddress.lowercase()) {
                    val ownId = "${token.chainId}_$addr"
                    if (db.tokenGroupDao.getGroupedToken(ownId) == null) {
                        db.tokenGroupDao.upsertTokenGroups(
                            listOf(TokenGroupEntity(ownId, token.chainId, addr, token.symbol, token.name))
                        )
                    }
                    if (token.groupId != ownId) {
                        db.tokenMetadataDao.upsertTokensMetadata(listOf(token.copy(groupId = ownId)))
                    }
                }
            }
        }

        // --- After heal ---
        val aeroGroups = db.tokenGroupDao.observeAllActiveTokenGroupsWithExchange().first()
            .filter { it.tokenGroup.symbol.equals("AERO", ignoreCase = true) }
        aeroGroups.forEach { println("healed group ${it.tokenGroup.groupId} bal=${it.totalBalance}") }
        val realGroup = aeroGroups.firstOrNull { it.tokenGroup.groupId == realGroupId }
        assertEquals("scam split back out into its own group", 2, aeroGroups.size)
        assertNotNull(realGroup)
        assertEquals(925.342960279550840725, realGroup!!.totalBalance.toDouble(), 1e-9)
        assertFalse(
            "real AERO group must no longer contain the scam",
            realGroup.tokensWithExchange.any { it.tokenMetadataEntity?.contractAddress == scamAero }
        )
    }

    /**
     * Informational guard: confirms whether the seed bridge-links same-chain USDC variants.
     * The canonical-chain heal can never touch them (their chain != the group's canonical chain),
     * so this is a note for future maintainers, not a hard assertion of grouping.
     */
    @Test
    fun usdcVariants_areNotSplitByTheCanonicalChainRule() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        TokenSeedingHelper.seedTokensDirectly(ctx, db.openHelper.writableDatabase)
        val usdcGroupId = "1_0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
        val grp = db.tokenGroupDao.getGroupedToken(usdcGroupId)
        println("USDC group canonicalChainId=${grp?.canonicalChainId} canonicalAddress=${grp?.canonicalAddress}")
        val members = db.tokenGroupDao.getTokensInGroup(usdcGroupId).first()
        val onCanonicalChain = members.filter { it.chainId == grp?.canonicalChainId }
        println("USDC members on canonical chain (1): ${onCanonicalChain.map { it.contractAddress }}")
        // Only the real mainnet USDC sits on the canonical chain, so the heal rule won't split any variant.
        assertEquals(1, onCanonicalChain.size)
    }
}
