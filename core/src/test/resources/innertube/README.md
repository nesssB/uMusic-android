# InnerTube fixtures

Real YouTube Music responses used by the parser tests in
`io.github.nesssb.umusic.core.innertube`. They were captured on 2026-10-30
with the `WEB_REMIX` client, version `1.20261027.10.00`, `hl=en` and `gl=US`.

| File                                     | Request                                                                   |
| ---------------------------------------- | ------------------------------------------------------------------------- |
| `search/search.json`                     | `search`, `query: "daft punk"`                                            |
| `search/search_songs.json`               | `search`, `query: "daft punk"`, Songs filter `params`                     |
| `search/search_songs_continuation.json`  | `search`, `continuation` from `search_songs.json`                         |
| `browse/home.json`                       | `browse`, `browseId: "FEmusic_home"`                                      |
| `browse/home_continuation.json`          | `browse`, `continuation` from `home.json`                                 |
| `browse/album.json`                      | `browse`, `browseId: "MPREb_7ltM34kr0mH"` (Discovery)                     |
| `browse/artist.json`                     | `browse`, `browseId: "UCRr1xG_2WIDs18a6cIiCxeA"` (Daft Punk)              |
| `browse/playlist.json`                   | `browse`, `browseId: "VLRDCLAK5uy_n20FRYQXNt1p1wS55Nj2r14IouO5weaYU"`     |
| `browse/playlist_long.json`              | `browse`, `browseId: "VLPL-hvaaoSDXQKUScT-amoPP3cqzEQ607m_"` (143 tracks) |
| `browse/playlist_long_continuation.json` | `browse`, `continuation` from `playlist_long.json`                        |
| `browse/related.json`                    | `browse`, `browseId: "MPTRt_7ltM34kr0mH-10"` (Related tab of `next_song`) |
| `next/next_song.json`                    | `next`, `videoId: "OWiVJMgms9E"`                                          |
| `next/next_radio.json`                   | `next`, `videoId: "OWiVJMgms9E"`, `playlistId: "RDAMVMOWiVJMgms9E"`       |
| `next/next_radio_continuation.json`      | `next`, `continuation` from `next_radio.json`                             |
| `next/next_album.json`                   | `next`, `videoId: "L93-7vRfxNs"`, `playlistId` of the Discovery album     |

All requests to `next` also sent `isAudioOnly: true`. Continuations only work
when the request carries the `visitorData` returned with the first page, both
in `context.client.visitorData` and in the `X-Goog-Visitor-Id` header.

## Trimming

To keep the repository small, the responses were saved minified and without
fields the parsers don't read. Everything else is unchanged:

```bash
jq -c 'del(.. | objects | (.trackingParams, .clickTrackingParams, .loggingContext,
    .accessibility, .accessibilityData, .accessibilityPlayData, .accessibilityPauseData,
    .serializedShareEntity, .menu))
  | del(.responseContext, .frameworkUpdates, .playerOverlays, .videoReporting)' \
  response.json > fixture.json
```

`responseContext` is dropped because it holds the `visitorData` of the session
that captured the fixture.
