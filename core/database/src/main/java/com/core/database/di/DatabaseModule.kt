package com.core.database.di

import android.content.Context
import androidx.room.Room
import com.core.database.WmDatabase
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
    ): WmDatabase = Room.databaseBuilder(
        context,
        WmDatabase::class.java,
        "wm-database"
    )
        .addTypeConverter(Erc1155MetadataConverter(MoshiJsonConverter(moshi)))
        .addTypeConverter(RawContractConverter(MoshiJsonConverter(moshi)))
        .build()
}