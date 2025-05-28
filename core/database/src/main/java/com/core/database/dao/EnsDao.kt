package com.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.core.database.model.EnsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnsDao {
    
    @Query("SELECT * FROM ens_cache WHERE address = :address")
    suspend fun getEnsForAddress(address: String): EnsEntity?
    
    @Query("SELECT * FROM ens_cache WHERE address IN (:addresses)")
    suspend fun getEnsForAddresses(addresses: List<String>): List<EnsEntity>
    
    @Query("SELECT * FROM ens_cache")
    fun getAllEnsEntries(): Flow<List<EnsEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEns(ensEntity: EnsEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleEns(ensEntities: List<EnsEntity>)
    
    @Update
    suspend fun updateEns(ensEntity: EnsEntity)
    
    @Query("DELETE FROM ens_cache WHERE address = :address")
    suspend fun deleteEnsForAddress(address: String)
    
    @Query("DELETE FROM ens_cache")
    suspend fun deleteAllEns()
    
    @Query("SELECT COUNT(*) FROM ens_cache")
    suspend fun getEnsCount(): Int
} 