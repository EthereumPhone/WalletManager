package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenBridgeEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenGroupDao {

    @Query("SELECT * FROM token_group WHERE groupId = :groupId")
    suspend fun getGroupedToken(groupId: String): TokenGroupEntity?

    @Query("""
        SELECT tm.* FROM token_metadata tm
        WHERE tm.groupId == :groupId
    """)
    fun getTokensInGroup(groupId: String): Flow<List<TokenMetadataEntity>>

    @Query("""
        SELECT tm.*, tb.tokenBalance FROM token_metadata tm
        LEFT JOIN token_balance tb ON tm.contractAddress = tb.contractAddress
            AND tm.chainId = tb.chainId
        WHERE tm.groupId = :groupId
    """)
    fun getTokenBalancesInGroup(groupId: String): Flow<List<TokenBalanceEntity>>

    @Upsert
    suspend fun upsertTokenGroup(tokenGroupEntity: TokenGroupEntity)

    @Upsert
    suspend fun upsertTokenGroups(tokenGroupEntities: List<TokenGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBridgeInfo(bridges: List<TokenBridgeEntity>)

    @Query("""
        UPDATE token_metadata 
        SET groupId = :groupId 
        WHERE contractAddress = :address AND chainId = :chainId
    """)
    suspend fun updateTokenGroupId(address: String, chainId: Int, groupId: String)

    @Transaction
    suspend fun createTokenGroupWithBridges(
        group: TokenGroupEntity,
        bridges: List<TokenBridgeEntity>,
        tokenUpdates: List<TokenMetadataEntity>
    ) {
        upsertTokenGroup(group)
        insertBridgeInfo(bridges)
        tokenUpdates.forEach { token ->
            updateTokenGroupId(token.contractAddress, token.chainId, group.groupId)
        }
    }
}