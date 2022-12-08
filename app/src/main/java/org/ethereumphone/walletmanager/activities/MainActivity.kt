package org.ethereumphone.walletmanager.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ethereumphone.walletmanager.R
import org.ethereumphone.walletmanager.models.Exchange
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.models.Transaction
import org.ethereumphone.walletmanager.utils.ExchangeApi
import org.ethereumphone.walletmanager.utils.WalletSDK
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import java.io.InputStream
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList


fun Context.copyToClipboard(text: CharSequence) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    // Default network is Mainnet
    var allNetworks: ArrayList<Network> = ArrayList<Network>()
    var selectedNetwork: Network = Network(
        chainId = 1,
        chainRPC = "https://rpc.flashbots.net/",
        chainCurrency = "eth",
        chainName = "Mainnet"
    )

    // Create wallet connection
    lateinit var walletSDK: WalletSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        walletSDK = WalletSDK(this, selectedNetwork.chainRPC)

        if (intent.extras?.getString(
                "tx_id",
                "default"
            ) != "default" && intent.extras?.getString("tx_id", "default") != null
        ) {
            val tx_id = intent.extras?.getString("tx_id", "default")

            Toast.makeText(
                this,
                "Transaction was sent!\nTx-id was copied to clipboard.",
                Toast.LENGTH_LONG
            ).show()
            if (tx_id != null) {
                this.copyToClipboard(tx_id)
            }
        }

        loadNetworks()

        val spinner = findViewById<Spinner>(R.id.spinner)
        val nameArray =ArrayList<String>()
        allNetworks.forEach {
            nameArray.add(it.chainName)
        }

        val stringList = nameArray.toArray(arrayOfNulls<String>(nameArray.size))
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this@MainActivity,
            android.R.layout.simple_spinner_item, stringList
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        updateValues()

        setupOnClicks()
    }

    private fun loadNetworks() {
        val gson = Gson()

        val sharedPreferences = this.getSharedPreferences("NETWORKS", Context.MODE_PRIVATE)

        val allNetworksString = sharedPreferences.getString("allNetworks", "")
        if (allNetworksString == "") {
            allNetworks.add(selectedNetwork)
            allNetworks.add(
                Network(
                    chainName = "Goerli Testnet",
                    chainId = 5,
                    chainCurrency = "eth",
                    chainRPC = "https://goerli.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161"
                )
            )
            sharedPreferences.edit().putString("allNetworks", gson.toJson(allNetworks)).apply()
        } else {
            val itemType = object : TypeToken<ArrayList<Network>>() {}.type
            allNetworks = gson.fromJson(allNetworksString, itemType)
        }
    }

    private fun setupOnClicks() {
        val sendButton = findViewById<Button>(R.id.addNetwork)
        val receiveButton = findViewById<Button>(R.id.receiveButton)
        val addNetworkButton = findViewById<Button>(R.id.addNetworkButton)

        sendButton.setOnClickListener {
            startSend()
        }
        receiveButton.setOnClickListener {
            showReceive()
        }
        addNetworkButton.setOnClickListener {
            startActivity(Intent(this, AddNetworkActivity::class.java))
        }
    }

    private fun showReceive() {
        TODO("Not yet implemented")
    }

    private fun startSend() {
        val intent = Intent(this, SendActivity::class.java)
        intent.putExtra("phoneAddress", walletSDK.getAddress())
        intent.putExtra("selectedNetwork", selectedNetwork)
        startActivity(intent)
    }

    fun updateValues() {
        val web3j = Web3j.build(HttpService(selectedNetwork.chainRPC))
        val amountTextView = findViewById<TextView>(R.id.currencyAmount)
        val phoneAddress = walletSDK.getAddress()
        val amountGet = BigDecimal(
            web3j.ethGetBalance(phoneAddress, DefaultBlockParameterName.LATEST).sendAsync()
                .get().balance
        )

        // Update currency amount textView
        amountTextView.setText(
            amountGet.divide(BigDecimal.TEN.pow(18)).setScale(6, BigDecimal.ROUND_HALF_EVEN)
                .toPlainString() + " " + selectedNetwork.chainCurrency
        )

    }

    fun getHistoricTransactions(address: String): ArrayList<Transaction> {
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

    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
        allNetworks.forEach {
            if (it.chainName == parent?.getItemAtPosition(pos).toString()) {
                selectedNetwork = it
                updateValues()
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        println("nothing selected")
    }

    fun startPriceUpdater() {
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
                    println(exchange)
                }

                mainHandler.postDelayed(this, 60 * 1000)
            }
        })


    }
}