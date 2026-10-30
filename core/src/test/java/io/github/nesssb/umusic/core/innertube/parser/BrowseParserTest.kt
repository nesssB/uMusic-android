package io.github.nesssb.umusic.core.innertube.parser

import io.github.nesssb.umusic.core.innertube.InnerTubeParseException
import io.github.nesssb.umusic.core.innertube.fixture
import io.github.nesssb.umusic.core.innertube.model.Album
import io.github.nesssb.umusic.core.innertube.model.AlbumItem
import io.github.nesssb.umusic.core.innertube.model.Artist
import io.github.nesssb.umusic.core.innertube.model.ArtistItem
import io.github.nesssb.umusic.core.innertube.model.BrowseEndpoint
import io.github.nesssb.umusic.core.innertube.model.MusicVideoType
import io.github.nesssb.umusic.core.innertube.model.PlaylistItem
import io.github.nesssb.umusic.core.innertube.model.SongItem
import io.github.nesssb.umusic.core.innertube.model.WatchEndpoint
import io.github.nesssb.umusic.core.innertube.response.BrowseResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowseParserTest {

    private val daftPunk = Artist("Daft Punk", "UCRr1xG_2WIDs18a6cIiCxeA")

    @Test
    fun home() {
        val home = BrowseParser.home(fixture("browse/home.json"))

        assertEquals(11, home.chips.size)
        assertEquals("Podcasts", home.chips.first().title)
        assertEquals("FEmusic_home", home.chips.first().endpoint.browseId)
        assertNotNull(home.continuation)

        assertEquals(
            listOf("Quick picks", "New releases", "Featured playlists for you"),
            home.sections.map { it.title },
        )
        assertEquals(listOf(20, 10, 5), home.sections.map { it.items.size })

        // Quick picks plays everything instead of opening a page
        val quickPicks = home.sections[0]
        assertNull(quickPicks.moreEndpoint)
        val thriller = quickPicks.items.first() as SongItem
        assertEquals("Z85lxckrtzg", thriller.id)
        assertEquals("Thriller", thriller.title)
        val michaelJackson = Artist("Michael Jackson", "UCoIOOL7QKuBhQHVKL8y7BEQ")
        assertEquals(listOf(michaelJackson), thriller.artists)
        assertEquals(Album("Thriller", "MPREb_dqWTncCjkSp"), thriller.album)

        val newReleases = home.sections[1]
        assertEquals(
            BrowseEndpoint("FEmusic_new_releases_albums", "ggMEgALSAg%3D%3D"),
            newReleases.moreEndpoint,
        )
        val album = newReleases.items.first() as AlbumItem
        assertEquals("MPREb_1e4whXWc6mt", album.id)
        assertEquals(
            listOf(Artist("Queens of the Stone Age", "UCcYgNw9ne8v1cxcAD_sIOWQ")),
            album.artists,
        )
        assertEquals("OLAK5uy_lretgzwPNmIDxz5H3ryUDePY_kZbM6F1U", album.playlistId)
        assertTrue(album.explicit)

        val chart = home.sections[2].items.first() as PlaylistItem
        assertEquals("PL4fGSI1pDJn4jhQB4kb9M36dvVmJQPt4T", chart.id)
        // Charts link to the charts page, not to a channel
        assertNull(chart.author)
    }

    @Test
    fun `home continuation`() {
        val page = BrowseParser.homeContinuation(fixture("browse/home_continuation.json"))
        assertTrue(page.chips.isEmpty())
        assertEquals(listOf("Trending songs for you"), page.sections.map { it.title })
        assertEquals(14, page.sections.single().items.size)
        assertEquals("AC2jglfa_6I", page.sections.single().items.first().id)
        assertNull(page.continuation)
    }

    @Test
    fun album() {
        val page = BrowseParser.album(fixture("browse/album.json"), "MPREb_7ltM34kr0mH")

        assertEquals(
            AlbumItem(
                id = "MPREb_7ltM34kr0mH",
                title = "Discovery",
                artists = listOf(daftPunk),
                year = 2001,
                playlistId = "OLAK5uy_mz6eafmqdRHSaR4IwG0ll6J6rgv0_ZpGw",
                thumbnailUrl = page.album.thumbnailUrl,
            ),
            page.album,
        )
        assertTrue(page.album.thumbnailUrl!!.endsWith("w544-h544-l90-rj"))
        assertTrue(page.description!!.startsWith("Discovery is the second studio album"))
        assertEquals(listOf("Releases for you"), page.sections.map { it.title })

        assertEquals(14, page.songs.size)
        val first = page.songs.first()
        assertEquals("FGBhQbmPwH8", first.id)
        assertEquals("One More Time", first.title)
        assertEquals(321, first.durationSeconds)
        assertEquals(MusicVideoType.OfficialVideo, first.videoType)
        assertEquals("9E9C1F2D910459BA", first.setVideoId)
        // Tracks take the artist, album and cover from the album itself
        assertTrue(page.songs.all { it.artists == listOf(daftPunk) })
        assertTrue(page.songs.all { it.album == Album("Discovery", "MPREb_7ltM34kr0mH") })
        assertTrue(page.songs.all { it.thumbnailUrl == page.album.thumbnailUrl })
    }

    @Test
    fun artist() {
        val page = BrowseParser.artist(fixture("browse/artist.json"), daftPunk.id!!)

        assertEquals(daftPunk.id, page.artist.id)
        assertEquals("Daft Punk", page.artist.title)
        assertNotNull(page.artist.thumbnailUrl)
        assertTrue(page.description!!.startsWith("Daft Punk were a French electronic music duo"))
        assertEquals(
            WatchEndpoint("Rgrt_8mXrK8", "RDAOni3jl65KF37F9JYsJM8DGg", "wAEB8gECGAE%3D"),
            page.shuffleEndpoint,
        )
        assertEquals(
            WatchEndpoint("6u9xMu1Jqs4", "RDEMni3jl65KF37F9JYsJM8DGg", "wAEB"),
            page.radioEndpoint,
        )
        assertEquals(
            listOf(
                "Top songs",
                "Albums",
                "Singles & EPs",
                "Videos",
                "Live performances",
                "Featured on",
                "Playlists by Daft Punk",
                "Fans might also like",
            ),
            page.sections.map { it.title },
        )
    }

    @Test
    fun `artist top songs`() {
        val page = BrowseParser.artist(fixture("browse/artist.json"), daftPunk.id!!)
        val topSongs = page.sections.first()

        assertEquals(
            BrowseEndpoint("VLOLAK5uy_lNVBcjNtiCwq-n95uoxJ-Gd4vsfMkyZNs", "ggMCCAI%3D"),
            topSongs.moreEndpoint,
        )
        assertEquals(5, topSongs.items.size)
        val song = topSongs.items.first() as SongItem
        assertEquals("khnokW3Mw24", song.id)
        assertEquals(
            listOf(daftPunk, Artist("Julian Casablancas", "UCWhpbdGLnR8XtXLr0yl2nYA")),
            song.artists,
        )
        assertEquals(Album("Random Access Memories", "MPREb_K8qWMWVqXGi"), song.album)
    }

    @Test
    fun `artist releases are credited to the artist`() {
        val page = BrowseParser.artist(fixture("browse/artist.json"), daftPunk.id!!)
        val sections = page.sections.associateBy { it.title }

        val albums = sections.getValue("Albums")
        assertEquals(
            BrowseEndpoint("MPADUCRr1xG_2WIDs18a6cIiCxeA", "ggMIegYIARoCAQI%3D"),
            albums.moreEndpoint,
        )
        val album = albums.items.first() as AlbumItem
        assertEquals("MPREb_OgeEnoHTsCm", album.id)
        assertEquals(2023, album.year)
        assertEquals(listOf(daftPunk), album.artists)

        val video = sections.getValue("Videos").items.first() as SongItem
        assertEquals("a5uQMwRMHcs", video.id)
        assertEquals(MusicVideoType.OfficialVideo, video.videoType)
        assertEquals(listOf(daftPunk), video.artists)

        val similar = sections.getValue("Fans might also like").items.first()
        assertEquals(
            ArtistItem("UCkjEqPY4fcDZFYER-rF8kmA", "Mr. Oizo", similar.thumbnailUrl),
            similar,
        )
    }

    @Test
    fun playlist() {
        val page = BrowseParser.playlist(fixture("browse/playlist.json"))

        assertEquals("RDCLAK5uy_n20FRYQXNt1p1wS55Nj2r14IouO5weaYU", page.playlist.id)
        assertEquals("Presenting Daft Punk", page.playlist.title)
        // Shown in the avatar row, but without a channel to link to
        assertEquals(Artist("YouTube Music", null), page.playlist.author)
        assertEquals("The most played hits and essential tracks.", page.description)
        assertEquals(34, page.songs.size)
        assertNull(page.continuation)

        val first = page.songs.first()
        assertEquals("D8K90hX4PrE", first.id)
        assertEquals("Technologic", first.title)
        assertEquals(listOf(daftPunk), first.artists)
        assertEquals(176, first.durationSeconds)
        assertEquals("56B44F6D10557CC6", first.setVideoId)
    }

    @Test
    fun `user playlist with more than 100 tracks`() {
        val page = BrowseParser.playlist(fixture("browse/playlist_long.json"))

        assertEquals("PL-hvaaoSDXQKUScT-amoPP3cqzEQ607m_", page.playlist.id)
        assertEquals(Artist("GotWub", "UCF4_HJNY5D0vAA2ju_gWc8w"), page.playlist.author)
        assertEquals(100, page.songs.size)
        assertNotNull(page.continuation)
        assertEquals(listOf("pHWj-hRP-cY"), page.songs.filter { it.explicit }.map { it.id })
        assertEquals(10, page.songs.count { it.videoType == MusicVideoType.UserVideo })

        // A track that's no longer available, with an artist that has no channel
        val unavailable = page.songs.single { !it.playable }
        assertEquals("e0xO5KriwBw", unavailable.id)
        assertEquals("Drive Unreleased 1994", unavailable.title)
        assertEquals(listOf(Artist("Daft Punk", null)), unavailable.artists)
        assertEquals(441, unavailable.durationSeconds)
        assertNull(unavailable.videoType)
    }

    @Test
    fun `playlist continuation`() {
        val page =
            BrowseParser.playlistContinuation(fixture("browse/playlist_long_continuation.json"))
        assertEquals(43, page.items.size)
        assertNull(page.continuation)
        assertEquals(5, page.items.count { it.explicit })
        assertTrue(page.items.all { it.playable && it.artists.isNotEmpty() })
    }

    @Test
    fun related() {
        val page = BrowseParser.related(fixture("browse/related.json"))

        assertEquals(
            listOf(
                "You might also like",
                "Recommended playlists",
                "Other performances",
                "Similar artists",
                "Daft Punk",
            ),
            page.sections.map { it.title },
        )
        val youMightLike = page.sections.first()
        assertEquals(20, youMightLike.items.size)
        val song = youMightLike.items.first() as SongItem
        assertEquals("CIUzGxMkVDQ", song.id)
        assertEquals(Album("Alive 2007", "MPREb_CtOaObihCXA"), song.album)

        // The last row links to the artist through its title
        val moreFrom = page.sections.last()
        assertEquals("MORE FROM", moreFrom.strapline)
        assertEquals(BrowseEndpoint(daftPunk.id!!), moreFrom.moreEndpoint)
        assertTrue(page.sections[3].items.all { it is ArtistItem })
    }

    @Test
    fun `wrong kind of page throws`() {
        val home = fixture<BrowseResponse>("browse/home.json")
        assertThrows(InnerTubeParseException::class.java) { BrowseParser.album(home, "MPREb_x") }
        assertThrows(InnerTubeParseException::class.java) { BrowseParser.playlist(home) }
        assertFalse(runCatching { BrowseParser.home(home) }.isFailure)
    }
}
