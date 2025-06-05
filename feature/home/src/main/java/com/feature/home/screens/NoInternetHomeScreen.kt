package com.feature.home.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.core.ui.R
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenGunMetal
import com.example.dgenlibrary.ui.theme.dgenTurqoise

@Composable
fun NoInternetHomeScreen(
    modifier: Modifier = Modifier,
    gifEnabledLoader: ImageLoader
){
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.offset(y= (-48).dp)
        ) {
            AsyncImage(
                imageLoader = gifEnabledLoader,
                model = R.drawable.wireframe_torus,
                contentDescription = null,
                modifier = Modifier.size(275.dp),
                colorFilter = ColorFilter.tint(dgenGunMetal)

            )

            Text(
                text = "Connect to the internet see your tokens.",
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenGunMetal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.width(300.dp)
            )
        }
    }
}