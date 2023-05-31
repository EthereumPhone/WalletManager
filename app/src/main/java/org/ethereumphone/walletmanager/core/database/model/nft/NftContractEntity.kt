package org.ethereumphone.walletmanager.core.database.model.nft

import androidx.room.PrimaryKey

data class NftContractEntity(
     @PrimaryKey(autoGenerate = false)
     val contractAddress: String,
     val name: String,
     val symbol: String,
     val totalSupply: Int,
     val tokenType: String,
)