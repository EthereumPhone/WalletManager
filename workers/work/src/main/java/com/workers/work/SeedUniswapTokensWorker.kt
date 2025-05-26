package com.workers.work

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.database.model.erc20.TokenMetadataEntity
import com.core.domain.UpdateTokensUseCase
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.workers.work.util.TokenList
import com.workers.work.util.UniswapToken
import com.workers.work.util.Version
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.util.concurrent.TimeUnit

@HiltWorker
class SeedUniswapTokensWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val moshi: Moshi,
    private val tokenMetadataRepository: TokenMetadataRepository

) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val list = buildTokenList(appContext, "1.0.0")
            val tokens = list.uniswapTokens.map { token ->
                TokenMetadataEntity(
                    contractAddress = token.address,
                    decimals = token.decimals,
                    name = token.name,
                    symbol = token.symbol,
                    logo = token.logoURI,
                    chainId = token.chainId,
                    swappable = true
                )
            }
            tokenMetadataRepository.insertTokenMetadata(tokens)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
        Result.success()
    }

    companion object {
        fun startSeedUniswapTokensWork() =
            OneTimeWorkRequestBuilder<SeedUniswapTokensWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()
    }
}

fun Context.loadTokenListFromRaw(resId: Int): List<UniswapToken> {
    val json = resources.openRawResource(resId).bufferedReader().use { it.readText() }
    return Json.decodeFromString(json)

}

fun buildTokenList(context: Context, version: String): TokenList {
    val parsed = version.split(".").map { it.toInt() }

    val allTokens = listOf(
        R.raw.mainnet,
        R.raw.ropsten,
        R.raw.goerli,
        R.raw.kovan,
        R.raw.rinkeby,
        R.raw.polygon,
        R.raw.mumbai,
        R.raw.optimism,
        R.raw.celo,
        R.raw.arbitrum,
        R.raw.bnb,
        R.raw.sepolia,
        R.raw.avalanche,
        R.raw.base,
        R.raw.blast,
        R.raw.zksync,
        R.raw.worldchain,
        R.raw.zora
    ).flatMap { context.loadTokenListFromRaw(it) }

    val sortedTokens = allTokens.sortedWith(compareBy<UniswapToken> { it.chainId }.thenBy { it.symbol.lowercase() })

    return TokenList(
        name = "Uniswap Labs Default",
        timestamp = Instant.now().toString(),
        version = Version(
            major = parsed[0],
            minor = parsed[1],
            patch = parsed[2]
        ),
        logoURI = "ipfs://QmNa8mQkrNKp1WEEeGjFezDmDeodkWRevGFN8JCV7b4Xir",
        keywords = listOf("uniswap", "default"),
        uniswapTokens = sortedTokens
    )
}