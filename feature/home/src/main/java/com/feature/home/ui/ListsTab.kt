package com.feature.home.ui

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListsTab(

) {
    val tabItems = TabItems.values().toList()

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                wmTabIndicator(tabPositions, pagerState)
            }
        ) {
            tabItems.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.zIndex(2f),
                    text = {
                           Text(
                               text = title.name,
                               color = Color.Red
                           )
                    },
                    selected = index == pagerState.currentPage,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }

        HorizontalPager(
            pageCount = tabItems.size,
            state = pagerState
        ) { page ->
            Box(Modifier.fillMaxSize()) {

                Text(modifier = Modifier.align(Alignment.Center), text = "Page $page")
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun wmTabIndicator(
    tabPositions: List<TabPosition>,
    pagerState: PagerState
) {
    val transition = updateTransition(pagerState.currentPage, label = "")
    val springSpec: SpringSpec<Dp> = spring(dampingRatio = 1f, stiffness = 150f)

    val indicatorStart by transition.animateDp(
        transitionSpec = { springSpec },
        label = "",
        targetValueByState = { tabPositions[it].left }
    )

    val indicatorEnd by transition.animateDp(
        transitionSpec = { springSpec },
        label = "",
        targetValueByState = { tabPositions[it].right }
    )

    Box(
        Modifier
            .offset(indicatorStart)
            .wrapContentSize(align = Alignment.BottomStart)
            .width(indicatorEnd - indicatorStart)
            .padding(2.dp)
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF), RoundedCornerShape(50))
            .border(BorderStroke(2.dp, Color(0xFFFFFFFF)), RoundedCornerShape(50))
            .zIndex(1f)
    )
}

enum class TabItems {
    TRANSFERS,
    ASSETS
}



@Preview
@Composable
fun ListsTabPreview() {
    ListsTab()
}