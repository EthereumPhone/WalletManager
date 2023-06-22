package com.core.database.model.erc20

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.core.model.TokenBalance

@Entity("token_balance",
    foreignKeys = [
        ForeignKey(
            entity = TokenMetadataEntity::class,
            parentColumns = ["contractAddress"],
            childColumns = ["contractAddress"],
            onDelete = ForeignKey.CASCADE
        )
    ]
    )
data class TokenBalanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val contractAddress: String,
    val chainId: Int,
    val tokenBalance: Double,
)


fun TokenBalanceEntity.asExternalModule(): TokenBalance {
    return TokenBalance(
        contractAddress,
        chainId,
        tokenBalance
    )

}