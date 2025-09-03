package com.core.database.model.erc20

import androidx.room.Embedded
import androidx.room.Relation
import com.core.model.TokenAssetWithPrice
import com.core.model.TokenGroupAssetWithPrice
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Represents a group of tokens across multiple chains with their latest exchange rates.
 * This is more efficient than loading all historical exchange data.
 */
data class CompositeTokenGroupWithExchange(
    @Embedded
    val tokenGroup: TokenGroupEntity,
    
    val tokensWithExchange: List<CompositeTokenWithExchange>
) {
    /**
     * The total balance across all chains for this token group.
     * Sums up all individual token balances, accounting for decimals.
     */
    val totalBalance: BigDecimal
        get() = tokensWithExchange.sumOf { tokenWithExchange ->
            tokenWithExchange.tokenBalanceEntity?.tokenBalance
                ?.movePointLeft(tokenWithExchange.tokenMetadataEntity.decimals)
                ?: BigDecimal.ZERO
        }
    
    /**
     * The total balance formatted as a string with appropriate decimal places.
     */
    val formattedTotalBalance: String
        get() {
            // Use the canonical token's decimals for formatting
            val decimals = tokensWithExchange.firstOrNull()?.tokenMetadataEntity?.decimals ?: 18
            return totalBalance
                .setScale(decimals, RoundingMode.HALF_DOWN)
                .stripTrailingZeros()
                .toPlainString()
        }
    
    /**
     * Number of chains where this token has a non-zero balance.
     */
    val activeChainCount: Int
        get() = tokensWithExchange.count { 
            it.tokenBalanceEntity?.tokenBalance?.compareTo(BigDecimal.ZERO) == 1 
        }
    
    /**
     * Total number of chains where this token exists.
     */
    val totalChainCount: Int
        get() = tokensWithExchange.size
    
    /**
     * List of chain IDs where this token exists.
     */
    val chainIds: List<Int>
        get() = tokensWithExchange.map { it.tokenMetadataEntity.chainId }
    
    /**
     * List of chain IDs where this token has a non-zero balance.
     */
    val activeChainIds: List<Int>
        get() = tokensWithExchange
            .filter { it.tokenBalanceEntity?.tokenBalance?.compareTo(BigDecimal.ZERO) == 1 }
            .map { it.tokenMetadataEntity.chainId }
    
    /**
     * Get the logo URL, preferring the canonical chain's logo.
     */
    val logoUrl: String?
        get() = tokensWithExchange
            .firstOrNull { it.tokenMetadataEntity.chainId == tokenGroup.canonicalChainId }
            ?.tokenMetadataEntity?.logo
            ?: tokensWithExchange.firstOrNull()?.tokenMetadataEntity?.logo
    
    /**
     * Whether this token group has any balance across all chains.
     */
    val hasBalance: Boolean
        get() = totalBalance.compareTo(BigDecimal.ZERO) == 1
    
    /**
     * Get the latest exchange rate for this token group.
     * Prefers the canonical chain's exchange rate, falls back to any available.
     */
    val latestExchangeEntity: TokenExchangeEntity?
        get() {
            // First try to get exchange rate from canonical chain
            val canonicalExchange = tokensWithExchange
                .firstOrNull { it.tokenMetadataEntity.chainId == tokenGroup.canonicalChainId }
                ?.latestExchangeEntity
            
            // If not found, get the most recent exchange rate from any chain
            return canonicalExchange ?: tokensWithExchange
                .mapNotNull { it.latestExchangeEntity }
                .maxByOrNull { it.timestamp }
        }
    
    /**
     * Get the total USD value across all chains.
     * Returns null if no exchange rate is available.
     */
    val totalBalanceInUsd: Double?
        get() {
            val exchangeRate = latestExchangeEntity?.value
            return if (exchangeRate != null && latestExchangeEntity?.currency == "usd") {
                totalBalance.toDouble() * exchangeRate
            } else {
                // Try to sum individual USD values if available
                val individualUsdValues = tokensWithExchange.mapNotNull { it.balanceInUsd }
                if (individualUsdValues.isNotEmpty()) {
                    individualUsdValues.sum()
                } else {
                    null
                }
            }
        }
    
    /**
     * Formatted USD balance string.
     */
    val formattedUsdBalance: String?
        get() = totalBalanceInUsd?.let { usd ->
            "$%.2f".format(usd)
        }
}

/**
 * Extension function to convert to an external model with price information.
 */
fun CompositeTokenGroupWithExchange.toExternalModelWithPrice() = TokenGroupAssetWithPrice(
    groupId = tokenGroup.groupId,
    symbol = tokenGroup.symbol,
    name = tokenGroup.name,
    totalBalance = totalBalance.toDouble(),
    formattedBalance = formattedTotalBalance,
    totalBalanceUsd = totalBalanceInUsd ?: 0.0,
    formattedBalanceUsd = formattedUsdBalance ?: "$0.00",
    activeChains = activeChainCount,
    totalChains = totalChainCount,
    logoUrl = logoUrl,
    tokens = tokensWithExchange.map { it.toExternalModelWithPrice() },
    chainIds = chainIds,
    activeChainIds = activeChainIds,
    exchangeRate = latestExchangeEntity?.value ?: 0.0,
    exchangeCurrency = latestExchangeEntity?.currency ?: "USD"
)