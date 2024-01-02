package com.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.ethOSButton
import com.feature.home.R
import com.feature.home.util.OnboardingItem


@Composable
fun WelcomeModalBottomSheet(onDismiss: () -> Unit) {

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(42.dp),
        modifier = Modifier.padding(start = 32.dp,end = 32.dp, bottom =32.dp)
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painterResource(id = R.drawable.wallet_icon), contentDescription = "WalletManager",tint = Color.White, modifier = Modifier.size(56.dp))
            Text(
                text = "WalletManager",
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(42.dp)
        ){
            onboardingItems.forEach { item ->
                item{
                    OnboardingListItem(item)
                }
            }
        }
        ethOSButton(text = "Continue", enabled = true, onClick = onDismiss)
    }
}






@Composable
fun OnboardingListItem(item: OnboardingItem){
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.imageVector,
            contentDescription = item.title,
            tint = Color.White,
            modifier = Modifier.size(42.dp)
        )
        Text(
            text = item.title,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = item.subtitle,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = Color(0xFF8C8C8C),
            fontWeight = FontWeight.SemiBold
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingModalBottomSheet(onDismiss: () -> Unit, sheetState: SheetState){

    ModalBottomSheet(
        containerColor= Color.Black,
        contentColor= Color.White,

        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {

        WelcomeModalBottomSheet(onDismiss)
    }
}

//Different between native apps
val onboardingItems = listOf(
    OnboardingItem(
        imageVector = Icons.Filled.Wallet,
        title= "Generated System Wallet",
        subtitle = "You can manage your personal ethOS address in the wallet manager."
    ),
    OnboardingItem(
        imageVector = Icons.Outlined.Send,
        title= "Send & Receive Crypto",
        subtitle = "Seamlessly send and receive crypto across chains through the system wallet."
    ),
    OnboardingItem(
        imageVector = Icons.Rounded.SwapVert,
        title= "Swap Tokens",
        subtitle = "Swap any ERC-20 Token directly on your phone. Currently supports mainnet and optimism."
    )

)



