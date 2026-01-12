package com.feature.send

import android.Manifest
import android.os.Build.VERSION.SDK_INT
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.core.model.NFT
import com.core.model.NftTokenType
import com.core.ui.SwipeButton
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenWhite
import com.core.ui.util.pulseOpacity
import com.feature.send.ui.RecipientSection
import com.feature.send.ui.SendHeader
import com.feature.send.ui.TransactionStatus
import com.feature.send.ui.TransactionStatusOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import kotlinx.coroutines.delay

/**
 * Send NFT Screen - For sending NFTs to another address
 * Similar to SendScreen but without amount field and with NFT-specific UI
 */
@Composable
fun SendNftRoute(
    contractAddress: String,
    tokenId: String,
    chainId: Int,
    onBackClick: () -> Unit,
    viewModel: SendNftViewModel = hiltViewModel()
) {
    val nft by viewModel.nft.collectAsStateWithLifecycle()
    val recipientUiState by viewModel.recipientUiState.collectAsStateWithLifecycle()
    val qrScannerTriggered by viewModel.qrScannerTriggered.collectAsStateWithLifecycle()
    val transactionStatus by viewModel.transactionStatus.collectAsStateWithLifecycle()
    val shouldDismissKeyboard by viewModel.shouldDismissKeyboard.collectAsStateWithLifecycle()

    // Load NFT on first composition
    LaunchedEffect(contractAddress, tokenId, chainId) {
        viewModel.loadNft(contractAddress, tokenId, chainId)
    }

    // Flag to ensure the first ON_RESUME (which happens on the initial screen launch) is ignored
    var hasHandledInitialResume by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, transactionStatus) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Skip the very first ON_RESUME that occurs when the screen is opened for the first time
                    if (!hasHandledInitialResume) {
                        hasHandledInitialResume = true
                    } else if (nft != null && transactionStatus == null) {
                        // Only call onScreenOpenedAfterResume on subsequent resumes when no transaction is running
                        viewModel.onScreenOpenedAfterResume()
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            viewModel.onSendNftClosed()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Display terminal layout when NFT is loaded and no transaction is active
    LaunchedEffect(nft, transactionStatus) {
        if (nft != null && transactionStatus == null) {
            viewModel.onScreenOpened()
        }
    }

    BackHandler {
        viewModel.onSendNftClosed()
        onBackClick()
    }

    LaunchedEffect(transactionStatus) {
        when (transactionStatus) {
            TransactionStatus.SUCCESS -> {
                delay(TransactionTiming.SUCCESS_DISPLAY_DURATION)
                onBackClick()
                delay(TransactionTiming.FADE_TRANSITION_DURATION)
                viewModel.clearTransactionStatus()
            }
            is TransactionStatus.FAILURE -> {
                delay(TransactionTiming.FAILURE_DISPLAY_DURATION)
                onBackClick()
                delay(TransactionTiming.FADE_TRANSITION_DURATION)
                viewModel.clearTransactionStatus()
            }
            else -> {}
        }
    }

    SendNftScreen(
        nft = nft,
        recipientUiState = recipientUiState,
        transactionStatus = transactionStatus,
        qrScannerTriggered = qrScannerTriggered,
        shouldDismissKeyboard = shouldDismissKeyboard,
        onRecipientChange = viewModel::updateAddress,
        onSendNft = viewModel::sendNft,
        clearTransactionStatus = viewModel::clearTransactionStatus,
        resetQrScannerTrigger = viewModel::resetQrScannerTrigger,
        onKeyboardDismissed = viewModel::onKeyboardDismissed,
        onBackClick = {
            viewModel.onSendNftClosed()
            onBackClick()
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SendNftScreen(
    nft: NFT?,
    recipientUiState: RecipientUiState,
    transactionStatus: TransactionStatus?,
    qrScannerTriggered: Boolean,
    shouldDismissKeyboard: Boolean,
    onRecipientChange: (String) -> Unit,
    onSendNft: () -> Unit,
    clearTransactionStatus: () -> Unit,
    resetQrScannerTrigger: () -> Unit,
    onKeyboardDismissed: () -> Unit,
    onBackClick: () -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
            .statusBarsPadding()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        // NFT Image as background with overlay
        if (nft?.imageUrl != null || nft?.thumbnailUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(nft.imageUrl ?: nft.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = gifEnabledLoader,
                contentDescription = nft?.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f)
            )
            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                dgenBlack.copy(alpha = 0.7f),
                                dgenBlack.copy(alpha = 0.9f),
                                dgenBlack
                            )
                        )
                    )
            )
        } else {
            // Wireframe background if no NFT image
            AsyncImage(
                modifier = Modifier
                    .alpha(pulseOpacity)
                    .offset(x = 250.dp, y = 20.dp)
                    .scale(1.3f)
                    .aspectRatio(1f),
                imageLoader = gifEnabledLoader,
                model = R.drawable.globe_wireframe,
                contentDescription = null,
                colorFilter = ColorFilter.tint(primaryColor)
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
            Column {
                // Header - using the same SendHeader pattern as SendScreen
                SendHeader(
                    modifier = Modifier.padding(bottom = 48.dp),
                    nftName = nft?.name ?: "NFT",
                    nftImageUrl = nft?.thumbnailUrl ?: nft?.imageUrl,
                    onBackClick = onBackClick
                )

                // NFT Info Card
                if (nft != null) {
                    NftInfoCard(
                        nft = nft,
                        primaryColor = primaryColor,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recipient Section
                RecipientSection(
                    recipientUiState = recipientUiState,
                    selectedContact = null,
                    hasContactsWithEth = false,
                    onContentChanged = onRecipientChange,
                    onContactIconClick = {},
                    onClearContact = {},
                    shouldDismissKeyboard = shouldDismissKeyboard,
                    onKeyboardDismissed = onKeyboardDismissed
                )
            }

            // Bottom section - Send Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Validate recipient address: must be a valid Ethereum address (0x followed by 40 hex chars)
                val isValidRecipient = recipientUiState.recipientAddress.matches(
                    Regex("^0x[a-fA-F0-9]{40}$")
                ) && recipientUiState.ensError.isEmpty()

//                SwipeButton(
//                    text = "SEND NFT",
//                    isComplete = transactionStatus == TransactionStatus.SUCCESS,
//                    onSwipe = {
//                        if (isValidRecipient) {
//                            onSendNft()
//                        }
//                    },
//                    primaryColor = primaryColor,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 16.dp)
//                        .alpha(if (isValidRecipient) 1f else 0.5f)
//                )
            }
        }

        // Transaction Status Overlay
        TransactionStatusOverlay(
            status = transactionStatus,
            gifLoader = gifEnabledLoader,
            onDismiss = { clearTransactionStatus() },
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    }

    // QR Scanner handling
    val barCodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                val cleanAddress = parseEthereumUri(result.contents)
                onRecipientChange(cleanAddress)
            }
        }
    )

    val scanningPermissionsToRequest = listOf(Manifest.permission.CAMERA)
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = scanningPermissionsToRequest
    )

    LaunchedEffect(qrScannerTriggered) {
        if (qrScannerTriggered) {
            if (multiplePermissionsState.allPermissionsGranted) {
                showCamera(barCodeLauncher)
            } else {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
            resetQrScannerTrigger()
        }
    }
}


