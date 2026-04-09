# AuraLink — WiFi Chat App

A beautiful, glassmorphism-themed Android chat app that lets anyone on the **same WiFi network** chat with each other — no internet required.

---

## ✨ Features

- **Zero-internet chat** — works entirely over your local WiFi network
- **Auto-discovery** — finds other AuraLink users automatically using mDNS/NSD
- **Real-time messaging** — instant TCP socket-based communication
- **Glassmorphism UI** — deep purple/navy gradient with frosted-glass cards
- **Modern design** — clean typography, gradient message bubbles, online indicators
- **3 elegant screens** — Welcome → Device Discovery → Chat

---

## 📱 Screenshots

| Home | Discovery | Chat |
|------|-----------|------|
| Enter your name on a beautiful gradient screen with glass card | See nearby devices on the same WiFi with live search | Chat with purple gradient sent bubbles & glass received bubbles |

---

## 🏗️ Architecture

```
wifi-chat-app/
├── app/src/main/
│   ├── java/com/auralink/app/
│   │   ├── MainActivity.kt          ← Username entry
│   │   ├── DiscoveryActivity.kt     ← Device discovery
│   │   ├── ChatActivity.kt          ← Chat screen
│   │   ├── network/
│   │   │   ├── NsdHelper.kt         ← mDNS/NSD service registration & discovery
│   │   │   ├── ChatServer.kt        ← TCP server (listens for incoming messages)
│   │   │   └── ChatClient.kt        ← TCP client (sends messages)
│   │   ├── model/Message.kt         ← Message data class
│   │   └── adapter/
│   │       ├── DeviceAdapter.kt     ← Device list RecyclerView adapter
│   │       └── MessageAdapter.kt    ← Message list RecyclerView adapter
│   └── res/
│       ├── layout/                  ← Activity & item layouts
│       ├── drawable/                ← Gradient, glass, bubble drawables
│       └── values/                  ← Colors, strings, themes, dimens
```

### How it works

1. **Registration** — When you enter your name, the app registers an mDNS service `AuraLink_<yourname>` on port 9001 using Android's NSD (Network Service Discovery).
2. **Discovery** — NSD scans the local network and resolves peer services to get their IP + port.
3. **Chat** — Tapping a device connects a TCP socket to port 9001 on their device. Messages are JSON-encoded (`{"sender":"Alice","content":"Hi!","timestamp":1234567890}`), newline-delimited.

---

## 🚀 Build & Run

### Requirements
- **Android Studio** Hedgehog (2023.1.1) or newer
- **Android SDK** API 26+ (Android 8.0+)
- **JDK 8+**
- Two or more Android devices on the **same WiFi network**

### Steps

1. Open Android Studio
2. Choose **File → Open** and select the `wifi-chat-app/` folder
3. Let Gradle sync finish
4. Click **Run ▶** to build and install on your device

### Build APK from command line

```bash
cd wifi-chat-app
# First time: generate gradle wrapper
gradle wrapper

# Build debug APK
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎨 Design System

| Token | Value | Use |
|-------|-------|-----|
| `bg_deep` | `#0B0018` | Page background start |
| `bg_mid` | `#0D1B4B` | Page background end |
| `accent_violet` | `#7C3AED` | Buttons, sent bubbles, avatars |
| `accent_indigo` | `#4F46E5` | Gradient end colour |
| `glass_bg` | `#14FFFFFF` | Frosted-glass card fill |
| `glass_border` | `#28FFFFFF` | Frosted-glass card stroke |
| `text_primary` | `#FFFFFF` | Main text |
| `text_secondary` | `#94A3B8` | Subtext, timestamps |
| `status_online` | `#10B981` | Online indicator |

**Glassmorphism** is achieved via semi-transparent white fills (`#14FFFFFF`) + subtle white stroke (`#28FFFFFF`) layered on the dark gradient background, giving a frosted-glass depth effect.

---

## 📋 Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
```

---

## 🔧 Requirements

- `minSdk 26` (Android 8.0 Oreo)
- `targetSdk 34` (Android 14)
- Kotlin 1.9.22
- Android Gradle Plugin 8.2.2
