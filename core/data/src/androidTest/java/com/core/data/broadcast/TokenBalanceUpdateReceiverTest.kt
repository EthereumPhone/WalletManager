package com.core.data.broadcast

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

/**
 * Instrumented test for TokenBalanceUpdateReceiver
 * 
 * To test this manually from ADB:
 * 
 * adb shell am broadcast -a org.ethereumphone.walletmanager.DEDUCT_TOKEN_BALANCE \
 *     -n org.ethereumphone.walletmanager/com.core.data.broadcast.TokenBalanceUpdateReceiver \
 *     --es TOKEN_ADDRESS "0x1234567890123456789012345678901234567890" \
 *     --ei CHAIN_ID 1 \
 *     --es AMOUNT_TO_DEDUCT "10.5"
 * 
 * Note: This will only work if the sending app has the proper permission or signature.
 */
@RunWith(AndroidJUnit4::class)
class TokenBalanceUpdateReceiverTest {
    
    @Test
    fun testSendTokenBalanceDeductionIntent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Create the intent as it would be sent from TokenLauncher
        val intent = Intent(TokenBalanceUpdateReceiver.ACTION_DEDUCT_TOKEN_BALANCE).apply {
            setPackage("org.ethereumphone.walletmanager")
            putExtra(TokenBalanceUpdateReceiver.EXTRA_TOKEN_ADDRESS, "0x1234567890123456789012345678901234567890")
            putExtra(TokenBalanceUpdateReceiver.EXTRA_CHAIN_ID, 1)
            putExtra(TokenBalanceUpdateReceiver.EXTRA_AMOUNT_TO_DEDUCT, "10.5")
        }
        
        // Send the broadcast (this will only work if permissions are set up correctly)
        context.sendBroadcast(intent)
        
        // In a real test, you would verify the database was updated correctly
        // For now, this just tests that the intent can be created and sent
    }
    
    @Test
    fun testSendNativeTokenBalanceDeductionIntent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // For native ETH, use chain ID as the contract address
        val intent = Intent(TokenBalanceUpdateReceiver.ACTION_DEDUCT_TOKEN_BALANCE).apply {
            setPackage("org.ethereumphone.walletmanager")
            putExtra(TokenBalanceUpdateReceiver.EXTRA_TOKEN_ADDRESS, "1")
            putExtra(TokenBalanceUpdateReceiver.EXTRA_CHAIN_ID, 1)
            putExtra(TokenBalanceUpdateReceiver.EXTRA_AMOUNT_TO_DEDUCT, "0.01")
        }
        
        context.sendBroadcast(intent)
    }
}
