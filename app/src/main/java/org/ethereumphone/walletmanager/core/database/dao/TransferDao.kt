package org.ethereumphone.walletmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.ethereumphone.walletmanager.core.database.model.EntryCategory
import org.ethereumphone.walletmanager.core.database.model.TransferEntity
import org.ethereumphone.walletmanager.core.domain.model.Transfer

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfer")
    fun getAllTransfers(): List<TransferEntity>

    @Query("SELECT * FROM transfer WHERE chainId == :chainId")
    fun getAllTransfersByChain(chainId: Int): List<TransferEntity>

    @Query("SELECT * FROM transfer WHERE category IN (:categories)")
    fun getAllTransfersByCategories(categories: List<EntryCategory>): List<TransferEntity>

    @Query("SELECT * FROM transfer WHERE chainID == :chainId AND category IN (:categories)")
    fun getAllTransfersByChainAndCategories(chainId: Int, categories: List<EntryCategory>): List<TransferEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTransfers(transfers: List<TransferEntity>)
}