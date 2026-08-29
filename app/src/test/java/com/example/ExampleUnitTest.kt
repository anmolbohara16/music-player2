package com.example

import com.example.model.Song
import com.example.model.SortOption
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testMostPlayedSorting() {
    val songA = Song(id = 1, title = "Song A", artist = "Artist", durationMs = 1000, uri = "", playCount = 2)
    val songB = Song(id = 2, title = "Song B", artist = "Artist", durationMs = 1000, uri = "", playCount = 15)
    val songC = Song(id = 3, title = "Song C", artist = "Artist", durationMs = 1000, uri = "", playCount = 0)

    val list = listOf(songA, songB, songC)
    val sorted = list.sortedWith(
      compareByDescending<Song> { it.playCount }
        .thenByDescending { it.dateAdded }
        .thenBy { it.title.lowercase() }
    )

    assertEquals(listOf(songB, songA, songC), sorted)
  }
}

