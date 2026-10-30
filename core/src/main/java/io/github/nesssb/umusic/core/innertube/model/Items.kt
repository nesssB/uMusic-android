package io.github.nesssb.umusic.core.innertube.model

/** Anything YouTube Music lists: a song or video, an album, an artist or a playlist. */
sealed interface YTItem {
    val id: String
    val title: String
    val thumbnailUrl: String?
}

/** A song or a video. [id] is the video id. */
data class SongItem(
    override val id: String,
    override val title: String,
    val artists: List<Artist>,
    val album: Album? = null,
    val durationSeconds: Int? = null,
    override val thumbnailUrl: String? = null,
    val videoType: MusicVideoType? = null,
    val explicit: Boolean = false,
    /** False for tracks YouTube Music lists but can't play, such as removed videos. */
    val playable: Boolean = true,
    /** Id of this entry within a playlist or queue, needed to move or remove it. */
    val setVideoId: String? = null,
) : YTItem

/** An album, single or EP. [id] is its browse id (`MPREb_…`). */
data class AlbumItem(
    override val id: String,
    override val title: String,
    val artists: List<Artist>,
    val year: Int? = null,
    /** Playlist holding the album's tracks (`OLAK5uy_…`), used to play it. */
    val playlistId: String? = null,
    override val thumbnailUrl: String? = null,
    val explicit: Boolean = false,
) : YTItem

/** An artist. [id] is its channel id (`UC…`). */
data class ArtistItem(
    override val id: String,
    override val title: String,
    override val thumbnailUrl: String? = null,
) : YTItem

/** A playlist. [id] is the playlist id, without the `VL` prefix of its browse id. */
data class PlaylistItem(
    override val id: String,
    override val title: String,
    val author: Artist? = null,
    override val thumbnailUrl: String? = null,
) : YTItem

/** A credited artist or channel. [id] is null when YouTube Music doesn't link it to a channel. */
data class Artist(val name: String, val id: String?)

/** The album a song belongs to. [id] is the album's browse id. */
data class Album(val title: String, val id: String)

/** What kind of video backs a [SongItem]. */
enum class MusicVideoType {
    /** An audio track that shows the album art (`MUSIC_VIDEO_TYPE_ATV`). */
    AudioTrack,

    /** The artist's official music video (`MUSIC_VIDEO_TYPE_OMV`). */
    OfficialVideo,

    /** A video uploaded by a user (`MUSIC_VIDEO_TYPE_UGC`). */
    UserVideo,

    /** Any other kind, such as a live performance or a lyric video. */
    Other,
}

/** Opens a page through the `browse` endpoint. */
data class BrowseEndpoint(val browseId: String, val params: String? = null)

/** Starts playback through the `next` endpoint. */
data class WatchEndpoint(
    val videoId: String? = null,
    val playlistId: String? = null,
    val params: String? = null,
)
