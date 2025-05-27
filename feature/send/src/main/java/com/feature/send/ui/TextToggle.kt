package com.feature.send.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.smallDuration

@Composable
fun TextToggle(
    modifier: Modifier = Modifier,
    primaryStateName: String,
    secondaryStateName: String,
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
                            color = dgenTurqoise,
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
                            color = dgenTurqoise,
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
                color = dgenTurqoise.copy(0.5f),
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
                            color = dgenTurqoise,
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
                            color = dgenTurqoise.copy(0.5f),
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
                color = dgenTurqoise,
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