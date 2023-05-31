package org.ethereumphone.walletmanager.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.ethereumphone.walletmanager.R
import org.ethereumphone.walletmanager.core.domain.model.Network

class AddNetworkActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var gson: Gson
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_network)

        sharedPreferences = this.getPreferences(MODE_PRIVATE)
        gson = Gson()

        val addNetworkButton = findViewById<Button>(R.id.addNetwork)

        addNetworkButton.setOnClickListener {
            saveNewNetwork()
        }
    }

    private fun saveNewNetwork() {
        val chainRPC= findViewById<EditText>(R.id.chainRPCTextView).text.toString()
        val chainID = Integer.parseInt(findViewById<EditText>(R.id.chainIDTextView).text.toString())
        val ticker = findViewById<EditText>(R.id.tickerTextView).text.toString()
        val chainName= findViewById<EditText>(R.id.chainNameTextView).text.toString()

        val newNetwork = Network(
            chainId = chainID,
            chainCurrency = ticker,
            chainRPC = chainRPC,
            chainName = chainName,
            chainExplorer = "https://etherscan.io"
        )

        val sharedPreferences = this.getSharedPreferences("NETWORKS", Context.MODE_PRIVATE)

        val allNetworksString = sharedPreferences.getString("allNetworks", "")

        val itemType = object : TypeToken<ArrayList<Network>>() {}.type
        val allNetworks = gson.fromJson<ArrayList<Network>>(allNetworksString, itemType)

        allNetworks.add(newNetwork)

        sharedPreferences.edit().putString("allNetworks", gson.toJson(allNetworks)).apply()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}