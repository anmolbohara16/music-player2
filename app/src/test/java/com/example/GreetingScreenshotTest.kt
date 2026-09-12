package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithText
import com.example.ui.screens.SongsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.model.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-mdpi", sdk = [36])
class GreetingScreenshotTest {
    @get:Rule val rule = createComposeRule()
    @Test fun songsScreenLight() { render(false, "songs-light") }
    @Test fun songsScreenDark() { render(true, "songs-dark") }
    private fun render(dark: Boolean, name: String) {
        val songs = listOf(Song(1, "Northern Lights", "The Evening Set", "Afterglow", 183000, "local"),
            Song(2, "Slow Sunday", "Mira", "Small Hours", 210000, "local", isFavorite = true))
        rule.setContent {
            MyApplicationTheme(darkTheme = dark) {
                androidx.compose.material3.Surface {
                    SongsScreen(songs, songs.first(), true, SortOption.TITLE_ASC,
                        onPlaySong = { _, _ -> }, onPlayAll = {}, onShuffleAll = {}, onOpenSortDialog = {},
                        onToggleFavorite = {}, onAddToQueue = {}, onPlayNext = {}, onAddToPlaylist = {},
                        onShowSongInfo = {}, onDeleteSong = {}, onRescanLibrary = {}, onLibraryWebSearch = {})
                }
            }
        }
        rule.onNodeWithText("Web Search").assertExists()
        rule.onNodeWithText("Rescan").assertExists()
        rule.onRoot().captureRoboImage(filePath = "build/outputs/screenshots/$name.png")
    }
}
