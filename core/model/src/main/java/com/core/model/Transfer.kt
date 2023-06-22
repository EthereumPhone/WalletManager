package com.core.model

import java.time.Instant

data class Transfer(
    val asset: String,
    val chainId: Int,
    val category: EntryCategory,
    val erc1155Metadata: List<Erc1155MetadataObject>,
    val erc721TokenId: String,
    val from: String,
    val rawContract: RawContract,
    val to: String,
    val tokenId: String,
    val value: Double,
    val blockTimestamp: Instant
) {
    data class Erc1155MetadataObject(
        val tokenId: String,
        val value: String
    )

    data class RawContract(
        val address: String?,
        val decimal: String?,
        val value: String?
    )
}

enum class EntryCategory(val query: String) {
    EXTERNAL("external"),
    INTERNAL("internal"),
    ERC20("erc20"),
    ERC721("erc721"),
    ERC1155("erc1155"),
    SPECIAL_NFT("specialnft")
}