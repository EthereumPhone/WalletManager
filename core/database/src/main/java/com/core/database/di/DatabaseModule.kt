package com.core.database.di

import android.content.Context
import androidx.room.Room
import com.core.WmDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DatabaseModule {

    @Provides
    @Singleton
    fun provideWmDatabase(
        @ApplicationContext context: Context
    ): WmDatabase = Room.databaseBuilder(
        context,
        WmDatabase::class.java,
        "wm-database"
    ).build()
}