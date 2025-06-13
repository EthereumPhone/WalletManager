package com.feature.home.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.core.ui.DgenLoadingMatrix

@Composable
fun LoadingHomeScreen(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
){
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        DgenLoadingMatrix(
            unactiveLEDColor = secondaryColor,
            activeLEDColor = primaryColor
        )
    }
}