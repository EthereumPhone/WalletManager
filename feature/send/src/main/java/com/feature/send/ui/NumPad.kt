package com.example.ethoscomponents.components

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.ethoscomponents.util.IconAsText
import com.feature.send.R


@Composable
fun NumPad(
    value: String,
    contentColor: Color = NumPadDefaults.contentColor,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    val numPadButtons = NumPadButtons.values().asList()
    val buttonSize = 64.dp

    LazyVerticalGrid(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            ,
        columns = GridCells.Fixed(3)){
        itemsIndexed(numPadButtons) { index, button ->
            val fixedIndex = (index+1).mod(11)

            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    ,
                contentAlignment = Alignment.Center
            ) {
                var input = when(button) {
                    NumPadButtons.UNDO -> Icons.Rounded.Backspace
                    NumPadButtons.COMMA -> "·"
                    else -> fixedIndex.toString()
                }

                if(input is ImageVector) {
                    IconButton(
                        modifier = Modifier
                            .size(buttonSize)
                            .clip(CircleShape),
                        onClick = {
                            //if()
                        when (button) {
                            NumPadButtons.COMMA -> {
                                if (!value.contains(".")) {
                                    if (value == "") {
                                        onValueChange("0.")

                                    } else {
                                        onValueChange("$value.")

                                    }
                                }
                            }

                            NumPadButtons.UNDO -> onValueChange(value.dropLast(1))
                            else -> onValueChange(value + fixedIndex.toString())
                        }
                    },
                        enabled = enabled
                    ) {
                        Icon(
                            tint= if(enabled) Color.White else Color(0xFF9FA2A5),
                            imageVector = Icons.Rounded.Backspace,
                            contentDescription = null // decorative element
                        )
                    }
//                    Box(
//                        modifier = Modifier
//                            .size(buttonSize)
//                            .clip(CircleShape)
//                            //.background(Color(0xFF3C4958))
//                            .clickable {
//                                when (button) {
//                                    NumPadButtons.COMMA -> {
//                                        if (!value.contains(".")) {
//                                            if (value == "") {
//                                                onValueChange("0.")
//
//                                            } else {
//                                                onValueChange("$value.")
//
//                                            }
//                                        }
//                                    }
//
//                                    NumPadButtons.UNDO -> onValueChange(value.dropLast(1))
//                                    else -> onValueChange(value + fixedIndex.toString())
//                                }
//                            },
//                            //.padding(8.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            tint= Color.White,
//                            imageVector = Icons.Rounded.Backspace,
//                            contentDescription = null // decorative element
//                        )
//                    }
                }
                if(input is String) {
                    IconButton(
                        modifier = Modifier
                            .size(buttonSize)
                            .clip(CircleShape),
                        onClick = {
                            when (button) {
                                NumPadButtons.COMMA -> {
                                    if (!value.contains(".")) {
                                        if (value == "") {
                                            onValueChange("0.")
                                        } else {
                                            onValueChange("$value.")
                                        }
                                    }
                                }

                                NumPadButtons.UNDO -> onValueChange(value.dropLast(1))
                                else -> onValueChange(value + fixedIndex.toString())
                            }
                                  },
                        enabled = enabled
                    ) {
                        Text(
                            text = input,
                            fontSize = NumPadDefaults.fontSize,
                            fontWeight = FontWeight.Normal,
                            color = if(enabled) contentColor else Color(0xFF9FA2A5),
                        )
                    }
//                    Box(
//                        modifier = Modifier
//                            .size(buttonSize)
//                            .clip(CircleShape)
//                            //.background(Color(0xFF24303D))//Color(0xFF1E2730))
//                            .clickable {
//                                when (button) {
//                                    NumPadButtons.COMMA -> {
//                                        if (!value.contains(".")) {
//                                            if (value == "") {
//                                                onValueChange("0.")
//                                            } else {
//                                                onValueChange("$value.")
//                                            }
//                                        }
//                                    }
//
//                                    NumPadButtons.UNDO -> onValueChange(value.dropLast(1))
//                                    else -> onValueChange(value + fixedIndex.toString())
//                                }
//                            }
//                        ,
//                        contentAlignment = Alignment.Center
//                    ){
//                        Text(
//                            text = input,
//                            fontSize = NumPadDefaults.fontSize,
//                            fontWeight = FontWeight.Normal,
//                            color = contentColor,
//                        )
//                    }

                }
            }
        }
    }


}


object NumPadDefaults {
    val contentColor = Color.White
    val fontSize = 32.sp
}

@Preview
@Composable
fun PreviewNumPad() {
//    NumPad(
//        value = "",
//        modifier = Modifier,
//        enabled = true
//    ) {}
}