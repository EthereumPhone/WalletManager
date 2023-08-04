package com.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.designsystem.theme.onPrimaryVariant

@Composable
fun ExpandableListItem(
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
    headline: @Composable () -> Unit,
    support: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = headline,
            supportingContent = {
                AnimatedVisibility(
                    visible = !expanded,
                    enter =  expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    support()
                }
            },
            trailingContent = {
                IconButton(
                    onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            colors = colors
        )

        AnimatedVisibility(visible = expanded) {
            expandedContent()
        }
    }
}


@Composable
//@Preview
fun ExpandableListItemPreview() {
    ExpandableListItem(
        modifier = Modifier.fillMaxWidth(),
        headline = {
            Row {
                Text(
                    text = "USDC: ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "1234.45",
                    fontSize = 20.sp
                )
            }

                   },
        support = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for( i in 1..10) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Red)
                            .size(14.dp)
                    )
                }
            }
        },
        expandedContent = {
                Text("TEstTest".repeat(20))
        }
    )

}