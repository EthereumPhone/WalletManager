package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.core.database.model.TransferEntity
import com.core.model.EntryCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfer")
    fun getTransfers(): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfer WHERE chainId == :chainId")
    fun getTransfers(chainId: Int): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfer WHERE category IN (:categories)")
    fun getTransfers(categories: List<String>): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfer WHERE chainID == :chainId AND category IN (:categories)")
    fun getTransfers(chainId: Int, categories: List<String>): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransfers(transfers: List<TransferEntity>)

}