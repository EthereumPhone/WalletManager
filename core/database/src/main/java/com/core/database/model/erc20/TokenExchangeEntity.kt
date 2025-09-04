package com.core.database.model.erc20

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.core.model.TokenExchange
import kotlinx.datetime.Instant

@Entity(
    tableName = "token_exchange",
    indices = [
        Index(
            value = ["address", "chainId", "currency"],
            unique = true
        )
    ]
)
data class TokenExchangeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val address: String?,
    val symbol: String,
    val chainId: Int?,
    val currency: String,
    val value: Double, // this value always relates to 1 unit of the token. FI: 1 eth = x usd
    val timestamp: Instant
)

fun TokenExchangeEntity.asExternalModel(): TokenExchange = TokenExchange(
    address = address,
    symbol =  symbol,
    chainId = chainId,
    currency = currency,
    value = value,
    timestamp = timestamp
)
