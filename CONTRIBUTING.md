# Contributing to Trackify

Thank you for your interest in contributing to Trackify! We're excited to collaborate with you. Whether you're fixing bugs, adding new features, improving documentation, or helping with translations, every contribution is valuable and appreciated.

## 🚀 Quick Start

1. **Fork** the repository on GitHub
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/trackify.git
   cd trackify
   ```
3. **Set up upstream** for syncing with the main repository:
   ```bash
   git remote add upstream https://github.com/onlive1337/trackify.git
   ```
4. **Open** the project in Android Studio and wait for Gradle sync to complete

## 🔄 Development Workflow

### Before Starting Work

1. **Check existing issues** to see if someone is already working on your idea
2. **Create or comment** on an issue to discuss your planned changes
3. **Sync** your fork with the latest changes:
   ```bash
   git checkout main
   git pull upstream main
   ```

### Making Changes

1. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/descriptive-name
   # or for bug fixes:
   git checkout -b fix/issue-description
   ```

2. **Make your changes** following our coding standards (see below)

3. **Test thoroughly** on different devices/screen sizes

4. **Commit** your changes with clear, descriptive messages:
   ```bash
   git add .
   git commit -m "Add subscription reminder customization feature
   
   - Allow users to set custom reminder intervals
   - Add UI for selecting reminder preferences
   - Update notification scheduling logic
   
   Fixes #42"
   ```

5. **Push** to your fork:
   ```bash
   git push origin feature/descriptive-name
   ```

6. **Create a Pull Request** to the main repository

## 📝 Coding Standards

### Kotlin Code Style

