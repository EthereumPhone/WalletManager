package com.feature.paymaster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.UserDataRepository
import com.core.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PayMasterViewModel @Inject constructor(): ViewModel() {

    suspend fun topUp(): Double {
        // TODO: Markus - implement logic
        return 0.0
    }

}
