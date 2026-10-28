package com.example.data.parser

import com.example.data.model.Channel
import java.io.BufferedReader
import java.io.StringReader

object M3uParser {

    private val ARABIC_COUNTRIES = setOf(
        "sa", "eg", "ae", "qa", "kw", "om", "jo", "lb", "iq", "sy",
        "ma", "dz", "tn", "ye", "sd", "ly", "ps", "bh", "mr", "so", "dj", "km"
    )

    private val ADULT_KEYWORDS = listOf("xxx", "adult", "porn", "+18", "erotic", "nsfw")

    fun parse(m3uContent: String): List<Channel> {
        val reader = BufferedReader(StringReader(m3uContent))
        val channels = ArrayList<Channel>(1000)

        var currentExtInf: String? = null
        var line: String? = reader.readLine()

        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                line = reader.readLine()
                continue
            }

            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                currentExtInf = trimmed
            } else if (!trimmed.startsWith("#") && currentExtInf != null) {
                val streamUrl = trimmed
                if (streamUrl.startsWith("http://", ignoreCase = true) || streamUrl.startsWith("https://", ignoreCase = true)) {
                    val channel = parseExtInfLine(currentExtInf, streamUrl)
                    if (channel != null && !isAdult(channel)) {
                        channels.add(channel)
                    }
                }
                currentExtInf = null
            }
            line = reader.readLine()
        }

        return channels
    }

    private fun parseExtInfLine(extInf: String, streamUrl: String): Channel? {
        val commaIndex = findAttributeSeparatorComma(extInf)
        if (commaIndex == -1) return null

        val attributesPart = extInf.substring(8, commaIndex).trim()
        val rawName = extInf.substring(commaIndex + 1).trim()
        if (rawName.isEmpty()) return null

        val tvgId = extractAttribute(attributesPart, "tvg-id")
        val tvgLogo = extractAttribute(attributesPart, "tvg-logo").ifEmpty {
            extractAttribute(attributesPart, "logo")
        }
        val groupTitle = extractAttribute(attributesPart, "group-title")
        val tvgCountry = extractAttribute(attributesPart, "tvg-country").ifEmpty {
            extractCountryFromId(tvgId)
        }

        val isArabic = detectArabic(tvgId, tvgCountry, groupTitle, rawName)
        val normalizedCategory = normalizeCategory(groupTitle, rawName)

        val id = if (tvgId.isNotEmpty()) tvgId else "ch_${streamUrl.hashCode()}"

        return Channel(
            id = id,
            name = cleanChannelName(rawName),
            streamUrl = streamUrl,
            logoUrl = tvgLogo.ifEmpty { null },
            category = normalizedCategory,
            country = tvgCountry.ifEmpty { if (isArabic) "عربي" else null },
            isArabic = isArabic,
            isCustom = false,
            isWorking = true,
            resolution = if (rawName.contains("FHD", ignoreCase = true) || rawName.contains("1080")) "FHD"
            else if (rawName.contains("4K", ignoreCase = true) || rawName.contains("UHD")) "4K"
            else "HD"
        )
    }

    private fun extractAttribute(attributes: String, key: String): String {
        val pattern = """$key="([^"]*)"""".toRegex(RegexOption.IGNORE_CASE)
        val match = pattern.find(attributes)
        return match?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun findAttributeSeparatorComma(line: String): Int {
        var inQuotes = false
        for (i in line.indices) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                return i
            }
        }
        return -1
    }

    private fun cleanChannelName(name: String): String {
        return name
            .replace("""\[.*?\]""".toRegex(), "")
            .replace("""\(.*?\)""".toRegex(), "")
            .trim()
            .ifEmpty { name }
    }

    private fun extractCountryFromId(tvgId: String): String {
        if (tvgId.isEmpty()) return ""
        val parts = tvgId.split(".")
        if (parts.size >= 2) {
            val last = parts.last().lowercase().trim()
            if (last.length == 2) return last
        }
        return ""
    }

    private fun detectArabic(tvgId: String, country: String, groupTitle: String, name: String): Boolean {
        val arabicRegex = """[\u0600-\u06FF]""".toRegex()
        if (arabicRegex.containsMatchIn(name) || arabicRegex.containsMatchIn(groupTitle)) {
            return true
        }

        val lowerId = tvgId.lowercase()
        val lowerCountry = country.lowercase()
        val lowerGroup = groupTitle.lowercase()

        if (ARABIC_COUNTRIES.contains(lowerCountry)) return true

        for (ac in ARABIC_COUNTRIES) {
            if (lowerId.endsWith(".$ac") || lowerId.contains(".$ac.") || lowerId.contains("-$ac")) {
                return true
            }
        }

        if (lowerGroup.contains("arabic") || lowerGroup.contains("arab") || lowerId.contains("arabic")) {
            return true
        }

        return false
    }

    private fun normalizeCategory(groupTitle: String, name: String): String {
        val text = "${groupTitle.lowercase()} ${name.lowercase()}"
        return when {
            text.contains("news") || text.contains("أخبار") || text.contains("اخبار") -> "أخبار"
            text.contains("sport") || text.contains("رياضة") || text.contains("رياضه") || text.contains("kooora") -> "رياضة"
            text.contains("movie") || text.contains("cinema") || text.contains("أفلام") || text.contains("افلام") || text.contains("film") -> "أفلام"
            text.contains("kid") || text.contains("child") || text.contains("أطفال") || text.contains("اطفال") || text.contains("cartoon") || text.contains("animation") -> "أطفال"
            text.contains("doc") || text.contains("وثائقي") || text.contains("national geographic") || text.contains("nature") -> "وثائقي"
            text.contains("religio") || text.contains("islam") || text.contains("quran") || text.contains("قرآن") || text.contains("ديني") || text.contains("sunnah") -> "ديني"
            text.contains("music") || text.contains("موسيقى") || text.contains("songs") -> "موسيقى"
            text.contains("entertain") || text.contains("series") || text.contains("مسلسلات") || text.contains("ترفيه") || text.contains("drama") || text.contains("دراما") -> "ترفيه"
            else -> "عام"
        }
    }

    private fun isAdult(channel: Channel): Boolean {
        val lowerGroup = channel.category.lowercase()
        val lowerName = channel.name.lowercase()
        for (kw in ADULT_KEYWORDS) {
            if (lowerGroup.contains(kw) || lowerName.contains(kw)) {
                return true
            }
        }
        return false
    }
}
