package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.ui.components.ChannelCard
import com.example.ui.theme.XtvAccent
import com.example.ui.theme.XtvBackground
import com.example.ui.theme.XtvError
import com.example.ui.theme.XtvSurface
import com.example.ui.theme.XtvTextPrimary
import com.example.ui.theme.XtvTextSecondary

@Composable
fun FavoritesScreen(
    favoriteChannels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onFavoriteToggle: (Channel) -> Unit,
    onClearAllFavorites: () -> Unit,
    onExploreChannelsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmClear by remember { mutableStateOf(false) }

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

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "القنوات المفضلة",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = XtvTextPrimary
                        )
                    )
                    Text(
                        text = "${favoriteChannels.size} قناة محفوظة",
                        style = MaterialTheme.typography.bodyMedium.copy(color = XtvTextSecondary)
                    )
                }

                if (favoriteChannels.isNotEmpty()) {
                    IconButton(
                        onClick = { showConfirmClear = true },
                        modifier = Modifier.testTag("clear_favorites_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "مسح المفضلة",
                            tint = XtvTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (favoriteChannels.isEmpty()) {
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
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = XtvAccent.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "لا توجد قنوات في المفضلة",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = XtvTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اضغط على أيقونة القلب في أي قناة لإضافتها هنا والوصول إليها بسرعة.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = XtvTextSecondary
                            ),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onExploreChannelsClick,
                            colors = ButtonDefaults.buttonColors(containerColor = XtvAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("استكشف القنوات الآن")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favoriteChannels, key = { it.id + it.streamUrl }) { ch ->
                        ChannelCard(
                            channel = ch,
                            isFavorite = true,
                            onClick = { onChannelClick(ch) },
                            onFavoriteToggle = { onFavoriteToggle(ch) },
                            cardWidth = 160.dp
                        )
                    }
                }
            }
        }

        // Confirmation Dialog
        if (showConfirmClear) {
            AlertDialog(
                onDismissRequest = { showConfirmClear = false },
                containerColor = XtvSurface,
                titleContentColor = XtvTextPrimary,
                textContentColor = XtvTextSecondary,
                title = { Text("مسح جميع القنوات المفضلة؟") },
                text = { Text("هل أنت متأكد من رغبتك في إزالة جميع القنوات من قائمة المفضلة؟") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmClear = false
                            onClearAllFavorites()
                        }
                    ) {
                        Text("نعم، إفراغ", color = XtvError, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmClear = false }) {
                        Text("إلغاء", color = XtvTextSecondary)
                    }
                }
            )
        }
    }
}
