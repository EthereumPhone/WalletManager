package com.feature.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.TokenExchangeRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.NetworkMonitor
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainIdToRPC
import com.core.domain.GetAllGroupedTokensUsecase
import com.core.domain.UpdateTokensByNetworkUseCase
import com.core.domain.GetAllTokensUsecase
import com.core.model.NetworkChain
import com.core.model.Price
import com.core.model.TokenAsset
import com.core.model.TokenAssetWithPrice
import com.core.model.TokenData
import com.core.model.UserData
import com.core.terminalsdk.TerminalSDK
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.IOException
import org.ethereumphone.walletsdk.WalletSDK
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.compose.ui.text.font.FontWeight
import com.core.ui.showCustomToast
import com.core.ui.showDgenToast
import com.core.ui.util.PitagonsSans
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenWhite

import com.core.terminalsdk.ReflectiveLedPattern

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val updateTokensByNetworkUseCase: UpdateTokensByNetworkUseCase,
    private val userDataRepository: UserDataRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val tokenExchangeRepository: TokenExchangeRepository,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val transferRepository: TransferRepository,
    private val getAllTokensUsecase: GetAllTokensUsecase,
    private val getAllGroupedTokensUsecase: GetAllGroupedTokensUsecase,
    private val walletSDK: WalletSDK?,
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val reflectiveLedPattern: ReflectiveLedPattern?,
    private val terminal: TerminalSDK?
) : ViewModel() {

    companion object {
        private val welcomeScreenShownThisSession = AtomicBoolean(false)
        private const val PREFS_NAME = "welcome_prefs"
        private const val KEY_FIRST_LAUNCH_COMPLETED = "isFirstLaunchCompleted"
        private const val KEY_LAST_REFRESH_TIME = "lastRefreshTime"
        private const val REFRESH_INTERVAL_MS = 90_000L
    }

    private val isHomeScreenVisible = AtomicBoolean(false)
    private var periodicRefreshJob: Job? = null

    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val tokenData = tokenExchangeRepository.getExchanges()
        .map { exchanges ->
            exchanges.groupBy { it.symbol }
                .map { (symbol, exchangeList) ->
                    // Get the most recent exchange rate for each symbol
                    val latestExchange = exchangeList.maxByOrNull { it.timestamp }
                    TokenData(
                        symbol = symbol,
                        prices = listOf(
                            Price(
                                currency = latestExchange?.currency ?: "",
                                value = latestExchange?.value?.toString() ?: "0.0",
                                lastUpdatedAt = latestExchange?.timestamp?.toString() ?: ""
                            )
                        )
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )


    val tokenAssetState: StateFlow<AssetsUiState> =
        combine(getAllGroupedTokensUsecase(), tokenData) { tokens, exchangeRates ->
            val exchangeRateMap = exchangeRates.associateBy { it.symbol }

            val tokensWithPrices = tokens.map { tokenAsset ->
                val priceInfo = exchangeRateMap[tokenAsset.symbol]?.prices?.firstOrNull()
                TokenAssetWithPrice(
                    address = tokenAsset.address,
                    name = tokenAsset.name,
                    symbol = tokenAsset.symbol,
                    decimals = tokenAsset.decimals,
                    balance = tokenAsset.balance,
                    fiatAmount = priceInfo?.value?.toDoubleOrNull()?.times(tokenAsset.balance) ?: 0.0,
                    chainId = tokenAsset.chainId,
                    logoUrl = tokenAsset.logoUrl,
                    swappable = tokenAsset.swappable,
                )
            }

            val filteredTokens = tokensWithPrices
                .filter { it.balance > 0 }
                .filter { token -> // Filter out tokens with URLs in their names or symbols
                    val name = token.name.lowercase()
                    val symbol = token.symbol.lowercase()

                    val urlPatterns = listOf(
                        "http://", "https://", "www.",
                        ".com", ".io", ".org", ".net", ".xyz",
                        "/", "t.me", "telegram", "twitter", "discord", "t.ly"
                    )

                    val containsNoUrlPatterns = urlPatterns.none { pattern ->
                        name.contains(pattern) || symbol.contains(pattern)
                    }
                    containsNoUrlPatterns
                }
                .sortedBy { it.fiatAmount }

            if (filteredTokens.isEmpty()) {
                AssetsUiState.Empty
            } else {
                AssetsUiState.Success(filteredTokens)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssetsUiState.Loading
        )

    val hasTransfers: StateFlow<Boolean> = flow {
        while (true) {
            // suspend until the first (or only) List<TransferItem> is emitted
            val items = transferRepository.getTransfers().first()
            emit(items.isNotEmpty())
            // wait one minute before the next check
            delay(TimeUnit.MINUTES.toMillis(1))
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { isOnlineValue ->
            val offlineValue = !isOnlineValue
            Log.d("HomeViewModel.isOffline", "Received isOnline=$isOnlineValue, emitting isOffline=$offlineValue")
            offlineValue
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true, // Start with offline assumption
        )

    private val _refreshState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshState.asStateFlow()

    private val welcomeMessages = listOf(
        "WELCOME BACK  ◕◡◕",
        "Hey Stranger  ⌐■◡■",
        "Look Who's Back  ▀̿◡ ̿▀̿ ̿",
        "Engaging warp drive  ◉◡◉",
        "Big Brain: Activated  ಠ◡ಠ",
        "Refueled n ready ⊹⋆☾⋆⊹✧"
    )

    private fun isFirstLaunchCompleted(): Boolean = sharedPrefs.getBoolean(KEY_FIRST_LAUNCH_COMPLETED, false)

    private fun setFirstLaunchCompleted() {
        sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH_COMPLETED, true).apply()
    }

    private var lastMessageIndex: Int
        get() = savedStateHandle.get<Int>("lastMessageIndex") ?: -1
        set(value) = savedStateHandle.set("lastMessageIndex", value)

    private val _selectedTokenAsset = MutableStateFlow<TokenAsset?>(null)
    val selectedTokenAsset: StateFlow<TokenAsset?> = _selectedTokenAsset.asStateFlow()

    init {
        observeNetworkStatus()
        // Optional: Initialen Refresh auslösen, wenn beim Start online
        viewModelScope.launch {
            // Warten, bis der erste Wert vom NetworkMonitor eintrifft, um Race Conditions zu vermeiden.
            // Ohne dies könnte isOnline.first() zu schnell sein, bevor der Monitor initialisiert ist.
            delay(100) // Kurze Verzögerung, um sicherzustellen, dass der callbackFlow im NetworkMonitor gestartet ist.
            if (networkMonitor.isOnline.first()) { // Prüft den ersten emittierten Wert nach kurzer Verzögerung
                Log.d("HomeViewModel", "Initially online, refreshing balances.")
                refreshAllBalances()
            }
        }
    }

    private fun observeNetworkStatus() {
        networkMonitor.isOnline
            .distinctUntilChanged() // Nur auf tatsächliche Änderungen reagieren
            .filter { isOnline -> isOnline } // Nur reagieren, wenn isOnline true wird (von false zu true)
            .onEach {
                Log.d("HomeViewModel", "Network came online, refreshing balances.")
                refreshAllBalances()
            }
            .launchIn(viewModelScope) // Flow in viewModelScope starten
    }

    fun setSelectedTokenAsset(tokenAsset: TokenAsset) {
        _selectedTokenAsset.value = tokenAsset
    }

    fun getSelectedTokenAsset(): TokenAsset? {
        return _selectedTokenAsset.value
    }

    suspend fun getLink(uri: String): String? {
        // Check for internet connectivity first
        // If offline, show a toast and return the original uri so that callers don't crash
        if (!networkMonitor.isOnline.first()) {
            showDgenToast(context,"No internet connection!")
            return null
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://getmoonpaynew-4bl33rjqpa-uc.a.run.app?text=$uri")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response: Response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                // Parse the JSON response using Moshi
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()

                val adapter = moshi.adapter(MoonpayResponse::class.java)
                val responseBody: ResponseBody? = response.body
                if (responseBody != null) {
                    val moonpayResponse = adapter.fromJson(responseBody.string())
                    return@withContext moonpayResponse?.link
                        ?: throw IOException("Invalid response format")
                } else {
                    throw IOException("Empty response")
                }
            } catch (e: Exception) {
                throw IOException("Error fetching link: ${e.message}", e)
            }
        }
    }

    data class MoonpayResponse(
        val link: String
    )

    fun setOnboardingComplete(onboardingComplete: Boolean) {
        viewModelScope.launch {
            userDataRepository.setOnboardingCompleted(onboardingComplete)
        }
    }

    fun changeNetwork(network: Int) {
        viewModelScope.launch {
            walletSDK?.changeChain(
                network,
                chainIdToRPC(network),
                chainIdToBundler(network)
            ) // chainIdToBundler(network))//"https://eth-mainnet.g.alchemy.com/v2/${chainToApiKey("eth-mainnet")}")
            userDataRepository.setWalletNetwork(network.toString())
        }
    }

    // We'll store our user input in the SavedStateHandle under a certain key




    fun refreshAllBalances() {
        viewModelScope.launch {
            _refreshState.value = true
            try {
                val userData = userDataRepository.userData.first()
                val walletAddress = userData.walletAddress
                val networkChains = NetworkChain.getAllNetworkChains().map { it.chainId }

                // Refresh network balances
                networkBalanceRepository.refreshNetworkBalance(walletAddress, networkChains)

                // Refresh token balances for each network
                networkChains.forEach { chainId ->
                    updateTokensByNetworkUseCase(walletAddress, chainId)
                }

                tokenExchangeRepository.fetchAllExchanges()

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error refreshing balances", e)
            } finally {
                _refreshState.value = false
            }
        }
    }

    /**
     * Formats very small balances to show up to 6 decimal places.
     * If the value is smaller than 0.000001, it's rounded up to 0.000001.
     */
    private fun formatSmallBalance(balance: Double): Double {
        if (balance == 0.0) return 0.0

        val precision = 6
        val minDisplayableValue = 1.0 / Math.pow(10.0, precision.toDouble())

        // For very small values (less than minDisplayableValue), return the minimum displayable value
        if (balance > 0 && balance < minDisplayableValue) {
            return minDisplayableValue
        }

        // Otherwise, round to 6 decimal places
        val bd = BigDecimal(balance)
        val rounded = bd.setScale(precision, BigDecimal.ROUND_HALF_UP)
        return rounded.toDouble()
    }

    fun showWelcomeBack() {
        if (welcomeScreenShownThisSession.getAndSet(true)) {
            return
        }

        viewModelScope.launch {
            // Trigger LED "Chad" pattern when we start showing the welcome message
            reflectiveLedPattern?.displayChad()
            val sdk = terminal ?: return@launch // SDK unavailable
            if (sdk.isAvailable()) {
                val message: String
                if (!isFirstLaunchCompleted()) {
                    message = "WELCOME ONBOARD ヽ(•◡•)ノ"
                    setFirstLaunchCompleted()
                } else {
                    var nextIndex = welcomeMessages.indices.random()
                    while (nextIndex == lastMessageIndex) {
                        nextIndex = welcomeMessages.indices.random()
                    }
                    message = welcomeMessages[nextIndex]
                    lastMessageIndex = nextIndex
                }

                // Add 2-second delay before displaying the message
                delay(500)
                sdk.displayBlackText(message)
            } else {
                welcomeScreenShownThisSession.set(false)
            }
        }
    }

    fun resetWelcomeScreenFlag() {
        welcomeScreenShownThisSession.set(false)
    }

    fun onHomeScreenVisible() {
        isHomeScreenVisible.set(true)
        // Start periodic refresh if not already running
        if (periodicRefreshJob?.isActive != true) {
            periodicRefreshJob = viewModelScope.launch {
                while (isActive && isHomeScreenVisible.get()) {
                    val now = System.currentTimeMillis()
                    val lastRefresh = sharedPrefs.getLong(KEY_LAST_REFRESH_TIME, 0L)
                    if (now - lastRefresh >= REFRESH_INTERVAL_MS) {
                        refreshAllBalances()
                        sharedPrefs.edit().putLong(KEY_LAST_REFRESH_TIME, now).apply()
                    }
                    delay(REFRESH_INTERVAL_MS)
                }
            }
        }
    }

    fun onHomeScreenHidden() {
        isHomeScreenVisible.set(false)
        periodicRefreshJob?.cancel()
        periodicRefreshJob = null
    }

    //LED Matrix

    fun showPlusMatrix(){
        viewModelScope.launch {
            reflectiveLedPattern?.displayPlus()
        }
    }

    fun clearMatrix(){
        viewModelScope.launch {
            reflectiveLedPattern?.clear()
        }
    }

}

sealed interface AssetsUiState {
    object Loading : AssetsUiState
    object Error : AssetsUiState
    object Empty : AssetsUiState
    data class Success(
        val assets: List<TokenAssetWithPrice>
    ) : AssetsUiState
}

sealed interface WalletDataUiState {
    object Loading : WalletDataUiState
    data class Success(val userData: UserData) : WalletDataUiState
}