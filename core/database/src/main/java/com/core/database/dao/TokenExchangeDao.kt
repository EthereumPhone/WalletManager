package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.core.database.model.erc20.TokenExchangeEntity
import com.core.model.TokenExchange
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenExchangeDao {

    @Query(
        """
            SELECT * FROM token_exchange
            LIMIT :limit
        """
    )
    fun getExchanges(limit: Int = -1): Flow<List<TokenExchange>>

    @Query(
        """
            SELECT * FROM token_exchange
            WHERE symbol = :symbol
            ORDER BY timestamp DESC
            LIMIT 1
        """
    )
    fun getLatestExchange(symbol: String): Flow<TokenExchange?>

    @Query(
        """
            SELECT * FROM token_exchange
            WHERE symbol = :symbol
            ORDER BY timestamp
        """
    )
    fun getHistoricalExchange(symbol: String): Flow<List<TokenExchange>>



    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertExchange(exchangeEntity: TokenExchangeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllExchanges(exchangeEntities: List<TokenExchangeEntity>)

}