package com.workers.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SeedNetworkBalanceWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "SeedNetworkData"

        fun startSeedNetworkBalanceWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setInputData(SeedNetworkBalanceWorker::class.delegatedData())
            .build()
    }
}