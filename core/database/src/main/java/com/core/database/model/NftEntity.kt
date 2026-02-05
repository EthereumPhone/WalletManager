package com.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.core.model.NFT
import com.core.model.NftTokenType

/**
 * Database entity for storing NFTs
 */
@Entity(tableName = "nfts")
data class NftEntity(
    @PrimaryKey
    val id: String, // contractAddress + tokenId + chainId
    val contractAddress: String,
    val tokenId: String,
    val chainId: Int,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val collectionName: String,
    val tokenType: String,
    val floorPriceEth: Double?,
    val floorPriceUsd: Double?,
    val balance: Int,
    val isSoulbound: Boolean = false // EIP-5192: Non-transferable token
)

/**
 * Convert entity to domain model
 */
fun NftEntity.asDomainModel(): NFT = NFT(
    contractAddress = contractAddress,
    tokenId = tokenId,
    chainId = chainId,
    name = name,
    description = description,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    collectionName = collectionName,
    tokenType = when (tokenType) {
        "ERC721" -> NftTokenType.ERC721
        "ERC1155" -> NftTokenType.ERC1155
        else -> NftTokenType.UNKNOWN
    },
    floorPriceEth = floorPriceEth,
    floorPriceUsd = floorPriceUsd,
    balance = balance,
    isSoulbound = isSoulbound
)

/**
 * Convert domain model to entity
 */
fun NFT.asEntity(): NftEntity = NftEntity(
    id = "${contractAddress}_${tokenId}_${chainId}",
    contractAddress = contractAddress,
    tokenId = tokenId,
    chainId = chainId,
    name = name,
    description = description,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    collectionName = collectionName,
    tokenType = tokenType.name,
    floorPriceEth = floorPriceEth,
    floorPriceUsd = floorPriceUsd,
    balance = balance,
    isSoulbound = isSoulbound
)
