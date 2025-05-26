package com.core.database.model.erc20

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "token_exchange")
data class TokenExchangeEntity(
    @PrimaryKey
    val contractAddress: String,
    val chainId: Int,
    val currency: String,
    val value: Double, // this value always relates to 1 unit of the token. FI: 1 eth = x usd
    val timestamp: Instant
)
