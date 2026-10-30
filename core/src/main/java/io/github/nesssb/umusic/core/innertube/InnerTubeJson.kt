package io.github.nesssb.umusic.core.innertube

import kotlinx.serialization.json.Json

/** Decodes InnerTube responses, which carry far more fields than the parsers declare. */
internal val InnerTubeJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/** Thrown when a response lacks a part every response of its kind should have. */
class InnerTubeParseException(message: String) : Exception(message)
