package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.core.database.model.erc20.TokenExchangeEntity
import com.core.model.TokenExchange
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface TokenExchangeDao {
    @Upsert
    suspend fun insertTokenExchange(tokenExchange: TokenExchangeEntity)

    @Upsert
    suspend fun insertAll(tokenExchanges: List<TokenExchangeEntity>)

    @Query("SELECT * FROM token_exchange WHERE symbol = :symbol AND currency = :currency ORDER BY timestamp DESC LIMIT 1")
    suspend fun getExchangeBySymbol(symbol: String, currency: String): TokenExchangeEntity?

    @Query("SELECT * FROM token_exchange WHERE symbol = :symbol AND currency = :currency ORDER BY timestamp DESC LIMIT 1")
    fun getExchangeBySymbolFlow(symbol: String, currency: String): Flow<TokenExchangeEntity?>

    @Query("SELECT DISTINCT symbol FROM token_exchange")
    suspend fun getAllUniqueSymbols(): List<String>

    @Query("SELECT DISTINCT currency FROM token_exchange")
    suspend fun getAllCurrencies(): List<String>

    @Query("SELECT * FROM token_exchange WHERE symbol = :symbol ORDER BY timestamp DESC")
    suspend fun getAllExchangesBySymbol(symbol: String): List<TokenExchangeEntity>

    @Query("SELECT * FROM token_exchange WHERE timestamp > :fromTimestamp")
    suspend fun getExchangesSince(fromTimestamp: Instant): List<TokenExchangeEntity>

    @Query("DELETE FROM token_exchange WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldExchanges(beforeTimestamp: Instant)

    @Query("SELECT * FROM token_exchange WHERE address = :address")
    suspend fun getByAddress(address: String): List<TokenExchangeEntity>

    @Query("DELETE FROM token_exchange")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsertAll(values: List<TokenExchangeEntity>)

    @Query("SELECT * FROM token_exchange WHERE chainId = :chainId")
    suspend fun getExchangesByChainId(chainId: Int): List<TokenExchangeEntity>

    @Query("SELECT * FROM token_exchange WHERE chainId = :chainId AND symbol = :symbol")
    suspend fun getExchangesByChainIdAndSymbol(chainId: Int, symbol: String): List<TokenExchangeEntity>

    @Query("SELECT * FROM token_exchange WHERE chainId = :chainId AND symbol = :symbol")
    fun observeExchangesByChainIdAndSymbol(chainId: Int, symbol: String): Flow<List<TokenExchangeEntity>>

    @Query("""
        SELECT * FROM token_exchange 
        WHERE address = :address AND chainId = :chainId 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    suspend fun getLatestExchangeByAddressAndChain(address: String, chainId: Int): TokenExchangeEntity?

    @Query("""
        SELECT * FROM token_exchange 
        WHERE address = :address AND chainId = :chainId 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
     fun observeLatestExchangeByAddressAndChain(address: String, chainId: Int): Flow<TokenExchangeEntity?>

    @Query("""
        SELECT * FROM token_exchange 
        WHERE address = :address 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    suspend fun getLatestExchangeByAddress(address: String): TokenExchangeEntity?

    @Query("""
        SELECT * FROM token_exchange 
        WHERE address = :address 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    fun observeLatestExchangeByAddress(address: String): Flow<TokenExchangeEntity?>

    @Query("""
        SELECT * FROM token_exchange 
        WHERE (address = :address OR (address IS NULL AND symbol = :symbol))
        AND (chainId = :chainId OR chainId IS NULL)
        AND LOWER(currency) = LOWER(:currency)
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    suspend fun getBestMatchingExchange(
        address: String,
        symbol: String,
        chainId: Int,
        currency: String = "USD"
    ): TokenExchangeEntity?

    @Upsert
    suspend fun insertAllExchanges(exchangeEntities: List<TokenExchangeEntity>)

}