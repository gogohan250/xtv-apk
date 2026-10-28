package com.example.data.model

data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val category: String = "عام",
    val country: String? = null,
    val isArabic: Boolean = false,
    val isCustom: Boolean = false,
    val isWorking: Boolean = true,
    val resolution: String? = "HD"
)

data class WatchHistoryItem(
    val channel: Channel,
    val watchedTimestamp: Long = System.currentTimeMillis()
)
