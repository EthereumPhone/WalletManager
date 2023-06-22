package com.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.core.database.model.erc20.TokenMetadataEntity

@Dao
interface TokenMetadataDao {

    @Query("SELECT * FROM token_metadata")
    fun getAllTokenMetadata(): List<TokenMetadataEntity>

    @Query("SELECT * FROM token_metadata WHERE symbol == :symbol")
    fun getTokensMetadataBySymbol(symbol: String): List<TokenMetadataEntity>

    @Query("SELECT * FROM token_metadata WHERE contractAddress == :contractAddress")
    fun getTokenMetadataByContractAddress(contractAddress: String): TokenMetadataEntity

    @Upsert
    fun upsertTokenMetadata(tokenMetadata: List<TokenMetadataEntity>)
}