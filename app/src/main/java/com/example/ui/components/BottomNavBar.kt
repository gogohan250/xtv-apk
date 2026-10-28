package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.XtvAccent
import com.example.ui.theme.XtvBorder
import com.example.ui.theme.XtvSurface
import com.example.ui.theme.XtvTextSecondary
import com.example.ui.viewmodel.NavTab

@Composable
fun BottomNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(XtvSurface.copy(alpha = 0.94f))
            .border(1.dp, XtvBorder.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                label = "الرئيسية",
                icon = Icons.Default.Home,
                selected = currentTab == NavTab.HOME,
                onClick = { onTabSelected(NavTab.HOME) },
                tag = "nav_home"
            )

            NavBarItem(
                label = "البحث",
                icon = Icons.Default.Search,
                selected = currentTab == NavTab.SEARCH,
                onClick = { onTabSelected(NavTab.SEARCH) },
                tag = "nav_search"
            )

            NavBarItem(
                label = "المفضلة",
                icon = Icons.Default.Favorite,
                selected = currentTab == NavTab.FAVORITES,
                onClick = { onTabSelected(NavTab.FAVORITES) },
                tag = "nav_favorites"
            )

            NavBarItem(
                label = "السجل",
                icon = Icons.Default.History,
                selected = currentTab == NavTab.HISTORY,
                onClick = { onTabSelected(NavTab.HISTORY) },
                tag = "nav_history"
            )

            NavBarItem(
                label = "الإعدادات",
                icon = Icons.Default.Settings,
                selected = currentTab == NavTab.SETTINGS,
                onClick = { onTabSelected(NavTab.SETTINGS) },
                tag = "nav_settings"
            )
        }
    }
}

@Composable
private fun NavBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) XtvAccent else XtvTextSecondary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) XtvAccent else XtvTextSecondary,
                fontSize = 10.sp
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Small glowing dot for active item
        if (selected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(XtvAccent)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
