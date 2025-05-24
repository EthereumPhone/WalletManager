package com.feature.paymaster

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.showCustomToast
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise

@Composable
internal fun PayMasterScreenRoute(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    PayMasterScreen(
        onBackClick = onBackClick
    )
}

@Composable
fun PayMasterScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
){

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(dgenBlack)
        ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    end = 24.dp,
                    start = 24.dp, top = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = "GAS",
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )

            Icon(
                modifier = Modifier.size(32.dp).pointerInput(Unit) {
                    detectTapGestures {
                        onBackClick()
                    }
                },
                painter = painterResource(R.drawable.baseline_close_24),
                contentDescription = "Back",
                tint = dgenTurqoise
            )

        }

    }
}

@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun PayMasterScreenPreview(){
    PayMasterScreen(onBackClick = {})
}