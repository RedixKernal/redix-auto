# RedixCarNative (Redix Auto)

A high-performance in-car video and web engine for Android Auto, optimized for automotive displays including the Renault Kwid Media Nav Evolution (800x480 @ 160 DPI).

## Features

- **Ultra-Clean In-Car UI**: Streamlined single-line toolbar with quick presets (YouTube, Spotify, Google).
- **100% Borderless Fullscreen**: Expand video or web content to 100% borderless display at a single tap.
- **HTML5 Fullscreen Support**: Native YouTube / web video full screen handling.
- **Steering Wheel & Audio Focus**: Android MediaSession integration for car volume and playback controls.
- **Desktop Head Unit (DHU) Emulation Ready**: Pre-configured with Kwid 800x480 touch profile (`kwid.ini`).

## Project Setup & Build

### Requirements

- Android Studio / Android SDK (API 35)
- Android Auto Desktop Head Unit (DHU)

### Build Debug APK

```bash
./gradlew assembleDebug
```

### Build Release APK

```bash
./gradlew assembleRelease
```

### Install onto Device

To ensure Android Auto launcher keeps the app visible:

```bash
adb install -r -g -i com.android.vending app/build/outputs/apk/release/app-release.apk
```

### Emulating with DHU

1. On your phone: Open **Android Auto Settings** -> tap 3 dots in top right -> **Start head unit server**.
2. Run the included launcher batch script:

```cmd
start-dhu.bat
```

Connect Git

```
git config core.sshCommand "ssh -i ~/.ssh/github_ravi -o IdentitiesOnly=yes"

git remote set-url origin git@github.com:RedixKernal/redix-auto.git

git push -u origin main
```
