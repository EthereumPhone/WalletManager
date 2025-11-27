package com.core.data.repository

import kotlinx.coroutines.flow.SharedFlow

/**
 * Minimal interface for terminal content interactions used by PayMaster.
 * Implemented by the concrete TerminalRepository to enable faking in tests.
 */
interface TerminalContentRepository {
    val events: SharedFlow<TerminalEvent>
    suspend fun generateTopUp()
    suspend fun dismissContent()
}


