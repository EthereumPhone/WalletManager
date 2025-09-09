package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.core.database.model.erc20.CompositeTokenGroup
import com.core.database.model.erc20.CompositeTokenGroupWithExchange
import com.core.database.model.erc20.CompositeTokenWithExchange
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenBridgeEntity
import com.core.database.model.erc20.TokenGroupEntity
import com.core.database.model.erc20.TokenMetadataEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow

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
    
    /**
     * Get a token with its balance and latest exchange rate.
     * This is more efficient than loading all exchange history.
     */
    @Query("""
        SELECT 
            tm.*,
            tb.*,
            te.id as exchange_id,
            te.address as exchange_address,
            te.symbol as exchange_symbol,
            te.chainId as exchange_chainId,
            te.currency as exchange_currency,
            te.value as exchange_value,
            te.timestamp as exchange_timestamp
        FROM token_metadata tm
        LEFT JOIN token_balance tb ON tm.contractAddress = tb.contractAddress 
            AND tm.chainId = tb.chainId
        LEFT JOIN (
            SELECT te1.* FROM token_exchange te1
            WHERE te1.id = (
                SELECT MAX(id) 
                FROM token_exchange 
                WHERE (address = :contractAddress AND chainId = :chainId)
                   OR (address IS NULL AND symbol = (SELECT symbol FROM token_metadata WHERE contractAddress = :contractAddress AND chainId = :chainId))
            )
        ) te ON (
            -- Join by address for ERC20 tokens
            (te.address = tm.contractAddress AND te.chainId = tm.chainId) 
            OR 
            -- Join by symbol for native tokens (where address is null)
            (te.address IS NULL AND te.symbol = tm.symbol)
        )
        WHERE tm.contractAddress = :contractAddress AND tm.chainId = :chainId
    """)
    suspend fun getTokenWithLatestExchange(contractAddress: String, chainId: Int): CompositeTokenWithExchange?
    
    /**
     * Get all tokens in a group with their latest exchange rates.
     */
    @Query("""
        SELECT 
            tm.*,
            tb.*,
            te.id as exchange_id,
            te.address as exchange_address,
            te.symbol as exchange_symbol,
            te.chainId as exchange_chainId,
            te.currency as exchange_currency,
            te.value as exchange_value,
            te.timestamp as exchange_timestamp
        FROM token_metadata tm
        LEFT JOIN token_balance tb ON tm.contractAddress = tb.contractAddress 
            AND tm.chainId = tb.chainId
        LEFT JOIN (
            SELECT te1.* FROM token_exchange te1
            WHERE te1.id IN (
                SELECT MAX(id) 
                FROM token_exchange 
                GROUP BY COALESCE(address, symbol), COALESCE(chainId, -1)
            )
        ) te ON (
            -- Join by address for ERC20 tokens
            (te.address = tm.contractAddress AND te.chainId = tm.chainId) 
            OR 
            -- Join by symbol for native tokens (where address is null)
            (te.address IS NULL AND te.symbol = tm.symbol)
        )
        WHERE tm.groupId = :groupId
    """)
    suspend fun getTokensInGroupWithLatestExchange(groupId: String): List<CompositeTokenWithExchange>
    
    /**
     * Get all tokens in a group with their latest exchange rates as a Flow.
     * This implementation uses a simpler approach for better Room Flow invalidation.
     */
    @Transaction
    suspend fun observeAllTokensInGroupWithLatestExchangeImpl(groupId: String): List<CompositeTokenWithExchange> {
        return getTokensInGroupWithLatestExchange(groupId)
    }
    
    /**
     * Observable wrapper that combines token data with exchange rate changes.
     * This ensures proper Flow emission when exchange rates are updated.
     */
    fun observeAllTokensInGroupWithLatestExchange(groupId: String): Flow<List<CompositeTokenWithExchange>> {
        return combine(
            getTokensInGroup(groupId),
            observeExchangeTableChanges()
        ) { _, _ ->
            groupId
        }.flatMapLatest { 
            flow {
                // Whenever either tokens or exchange rates change, fetch the latest data
                emit(observeAllTokensInGroupWithLatestExchangeImpl(groupId))
            }
        }
    }



    /**
     * Get all token groups with their tokens and latest exchange rates.
     * This provides complete data including prices in a single query.
     */
    @Transaction
    suspend fun getAllTokenGroupsWithExchange(): List<CompositeTokenGroupWithExchange> {
        val groups = getAllCompositeTokenGroupsSync()
        return groups.map { group ->
            val tokensWithExchange = getTokensInGroupWithLatestExchange(group.tokenGroup.groupId)
            CompositeTokenGroupWithExchange(
                tokenGroup = group.tokenGroup,
                tokensWithExchange = tokensWithExchange
            )
        }
    }
    
    /**
     * Get active token groups (with balances) including latest exchange rates.
     */
    @Transaction
    suspend fun getActiveTokenGroupsWithExchange(): List<CompositeTokenGroupWithExchange> {
        val groups = getActiveCompositeTokenGroups().first()
        return groups.map { group ->
            val tokensWithExchange = getTokensInGroupWithLatestExchange(group.tokenGroup.groupId)
            CompositeTokenGroupWithExchange(
                tokenGroup = group.tokenGroup,
                tokensWithExchange = tokensWithExchange
            )
        }
    }
    

    /**
     * Observe token exchanges to ensure Flow emission when exchange rates change
     */
    @Query("SELECT COUNT(*) FROM token_exchange")
    fun observeExchangeTableChanges(): Flow<Int>

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeAllActiveTokenGroupsWithExchange(): Flow<List<CompositeTokenGroupWithExchange>> {
        // Combine the active groups flow with exchange table changes to ensure proper invalidation
        return combine(
            getActiveCompositeTokenGroups(),
            observeExchangeTableChanges()
        ) { groups, _ ->
            groups
        }.flatMapLatest { groups ->
            if (groups.isEmpty()) {
                flowOf(emptyList())
            } else {
                // Create a Flow for each group that combines the group with its tokens
                val groupFlows = groups.map { group ->
                    observeAllTokensInGroupWithLatestExchange(group.tokenGroup.groupId).map { tokensWithExchange ->
                        CompositeTokenGroupWithExchange(
                            tokenGroup = group.tokenGroup,
                            tokensWithExchange = tokensWithExchange
                        )
                    }
                }
                
                // Combine all group flows into a single Flow
                combine(groupFlows) { it.toList() }
            }
        }
    }


}