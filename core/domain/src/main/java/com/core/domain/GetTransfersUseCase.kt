package com.core.domain

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
    private val transferRepository: TransferRepository
) {

    operator fun invoke(): Flow<List<TransferItem>> =
        transferRepository.getTransfers(listOf("external", "erc20", "erc721"))
            .map { items ->
                items
                    .asSequence()
                    .filter { transfer ->
                        val name = transfer.asset.lowercase()
                        name.isNotBlank() && urlPatterns.none { name.contains(it) }
                    }
                    .sortedBy { it.blockTimestamp }
                    .map { transfer ->
                        val truncatedAsset = truncate(transfer.asset).trim()
                        val dateTime = transfer.blockTimestamp
                            .toLocalDateTime(TimeZone.currentSystemDefault())

                        TransferItem(
                            chainId = transfer.chainId,
                            from = transfer.from,
                            to = transfer.to,
                            asset = truncatedAsset,
                            value = formatDouble(transfer.value),
                            timeStamp = "${dateTime.date} ${dateTime.time}",
                            userSent = transfer.userIsSender,
                            txHash = transfer.txHash
                        )
                    }
                    .toList()
            }

    fun formatDouble(input: Double): String {
        val decimalFormat = DecimalFormat("#.#####")
        return decimalFormat.format(input)
    }
}

fun truncate(input: String): String {
    return if (input.length > 6) {
        input.take(6) + "..."
    } else {
        input
    }
}

private val urlPatterns = listOf(
    "http://", "https://", "www.",
    ".com", ".io", ".org", ".net", ".xyz",
    "/", "t.me", "telegram", "twitter", "discord", "t.ly"
)