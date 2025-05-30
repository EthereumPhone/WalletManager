package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.core.database.model.erc20.TokenMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenMetadataDao {

    @Query("SELECT * FROM token_metadata")
    fun getTokensMetadata(): Flow<List<TokenMetadataEntity>>

    @Query("SELECT * FROM token_metadata WHERE symbol == :symbol")
    fun getTokensMetadata(symbol: String): Flow<List<TokenMetadataEntity>>

    @Query("SELECT * FROM token_metadata WHERE contractAddress IN (:contractAddresses)")
    fun getTokenMetadata(contractAddresses: List<String>): Flow<List<TokenMetadataEntity>>

    @Query("SELECT * FROM token_metadata WHERE chainId == :chainId")
    fun getTokenMetadata(chainId: Int): Flow<List<TokenMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun upsertTokensMetadata(tokenMetadata: List<TokenMetadataEntity>)
}