package com.feature.paymaster.domain

import com.core.terminalsdk.TerminalSDK

class IsTerminalAvailableUseCase(
    private val terminalSDK: TerminalSDK?
) {
    suspend operator fun invoke(): Boolean {
        return try {
            terminalSDK?.isAvailable() == true
        } catch (_: Exception) {
            false
        }
    }
}


