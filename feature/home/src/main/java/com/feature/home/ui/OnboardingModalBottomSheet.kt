package com.feature.home.ui

import android.media.tv.TvContract
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.data.model.dto.Contact
import com.core.model.TokenAsset
import com.feature.home.AssetsUiState
import com.feature.home.R
import com.feature.home.WalletDataUiState
import com.feature.home.formatDouble
import com.feature.home.util.OnboardingItem
import org.ethosmobile.components.library.core.ethOSButton
import org.ethosmobile.components.library.core.ethOSListItem
import org.ethosmobile.components.library.models.OnboardingObject
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import org.ethosmobile.components.library.theme.Logos


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ethOSNetworkModalBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    userData: WalletDataUiState,
    assetsUiState: AssetsUiState,
    onClick: (Int) -> Unit
){

    ModalBottomSheet(
        containerColor= Colors.BLACK,
        contentColor= Colors.WHITE,
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {

        NetworkPickerSheet(
            userData,
            assetsUiState,
            onClick
        )
        //NetworkModalBottomSheet(onDismiss, onboardingObject)
    }
}

@Composable
fun NetworkPickerSheet(
    userData: WalletDataUiState,
    assetsUiState: AssetsUiState,
    onClick: (Int) -> Unit

   // onSelectContact: (Contact) -> Unit //method, when asset is selected

) {



    var chainColor = when(userData) {
        is WalletDataUiState.Success -> {
            val data = userData.userData
            when(data.walletNetwork) {
                "1" -> Color(0xFF32CD32)
                "5" -> Color(0xFFF0EAD6)
                "137" -> Color(0xFF442fb2) // Polygon
                "10" -> Color(0xFFc82e31) // Optimum
                "42161" -> Color(0xFF2b88b8) // Arbitrum
                "8453" -> Color(0xFF053BCB) // Base
                "7777777" -> Color(0xFF777777) // Zora

                else -> {
                    Color(0xFF030303)
                }
            }
        }
        is WalletDataUiState.Loading -> {
            Color.Transparent
        }

        else -> {}
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
            .padding(start = 12.dp, end = 12.dp, bottom = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement = Arrangement.Center
        ){

            Text(
                text = "Networks",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))


        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            when(assetsUiState) {
                is AssetsUiState.Success -> {

                    //Balances
                    val mainnetBalance =  findBalanceByName(assetsUiState.assets,"Mainnet")

                    val optimismBalance=findBalanceByName(assetsUiState.assets, "Optimism")

                    val arbitrumBalance=findBalanceByName(assetsUiState.assets,"Arbitrum")

                    val baseBalance=findBalanceByName(assetsUiState.assets,"Base")

                    val zoraBalance=findBalanceByName(assetsUiState.assets,"Zora")

                    val basetestnetBalance= findBalanceByName(assetsUiState.assets,"Base Testnet")



                    Column(
                    ) {
                        NetworkListItem(
                            logo = R.drawable.ethereum_logo,
                            title = "Mainnet",
                            balance = "${mainnetBalance?.let { formatDouble(it) }} ETH",
                            onClick = {onClick(1)}
                        )

                        NetworkListItem(
                            logo = R.drawable.optimism_logo,
                            title = "Optimism",
                            balance = "${optimismBalance?.let { formatDouble(it) }} ETH",
                            onClick = { onClick(10) }
                        )

                        NetworkListItem(
                            logo = R.drawable.arbitrum_logo,
                            title = "Arbitrum",
                            balance = "${arbitrumBalance?.let { formatDouble(it) }} ETH",
                            onClick = {onClick(42161)}
                        )

                        NetworkListItem(
                            logo = R.drawable.base_logo,
                            title = "Base",
                            balance = "${baseBalance?.let { formatDouble(it) }} ETH",
                            onClick = {onClick(8453)}
                        )

                        NetworkListItem(
                            logo = R.drawable.zora_wordmark_white,
                            title = "Zora",
                            balance = "${zoraBalance?.let { formatDouble(it) }} ETH",
                            onClick = {onClick(7777777)}
                        )

                        NetworkListItem(
                            logo = R.drawable.base_logo,
                            title = "Base Testnet",
                            balance = "${basetestnetBalance?.let { formatDouble(it) }} ETH",
                            onClick = {onClick(8453)}

                        )
                        // TODO : Sepolia


                    }
                }

                else -> {
                    Text(text = "Loading...", color = Colors.GRAY, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
            }
        }










    }



}

fun findBalanceByName(assets: List<TokenAsset>, name: String): Double? {
    val asset = assets.find { it.name == name }

    return asset?.balance ?: 0.0
}

@Composable
fun NetworkListItem(logo: Int, title: String, balance: String, onClick: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp, 16.dp)
            .clickable {
                onClick()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(

            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)){
                Image(painter = painterResource(id=logo), contentDescription = "")
            }
            Text(text = title, fontFamily = Fonts.INTER, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color.White)

        }
        Text(text = balance,fontFamily = Fonts.INTER, fontWeight = FontWeight.Medium, fontSize = 18.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.30f), color = Color.White )
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



