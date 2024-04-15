package com.core.datastore.di

import com.core.datastore.ExclusionListProtoSerializer
import com.core.datastore.UserPreferencesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SerializerModule {

    @Singleton
    @Provides
    fun provideExclusionListProtoSerializer(): ExclusionListProtoSerializer {
        return ExclusionListProtoSerializer
    }

    @Singleton
    @Provides
    fun provideUserPreferencesSerializer(): UserPreferencesSerializer {
        return UserPreferencesSerializer()
    }
}
