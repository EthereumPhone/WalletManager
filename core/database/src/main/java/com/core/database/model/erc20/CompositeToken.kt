package com.core.database.model.erc20

import androidx.room.Embedded
import androidx.room.Relation
import com.core.model.TokenAsset
import com.core.model.TokenAssetWithPrice
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow


/**
 * Composite token data class.
 * 
 * Design Philosophy:
 * - TokenMetadataEntity can be NULL when API fails to fetch metadata
 * - TokenBalanceEntity is typically present (from blockchain data) but nullable for Room compatibility
 * - In practice, always query so that balance is available
 * 
 * Maintainability Notes:
 * - Balance is the reliable data source (blockchain)
 * - Metadata is optional enrichment data (external API)
 * - Use helper properties (contractAddress, chainId) for safe access
 */
data class CompositeToken(
    @Embedded
    val tokenMetadataEntity: TokenMetadataEntity?,

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
        
        val decimals = tokenMetadataEntity?.decimals ?: 18 // Default to 18 decimals
        val balance = tokenBalanceEntity?.tokenBalance
            ?.movePointLeft(decimals)
            ?.toDouble() ?: 0.0
            
        return balance * exchangeRate
    }

    /**
     * Contract address - prefer balance entity (more reliable), fallback to metadata
     */
    val contractAddress: String
        get() = tokenBalanceEntity?.contractAddress 
            ?: tokenMetadataEntity?.contractAddress 
            ?: ""

    /**
     * Chain ID - prefer balance entity (more reliable), fallback to metadata
     */
    val chainId: Int
        get() = tokenBalanceEntity?.chainId 
            ?: tokenMetadataEntity?.chainId 
            ?: 0
}

fun CompositeToken.toExternalModel(): TokenAsset {
    val decimals = tokenMetadataEntity?.decimals ?: 18
    val balance = tokenBalanceEntity?.tokenBalance
        ?.movePointLeft(decimals)
        ?.setScale(decimals, RoundingMode.HALF_DOWN)
        ?.stripTrailingZeros()
        ?.toDouble() ?: 0.0
    
    return TokenAsset(
        address = contractAddress,
        chainId = chainId,
        symbol = tokenMetadataEntity?.symbol ?: (contractAddress.take(6) + "..."),
        name = tokenMetadataEntity?.name ?: (contractAddress.take(6) + "..."),
        balance = balance,
        decimals = decimals,
        logoUrl = tokenMetadataEntity?.logo,
        swappable = tokenMetadataEntity?.swappable ?: false
    )
}

/**
 * Convert to external model with price information.
 * Note: This version has no price data. Use CompositeTokenWithExchange for price-aware conversion.
 */
fun CompositeToken.toExternalModelWithPrice(): TokenAssetWithPrice {
    val decimals = tokenMetadataEntity?.decimals ?: 18
    val balance = tokenBalanceEntity?.tokenBalance
        ?.movePointLeft(decimals)
        ?.setScale(decimals, RoundingMode.HALF_DOWN)
        ?.stripTrailingZeros()
        ?.toDouble() ?: 0.0

    return TokenAssetWithPrice(
        address = contractAddress,
        chainId = chainId,
        symbol = tokenMetadataEntity?.symbol ?: (contractAddress.take(6) + "..."),
        name = tokenMetadataEntity?.name ?: (contractAddress.take(6) + "..."),
        balance = balance,
        decimals = decimals,
        logoUrl = tokenMetadataEntity?.logo,
        swappable = tokenMetadataEntity?.swappable ?: false,
        fiatAmount = 0.0 // No price data available in base CompositeToken
    )
}
