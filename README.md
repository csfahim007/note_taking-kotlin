# Notes

A native Android notes app built with Kotlin and Jetpack Compose.

- Create, edit, and delete notes
- Search by title or content
- Undo after deleting
- Offline: notes are stored on the device with Room

## Tech stack

| Area | Library |
| --- | --- |
| Language | Kotlin 2.0 |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Architecture | MVVM (ViewModel + UI state), repository pattern |
| Async | Coroutines and Flow |
| Storage | Room (queries return `Flow`, so the list updates automatically) |
| DI | Hilt |

## Structure

```
com.example.notes
├── data
│   ├── local          Room entity, DAO, database
│   └── repository     NoteRepository + implementation
├── domain.model       Note
├── di                 Hilt modules
└── ui
    ├── list           note list screen + ViewModel
    ├── edit           note editor screen + ViewModel
    ├── navigation     NavHost and routes
    ├── theme          Material 3 theme (dynamic color on Android 12+)
    └── util
```

## Run it

1. Open this folder in Android Studio (Ladybug or newer).
2. Wait for the Gradle sync to finish.
3. Pick an emulator or a connected phone (Android 8.0 / API 26 or higher) and press Run.

Or from a terminal: `./gradlew assembleDebug`
