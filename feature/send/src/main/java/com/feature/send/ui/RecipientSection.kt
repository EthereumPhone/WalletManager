package com.feature.send.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.core.data.model.dto.Contact
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.SimpleDgenTextfield
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenGray
import com.core.ui.util.dgenGreen
import com.core.ui.util.dgenOrche
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenWhite
import com.core.ui.util.label_fontSize
import com.core.ui.util.pulseOpacity
import com.feature.send.RecipientUiState

@Composable
fun RecipientSection(
    recipientUiState: RecipientUiState,
    selectedContact: Contact? = null,
    hasContactsWithEth: Boolean = false,
    onContentChanged: (String) -> Unit,
    onContactIconClick: () -> Unit = {},
    onClearContact: () -> Unit = {},
    shouldDismissKeyboard: Boolean = false,
    onKeyboardDismissed: () -> Unit = {}
) {
    val primaryColor = SystemColorManager.primaryColor
    val view = LocalView.current
    val focusManager = LocalFocusManager.current


    
    val (headerText, headerColor) = when {
        selectedContact != null -> "SENDING TO CONTACT" to dgenGreen
        recipientUiState.ensError.isNotEmpty() -> "ENS ERROR" to dgenRed
        recipientUiState.isResolving -> "RESOLVING ENS..." to dgenOrche
        recipientUiState.recipientAddress.endsWith(".eth") -> "ENS RESOLVED" to dgenGreen
        else -> "TARGET ADDRESS" to primaryColor
    }

    Column(Modifier.offset(x = (-12).dp)) {
        // Header with contacts icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerText,
                color = headerColor,
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = label_fontSize,
                    lineHeight = label_fontSize,
                    letterSpacing = 1.sp,
                    textDecoration = TextDecoration.None,
                    textAlign = TextAlign.Left
                )
            )

            Spacer(modifier = Modifier.width(25.dp))
            
            // Only show contacts icon if we have contacts with ETH addresses and no contact is selected
            if (hasContactsWithEth && selectedContact == null) {
                IconButton(
                    onClick = onContactIconClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Contacts,
                        contentDescription = "Select contact",
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // Show contact UI if a contact is selected
        if (selectedContact != null) {
            ContactSelectedUI(
                contact = selectedContact,
                primaryColor = primaryColor,
                onClear = onClearContact
            )
        } else {
        // trick to keep cursor position
        var textFieldValue by remember { mutableStateOf(TextFieldValue(recipientUiState.recipientAddress)) }
        LaunchedEffect(recipientUiState) {
            if (textFieldValue.text != recipientUiState.recipientAddress) {
                // Move cursor to end when text changes (e.g., after ENS resolution)
                textFieldValue = TextFieldValue(
                    text = recipientUiState.recipientAddress,
                    selection = TextRange(recipientUiState.recipientAddress.length)
                )
            }
        }
        
        // Clear keyboard when ViewModel indicates it should be dismissed
        LaunchedEffect(shouldDismissKeyboard) {
            if (shouldDismissKeyboard) {
                focusManager.clearFocus()
                onKeyboardDismissed()
            }
        }

        SimpleDgenTextfield(
            value = textFieldValue,
            maxLines = 4,
            maxLength = 43,
            scrollHorizontally = false,
            autoCorrectEnabled = false,
            onValueChange = { new ->
                textFieldValue = new
                onContentChanged(new.text)
            },
            textStyle = TextStyle(
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight. SemiBold,
                fontSize = 25.sp
            ),
            activeColor = primaryColor,
            placeholder = {
                Text(
                    modifier = Modifier,
                    text = "Address",
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenWhite.copy(pulseOpacity),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    ),
                )
            },
            cursorColor = primaryColor,
            keyboardtype =  KeyboardType.Text,
            cursorWidth = 16.dp,
            cursorHeight= 16.dp,
            isAnyFieldFocused= remember { mutableStateOf(false) },
            onEditDone = {},
            view = view
            ) {
                // Header is now shown above the text field
            }
        }
    }
}

@Composable
private fun ContactSelectedUI(
    contact: Contact,
    primaryColor: Color,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
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
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = when {
                        contact.ens.isNotEmpty() -> contact.ens
                        contact.address.length > 10 -> "${contact.address.take(6)}...${contact.address.takeLast(4)}"
                        else -> contact.address
                    },
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        fontSize = 12.sp,
                        color = if (contact.ens.isNotEmpty()) primaryColor else dgenGray
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        IconButton(
            onClick = onClear,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear contact",
                tint = dgenGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewRecipientSection() {
    RecipientSection(
        recipientUiState = RecipientUiState(),
        selectedContact = null,
        hasContactsWithEth = false,
        onContentChanged = {},
        onContactIconClick = {},
        onClearContact = {}
    )
}