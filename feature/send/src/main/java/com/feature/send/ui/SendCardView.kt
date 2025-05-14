package com.feature.send.ui


import InstantGif
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.core.data.util.chainToApiKey
import com.core.ui.R
import com.core.ui.util.abbreviateNumber
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.CompletableFuture
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.crypto.WalletUtils
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

@Composable
fun SendCardView(
    amount: String,
    toAddress: String,
    maxamount: Double,
    tokenName: String,
    onAddressChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
){

    var validSendAddress by remember { mutableStateOf(false) }


    var context = LocalContext.current

    val cursorVisible = remember { mutableStateOf(true) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    var amountCard by remember { mutableStateOf("") }
    var toAddressCard by remember { mutableStateOf("") }

    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()


    // Create a blinking effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // Toggle every 500ms
            cursorVisible.value = !cursorVisible.value
        }
    }

    Box(
        modifier = Modifier.alpha(0.2f).offset(x = 100.dp, y = 40.dp).scale(0.8f).aspectRatio(1f),
        contentAlignment = Alignment.CenterEnd
    ) {

        AsyncImage(
            imageLoader = gifEnabledLoader,
            model = R.drawable.wireframe_torus,
            contentDescription = null

        )
    }
    Column (
        verticalArrangement =  Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp, horizontal = 16.dp),
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {


                Text(
                    text = "SEND $tokenName",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,

                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
            }

//            SelectableIconRow(
//                chainList,
//                {}
//            )

        }


        Row {
            Box(
                modifier = Modifier.weight(1f)
            ){
                if (amountCard.isEmpty()){
                    Text(
                        modifier = Modifier.alpha(0.5f),
                        text = "0.0",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 48.sp,

                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None

                        )
                    )
                }
                BasicTextField(
                    maxLines= 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            textLayoutResult?.let { layoutResult ->
                                if (cursorVisible.value) {
                                    // Get last character position (if text is not empty)
                                    val cursorOffset = if (amountCard.isNotEmpty()) {
                                        val lastCharIndex = amountCard.length - 1
                                        val cursorRect = layoutResult.getBoundingBox(lastCharIndex)
                                        cursorRect
                                    } else {
                                        Rect(
                                            0f,
                                            0f,
                                            2.dp.toPx(),
                                            layoutResult.size.height.toFloat()
                                        )
                                    }

                                    // Draw the blinking cursor at the correct position
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(
                                            cursorOffset.right + 2.dp.toPx(),
                                            cursorOffset.top
                                        ),
                                        end = Offset(
                                            cursorOffset.right + 2.dp.toPx(),
                                            cursorOffset.bottom
                                        ),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                            }
                        },
                    value = amount,
                    onValueChange = { text ->
                        if (text.isEmpty() || text == "." || text.matches("-?\\d*(\\.\\d*)?".toRegex())) {
                            // If it's a valid format or empty, call onAmountChange with the text
                            onAmountChange(text)
                            amountCard = text
                        }
                    },
                    cursorBrush = SolidColor(Color.White), // Hide default cursor
                    textStyle = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 48.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    decorationBox = { innerTextField ->


                        innerTextField()

                    }
                )
            }
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ){
            Box(
                modifier = Modifier.weight(1f)
            ){
                if (toAddressCard.isEmpty()){
                    Text(
                        modifier = Modifier.alpha(0.5f),
                        text = "Address",
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,

                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None

                        )
                    )
                }
                BasicTextField(
                    modifier = Modifier.fillMaxWidth()
                    ,
                    value = toAddress,
                    onValueChange = {

                        onAddressChange(it)
                        toAddressCard = it
                        CompletableFuture.runAsync {
                            if (it.endsWith(".eth")) {
                                if (ENSName(it.lowercase()).isPotentialENSDomain()) {
                                    // It is ENS
                                    println("Checking ENS")
                                    val ens = ENS(
                                        HttpEthereumRPC(
                                            "https://eth-mainnet.g.alchemy.com/v2/${
                                                chainToApiKey("eth-mainnet")
                                            }"
                                        )
                                    )
                                    val ensAddr = ens.getAddress(ENSName(it.lowercase()))
                                    ensAddr?.let { address ->
                                        onAddressChange(address.hex)
                                        validSendAddress = true
                                    }
                                } else {
                                    if (it.isNotEmpty()) validSendAddress =
                                        WalletUtils.isValidAddress(it.lowercase())
                                }
                                if (it.isEmpty()) {
                                    validSendAddress = false
                                }
                            }
                        }
                    },
                    cursorBrush = SolidColor(Color.White), // Hide default cursor
                    textStyle = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,

                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Text
                    ),
                    maxLines= 2,
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =Modifier.background(Color.Transparent, RoundedCornerShape(percent = 30))
                            //.padding(16.dp)
                        ) {
                            innerTextField()
                        }
                    }
                )
            }


            Column (
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(0.25f),

                ){
                Text(
                    text= "MAX",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
                Text(
                    text= abbreviateNumber(maxamount),
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
            }

        }
    }
}


@Composable
fun ErrorCardView(){
    Box(
        modifier = Modifier.alpha(0.25f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier
                .size(400.dp)
                .offset(x = 100.dp, y = 40.dp),
            painter = painterResource(R.drawable.iso),
            contentDescription = "Ethereum"
        )
    }
    Column (
        verticalArrangement =  Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp, horizontal = 16.dp),
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ERROR",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
            }
        }
    }
}



@Preview(
//    showBackground = true,
    widthDp = 447,
    heightDp = 447,
)
@Composable
fun SendCardPreviewView(){
    /*SendCardView(
        amount = "120.00",
        tokenName = "USDC",
        toAddress = TODO(),
        maxamount = TODO(),
        onAddressChange = TODO(),
        onAmountChange = TODO()
    )*/
}