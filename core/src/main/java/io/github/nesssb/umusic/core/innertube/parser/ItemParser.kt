package io.github.nesssb.umusic.core.innertube.parser

import io.github.nesssb.umusic.core.innertube.model.Album
import io.github.nesssb.umusic.core.innertube.model.AlbumItem
import io.github.nesssb.umusic.core.innertube.model.Artist
import io.github.nesssb.umusic.core.innertube.model.ArtistItem
import io.github.nesssb.umusic.core.innertube.model.BrowseEndpoint
import io.github.nesssb.umusic.core.innertube.model.MusicVideoType
import io.github.nesssb.umusic.core.innertube.model.PlaylistItem
import io.github.nesssb.umusic.core.innertube.model.Section
import io.github.nesssb.umusic.core.innertube.model.SongItem
import io.github.nesssb.umusic.core.innertube.model.WatchEndpoint
import io.github.nesssb.umusic.core.innertube.model.YTItem
import io.github.nesssb.umusic.core.innertube.response.Badge
import io.github.nesssb.umusic.core.innertube.response.Continuation
import io.github.nesssb.umusic.core.innertube.response.MusicCarouselShelfRenderer
import io.github.nesssb.umusic.core.innertube.response.MusicResponsiveListItemRenderer
import io.github.nesssb.umusic.core.innertube.response.MusicShelfRenderer
import io.github.nesssb.umusic.core.innertube.response.MusicTwoRowItemRenderer
import io.github.nesssb.umusic.core.innertube.response.NavigationEndpoint
import io.github.nesssb.umusic.core.innertube.response.PlaylistPanelVideoRenderer
import io.github.nesssb.umusic.core.innertube.response.Run
import io.github.nesssb.umusic.core.innertube.response.SectionListItem
import io.github.nesssb.umusic.core.innertube.response.ShelfItem

/*
 * Turns the renderers shared by every endpoint into items and sections.
 *
 * The text YouTube Music shows ("Song", "1.2B plays", " & ") depends on the language of the
 * request, so these functions never compare against it. They rely on links, ids and number
 * formats instead, plus the " • " separator, which every language uses.
 */

internal const val PAGE_TYPE_ALBUM = "MUSIC_PAGE_TYPE_ALBUM"
internal const val PAGE_TYPE_ARTIST = "MUSIC_PAGE_TYPE_ARTIST"
internal const val PAGE_TYPE_PLAYLIST = "MUSIC_PAGE_TYPE_PLAYLIST"
internal const val PAGE_TYPE_TRACK_LYRICS = "MUSIC_PAGE_TYPE_TRACK_LYRICS"
internal const val PAGE_TYPE_TRACK_RELATED = "MUSIC_PAGE_TYPE_TRACK_RELATED"

private const val VIDEO_TYPE_PODCAST_EPISODE = "MUSIC_VIDEO_TYPE_PODCAST_EPISODE"
private const val DISPLAY_POLICY_GREY_OUT = "MUSIC_ITEM_RENDERER_DISPLAY_POLICY_GREY_OUT"
private const val EXPLICIT_BADGE = "MUSIC_EXPLICIT_BADGE"
private const val SEPARATOR = " • "
private val SEPARATOR_RUN = Run(SEPARATOR)

private val DURATION = Regex("""\d+(:\d{2}){1,2}""")
private val YEAR = Regex("""\d{4}""")

/** What an item's secondary text says, such as "Daft Punk • Discovery • 3:33". */
internal class Subtitle(
    /** The text split at each " • ", so each group is one piece of information. */
    val groups: List<List<Run>>,
) {
    private val runs = groups.flatten()
    private val texts = groups.map { group -> group.joinToString("") { it.text }.trim() }

    val artists: List<Artist> = runs.mapNotNull { it.toArtist() }
    val album: Album? = runs.firstNotNullOfOrNull { it.toAlbum() }
    val durationSeconds: Int? = texts.firstNotNullOfOrNull { parseDuration(it) }
    val year: Int? = texts.firstOrNull { YEAR.matches(it) }?.toInt()

    /** Linked artists, or the first group as one unlinked artist when nothing is linked. */
    fun artistsOrFirstGroup(): List<Artist> =
        artists.ifEmpty { listOfNotNull(groups.firstOrNull()?.toUnlinkedArtist()) }
}

internal fun subtitleOf(runs: List<Run>?) = Subtitle(runs.orEmpty().splitBySeparator())

private fun List<Run>.splitBySeparator(): List<List<Run>> {
    val groups = mutableListOf(mutableListOf<Run>())
    for (run in this) {
        if (run.text == SEPARATOR) groups += mutableListOf<Run>() else groups.last() += run
    }
    return groups.filter { it.isNotEmpty() }
}

private fun Run.toArtist(): Artist? {
    val browse = navigationEndpoint?.browseEndpoint ?: return null
    return if (browse.browseId.startsWith("UC")) Artist(text, browse.browseId) else null
}

