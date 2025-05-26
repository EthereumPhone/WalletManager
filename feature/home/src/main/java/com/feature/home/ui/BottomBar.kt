package com.feature.home.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.BottomBarButton
import com.core.ui.showCustomToast
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.example.dgenlibrary.ui.theme.mediumExitDuration
import com.feature.home.R

@Composable
fun BottomBar(
    hasTransfer: Boolean,
    navigateToLog: () -> Unit,
    navigateToReceive: () -> Unit,
    navigateToBuy: () -> Unit,
    navigateToPayMaster: () -> Unit,
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(dgenBlack)
            .padding(start=8.dp, end=8.dp, top=8.dp),
        horizontalArrangement = Arrangement.Center
    ) {

        AnimatedVisibility(
            hasTransfer,
            enter = fadeIn(
                animationSpec = tween(mediumEnterDuration,easing= FastOutSlowInEasing)
            ),
            exit = fadeOut(
                animationSpec = tween(mediumExitDuration,easing= FastOutSlowInEasing)
            )
        ){
            BottomBarButton(
                onClick = navigateToLog,
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.baseline_swap_vert_24),
                        contentDescription = "Back",
                        tint = dgenTurqoise
                    )
                },
                text = "Log"
            )

        }
        AnimatedVisibility(
            hasTransfer,
            enter = fadeIn(
                animationSpec = tween(mediumEnterDuration,easing= FastOutSlowInEasing)
            ),
            exit = fadeOut(
                animationSpec = tween(mediumExitDuration,easing= FastOutSlowInEasing)
            )
        ){
            Spacer(modifier = Modifier.width(8.dp))
        }

        BottomBarButton(
            onClick = navigateToBuy,
            icon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Back",
                    tint = dgenTurqoise
                )
            },
            text = "Buy"
        )
        Spacer(modifier = Modifier.width(8.dp))
        BottomBarButton(
            onClick = navigateToReceive,
            icon = {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(180f),
                    imageVector = Icons.Outlined.ArrowOutward,
                    contentDescription = "Back",
                    tint = dgenTurqoise
                )
            },
            text = "Receive"
        )

        Spacer(modifier = Modifier.width(8.dp))
        BottomBarButton(
            onClick = navigateToPayMaster,
            icon = {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.gas_icon),
                        contentDescription = "Back",
                        tint = dgenTurqoise
                    )
                }

            },
            text = "Gas"
        )
    }
}


