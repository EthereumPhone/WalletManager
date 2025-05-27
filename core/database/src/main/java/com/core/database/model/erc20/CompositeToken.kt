package com.core.database.model.erc20

import androidx.room.Embedded
import androidx.room.Relation
import com.core.model.TokenAsset
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow


data class CompositeToken(
    @Embedded
    val tokenMetadataEntity: TokenMetadataEntity,

    @Relation(
        parentColumn = "contractAddress",
        entityColumn = "contractAddress",
    )
    val tokenBalanceEntity: TokenBalanceEntity?,
)

fun CompositeToken.toExternalModel(): TokenAsset {
    if (tokenMetadataEntity.symbol == "DEGEN") {
        println("test")
    }

    val test = TokenAsset(
        address = tokenMetadataEntity.contractAddress,
        chainId = tokenMetadataEntity.chainId,
        symbol = tokenMetadataEntity.symbol,
        name = tokenMetadataEntity.name,
        balance = tokenBalanceEntity?.tokenBalance?.movePointLeft(tokenMetadataEntity.decimals)
            ?.setScale(tokenMetadataEntity.decimals, RoundingMode.HALF_DOWN)?.stripTrailingZeros()
            ?.toDouble()
            ?: 0.0,
        decimals = tokenMetadataEntity.decimals,
        logoUrl = tokenMetadataEntity.logo,
        swappable = tokenMetadataEntity.swappable
    )

    if (tokenMetadataEntity.symbol == "DEGEN") {
        println("test")
    }


    return test
}

