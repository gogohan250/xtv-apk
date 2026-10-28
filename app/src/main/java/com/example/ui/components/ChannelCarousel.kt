package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.ui.theme.XtvAccent
import com.example.ui.theme.XtvTextPrimary
import com.example.ui.theme.XtvTextSecondary

@Composable
fun ChannelCarousel(
    title: String,
    channels: List<Channel>,
    favoriteIds: Set<String>,
    onChannelClick: (Channel) -> Unit,
    onFavoriteToggle: (Channel) -> Unit,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (channels.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = XtvTextPrimary,
                        fontSize = 17.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${channels.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = XtvTextSecondary,
                        fontSize = 12.sp
                    )
                )
            }

            if (onSeeAllClick != null && channels.size > 4) {
                Text(
                    text = "عرض الكل",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = XtvAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.clickable { onSeeAllClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Row of Cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(channels.take(20), key = { it.id + it.streamUrl }) { channel ->
                ChannelCard(
                    channel = channel,
                    isFavorite = favoriteIds.contains(channel.id),
                    onClick = { onChannelClick(channel) },
                    onFavoriteToggle = { onFavoriteToggle(channel) }
                )
            }
        }
    }
}
