package com.core.database.model.erc20

import androidx.room.Embedded
import androidx.room.Relation


data class CompositeToken(
    @Embedded
    val tokenBalanceEntity: TokenBalanceEntity,

    @Relation(
        parentColumn = "contractAddress",
        entityColumn = "contractAddress",
    )
    val tokenMetadataEntity: TokenMetadataEntity,

    @Relation(
        parentColumn = "contractAddress",
        entityColumn = "contractAddress",
    )
    val tokenExchangeEntity: TokenExchangeEntity
)
