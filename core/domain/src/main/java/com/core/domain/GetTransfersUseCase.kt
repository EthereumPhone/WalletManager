package com.core.domain

import com.core.data.repository.TransferRepository
import com.core.model.Transfer
import com.core.model.TransferItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetTransfersUseCase @Inject constructor(
    private val transferRepository: TransferRepository
) {

    operator fun invoke(): Flow<List<TransferItem>> =
        transferRepository.getTransfers(listOf("external"))
            .map { items ->
                val sortedItems = items.sortedBy { it.blockTimestamp }
                sortedItems.map {
                    val networkCurrency = if (it.chainId == 137) "matic" else "eth"
                    //val assetType = it.asset.ifEmpty { networkCurrency }
                    val address = if (it.userIsSender) it.to else it.from
                    val timeStamp = it.blockTimestamp
                        .toLocalDateTime(TimeZone.currentSystemDefault())


                    //println("${address.substring(0,5) + "..." + address.takeLast(3)} - ${it.value} - ${it.userIsSender}")
                    TransferItem(
                        chainId = it.chainId,
                        from = it.from,//address.substring(0,5) + "..." + address.takeLast(3),
                        to = it.to,
                        asset = networkCurrency,
                        value = it.value.toString(),
                        timeStamp = timeStamp.date.toString() + " " + timeStamp.time,
                        userSent = it.userIsSender
                    )
                }
            }
}