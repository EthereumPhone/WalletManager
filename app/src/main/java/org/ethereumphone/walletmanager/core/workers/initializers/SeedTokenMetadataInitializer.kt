package org.ethereumphone.walletmanager.core.workers.initializers

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import org.ethereumphone.walletmanager.core.workers.SeedTokenMetadataWorker

object SeedTokenMetadata {
    fun initialize(context: Context) {
        AppInitializer.getInstance(context)
            .initializeComponent(SeedTokenMetadataInitializer::class.java)
    }
}

internal const val SeedTokenMetadataName = "SeedTokenMetadataWorkName"


class SeedTokenMetadataInitializer: Initializer<SeedTokenMetadata> {
    override fun create(context: Context): SeedTokenMetadata {
        WorkManager.getInstance(context).apply {
            enqueueUniqueWork(
                SeedTokenMetadataName,
                ExistingWorkPolicy.KEEP,
                SeedTokenMetadataWorker.startSeedTokenMetadataWork()
            )
        }

        return SeedTokenMetadata
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(WorkManagerInitializer::class.java)
}