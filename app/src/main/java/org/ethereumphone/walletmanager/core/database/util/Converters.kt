package org.ethereumphone.walletmanager.core.database.util

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import org.ethereumphone.walletmanager.core.domain.model.TokenMetadata
import org.ethereumphone.walletmanager.core.workers.util.JsonParser

@ProvidedTypeConverter
class Converters(
    private val jsonParser: JsonParser
) {
    @TypeConverter
    fun fromTokenMetaDataJson(json: String): List<TokenMetadata> {
        return jsonParser.fromJson<ArrayList<TokenMetadata>>(
            json,
            object : TypeToken<ArrayList<TokenMetadata>>(){}.type
        ) ?: emptyList()
    }
}