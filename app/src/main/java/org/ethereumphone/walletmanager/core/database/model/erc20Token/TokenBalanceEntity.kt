package org.ethereumphone.walletmanager.core.database.model.erc20Token

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("token_balance")
data class TokenBalanceEntity(
    @PrimaryKey
    val contractAddress: String,
    val tokenBalance: Double
)
