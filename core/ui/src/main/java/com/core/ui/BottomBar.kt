package com.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenTurqoise
import com.core.ui.util.mediumEnterDuration
import com.core.ui.util.mediumExitDuration
import com.core.ui.util.SystemColorManager


@Composable
fun BottomBar(
    hasTransfer: Boolean,
    navigateToLog: () -> Unit,
    navigateToReceive: () -> Unit,
    navigateToBuy: () -> Unit,
    navigateToPayMaster: () -> Unit,
    primaryColor: Color
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
                primaryColor = primaryColor,
                onClick = navigateToLog,
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.baseline_swap_vert_24),
                        contentDescription = "Back",
                        tint = primaryColor
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
            primaryColor = primaryColor,
            onClick = navigateToReceive,
            icon = {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(180f),
                    imageVector = Icons.Outlined.ArrowOutward,
                    contentDescription = "Back",
                    tint = primaryColor
                )
            },
            text = "Receive"
        )
        Spacer(modifier = Modifier.width(8.dp))
        BottomBarButton(
            modifier = Modifier.testTag(UiTestTags.SWAP_BUTTON),
            primaryColor = primaryColor,
            onClick = navigateToBuy,
            icon = {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(90f),
                    painter = painterResource(R.drawable.baseline_swap_vert_24),
                    contentDescription = "Swap",
                    tint = primaryColor
                )
            },
            text = "Swap"
        )

        Spacer(modifier = Modifier.width(8.dp))
        BottomBarButton(
            primaryColor = primaryColor,
            onClick = navigateToPayMaster,
            icon = {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.gas_icon),
                        contentDescription = "Back",
                        tint = primaryColor
                    )
                }

            },
            text = "Gas"
        )
    }
}


