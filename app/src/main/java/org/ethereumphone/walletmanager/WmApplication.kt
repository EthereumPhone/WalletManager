package org.ethereumphone.walletmanager

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.UpdateTokensUseCase
import com.workers.work.SeedTokensWorker
import com.workers.work.UpdateTokensWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.ethereumphone.walletsdk.WalletSDK
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class WmApplication: Application(), Configuration.Provider, DefaultLifecycleObserver, ImageLoaderFactory {
    
    companion object {
        private const val PREFS_NAME = "welcome_prefs"
        private const val KEY_APP_FIRST_LAUNCH = "appFirstLaunch"
        private const val KEY_SEED_COMPLETED = "seedWorkCompleted"
        private const val UPDATE_INTERVAL_MS = 90_000L // 1.5 minutes for token updates
        private const val TAG = "WmApplication"
    }
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var userDataRepository: UserDataRepository
    
    private val applicationScope = CoroutineScope(Dispatchers.IO)
    private var updateJob: Job? = null
    private var hasWalletAddress = false

    override fun onCreate() {
        super<Application>.onCreate()
        
        // Register lifecycle observer to track app foreground/background state
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Initialize workers early in the app lifecycle
        initializeWorkers()
    }
    
    private fun initializeWorkers() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_APP_FIRST_LAUNCH, true)
        
        // Check if we have a wallet address available
        applicationScope.launch {
            try {
                userDataRepository.userData.collect { userData ->
                    if (userData.walletAddress.isNotBlank()) {
                        Log.d(TAG, "Wallet address available, initializing workers")
                        
                        hasWalletAddress = true
                        
                        // Run SeedTokensWorker only on the very first app launch
                        seedDataIfAllowed()
                        
                        // If not first launch, also queue UpdateTokensWorker
                        if (!isFirstLaunch) {
                            val updateWork = UpdateTokensWorker.createUpdateWork()
                            WorkManager.getInstance(applicationContext)
                                .enqueueUniqueWork(
                                    UpdateTokensWorker.UPDATE_WORK_NAME,
                                    ExistingWorkPolicy.KEEP,
                                    updateWork
                                )
                        }
                        
                        // Mark that we've launched at least once
                        if (isFirstLaunch) {
                            prefs.edit().putBoolean(KEY_APP_FIRST_LAUNCH, false).apply()
                        }
                        
                        // Cancel collection after first valid address
                        return@collect
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing workers", e)
            }
        }
    }
    
    private fun seedDataIfAllowed() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasSeedCompleted = prefs.getBoolean(KEY_SEED_COMPLETED, false)
        
        if (!hasSeedCompleted) {
            Log.d(TAG, "First app launch - Starting SeedTokensWorker")
            val seedNetworkBalanceWork = SeedTokensWorker.startSeedNetworkBalanceWork()
            
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    SeedTokensWorker.SEED_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    seedNetworkBalanceWork
                )

            // Mark that seed work has been queued
            prefs.edit().putBoolean(KEY_SEED_COMPLETED, true).apply()
        } else {
            Log.d(TAG, "Skipping SeedTokensWorker - already completed on first launch")
        }
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.VERBOSE)
            .setWorkerFactory(workerFactory)
            .build()
    
    override fun newImageLoader(): ImageLoader {
        // Create OkHttpClient with longer timeouts for IPFS and other slow image sources
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                
                // Add headers required for IPFS gateways
                val newRequest = originalRequest.newBuilder()
                    .header("User-Agent", "WalletManager/1.0 (Android)")
                    .header("Accept", "image/*,*/*")
                    .build()
                
                Log.d(TAG, "Loading image from: ${newRequest.url}")
                try {
                    val response = chain.proceed(newRequest)
                    Log.d(TAG, "Image response code: ${response.code}, content-type: ${response.header("Content-Type")}")
                    response
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading image from ${newRequest.url}", e)
                    throw e
                }
            }
            .build()
        
        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .respectCacheHeaders(false) // Important for IPFS URLs
            .allowHardware(true) // Enable hardware bitmaps
            .logger(object : coil.util.Logger {
                override var level: Int = Log.DEBUG
                
                override fun log(tag: String, priority: Int, message: String?, throwable: Throwable?) {
                    when (priority) {
                        Log.VERBOSE -> Log.v(tag, message ?: "", throwable)
                        Log.DEBUG -> Log.d(tag, message ?: "", throwable)
                        Log.INFO -> Log.i(tag, message ?: "", throwable)
                        Log.WARN -> Log.w(tag, message ?: "", throwable)
                        Log.ERROR -> Log.e(tag, message ?: "", throwable)
                        else -> Log.d(tag, message ?: "", throwable)
                    }
                }
            })
            .build()
    }
    
    // Lifecycle observer methods - called when app enters/exits foreground
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "App entered foreground")
        
        // Only start periodic updates if we have a wallet address
        if (hasWalletAddress) {
            // Run an immediate update when entering foreground
            val updateWork = UpdateTokensWorker.createUpdateWork()
            WorkManager.getInstance(applicationContext)
                .enqueue(updateWork)
                
            // Start periodic updates
            startPeriodicTokenUpdates()
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App entered background")
        
        // Stop periodic updates when app goes to background
        stopPeriodicTokenUpdates()
    }
    
    private fun startPeriodicTokenUpdates() {
        // Cancel any existing job
        updateJob?.cancel()
        
        Log.d(TAG, "Starting periodic token updates")
        
        // Start a new coroutine job that runs every 1.5 minutes
        updateJob = applicationScope.launch {
            while (isActive) {
                // Wait for 1.5 minutes before next update
                delay(UPDATE_INTERVAL_MS)
                
                // Queue the update work
                val updateWork = UpdateTokensWorker.createUpdateWork()
                WorkManager.getInstance(applicationContext)
                    .enqueueUniqueWork(
                        UpdateTokensWorker.UPDATE_WORK_NAME,
                        ExistingWorkPolicy.KEEP,
                        updateWork
                    )
            }
        }
    }
    
    private fun stopPeriodicTokenUpdates() {
        Log.d(TAG, "Stopping periodic token updates")
        updateJob?.cancel()
        updateJob = null
    }
}
