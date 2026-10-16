# 📝 Notes — Kotlin Android App



\


## 📥 Download the App

**[⬇️ Download Notes for Android — v1.0.0](https://github.com/csfahim007/note_taking-kotlin/releases/tag/v1.0.0)**

Visit the release page and download the attached APK or ZIP containing the APK. If you download a ZIP, extract it before installing the APK on a compatible Android device.

> This is a portfolio/demo release and is not published on the Google Play Store.

---

## 📱 Overview

**Notes** is a native Android note-taking and to-do application built with Kotlin and Jetpack Compose. It provides a simple way to create, organize, search, edit, and delete notes and checklists, with local storage for offline access.

The project demonstrates modern Android development practices, reactive UI state management, persistent local storage, and a maintainable application architecture.

## ✨ Features

- **📝 Create Notes & To-Dos:** Write notes or create interactive task checklists.
- **☑️ Interactive Checklists:** Add, check off, edit, or remove task items dynamically in To-Do mode.
- **🏷️ Category Filtering:** Easily filter your list by **All**, **Notes**, or **To-Dos**.
- **✔️ Quick Task Completion:** Mark notes or to-dos as completed directly from the list view.
- **✏️ Edit Notes & Tasks:** Update existing notes or checklist items whenever needed.
- **🗑️ Delete Notes:** Remove notes or task lists you no longer need.
- **↩️ Undo Deletion:** Restore a recently deleted note using the undo action.
- **🔍 Search:** Find notes and tasks by title or content.
- **📴 Offline Storage:** Store notes locally using the Room persistence library.
- **⚡ Reactive Updates:** Automatically reflect database changes in the UI using Kotlin Flow.
- **🎨 Modern UI:** Native Android interface built with Jetpack Compose and Material Design 3.

## 🛠️ Tech Stack

| Category                 | Technology                  |
| ------------------------ | --------------------------- |
| Language                 | Kotlin 2.0                  |
| UI Framework             | Jetpack Compose             |
| Design System            | Material Design 3           |
| Navigation               | Navigation Compose          |
| Architecture             | MVVM and Repository Pattern |
| Database                 | Room                        |
| Asynchronous Programming | Kotlin Coroutines and Flow  |
| Dependency Injection     | Hilt                        |
| Build System             | Gradle with Kotlin DSL      |

## 🏗️ Project Structure

```text
com.example.notes/
├── data/
│   ├── local/          # Room entity, DAO, and database
│   └── repository/     # Repository implementation
├── domain/
│   └── model/          # Note domain model
├── di/                 # Hilt dependency injection modules
└── ui/
    ├── list/           # Notes list screen and ViewModel
    ├── edit/           # Note editor screen and ViewModel
    ├── navigation/     # Navigation routes and NavHost
    ├── theme/          # Material 3 theme
    └── util/           # UI utilities
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- A compatible JDK for the project's Gradle configuration
- Android SDK with API 26 or higher available
- An Android emulator or compatible Android device

### Run Locally

1. Clone the repository:

   ```bash
   git clone https://github.com/csfahim007/note_taking-kotlin.git
   cd note_taking-kotlin
   ```

2. Open the project directory in Android Studio.

3. Wait for Gradle synchronization to finish.

4. Select an Android emulator or connected device.

5. Click **Run** in Android Studio.

### Build the APK

To generate a debug APK from the terminal:

```bash
./gradlew assembleDebug
```

The APK is typically generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Alternatively, use **Build → Build APK(s)** in Android Studio.

## 💾 Data Storage

Notes and to-dos are stored locally using Room, allowing users to access saved notes without relying on a remote backend. Room queries and Kotlin Flow support reactive updates when the underlying data changes.

## 👨‍💻 Developer

**Ahmed Fahim Kabir**

- GitHub: [@csfahim007](https://github.com/csfahim007)



## 📄 License

No license is specified here. Please add a `LICENSE` file to the repository if you intend to publish the project under an open-source license.

---

Built as a hands-on project to practice modern native Android development with Kotlin.