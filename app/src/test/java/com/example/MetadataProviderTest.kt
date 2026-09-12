package com.example

import com.example.metadata.provider.*
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MetadataProviderTest {
    private fun client(block: (Request) -> Pair<Int, String>) = OkHttpClient.Builder().addInterceptor { chain ->
        val (code, body) = block(chain.request())
        Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(code).message("Test").body(body.toResponseBody()).build()
    }.build()
    @Test fun lyricsSearchRejectsUnrelatedFirstResult() = runTest {
        val provider = LrclibLyricsProvider(client { request ->
            if (request.url.encodedPath.endsWith("get")) 404 to "{}" else 200 to """[
                {"trackName":"Light","artistName":"Other Artist","duration":180,"plainLyrics":"Wrong lyrics"},
                {"trackName":"Light (Live)","artistName":"Artist","duration":180,"plainLyrics":"Wrong version"},
                {"trackName":"Light","artistName":"Artist","duration":180,"plainLyrics":"Correct lyrics"}
            ]"""
        })
        assertEquals("Correct lyrics", provider.fetchLyrics("Light", "Artist", "", 180000).first)
    }
    @Test fun missingLyricsAreNullAndNoTitleOnlyLookup() = runTest {
        var calls = 0
        val provider = LrclibLyricsProvider(client { calls++; 404 to "{}" })
        assertEquals(null to null, provider.fetchLyrics("Light", "Unknown Artist", "", 180000))
        assertEquals(0, calls)
        assertEquals(null to null, provider.fetchLyrics("Light", "Artist", "", 180000))
    }
    @Test fun spotifyLinkNeverInventsArtistOrPerformsCatalogSubstitution() = runTest {
        var calls = 0
        val provider = LinkMetadataProvider(client { calls++; 200 to """{"title":"Light","thumbnail_url":"https://image.test/cover.jpg"}""" })
        val result = provider.resolveLink("https://open.spotify.com/track/abc123")!!
        assertEquals(1, calls); assertEquals("Light", result.title); assertEquals("", result.artist)
        assertEquals(0, result.durationMs); assertEquals("", result.album); assertEquals("abc123", result.spotifyId)
    }
    @Test fun apiFailureIsNotNoMatch() = runTest {
        val provider = ItunesMetadataProvider(client { 429 to "{}" })
        try { provider.searchMetadata("Light", "Artist"); fail("Expected rate limit failure") }
        catch (e: MetadataHttpException) { assertEquals(429, e.status) }
    }
    @Test fun missingProviderFieldsStayUnknown() = runTest {
        val provider = ItunesMetadataProvider(client { 200 to """{"results":[{"trackName":"Light","artistName":"Artist","trackTimeMillis":180000}]}""" })
        val result = provider.searchMetadata("Light", "Artist").single()
        assertNull(result.artworkUrl); assertEquals(0, result.discNumber); assertEquals("", result.albumArtist)
    }
}
