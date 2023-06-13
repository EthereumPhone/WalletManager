package org.ethereumphone.walletmanager.core.domain.model

import org.ethereumphone.walletmanager.core.data.remote.dto.TransferDto
import org.ethereumphone.walletmanager.core.database.model.EntryCategory
import java.time.Instant
import java.time.LocalDateTime

data class Transfer(
    val asset: String,
    val chainId: Int,
    val category: EntryCategory,
    val erc1155Metadata: List<TransferDto.Erc1155MetadataObject>,
    val erc721TokenId: String,
    val from: String,
    val rawContract: TransferDto.RawContract,
    val to: String,
    val tokenId: String,
    val value: Double,
    val blockTimestamp: Instant
)