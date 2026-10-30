package io.github.nesssb.umusic.core.innertube.parser

import io.github.nesssb.umusic.core.innertube.InnerTubeParseException
import io.github.nesssb.umusic.core.innertube.model.Album
import io.github.nesssb.umusic.core.innertube.model.AlbumItem
import io.github.nesssb.umusic.core.innertube.model.AlbumPage
import io.github.nesssb.umusic.core.innertube.model.Artist
import io.github.nesssb.umusic.core.innertube.model.ArtistItem
import io.github.nesssb.umusic.core.innertube.model.ArtistPage
import io.github.nesssb.umusic.core.innertube.model.HomeChip
import io.github.nesssb.umusic.core.innertube.model.HomePage
import io.github.nesssb.umusic.core.innertube.model.ItemsPage
import io.github.nesssb.umusic.core.innertube.model.PlaylistItem
import io.github.nesssb.umusic.core.innertube.model.PlaylistPage
import io.github.nesssb.umusic.core.innertube.model.RelatedPage
import io.github.nesssb.umusic.core.innertube.model.SongItem
import io.github.nesssb.umusic.core.innertube.response.BrowseResponse
import io.github.nesssb.umusic.core.innertube.response.MusicResponsiveHeaderRenderer
import io.github.nesssb.umusic.core.innertube.response.SectionListRenderer
import io.github.nesssb.umusic.core.innertube.response.ShelfItem
import io.github.nesssb.umusic.core.innertube.response.Tabs

/** Parses the responses of the `browse` endpoint, one function per kind of page. */
internal object BrowseParser {

    fun home(response: BrowseResponse): HomePage {
        val sectionList = response.contents?.singleColumnBrowseResultsRenderer?.sectionList()
            ?: throw InnerTubeParseException("Home response without sections")
        return HomePage(
            chips = sectionList.header?.chipCloudRenderer?.chips.orEmpty().mapNotNull { chip ->
                val renderer = chip.chipCloudChipRenderer ?: return@mapNotNull null
                val endpoint = renderer.navigationEndpoint?.browseEndpoint ?: return@mapNotNull null
                HomeChip(renderer.text?.text.orEmpty(), endpoint.toBrowseEndpoint())
            },
            sections = sectionList.contents.toSections(),
            continuation = sectionList.continuations.token(),
        )
    }

    fun homeContinuation(response: BrowseResponse): HomePage {
        val sectionList = response.continuationContents?.sectionListContinuation
            ?: throw InnerTubeParseException("Home continuation without sections")
        return HomePage(
            chips = emptyList(),
            sections = sectionList.contents.toSections(),
            continuation = sectionList.continuations.token(),
        )
    }

    /** @param browseId the album's browse id, which the response doesn't repeat. */
    fun album(response: BrowseResponse, browseId: String): AlbumPage {
        val columns = response.contents?.twoColumnBrowseResultsRenderer
            ?: throw InnerTubeParseException("Album response without contents")
        val header = columns.tabs.header()
            ?: throw InnerTubeParseException("Album response without header")
        val secondary = columns.secondaryContents?.sectionListRenderer?.contents.orEmpty()

        val album = AlbumItem(
            id = browseId,
            title = header.title?.text.orEmpty(),
            artists = subtitleOf(header.straplineTextOne?.runs).artistsOrFirstGroup(),
            year = subtitleOf(header.subtitle?.runs).year,
            playlistId = header.buttons.orEmpty().firstNotNullOfOrNull {
                it.musicPlayButtonRenderer?.playNavigationEndpoint
                    ?.watchPlaylistEndpoint?.playlistId
            },
            thumbnailUrl = header.thumbnail?.largestUrl,
        )
        // Tracks only list what differs from the album: featured artists, mostly
        val songs = secondary.firstNotNullOfOrNull { it.musicShelfRenderer }?.contents.orEmpty()
            .mapNotNull { it.musicResponsiveListItemRenderer?.toSongItem() }
            .map { song ->
                song.copy(
                    artists = song.artists.ifEmpty { album.artists },
                    album = song.album ?: Album(album.title, album.id),
                    thumbnailUrl = song.thumbnailUrl ?: album.thumbnailUrl,
                )
            }
        return AlbumPage(
            album = album,
            description = header.descriptionText(),
            songs = songs,
            sections = secondary.mapNotNull { it.musicCarouselShelfRenderer?.toSection() },
        )
    }

