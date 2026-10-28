package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.XtvAccent
import com.example.ui.theme.XtvBorder
import com.example.ui.theme.XtvLiveGreen
import com.example.ui.theme.XtvSurface
import com.example.ui.theme.XtvTextPrimary
import com.example.ui.theme.XtvTextSecondary

@Composable
fun HeaderBar(
    isOffline: Boolean,
    isDemoMode: Boolean,
    isLoading: Boolean,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by if (isLoading) {
        val transition = rememberInfiniteTransition(label = "refresh_rotate")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing)
            ),
            label = "spin"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(XtvSurface.copy(alpha = 0.92f))
            .border(
                width = 1.dp,
                color = XtvBorder.copy(alpha = 0.6f),
                shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Logo
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                XtvLogo(size = 36.dp, showText = true)

                Spacer(modifier = Modifier.width(10.dp))

                // Status Badge
                when {
                    isDemoMode -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(XtvAccent.copy(alpha = 0.18f))
                                .border(1.dp, XtvAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "تجريبي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = XtvAccent,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    isOffline -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(XtvSurface)
                                .border(1.dp, XtvBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "بدون اتصال",
                                tint = XtvTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "أوفلاين",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = XtvTextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                    else -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(XtvLiveGreen.copy(alpha = 0.12f))
                                .border(1.dp, XtvLiveGreen.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(XtvLiveGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "مباشر",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = XtvLiveGreen,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("header_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث القنوات",
                        tint = XtvTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("header_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث القنوات",
                        tint = if (isLoading) XtvAccent else XtvTextPrimary,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(rotation)
                    )
                }
            }
        }
    }
}
