package com.feature.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenBalanceRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetGroupedTokenAssets
import com.core.domain.GetTokenBalancesWithMetadataUseCase
import com.core.domain.GetTransfersUseCase
import com.core.domain.UpdateTokensUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.Transfer
import com.core.model.TransferItem
import com.core.result.Result
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getTokenBalancesWithMetadataUseCase: GetTokenBalancesWithMetadataUseCase,
    getTransfersUseCase: GetTransfersUseCase,
    getGroupedTokenAssets: GetGroupedTokenAssets,
    private val updateTokensUseCase: UpdateTokensUseCase,
    userDataRepository: UserDataRepository,
    private val transferRepository: TransferRepository,
    private val networkBalanceRepository: NetworkBalanceRepository,
): ViewModel() {

    val userAddress: StateFlow<String> =
        userDataRepository.userData.map {
            it.walletAddress
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
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

    val transferState: StateFlow<TransfersUiState> =
        getTransfersUseCase()
            .map(TransfersUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransfersUiState.Loading
        )

    private val _refreshState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshState.asStateFlow()

    fun refreshData() {
        Log.d("refresh Started", "update started")

        viewModelScope.launch {
            _refreshState.value = true

            try {
                val address = userAddress.first()
                transferRepository.refreshTransfers(address)
                updateTokensUseCase(address)
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
                        symbol = name,
                        name = name,
                        balance = it.tokenBalance.toDouble()
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
                    if(tokens.isEmpty() && networkTokens.isEmpty()) {
                        AssetUiState.Empty
                    } else {
                        AssetUiState.Success(
                            networkTokens.sortedBy { it.chainId } + tokens.sortedBy { it.chainId }
                        )
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

sealed interface TransfersUiState {
    object Loading: TransfersUiState
    data class Success(val transfers: List<TransferItem>): TransfersUiState

}