package com.core.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.data.BuildConfig
import com.core.data.model.dto.TokenMetadataJsonResponse
import com.core.data.model.requestBody.TokenMetadataRequestBody
import com.core.data.remote.TokenMetadataApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Integration tests against the real Alchemy token metadata API to verify
 * we can fetch token logos correctly.
 *
 * Notes:
 * - Requires valid API keys in local.properties (ALCHEMY_API, etc.)
 * - Requires network access during instrumentation tests.
 * - Uses well-known tokens (USDC, WETH, etc.) that have stable logo URLs.
 */
@RunWith(AndroidJUnit4::class)
class TokenLogoIntegrationTest {

    companion object {
        // Well-known token addresses on Ethereum mainnet
        private const val USDC_ADDRESS = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"
        private const val WETH_ADDRESS = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"
        private const val USDT_ADDRESS = "0xdAC17F958D2ee523a2206206994597C13D831ec7"
        private const val DAI_ADDRESS = "0x6B175474E89094C44Da98b954EescdeCB5aC5295d"

        // ETH mainnet chain name for Alchemy
        private const val ETH_MAINNET = "eth-mainnet"
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun buildAlchemyUrl(chainName: String): String =
        "https://$chainName.g.alchemy.com/v2/${BuildConfig.ALCHEMY_API}"

    private fun createTokenMetadataApi(baseUrl: String): TokenMetadataApi {
        return Retrofit.Builder()
            .baseUrl("https://eth-mainnet.g.alchemy.com/") // Placeholder base URL
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TokenMetadataApi::class.java)
    }

    @Test
    fun liveAlchemy_usdcLogo_isValidUrl() = runBlocking {
        val api = createTokenMetadataApi(buildAlchemyUrl(ETH_MAINNET))
        val response = api.getTokenMetadata(
            url = buildAlchemyUrl(ETH_MAINNET),
            requestBody = TokenMetadataRequestBody(params = listOf(USDC_ADDRESS))
        )

        val metadata = response.result

        // Verify basic metadata is present
        assertNotNull("USDC metadata should not be null", metadata)
        assertTrue("USDC symbol should be USDC", metadata.symbol.equals("USDC", ignoreCase = true))

        // Verify logo URL is present and valid
        assertNotNull("USDC logo should not be null", metadata.logo)
        assertTrue(
            "USDC logo should be a valid URL starting with http",
            metadata.logo!!.startsWith("http")
        )
    }

    @Test
    fun liveAlchemy_wethLogo_isValidUrl() = runBlocking {
        val api = createTokenMetadataApi(buildAlchemyUrl(ETH_MAINNET))
        val response = api.getTokenMetadata(
            url = buildAlchemyUrl(ETH_MAINNET),
            requestBody = TokenMetadataRequestBody(params = listOf(WETH_ADDRESS))
        )

        val metadata = response.result

        // Verify basic metadata is present
        assertNotNull("WETH metadata should not be null", metadata)
        assertTrue("WETH symbol should be WETH", metadata.symbol.equals("WETH", ignoreCase = true))

        // Verify logo URL is present and valid
        assertNotNull("WETH logo should not be null", metadata.logo)
        assertTrue(
            "WETH logo should be a valid URL starting with http",
            metadata.logo!!.startsWith("http")
        )
    }

    @Test
    fun liveAlchemy_usdtLogo_isValidUrl() = runBlocking {
        val api = createTokenMetadataApi(buildAlchemyUrl(ETH_MAINNET))
        val response = api.getTokenMetadata(
            url = buildAlchemyUrl(ETH_MAINNET),
            requestBody = TokenMetadataRequestBody(params = listOf(USDT_ADDRESS))
        )

        val metadata = response.result

        // Verify basic metadata is present
        assertNotNull("USDT metadata should not be null", metadata)
        assertTrue("USDT symbol should be USDT", metadata.symbol.equals("USDT", ignoreCase = true))

        // Verify logo URL is present and valid
        assertNotNull("USDT logo should not be null", metadata.logo)
        assertTrue(
            "USDT logo should be a valid URL starting with http",
            metadata.logo!!.startsWith("http")
        )
    }

    @Test
    fun liveAlchemy_logoUrlsAreDifferentPerToken() = runBlocking {
        val api = createTokenMetadataApi(buildAlchemyUrl(ETH_MAINNET))

        // Fetch metadata for multiple tokens
        val usdcResponse = api.getTokenMetadata(
            url = buildAlchemyUrl(ETH_MAINNET),
            requestBody = TokenMetadataRequestBody(params = listOf(USDC_ADDRESS))
        )
        val wethResponse = api.getTokenMetadata(
            url = buildAlchemyUrl(ETH_MAINNET),
            requestBody = TokenMetadataRequestBody(params = listOf(WETH_ADDRESS))
        )

        val usdcLogo = usdcResponse.result.logo
        val wethLogo = wethResponse.result.logo

        // Both should have logos
        assertNotNull("USDC logo should not be null", usdcLogo)
        assertNotNull("WETH logo should not be null", wethLogo)

        // Logos should be different for different tokens
        assertTrue(
            "Different tokens should have different logos",
            usdcLogo != wethLogo
        )
    }

    @Test
    fun liveAlchemy_baseChain_usdcLogo_isValidUrl() = runBlocking {
        val baseChainName = "base-mainnet"
        val baseUsdcAddress = "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913" // USDC on Base

        val api = createTokenMetadataApi(buildAlchemyUrl(baseChainName))
        val response = api.getTokenMetadata(
            url = buildAlchemyUrl(baseChainName),
            requestBody = TokenMetadataRequestBody(params = listOf(baseUsdcAddress))
        )

        val metadata = response.result

        // Verify basic metadata is present
        assertNotNull("Base USDC metadata should not be null", metadata)
        assertTrue(
            "Base USDC symbol should be USDC",
            metadata.symbol.equals("USDC", ignoreCase = true)
        )

        // Verify logo URL is present and valid
        assertNotNull("Base USDC logo should not be null", metadata.logo)
        assertTrue(
            "Base USDC logo should be a valid URL starting with http",
            metadata.logo!!.startsWith("http")
        )
    }

    @Test
    fun liveAlchemy_arbitrum_wethLogo_isValidUrl() = runBlocking {
        val arbChainName = "arb-mainnet"
        val arbWethAddress = "0x82aF49447D8a07e3bd95BD0d56f35241523fBab1" // WETH on Arbitrum

        val api = createTokenMetadataApi(buildAlchemyUrl(arbChainName))
        val response = api.getTokenMetadata(
            url = buildAlchemyUrl(arbChainName),
            requestBody = TokenMetadataRequestBody(params = listOf(arbWethAddress))
        )

        val metadata = response.result

        // Verify basic metadata is present
        assertNotNull("Arbitrum WETH metadata should not be null", metadata)
        assertTrue(
            "Arbitrum WETH symbol should be WETH",
            metadata.symbol.equals("WETH", ignoreCase = true)
        )

        // Verify logo URL is present and valid
        assertNotNull("Arbitrum WETH logo should not be null", metadata.logo)
        assertTrue(
            "Arbitrum WETH logo should be a valid URL starting with http",
            metadata.logo!!.startsWith("http")
        )
    }
}




