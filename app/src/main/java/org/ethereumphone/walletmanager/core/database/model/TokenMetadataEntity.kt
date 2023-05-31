package org.ethereumphone.walletmanager.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_metadata")
data class TokenMetadataEntity(
    @PrimaryKey(autoGenerate = false)
    val symbol: String,
    val chainId: Int,
    val address: String,
    val name: String,
    val decimals: Int,
    val logoURI: String
)