package org.ethereumphone.walletmanager.core.data.remote.dto

import org.ethereumphone.walletmanager.BuildConfig
import org.ethereumphone.walletmanager.core.database.model.EntryCategory
import org.ethereumphone.walletmanager.core.database.model.TransferEntity
import java.time.Instant
import java.time.ZoneId

data class TransferJsonResponse(
    val id: Int,
    val jsonrpc: String,
    val result: Result
) {
    data class Result(
        val transfers: List<TransferDto>
    )
}

data class TransferDto(
    val asset: String?,
    val blockNum: String,
    val category: String,
    val erc1155Metadata: List<Erc1155MetadataObject>?,
    val erc721TokenId: String?,
    val from: String,
    val hash: String,
    val rawContract: RawContract,
    val to: String,
    val tokenId: String,
    val uniqueId: String,
    val value: String?,
    val metadata: TransferMetadata
) {
    data class RawContract(
        val address: String?,
        val decimal: String?,
        val value: String?
    )

    data class TransferMetadata(
        val blockTimestamp: String
    )

    data class Erc1155MetadataObject(
        val tokenId: String,
        val value: String
    )
    fun toTransferEntity(chainId: Int): TransferEntity {
        return TransferEntity(
            asset = asset?: "",
            blockNum = blockNum,
            category = EntryCategory.valueOf(category),
            chainId = chainId,
            erc1155Metadata = erc1155Metadata?: listOf(),
            erc721TokenId = erc721TokenId ?: "",
            from = from,
            hash = hash,
            rawContract = rawContract,
            to = to,
            tokenId = tokenId,
            uniqueId = uniqueId,
            value = value?.toDouble() ?: 0.0,
            blockTimestamp = Instant.parse(metadata.blockTimestamp)
        )
    }
}






