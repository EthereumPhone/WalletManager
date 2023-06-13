package org.ethereumphone.walletmanager.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ethereumphone.walletmanager.core.data.remote.dto.TransferDto
import org.ethereumphone.walletmanager.core.domain.model.Transfer
import java.time.Instant

@Entity("transfer")
data class TransferEntity(
    @PrimaryKey
    val uniqueId: String,
    val asset: String,
    val chainId: Int,
    val blockNum: String,
    val category: EntryCategory,
    val erc1155Metadata: List<TransferDto.Erc1155MetadataObject>,
    val erc721TokenId: String,
    val from: String,
    val hash: String,
    val rawContract: TransferDto.RawContract,
    val to: String,
    val tokenId: String,
    val value: Double,
    val blockTimestamp: Instant
) {
    fun toTransfer(): Transfer {
        return Transfer(
            asset,
            chainId,
            category,
            erc1155Metadata,
            erc721TokenId,
            from,
            rawContract,
            to,
            tokenId,
            value,
            blockTimestamp
        )
    }
}

enum class EntryCategory(val query: String) {
    EXTERNAL("external"),
    INTERNAL("internal"),
    ERC20("erc20"),
    ERC721("erc721"),
    ERC1155("erc1155"),
    SPECIAL_NFT("specialnft")
}