package com.core.database.model.erc20

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "token_group")
data class TokenGroupEntity(
    @PrimaryKey
    val groupId: String,
    val canonicalChainId: Int, // The "main" chain for this token (e.g., 1 for Ethereum mainnet)
    val canonicalAddress: String, // The address on the canonical chain
    val symbol: String,
    val name: String,
)