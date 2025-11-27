package com.feature.paymaster.fakes

import com.core.data.repository.TerminalContentRepository
import com.core.data.repository.TerminalEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class FakeTerminalContentRepository : TerminalContentRepository {
    private val _events = MutableSharedFlow<TerminalEvent>()
    override val events: SharedFlow<TerminalEvent> = _events

    var generatedTopUpCount: Int = 0
    var dismissedCount: Int = 0

    override suspend fun generateTopUp() {
        generatedTopUpCount++
    }

    override suspend fun dismissContent() {
        dismissedCount++
    }

    suspend fun emit(event: TerminalEvent) {
        _events.emit(event)
    }
}


