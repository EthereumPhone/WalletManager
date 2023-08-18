package com.feature.swap

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.ui.SwipeButton
import com.core.ui.TopHeader
import com.core.ui.WmButton
import com.feature.home.AssetUiState
import com.feature.swap.ui.TokenPickerSheet
import com.feature.swap.ui.TokenSelector
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun SwapRoute(
    modifier: Modifier,
    viewModel: SwapViewModel = hiltViewModel()

) {
    val amountsUiState by viewModel.amountsUiState.collectAsStateWithLifecycle()
    val assetsUiState by viewModel.assetsUiState.collectAsStateWithLifecycle()

    val swapTokenUiState by viewModel.swapTokenUiState.collectAsStateWithLifecycle()
    val exchangeUiState by viewModel.exchangeRate.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    SwapScreen(
        modifier = modifier,
//        swapTokenUiState = swapTokenUiState,
//        exchangeUiState = exchangeUiState,
//        amountsUiState = amountsUiState,
//        assetsUiState = assetsUiState,
//        searchQuery = searchQuery,
//        onQueryChange = viewModel::updateSearchQuery,
//        switchTokens = viewModel::switchTokens,
//        onTextFieldSelected = viewModel::setSelectedTextField,
//        onAmountChange = viewModel::updateAmount,
//        onSelectAsset = viewModel::selectAsset,
//        onSwapClicked = viewModel::swap
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwapScreen(
    modifier: Modifier=Modifier,
//    swapTokenUiState: SwapTokenUiState,
//    exchangeUiState: Double,
//    amountsUiState: AmountsUiState,
//    assetsUiState: AssetsUiState,
//    searchQuery: String,
//    onQueryChange: (String) -> Unit,
//    switchTokens: () -> Unit,
//    onTextFieldSelected: (TextFieldSelected) -> Unit,
//    onAmountChange: (String) -> Unit,
//    onSelectAsset: (TokenAsset) -> Unit,
//    onSwapClicked: () -> Unit
) {


    // Create a BottomSheetScaffoldState
    var showSheet by remember { mutableStateOf(false) }
    val modalSheetState = rememberModalBottomSheetState(true)
    val coroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2730))
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {

        TopHeader(
            onBackClick = { /*TODO*/ },
            title = "Swap",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Information",
                    tint = Color(0xFF9FA2A5),
                    modifier = modifier
                        .clip(CircleShape)
                        //.background(Color.Red)
                        .clickable {
                            //opens InfoDialog
                            //showDialog.value = true
                        }

                )
            }
        )
        Spacer(modifier = modifier.height(12.dp))

        var clicked by remember { mutableStateOf(0) }
        var amount by remember { mutableStateOf(AmountsUiState("0.123", "0.234")) }

        TokenSelector(
//            amountsUiState = amountsUiState,
//            assetsUiState = assetsUiState,
//            switchTokens = switchTokens,
//            onAmountChange = { selectedTextField, amount ->
//                onTextFieldSelected(selectedTextField)
//                onAmountChange(amount)
//            },

            amountsUiState = amount,
            assetsUiState = AssetsUiState(SelectedTokenUiState.Unselected, SelectedTokenUiState.Unselected),
            modifier = Modifier.fillMaxWidth(),
            switchTokens = {},
            onPickAssetClicked = {showSheet = true},//{textFieldSelected: TextFieldSelected -> clicked += 1 },
            onAmountChange = {
                    textFieldSelected: TextFieldSelected, text: String ->
                amount = when(textFieldSelected) {
                    TextFieldSelected.FROM -> {
                        amount.copy(
                            fromAmount = text
                        )
                    }

                    TextFieldSelected.TO -> {
                        amount.copy(
                            toAmount = text
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.weight(1f))

//        WmButton(
//            onClick = {},//onSwapClicked,
//        ) {
//            Text("Swap")
//        }
        SwipeButton(
            text = "Swipe to swap",
            icon = Icons.Rounded.ArrowForward,
            completeIcon = Icons.Rounded.Check,
            onSwipe = {

            }//onSwapClicked,
        )


        if(showSheet) {
            ModalBottomSheet(
                containerColor= Color(0xFF24303D),
                contentColor= Color.White,

                onDismissRequest = {
                    coroutineScope.launch {
                        modalSheetState.hide()
                    }.invokeOnCompletion {
                        if(!modalSheetState.isVisible) showSheet = false
                    }
                },
                sheetState = modalSheetState
            ) {
                TokenPickerSheet(
//                    swapTokenUiState = swapTokenUiState,
//                    searchQuery = searchQuery,
//                    onQueryChange = onQueryChange,
//                    onSelectAsset = {
//                        onSelectAsset(it)
//                        coroutineScope.launch {
//                            modalSheetState.hide()
//                        }.invokeOnCompletion {
//                            if(!modalSheetState.isVisible) showSheet = false
//                        }
//                    }
                    //MockData
                    SwapTokenUiState.Success(
                        listOf(
                            TokenAsset("1", 1, "test", "test test", 123.2),
                            TokenAsset("2", 5, "test", "test test", 123.2),
                            TokenAsset("3", 10, "test", "test test", 123.2),
                            TokenAsset("4", 137, "test", "test test", 123.2),
                            TokenAsset("5", 42161, "test", "test test", 123.2),
                        )
                    ),
                    "",
                    {},
                    {}
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSwapScreen() {
    SwapScreen()
}


