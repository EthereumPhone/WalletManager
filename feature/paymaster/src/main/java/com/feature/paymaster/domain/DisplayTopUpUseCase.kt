package com.feature.paymaster.domain

import com.core.terminalsdk.TerminalSDK

class DisplayTopUpUseCase(
    private val terminalSDK: TerminalSDK?
) {
    /**
     * Shows the TopUp screen on the terminal and wires the provided callback.
     * Returns true if successfully displayed, false otherwise.
     */
    suspend operator fun invoke(onTopUp: () -> Unit): Boolean {
        return try {
            if (terminalSDK?.isAvailable() == true) {
                terminalSDK.displayTopUp(onTopUp)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}


