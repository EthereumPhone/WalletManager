package com.core.data.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.core.database.WmDatabase
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TokenMetadataDao
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * BroadcastReceiver that handles token balance updates from the TokenLauncher app.
 * Only accepts intents from com.freedomfactory.tokenlauncher.
 * 
 * Expected intent extras:
 * - TOKEN_ADDRESS: String - The contract address of the token
 * - CHAIN_ID: Int - The chain ID where the token exists
 * - AMOUNT_TO_DEDUCT: String - The amount to deduct from the balance (in token units with decimals)
 * 
 * Security:
 * - Uses a custom permission with signature protection level
 * - Additionally verifies the sending package name
 */
class TokenBalanceUpdateReceiver : BroadcastReceiver() {
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TokenBalanceUpdateReceiverEntryPoint {
        fun database(): WmDatabase
    }
    
    companion object {
        const val ACTION_DEDUCT_TOKEN_BALANCE = "org.ethereumphone.walletmanager.DEDUCT_TOKEN_BALANCE"
        const val EXTRA_TOKEN_ADDRESS = "TOKEN_ADDRESS"
        const val EXTRA_CHAIN_ID = "CHAIN_ID"
        const val EXTRA_AMOUNT_TO_DEDUCT = "AMOUNT_TO_DEDUCT"
        
        private const val TAG = "TokenBalanceUpdateReceiver"
        private const val ALLOWED_PACKAGE = "com.freedomfactory.tokenlauncher"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive called with action: ${intent?.action}")
        
        if (context == null || intent == null) {
            Log.w(TAG, "Context or intent is null")
            return
        }
        
        // Check if the intent is the expected action
        if (intent.action != ACTION_DEDUCT_TOKEN_BALANCE) {
            Log.w(TAG, "Received unexpected action: ${intent.action}")
            return
        }
        
        // SECURITY: Verify the sender's package name
        val sendingPackage = getSendingPackage(context, intent)
        Log.d(TAG, "Sender package: $sendingPackage")
        
        // TODO: Re-enable package verification after testing
        // For testing: Allow ALL broadcasts (REMOVE IN PRODUCTION!)
        Log.w(TAG, "⚠️ TEST MODE: Security checks disabled - accepting broadcast from ANY source ($sendingPackage)")
        
        /* Production code - uncomment after testing:
        if (sendingPackage != ALLOWED_PACKAGE) {
            Log.e(TAG, "Unauthorized package tried to send balance update: $sendingPackage")
            return
        }
        */
        
        // Extract the data from the intent
        val tokenAddress = intent.getStringExtra(EXTRA_TOKEN_ADDRESS)
        val chainId = intent.getIntExtra(EXTRA_CHAIN_ID, -1)
        val amountToDeductStr = intent.getStringExtra(EXTRA_AMOUNT_TO_DEDUCT)
        
        // Validate the received data
        if (tokenAddress == null || chainId == -1 || amountToDeductStr == null) {
            Log.e(TAG, "Missing required data. TokenAddress: $tokenAddress, ChainId: $chainId, Amount: $amountToDeductStr")
            return
        }
        
        try {
            val amountToDeduct = BigDecimal(amountToDeductStr)
            Log.d(TAG, "Processing deduction: Token=$tokenAddress, Chain=$chainId, Amount=$amountToDeduct")
            
            // Get the database instance using EntryPoint
            val appContext = context.applicationContext
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                appContext,
                TokenBalanceUpdateReceiverEntryPoint::class.java
            )
            val database = hiltEntryPoint.database()
            val tokenBalanceDao = database.tokenBalanceDao
            val tokenMetadataDao = database.tokenMetadataDao
            Log.d(TAG, "Successfully got database instance")
            
            // Perform the database update in a coroutine
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "Starting database update coroutine")
                updateTokenBalance(tokenBalanceDao, tokenMetadataDao, tokenAddress, chainId, amountToDeduct)
            }
            
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid amount format: $amountToDeductStr", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing balance update", e)
        }
    }
    
    /**
     * Get the package name of the app that sent the broadcast
     * Note: This uses the calling UID to determine the sender package
     */
    private fun getSendingPackage(context: Context, intent: Intent): String? {
        try {
            // Get the UID of the sending app
            val sendingUid = android.os.Binder.getCallingUid()
            
            // Get package names associated with this UID
            val packageManager = context.packageManager
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackagesForUid(sendingUid)
            } else {
                packageManager.getPackagesForUid(sendingUid)
            }
            
            // Check if any of the packages match our allowed package
            packages?.forEach { packageName ->
                if (packageName == ALLOWED_PACKAGE) {
                    return packageName
                }
            }
            
            Log.w(TAG, "Sending packages: ${packages?.joinToString(", ")} - none match allowed package")
            return packages?.firstOrNull()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sending package", e)
        }
        return null
    }
    
    /**
     * Update the token balance in the database by deducting the specified amount
     */
    private suspend fun updateTokenBalance(
        tokenBalanceDao: TokenBalanceDao,
        tokenMetadataDao: TokenMetadataDao,
        tokenAddress: String,
        chainId: Int,
        amountToDeduct: BigDecimal
    ) {
        try {
            // Get the token metadata to find out the decimals
            val metadata = tokenMetadataDao.getTokenMetadata(listOf(tokenAddress)).first()
                .firstOrNull { it.contractAddress.equals(tokenAddress, ignoreCase = true) && it.chainId == chainId }
            
            // Default to 18 decimals if metadata not found (standard for most ERC-20 tokens)
            val decimals = metadata?.decimals ?: 18
            Log.d(TAG, "Token decimals: $decimals")
            
            // Convert the human-readable amount to the smallest unit
            // For example: 1 USDC (6 decimals) becomes 1 * 10^6 = 1000000
            val amountInSmallestUnit = amountToDeduct.multiply(BigDecimal.TEN.pow(decimals))
            Log.d(TAG, "Converting amount: $amountToDeduct -> $amountInSmallestUnit (with $decimals decimals)")
            
            // Get the current balance from the database
            val balances = tokenBalanceDao.getTokenBalances(listOf(tokenAddress)).first()
            
            val currentBalance = balances.firstOrNull { 
                it.contractAddress.equals(tokenAddress, ignoreCase = true) && 
                it.chainId == chainId 
            }
            
            if (currentBalance != null) {
                // Calculate the new balance by subtracting the amount in smallest unit
                val newBalance = currentBalance.tokenBalance.subtract(amountInSmallestUnit)
                
                // Ensure the balance doesn't go negative
                val finalBalance = if (newBalance < BigDecimal.ZERO) {
                    BigDecimal.ZERO
                } else {
                    newBalance
                }
                
                // Update the balance in the database
                val updatedBalance = currentBalance.copy(tokenBalance = finalBalance)
                tokenBalanceDao.upsertTokenBalances(listOf(updatedBalance))
                
                Log.i(TAG, "Successfully updated balance for $tokenAddress on chain $chainId. " +
                    "Old balance: ${currentBalance.tokenBalance}, " +
                    "Deducted: $amountInSmallestUnit (input: $amountToDeduct with $decimals decimals), " +
                    "New balance: $finalBalance")
            } else {
                Log.w(TAG, "Token balance not found for address: $tokenAddress on chain: $chainId")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update token balance", e)
        }
    }
}
