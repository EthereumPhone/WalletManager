package org.ethereumphone.walletmanager.core.workers.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ethereumphone.walletmanager.core.workers.util.GenericTokenMetadataParser
import org.ethereumphone.walletmanager.core.workers.util.JsonParser
import org.ethereumphone.walletmanager.core.workers.util.MoshiParser
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
