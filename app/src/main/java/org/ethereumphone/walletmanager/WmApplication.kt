package org.ethereumphone.walletmanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WmApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}