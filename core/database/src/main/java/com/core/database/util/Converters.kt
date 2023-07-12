package com.core.database.util

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.core.database.model.Erc1155MetadataObject
import com.core.database.model.RawContract
import com.squareup.moshi.Types
import kotlinx.datetime.Instant
import java.math.BigDecimal

class InstantConverter {
    @TypeConverter
    fun longToInstant(value: Long?): Instant? =
        value?.let(Instant::fromEpochMilliseconds)

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? =
        instant?.toEpochMilliseconds()
}

/**
 * I don't know how to make this using generics :(
 */
@ProvidedTypeConverter
class Erc1155MetadataConverter(
    private val jsonConverter: JsonConverter
) {
    private val type = Types.newParameterizedType(
        List::class.java,
        Erc1155MetadataObject::class.java)

    @TypeConverter
    fun StringToErc1155Metadata(value: String?): List<Erc1155MetadataObject>? =
        value?.let {
            jsonConverter.fromJson(
                it,
                type
            )
        }

    @TypeConverter
    fun Erc1155MetadataToString(erc1155Metadata: List<Erc1155MetadataObject>?): String? =
        erc1155Metadata?.let {
            jsonConverter.toJson(
                it,
                type
            )
        }
}

@ProvidedTypeConverter
class RawContractConverter(
    private val jsonConverter: JsonConverter
) {
    @TypeConverter
    fun StringToRawContract(value: String?): RawContract? =
        value?.let {
            jsonConverter.fromJson(
                it,
                RawContract::class.java
            )
        }

    @TypeConverter
    fun RawContractToString(rawContract: RawContract?): String? =
        rawContract?.let {
            jsonConverter.toJson(
                it,
                RawContract::class.java
            )
        }
}

@ProvidedTypeConverter
class BigDecimalTypeConverter {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }
}
