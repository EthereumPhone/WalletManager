package org.ethereumphone.walletmanager.core.database.model.nft.relations

import androidx.room.Embedded
import androidx.room.Relation
import org.ethereumphone.walletmanager.core.database.model.nft.NftContractEntity

data class ContractWithNfts(
    @Embedded val contract: NftContractEntity,
    @Relation(
        parentColumn = "contractAddress",
        entityColumn = "contractAddress"
    )
    val nfts: List<NftContractEntity>
)