    /** @param browseId the artist's channel id, which the response doesn't repeat. */
    fun artist(response: BrowseResponse, browseId: String): ArtistPage {
        val header = response.header?.musicImmersiveHeaderRenderer
            ?: response.header?.musicVisualHeaderRenderer
            ?: throw InnerTubeParseException("Artist response without header")
        val sectionList = response.contents?.singleColumnBrowseResultsRenderer?.sectionList()
            ?: throw InnerTubeParseException("Artist response without sections")

        val artist = ArtistItem(
            id = browseId,
            title = header.title?.text.orEmpty(),
            thumbnailUrl = header.thumbnail?.largestUrl ?: header.foregroundThumbnail?.largestUrl,
        )
        // The artist's own albums and videos don't name the artist
        val credit = Artist(artist.title, artist.id)
        val sections = sectionList.contents.toSections().map { section ->
            section.copy(items = section.items.map { it.withDefaultArtist(credit) })
        }
        val description = header.description?.text?.ifEmpty { null }
            ?: sectionList.contents.orEmpty()
                .firstNotNullOfOrNull { it.musicDescriptionShelfRenderer?.description?.text }
        return ArtistPage(
            artist = artist,
            description = description,
            shuffleEndpoint = header.playButton?.buttonRenderer?.navigationEndpoint
                ?.watchEndpoint?.toWatchEndpoint(),
            radioEndpoint = header.startRadioButton?.buttonRenderer?.navigationEndpoint
                ?.watchEndpoint?.toWatchEndpoint(),
            sections = sections,
        )
    }

    fun playlist(response: BrowseResponse): PlaylistPage {
        val columns = response.contents?.twoColumnBrowseResultsRenderer
            ?: throw InnerTubeParseException("Playlist response without contents")
        val header = columns.tabs.header()
            ?: throw InnerTubeParseException("Playlist response without header")
        val shelf = columns.secondaryContents?.sectionListRenderer?.contents.orEmpty()
            .firstNotNullOfOrNull { it.musicPlaylistShelfRenderer }
            ?: throw InnerTubeParseException("Playlist response without tracks")

        val playlist = PlaylistItem(
            id = shelf.playlistId
                ?: throw InnerTubeParseException("Playlist response without playlist id"),
            title = header.title?.text.orEmpty(),
            author = header.author(),
            thumbnailUrl = header.thumbnail?.largestUrl,
        )
        return PlaylistPage(
            playlist = playlist,
            description = header.descriptionText(),
            songs = shelf.contents.toPlaylistSongs(),
            continuation = shelf.contents.continuationItemToken() ?: shelf.continuations.token(),
        )
    }

    fun playlistContinuation(response: BrowseResponse): ItemsPage<SongItem> {
        val items = response.onResponseReceivedActions.orEmpty()
            .firstNotNullOfOrNull { it.appendContinuationItemsAction?.continuationItems }
            ?: throw InnerTubeParseException("Playlist continuation without tracks")
        return ItemsPage(items.toPlaylistSongs(), items.continuationItemToken())
    }

    /** The "Related" tab of the player, browsed with the `relatedBrowseId` of the queue. */
    fun related(response: BrowseResponse): RelatedPage {
        val sectionList = response.contents?.sectionListRenderer
            ?: throw InnerTubeParseException("Related response without sections")
        return RelatedPage(sectionList.contents.toSections())
    }

    private fun Tabs.sectionList(): SectionListRenderer? =
        tabs.firstOrNull()?.tabRenderer?.content?.sectionListRenderer

    private fun List<Tabs.Tab>.header(): MusicResponsiveHeaderRenderer? =
        firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents.orEmpty()
            .firstNotNullOfOrNull { it.musicResponsiveHeaderRenderer }

    private fun MusicResponsiveHeaderRenderer.descriptionText(): String? =
        description?.musicDescriptionShelfRenderer?.description?.text?.ifEmpty { null }

    /** Owner named above the title, or in the avatar row of user playlists. */
    private fun MusicResponsiveHeaderRenderer.author(): Artist? {
        subtitleOf(straplineTextOne?.runs).artistsOrFirstGroup().firstOrNull()?.let { return it }
        val avatars = facepile?.avatarStackViewModel ?: return null
        return Artist(
            name = avatars.text?.content ?: return null,
            id = avatars.rendererContext?.commandContext?.onTap?.innertubeCommand
                ?.browseEndpoint?.browseId,
        )
    }

    private fun List<ShelfItem>?.toPlaylistSongs(): List<SongItem> = orEmpty().mapNotNull {
        it.musicResponsiveListItemRenderer?.toSongItem(unlinkedArtists = true)
    }
}
