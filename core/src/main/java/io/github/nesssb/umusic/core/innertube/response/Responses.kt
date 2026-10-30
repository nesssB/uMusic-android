package io.github.nesssb.umusic.core.innertube.response

import kotlinx.serialization.Serializable

/** Contents of a continuation page in the older format. Only one of the fields is set. */
@Serializable
internal data class ContinuationContents(
    val musicShelfContinuation: MusicShelfRenderer? = null,
    val sectionListContinuation: SectionListRenderer? = null,
    val musicPlaylistShelfContinuation: MusicPlaylistShelfRenderer? = null,
    val playlistPanelContinuation: PlaylistPanelRenderer? = null,
)

/** Contents of a continuation page in the newer format. */
@Serializable
internal data class ResponseReceivedAction(
    val appendContinuationItemsAction: AppendContinuationItemsAction? = null,
) {
    @Serializable
    data class AppendContinuationItemsAction(val continuationItems: List<ShelfItem>? = null)
}

@Serializable
internal data class SearchResponse(
    val contents: Contents? = null,
    val continuationContents: ContinuationContents? = null,
) {
    @Serializable
    data class Contents(val tabbedSearchResultsRenderer: Tabs? = null)
}

@Serializable
internal data class BrowseResponse(
    val contents: Contents? = null,
    val header: Header? = null,
    val continuationContents: ContinuationContents? = null,
    val onResponseReceivedActions: List<ResponseReceivedAction>? = null,
) {
    @Serializable
    data class Contents(
        /** Home and artist pages. */
        val singleColumnBrowseResultsRenderer: Tabs? = null,
        /** Album and playlist pages: header in the tab, track list in the secondary contents. */
        val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer? = null,
        /** Related page. */
        val sectionListRenderer: SectionListRenderer? = null,
    )

    @Serializable
    data class TwoColumnBrowseResultsRenderer(
        val tabs: List<Tabs.Tab> = emptyList(),
        val secondaryContents: SecondaryContents? = null,
    )

    @Serializable
    data class SecondaryContents(val sectionListRenderer: SectionListRenderer? = null)

    @Serializable
    data class Header(
        val musicImmersiveHeaderRenderer: MusicArtistHeaderRenderer? = null,
        val musicVisualHeaderRenderer: MusicArtistHeaderRenderer? = null,
    )
}

@Serializable
internal data class NextResponse(
    val contents: Contents? = null,
    val continuationContents: ContinuationContents? = null,
) {
    @Serializable
    data class Contents(val singleColumnMusicWatchNextResultsRenderer: WatchNextResults? = null)

    @Serializable
    data class WatchNextResults(val tabbedRenderer: TabbedRenderer? = null)

    @Serializable
    data class TabbedRenderer(val watchNextTabbedResultsRenderer: Tabs? = null)
}
