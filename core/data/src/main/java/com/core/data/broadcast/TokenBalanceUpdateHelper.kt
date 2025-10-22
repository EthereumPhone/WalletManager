package com.core.data.broadcast

import android.content.Context
import android.content.Intent
import android.util.Log
import java.math.BigDecimal

/**
 * Helper class for the TokenLauncher app to send token balance deduction intents.
 * 
 * This class demonstrates how the com.freedomfactory.tokenlauncher app should send
 * intents to update token balances in the WalletManager database.
 * 
 * IMPORTANT: The sending app (com.freedomfactory.tokenlauncher) must:
 * 1. Have the same signature as this app OR
 * 2. Request the permission: org.ethereumphone.walletmanager.permission.UPDATE_TOKEN_BALANCE
 * 
 * Usage example from TokenLauncher app:
 * ```kotlin
 * TokenBalanceUpdateHelper.sendTokenBalanceDeduction(
 *     context = context,
 *     tokenAddress = "0x1234...abcd",
 *     chainId = 1,
 *     amountToDeduct = BigDecimal("10.5")
 * )
 * ```
 */
object TokenBalanceUpdateHelper {
    
    private const val TAG = "TokenBalanceUpdateHelper"
    
    /**
     * Send an intent to deduct token balance in the WalletManager app
     * 
     * @param context The context from which to send the broadcast
     * @param tokenAddress The contract address of the token (e.g., "0x1234...")
     * @param chainId The chain ID where the token exists (e.g., 1 for Ethereum mainnet)
     * @param amountToDeduct The amount to deduct from the balance (in token units with decimals)
     * @return true if the intent was sent successfully, false otherwise
     */
    fun sendTokenBalanceDeduction(
        context: Context,
        tokenAddress: String,
        chainId: Int,
        amountToDeduct: BigDecimal
    ): Boolean {
        return try {
            val intent = Intent(TokenBalanceUpdateReceiver.ACTION_DEDUCT_TOKEN_BALANCE).apply {
                // Set the package to ensure the intent goes to the right app
                setPackage("org.ethereumphone.walletmanager")
                
                // Add the required extras
                putExtra(TokenBalanceUpdateReceiver.EXTRA_TOKEN_ADDRESS, tokenAddress)
                putExtra(TokenBalanceUpdateReceiver.EXTRA_CHAIN_ID, chainId)
                putExtra(TokenBalanceUpdateReceiver.EXTRA_AMOUNT_TO_DEDUCT, amountToDeduct.toString())
            }
            
            // Send the broadcast
            context.sendBroadcast(intent)
            
            Log.d(TAG, "Sent balance deduction intent - Token: $tokenAddress, Chain: $chainId, Amount: $amountToDeduct")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send balance deduction intent", e)
            false
        }
    }
    
    /**
     * Send an intent to deduct native token (ETH) balance
     * 
     * @param context The context from which to send the broadcast
     * @param chainId The chain ID (e.g., 1 for Ethereum mainnet)
     * @param amountToDeduct The amount to deduct from the balance (in ETH)
     * @return true if the intent was sent successfully, false otherwise
     */
    fun sendNativeTokenBalanceDeduction(
        context: Context,
        chainId: Int,
        amountToDeduct: BigDecimal
    ): Boolean {
        // For native tokens, use the chain ID as the contract address
        return sendTokenBalanceDeduction(
            context = context,
            tokenAddress = chainId.toString(),
            chainId = chainId,
            amountToDeduct = amountToDeduct
        )
    }
    
    /**
     * Check if the app has permission to send token balance updates
     * Note: This is for informational purposes. The actual permission check
     * happens on the receiver side.
     */
    fun hasUpdatePermission(context: Context): Boolean {
        return context.packageManager.checkPermission(
            "org.ethereumphone.walletmanager.permission.UPDATE_TOKEN_BALANCE",
            context.packageName
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
