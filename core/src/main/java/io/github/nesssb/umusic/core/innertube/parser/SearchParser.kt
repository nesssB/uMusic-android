package io.github.nesssb.umusic.core.innertube.parser

import io.github.nesssb.umusic.core.innertube.InnerTubeParseException
import io.github.nesssb.umusic.core.innertube.model.Artist
import io.github.nesssb.umusic.core.innertube.model.ArtistItem
import io.github.nesssb.umusic.core.innertube.model.ItemsPage
import io.github.nesssb.umusic.core.innertube.model.SearchPage
import io.github.nesssb.umusic.core.innertube.model.YTItem
import io.github.nesssb.umusic.core.innertube.response.MusicCardShelfRenderer
import io.github.nesssb.umusic.core.innertube.response.SearchResponse

/** Parses the responses of the `search` endpoint. */
internal object SearchParser {

    /**
     * Unfiltered searches return the top result card followed by each result in its own item
     * section. Filtered searches return a single shelf that continues on further pages.
     */
    fun parse(response: SearchResponse): SearchPage {
        val sectionList = response.contents?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer
            ?: throw InnerTubeParseException("Search response without results")

        var topResult: YTItem? = null
        var topResultItems = emptyList<YTItem>()
        val items = mutableListOf<YTItem>()
        var continuation: String? = null
        for (section in sectionList.contents.orEmpty()) {
            val card = section.musicCardShelfRenderer
            if (card != null) {
                val top = card.toYTItem()
                val cardItems = card.contents.orEmpty().mapNotNull { it.toYTItem() }
                // The songs in an artist's card don't repeat the artist
                topResultItems = if (top is ArtistItem) {
                    cardItems.map { it.withDefaultArtist(Artist(top.title, top.id)) }
                } else {
                    cardItems
                }
                topResult = top
            }
            section.itemSectionRenderer?.contents?.mapNotNullTo(items) { it.toYTItem() }
            section.musicShelfRenderer?.let { shelf ->
                shelf.contents?.mapNotNullTo(items) { it.toYTItem() }
                continuation = shelf.continuations.token() ?: continuation
            }
        }
        return SearchPage(topResult, topResultItems, items, continuation)
    }

    fun parseContinuation(response: SearchResponse): ItemsPage<YTItem> {
        val shelf = response.continuationContents?.musicShelfContinuation
            ?: throw InnerTubeParseException("Search continuation without results")
        return ItemsPage(
            items = shelf.contents.orEmpty().mapNotNull { it.toYTItem() },
            continuation = shelf.continuations.token(),
        )
    }

    private fun MusicCardShelfRenderer.toYTItem(): YTItem? = itemFor(
        endpoint = title?.runs?.firstOrNull()?.navigationEndpoint ?: onTap ?: return null,
        title = title?.text ?: return null,
        subtitle = subtitleOf(subtitle?.runs),
        thumbnailUrl = thumbnail?.largestUrl,
    )
}
