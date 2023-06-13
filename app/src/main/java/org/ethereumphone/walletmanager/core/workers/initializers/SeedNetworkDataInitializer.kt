package org.ethereumphone.walletmanager.core.workers.initializers

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import org.ethereumphone.walletmanager.core.workers.SeedNetworkDataWorker

object SeedNetworkData {

    fun initialize(context: Context) {
        AppInitializer.getInstance(context)
            .initializeComponent(SeedNetworkDataInitializer::class.java)
    }
}

internal const val SeedNetworkDataName = "SeedNetworkDataName"

class SeedNetworkDataInitializer: Initializer<SeedNetworkData> {

    override fun create(context: Context): SeedNetworkData {
        WorkManager.getInstance(context).apply {
            enqueueUniqueWork(
                SeedNetworkDataName,
                ExistingWorkPolicy.KEEP,
                SeedNetworkDataWorker.startSeedNetworkDataWork()
            )
        }

        return SeedNetworkData
    }


    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(WorkManagerInitializer::class.java)

}