package com.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feature.home.AssetUiState

@Composable
fun AssetList(assetsUiState: AssetUiState) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
    ) {
        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(text = "Overview", color = Color(0xFF9FA2A5), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            when(assetsUiState){
                is AssetUiState.Empty ->{
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center){
                        Text(text = "No assets", color = Color(0xFF9FA2A5), fontSize = 20.sp, fontWeight = FontWeight.Medium)

                    }
                }
                is AssetUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center){
                        Text(text = "Loading...", color = Color(0xFF9FA2A5), fontSize = 20.sp, fontWeight = FontWeight.Medium)

                    }
                }
                is AssetUiState.Success -> {
                    val assets = assetsUiState.assets
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        assets.forEach { item ->
                            item(key = item.address) {

                                AssetListItem(title = item.symbol, value = item.balance)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                is AssetUiState.Error -> { }
            }
        }
    }
}

fun formatString(input: String): String {
    return input.uppercase()
}