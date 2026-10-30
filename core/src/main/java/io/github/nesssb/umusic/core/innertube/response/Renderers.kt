package io.github.nesssb.umusic.core.innertube.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Row of a list: a song in a playlist, a search result, a track of an album... */
@Serializable
internal data class MusicResponsiveListItemRenderer(
    val flexColumns: List<FlexColumn> = emptyList(),
    val fixedColumns: List<FixedColumn>? = null,
    val thumbnail: ThumbnailRenderer? = null,
    val overlay: ThumbnailOverlay? = null,
    val navigationEndpoint: NavigationEndpoint? = null,
    val playlistItemData: PlaylistItemData? = null,
    val badges: List<Badge>? = null,
    val musicItemRendererDisplayPolicy: String? = null,
) {
    @Serializable
    data class FlexColumn(
        @SerialName("musicResponsiveListItemFlexColumnRenderer") val renderer: Column? = null,
    )

    @Serializable
    data class FixedColumn(
        @SerialName("musicResponsiveListItemFixedColumnRenderer") val renderer: Column? = null,
    )

    @Serializable
    data class Column(val text: Runs? = null)

    @Serializable
    data class PlaylistItemData(val videoId: String, val playlistSetVideoId: String? = null)
}

/** Card with a thumbnail on top and two lines of text below, as shown in carousels. */
@Serializable
internal data class MusicTwoRowItemRenderer(
    val title: Runs? = null,
    val subtitle: Runs? = null,
    val thumbnailRenderer: ThumbnailRenderer? = null,
    val thumbnailOverlay: ThumbnailOverlay? = null,
    val navigationEndpoint: NavigationEndpoint? = null,
    val subtitleBadges: List<Badge>? = null,
)

/** Entry of a shelf or carousel. Only one of the fields is set. */
@Serializable
internal data class ShelfItem(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer? = null,
    val continuationItemRenderer: ContinuationItemRenderer? = null,
)

/** Vertical list, such as an artist's top songs or the results of a filtered search. */
@Serializable
internal data class MusicShelfRenderer(
    val title: Runs? = null,
    val contents: List<ShelfItem>? = null,
    val continuations: List<Continuation>? = null,
    val bottomEndpoint: NavigationEndpoint? = null,
)

/** Track list of a playlist. */
@Serializable
internal data class MusicPlaylistShelfRenderer(
    val playlistId: String? = null,
    val contents: List<ShelfItem>? = null,
    val continuations: List<Continuation>? = null,
)

/** Horizontal row of items with a title. */
@Serializable
internal data class MusicCarouselShelfRenderer(
    val header: Header? = null,
    val contents: List<ShelfItem>? = null,
) {
    @Serializable
    data class Header(val musicCarouselShelfBasicHeaderRenderer: BasicHeader? = null)

    @Serializable
    data class BasicHeader(
        val title: Runs? = null,
        val strapline: Runs? = null,
        val moreContentButton: Button? = null,
    )
}

/** The "Top result" card of a search. */
@Serializable
internal data class MusicCardShelfRenderer(
    val title: Runs? = null,
    val subtitle: Runs? = null,
    val thumbnail: ThumbnailRenderer? = null,
    val onTap: NavigationEndpoint? = null,
    val contents: List<ShelfItem>? = null,
)

/** Generic container. Unfiltered searches wrap each result in one. */
@Serializable
internal data class ItemSectionRenderer(val contents: List<ShelfItem>? = null)

@Serializable
internal data class MusicDescriptionShelfRenderer(
    val header: Runs? = null,
    val description: Runs? = null,
)

/** Header of album and playlist pages. */
@Serializable
internal data class MusicResponsiveHeaderRenderer(
    val title: Runs? = null,
    val subtitle: Runs? = null,
    val secondSubtitle: Runs? = null,
    val straplineTextOne: Runs? = null,
    val thumbnail: ThumbnailRenderer? = null,
    val description: Description? = null,
    val buttons: List<HeaderButton>? = null,
    val facepile: Facepile? = null,
) {
    @Serializable
    data class Description(val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer? = null)

    @Serializable
    data class HeaderButton(val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null)

    /** Avatar and name of a playlist's owner. */
    @Serializable
    data class Facepile(val avatarStackViewModel: AvatarStackViewModel? = null)

    @Serializable
    data class AvatarStackViewModel(
        val text: Content? = null,
        val rendererContext: RendererContext? = null,
    )

    @Serializable
    data class Content(val content: String? = null)

    @Serializable
    data class RendererContext(val commandContext: CommandContext? = null)

    @Serializable
    data class CommandContext(val onTap: OnTap? = null)

    @Serializable
    data class OnTap(val innertubeCommand: NavigationEndpoint? = null)
}

