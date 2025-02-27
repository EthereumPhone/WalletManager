package com.feature.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.feature.home.ui.CardCarousel
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.feature.send.SelectedTokenUiState
import com.feature.send.SendViewModel
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun HomeRoute2(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToLog: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    sendViewModel: SendViewModel = hiltViewModel()

) {
    val walletDataUiState: WalletDataUiState by viewModel.walletDataState.collectAsStateWithLifecycle()
    val assetsUiState: AssetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val selectedTokenUiState: SelectedTokenUiState by sendViewModel.selectedAssetUiState.collectAsStateWithLifecycle()

    var updater by remember { mutableStateOf(true) }

    if(updater) {
        Log.d("automatic updater", "TEST")
        viewModel.refreshData()
        updater = false
    }

    HomeScreen2(
        userData = walletDataUiState,
        assetsUiState = assetsUiState,
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToLog = navigateToLog,
        selectedTokenUiState = selectedTokenUiState,
        setSelectedToken = sendViewModel::updateSelectedAsset,

    )
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen2(
    userData: WalletDataUiState,
    assetsUiState: AssetsUiState,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToLog: () -> Unit,
    selectedTokenUiState: SelectedTokenUiState,
    setSelectedToken: (TokenAsset) -> Unit,
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current
    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack),
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .background(dgenBlack)
            //.weight(1f) // Allows it to take up remaining space

        ) {

            when(assetsUiState){
                AssetsUiState.Empty -> {

                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(82.dp),
                                contentScale = ContentScale.Crop,
                                painter = painterResource(id = com.core.ui.R.drawable.no_assets),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(dgenTurqoise)
                            )
                            Text(
                                text = "No assets",
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = dgenTurqoise,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 24.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                )
                            )
                        }
                    }
                }
                AssetsUiState.Error -> {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(82.dp),
                                contentScale = ContentScale.Crop,
                                painter = painterResource(id = com.core.ui.R.drawable.baseline_error_outline_24),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Colors.GRAY)
                            )
                            Text(text = "Error",
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = dgenTurqoise,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 24.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                ))

                        }
                    }
                }
                AssetsUiState.Loading -> {

                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            modifier = modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "Loading...",
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    color = dgenTurqoise,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 24.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                ))
                        }
                  }
                }
                is AssetsUiState.Success -> {
                    if(assetsUiState.assets.isNotEmpty()){
                        CardCarousel(
                            modifier = Modifier.padding(bottom = 24.dp),
                            assets = assetsUiState.assets,
                            selectedTokenUiState = selectedTokenUiState,
                            setSelectedToken = setSelectedToken
                        )
                    }else {
                        Box(
                            modifier = modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    modifier = Modifier.size(82.dp),
                                    contentScale = ContentScale.Fit,
                                    painter = painterResource(id = R.drawable.wallet_icon),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(dgenTurqoise)
                                )
                                Text(
                                    text = "No assets".uppercase(),
                                    style = TextStyle(
                                        fontFamily = SpaceMono,
                                        color = dgenTurqoise,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 24.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    )
                                )
                            }
                        }
                    }

                }
            }

        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()

        ) {
            // Fading border overlay (Green to Transparent)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp) // Adjust thickness of fading border
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, dgenBlack)
                        )
                    )

            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(dgenBlack)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                when(assetsUiState){
                    AssetsUiState.Empty -> {

                    }
                    AssetsUiState.Error -> {

                    }
                    AssetsUiState.Loading -> {

                    }
                    is AssetsUiState.Success -> {
                        if(assetsUiState.assets.isNotEmpty()){
                            IconButton(modifier = Modifier, onClick = navigateToSend) {

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        modifier = Modifier.width(16.dp),
                                        painter = painterResource(R.drawable.baseline_arrow_outward_24),
                                        contentDescription = "Back",
                                        tint = dgenTurqoise
                                    )
                                    Text(
                                        text= "SEND",
                                        style = TextStyle(
                                            fontFamily = SpaceMono,
                                            color = dgenTurqoise,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            lineHeight = 12.sp,
                                            letterSpacing = 0.sp,
                                            textDecoration = TextDecoration.None
                                        )
                                    )
                                }

                            }
                            Spacer(modifier = Modifier.width(32.dp))
                        }

                    }
                }

                IconButton(modifier = Modifier, onClick = navigateToLog) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.width(16.dp),
                            painter = painterResource(R.drawable.baseline_swap_vert_24),
                            contentDescription = "Back",
                            tint = dgenTurqoise
                        )
                        Text(
                            text= "LOG",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = dgenTurqoise,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }

                }
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = dgenTurqoise,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.width(IntrinsicSize.Max),
                    onClick = {

                        when(userData){
                            WalletDataUiState.Loading -> {
                                copyTextToClipboard(context,"Loading...")
                                Toast.makeText(context, "Failed to copy address", Toast.LENGTH_SHORT).show()

                            }
                            is WalletDataUiState.Success -> {
                                copyTextToClipboard(context, userData.userData.walletAddress)
                                Toast.makeText(context, "Copied Address", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier
                                .width(16.dp)
                                .graphicsLayer {
                                    rotationZ = 90f
                                },
                            painter = painterResource(R.drawable.cpyaddress),
                            contentDescription = "Back",
                            tint = dgenTurqoise
                        )
                        Text(
                            text= "CPY ADD",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = dgenTurqoise,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }

                }

            }
        }
    }

}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}
