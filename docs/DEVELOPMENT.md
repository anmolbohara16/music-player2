# Metadata and UI implementation

The existing Compose, MediaStore, Room and MediaPlayer architecture is retained. No audio-file tags are rewritten. The application owns the playback runtime so closing an Activity does not destroy the player.

## Metadata workflow

- Songs → **Web Search**, next to **Rescan**, starts a sequential library search. Progress, current song, individual outcomes and the final summary remain available while browsing. Cancellation stops pending network calls; completed updates remain saved.
- iTunes and MusicBrainz remain the catalog providers. Searches are throttled, cancellable and cached in a bounded session cache. Provider outages do not masquerade as successful no-match responses.
- High confidence requires matching title, artist, recording version and duration within three seconds. Album/ISRC conflicts and cross-provider disagreements require review. A duration difference above ten seconds blocks high confidence.
- Automatic updates fill missing values only, with conservative protection for existing overrides/manual edits. Review presents current and online values, provider alternatives, field selection, and Apply all. Each accepted set of changes is merged with the latest Room record in one transaction.
- **Identify Using Link** accepts YouTube video and Spotify track HTTPS URLs. Their oEmbed responses are limited suggestions, always reviewed; the app does not substitute the first catalog search hit or invent missing credits/duration. No Spotify credentials are required for this limited flow, and full Spotify Web API search is not implemented.
- LRCLIB results must agree on title, artist, version and duration. Missing artwork/lyrics leave existing data intact. Coil caches artwork, and failed online artwork can fall back to original local artwork.
- Edit Metadata and Identify Using Link are accessible from song actions and Track Information. Manual edits remain marked and protected.

## Appearance and playback

Shared tokens provide lavender-neutral light surfaces, supporting lavender containers and purple actions, with plum dark mode following the device. Existing library, detail, player, search, settings and dialog surfaces use the same theme. Home uses actual library/history data; synthetic sample songs are no longer injected. Library exposes favorites, playlists, playback settings and hidden-song restoration.

Now Playing has direct queue/lyrics access and an adaptive landscape arrangement. Queue removal works below five songs and correctly advances when removing the current song. Preparation is asynchronous. Metadata refreshes queue/player labels without restarting audio. Equalizer presets now configure Android's audio effect when supported. Notifications use MediaStyle and asynchronously cached artwork.

## Build and tests

Use an Android SDK with platform 36.1 and a supported Gradle JDK. Robolectric SDK 36 tests require Java 21 or later. If compilation uses Java 17, a separate test runtime can be supplied:

```bash
./gradlew -PtestJavaHome=/path/to/java21-or-newer :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-configuration-cache
```

The local verification environment has a usable test runtime at `/opt/datagrip/jbr`; it lacks `jlink`, so it is selected only for tests, not for Android compilation.

To record the UI gallery:

```bash
./gradlew -PtestJavaHome=/path/to/java21-or-newer -Proborazzi.test.record=true :app:testDebugUnitTest --no-configuration-cache
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Tests: `app/build/reports/tests/testDebugUnitTest/index.html`.
Rendered UI: `app/build/outputs/screenshots/`.

Tests cover version-aware matching, provider conflicts, missing values, selected merges, Room transaction rollback, provider HTTP/lyrics fixtures, batch outcomes/cache/cancellation, and rendered Compose screens. These are deterministic tests; live provider availability and real-device audio require separate checks.

## Verification limits

The connected phone rejected the installation attempt with `INSTALL_FAILED_VERIFICATION_FAILURE`. No device data was erased or verification settings bypassed. Physical-device playback, audio effects and notifications therefore remain unverified. The Room database is now at schema version 5; migration 4 to 5 adds playback events without changing or deleting existing favorites, playlists, history, settings, or metadata overrides. Historical migrations predating the provided repository are not reconstructed.
