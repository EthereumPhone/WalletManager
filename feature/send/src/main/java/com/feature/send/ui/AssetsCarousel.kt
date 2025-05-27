package com.feature.send.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body1_fontSize
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.feature.send.R

@Composable
fun SelectableCarousel(
    items: List<String>,
    itemWidth: Dp = 200.dp,
    itemHeight: Dp = 150.dp,
    initialSelectedIndex: Int? = null,
    onItemSelected: (index: Int?) -> Unit
) {
    // State to track currently selected index; null means none selected
    var selectedIndex by remember { mutableStateOf(initialSelectedIndex) }

    // Notify initial selection if provided
    LaunchedEffect(initialSelectedIndex) {
        onItemSelected(selectedIndex)
    }

    Box(Modifier.fillMaxWidth()){
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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
                        containerColor = if (isSelected) dgenTurqoise
                        else dgenOcean
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .width(itemWidth)
                        .height(itemHeight)
                        .clickable {
                            selectedIndex = if (isSelected) null else index
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
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            when(item) {
                                "base" -> {
                                    Image(
                                        modifier = Modifier
                                            .size(32.dp),
                                        painter = painterResource(R.drawable.base),
                                        contentDescription = "Base"
                                    )
                                }
                                "mainnet" -> {
                                    Image(
                                        modifier = Modifier
                                            .size(32.dp),
                                        painter = painterResource(R.drawable.mainnet),
                                        contentDescription = "Mainnet"
                                    )
                                }
                                "zora" -> {
                                    Image(
                                        modifier = Modifier
                                            .size(32.dp),
                                        painter = painterResource(id = R.drawable.zorb),
                                        contentDescription = "Zorb"
                                    )
                                }
                                "optimism" -> {
                                    Image(
                                        modifier = Modifier
                                            .size(32.dp),
                                        painter = painterResource(id = R.drawable.optimism),
                                        contentDescription = "Optimism"
                                    )
                                }
                                "arbitrum" -> {
                                    Image(
                                        modifier = Modifier.size(32.dp),
                                        painter = painterResource(id = R.drawable.arbitrum),
                                        contentDescription = "Optimism"
                                    )
                                }
                                "polygon" -> {
                                    Image(
                                        modifier = Modifier.size(32.dp),
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
                                color = if (isSelected) dgenOcean
                                else dgenTurqoise
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
            onItemSelected = { index -> selected = index }
        )
    }
}
