package org.ethereumphone.walletmanager.core.database.model.erc20Token

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ethereumphone.walletmanager.core.domain.model.TokenExchange

@Entity(tableName = "token_exchange")
data class TokenExchangeEntity(
    @PrimaryKey
    val symbol: String,
    val price: Double
) {
    fun toTokenExchange(): TokenExchange {
        return TokenExchange(
            symbol = symbol,
            price = price
        )
    }
}