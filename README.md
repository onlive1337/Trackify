# Trackify 📱

<div align="center">

**Modern subscription management app for Android**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Material 3](https://img.shields.io/badge/Design-Material%203-blue.svg)](https://m3.material.io)
[![Min API](https://img.shields.io/badge/API-28+-brightgreen.svg)](https://android-arsenal.com/api?level=28)

*Never miss a subscription payment again*

[Download APK](https://github.com/onlive1337/trackify/releases) • [Report Bug](https://github.com/onlive1337/trackify/issues) • [Request Feature](https://github.com/onlive1337/trackify/issues)

</div>

## ✨ Features

### 📊 Smart Tracking
- **Subscription Management** - Add, edit, and organize all your recurring payments
- **Payment History** - Complete transaction log with notes and dates
- **Smart Notifications** - Customizable reminders before payments are due
- **Multi-Currency Support** - Track subscriptions in USD, EUR, RUB, and more

### 📈 Analytics & Insights
- **Statistics Dashboard** - Visualize spending patterns with interactive charts
- **Category Organization** - Custom categories with color coding
- **Monthly/Yearly Overview** - Track spending trends over time
- **Expense Analysis** - Breakdown by subscription type and frequency

### 🎨 Modern Experience
- **Material 3 Design** - Beautiful, intuitive interface with dynamic colors
- **Dark/Light Theme** - Automatic theme switching based on system preferences
- **Multi-language Support** - Available in English and Russian
- **Responsive UI** - Optimized for all screen sizes

### 🔒 Privacy & Data
- **Local Storage** - All data stays on your device
- **Data Export/Import** - Backup and restore via JSON files
- **No Telemetry** - Zero data collection or tracking
- **Open Source** - Transparent and community-driven

## 📱 Screenshots

| Home Screen                        | Statistics                           | Settings                                   | Add Subscription                 |
|------------------------------------|--------------------------------------|--------------------------------------------|----------------------------------|
| ![Home](docs/screenshots/home.png) | ![Stats](docs/screenshots/stats.png) | ![Settings](docs/screenshots/settings.png) | ![Add](docs/screenshots/add.png) |

## 🛠️ Tech Stack

### Core
- **Language**: [Kotlin](https://kotlinlang.org/) 2.1.21
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Architecture**: MVVM with Repository pattern
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)

### Data & Storage
- **Database**: [Room](https://developer.android.com/jetpack/androidx/releases/room) (SQLite)
- **Serialization**: [Gson](https://github.com/google/gson)
- **Preferences**: SharedPreferences with custom wrapper

### Async & Background
- **Concurrency**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) + Flow
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Lifecycle**: [Lifecycle-aware components](https://developer.android.com/topic/libraries/architecture/lifecycle)

### Additional Features
- **Charts**: Custom Compose-based charts
- **Dependency Injection**: Manual DI (lightweight approach)
- **Crash Reporting**: Firebase Crashlytics
- **Notifications**: Android notification system
- **File I/O**: Scoped Storage for data export/import

## 📋 Requirements

- **Android 9.0 (API 28)** or higher
- **Target Android 14 (API 35)**
- **RAM**: 2GB+ recommended
- **Storage**: 50MB available space

## 🚀 Installation

### Option 1: Download Release APK
1. Go to [Releases](https://github.com/onlive1337/trackify/releases)
2. Download the latest `trackify-v*.apk`
3. Enable "Install from unknown sources" in your device settings
4. Install the APK

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/onlive1337/trackify.git
cd trackify

# Open in Android Studio
# Android Studio Arctic Fox 2020.3.1 or later required

# Build and install
./gradlew assembleDebug
# or use Android Studio's Run button
```

## 🏗️ Project Structure

```
app/src/main/java/com/onlive/trackify/
├── data/                   # Data layer
│   ├── database/          # Room database (DAOs, entities, converters)
│   ├── model/             # Data models (Subscription, Payment, Category)
│   ├── repository/        # Repository implementations
│   └── LiveStatisticsUpdater.kt  # Real-time statistics calculator
├── ui/                    # UI layer
│   ├── components/        # Reusable Compose components
│   ├── screens/           # Screen implementations
│   │   ├── home/         # Subscription list
│   │   ├── payments/     # Payment management
│   │   ├── statistics/   # Analytics dashboard
│   │   ├── settings/     # App configuration
│   │   └── onboarding/   # First-time setup
│   ├── navigation/        # Navigation setup
│   └── theme/             # Material 3 theming
├── utils/                 # Utility classes
│   ├── CurrencyFormatter.kt    # Multi-currency support
│   ├── DateUtils.kt           # Date handling
│   ├── NotificationHelper.kt  # Push notifications
│   ├── PreferenceManager.kt   # Settings management
│   └── DataExportImportManager.kt  # Backup functionality
├── viewmodel/             # ViewModels (MVVM)
└── workers/               # Background tasks
```

## 🔧 Development Setup

### Prerequisites
- **Android Studio**: Arctic Fox 2020.3.1+
- **JDK**: 8 or higher
- **Android SDK**: API 28-35
- **Kotlin**: 2.1.21

### Getting Started
1. **Fork** the repository
2. **Clone** your fork:
   ```bash
   git clone https://github.com/onlive1337/trackify.git
   ```
3. **Open** in Android Studio
4. **Wait** for Gradle sync to complete
5. **Run** the app on device/emulator

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing)
./gradlew assembleRelease

# Run tests
./gradlew test

# Generate APK
./gradlew build
```

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# Test coverage
./gradlew jacocoTestReport
```

## 🌍 Localization

Currently supported languages:
- 🇺🇸 **English** (default)
- 🇷🇺 **Russian**

### Adding a new language:
1. Create `values-[language]/strings.xml`
2. Translate all string resources
3. Add language to `LocaleHelper.getAvailableLanguages()`
4. Test with different locales

## 🤝 Contributing

We welcome contributions! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

### Quick Start
1. **Check** [Issues](https://github.com/onlive1337/trackify/issues) for available tasks
2. **Fork** the repository
3. **Create** a feature branch: `git checkout -b feature/amazing-feature`
4. **Make** your changes following our [coding standards](CONTRIBUTING.md#coding-standards)
5. **Test** your changes thoroughly
6. **Commit** with descriptive messages: `git commit -m 'Add amazing feature'`
7. **Push** to your branch: `git push origin feature/amazing-feature`
8. **Open** a Pull Request

### Areas needing help:
- 🌍 Translations to other languages
- 🎨 UI/UX improvements
- 📱 Testing on different devices
- 📝 Documentation improvements
- 🐛 Bug fixes and performance optimizations

## 🛡️ Privacy & Security

**Trackify is designed with privacy in mind:**

- ✅ **No data collection** - Zero telemetry or analytics
- ✅ **Local storage only** - All data stays on your device
- ✅ **No network requests** - Except for crash reporting (optional)
- ✅ **Open source** - Transparent code you can audit
- ✅ **No ads or tracking** - Completely free and clean experience

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License - Feel free to use, modify, and distribute
```

## 🐛 Bug Reports & Feature Requests

- **🐛 Bug reports**: [Create an issue](https://github.com/onlive1337/trackify/issues/new?template=bug_report.md)
- **💡 Feature requests**: [Create an issue](https://github.com/onlive1337/trackify/issues/new?template=feature_request.md)
- **❓ Questions**: [Discussions](https://github.com/onlive1337/trackify/discussions)

## 📞 Contact & Support

- **Telegram**: [@onswix](https://t.me/onswix)
- **GitHub Issues**: [Create an issue](https://github.com/onlive1337/trackify/issues)
- **Developer Telegram**: [@onlivedev](https://t.me/onlivedev)

## 🙏 Acknowledgments

- **Material Design** team for the amazing design system
- **JetBrains** for Kotlin and excellent development tools
- **Android team** for Jetpack Compose and modern Android development
- **Open source community** for inspiration and contributions
- **All contributors** who help improve this project

## ⭐ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=onlive1337/trackify&type=Date)](https://star-history.com/#onlive1337/trackify&Date)

---

<div align="center">

**Made with ❤️ for the open source community**

[⭐ Star this repo](https://github.com/onlive1337/trackify) • [🐛 Report Bug](https://github.com/onlive1337/trackify/issues) • [💡 Request Feature](https://github.com/onlive1337/trackify/issues) • [🤝 Contribute](CONTRIBUTING.md)

**Support the project by giving it a star! ⭐**

</div>