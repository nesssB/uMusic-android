package io.github.nesssb.umusic.core.innertube.parser

import io.github.nesssb.umusic.core.innertube.model.Album
import io.github.nesssb.umusic.core.innertube.model.Artist
import io.github.nesssb.umusic.core.innertube.response.NavigationEndpoint
import io.github.nesssb.umusic.core.innertube.response.Run
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ItemParserTest {

    @Test
    fun `parses durations`() {
        assertEquals(213, parseDuration("3:33"))
        assertEquals(4425, parseDuration("1:13:45"))
        assertNull(parseDuration("53M plays"))
        assertNull(parseDuration("2001"))
        assertNull(parseDuration(null))
    }

    @Test
    fun `subtitle doesn't depend on the language of the text`() {
        // "Canción • Daft Punk y Paul Williams • Random Access Memories • 3:00 • 2013"
        val subtitle = subtitleOf(
            listOf(
                Run("Canción"),
                Run(" • "),
                Run("Daft Punk", browse("UCRr1xG_2WIDs18a6cIiCxeA")),
                Run(" y "),
                Run("Paul Williams", browse("UCPtp29kketDMCRawl_lT2oQ")),
                Run(" • "),
                Run("Random Access Memories", browse("MPREb_K8qWMWVqXGi", PAGE_TYPE_ALBUM)),
                Run(" • "),
                Run("3:00"),
                Run(" • "),
                Run("2013"),
            ),
        )
        assertEquals(
            listOf(
                Artist("Daft Punk", "UCRr1xG_2WIDs18a6cIiCxeA"),
                Artist("Paul Williams", "UCPtp29kketDMCRawl_lT2oQ"),
            ),
            subtitle.artists,
        )
        assertEquals(Album("Random Access Memories", "MPREb_K8qWMWVqXGi"), subtitle.album)
        assertEquals(180, subtitle.durationSeconds)
        assertEquals(2013, subtitle.year)
    }

    @Test
    fun `unlinked artists fall back to the first group`() {
        val subtitle = subtitleOf(listOf(Run("Various Artists"), Run(" • "), Run("1.2K views")))
        assertEquals(emptyList<Artist>(), subtitle.artists)
        assertEquals(listOf(Artist("Various Artists", null)), subtitle.artistsOrFirstGroup())
    }

    private fun browse(browseId: String, pageType: String? = null) = NavigationEndpoint(
        browseEndpoint = NavigationEndpoint.Browse(
            browseId = browseId,
            browseEndpointContextSupportedConfigs = pageType?.let {
                NavigationEndpoint.Browse.ContextSupportedConfigs(
                    NavigationEndpoint.Browse.MusicConfig(it),
                )
            },
        ),
    )
}
