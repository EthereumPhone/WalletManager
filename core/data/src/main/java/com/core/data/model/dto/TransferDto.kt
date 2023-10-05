package com.core.data.model.dto

import com.core.database.model.TransferEntity
import com.squareup.moshi.Json
import kotlinx.datetime.Instant

data class TransferJsonResponse(
    val id: Int,
    val jsonrpc: String,
    @Json(name = "result")
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
    val tokenId: String?,
    val uniqueId: String,
    val value: String?,
    val metadata: TransferMetadata,
    val ispending: Boolean
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

    fun asEntity(
        chainId: Int,
        userIsSender: Boolean,
    ): TransferEntity {
        return TransferEntity(
            asset = asset.orEmpty(),
            blockNum = blockNum,
            category = category,
            chainId = chainId,
            erc1155Metadata = erc1155Metadata?.map { metadata ->
                com.core.database.model.Erc1155MetadataObject(
                    tokenId = metadata.tokenId,
                    value = metadata.value
                )
            } ?: emptyList(),
            erc721TokenId = erc721TokenId.orEmpty(),
            from = from,
            hash = hash,
            rawContract = rawContract.run {
                com.core.database.model.RawContract(
                    address = address.orEmpty(),
                    decimal = decimal.orEmpty(),
                    value = value.orEmpty()
                )
            },
            to = to,
            tokenId = tokenId?: "",
            uniqueId = uniqueId,
            value = value?.toDoubleOrNull() ?: 0.0,
            blockTimestamp = Instant.parse(metadata.blockTimestamp),
            userIsSender = userIsSender,
            ispending = ispending // not pending
        )
    }
}


