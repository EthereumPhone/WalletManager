package com.feature.swap

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.SwapRepository
import com.core.data.repository.UserDataRepository
import com.core.data.repository.DEFAULT_EXCLUDE_LIST
import com.core.data.repository.GroupedTokenRepository
import com.core.data.repository.TerminalRepository
import com.core.data.repository.TerminalEvent
import com.core.domain.GetAllGroupedTokensUsecase
import com.core.domain.GetAllTokensUsecase
import com.core.domain.GetSwapTokens
import com.core.domain.GetSwappableTokensForSelection
import com.core.domain.QueryTokenAssetsByNetwork
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.UserData
import com.core.model.SwapUIState
import com.core.model.SwapToken
import com.core.result.Result
import com.core.result.asResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
    private val getAllTokensUsecase: GetAllTokensUsecase,
    private val groupedTokenRepository: GroupedTokenRepository,
    private val getSwappableTokensForSelection: GetSwappableTokensForSelection,
    private val terminalRepository: TerminalRepository,
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

    // Individual tokens by chain for token selection overlay
    val fromTokensState: StateFlow<FromTokensUiState> =
        getAllTokensUsecase().map { tokens ->
            if (tokens.isEmpty()) {
                FromTokensUiState.Empty
            } else {
                // Filter tokens with balance > 0 and sort by balance descending
                val tokensWithBalance = tokens.filter { it.balance > 0.0 }
                    .sortedByDescending { it.balance }
                FromTokensUiState.Success(tokensWithBalance)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FromTokensUiState.Loading
        )

    // Token overlay visibility state
    private val _isTokenOverlayVisible = MutableStateFlow(false)
    val isTokenOverlayVisible: StateFlow<Boolean> = _isTokenOverlayVisible.asStateFlow()
    
    // Token selection mode state
    private val _tokenSelectionMode = MutableStateFlow<TokenSelectionMode>(TokenSelectionMode.None)
    val tokenSelectionMode: StateFlow<TokenSelectionMode> = _tokenSelectionMode.asStateFlow()

    // Selected chain for token selector (default to Base)
    private val _selectedTokenChainId = MutableStateFlow<Int>(8453)
    val selectedTokenChainId: StateFlow<Int> = _selectedTokenChainId.asStateFlow()

    // SwapUIState management
    private val _swapUIState = MutableStateFlow(SwapUIState())
    val swapUIState: StateFlow<SwapUIState> = _swapUIState.asStateFlow()
    
    // Toast message state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

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
    
    fun setTokenSelectorChain(chainId: Int) {
        Log.d("SwapViewModel", "setTokenSelectorChain: chainId=$chainId")
        _selectedTokenChainId.value = chainId
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
        
        // Observe terminal events
        viewModelScope.launch {
            onSwapTerminalOpened()
            
            terminalRepository.events.collect { event ->
                if (event == TerminalEvent.SwapTapped) {
                    Log.d("SwapViewModel", "SwapTapped event received")
                    // Trigger swap when terminal swap button is tapped
                    swap { result ->
                        Log.d("SwapViewModel", "Terminal swap result: $result")
                    }
                }
            }
        }
    }
    
    private fun observeAndSetDefaultToken() {
        viewModelScope.launch {
            // Only collect until we find a success state with tokens
            fromTokensState
                .filter { it is FromTokensUiState.Success && it.tokens.isNotEmpty() }
                .first()
                .let { state ->
                    if (state is FromTokensUiState.Success && _swapUIState.value.fromToken == null) {
                        // Get the highest balance token (first in the sorted list)
                        val highestBalanceToken = state.tokens.first()
                        selectFromTokenAsset(highestBalanceToken)
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

    fun selectToTokenAsset(tokenAsset: TokenAsset) {
        viewModelScope.launch {
            Log.d("SwapViewModel", "selectToTokenAsset: ${tokenAsset.symbol} on chain ${tokenAsset.chainId}")
            
            val currentFromToken = _swapUIState.value.fromToken
            
            // Check if this is a cross-chain swap attempt
            if (currentFromToken != null && currentFromToken.token.chainId != tokenAsset.chainId) {
                Log.d("SwapViewModel", "Cross-chain detected: FROM chain ${currentFromToken.token.chainId}, TO chain ${tokenAsset.chainId}")
                
                val fromSymbol = currentFromToken.token.symbol.uppercase()
                val toSymbol = tokenAsset.symbol.uppercase()
                
                // Only allow cross-chain for ETH-ETH or USDC-USDC (preparing for future bridge integration)
                val isValidBridgePair = (fromSymbol == "ETH" && toSymbol == "ETH") ||
                                       (fromSymbol == "USDC" && toSymbol == "USDC")
                
                if (!isValidBridgePair) {
                    Log.w("SwapViewModel", "Invalid cross-chain pair: $fromSymbol (chain ${currentFromToken.token.chainId}) -> $toSymbol (chain ${tokenAsset.chainId})")
                    
                    // Strategy: Try to find a same-chain alternative, otherwise unselect TO token
                    val sameChainAlternative = findTokenBySymbolAndChain(toSymbol, currentFromToken.token.chainId)
                    
                    if (sameChainAlternative != null) {
                        // Found same token on FROM chain - use that instead
                        Log.d("SwapViewModel", "Auto-switching TO token to $toSymbol on chain ${currentFromToken.token.chainId} (same chain as FROM)")
                        val swapToken = SwapToken(
                            token = sameChainAlternative,
                            balance = "",
                            fiatBalance = "",
                            formattedMaxAmount = "",
                            formattedMaxFiatAmount = ""
                        )
                        _swapUIState.update { currentState ->
                            currentState.copy(
                                toToken = swapToken,
                                toCurrentAmount = "",
                                toCurrentFiatAmount = "",
                                toUseMaxAmount = false
                            )
                        }
                    } else {
                        // No same-chain alternative found - unselect TO token
                        Log.d("SwapViewModel", "No same-chain alternative found. Unselecting TO token.")
                        _swapUIState.update { currentState ->
                            currentState.copy(
                                toToken = null,
                                toCurrentAmount = "",
                                toCurrentFiatAmount = "",
                                toUseMaxAmount = false
                            )
                        }
                        // Show toast notification with chain name
                        val chainName = NetworkChain.getNetworkByChainId(currentFromToken.token.chainId)?.name ?: "chain ${currentFromToken.token.chainId}"
                        _toastMessage.value = "Cross-chain swap not supported. Select a token on $chainName"
                    }
                    
                    hideTokenOverlay()
                    return@launch
                }
            }
            
            // Valid selection (same chain or valid bridge pair) - proceed normally
            val swapToken = SwapToken(
                token = tokenAsset,
                balance = "",
                fiatBalance = "",
                formattedMaxAmount = "",
                formattedMaxFiatAmount = ""
            )
            _swapUIState.update { currentState ->
                currentState.copy(
                    toToken = swapToken,
                    toCurrentAmount = "",
                    toCurrentFiatAmount = "",
                    toUseMaxAmount = false
                )
            }
            hideTokenOverlay()
        }
    }
    
    /**
     * Find a token by symbol and chain ID from the user's token list
     * @param requireBalance If true, only returns tokens with balance > 0 (for FROM token selection)
     *                       If false, returns any matching token (for TO token selection)
     */
    private suspend fun findTokenBySymbolAndChain(
        symbol: String, 
        chainId: Int,
        requireBalance: Boolean = false
    ): TokenAsset? {
        return try {
            val allTokens = getAllTokensUsecase().first()
            allTokens.find { 
                it.symbol.uppercase() == symbol.uppercase() && 
                it.chainId == chainId &&
                (!requireBalance || it.balance > 0.0)
            }
        } catch (e: Exception) {
            Log.e("SwapViewModel", "Error finding token by symbol and chain", e)
            null
        }
    }
    
    fun selectFromTokenAsset(tokenAsset: TokenAsset) {
        viewModelScope.launch {
            Log.d("SwapViewModel", "selectFromTokenAsset: ${tokenAsset.symbol} on chain ${tokenAsset.chainId}")
            
            // Format the balance for display
            val formattedBalance = String.format("%.6f", tokenAsset.balance).trimEnd('0').trimEnd('.')
            
            val swapToken = SwapToken(
                token = tokenAsset,
                balance = formattedBalance,
                fiatBalance = "", // TODO: Add price calculation
                formattedMaxAmount = formattedBalance,
                formattedMaxFiatAmount = "" // TODO: Add fiat calculation
            )
            
            _swapUIState.update { currentState ->
                currentState.copy(
                    fromToken = swapToken,
                    fromCurrentAmount = "",
                    fromCurrentFiatAmount = "",
                    fromUseMaxAmount = false
                )
            }
            
            Log.d("SwapViewModel", "FROM token updated. Current: ${_swapUIState.value.fromToken?.token?.symbol}")
            hideTokenOverlay()
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

    // Enhanced token list that includes both owned and available swappable tokens
    val swapTokenUiState: StateFlow<SwapTokenUiState> = combine(
        swapUIState,
        searchQuery,
        selectedTokenChainId
    ) { uiState, query, chainId ->
        Triple(uiState.fromToken?.token, query, chainId)
    }.flatMapLatest { (fromToken, query, chainId) ->
        getSwappableTokensForSelection(
            excludeToken = fromToken,
            targetChainId = chainId,
            query = query
        ).asResult().map { result ->
            when(result) {
                is Result.Error -> SwapTokenUiState.Error
                is Result.Loading -> SwapTokenUiState.Loading
                is Result.Success -> SwapTokenUiState.Success(result.data)
            }
        }
    }.stateIn(
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
    
    fun clearToastMessage() {
        _toastMessage.value = null
    }

    /**
     * Unified swap entry point - called by both debug swap button and terminal swap button
     * 
     * IMPORTANT: The 0x API only supports same-chain swaps. Cross-chain swaps/bridging
     * requires integration with a dedicated bridge protocol (e.g., Across Protocol, LayerZero).
     */
    fun swap(callback: (String) -> Unit) {
        viewModelScope.launch {
            Log.d("SwapViewModel", "=== SWAP INITIATED ===")
            
            // Prefer the new unified UI state when available
            val uiState = swapUIState.value
            val fromToken = uiState.fromToken
            val toToken = uiState.toToken
            val fromAmountFromUi = uiState.fromCurrentAmount

            // Legacy state fallback
            val (fromAsset, toAsset) = swapAssetsUiState.value
            val fromAmountLegacy = amountsUiState.value.fromAmount
            
            try {
                // Check if we have valid tokens from the new UI state
                val hasUiTokens = fromToken != null && toToken != null && !fromAmountFromUi.isNullOrBlank()
                
                if (hasUiTokens) {
                    // Check if this is a cross-chain swap attempt
                    val isCrossChain = fromToken!!.token.chainId != toToken!!.token.chainId
                    
                    if (isCrossChain) {
                        // 0x API does NOT support cross-chain swaps
                        val fromSymbol = fromToken.token.symbol.uppercase()
                        val toSymbol = toToken.token.symbol.uppercase()
                        val isValidBridgePair = (fromSymbol == "ETH" && toSymbol == "ETH") ||
                                               (fromSymbol == "USDC" && toSymbol == "USDC")
                        
                        if (isValidBridgePair) {
                            Log.w("SwapViewModel", "🌉 BRIDGING NEEDED!")
                            Log.w("SwapViewModel", "  FROM: $fromSymbol on chain ${fromToken.token.chainId}")
                            Log.w("SwapViewModel", "  TO: $toSymbol on chain ${toToken.token.chainId}")
                            Log.w("SwapViewModel", "  AMOUNT: $fromAmountFromUi")
                            Log.w("SwapViewModel", "  NOTE: 0x API does not support cross-chain. Bridge protocol integration required.")
                            
                            callback("Error: Bridging not yet implemented. 0x API only supports same-chain swaps. " +
                                    "Please integrate a bridge protocol (Across, LayerZero, etc.) for cross-chain functionality.")
                        } else {
                            Log.e("SwapViewModel", "❌ INVALID CROSS-CHAIN PAIR!")
                            Log.e("SwapViewModel", "  FROM: $fromSymbol on chain ${fromToken.token.chainId}")
                            Log.e("SwapViewModel", "  TO: $toSymbol on chain ${toToken.token.chainId}")
                            Log.e("SwapViewModel", "  NOTE: Cross-chain swaps are not supported by 0x API")
                            
                            callback("Error: Cross-chain swaps are not supported. Please select tokens on the same chain.")
                        }
                        return@launch
                    } else {
                        // Same chain - execute normal swap via 0x
                        Log.d("SwapViewModel", "✅ SAME-CHAIN SWAP (via 0x API)")
                        Log.d("SwapViewModel", "  FROM: ${fromToken.token.symbol} (${fromToken.token.address})")
                        Log.d("SwapViewModel", "  TO: ${toToken.token.symbol} (${toToken.token.address})")
                        Log.d("SwapViewModel", "  CHAIN: ${fromToken.token.chainId}")
                        Log.d("SwapViewModel", "  AMOUNT: $fromAmountFromUi")
                        
                        executeSwap(
                            fromAddress = fromToken.token.address,
                            toAddress = toToken.token.address,
                            amount = fromAmountFromUi,
                            callback = callback
                        )
                    }
                } else if (fromAsset is SelectedTokenUiState.Selected && toAsset is SelectedTokenUiState.Selected) {
                    // Legacy state handling
                    Log.d("SwapViewModel", "Using legacy swap state")
                    
                    // Check if cross-chain in legacy state too
                    if (fromAsset.tokenAsset.chainId != toAsset.tokenAsset.chainId) {
                        Log.e("SwapViewModel", "❌ Cross-chain swap attempted in legacy state (not supported)")
                        callback("Error: Cross-chain swaps are not supported.")
                        return@launch
                    }
                    
                    executeSwap(
                        fromAddress = fromAsset.tokenAsset.address,
                        toAddress = toAsset.tokenAsset.address,
                        amount = fromAmountLegacy,
                        callback = callback
                    )
                } else {
                    Log.e("SwapViewModel", "❌ No valid tokens selected")
                    callback("Error: Please select both FROM and TO tokens")
                }
            } catch (e: Exception) {
                Log.e("SwapViewModel", "Error during swap", e)
                callback("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Execute a same-chain swap via 0x API
     */
    private suspend fun executeSwap(
        fromAddress: String,
        toAddress: String,
        amount: String,
        callback: (String) -> Unit
    ) {
        val amt = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
        if (amt > 0) {
            val result = swapRepository.swap(
                fromAddress,
                toAddress,
                amt
            )
            Log.d("SwapViewModel", "Swap result: $result")
            callback(result)
        } else {
            Log.e("SwapViewModel", "Invalid amount: $amount")
            callback("Error: Invalid amount")
        }
    }

    /**
     * Calls this function when the swap screen is opened
     * When opened it displays the swap button on terminal
     */
    suspend fun onSwapTerminalOpened() {
        try {
            // Use TerminalRepository to generate swap screen
            terminalRepository.generateSwap()
        } catch (e: Exception) {
            Log.e("SwapViewModel", "Error displaying swap terminal screen", e)
        }
    }

    fun onScreenOpenedAfterResume() {
        viewModelScope.launch {
            try {
                // Redraw the terminal content after resume
                delay(300)
                terminalRepository.generateSwap()
            } catch (e: Exception) {
                Log.e("SwapViewModel", "Error redrawing swap terminal screen after resume", e)
            }
        }
    }

    fun onSwapTerminalClosed() {
        viewModelScope.launch {
            try {
                // Use TerminalRepository to dismiss content
                terminalRepository.dismissContent()
            } catch (e: Exception) {
                Log.e("SwapViewModel", "Error removing swap terminal screen", e)
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


// Legacy helper function - no longer used, replaced by enhanced getSwappableTokensForSelection
// Keeping for reference in case rollback is needed
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

sealed interface FromTokensUiState {
    object Loading : FromTokensUiState
    object Error : FromTokensUiState
    object Empty : FromTokensUiState
    data class Success(
        val tokens: List<TokenAsset>
    ) : FromTokensUiState
}
