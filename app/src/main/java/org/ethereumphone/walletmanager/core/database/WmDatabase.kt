package org.ethereumphone.walletmanager.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.ethereumphone.walletmanager.core.database.dao.TokenExchangeDao
import org.ethereumphone.walletmanager.core.database.dao.TokenMetadataDao
import org.ethereumphone.walletmanager.core.database.model.TokenExchangeEntity
import org.ethereumphone.walletmanager.core.database.model.TokenMetadataEntity

@Database(
    entities = [
        TokenExchangeEntity::class,
        TokenMetadataEntity::class
               ],
    version = 1
)
abstract class WmDatabase: RoomDatabase() {
    abstract fun tokenExchangeDao(): TokenExchangeDao
    abstract fun tokenMetadataDao(): TokenMetadataDao
}