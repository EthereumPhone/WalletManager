package com.core.domain

import com.core.data.repository.EnsRepository
import com.core.data.repository.TransferRepository
import com.core.model.Transfer
import com.core.model.TransferItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetTransfersUseCase @Inject constructor(
    private val transferRepository: TransferRepository,
    private val ensRepository: EnsRepository
) {

    operator fun invoke(): Flow<List<TransferItem>> =
        transferRepository.getTransfers(TRANSFER_CATEGORIES)
            .map { transfers ->
                // Filter out spam/scam tokens
                val validTransfers = transfers.filterValidTransfers()
                
                // Resolve ENS names for all addresses
                val ensMap = resolveEnsNames(validTransfers)
                
                // Transform to UI model
                validTransfers.map { transfer ->
                    transfer.toTransferItem(ensMap)
                }
            }

    private fun List<Transfer>.filterValidTransfers(): List<Transfer> =
        asSequence()
            .filter { transfer ->
                val assetName = transfer.asset.lowercase()
                assetName.isNotBlank() && !assetName.containsSpamPattern()
            }
            .sortedBy { it.blockTimestamp }
            .toList()

    private fun String.containsSpamPattern(): Boolean =
        SPAM_PATTERNS.any { pattern -> this.contains(pattern) }

    private suspend fun resolveEnsNames(transfers: List<Transfer>): Map<String, String?> {
        val uniqueAddresses = transfers
            .flatMap { listOf(it.from, it.to) }
            .distinct()
            .filter { it.isNotBlank() }
        
        return if (uniqueAddresses.isNotEmpty()) {
            ensRepository.getEnsNames(uniqueAddresses)
        } else {
            emptyMap()
        }
    }

    private fun Transfer.toTransferItem(ensMap: Map<String, String?>): TransferItem {
        val dateTime = blockTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
        
        return TransferItem(
            chainId = chainId,
            from = ensMap[from.lowercase()] ?: from,
            to = ensMap[to.lowercase()] ?: to,
            asset = asset.truncateAssetName(),
            value = value.formatAmount(),
            timeStamp = "${dateTime.date} ${dateTime.time}",
            userSent = userIsSender,
            txHash = txHash
        )
    }

    private fun String.truncateAssetName(): String =
        if (length > MAX_ASSET_NAME_LENGTH) {
            "${take(MAX_ASSET_NAME_LENGTH)}..."
        } else {
            this
        }.trim()

    private fun Double.formatAmount(): String =
        DECIMAL_FORMAT.format(this)

    companion object {
        private const val MAX_ASSET_NAME_LENGTH = 10
        private val DECIMAL_FORMAT = DecimalFormat("#.####")
        
        private val TRANSFER_CATEGORIES = listOf("external", "erc20", "erc721")
        
        private val SPAM_PATTERNS = listOf(
            // URL patterns
            "http://", "https://", "www.", ".com", ".org", ".net", ".io", ".xyz", ".info",
            // File/image patterns
            "ipfs://", "data:", "image/", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp",
            // Social media patterns
            "telegram", "discord", "twitter", "t.me", "t.ly",
            // Common spam patterns
            "visit", "claim", "airdrop", "free", "bonus",
            // Path separators
            "/"
        )
    }
}