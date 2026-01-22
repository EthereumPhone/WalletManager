package io.freedomfactory.privacy.utils

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MnemonicGeneratorTest {

    companion object {
        private const val TAG = "MnemonicGeneratorTest"
    }

    private lateinit var mnemonicGenerator: MnemonicGenerator

    @Before
    fun setUp() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "Setting up MnemonicGenerator test")
        mnemonicGenerator = MnemonicGenerator()
    }

    @Test
    fun generate_withSameInputs_returnsDeterministicMnemonic() {
        runBlocking {
        Log.d(TAG, "========================================")
        Log.d(TAG, "TEST: generate_withSameInputs_returnsDeterministicMnemonic")
        Log.d(TAG, "========================================")

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Fixed test inputs
        val testPassword = "MySecureTestPassword123!"
        val testWalletAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f8dB21"

        Log.d(TAG, "Input wallet: $testWalletAddress")
        Log.d(TAG, "Input password: $testPassword")
        Log.d(TAG, "----------------------------------------")

        // Generate mnemonic multiple times with the same inputs
        Log.d(TAG, "Generating mnemonic (attempt 1)...")
        val mnemonic1 = mnemonicGenerator.generate(context, testWalletAddress, testPassword)
        Log.d(TAG, "Mnemonic 1: $mnemonic1")

        Log.d(TAG, "Generating mnemonic (attempt 2)...")
        val mnemonic2 = mnemonicGenerator.generate(context, testWalletAddress, testPassword)
        Log.d(TAG, "Mnemonic 2: $mnemonic2")

        Log.d(TAG, "Generating mnemonic (attempt 3)...")
        val mnemonic3 = mnemonicGenerator.generate(context, testWalletAddress, testPassword)
        Log.d(TAG, "Mnemonic 3: $mnemonic3")

        Log.d(TAG, "----------------------------------------")
        Log.d(TAG, "Comparing results...")
        Log.d(TAG, "Mnemonic 1 == Mnemonic 2: ${mnemonic1 == mnemonic2}")
        Log.d(TAG, "Mnemonic 2 == Mnemonic 3: ${mnemonic2 == mnemonic3}")

        // Verify all mnemonics are identical
        assertEquals("Mnemonic should be deterministic", mnemonic1, mnemonic2)
        assertEquals("Mnemonic should be deterministic", mnemonic2, mnemonic3)

        // Verify mnemonic has 24 words
        val wordCount = mnemonic1.split(" ").size
        Log.d(TAG, "Word count: $wordCount")
        assertEquals("Mnemonic should have 24 words", 24, wordCount)

        Log.d(TAG, "TEST PASSED: Mnemonic generation is deterministic!")
        Log.d(TAG, "========================================")
        }
    }

    @Test
    fun generate_withDifferentPasswords_returnsDifferentMnemonics() {
        runBlocking {
        Log.d(TAG, "========================================")
        Log.d(TAG, "TEST: generate_withDifferentPasswords_returnsDifferentMnemonics")
        Log.d(TAG, "========================================")

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val testWalletAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f8dB21"
        val password1 = "Password1"
        val password2 = "Password2"

        Log.d(TAG, "Input wallet: $testWalletAddress")
        Log.d(TAG, "Password 1: $password1")
        Log.d(TAG, "Password 2: $password2")
        Log.d(TAG, "----------------------------------------")

        Log.d(TAG, "Generating mnemonic with password 1...")
        val mnemonic1 = mnemonicGenerator.generate(context, testWalletAddress, password1)
        Log.d(TAG, "Mnemonic 1: $mnemonic1")

        Log.d(TAG, "Generating mnemonic with password 2...")
        val mnemonic2 = mnemonicGenerator.generate(context, testWalletAddress, password2)
        Log.d(TAG, "Mnemonic 2: $mnemonic2")

        Log.d(TAG, "----------------------------------------")
        Log.d(TAG, "Mnemonics are different: ${mnemonic1 != mnemonic2}")

        assertNotEquals("Different passwords should produce different mnemonics", mnemonic1, mnemonic2)

        Log.d(TAG, "TEST PASSED: Different passwords produce different mnemonics!")
        Log.d(TAG, "========================================")
        }
    }

    @Test
    fun generate_withDifferentWallets_returnsDifferentMnemonics() {
        runBlocking {
        Log.d(TAG, "========================================")
        Log.d(TAG, "TEST: generate_withDifferentWallets_returnsDifferentMnemonics")
        Log.d(TAG, "========================================")

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val password = "TestPassword123"
        val wallet1 = "0x742d35Cc6634C0532925a3b844Bc9e7595f8dB21"
        val wallet2 = "0x853e46Dd7745D1643a4c844Bd0e8695f9eC22B32"

        Log.d(TAG, "Input password: $password")
        Log.d(TAG, "Wallet 1: $wallet1")
        Log.d(TAG, "Wallet 2: $wallet2")
        Log.d(TAG, "----------------------------------------")

        Log.d(TAG, "Generating mnemonic with wallet 1...")
        val mnemonic1 = mnemonicGenerator.generate(context, wallet1, password)
        Log.d(TAG, "Mnemonic 1: $mnemonic1")

        Log.d(TAG, "Generating mnemonic with wallet 2...")
        val mnemonic2 = mnemonicGenerator.generate(context, wallet2, password)
        Log.d(TAG, "Mnemonic 2: $mnemonic2")

        Log.d(TAG, "----------------------------------------")
        Log.d(TAG, "Mnemonics are different: ${mnemonic1 != mnemonic2}")

        assertNotEquals("Different wallets should produce different mnemonics", mnemonic1, mnemonic2)

        Log.d(TAG, "TEST PASSED: Different wallets produce different mnemonics!")
        Log.d(TAG, "========================================")
        }
    }
}
