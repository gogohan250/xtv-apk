package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.ui.components.ChannelCard
import com.example.ui.theme.XtvAccent
import com.example.ui.theme.XtvBackground
import com.example.ui.theme.XtvBorder
import com.example.ui.theme.XtvSurface
import com.example.ui.theme.XtvSurfaceElevated
import com.example.ui.theme.XtvTextPrimary
import com.example.ui.theme.XtvTextSecondary

@Composable
fun SearchScreen(
    query: String,
    results: List<Channel>,
    recentSearches: List<String>,
    favoriteIds: Set<String>,
    onQueryChanged: (String) -> Unit,
    onSearchTagClicked: (String) -> Unit,
    onClearRecent: () -> Unit,
    onChannelClick: (Channel) -> Unit,
    onFavoriteToggle: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(XtvBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar Header
            Text(
                text = "البحث في القنوات",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = XtvTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Text Field
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                placeholder = {
                    Text(
                        text = "ابحث باسم القناة، التصنيف، أو الدولة...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = XtvTextSecondary)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = if (query.isNotEmpty()) XtvAccent else XtvTextSecondary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = XtvTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = XtvSurface,
                    unfocusedContainerColor = XtvSurface,
                    focusedBorderColor = XtvAccent,
                    unfocusedBorderColor = XtvBorder,
                    focusedTextColor = XtvTextPrimary,
                    unfocusedTextColor = XtvTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Suggestions / Quick Category tags
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val suggestions = listOf("الجزيرة", "العربية", "أخبار", "رياضة", "وثائقي", "ديني", "أطفال", "DW")
                items(suggestions) { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(XtvSurfaceElevated)
                            .border(1.dp, XtvBorder, RoundedCornerShape(10.dp))
                            .clickable { onSearchTagClicked(tag) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = XtvTextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recent searches if query is empty
            if (query.isEmpty() && recentSearches.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = XtvTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "عمليات البحث الأخيرة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = XtvTextSecondary,
                                fontSize = 13.sp
                            )
                        )
                    }
                    TextButton(onClick = onClearRecent) {
                        Text("مسح السجل", color = XtvAccent, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentSearches) { recent ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(XtvSurface)
                                .border(1.dp, XtvBorder, RoundedCornerShape(8.dp))
                                .clickable { onSearchTagClicked(recent) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = recent,
                                style = MaterialTheme.typography.labelSmall.copy(color = XtvTextPrimary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Results Content
            if (query.isNotEmpty()) {
                if (results.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = XtvTextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لا توجد نتائج تطابق \"$query\"",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = XtvTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "جرّب البحث باسم قناة أخرى أو اختر أحد الاقتراحات أعلاه.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = XtvTextSecondary
                                )
                            )
                        }
                    }
                } else {
                    Text(
                        text = "نتائج البحث (${results.size})",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = XtvTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(results, key = { it.id + it.streamUrl }) { ch ->
                            ChannelCard(
                                channel = ch,
                                isFavorite = favoriteIds.contains(ch.id),
                                onClick = { onChannelClick(ch) },
                                onFavoriteToggle = { onFavoriteToggle(ch) },
                                cardWidth = 160.dp
                            )
                        }
                    }
                }
            } else {
                // Helpful guidance when search is empty
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = XtvBorder,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "اكتب كلمة للبحث الفوري",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = XtvTextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}
