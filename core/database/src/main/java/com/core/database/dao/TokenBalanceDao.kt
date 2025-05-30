package com.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.core.database.model.erc20.CompositeToken
import com.core.database.model.erc20.TokenBalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenBalanceDao {
    @Query("SELECT * FROM token_balance")
    fun getTokenBalances(): Flow<List<TokenBalanceEntity>>

    @Query("SELECT * FROM token_balance WHERE contractAddress IN (:contractAddresses)")
    fun getTokenBalances(contractAddresses: List<String>): Flow<List<TokenBalanceEntity>>

    @Query("SELECT * FROM token_balance WHERE chainId == :chainId")
    fun getTokenBalances(chainId: Int): Flow<List<TokenBalanceEntity>>

    @Transaction
    @Query("""SELECT * FROM token_metadata""")
    fun getCompositeTokens(): Flow<List<CompositeToken>>

    @Transaction
    @Query("""
        SELECT * FROM token_metadata
        GROUP BY symbol
    """)
    fun getCompositeTokensGroupedBySymbol(): Flow<Map<String, List<CompositeToken>>>

    @Upsert
    fun upsertTokenBalances(tokenBalance: List<TokenBalanceEntity>)
}