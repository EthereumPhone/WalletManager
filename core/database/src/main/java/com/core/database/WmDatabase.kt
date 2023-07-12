package com.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenMetadataDao
import com.core.database.dao.TransferDao
import com.core.database.model.TransferEntity
import com.core.database.model.erc20.TokenBalanceEntity
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.database.util.BigDecimalTypeConverter
import com.core.database.util.Erc1155MetadataConverter
import com.core.database.util.InstantConverter
import com.core.database.util.RawContractConverter

@Database(
    entities = [
        TransferEntity::class,
        TokenMetadataEntity::class,
        TokenBalanceEntity::class
    ],
    version = 1
)

@TypeConverters(
    InstantConverter::class,
    Erc1155MetadataConverter::class,
    RawContractConverter::class,
    BigDecimalTypeConverter::class
)
abstract class WmDatabase: RoomDatabase() {
    abstract val transferDao: TransferDao
    abstract val tokenBalanceDao: TokenBalanceDao
    abstract val tokenMetadataDao: TokenMetadataDao
}