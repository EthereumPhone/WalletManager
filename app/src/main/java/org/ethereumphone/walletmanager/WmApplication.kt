package org.ethereumphone.walletmanager

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.UpdateTokensUseCase
import com.workers.work.SeedTokensWorker
import dagger.hilt.android.HiltAndroidApp
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject

@HiltAndroidApp
class WmApplication: Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.VERBOSE)
            .setWorkerFactory(workerFactory)
            .build()
}

/*
class SeedWorkerFactory @Inject constructor(
    private val updateTokensUseCase: UpdateTokensUseCase,
    private val userDataRepository: UserDataRepository,
    private val transferRepository: TransferRepository
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = SeedTokensWorker(
        appContext,
        workerParameters,
        updateTokensUseCase,
        transferRepository,
        userDataRepository
    )
}

 */
