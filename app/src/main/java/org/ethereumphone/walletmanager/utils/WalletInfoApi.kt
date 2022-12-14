package org.ethereumphone.walletmanager.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ethereumphone.walletmanager.models.Exchange
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.models.Transaction
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import java.io.InputStream
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList


class WalletInfoApi(
    val context: Context,
    val sharedPreferences: SharedPreferences
) {
    val wallet = WalletSDK(context)
    val walletAddress = wallet.getAddress()

    private fun loadNetworks(): ArrayList<Network> {
        val gson = Gson()
        val allNetworksString = sharedPreferences.getString("allNetworks", "")

        if (allNetworksString == "") {
            return getDefaultNetworks()
        } else {
            try {
                val itemType = object : TypeToken<ArrayList<Network>>() {}.type
                val allNetworks = gson.fromJson<ArrayList<Network>>(allNetworksString, itemType)
                if (allNetworks == null) {
                    return getDefaultNetworks()
                }
                return allNetworks
            } catch (e: Exception) {
                e.printStackTrace()
                return getDefaultNetworks()
            }
        }
    }

    private fun getDefaultNetworks(): ArrayList<Network> {
        val allNetworks = ArrayList<Network>()
        allNetworks.add(Network(
            chainId = 1,
            chainCurrency = "ETH",
            chainRPC = "https://mainnet.infura.io/v3/8a3d95b3bc4840f88a3d95b3bcf840f8",
            chainName = "Ethereum",
            chainExplorer = "https://etherscan.io"
        ))
        allNetworks.add(
            Network(
                chainName = "Goerli Testnet",
                chainId = 5,
                chainCurrency = "eth",
                chainRPC = "https://goerli.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161",
                chainExplorer = "https://goerli.etherscan.io"
            )
        )
        return allNetworks
    }

    fun getEthAmount(selectedNetwork: Network): Double {
        // Get web3j instance for the selected network
        val web3j = Web3j.build(HttpService(selectedNetwork.chainRPC))
        // Get the phone's address
        val amountGet = BigDecimal(
            web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).sendAsync()
                .get().balance
        )

        // Return the balance in ETH, rounded to 6 decimal places
        return amountGet.divide(BigDecimal.TEN.pow(18)).setScale(6, BigDecimal.ROUND_HALF_EVEN).toDouble()
    }

    fun getHistoricTransactions(selectedNetwork: Network): ArrayList<Transaction> {
        val address = wallet.getAddress()
        try {
            val output = ArrayList<Transaction>()
            val rpcURL = if (selectedNetwork.chainId == 1) "https://eth-mainnet.g.alchemy.com/v2/lZSeyaiKTV9fKK3kcYYt9CxDZDobSv_Z" else "https://eth-goerli.g.alchemy.com/v2/wEno3MttLG5usiVg4xL5_dXrDy_QH95f"

            val url = URL(rpcURL)
            val httpConn: HttpURLConnection = url.openConnection() as HttpURLConnection
            httpConn.setRequestMethod("POST")

            httpConn.setRequestProperty("Content-Type", "application/json")

            httpConn.setDoOutput(true)
            val writer = OutputStreamWriter(httpConn.getOutputStream())
            writer.write("{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"alchemy_getAssetTransfers\",\"params\":[{\"fromBlock\":\"0x0\",\"fromAddress\":\"$address\", \"category\": [\"external\"]}]}")
            writer.flush()
            writer.close()
            httpConn.getOutputStream().close()

            val responseStream: InputStream =
                if (httpConn.responseCode/ 100 === 2) httpConn.getInputStream() else httpConn.getErrorStream()
            val s: Scanner = Scanner(responseStream).useDelimiter("\\A")
            val response = if (s.hasNext()) s.next() else ""

            val json = JSONObject(response).getJSONObject("result").getJSONArray("transfers") as JSONArray

            for (i in 0..json.length()-1) {
                val transferData = json.get(i) as JSONObject
                output.add(
                    Transaction(
                        type = (transferData.getString("to") == address),
                        value = transferData.getString("value"),
                        hash = transferData.getString("hash"),
                        fromAddr = transferData.getString("from"),
                        toAddr = transferData.getString("to")
                    )
                )
            }
            return output
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList<Transaction>()
        }
    }

    fun getEthAmountInUSD(ethAmount: Double): Double {
        val url = URL("https://api.coingecko.com/api/v3/simple/price?ids=ethereum&vs_currencies=usd")
        val httpConn: HttpURLConnection = url.openConnection() as HttpURLConnection
        httpConn.setRequestMethod("GET")

        httpConn.setRequestProperty("Content-Type", "application/json")

        httpConn.setDoOutput(true)
        val writer = OutputStreamWriter(httpConn.getOutputStream())
        writer.write("")
        writer.flush()
        writer.close()
        httpConn.getOutputStream().close()

        val responseStream: InputStream =
            if (httpConn.responseCode/ 100 === 2) httpConn.getInputStream() else httpConn.getErrorStream()
        val s: Scanner = Scanner(responseStream).useDelimiter("\\A")
        val response = if (s.hasNext()) s.next() else ""

        val json = JSONObject(response).getJSONObject("ethereum") as JSONObject

        return (json.getDouble("usd") * ethAmount)
    }

    
    // This should accept a callback in the parameters
    fun startPriceUpdater(priceUpdated: (Exchange) -> Unit) {
        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                val completableFuture = CompletableFuture<Exchange>()
                CompletableFuture.runAsync {
                    GlobalScope.launch {
                        val output = ExchangeApi.retrofitService.getExchange(
                            symbol = "ETHUSDT"
                        )
                        completableFuture.complete(output)
                    }
                }
                completableFuture.whenComplete { exchange, throwable ->
                    // Exchange is the object with price and ticker
                    priceUpdated(exchange)
                }

                mainHandler.postDelayed(this, 60 * 1000)
            }
        })
    }

    fun buyCryptoClicked(): Intent {
        val userAddress = wallet.getAddress()
        return if (userAddress != null) {
            val uri = Uri.Builder()
                .scheme("https")
                .authority("buy.ramp.network")
                .appendQueryParameter("swapAsset", "ETH_ETH")
                .appendQueryParameter("userAddress", userAddress)
                .build()
                .toString()
            Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uri) }
        } else {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://buy.ramp.network/")
            }
        }
    }

    fun openTxInEtherscan(txHash: String, selectedNetwork: Network): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("${selectedNetwork.chainExplorer}/tx/$txHash"))
    }

}

/**
 * ViewModel class that works with the WalletInfoApi
 */
class WalletInfoViewModel(
    private val walletInfoApi: WalletInfoApi,
    private val network: Network
) : ViewModel() {

    private val _exchange = MutableLiveData<Exchange>(
        Exchange(
            price = "0.0",
            symbol = "ETHUSDT"
        )
    )
    val exchange: LiveData<Exchange> = _exchange

    private val _historicTransactions = MutableLiveData<ArrayList<Transaction>>(listOf<Transaction>() as ArrayList<Transaction>)

    init {
        walletInfoApi.startPriceUpdater { exchange ->
            _exchange.value = exchange
        }

        // Get historic transactions in the using the IO scope and feed them to the UI
        viewModelScope.launch(Dispatchers.IO) {
            val historicTransactions = walletInfoApi.getHistoricTransactions(network)
            _historicTransactions.value = historicTransactions
        }


    }
}