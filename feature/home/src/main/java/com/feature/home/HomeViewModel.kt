package com.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.CoinbaseExchangeRepository
import com.core.data.repository.ExchangeRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.UpdateTokensByNetworkUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val updateTokensByNetworkUseCase: UpdateTokensByNetworkUseCase,
    private val userDataRepository: UserDataRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val coinbaseExchangeRepository: ExchangeRepository
): ViewModel() {

    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val shouldShowOnboarding: Flow<Boolean> =
        userDataRepository.userData.map { !it.onboardingCompleted }



    val tokenAssetState: StateFlow<AssetUiState> =
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
                AssetUiState.Success(netWorkAssets)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AssetUiState.Loading
            )

    val currentNetworkCurrency: StateFlow<AssetUiState> =
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
                val netWorkAsset = netWorkAssets.first { it.chainId ==  userDataRepository.userData.first().walletNetwork.toInt() }
                AssetUiState.Success(listOf(netWorkAsset))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AssetUiState.Loading
            )


    private val _refreshState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshState.asStateFlow()


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
}

sealed interface AssetUiState {
    object Loading: AssetUiState
    object Error: AssetUiState
    object Empty: AssetUiState
    data class Success(
        val assets: List<TokenAsset>
    ): AssetUiState
}

sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}