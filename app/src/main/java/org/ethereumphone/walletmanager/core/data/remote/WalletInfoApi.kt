package org.ethereumphone.walletmanager.core.data.remote

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ethereumphone.walletmanager.BuildConfig
import org.ethereumphone.walletmanager.core.domain.model.TokenExchange
import org.ethereumphone.walletmanager.core.domain.model.Network
import org.ethereumphone.walletmanager.core.model.Transaction
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import java.lang.Double.parseDouble
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import kotlin.collections.ArrayList


class WalletInfoApi @Inject constructor(
    @ApplicationContext private val context: Context,
    private val web3jInstance: Web3j,
    val sharedPreferences: SharedPreferences
) {
    private val wallet = WalletSDK(context)
    val walletAddress = wallet.getAddress()



    fun getEthAmount(selectedNetwork: Network): Double {
        // Get web3j instance for the selected network
        val web3j = Web3j.build(HttpService(selectedNetwork.chainRPC))
        // Get the phone's address
        val amountGet = BigDecimal(
            web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).sendAsync()
                .get().balance
        )

        // Return the balance in ETH, rounded to 6 decimal places
        val resultBalance = amountGet.divide(BigDecimal.TEN.pow(18)).setScale(6, BigDecimal.ROUND_HALF_EVEN).toDouble()
        return resultBalance
    }

    fun round(value: Double, places: Int): Double {
        require(places >= 0)
        var bd: BigDecimal = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun getRpcUrl(selectedNetwork: Network): String {
        val address = wallet.getAddress()

        return when(selectedNetwork.chainId){
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
    }

    /*
    fun getNetworkCurrency(): String  {
        return if(getCurrentNetwork() == 137) "MATICUSDT" else "ETHUSDT"
    }

     */

    /*
    suspend fun getEthAmountInUSD(ethAmount: Double, selectedNetwork: Network): Double {
        var symbol = when(selectedNetwork.chainId) {
            137 -> "MATICUSDT"
            else -> "ETHUSDT"

        }
        //val exchange = ExchangeApi.retrofitService.getExchange(
            symbol = symbol
       // )

        //return round(parseDouble(exchange.price) * ethAmount, 3)
    }

    */

    /*
    fun startPriceUpdater(priceUpdated: (TokenExchange) -> Unit) {
        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                val completableFuture = CompletableFuture<TokenExchange>()
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

     */

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
    private val _Token_exchange = MutableLiveData<TokenExchange>()
    val tokenExchange: LiveData<TokenExchange> = _Token_exchange

    private val _historicTransactions = MutableLiveData(ArrayList<Transaction>())
    val historicTransactions: LiveData<ArrayList<Transaction>> = _historicTransactions

    private val _networkAmount = MutableLiveData(Double.MAX_VALUE)
    val networkAmount: LiveData<Double> = _networkAmount

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    fun getHistoricalTransactions() {
        viewModelScope.launch {
            try {
                val url = walletInfoApi.getRpcUrl(network)
                var transactionsResult = HistoricTransactionsApi.retrofitService.getHistoricTransactions(url).result
                // amountGet.divide(BigDecimal.TEN.pow(18)).setScale(6, BigDecimal.ROUND_HALF_EVEN).toDouble()
                for(i in transactionsResult) {
                    var toRound = BigDecimal(i.value)
                    i.value = toRound.divide(BigDecimal.TEN.pow(18)).setScale(6,RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
                }
                _historicTransactions.value = ArrayList(transactionsResult)
            } catch (e: Exception) {
                e.message?.let { Log.d("histResp", it) }
                _historicTransactions.value = ArrayList()
            }
        }
    }

    fun getNetworkBalance() {
        viewModelScope.launch{
            try {
                _networkAmount.value = walletInfoApi.getEthAmount(network)
            } catch (e: Exception) {}
        }
    }

    /*
    fun getExchangeRate() {
        viewModelScope.launch{
            try {
                _Token_exchange.value = ExchangeApi.retrofitService.getExchange(walletInfoApi.getNetworkCurrency())
            } catch (e: Exception) {}
        }
    }
*/
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            getHistoricalTransactions()
            getNetworkBalance()
            //getExchangeRate()
            _isRefreshing.value = false
        }
    }

    init {
        refreshInfo()
    }

    fun refreshInfo() {
        println("WalletInfoApi: Refreshing state")
        getHistoricalTransactions()
        getNetworkBalance()
        //getExchangeRate()
    }
}

