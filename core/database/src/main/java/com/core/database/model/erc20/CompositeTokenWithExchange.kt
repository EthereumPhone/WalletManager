package com.core.database.model.erc20

import androidx.room.Embedded
import androidx.room.Relation
import com.core.model.TokenAssetWithPrice
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Composite data class that includes a token with its latest exchange rate.
 * This is more efficient than loading all historical exchange data.
 */
data class CompositeTokenWithExchange(
    @Embedded
    val compositeToken: CompositeToken,
    
    @Embedded(prefix = "exchange_")
    val latestExchangeEntity: TokenExchangeEntity? = null
) {
    // Delegate properties for easier access
    val tokenMetadataEntity: TokenMetadataEntity
        get() = compositeToken.tokenMetadataEntity
    
    val tokenBalanceEntity: TokenBalanceEntity?
        get() = compositeToken.tokenBalanceEntity
    
    /**
     * Get the current USD value of this token's balance.
     * Returns null if no exchange rate is available.
     */
    val balanceInUsd: Double?
        get() {
            val balance = tokenBalanceEntity?.tokenBalance
                ?.movePointLeft(tokenMetadataEntity.decimals)
                ?.toDouble() ?: 0.0
            val exchangeRate = latestExchangeEntity?.value
            
            return if (exchangeRate != null && latestExchangeEntity?.currency == "USD") {
                balance * exchangeRate
            } else {
                null
            }
        }
    
    /**
     * Get the formatted balance with appropriate decimal places.
     */
    val formattedBalance: String
        get() {
            val balance = tokenBalanceEntity?.tokenBalance
                ?.movePointLeft(tokenMetadataEntity.decimals)
                ?.setScale(tokenMetadataEntity.decimals, RoundingMode.HALF_DOWN)
                ?.stripTrailingZeros()
                ?.toPlainString()
            return balance ?: "0"
        }
    
    /**
     * Get the formatted USD value.
     */
    val formattedBalanceUsd: String?
        get() = balanceInUsd?.let { usd ->
            "$%.2f".format(usd)
        }
}

/**
 * Convert to external model with price information.
 */
fun CompositeTokenWithExchange.toExternalModelWithPrice(): TokenAssetWithPrice = TokenAssetWithPrice(
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
    fiatAmount = balanceInUsd ?: 0.0
)
