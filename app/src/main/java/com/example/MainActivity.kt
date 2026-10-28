package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.FullscreenPlayer
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.XtvBackground
import com.example.ui.theme.XtvTheme
import com.example.ui.viewmodel.NavTab
import com.example.ui.viewmodel.XtvViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XtvTheme {
                XtvAppRoot()
            }
        }
    }
}

@Composable
fun XtvAppRoot(
    viewModel: XtvViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSplashVisible by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.messageSnackbar) {
        uiState.messageSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    // Back button handling: if in sub-tab, return to HOME
    BackHandler(enabled = !isSplashVisible && !uiState.showPlayer && uiState.currentTab != NavTab.HOME) {
        viewModel.selectTab(NavTab.HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(XtvBackground)
    ) {
        if (isSplashVisible) {
            SplashScreen(onTimeout = { isSplashVisible = false })
        } else {
            Scaffold(
                containerColor = XtvBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!uiState.showPlayer) {
                        BottomNavBar(
                            currentTab = uiState.currentTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (uiState.currentTab) {
                        NavTab.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                onChannelClick = { viewModel.openPlayer(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                onCategorySelected = { viewModel.selectCategory(it) },
                                onSearchClick = { viewModel.selectTab(NavTab.SEARCH) },
                                onRefreshClick = { viewModel.loadChannels(forceRefresh = true) }
                            )
                        }

                        NavTab.SEARCH -> {
                            SearchScreen(
                                query = uiState.searchQuery,
                                results = uiState.searchResults,
                                recentSearches = uiState.recentSearches,
                                favoriteIds = uiState.favoriteIds,
                                onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                onSearchTagClicked = { viewModel.executeSearch(it) },
                                onClearRecent = { viewModel.clearRecentSearches() },
                                onChannelClick = { viewModel.openPlayer(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) }
                            )
                        }

                        NavTab.FAVORITES -> {
                            val favoriteChannels = uiState.channels.filter { uiState.favoriteIds.contains(it.id) }
                            FavoritesScreen(
                                favoriteChannels = favoriteChannels,
                                onChannelClick = { viewModel.openPlayer(it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                onClearAllFavorites = { viewModel.clearFavorites() },
                                onExploreChannelsClick = { viewModel.selectTab(NavTab.HOME) }
                            )
                        }

                        NavTab.HISTORY -> {
                            HistoryScreen(
                                historyItems = uiState.history,
                                onChannelClick = { viewModel.openPlayer(it) },
                                onClearHistory = { viewModel.clearHistory() },
                                onExploreChannelsClick = { viewModel.selectTab(NavTab.HOME) }
                            )
                        }

                        NavTab.SETTINGS -> {
                            SettingsScreen(
                                isDemoMode = uiState.isDemoMode,
                                onToggleDemoMode = { viewModel.toggleDemoMode() },
                                onClearFavorites = { viewModel.clearFavorites() },
                                onClearHistory = { viewModel.clearHistory() },
                                onClearCache = { viewModel.clearCache() }
                            )
                        }
                    }
                }
            }

            // Fullscreen Player Overlay
            AnimatedVisibility(
                visible = uiState.showPlayer && uiState.activePlayingChannel != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.activePlayingChannel?.let { activeChannel ->
                    val activeList = when (uiState.selectedCategory) {
                        "عربي" -> if (uiState.arabicChannels.isNotEmpty()) uiState.arabicChannels else uiState.channels
                        "قنواتي" -> uiState.channels.filter { it.isCustom }
                        "الكل" -> uiState.channels
                        else -> uiState.channels.filter { it.category == uiState.selectedCategory }
                    }.ifEmpty { uiState.channels }

                    FullscreenPlayer(
                        channel = activeChannel,
                        channelList = activeList,
                        isFavorite = uiState.favoriteIds.contains(activeChannel.id),
                        sleepTimerMinutes = uiState.sleepTimerMinutes,
                        sleepTimerSecondsRemaining = uiState.sleepTimerSecondsRemaining,
                        onClose = { viewModel.closePlayer() },
                        onNext = { viewModel.playNextChannel() },
                        onPrevious = { viewModel.playPreviousChannel() },
                        onSelectChannel = { viewModel.openPlayer(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(activeChannel) },
                        onMarkFailed = { viewModel.markCurrentChannelFailed() },
                        onSetSleepTimer = { viewModel.setSleepTimer(it) }
                    )
                }
            }
        }
    }
}
