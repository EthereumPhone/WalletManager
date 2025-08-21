package com.feature.send.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.util.SpaceMono
import com.core.ui.util.lazerCore
import com.feature.send.R
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape

@Composable
fun SelectableCarousel(
    modifier: Modifier = Modifier,
    items: List<String>,
    itemWidth: Dp = 200.dp,
    itemHeight: Dp = 150.dp,
    primaryColor: Color,
    secondaryColor: Color,
    initialSelectedIndex: Int? = null,
    onItemSelected: (index: Int?) -> Unit
) {
    // State to track currently selected index; null means none selected
    // For single item or multiple items, ensure one is always selected
    var selectedIndex by remember { 
        mutableStateOf(
            when {
                items.size == 1 -> 0  // Single item is always selected
                initialSelectedIndex != null -> initialSelectedIndex
                items.isNotEmpty() -> 0  // Default to first item if multiple items
                else -> null
            }
        )
    }

    // Notify initial selection if provided
    LaunchedEffect(initialSelectedIndex, items.size) {
        val finalSelectedIndex = when {
            items.size == 1 -> 0  // Single item is always selected
            initialSelectedIndex != null -> initialSelectedIndex
            items.isNotEmpty() -> selectedIndex ?: 0  // Ensure something is selected for multiple items
            else -> null
        }
        if (selectedIndex != finalSelectedIndex) {
            selectedIndex = finalSelectedIndex
        }
        onItemSelected(selectedIndex)
    }

    val listState = rememberLazyListState()

    Box(modifier.fillMaxWidth()){
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            state = listState,
            flingBehavior = remember {
                object : FlingBehavior {
                    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                        return 0f
                    }
                }
            },
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
//            item {
//                Spacer(Modifier.height(80.dp).width(1.dp))
//            }
            itemsIndexed(items) { index, item ->
                val isSelected = selectedIndex == index

                Card(
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) primaryColor
                        else secondaryColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .width(itemWidth)
                        .height(itemHeight)
                        .clickable {
                            // If only one item, keep it selected (can't unselect)
                            // If multiple items, always keep one selected
                            if (items.size == 1) {
                                // Single item stays selected
                                selectedIndex = 0
                            } else if (items.size > 1) {
                                // Multiple items: can switch between them but always keep one selected
                                selectedIndex = index
                            }
                            onItemSelected(selectedIndex)
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val imageModifier = if (primaryColor == lazerCore) {
                                Modifier
                                    .size(34.dp)
                                    .border(1.dp, secondaryColor, CircleShape)
                            } else {
                                Modifier.size(34.dp)
                            }
                            when(item) {
                                "base" -> {
                                    // Use special modifier for base_square to maintain its shape
                                    // Using 2.dp corner radius to match the subtle rounding of the base_square icon
                                    val baseModifier = if (primaryColor == lazerCore) {
                                        Modifier
                                            .size(34.dp)
                                            .border(1.dp, secondaryColor, RoundedCornerShape(2.dp))
                                    } else {
                                        Modifier.size(34.dp)
                                    }
                                    Image(
                                        modifier = baseModifier,
                                        painter = painterResource(R.drawable.base_square),
                                        contentDescription = "Base"
                                    )
                                }
                                "main" -> {
                                    Image(
                                        modifier = imageModifier,
                                        painter = painterResource(R.drawable.mainnet),
                                        contentDescription = "Mainnet"
                                    )
                                }
                                "zora" -> {
                                    Image(
                                        modifier = imageModifier,
                                        painter = painterResource(id = R.drawable.zorb),
                                        contentDescription = "Zorb"
                                    )
                                }
                                "op" -> {
                                    
                                    Image(
                                        modifier = imageModifier,
                                        painter = painterResource(id = R.drawable.optimism),
                                        contentDescription = "Optimism"
                                    )
                                }
                                "arb" -> {
                                    Image(
                                        modifier = imageModifier,
                                        painter = painterResource(id = R.drawable.arbitrum),
                                        contentDescription = "Optimism"
                                    )
                                }
                                "pol" -> {
                                    Image(
                                        modifier = imageModifier,
                                        painter = painterResource(id = R.drawable.polygon),
                                        contentDescription = "Optimism"
                                    )
                                }
                                else -> ""
                            }
                            Text(
                                text = item.uppercase(),
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                color = if (isSelected) secondaryColor
                                else primaryColor
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(70.dp).width(30.dp))
            }
        }
//        Spacer(Modifier.align(Alignment.CenterStart).height(80.dp).width(8.dp).background(
//            Brush.horizontalGradient(
//            listOf(dgenBlack, Color.Transparent)
//        )))
//        Spacer(Modifier.align(Alignment.CenterEnd).height(80.dp).width(24.dp).background(
//            Brush.horizontalGradient(
//            listOf(Color.Transparent, dgenBlack)
//        )))
    }

}

@Preview(showBackground = true)
@Composable
fun SelectableCarouselPreview() {
    // Sample list
    val sampleItems = listOf("Card A", "Card B", "Card C", "Card D")

    // Preview state holder
    var selected by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Selected: ${selected?.let { sampleItems[it] } ?: "None"}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SelectableCarousel(
            items = sampleItems,
            initialSelectedIndex = 0,
            onItemSelected = { index -> selected = index }, primaryColor =  Color.Red, secondaryColor = Color.Blue
        )
    }
}
