package com.core

import androidx.room.Database
import androidx.room.RoomDatabase
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.dao.TransferDao
import com.core.database.model.TransferEntity
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenMetadataEntity

@Database(
    entities = [
        TransferEntity::class,
        TokenMetadataEntity::class,
        TokenBalanceEntity::class
    ],
    version = 1
)

abstract class WmDatabase: RoomDatabase() {
    abstract val transferDao: TransferDao
    abstract val tokenBalanceDao: TokenBalanceDao
    abstract val tokenMetadataDao: TokenMetadataDao
}