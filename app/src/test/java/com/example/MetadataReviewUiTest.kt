package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.metadata.model.*
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-mdpi", sdk = [36])
class MetadataReviewUiTest {
    @get:Rule val rule = createComposeRule()
    private val song = Song(1, "Northern Lights", "Artist", "Album", 180000, "local", genre = "Unknown")
    @Test fun applyingSelectedFieldsRequiresAnExplicitSelection() {
        var accepted = emptySet<String>()
        rule.setContent { MyApplicationTheme {
            MetadataSearchResultDialog(song, false, OnlineSongMetadata(song.title, song.artist, genre = "Folk"), null,
                { _, fields -> accepted = fields }, {}, {}, {})
        } }
        rule.onNodeWithText("Apply selected").assertIsNotEnabled()
        rule.onNodeWithText("Genre").performClick()
        rule.onNodeWithText("Apply selected").performClick()
        assertEquals(setOf("genre"), accepted)
        rule.onRoot().captureRoboImage(filePath = "build/outputs/screenshots/metadata-review.png")
    }
    @Test
    @Config(qualifiers = "w760dp-h360dp-mdpi", sdk = [36])
    fun nowPlayingLandscapeKeepsControlsReachable() {
        rule.setContent { MyApplicationTheme {
            NowPlayingSheet(song, false, 30000, 180000, false, RepeatMode.OFF, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
        } }
        rule.onRoot().captureRoboImage(filePath = "build/outputs/screenshots/player-landscape.png")
        rule.onNodeWithTag("playback_play_pause_fab").assertIsDisplayed()
        rule.onNodeWithTag("now_playing_favorite_button").assertIsDisplayed()
    }
    @Test fun metadataEditorRenders() {
        rule.setContent { MyApplicationTheme { EditMetadataDialog(song, {}, {}, {}) } }
        rule.onNodeWithTag("save_metadata_button").assertIsDisplayed()
        rule.onRoot().captureRoboImage(filePath = "build/outputs/screenshots/metadata-editor.png")
    }
}
