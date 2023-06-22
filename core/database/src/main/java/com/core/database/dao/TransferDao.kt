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
    fun getAllTransfers(): List<TransferEntity>

    @Query("SELECT * FROM transfer WHERE chainId == :chainId")
    fun getAllTransfersByChain(chainId: Int): List<TransferEntity>

    @Query("SELECT * FROM transfer WHERE category IN (:categories)")
    fun getAllTransfersByCategories(categories: List<String>): List<TransferEntity>

    @Query("SELECT * FROM transfer WHERE chainID == :chainId AND category IN (:categories)")
    fun getAllTransfersByChainAndCategories(chainId: Int, categories: List<String>): List<TransferEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransfers(transfers: List<TransferEntity>)

}