@Composable
private fun NftInfoCard(
    nft: NFT,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = primaryColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // NFT Thumbnail
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(nft.thumbnailUrl ?: nft.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = nft.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        // NFT Details
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = nft.name,
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = nft.collectionName,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenWhite.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Token ID
                Text(
                    text = "#${nft.tokenId.take(8)}${if (nft.tokenId.length > 8) "..." else ""}",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenWhite.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp
                    )
                )

                // Floor price if available
                nft.floorPriceEth?.let { price ->
                    Text(
                        text = "Floor: ${String.format("%.4f", price)} ETH",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = primaryColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Preview(device = "spec:width=720px,height=720px,dpi=240")
@Composable
fun PreviewSendNftScreen() {
    val nft = NFT(
        contractAddress = "0x1234567890abcdef",
        tokenId = "1234",
        chainId = 1,
        name = "Bored Ape #1234",
        description = "A bored ape from the yacht club",
        imageUrl = null,
        thumbnailUrl = null,
        collectionName = "Bored Ape Yacht Club",
        tokenType = NftTokenType.ERC721,
        floorPriceEth = 12.5,
        floorPriceUsd = 25000.0
    )

    SendNftScreen(
        nft = nft,
        recipientUiState = RecipientUiState(),
        transactionStatus = null,
        qrScannerTriggered = false,
        shouldDismissKeyboard = false,
        onRecipientChange = {},
        onSendNft = {},
        clearTransactionStatus = {},
        resetQrScannerTrigger = {},
        onKeyboardDismissed = {},
        onBackClick = {}
    )
}
