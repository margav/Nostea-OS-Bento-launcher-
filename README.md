# Nostea OS (Bento launcher)

A home-screen replacement for Android 5.0+ (API 21). The Bento interface lives in
`app/src/main/assets/index.html`; a small Kotlin shell (`MainActivity`, `BrowserActivity`)
gives it internet, a full in-app browser (YouTube included), phone/messages/camera/maps
shortcuts, and a drawer listing every installed app.

## Get an APK for your Samsung A40 (no Android Studio needed)

1. Create a new GitHub repository and upload everything in this folder (keep the `.github` folder).
2. Open the repo's **Actions** tab, run **Build APK**, wait a few minutes.
3. Download `nostea-os-debug-apk` from the finished run, unzip, copy `app-debug.apk` to the phone.
4. On the phone, open the APK and allow "install unknown apps" for your file manager.
5. Press Home, choose **Nostea OS**, then **Always**. (Settings > Apps > Default apps > Home app switches back.)

## Or build with Android Studio

Open this folder in Android Studio, let it sync, press Run with the phone connected over USB
(USB debugging on).

## Edit the interface

Change `app/src/main/assets/index.html`, rebuild. The same file opens in any phone browser
for quick design checks (native buttons such as Camera show a hint there).

## What works

- Lock screen (swipe up), home screen, dock, app drawer with installed apps
- YouTube tile and Browser tile open a full WebView browser (video, fullscreen, history, URL bar)
- Notes, Calculator, Clock (stopwatch, timer), Settings (theme, accent, 12/24-hour)
- Phone, Messages, Camera, Maps, Gallery, Music, Calendar open the phone's own apps

## Not included

This is an Android app, not a new kernel or Linux distribution. Compatibility depends on the
phone's Android System WebView being reasonably current (Play Store updates it on Android 5+).
