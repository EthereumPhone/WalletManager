package com.core.database.model.erc20

import androidx.room.Embedded
import androidx.room.Relation
import com.core.model.TokenAsset
import kotlin.math.pow


data class CompositeToken(
    @Embedded
    val tokenBalanceEntity: TokenBalanceEntity,

    @Relation(
        parentColumn = "contractAddress",
        entityColumn = "contractAddress",
    )
    val tokenMetadataEntity: TokenMetadataEntity,
)

fun CompositeToken.toExternalModel() = TokenAsset(
    address = tokenBalanceEntity.contractAddress,
    chainId = tokenBalanceEntity.chainId,
    symbol = tokenMetadataEntity.symbol,
    name = tokenMetadataEntity.name,
    balance = tokenBalanceEntity.tokenBalance.divide(
        (10.0.pow(tokenMetadataEntity.decimals)).toBigDecimal()).toDouble(),
    decimals = tokenMetadataEntity.decimals,
    logoUrl = tokenMetadataEntity.logo,
    swappable = tokenMetadataEntity.swappable
)

