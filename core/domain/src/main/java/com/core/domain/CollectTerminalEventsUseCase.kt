package com.core.domain

import com.core.data.repository.TerminalContentRepository
import com.core.data.repository.TerminalEvent
import kotlinx.coroutines.flow.SharedFlow

class CollectTerminalEventsUseCase(
    private val terminalContentRepository: TerminalContentRepository
) {
    operator fun invoke(): SharedFlow<TerminalEvent> = terminalContentRepository.events
}


