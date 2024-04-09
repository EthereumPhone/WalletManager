package com.core.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


//--
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun TextToggleButton(
    text: String,
    selected: MutableState<Boolean>,
    onClickChange: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Button(
        onClick = onClickChange,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected.value) Color.White else Color(0xFF24303D),
            contentColor = Color.White
        ),
        shape = CircleShape,
        enabled = enabled
        //elevation = ButtonDefaults.elevation(0.dp, 0.dp),

    ) {
        Row(
            modifier = modifier.padding(0.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                color = if (selected.value) Color(0xFF24303D) else Color.White,
                modifier = modifier.padding(0.dp),
            )
        }
    }
}






//@Composable
//fun ethOSTabRow(){
//    var tabIndex by remember { mutableStateOf(0) }
//
//    val tabs = listOf("Home", "About", "Settings")
//
//    Column(modifier = Modifier.fillMaxWidth()) {
////        TabRow(selectedTabIndex = tabIndex) {
////            tabs.forEachIndexed { index, title ->
////                Tab(text = { Text(title) },
////                    selected = tabIndex == index,
////                    onClick = { tabIndex = index }
////                )
////            }
////        }
////        when (tabIndex) {
////            0 -> Text("Screen 0")
////            1 -> Text("Screen 1")
////            2 -> Text("Screen 2")
////        }
//        Surface(
//            modifier = Modifier.selectableGroup(),
//            color = Color.Red,
//            contentColor = Color.White
//        ) {
//            SubcomposeLayout(Modifier.fillMaxWidth()) { constraints ->
//                val tabRowWidth = constraints.maxWidth
//                val tabMeasurables = subcompose(TabSlots.Tabs, tabs)
//                val tabCount = tabMeasurables.size
//                var tabWidth = 0
//                if (tabCount > 0) {
//                    tabWidth = (tabRowWidth / tabCount)
//                }
//                val tabRowHeight = tabMeasurables.fold(initial = 0) { max, curr ->
//                    maxOf(curr.maxIntrinsicHeight(tabWidth), max)
//                }
//
//                val tabPlaceables = tabMeasurables.map {
//                    it.measure(
//                        constraints.copy(
//                            minWidth = tabWidth,
//                            maxWidth = tabWidth,
//                            minHeight = tabRowHeight,
//                            maxHeight = tabRowHeight,
//                        )
//                    )
//                }
//
//                val tabPositions = List(tabCount) { index ->
//                    TabPosition(tabWidth.toDp() * index, tabWidth.toDp())
//                }
//
//                layout(tabRowWidth, tabRowHeight) {
//                    tabPlaceables.forEachIndexed { index, placeable ->
//                        placeable.placeRelative(index * tabWidth, 0)
//                    }
//
//                    subcompose(TabSlots.Divider, divider).forEach {
//                        val placeable = it.measure(constraints.copy(minHeight = 0))
//                        placeable.placeRelative(0, tabRowHeight - placeable.height)
//                    }
//
//                    subcompose(TabSlots.Indicator) {
//                        indicator(tabPositions)
//                    }.forEach {
//                        it.measure(Constraints.fixed(tabRowWidth, tabRowHeight)).placeRelative(0, 0)
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
private fun ethOSTabIndicator(
    indicatorWidth: Dp,
    indicatorOffset: Dp,
    indicatorColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(
                width = indicatorWidth,
            )
            .offset(
                x = indicatorOffset,
            )
            .clip(
                shape = CircleShape,
            )
            .background(
                color = indicatorColor,
            ),
    )
}

@Composable
private fun ethOSTabItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    tabWidth: Dp,
    text: String,
) {
    val tabTextColor: Color by animateColorAsState(
        targetValue = if (isSelected) {
            Colors.BLACK
        } else {
            Colors.WHITE
        },
        animationSpec = tween(easing = FastOutLinearInEasing),
    )
    Text(
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            }
            .width(tabWidth)
            .padding(
                vertical = 4.dp,
                horizontal = 8.dp,
            ),
        text = text,
        fontFamily = Fonts.INTER,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = tabTextColor,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun ethOSTabRow(
    selectedItemIndex: Int,
    items: List<String>,
    modifier: Modifier = Modifier,
    tabWidth: Dp = 100.dp,
    onClick: (index: Int) -> Unit,
) {
    val indicatorOffset: Dp by animateDpAsState(
        targetValue = tabWidth * selectedItemIndex,
        animationSpec = tween(
            easing = FastOutLinearInEasing
        ),
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Colors.TRANSPARENT)
            .border(1.dp,Colors.WHITE,CircleShape)
            .height(intrinsicSize = IntrinsicSize.Min),
    ) {
        ethOSTabIndicator(
            indicatorWidth = tabWidth,
            indicatorOffset = indicatorOffset,
            indicatorColor = Colors.WHITE,
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clip(CircleShape),
        ) {
            items.mapIndexed { index, text ->
                val isSelected = index == selectedItemIndex
                ethOSTabItem(
                    isSelected = isSelected,
                    onClick = {
                        onClick(index)
                    },
                    tabWidth = tabWidth,
                    text = text,
                )
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