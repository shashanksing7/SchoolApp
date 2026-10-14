# School App

Android app (Kotlin + Jetpack Compose) that:

1. Loads schools from Firestore collection **`schools`**, shows only docs with
   `status == "show"`, sorted by `priority` ascending, in a 2-column grid with
   a search bar on top.
2. Tapping a school asks the user to choose **Student/Parent** or **Staff**.
3. Next screen asks for a **roll number**, then builds the final login URL
   from the school's `studentURL` / `staffURL` and opens it in a WebView.
4. If that page loads successfully, the app saves `{url, schoolDocId,
   schoolName}` to **DataStore**. On the next app launch it reads DataStore
   first — if a session is saved, it skips straight to that WebView instead
   of showing the school grid again.
5. Once a page loads successfully (whether fresh login or restored from
   DataStore) a **bottom nav bar** appears with two tabs:
   - **Home** — reloads the saved URL in the WebView.
   - **Settings** — contact support (pulled from the Firestore `support`
     collection: `email`, `phone`, `website`, `message` fields, whichever
     exist) and a **Logout** button that clears DataStore and returns to the
     school grid.

## Setup

1. Open this folder in Android Studio (Giraffe/Koala or newer). The Gradle
   wrapper binary (`gradlew`/`gradle-wrapper.jar`) isn't included since it
   can't be generated in this environment — Android Studio will offer to
   create it automatically the first time you open the project (or run
   `gradle wrapper` yourself if you have Gradle installed).
2. In the [Firebase console](https://console.firebase.google.com), add an
   Android app with package name `com.example.schoolapp`, download
   **`google-services.json`**, and place it at `app/google-services.json`
   (a placeholder is provided at `app/google-services.json.example` — rename
   your real file to replace it).
3. Make sure **Cloud Firestore** is enabled on that Firebase project and that
   its security rules allow the app to read the `schools` and `support`
   collections (read-only rules are enough since the app never writes to
   Firestore).
4. Gradle sync, then Run.

## One assumption worth checking

The task said the roll number gets "added at the end of the url." I
implemented that as a query parameter:

```
https://srdav.mmtschool.com/site/userlogin?rollNumber=12345
```

That logic lives in one place — `buildFinalUrl()` in
`RollNumberScreen.kt` — so if your backend actually expects it as a path
segment (`/userlogin/12345`) or a different query key, that's the only
function you need to change.

## Project structure

```
app/src/main/java/com/example/schoolapp/
├── MainActivity.kt
├── SchoolApp.kt                 (Application, inits Firebase)
├── data/
│   ├── School.kt                 (model + UserRole enum)
│   ├── SchoolRepository.kt       (Firestore reads for "schools")
│   ├── SupportRepository.kt      (Firestore reads for "support")
│   └── UserPrefsRepository.kt    (DataStore - saved session)
├── navigation/
│   ├── Routes.kt
│   └── AppNavigation.kt
└── ui/
    ├── screens/
    │   ├── SplashScreen.kt        (checks DataStore, routes accordingly)
    │   ├── SchoolListScreen.kt    (grid + search)
    │   ├── RoleSelectionScreen.kt
    │   ├── RollNumberScreen.kt    (builds final URL)
    │   ├── WebViewScreen.kt       (WebView + bottom nav on success)
    │   └── SettingsScreen.kt      (support contact + logout)
    └── theme/
        ├── Color.kt
        └── Theme.kt
```

## Notes / things you may want to adjust

- The launcher icon is a plain placeholder vector — regenerate a real one
  with Android Studio's *New > Image Asset*.
- `WebViewScreen` treats any main-frame load error as a failure and shows a
  Retry button; it doesn't yet distinguish "wrong roll number" (which your
  site itself would report, e.g. an error message rendered inside the page)
  from a network failure. If you want the app to detect a login rejection
  and send the user back to re-enter their roll number, that would need a
  JavaScript bridge or a URL-pattern check specific to your ERP's login
  flow — happy to add that if you can share what an invalid login look like
  (URL redirect, page text, etc).
- Minimum SDK is 24 (Android 7.0); bump `minSdk` in `app/build.gradle.kts`
  if you need to support older devices, or raise it if you don't.
