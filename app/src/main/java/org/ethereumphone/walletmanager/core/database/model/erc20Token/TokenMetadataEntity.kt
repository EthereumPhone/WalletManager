package org.ethereumphone.walletmanager.core.database.model.erc20Token

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ethereumphone.walletmanager.core.domain.model.TokenMetadata

@Entity(tableName = "token_metadata")
data class TokenMetadataEntity(
    @PrimaryKey
    val contractAddress: String,
    val symbol: String,
    val chainId: Int,
    val name: String,
    val decimals: Int,
    val logoURI: String
) {
    fun toTokenMetadata(): TokenMetadata {
        return TokenMetadata(
            chainId,
            contractAddress,
            name,
            symbol,
            decimals,
            logoURI
        )
    }
}