package com.core.database.model.erc20

import androidx.room.Embedded
import androidx.room.Relation
import com.core.model.TokenAsset
import com.core.model.TokenAssetWithPrice
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow


data class CompositeToken(
    @Embedded
    val tokenMetadataEntity: TokenMetadataEntity,

    @Relation(
        parentColumn = "contractAddress",
        entityColumn = "contractAddress",
    )
    val tokenBalanceEntity: TokenBalanceEntity?,

    // Note: For latest exchange rate, use CompositeTokenWithExchange or fetch separately
    // This avoids loading all historical exchange data
) {
    /**
     * Get the current USD value of this token's balance.
     * Note: This requires exchange rate to be provided separately.
     */
    fun getBalanceInUsd(exchangeRate: Double?, currency: String = "USD"): Double? {
        if (exchangeRate == null || currency != "USD") return null
        
        val balance = tokenBalanceEntity?.tokenBalance
            ?.movePointLeft(tokenMetadataEntity.decimals)
            ?.toDouble() ?: 0.0
            
        return balance * exchangeRate
    }
}

fun CompositeToken.toExternalModel(): TokenAsset = TokenAsset(
        address = tokenMetadataEntity.contractAddress,
        chainId = tokenMetadataEntity.chainId,
        symbol = tokenMetadataEntity.symbol,
        name = tokenMetadataEntity.name,
        balance = tokenBalanceEntity?.tokenBalance?.movePointLeft(tokenMetadataEntity.decimals)
            ?.setScale(tokenMetadataEntity.decimals, RoundingMode.HALF_DOWN)?.stripTrailingZeros()
            ?.toDouble()
            ?: 0.0,
        decimals = tokenMetadataEntity.decimals,
        logoUrl = tokenMetadataEntity.logo,
        swappable = tokenMetadataEntity.swappable
    )

/**
 * Convert to external model with price information.
 * Note: This version has no price data. Use CompositeTokenWithExchange for price-aware conversion.
 */
fun CompositeToken.toExternalModelWithPrice(): TokenAssetWithPrice = TokenAssetWithPrice(
        address = tokenMetadataEntity.contractAddress,
        chainId = tokenMetadataEntity.chainId,
        symbol = tokenMetadataEntity.symbol,
        name = tokenMetadataEntity.name,
        balance = tokenBalanceEntity?.tokenBalance?.movePointLeft(tokenMetadataEntity.decimals)
            ?.setScale(tokenMetadataEntity.decimals, RoundingMode.HALF_DOWN)?.stripTrailingZeros()
            ?.toDouble()
            ?: 0.0,
        decimals = tokenMetadataEntity.decimals,
        logoUrl = tokenMetadataEntity.logo,
        swappable = tokenMetadataEntity.swappable,
        fiatAmount = 0.0 // No price data available in base CompositeToken
    )
