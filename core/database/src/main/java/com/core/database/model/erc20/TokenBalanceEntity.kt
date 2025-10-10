package com.core.database.model.erc20

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.core.model.TokenBalance
import java.math.BigDecimal

@Entity(
    tableName = "token_balance",
    primaryKeys = ["contractAddress", "chainId"]
)
data class TokenBalanceEntity(
    val contractAddress: String,
    val chainId: Int,
    val tokenBalance: BigDecimal,
)


fun TokenBalanceEntity.asExternalModule(): TokenBalance {
    return TokenBalance(
        contractAddress,
        chainId,
        tokenBalance
    )

}