- Follow [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **meaningful names** for variables, functions, and classes
- **Prefer composition over inheritance**
- Use **data classes** for simple data holders
- Leverage **Kotlin's null safety** features

#### Example:
```kotlin
// ✅ Good
class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val categoryDao: CategoryDao
) {
    suspend fun getActiveSubscriptions(): List<Subscription> {
        return subscriptionDao.getActiveSubscriptions()
            .filter { it.endDate?.after(Date()) != false }
    }
}

// ❌ Avoid
class repo {
    fun get() {
        // unclear purpose, poor naming
    }
}
```

### Compose UI Guidelines

- **Prefer stateless composables** with state hoisting
- Use **Modifier extensively** for customization
- Follow **Material 3 design guidelines**
- Keep composables **small and focused**
- Use **preview annotations** for UI components

#### Example:
```kotlin
// ✅ Good
@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onCardClick() },
        colors = CardDefaults.cardColors(...)
    ) {
        // Card content
    }
}

@Preview
@Composable
private fun SubscriptionCardPreview() {
    TrackifyTheme {
        SubscriptionCard(
            subscription = previewSubscription,
            onCardClick = {}
        )
    }
}
```

### Architecture Guidelines

- Follow **MVVM pattern** with Repository
- Use **single source of truth** principle
- **Separate concerns** clearly between layers
- Prefer **dependency injection** (manual DI used in this project)
- Use **coroutines and Flow** for asynchronous operations

#### Repository Pattern:
```kotlin
// ✅ Good - Repository abstracts data access
class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val context: Context
) {
    val allPayments: LiveData<List<Payment>> = paymentDao.getAllPayments()
    
    suspend fun insert(payment: Payment): Result<Long> {
        return try {
            val id = paymentDao.insert(payment)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(context.getString(R.string.error_creating_payment), e)
        }
    }
}
```

### Documentation

- **Comment complex logic** and important decisions
- Use **KDoc** for public methods and classes
- Keep comments **up-to-date** with code changes
- Avoid **obvious comments**

```kotlin
/**
 * Calculates the next payment date for a subscription based on its billing frequency.
 * 
 * @param subscription The subscription to calculate for
 * @param fromDate Calculate from this date (usually current date)
 * @return Next payment date, or null if subscription has ended
 */
fun calculateNextPaymentDate(subscription: Subscription, fromDate: Date): Date? {
    // Complex logic with clear explanation
}
```

### Error Handling

- Use **sealed classes** for results (`Result<T>`)
- **Handle all error cases** appropriately
- **Provide meaningful error messages** to users
- **Log errors** for debugging

### Testing

- Write **unit tests** for new functionality
- Use **meaningful test names** that describe the scenario
- Test **both success and failure cases**
- Mock external dependencies appropriately

```kotlin
@Test
fun `calculateMonthlySpending returns correct total for mixed billing frequencies`() {
    // Given
    val subscriptions = listOf(
        monthlySubscription(price = 10.0),
        yearlySubscription(price = 120.0)
    )
    
    // When
    val result = statisticsCalculator.calculateMonthlySpending(subscriptions)
    
    // Then
    assertEquals(20.0, result) // 10 + (120/12)
}
```

## 🎨 UI/UX Guidelines

### Material 3 Compliance
- Use **Material 3 components** whenever possible
- Follow **color system** guidelines
- Implement **proper accessibility** (content descriptions, semantic roles)
- Support **dynamic colors** on Android 12+

### Responsive Design
- Test on **different screen sizes** and orientations
- Use **appropriate spacing** and sizing
- Ensure **touch targets** meet minimum size requirements (48dp)
- Handle **edge-to-edge** display properly

### Animation & Polish
- Use **meaningful animations** that enhance UX
- Keep animations **smooth and purposeful**
- Provide **loading states** for async operations
- Handle **empty states** gracefully

## 🌍 Localization

### Adding New Languages

1. **Create** `app/src/main/res/values-[language_code]/strings.xml`
2. **Translate all string resources** (400+ strings)
3. **Add language** to `LocaleHelper.getAvailableLanguages()`
4. **Test** the app in the new locale
5. **Update** documentation

### Translation Guidelines
- **Maintain context** and meaning
- **Keep string lengths** reasonable for UI
- **Use proper formatting** for plurals and parameters
- **Test on actual devices** with the target language

## 🧪 Testing Guidelines

### Before Submitting
- [ ] **Unit tests** pass: `./gradlew test`
- [ ] **App builds** successfully: `./gradlew assembleDebug`
- [ ] **Manual testing** on at least one device/emulator
- [ ] **No new warnings** in build output
- [ ] **Performance** hasn't degraded noticeably

### Testing Checklist
- [ ] **Core functionality** works as expected
- [ ] **Edge cases** are handled properly
- [ ] **Error states** display appropriate messages
- [ ] **Navigation** flows work correctly
- [ ] **Data persistence** functions properly
- [ ] **Notifications** work if applicable
- [ ] **Different screen sizes** are supported
- [ ] **Accessibility** features work

## 📋 Pull Request Process

### PR Requirements
- [ ] **Descriptive title** and description
- [ ] **Link to related issues** (use "Fixes #123")
- [ ] **Screenshots/videos** for UI changes
- [ ] **Testing evidence** (manual testing notes)
- [ ] **Breaking changes** documented
- [ ] **Backwards compatibility** maintained

### PR Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Manual testing completed
- [ ] Tested on: [device/emulator info]

## Screenshots
[Add screenshots for UI changes]

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated if needed
- [ ] No new warnings introduced
```

### Review Process
1. **Automated checks** must pass (CI/CD)
2. **Code review** by maintainers
3. **Testing** by reviewers if needed
4. **Merge** after approval

## 🏷️ Issue Guidelines

### Creating Issues

#### Bug Reports
```markdown
**Describe the bug**
Clear description of what the bug is

**To Reproduce**
Steps to reproduce:
1. Go to '...'
2. Click on '....'
3. See error

**Expected behavior**
What you expected to happen

**Screenshots**
Add screenshots if applicable

**Device Info:**
- Device: [e.g. Pixel 6]
- OS: [e.g. Android 13]
- App Version: [e.g. 1.2.0]
```

#### Feature Requests
```markdown
**Is your feature request related to a problem?**
Clear description of the problem

**Describe the solution you'd like**
Clear description of what you want to happen

**Describe alternatives considered**
Any alternative solutions or features considered

**Additional context**
Mockups, examples, or other context
```

### Issue Labels
- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Improvements or additions to docs
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention is needed
- `question` - Further information is requested

## 🛠️ Development Environment

### Required Tools
- **Android Studio** Arctic Fox 2020.3.1 or later
- **JDK 8+** (JDK 11 recommended)
- **Android SDK** with API levels 28-35
- **Git** for version control

### Recommended Setup
- **Enable** auto-format on save
- **Install** Kotlin plugin updates
- **Configure** code style settings
- **Set up** live templates for common patterns

### Project Dependencies
- **Compile SDK**: 35
- **Min SDK**: 28
- **Target SDK**: 35
- **Kotlin**: 2.1.21
- **Compose BOM**: 2025.06.00

## 📊 Project Stats

- **Languages**: Kotlin (95%), XML (5%)
- **Architecture**: MVVM + Repository
- **Database**: Room (SQLite)
- **UI**: 100% Jetpack Compose
- **Dependencies**: Minimal, well-maintained
- **Test Coverage**: Aiming for 80%+

## 💬 Communication

### Getting Help
- **GitHub Discussions** for questions and ideas
- **GitHub Issues** for bugs and feature requests
- **Telegram** [@onswix](https://t.me/onswix) for urgent matters

### Code of Conduct
- **Be respectful** and inclusive
- **Provide constructive feedback**
- **Focus on the code**, not the person
- **Help others learn** and grow

## 🎯 Roadmap & Priorities

### High Priority
- 🐛 **Bug fixes** and stability improvements
- 🌍 **Localization** to more languages
- ♿ **Accessibility** improvements
- 📱 **Tablet optimization**

### Medium Priority
- 🔄 **Data sync** capabilities
- 📊 **Advanced analytics**
- 🎨 **More customization options**
- 🔔 **Enhanced notifications**

### Future Ideas
- 🤖 **Smart spending insights**
- 📈 **Budget planning tools**
- 🔗 **Integration with banks/services**
- ☁️ **Cloud backup options**

## 🙏 Recognition

All contributors will be recognized in:
- **README.md** contributors section
- **Release notes** for their contributions
- **Special thanks** in the app's about section

Thank you for making Trackify better for everyone! 🚀

---

**Questions?** Feel free to reach out through any of our communication channels. We're here to help!