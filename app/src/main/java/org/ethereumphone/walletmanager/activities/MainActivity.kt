package org.ethereumphone.walletmanager.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.ethereumphone.walletmanager.R
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.utils.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal


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
        chainRPC = "https://eth-mainnet.nodereal.io/v1/1659dfb40aa24bbb8153a677b98064d7",
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
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this@MainActivity,
            android.R.layout.simple_spinner_item, nameArray.toArray() as Array<out String>
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        updateValues()

        setupOnClicks()
    }

    private fun loadNetworks() {
        val gson = Gson()

        val sharedPreferences = this.getPreferences(MODE_PRIVATE)
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
        val sendButton = findViewById<Button>(R.id.realSendButton)
        val receiveButton = findViewById<Button>(R.id.receiveButton)

        sendButton.setOnClickListener {
            startSend()
        }
        receiveButton.setOnClickListener {
            showReceive()
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
        val spinner = findViewById<Spinner>(R.id.spinner)
        val items = arrayOf("1", "2", "three")

        // Update currency amount textView
        amountTextView.setText(
            amountGet.divide(BigDecimal.TEN.pow(18)).setScale(6, BigDecimal.ROUND_HALF_EVEN)
                .toPlainString() + " " + selectedNetwork.chainCurrency
        )

    }

    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
        println("Clicked!")
        println(parent?.getItemAtPosition(pos).toString())
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        println("nothing selected")
    }
}