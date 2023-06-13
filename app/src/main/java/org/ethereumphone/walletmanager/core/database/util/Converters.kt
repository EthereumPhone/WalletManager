package org.ethereumphone.walletmanager.core.database.util

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import org.ethereumphone.walletmanager.core.data.remote.dto.TransferDto
import org.ethereumphone.walletmanager.utils.JsonParser
import java.time.Instant

@ProvidedTypeConverter
class Converters(
    private val jsonParser: JsonParser
) {
    @TypeConverter
    fun fromInstantJson(json: String): Instant {
        return jsonParser.fromJson<Instant>(
            json,
            object : TypeToken<Instant>(){}.type
        )?: Instant.parse(json)
    }

    @TypeConverter
    fun toInstantJson(time: Instant): String {
        return jsonParser.toJson(
            time,
            object : TypeToken<Instant>(){}.type
        ) ?: time.toString()
    }

    @TypeConverter
    fun toErc1155MetadataObjectJson(erc1155: List<TransferDto.Erc1155MetadataObject>): String {
        return jsonParser.toJson(
            erc1155,
            object: TypeToken<ArrayList<TransferDto.Erc1155MetadataObject>>(){}.type
        ) ?: ""
    }

    @TypeConverter
    fun fromErc1155MetadataObjectJson(json: String): List<TransferDto.Erc1155MetadataObject> {
        return jsonParser.fromJson<ArrayList<TransferDto.Erc1155MetadataObject>>(
            json,
            object: TypeToken<ArrayList<TransferDto.Erc1155MetadataObject>>(){}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toRawContractJson(contract: TransferDto.RawContract): String {
        return jsonParser.toJson(
            contract,
            object: TypeToken<TransferDto.RawContract>(){}.type
        ) ?: ""
    }

    @TypeConverter
    fun fromRawContractObjectJson(json: String): TransferDto.RawContract {
        return jsonParser.fromJson<TransferDto.RawContract>(
            json,
            object: TypeToken<TransferDto.RawContract>(){}.type
        ) ?: TransferDto.RawContract("","","")
    }
}