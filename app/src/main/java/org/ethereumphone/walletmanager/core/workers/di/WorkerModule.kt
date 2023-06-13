package org.ethereumphone.walletmanager.core.workers.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ethereumphone.walletmanager.utils.JsonParser
import org.ethereumphone.walletmanager.utils.MoshiParser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Singleton
    @Provides
    fun provideJsonParser(moshi: Moshi): JsonParser {
        return MoshiParser(moshi)
    }
}
