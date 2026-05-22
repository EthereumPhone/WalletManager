package com.core.database

import com.core.database.model.erc20.CompositeTokenGroupWithExchange
import com.core.database.model.erc20.CompositeTokenWithExchange
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.database.model.erc20.toExternalModelWithPrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * Regression test for the support report:
 *   "User 0x31145208a6b6323f71fd66e63191a8933385c4be cannot see their USDC balance on Base."
 *
 * On-chain (verified via Alchemy `alchemy_getTokenBalances` on base-mainnet) this address holds:
 *   - USDC on Base (0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913, 6 decimals): raw 10_275_243  -> 10.275243
 *   - USDC on Arbitrum (0xaf88d065e77c8cC2239327C5EDb3A432268e5831, 6 decimals): raw 20_000 -> 0.02
 *
 * The home screen renders ERC20 holdings via the grouped path
 * (GetAllGroupedTokensUsecase -> observeAllActiveTokenGroupsWithExchange ->
 *  CompositeTokenGroupWithExchange), which merges the same symbol across chains into one
 * "USDC" row whose total is summed PER MEMBER using that member's own decimals.
 *
 * These tests pin that behaviour: given correctly-seeded USDC metadata + the user's real
 * balances, the Base balance MUST surface (correct decimals, listed as an active chain).
 * They also pin the failure mode that actually hides a balance: a held token with NO metadata
 * falls back to 18 decimals and renders as ~0, i.e. effectively invisible.
 */
class UsdcBaseBalanceTest {

    private val baseUsdc = "0x833589fcd6edb6e08f4c7c32d4f71b54bda02913"
    private val arbUsdc = "0xaf88d065e77c8cc2239327c5edb3a432268e5831"
    private val mainnetUsdc = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"

    /** groupId as produced by TokenSeedingHelper.generateGroupId for canonical mainnet USDC. */
    private val usdcGroupId = "1_$mainnetUsdc"

    private fun usdcMetadata(address: String, chainId: Int) = TokenMetadataEntity(
        contractAddress = address,
        chainId = chainId,
        decimals = 6,
        name = "USD Coin",
        symbol = "USDC",
        logo = null,
        swappable = true,
        groupId = usdcGroupId
    )

    private fun member(address: String, chainId: Int, rawBalance: String?) =
        CompositeTokenWithExchange(
            tokenMetadataEntity = usdcMetadata(address, chainId),
            tokenBalanceEntity = rawBalance?.let {
                TokenBalanceEntity(
                    contractAddress = address,
                    chainId = chainId,
                    tokenBalance = BigDecimal(it)
                )
            }
        )

    private val usdcGroupEntity = TokenGroupEntity(
        groupId = usdcGroupId,
        canonicalChainId = 1,
        canonicalAddress = mainnetUsdc,
        symbol = "USDC",
        name = "USD Coin"
    )

    @Test
    fun usdcOnBase_surfacesWithCorrectBalance() {
        val group = CompositeTokenGroupWithExchange(
            tokenGroup = usdcGroupEntity,
            tokensWithExchange = listOf(
                member(mainnetUsdc, 1, rawBalance = null),     // no balance on mainnet
                member(baseUsdc, 8453, rawBalance = "10275243"),
                member(arbUsdc, 42161, rawBalance = "20000")
            )
        )

        // Base (10.275243) + Arbitrum (0.02) summed with each member's own 6 decimals.
        assertEquals(10.295243, group.totalBalance.toDouble(), 1e-9)
        assertTrue("USDC group must report a balance", group.hasBalance)
        assertTrue("Base (8453) must be listed as an active chain", group.activeChainIds.contains(8453))
        assertEquals(2, group.activeChainCount)
    }

    @Test
    fun usdcBaseMemberAlone_isNotZeroAndUsesSixDecimals() {
        val baseMember = member(baseUsdc, 8453, rawBalance = "10275243")
        assertEquals("10.275243", baseMember.formattedBalance)
        assertEquals(10.275243, baseMember.toExternalModelWithPrice().balance, 1e-9)
    }

    /**
     * The actual mechanism by which a balance becomes invisible: when a held token has NO
     * metadata, the model falls back to 18 decimals. USDC's raw 10_275_243 then renders as
     * ~0.0000000000103 — i.e. the user "can't see" it. This is what would happen if Base USDC
     * metadata were never seeded for this device (e.g. stale seed + the re-seed guard in
     * TokenSeedingHelper.seedTokensDirectly), before on-chain metadata back-fill runs.
     */
    @Test
    fun heldTokenWithoutMetadata_rendersEffectivelyZero_documentingTheFailureMode() {
        val orphan = CompositeTokenWithExchange(
            tokenMetadataEntity = null,
            tokenBalanceEntity = TokenBalanceEntity(
                contractAddress = baseUsdc,
                chainId = 8453,
                tokenBalance = BigDecimal("10275243")
            )
        )
        // Falls back to 18 decimals -> ~1.03e-11, which the UI shows as 0.
        assertTrue(orphan.toExternalModelWithPrice().balance < 1e-6)
    }
}
