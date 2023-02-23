package org.ethereumphone.walletmanager.feature_send.ui

import android.view.Gravity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import org.ethereumphone.walletmanager.core.designsystem.WmTheme
import org.ethereumphone.walletmanager.core.designsystem.onPrimary
import org.ethereumphone.walletmanager.core.ui.SwipeIndicator
import org.ethereumphone.walletmanager.core.ui.WmSwipeButton
import org.ethereumphone.walletmanager.core.ui.WmTextField
import org.ethereumphone.walletmanager.feature_home.model.WalletAmount

private val NORMAL = Color(0XFF8F7CFF)
private val ERROR = Color(0xFFFF5F5F)
@Composable
fun SendDialog(
    modifier: Modifier = Modifier,
    walletAmount: WalletAmount = WalletAmount(),
    setShowDialog: () -> Unit,
    onConfirm: (Float) -> Unit

) {
    var address by remember { mutableStateOf("") }
    var value by remember { mutableStateOf(0f) }
    Dialog(
        onDismissRequest = {setShowDialog()},
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            color = onPrimary,
            shape = RoundedCornerShape(3),
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(
                        horizontal = 40.dp,
                        vertical = 40.dp,
                    )
            ) {
                WmTextField(
                    text = address,
                    label = "Address or ENS",
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.clickable {

                            }
                        )
                    },
                    onTextChanged = {address = it}
                )
                Spacer(
                    Modifier.height(10.dp)
                )
                Text(
                    text = "$value ETH",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if(value <= walletAmount.ethAmount.toFloat()) NORMAL else ERROR
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier.width(180.dp)
                ) {
                    Divider(
                        color = Color(0xFF394450),
                        thickness = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    NumPad(
                        modifier = Modifier.fillMaxWidth(),
                        value = value.toString(),
                        onValueChange = {value = it.toFloat()}
                    )
                }

                Spacer(Modifier.height(20.dp))
                WmSwipeButton(
                    text = "Swipe to send",
                    icon = Icons.Rounded.ArrowForward,
                    completeIcon = Icons.Default.Check,
                ) {
                    onConfirm(value)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSendDialog() {
    WmTheme {
        SendDialog(
            walletAmount = WalletAmount(1.0, 2.0),
            modifier = Modifier
                .width(400.dp),
            setShowDialog = {}
        ) {}
    }
}