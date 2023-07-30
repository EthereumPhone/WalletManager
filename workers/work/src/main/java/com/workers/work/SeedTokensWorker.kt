package com.workers.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.UpdateTokensUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
import org.ethereumphone.walletsdk.WalletSDK
import java.util.concurrent.TimeUnit

@HiltWorker
class SeedTokensWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateTokenUseCase: UpdateTokensUseCase,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val userDataRepository: UserDataRepository


) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        while (userDataRepository.userData.first().walletAddress == "") {
            // just wait
            Log.d("worker-TEst", "asdasdasd")
        }

        val address = userDataRepository.userData.first()
        Log.d("Worker Debug", address.walletAddress)

        try {
            updateTokenUseCase(address.walletAddress)
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