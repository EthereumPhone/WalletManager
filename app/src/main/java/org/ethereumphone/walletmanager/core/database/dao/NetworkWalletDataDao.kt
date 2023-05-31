package org.ethereumphone.walletmanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.ethereumphone.walletmanager.core.database.model.NetworkInformationEntity
import org.ethereumphone.walletmanager.core.model.data.NetworkInformation
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkWalletDataDao {
    @Query(
        value = """ 
            SELECT * FROM network_information WHERE chainID = :chainID 
            ORDER BY chainID DESC
        """
    )
    suspend fun getNetworkBalance(chainID: Int): Flow<NetworkInformationEntity>

    /**
     * Updates or Inserts [networkInformation] in the db via primary key
     */
    @Upsert
    suspend fun upsertNetworkInformation(networkInformation: NetworkInformation)
}