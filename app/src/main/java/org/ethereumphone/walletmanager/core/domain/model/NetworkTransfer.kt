package org.ethereumphone.walletmanager.core.domain.model

import org.ethereumphone.walletmanager.core.data.remote.dto.NetworkTransferDto
import java.time.LocalDateTime

data class NetworkTransfer(
    val asset: String,
    val blockNum: String,
    val category: String,
    val erc1155Metadata: Any,
    val erc721TokenId: String,
    val from: String,
    val hash: String,
    val rawContract: NetworkTransferDto.RawContract,
    val to: String,
    val tokenId: String,
    val value: Double,
    val blockTimestamp: LocalDateTime
)