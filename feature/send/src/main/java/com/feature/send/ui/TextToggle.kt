package com.feature.send.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenTurqoise
import com.core.ui.util.smallDuration

@Composable
fun TextToggle(
    modifier: Modifier = Modifier,
    primaryStateName: String,
    secondaryStateName: String,
    primaryColor: Color,
    onToggle: () -> Unit,
    value: Boolean
){

    Crossfade(
        value,
        animationSpec = tween(smallDuration),
        modifier = modifier
    ) { on ->
        if(on){
            Text(
                buildAnnotatedString {
                    append(primaryStateName)

                    withStyle(
                        style = SpanStyle(
                            fontFamily = PitagonsSans,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        append(" / ")
                    }

                    withStyle(
                        style = SpanStyle(
                            fontFamily = PitagonsSans,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        append(secondaryStateName)
                    }
                },
                fontFamily = SpaceMono,
                color = primaryColor.copy(0.25f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None,
                modifier = Modifier.pointerInput(Unit){
                    detectTapGestures {
                        onToggle()
                    }
                }
            )
        } else {
            Text(
                buildAnnotatedString {
                    //append("Sent ")
                    append(primaryStateName)

                    withStyle(
                        style = SpanStyle(
                            fontFamily = PitagonsSans,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        append(" / ")
                    }

                    withStyle(
                        style = SpanStyle(
                            fontFamily = PitagonsSans,
                            color = primaryColor.copy(0.25f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        append(secondaryStateName)
                    }
                },
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None,
                modifier = Modifier.pointerInput(Unit){
                    detectTapGestures {
                        onToggle()
                    }
                }
            )
        }

    }

}