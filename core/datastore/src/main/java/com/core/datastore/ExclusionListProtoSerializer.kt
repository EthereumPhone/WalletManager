package com.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.core.datastore.proto.ExclusionListProto

import java.io.InputStream
import java.io.OutputStream

object ExclusionListProtoSerializer : Serializer<ExclusionListProto.ExclusionList> {
    override val defaultValue: ExclusionListProto.ExclusionList
        get() = ExclusionListProto.ExclusionList.getDefaultInstance()

    @Throws(CorruptionException::class)
    override suspend fun readFrom(input: InputStream): ExclusionListProto.ExclusionList {
        try {
            return ExclusionListProto.ExclusionList.parseFrom(input)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ExclusionListProto.ExclusionList, output: OutputStream) {
        t.writeTo(output)
    }
}
