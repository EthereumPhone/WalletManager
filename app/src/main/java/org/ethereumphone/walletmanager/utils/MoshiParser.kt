package org.ethereumphone.walletmanager.utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

class MoshiParser(
    private val moshi: Moshi
): JsonParser {

    override fun <T> fromJson(json: String, type: Type): T? {
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(type)
        return jsonAdapter.fromJson(json)
    }

    override fun <T> toJson(obj: T, type: Type): String? {
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(type)
        return jsonAdapter.toJson(obj)
    }
}