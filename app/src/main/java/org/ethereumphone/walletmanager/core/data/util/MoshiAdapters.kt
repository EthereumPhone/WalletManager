package org.ethereumphone.walletmanager.core.data.util

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.ethereumphone.walletmanager.core.data.remote.dto.NetworkTransferDto
import org.ethereumphone.walletmanager.core.data.remote.dto.TokenExchangeDto
import org.ethereumphone.walletmanager.core.domain.model.NetworkTransfer
import org.ethereumphone.walletmanager.core.domain.model.TokenExchange
import java.time.ZoneId
import java.time.ZonedDateTime

class ExchangeAdapter {
    @FromJson
    fun ExchangeFromJson(tokenExchangeJson: TokenExchangeDto): TokenExchange {
        return TokenExchange(
            symbol = tokenExchangeJson.symbol,
            price = tokenExchangeJson.symbol.toDouble()
        )
    }

    @ToJson
    fun ExchangeToJson(tokenExchange: TokenExchange): TokenExchangeDto {
        return TokenExchangeDto(
            symbol = tokenExchange.symbol,
            price = tokenExchange.toString()
        )
    }
}

class TransferAdapter {
    @FromJson
    fun TransferFromJson(networkTransferJson: NetworkTransferDto): List<NetworkTransfer> {
        val res = networkTransferJson.result.transfers
        val deviceZoneId = ZoneId.systemDefault()

        return res.map {
            NetworkTransfer(
                asset = it.asset,
                blockNum = it.blockNum,
                category = it.category,
                erc1155Metadata = it.erc1155Metadata,
                erc721TokenId = it.erc721TokenId,
                from = it.from,
                hash = it.hash,
                rawContract =it.rawContract ,
                to = it.to,
                tokenId = it.tokenId,
                value = it.value.toDouble(),
                blockTimestamp = run {
                    val zonedDateTime = ZonedDateTime.parse(res[0].metadata.blockTimestamp)
                    zonedDateTime.withZoneSameInstant(deviceZoneId).toLocalDateTime()
                }
            )
        }
    }
}