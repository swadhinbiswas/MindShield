# Contributing to MindShield

First off, thank you for considering contributing to MindShield! 🎉

## Code of Conduct

This project and everyone participating in it is governed by our commitment to creating a welcoming and inclusive community. Please be respectful and constructive in all interactions.

## How Can I Contribute?

### 🐛 Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

- **Device Information**: Android version, device model
- **App Version**: Check in Settings
- **Steps to Reproduce**: Detailed steps to reproduce the issue
- **Expected Behavior**: What you expected to happen
- **Actual Behavior**: What actually happened
- **Screenshots**: If applicable

### 💡 Suggesting Features

Feature suggestions are welcome! Please:

1. Check if the feature is already in our [Roadmap](README.md#-roadmap)
2. Search existing issues for similar suggestions
3. Create a new issue with the `enhancement` label
4. Describe the feature and why it would be useful

### 🔧 Pull Requests

1. **Fork** the repository
2. **Clone** your fork: `git clone https://github.com/YOUR_USERNAME/MindShield.git`
3. **Create** a branch: `git checkout -b feature/amazing-feature`
4. **Make** your changes
5. **Test** your changes thoroughly
6. **Commit**: `git commit -m 'Add amazing feature'`
7. **Push**: `git push origin feature/amazing-feature`
8. **Open** a Pull Request

### Development Setup

```bash
# Clone the repository
git clone https://github.com/swadhin/MindShield.git
cd MindShield/FocusGuard

# Install dependencies
npm install

# Start Metro bundler
npm start

# Run on Android (debug mode)
npm run android

# Build release APK
cd android && ./gradlew assembleRelease
```

### Code Style

- Use TypeScript for React Native code
- Follow existing code formatting
- Write meaningful commit messages
- Add comments for complex logic
- Update documentation when needed

### Testing

- Test on multiple Android versions if possible
- Verify blocking functionality works correctly
- Check for memory leaks and performance issues
- Test both light and dark themes

## Project Structure

```
FocusGuard/
├── src/
│   ├── navigation/     # React Navigation setup
│   ├── screens/        # UI screens
│   ├── services/       # Business logic
│   ├── store/          # Zustand state management
│   ├── components/     # Reusable components
│   └── theme/          # Theme configuration
├── android/
│   └── app/src/main/java/com/focusguard/
│       ├── BlockingAccessibilityService.java
│       ├── BlockOverlayManager.java
│       └── BlockingModule.java
└── docs/               # Website files
```

## Questions?

Feel free to open an issue with the `question` label or reach out through GitHub Discussions.

Thank you for helping make MindShield better! 🛡️
