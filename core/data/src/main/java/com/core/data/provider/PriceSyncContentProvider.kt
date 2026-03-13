package com.core.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.core.data.repository.TokenExchangeRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

/**
 * ContentProvider that allows external apps to trigger a price sync.
 *
 * Usage from another app:
 * ```
 * val result = contentResolver.call(
 *     Uri.parse("content://com.walletmanager.pricesync.provider"),
 *     "syncPrices",
 *     null,
 *     null
 * )
 * val success = result?.getBoolean("success", false) ?: false
 * ```
 */
class PriceSyncContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "PriceSyncProvider"
        const val AUTHORITY = "com.walletmanager.pricesync.provider"
        const val METHOD_SYNC_PRICES = "syncPrices"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PriceSyncEntryPoint {
        fun tokenExchangeRepository(): TokenExchangeRepository
    }

    private lateinit var exchangeRepository: TokenExchangeRepository

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        val entryPoint = EntryPointAccessors.fromApplication(
            ctx.applicationContext,
            PriceSyncEntryPoint::class.java
        )
        exchangeRepository = entryPoint.tokenExchangeRepository()
        Log.d(TAG, "PriceSyncContentProvider initialized")
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        val result = Bundle()
        if (method == METHOD_SYNC_PRICES) {
            Log.d(TAG, "syncPrices requested by external app")
            try {
                runBlocking {
                    exchangeRepository.fetchAllExchanges()
                }
                result.putBoolean("success", true)
                Log.d(TAG, "syncPrices completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "syncPrices failed", e)
                result.putBoolean("success", false)
                result.putString("error", e.message)
            }
        } else {
            result.putBoolean("success", false)
            result.putString("error", "Unknown method: $method")
        }
        return result
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<out String>?): Int = 0
}
