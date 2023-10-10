package com.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
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

    /**
     * Insert Entry into transfer table
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransfer(transfer: TransferEntity)

    /**
     * Delete Entry into transfer table
     */
//    @Query("DELETE FROM transfer " +
//            "WHERE " +
//            "ispending == :ispending AND " +
//            "userIsSender == :userIsSender AND " +
//            "value == :value AND " +
//            "chainId == :chainId AND " +
//            "toaddress == :toaddress LIMIT 1")
    @Query("DELETE FROM transfer WHERE uniqueId IN (SELECT uniqueId FROM transfer WHERE chainId = :chainId AND value = :value AND isPending = :ispending AND userIsSender = :userIsSender AND toAddress = :toaddress LIMIT 1)")
    suspend fun deleteTransfer(
        chainId: Int,
        value: Double,
        ispending: Boolean,
        userIsSender: Boolean,
        toaddress: String
    )

}