package com.example

import com.example.data.parser.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testM3uParser_basicParsing() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="AlJazeera.qa" tvg-country="qa" tvg-logo="https://example.com/logo.png" group-title="News", الجزيرة الإخبارية [FHD]
            https://example.com/stream.m3u8
            #EXTINF:-1 tvg-id="Test.us" group-title="Adult", XXX Channel
            https://example.com/adult.m3u8
            #EXTINF:-1 tvg-id="BBC.uk" tvg-country="uk" group-title="Sports", BBC Sport
            https://example.com/bbc.m3u8
        """.trimIndent()

        val channels = M3uParser.parse(sampleM3u)

        assertEquals(2, channels.size) // Adult filtered out
        val alJazeera = channels[0]
        assertEquals("الجزيرة الإخبارية", alJazeera.name)
        assertEquals("https://example.com/stream.m3u8", alJazeera.streamUrl)
        assertEquals("أخبار", alJazeera.category)
        assertTrue(alJazeera.isArabic)

        val bbc = channels[1]
        assertEquals("BBC Sport", bbc.name)
        assertEquals("رياضة", bbc.category)
        assertFalse(bbc.isArabic)
    }

    @Test
    fun testM3uParser_nameWithCommas() {
        val sample = """
            #EXTINF:-1 tvg-id="Test.sa" group-title="Entertainment", قناة دبي, مباشر بجودة عالية
            https://example.com/dubai.m3u8
        """.trimIndent()

        val channels = M3uParser.parse(sample)
        assertEquals(1, channels.size)
        assertEquals("قناة دبي, مباشر بجودة عالية", channels[0].name)
        assertTrue(channels[0].isArabic)
    }
}
