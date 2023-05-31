package org.ethereumphone.walletmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.ethereumphone.walletmanager.core.domain.model.TokenExchange

@Dao
interface TokenExchangeDao {

    @Query("""
        SELECT * FROM token_exchange
        ORDER BY price DESC
    """)
    fun getAllTokenExchanges(): List<TokenExchange>

    @Query("""
        SELECT * FROM token_exchange
        WHERE symbol == :symbol
    """)
    fun getTokenExchange(symbol: String): TokenExchange


    @Upsert
    fun upsertTokenExchange(tokenExchange: TokenExchange)

}