package com.example.transactions.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TransferItem
import com.core.ui.util.abbreviateNumber
import com.core.ui.util.formatAddress
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.transactions.TokenAssetUiState
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class TxType{
    SENT,
    RECEIVED,
    SWAPED,
    NFT
}

data class TxEntry (
    val fromAmount: Double = 0.0,
    val toAmount: Double = 0.0,
    val fromTokenName: String = "",
    val toTokenName: String = "",
    val fromAddress: String = "",
    val toAddress: String = "",
    val txType : TxType = TxType.SENT
)

@Composable
fun LogEntry(
    logoUrl: String = "",
    logEntry : TransferItem
) {
    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US) // Forces the decimal point
    }

    //formating
    val fromValue = if(logEntry.from.takeLast(4) == ".eth"){
        logEntry.from
    } else {
        formatAddress(logEntry.from)
    }

    val toValue = if(logEntry.to.takeLast(4) == ".eth"){
        logEntry.to
    } else {
        formatAddress(logEntry.to)
    }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        if(logoUrl != ""){
            //                TODO: Add Async Images

//                    AsyncImage(
//                        modifier = Modifier.padding(bottom = 2.dp).clip(CircleShape).size(32.dp),
//                        model = "https://example.com/image.jpg",
//                        contentDescription = "Translated description of what the image contains"
//                    )

        }else{
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(com.core.ui.R.drawable.placeholer_icon_5),
                contentDescription = "Ethereum"
            )
        }

        //if the tx was sending something
        if (logEntry.userSent) {

            Text(
                buildAnnotatedString {
                    //append("Sent ")
                    append("${abbreviateNumber(logEntry.value.toDouble())} ${logEntry.asset}")

                    withStyle(
                        style = SpanStyle(
                            fontFamily = SpaceMono,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        append(" TO ")
                    }

                    append(toValue)
                },
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )
        }
        else{
            Text(

                buildAnnotatedString {
                    append("${abbreviateNumber(logEntry.value.toDouble())} ${logEntry.asset}")

                    withStyle(style = SpanStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                    ) {
                        append(" FROM ")
                    }

                    append(fromValue)
                },
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )

        }
    }


}