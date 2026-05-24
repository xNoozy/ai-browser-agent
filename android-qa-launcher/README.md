# QA Lab Launcher - Android QA Testing Suite

An Android launcher & work profile manager application designed for internal QA testing. This app provides isolated sandbox environments, real-time monitoring, device compatibility testing, and automated UI testing - all without bypassing any third-party security mechanisms.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │Dashboard │  │Monitoring│  │Device Lab│  │ Sandbox  │       │
│  │  Screen  │  │  Screen  │  │  Screen  │  │  Screen  │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │              │              │              │             │
│  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐       │
│  │Dashboard │  │Monitoring│  │DeviceLab │  │ Sandbox  │       │
│  │ViewModel│  │ViewModel│  │ViewModel│  │ViewModel│       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
├───────┼──────────────┼──────────────┼──────────────┼────────────┤
│                    DOMAIN LAYER                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌────────────────┐  │
│  │   Use Cases     │  │   Repositories  │  │    Models       │  │
│  │  (Interfaces)   │  │  (Interfaces)   │  │                │  │
│  └────────┬────────┘  └────────┬────────┘  └────────────────┘  │
├───────────┼─────────────────────┼───────────────────────────────┤
│                    DATA LAYER                                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌────────────────┐  │
│  │  Room Database  │  │  Repository     │  │  Firebase      │  │
│  │  (DAOs/Entities)│  │  Implementations│  │  Test Lab      │  │
│  └─────────────────┘  └─────────────────┘  └────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                    SERVICE LAYER                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ Sandbox  │  │Monitoring│  │ Plugin   │  │Accessibi-│       │
│  │ Manager  │  │ Service  │  │ Manager  │  │lity Svc  │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

## Features

### 1. Sandbox/Work Profile Manager
- Launch target apps in isolated work profiles
- Configurable sandbox: isolate storage, network, permissions
- Clear app data on session stop
- Work profile provisioning via Android Device Admin API

### 2. Real-time Monitoring
- **Crash Detection**: Process monitoring + ApplicationExitInfo API
- **ANR Detection**: ActivityManager error state polling
- **Logcat Capture**: Real-time log filtering and parsing
- **Network Monitoring**: Traffic stats, bandwidth detection, connectivity callbacks
- **Battery Monitoring**: Drain rate calculation, temperature tracking
- **Permission Monitoring**: Track permission grants/revocations

### 3. Device Profile Manager
- Predefined profiles: Pixel 7, Galaxy S23, Pixel Tablet, Budget Phone, Z Fold
- Custom profile creation with configurable:
  - Screen dimensions (dp), density (dpi), SDK version
  - Locale, orientation, font scale, night mode
  - Network type simulation
- Apply profiles via ADB commands (emulator integration)

### 4. Plugin System
- Built-in plugins:
  - **Monkey Test**: Random UI interaction stress testing
  - **UI Traversal**: Systematic screen exploration
  - **Stress Test**: Memory leak & performance monitoring
- Custom script plugins (JSON-based test definitions)
- Plugin progress tracking & result reporting

### 5. Emulator & Firebase Test Lab Integration
- List and launch Android emulators
- ADB device management
- Firebase Test Lab API integration (Robo & Instrumentation tests)
- Remote test execution & result retrieval

### 6. Accessibility-Based Automation
- User-consented accessibility service
- UI hierarchy inspection
- Programmatic gestures (tap, swipe, scroll)
- Element finding by text/ID/content description

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt (Dagger) |
| Database | Room |
| Async | Kotlin Coroutines + Flow |
| Navigation | Navigation Compose |
| Network | OkHttp + Gson |
| Elevated Ops | Shizuku API |
| Charts | Vico |
| Testing | JUnit + MockK + Espresso |

## Project Structure

```
android-qa-launcher/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/qalab/launcher/
│       │   ├── QALabApplication.kt            # Application class
│       │   ├── di/
│       │   │   └── AppModule.kt               # Hilt DI module
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── dao/                   # Room DAOs
│       │   │   │   ├── entity/                # Room entities
│       │   │   │   └── database/              # Database definition
│       │   │   ├── remote/
│       │   │   │   └── FirebaseTestLabClient.kt
│       │   │   └── repository/                # Repository implementations
│       │   ├── domain/
│       │   │   ├── model/                     # Domain models
│       │   │   ├── repository/                # Repository interfaces
│       │   │   └── usecase/                   # Use cases
│       │   ├── presentation/
│       │   │   ├── theme/                     # Compose theme
│       │   │   ├── navigation/                # Nav graph
│       │   │   ├── ui/
│       │   │   │   ├── MainActivity.kt
│       │   │   │   ├── dashboard/             # Dashboard screen
│       │   │   │   ├── monitoring/            # Monitoring screen
│       │   │   │   ├── devicelab/             # Device Lab screen
│       │   │   │   ├── sandbox/               # Sandbox screen
│       │   │   │   ├── plugin/                # Plugin screen
│       │   │   │   └── settings/              # Settings screen
│       │   │   └── viewmodel/                 # ViewModels
│       │   ├── service/
│       │   │   ├── monitoring/                # Background monitors
│       │   │   ├── sandbox/                   # Work profile management
│       │   │   ├── plugin/                    # Test plugins
│       │   │   └── accessibility/             # UI automation service
│       │   └── util/
│       │       └── EmulatorIntegration.kt
│       └── res/
│           ├── values/
│           └── xml/
├── build.gradle.kts                           # Root build file
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## Architecture Flow

### Session Lifecycle
```
User creates session → SandboxManager creates isolated context
    → App launched in work profile
    → MonitoringService starts (foreground)
        → CrashMonitor (polls every 2s)
        → LogcatMonitor (streams output)
        → NetworkMonitor (polls every 5s)
        → BatteryMonitor (polls every 5s)
        → PermissionMonitor (polls every 3s)
    → Logs stored in Room DB
    → UI updates via Flow/StateFlow
User stops session → Cleanup + optional data clear
```

### Plugin Execution Flow
```
User selects plugin → PluginManager.executePlugin()
    → Plugin.initialize(config)
    → Plugin.execute(session)
        → Uses AccessibilityService for UI interaction
        → Reports progress via callback
    → Plugin.cleanup()
    → TestResult stored & displayed
```

## Security Policy

This application **does NOT**:
- Bypass security checks of third-party apps
- Hook into other apps' processes
- Modify APK signatures
- Disable root detection
- Tamper with app integrity mechanisms
- Use Xposed/Magisk/Frida or similar tools

All operations use **official Android APIs**:
- DevicePolicyManager for work profiles
- AccessibilityService (user-consented) for UI testing
- Shizuku for elevated operations (user-authorized)
- Standard ActivityManager/PackageManager for monitoring

## Building

```bash
cd android-qa-launcher
./gradlew assembleDebug
```

## Requirements

- Android 9.0+ (API 28)
- Target SDK 34
- Shizuku (optional, for elevated operations)
- Device Admin privileges (for work profile creation)

## License

Internal Use Only - Proprietary
