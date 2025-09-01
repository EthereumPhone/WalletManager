package com.core.database.model.erc20

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Represents bridge relationships between tokens.
 * This is a many-to-many relationship table.
 */
@Entity(
    tableName = "token_bridge",
    primaryKeys = ["sourceChainId", "sourceAddress", "targetChainId", "targetAddress"],
    foreignKeys = [
        ForeignKey(
            entity = TokenMetadataEntity::class,
            parentColumns = ["contractAddress", "chainId"],
            childColumns = ["sourceAddress", "sourceChainId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TokenMetadataEntity::class,
            parentColumns = ["contractAddress", "chainId"],
            childColumns = ["targetAddress", "targetChainId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sourceChainId", "sourceAddress"]),
        Index(value = ["targetChainId", "targetAddress"]),
        Index(value = ["groupId"])
    ]
)
data class TokenBridgeEntity(
    val sourceChainId: Int,
    val sourceAddress: String,
    val targetChainId: Int,
    val targetAddress: String,
    val groupId: String, // References TokenGroupEntity
)