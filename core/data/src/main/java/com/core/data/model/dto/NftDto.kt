package com.core.data.model.dto

import com.core.model.NFT
import com.core.model.NftTokenType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from Alchemy's getNFTsForOwner API
 */
@JsonClass(generateAdapter = true)
data class NftResponse(
    @Json(name = "ownedNfts")
    val ownedNfts: List<OwnedNft>,
    @Json(name = "totalCount")
    val totalCount: Int,
    @Json(name = "pageKey")
    val pageKey: String?
)

@JsonClass(generateAdapter = true)
data class OwnedNft(
    @Json(name = "contract")
    val contract: NftContract,
    @Json(name = "tokenId")
    val tokenId: String,
    @Json(name = "tokenType")
    val tokenType: String,
    @Json(name = "name")
    val name: String?,
    @Json(name = "description")
    val description: String?,
    @Json(name = "image")
    val image: NftImage?,
    @Json(name = "collection")
    val collection: NftCollection?,
    @Json(name = "balance")
    val balance: String?
)

@JsonClass(generateAdapter = true)
data class NftContract(
    @Json(name = "address")
    val address: String,
    @Json(name = "name")
    val name: String?,
    @Json(name = "symbol")
    val symbol: String?,
    @Json(name = "tokenType")
    val tokenType: String?,
    @Json(name = "openSeaMetadata")
    val openSeaMetadata: OpenSeaMetadata?
)

@JsonClass(generateAdapter = true)
data class NftImage(
    @Json(name = "cachedUrl")
    val cachedUrl: String?,
    @Json(name = "thumbnailUrl")
    val thumbnailUrl: String?,
    @Json(name = "pngUrl")
    val pngUrl: String?,
    @Json(name = "originalUrl")
    val originalUrl: String?
)

@JsonClass(generateAdapter = true)
data class NftCollection(
    @Json(name = "name")
    val name: String?,
    @Json(name = "slug")
    val slug: String?,
    @Json(name = "floorPrice")
    val floorPrice: NftFloorPrice?
)

@JsonClass(generateAdapter = true)
data class NftFloorPrice(
    @Json(name = "marketplace")
    val marketplace: String?,
    @Json(name = "floorPrice")
    val floorPrice: Double?,
    @Json(name = "priceCurrency")
    val priceCurrency: String?
)

@JsonClass(generateAdapter = true)
data class OpenSeaMetadata(
    @Json(name = "floorPrice")
    val floorPrice: Double?,
    @Json(name = "collectionName")
    val collectionName: String?,
    @Json(name = "collectionSlug")
    val collectionSlug: String?,
    @Json(name = "imageUrl")
    val imageUrl: String?
)

/**
 * Extension function to convert OwnedNft DTO to domain model
 * Note: isSoulbound is set to false by default and will be checked on-chain via EIP-5192
 */
fun OwnedNft.toDomainModel(chainId: Int, ethPriceUsd: Double = 0.0): NFT {
    val floorPriceEth = contract.openSeaMetadata?.floorPrice 
        ?: collection?.floorPrice?.floorPrice
    
    return NFT(
        contractAddress = contract.address,
        tokenId = tokenId,
        chainId = chainId,
        name = name ?: contract.name ?: "Unknown NFT",
        description = description ?: "",
        imageUrl = image?.cachedUrl ?: image?.pngUrl ?: image?.originalUrl,
        thumbnailUrl = image?.thumbnailUrl ?: image?.cachedUrl,
        collectionName = collection?.name 
            ?: contract.openSeaMetadata?.collectionName 
            ?: contract.name 
            ?: "Unknown Collection",
        tokenType = when (tokenType.uppercase()) {
            "ERC721" -> NftTokenType.ERC721
            "ERC1155" -> NftTokenType.ERC1155
            else -> NftTokenType.UNKNOWN
        },
        floorPriceEth = floorPriceEth,
        floorPriceUsd = floorPriceEth?.let { it * ethPriceUsd },
        balance = balance?.toIntOrNull() ?: 1,
        isSoulbound = false // Checked on-chain via EIP-5192 when needed
    )
}
