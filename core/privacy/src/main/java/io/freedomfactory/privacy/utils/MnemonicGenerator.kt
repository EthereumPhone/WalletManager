package io.freedomfactory.privacy.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class MnemonicGenerator {

    companion object {
        val SALT = "DGEN1-PRIVACY-POOLS-WALLET-1"
    }

    /**
     * Takes user password, generates deterministic 24 word mnemonic phrase
     */
    suspend fun generate(context: Context, userWallet: String, userPassword: String): String {
        return withContext(Dispatchers.Default) {
            val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(SALT.toByteArray(StandardCharsets.UTF_8))
                .withParallelism(4)        // Use 4 parallel threads
                .withMemoryAsKB(262144)    // 256 MB memory cost (max practical for Android)
                .withIterations(16)        // 16 iterations to compensate for lower memory
                .build()

            val generator = Argon2BytesGenerator()
            generator.init(params)

            val hash = ByteArray(32)
            generator.generateBytes(
                (userWallet + SALT + userPassword).toByteArray(StandardCharsets.UTF_8),
                hash
            )
            bytesToMnemonic(
                context = context,
                entropy = hash
            )
        }
    }

    fun bytesToMnemonic(context: Context, entropy: ByteArray): String {
        require(entropy.size == 32) { "Entropy must be 32 bytes for 24-word mnemonic" }

        // Load wordlist from assets
        val wordlist = context.assets.open("wordlist.txt").bufferedReader().readLines()

        // Calculate SHA-256 checksum and take first byte (8 bits)
        val checksum = MessageDigest.getInstance("SHA-256").digest(entropy)[0]

        // Combine entropy (256 bits) + checksum (8 bits) = 264 bits
        val combined = entropy + checksum

        // Convert to 24 x 11-bit indices
        val words = mutableListOf<String>()
        var bitBuffer = 0
        var bitsInBuffer = 0
        var byteIndex = 0

        repeat(24) {
            while (bitsInBuffer < 11) {
                bitBuffer = (bitBuffer shl 8) or (combined[byteIndex++].toInt() and 0xFF)
                bitsInBuffer += 8
            }
            bitsInBuffer -= 11
            words.add(wordlist[(bitBuffer shr bitsInBuffer) and 0x7FF])
        }

        return words.joinToString(" ")
    }
}