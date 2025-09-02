package com.core.database.di

import android.content.Context
import androidx.room.Room
import com.core.database.DatabaseCallbacks
import com.core.database.DatabaseMigrations
import com.core.database.WmDatabase
import com.core.database.dao.EnsDao
import com.core.database.util.BigDecimalTypeConverter
import com.core.database.util.Erc1155MetadataConverter
import com.core.database.util.MoshiJsonConverter
import com.core.database.util.RawContractConverter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWmDatabase(
        @ApplicationContext context: Context,
        moshi: Moshi
    ): WmDatabase {
        val builder = Room.databaseBuilder(
            context,
            WmDatabase::class.java,
            "wm-database"
        )
            .addTypeConverter(Erc1155MetadataConverter(MoshiJsonConverter(moshi)))
            .addTypeConverter(RawContractConverter(MoshiJsonConverter(moshi)))
            .addTypeConverter(BigDecimalTypeConverter())
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            
        // Check if database already exists (for existing installations)
        val dbFile = context.getDatabasePath("wm-database")
        if (!dbFile.exists()) {
            // For new installations, use pre-populated database
            builder.createFromAsset("database/wm_database.db")
        } else {
            // For existing installations, we'll handle token seeding via migration
            // or a callback if needed
            builder.addCallback(DatabaseCallbacks.TOKEN_SEEDING_CALLBACK)
        }
        
        // Uncomment for development/testing if you want to start fresh:
        // builder.fallbackToDestructiveMigration()
        
        return builder.build()
    }

}