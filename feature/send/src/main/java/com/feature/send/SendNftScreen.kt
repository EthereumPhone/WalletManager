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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.core.ui.DetailItem
import com.core.ui.SwipeButton
import com.core.ui.getChainName
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenWhite
import com.core.ui.util.pulseOpacity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.core.data.model.dto.Contact
import com.core.ui.util.mediumEnterDuration
import com.feature.send.ui.ContactPickerOverlay
import com.feature.send.ui.NftImageOverlay
import com.feature.send.ui.RecipientSection
import com.feature.send.ui.SendHeader
import com.feature.send.ui.TransactionStatus
import com.feature.send.ui.TransactionStatusOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import android.util.Log
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.core.ui.util.body1_fontSize
import com.core.ui.util.button_fontSize
import com.core.ui.util.label_fontSize
import com.core.ui.util.neonOpacity
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
    val selectedContact by viewModel.selectedContact.collectAsStateWithLifecycle()
    val contactsWithEth by viewModel.contactsWithEth.collectAsStateWithLifecycle()
    val shouldRequestContactsPermission by viewModel.shouldRequestContactsPermission.collectAsStateWithLifecycle()

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
        selectedContact = selectedContact,
        contactsWithEth = contactsWithEth,
        shouldRequestContactsPermission = shouldRequestContactsPermission,
        onRecipientChange = viewModel::updateAddress,
        onContactSelected = viewModel::selectContact,
        onClearContact = viewModel::clearSelectedContact,
        onContactIconClick = viewModel::onContactIconClick,
        onContactsPermissionResult = viewModel::onContactsPermissionResult,
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
    selectedContact: Contact? = null,
    contactsWithEth: List<Contact> = emptyList(),
    shouldRequestContactsPermission: Boolean = false,
    onRecipientChange: (String) -> Unit,
    onContactSelected: (Contact) -> Unit = {},
    onClearContact: () -> Unit = {},
    onContactIconClick: () -> Unit = {},
    onContactsPermissionResult: (Boolean) -> Unit = {},
    onSendNft: () -> Unit,
    clearTransactionStatus: () -> Unit,
    resetQrScannerTrigger: () -> Unit,
    onKeyboardDismissed: () -> Unit,
    onBackClick: () -> Unit
) {
    // State for showing contact picker
    var showContactPicker by remember { mutableStateOf(false) }
    
    // State for showing NFT image overlay
    var showNftImageOverlay by remember { mutableStateOf(false) }

    // Handle contacts permission request
    val contactsPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.READ_CONTACTS)
    )

    LaunchedEffect(shouldRequestContactsPermission) {
        if (shouldRequestContactsPermission) {
            contactsPermissionState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(contactsPermissionState.allPermissionsGranted) {
        if (contactsPermissionState.allPermissionsGranted && shouldRequestContactsPermission) {
            onContactsPermissionResult(true)
            showContactPicker = true
        } else if (!contactsPermissionState.allPermissionsGranted &&
            contactsPermissionState.shouldShowRationale) {
            onContactsPermissionResult(false)
        }
    }
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
    

        val configuration = LocalConfiguration.current
        val horizontalPadding = (configuration.screenWidthDp * 0.06f).dp
        val bottomPadding = (configuration.screenHeightDp * 0.04f).dp
        val topPadding = (configuration.screenHeightDp * 0.015f).dp

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = horizontalPadding, end = horizontalPadding, bottom = bottomPadding, top = topPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
                // Header - using the same SendHeader pattern as SendScreen
                SendHeader(
                    modifier = Modifier.padding(bottom = 0.dp),
                    nftName = nft?.name ?: "NFT",
                    nftImageUrl = nft?.thumbnailUrl ?: nft?.imageUrl,
                    onBackClick = onBackClick
                )

                Column(
                    Modifier.fillMaxWidth()
                ){
                    // NFT Info Card
                    if (nft != null) {
                        // NFT Image and Basic Info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        )
                        {
                            // NFT Thumbnail - Clickable to open fullscreen view
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(8.dp))
//
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(nft.thumbnailUrl ?: nft.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = nft.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(modifier = Modifier.fillMaxSize()
                                    .background(dgenBlack.copy(0.25f))
                                    .clickable{ showNftImageOverlay = true }
                                ){

                                    Icon(
                                        modifier = Modifier.size(40.dp).align(Alignment.Center),
                                        painter = painterResource(R.drawable.expand_content),
                                        contentDescription = "Back",
                                        tint = primaryColor
                                    )
                                }
                            }

                            // NFT Name and Collection
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                {
                                    Text(
                                        text = nft.name,
                                        style = TextStyle(
                                            fontFamily = SpaceMono,
                                            color = primaryColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = body1_fontSize
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = nft.collectionName,
                                        style = TextStyle(
                                            fontFamily = PitagonsSans,
                                            color = dgenWhite,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = button_fontSize
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.fillMaxWidth().height(4.dp))
                                    // Token ID
                                    Text(
                                        text = "#${nft.tokenId.take(8)}${if (nft.tokenId.length > 8) "..." else ""}",
                                        style = TextStyle(
                                            fontFamily = SpaceMono,
                                            color = primaryColor.copy(neonOpacity),
                                            fontWeight = FontWeight.Light,
                                            fontSize = 16.sp
                                        )
                                    )
                                }

//                            IconButton(
//                                onClick = { showNftImageOverlay = true },
//                            ) {
//                                Box(modifier = Modifier.size(56.dp)) {
//                                    Icon(
//                                        modifier = Modifier.size(40.dp).align(Alignment.Center),
//                                        painter = painterResource(R.drawable.expand_content),
//                                        contentDescription = "Back",
//                                        tint = primaryColor
//                                    )
//                                }
//
//                            }
                            }

                        }

                        // DetailItem(
                        //     label = "Network",
                        //     value = getChainName(nft.chainId),
                        //     primaryColor = primaryColor
                        // )

                        // Floor Price
                        val floorPrice = nft.floorPriceEth
                        val floorPriceText = if (floorPrice != null && floorPrice > 0) {
                            String.format("%.4f ETH", floorPrice)
                        } else {
                            "---"
                        }

                        DetailItem(
                            label = "Floor Price",
                            value = floorPriceText,
                            primaryColor = primaryColor
                        )


                    }
                }




                // #region agent log
                Log.d("DEBUG_AGENT", "SendNftScreen:RecipientSection - hasContactsWithEth=${contactsWithEth.isNotEmpty()}, selectedContact=${selectedContact?.name ?: "null"}, hypothesisId=A")
                // #endregion

                // Recipient Section
                RecipientSection(
                    recipientUiState = recipientUiState,
                    selectedContact = selectedContact,
                    hasContactsWithEth = contactsWithEth.isNotEmpty(),
                    onContentChanged = onRecipientChange,
                    onContactIconClick = {
                        onContactIconClick()
                        if (contactsWithEth.isNotEmpty()) {
                            showContactPicker = true
                        }
                    },
                    onClearContact = onClearContact,
                    shouldDismissKeyboard = shouldDismissKeyboard,
                    onKeyboardDismissed = onKeyboardDismissed
                )
        }

        // Transaction Status Overlay
        TransactionStatusOverlay(
            status = transactionStatus,
            gifLoader = gifEnabledLoader,
            onDismiss = { clearTransactionStatus() },
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )

        // Contact picker overlay with fade in/out
        AnimatedVisibility(
            visible = showContactPicker,
            enter = fadeIn(animationSpec = tween(durationMillis = mediumEnterDuration)),
            exit = fadeOut(animationSpec = tween(durationMillis = mediumEnterDuration))
        ) {
            ContactPickerOverlay(
                contacts = contactsWithEth,
                onContactSelected = { contact ->
                    onContactSelected(contact)
                    showContactPicker = false
                },
                onDismiss = { showContactPicker = false }
            )
        }
        
        // NFT Image Overlay with zoom and pan
        NftImageOverlay(
            visible = showNftImageOverlay,
            imageUrl = nft?.imageUrl ?: nft?.thumbnailUrl,
            onDismiss = { showNftImageOverlay = false }
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
        selectedContact = null,
        contactsWithEth = emptyList(),
        onRecipientChange = {},
        onContactSelected = {},
        onClearContact = {},
        onContactIconClick = {},
        onContactsPermissionResult = {},
        onSendNft = {},
        clearTransactionStatus = {},
        resetQrScannerTrigger = {},
        onKeyboardDismissed = {},
        onBackClick = {}
    )
}
