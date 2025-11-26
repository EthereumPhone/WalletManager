package com.feature.send.fakes

import android.content.Context
import com.core.data.repository.TerminalRepository
import com.core.terminalsdk.TerminalSDKWrapper

fun createNoopTerminalRepository(context: Context): TerminalRepository {
    return TerminalRepository(TerminalSDKWrapper.Unavailable, context)
}


