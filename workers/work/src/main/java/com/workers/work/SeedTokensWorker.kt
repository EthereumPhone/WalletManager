package com.workers.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.core.domain.UpdateTokensUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ethereumphone.walletsdk.WalletSDK

@HiltWorker
class SeedTokensWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateTokenUseCase: UpdateTokensUseCase,
    private val walletSDK: WalletSDK


) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        updateTokenUseCase(walletSDK.getAddress())
        Result.success()
    }

    companion object {

        fun startSeedNetworkBalanceWork() =
            OneTimeWorkRequestBuilder<SeedTokensWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

    }
}