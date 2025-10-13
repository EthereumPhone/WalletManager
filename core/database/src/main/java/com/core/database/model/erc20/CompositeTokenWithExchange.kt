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
    val tokenMetadataEntity: TokenMetadataEntity?
        get() = compositeToken.tokenMetadataEntity
    
    val tokenBalanceEntity: TokenBalanceEntity?
        get() = compositeToken.tokenBalanceEntity
    
    val contractAddress: String
        get() = compositeToken.contractAddress
    
    val chainId: Int
        get() = compositeToken.chainId
    
    /**
     * Get the current USD value of this token's balance.
     * Returns null if no exchange rate is available.
     */
    val balanceInUsd: Double?
        get() {
            val decimals = tokenMetadataEntity?.decimals ?: 18
            val balance = tokenBalanceEntity?.tokenBalance
                ?.movePointLeft(decimals)
                ?.toDouble() ?: 0.0
            val exchangeRate = latestExchangeEntity?.value
            
            return if (exchangeRate != null && latestExchangeEntity?.currency == "usd") {
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
            val decimals = tokenMetadataEntity?.decimals ?: 18
            val balance = tokenBalanceEntity?.tokenBalance
                ?.movePointLeft(decimals)
                ?.setScale(decimals, RoundingMode.HALF_DOWN)
                ?.stripTrailingZeros()
                ?.toPlainString() ?: "0"
            return balance
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
fun CompositeTokenWithExchange.toExternalModelWithPrice(): TokenAssetWithPrice {
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
        fiatAmount = balanceInUsd ?: 0.0
    )
}
