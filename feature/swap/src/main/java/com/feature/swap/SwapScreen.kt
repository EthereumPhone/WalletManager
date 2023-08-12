package com.feature.swap

import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.feature.swap.ui.TokenPickerSheet

@Composable
internal fun SwapRoute(

) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwapScreen(
    modifier: Modifier
) {

    var isVisible by remember { mutableStateOf(true) }

    // Create a BottomSheetScaffoldState
    val scaffoldState = rememberBottomSheetScaffoldState()

    // Wrap the content inside the BottomSheetScaffold
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            //TokenPickerSheet(emptyList())
                       },
        sheetPeekHeight = 0.dp
    ) {

    }
}
