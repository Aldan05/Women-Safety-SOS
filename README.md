# Women Safety SOS Assistant

An Android application designed to provide quick emergency response, wake word detection, location tracking, and SOS alerts for women's safety.

## Features

- **Wake Word Detection**: Background service listening for emergency trigger words to automatically initiate SOS.
- **Instant SOS Alerting**: Triggers alarms, sends SMS with real-time location to emergency contacts, and initiates emergency calls.
- **Location Tracking**: Continuously tracks and shares location coordinates during emergency states.
- **Emergency Contacts Management**: Add, edit, and organize trusted contacts for distress communication.
- **Movement Detection**: Detects unusual movement patterns or shakes to trigger safety mechanisms.
- **Screen & Hardware Triggers**: Quick activation via power/screen button combinations and boot listeners.

## Tech Stack

- **Platform**: Android
- **Language**: Kotlin / Java
- **Build System**: Gradle (Kotlin DSL)
- **Services & APIs**:
  - Android Foreground Services (Microphone, Location, Data Sync)
  - Firebase Authentication & Cloud Services
  - Android Location Services & Telephony APIs

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/Aldan05/Women-Safety-SOS.git
   ```
2. Open the project in **Android Studio** (Hedgehog or newer recommended).
3. Ensure you have your `google-services.json` in the `app/` directory if connecting to your own Firebase project.
4. Sync Gradle and run on a physical device or emulator (physical device recommended for telephony and sensor features).
