package com.core.database.model.erc20

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.core.model.TokenBalance
import java.math.BigDecimal

@Entity("token_balance")
data class TokenBalanceEntity(
    @PrimaryKey
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