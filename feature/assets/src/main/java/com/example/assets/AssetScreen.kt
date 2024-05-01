package com.example.assets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.example.assets.ui.AssetListItem
import com.example.assets.ui.LoadingAssetListItem
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.core.ethOSTabRow
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.text.DecimalFormat
import com.core.ui.R
import com.example.assets.ui.AssetDialog

@Composable
fun AssetRoute(
    modifier: Modifier = Modifier,
    navigateToAssetDetail: (String) -> Unit,
    viewModel: AssetViewModel = hiltViewModel(),
) {
    val assetsUiState: AssetUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val userData: WalletDataUiState by viewModel.userData.collectAsStateWithLifecycle()
    val refreshState: Boolean by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val exclusionList: List<String> by viewModel.exclusionList.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    AssetScreen(
        assetsUiState = assetsUiState,
        refreshState = refreshState,
        onRefresh = viewModel::refreshData,
        navigateToAssetDetail = navigateToAssetDetail,
        addToExlustion = viewModel::addToExclusionList,
        removeFromExlustion = viewModel::removeFromExclusionList,
        userData = userData,
        exclusionList = exclusionList,
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun AssetScreen(
    modifier: Modifier = Modifier,
    assetsUiState: AssetUiState,
    refreshState: Boolean,
    onRefresh: () -> Unit,
    userData: WalletDataUiState,
    exclusionList: List<String>,
    addToExlustion: (String) -> Unit,
    removeFromExlustion: (String) -> Unit,
    navigateToAssetDetail: (String) -> Unit,
) {



    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshState,
        onRefresh = {
            onRefresh()
        }
    )

    var assetToHide by remember { mutableStateOf("") }//Asset address
    val expandAssetDialog = remember { mutableStateOf(false) }
    var hideOrUnhide = remember { mutableStateOf(true) }

    if (expandAssetDialog.value){
        AssetDialog(
            expanded = expandAssetDialog,
            title = "${if(hideOrUnhide.value) "Mark" else "Remove"} ${assetToHide.uppercase()} as Spam",
            btntext = "${if(hideOrUnhide.value) "Add ${assetToHide.uppercase()} to Spam" else "Remove ${assetToHide.uppercase()}"} ",
            subtext = "With this action your ${assetToHide.uppercase()} will be ${if(hideOrUnhide.value) "moved to the spam list." else "removed from the spam list."}"
        ) {
            if (hideOrUnhide.value){
                addToExlustion(assetToHide)
            } else {
                removeFromExlustion(assetToHide)
            }

            expandAssetDialog.value = false
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(start = 32.dp, end = 32.dp, bottom = 32.dp)
    ) {
        ethOSHeader(title = "Assets")
        Spacer(modifier = Modifier.height(32.dp))

        when(assetsUiState){
            is AssetUiState.Loading -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        modifier = modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()
                        LoadingAssetListItem()

                    }
//                    Text(text = "Loading...", fontFamily = Fonts.INTER, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                }
            }
            is AssetUiState.Empty -> {
                
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
                            painter = painterResource(id = R.drawable.no_assets),
                            contentDescription = null
                        )
                        Text(
                            text = "No assets",
                            fontFamily = Fonts.INTER,
                            color = Colors.GRAY,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            is AssetUiState.Error -> {
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
                            painter = painterResource(id = R.drawable.baseline_error_outline_24),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Colors.GRAY)
                        )
                        Text(text = "Error", fontFamily = Fonts.INTER, color = Colors.GRAY, fontSize = 24.sp, fontWeight = FontWeight.Medium)

                    }
                }
            }
            is AssetUiState.Success -> {
                val pagelist = listOf("All","Spam")
                val pagerState = rememberPagerState(pageCount = {
                    pagelist.size
                })
                val coroutineScope = rememberCoroutineScope()
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ){

                    //TODO: Hide Assets
                    ethOSTabRow(
                        items = pagelist,
                        selectedItemIndex = pagerState.currentPage,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(it)
                            }

                        },
                        tabWidth= 80.dp
                    )
                    Spacer(modifier = Modifier.height(32.dp))


                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) { page ->
                        // You can customize the content of each page based on 'page'
                        when (page) {
                            0 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pullRefresh(pullRefreshState)
                                ) {

                                    val filteredlist = assetsUiState.assets.filterNot { it.key in exclusionList }

                                    LazyColumn(

                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        filteredlist.forEach {
                                            item(it.key) {
                                                AssetListItem(
                                                    title = it.key,
                                                    assets = it.value,
                                                    longClick = {
                                                        assetToHide = it.key
                                                        hideOrUnhide.value = true
                                                        expandAssetDialog.value = true
                                                    },
                                                    linkTo = {
                                                        navigateToAssetDetail(it.key)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    PullRefreshIndicator(
                                        refreshing = refreshState,
                                        state = pullRefreshState,
                                        modifier = Modifier.align(Alignment.TopCenter)
                                    )


                                }
                            }
                            1 -> { //Spam
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pullRefresh(pullRefreshState)
                                ) {

                                    val filteredlist = assetsUiState.assets.filter { it.key in exclusionList }
                                    if(filteredlist.isNotEmpty()){
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            filteredlist.forEach {
                                                item(it.key) {
                                                    AssetListItem(
                                                        title = it.key,
                                                        assets = it.value,
                                                        longClick = {
                                                            assetToHide = it.key
                                                            hideOrUnhide.value = false
                                                            expandAssetDialog.value = true
                                                        },
                                                        linkTo = {
                                                            navigateToAssetDetail(it.key)
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        PullRefreshIndicator(
                                            refreshing = refreshState,
                                            state = pullRefreshState,
                                            modifier = Modifier.align(Alignment.TopCenter)
                                        )
                                    }else{
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Image(
                                                modifier = Modifier.size(82.dp),
                                                contentScale = ContentScale.Fit,
                                                painter = painterResource(id = R.drawable.hidden_assets),
                                                contentDescription = null
                                            )
                                            Text(text = "No hidden assets", fontFamily = Fonts.INTER, color = Colors.GRAY, fontSize = 24.sp, fontWeight = FontWeight.Medium)

                                        }
                                    }


                                }
                            }

                        }
                    }
                }



            }
        }
    }
}


