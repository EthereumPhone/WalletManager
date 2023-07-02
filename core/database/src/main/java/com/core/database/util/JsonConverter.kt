package com.core.database.util

import java.lang.reflect.Type

interface JsonConverter {
    fun <T> fromJson(json: String, type: Type): T?
    fun <T> toJson(obj: T, type: Type): String?
}