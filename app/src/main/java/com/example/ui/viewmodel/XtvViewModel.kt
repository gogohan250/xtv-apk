package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Channel
import com.example.data.model.WatchHistoryItem
import com.example.data.repository.ChannelRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NavTab {
    HOME,
    SEARCH,
    FAVORITES,
    HISTORY,
    SETTINGS
}

data class XtvUiState(
    val channels: List<Channel> = emptyList(),
    val arabicChannels: List<Channel> = emptyList(),
    val categories: List<String> = listOf("عربي", "أخبار", "رياضة", "أفلام", "وثائقي", "ديني", "أطفال", "ترفيه", "موسيقى", "قنواتي", "الكل"),
    val selectedCategory: String = "عربي",
    val featuredChannel: Channel? = null,
    val favoriteIds: Set<String> = emptySet(),
    val history: List<WatchHistoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val isDemoMode: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Channel> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val activePlayingChannel: Channel? = null,
    val showPlayer: Boolean = false,
    val currentTab: NavTab = NavTab.HOME,
    val sleepTimerMinutes: Int? = null,
    val sleepTimerSecondsRemaining: Int? = null,
    val failedChannelIds: Set<String> = emptySet(),
    val messageSnackbar: String? = null
)

class XtvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChannelRepository(application)

    private val _uiState = MutableStateFlow(
        XtvUiState(
            favoriteIds = repository.getFavoriteIds(),
            history = repository.getWatchHistory(),
            isDemoMode = repository.isDemoMode,
            failedChannelIds = repository.getFailedChannelIds()
        )
    )
    val uiState: StateFlow<XtvUiState> = _uiState.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadChannels(forceRefresh = false)
    }

    fun loadChannels(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val (channels, isOffline) = repository.loadChannels(forceRefresh = forceRefresh)

            val effectiveChannels = if (_uiState.value.isDemoMode) {
                repository.demoChannels
            } else {
                channels
            }

            val arabicList = effectiveChannels.filter { it.isArabic }
            val featured = effectiveChannels.firstOrNull { it.isArabic && it.isWorking }
                ?: effectiveChannels.firstOrNull()

            _uiState.update {
                it.copy(
                    channels = effectiveChannels,
                    arabicChannels = arabicList,
                    featuredChannel = featured,
                    isLoading = false,
                    isOffline = isOffline,
                    favoriteIds = repository.getFavoriteIds(),
                    failedChannelIds = repository.getFailedChannelIds()
                )
            }
        }
    }

    fun selectCategory(cat: String) {
        _uiState.update { it.copy(selectedCategory = cat) }
    }

    fun selectTab(tab: NavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun toggleFavorite(channel: Channel) {
        val updated = repository.toggleFavorite(channel.id)
        _uiState.update { state ->
            val set = state.favoriteIds.toMutableSet()
            if (updated) set.add(channel.id) else set.remove(channel.id)
            state.copy(
                favoriteIds = set,
                messageSnackbar = if (updated) "تمت إضافة ${channel.name} للمفضلة" else "تمت الإزالة من المفضلة"
            )
        }
    }

    fun openPlayer(channel: Channel) {
        repository.addToHistory(channel)
        _uiState.update {
            it.copy(
                activePlayingChannel = channel,
                showPlayer = true,
                history = repository.getWatchHistory()
            )
        }
    }

    fun closePlayer() {
        _uiState.update { it.copy(showPlayer = false) }
    }

    fun playNextChannel() {
        val current = _uiState.value.activePlayingChannel ?: return
        val list = getActiveChannelList()
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && list.isNotEmpty()) {
            val nextIndex = (currentIndex + 1) % list.size
            openPlayer(list[nextIndex])
        }
    }

    fun playPreviousChannel() {
        val current = _uiState.value.activePlayingChannel ?: return
        val list = getActiveChannelList()
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && list.isNotEmpty()) {
            val prevIndex = if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1
            openPlayer(list[prevIndex])
        }
    }

    private fun getActiveChannelList(): List<Channel> {
        val state = _uiState.value
        return when (state.selectedCategory) {
            "عربي" -> if (state.arabicChannels.isNotEmpty()) state.arabicChannels else state.channels
            "الكل" -> state.channels
            "قنواتي" -> state.channels.filter { it.isCustom }
            else -> state.channels.filter { it.category == state.selectedCategory }
        }.ifEmpty { state.channels }
    }

    fun markCurrentChannelFailed() {
        val current = _uiState.value.activePlayingChannel ?: return
        repository.markChannelFailed(current.id)
        val updatedFailed = repository.getFailedChannelIds()
        _uiState.update { state ->
            val updatedChannels = state.channels.map {
                if (it.id == current.id) it.copy(isWorking = false) else it
            }
            state.copy(
                channels = updatedChannels,
                failedChannelIds = updatedFailed,
                messageSnackbar = "تم تسجيل تعذر تشغيل ${current.name}"
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250) // Debounce
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                _uiState.update { it.copy(searchResults = emptyList()) }
            } else {
                val results = _uiState.value.channels.filter { channel ->
                    channel.name.contains(trimmed, ignoreCase = true) ||
                            channel.category.contains(trimmed, ignoreCase = true) ||
                            (channel.country?.contains(trimmed, ignoreCase = true) == true)
                }.take(60)
                _uiState.update { it.copy(searchResults = results) }
            }
        }
    }

    fun executeSearch(term: String) {
        onSearchQueryChanged(term)
        _uiState.update { state ->
            val updated = (listOf(term) + state.recentSearches).distinct().take(8)
            state.copy(recentSearches = updated)
        }
    }

    fun clearRecentSearches() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null || minutes <= 0) {
            _uiState.update { it.copy(sleepTimerMinutes = null, sleepTimerSecondsRemaining = null) }
            return
        }

        var remaining = minutes * 60
        _uiState.update { it.copy(sleepTimerMinutes = minutes, sleepTimerSecondsRemaining = remaining) }

        sleepTimerJob = viewModelScope.launch {
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(sleepTimerSecondsRemaining = remaining) }
            }
            // Timer expired, close player
            closePlayer()
            _uiState.update {
                it.copy(
                    sleepTimerMinutes = null,
                    sleepTimerSecondsRemaining = null,
                    messageSnackbar = "انتهى مؤقت النوم وتم إيقاف المشغل"
                )
            }
        }
    }

    fun toggleDemoMode() {
        val next = !_uiState.value.isDemoMode
        repository.isDemoMode = next
        _uiState.update { it.copy(isDemoMode = next) }
        loadChannels(forceRefresh = false)
    }

    fun clearFavorites() {
        repository.clearFavorites()
        _uiState.update {
            it.copy(
                favoriteIds = emptySet(),
                messageSnackbar = "تم إفراغ قائمة المفضلة"
            )
        }
    }

    fun clearHistory() {
        repository.clearHistory()
        _uiState.update {
            it.copy(
                history = emptyList(),
                messageSnackbar = "تم مسح سجل المشاهدة"
            )
        }
    }

    fun clearCache() {
        repository.clearCache()
        _uiState.update { it.copy(messageSnackbar = "تم مسح الذاكرة المؤقتة") }
        loadChannels(forceRefresh = true)
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(messageSnackbar = null) }
    }
}
