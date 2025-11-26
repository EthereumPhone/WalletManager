package com.feature.home.fakes

import com.core.data.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkMonitor(
    initialIsOnline: Boolean = true
) : NetworkMonitor {
    private val onlineState = MutableStateFlow(initialIsOnline)

    override val isOnline: Flow<Boolean> = onlineState

    fun setOnline(isOnline: Boolean) {
        onlineState.value = isOnline
    }

    fun toggle() {
        onlineState.value = !onlineState.value
    }
}


