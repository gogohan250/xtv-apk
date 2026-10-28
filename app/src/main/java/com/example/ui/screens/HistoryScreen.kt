package com.example.ui.screens

import android.text.format.DateUtils
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.data.model.WatchHistoryItem
import com.example.ui.components.ChannelMonogram
import com.example.ui.theme.XtvAccent
import com.example.ui.theme.XtvBackground
import com.example.ui.theme.XtvBorder
import com.example.ui.theme.XtvError
import com.example.ui.theme.XtvSurface
import com.example.ui.theme.XtvSurfaceElevated
import com.example.ui.theme.XtvTextPrimary
import com.example.ui.theme.XtvTextSecondary

@Composable
fun HistoryScreen(
    historyItems: List<WatchHistoryItem>,
    onChannelClick: (Channel) -> Unit,
    onClearHistory: () -> Unit,
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
                        text = "سجل المشاهدة",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = XtvTextPrimary
                        )
                    )
                    Text(
                        text = "آخر القنوات التي شاهدتها",
                        style = MaterialTheme.typography.bodyMedium.copy(color = XtvTextSecondary)
                    )
                }

                if (historyItems.isNotEmpty()) {
                    IconButton(
                        onClick = { showConfirmClear = true },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "مسح السجل",
                            tint = XtvTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (historyItems.isEmpty()) {
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = XtvTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "سجل المشاهدة فارغ",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = XtvTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "القنوات التي تقوم بتشغيلها ستظهر هنا لتتمكن من العودة إليها بسهولة.",
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
                            Text("شاهد قنوات الآن")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyItems, key = { it.channel.id + it.watchedTimestamp }) { item ->
                        val timeString = DateUtils.getRelativeTimeSpanString(
                            item.watchedTimestamp,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(XtvSurface)
                                .border(1.dp, XtvBorder, RoundedCornerShape(16.dp))
                                .clickable { onChannelClick(item.channel) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(XtvSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                ChannelMonogram(name = item.channel.name, modifier = Modifier.size(38.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.channel.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = XtvTextPrimary,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${item.channel.category} • $timeString",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = XtvTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            IconButton(
                                onClick = { onChannelClick(item.channel) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "تشغيل",
                                    tint = XtvAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
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
                title = { Text("مسح سجل المشاهدة؟") },
                text = { Text("هل تريد مسح جميع القنوات من سجل المشاهدة؟") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmClear = false
                            onClearHistory()
                        }
                    ) {
                        Text("نعم، مسح", color = XtvError, fontWeight = FontWeight.Bold)
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
