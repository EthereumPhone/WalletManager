package com.feature.paymaster.fakes

import android.content.Context
import com.core.data.repository.TerminalContentRepository
import com.core.data.repository.TerminalRepository
import com.core.data.repository.WalletRepository
import com.core.domain.CollectTerminalEventsUseCase
import com.core.domain.DismissTerminalContentUseCase
import com.core.domain.GenerateTopUpUseCase
import com.core.domain.GetWalletAddressUseCase
import com.core.terminalsdk.TerminalSDKWrapper
import com.feature.paymaster.domain.DisplayPlusLedPatternUseCase
import com.feature.paymaster.domain.DisplayTopUpUseCase
import com.feature.paymaster.domain.IsTerminalAvailableUseCase
import com.feature.paymaster.domain.IsTerminalScreenOnUseCase
import com.core.terminalsdk.TerminalSDK
import com.core.terminalsdk.ReflectiveLedPattern

object PayMasterUsecaseFactory {

    data class TerminalContentComponents(
        val terminalContentRepository: TerminalContentRepository,
        val generateTopUpUseCase: GenerateTopUpUseCase,
        val dismissTerminalContentUseCase: DismissTerminalContentUseCase,
        val collectTerminalEventsUseCase: CollectTerminalEventsUseCase
    )

    fun createTerminalContentWithFake(): TerminalContentComponents {
        val fakeRepo = FakeTerminalContentRepository()
        return TerminalContentComponents(
            terminalContentRepository = fakeRepo,
            generateTopUpUseCase = GenerateTopUpUseCase(fakeRepo),
            dismissTerminalContentUseCase = DismissTerminalContentUseCase(fakeRepo),
            collectTerminalEventsUseCase = CollectTerminalEventsUseCase(fakeRepo)
        )
    }

    /**
     * Convenience to create a no-op real TerminalRepository (unavailable SDK).
     * Mirrors helpers in other features.
     */
    fun createNoopTerminalRepository(context: Context): TerminalRepository {
        return TerminalRepository(TerminalSDKWrapper.Unavailable, context)
    }

    data class WalletComponents(
        val walletRepository: WalletRepository,
        val getWalletAddressUseCase: GetWalletAddressUseCase
    )

    fun createWalletWithFake(initialAddress: String = ""): WalletComponents {
        val walletRepo = FakeWalletRepository(initialAddress)
        return WalletComponents(
            walletRepository = walletRepo,
            getWalletAddressUseCase = GetWalletAddressUseCase(walletRepo)
        )
    }

    data class TerminalSdkUseCases(
        val isTerminalAvailableUseCase: IsTerminalAvailableUseCase,
        val isTerminalScreenOnUseCase: IsTerminalScreenOnUseCase,
        val displayTopUpUseCase: DisplayTopUpUseCase,
        val displayPlusLedPatternUseCase: DisplayPlusLedPatternUseCase
    )

    /**
     * Build TerminalSDK-related use cases. These accept nullable dependencies and no-op safely.
     */
    fun createTerminalSdkUseCases(
        terminalSDK: TerminalSDK? = null,
        reflectiveLedPattern: ReflectiveLedPattern? = null
    ): TerminalSdkUseCases {
        return TerminalSdkUseCases(
            isTerminalAvailableUseCase = IsTerminalAvailableUseCase(terminalSDK),
            isTerminalScreenOnUseCase = IsTerminalScreenOnUseCase(terminalSDK),
            displayTopUpUseCase = DisplayTopUpUseCase(terminalSDK),
            displayPlusLedPatternUseCase = DisplayPlusLedPatternUseCase(reflectiveLedPattern)
        )
    }
}


