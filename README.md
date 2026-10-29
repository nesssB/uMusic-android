# uMusic

An Android client for YouTube Music, built with Kotlin and Jetpack Compose.

> [!IMPORTANT]
> **uMusic is an unofficial, independent project.** It is not affiliated with,
> endorsed by, sponsored by or in any way connected to Google LLC, YouTube or
> YouTube Music.
>
> - "YouTube" and "YouTube Music" are trademarks of Google LLC. They are used
>   here only to describe what the app works with.
> - uMusic does not host, store or distribute any content. Everything you play
>   is streamed from YouTube Music, as it would be in the official app.
> - You are responsible for using the service in line with
>   [YouTube's Terms of Service](https://www.youtube.com/t/terms).
> - The software is provided "as is", without warranty of any kind. See the
>   [license](#license) for details.

## Status

Early development, so things will change often. There are no published builds
yet; for now the only way to try it is to build it from source.

## Requirements

To run the app:

- An Android phone or emulator running Android 8.0 (API 26) or newer

To build it:

- [Android Studio](https://developer.android.com/studio) (recommended), or
  just the Android SDK command-line tools
- JDK 21. Gradle downloads it automatically if it isn't installed
- Android SDK Platform 37.2. Gradle installs it on the first build once the
  SDK licenses are accepted

## Getting started

```bash
git clone https://github.com/nesssB/uMusic-android.git
cd uMusic-android
./gradlew installDebug
```

`installDebug` builds the app and installs it on the connected device or
running emulator. You can also open the project in Android Studio and press
**Run**.

## Gradle tasks

| Command                          | What it does                                      |
| -------------------------------- | ------------------------------------------------- |
| `./gradlew assembleDebug`        | Build a debug APK in `app/build/outputs/apk/`     |
| `./gradlew installDebug`         | Build and install the debug APK on a device       |
| `./gradlew assembleRelease`      | Build an unsigned release APK                     |
| `./gradlew test`                 | Run the unit tests                                |
| `./gradlew connectedAndroidTest` | Run the instrumented tests on a connected device  |
| `./gradlew lint`                 | Run Android Lint                                  |

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Built with

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Android](https://img.shields.io/badge/Android-34A853?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)

- [Media3](https://developer.android.com/media/media3) (ExoPlayer and
  MediaLibraryService) for playback
- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor) to
  resolve the audio streams
- [OkHttp](https://square.github.io/okhttp/) and
  [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
  for networking
- [Coil](https://coil-kt.github.io/coil/) for artwork and
  [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
  for settings

## License

uMusic is free software, released under the
[GNU General Public License v3.0 or later](LICENSE).
