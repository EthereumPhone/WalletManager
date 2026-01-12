package com.feature.paymaster.domain

import com.core.terminalsdk.TerminalSDK

class IsTerminalScreenOnUseCase(
    private val terminalSDK: TerminalSDK?
) {
    suspend operator fun invoke(): Boolean {
        return try {
            terminalSDK?.isScreenOn() == true
        } catch (_: Exception) {
            false
        }
    }
}



