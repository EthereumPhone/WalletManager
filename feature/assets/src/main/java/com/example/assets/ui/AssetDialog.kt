package com.example.assets.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetDialog(
    expanded: MutableState<Boolean>,
    title: String,
    subtext: String,
    btntext: String,
    onClick: () -> Unit = {}

){
    Dialog(
        onDismissRequest = { expanded.value = false },
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Colors.BLACK,
            contentColor = Colors.WHITE,
            elevation = 20.dp,
            border = BorderStroke(width = 1.dp, Colors.WHITE)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(36.dp)

                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = TextStyle(
                                fontSize = 24.sp,
                                color = Colors.WHITE,
                                fontFamily = Fonts.INTER,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subtext,
                            style = TextStyle(
                                fontSize = 18.sp,
                                color = Colors.GRAY,
                                fontFamily = Fonts.INTER,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            onClick = onClick,
                            shape = CircleShape,
                            colors =  ButtonDefaults.buttonColors(
                                containerColor = Colors.WHITE,
                                contentColor = Colors.BLACK,
                                disabledContainerColor=Colors.TRANSPARENT ,
                                disabledContentColor=Colors.GRAY
                            ),
                            elevation =  ButtonDefaults.buttonElevation(
                                defaultElevation = 5.dp,
                                pressedElevation = 0.dp,
                                focusedElevation = 2.dp,
                                hoveredElevation = 5.dp,
                                disabledElevation = 0.dp
                            )

                            ) {
                                Text(
                                    text = btntext,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    )
                        }


                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            onClick = { expanded.value = false },
                            shape = CircleShape,
                            colors =  ButtonDefaults.buttonColors(
                                containerColor = Colors.TRANSPARENT,
                            contentColor = Colors.WHITE,
                            disabledContainerColor = Colors.TRANSPARENT ,
                            disabledContentColor = Colors.GRAY
                        ),
                        elevation =  ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            focusedElevation = 2.dp,
                            hoveredElevation = 5.dp,
                            disabledElevation = 0.dp
                        ),

                        ) {
                        Text(
                            text = "Cancel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,

                            )
                    }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewAssetDialog(){
    AssetDialog(
        expanded = mutableStateOf(true),
        title = "Hide USDC",
        btntext = "Hide your USDC",
        subtext = "With this action your USDC will be hidden from assets."
    )
}
