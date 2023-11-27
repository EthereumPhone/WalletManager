package com.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectedNetworkButton(
    chainId: Int,
    onClickChange: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {

    var color = when(chainId){
        1 -> Color(0xFF32CD32)
        5 -> Color(0xFFFAF9F6)
        137 -> Color(0xFF442fb2)//Polygon
        10 -> Color(0xFFc82e31)//Optimum
        42161 -> Color(0xFF2b88b8)//Arbitrum
        else -> {
            Color(0xFF030303)
        }
    }
    var network = when(chainId){
        1 -> "Mainnet"
                                5 -> "Görli"
                                10 -> "Optimism"
                                137 -> "Polygon"
                                42161 -> "Arbitrum"
                                else -> "Select Network"
    }
        Button(
            onClick = onClickChange,
            //modifier= modifier.height(64.dp),
            contentPadding = PaddingValues(14.dp, 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            shape = CircleShape,
            //elevation = ButtonDefaults.elevation(0.dp, 0.dp),
            //modifier = modifier.padding(14.dp, 0.dp),

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ){
                Box(
                    modifier = modifier
                        .clip(CircleShape)
                        .size(18.dp)
                        .background(color)
                ){}
                Text(
                    text = network,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }


}


@Preview
@Composable
fun PreviewSelectedNetworkButton() {

    var id by remember {mutableStateOf(1) }
    //val tmp by remember {mutableStateOf(false) }

        SelectedNetworkButton(
            chainId = id,
            onClickChange = {
                id++
                if (id==3){
                    id=1
                }


            }
        )
}