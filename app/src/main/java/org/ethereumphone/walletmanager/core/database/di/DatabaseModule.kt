package org.ethereumphone.walletmanager.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ethereumphone.walletmanager.core.database.WmDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesWmDatabase(
        @ApplicationContext context: Context,
    ): WmDatabase = Room.databaseBuilder(
        context,
        WmDatabase::class.java,
        "wm-database"
    ).build()
}