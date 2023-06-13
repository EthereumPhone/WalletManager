package org.ethereumphone.walletmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import org.ethereumphone.walletmanager.core.database.model.WalletDataEntity

@Dao
interface WalletDataDao {
    @Query(
        """ 
            SELECT * FROM network_information WHERE chainID = :chainID 
            ORDER BY chainID DESC
        """
    )
    suspend fun getNetworkDataByChainId(chainID: Int): WalletDataEntity

    @Query("SELECT * FROM network_information")
    suspend fun getAllNetworksData(): List<WalletDataEntity>

    /**
     * Updates or Inserts [networkInformation] in the db via primary key
     */
    @Upsert
    suspend fun upsertNetworkInformation(networkInformation: WalletDataEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNetworks(entities: List<WalletDataEntity>)
}