/** Header of artist pages, with a full-width picture (immersive) or without one (visual). */
@Serializable
internal data class MusicArtistHeaderRenderer(
    val title: Runs? = null,
    val description: Runs? = null,
    val thumbnail: ThumbnailRenderer? = null,
    val foregroundThumbnail: ThumbnailRenderer? = null,
    val playButton: Button? = null,
    val startRadioButton: Button? = null,
)

@Serializable
internal data class SectionListRenderer(
    val contents: List<SectionListItem>? = null,
    val continuations: List<Continuation>? = null,
    val header: Header? = null,
) {
    @Serializable
    data class Header(val chipCloudRenderer: ChipCloudRenderer? = null)

    @Serializable
    data class ChipCloudRenderer(val chips: List<Chip>? = null)

    @Serializable
    data class Chip(val chipCloudChipRenderer: ChipCloudChipRenderer? = null)

    @Serializable
    data class ChipCloudChipRenderer(
        val text: Runs? = null,
        val navigationEndpoint: NavigationEndpoint? = null,
    )
}

/** Entry of a section list. Only one of the fields is set. */
@Serializable
internal data class SectionListItem(
    val musicCarouselShelfRenderer: MusicCarouselShelfRenderer? = null,
    val musicShelfRenderer: MusicShelfRenderer? = null,
    val musicPlaylistShelfRenderer: MusicPlaylistShelfRenderer? = null,
    val musicCardShelfRenderer: MusicCardShelfRenderer? = null,
    val itemSectionRenderer: ItemSectionRenderer? = null,
    val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer? = null,
    val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer? = null,
)

@Serializable
internal data class Tabs(val tabs: List<Tab> = emptyList()) {
    @Serializable
    data class Tab(val tabRenderer: TabRenderer? = null)

    @Serializable
    data class TabRenderer(
        val endpoint: NavigationEndpoint? = null,
        val content: TabContent? = null,
    )

    @Serializable
    data class TabContent(
        val sectionListRenderer: SectionListRenderer? = null,
        val musicQueueRenderer: MusicQueueRenderer? = null,
    )
}

/** Play queue of the player. */
@Serializable
internal data class MusicQueueRenderer(val content: Content? = null) {
    @Serializable
    data class Content(val playlistPanelRenderer: PlaylistPanelRenderer? = null)
}

@Serializable
internal data class PlaylistPanelRenderer(
    val playlistId: String? = null,
    val contents: List<PlaylistPanelItem>? = null,
    val continuations: List<Continuation>? = null,
)

/** Entry of the play queue. Only one of the fields is set. */
@Serializable
internal data class PlaylistPanelItem(
    val playlistPanelVideoRenderer: PlaylistPanelVideoRenderer? = null,
    val automixPreviewVideoRenderer: AutomixPreviewVideoRenderer? = null,
)

@Serializable
internal data class PlaylistPanelVideoRenderer(
    val videoId: String? = null,
    val title: Runs? = null,
    val longBylineText: Runs? = null,
    val lengthText: Runs? = null,
    val thumbnail: Thumbnails? = null,
    val navigationEndpoint: NavigationEndpoint? = null,
    val playlistSetVideoId: String? = null,
    val selected: Boolean = false,
    val badges: List<Badge>? = null,
)

/** Placeholder at the end of the queue for the mix that follows it. */
@Serializable
internal data class AutomixPreviewVideoRenderer(val content: Content? = null) {
    @Serializable
    data class Content(val automixPlaylistVideoRenderer: AutomixPlaylistVideoRenderer? = null)

    @Serializable
    data class AutomixPlaylistVideoRenderer(val navigationEndpoint: NavigationEndpoint? = null)
}
