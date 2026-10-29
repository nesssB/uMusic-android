package io.github.nesssb.umusic

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * Manual dependency container. Holds the app-wide singletons; everything else
 * receives what it needs through its constructor.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val okHttpClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }

    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
