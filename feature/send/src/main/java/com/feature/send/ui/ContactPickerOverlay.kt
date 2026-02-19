package com.feature.send.ui

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.data.model.dto.Contact
import com.core.ui.DgenSearchBar
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.core.ui.util.pulseOpacity
import com.example.dgenlibrary.InfoScreen
import com.example.dgenlibrary.contacts.ContactItem
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.backgrounds.FadeDirection
import com.example.dgenlibrary.ui.backgrounds.FadeEdge
import com.example.dgenlibrary.ui.theme.DgenBackgroundHorizontalPadding
import com.feature.send.AssetsUiState
import kotlin.compareTo


@Composable
fun ContactPickerOverlay(
    modifier: Modifier = Modifier,
    contacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
    onDismiss: () -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor
    var searchQuery by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
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


    DgenHeaderBackground(
        title = "SELECT CONTACT",
        primaryColor = primaryColor,
        onBackClick = onDismiss
    ){

        Column(
            Modifier
                .fillMaxSize()
                .padding(top = DgenBackgroundHorizontalPadding),
        ){
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                DgenSearchBar(
                    searchValue = searchQuery,
                    onSearchValueChange = { searchQuery = it },
                    focusedSearch = isFocused,
                    onFocusChanged = { isFocused = it },
                    textColor = primaryColor,
                    backgroundColor = secondaryColor,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    focusRequester = focusRequester,
                    keyboardController = keyboardController,
                    onClear = { searchQuery = "" },
                    onNavigateBack = {},
                    leadingIconResId = com.core.ui.R.drawable.searchicon,
                    selectedChainId = null,
                    onNetworkClick = {},
                    showChainButton = false
                )
            }

            // Contacts List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            {
                Crossfade(
                    targetState = filteredContacts.isEmpty(),
                    label = "contacts_empty_crossfade"
                ) { isEmpty ->
                    if (isEmpty) {
                        InfoScreen(
                            modifier = Modifier.fillMaxSize(),
                            description = if (searchQuery.isEmpty()) {
                                "No contacts with Ethereum addresses"
                            } else {
                                "No contacts found"
                            },
                            primaryColor = primaryColor
                        )
                    } else {
                        Box() {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ){
                                items(filteredContacts) { contact ->
                                    ContactItem(
                                        header = contact.name,
                                        subheader = when {
                                            contact.ens.isNotEmpty() -> contact.ens
                                            contact.address.length > 10 -> "${contact.address.take(6)}...${contact.address.takeLast(4)}"
                                            else -> contact.address
                                        },
                                        primaryColor = primaryColor,
                                        onClick = {
                                            onContactSelected(contact)
                                            onDismiss()
                                        }
                                    )
                                }

                                item {
                                    Spacer(Modifier.fillMaxWidth().height(16.dp))
                                }
                            }

                            FadeEdge(FadeDirection.Top,  modifier = Modifier.align(Alignment.TopCenter),)
                            FadeEdge(FadeDirection.Bottom, size = 24.dp, modifier = Modifier.align(Alignment.BottomCenter))

                        }

                    }
                }
            }
        }
    }

}
