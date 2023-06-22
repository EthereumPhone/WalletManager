package com.core.database.model.erc20

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.core.model.TokenMetadata

@Entity("token_metadata")
data class TokenMetadataEntity(
    @PrimaryKey
    val contractAddress: String,
    val decimals: Int,
    val name: String,
    val symbol: String,
    val logo: String,
    val chainId: Int
)


fun TokenMetadataEntity.asExternalModel(): TokenMetadata {
    return TokenMetadata(
        contractAddress, decimals, name, symbol, logo, chainId
    )
}