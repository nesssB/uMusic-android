package io.github.nesssb.umusic.core.innertube.model

/** A page of items with the token to load the next one, or null when there are no more. */
data class ItemsPage<out T : YTItem>(val items: List<T>, val continuation: String?)

/** A titled row of items, as shown on the home, artist and related pages. */
data class Section(
    val title: String,
    /** Short line shown above the title, if any. */
    val strapline: String?,
    val items: List<YTItem>,
    /** Opens the full list behind the section's title or "More" button, if it has one. */
    val moreEndpoint: BrowseEndpoint?,
)

/** Results of the `search` endpoint, with or without a filter. */
data class SearchPage(
    /** Item of the "Top result" card, if the results start with one. */
    val topResult: YTItem?,
    /** Items listed inside the "Top result" card, such as the artist's popular songs. */
    val topResultItems: List<YTItem>,
    val items: List<YTItem>,
    /** Token for the next page. Only filtered searches have more than one. */
    val continuation: String?,
)

/** The home feed (`FEmusic_home`). */
data class HomePage(
    /** Mood and genre filters shown above the feed. Empty on continuation pages. */
    val chips: List<HomeChip>,
    val sections: List<Section>,
    val continuation: String?,
)

/** A home feed filter. Browsing [endpoint] loads the feed with the filter applied. */
data class HomeChip(val title: String, val endpoint: BrowseEndpoint)

data class AlbumPage(
    val album: AlbumItem,
    val description: String?,
    val songs: List<SongItem>,
    /** Rows shown after the track list, such as other versions of the album. */
    val sections: List<Section>,
)

data class ArtistPage(
    val artist: ArtistItem,
    val description: String?,
    val shuffleEndpoint: WatchEndpoint?,
    val radioEndpoint: WatchEndpoint?,
    /** Top songs, albums, singles, videos, similar artists and so on. */
    val sections: List<Section>,
)

data class PlaylistPage(
    val playlist: PlaylistItem,
    val description: String?,
    val songs: List<SongItem>,
    /** Token for the next tracks. YouTube Music sends them 100 at a time. */
    val continuation: String?,
)

/** The "Related" tab of the player, loaded from [NextPage.relatedBrowseId]. */
data class RelatedPage(val sections: List<Section>)

/** The play queue returned by the `next` endpoint. */
data class NextPage(
    val items: List<SongItem>,
    /** Index in [items] of the song being played. */
    val currentIndex: Int?,
    val playlistId: String?,
    /** Token for more items. Radios and mixes never run out of them. */
    val continuation: String?,
    /** Mix that keeps playing once [items] end, when the queue offers one. */
    val automix: WatchEndpoint?,
    /** Browse id of the song's lyrics, null when it has none. */
    val lyricsBrowseId: String?,
    /** Browse id of the songs, playlists and artists related to the song. */
    val relatedBrowseId: String?,
)
