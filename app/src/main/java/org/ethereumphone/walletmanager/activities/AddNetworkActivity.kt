package org.ethereumphone.walletmanager.activities

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import org.ethereumphone.walletmanager.R

class AddNetworkActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var gson: Gson
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_network)

        sharedPreferences = this.getPreferences(MODE_PRIVATE)
        gson = Gson()
    }
}