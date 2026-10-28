package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.Channel
import com.example.data.model.WatchHistoryItem
import com.example.data.parser.M3uParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class ChannelRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("xtv_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val cacheFile = File(context.filesDir, "channels_cache.json")

    // Curated high-availability live HLS streams (working guaranteed)
    val demoChannels = listOf(
        Channel(
            id = "demo_local_sample",
            name = "بث XTV التجريبي الفاخر",
            streamUrl = "android.resource://${context.packageName}/raw/xtv_sample",
            logoUrl = null,
            category = "عام",
            country = "محلي",
            isArabic = true,
            resolution = "1080p FHD"
        ),
        Channel(
            id = "demo_aljazeera",
            name = "الجزيرة الإخبارية",
            streamUrl = "https://live-hls-web-aje.getaj.net/AJE/01.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Al_Jazeera_English_logo.svg/300px-Al_Jazeera_English_logo.svg.png",
            category = "أخبار",
            country = "عربي",
            isArabic = true,
            resolution = "1080p FHD"
        ),
        Channel(
            id = "demo_skynews",
            name = "سكاي نيوز عربية",
            streamUrl = "https://stream.skynewsarabia.com/hls/sna_720.m3u8",
            logoUrl = "https://images.skynewsarabia.com/images/v1/2021/07/07/1449339/800/450/1-1449339.png",
            category = "أخبار",
            country = "عربي",
            isArabic = true,
            resolution = "720p HD"
        ),
        Channel(
            id = "demo_dw_arabia",
            name = "دويتشه فيله DW عربية",
            streamUrl = "https://dwamdstream104.akamaized.net/hls/live/2015530/dwstream104/index.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/Deutsche_Welle_logo.svg/300px-Deutsche_Welle_logo.svg.png",
            category = "أخبار",
            country = "عربي",
            isArabic = true,
            resolution = "1080p FHD"
        ),
        Channel(
            id = "demo_france24_ar",
            name = "فرانس 24 العربية",
            streamUrl = "https://static.france24.com/live/F24_AR_HI_HLS/live_tv.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/65/France_24_logo.svg/300px-France_24_logo.svg.png",
            category = "أخبار",
            country = "عربي",
            isArabic = true,
            resolution = "720p HD"
        ),
        Channel(
            id = "demo_trt_arabi",
            name = "تي آر تي عربي TRT",
            streamUrl = "https://tv-trtarabi.medya.trt.com.tr/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/00/TRT_Arabi_logo.png/300px-TRT_Arabi_logo.png",
            category = "أخبار",
            country = "عربي",
            isArabic = true,
            resolution = "1080p FHD"
        ),
        Channel(
            id = "demo_quran_makkah",
            name = "إذاعة القرآن الكريم - مكة",
            streamUrl = "https://stream.radiojar.com/4wqre23fytzuv",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7a/Quran_Kareem_Logo.png/300px-Quran_Kareem_Logo.png",
            category = "ديني",
            country = "عربي",
            isArabic = true,
            resolution = "HD"
        ),
        Channel(
            id = "demo_sunnah_madinah",
            name = "قناة السنة النبوية - المدينة",
            streamUrl = "https://stream.radiojar.com/0t523w0272zuv",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Sunnah_Channel_Logo.png/300px-Sunnah_Channel_Logo.png",
            category = "ديني",
            country = "عربي",
            isArabic = true,
            resolution = "HD"
        ),
        Channel(
            id = "demo_nasa_tv",
            name = "NASA TV Live",
            streamUrl = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/300px-NASA_logo.svg.png",
            category = "وثائقي",
            country = "US",
            isArabic = false,
            resolution = "1080p FHD"
        ),
        Channel(
            id = "demo_redbull",
            name = "Red Bull TV Live",
            streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f5/Red_Bull_TV_logo.svg/300px-Red_Bull_TV_logo.svg.png",
            category = "رياضة",
            country = "AT",
            isArabic = false,
            resolution = "1080p FHD"
        ),
        Channel(
            id = "demo_bbb",
            name = "XTV Cinema Test 4K",
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            logoUrl = null,
            category = "أفلام",
            country = "عالمي",
            isArabic = true,
            resolution = "4K UHD"
        )
    )

    suspend fun loadChannels(forceRefresh: Boolean = false): Pair<List<Channel>, Boolean> = withContext(Dispatchers.IO) {
        var isOffline = false

        // 1. Check if we have cached channels and don't force refresh
        if (!forceRefresh && cacheFile.exists()) {
            val cached = readCache()
            if (cached.isNotEmpty()) {
                val markedCached = applyWorkingAndCustomFlags(cached)
                return@withContext Pair(markedCached, false)
            }
        }

        // 2. Fetch live data from sources
        var fetchedChannels: List<Channel>? = null
        try {
            val primaryChannels = fetchM3uChannels("https://iptv-org.github.io/iptv/index.m3u")
            val secondaryChannels = fetchCustomChannels("https://raw.githubusercontent.com/azalkmzc-lab/sarib-TV-2/refs/heads/main/channels.json")

            val combined = ArrayList<Channel>()
            val seenUrls = HashSet<String>()

            // Always prioritize reliable demo channels at top of lists
            for (ch in demoChannels) {
                if (seenUrls.add(ch.streamUrl)) {
                    combined.add(ch)
                }
            }

            // Add custom channels
            for (ch in secondaryChannels) {
                if (seenUrls.add(ch.streamUrl)) {
                    combined.add(ch)
                }
            }

            // Add primary channels
            for (ch in primaryChannels) {
                if (seenUrls.add(ch.streamUrl)) {
                    combined.add(ch)
                }
            }

            if (combined.isNotEmpty()) {
                fetchedChannels = combined
                saveCache(combined)
            }
        } catch (e: Exception) {
            Log.e("ChannelRepo", "Network error fetching channels: ${e.message}")
            isOffline = true
        }

        // If fetch failed, fallback to cache
        if (fetchedChannels == null) {
            if (cacheFile.exists()) {
                fetchedChannels = readCache()
                isOffline = true
            }
        }

        // If still empty, use demoChannels
        val result = fetchedChannels?.ifEmpty { demoChannels } ?: demoChannels
        val marked = applyWorkingAndCustomFlags(result)
        Pair(marked, isOffline)
    }

    private fun fetchM3uChannels(url: String): List<Channel> {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "XTV-Player/1.0")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Failed M3U HTTP ${response.code}")
        val body = response.body?.string() ?: throw Exception("Empty M3U response")
        return M3uParser.parse(body)
    }

    private fun fetchCustomChannels(url: String): List<Channel> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "XTV-Player/1.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            parseSecondaryJson(body)
        } catch (e: Exception) {
            Log.w("ChannelRepo", "Secondary source failed or empty: ${e.message}")
            emptyList()
        }
    }

    private fun parseSecondaryJson(jsonString: String): List<Channel> {
        val list = mutableListOf<Channel>()
        try {
            val root = JSONObject(jsonString)
            val channelsArray = root.optJSONArray("channels") ?: JSONArray()
            for (i in 0 until channelsArray.length()) {
                val item = channelsArray.optJSONObject(i) ?: continue
                val name = item.optString("name", item.optString("title", "قناة بدون اسم"))
                val streamUrl = item.optString("url", item.optString("stream", item.optString("link", "")))
                if (streamUrl.isEmpty()) continue
                val logo = item.optString("logo", item.optString("image", ""))
                val category = item.optString("category", item.optString("group", "قنواتي"))

                list.add(
                    Channel(
                        id = "custom_${streamUrl.hashCode()}",
                        name = name,
                        streamUrl = streamUrl,
                        logoUrl = logo.ifEmpty { null },
                        category = if (category.isEmpty()) "قنواتي" else category,
                        country = "خاص",
                        isArabic = true,
                        isCustom = true,
                        isWorking = true,
                        resolution = "HD"
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("ChannelRepo", "Error parsing secondary json: ${e.message}")
        }
        return list
    }

    private fun saveCache(channels: List<Channel>) {
        try {
            val array = JSONArray()
            // Cache up to first 2500 channels for memory & disk safety
            val toCache = channels.take(2500)
            for (ch in toCache) {
                val obj = JSONObject()
                obj.put("id", ch.id)
                obj.put("name", ch.name)
                obj.put("streamUrl", ch.streamUrl)
                obj.put("logoUrl", ch.logoUrl ?: "")
                obj.put("category", ch.category)
                obj.put("country", ch.country ?: "")
                obj.put("isArabic", ch.isArabic)
                obj.put("isCustom", ch.isCustom)
                obj.put("resolution", ch.resolution ?: "HD")
                array.put(obj)
            }
            cacheFile.writeText(array.toString())
        } catch (e: Exception) {
            Log.e("ChannelRepo", "Failed saving cache: ${e.message}")
        }
    }

    private fun readCache(): List<Channel> {
        return try {
            val content = cacheFile.readText()
            val array = JSONArray(content)
            val list = mutableListOf<Channel>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Channel(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        streamUrl = obj.getString("streamUrl"),
                        logoUrl = obj.optString("logoUrl").ifEmpty { null },
                        category = obj.optString("category", "عام"),
                        country = obj.optString("country").ifEmpty { null },
                        isArabic = obj.optBoolean("isArabic", false),
                        isCustom = obj.optBoolean("isCustom", false),
                        isWorking = true,
                        resolution = obj.optString("resolution", "HD")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("ChannelRepo", "Failed reading cache: ${e.message}")
            emptyList()
        }
    }

    fun clearCache() {
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    // --- Favorites ---
    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet("favorite_ids", emptySet()) ?: emptySet()
    }

    fun toggleFavorite(channelId: String): Boolean {
        val current = getFavoriteIds().toMutableSet()
        val isFav: Boolean
        if (current.contains(channelId)) {
            current.remove(channelId)
            isFav = false
        } else {
            current.add(channelId)
            isFav = true
        }
        prefs.edit().putStringSet("favorite_ids", current).apply()
        return isFav
    }

    fun clearFavorites() {
        prefs.edit().remove("favorite_ids").apply()
    }

    // --- Broken Channels Tracking ---
    fun getFailedChannelIds(): Set<String> {
        return prefs.getStringSet("failed_channel_ids", emptySet()) ?: emptySet()
    }

    fun markChannelFailed(channelId: String) {
        val current = getFailedChannelIds().toMutableSet()
        current.add(channelId)
        prefs.edit().putStringSet("failed_channel_ids", current).apply()
    }

    private fun applyWorkingAndCustomFlags(channels: List<Channel>): List<Channel> {
        val failed = getFailedChannelIds()
        return channels.map {
            if (failed.contains(it.id)) it.copy(isWorking = false) else it
        }
    }

    // --- History ---
    fun getWatchHistory(): List<WatchHistoryItem> {
        val raw = prefs.getString("watch_history_json", null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<WatchHistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val chObj = obj.getJSONObject("channel")
                val channel = Channel(
                    id = chObj.getString("id"),
                    name = chObj.getString("name"),
                    streamUrl = chObj.getString("streamUrl"),
                    logoUrl = chObj.optString("logoUrl").ifEmpty { null },
                    category = chObj.optString("category", "عام"),
                    country = chObj.optString("country").ifEmpty { null },
                    isArabic = chObj.optBoolean("isArabic", true),
                    isCustom = chObj.optBoolean("isCustom", false),
                    isWorking = chObj.optBoolean("isWorking", true),
                    resolution = chObj.optString("resolution", "HD")
                )
                val ts = obj.optLong("timestamp", System.currentTimeMillis())
                list.add(WatchHistoryItem(channel, ts))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addToHistory(channel: Channel) {
        val current = getWatchHistory().toMutableList()
        current.removeAll { it.channel.id == channel.id || it.channel.streamUrl == channel.streamUrl }
        current.add(0, WatchHistoryItem(channel, System.currentTimeMillis()))
        val trimmed = current.take(30)
        saveHistory(trimmed)
    }

    private fun saveHistory(items: List<WatchHistoryItem>) {
        try {
            val array = JSONArray()
            for (item in items) {
                val obj = JSONObject()
                obj.put("timestamp", item.watchedTimestamp)
                val chObj = JSONObject()
                chObj.put("id", item.channel.id)
                chObj.put("name", item.channel.name)
                chObj.put("streamUrl", item.channel.streamUrl)
                chObj.put("logoUrl", item.channel.logoUrl ?: "")
                chObj.put("category", item.channel.category)
                chObj.put("country", item.channel.country ?: "")
                chObj.put("isArabic", item.channel.isArabic)
                chObj.put("isCustom", item.channel.isCustom)
                chObj.put("isWorking", item.channel.isWorking)
                chObj.put("resolution", item.channel.resolution ?: "HD")
                obj.put("channel", chObj)
                array.put(obj)
            }
            prefs.edit().putString("watch_history_json", array.toString()).apply()
        } catch (e: Exception) {
            Log.e("ChannelRepo", "Error saving history: ${e.message}")
        }
    }

    fun clearHistory() {
        prefs.edit().remove("watch_history_json").apply()
    }

    // --- Settings Preferences ---
    var isDemoMode: Boolean
        get() = prefs.getBoolean("pref_demo_mode", false)
        set(value) = prefs.edit().putBoolean("pref_demo_mode", value).apply()

    var isDataSaver: Boolean
        get() = prefs.getBoolean("pref_data_saver", false)
        set(value) = prefs.edit().putBoolean("pref_data_saver", value).apply()

    var isAutoPlay: Boolean
        get() = prefs.getBoolean("pref_auto_play", true)
        set(value) = prefs.edit().putBoolean("pref_auto_play", value).apply()

    var defaultQuality: String
        get() = prefs.getString("pref_default_quality", "تلقائي") ?: "تلقائي"
        set(value) = prefs.edit().putString("pref_default_quality", value).apply()
}
