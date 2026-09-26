# Android 17 SDK

- Compile SDK and app target SDK: 37 (Android 17, base SDK 37.0).
- Minimum SDK: unchanged at 28 (Android 9).
- SDK Build Tools: 37.0.0.
- Android Gradle Plugin: 9.1.1; Gradle wrapper: 9.3.1, checksum-pinned.
- Build/test JDK: 21; app bytecode target: unchanged at 17.
- Kotlin/Compose compiler: 2.2.10, using AGP's built-in Kotlin support.
- KSP: 2.3.10; Hilt: 2.59.2.
- Paparazzi: 2.0.0-alpha05 for Gradle 9 compatibility. Older screenshot baselines
  may need review with the newer renderer; they are not automatically re-recorded.

The obsolete Molecule compiler plugin has been removed; the official Kotlin Compose compiler
already performs that work. The test-support module now declares Molecule runtime explicitly.
The unused, nonexistent `:shared:common-ui` project entry was removed because Gradle 9 rejects
project entries without directories. No app data or installed applications are removed.

Textual Compose compiler reports are opt-in with `-PcomposeCompilerReports=true`: the Kotlin
2.2.10 report printer crashes on some legacy composable default expressions. Metrics remain
enabled, and this does not disable app compilation or tests.

Install the SDK with:

```sh
sdkmanager 'platforms;android-37.0' 'build-tools;37.0.0'
./gradlew :app:assembleDebug --no-configuration-cache --max-workers=2
```

Use JDK 21 for both commands. Android Studio Panda 3 Patch 1 or later supports this toolchain.

The app already uses edge-to-edge layout and the AndroidX back dispatcher. Targeting a new SDK
also opts into new runtime behavior, so Android 17 device checks of navigation, large-screen
layouts, widgets, notifications, and cloud sync are still needed. Public TLS cloud endpoints
are the intended sync configuration; custom LAN endpoints require separate review of Android
17's local-network permission requirement.

References:

- [Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk)
- [AGP 9.1.1 compatibility](https://developer.android.com/build/releases/agp-9-1-0-release-notes)
- [Built-in Kotlin migration](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [Android 17 target-SDK behavior changes](https://developer.android.com/about/versions/17/behavior-changes-17)
