package com.feature.paymaster

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject
import java.net.UnknownHostException
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalLEDController
import com.core.ui.showCustomToast
import com.core.ui.util.dgenOcean
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenTurqoise
import com.core.ui.util.dgenWhite
import com.core.terminalsdk.TerminalSDK
import kotlinx.coroutines.delay
import com.core.ui.showDgenToast
import com.core.ui.util.PitagonsSans

// Data classes for API interaction
data class InitiateBalanceRequest(val userId: String, val amount: String)
data class InitiateBalanceResponse(val daimoPaymentId: String?, val daimoPaymentUrl: String?, val message: String?, val internalPaymentId: String?)

@HiltViewModel
class PayMasterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val walletSDK: WalletSDK?,
    private val terminalSDK: TerminalSDK?,
    private val reflectiveLedPattern: ReflectiveLedPattern?,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _topUpAmount = MutableStateFlow(TextFieldValue(""))
    val topUpAmount: StateFlow<TextFieldValue> = _topUpAmount

    // Function to update the state
    fun onTopUpAmountChanged(newValue: TextFieldValue) {
        _topUpAmount.value = newValue
    }

    private val _balance = MutableStateFlow("0.0")
    val balance: StateFlow<String> = _balance.asStateFlow()

    private val httpClient = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val paymasterSDK = PaymasterSDK(context)

    companion object {
        private const val INITIATE_BALANCE_URL = "https://api.markushaas.com/api/initiate-add-balance"
        private const val DAIMO_APP_ID = "pay-demo" // As per prompt for prototyping
        private const val DAIMO_CHECKOUT_BASE_URL = "https://pay.daimo.com/checkout"
    }

    init {
        viewModelScope.launch {
            try {
                if (paymasterSDK.initialize()) {
                    paymasterSDK.registerObserver { newBalance ->
                        _balance.value = newBalance
                    }
                    // Initial fetch of balance after registration
                    val initialBalance = paymasterSDK.getCurrentBalance()
                    if (initialBalance != null) {
                        _balance.value = initialBalance
                    } else {
                        // If initial balance is null (e.g. error during fetch), query for an update.
                        // The observer will then pick up the change.
                        paymasterSDK.queryUpdate()
                    }
                    // Also, trigger a query update to ensure we get the latest from backend if needed.
                    // This is useful if the service starts with a stale value before observer is hit.
                    paymasterSDK.queryUpdate() // Query after registration to ensure observer gets it
                } else {
                    // Keep balance as 0.0 on SDK init failure
                    _balance.value = "0.0"
                    Log.e("PayMasterViewModel", "SDK initialization failed")
                    // Generic error message for users
                    showDgenToast(appContext,"An error occurred. Please try again later.")
                }
            } catch (e: Exception) {
                Log.e("PayMasterViewModel", "Error during SDK initialization", e)
                _balance.value = "0.0"
                showDgenToast(appContext,"An error occurred. Please try again later.")
            }
        }
    }

    suspend fun topUp(amount: String): String? = withContext(Dispatchers.IO) {
        // Early exit if there is no internet connection
        if (!isInternetAvailable()) {
            Log.w("PayMasterViewModel", "No internet connection available")
            showDgenToast(appContext,"No internet connection")
            return@withContext null
        }

        try {
            val userId = walletSDK?.getAddress() ?: "" // Get address from WalletSDK
            if (userId.isBlank()) {
                // Handle case where userId is not available
                Log.e("PayMasterViewModel", "User ID not found from wallet SDK")
                showDgenToast(appContext,"An error occurred. Please try again later.")
                return@withContext null
            }

            val requestAdapter = moshi.adapter(InitiateBalanceRequest::class.java)
            val requestBodyJson = requestAdapter.toJson(InitiateBalanceRequest(userId, amount))

            val request = Request.Builder()
                .url(INITIATE_BALANCE_URL)
                .post(requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Log the actual error for debugging
                    val errorBody = response.body?.string()
                    Log.e("PayMasterViewModel", "API error response (${response.code}): $errorBody")
                    // Show generic error to user
                    showDgenToast(appContext,"An error occurred. Please try again later.")
                    return@withContext null
                }

                val responseBodyString = response.body?.string()
                if (responseBodyString == null) {
                    Log.e("PayMasterViewModel", "Empty API response")
                    showDgenToast(appContext,"An error occurred. Please try again later.")
                    return@withContext null
                }

                val responseAdapter = moshi.adapter(InitiateBalanceResponse::class.java)
                val apiResponse = responseAdapter.fromJson(responseBodyString)
                val daimoPaymentUrl = apiResponse?.daimoPaymentUrl

                if (daimoPaymentUrl.isNullOrBlank()) {
                    Log.e("PayMasterViewModel", "Daimo Payment URL missing in response: $responseBodyString")
                    showDgenToast(appContext,"An error occurred. Please try again later.")
                    return@withContext null
                }
                
                // Construct Daimo URL
                Log.d("PayMasterViewModel", "Successfully obtained Daimo URL: $daimoPaymentUrl")
                return@withContext daimoPaymentUrl
            }
        } catch (e: Exception) {
            Log.e("PayMasterViewModel", "Exception during topUp", e)
            if (e is UnknownHostException) {
                showDgenToast(appContext,"No internet connection")
            } else {
                // Generic error message for any other exception
                showDgenToast(appContext,"An error occurred. Please try again later.")
            }
            return@withContext null
        }
    }

    fun forceUpdateBalance() {
        viewModelScope.launch {
            try {
                paymasterSDK.queryUpdate()
            } catch (e: Exception) {
                Log.e("PayMasterViewModel", "Error updating balance", e)
                // Don't show error to user, just silently fail and keep current balance
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        paymasterSDK.cleanup()
    }

    // Helper to check internet connectivity
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    /**
     * Calls this function when the paymaster screen is opened
     *
     * When opened it displays the copy button
     */
    fun onScreenOpenedAfterResume() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                delay(2000)
                if (terminalSDK?.isAvailable() == true) {
                    while (terminalSDK.isScreenOn() != true) {
                        Log.d(
                            "PayMasterViewModel",
                            "ETHOSDEBUG: Waiting for secondary screen to be on..."
                        )
                        delay(500)
                    }
                    TerminalLEDController.displayChadPattern()
                    terminalSDK.displayTopUp {
                        // Wenn kein Betrag eingegeben wurde, nichts tun und Hinweis anzeigen
                        val cleanAmount = topUpAmount.value.text.removePrefix("$").trim()
                        if (cleanAmount.isEmpty()) {
                            showDgenToast(context, "Please enter an amount")
                            return@displayTopUp
                        }
                        viewModelScope.launch(Dispatchers.Main) {
                            Log.e(
                                "PayMasterViewModel",
                                "topUpAmount.value.text: ${topUpAmount.value.text}"
                            )
                            val daimoUrl = topUp(topUpAmount.value.text)
                            if (daimoUrl != null) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(daimoUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                appContext.startActivity(intent)

                                
                                // Only show success toasts if internet is available and topUp was successful
                                if (isInternetAvailable()) {
                                    showDgenToast(
                                        context = appContext,
                                        message = "You added ${topUpAmount.value.text} to your Paymaster."
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PayMasterViewModel", "Error in onScreenOpenedAfterResume", e)
                // Don't show error to user - fail silently for terminal operations
            }
        }
    }

    suspend fun onTopUpOpened(){
        try{
            //check if terminal sdk is available
            reflectiveLedPattern?.displayPlus(getSystemColorHex())
//            TerminalLEDController.displaySignPattern()
            if (terminalSDK?.isAvailable() == true) {
                terminalSDK.displayTopUp {
                    // Wenn kein Betrag eingegeben wurde, nichts tun und Hinweis anzeigen
                    val cleanAmount = topUpAmount.value.text.removePrefix("$").trim()
                    if (cleanAmount.isEmpty()) {
                        showDgenToast(appContext,"Please enter an amount")
                        return@displayTopUp
                    }
                    viewModelScope.launch(Dispatchers.Main) {
                        Log.e("PayMasterViewModel", "topUpAmount.value.text: ${topUpAmount.value.text}")
                        val daimoUrl = topUp(topUpAmount.value.text)
                        if (daimoUrl != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(daimoUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            appContext.startActivity(intent)
                            
                            // Only show success toasts if internet is available and topUp was successful
                            if (isInternetAvailable()) {
                                showDgenToast(
                                    context = appContext,
                                    message = "You added ${topUpAmount.value.text} to your Paymaster."
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PayMasterViewModel", "Error in onTopUpOpened", e)
            // Don't show error to user - fail silently for terminal operations
        }
    }

    fun onTopUpClosed(clearLed: Boolean = true) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (terminalSDK?.isAvailable() == true) {
                    terminalSDK.removeTopUp()
                    // Only clear LED if explicitly requested
                    if (clearLed) {
                        reflectiveLedPattern?.clear()
                    }
                } else {
                    Log.w("PayMasterViewModel", "TerminalSDK not available")
                }
            } catch (e: Exception) {
                Log.e("PayMasterViewModel", "Error removing top up terminal screen", e)
            }
        }
    }

    /**
     * Get system accent color as hex string
     */
    private fun getSystemColorHex(): String? {
        return appContext.let { context ->
            val accentInt = android.provider.Settings.Secure.getInt(
                context.contentResolver,
                "systemui_accent_color",
                0xFFFF0000.toInt()  // Default red
            )
            String.format("0x%08X", accentInt)
        }
    }

}
