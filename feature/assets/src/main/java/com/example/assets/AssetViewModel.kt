package com.example.assets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetTokenBalancesWithMetadataUseCase
import com.core.domain.UpdateTokensUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.UserData
import com.core.result.Result
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AssetViewModel @Inject constructor(
    getTokenBalancesWithMetadataUseCase: GetTokenBalancesWithMetadataUseCase,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val updateTokensUseCase: UpdateTokensUseCase,
    private val userDataRepository: UserDataRepository,
    private val transferRepository: TransferRepository,
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
            getTokenBalancesWithMetadataUseCase,
            networkBalanceRepository
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AssetUiState.Loading
        )



    private val _exchange = MutableStateFlow("")
    val exchange: Flow<String> = _exchange

    private val _refreshState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshState.asStateFlow()


    fun refreshData() {
        Log.d("refresh Started", "update started")

        viewModelScope.launch {
            _refreshState.value = true

            try {
                val userData = userDataRepository.userData.first()
                transferRepository.refreshTransfers(userData.walletAddress)
                updateTokensUseCase(userData.walletAddress)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _refreshState.value = false
        }
    }

}

fun assetUiState(
    getTokenBalancesWithMetadataUseCase: GetTokenBalancesWithMetadataUseCase,
    networkBalanceRepository: NetworkBalanceRepository
): Flow<AssetUiState> {
    // observe erc20 tokens
    val erc20Tokens: Flow<List<TokenAsset>> =
        getTokenBalancesWithMetadataUseCase()

    // observe network currency
    val networkToken: Flow<List<TokenAsset>> =
        networkBalanceRepository.getNetworksBalance()
            .map { balances ->
                balances.map {
                    val name = NetworkChain.getNetworkByChainId(it.chainId)?.name ?: ""
                    TokenAsset(
                        address = it.contractAddress,
                        chainId = it.chainId,
                        symbol = if(name.contains("POLYGON")) "matic" else "eth",
                        name = if(name.contains("POLYGON")) "matic" else "eth",
                        balance = it.tokenBalance.toDouble(),
                        decimals = 18
                    )
                }
            }
    return combine(
        erc20Tokens,
        networkToken,
        ::Pair
    ).asResult()
        .map { tokenToTokeResult ->
            when(tokenToTokeResult) {
                is Result.Success -> {
                    val (tokens, networkTokens) = tokenToTokeResult.data
                    val filteredAssets = networkTokens.filter { it.balance != 0.0 } + tokens.filter { it.balance != 0.0 }
                    if(filteredAssets.isEmpty()) {
                        AssetUiState.Empty
                    } else {
                        AssetUiState.Success(filteredAssets.filter { it.chainId != 5 })
                    }
                }
                is Result.Loading -> {
                    AssetUiState.Loading
                }
                is Result.Error -> {
                    AssetUiState.Error
                }
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