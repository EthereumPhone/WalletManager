package com.core.database.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.core.database.DatabaseCallbacks
import com.core.database.WmDatabase
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
        return try {
            // First attempt: try to build the database
            buildDatabase(context, moshi)
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Failed to migrate database, deleting and recreating", e)
            // If migration fails, delete the database and recreate it
            context.deleteDatabase("wm-database")
            buildDatabase(context, moshi)
        }
    }
    
    private fun buildDatabase(
        context: Context,
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
            
        // Don't add any migrations - we want destructive migration for all versions
        // if (withMigrations) {
        //     builder.addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        // }
        
        return builder
            // Add callback for token seeding on database creation/open
            .addCallback(DatabaseCallbacks.createTokenSeedingCallback(context))
            // Enable destructive migration as fallback for all versions
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

}