package io.github.nesssb.umusic.core.innertube.parser

import io.github.nesssb.umusic.core.innertube.InnerTubeParseException
import io.github.nesssb.umusic.core.innertube.model.ItemsPage
import io.github.nesssb.umusic.core.innertube.model.NextPage
import io.github.nesssb.umusic.core.innertube.model.SongItem
import io.github.nesssb.umusic.core.innertube.response.NextResponse
import io.github.nesssb.umusic.core.innertube.response.Tabs

/** Parses the responses of the `next` endpoint, which fills the player's queue. */
internal object NextParser {

    fun parse(response: NextResponse): NextPage {
        val tabs = response.contents?.singleColumnMusicWatchNextResultsRenderer?.tabbedRenderer
            ?.watchNextTabbedResultsRenderer?.tabs
            ?: throw InnerTubeParseException("Next response without tabs")
        val panel = tabs.firstNotNullOfOrNull {
            it.tabRenderer?.content?.musicQueueRenderer?.content?.playlistPanelRenderer
        } ?: throw InnerTubeParseException("Next response without a queue")

        val items = mutableListOf<SongItem>()
        var currentIndex: Int? = null
        for (renderer in panel.contents.orEmpty().mapNotNull { it.playlistPanelVideoRenderer }) {
            val song = renderer.toSongItem() ?: continue
            if (renderer.selected) currentIndex = items.size
            items += song
        }
        val automix = panel.contents.orEmpty().firstNotNullOfOrNull {
            it.automixPreviewVideoRenderer?.content?.automixPlaylistVideoRenderer
                ?.navigationEndpoint?.watchPlaylistEndpoint
        }
        return NextPage(
            items = items,
            currentIndex = currentIndex,
            playlistId = panel.playlistId,
            continuation = panel.continuations.token(),
            automix = automix?.toWatchEndpoint(),
            lyricsBrowseId = tabs.browseIdOf(PAGE_TYPE_TRACK_LYRICS),
            relatedBrowseId = tabs.browseIdOf(PAGE_TYPE_TRACK_RELATED),
        )
    }

    fun parseContinuation(response: NextResponse): ItemsPage<SongItem> {
        val panel = response.continuationContents?.playlistPanelContinuation
            ?: throw InnerTubeParseException("Next continuation without a queue")
        return ItemsPage(
            items = panel.contents.orEmpty().mapNotNull {
                it.playlistPanelVideoRenderer?.toSongItem()
            },
            continuation = panel.continuations.token(),
        )
    }

    /** Browse id of the tab that opens [pageType]. Tabs without content have no endpoint. */
    private fun List<Tabs.Tab>.browseIdOf(pageType: String): String? = firstNotNullOfOrNull { tab ->
        tab.tabRenderer?.endpoint?.browseEndpoint?.takeIf { it.pageType == pageType }?.browseId
    }
}
