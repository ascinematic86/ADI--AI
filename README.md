# Adi AI Android Assistant

Basic Android voice/text command assistant with Marathi speech recognition, text-to-speech, camera, dialer, alarm, settings, flashlight, volume and web search. Speech recognition requires a compatible service on the device. This is an MVP, not an always-on wake-word assistant.

## Build APK
Upload the contents of this ZIP to a GitHub repository (including `.github/workflows/build-apk.yml`). In GitHub Actions, run **Build Adi AI APK** or push to `main`. Download artifact **AdiAI-debug-APK**, extract and install `app-debug.apk` on Android.

Permissions: microphone and camera (for flashlight). Some actions open system apps and require user interaction.
