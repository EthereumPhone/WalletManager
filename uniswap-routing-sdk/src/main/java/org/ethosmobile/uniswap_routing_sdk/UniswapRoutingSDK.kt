package org.ethosmobile.uniswap_routing_sdk

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.math.BigDecimal
import java.net.URL
import java.util.Scanner
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


class UniswapRoutingSDK(
    private val context: Context,
    private val web3RPC: String
) {
    companion object {
        val ETH_MAINNET = Token(
            1,
            "ETH",
            18,
            "ETH",
            "Ether"
        )
    }

    fun getQuote(inputToken: Token, outputToken: Token, amountIn: Double, receiverAddress: String): CompletableFuture<Double> {
        val completableFuture: CompletableFuture<Double> = CompletableFuture()


        CompletableFuture.runAsync {
            val params = StringBuilder()
            params.append("?inputTokenChainId=${inputToken.chainId}")
            params.append("&inputTokenAddress=${inputToken.address}")
            params.append("&inputTokenDecimals=${inputToken.decimals}")
            params.append("&inputTokenSymbol=${inputToken.symbol}")
            params.append("&inputTokenName=${inputToken.name}")

            params.append("&outputTokenChainId=${outputToken.chainId}")
            params.append("&outputTokenAddress=${outputToken.address}")
            params.append("&outputTokenDecimals=${outputToken.decimals}")
            params.append("&outputTokenSymbol=${outputToken.symbol}")
            params.append("&outputTokenName=${outputToken.name}")

            params.append("&amount=${amountIn}")
            params.append("&receiverAddress=${receiverAddress}")

            println("Doing request")

            val response = URL("https://getquote-dey2ouq2ya-uc.a.run.app/${params}").readText()

            println("Response: ${response}")
            val responseObj = JSONObject(response)
            try {
                val outputDouble: Double = responseObj.getString("result").toDouble()
                println("Completing future")
                completableFuture.complete(outputDouble)
            } catch (e: Exception) {
                e.printStackTrace()
                completableFuture.complete(0.0)
            }

        }


        return completableFuture
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getCalldata(inputToken: Token, outputToken: Token, amountIn: Double, receiverAddress: String): CompletableFuture<String> {
        val wv = WebView(context)
        wv.settings.javaScriptEnabled = true
        wv.settings.allowFileAccess = true
        wv.settings.domStorageEnabled = true // Turn on DOM storage
        wv.settings.databaseEnabled = true

        val completableFuture: CompletableFuture<String> = CompletableFuture()


        val bundleJs = getAssetContent(context.assets.open("bundle.js"))

        wv.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                if (consoleMessage.message().contains("OUT_CALLDATA")) {
                    val callData = consoleMessage.message().split("OUT_CALLDATA:")[1]
                    completableFuture.complete(callData)
                }
                return true
            }
        }

        val output = StringBuilder()
        output.append("<script type='text/javascript'>\n")
        output.append(bundleJs)
        output.append("</script>\n")
        var realJs = output.toString()
            .replace("CHAINID_INPUT_TOKEN", inputToken.chainId.toString())
            .replace("TOKEN_INPUT_ADDRESS", inputToken.address)
            .replace("DECIMALS_INPUT_TOKEN", inputToken.decimals.toString())
            .replace("SYMBOL_INPUT_TOKEN", inputToken.symbol)
            .replace("NAME_INPUT_TOKEN", inputToken.name)
            .replace("CHAINID_OUTPUT_TOKEN", outputToken.chainId.toString())
            .replace("TOKEN_OUTPUT_ADDRESS", outputToken.address)
            .replace("DECIMALS_OUTPUT_TOKEN", outputToken.decimals.toString())
            .replace("SYMBOL_OUTPUT_TOKEN", outputToken.symbol)
            .replace("NAME_OUTPUT_TOKEN", outputToken.name)
            .replace("CHOSEN_IN_AMOUNT", amountIn.toString())
            .replace("RECEIVER_ADDRESS", receiverAddress)
            .replace("RPC_PROVIDER_URL", web3RPC)

        realJs = if (inputToken == ETH_MAINNET) {
            realJs.replace("CHOSEN_IN_TOKEN", "ETH")
        } else {
            realJs.replace("CHOSEN_IN_TOKEN", "USDC_TOKEN")
        }

        realJs = if (outputToken == ETH_MAINNET) {
            realJs.replace("CHOSEN_OUT_TOKEN", "ETH")
        } else {
            realJs.replace("CHOSEN_OUT_TOKEN", "DAI_TOKEN")
        }

        wv.loadDataWithBaseURL("file:///android_asset/index.html", realJs, "text/html", "utf-8", null)

        // Make completableFuture complete exceptionally if it hasn't completed after 30 seconds
        Thread {
            Thread.sleep(30000)
            if (!completableFuture.isDone) {
                completableFuture.completeExceptionally(Exception("Timeout"))
            }
        }.start()

        return completableFuture
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getAllRouter(inputToken: Token, outputToken: Token, amountIn: Double, receiverAddress: String): CompletableFuture<List<String>> {
        val completableFuture: CompletableFuture<List<String>> = CompletableFuture()


        CompletableFuture.runAsync {
            println("getAllRouter running now")
            val params = StringBuilder()
            params.append("?inputTokenChainId=${inputToken.chainId}")
            params.append("&inputTokenAddress=${inputToken.address}")
            params.append("&inputTokenDecimals=${inputToken.decimals}")
            params.append("&inputTokenSymbol=${java.net.URLEncoder.encode(inputToken.symbol, "utf-8")}")
            params.append("&inputTokenName=${java.net.URLEncoder.encode(inputToken.name, "utf-8")}")

            params.append("&outputTokenChainId=${outputToken.chainId}")
            params.append("&outputTokenAddress=${outputToken.address}")
            params.append("&outputTokenDecimals=${outputToken.decimals}")
            params.append("&outputTokenSymbol=${java.net.URLEncoder.encode(outputToken.symbol, "utf-8")}")
            params.append("&outputTokenName=${java.net.URLEncoder.encode(outputToken.name, "utf-8")}")

            params.append("&amount=${BigDecimal(amountIn).toPlainString()}")
            params.append("&receiverAddress=${receiverAddress}")

            println("Doing request")
            val finalUrl = "https://getallrouter-dey2ouq2ya-uc.a.run.app/${params}"
            println(finalUrl)

            val response = try {
                URL(finalUrl).readText()
            } catch (e: Exception) {
                e.printStackTrace()
                val list = ArrayList<String>()
                list.add(inputToken.address)
                list.add(outputToken.address)
                completableFuture.complete(list)
                return@runAsync
            }

            println("Response getAllRouter: ${response}")
            val responseObj = JSONObject(response)
            try {
                println("Completing future")
                val jsonArray = JSONArray(responseObj.getString("result"))
                val list = ArrayList<String>()
                for (i in 0 until jsonArray.length()) {
                    list.add(jsonArray.getJSONObject(i).getString("address"))
                }
                completableFuture.complete(list)
            } catch (e: Exception) {
                e.printStackTrace()
                completableFuture.completeExceptionally(e)
            }

        }


        return completableFuture
    }

    private fun getAssetContent(filename: InputStream): String? {
        val s = Scanner(filename).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}