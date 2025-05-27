package com.feature.paymaster

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.core.ui.HeaderBar
import com.core.ui.showCustomToast
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenGunMetal
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
internal fun PayMasterScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: PayMasterViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val balance by viewModel.balance.collectAsState()

    PayMasterScreen(
        balance = balance,
        onBackClick = onBackClick,
        topUp = viewModel::topUp,
        forceRefresh = viewModel::forceUpdateBalance
    )
}

@Composable
fun PayMasterScreen(
    modifier: Modifier = Modifier,
    balance: String,
    onBackClick: () -> Unit,
    topUp: suspend () -> String?,
    forceRefresh: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val formattedBalance = try {
        val bd = BigDecimal(balance)
        bd.setScale(2, RoundingMode.HALF_UP).toPlainString()
    } catch (e: NumberFormatException) {
        balance
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .background(dgenBlack)
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
    ){
        HeaderBar(text = "Gas", onClick = onBackClick)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(Modifier.offset(y = 5.dp)
                    .height(77.dp)
                    .width(8.dp)
                    .background(dgenGray.copy(0.5f))
                    .padding(end = 16.dp)
                )
                Column {
                    Text(
                        buildAnnotatedString {
                            append("TOTAL")
                        },
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None,
                        modifier = Modifier.offset(y = 8.dp)
                    )
                    Text(
                        "$${formattedBalance}",
                        fontFamily = PitagonsSans,
                        color = dgenWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 48.sp,
                        lineHeight = 48.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                }
            }

            Text(
                "Your wallet comes with a Paymaster account that covers gas on any chain. You can transact across chains without ETH or native tokens.",
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenGunMetal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier.width(370.dp)
            )
        }

        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                contentColor = dgenTurqoise,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures {
                        scope.launch {
                            val daimoUrl = topUp()
                            if (daimoUrl != null) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(daimoUrl))
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.topup_icon),
                        contentDescription = "Top up",
                        tint = dgenTurqoise
                    )
                    Text(
                        "Top up".uppercase(),
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                }
            }
        }
    }
}

@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun PayMasterScreenPreview() {
    PayMasterScreen(
        balance = "123.456789",
        onBackClick = {},
        topUp = { null },
        forceRefresh = {}
    )
}