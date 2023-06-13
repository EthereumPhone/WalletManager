package org.ethereumphone.walletmanager.core.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ethereumphone.walletmanager.core.domain.model.TokenMetadata
import org.ethereumphone.walletmanager.core.domain.repository.TokenMetadataRepository
import org.ethereumphone.walletmanager.utils.JsonParser
import java.lang.Exception

@HiltWorker
class SeedTokenMetadataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val jsonParser: JsonParser,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Database Seed started")
        try {
            val lines = applicationContext.resources.openRawResource(
                applicationContext.resources.getIdentifier(
                    "uniswap_token_list",
                    "raw",
                    applicationContext.packageName
                )
            ).bufferedReader().use{ it.readLine()}

            if(!lines.isNullOrEmpty()) {
                tokenMetadataRepository.insertAllToken(getTokenList(lines))
                Result.success()
            } else {
                Log.e(TAG, "Error seeding database - no valid filename")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding database", e)
            Result.failure()
        }


    }
    fun getTokenList(jsonString: String): List<TokenMetadata> = jsonParser.fromJson<ArrayList<TokenMetadata>>(
        jsonString,
        object : TypeToken<ArrayList<TokenMetadata>>(){}.type
    ) ?: emptyList()


    companion object {
        private const val TAG = "SeedTokenMetadata"

        fun startSeedTokenMetadataWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setInputData(SeedTokenMetadataWorker::class.delegatedData())
            .build()
    }


}