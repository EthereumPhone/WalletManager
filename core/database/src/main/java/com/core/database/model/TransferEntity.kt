package com.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.core.model.EntryCategory
import com.core.model.Transfer
import kotlinx.datetime.Instant

@Entity("transfer")
data class TransferEntity(
    @PrimaryKey
    val uniqueId: String,
    val asset: String,
    val chainId: Int,
    val blockNum: String,
    val category: String,
    val erc1155Metadata: List<Erc1155MetadataObject>,
    val erc721TokenId: String,
    val from: String,
    val hash: String,
    val rawContract: RawContract,
    val to: String,
    val tokenId: String,
    val value: Double,
    val blockTimestamp: Instant,
    val userIsSender: Boolean
)

data class Erc1155MetadataObject(
    val tokenId: String,
    val value: String
)

data class RawContract(
    val address: String?,
    val decimal: String?,
    val value: String?
)

fun TransferEntity.asExternalModel() = Transfer(
    asset,
    chainId,
    EntryCategory.valueOf(category),
    erc1155Metadata.map { metadata ->
        Transfer.Erc1155MetadataObject(
            tokenId = metadata.tokenId,
            value = metadata.value
        )
    },
    erc721TokenId,
    from,
    rawContract.run {
        Transfer.RawContract(
            address = address.orEmpty(),
            decimal = decimal.orEmpty(),
            value = value.orEmpty()
        )
    },
    to,
    tokenId,
    value,
    blockTimestamp,
    userIsSender
)
