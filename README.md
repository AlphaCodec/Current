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
- Only a description/summary is returned, not full article bodies — this
  is a hard limit of the free tier (the `full_content` parameter exists
  but is paid-plan-only; the free tier ignores it). The reader shows the
  summary plus a **"Read full story at [source] →"** link that opens the
  real article in a Chrome Custom Tab — an in-app browser sheet, not a
  separate app — so reading feels continuous even though the text itself
  comes from the publisher's own page. (Scraping and re-displaying full
  article bodies from arbitrary sites would sidestep publishers'
  ads/paywalls and their terms of service, so this app doesn't do that;
  the Custom Tab approach is what most legitimate news readers do.)
- 200 requests/day. The app doesn't poll or auto-refresh, but **infinite
  scroll does mean scrolling through a long feed spends credits faster**
  than the previous single-page version — each additional page pulled in
  is one more request. Reaching the end of a feed ("You're all caught
  up") or a burst of scrolling can realistically use 5-15 credits in one
  session.

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
  story list with **infinite scroll** (loads the next page automatically
  as you near the bottom, via NewsData.io's `nextPage` token)
- **Article reader** — paper reading surface, drop-cap opening
  paragraph, real thumbnail, scroll progress indicator, save/share
  (real Android share sheet)/listen toolbar, "Read full story" link that
  opens the source in a Chrome Custom Tab (in-app browser sheet)
- **Explore** — topic tiles that browse real categories, a "writers in
  today's feed" row derived from actual bylines in the current feed
- **Search** — live query against the API (debounced), Articles/Opinion
  filters, infinite scroll on results
- **Saved** — bookmarking of full articles, persisted for the session,
  with an empty state
- **Profile** — account shell + a working **Appearance** setting
  (Light/Dark/System), persisted with DataStore
- Loading states, error states with retry, and graceful fallback to
  sample data — this is written the way a shipped app handles a flaky
  network, not a happy-path-only demo
- **Android Splash Screen API** — the launcher icon shows immediately on
  a solid background while the process cold-starts, instead of a blank
  white/black flash, so the app *feels* faster to open even though the
  underlying cold-start work is the same
- **Bounded image cache** — Coil's default disk cache grows with device
  storage (~2% of total, uncapped in practice) and never self-cleans;
  this app supplies its own `ImageLoader` (see `CurrentApp.kt`) capped at
  75MB disk / 15% of available RAM, plus a manual "Clear image cache"
  row in Profile → Storage showing the current size
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
- Cache responses per-edition in memory during a session to cut down on
  redundant requests against the 200/day free quota
- If full in-app article text matters more than staying free, NewsData.io's
  paid Basic plan ($199.99/mo) unlocks `full_content=1` — swap that one
  query param in `NewsDataApi.kt` and the reader UI needs no changes
- Push notifications aren't feasible on the free tier (no real
  breaking-news webhook) without a paid plan or a different provider
- **On perceived vs. actual startup time**: the splash screen fixes the
  "blank flash" perception issue, but the underlying cold-start work
  (process init, Compose first composition, first network call) still
  takes however long it takes. If startup is still slow after this
  change, the next lever is enabling R2/R8 code shrinking on the release
  build — not done here because it requires explicit ProGuard keep rules
  for Gson's reflection-based parsing of the DTOs in `network/`, and
  getting that wrong silently nulls out fields at runtime rather than
  failing loudly. Worth doing carefully in a follow-up, not as a quick
  toggle.
