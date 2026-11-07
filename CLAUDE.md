# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Trackify is a native Android app (Kotlin + Jetpack Compose, Material 3 Expressive) for tracking subscriptions and recurring payments. All data is stored locally; there is no backend, telemetry, or network sync. Package: `com.onlive.trackify`, minSdk 28, target/compileSdk 37, Java 17 target.

## Build & Test

```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # build + install on connected device/emulator
./gradlew testDebugUnitTest      # run JVM unit tests
./gradlew lintDebug              # run Android Lint on debug variant
./gradlew assembleRelease        # release APK (minified + shrunk; requires keystore)
./gradlew bundleRelease          # release AAB for Google Play (requires keystore)
./gradlew build                  # full build incl. tests & lint
```

JVM unit tests live under `app/src/test/java/com/onlive/trackify/` (`PaymentScheduleCalculatorTest`, `NotificationCheckerTest`, `LiveStatisticsUpdaterTest`, `DataExportImportManagerTest`, `SubscriptionViewModelTest`).

Release signing reads from env vars (`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) or, as a fallback, a `keystore.properties` file in the repo root. Release tasks (`assembleRelease`, `bundleRelease`, etc.) fail fast if neither is present — release builds never fall back to the debug signing key.

Dependencies are managed via the version catalog at `gradle/libs.versions.toml` — add/upgrade libraries there, not inline in `app/build.gradle.kts`.

## Architecture

MVVM with a manual (no DI framework) dependency graph rooted in `TrackifyApplication`. Layering: Compose screen → `*ViewModel` → `*Repository` → Room DAO.

- **Data layer** (`data/`): Room `AppDatabase` is a singleton obtained via `AppDatabase.getDatabase(context)` (`version = 1`, `exportSchema = true` with schemas under `app/schemas/`). Pure immutable entities `Subscription`, `Payment`, `Category` (`data/model/`) use `java.time.LocalDate` for calendar dates, joined via Room `@Relation` (`SubscriptionWithCategory`). DAOs in `data/database/` return reactive Kotlin `Flow` streams and `suspend` write/snapshot functions. `Converters` maps `LocalDate` (epoch day) and `BillingFrequency`. Bumping `version` requires adding a `Migration` to `AppDatabase.MIGRATIONS`.
- **Repositories** (`data/repository/`): pure-Kotlin singletons (no Android `Context`) lazily provided on `TrackifyApplication`. Write operations (`insert`/`update`/`delete`) run on an injected `CoroutineDispatcher` (defaulting to `Dispatchers.IO`) and return `Result<T>` (`utils/Result.kt`, `Success`/`Error(@StringRes val messageRes: Int)`). Reads expose `Flow` (`SubscriptionRepository` exposes `Flow<List<SubscriptionWithCategory>>` directly from Room `@Transaction` queries). `LiveStatisticsUpdater` (`data/`) is also pure Kotlin and aggregates active subscription spending via `map` + `distinctUntilChanged`.
- **ViewModels** (`viewmodel/`): plain `ViewModel` subclasses (not `AndroidViewModel`) wired via `viewModelFactory` (`*ViewModel.Factory` reading `APPLICATION_KEY`) and testable in pure JVM JUnit tests. Write operations accept `onSuccess`/`onError` callbacks so form screens pop the back stack only after Room writes complete, and `SubscriptionViewModel` triggers `NotificationScheduler` re-arming on mutations. Failures report to `ErrorReporter` (`ErrorHandler`).
- **UI** (`ui/`): single-Activity Material 3 Expressive (`MD3E`) Compose app (`MaterialExpressiveTheme`, `MotionScheme.expressive()`, `ShortNavigationBar`, `SegmentedListItem`, `MediumFlexibleTopAppBar`, `SearchBarDefaults.InputField` with `TextFieldState`, pure platform `android:Theme.Material.*.NoActionBar` XML theme with no legacy `com.google.android.material` View dependency). Hierarchy: `MainActivity` → `TrackifyApp` (root `Scaffold` + `SnackbarHost`) → `TrackifyNavGraph`. Screens live in `ui/screens/<feature>/`, reusable composables in `ui/components/`, theming in `ui/theme/`.

### Navigation

All routes and navigation are centralized in `ui/navigation/NavGraph.kt`:
- `Screen` is a `@Serializable` sealed interface using type-safe Navigation Compose (`composable<Screen.*>`, `backStackEntry.toRoute<...>()`, `destination.hasRoute(...)`). Keep `proguard-rules.pro` `@Serializable` rules intact for R8.
- `NavigationActions` holds navigation lambdas passed down into screens.
- Start destination is decided in `TrackifyApp` based on `PreferenceManager.isOnboardingCompleted()` (`Screen.Onboarding` vs. `Screen.Home`).
- The four bottom-tab routes (`Home`, `Payments`, `Statistics`, `Settings`) use directional slide + fade transitions derived from `MotionScheme` specs.

### Application setup & background work

`TrackifyApplication` is a `Configuration.Provider` for WorkManager, installs `CrashLogger`, creates the notification channel via `NotificationHelper`, schedules reminders if enabled, and enqueues two unique periodic WorkManager jobs:
- `DatabaseCleanupWorker` (`database_cleanup`, every 7 days) — cleans up stale records and old notification dedup keys.
- `NotificationRefreshWorker` (`notification_refresh`, every 12 hours) — safety net ensuring alarms stay armed.

Payment reminders themselves use `AlarmManager`: `NotificationScheduler` → `AlarmScheduler` → `NotificationReceiver` (`NotificationChecker` checks due subscriptions using date-keyed deduplication and posts notifications) → reschedules the next alarm. `BootReceiver` re-arms alarms after reboot or app update.

### Theme, localization & currency

- **Theme**: `ThemeManager` (`utils/`) delegates light/dark mode and dynamic color persistence to `PreferenceManager` (`trackify_preferences`, with transparent migration from legacy `theme_prefs`), backed by Compose state so `TrackifyTheme` recomposes live.
- **Locale**: supports English and Russian (`res/values` + `res/values-ru`, `enableSplit = false` in AAB bundle config so in-app language switching works after Play Store install). `MainActivity.attachBaseContext` wraps the base context via `LocaleHelper`, and `ProvideLocaleManager` (`utils/LocaleProvider.kt`) provides `LocalLocalizedContext` at runtime. Always add user-facing strings to both `values/strings.xml` and `values-ru/strings.xml`, and avoid `Locale.getDefault()` inside `@Composable` functions (`NonObservableLocale` lint error).
- **Currency**: `ProvideCurrency` (`utils/CurrencyFormatter.kt`) observes `PreferenceManager.KEY_CURRENCY_CODE` via `SharedPreferences.OnSharedPreferenceChangeListener` and exposes `LocalCurrency`. Use `@Composable CurrencyFormatter.formatAmount(amount)` or `LocalCurrency.current.format(amount)` in Compose UI; use `CurrencyFormatter.formatAmount(context, amount)` only outside Compose (e.g., `NotificationHelper`).

### Preferences, error handling & backup

- `PreferenceManager` (`utils/`) is the single `SharedPreferences` store (`trackify_preferences`) for onboarding, theme mode, dynamic color, notifications, reminder days/time, language, currency, home sort option, and notification deduplication keys.
- `ErrorHandler` (`utils/`) implements `ErrorReporter`, emits `UiErrorMessage` events on `errors: SharedFlow<UiErrorMessage>` collected by the root `SnackbarHost` in `TrackifyApp` (resolved against `LocalLocalizedContext`), and logs exceptions via `CrashLogger`.
- `DataExportImportManager` (`utils/`) exports and imports versioned (`backupVersion = 1`) JSON backups using `kotlinx.serialization-json` DTOs inside a Room transaction, applying restored theme, locale, currency, and notification schedules live.
