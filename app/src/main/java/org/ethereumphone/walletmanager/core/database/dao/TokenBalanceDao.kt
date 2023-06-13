package org.ethereumphone.walletmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenBalanceEntity

@Dao
interface TokenBalanceDao {

    @Query("SELECT * FROM token_balance")
    fun getAllTokenBalances(): List<TokenBalanceEntity>
    @Query("SELECT * FROM token_balance WHERE chainID == :chainId")
    fun getAllTokenBalancesByChain(chainId: Int): List<TokenBalanceEntity>
    @Query("SELECT * FROM token_balance WHERE contractAddress == :contractAddress")
    fun getTokenBalanceByContractAddress(contractAddress: String): TokenBalanceEntity
    @Upsert
    fun upserTokenBalance(tokenBalance: TokenBalanceEntity)
}