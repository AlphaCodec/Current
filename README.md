# Current — Android news app

A native Android implementation of the "Current" news app, built with
Kotlin and Jetpack Compose (Material 3), backed by real, live news data
from the [NewsData.io](https://newsdata.io) API.

## Live data

The app calls NewsData.io's `/api/1/latest` endpoint for the home feed
(by category) and search. This provider was chosen deliberately over
NewsAPI.org/GNews because **its free tier is licensed for commercial/
production use** (200 requests/day) — the others explicitly forbid
production use on their free plans. Honest limitations of the free tier,
carried through into the UI:

- No true real-time/breaking feed — there's a delay, so the app labels
  its top-of-feed banner "Just in" rather than "Live", and there is no
  fabricated live indicator.
- Only a description/summary is returned, not full article bodies — the
  reader shows the summary plus a "Read full story at [source] →" link
  that opens the original article in the browser.
- 200 requests/day. The app doesn't poll or auto-refresh; it fetches on
  tab open / edition change / search, which is enough for normal use but
  will exhaust quickly under heavy testing.

**If no API key is configured, the app runs on a small bundled sample
data set** instead of crashing, with a banner explaining how to add one.
This means the project is always demoable, CI always succeeds, and real
data is opt-in.

### Getting a key

1. Register for free at https://newsdata.io/register (no credit card).
2. For local builds: copy `local.properties.example` to `local.properties`
   (already gitignored) and set `NEWSDATA_API_KEY=your_key`.
3. For CI builds: add `NEWSDATA_API_KEY` as a repository secret
   (Settings → Secrets and variables → Actions) — the workflow already
   passes it through.

## Features

- **Home feed** — editions filter row mapped to real NewsData.io
  categories, a "Just in" banner, hero story with a real photo, ranked
  story list
- **Article reader** — paper reading surface, drop-cap opening
  paragraph, real thumbnail, scroll progress indicator, save/share
  (real Android share sheet)/listen toolbar, "read full story" link out
  to the original source
- **Explore** — topic tiles that browse real categories, a "writers in
  today's feed" row derived from actual bylines in the current feed
- **Search** — live query against the API (debounced), Articles/Opinion
  filters
- **Saved** — bookmarking of full articles, persisted for the session,
  with an empty state
- **Profile** — account shell + a working **Appearance** setting
  (Light/Dark/System), persisted with DataStore
- Loading states, error states with retry, and graceful fallback to
  sample data — this is written the way a shipped app handles a flaky
  network, not a happy-path-only demo
- Bottom tab navigation (Home / Explore / Saved / Profile) built on
  Jetpack Navigation Compose
- One shared `NewsViewModel` (StateFlow-based) driving feed, search,
  and bookmarks, with an in-memory article cache so opening a story
  doesn't require a second network call

## Project structure

```
app/src/main/java/com/current/news/
  network/           Retrofit API interface, DTOs, OkHttp/Retrofit client
  data/              Article/Writer/Topic models, NewsRepository
                       (real API + sample-data fallback), SettingsRepository
  viewmodel/         NewsViewModel (feed/search/bookmarks),
                       SettingsViewModel (appearance)
  navigation/        NavGraph wiring screens + bottom nav together
  ui/theme/          Color, typography, Material3 theme, AppColors
                       (light/dark chrome palette)
  ui/components/     Shared building blocks (story row w/ real images,
                       chips, bottom nav)
  ui/screens/        HomeScreen, ArticleScreen, ExploreScreen,
                      SearchScreen, SavedScreen, ProfileScreen
  MainActivity.kt
```

## Building

### Locally

Requires JDK 17 and Android SDK (compileSdk 34). Set up
`local.properties` as described above, then open the project root in
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
3. Runs `gradle assembleDebug` and `gradle assembleRelease`, with
   `NEWSDATA_API_KEY` injected from the `NEWSDATA_API_KEY` repo secret
4. Uploads both APKs as workflow artifacts (`current-debug-apk`,
   `current-release-apk-unsigned`)

To get a build: push this repo to GitHub, set the `NEWSDATA_API_KEY`
secret, open the **Actions** tab, wait for the workflow to finish, then
download the APK from the run's **Artifacts** section. The release APK
is unsigned — sign it before distributing outside of sideloading for
testing (see below).

### Signing a release build

For a Play Store-ready build, add a signing config. Store your keystore
as a GitHub secret (e.g. `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
`KEY_ALIAS`, `KEY_PASSWORD`), decode it in the workflow, and reference it
from a `signingConfigs` block in `app/build.gradle.kts`. This is
intentionally left out of the starter workflow so the project builds
out of the box without any secrets configured.

## Next steps

- Move bookmarks from in-memory to DataStore/Room so they survive app
  restarts (currently session-only, called out above)
- Add pull-to-refresh on the home feed
- Add pagination using `nextPage` from the NewsData.io response
- If the free tier's 200 requests/day becomes limiting, either cache
  responses locally with a TTL, or upgrade to a paid tier — the
  repository layer is already isolated so this is a one-file change
- Push notifications aren't feasible on the free tier (no real
  breaking-news webhook) without a paid plan or a different provider
