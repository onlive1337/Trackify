# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Trackify is a native Android app (Kotlin + Jetpack Compose, Material 3) for tracking subscriptions and recurring payments. All data is stored locally; there is no backend, telemetry, or network sync. Package: `com.onlive.trackify`, minSdk 28, target/compileSdk 37, JDK 17.

## Build & Run

```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # build + install on connected device/emulator
./gradlew assembleRelease        # release build (minified + shrunk; see signing below)
./gradlew lint                   # Android Lint
./gradlew build                  # full build incl. lint
```

There are no unit or instrumentation tests in the repo. The `testInstrumentationRunner` is configured, but `src/test` and `src/androidTest` do not exist yet — add them under `app/src/test/java` / `app/src/androidTest/java` if introducing tests.

Release signing reads from env vars (`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) or, as a fallback, a `keystore.properties` file in the repo root. If neither is present, the release build falls back to the debug signing config.

Dependencies are managed via the version catalog at `gradle/libs.versions.toml` — add/upgrade libraries there, not inline in `app/build.gradle.kts`.

## Architecture

MVVM with a manual (no DI framework) dependency graph. Layering: Compose screen → `*ViewModel` → `*Repository` → Room DAO.

- **Data layer** (`data/`): Room `AppDatabase` is a singleton obtained via `AppDatabase.getDatabase(context)`. Entities `Subscription`, `Payment`, `Category` (`data/model/`) with DAOs in `data/database/`. `Converters` handles `Date`/enum type conversion. `schemaVersion = 1`, `exportSchema = false` — bumping the schema requires a migration or `fallbackToDestructiveMigration`.
- **Repositories** (`data/repository/`): wrap DAO calls. Write operations (`insert`/`update`/`delete`) run on `Dispatchers.IO` and return a `Result<T>` (`utils/Result.kt`, a `Success`/`Error` sealed class) instead of throwing — error messages are pulled from string resources so they're localized. Reads expose `LiveData`. Repositories use `MediatorLiveData` to join subscriptions with their category name/color at read time (the `categoryName`/`categoryColor` fields on `Subscription` are `@Ignore`d, populated by the repository, not stored).
- **ViewModels** (`viewmodel/`): `AndroidViewModel` subclasses. They construct their own repository from `AppDatabase.getDatabase(application)` in `init`, and report errors via the app-wide `ErrorHandler` obtained from `(application as TrackifyApplication).errorHandler`.
- **UI** (`ui/`): single-Activity Compose app. `MainActivity` → `TrackifyApp` (Scaffold + bottom bar) → `TrackifyNavGraph`. Screens live in `ui/screens/<feature>/`, reusable composables in `ui/components/`, theming in `ui/theme/`.

### Navigation

All routes and navigation are centralized in `ui/navigation/NavGraph.kt`:
- `Screen` sealed class defines every route string; routes with arguments expose a `createRoute(...)` helper — use these rather than building route strings by hand.
- `NavigationActions` holds the navigation lambdas passed down into screens.
- Start destination is decided in `TrackifyApp` based on `PreferenceManager.isOnboardingCompleted()` (onboarding vs. home).
- The four bottom-tab routes (`home`, `payments`, `statistics`, `settings`) get directional slide transitions based on tab index.

### Application setup

`TrackifyApplication` (registered in the manifest) is a `Configuration.Provider` for WorkManager. On startup it initializes `NotificationHelper` (creates the notification channel), schedules notifications if enabled, and enqueues a weekly `DatabaseCleanupWorker` as unique periodic work named `database_cleanup`.

### Notifications & background work

Payment reminders use `AlarmManager`, not WorkManager. Flow: `NotificationScheduler` → `AlarmScheduler` (schedules the next alarm) → `NotificationReceiver` (BroadcastReceiver, checks due subscriptions and posts notifications) → reschedules the next alarm. `BootReceiver` re-arms alarms after device reboot. Periodic DB cleanup is the one task that uses WorkManager (`workers/DatabaseCleanupWorker`).

### Theme & localization

- **Theme**: `ThemeManager` (`utils/`) stores light/dark mode in its own SharedPreferences (`theme_prefs`); defaults to dark. `TrackifyTheme` consumes it. It uses a Compose `mutableIntStateOf` so theme changes recompose live.
- **Locale**: app supports English and Russian (`res/values` + `res/values-ru`). Language is persisted in `PreferenceManager`. `MainActivity.attachBaseContext` wraps the context via `LocaleHelper.getLocalizedContext`, and `LocaleManager`/`ProvideLocaleManager` (a `CompositionLocal`, see `utils/LocaleProvider.kt`) make locale changes take effect at runtime. When adding user-facing strings, add them to both `values/strings.xml` and `values-ru/strings.xml`.

### Preferences & error handling

- `PreferenceManager` (`utils/`) is the single SharedPreferences wrapper for app settings (notifications enabled, language, onboarding completed, currency, etc.) and supports change listeners via `OnPreferenceChangedListener`.
- `ErrorHandler` is an app-wide singleton; repositories surface failures as `Result.Error` and ViewModels forward them to it.

### Data export/import

`DataExportImportManager` (`utils/`) serializes the database to/from JSON via Gson for the backup feature on the Data Management screen.
