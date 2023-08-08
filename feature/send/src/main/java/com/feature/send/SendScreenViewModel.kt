package com.feature.send

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.NetworkBalanceRepository
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
import kotlinx.coroutines.coroutineScope
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
class SendViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
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

    val networkBalanceInfo = networkBalanceRepository



}



