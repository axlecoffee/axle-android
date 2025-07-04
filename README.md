# Axle Weather App

A modern Android weather application built with Kotlin that displays weather data from the Axle API.

## Features

- **Real-time Weather Data**: Fetches current weather conditions for Ottawa from `https://api.axle.coffee/api/weather`
- **Modern UI**: Clean Material Design interface with cards and proper spacing
- **24-Hour Forecast**: Horizontal scrolling list showing hourly weather predictions
- **Weather Widget**: Home screen widget displaying current conditions and API status
- **Pull-to-Refresh**: Swipe down to refresh weather data
- **Error Handling**: Robust error handling with user-friendly messages
- **API Status Indicator**: Shows connection status to the Axle Weather API

## Architecture

The app follows modern Android development best practices:

- **MVVM Architecture**: ViewModel manages data and business logic
- **Repository Pattern**: Centralized data management
- **Kotlin Coroutines**: Asynchronous operations
- **Retrofit**: Type-safe HTTP client for API calls
- **ViewBinding**: Safe view references
- **LiveData**: Reactive data observation

## Project Structure

```
app/src/main/java/coffee/axle/android/
├── data/
│   ├── api/                 # API service and client
│   ├── model/              # Data models for API responses
│   └── repository/         # Repository for data management
├── ui/
│   ├── activity/           # Main activity
│   ├── adapter/            # RecyclerView adapters
│   └── viewmodel/          # ViewModels
└── widget/                 # Home screen widget
```

## API Integration

The app integrates with the Axle Weather API:
- **Endpoint**: `https://api.axle.coffee/api/weather`
- **Authentication**: None required
- **Default Location**: Ottawa (no coordinates needed)
- **Data Sources**: Environment Canada + Open-Meteo

### API Response Structure

The API returns comprehensive weather data including:
- Current conditions (temperature, humidity, wind, pressure)
- 24-hour hourly forecast
- 7-day and 14-day forecasts
- Weather alerts
- Data source information

## Installation

1. **Build the project**:
   ```bash
   ./gradlew assembleDebug
   ```

2. **Install via ADB**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Or use the VS Code task**: Run "Build and Install Debug APK" from the task menu

## Widget Installation

1. Long-press on your home screen
2. Select "Widgets"
3. Find "Axle Weather" widget
4. Drag to your home screen
5. The widget will automatically load current weather data

## Development

### Dependencies

- **Kotlin**: Modern programming language for Android
- **AndroidX**: Latest Android support libraries
- **Material Design**: Modern UI components
- **Retrofit**: HTTP client for API calls
- **Gson**: JSON parsing
- **Coroutines**: Asynchronous programming

### Requirements

- Android SDK 21+ (Android 5.0+)
- Target SDK 34 (Android 14)
- Kotlin 1.9.22
- Gradle 8.5.2

### Network Permissions

The app requires internet access to fetch weather data:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Features Overview

### Main Activity
- Clean card-based layout
- Current weather display with temperature, condition, and details
- Horizontal scrolling 24-hour forecast
- Pull-to-refresh functionality
- Loading states and error handling

### Weather Widget
- Compact home screen widget
- Shows current temperature and condition
- API connection status
- Tap to open main app
- Automatic updates every 30 minutes

### Data Models
- Type-safe Kotlin data classes
- Gson annotations for JSON parsing
- Comprehensive coverage of API response structure

## Testing

The project includes:
- Unit tests for basic functionality
- Proper error handling testing
- Network response validation

Run tests with:
```bash
./gradlew test
```

## Building for Release

```bash
./gradlew assembleRelease
```

The release APK will be generated at `app/build/outputs/apk/release/app-release-unsigned.apk`

## License

This project is built for demonstration purposes using the Axle Weather API.
