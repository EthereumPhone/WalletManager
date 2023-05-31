package org.ethereumphone.walletmanager.feature_swap.model

import org.ethereumphone.walletmanager.core.domain.model.ERCToken

data class SwapState(
    val tokens: List<ERCToken> ?= emptyList(),
    val isLoading: Boolean = false
)