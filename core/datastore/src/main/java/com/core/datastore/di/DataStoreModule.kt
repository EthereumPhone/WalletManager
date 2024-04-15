package com.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.core.datastore.ExclusionListProtoSerializer
import com.core.datastore.UserPreferencesSerializer
import com.core.datastore.proto.UserPreferences
import com.core.datastore.proto.ExclusionListProto
import com.core.datastore.proto.ExclusionListProto.ExclusionList
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providesUserPreferencesDataStore(
        @ApplicationContext context: Context,
        userPreferencesSerializer: UserPreferencesSerializer
    ): DataStore<UserPreferences> =
        DataStoreFactory.create(
            serializer = userPreferencesSerializer,
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ) {
            context.dataStoreFile("user_preferences.pb")
        }

    @Provides
    @Singleton
    fun providesExclustionDataStore(
        @ApplicationContext context: Context,
        exclusionListProtoSerializer: ExclusionListProtoSerializer
    ): DataStore<ExclusionList> =
        DataStoreFactory.create(
            serializer = exclusionListProtoSerializer,
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ) {
            context.dataStoreFile("exclusion_list.pb.pb")
        }
}