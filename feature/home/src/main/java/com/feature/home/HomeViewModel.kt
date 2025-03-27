package com.feature.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.TokenExchangeRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.chainIdToBundler
import com.core.data.util.chainIdToRPC
import com.core.domain.UpdateTokensByNetworkUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenData
import com.core.model.UserData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val updateTokensByNetworkUseCase: UpdateTokensByNetworkUseCase,
    private val userDataRepository: UserDataRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val tokenExchangeRepository: TokenExchangeRepository,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val walletSDK: WalletSDK?,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {


    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val tokenAssetState: StateFlow<AssetsUiState> =
        networkBalanceRepository.getNetworksBalance()
            .map { balances ->
                val netWorkAssets = balances.map {
                    val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""

                    TokenAsset(
                        address = it.contractAddress,
                        chainId = it.chainId,
                        symbol = name.lowercase(),
                        name = name.lowercase(),
                        balance = it.tokenBalance.toDouble(),
                        decimals = 18
                    )
                }
                .sortedByDescending { it.balance }

                // Set the first value of selectedTokenAsset to the last item in the list
                if (netWorkAssets.isNotEmpty()) {
                    _selectedTokenAsset.value = netWorkAssets.last()
                }

                AssetsUiState.Success(netWorkAssets)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AssetsUiState.Loading
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
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val userData = userDataRepository.userData.first()
                    updateTokensByNetworkUseCase(userData.walletAddress, userData.walletNetwork.toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions if needed
            }
        }
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

    // We’ll store our user input in the SavedStateHandle under a certain key


    private val _tokenData = MutableStateFlow<List<TokenData>>(emptyList())
    val tokenData = _tokenData.asStateFlow()

    fun loadSymbol(symbol: List<String>) {
        viewModelScope.launch {
            try {
                tokenExchangeRepository.fetchExchangeBySymbols(symbol)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    val tokenMetadata = tokenMetadataRepository.getTokensMetadata()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

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