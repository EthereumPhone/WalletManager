package org.ethereumphone.walletmanager.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.theme.md_theme_dark_onSurface
import org.ethereumphone.walletmanager.ui.theme.InputFiledColors
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import java.util.concurrent.CompletableFuture


@Composable
fun InputField(
    value: String,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    singeLine: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    shape: Shape = RoundedCornerShape(50.dp),
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    onFocusChange: (() -> Unit)? = null,
    colors: TextFieldColors = InputFiledColors(),
    onChange: (value: String) -> Unit,
) {
    TextField(
        value = value,
        keyboardOptions = keyboardOptions,
        onValueChange = onChange,
        label = label,
        readOnly = readOnly,
        isError = isError,
        colors = colors,
        shape = shape,
        modifier = modifier.onFocusChanged {
            if (onFocusChange != null) {
                onFocusChange()
            }
        },
        singleLine = singeLine,
        maxLines = maxLines,
        trailingIcon = trailingIcon,

        )
}

@Composable
fun ENSInputField(
    value: String,
    label: String = "",
    onlyNumbers : Boolean = false,
    readOnly: Boolean = false,
    singeLine: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    shape: Shape = RoundedCornerShape(50.dp),
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    onChange: ((value: String) -> Unit)? = null
) {
    var text by remember { mutableStateOf(value) }
    TextField(
        value = text,
        keyboardOptions = if (onlyNumbers) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        onValueChange = {
            text = it
            if (onChange != null) {
                onChange(it)
            }
        },
        label = {
            Text(text = label, color = md_theme_dark_onSurface)
        },
        readOnly = readOnly,
        colors = InputFiledColors(),
        shape = shape,
        modifier = modifier.onFocusChanged {
            if(ENSName(text).isPotentialENSDomain()){
                // It is ENS
                CompletableFuture.runAsync {
                    val ens = ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
                    val ensAddr = ens.getAddress(ENSName(text))
                    text = ensAddr?.hex.toString()
                }
            }
        },
        singleLine = singeLine,
        maxLines = maxLines,
        trailingIcon = trailingIcon
    )
}



@Composable
@Preview
fun PreviewInputFiledEmpty() {
    WalletManagerTheme {
        InputField("") {
            println(it)
        }
    }
}

@Composable
@Preview
fun PreviewInputFiled() {
    WalletManagerTheme {
        InputField("Test") {
            println(it)
        }
    }
}

@Composable
@Preview
fun PreviewInputFiledBox() {
    WalletManagerTheme {
        InputField(
            "Test",
            singeLine = false,
            maxLines = 5,
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.7f)
        ) {
            println(it)
        }
    }
}

@Composable
@Preview
fun PreviewNumberBox() {
    WalletManagerTheme {
        InputField(
            "",
            singeLine = false,
            maxLines = 1,
        ) {
            println(it)
        }
    }
}