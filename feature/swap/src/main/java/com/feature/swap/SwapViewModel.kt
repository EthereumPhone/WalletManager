package com.feature.swap

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.SwapRepository
import com.core.data.repository.UserDataRepository
import com.core.data.repository.DEFAULT_EXCLUDE_LIST
import com.core.data.repository.GroupedTokenRepository
import com.core.domain.GetAllGroupedTokensUsecase
import com.core.domain.GetSwapTokens
import com.core.domain.QueryTokenAssetsByNetwork
import com.core.model.TokenAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.UserData
import com.core.model.SwapUIState
import com.core.model.SwapToken
import com.core.result.Result
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ethereumphone.walletsdk.WalletSDK
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class SwapViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    getSwapTokens: GetSwapTokens,
    queryTokenAssetsByNetwork: QueryTokenAssetsByNetwork,
    private val swapRepository: SwapRepository,
    private val savedStateHandle: SavedStateHandle,
    private val getAllGroupedTokensUsecase: GetAllGroupedTokensUsecase,
    private val groupedTokenRepository: GroupedTokenRepository,
    ): ViewModel() {

    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    // Grouped tokens for token selection overlay
    val groupedTokenAssetState: StateFlow<GroupedAssetsUiState> =
        getAllGroupedTokensUsecase(DEFAULT_EXCLUDE_LIST).map {
            if (it.isEmpty()) {
                GroupedAssetsUiState.Empty
            } else {
                // Sort by highest fiat balance first (descending order)
                val sortedAssets = it.sortedByDescending { asset -> asset.totalFiatBalance ?: 0.0 }
                GroupedAssetsUiState.Success(sortedAssets)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GroupedAssetsUiState.Loading
        )

    // Token overlay visibility state
    private val _isTokenOverlayVisible = MutableStateFlow(false)
    val isTokenOverlayVisible: StateFlow<Boolean> = _isTokenOverlayVisible.asStateFlow()
    
    // Token selection mode state
    private val _tokenSelectionMode = MutableStateFlow<TokenSelectionMode>(TokenSelectionMode.None)
    val tokenSelectionMode: StateFlow<TokenSelectionMode> = _tokenSelectionMode.asStateFlow()

    // SwapUIState management
    private val _swapUIState = MutableStateFlow(SwapUIState())
    val swapUIState: StateFlow<SwapUIState> = _swapUIState.asStateFlow()

    fun showTokenOverlay(mode: TokenSelectionMode = TokenSelectionMode.From) {
        Log.d("SwapViewModel", "showTokenOverlay: mode=$mode")
        _tokenSelectionMode.value = mode
        _isTokenOverlayVisible.value = true
    }

    fun hideTokenOverlay() {
        Log.d("SwapViewModel", "hideTokenOverlay")
        _isTokenOverlayVisible.value = false
        _tokenSelectionMode.value = TokenSelectionMode.None
    }
    
    init {
        initializeSwapUIState()
        observeAndSetDefaultToken()
        
        // Log swapUIState changes for debugging
        viewModelScope.launch {
            swapUIState.collect { state ->
                Log.d("SwapViewModel", "SwapUIState updated - FROM: ${state.fromToken?.token?.symbol}, TO: ${state.toToken?.token?.symbol}")
            }
        }
    }
    
    private fun observeAndSetDefaultToken() {
        viewModelScope.launch {
            // Only collect until we find a success state with assets
            groupedTokenAssetState
                .filter { it is GroupedAssetsUiState.Success && it.assets.isNotEmpty() }
                .first()
                .let { state ->
                    if (state is GroupedAssetsUiState.Success && _swapUIState.value.fromToken == null) {
                        // Get the highest value token (first in the sorted list)
                        val highestToken = state.assets.first()
                        selectTokenFromCarousel(highestToken.groupId, setAsDefault = true)
                    }
                }
        }
    }
    
    private fun initializeSwapUIState() {
        _swapUIState.update { currentState ->
            currentState.copy(
                fromTitle = "FROM",
                toTitle = "TO",
                fromReadOnly = false,
                toReadOnly = true,
                fromOnAmountChange = { amount, _ -> 
                    updateFromAmount(amount)
                },
                fromOnMaxClick = {
                    // TODO: Implement max click functionality
                },
                fromOnTokenClick = {
                    Log.d("SwapViewModel", "fromOnTokenClick called")
                    showTokenOverlay(TokenSelectionMode.From)
                },
                toOnAmountChange = { amount, _ ->
                    updateToAmount(amount)
                },
                toOnMaxClick = {
                    // TODO: Implement max click functionality  
                },
                toOnTokenClick = {
                    showTokenOverlay(TokenSelectionMode.To)
                }
            )
        }
        Log.d("SwapViewModel", "SwapUIState initialized with callbacks")
    }
    
    private fun updateFromAmount(amount: String) {
        _swapUIState.update { currentState ->
            currentState.copy(
                fromCurrentAmount = amount,
                fromCurrentFiatAmount = calculateFiatAmount(amount, currentState.fromToken)
            )
        }
    }
    
    private fun updateToAmount(amount: String) {
        _swapUIState.update { currentState ->
            currentState.copy(
                toCurrentAmount = amount,
                toCurrentFiatAmount = calculateFiatAmount(amount, currentState.toToken)
            )
        }
    }
    
    private fun calculateFiatAmount(amount: String, token: SwapToken?): String {
        // TODO: Implement proper fiat calculation based on exchange rates
        return ""
    }
    
    fun selectTokenFromCarousel(groupId: String, setAsDefault: Boolean = false) {
        viewModelScope.launch {
            Log.d("SwapViewModel", "selectTokenFromCarousel called: groupId=$groupId, setAsDefault=$setAsDefault, mode=${_tokenSelectionMode.value}")
            
            // Capture the mode BEFORE any async operations
            val targetMode = if (setAsDefault) {
                TokenSelectionMode.From
            } else {
                _tokenSelectionMode.value
            }
            
            Log.d("SwapViewModel", "Target mode (captured): $targetMode")
            
            val groupedAssets = (groupedTokenAssetState.value as? GroupedAssetsUiState.Success)?.assets
            val selectedAsset = groupedAssets?.find { it.groupId == groupId }
            
            if (selectedAsset == null) {
                Log.e("SwapViewModel", "Could not find asset with groupId: $groupId")
                if (!setAsDefault) {
                    hideTokenOverlay()
                }
                return@launch
            }
            
            // Get the actual token details with proper chain information
            val swapToken = convertToSwapToken(selectedAsset, groupId)
            Log.d("SwapViewModel", "Converted to SwapToken: ${swapToken.token.symbol} on chain ${swapToken.token.chainId}")
            
            when (targetMode) {
                TokenSelectionMode.From -> {
                    Log.d("SwapViewModel", "Updating FROM token to ${swapToken.token.symbol}")
                    _swapUIState.update { currentState ->
                        currentState.copy(
                            fromToken = swapToken,
                            fromCurrentAmount = "",
                            fromCurrentFiatAmount = "",
                            fromUseMaxAmount = false
                        )
                    }
                    Log.d("SwapViewModel", "FROM token updated. Current: ${_swapUIState.value.fromToken?.token?.symbol}")
                }
                TokenSelectionMode.To -> {
                    Log.d("SwapViewModel", "Updating TO token to ${swapToken.token.symbol}")
                    _swapUIState.update { currentState ->
                        currentState.copy(
                            toToken = swapToken,
                            toCurrentAmount = "",
                            toCurrentFiatAmount = "",
                            toUseMaxAmount = false
                        )
                    }
                    Log.d("SwapViewModel", "TO token updated. Current: ${_swapUIState.value.toToken?.token?.symbol}")
                }
                TokenSelectionMode.None -> {
                    Log.w("SwapViewModel", "Target mode is None, not updating any token")
                }
            }
            
            // Hide overlay AFTER updating the state
            if (!setAsDefault) {
                hideTokenOverlay()
            }
        }
    }
    
    private suspend fun convertToSwapToken(asset: TokenGroupAssetOverview, groupId: String): SwapToken {
        Log.d("SwapViewModel", "convertToSwapToken: Converting ${asset.symbol} with groupId: $groupId")
        
        return try {
            // Get the actual token details with proper chain information
            val tokensInGroup = groupedTokenRepository.observeAllTokensWithPriceInGroup(groupId, filterZeroBalance = false)
                .first()
            
            Log.d("SwapViewModel", "convertToSwapToken: Found ${tokensInGroup.size} tokens in group")
            
            // Get the token with the highest fiat balance (most valuable chain for this token)
            val primaryToken = tokensInGroup.maxByOrNull { it.fiatAmount }
            
            val tokenAsset = if (primaryToken != null) {
                Log.d("SwapViewModel", "convertToSwapToken: Using primary token: ${primaryToken.symbol} on chain ${primaryToken.chainId}")
                // Use actual token data
                TokenAsset(
                    address = primaryToken.address,
                    chainId = primaryToken.chainId,
                    symbol = primaryToken.symbol,
                    name = primaryToken.name,
                    balance = primaryToken.balance,
                    decimals = primaryToken.decimals,
                    logoUrl = primaryToken.logoUrl,
                    swappable = primaryToken.swappable
                )
            } else {
                Log.w("SwapViewModel", "convertToSwapToken: No primary token found, using fallback")
                // Fallback to creating from overview data
                TokenAsset(
                    address = "0x0000000000000000000000000000000000000000",
                    chainId = 1,
                    symbol = asset.symbol,
                    name = asset.name,
                    balance = asset.totalBalance,
                    decimals = 18,
                    logoUrl = asset.logoUrl,
                    swappable = true
                )
            }
            
            SwapToken(
                token = tokenAsset,
                balance = asset.formattedBalance,
                fiatBalance = asset.formattedFiatBalance ?: "0.00",
                formattedMaxAmount = asset.formattedBalance,
                formattedMaxFiatAmount = asset.formattedFiatBalance ?: "0.00"
            )
        } catch (e: Exception) {
            Log.e("SwapViewModel", "convertToSwapToken: Error converting token", e)
            // Return fallback on error
            SwapToken(
                token = TokenAsset(
                    address = "0x0000000000000000000000000000000000000000",
                    chainId = 1,
                    symbol = asset.symbol,
                    name = asset.name,
                    balance = asset.totalBalance,
                    decimals = 18,
                    logoUrl = asset.logoUrl,
                    swappable = true
                ),
                balance = asset.formattedBalance,
                fiatBalance = asset.formattedFiatBalance ?: "0.00",
                formattedMaxAmount = asset.formattedBalance,
                formattedMaxFiatAmount = asset.formattedFiatBalance ?: "0.00"
            )
        }
    }

    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, "")

    val swapTokenUiState: StateFlow<SwapTokenUiState> =
        swapTokenUiState(
            userDataRepository,
            getSwapTokens,
            searchQuery
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SwapTokenUiState.Loading
        )

    private val _swapAssetsUiState = MutableStateFlow(AssetsUiState())
    val swapAssetsUiState = _swapAssetsUiState.asStateFlow()

    private val _amountsUiState = MutableStateFlow(AmountsUiState())
    val amountsUiState = _amountsUiState.asStateFlow()


    private val _selectedTextField = MutableStateFlow(TextFieldSelected.FROM)
    val selectedTextField = _selectedTextField.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    val exchangeRate: StateFlow<Double> =
        swapAssetsUiState.map { currentState ->
            val fromAsset = currentState.fromAsset
            val toAsset = currentState.toAsset

            if (fromAsset is SelectedTokenUiState.Selected && toAsset is SelectedTokenUiState.Selected) {
                _isSyncing.value = true
                val address = userDataRepository.userData.first().walletAddress

                val quote = swapRepository.getQuote(
                    fromAsset.tokenAsset.address,
                    toAsset.tokenAsset.address,
                    1.0,
                    address,
                    userDataRepository.userData.first().walletNetwork.toInt()
                )
                Log.d("getQuote", quote.toString())
                _isSyncing.value = false
                quote
            } else {
                0.0
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )

    fun setSelectedTextField(selectedTextField: TextFieldSelected) {
        _selectedTextField.value = selectedTextField
    }

    fun selectAsset(tokenAsset: TokenAsset) {
        viewModelScope.launch {
            _swapAssetsUiState.update { currentState ->
                val fromAsset = currentState.fromAsset
                val toAsset = currentState.toAsset

                when(_selectedTextField.value) {
                    TextFieldSelected.FROM -> {
                        if (toAsset is SelectedTokenUiState.Selected && toAsset.tokenAsset == tokenAsset) {
                            currentState.copy(
                                toAsset = SelectedTokenUiState.Unselected,
                                fromAsset = SelectedTokenUiState.Selected(tokenAsset)
                            )
                        } else {
                            currentState.copy(
                                fromAsset = SelectedTokenUiState.Selected(tokenAsset)
                            )
                        }
                    }

                    TextFieldSelected.TO -> {
                        if (fromAsset is SelectedTokenUiState.Selected && fromAsset.tokenAsset == tokenAsset) {
                            currentState.copy(
                                fromAsset = SelectedTokenUiState.Unselected,
                                toAsset = SelectedTokenUiState.Selected(tokenAsset)
                            )
                        } else {
                            currentState.copy(
                                toAsset = SelectedTokenUiState.Selected(tokenAsset)
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateAmount(
        selectedTextField: TextFieldSelected,
        amount: String,
    ) {
        val updatedAmount = amount.replace(",",".")

        viewModelScope.launch {
            exchangeRate.collect { rate ->
                _amountsUiState.update { currentState ->
                    Log.d("rate", rate.toString())
                    when(selectedTextField) {
                        TextFieldSelected.FROM -> {
                            val amountText = if (updatedAmount == "") "" else (BigDecimal(updatedAmount)
                                .multiply(BigDecimal(rate)))
                                .setScale(4, RoundingMode.HALF_EVEN)
                                .stripTrailingZeros()
                                .toPlainString()

                            currentState.copy(
                                fromAmount = updatedAmount,
                                toAmount = amountText
                            )
                        }
                        TextFieldSelected.TO -> {
                            val amountText = if(rate != 0.0) {
                                if(updatedAmount == "") "" else BigDecimal(updatedAmount)
                                    .divide(BigDecimal(rate), 4, RoundingMode.HALF_EVEN)
                                    .stripTrailingZeros()
                                    .toPlainString()
                            } else {
                                "0"
                            }

                            currentState.copy(
                                fromAmount = amountText,
                                toAmount = updatedAmount
                            )
                        }
                    }
                }
            }
        }
    }

    fun switchTokens() {
        _swapAssetsUiState.update { currentState ->
            val currentFromAsset = currentState.fromAsset

            currentState.copy(
                fromAsset = currentState.toAsset,
                toAsset = currentFromAsset
            )
        }

        _amountsUiState.update { currentState ->
            val currentFromAmount = currentState.fromAmount

            currentState.copy(
                fromAmount = currentState.toAmount,
                toAmount = currentFromAmount,
                )
        }
    }

    fun updateSearchQuery(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    fun swap(callback: (String) -> Unit) {
        viewModelScope.launch {
            val (fromAsset, toAsset) = swapAssetsUiState.value
            val fromAmount = amountsUiState.value.fromAmount
            try {
                if(fromAsset is SelectedTokenUiState.Selected && toAsset is SelectedTokenUiState.Selected) {
                    val result = swapRepository.swap(
                        fromAsset.tokenAsset.address,
                        toAsset.tokenAsset.address,
                        fromAmount.toDouble()
                    )
                    callback(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

enum class TextFieldSelected {
    FROM, TO
}

enum class TokenSelectionMode {
    None, From, To
}

private const val SEARCH_QUERY = "searchQuery"


@OptIn(ExperimentalCoroutinesApi::class)
private fun swapTokenUiState(
    userDataRepository: UserDataRepository,
    getSwapTokens: GetSwapTokens,
    searchQuery: Flow<String>
): Flow<SwapTokenUiState> = searchQuery.flatMapLatest { query ->
        getSwapTokens(
            query,
            userDataRepository.userData.first().walletNetwork.toInt()
        ).asResult()
            .mapLatest { result ->
                when(result) {
                    is Result.Error -> { SwapTokenUiState.Error }
                    is Result.Loading -> { SwapTokenUiState.Loading }
                    is Result.Success -> { SwapTokenUiState.Success(result.data) }
                }
            }
}

sealed interface SwapTokenUiState {
    object Loading: SwapTokenUiState
    object Error: SwapTokenUiState
    data class Success(val tokenAssets: List<TokenAsset>): SwapTokenUiState
}

data class AmountsUiState(
    val fromAmount: String = "",
    val toAmount: String = "",
)

data class AssetsUiState(
    val fromAsset: SelectedTokenUiState = SelectedTokenUiState.Unselected,
    val toAsset: SelectedTokenUiState = SelectedTokenUiState.Unselected
)

sealed interface SelectedTokenUiState {
    object Unselected: SelectedTokenUiState
    data class Selected(
        val tokenAsset: TokenAsset,
    ): SelectedTokenUiState
}

sealed interface WalletDataUiState {
    object Loading: WalletDataUiState
    data class Success(val userData: UserData): WalletDataUiState
}

sealed interface GroupedAssetsUiState {
    object Loading : GroupedAssetsUiState
    object Error : GroupedAssetsUiState
    object Empty : GroupedAssetsUiState
    data class Success(
        val assets: List<TokenGroupAssetOverview>
    ) : GroupedAssetsUiState
}
