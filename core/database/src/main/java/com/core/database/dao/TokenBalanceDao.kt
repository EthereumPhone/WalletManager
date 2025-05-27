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
    @Query("""SELECT * FROM token_balance""")
    fun getCompositeTokens(): Flow<List<CompositeToken>>

    @Transaction
    @Query("""
        SELECT tb.* FROM token_balance tb
        LEFT JOIN token_metadata tm ON tb.contractAddress = tm.contractAddress
        LEFT JOIN (
            SELECT te.* 
            FROM token_exchange te
            INNER JOIN (
                SELECT symbol, MAX(timestamp) as max_timestamp
                FROM token_exchange
                GROUP BY symbol
            ) latest ON te.symbol = latest.symbol AND te.timestamp = latest.max_timestamp
        ) te ON tm.symbol = te.symbol
        WHERE tb.contractAddress = :contractAddress
    """)
    fun getCompositeToken(contractAddress: String): Flow<CompositeToken>

    @Upsert
    fun upsertTokenBalances(tokenBalance: List<TokenBalanceEntity>)
}