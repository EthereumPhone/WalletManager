package com.feature.paymaster

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import com.core.data.repository.TerminalRepository
import com.feature.paymaster.fakes.PayMasterUsecaseFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PayMasterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var terminalRepository: TerminalRepository

    private lateinit var viewModel: PayMasterViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        terminalRepository = PayMasterUsecaseFactory.createNoopTerminalRepository(context)

        viewModel = PayMasterViewModel(
            context = context,
            walletSDK = null,
            terminalRepository = terminalRepository,
            terminalSDK = null,
            reflectiveLedPattern = null
        )
    }

    @Test
    fun init_defaults_balance_to_zero_when_sdk_init_fails() {
        // With no real Paymaster service available, initialize() returns false
        // and balance should remain "0.0"
        assertEquals("0.0", viewModel.balance.value)
    }

    @Test
    fun topUp_returns_null_when_no_internet() = runTest {
        val result = viewModel.topUp("10")
        assertNull(result)
    }

    @Test
    fun onTopUpOpened_with_empty_amount_does_not_launch_intent() = runTest {
        // terminalSDK is null, so no displayTopUp path is executed and no Intent is launched
        viewModel.onTopUpOpened()

        val app = ApplicationProvider.getApplicationContext<Application>()
        val next = Shadows.shadowOf(app).nextStartedActivity
        assertNull(next)
    }
}


