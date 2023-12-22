package com.feature.send.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.core.data.model.dto.Contact
import com.core.model.TokenAsset
import com.core.ui.WmListItem
import com.feature.send.AssetUiState
import com.feature.send.R
import org.ethosmobile.components.library.core.ethOSListItem

@Composable
fun ContactPickerSheet(
    //balancesState: AssetUiState,
    //getContacts: (Context) -> Unit,
    contacts: List<Contact>,
    onSelectContact: (Contact) -> Unit //method, when asset is selected
    //swapTokenUiState: SwapTokenUiState,
    //searchQuery: String,
    //onQueryChange: (String) -> Unit,
    //onSelectAsset: (TokenAsset) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
            .background(Color(0xFF262626))
            .padding(start = 12.dp, end = 12.dp, bottom =48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ){

            Text(
                text = "Contacts",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(
        ) {
            contacts.forEach {contact ->
                item(key = contact.id) {

                    ethOSListItem(
                        withImage = true,
                        image = {
                            if (contact.image != ""){
                                Image(
                                    painter = rememberImagePainter(contact.image),
                                    contentDescription = "Contact Profile Pic",
                                    contentScale = ContentScale.Crop
                                )
                            } else{
                                Image(painter = painterResource(id = R.drawable.nouns), contentDescription = "Contact Profile Pic" )
                            }
                        },
                        header = contact.name,
                        onClick = { onSelectContact(contact) }
                    )
                }
            }





        }




    }
}

@Composable
@Preview
fun ContactPickerSheetPreview(){
    ContactPickerSheet(
        listOf(
            Contact(
                id = "1",
                name = "Elie Munsi",
                phone = "+43123456789",
                email = "emunsi123@outlook.com",
                ens = "emunsi.eth",
                address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92",
                image = ""
            ),
            Contact(
                id = "2",
                name = "Nicola Ceornea",
                phone = "+43109876543",
                email = "nceornea123@outlook.com",
                ens = "nceornea.eth",
                address = "0xff47n49no48n8a9g4bkksbe344302",
                image = ""
            ),
            Contact(
                id = "3",
                name = "Markus Haas",
                phone = "+4323123223455",
                email = "mhaas123@outlook.com",
                ens = "mhaas.eth",
                address = "0xgf4oh4bnjn48h2n32b9sgdee034n50jf0gt0h392",
                image = ""
            )
        ),
        {}
    )
}