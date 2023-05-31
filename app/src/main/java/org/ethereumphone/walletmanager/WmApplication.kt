package org.ethereumphone.walletmanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.ethereumphone.walletmanager.core.workers.initializers.SeedTokenMetadata

@HiltAndroidApp
class WmApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        SeedTokenMetadata.initialize(context = this)
    }
}