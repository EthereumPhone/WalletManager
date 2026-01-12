package com.core.model

/**
 * Represents an NFT (Non-Fungible Token) owned by the user
 */
data class NFT(
    val contractAddress: String,
    val tokenId: String,
    val chainId: Int,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val collectionName: String,
    val tokenType: NftTokenType,
    val floorPriceEth: Double?,
    val floorPriceUsd: Double?,
    val balance: Int = 1 // For ERC1155, can be > 1
)

enum class NftTokenType {
    ERC721,
    ERC1155,
    UNKNOWN
}
