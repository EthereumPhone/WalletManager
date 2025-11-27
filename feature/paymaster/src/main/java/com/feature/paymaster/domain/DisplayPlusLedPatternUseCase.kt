package com.feature.paymaster.domain

import com.core.terminalsdk.ReflectiveLedPattern

class DisplayPlusLedPatternUseCase(
    private val reflectiveLedPattern: ReflectiveLedPattern?
) {
    operator fun invoke(color: String?) {
        try {
            reflectiveLedPattern?.displayPlus(color)
        } catch (_: Exception) {
            // swallow errors – LED operations are best-effort
        }
    }
}


