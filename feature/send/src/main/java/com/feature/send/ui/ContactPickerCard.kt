package com.feature.send.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.core.data.model.dto.Contact
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenGray
import com.core.ui.util.dgenWhite
import com.feature.send.R

@Composable
fun ContactPickerCard(
    contacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
    onDismiss: () -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredContacts = remember(searchQuery, contacts) {
        if (searchQuery.isEmpty()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                contact.address.contains(searchQuery, ignoreCase = true) ||
                contact.ens.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = dgenBlack
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT CONTACT",
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryColor,
                            letterSpacing = 1.sp
                        )
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = dgenGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search Field
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    placeholder = {
                        Text(
                            "Search contacts...",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                fontSize = 16.sp,
                                color = dgenGray
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = dgenGray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = PitagonsSans,
                        fontSize = 16.sp,
                        color = dgenWhite
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1A1A),
                        unfocusedContainerColor = Color(0xFF1A1A1A),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = primaryColor
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contacts List
                if (filteredContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) {
                                "No contacts with Ethereum addresses"
                            } else {
                                "No contacts found"
                            },
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                fontSize = 16.sp,
                                color = dgenGray
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredContacts) { contact ->
                            ContactItem(
                                contact = contact,
                                primaryColor = primaryColor,
                                onClick = {
                                    onContactSelected(contact)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: Contact,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(dgenGray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (contact.image.isNotEmpty()) {
                AsyncImage(
                    model = contact.image,
                    contentDescription = "Contact photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder - show first letter of name
                Text(
                    text = contact.name.firstOrNull()?.uppercase() ?: "?",
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = dgenWhite
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Contact Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = contact.name,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = dgenWhite
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = when {
                    contact.ens.isNotEmpty() -> contact.ens
                    contact.address.length > 10 -> "${contact.address.take(6)}...${contact.address.takeLast(4)}"
                    else -> contact.address
                },
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontSize = 14.sp,
                    color = if (contact.ens.isNotEmpty()) primaryColor else dgenGray
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
