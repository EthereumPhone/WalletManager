package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.core.database.model.erc20.CompositeTokenGroup
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenBridgeEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenGroupDao {
    
    /**
     * Get a single token group with all its tokens and balances.
     */
    @Transaction
    @Query("SELECT * FROM token_group WHERE groupId = :groupId")
    suspend fun getCompositeTokenGroup(groupId: String): CompositeTokenGroup?
    
    /**
     * Get all token groups with their tokens and balances.
     */
    @Transaction
    @Query("SELECT * FROM token_group")
    fun getAllCompositeTokenGroups(): Flow<List<CompositeTokenGroup>>
    
    /**
     * Get all token groups that have at least one token with a non-zero balance.
     */
    @Transaction
    @Query("""
        SELECT DISTINCT tg.* FROM token_group tg
        INNER JOIN token_metadata tm ON tg.groupId = tm.groupId
        INNER JOIN token_balance tb ON tm.contractAddress = tb.contractAddress 
            AND tm.chainId = tb.chainId
        WHERE tb.tokenBalance > 0
    """)
    fun getActiveCompositeTokenGroups(): Flow<List<CompositeTokenGroup>>
    
    /**
     * Get token groups ordered by total balance (requires post-processing).
     */
    @Transaction
    @Query("SELECT * FROM token_group")
    suspend fun getAllCompositeTokenGroupsSync(): List<CompositeTokenGroup>

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