private fun Run.toAlbum(): Album? {
    val browse = navigationEndpoint?.browseEndpoint ?: return null
    val isAlbum = browse.pageType == PAGE_TYPE_ALBUM || browse.browseId.startsWith("MPREb")
    return if (isAlbum) Album(text, browse.browseId) else null
}

/** Artist named without a link, which happens when it has no channel on YouTube Music. */
internal fun List<Run>.toUnlinkedArtist(): Artist? =
    joinToString("") { it.text }.trim().takeIf { it.isNotEmpty() }?.let { Artist(it, null) }

/** Seconds in a duration such as "3:33" or "1:13:45", or null if [text] isn't one. */
internal fun parseDuration(text: String?): Int? {
    if (text == null || !DURATION.matches(text)) return null
    return text.split(':').fold(0) { total, part -> total * 60 + part.toInt() }
}

private fun musicVideoType(raw: String?): MusicVideoType? = when (raw) {
    null -> null
    "MUSIC_VIDEO_TYPE_ATV" -> MusicVideoType.AudioTrack
    "MUSIC_VIDEO_TYPE_OMV" -> MusicVideoType.OfficialVideo
    "MUSIC_VIDEO_TYPE_UGC" -> MusicVideoType.UserVideo
    else -> MusicVideoType.Other
}

private fun List<Badge>?.hasExplicit(): Boolean =
    orEmpty().any { it.musicInlineBadgeRenderer?.icon?.iconType == EXPLICIT_BADGE }

internal fun NavigationEndpoint.Browse.toBrowseEndpoint() = BrowseEndpoint(browseId, params)

internal fun NavigationEndpoint.Watch.toWatchEndpoint() = WatchEndpoint(videoId, playlistId, params)

internal fun NavigationEndpoint.WatchPlaylist.toWatchEndpoint() =
    WatchEndpoint(playlistId = playlistId, params = params)

internal fun List<Continuation>?.token(): String? = this?.firstNotNullOfOrNull {
    it.nextContinuationData?.continuation ?: it.nextRadioContinuationData?.continuation
}

internal fun List<ShelfItem>?.continuationItemToken(): String? =
    this?.firstNotNullOfOrNull { it.continuationItemRenderer?.token }

/**
 * The item [endpoint] opens: a song or video for a watch link; an album, artist or playlist for
 * a browse link. Anything else, such as podcasts and user profiles, is skipped.
 */
internal fun itemFor(
    endpoint: NavigationEndpoint,
    title: String,
    subtitle: Subtitle,
    thumbnailUrl: String?,
    explicit: Boolean = false,
    playEndpoint: NavigationEndpoint? = null,
): YTItem? {
    endpoint.browseEndpoint?.let { browse ->
        return browseItem(browse, title, subtitle, thumbnailUrl, explicit, playEndpoint)
    }
    val watch = endpoint.watchEndpoint ?: return null
    if (watch.musicVideoType == VIDEO_TYPE_PODCAST_EPISODE) return null
    return SongItem(
        id = watch.videoId ?: return null,
        title = title,
        artists = subtitle.artists,
        album = subtitle.album,
        durationSeconds = subtitle.durationSeconds,
        thumbnailUrl = thumbnailUrl,
        videoType = musicVideoType(watch.musicVideoType),
        explicit = explicit,
    )
}

private fun browseItem(
    browse: NavigationEndpoint.Browse,
    title: String,
    subtitle: Subtitle,
    thumbnailUrl: String?,
    explicit: Boolean,
    playEndpoint: NavigationEndpoint?,
): YTItem? = when (browse.pageType) {
    PAGE_TYPE_ALBUM -> AlbumItem(
        id = browse.browseId,
        title = title,
        artists = subtitle.artists,
        year = subtitle.year,
        playlistId = playEndpoint?.watchPlaylistEndpoint?.playlistId,
        thumbnailUrl = thumbnailUrl,
        explicit = explicit,
    )

    PAGE_TYPE_ARTIST -> ArtistItem(id = browse.browseId, title = title, thumbnailUrl = thumbnailUrl)

    PAGE_TYPE_PLAYLIST -> PlaylistItem(
        id = browse.browseId.removePrefix("VL"),
        title = title,
        author = subtitle.artists.firstOrNull(),
        thumbnailUrl = thumbnailUrl,
    )

    else -> null
}

private val MusicResponsiveListItemRenderer.titleText: String?
    get() = flexColumns.firstOrNull()?.renderer?.text?.text

/** Every column but the title, which together play the role of a subtitle. */
private val MusicResponsiveListItemRenderer.columnsSubtitle: Subtitle
    get() = subtitleOf(
        flexColumns.drop(1).flatMap { it.renderer?.text?.runs.orEmpty() + SEPARATOR_RUN },
    )

