package com.core.domain

import com.core.data.repository.TransferRepository
import com.core.model.NetworkChain
import com.core.model.TransferItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetTransfersByNetwork @Inject constructor(
    private val transferRepository: TransferRepository

) {
    operator fun invoke(networkChain: NetworkChain): Flow<List<TransferItem>> =
        transferRepository.getTransfers(networkChain.chainId)
            .map { items ->
                val sortedItems = items.sortedBy { it.blockTimestamp }
                sortedItems.map {
                    val networkCurrency = if (it.chainId == 137) "MATIC" else "ETH"
                    val timeStamp = it.blockTimestamp
                        .toLocalDateTime(TimeZone.currentSystemDefault())

                    TransferItem(
                        chainId = it.chainId,
                        from = it.from,
                        to = it.to,
                        asset = networkCurrency,
                        value = it.value.toString(),
                        timeStamp = timeStamp.date.toString() + " " + timeStamp.time,
                        userSent = it.userIsSender,
                        txHash = it.txHash
                    )
                }
            }
}