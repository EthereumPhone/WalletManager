package org.ethereumphone.walletmanager.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.ethereumphone.walletmanager.core.database.dao.TokenExchangeDao
import org.ethereumphone.walletmanager.core.database.dao.TokenMetadataDao
import org.ethereumphone.walletmanager.core.database.dao.TransferDao
import org.ethereumphone.walletmanager.core.database.dao.WalletDataDao
import org.ethereumphone.walletmanager.core.database.model.WalletDataEntity
import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenExchangeEntity
import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenMetadataEntity
import org.ethereumphone.walletmanager.core.database.model.TransferEntity
import org.ethereumphone.walletmanager.core.database.util.Converters

@Database(
    entities = [
        TokenExchangeEntity::class,
        TokenMetadataEntity::class,
        TransferEntity::class,
        WalletDataEntity::class
               ],
    version = 1
)

@TypeConverters(Converters::class)
abstract class WmDatabase: RoomDatabase() {
    abstract fun tokenExchangeDao(): TokenExchangeDao
    abstract fun tokenMetadataDao(): TokenMetadataDao
    abstract fun walletDataDao(): WalletDataDao
    abstract fun transferDao(): TransferDao
}