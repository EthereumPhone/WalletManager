package com.feature.swap

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.NetworkBalanceRepository
import com.core.data.repository.TokenMetadataRepository
import com.core.data.repository.UniSwapRepository
import com.core.data.repository.UserDataRepository
import com.core.domain.GetTokenBalancesWithMetadataUseCase
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenMetadata
import com.core.result.Result
import com.core.result.asResult
import com.feature.home.AssetUiState
import com.feature.home.assetUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SwapViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    getTokenBalancesWithMetadataUseCase: GetTokenBalancesWithMetadataUseCase,
    private val networkBalanceRepository: NetworkBalanceRepository,
    private val tokenMetadataRepository: TokenMetadataRepository,
    private val savedStateHandle: SavedStateHandle,
    private val uniSwapRepository: UniSwapRepository,

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

    val uniswapTokenState: StateFlow<UniswapTokenUiState> =
        tokenMetadataRepository.getTokensMetadata()
            .map { tokenList -> tokenList.filter { it.chainId == 1 } }
            .map(UniswapTokenUiState::Success)
            .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UniswapTokenUiState.Loading
        )


    val fromTokenAmount = savedStateHandle.getStateFlow(FROM_AMOUNT, "")
    val toTokenAmount = savedStateHandle.getStateFlow(TO_AMOUNT, "")


    fun updateFromTokenTextField(input: String) {
        savedStateHandle[FROM_AMOUNT] = input
        savedStateHandle[TO_AMOUNT] = ""
    }

    fun updateToTokenTextField(input: String) {
        savedStateHandle[TO_AMOUNT] = input
        savedStateHandle[FROM_AMOUNT] = input
    }

    fun switchTokens() {

    }

    fun swap() {

    }
}

private const val FROM_AMOUNT = "fromAmount"
private const val TO_AMOUNT = "toAmount"

sealed interface UniswapTokenUiState {
    object Loading: UniswapTokenUiState
    object Error: UniswapTokenUiState
    data class Success(val tokenMetadata: List<TokenMetadata>): UniswapTokenUiState
}


