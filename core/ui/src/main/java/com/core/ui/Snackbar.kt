package com.core.ui



import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.dgenlibrary.ui.theme.dgenDarkBlack
import com.example.dgenlibrary.ui.theme.dgenGreen
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenOrche
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


enum class SnackbarState {
    DEFAULT,
    ERROR,
    SUCCESS,
    WARNING
}

class SnackbarDelegate(
    hostState: SnackbarHostState,
    coroutine: CoroutineScope
) {
    var snackbarHostState by mutableStateOf(hostState)
    var coroutineScope by mutableStateOf(coroutine)

    var snackbarState: SnackbarState = SnackbarState.DEFAULT


    var snackbarBackgroundColor = dgenOcean

    var snackbarOnColor = dgenOcean

    var snackbarIcon = R.drawable.outline_info_24



    fun showSnackbar(
        state: SnackbarState,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ){
        this.snackbarState = state

        this.snackbarBackgroundColor = when (this.snackbarState) {
            SnackbarState.DEFAULT -> dgenOcean
            SnackbarState.ERROR -> dgenRed
            SnackbarState.SUCCESS -> dgenGreen
            SnackbarState.WARNING -> dgenOrche
        }

        this.snackbarOnColor = when(this.snackbarState){
            SnackbarState.DEFAULT -> dgenTurqoise
            SnackbarState.ERROR -> dgenWhite
            SnackbarState.SUCCESS -> dgenWhite
            SnackbarState.WARNING -> dgenDarkBlack.copy(0.6f)
        }

        //== SnackbarState.WARNING) Color.GRAY else Color.WHITE

        this.snackbarIcon = when (this.snackbarState) {
            SnackbarState.DEFAULT -> R.drawable.outline_info_24
            SnackbarState.ERROR -> R.drawable.outline_error_outline_24
            SnackbarState.SUCCESS -> R.drawable.baseline_check_24
            SnackbarState.WARNING -> R.drawable.outline_warning_24
        }

        coroutineScope.launch {
            snackbarHostState.showSnackbar(message=message, actionLabel=actionLabel, duration=duration)
        }
    }

}

@Composable
fun rememberSnackbarDelegate(
    hostState: SnackbarHostState,
    coroutine: CoroutineScope
) = remember(hostState,coroutine) {
    SnackbarDelegate(hostState,coroutine)
}