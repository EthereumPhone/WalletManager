package com.workers.work

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.domain.UpdateTokensUseCase
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.workers.work.util.UniswapToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

@HiltWorker
class SeedUniswapTokensWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val moshi: Moshi,
    private val tokenMetadataRepository: TokenMetadataRepository

) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            val tokens = withContext(Dispatchers.Default) {
                val listType = Types.newParameterizedType(List::class.java, UniswapToken::class.java)
                val adapter: JsonAdapter<List<UniswapToken>> = moshi.adapter(listType)

                val inputStream = appContext.resources.openRawResource(R.raw.uniswap_token_list)
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                adapter.fromJson(jsonString) ?: emptyList()
            }

            withContext(Dispatchers.IO) {
                tokenMetadataRepository.insertTokenMetadata(
                    tokens.map {
                        TokenMetadataEntity(
                            contractAddress = it.address,
                            decimals = it.decimals,
                            name = it.name,
                            symbol = it.symbol,
                            logo = it.logoURI,
                            chainId = it.chainId
                        )
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }

        Result.success()
    }

    companion object {
        fun startSeedUniswapTokensWork() =
            OneTimeWorkRequestBuilder<SeedUniswapTokensWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()
    }
}