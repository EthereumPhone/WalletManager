package com.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopHeader(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    title: String,
    icon: @Composable () -> Unit,
){
    Row (
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom=24.dp),
        //.background(Color.Red),
        Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .width(100.dp)
        ) {


            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Go back",
                tint = Color.White,
                modifier = modifier.clickable {
                    onBackClick()
                }
            )
//                    Text(
//                        text = "Home",
//                        color = Color.White,
//                        fontSize = 18.sp,
//
//                        )
        }

        //Header title
        Text(
            modifier = modifier.weight(1f),
            textAlign = TextAlign.Center,
            text = title,
            fontSize = 18.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )

        //Warning or info

        Box (
            contentAlignment = Alignment.CenterEnd,
            modifier = modifier
                .width(100.dp)
        ){
//            Icon(
//                imageVector = Icons.Outlined.Info,
//                contentDescription = "Information",
//                tint = Color(0xFF9FA2A5),
//                modifier = modifier
//                    .clip(CircleShape)
//                    //.background(Color.Red)
//                    .clickable {
//                        //opens InfoDialog
//                        //showDialog.value = true
//                        openOnClick
//                    }
//
//            )
            icon()
        }
    }
}