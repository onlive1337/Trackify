# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Performance improvements for large subscription lists
- Better error handling with user-friendly messages
- Enhanced accessibility support

### Changed
- Improved UI animations and transitions
- Updated dependencies to latest stable versions

### Fixed
- Minor UI glitches on different screen sizes
- Notification scheduling edge cases

## [1.0.0] - 2025-01-15

### Added
- 🎉 **Initial release of Trackify**
- ✨ Complete subscription management system
- 📊 Comprehensive statistics and analytics dashboard
- 🔔 Smart notification system with customizable reminders
- 🎨 Material 3 design with dark/light theme support
- 🌍 Multi-language support (English, Russian)
- 💱 Multi-currency support (USD, EUR, RUB, and more)
- 📱 Responsive design for all Android screen sizes
- 📂 Data export/import functionality (JSON format)
- 🔒 Complete privacy with local-only data storage
- 📈 Interactive charts for spending visualization
- 🏷️ Custom categories with color coding
- ⚡ Background tasks with WorkManager
- 🚀 Onboarding flow for new users

### Core Features
- **Subscription Management**
    - Add, edit, and delete subscriptions
    - Set billing frequency (monthly/yearly)
    - Custom start and end dates
    - Rich subscription details with descriptions

- **Payment Tracking**
    - Complete payment history
    - Add manual payments with notes
    - View payments by subscription
    - Track payment patterns

- **Statistics & Analytics**
    - Monthly and yearly spending overview
    - Category-based spending breakdown
    - Historical spending trends (6-month view)
    - Subscription type analysis (monthly vs yearly)
    - Interactive charts and visualizations

- **Smart Notifications**
    - Customizable reminder days (0, 1, 3, 7 days before)
    - Configurable notification time
    - Payment due reminders
    - Subscription expiration alerts
    - Respects system notification preferences

- **Categories & Organization**
    - Create custom categories
    - Color-coded organization
    - Category-based filtering
    - Spending analysis by category

- **Data Management**
    - Export all data to JSON
    - Import data from backup files
    - Data validation and error handling
    - Backwards compatibility support

- **Settings & Customization**
    - Theme selection (light/dark/system)
    - Language preferences
    - Currency configuration
    - Notification preferences
    - Category management

### Technical Highlights
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite) with type converters
- **UI**: 100% Jetpack Compose with Material 3
- **Async**: Kotlin Coroutines and Flow
- **Background**: WorkManager for notifications
- **Navigation**: Navigation Compose
- **Dependency Injection**: Manual DI (lightweight)
- **Testing**: Unit tests for core functionality
- **Build System**: Gradle with Kotlin DSL
- **Minimum SDK**: Android 9.0 (API 28)
- **Target SDK**: Android 14 (API 35)

### Security & Privacy
- ✅ No data collection or telemetry
- ✅ All data stored locally on device
- ✅ No network requests (except optional crash reporting)
- ✅ Open source and transparent
- ✅ No ads or tracking

### Performance
- ⚡ Fast app startup (< 2 seconds)
- 📱 Optimized for low-end devices
- 🔋 Battery-efficient background tasks
- 💾 Minimal storage footprint (~50MB)
- 📊 Smooth 60fps animations
- 🚀 Lazy loading for large datasets

### Known Issues
- Some animations may stutter on very old devices (Android 9)
- Large datasets (1000+ subscriptions) may experience slight delays
- Dark theme may not follow system theme changes immediately

---

## Release Process

### Version Numbering
- **Major** (X.0.0): Breaking changes, major new features
- **Minor** (1.X.0): New features, backwards compatible
- **Patch** (1.0.X): Bug fixes, minor improvements

### Release Channels
- **Stable**: Tested releases for general use
- **Beta**: Preview releases for early feedback
- **Alpha**: Development builds (internal testing)

### What's Next?
Check our [project roadmap](https://github.com/onlive1337/trackify/projects) to see planned features for upcoming releases.

### Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md) for information on how to contribute to future releases.

---

**Note**: This changelog will be updated with each release. For the most current information, check the [GitHub Releases](https://github.com/onlive1337/trackify/releases) page.