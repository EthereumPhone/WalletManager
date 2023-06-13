package org.ethereumphone.walletmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenExchangeEntity

@Dao
interface TokenExchangeDao {

    @Query("""
        SELECT * FROM token_exchange
        ORDER BY price DESC
    """)
    fun getAllTokenExchanges(): List<TokenExchangeEntity>

    @Query("""
        SELECT * FROM token_exchange
        WHERE symbol == :symbol
    """)
    fun getTokenExchange(symbol: String): TokenExchangeEntity


    @Upsert
    fun upsertTokenExchange(tokenExchange: TokenExchangeEntity)

}