package com.example.assets

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.AlchemyTransferRepository
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.ExchangeApi
import com.core.domain.GetGroupedTokenAssets
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AssetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tokenAssetBySymbolUseCase: GetTokenAssetsBySymbolUseCase,
): ViewModel() {

    private val assetDetailArgs: SymbolArgs = SymbolArgs(savedStateHandle)

    val symbol = assetDetailArgs.symbol

    val currentState: StateFlow<AssetDetailUiState> =
        tokenAssetBySymbolUseCase(
            symbol = assetDetailArgs.symbol
        )
            .map(AssetDetailUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AssetDetailUiState.Loading
            )


}

sealed interface AssetDetailUiState {
    object Loading: AssetDetailUiState
    data class Success(val asset: List<TokenAsset>): AssetDetailUiState

}

