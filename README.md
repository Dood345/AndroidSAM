# AndroidSAM - Text-to-Speech App

Integration of https://github.com/discordier/sam

AndroidSAM is an Android application that demonstrates Text-to-Speech (TTS) functionality using the "Software Automatic Mouth" (SAM) speech synthesizer, a JavaScript port of the original Commodore 64 SAM program. The app uses a WebView to run the SAM JavaScript engine and leverages a JavaScript interface to communicate between the Android native code (Kotlin) and the JavaScript environment.

## Features

*   **Text Input:** Users can enter text into an EditText field.
*   **Speech Synthesis:** Pressing a "Speak" button converts the input text into audible speech.
*   **SAM Engine:** Utilizes `samjs.js`, a JavaScript implementation of the SAM TTS engine.
*   **WebView Integration:** The JavaScript engine runs within a hidden WebView.
*   **JavaScript Interface:** A bridge (`SAMInterface`) allows Kotlin to invoke JavaScript functions and JavaScript to call back into Kotlin.
*   **UI Feedback:** A progress bar indicates when speech synthesis is in progress, and toasts provide feedback on speech completion or errors.
*   **Logging:** Includes logging for both Android and JavaScript sides for easier debugging.

## How it Works

1.  **Layout (`activity_main.xml`):** The main activity's layout consists of an `EditText` for text input, a `Button` to trigger speech, a `ProgressBar` for visual feedback, and a `WebView`.
2.  **`MainActivity.kt`:**
    *   Initializes the UI elements.
    *   Configures the `WebView` to enable JavaScript, DOM storage, and local file access.
    *   Loads `sam.html` from the app's assets folder into the WebView.
    *   Sets up a `SAMInterface` class as a JavaScript interface, allowing JavaScript code running in the WebView to call native Kotlin methods (e.g., `onSpeechComplete`, `logMessage`, `logError`).
    *   When the "Speak" button is clicked, it retrieves the text from the `EditText`, cleans it slightly, and then calls the `speakText()` JavaScript function within `sam.html` via `webView.evaluateJavascript(...)`.
3.  **`sam.html` (assets):**
    *   This HTML file includes the `samjs.js` library.
    *   It defines the `speakText(text, pitch, speed)` JavaScript function, which is called by `MainActivity`.
    *   Inside `speakText()`:
        *   It creates an instance of `SamJs`.
        *   It calls `sam.buf32(text)` to get the raw audio data (a `Float32Array`).
        *   It uses the Web Audio API (`AudioContext`, `createBuffer`, `createBufferSource`) to play this audio data. The sample rate is hardcoded to 22050 Hz to match SAM's output.
        *   It uses the `Android` JavaScript interface to log messages/errors and to call `Android.onSpeechComplete()` when the audio finishes playing.
4.  **`samjs.js` (assets):** This is the core JavaScript library for the Software Automatic Mouth, responsible for parsing text and generating the speech audio data.

## Project Structure (Key Files)

*   `app/src/main/java/com/example/androidsam/MainActivity.kt`: The main Android activity controlling the UI and WebView interaction.
*   `app/src/main/res/layout/activity_main.xml`: The XML layout file defining the user interface.
*   `app/src/main/assets/sam.html`: The HTML page that loads `samjs.js` and handles audio playback via the Web Audio API.
*   `app/src/main/assets/samjs.js`: The JavaScript SAM TTS engine.
*   `app/src/main/AndroidManifest.xml`: The Android application manifest, which includes the necessary `INTERNET` permission (though for local asset loading, it might not be strictly required, it's good practice for WebViews that *could* load remote content, and Web Audio API can sometimes be restricted without it).

## Setup and Build

1.  Clone this repository.
2.  Open the project in Android Studio.
3.  Ensure you have a compatible Android SDK installed.
4.  Build and run the application on an Android emulator or a physical device.

## Notes

*   The speech quality is characteristic of the original SAM synthesizer (robotic and retro).
*   Error handling and logging are implemented on both the Android (Kotlin) and JavaScript sides to aid in debugging.
