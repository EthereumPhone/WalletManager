package com.example.transactions.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.core.model.TransferItem
import com.core.ui.util.abbreviateNumber
import com.core.ui.util.formatAddress
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
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
    logEntry : TransferItem
) {
    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US) // Forces the decimal point
    }

    if (logEntry.userSent) {

        Text(
            buildAnnotatedString {
                append("Sent ")
                withStyle(
                    style = SpanStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                ) {
                    append("${abbreviateNumber(logEntry.value.toDouble())} ${logEntry.asset} ")
                }
                append("to ")
                withStyle(
                    style = SpanStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                ) {
                    append(formatAddress(logEntry.to))
                }
            },
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        )
    }
    else{
            Text(

                buildAnnotatedString {
                    append("Received ")
                    withStyle(style = SpanStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                    ) {
                        append("${abbreviateNumber(logEntry.value.toDouble())} ${logEntry.asset} ")
                    }
                    append("from ")

                    withStyle(style = SpanStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                    ) {
                        append(formatAddress(logEntry.from))
                    }

                },
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )

    }

}