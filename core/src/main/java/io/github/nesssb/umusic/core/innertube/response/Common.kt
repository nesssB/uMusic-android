package io.github.nesssb.umusic.core.innertube.response

import kotlinx.serialization.Serializable

/*
 * Building blocks shared by the InnerTube renderers. Only the fields the parsers read are
 * declared; everything else is skipped while decoding.
 */

/** Text split into runs, some of which link somewhere. */
@Serializable
internal data class Runs(val runs: List<Run>? = null) {
    val text: String get() = runs.orEmpty().joinToString("") { it.text }
}

@Serializable
internal data class Run(val text: String = "", val navigationEndpoint: NavigationEndpoint? = null)

@Serializable
internal data class NavigationEndpoint(
    val watchEndpoint: Watch? = null,
    val watchPlaylistEndpoint: WatchPlaylist? = null,
    val browseEndpoint: Browse? = null,
) {
    @Serializable
    data class Watch(
        val videoId: String? = null,
        val playlistId: String? = null,
        val params: String? = null,
        val watchEndpointMusicSupportedConfigs: MusicSupportedConfigs? = null,
    ) {
        val musicVideoType: String?
            get() = watchEndpointMusicSupportedConfigs?.watchEndpointMusicConfig?.musicVideoType

        @Serializable
        data class MusicSupportedConfigs(val watchEndpointMusicConfig: MusicConfig? = null)

        @Serializable
        data class MusicConfig(val musicVideoType: String? = null)
    }

    @Serializable
    data class WatchPlaylist(val playlistId: String, val params: String? = null)

    @Serializable
    data class Browse(
        val browseId: String,
        val params: String? = null,
        val browseEndpointContextSupportedConfigs: ContextSupportedConfigs? = null,
    ) {
        val pageType: String?
            get() = browseEndpointContextSupportedConfigs
                ?.browseEndpointContextMusicConfig?.pageType

        @Serializable
        data class ContextSupportedConfigs(
            val browseEndpointContextMusicConfig: MusicConfig? = null,
        )

        @Serializable
        data class MusicConfig(val pageType: String? = null)
    }
}

@Serializable
internal data class Thumbnails(val thumbnails: List<Thumbnail> = emptyList()) {
    val largestUrl: String? get() = thumbnails.maxByOrNull { it.width ?: 0 }?.url
}

@Serializable
internal data class Thumbnail(val url: String, val width: Int? = null, val height: Int? = null)

@Serializable
internal data class ThumbnailRenderer(val musicThumbnailRenderer: MusicThumbnailRenderer? = null) {
    val largestUrl: String? get() = musicThumbnailRenderer?.thumbnail?.largestUrl

    @Serializable
    data class MusicThumbnailRenderer(val thumbnail: Thumbnails? = null)
}

@Serializable
internal data class Badge(val musicInlineBadgeRenderer: MusicInlineBadgeRenderer? = null) {
    @Serializable
    data class MusicInlineBadgeRenderer(val icon: Icon? = null)
}

@Serializable
internal data class Icon(val iconType: String? = null)

/** Play button drawn over an item's thumbnail. */
@Serializable
internal data class ThumbnailOverlay(
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer? = null,
) {
    val playEndpoint: NavigationEndpoint?
        get() = musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer
            ?.playNavigationEndpoint

    @Serializable
    data class MusicItemThumbnailOverlayRenderer(val content: Content? = null)

    @Serializable
    data class Content(val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null)
}

@Serializable
internal data class MusicPlayButtonRenderer(val playNavigationEndpoint: NavigationEndpoint? = null)

@Serializable
internal data class Button(val buttonRenderer: ButtonRenderer? = null) {
    @Serializable
    data class ButtonRenderer(val navigationEndpoint: NavigationEndpoint? = null)
}

/** Continuation token in the older format, attached to a shelf or section list. */
@Serializable
internal data class Continuation(
    val nextContinuationData: Data? = null,
    val nextRadioContinuationData: Data? = null,
) {
    @Serializable
    data class Data(val continuation: String)
}

/** Continuation token in the newer format, sent as the last item of a list. */
@Serializable
internal data class ContinuationItemRenderer(
    val continuationEndpoint: ContinuationEndpoint? = null,
) {
    val token: String? get() = continuationEndpoint?.continuationCommand?.token

    @Serializable
    data class ContinuationEndpoint(val continuationCommand: ContinuationCommand? = null)

    @Serializable
    data class ContinuationCommand(val token: String? = null)
}