internal fun MusicResponsiveListItemRenderer.toYTItem(): YTItem? {
    val browse = navigationEndpoint?.browseEndpoint ?: return toSongItem()
    return browseItem(
        browse = browse,
        title = titleText ?: return null,
        subtitle = columnsSubtitle,
        thumbnailUrl = thumbnail?.largestUrl,
        explicit = badges.hasExplicit(),
        playEndpoint = overlay?.playEndpoint,
    )
}

/**
 * @param unlinkedArtists whether the first column after the title lists the artists, so it can
 *   be read as an artist when it has no link. True for playlist tracks.
 */
internal fun MusicResponsiveListItemRenderer.toSongItem(
    unlinkedArtists: Boolean = false,
): SongItem? {
    val title = titleText ?: return null
    val watch = flexColumns.firstOrNull()?.renderer?.text?.runs?.firstOrNull()
        ?.navigationEndpoint?.watchEndpoint
        ?: overlay?.playEndpoint?.watchEndpoint
    if (watch?.musicVideoType == VIDEO_TYPE_PODCAST_EPISODE) return null
    val subtitle = columnsSubtitle
    val fixedDuration = fixedColumns.orEmpty()
        .firstNotNullOfOrNull { parseDuration(it.renderer?.text?.text) }
    return SongItem(
        id = playlistItemData?.videoId ?: watch?.videoId ?: return null,
        title = title,
        artists = if (unlinkedArtists) subtitle.artistsOrFirstGroup() else subtitle.artists,
        album = subtitle.album,
        durationSeconds = fixedDuration ?: subtitle.durationSeconds,
        thumbnailUrl = thumbnail?.largestUrl,
        videoType = musicVideoType(watch?.musicVideoType),
        explicit = badges.hasExplicit(),
        playable = musicItemRendererDisplayPolicy != DISPLAY_POLICY_GREY_OUT,
        setVideoId = playlistItemData?.playlistSetVideoId,
    )
}

internal fun MusicTwoRowItemRenderer.toYTItem(): YTItem? = itemFor(
    endpoint = navigationEndpoint ?: return null,
    title = title?.text ?: return null,
    subtitle = subtitleOf(subtitle?.runs),
    thumbnailUrl = thumbnailRenderer?.largestUrl,
    explicit = subtitleBadges.hasExplicit(),
    playEndpoint = thumbnailOverlay?.playEndpoint,
)

internal fun ShelfItem.toYTItem(): YTItem? =
    musicResponsiveListItemRenderer?.toYTItem() ?: musicTwoRowItemRenderer?.toYTItem()

/** A song of the play queue. Its byline always starts with the artists. */
internal fun PlaylistPanelVideoRenderer.toSongItem(): SongItem? {
    val watch = navigationEndpoint?.watchEndpoint
    val subtitle = subtitleOf(longBylineText?.runs)
    return SongItem(
        id = videoId ?: watch?.videoId ?: return null,
        title = title?.text.orEmpty(),
        artists = subtitle.artistsOrFirstGroup(),
        album = subtitle.album,
        durationSeconds = parseDuration(lengthText?.text),
        thumbnailUrl = thumbnail?.largestUrl,
        videoType = musicVideoType(watch?.musicVideoType),
        explicit = badges.hasExplicit(),
        setVideoId = playlistSetVideoId,
    )
}

/** Credits [artist] on songs and albums that list no artist, as on the artist's own page. */
internal fun YTItem.withDefaultArtist(artist: Artist): YTItem = when {
    this is SongItem && artists.isEmpty() -> copy(artists = listOf(artist))
    this is AlbumItem && artists.isEmpty() -> copy(artists = listOf(artist))
    else -> this
}

internal fun MusicCarouselShelfRenderer.toSection(): Section? {
    val header = header?.musicCarouselShelfBasicHeaderRenderer ?: return null
    val items = contents.orEmpty().mapNotNull { it.toYTItem() }
    if (items.isEmpty()) return null
    val moreEndpoint = header.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint
        ?: header.title?.runs?.firstOrNull()?.navigationEndpoint?.browseEndpoint
    return Section(
        title = header.title?.text.orEmpty(),
        strapline = header.strapline?.text?.ifEmpty { null },
        items = items,
        moreEndpoint = moreEndpoint?.toBrowseEndpoint(),
    )
}

internal fun MusicShelfRenderer.toSection(): Section? {
    val items = contents.orEmpty().mapNotNull { it.toYTItem() }
    if (items.isEmpty()) return null
    val moreEndpoint = title?.runs?.firstOrNull()?.navigationEndpoint?.browseEndpoint
        ?: bottomEndpoint?.browseEndpoint
    return Section(
        title = title?.text.orEmpty(),
        strapline = null,
        items = items,
        moreEndpoint = moreEndpoint?.toBrowseEndpoint(),
    )
}

internal fun List<SectionListItem>?.toSections(): List<Section> = orEmpty().mapNotNull {
    it.musicCarouselShelfRenderer?.toSection() ?: it.musicShelfRenderer?.toSection()
}
