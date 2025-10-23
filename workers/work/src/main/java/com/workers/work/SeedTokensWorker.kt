package com.workers.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import com.core.data.remote.EnsApi
import com.core.data.repository.EnsRepository
import com.core.data.repository.TokenExchangeRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.UpdateTokensUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit


private val WORKERLOG: String = "SEED_WORKER"

@HiltWorker
class SeedTokensWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateTokenUseCase: UpdateTokensUseCase,
    private val transferRepository: TransferRepository,
    private val userDataRepository: UserDataRepository,
    private val exchangeRepository: TokenExchangeRepository,
    private val ensApi: EnsApi,
    private val ensRepository: EnsRepository,
    private val tokenMetadataRepository: TokenMetadataRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Wait until a non-blank wallet address is available (suspends without busy-waiting)
        val userData = userDataRepository.userData.first { it.walletAddress.isNotBlank() }
        val address = userData.walletAddress
        Log.d("Worker Debug", address)

        try {
            coroutineScope {
                // First, refresh transfers and update tokens
                launch { transferRepository.refreshTransfers(address) }
                launch { updateTokenUseCase(address) }
            }

            // Reconcile cross-chain token grouping after metadata/balances updates
            tokenMetadataRepository.reconcileTokenGroups()
            
            // fetch ens and exchange rate
            coroutineScope {

                launch { resolveEnsForTransfers() }

                Log.d(WORKERLOG, "fetching prices")

                launch { exchangeRepository.fetchAllExchanges() }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.failure()
        }

        Result.success()
    }
    
    private suspend fun resolveEnsForTransfers() {
        try {
            // Get all transfers to extract unique addresses
            val transfers = transferRepository.getTransfers().first()
            
            // Extract unique addresses from transfers
            val uniqueAddresses = transfers
                .flatMap { listOf(it.from, it.to) }
                .distinct()
                .filter { it.isNotBlank() && it.startsWith("0x", ignoreCase = true) }
            
            Log.d("ENS Resolution", "Found ${uniqueAddresses.size} unique addresses to resolve")
            
            // Get already cached ENS names
            val cachedEnsMap = ensRepository.getEnsNames(uniqueAddresses)
            val cachedAddresses = cachedEnsMap.keys.toSet()
            
            // Filter addresses that need resolution
            val addressesToResolve = uniqueAddresses.filter { 
                it.lowercase() !in cachedAddresses 
            }
            
            Log.d("ENS Resolution", "Need to resolve ${addressesToResolve.size} new addresses")
            
            // Resolve ENS names in parallel batches
            val batchSize = 10
            addressesToResolve.chunked(batchSize).forEach { batch ->
                coroutineScope {
                    val resolvedPairs = batch.map { address ->
                        async {
                            try {
                                val ensName = ensApi.resolveAddressToEns(address)
                                Log.d("ENS Resolution", "Resolved $address to $ensName")
                                address to ensName
                            } catch (e: Exception) {
                                Log.e("ENS Resolution", "Failed to resolve $address", e)
                                address to null
                            }
                        }
                    }.awaitAll()
                    
                    // Save resolved ENS names to repository
                    ensRepository.saveEnsNames(resolvedPairs)
                }
            }
            
            Log.d("ENS Resolution", "ENS resolution completed")
            
        } catch (e: Exception) {
            Log.e("ENS Resolution", "Error during ENS resolution", e)
        }
    }
    
    companion object {

        const val SEED_WORK_NAME = "seed_tokens_work"

        fun startSeedNetworkBalanceWork() =
            OneTimeWorkRequestBuilder<SeedTokensWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

    }
}