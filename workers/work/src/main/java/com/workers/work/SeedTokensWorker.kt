package com.workers.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.UpdateTokensUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit


@HiltWorker
class SeedTokensWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateTokenUseCase: UpdateTokensUseCase,
    private val transferRepository: TransferRepository,
    private val userDataRepository: UserDataRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        while (userDataRepository.userData.first().walletAddress == "") {
            // just wait
        }

        val address = userDataRepository.userData.first()
        Log.d("Worker Debug", address.walletAddress)

        try {
            coroutineScope {
                launch  { transferRepository.refreshTransfers(address.walletAddress) }
                launch { updateTokenUseCase(address.walletAddress) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }

        Result.success()
    }
    companion object {

        fun startSeedNetworkBalanceWork() =
            OneTimeWorkRequestBuilder<SeedTokensWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

    }
}