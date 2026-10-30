package io.github.nesssb.umusic.core.innertube

/** Decodes the response saved at [path], relative to the `innertube` test resources folder. */
internal inline fun <reified T> fixture(path: String): T =
    InnerTubeJson.decodeFromString(readFixture(path))

internal fun readFixture(path: String): String {
    val resource = InnerTubeParseException::class.java.getResource("/innertube/$path")
    return checkNotNull(resource) { "Missing fixture innertube/$path" }.readText()
}
