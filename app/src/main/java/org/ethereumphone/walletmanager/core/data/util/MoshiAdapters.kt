package org.ethereumphone.walletmanager.core.data.util

import com.squareup.moshi.FromJson
import org.ethereumphone.walletmanager.core.data.remote.dto.TokenBalanceJsonResponse
import org.ethereumphone.walletmanager.core.data.remote.dto.TokenExchangeDto
import org.ethereumphone.walletmanager.core.data.remote.dto.TransferDto
import org.ethereumphone.walletmanager.core.data.remote.dto.TransferJsonResponse
import org.ethereumphone.walletmanager.core.database.model.erc20Token.TokenExchangeEntity
import java.time.Instant
import java.time.ZoneId

class MoshiAdapters {
    @FromJson
    fun tokenExchangeEntity(tokenExchangeDto: TokenExchangeDto): TokenExchangeEntity {
        return TokenExchangeEntity(
            symbol = tokenExchangeDto.symbol,
            price = tokenExchangeDto.price.toDouble()
        )
    }

    @FromJson
    fun toTransferEntity(transferJsonResponse: TransferJsonResponse): List<TransferDto> {
        val res = transferJsonResponse.result.transfers
        val deviceZoneId = ZoneId.systemDefault()

        return res.map {
            val zonedTime = Instant.parse(it.metadata.blockTimestamp).atZone(deviceZoneId).toString()
            it.copy(
                metadata = TransferDto.TransferMetadata(zonedTime)
            )
        }
    }

    @FromJson
    //fun toTokenBalaceEntity(tokenBalanceJsonResponse: TokenBalanceJsonResponse): List<TokenBala>
}