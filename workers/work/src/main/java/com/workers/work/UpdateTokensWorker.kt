package com.workers.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import com.core.data.repository.ClaimDataRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TokenExchangeRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.UserDataRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateTokensWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userDataRepository: UserDataRepository,
    private val exchangeRepository: TokenExchangeRepository,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val tokenBalanceRepository: TokenBalanceRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val claimDataRepository: ClaimDataRepository,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "UpdateTokensWorker"
        const val UPDATE_WORK_NAME = "token_update_work"
        
        fun createUpdateWork() =
            OneTimeWorkRequestBuilder<UpdateTokensWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
    }


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting token update work")
            
            // Wait until a non-blank wallet address is available (suspends, no busy-wait)
            val address = userDataRepository.userData.first { it.walletAddress.isNotBlank() }.walletAddress
            
            // Use supervisorScope to handle individual failures without cancelling other operations
            supervisorScope {
                // Refresh claim token metadata and balances (non-blocking, adds metadata and checks on-chain balances for claim tokens)
                val claimDataJob = async {
                    try {
                        Log.d(TAG, "Refreshing claim token metadata and balances...")
                        claimDataRepository.refreshClaimTokens(address)
                        Log.d(TAG, "Claim token metadata and balance refresh completed")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error refreshing claim tokens (non-fatal)", e)
                        // Don't throw - claim data is supplementary
                    }
                }
                
                // Run balance and metadata operations in parallel
                val balanceJob = async { 
                    try {
                        fetchBalances(address)
                        Log.d(TAG, "Balance fetch completed")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching balances", e)
                        throw e
                    }
                }
                
                val metadataJob = async { 
                    try {
                        fetchMetadata()
                        Log.d(TAG, "Metadata fetch completed")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching metadata", e)
                        throw e
                    }
                }
                
                // Wait for claim data to complete (non-blocking errors already handled)
                claimDataJob.await()
                
                // Wait for both operations to complete
                try {
                    balanceJob.await()
                    metadataJob.await()
                } catch (e: Exception) {
                    // If either fails, we still want to return retry
                    Log.e(TAG, "Error in parallel operations", e)
                    return@supervisorScope Result.retry()
                }
                
                // Only run exchange fetch if both previous operations succeeded
                try {
                    exchangeRepository.fetchAllExchanges()
                    Log.d(TAG, "Exchange fetch completed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching exchanges", e)
                    // Exchange failure might be less critical, could potentially return success
                    // depending on business requirements
                    return@supervisorScope Result.retry()
                }
            }
            
            Log.d(TAG, "Token update work completed successfully")
            Result.success()
            
        } catch (e: CancellationException) {
            // Rethrow cancellation exceptions to properly handle worker cancellation
            Log.d(TAG, "Token update work was cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in token update work", e)
            
            // Determine if we should retry based on the exception type
            when (e) {
                is java.net.UnknownHostException,
                is java.net.SocketTimeoutException,
                is java.io.IOException -> {
                    Log.d(TAG, "Network error, will retry")
                    Result.retry()
                }
                else -> {
                    // For unexpected errors, fail the work
                    Log.e(TAG, "Unrecoverable error, failing work")
                    Result.failure()
                }
            }
        }
    }






    private suspend fun fetchBalances(address: String) = supervisorScope {
        // Use supervisorScope so one failure doesn't cancel the other
        val tokenBalanceJob = launch { 
            try {
                Log.d(TAG, "Fetching token balances for address: $address")
                tokenBalanceRepository.refreshTokensBalances(address)
                Log.d(TAG, "Token balances fetched successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching token balances", e)
                throw e
            }
        }
        
        val networkBalanceJob = launch { 
            try {
                Log.d(TAG, "Fetching network balance for address: $address")
                networkBalanceRepository.refreshNetworkBalance(address)
                Log.d(TAG, "Network balance fetched successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching network balance", e)
                throw e
            }
        }
        
        // Wait for both to complete
        tokenBalanceJob.join()
        networkBalanceJob.join()
    }

    private suspend fun fetchMetadata() {
        try {
            // Get balances without metadata just once (not observe indefinitely)
            val tokenBalances = tokenBalanceRepository.observeBalancesWithoutMetadata().first()
            
            if (tokenBalances.isEmpty()) {
                Log.d(TAG, "No tokens without metadata found")
                return
            }
            
            val groupedByChain = tokenBalances.groupBy { it.chainId }
            
            // Use supervisorScope to ensure one chain failure doesn't affect others
            supervisorScope {
                groupedByChain.forEach { (chainId, tokens) ->
                    launch {
                        try {
                            val addresses = tokens
                                .filter { it.chainId.toString() != it.contractAddress }
                                .map { it.contractAddress }
                            Log.d(TAG, "Fetching metadata for ${addresses.size} tokens on chain $chainId")
                            tokenMetadataRepository.refreshTokensMetadata(addresses, chainId)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching metadata for chain $chainId", e)
                            // Don't rethrow - let other chains continue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchMetadata", e)
            throw e
        }
    }

}
