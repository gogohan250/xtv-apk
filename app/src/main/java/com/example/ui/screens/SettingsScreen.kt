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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.XtvLogo
import com.example.ui.theme.XtvAccent
import com.example.ui.theme.XtvBackground
import com.example.ui.theme.XtvBorder
import com.example.ui.theme.XtvError
import com.example.ui.theme.XtvSurface
import com.example.ui.theme.XtvSurfaceElevated
import com.example.ui.theme.XtvTextPrimary
import com.example.ui.theme.XtvTextSecondary

@Composable
fun SettingsScreen(
    isDemoMode: Boolean,
    onToggleDemoMode: () -> Unit,
    onClearFavorites: () -> Unit,
    onClearHistory: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDataSaver by remember { mutableStateOf(false) }
    var isAutoPlay by remember { mutableStateOf(true) }

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

            Text(
                text = "الإعدادات",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = XtvTextPrimary
                )
            )
            Text(
                text = "تخصيص تجربة المشاهدة وإدارة البيانات",
                style = MaterialTheme.typography.bodyMedium.copy(color = XtvTextSecondary)
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Playback Settings Section
                item {
                    SettingsGroupCard(title = "تشغيل البث") {
                        SettingsToggleRow(
                            title = "الوضع التجريبي (Demo)",
                            subtitle = "تشغيل باقة قنوات حية مضمونة وثابتة للاختبار السريع",
                            checked = isDemoMode,
                            onCheckedChange = { onToggleDemoMode() },
                            icon = Icons.Default.Tune,
                            testTag = "setting_demo_toggle"
                        )

                        HorizontalDivider(color = XtvBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                        SettingsToggleRow(
                            title = "التشغيل التلقائي",
                            subtitle = "بدء تشغيل القناة مباشرة عند فتحها",
                            checked = isAutoPlay,
                            onCheckedChange = { isAutoPlay = it },
                            icon = Icons.Default.PlayCircleFilled
                        )

                        HorizontalDivider(color = XtvBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                        SettingsToggleRow(
                            title = "توفير البيانات",
                            subtitle = "تقليل استهلاك الإنترنت واختيار جودة متوسطة",
                            checked = isDataSaver,
                            onCheckedChange = { isDataSaver = it },
                            icon = Icons.Default.NetworkCheck
                        )
                    }
                }

                // Data Management Section
                item {
                    SettingsGroupCard(title = "إدارة البيانات والذاكرة") {
                        SettingsActionRow(
                            title = "تحديث القنوات ومسح الكاش",
                            subtitle = "إعادة جلب القنوات وحذف البيانات المؤقتة",
                            icon = Icons.Default.Cached,
                            actionLabel = "مسح وتحديث",
                            onClick = onClearCache
                        )

                        HorizontalDivider(color = XtvBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                        SettingsActionRow(
                            title = "مسح قائمة المفضلة",
                            subtitle = "إزالة جميع القنوات المحفوظة لديك",
                            icon = Icons.Default.Favorite,
                            actionLabel = "إفراغ",
                            isDestructive = true,
                            onClick = onClearFavorites
                        )

                        HorizontalDivider(color = XtvBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                        SettingsActionRow(
                            title = "مسح سجل المشاهدة",
                            subtitle = "حذف قائمة القنوات المشاهدة مؤخراً",
                            icon = Icons.Default.History,
                            actionLabel = "مسح",
                            isDestructive = true,
                            onClick = onClearHistory
                        )
                    }
                }

                // About XTV Section
                item {
                    SettingsGroupCard(title = "حول التطبيق") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            XtvLogo(size = 54.dp, showText = true)

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "الإصدار 1.0 (فاخر)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = XtvAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "تطبيق XTV الفاخر لمشاهدة القنوات التلفزيونية المباشرة العالمية والعربية، بتصميم سينمائي هادئ وتقنيات بث فائقة السرعة.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = XtvTextSecondary,
                                    lineHeight = 20.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = XtvTextSecondary,
                fontSize = 13.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(XtvSurface)
                .border(1.dp, XtvBorder, RoundedCornerShape(18.dp))
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(XtvSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) XtvAccent else XtvTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = XtvTextPrimary,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = XtvTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = XtvAccent,
                uncheckedThumbColor = XtvTextSecondary,
                uncheckedTrackColor = XtvSurfaceElevated,
                uncheckedBorderColor = XtvBorder
            ),
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionLabel: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(XtvSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) XtvError else XtvTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = XtvTextPrimary,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = XtvTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDestructive) XtvError.copy(alpha = 0.15f) else XtvSurfaceElevated)
                .border(
                    1.dp,
                    if (isDestructive) XtvError.copy(alpha = 0.4f) else XtvBorder,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isDestructive) XtvError else XtvAccent,
                    fontSize = 11.sp
                )
            )
        }
    }
}
