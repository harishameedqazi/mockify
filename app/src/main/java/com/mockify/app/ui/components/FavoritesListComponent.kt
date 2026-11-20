package com.mockify.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mockify.app.ui.components.listitems.FavoriteListItem
import com.mockify.app.ui.models.LocationEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesListComponent(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    data: List<LocationEntry>,
    onEntryClicked: (entry: LocationEntry) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center // Centers the text horizontally within the Box
            ) {
                Text(
                    text = "Favorite Locations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // List view
            if (data.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    userScrollEnabled = true
                ) {
                    items(data) {
                        Row {
                            FavoriteListItem(
                                locationEntry = it,
                                onClick = { onEntryClicked(it) }
                            )
                        }
                    }
                }
            } else {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "No favorite locations have been added yet!",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}