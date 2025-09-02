package com.core.database.model.erc20

import androidx.room.Embedded
import androidx.room.Relation
import com.core.model.TokenAsset
import com.core.model.TokenAssetWithPrice
import com.core.model.TokenGroupAsset
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Represents a group of tokens across multiple chains with aggregated data.
 * This is used to display grouped tokens (e.g., all WETH instances across chains).
 */
data class CompositeTokenGroup(
    @Embedded
    val tokenGroup: TokenGroupEntity,
    
    @Relation(
        entity = TokenMetadataEntity::class,
        parentColumn = "groupId",
        entityColumn = "groupId"
    )
    val tokens: List<CompositeToken>
) {
    /**
     * The total balance across all chains for this token group.
     * Sums up all individual token balances, accounting for decimals.
     */
    val totalBalance: BigDecimal
        get() = tokens.sumOf { compositeToken ->
            compositeToken.tokenBalanceEntity?.tokenBalance
                ?.movePointLeft(compositeToken.tokenMetadataEntity.decimals)
                ?: BigDecimal.ZERO
        }
    
    /**
     * The total balance formatted as a string with appropriate decimal places.
     */
    val formattedTotalBalance: String
        get() {
            // Use the canonical token's decimals for formatting
            val decimals = tokens.firstOrNull()?.tokenMetadataEntity?.decimals ?: 18
            return totalBalance
                .setScale(decimals, RoundingMode.HALF_DOWN)
                .stripTrailingZeros()
                .toPlainString()
        }

    val activeChainCount: Int
        get() = tokens.count { 
            it.tokenBalanceEntity?.tokenBalance?.compareTo(BigDecimal.ZERO) == 1 
        }

    val totalChainCount: Int
        get() = tokens.size
    

    val chainIds: List<Int>
        get() = tokens.map { it.tokenMetadataEntity.chainId }
    

    val activeChainIds: List<Int>
        get() = tokens
            .filter { it.tokenBalanceEntity?.tokenBalance?.compareTo(BigDecimal.ZERO) == 1 }
            .map { it.tokenMetadataEntity.chainId }
    

    val logoUrl: String?
        get() = tokens
            .firstOrNull { it.tokenMetadataEntity.chainId == tokenGroup.canonicalChainId }
            ?.tokenMetadataEntity?.logo
            ?: tokens.firstOrNull()?.tokenMetadataEntity?.logo

    val hasBalance: Boolean
        get() = totalBalance.compareTo(BigDecimal.ZERO) == 1
}

/**
 * Extension function to convert to an external model for UI consumption.
 */
fun CompositeTokenGroup.toExternalModel() = TokenGroupAsset(
    groupId = tokenGroup.groupId,
    symbol = tokenGroup.symbol,
    name = tokenGroup.name,
    totalBalance = totalBalance.toDouble(),
    formattedBalance = formattedTotalBalance,
    activeChains = activeChainCount,
    totalChains = totalChainCount,
    logoUrl = logoUrl,
    tokens = tokens.map { it.toExternalModel() },
    chainIds = chainIds,
    activeChainIds = activeChainIds
)
