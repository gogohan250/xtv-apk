package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.ui.components.CategoryTabs
import com.example.ui.components.ChannelCard
import com.example.ui.components.ChannelCarousel
import com.example.ui.components.HeaderBar
import com.example.ui.components.HeroSection
import com.example.ui.theme.XtvAccent
import com.example.ui.theme.XtvBackground
import com.example.ui.theme.XtvTextSecondary
import com.example.ui.viewmodel.XtvUiState

@Composable
fun HomeScreen(
    uiState: XtvUiState,
    onChannelClick: (Channel) -> Unit,
    onFavoriteToggle: (Channel) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(XtvBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            HeaderBar(
                isOffline = uiState.isOffline,
                isDemoMode = uiState.isDemoMode,
                isLoading = uiState.isLoading,
                onSearchClick = onSearchClick,
                onRefreshClick = onRefreshClick
            )

            if (uiState.isLoading && uiState.channels.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = XtvAccent,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "جاري تحميل وتحديث الباقات...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = XtvTextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Hero Section
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        HeroSection(
                            channel = uiState.featuredChannel,
                            isFavorite = uiState.featuredChannel?.let { uiState.favoriteIds.contains(it.id) } == true,
                            onWatchClick = onChannelClick,
                            onFavoriteToggle = onFavoriteToggle,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Continue Watching Carousel (if history exists)
                    if (uiState.history.isNotEmpty()) {
                        item {
                            ChannelCarousel(
                                title = "تابع المشاهدة",
                                channels = uiState.history.map { it.channel },
                                favoriteIds = uiState.favoriteIds,
                                onChannelClick = onChannelClick,
                                onFavoriteToggle = onFavoriteToggle
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    // Category Tabs
                    item {
                        CategoryTabs(
                            categories = uiState.categories,
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = onCategorySelected
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Category Specific Content
                    when (uiState.selectedCategory) {
                        "عربي" -> {
                            val arabicList = uiState.arabicChannels.ifEmpty { uiState.channels }
                            val news = arabicList.filter { it.category == "أخبار" }
                            val religious = arabicList.filter { it.category == "ديني" }
                            val sports = arabicList.filter { it.category == "رياضة" }
                            val movies = arabicList.filter { it.category == "أفلام" || it.category == "ترفيه" }
                            val kids = arabicList.filter { it.category == "أطفال" }

                            item {
                                ChannelCarousel(
                                    title = "أبرز القنوات العربية",
                                    channels = arabicList,
                                    favoriteIds = uiState.favoriteIds,
                                    onChannelClick = onChannelClick,
                                    onFavoriteToggle = onFavoriteToggle
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            if (news.isNotEmpty()) {
                                item {
                                    ChannelCarousel(
                                        title = "قنوات الأخبار المباشرة",
                                        channels = news,
                                        favoriteIds = uiState.favoriteIds,
                                        onChannelClick = onChannelClick,
                                        onFavoriteToggle = onFavoriteToggle
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }

                            if (religious.isNotEmpty()) {
                                item {
                                    ChannelCarousel(
                                        title = "القرآن الكريم والبرامج الدينية",
                                        channels = religious,
                                        favoriteIds = uiState.favoriteIds,
                                        onChannelClick = onChannelClick,
                                        onFavoriteToggle = onFavoriteToggle
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }

                            if (sports.isNotEmpty()) {
                                item {
                                    ChannelCarousel(
                                        title = "الرياضة والفعاليات الحية",
                                        channels = sports,
                                        favoriteIds = uiState.favoriteIds,
                                        onChannelClick = onChannelClick,
                                        onFavoriteToggle = onFavoriteToggle
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }

                            if (movies.isNotEmpty()) {
                                item {
                                    ChannelCarousel(
                                        title = "الأفلام والمسلسلات",
                                        channels = movies,
                                        favoriteIds = uiState.favoriteIds,
                                        onChannelClick = onChannelClick,
                                        onFavoriteToggle = onFavoriteToggle
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }

                            if (kids.isNotEmpty()) {
                                item {
                                    ChannelCarousel(
                                        title = "قنوات الأطفال والرسوم المتحركة",
                                        channels = kids,
                                        favoriteIds = uiState.favoriteIds,
                                        onChannelClick = onChannelClick,
                                        onFavoriteToggle = onFavoriteToggle
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }

                        "قنواتي" -> {
                            val customChannels = uiState.channels.filter { it.isCustom }
                            item {
                                if (customChannels.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "قنواتي الخاصة فارغة حالياً. يمكنك تحديث المصادر من الإعدادات.",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = XtvTextSecondary)
                                        )
                                    }
                                } else {
                                    ChannelCarousel(
                                        title = "قنواتي المخصصة",
                                        channels = customChannels,
                                        favoriteIds = uiState.favoriteIds,
                                        onChannelClick = onChannelClick,
                                        onFavoriteToggle = onFavoriteToggle
                                    )
                                }
                            }
                        }

                        "الكل" -> {
                            item {
                                ChannelCarousel(
                                    title = "جميع القنوات المتاحة",
                                    channels = uiState.channels,
                                    favoriteIds = uiState.favoriteIds,
                                    onChannelClick = onChannelClick,
                                    onFavoriteToggle = onFavoriteToggle
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }

                        else -> {
                            val filtered = uiState.channels.filter { it.category == uiState.selectedCategory }
                            item {
                                ChannelCarousel(
                                    title = "قنوات ${uiState.selectedCategory}",
                                    channels = filtered.ifEmpty { uiState.channels },
                                    favoriteIds = uiState.favoriteIds,
                                    onChannelClick = onChannelClick,
                                    onFavoriteToggle = onFavoriteToggle
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
