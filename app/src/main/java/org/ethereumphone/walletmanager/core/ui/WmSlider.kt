package org.ethereumphone.walletmanager.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.math.RoundingMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WmSlider() {
    var sliderPosition by remember { mutableStateOf(0f) }
    Slider(
        value = sliderPosition,
        onValueChange = {
            // Round to nearest 0.5 step
            sliderPosition = (it * 2).roundToInt() / 2f
        },
        valueRange = 0f.. 5f,
        steps = 10,
        colors = WmSliderDefaults.colors(),
        thumb = {
            Text(
                text = "${sliderPosition}%",
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
                    .padding(5.dp)
            )
        }
    )
}

object WmSliderDefaults {
    @Composable
    fun colors() = SliderDefaults.colors(
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
}



@Preview
@Composable
fun PreviewWmSlider() {
    WmSlider()
}