# Willow - Health Tracking Application

Willow is a modern Android application that tracks sleep and exercise data with Android Health Connect integration.

## Features

- **Exercise Tracking**: Record and view exercise sessions
- **Sleep Tracking**: Monitor and analyze sleep data
- **Data History**: View detailed sleep and exercise history
- **Health Connect Integration**: Synchronization with Google Health Connect
- **Conflict Management**: Manual conflict detection and resolution
- **Modern UI**: User-friendly interface built with Jetpack Compose

## Technology Stack

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern Android UI toolkit
- **Room Database** - Local data storage
- **Health Connect** - Health data integration
- **Dagger Hilt** - Dependency injection
- **Navigation Compose** - Screen navigation
- **Coroutines** - Asynchronous operations

## Requirements

- Android API 28+ (Android 9.0)
- Health Connect app installed
- Sleep and exercise permissions

## Installation

1. Clone the project:
```bash
git clone [repository-url]
cd willow
```

2. Open in Android Studio

3. Install dependencies:
```bash
./gradlew build
```

4. Run the application

## Permissions

The application requires the following Health Connect permissions:

- `READ_SLEEP` - Read sleep data
- `WRITE_SLEEP` - Write sleep data
- `READ_EXERCISE` - Read exercise data
- `WRITE_EXERCISE` - Write exercise data

## Project Structure

```
app/src/main/java/com/marki/willow/
├── data/
│   ├── dao/           # Room DAOs
│   ├── database/      # Database configuration
│   ├── entity/        # Data models
│   ├── health/        # Health Connect management
│   └── repository/    # Data layer
├── di/                # Dependency injection
├── navigation/        # Navigation configuration
├── ui/
│   ├── components/    # Reusable components
│   ├── screens/       # Application screens
│   ├── theme/         # UI theme
│   └── viewmodel/     # ViewModels
└── MainActivity.kt
```

## Screens

- **Home**: Overview and quick access
- **Sleep Log**: New sleep data entry
- **Sleep History**: View past sleep data
- **Exercise Log**: New exercise session entry
- **Exercise History**: View past exercise data
- **Conflicts**: Manual conflict detection and resolution
- **Settings**: Application configuration

## Screenshots

<table>
  <tr>
    <td><img src="screenshots/01.jpg" width="200"/></td>
    <td><img src="screenshots/02.jpg" width="200"/></td>
    <td><img src="screenshots/03.jpg" width="200"/></td>
  </tr>
  <tr>
    <td><img src="screenshots/04.jpg" width="200"/></td>
    <td><img src="screenshots/05.jpg" width="200"/></td>
    <td><img src="screenshots/06.jpg" width="200"/></td>
  </tr>
  <tr>
    <td><img src="screenshots/07.jpg" width="200"/></td>
    <td></td>
    <td></td>
  </tr>
</table>

## License

This project is licensed under the MIT License.

## Contact

Project Owner: markiyurtdas