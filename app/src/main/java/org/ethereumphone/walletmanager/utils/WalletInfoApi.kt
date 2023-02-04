package org.ethereumphone.walletmanager.utils

import android.app.Activity
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumphone.walletmanager.BuildConfig
import org.ethereumphone.walletmanager.models.Exchange
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.models.Transaction
import org.ethereumphone.walletsdk.WalletSDK
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import java.io.InputStream
import java.lang.Double.parseDouble
import java.math.BigDecimal
import java.math.RoundingMode
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

    fun round(value: Double, places: Int): Double {
        require(places >= 0)
        var bd: BigDecimal = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun getHistoricTransactions(selectedNetwork: Network): ArrayList<Transaction> {
        val address = wallet.getAddress()
        try {
            val output = ArrayList<Transaction>()
            //val rpcURL = if (selectedNetwork.chainId == 1) "https://api.etherscan.io/api?module=account&action=txlist&address=$address&startblock=0&endblock=99999999&page=1&offset=7&sort=desc&apikey=NXXTAQGMIT39T9R465P4564JDW1PRPD27J" else ""
            val rpcURL = when(selectedNetwork.chainId){
                // Mainnet
                1 -> "https://api.etherscan.io/api?module=account&action=txlist&address=$address&startblock=0&endblock=99999999&page=1&offset=7&sort=desc&apikey=${BuildConfig.ETHSCAN_API}"
                // Goerli
                5 -> "https://api-goerli.etherscan.io/api?module=account&action=txlist&address=$address&startblock=0&endblock=99999999&page=1&offset=7&sort=desc&apikey=${BuildConfig.ETHSCAN_API}"
                // Optimism
                10 -> "https://api-optimistic.etherscan.io/api?module=account&action=txlist&address=$address&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=${BuildConfig.OPTISCAN_API}"
                // Polygon
                137 -> "https://api.polygonscan.com/api?module=account&action=txlist&address=$address&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=${BuildConfig.POLYSCAN_API}"
                // Arbitrum
                42161 -> "https://api.arbiscan.io/api?module=account&action=txlistinternal&address=$address&startblock=0&endblock=99999999&page=1&offset=10&sort=asc&apikey=${BuildConfig.ARBISCAN_API}"
                else -> ""
            }

            if (rpcURL == "") {
                return output
            }

            val url = URL(rpcURL)
            val httpConn: HttpURLConnection = url.openConnection() as HttpURLConnection
            httpConn.setRequestMethod("GET")

            httpConn.setRequestProperty("Content-Type", "application/json")

            val responseStream: InputStream =
                if (httpConn.responseCode/ 100 === 2) httpConn.getInputStream() else httpConn.getErrorStream()
            val s: Scanner = Scanner(responseStream).useDelimiter("\\A")
            val response = if (s.hasNext()) s.next() else ""

            val jsonArray = JSONObject(response).getJSONArray("result") as JSONArray


            for (i in 0..jsonArray.length()-1) {
                val transferData = jsonArray.get(i) as JSONObject
                output.add(
                    Transaction(
                        type = !(transferData.getString("to").equals(address, true)),
                        value = BigDecimal(transferData.getString("value")).divide(BigDecimal.TEN.pow(18)).setScale(6, BigDecimal.ROUND_HALF_EVEN).toString(),
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

    suspend fun getEthAmountInUSD(ethAmount: Double, selectedNetwork: Network): Double {
        var symbol = when(selectedNetwork.chainId) {
            137 -> "MATICUSDT"
            else -> "ETHUSDT"

        }
        val exchange = ExchangeApi.retrofitService.getExchange(
            symbol = symbol
        )

        return round(parseDouble(exchange.price) * ethAmount, 3)
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

    private val _historicTransactions = MutableLiveData<ArrayList<Transaction>>(ArrayList<Transaction>())
    val historicTransactions: LiveData<ArrayList<Transaction>> = _historicTransactions

    private val _ethAmount = MutableLiveData<Double>(Double.MAX_VALUE)
    val ethAmount: LiveData<Double> = _ethAmount

    private val _ethAmountInUSD = MutableLiveData<Double>(0.0)
    val ethAmountInUSD: LiveData<Double> = _ethAmountInUSD

    init {
        walletInfoApi.startPriceUpdater { exchange ->
            (walletInfoApi.context as Activity).runOnUiThread {
                _exchange.value = exchange
            }
        }
        ethAmount.observeForever {
            val number = it
            if (number == Double.MAX_VALUE) {
                _ethAmountInUSD.value = 0.0
                return@observeForever
            }
            CompletableFuture.runAsync {
                GlobalScope.launch {
                    val cryptoToUSD = walletInfoApi.getEthAmountInUSD(number, network)
                    // Wait till app has been open for at least 1 second
                    (walletInfoApi.context as Activity).runOnUiThread {
                        _ethAmountInUSD.postValue(cryptoToUSD)
                    }
                }
            }
        }

        viewModelScope.launch {
            try {
                val transactionsResult = walletInfoApi.getHistoricTransactions(network)
                _historicTransactions.value = transactionsResult
            } catch (e: Exception) {
                _historicTransactions.value = ArrayList()
            }
        }

        /**
        CompletableFuture.runAsync {
            val historicTransactions = walletInfoApi.getHistoricTransactions(network)

            (walletInfoApi.context as Activity).runOnUiThread {
            _historicTransactions.postValue(historicTransactions)
            }
        }
         */


        CompletableFuture.runAsync {
            GlobalScope.launch {
                try {
                    val ethAmount = walletInfoApi.getEthAmount(network)
                    (walletInfoApi.context as Activity).runOnUiThread {
                        _ethAmount.postValue(ethAmount)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(1400)
                    (walletInfoApi.context as Activity).runOnUiThread {
                        _ethAmount.postValue(0.0)
                    }
                }
            }
        }
    }
}

