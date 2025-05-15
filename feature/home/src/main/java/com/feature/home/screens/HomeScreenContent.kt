package com.feature.home.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.core.ui.DgenLoadingMatrix

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    areAssetsVisible: Boolean,
    primaryContent:  @Composable () -> Unit,
    secondaryContent:  @Composable () -> Unit,
){
    Crossfade(
        modifier = modifier.fillMaxSize(),
        targetState = areAssetsVisible,
        animationSpec = tween(300)
    ){ showCarousel ->
        if (showCarousel){
            //Box(modifier = Modifier.fillMaxSize()) {
            primaryContent()
            //}
        } else {
            secondaryContent()
        }
    }
}