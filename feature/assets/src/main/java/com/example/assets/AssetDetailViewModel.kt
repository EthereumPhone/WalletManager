package com.example.assets

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.AlchemyTransferRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetGroupedTokenAssets
import com.core.domain.GetTokenAssetsByNetwork
import com.core.domain.GetTokenAssetsBySymbolUseCase
import com.core.domain.GetTokenBalancesWithMetadataUseCase
import com.core.domain.GetTransfersUseCase
import com.core.domain.UpdateTokensUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.model.UserData
import com.core.result.Result
import com.core.result.asResult
import com.example.assets.navigation.SymbolArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AssetDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val tokenAssetBySymbolUseCase: GetTokenAssetsBySymbolUseCase,
    private val networkBalanceRepository: NetworkBalanceRepository
): ViewModel() {

    private val assetDetailArgs: SymbolArgs = SymbolArgs(savedStateHandle)

    val symbol = assetDetailArgs.symbol

    val currentState: StateFlow<DetailAssetUiState> =
        assetUiState(
            tokenAssetBySymbolUseCase,
            networkBalanceRepository,
            assetDetailArgs.symbol
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailAssetUiState.Loading
        )
}


fun assetUiState(
    getTokenAssetsByNetwork: GetTokenAssetsBySymbolUseCase,
    networkBalanceRepository: NetworkBalanceRepository,
    symbol: String
): Flow<DetailAssetUiState> {

    // observe erc20 tokens
    val erc20Tokens: Flow<List<TokenAsset>> = getTokenAssetsByNetwork(symbol)


    // observe network currency
    val networkToken: Flow<List<TokenAsset>> =
        networkBalanceRepository.getNetworksBalance()
            .map { balances ->
                balances
                    .map {
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
        ::Pair
    ).asResult()
        .mapLatest { tokenToTokeResult ->
            when(tokenToTokeResult) {
                is Result.Success -> {
                    val (tokens, networkTokens) = tokenToTokeResult.data
                    val filteredNetwork = networkTokens.filter { it.symbol == symbol }
                    DetailAssetUiState.Success(filteredNetwork + tokens)
                }
                is Result.Loading -> DetailAssetUiState.Loading
                is Result.Error -> DetailAssetUiState.Error
            }
        }
}


sealed interface DetailAssetUiState {
    object Loading: DetailAssetUiState
    object Error: DetailAssetUiState
    object Empty: DetailAssetUiState
    data class Success(
        val assets: List<TokenAsset>
    ): DetailAssetUiState
}


