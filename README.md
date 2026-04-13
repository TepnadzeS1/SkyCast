# 🌦️ SkyCast Weather

A modern, glassmorphic weather application for Android built with Jetpack Compose. SkyCast provides a vivid, high-fidelity weather experience using real-time data and dynamic visuals.

## ✨ Key Features
- **Dynamic Video Backgrounds:** immersive backgrounds that adapt to 8+ specific weather conditions (Rain, Snow, Thunder, Windy, etc.) using ExoPlayer.
- **Interactive Radar Map:** A high-contrast precipitation radar with double-stacked layers for better visibility.
- **World Clock Integration:** Real-time local time tracking for any city globally based on UTC offsets.
- **Unit Switching:** Toggle between Celsius and Fahrenheit with persistent state.
- **Glassmorphic Design:** Modern Material 3 implementation with blurred surfaces and elegant typography.

## 🛠️ Technical Stack
- **UI:** Jetpack Compose
- **Networking:** Retrofit & KotlinX Serialization
- **Concurrency:** Kotlin Coroutines & Flow
- **Storage:** Room Database
- **Media:** Media3 ExoPlayer
- **Security:** Secrets Gradle Plugin for API Key obfuscation

## 🚀 Getting Started
To run this project locally, you will need to add your API keys:
1. Create a `local.properties` file in the root directory.
2. Add the following lines:
   ```properties
   WEATHER_API_KEY=059a36449705f13934c44e185f4efacb
   MAPS_API_KEY=AIzaSyDoRMX4wdDEAHRg_5lKH4bf5zpLa1go234
