package org.ethereumphone.walletmanager.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ethereumphone.walletmanager.core.domain.model.TokenExchange

@Entity(tableName = "token_exchange")
data class TokenExchangeEntity(
    @PrimaryKey
    val symbol: String,
    val price: Double
)

fun TokenExchangeEntity.asExternalModel() = TokenExchange(
    symbol = symbol,
    price = price
)