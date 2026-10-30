package io.github.nesssb.umusic.core.innertube.parser

import io.github.nesssb.umusic.core.innertube.fixture
import io.github.nesssb.umusic.core.innertube.model.Album
import io.github.nesssb.umusic.core.innertube.model.Artist
import io.github.nesssb.umusic.core.innertube.model.MusicVideoType
import io.github.nesssb.umusic.core.innertube.model.WatchEndpoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NextParserTest {

    private val daftPunk = Artist("Daft Punk", "UCRr1xG_2WIDs18a6cIiCxeA")

    @Test
    fun `single song offers an automix`() {
        val page = NextParser.parse(fixture("next/next_song.json"))

        val song = page.items.single()
        assertEquals("OWiVJMgms9E", song.id)
        assertEquals("Voyager", song.title)
        assertEquals(listOf(daftPunk), song.artists)
        assertEquals(Album("Discovery", "MPREb_7ltM34kr0mH"), song.album)
        assertEquals(228, song.durationSeconds)
        assertEquals(MusicVideoType.AudioTrack, song.videoType)
        assertNotNull(song.thumbnailUrl)

        assertEquals(0, page.currentIndex)
        assertNull(page.playlistId)
        assertNull(page.continuation)
        assertEquals(
            WatchEndpoint(
                playlistId = "RDAMVMOWiVJMgms9E",
                params = "wAEB8gECeAHqBAtPV2lWSk1nbXM5RQ%3D%3D",
            ),
            page.automix,
        )
        assertEquals("MPLYt_7ltM34kr0mH-10", page.lyricsBrowseId)
        assertEquals("MPTRt_7ltM34kr0mH-10", page.relatedBrowseId)
    }

    @Test
    fun `album queue starts at the selected track`() {
        val page = NextParser.parse(fixture("next/next_album.json"))

        assertEquals(14, page.items.size)
        assertEquals(1, page.currentIndex)
        assertEquals("L93-7vRfxNs", page.items[1].id)
        assertEquals("OLAK5uy_mz6eafmqdRHSaR4IwG0ll6J6rgv0_ZpGw", page.playlistId)
        assertNull(page.continuation)
        assertEquals(
            "RDAMPLOLAK5uy_mz6eafmqdRHSaR4IwG0ll6J6rgv0_ZpGw",
            page.automix?.playlistId,
        )
        assertEquals("MPLYt_4kU74ZL6mDE", page.lyricsBrowseId)

        // Videos list views and likes instead of the album
        val video = page.items.first()
        assertEquals("FGBhQbmPwH8", video.id)
        assertEquals(listOf(daftPunk), video.artists)
        assertNull(video.album)
        assertEquals(322, video.durationSeconds)
        assertEquals(MusicVideoType.OfficialVideo, video.videoType)
        assertEquals("9E9C1F2D910459BA", video.setVideoId)
    }

    @Test
    fun `radio continues indefinitely`() {
        val page = NextParser.parse(fixture("next/next_radio.json"))

        assertEquals(50, page.items.size)
        assertEquals(0, page.currentIndex)
        assertEquals("RDAMVMOWiVJMgms9E", page.playlistId)
        assertNotNull(page.continuation)
        assertNull(page.automix)

        val second = page.items[1]
        assertEquals("Smi8vAbm_8o", second.id)
        assertEquals(listOf(daftPunk), second.artists)
        assertEquals("Discovery", second.album?.title)
    }

    @Test
    fun `radio continuation`() {
        val page = NextParser.parseContinuation(fixture("next/next_radio_continuation.json"))
        assertEquals(49, page.items.size)
        assertNotNull(page.continuation)
    }
}