@Composable
@Preview
fun PreviewAssetScreen(){
    val testData = mapOf("ethereum" to listOf(
        TokenAsset(
            address = "sdfrthjkl",
            chainId = 1,
            symbol = "eth",
            name = "ethereum",
            logoUrl = "https://www.deviantart.com/jukeboxfromao/art/bruh-839511181",
            balance = 1.2,
        )
    )
    )


//    AssetScreen(
//        assetsUiState = AssetUiState.Success(testData),
//        refreshState = false,
//        onRefresh = {},
//        navigateToAssetDetail ={},
//        setHiddenAssets = {}
////        toAssetDetail= {
////            CurrentState(
////                address = "",
////                symbol = "ETH",
////                name = "assetName",
////                balance = 0.0,
////                assets = emptyList()
////            )
////        }
//    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun PreviewAssetTabScreen(){
    val pagelist = listOf("All", "Hidden","Spam")
    val pagerState = rememberPagerState(pageCount = {
        pagelist.size
    })

    val coroutineScope = rememberCoroutineScope()
    Column {
        // Buttons for navigating pages
//        Button(onClick = {
//            coroutineScope.launch {
//                pagerState.animateScrollToPage(0) }
//            }
//        ) {
//            Text(text = "Go to Page 1")
//        }
//        Button(onClick = {
//            coroutineScope.launch {
//                pagerState.animateScrollToPage(1) }
//        }
//        ) {
//            Text(text = "Go to Page 2")
//        }

//        ethOSTabRow(
//            items = pagelist,
//            selectedItemIndex = pagerState.currentPage,
//            onClick = {
//                coroutineScope.launch {
//                    pagerState.animateScrollToPage(it)
//                }
//
//            },
//        )


        // HorizontalPager with 2 pages
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) { page ->
            // You can customize the content of each page based on 'page'
            when (page) {
                0 -> Text("Page 1 Content")
                1 -> Text("Page 2 Content")
                2 -> Text("Page 3 Content")
            }
        }
    }
}



@Composable
@Preview
fun ethOSTabRowPreview() {
    val (selected, setSelected) = remember {
        mutableStateOf(0)
    }

    ethOSTabRow(
        items = listOf("All", "Hidden","Spam"),
        selectedItemIndex = selected,
        onClick = setSelected,
    )
}