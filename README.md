<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/74e7d7db-1161-4ec6-98b8-56c99e674ccf

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Metadata search uses public providers; no Gemini or Spotify API key is required. The existing Secrets plugin can use `.env.example` defaults.
5. Debug builds use the standard local Android debug keystore. Configure your own release signing separately.
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## Run on a physical phone from VS Code

1. On your Android phone, open **Settings > About phone** and tap **Build number** seven times to enable Developer options.
2. In **Developer options**, enable **USB debugging**.
3. Connect the phone to your computer with a USB data cable and accept the USB debugging prompt on the phone.
4. From the project root in the VS Code terminal, verify that the phone is detected:

	```bash
	adb devices
	```

	The device should appear with the status `device`. If it appears as `unauthorized`, unlock the phone and accept the prompt.

5. Install the debug build:

	```bash
	./gradlew :app:installDebug
	```

6. Launch the app on the phone:

	```bash
	adb shell monkey -p com.aistudio.musicplayer.vznrkq 1
	```

7. To view runtime logs while the app is running:

	```bash
	adb logcat
	```

If `adb` is not found, install Android SDK Platform Tools and add its `platform-tools` directory to your `PATH`.

## Library metadata and design update

Use **Songs → Web Search** beside **Rescan** for library identification and review. Single-song **Identify Using Link** and **Edit Metadata** remain separate actions. Changes are stored in the app's database, without rewriting audio files. Light lavender and dark plum themes follow the device appearance.

See [development and verification notes](docs/DEVELOPMENT.md) for matching rules, provider limits, build/test commands and the UI screenshot gallery. Android 36 Robolectric tests require Java 21+; `-PtestJavaHome=/path/to/runtime` can select a separate test JVM.
