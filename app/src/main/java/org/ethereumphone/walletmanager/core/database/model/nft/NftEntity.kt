package org.ethereumphone.walletmanager.core.database.model.nft

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nft")
data class NftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: String,
    val contractAddress: String,
    val tokenType: String,
    val title: String,
    val description: String,
    val tokenURI: String,
    val imageURI: String,
    val metadata: String
)