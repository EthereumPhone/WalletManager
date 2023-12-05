package com.feature.send.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import com.core.data.model.dto.Contact
import com.feature.send.R
import coil.compose.rememberImagePainter

@Composable
fun ContactPill(
    contact: Contact,
    onClose: () -> Unit
){
    Surface(
        shape= CircleShape,
        color = Color(0xFF262626),
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(start = 4.dp,top=2.dp, bottom = 2.dp, end = 12.dp)
        ){
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262626))
            ){
                if (contact.image != ""){
//                    Image(painter = painterResource(id = R.drawable.nouns), contentDescription = "" )
                    Image(
                        painter = rememberImagePainter(contact.image),
                        contentDescription = "Contact Profile Pic",
                        contentScale = ContentScale.Crop
                    )
                } else{
                    Image(painter = painterResource(id = R.drawable.nouns), contentDescription = "Contact Profile Pic" )

                }//contact.image else R.drawable.nouns
                }
            Text(
                text = contact.name,
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp ,4.dp)
            )
            IconButton(modifier = Modifier
                .size(24.dp),onClick = onClose) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
            }
        }

    }
}

@Composable
@Preview
fun ContactPillPreview(){
    ContactPill(
        Contact(
            id = "1",
            name = "Elie Munsi",
            phone = "+43123456789",
            email = "emunsi123@outlook.com",
            ens = "emunsi.eth",
            address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92",
            image = ""
        )
        ,{})
}
