package org.ethosmobile.uniswap_routing_sdk

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import org.json.JSONArray
import java.io.InputStream
import java.util.Scanner
import java.util.concurrent.CompletableFuture


class UniswapRoutingSDK(private val context: Context, private val web3RPC: String)
{
    companion object {
        val ETH_MAINNET = Token(
            1,
            "0x3a4e6eD8B0F02BFBfaA3C6506Af2DB939eA5798c", // mhaas.eth, but it doesn't matter as long as its a valid address
            18,
            "ETH",
            "Ether"
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getQuote(inputToken: Token, outputToken: Token, amountIn: Double, receiverAddress: String, context: Context): CompletableFuture<Double> {
        val wv = WebView(context)
        wv.settings.javaScriptEnabled = true
        wv.settings.allowFileAccess = true
        wv.settings.domStorageEnabled = true // Turn on DOM storage
        wv.settings.databaseEnabled = true

        val completableFuture: CompletableFuture<Double> = CompletableFuture()


        val bundleJs = getAssetContent(context.assets.open("getquote.js"))

        wv.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                println(consoleMessage.message())
                if (consoleMessage.message().contains("OUT_CALLDATA")) {
                    val callData = consoleMessage.message().split("OUT_CALLDATA:")[1]
                    completableFuture.complete(callData.toDouble())
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
            .replace("RPC_PROVIDER_URL", "https://eth-mainnet.g.alchemy.com/v2/lZSeyaiKTV9fKK3kcYYt9CxDZDobSv_Z")

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
        val wv = WebView(context)
        wv.settings.javaScriptEnabled = true
        wv.settings.allowFileAccess = true
        wv.settings.domStorageEnabled = true // Turn on DOM storage
        wv.settings.databaseEnabled = true

        val completableFuture: CompletableFuture<List<String>> = CompletableFuture()


        val bundleJs = getAssetContent(context.assets.open("bundledRouting.js"))

        wv.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("UniswapRoutingSDK", consoleMessage.message())
                if (consoleMessage.message().contains("OUT_CALLDATA")) {
                    val callData = consoleMessage.message().split("OUT_CALLDATA:")[1]
                    val jsonArray = JSONArray(callData)
                    val list = ArrayList<String>()
                    for (i in 0 until jsonArray.length()) {
                        list.add(jsonArray.getJSONObject(i).getString("address"))
                    }
                    completableFuture.complete(list)
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

    private fun getAssetContent(filename: InputStream): String? {
        val s = Scanner(filename).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}