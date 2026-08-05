# Current — Android news app

A native Android implementation of the "Current" news app UI, built with
Kotlin and Jetpack Compose (Material 3). Same visual language as the
original web mockup: dark, wire-service chrome for scanning; a warm paper
surface for the reading moment.

## Features

- **Home feed** — editions filter row, live/breaking banner, hero story,
  ranked story list
- **Article reader** — paper reading surface, drop-cap opening paragraph,
  scroll progress indicator, save/share/listen toolbar
- **Explore** — topic grid, writers-to-follow row, entry point into search
- **Search** — live query filtering, category filters (All/Articles/Live/Opinion)
- **Saved** — bookmarked stories persist across the session (toggle from
  the article reader), with an empty state
- **Profile** — account summary and preferences list
- Bottom tab navigation (Home / Explore / Saved / Profile) built on
  Jetpack Navigation Compose
- Single shared `NewsViewModel` (StateFlow-based) driving feed filtering,
  search, and bookmarks — swap `NewsRepository`'s in-memory data for a
  real API/Room-backed source without touching the UI layer

## Project structure

```
app/src/main/java/com/current/news/
  data/            Article/Writer/Topic models + in-memory NewsRepository
  viewmodel/        NewsViewModel — feed, search, and bookmark state
  navigation/        NavGraph wiring screens + bottom nav together
  ui/theme/          Color, typography, and Material3 theme tokens
  ui/components/    Shared building blocks (story row, chips, bottom nav)
  ui/screens/        HomeScreen, ArticleScreen, ExploreScreen,
                      SearchScreen, SavedScreen, ProfileScreen
  MainActivity.kt
```

## Building

### Locally

Requires JDK 17 and Android SDK (compileSdk 34). Open the project root in
Android Studio (Koala or newer) and run, or from the command line:

```bash
gradle assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Via GitHub Actions (included)

`.github/workflows/android-build.yml` builds the app automatically on
every push/PR to `main`, and can also be triggered manually from the
Actions tab (`workflow_dispatch`). It:

1. Checks out the repo
2. Sets up JDK 17 and Gradle 8.9
3. Runs `gradle assembleDebug` and `gradle assembleRelease`
4. Uploads both APKs as workflow artifacts (`current-debug-apk`,
   `current-release-apk-unsigned`)

To get a build: push this repo to GitHub, open the **Actions** tab, wait
for the workflow to finish, then download the APK from the run's
**Artifacts** section. The release APK is unsigned — sign it before
distributing outside of sideloading for testing (see below).

### Signing a release build

For a Play Store-ready build, add a signing config. Store your keystore
as a GitHub secret (e.g. `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
`KEY_ALIAS`, `KEY_PASSWORD`), decode it in the workflow, and reference it
from a `signingConfigs` block in `app/build.gradle.kts`. This is
intentionally left out of the starter workflow so the project builds
out of the box without any secrets configured.

## Next steps

- Wire `NewsRepository` to a real backend (Retrofit) or local cache (Room)
- Add pull-to-refresh on the home feed
- Persist bookmarks with DataStore/Room so they survive app restarts
- Add push notifications for breaking/live stories
- Add a settings-driven light/paper-mode toggle for the whole app, not
  just the reader screen
