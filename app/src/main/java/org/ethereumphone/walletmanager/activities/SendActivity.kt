package org.ethereumphone.walletmanager.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.ethereumphone.walletmanager.R
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.utils.WalletSDK
import org.kethereum.eip137.model.ENSName
import org.web3j.ens.EnsResolver
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.kethereum.ens.ENS
import org.kethereum.rpc.HttpEthereumRPC
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.CompletableFuture


class SendActivity : AppCompatActivity() {
    lateinit var phoneAddress: String
    lateinit var network: Network
    lateinit var web3j: Web3j
    lateinit var wallet: WalletSDK
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)
        phoneAddress = intent.extras?.getString("phoneAddress")!!
        network = intent.extras?.getSerializable("selectedNetwork") as Network
        web3j = Web3j.build(HttpService(network.chainRPC))
        wallet = WalletSDK(
            context = this,
            web3RPC = network.chainRPC
        )
        updateUi()
    }

    private fun updateUi() {
        val textView = findViewById<TextView>(R.id.sendTitle)
        val toAddr = findViewById<EditText>(R.id.toAddress)
        val sendButton = findViewById<Button>(R.id.realSendButton)
        val context: Context = this

        textView.setText("Send " + network.chainCurrency)
        toAddr.setOnFocusChangeListener { view, b ->
            updateENSOnView(
                context = context,
                editText = view as EditText
            )
        }

        sendButton.setOnClickListener {
            sendETH()
        }

    }

    private fun sendETH() {
        val amount = findViewById<EditText>(R.id.editTextNumberDecimal).text.toString()
        var toAddr = findViewById<EditText>(R.id.toAddress).text.toString()
        val context: Context = this
        if (toAddr.endsWith(".eth")) {
            toAddr = resolveENS(toAddr)
        }

        val valueToBeSent = BigDecimal(amount).times(BigDecimal.TEN.pow(18)).toBigInteger().toString()

        wallet.sendTransaction(
            to = toAddr,
            value = valueToBeSent,
            data = "",
            chainId = network.chainId
        ).whenComplete { s, throwable ->
            if (s != WalletSDK.DECLINE) {
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("tx_id", s)
                startActivity(intent)
            }
        }
    }

    private fun updateENSOnView(context: Context, editText: EditText) {
        val runnable = Runnable {
            if (editText.text.endsWith(".eth")) {
                try {
                    val ens = ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
                    val address = ens.getAddress(ENSName(editText.text.toString()))
                    editText.setText(address.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Cannot resolve this ENS domain", Toast.LENGTH_LONG).show()
                }
            }
        }
        Thread(runnable).start()
    }

    private fun resolveENS(name: String): String {
        val completableFuture = CompletableFuture<String>()
        CompletableFuture.runAsync {
            val ens = ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
            val address = ens.getAddress(ENSName(name))
            completableFuture.complete(address.toString())
        }
        return completableFuture.get()
    }



}