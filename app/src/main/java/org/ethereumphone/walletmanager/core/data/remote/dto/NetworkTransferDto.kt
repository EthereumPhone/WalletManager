package org.ethereumphone.walletmanager.core.data.remote.dto

data class NetworkTransferDto(
    val id: Int,
    val jsonrpc: String,
    val result: Result
) {
    data class Result(
        val transfers: List<Transfer>
    )

    data class Transfer(
        val asset: String,
        val blockNum: String,
        val category: String,
        val erc1155Metadata: Any,
        val erc721TokenId: String,
        val from: String,
        val hash: String,
        val rawContract: RawContract,
        val to: String,
        val tokenId: String,
        val uniqueId: String,
        val value: String,
        val metadata: TransferMetadata
    )

    data class RawContract(
        val address: String,
        val decimal: String,
        val value: String
    )

    data class TransferMetadata(
        val blockTimestamp: String?= ""
    )
}








