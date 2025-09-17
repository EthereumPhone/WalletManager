package com.feature.home

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.NetworkMonitor
import com.core.domain.GetAllGroupedTokensUsecase
import com.core.model.TokenAsset
import com.core.model.UserData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import com.core.data.repository.DEFAULT_EXCLUDE_LIST
import com.core.model.TokenGroupAssetOverview
import com.core.ui.showDgenToast

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val userDataRepository: UserDataRepository,
    private val transferRepository: TransferRepository,
    private val getAllGroupedTokensUsecase: GetAllGroupedTokensUsecase,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val walletDataState: StateFlow<WalletDataUiState> = userDataRepository.userData.map {
        WalletDataUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = WalletDataUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )



    val groupedTokenAssetState: StateFlow<GroupedAssetsUiState> =
        getAllGroupedTokensUsecase(DEFAULT_EXCLUDE_LIST).map {
            if (it.isEmpty()) {
                GroupedAssetsUiState.Empty
            } else {
                // Sort assets by totalFiatBalance in descending order (highest value first)
                val sortedAssets = it.sortedBy { asset -> asset.totalFiatBalance ?: 0.0 }
                GroupedAssetsUiState.Success(sortedAssets)
            }

        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GroupedAssetsUiState.Loading
        )


    val hasTransfers: StateFlow<Boolean> = transferRepository.observeTransfersExist()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
        .map { isOnlineValue ->
            val offlineValue = !isOnlineValue
            Log.d("HomeViewModel.isOffline", "Received isOnline=$isOnlineValue, emitting isOffline=$offlineValue")
            offlineValue
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true, // Start with offline assumption
        )

    private val _refreshState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _refreshState.asStateFlow()

    private val _selectedTokenAsset = MutableStateFlow<TokenAsset?>(null)
    val selectedTokenAsset: StateFlow<TokenAsset?> = _selectedTokenAsset.asStateFlow()


    suspend fun getLink(uri: String): String? {
        // Check for internet connectivity first
        // If offline, show a toast and return the original uri so that callers don't crash
        if (!networkMonitor.isOnline.first()) {
            showDgenToast(context,"No internet connection!")
            return null
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://getmoonpaynew-4bl33rjqpa-uc.a.run.app?text=$uri")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response: Response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                // Parse the JSON response using Moshi
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()

                val adapter = moshi.adapter(MoonpayResponse::class.java)
                val responseBody: ResponseBody? = response.body
                if (responseBody != null) {
                    val moonpayResponse = adapter.fromJson(responseBody.string())
                    return@withContext moonpayResponse?.link
                        ?: throw IOException("Invalid response format")
                } else {
                    throw IOException("Empty response")
                }
            } catch (e: Exception) {
                throw IOException("Error fetching link: ${e.message}", e)
            }
        }
    }

    data class MoonpayResponse(
        val link: String
    )
}


sealed interface GroupedAssetsUiState {
    object Loading : GroupedAssetsUiState

    object Error: GroupedAssetsUiState

    object Empty: GroupedAssetsUiState

    data class Success(
        val assets: List<TokenGroupAssetOverview>
    ) : GroupedAssetsUiState
}
sealed interface WalletDataUiState {
    object Loading : WalletDataUiState
    data class Success(val userData: UserData) : WalletDataUiState
}