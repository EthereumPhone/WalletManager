package com.core.ui


import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.ColumnScopeInstance.align
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import kotlinx.coroutines.launch


@Composable
@Preview
fun dgenSnackbarPreview() {

        val scaff = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        val hostState = remember { SnackbarHostState() }
        val s = rememberSnackbarDelegate(hostState,scope)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dgenBlack)
                    //.padding(8.dp)
                    //.padding(paddingValues = it)
            ) {

                Column(modifier = Modifier.fillMaxSize()) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            s.coroutineScope.launch {
                                s.showSnackbar(SnackbarState.DEFAULT,"Address copied!")
                            }
                        }) {
                        Text("Show Default")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            s.coroutineScope.launch {
                                s.showSnackbar(SnackbarState.SUCCESS,"SUCCESS")
                            }
                        }) {
                        Text("Show Success")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            s.coroutineScope.launch {
                                s.showSnackbar(SnackbarState.WARNING,"WARNING")
                            }
                        }) {
                        Text("Show Warning")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            s.coroutineScope.launch {
                                s.showSnackbar(SnackbarState.ERROR,"ERROR")
                            }
                        }) {
                        Text("Show Error")
                    }

                }
                dgenSnackbarHost(delegate = s, modifier = Modifier.padding(12.dp).align(alignment = Alignment.BottomStart))
            }
}

@Composable
fun CustomSnackBar(
    delegate: SnackbarDelegate,
    message: String,
) {
    Snackbar(
        contentColor = delegate.snackbarOnColor,
        shape =  RoundedCornerShape(60.dp),
        backgroundColor = delegate.snackbarBackgroundColor,
        elevation = 8.dp,
    ) {

        Row (
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = delegate.snackbarOnColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    letterSpacing = 1.sp,
                    textDecoration = TextDecoration.None
                )
            )
            Image(
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(delegate.snackbarOnColor),
                painter = painterResource(id = delegate.snackbarIcon),
                contentDescription = "Snackbar icon"
            )
        }
        //}
    }
}

@Composable
fun dgenSnackbarHost(
    delegate: SnackbarDelegate,
    modifier: Modifier
) {

        SnackbarHost(
            modifier= modifier,
            hostState = delegate.snackbarHostState
        ) { snackbarData: SnackbarData ->
            CustomSnackBar(
                delegate,
                snackbarData.message
            )
        }

}
