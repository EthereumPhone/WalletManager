package com.example.transactions.fakes

import android.content.Context
import com.core.data.repository.TerminalRepository
import com.core.terminalsdk.TerminalSDKWrapper

/**
 * Creates a real TerminalRepository wired to an unavailable SDK.
 *
 * This mirrors helpers in other features (send/swap/paymaster) and ensures
 * that Terminal calls in the ViewModel are safe no-ops in unit tests.
 */
fun createNoopTerminalRepository(context: Context): TerminalRepository {
    return TerminalRepository(TerminalSDKWrapper.Unavailable, context)
}




