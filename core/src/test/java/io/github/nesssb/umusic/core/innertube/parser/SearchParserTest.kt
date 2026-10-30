package io.github.nesssb.umusic.core.innertube.parser

import io.github.nesssb.umusic.core.innertube.fixture
import io.github.nesssb.umusic.core.innertube.model.Album
import io.github.nesssb.umusic.core.innertube.model.AlbumItem
import io.github.nesssb.umusic.core.innertube.model.Artist
import io.github.nesssb.umusic.core.innertube.model.ArtistItem
import io.github.nesssb.umusic.core.innertube.model.MusicVideoType
import io.github.nesssb.umusic.core.innertube.model.PlaylistItem
import io.github.nesssb.umusic.core.innertube.model.SongItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchParserTest {

    private val daftPunk = Artist("Daft Punk", "UCRr1xG_2WIDs18a6cIiCxeA")

    private val page = SearchParser.parse(fixture("search/search.json"))

    @Test
    fun `top result is the artist card`() {
        val top = page.topResult as ArtistItem
        assertEquals(daftPunk.id, top.id)
        assertEquals("Daft Punk", top.title)
        assertNotNull(top.thumbnailUrl)
    }

    @Test
    fun `songs in the artist card are credited to the artist`() {
        val songs = page.topResultItems.map { it as SongItem }
        assertEquals(listOf("52fNscjkST4", "4qlDWL1kMcQ", "qXI87eMP-bs"), songs.map { it.id })
        assertEquals("Aerodynamic", songs[0].title)
        assertEquals(213, songs[0].durationSeconds)
        assertTrue(songs.all { it.artists == listOf(daftPunk) })
    }

    @Test
    fun `results skip podcasts, episodes and profiles`() {
        assertEquals(20, page.items.size)
        assertEquals(3, page.items.count { it is AlbumItem })
        assertEquals(3, page.items.count { it is ArtistItem })
        assertEquals(8, page.items.count { it is SongItem })
        assertEquals(6, page.items.count { it is PlaylistItem })
        assertNull(page.continuation)
    }

    @Test
    fun `album result`() {
        val album = page.items.first() as AlbumItem
        assertEquals("MPREb_K8qWMWVqXGi", album.id)
        assertEquals("Random Access Memories", album.title)
        assertEquals(listOf(daftPunk), album.artists)
        assertEquals(2013, album.year)
        assertEquals("OLAK5uy_kNhM2yaBTOVwrcZJepB1C9P3-n5_Sfy5c", album.playlistId)
        assertFalse(album.explicit)

        val alive = page.items.single { it.id == "MPREb_CtOaObihCXA" } as AlbumItem
        assertTrue(alive.explicit)
    }

    @Test
    fun `song results credit every linked artist`() {
        val song = page.items.single { it.title == "Touch (feat. Paul Williams)" } as SongItem
        assertEquals("RRMbhEdmhYw", song.id)
        assertEquals(
            listOf(daftPunk, Artist("Paul Williams", "UCPtp29kketDMCRawl_lT2oQ")),
            song.artists,
        )
        assertEquals(MusicVideoType.AudioTrack, song.videoType)
    }

    @Test
    fun `video results keep their kind`() {
        val official = page.items.single { it.id == "4whD6uAryMs" } as SongItem
        assertEquals(MusicVideoType.OfficialVideo, official.videoType)

        val upload = page.items.single { it.id == "Q5l2ChAqRDg" } as SongItem
        assertEquals(MusicVideoType.UserVideo, upload.videoType)
        assertEquals(listOf(Artist("John Schroter", "UC-L8sbkj8DxzCbK80MEySXQ")), upload.artists)
    }

    @Test
    fun `playlist results drop the VL prefix and keep linked authors`() {
        val community = page.items.single { it.title == "Todas las canciones de Daft Punk 😃" }
        assertEquals(
            PlaylistItem(
                id = "PLg5OoypX0j0lK8f28L2jz4cmJ9lHJ22re",
                title = "Todas las canciones de Daft Punk 😃",
                author = Artist("Manny/Doi Gribbleston", "UCdp_qUQwntxUliVJ5Naxuaw"),
                thumbnailUrl = community.thumbnailUrl,
            ),
            community,
        )
        // "YouTube Music" isn't linked, and the subtitle can't tell it apart from the item type
        val featured = page.items.single { it.title == "Presenting Daft Punk" } as PlaylistItem
        assertEquals("RDCLAK5uy_n20FRYQXNt1p1wS55Nj2r14IouO5weaYU", featured.id)
        assertNull(featured.author)
    }

    @Test
    fun `filtered search reads album and duration`() {
        val songsPage = SearchParser.parse(fixture("search/search_songs.json"))
        assertNull(songsPage.topResult)
        assertEquals(20, songsPage.items.size)
        assertTrue(songsPage.items.all { it is SongItem })
        assertNotNull(songsPage.continuation)

        val first = songsPage.items.first() as SongItem
        assertEquals("-xFur30vvVc", first.id)
        assertEquals("Touch (2021 Epilogue) (feat. Paul Williams)", first.title)
        assertEquals(
            Album("Random Access Memories (10th Anniversary Edition)", "MPREb_eMBjPmWySjR"),
            first.album,
        )
        assertEquals(180, first.durationSeconds)
    }

    @Test
    fun `filtered search continuation`() {
        val next = SearchParser.parseContinuation(fixture("search/search_songs_continuation.json"))
        assertEquals(20, next.items.size)
        assertNotNull(next.continuation)

        val first = next.items.first() as SongItem
        assertEquals("JhulBGMA7G4", first.id)
        assertEquals("Harder, Better, Faster, Stronger", first.title)
        assertEquals(listOf(daftPunk), first.artists)
        assertEquals("Discovery", first.album?.title)
        assertEquals(227, first.durationSeconds)
        assertEquals(2, next.items.count { (it as SongItem).explicit })
    }
}
