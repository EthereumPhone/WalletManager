package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.core.database.model.ExchangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenExchangeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(exchangeEntity: ExchangeEntity)

    @Query("SELECT * FROM ExchangeEntity WHERE base = :base AND currency = :currency")
    fun getExchange(base: String, currency: String): Flow<ExchangeEntity?>

    @Query("SELECT * FROM ExchangeEntity WHERE base = :base")
    fun getExchangesByBase(base: String): Flow<List<ExchangeEntity>>

    @Query("DELETE FROM ExchangeEntity WHERE base = :base AND currency = :currency")
    fun deleteExchange(base: String, currency: String)
}