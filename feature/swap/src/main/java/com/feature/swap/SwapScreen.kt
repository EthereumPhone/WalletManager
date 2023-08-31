package com.feature.swap

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.core.model.TokenAsset
import com.core.ui.InfoDialog
import com.core.ui.SwipeButton
import com.core.ui.TopHeader
//import com.core.ui.WmButton
import com.feature.home.AssetUiState
import com.feature.swap.ui.TokenPickerSheet
import com.feature.swap.ui.TokenSelector
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun SwapRoute(
    modifier: Modifier,
    viewModel: SwapViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val amountsUiState by viewModel.amountsUiState.collectAsStateWithLifecycle()
    val assetsUiState by viewModel.assetsUiState.collectAsStateWithLifecycle()

    val swapTokenUiState by viewModel.swapTokenUiState.collectAsStateWithLifecycle()
    val exchangeUiState by viewModel.exchangeRate.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    SwapScreen(
        modifier = modifier,
        swapTokenUiState = swapTokenUiState,
        exchangeUiState = exchangeUiState,
        amountsUiState = amountsUiState,
        assetsUiState = assetsUiState,
        searchQuery = searchQuery,
        onQueryChange = viewModel::updateSearchQuery,
        switchTokens = viewModel::switchTokens,
        onTextFieldSelected = viewModel::setSelectedTextField,
        onAmountChange = viewModel::updateAmount,
        onSelectAsset = viewModel::selectAsset,
        onSwapClicked = {
                        viewModel.swap(it)
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwapScreen(

    modifier: Modifier=Modifier,
    swapTokenUiState: SwapTokenUiState,
    exchangeUiState: Double,
    amountsUiState: AmountsUiState,
    assetsUiState: AssetsUiState,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    switchTokens: () -> Unit,
    onTextFieldSelected: (TextFieldSelected) -> Unit,
    onAmountChange: (String) -> Unit,
    onSelectAsset: (TokenAsset) -> Unit,
    onSwapClicked: ((String) -> Unit) -> Unit,
    onBackClick: () -> Unit

) {


    // Create a BottomSheetScaffoldState
    var showSheet by remember { mutableStateOf(false) }
    val modalSheetState = rememberModalBottomSheetState(true)
    val coroutineScope = rememberCoroutineScope()

    //Info
    val showInfoDialog =  remember { mutableStateOf(false) }
    if(showInfoDialog.value){
        InfoDialog(
            setShowDialog = {
                showInfoDialog.value = false
            },
            title = "Info",
            text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt"
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2730))
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {

        TopHeader(
            onBackClick = onBackClick
                          ,
            title = "Swap",
            icon = {
                IconButton(
                    onClick = {
                        //opens InfoDialog
                        showInfoDialog.value = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Information",
                        tint = Color(0xFF9FA2A5),
                        modifier = modifier
                            .clip(CircleShape)

                    )
                }
            }
        )
        Spacer(modifier = modifier.height(12.dp))

        TokenSelector(
            amountsUiState = amountsUiState,
            assetsUiState = assetsUiState,
            switchTokens = switchTokens,
            onAmountChange = { selectedTextField, amount ->
                onTextFieldSelected(selectedTextField)
                if (amount != "") {
                    onAmountChange(amount)
                }
            },
            onPickAssetClicked = {
                showSheet = true
                onTextFieldSelected(it)
            }
        )
        Spacer(modifier = modifier.height(24.dp))
        Column (
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)


        ){
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Swap fee",
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    color= Color(0xFF9FA2A5)
                )
                Text(
                    text = "(0.5%)",
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color= Color(0xFF9FA2A5)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        val context = LocalContext.current
        SwipeButton(
            text = "Swipe to swap",
            icon = Icons.Rounded.ArrowForward,
            //enabled = true,
            onSwipe = {
                onSwapClicked {
                    if (it.length == 66 && isEthereumTransactionHash(it)) {
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "Swap successful.", Toast.LENGTH_LONG).show()
                        }
                    } else if (it == "decline") {
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "Transaction declined", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                        }
                    }
                    onBackClick()


                }
            }
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
                    swapTokenUiState = swapTokenUiState,
                    searchQuery = searchQuery,
                    onQueryChange = onQueryChange,
                    onSelectAsset = {
                        onSelectAsset(it)
                        coroutineScope.launch {
                            modalSheetState.hide()
                        }.invokeOnCompletion {
                            if(!modalSheetState.isVisible) showSheet = false
                        }
                    }
                )
            }
        }
    }
}

fun isEthereumTransactionHash(input: String): Boolean {
    val transactionHashPattern = "^0x([A-Fa-f0-9]{64})$"
    return Regex(transactionHashPattern).matches(input)
}

@Preview
@Composable
fun PreviewSwapScreen() {
    //SwapScreen()
}


