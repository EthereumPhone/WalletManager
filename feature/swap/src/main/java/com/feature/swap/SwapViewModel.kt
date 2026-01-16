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
import com.core.domain.GetAllTokensWithExchangeUsecase
import com.core.domain.GetSwapTokens
import com.core.domain.GetSwappableTokensForSelection
import com.core.domain.LookupTokenByAddress
import com.core.domain.QueryTokenAssetsByNetwork
import com.core.model.NetworkChain
import com.core.model.TokenAsset
import com.core.model.TokenGroupAssetOverview
import com.core.model.UserData
import com.core.model.SwapUIState
import com.core.model.SwapToken
import com.core.model.TokenAssetWithPrice
import com.core.result.Result
import com.core.result.asResult
import com.feature.swap.ui.SwapTransactionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject
import com.core.database.dao.TokenBalanceDao
import com.core.database.dao.TransferDao
import com.core.database.model.TransferEntity
import com.core.database.model.RawContract
import com.core.database.model.Erc1155MetadataObject
import kotlinx.datetime.Clock
import kotlinx.coroutines.Dispatchers
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalLEDController

@HiltViewModel
class SwapViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    getSwapTokens: GetSwapTokens,
    queryTokenAssetsByNetwork: QueryTokenAssetsByNetwork,
    private val swapRepository: SwapRepository,
    private val savedStateHandle: SavedStateHandle,
    private val getAllGroupedTokensUsecase: GetAllGroupedTokensUsecase,
    private val getAllTokensUsecase: GetAllTokensUsecase,
    private val getAllTokensWithExchangeUsecase: GetAllTokensWithExchangeUsecase,
    private val groupedTokenRepository: GroupedTokenRepository,
    private val getSwappableTokensForSelection: GetSwappableTokensForSelection,
    private val terminalRepository: TerminalRepository,
    private val tokenBalanceDao: TokenBalanceDao,
    private val transferDao: TransferDao,
    private val reflectiveLedPattern: ReflectiveLedPattern?,
    private val lookupTokenByAddress: LookupTokenByAddress,
    ): ViewModel() {

    private val supportedSwapChainIds = setOf(1, 10, 137, 42161, 8453)

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
    // Note: Default FROM token is selected by highest USD price on Base (chainId 8453)
    val fromTokensState: StateFlow<FromTokensUiState> =
        getAllTokensWithExchangeUsecase().map { tokens ->
            if (tokens.isEmpty()) {
                FromTokensUiState.Empty
            } else {
                // Apply the same spam filter used on Home (filter out tokens with URL-like patterns)
                val spamFiltered = tokens.filter { token ->
                    DEFAULT_EXCLUDE_LIST.none { pattern ->
                        token.name.contains(pattern) || token.symbol.contains(pattern)
                    }
                }
                // Filter tokens with balance > 0 and sort by balance descending for display
                val tokensWithBalance = spamFiltered.filter { it.balance > 0.0 }
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
    
    // Transaction status state for overlay
    private val _swapTransactionStatus = MutableStateFlow<SwapTransactionStatus?>(null)
    val swapTransactionStatus: StateFlow<SwapTransactionStatus?> = _swapTransactionStatus.asStateFlow()
    
    // Quote fetching state
    private val _isFetchingQuote = MutableStateFlow(false)
    val isFetchingQuote: StateFlow<Boolean> = _isFetchingQuote.asStateFlow()
    
    // Track the last quote fetched
    private val _lastQuote = MutableStateFlow<com.core.data.swap.ZeroXSwapQuoteResponse?>(null)
    val lastQuote: StateFlow<com.core.data.swap.ZeroXSwapQuoteResponse?> = _lastQuote.asStateFlow()
    
    // Track the last fetch parameters to avoid duplicate requests
    private var lastFetchParams: Triple<String?, String?, String>? = null

    // Custom token lookup state (for paste contract address feature)
    private val _customTokenLookupState = MutableStateFlow<CustomTokenLookupState>(CustomTokenLookupState.Idle)
    val customTokenLookupState: StateFlow<CustomTokenLookupState> = _customTokenLookupState.asStateFlow()

    /**
     * Look up a token by contract address when user pastes an address in the search field.
     * This enables swapping into any token by just pasting its contract address.
     */
    fun lookupCustomToken(contractAddress: String, chainId: Int) {
        // Validate the address format
        if (!lookupTokenByAddress.isValidContractAddress(contractAddress)) {
            _customTokenLookupState.value = CustomTokenLookupState.Idle
            return
        }

        viewModelScope.launch {
            Log.d("SwapViewModel", "Looking up custom token: $contractAddress on chain $chainId")
            _customTokenLookupState.value = CustomTokenLookupState.Loading

            try {
                val token = lookupTokenByAddress(contractAddress, chainId)

                if (token != null) {
                    Log.d("SwapViewModel", "Custom token found: ${token.symbol} (${token.name})")
                    val tokenWithPrice = TokenAssetWithPrice(
                        address = token.address,
                        chainId = token.chainId,
                        symbol = token.symbol,
                        name = token.name,
                        balance = token.balance,
                        decimals = token.decimals,
                        logoUrl = token.logoUrl,
                        swappable = true,
                        fiatAmount = 0.0
                    )
                    _customTokenLookupState.value = CustomTokenLookupState.Found(tokenWithPrice)
                } else {
                    Log.w("SwapViewModel", "Custom token not found: $contractAddress")
                    _customTokenLookupState.value = CustomTokenLookupState.NotFound(contractAddress)
                }
            } catch (e: Exception) {
                Log.e("SwapViewModel", "Error looking up custom token", e)
                _customTokenLookupState.value = CustomTokenLookupState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Look up a token on all supported chains when the chain is unknown.
     */
    fun lookupCustomTokenOnAllChains(contractAddress: String) {
        if (!lookupTokenByAddress.isValidContractAddress(contractAddress)) {
            _customTokenLookupState.value = CustomTokenLookupState.Idle
            return
        }

        viewModelScope.launch {
            Log.d("SwapViewModel", "Looking up custom token on all chains: $contractAddress")
            _customTokenLookupState.value = CustomTokenLookupState.Loading

            try {
                val result = lookupTokenByAddress.lookupOnAllChains(contractAddress)

                if (result != null) {
                    val (token, chainId) = result
                    Log.d("SwapViewModel", "Custom token found: ${token.symbol} on chain $chainId")
                    val tokenWithPrice = TokenAssetWithPrice(
                        address = token.address,
                        chainId = token.chainId,
                        symbol = token.symbol,
                        name = token.name,
                        balance = token.balance,
                        decimals = token.decimals,
                        logoUrl = token.logoUrl,
                        swappable = true,
                        fiatAmount = 0.0
                    )
                    _customTokenLookupState.value = CustomTokenLookupState.Found(tokenWithPrice)
                } else {
                    Log.w("SwapViewModel", "Custom token not found on any chain: $contractAddress")
                    _customTokenLookupState.value = CustomTokenLookupState.NotFound(contractAddress)
                }
            } catch (e: Exception) {
                Log.e("SwapViewModel", "Error looking up custom token on all chains", e)
                _customTokenLookupState.value = CustomTokenLookupState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Clear the custom token lookup state.
     */
    fun clearCustomTokenLookup() {
        _customTokenLookupState.value = CustomTokenLookupState.Idle
    }

    /**
     * Check if a string looks like a contract address.
     */
    fun isContractAddress(query: String): Boolean {
        return lookupTokenByAddress.isValidContractAddress(query)
    }

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
        if (chainId !in supportedSwapChainIds) {
            Log.w("SwapViewModel", "Selected unsupported chain for swaps: $chainId")
            _toastMessage.value = swapsUnsupportedMessage(chainId)
            return
        }
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
        
        // Initialize quote fetching with debouncing
        setupQuoteFetching()

        // Observe transaction status and reflect on terminal LEDs
        viewModelScope.launch {
            swapTransactionStatus.collect { status ->
                when (status) {
                    SwapTransactionStatus.SUCCESS -> {
                        showSuccessMatrix()
                    }
                    is SwapTransactionStatus.FAILURE -> {
                        showFailedMatrix()
                    }
                    else -> { /* no-op */ }
                }
            }
        }
    }
    
    private fun observeAndSetDefaultToken() {
        viewModelScope.launch {
            // Wait until grouped tokens (already sorted by total fiat descending) are available
            groupedTokenAssetState
                .filter { it is GroupedAssetsUiState.Success }
                .first()
                .let { groupedState ->
                    if (_swapUIState.value.fromToken == null) {
                        val success = groupedState as GroupedAssetsUiState.Success
                        val topAsset = success.assets.firstOrNull()
                        if (topAsset != null) {
                            Log.d("SwapViewModel", "Selecting default FROM token from highest-value asset group: ${topAsset.symbol}")
                            // Use group conversion to pick the chain variant with the highest fiat amount
                            selectTokenFromCarousel(topAsset.groupId, setAsDefault = true)
                        } else {
                            Log.w("SwapViewModel", "Grouped assets empty; cannot auto-select default FROM token")
                        }
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
                    handleMaxClickFrom()
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
                fromCurrentFiatAmount = calculateFiatAmount(amount, currentState.fromToken),
                fromUseMaxAmount = false // Clear MAX flag when user types manually
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
    
    /**
     * Handle max button click for FROM token
     * Sets the amount to the maximum available balance
     * 
     * IMPORTANT: Displays the rounded amount in the textfield for UX,
     * but uses the exact full-precision amount for API calls to avoid balance issues
     */
    private fun handleMaxClickFrom() {
        val currentState = _swapUIState.value
        val fromToken = currentState.fromToken
        
        if (fromToken == null) {
            Log.w("SwapViewModel", "handleMaxClickFrom: No FROM token selected")
            return
        }
        
        Log.d("SwapViewModel", "=== MAX BUTTON CLICKED ===")
        Log.d("SwapViewModel", "FROM token: ${fromToken.token.symbol}")
        Log.d("SwapViewModel", "Actual balance (full precision): ${fromToken.token.balance}")
        Log.d("SwapViewModel", "Max amount (exact): ${fromToken.formattedMaxAmount}")
        Log.d("SwapViewModel", "Current TO token: ${currentState.toToken?.token?.symbol ?: "null"}")
        
        // Set the FULL PRECISION amount in the textfield
        // Set fromUseMaxAmount=true so we apply safety multiplier (99.99%) in API calls
        _swapUIState.update { state ->
            state.copy(
                fromCurrentAmount = fromToken.formattedMaxAmount, // Full precision in textfield
                fromUseMaxAmount = true, // Flag to apply 99.99% multiplier in API calls
                fromCurrentFiatAmount = fromToken.formattedMaxFiatAmount
            )
        }
        
        Log.d("SwapViewModel", "State updated:")
        Log.d("SwapViewModel", "  Display amount: ${_swapUIState.value.fromCurrentAmount}")
        Log.d("SwapViewModel", "  Use max flag: ${_swapUIState.value.fromUseMaxAmount}")
        Log.d("SwapViewModel", "  (API will use 99.99% of this for safety)")
    }
    
    /**
     * Setup automatic quote fetching when FROM amount changes
     * Uses 500ms debouncing to avoid excessive API calls (matching TokenLauncher implementation)
     * 
     * IMPORTANT: Only observes FROM token, TO token, and FROM amount changes.
     * Does NOT trigger when TO amount changes (to avoid infinite loops when we update TO amount)
     */
    private fun setupQuoteFetching() {
        viewModelScope.launch {
            swapUIState
                .map { state ->
                    // Only track FROM token, TO token, and FROM amount
                    // Ignore TO amount to prevent triggering when we update it
                    Log.d("SwapViewModel", "setupQuoteFetching: State observed")
                    Log.d("SwapViewModel", "  FROM token: ${state.fromToken?.token?.symbol} (address: ${state.fromToken?.token?.address})")
                    Log.d("SwapViewModel", "  TO token: ${state.toToken?.token?.symbol} (address: ${state.toToken?.token?.address})")
                    Log.d("SwapViewModel", "  FROM amount: '${state.fromCurrentAmount}'")
                    Triple(
                        state.fromToken?.token?.address to state.fromToken?.token?.symbol,
                        state.toToken?.token?.address to state.toToken?.token?.symbol,
                        state.fromCurrentAmount
                    )
                }
                .distinctUntilChanged()
                .collect { (fromTokenPair, toTokenPair, fromAmount) ->
                    val fromToken = _swapUIState.value.fromToken
                    val toToken = _swapUIState.value.toToken
                    
                    Log.d("SwapViewModel", "setupQuoteFetching: Collect triggered")
                    Log.d("SwapViewModel", "  FROM amount='$fromAmount'")
                    
                    // Reset TO amount and quote if inputs are invalid
                    if (fromAmount.isNullOrBlank() || 
                        fromAmount.toBigDecimalOrNull() == null ||
                        fromAmount.toBigDecimalOrNull() == BigDecimal.ZERO ||
                        fromToken == null || 
                        toToken == null) {
                        
                        Log.d("SwapViewModel", "setupQuoteFetching: Invalid inputs, clearing TO amount and quote")
                        _isFetchingQuote.value = false
                        _lastQuote.value = null
                        lastFetchParams = null
                        _swapUIState.update { currentState ->
                            currentState.copy(
                                toCurrentAmount = "",
                                toCurrentFiatAmount = ""
                            )
                        }
                        return@collect
                    }
                    
                    // If MAX button was clicked, use the exact full-precision amount
                    // Otherwise use the amount from the textfield
                    val currentState = _swapUIState.value
                    val useMaxAmount = currentState.fromUseMaxAmount
                    val exactAmount = if (useMaxAmount && fromToken != null) {
                        fromToken.formattedMaxAmount // Exact full precision
                    } else {
                        fromAmount // User-entered amount
                    }
                    
                    // Check if this is a duplicate request (same parameters as last fetch)
                    val currentParams = Triple(fromToken.token.address, toToken.token.address, exactAmount)
                    if (currentParams == lastFetchParams) {
                        Log.d("SwapViewModel", "setupQuoteFetching: ⏭️ Skipping duplicate request (same parameters)")
                        return@collect
                    }
                    
                    Log.d("SwapViewModel", "setupQuoteFetching: Valid inputs, starting quote fetch")
                    _isFetchingQuote.value = true
                    
                    // Debounce for 500ms to avoid too many API calls
                    delay(500)
                    
                    try {
                        var amount = exactAmount.toBigDecimalOrNull() ?: return@collect
                        
                        // If using MAX, use 99.99% of the balance to account for Double precision loss
                        // This ensures we never try to sell more than we actually have
                        if (useMaxAmount) {
                            // Use 99.99% of max to handle precision issues (standard DeFi practice)
                            val maxMultiplier = BigDecimal("0.9999")
                            amount = amount.multiply(maxMultiplier)
                            Log.d("SwapViewModel", "Applied MAX safety multiplier: 99.99%")
                            Log.d("SwapViewModel", "Original: $exactAmount")
                            Log.d("SwapViewModel", "Adjusted: $amount")
                        }
                        
                        // Detect if this is a cross-chain swap
                        val isCrossChain = fromToken.token.chainId != toToken.token.chainId
                        
                        Log.d("SwapViewModel", "=== Fetching Quote ===")
                        Log.d("SwapViewModel", "FROM: ${fromToken.token.symbol} (${fromToken.token.address}) on chain ${fromToken.token.chainId}")
                        Log.d("SwapViewModel", "TO: ${toToken.token.symbol} (${toToken.token.address}) on chain ${toToken.token.chainId}")
                        Log.d("SwapViewModel", "Display amount: $fromAmount")
                        Log.d("SwapViewModel", "Use MAX amount: $useMaxAmount")
                        Log.d("SwapViewModel", "Exact amount for API: $amount")
                        Log.d("SwapViewModel", "Is Cross-Chain: $isCrossChain")
                        
                        if (isCrossChain) {
                            // Cross-chain swap - use Socket API
                            Log.d("SwapViewModel", "🌉 Using Socket API for cross-chain quote")
                            Log.d("SwapViewModel", "📞 Calling swapRepository.getCrossChainQuote()")
                            
                            val crossChainQuote = try {
                                swapRepository.getCrossChainQuote(
                                    fromChainId = fromToken.token.chainId,
                                    toChainId = toToken.token.chainId,
                                    fromTokenAddress = fromToken.token.address,
                                    toTokenAddress = toToken.token.address,
                                    amount = amount,
                                    fromTokenDecimals = fromToken.token.decimals,
                                    toTokenDecimals = toToken.token.decimals,
                                    fromTokenSymbol = fromToken.token.symbol,
                                    toTokenSymbol = toToken.token.symbol
                                )
                            } catch (e: Exception) {
                                Log.e("SwapViewModel", "💥 Exception calling getCrossChainQuote()", e)
                                null
                            }
                            
                            if (crossChainQuote != null && crossChainQuote.result?.routes?.isNotEmpty() == true) {
                                val bestRoute = crossChainQuote.result!!.routes.first()
                                Log.d("SwapViewModel", "✅ Cross-chain quote received")
                                Log.d("SwapViewModel", "  Output amount (smallest unit): ${bestRoute.toAmount}")
                                Log.d("SwapViewModel", "  Bridges: ${bestRoute.usedBridgeNames.joinToString(", ")}")
                                Log.d("SwapViewModel", "  Service time: ${bestRoute.serviceTime}s")
                                
                                lastFetchParams = currentParams
                                _lastQuote.value = null // Clear 0x quote for cross-chain
                                
                                // Convert output amount
                                val toDecimals = toToken.token.decimals
                                val buyAmountBigInt = bestRoute.toAmount.toBigIntegerOrNull() ?: BigInteger.ZERO
                                val buyAmountDecimal = BigDecimal(buyAmountBigInt)
                                    .divide(BigDecimal.TEN.pow(toDecimals), toDecimals, RoundingMode.DOWN)
                                val formattedToAmount = buyAmountDecimal.stripTrailingZeros().toPlainString()
                                
                                Log.d("SwapViewModel", "  Formatted TO amount: $formattedToAmount ${toToken.token.symbol}")
                                
                                _swapUIState.update { currentState ->
                                    currentState.copy(
                                        toCurrentAmount = formattedToAmount,
                                        toCurrentFiatAmount = ""
                                    )
                                }
                            } else {
                                Log.w("SwapViewModel", "⚠️ Cross-chain quote returned null or no routes")
                                _lastQuote.value = null
                                _swapUIState.update { currentState ->
                                    currentState.copy(
                                        toCurrentAmount = "",
                                        toCurrentFiatAmount = ""
                                    )
                                }
                            }
                        } else {
                            // Same-chain swap - use 0x API
                            Log.d("SwapViewModel", "📞 Calling swapRepository.getSwapQuote() (0x API)")
                            
                            val quote = try {
                                swapRepository.getSwapQuote(
                                    inputTokenAddress = fromToken.token.address,
                                    outputTokenAddress = toToken.token.address,
                                    amount = amount,
                                    inputTokenDecimals = fromToken.token.decimals,
                                    outputTokenDecimals = toToken.token.decimals,
                                    chainId = fromToken.token.chainId,
                                    inputTokenSymbol = fromToken.token.symbol,
                                    outputTokenSymbol = toToken.token.symbol
                                )
                            } catch (quoteException: Exception) {
                                Log.e("SwapViewModel", "💥 Exception calling swapRepository.getSwapQuote()", quoteException)
                                quoteException.printStackTrace()
                                null
                            }
                            
                            Log.d("SwapViewModel", "📬 Quote response received: ${if (quote != null) "SUCCESS" else "NULL"}")
                            
                            if (quote != null) {
                                Log.d("SwapViewModel", "✅ Quote received successfully")
                                Log.d("SwapViewModel", "  Buy amount (smallest unit): ${quote.buyAmount}")
                                Log.d("SwapViewModel", "  Price: ${quote.price ?: "N/A"}")
                                
                                lastFetchParams = currentParams
                                _lastQuote.value = quote
                                
                                val toDecimals = toToken.token.decimals
                                val buyAmountBigInt = quote.buyAmount.toBigIntegerOrNull() ?: BigInteger.ZERO
                                val buyAmountDecimal = BigDecimal(buyAmountBigInt)
                                    .divide(BigDecimal.TEN.pow(toDecimals), toDecimals, RoundingMode.DOWN)
                                val formattedToAmount = buyAmountDecimal.stripTrailingZeros().toPlainString()
                                
                                Log.d("SwapViewModel", "  Formatted TO amount: $formattedToAmount ${toToken.token.symbol}")
                                
                                _swapUIState.update { currentState ->
                                    currentState.copy(
                                        toCurrentAmount = formattedToAmount,
                                        toCurrentFiatAmount = ""
                                    )
                                }
                            } else {
                                Log.w("SwapViewModel", "⚠️ Quote returned null")
                                _lastQuote.value = null
                                _swapUIState.update { currentState ->
                                    currentState.copy(
                                        toCurrentAmount = "",
                                        toCurrentFiatAmount = ""
                                    )
                                }
                            }
                        }
                        
                    } catch (e: Exception) {
                        Log.e("SwapViewModel", "❌ Exception while fetching quote", e)
                        _lastQuote.value = null
                        _swapUIState.update { currentState ->
                            currentState.copy(
                                toCurrentAmount = "",
                                toCurrentFiatAmount = ""
                            )
                        }
                    } finally {
                        _isFetchingQuote.value = false
                    }
                }
        }
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
            
            if (swapToken.token.chainId !in supportedSwapChainIds) {
                Log.w("SwapViewModel", "Selected token on unsupported chain ${swapToken.token.chainId}")
                _toastMessage.value = swapsUnsupportedMessage(swapToken.token.chainId)
                if (!setAsDefault) {
                    hideTokenOverlay()
                }
                return@launch
            }
            
            when (targetMode) {
                TokenSelectionMode.From -> {
                    Log.d("SwapViewModel", "Updating FROM token to ${swapToken.token.symbol}")
                    lastFetchParams = null // Clear cached params when token changes
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
                    lastFetchParams = null // Clear cached params when token changes
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

    fun selectToTokenAsset(tokenAsset: TokenAssetWithPrice) {
        viewModelScope.launch {
            Log.d("SwapViewModel", "selectToTokenAsset: ${tokenAsset.symbol} on chain ${tokenAsset.chainId}")
            
            // Cross-chain swaps are now supported! Only check if chain is in supported list
            if (tokenAsset.chainId !in supportedSwapChainIds) {
                Log.w("SwapViewModel", "Attempted to select TO token on unsupported chain ${tokenAsset.chainId}")
                _toastMessage.value = swapsUnsupportedMessage(tokenAsset.chainId)
                hideTokenOverlay()
                return@launch
            }
            
            lastFetchParams = null // Clear cached params when token changes
            val currentFromToken = _swapUIState.value.fromToken
            
            // Log cross-chain detection (now allowed!)
            if (currentFromToken != null && currentFromToken.token.chainId != tokenAsset.chainId) {
                Log.d("SwapViewModel", "🌉 Cross-chain swap selected!")
                Log.d("SwapViewModel", "  FROM: ${currentFromToken.token.symbol} on chain ${currentFromToken.token.chainId}")
                Log.d("SwapViewModel", "  TO: ${tokenAsset.symbol} on chain ${tokenAsset.chainId}")
            }
            
            // All valid chain combinations are now allowed - proceed with selection
            // Format the balance for display
            val formattedBalance = String.format("%.6f", tokenAsset.balance).trimEnd('0').trimEnd('.')
            
            // For MAX amount, use the full precision balance to avoid rounding errors
            val maxAmountFullPrecision = tokenAsset.balance.toString()
            
            val swapToken = SwapToken(
                token = tokenAsset.toTokenAsset(),
                balance = formattedBalance,
                fiatBalance = "", // TODO: Add price calculation
                formattedMaxAmount = maxAmountFullPrecision, // Use full precision for MAX
                formattedMaxFiatAmount = "" // TODO: Add fiat calculation
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
            
            Log.d("SwapViewModel", "findTokenBySymbolAndChain - Looking for: symbol=$symbol, chainId=$chainId, requireBalance=$requireBalance")
            Log.d("SwapViewModel", "findTokenBySymbolAndChain - Total tokens available: ${allTokens.size}")
            
            // Find all matching tokens
            val matches = allTokens.filter { 
                it.symbol.uppercase() == symbol.uppercase() && 
                it.chainId == chainId
            }
            
            Log.d("SwapViewModel", "findTokenBySymbolAndChain - Found ${matches.size} matches for $symbol on chain $chainId")
            matches.forEach { token ->
                Log.d("SwapViewModel", "  - Match: address=${token.address}, balance=${token.balance}, chainId=${token.chainId}")
            }
            
            // Return the token with the highest balance (prefer owned tokens)
            val result = if (requireBalance) {
                matches.filter { it.balance > 0.0 }.maxByOrNull { it.balance }
            } else {
                // Even if not requiring balance, prefer tokens with balance
                matches.maxByOrNull { it.balance } ?: matches.firstOrNull()
            }
            
            Log.d("SwapViewModel", "findTokenBySymbolAndChain - Selected token: address=${result?.address}, balance=${result?.balance}")
            result
        } catch (e: Exception) {
            Log.e("SwapViewModel", "Error finding token by symbol and chain", e)
            null
        }
    }
    
    fun selectFromTokenAsset(tokenAsset: TokenAssetWithPrice) {
        viewModelScope.launch {
            Log.d("SwapViewModel", "selectFromTokenAsset: ${tokenAsset.symbol} on chain ${tokenAsset.chainId}")
            
            if (tokenAsset.chainId !in supportedSwapChainIds) {
                Log.w("SwapViewModel", "Attempted to select FROM token on unsupported chain ${tokenAsset.chainId}")
                _toastMessage.value = swapsUnsupportedMessage(tokenAsset.chainId)
                hideTokenOverlay()
                return@launch
            }
            
            lastFetchParams = null // Clear cached params when token changes
            
            // Format the balance for display (showing 6 decimals max)
            val formattedBalance = String.format("%.6f", tokenAsset.balance).trimEnd('0').trimEnd('.')
            
            // For MAX amount, use the full precision balance to avoid rounding errors
            // This ensures we don't try to sell more than we actually have
            val maxAmountFullPrecision = tokenAsset.balance.toString()
            
            Log.d("SwapViewModel", "  Balance: ${tokenAsset.balance}")
            Log.d("SwapViewModel", "  Formatted for display: $formattedBalance")
            Log.d("SwapViewModel", "  Max amount (full precision): $maxAmountFullPrecision")
            
            val swapToken = SwapToken(
                token = tokenAsset.toTokenAsset(),
                balance = formattedBalance,
                fiatBalance = "", // TODO: Add price calculation
                formattedMaxAmount = maxAmountFullPrecision, // Use full precision for MAX
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
            
            val supportedTokens = tokensInGroup.filter { supportedSwapChainIds.contains(it.chainId) }
            
            // Get the token with the highest fiat balance (most valuable chain for this token)
            val primaryToken = (if (supportedTokens.isNotEmpty()) supportedTokens else tokensInGroup).maxByOrNull { it.fiatAmount }
            
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
            
            // Use full precision for max amount to avoid rounding errors
            val maxAmountFullPrecision = tokenAsset.balance.toString()
            
            SwapToken(
                token = tokenAsset,
                balance = asset.formattedBalance,
                fiatBalance = asset.formattedFiatBalance ?: "0.00",
                formattedMaxAmount = maxAmountFullPrecision, // Use full precision for MAX
                formattedMaxFiatAmount = asset.formattedFiatBalance ?: "0.00"
            )
        } catch (e: Exception) {
            Log.e("SwapViewModel", "convertToSwapToken: Error converting token", e)
            // Return fallback on error
            val fallbackMaxAmount = asset.totalBalance.toString()
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
                formattedMaxAmount = fallbackMaxAmount, // Use full precision for MAX
                formattedMaxFiatAmount = asset.formattedFiatBalance ?: "0.00"
            )
        }
    }

    private fun chainDisplayName(chainId: Int): String = when (chainId) {
        1 -> "Ethereum"
        10 -> "Optimism"
        137 -> "Polygon"
        42161 -> "Arbitrum"
        8453 -> "Base"
        7777777 -> "Zora"
        else -> "chain $chainId"
    }

    private fun swapsUnsupportedMessage(chainId: Int): String =
        "Swaps on ${chainDisplayName(chainId)} are not supported yet."

    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, "")
    
    // Track if we want to show all chains in the token selector (for cross-chain swaps)
    private val _showAllChainsInSelector = MutableStateFlow(true) // Default to all chains for cross-chain support
    val showAllChainsInSelector: StateFlow<Boolean> = _showAllChainsInSelector.asStateFlow()
    
    fun setShowAllChainsInSelector(showAll: Boolean) {
        _showAllChainsInSelector.value = showAll
    }

    // Enhanced token list that includes both owned and available swappable tokens
    // Now supports cross-chain by loading tokens from all chains when allChains is true
    val swapTokenUiState: StateFlow<SwapTokenUiState> = combine(
        swapUIState,
        searchQuery,
        selectedTokenChainId,
        showAllChainsInSelector
    ) { uiState, query, chainId, allChains ->
        // When selecting TO token and allChains is true, don't filter by chain
        val effectiveChainId = if (allChains) null else chainId
        Triple(uiState.fromToken?.token, query, effectiveChainId) to allChains
    }.flatMapLatest { (triple, allChains) ->
        val (fromToken, query, chainId) = triple
        getSwappableTokensForSelection(
            excludeToken = fromToken,
            targetChainId = chainId,
            query = query,
            allChains = allChains
        ).asResult().map { result ->

            when(result) {
                is Result.Error -> SwapTokenUiState.Error
                is Result.Loading -> SwapTokenUiState.Loading
                is Result.Success -> {

                    val newTokens = result.data.map { it.toTokenAssetWithPrice(0.0) }


                    SwapTokenUiState.Success(newTokens)
                }
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
     * Clear the transaction status, e.g., when the overlay is dismissed or when navigating away
     */
    fun clearSwapTransactionStatus() {
        _swapTransactionStatus.value = null
    }

    /**
     * Reset the swap screen to its initial state.
     * Called after a swap completes (success or failure) to allow the user to perform another swap.
     */
    fun resetSwapScreen() {
        Log.d("SwapViewModel", "Resetting swap screen to initial state")

        // Clear the transaction status
        _swapTransactionStatus.value = null

        // Clear the quote
        _lastQuote.value = null
        lastFetchParams = null
        _isFetchingQuote.value = false

        // Reset the UI state - clear tokens and amounts but keep the callbacks
        _swapUIState.update { currentState ->
            currentState.copy(
                fromToken = null,
                fromCurrentAmount = "",
                fromCurrentFiatAmount = "",
                fromUseMaxAmount = false,
                toToken = null,
                toCurrentAmount = "",
                toCurrentFiatAmount = "",
                toUseMaxAmount = false
            )
        }

        // Re-trigger default token selection
        observeAndSetDefaultToken()

        // Re-initialize the terminal display (SWAP text and click callback)
        viewModelScope.launch {
            try {
                Log.d("SwapViewModel", "Re-initializing terminal display after reset")
                terminalRepository.generateSwap()
                reflectiveLedPattern?.displaySwap()
            } catch (e: Exception) {
                Log.e("SwapViewModel", "Error re-initializing terminal display", e)
            }
        }

        Log.d("SwapViewModel", "Swap screen reset complete")
    }

    private fun showFailedMatrix() {
        viewModelScope.launch {
            reflectiveLedPattern?.displayError()
            delay(2000)
            reflectiveLedPattern?.clear()
            reflectiveLedPattern?.displaySwap()//.displayChad(TerminalLEDController.getColorHex())
        }
    }

    private fun showSuccessMatrix() {
        viewModelScope.launch {
            reflectiveLedPattern?.displaySuccess()
            delay(2000)
            reflectiveLedPattern?.clear()
            reflectiveLedPattern?.displaySwap()//.displayChad(TerminalLEDController.getColorHex())
        }
    }

    /**
     * Unified swap entry point - called by both debug swap button and terminal swap button
     * 
     * IMPORTANT: The 0x API only supports same-chain swaps. Cross-chain swaps/bridging
     * requires integration with a dedicated bridge protocol (e.g., Across Protocol, LayerZero).
     */
    fun swap(callback: (String) -> Unit) {
        viewModelScope.launch {
            Log.d("SwapViewModel", "")
            Log.d("SwapViewModel", "═══════════════════════════════════════")
            Log.d("SwapViewModel", "     SWAP INITIATED")
            Log.d("SwapViewModel", "═══════════════════════════════════════")
            
            // Prefer the new unified UI state when available
            val uiState = swapUIState.value
            val fromToken = uiState.fromToken
            val toToken = uiState.toToken
            val fromAmountFromUi = uiState.fromCurrentAmount

            // Legacy state fallback
            val (fromAsset, toAsset) = swapAssetsUiState.value
            val fromAmountLegacy = amountsUiState.value.fromAmount
            
            Log.d("SwapViewModel", "📊 Current State:")
            Log.d("SwapViewModel", "  UI State - FROM: ${fromToken?.token?.symbol}, TO: ${toToken?.token?.symbol}, Amount: $fromAmountFromUi")
            Log.d("SwapViewModel", "  Legacy State - FROM: ${(fromAsset as? SelectedTokenUiState.Selected)?.tokenAsset?.symbol}, TO: ${(toAsset as? SelectedTokenUiState.Selected)?.tokenAsset?.symbol}, Amount: $fromAmountLegacy")
            
            try {
                // Check if we have valid tokens from the new UI state
                val hasUiTokens = fromToken != null && toToken != null && !fromAmountFromUi.isNullOrBlank()
                
                Log.d("SwapViewModel", "🔍 Has UI tokens: $hasUiTokens")
                
                if (hasUiTokens) {
                    // Check if this is a cross-chain swap
                    val isCrossChain = fromToken!!.token.chainId != toToken!!.token.chainId
                    
                    Log.d("SwapViewModel", "🌐 Cross-chain check:")
                    Log.d("SwapViewModel", "  FROM chain: ${fromToken.token.chainId}")
                    Log.d("SwapViewModel", "  TO chain: ${toToken.token.chainId}")
                    Log.d("SwapViewModel", "  Is cross-chain: $isCrossChain")
                    
                    if (isCrossChain) {
                        // Cross-chain swap via Socket API
                        Log.d("SwapViewModel", "")
                        Log.d("SwapViewModel", "🌉 CROSS-CHAIN SWAP (via Socket API)")
                        Log.d("SwapViewModel", "  FROM: ${fromToken.token.symbol} on chain ${fromToken.token.chainId}")
                        Log.d("SwapViewModel", "  TO: ${toToken.token.symbol} on chain ${toToken.token.chainId}")
                        Log.d("SwapViewModel", "  Amount: $fromAmountFromUi")
                        
                        // Set status to PENDING
                        Log.d("SwapViewModel", "⏳ Setting status to PENDING...")
                        _swapTransactionStatus.value = SwapTransactionStatus.PENDING
                        
                        // Calculate exact amount
                        val exactSwapAmount = if (uiState.fromUseMaxAmount) {
                            val fullAmount = fromToken.formattedMaxAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            val maxMultiplier = BigDecimal("0.9999")
                            fullAmount.multiply(maxMultiplier)
                        } else {
                            fromAmountFromUi.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        }
                        
                        Log.d("SwapViewModel", "🚀 Executing cross-chain swap...")
                        Log.d("SwapViewModel", "  Exact amount: $exactSwapAmount")
                        
                        executeCrossChainSwap(
                            fromChainId = fromToken.token.chainId,
                            toChainId = toToken.token.chainId,
                            fromAddress = fromToken.token.address,
                            toAddress = toToken.token.address,
                            fromDecimals = fromToken.token.decimals,
                            toDecimals = toToken.token.decimals,
                            fromSymbol = fromToken.token.symbol,
                            toSymbol = toToken.token.symbol,
                            amount = exactSwapAmount,
                            callback = callback
                        )
                        return@launch
                    } else {
                        // Same chain - execute normal swap via 0x
                        Log.d("SwapViewModel", "")
                        Log.d("SwapViewModel", "✅ SAME-CHAIN SWAP (via 0x API)")
                        Log.d("SwapViewModel", "  FROM Token:")
                        Log.d("SwapViewModel", "    Symbol: ${fromToken.token.symbol}")
                        Log.d("SwapViewModel", "    Address: ${fromToken.token.address}")
                        Log.d("SwapViewModel", "    Decimals: ${fromToken.token.decimals}")
                        Log.d("SwapViewModel", "  TO Token:")
                        Log.d("SwapViewModel", "    Symbol: ${toToken.token.symbol}")
                        Log.d("SwapViewModel", "    Address: ${toToken.token.address}")
                        Log.d("SwapViewModel", "    Decimals: ${toToken.token.decimals}")
                        Log.d("SwapViewModel", "  Chain ID: ${fromToken.token.chainId}")
                        Log.d("SwapViewModel", "  Amount: $fromAmountFromUi")
                        
                        // Set status to PENDING before executing swap
                        Log.d("SwapViewModel", "⏳ Setting status to PENDING...")
                        _swapTransactionStatus.value = SwapTransactionStatus.PENDING
                        
                        // If MAX was clicked, use 99.99% to avoid Double precision issues
                        val exactSwapAmount = if (uiState.fromUseMaxAmount) {
                            // Use 99.99% of max to handle precision issues (standard DeFi practice)
                            val fullAmount = fromToken.formattedMaxAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            val maxMultiplier = BigDecimal("0.9999")
                            val adjustedAmount = fullAmount.multiply(maxMultiplier)
                            Log.d("SwapViewModel", "Applied MAX safety multiplier for swap: 99.99%")
                            Log.d("SwapViewModel", "  Original: ${fromToken.formattedMaxAmount}")
                            Log.d("SwapViewModel", "  Adjusted: $adjustedAmount")
                            adjustedAmount.toPlainString()
                        } else {
                            fromAmountFromUi
                        }
                        
                        Log.d("SwapViewModel", "🚀 Executing swap...")
                        Log.d("SwapViewModel", "  Display amount: $fromAmountFromUi")
                        Log.d("SwapViewModel", "  Use MAX: ${uiState.fromUseMaxAmount}")
                        Log.d("SwapViewModel", "  Exact amount for swap: $exactSwapAmount")
                        
                        executeSwap(
                            fromAddress = fromToken.token.address,
                            toAddress = toToken.token.address,
                            amount = exactSwapAmount,
                            callback = callback
                        )
                    }
                } else if (fromAsset is SelectedTokenUiState.Selected && toAsset is SelectedTokenUiState.Selected) {
                    // Legacy state handling
                    Log.d("SwapViewModel", "")
                    Log.d("SwapViewModel", "⚠️ Using LEGACY swap state")
                    Log.d("SwapViewModel", "  FROM: ${fromAsset.tokenAsset.symbol} (${fromAsset.tokenAsset.address})")
                    Log.d("SwapViewModel", "  TO: ${toAsset.tokenAsset.symbol} (${toAsset.tokenAsset.address})")
                    Log.d("SwapViewModel", "  Amount: $fromAmountLegacy")
                    
                    // Check if cross-chain in legacy state too
                    if (fromAsset.tokenAsset.chainId != toAsset.tokenAsset.chainId) {
                        Log.e("SwapViewModel", "❌ Cross-chain swap attempted in legacy state (not supported)")
                        val errorMsg = "Cross-chain swaps are not supported"
                        _swapTransactionStatus.value = SwapTransactionStatus.FAILURE(errorMsg)
                        callback("Error: $errorMsg.")
                        return@launch
                    }
                    
                    // Set status to PENDING before executing swap
                    Log.d("SwapViewModel", "⏳ Setting status to PENDING...")
                    _swapTransactionStatus.value = SwapTransactionStatus.PENDING
                    
                    Log.d("SwapViewModel", "🚀 Executing swap (legacy path)...")
                    executeSwap(
                        fromAddress = fromAsset.tokenAsset.address,
                        toAddress = toAsset.tokenAsset.address,
                        amount = fromAmountLegacy,
                        callback = callback
                    )
                } else {
                    Log.e("SwapViewModel", "")
                    Log.e("SwapViewModel", "❌ NO VALID TOKENS SELECTED")
                    Log.e("SwapViewModel", "  FROM token: ${fromToken?.token?.symbol ?: "null"}")
                    Log.e("SwapViewModel", "  TO token: ${toToken?.token?.symbol ?: "null"}")
                    Log.e("SwapViewModel", "  FROM amount: $fromAmountFromUi")
                    
                    val errorMsg = "Please select both FROM and TO tokens"
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE(errorMsg)
                    callback("Error: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("SwapViewModel", "")
                Log.e("SwapViewModel", "💥 EXCEPTION IN SWAP FUNCTION", e)
                Log.e("SwapViewModel", "  Type: ${e::class.simpleName}")
                Log.e("SwapViewModel", "  Message: ${e.message}")
                e.printStackTrace()
                
                _swapTransactionStatus.value = SwapTransactionStatus.FAILURE(e.message ?: "Unknown error")
                callback("Error: ${e.message}")
            }
            
            Log.d("SwapViewModel", "═══════════════════════════════════════")
            Log.d("SwapViewModel", "")
        }
    }
    
    /**
     * Execute a same-chain swap via 0x API
     * This uses the SwapRepository which delegates to SwapHandler for 0x integration
     */
    private suspend fun executeSwap(
        fromAddress: String,
        toAddress: String,
        amount: String,
        callback: (String) -> Unit
    ) {
        Log.d("SwapViewModel", "=== executeSwap called ===")
        Log.d("SwapViewModel", "From address: $fromAddress")
        Log.d("SwapViewModel", "To address: $toAddress")
        Log.d("SwapViewModel", "Amount string: $amount")
        
        val amt = amount.replace(",", ".").toDoubleOrNull()
        
        if (amt == null || amt <= 0.0) {
            Log.e("SwapViewModel", "❌ Invalid amount: $amount (parsed: $amt)")
            val errorMsg = "Invalid amount: $amount"
            _swapTransactionStatus.value = SwapTransactionStatus.FAILURE(errorMsg)
            callback("Error: $errorMsg")
            return
        }
        
        Log.d("SwapViewModel", "✅ Amount validated: $amt")
        
        try {
            Log.d("SwapViewModel", "📞 Calling swapRepository.swap()...")
            
            // Call the swap method which uses SwapHandler internally
            val result = swapRepository.swap(
                inputTokenAddress = fromAddress,
                outputTokenAddress = toAddress,
                amount = amt
            )
            
            Log.d("SwapViewModel", "📬 Swap result received: '$result'")
            
            // Parse the result to determine success/failure
            when {
                result.startsWith("0x") -> {
                    // Transaction hash returned - success!
                    Log.d("SwapViewModel", "🟢 SWAP SUCCESS!")
                    Log.d("SwapViewModel", "  Transaction hash: $result")
                    _swapTransactionStatus.value = SwapTransactionStatus.SUCCESS
                    callback("Success: Transaction hash: $result")
                    // Apply local adjustments and log entries
                    try {
                        applyLocalSwapAdjustments(
                            fromAmountStr = amount,
                            txHash = result
                        )
                    } catch (e: Exception) {
                        Log.e("SwapViewModel", "Failed to apply local swap adjustments", e)
                    }
                }
                result.equals("DECLINE", ignoreCase = true) -> {
                    Log.w("SwapViewModel", "⚠️ USER DECLINED SWAP")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE("User declined transaction")
                    callback("User declined the transaction")
                }
                result.equals("ERROR", ignoreCase = true) -> {
                    Log.e("SwapViewModel", "🔴 SWAP ERROR")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE("Swap failed")
                    callback("Error: Swap failed")
                }
                result.contains("NOT_ENOUGH_GAS", ignoreCase = true) -> {
                    Log.e("SwapViewModel", "🔴 INSUFFICIENT GAS")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE("Insufficient gas for transaction")
                    callback("Error: Insufficient gas")
                }
                result.isEmpty() -> {
                    Log.e("SwapViewModel", "🔴 EMPTY RESULT")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE("No response from swap service")
                    callback("Error: No response from swap service")
                }
                result.lowercase().contains("error") || 
                result.lowercase().contains("failed") -> {
                    Log.e("SwapViewModel", "🔴 SWAP FAILED: $result")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE(result)
                    callback("Error: $result")
                }
                else -> {
                    // Unknown result format - assume success if it's not empty
                    Log.w("SwapViewModel", "⚠️ UNKNOWN RESULT FORMAT: $result")
                    Log.w("SwapViewModel", "  Assuming success since no error keywords detected")
                    _swapTransactionStatus.value = SwapTransactionStatus.SUCCESS
                    callback("Success: $result")
                    try {
                        applyLocalSwapAdjustments(
                            fromAmountStr = amount,
                            txHash = if (result.startsWith("0x")) result else null
                        )
                    } catch (e: Exception) {
                        Log.e("SwapViewModel", "Failed to apply local swap adjustments (unknown result)", e)
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e("SwapViewModel", "🔴 SWAP EXCEPTION", e)
            Log.e("SwapViewModel", "  Exception type: ${e::class.simpleName}")
            Log.e("SwapViewModel", "  Exception message: ${e.message}")
            e.printStackTrace()
            
            val errorMsg = e.message ?: "Unknown error occurred"
            _swapTransactionStatus.value = SwapTransactionStatus.FAILURE(errorMsg)
            callback("Error: $errorMsg")
        }
    }

    /**
     * Execute a cross-chain swap via Socket API
     */
    private suspend fun executeCrossChainSwap(
        fromChainId: Int,
        toChainId: Int,
        fromAddress: String,
        toAddress: String,
        fromDecimals: Int,
        toDecimals: Int,
        fromSymbol: String,
        toSymbol: String,
        amount: BigDecimal,
        callback: (String) -> Unit
    ) {
        Log.d("SwapViewModel", "=== executeCrossChainSwap called ===")
        Log.d("SwapViewModel", "From: $fromSymbol on chain $fromChainId")
        Log.d("SwapViewModel", "To: $toSymbol on chain $toChainId")
        Log.d("SwapViewModel", "Amount: $amount")
        
        if (amount <= BigDecimal.ZERO) {
            Log.e("SwapViewModel", "❌ Invalid amount: $amount")
            val errorMsg = "Invalid amount"
            _swapTransactionStatus.value = SwapTransactionStatus.FAILURE(errorMsg)
            callback("Error: $errorMsg")
            return
        }
        
        try {
            Log.d("SwapViewModel", "📞 Calling swapRepository.crossChainSwap()...")
            
            val result = swapRepository.crossChainSwap(
                fromChainId = fromChainId,
                toChainId = toChainId,
                fromTokenAddress = fromAddress,
                toTokenAddress = toAddress,
                amount = amount,
                fromTokenDecimals = fromDecimals,
                toTokenDecimals = toDecimals,
                fromTokenSymbol = fromSymbol,
                toTokenSymbol = toSymbol
            )
            
            Log.d("SwapViewModel", "📬 Cross-chain swap result: '$result'")
            
            when {
                result.startsWith("0x") -> {
                    Log.d("SwapViewModel", "🟢 CROSS-CHAIN SWAP INITIATED!")
                    Log.d("SwapViewModel", "  Transaction hash: $result")
                    Log.d("SwapViewModel", "  Note: Funds will arrive on destination chain shortly")
                    _swapTransactionStatus.value = SwapTransactionStatus.SUCCESS
                    callback("Success: Cross-chain swap initiated. TX: $result")
                    
                    // Apply local adjustments for the source chain
                    try {
                        applyLocalSwapAdjustments(
                            fromAmountStr = amount.toPlainString(),
                            txHash = result
                        )
                    } catch (e: Exception) {
                        Log.e("SwapViewModel", "Failed to apply local adjustments", e)
                    }
                }
                result.equals("DECLINE", ignoreCase = true) -> {
                    Log.w("SwapViewModel", "⚠️ USER DECLINED CROSS-CHAIN SWAP")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE("User declined transaction")
                    callback("User declined the transaction")
                }
                result.equals("ERROR_NO_ROUTES", ignoreCase = true) -> {
                    Log.e("SwapViewModel", "🔴 NO ROUTES AVAILABLE")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE("No bridge routes available for this pair")
                    callback("Error: No bridge routes available. Try a different token pair or amount.")
                }
                result.equals("ERROR_BUILD_TX", ignoreCase = true) -> {
                    Log.e("SwapViewModel", "🔴 FAILED TO BUILD TRANSACTION")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE("Failed to build bridge transaction")
                    callback("Error: Failed to build bridge transaction")
                }
                result.contains("NOT_ENOUGH_GAS", ignoreCase = true) -> {
                    Log.e("SwapViewModel", "🔴 INSUFFICIENT GAS")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE("Insufficient gas")
                    callback("Error: Insufficient gas for cross-chain transaction")
                }
                result.isEmpty() || result.equals("ERROR", ignoreCase = true) -> {
                    Log.e("SwapViewModel", "🔴 CROSS-CHAIN SWAP FAILED")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE("Cross-chain swap failed")
                    callback("Error: Cross-chain swap failed")
                }
                else -> {
                    Log.w("SwapViewModel", "⚠️ UNKNOWN RESULT: $result")
                    _swapTransactionStatus.value = SwapTransactionStatus.FAILURE(result)
                    callback("Error: $result")
                }
            }
            
        } catch (e: Exception) {
            Log.e("SwapViewModel", "🔴 CROSS-CHAIN SWAP EXCEPTION", e)
            val errorMsg = e.message ?: "Unknown error"
            _swapTransactionStatus.value = SwapTransactionStatus.FAILURE(errorMsg)
            callback("Error: $errorMsg")
        }
    }
    
    private fun applyLocalSwapAdjustments(fromAmountStr: String, txHash: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val state = _swapUIState.value
                val fromToken = state.fromToken?.token
                val toToken = state.toToken?.token
                if (fromToken == null || toToken == null) return@launch

                val fromAmount = fromAmountStr.replace(",", ".").toBigDecimalOrNull()
                // Prefer UI "to" amount; fallback to last quote buyAmount
                val toAmountUi = state.toCurrentAmount.replace(",", ".").toBigDecimalOrNull()
                val toAmount = toAmountUi ?: run {
                    val quote = _lastQuote.value
                    if (quote != null) {
                        val toDecimals = toToken.decimals
                        val buyAmount = quote.buyAmount.toBigIntegerOrNull()
                        if (buyAmount != null) {
                            java.math.BigDecimal(buyAmount).movePointLeft(toDecimals)
                        } else null
                    } else null
                }
                if (fromAmount == null) return@launch

                // Helper to detect network/native tokens: address equals chainId as string
                fun isNetworkToken(address: String, chainId: Int): Boolean = address.equals(chainId.toString(), ignoreCase = true)

                // Convert human amount to stored units for a given token
                fun toStoredUnits(amount: java.math.BigDecimal, address: String, chainId: Int, decimals: Int): java.math.BigDecimal {
                    return if (isNetworkToken(address, chainId)) {
                        // Native token stored in Ether units
                        amount
                    } else {
                        amount.movePointRight(decimals)
                    }
                }

                // Adjust FROM balance (subtract)
                run {
                    val current = tokenBalanceDao.getTokenBalanceEntity(fromToken.address.lowercase(), fromToken.chainId)
                    val delta = toStoredUnits(fromAmount, fromToken.address, fromToken.chainId, fromToken.decimals)
                    val newBal = (current?.tokenBalance ?: java.math.BigDecimal.ZERO).subtract(delta)
                        .let { if (it.signum() < 0) java.math.BigDecimal.ZERO else it }
                    tokenBalanceDao.upsertTokenBalances(listOf(
                        com.core.database.model.erc20.TokenBalanceEntity(
                            contractAddress = fromToken.address.lowercase(),
                            chainId = fromToken.chainId,
                            tokenBalance = newBal
                        )
                    ))
                }

                // Adjust TO balance (add)
                if (toAmount != null) {
                    val current = tokenBalanceDao.getTokenBalanceEntity(toToken.address.lowercase(), toToken.chainId)
                    val delta = toStoredUnits(toAmount, toToken.address, toToken.chainId, toToken.decimals)
                    val newBal = (current?.tokenBalance ?: java.math.BigDecimal.ZERO).add(delta)
                    tokenBalanceDao.upsertTokenBalances(listOf(
                        com.core.database.model.erc20.TokenBalanceEntity(
                            contractAddress = toToken.address.lowercase(),
                            chainId = toToken.chainId,
                            tokenBalance = newBal
                        )
                    ))
                }

                // Insert synthetic transfer entries for Logs: OUT (from token) and IN (to token)
                val walletAddress = try { userDataRepository.userData.first().walletAddress } catch (_: Exception) { "" }
                val now = Clock.System.now()

                // Outgoing entry
                transferDao.insertTransfer(
                    TransferEntity(
                        uniqueId = "local_swap_out_${System.currentTimeMillis()}",
                        asset = fromToken.symbol,
                        chainId = fromToken.chainId,
                        blockNum = "0",
                        category = if (isNetworkToken(fromToken.address, fromToken.chainId)) "external" else "erc20",
                        erc1155Metadata = emptyList<Erc1155MetadataObject>(),
                        erc721TokenId = "",
                        fromaddress = walletAddress,
                        hash = txHash ?: "",
                        rawContract = RawContract(address = fromToken.address, decimal = fromToken.decimals.toString(), value = fromAmount.toPlainString()),
                        toaddress = toToken.address,
                        tokenId = "",
                        value = fromAmount.toDouble(),
                        blockTimestamp = now,
                        userIsSender = true
                    )
                )

                // Incoming entry
                if (toAmount != null) {
                    transferDao.insertTransfer(
                        TransferEntity(
                            uniqueId = "local_swap_in_${System.currentTimeMillis()}",
                            asset = toToken.symbol,
                            chainId = toToken.chainId,
                            blockNum = "0",
                            category = if (isNetworkToken(toToken.address, toToken.chainId)) "external" else "erc20",
                            erc1155Metadata = emptyList<Erc1155MetadataObject>(),
                            erc721TokenId = "",
                            fromaddress = fromToken.address,
                            hash = txHash ?: "",
                            rawContract = RawContract(address = toToken.address, decimal = toToken.decimals.toString(), value = toAmount.toPlainString()),
                            toaddress = walletAddress,
                            tokenId = "",
                            value = toAmount.toDouble(),
                            blockTimestamp = now,
                            userIsSender = false
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("SwapViewModel", "applyLocalSwapAdjustments failed", e)
            }
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
            reflectiveLedPattern?.displaySwap()
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
                reflectiveLedPattern?.displaySwap()
            } catch (e: Exception) {
                Log.e("SwapViewModel", "Error redrawing swap terminal screen after resume", e)
            }
        }
    }

    fun onSwapTerminalClosed() {
        viewModelScope.launch {
            try {
                // Clear any pending transaction status when leaving swap screen
                clearSwapTransactionStatus()
                
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


sealed interface SwapTokenUiState {
    object Loading: SwapTokenUiState
    object Error: SwapTokenUiState
    data class Success(val tokenAssets: List<TokenAssetWithPrice>): SwapTokenUiState
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
        val tokens: List<TokenAssetWithPrice>
    ) : FromTokensUiState
}

/**
 * State for custom token lookup (paste contract address feature)
 */
sealed interface CustomTokenLookupState {
    /** No lookup in progress */
    object Idle : CustomTokenLookupState

    /** Looking up token metadata */
    object Loading : CustomTokenLookupState

    /** Token found successfully */
    data class Found(val token: TokenAssetWithPrice) : CustomTokenLookupState

    /** Token not found at the given address */
    data class NotFound(val address: String) : CustomTokenLookupState

    /** Error occurred during lookup */
    data class Error(val message: String) : CustomTokenLookupState
}
