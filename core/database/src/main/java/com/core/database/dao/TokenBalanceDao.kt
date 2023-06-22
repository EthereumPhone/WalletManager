package com.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.core.database.model.erc20.TokenBalanceEntity

@Dao
interface TokenBalanceDao {
    @Query("SELECT * FROM token_balance")
    fun getAllBalances(): List<TokenBalanceEntity>
    @Query("SELECT * FROM token_balance WHERE contractAddress == :contractAddress")
    fun getBalanceByContractAddress(contractAddress: String): TokenBalanceEntity?

    @Query("SELECT * FROM token_balance WHERE contractAddress IN (:contractAddresses)")
    fun getBalancesByContractAddresses(contractAddresses: List<String>): List<TokenBalanceEntity>

    @Upsert
    fun upsertTokenBalances(tokenBalance: List<TokenBalanceEntity>)
}