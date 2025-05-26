package com.feature.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.TokenExchangeRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainIdToRPC
import com.core.database.dao.TokenExchangeDao
import com.core.domain.UpdateTokensByNetworkUseCase
import com.core.domain.GetTokenBalancesWithMetadataUseCase
import com.core.model.NetworkChain
import com.core.model.Price
import com.core.model.TokenAsset
import com.core.model.TokenData
import com.core.model.UserData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val updateTokensByNetworkUseCase: UpdateTokensByNetworkUseCase,
    private val userDataRepository: UserDataRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val tokenExchangeRepository: TokenExchangeRepository,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val transferRepository: TransferRepository,
    private val getTokenBalancesWithMetadataUseCase: GetTokenBalancesWithMetadataUseCase,
    private val walletSDK: WalletSDK?,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {


    init {
        Log.d("HomeViewModel", "Initializing HomeViewModel")
        // Collect the flow to trigger database operations
        viewModelScope.launch {
            tokenExchangeRepository.getExchanges()
                .collect { exchanges ->
                    Log.d("HomeViewModel", "Received ${exchanges.size} exchanges")
                }
        }
    }

    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val tokenAssetState: StateFlow<AssetsUiState> =
        combine(
            networkBalanceRepository.getNetworksBalance(),
            getTokenBalancesWithMetadataUseCase(),
            ::Pair
        )
        .map { (networkBalances, tokenBalances) ->
            val netWorkAssets = networkBalances.map {
                val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""
                
                TokenAsset(
                    address = it.contractAddress,
                    chainId = it.chainId,
                    symbol = name.lowercase(),
                    name = name.lowercase(),
                    balance = formatSmallBalance(it.tokenBalance.toDouble()),
                    decimals = 18
                )
            }
            .filter { it.balance > 0 }
            .sortedByDescending { it.balance }
            
            // Add token balances to the list
            val allAssets = netWorkAssets + tokenBalances
                .filter { it.balance > 0 }
                .map { token ->
                    // Create a copy with properly formatted balance
                    token.copy(balance = formatSmallBalance(token.balance))
                }
                .filter { token ->
                    // Filter out tokens with URLs in their names or symbols
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
            
            // Set the first value of selectedTokenAsset to the first item in the list
            if (allAssets.isNotEmpty()) {
                _selectedTokenAsset.value = allAssets.first()
            }
            
            if (allAssets.isEmpty()) {
                AssetsUiState.Empty
            } else {
                AssetsUiState.Success(allAssets)
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

    private val _refreshState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshState.asStateFlow()

    private val _selectedTokenAsset = MutableStateFlow<TokenAsset?>(null)
    val selectedTokenAsset: StateFlow<TokenAsset?> = _selectedTokenAsset.asStateFlow()

    fun setSelectedTokenAsset(tokenAsset: TokenAsset) {
        _selectedTokenAsset.value = tokenAsset
    }

   fun getSelectedTokenAsset(): TokenAsset? {
        return _selectedTokenAsset.value
    }

    suspend fun getLink(uri: String): String {
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
                    return@withContext moonpayResponse?.link ?: throw IOException("Invalid response format")
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


    fun refreshData() {
        refreshAllBalances()
    }

    fun setOnboardingComplete(onboardingComplete: Boolean){
        viewModelScope.launch {
            userDataRepository.setOnboardingCompleted(onboardingComplete)
        }
    }

    fun changeNetwork(network: Int){
        viewModelScope.launch {
            walletSDK?.changeChain(network, chainIdToRPC(network), chainIdToBundler(network)) // chainIdToBundler(network))//"https://eth-mainnet.g.alchemy.com/v2/${chainToApiKey("eth-mainnet")}")
            userDataRepository.setWalletNetwork(network.toString())
        }
    }

    // We'll store our user input in the SavedStateHandle under a certain key


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

    fun loadSymbol(symbol: List<String>) {


    }

    val tokenMetadata = tokenMetadataRepository.getTokensMetadata()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

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

}

sealed interface AssetsUiState {
    object Loading: AssetsUiState
    object Error: AssetsUiState
    object Empty: AssetsUiState
    data class Success(
        val assets: List<TokenAsset>
    ): AssetsUiState
}

sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}