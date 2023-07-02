package com.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.core.database.model.erc20.TokenBalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenBalanceDao {
    @Query("SELECT * FROM token_balance")
    fun getTokenBalances(): Flow<List<TokenBalanceEntity>>

    @Query("SELECT * FROM token_balance WHERE contractAddress IN (:contractAddresses)")
    fun getTokenBalances(contractAddresses: List<String>): Flow<List<TokenBalanceEntity>>

    @Upsert
    fun upsertTokenBalances(tokenBalance: List<TokenBalanceEntity>)
}