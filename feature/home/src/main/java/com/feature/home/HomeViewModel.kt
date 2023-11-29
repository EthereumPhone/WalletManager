package com.feature.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.ExchangeApi
import com.core.domain.GetGroupedTokenAssets
import com.core.domain.GetTokenAssetsByNetwork
import com.core.domain.GetTokenBalancesWithMetadataUseCase
import com.core.domain.GetTransfersByNetwork
import com.core.domain.GetTransfersUseCase
import com.core.domain.UpdateTokensByNetworkUseCase
import com.core.domain.UpdateTokensUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.model.UserData
import com.core.result.Result
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getTokenAssetsByNetwork: GetTokenAssetsByNetwork,
    getTransfersByNetwork: GetTransfersByNetwork,
    private val updateTokensByNetworkUseCase: UpdateTokensByNetworkUseCase,
    private val userDataRepository: UserDataRepository,
    private val transferRepository: TransferRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
): ViewModel() {

    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }
        .stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val tokenAssetState: StateFlow<AssetUiState> =
        assetUiState(
            getTokenAssetsByNetwork,
            networkBalanceRepository,
            userDataRepository
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssetUiState.Loading
        )

    private val _refreshState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transferState: StateFlow<TransfersUiState> =
        userDataRepository.userData.flatMapLatest {
            getTransfersByNetwork(it.walletNetwork.toInt())
        }
            .map(TransfersUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransfersUiState.Loading
        )


    fun refreshData() {
        viewModelScope.launch {

            try {
                withContext(Dispatchers.IO) {
                    val userData = userDataRepository.userData.first()
                    transferRepository.refreshTransfersByNetwork(userData.walletAddress, userData.walletNetwork.toInt())
                    updateTokensByNetworkUseCase(userData.walletAddress, userData.walletNetwork.toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions if needed
            }

        }
    }
}


  fun assetUiState(
    getTokenAssetsByNetwork: GetTokenAssetsByNetwork,
    networkBalanceRepository: NetworkBalanceRepository,
    userDataRepository: UserDataRepository
): Flow<AssetUiState> {

    // observe erc20 tokens
    val erc20Tokens: Flow<List<TokenAsset>> = getTokenAssetsByNetwork()


    // observe network currency
      val networkToken: Flow<List<TokenAsset>> =
          networkBalanceRepository.getNetworksBalance()
              .map { balances ->
                  balances.map {
                      val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""
                      val isPolygon = name.contains("POLYGON")
                      TokenAsset(
                          address = it.contractAddress,
                          chainId = it.chainId,
                          symbol = if (isPolygon) "matic" else "eth",
                          name = if (isPolygon) "matic" else "eth",
                          balance = it.tokenBalance.toDouble(),
                          decimals = 18
                      )
                  }
              }

    return combine(
            erc20Tokens,
            networkToken,
            userDataRepository.userData,
            ::Triple
        ).asResult()
            .mapLatest { tokenToTokeResult ->
                when(tokenToTokeResult) {
                    is Result.Success -> {
                        val (tokens, networkTokens, userData) = tokenToTokeResult.data
                        val allAssets = networkTokens.filter { it.balance != 0.0 } + tokens.filter { it.balance != 0.0 }
                        val filteredAssets = allAssets.filter { it.chainId == userData.walletNetwork.toInt() }
                        if(filteredAssets.isEmpty()) {
                            AssetUiState.Empty
                        } else {
                            AssetUiState.Success(filteredAssets)
                        }
                    }
                    is Result.Loading -> AssetUiState.Loading
                    is Result.Error -> AssetUiState.Error
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

sealed interface TransfersUiState {
    object Empty: TransfersUiState
    object Loading: TransfersUiState
    data class Success(val transfers: List<TransferItem>): TransfersUiState
}

sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}