package org.ethereumphone.walletmanager.core.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import org.ethereumphone.walletmanager.core.database.dao.WalletDataDao
import org.ethereumphone.walletmanager.core.util.DefaultNetworks


@HiltWorker
class SeedNetworkDataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val walletDataDao: WalletDataDao
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        Log.d(TAG, "Network Seed Started")
        try {
            val defaultNetworks = DefaultNetworks.values().asList().map { it.walletDataEntity }
            walletDataDao.insertNetworks(defaultNetworks)
            Result.success()
        } catch (e : Exception) {
            Log.e(TAG, "Error seeding database", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SeedNetworkData"

        fun startSeedNetworkDataWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setInputData(SeedNetworkDataWorker::class.delegatedData())
            .build()
    }


}

