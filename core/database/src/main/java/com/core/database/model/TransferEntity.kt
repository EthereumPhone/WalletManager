package com.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.core.model.EntryCategory
import com.core.model.Transfer
import kotlinx.datetime.Instant

/**
 * TABLE: Tranfer
 */
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
    val userIsSender: Boolean,
    val ispending: Boolean
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

//TransferEntity that gets maped
fun TransferEntity.asExternalModel() = Transfer(
    asset,
    chainId,
    when(category) {
        "external" -> EntryCategory.EXTERNAL
        "internal" -> EntryCategory.INTERNAL
        "erc20" -> EntryCategory.ERC20
        "erc721" -> EntryCategory.ERC721
        "erc1155" -> EntryCategory.ERC1155
        "special_nft" -> EntryCategory.SPECIAL_NFT
        else -> EntryCategory.EXTERNAL
                   },
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
    userIsSender,
    txHash = hash,
    ispending = ispending
)
