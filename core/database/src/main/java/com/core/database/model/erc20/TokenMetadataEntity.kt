package com.core.database.model.erc20

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.core.model.TokenMetadata

@Entity(
    tableName = "token_metadata",
    primaryKeys = ["contractAddress", "chainId"],
    foreignKeys = [
        ForeignKey(
            entity = TokenGroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["groupId"])
    ]
)
data class TokenMetadataEntity(
    val contractAddress: String,
    val chainId: Int,
    val decimals: Int,
    val name: String,
    val symbol: String,
    val logo: String?,
    val swappable: Boolean = false,
    val groupId: String? = null
)


fun TokenMetadataEntity.asExternalModel(): TokenMetadata {
    return TokenMetadata(
        contractAddress, decimals, name, symbol, logo, chainId, swappable
    )
}