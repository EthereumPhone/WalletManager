package org.ethereumphone.walletmanager.core.database.model.erc20Token.relations

import androidx.room.Embedded
import androidx.room.Relation
import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenBalanceEntity
import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenMetadataEntity


data class MetadataAndBalance(
    @Embedded val metadata: TokenMetadataEntity,
    @Relation(
        parentColumn = "contractAddress",
        entityColumn = ""
    )
    val balance: TokenBalanceEntity
)
