package org.ethereumphone.walletmanager.core.workers.util

import com.google.gson.reflect.TypeToken
import org.ethereumphone.walletmanager.core.domain.model.TokenMetadata
import java.io.File
import java.io.FileNotFoundException

class GenericTokenMetadataParser(
    private val jsonParser: JsonParser
) {
    private val jsonFile: File = File("../uniswap_token_list.json")
    private val jsonString: String = try{
        jsonFile.readText()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        "Path doesn't exist"
    }

    fun getTokenList(): List<TokenMetadata> = jsonParser.fromJson<ArrayList<TokenMetadata>>(
            jsonString,
            object : TypeToken<ArrayList<TokenMetadata>>(){}.type
        ) ?: emptyList()
}