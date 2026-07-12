# iOS AirPlay Receiver Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a self-contained Windows 10/11 x64 AirPlay receiver that an iPhone 15 on iOS 26 or iPad mini on iPadOS 26 can discover and use for 1080p/30fps screen mirroring with computer audio.

**Architecture:** Fork the MIT-licensed `serezhka/java-airplay` source into this repository, preserve its protocol/server modules, and make GStreamer the single supported playback backend. Add a small Swing application around explicit service, playback, configuration, and UI-state interfaces; prove iOS 26 compatibility before completing the UI and portable ZIP.

**Tech Stack:** Java 17, Gradle 8, Netty, JmDNS, GStreamer 1.x with gst1-java-core, Swing, JUnit 5, PowerShell packaging scripts, `jlink`/`jpackage` tooling.

## Global Constraints

- Target only Windows 10 x64 and Windows 11 x64.
- Primary devices are iPhone 15 on iOS 26 and iPad mini on iPadOS 26.
- Accept one AirPlay sender at a time on the same local network; no password in v1.
- Offer 1920×1080 at 30fps and 1280×720 at 30fps, defaulting to 1920×1080 at 30fps.
- Decode H.264 video and ALAC/AAC-ELD audio through GStreamer.
- Do not add recording, remote control, WAN casting, accounts, ads, multi-device display, or automatic updates.
- Do not attempt to bypass DRM or HDCP.
- Store configuration and diagnostic logs beside the application; logs must not contain media payloads.
- The final ZIP must include the Java runtime and required GStreamer files and must not require a separate Java, GStreamer, or FFmpeg installation.

---

## Planned File Structure

Retain upstream `lib/`, `server/`, `client/`, and `player/gstreamer/`. Remove unsupported player modules only after the GStreamer path passes real-device validation.

- `settings.gradle`: included Gradle modules.
- `build.gradle`: shared Java/test versions and repositories.
- `LICENSE`: upstream MIT license.
- `THIRD_PARTY_NOTICES.md`: redistributed dependency notices.
- `player/app/build.gradle`: runnable app, test, and packaging dependencies.
- `player/app/src/main/java/com/github/serezhka/airplay/app/PlayerApp.java`: process entry point and lifecycle wiring.
- `player/app/src/main/java/com/github/serezhka/airplay/app/AppPaths.java`: portable config/log/runtime path resolution.
- `player/app/src/main/java/com/github/serezhka/airplay/app/config/AppSettings.java`: immutable validated settings.
- `player/app/src/main/java/com/github/serezhka/airplay/app/config/SettingsStore.java`: properties-file persistence.
- `player/app/src/main/java/com/github/serezhka/airplay/app/session/ReceiverState.java`: UI-visible state enum.
- `player/app/src/main/java/com/github/serezhka/airplay/app/session/ReceiverController.java`: service/player lifecycle and state transitions.
- `player/app/src/main/java/com/github/serezhka/airplay/app/ui/MainWindow.java`: waiting/player window and controls.
- `player/app/src/main/java/com/github/serezhka/airplay/app/ui/VideoSurface.java`: aspect-ratio-preserving GStreamer video surface.
- `player/gstreamer/src/main/java/com/github/serezhka/airplay/player/gstreamer/GstPlayer.java`: audio/video pipelines and lifecycle.
- `player/gstreamer/src/main/java/com/github/serezhka/airplay/player/gstreamer/GstPlayerUtils.java`: bundled GStreamer lookup.
- `server/src/main/java/com/github/serezhka/airplay/server/AirPlayServer.java`: server lifecycle and connection events.
- `server/src/main/java/com/github/serezhka/airplay/server/AirPlayConsumer.java`: media callback boundary.
- `server/src/test/resources/ios26/`: sanitized RTSP/HTTP metadata fixtures only, never captured media payloads.
- `packaging/windows/runtime-modules.txt`: Java modules passed to `jlink`.
- `packaging/windows/package.ps1`: produces the portable directory and ZIP.
- `packaging/windows/smoke-test.ps1`: validates a clean extracted package.
- `README.zh-CN.md`: Chinese usage and troubleshooting.

---

### Task 1: Import and Reproduce the Upstream Baseline

**Files:**
- Create from upstream: `build.gradle`, `settings.gradle`, `gradlew`, `gradlew.bat`, `gradle/wrapper/**`, `lib/**`, `server/**`, `client/**`, `player/**`, `LICENSE`, `README.md`
- Create: `UPSTREAM.md`

**Interfaces:**
- Consumes: upstream commit resolved from `https://github.com/serezhka/java-airplay.git` branch `main`.
- Produces: a reproducible multi-module Gradle build at repository root and an audit record in `UPSTREAM.md`.

- [ ] **Step 1: Record the exact upstream revision**

Run:

```powershell
git ls-remote https://github.com/serezhka/java-airplay.git refs/heads/main
```

Expected: one SHA followed by `refs/heads/main`. Save the SHA for the next steps.

- [ ] **Step 2: Import the source without overwriting the approved docs**

Run from a temporary directory:

```powershell
$upstreamSha = (git ls-remote https://github.com/serezhka/java-airplay.git refs/heads/main).Split("`t")[0]
git clone https://github.com/serezhka/java-airplay.git "$env:TEMP\java-airplay-upstream"
git -C "$env:TEMP\java-airplay-upstream" checkout $upstreamSha
Copy-Item "$env:TEMP\java-airplay-upstream\*" . -Recurse -Force -Exclude .git
```

Expected: Gradle modules exist at repository root and `docs/superpowers/**` remains intact.

- [ ] **Step 3: Create the provenance record**

Create `UPSTREAM.md` using the `$upstreamSha` value resolved above:

```markdown
# Upstream

This project is derived from [serezhka/java-airplay](https://github.com/serezhka/java-airplay).

- Upstream revision: `$upstreamSha`
- Upstream license: MIT (`LICENSE`)
- Local product scope: personal Windows 10/11 x64 AirPlay receiver
```

- [ ] **Step 4: Run the unmodified automated tests**

Run:

```powershell
.\gradlew.bat clean test
```

Expected: `BUILD SUCCESSFUL`. If upstream tests fail before local changes, save the complete output in the task notes and fix only environment/reproducibility issues before continuing.

- [ ] **Step 5: Commit the baseline**

```powershell
git add build.gradle settings.gradle gradlew gradlew.bat gradle lib server client player LICENSE README.md UPSTREAM.md
git commit -m "chore: import java-airplay baseline"
```

Expected: one baseline commit containing no product modifications.

---

### Task 2: Add Portable Paths and Validated Settings

**Files:**
- Create: `player/app/src/main/java/com/github/serezhka/airplay/app/AppPaths.java`
- Create: `player/app/src/main/java/com/github/serezhka/airplay/app/config/AppSettings.java`
- Create: `player/app/src/main/java/com/github/serezhka/airplay/app/config/SettingsStore.java`
- Create: `player/app/src/test/java/com/github/serezhka/airplay/app/config/SettingsStoreTest.java`

**Interfaces:**
- Consumes: system property `airplay.home`, falling back to the directory containing the application launcher.
- Produces: `AppPaths.home()`, `AppPaths.configFile()`, `AppPaths.logsDirectory()`; `AppSettings.defaults()`, `AppSettings.validated(...)`; `SettingsStore.load()` and `SettingsStore.save(AppSettings)`.

- [ ] **Step 1: Write failing settings tests**

Create `SettingsStoreTest.java` with tests equivalent to:

```java
@TempDir Path temp;

@Test void missingFileUsesDefaults() throws Exception {
    var store = new SettingsStore(temp.resolve("config.properties"));
    assertEquals(new AppSettings("iOS投屏", 1920, 1080, 30, 1.0), store.load());
}

@Test void savesAndLoadsSettings() throws Exception {
    var file = temp.resolve("config.properties");
    var store = new SettingsStore(file);
    var expected = new AppSettings("客厅电脑", 1280, 720, 30, 0.4);
    store.save(expected);
    assertEquals(expected, store.load());
}

@Test void invalidValuesFallBackToSupportedValues() throws Exception {
    Files.writeString(temp.resolve("config.properties"), """
        receiver.name=   
        video.width=999
        video.height=999
        video.fps=120
        audio.volume=4
        """);
    assertEquals(AppSettings.defaults(), new SettingsStore(temp.resolve("config.properties")).load());
}
```

- [ ] **Step 2: Verify the tests fail**

Run:

```powershell
.\gradlew.bat :player:app:test --tests "*SettingsStoreTest"
```

Expected: compilation fails because `AppSettings` and `SettingsStore` do not exist.

- [ ] **Step 3: Implement the minimum settings API**

Implement this public shape:

```java
public record AppSettings(String receiverName, int width, int height, int fps, double volume) {
    public static AppSettings defaults() { return new AppSettings("iOS投屏", 1920, 1080, 30, 1.0); }
    public static AppSettings validated(String name, int width, int height, int fps, double volume) {
        boolean supportedSize = (width == 1920 && height == 1080) || (width == 1280 && height == 720);
        if (name == null || name.isBlank() || !supportedSize || fps != 30 || volume < 0 || volume > 1) return defaults();
        return new AppSettings(name.trim(), width, height, fps, volume);
    }
}
```

`SettingsStore` must read/write exactly these keys: `receiver.name`, `video.width`, `video.height`, `video.fps`, and `audio.volume`. Write through a temporary sibling file and move it over the destination so an interrupted save cannot truncate a valid configuration.

Implement `AppPaths` so `-Dairplay.home=C:\path` is authoritative and creates `logs/` on demand; do not use the registry or user profile.

- [ ] **Step 4: Run the focused and full tests**

```powershell
.\gradlew.bat :player:app:test --tests "*SettingsStoreTest"
.\gradlew.bat test
```

Expected: both commands report `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add player/app/src/main player/app/src/test
git commit -m "feat: add portable receiver settings"
```

---

### Task 3: Expose Receiver Lifecycle and State Events

**Files:**
- Modify: `server/src/main/java/com/github/serezhka/airplay/server/AirPlayConsumer.java`
- Modify: `server/src/main/java/com/github/serezhka/airplay/server/AirPlayServer.java`
- Create: `player/app/src/main/java/com/github/serezhka/airplay/app/session/ReceiverState.java`
- Create: `player/app/src/main/java/com/github/serezhka/airplay/app/session/ReceiverController.java`
- Create: `player/app/src/test/java/com/github/serezhka/airplay/app/session/ReceiverControllerTest.java`

**Interfaces:**
- Consumes: `AirPlayServer.start()`, `AirPlayServer.stop()`, `AirPlayConsumer` media callbacks, and `AppSettings`.
- Produces: `ReceiverState { STOPPED, WAITING, CONNECTING, PLAYING, RECONNECTING, ERROR }`; `ReceiverController.start()`, `stop()`, `disconnect()`, `addStateListener(Consumer<ReceiverState>)`, and `state()`.

- [ ] **Step 1: Write failing controller transition tests**

Use fake `ReceiverService` and `PlaybackService` test doubles and verify:

```java
@Test void startMovesFromStoppedToWaiting() {
    controller.start();
    assertEquals(ReceiverState.WAITING, controller.state());
    assertEquals(1, receiver.startCalls);
}

@Test void mediaFormatMovesToPlaying() {
    controller.start();
    controller.onConnecting();
    controller.onVideoFormat(videoInfo);
    assertEquals(ReceiverState.PLAYING, controller.state());
}

@Test void protocolErrorReturnsToWaitingAfterCleanup() {
    controller.start();
    controller.onError(new IOException("handshake"));
    assertEquals(ReceiverState.WAITING, controller.state());
    assertEquals(1, playback.resetCalls);
}
```

Define package-local interfaces inside the session package:

```java
interface ReceiverService { void start(); void stop(); void disconnect(); }
interface PlaybackService { void reset(); }
```

- [ ] **Step 2: Verify failure**

```powershell
.\gradlew.bat :player:app:test --tests "*ReceiverControllerTest"
```

Expected: compilation fails because the session types do not exist.

- [ ] **Step 3: Implement serialized state transitions**

Implement `ReceiverController` with one single-thread executor named `receiver-state`. All public lifecycle and protocol-event methods enqueue transitions on that executor; listener callbacks receive states in order. Reject a second sender by calling `ReceiverService.disconnect()` while already `CONNECTING` or `PLAYING`.

Add default no-op connection/error methods to `AirPlayConsumer` so protocol code can report lifecycle without breaking existing consumers:

```java
default void onSessionConnecting() {}
default void onSessionError(Throwable error) {}
```

Wire actual handler entry/error points in `AirPlayServer` to these callbacks. Do not infer `PLAYING` from a TCP connection alone; enter it only after a valid audio or video format callback.

- [ ] **Step 4: Run transition and regression tests**

```powershell
.\gradlew.bat :player:app:test --tests "*ReceiverControllerTest"
.\gradlew.bat :server:test :player:app:test
```

Expected: `BUILD SUCCESSFUL`; existing server fixture tests remain green.

- [ ] **Step 5: Commit**

```powershell
git add server/src player/app/src
git commit -m "feat: expose receiver session states"
```

---

### Task 4: Make Bundled GStreamer the Supported Playback Backend

**Files:**
- Modify: `player/gstreamer/src/main/java/com/github/serezhka/airplay/player/gstreamer/GstPlayerUtils.java`
- Modify: `player/gstreamer/src/main/java/com/github/serezhka/airplay/player/gstreamer/GstPlayer.java`
- Create: `player/gstreamer/src/main/java/com/github/serezhka/airplay/player/gstreamer/GstRuntime.java`
- Create: `player/gstreamer/src/test/java/com/github/serezhka/airplay/player/gstreamer/GstRuntimeTest.java`
- Create: `player/gstreamer/src/test/java/com/github/serezhka/airplay/player/gstreamer/GstPipelineSpecTest.java`

**Interfaces:**
- Consumes: `airplay.home`, bundled directory `runtime/gstreamer/1.0/msvc_x86_64`, and `AudioStreamInfo.CompressionType`.
- Produces: `GstRuntime.resolve(Path appHome)`, `GstRuntime.configureEnvironment(Path gstRoot)`, `GstPlayer.setVolume(double)`, and idempotent `GstPlayer.reset()`.

- [ ] **Step 1: Write failing runtime and pipeline tests**

Assert that `GstRuntime.resolve(home)` requires `bin/gstreamer-1.0-0.dll` and `lib/gstreamer-1.0`, and that it throws an exception whose message begins `Bundled GStreamer is incomplete:` when either is missing.

Add pipeline string tests for these exact functional elements:

```text
appsrc name=h264-src is-live=true format=time
h264parse
avdec_h264
videoconvert
appsrc name=alac-src is-live=true format=time
avdec_alac
appsrc name=aac-eld-src is-live=true format=time
avdec_aac
audioconvert
audioresample
wasapisink
```

- [ ] **Step 2: Verify failure**

```powershell
.\gradlew.bat :player:gstreamer:test --tests "*GstRuntimeTest" --tests "*GstPipelineSpecTest"
```

Expected: tests fail because `GstRuntime` and testable pipeline factories do not exist.

- [ ] **Step 3: Implement bundled runtime resolution and safe pipelines**

Resolve only the bundled runtime by default. Permit `-Dairplay.gstreamer=<absolute-path>` as a developer override, but never silently fall back to a machine-wide installation in the packaged application.

Before `Gst.init`, set `jna.library.path`, prepend `<gstRoot>/bin` to `PATH`, set `GST_PLUGIN_PATH` to `<gstRoot>/lib/gstreamer-1.0`, and set `GST_REGISTRY` to `<appHome>/cache/gstreamer-registry.bin`.

Fix the AAC caps typo from `channnels` to `channels`. Replace `sync=false` audio output with a Windows `wasapisink sync=true` pipeline and use timestamps/durations on buffers. Make `reset()` stop and flush all pipelines and be safe when called twice. `setVolume` must clamp to `0.0..1.0` and update a named `volume` element in both audio pipelines.

- [ ] **Step 4: Run GStreamer unit tests**

```powershell
.\gradlew.bat :player:gstreamer:test
```

Expected: `BUILD SUCCESSFUL`. These tests validate configuration and pipeline construction without requiring a physical iPhone.

- [ ] **Step 5: Commit**

```powershell
git add player/gstreamer/src
git commit -m "feat: support bundled gstreamer playback"
```

---

### Task 5: Establish the iOS 26 Compatibility Gate

**Files:**
- Create: `docs/testing/ios26-compatibility.md`
- Create only when needed: `server/src/test/resources/ios26/*.txt`, `server/src/test/resources/ios26/*.plist`
- Modify only when a reproduced incompatibility requires it: `lib/src/main/java/com/github/serezhka/airplay/lib/**`, `server/src/main/java/com/github/serezhka/airplay/server/**`
- Add matching regression tests under: `lib/src/test/java/**` or `server/src/test/java/**`

**Interfaces:**
- Consumes: the headless receiver, structured protocol-stage logs, iPhone 15/iOS 26 and iPad mini/iPadOS 26.
- Produces: a pass/fail compatibility record and a regression fixture/test for every protocol change.

- [ ] **Step 1: Build and start the headless GStreamer receiver**

```powershell
.\gradlew.bat :player:app:bootJar
java -Dairplay.home="$PWD\dev-home" -jar .\player\app\build\libs\java-airplay-server-1.0.7.jar
```

Expected: the service publishes its receiver name and writes protocol-stage logs without media bytes.

- [ ] **Step 2: Run the real-device discovery matrix**

Record in `docs/testing/ios26-compatibility.md`:

```markdown
| Host | Sender | Discovered | Connected | Video | Audio | Reconnect | Result |
|---|---|---:|---:|---:|---:|---:|---|
| Windows 11 x64 | iPhone 15 / iOS 26 |  |  |  |  |  |  |
| Windows 11 x64 | iPad mini / iPadOS 26 |  |  |  |  |  |  |
```

Expected gate: both devices are discovered. Do not begin the polished UI if discovery or pairing fails.

- [ ] **Step 3: Reproduce each protocol failure with metadata-only fixtures**

For any failure, capture only headers, plist metadata, message types, status codes, and lengths. Redact device identifiers and do not save encrypted or decrypted audio/video payloads. Write a focused failing JUnit test that feeds the sanitized request into the existing handler/decoder and asserts the required response or state.

- [ ] **Step 4: Implement the smallest compatibility fix**

Change only the failing parser, response field, cryptographic negotiation, or session transition demonstrated by the test. Do not rewrite the protocol stack. Run the focused test first, followed by:

```powershell
.\gradlew.bat :lib:test :server:test
```

Expected: focused regression passes and all upstream fixtures remain green.

- [ ] **Step 5: Validate audio and video for 10 minutes before UI work**

On each device, mirror the system UI, rotate portrait/landscape, play a local non-DRM video with sound, disconnect, and reconnect. Record exact results in the compatibility table.

Expected gate: both devices connect; at least one produces stable video and audio for 10 minutes. If neither device passes, stop product work and report the protocol blocker instead of building a misleading UI.

- [ ] **Step 6: Commit the verified compatibility work**

```powershell
git add lib server docs/testing/ios26-compatibility.md
git commit -m "fix: support ios 26 airplay sessions"
```

Expected: commit includes evidence and regression tests for every protocol modification.

---

### Task 6: Build the Single-Window Swing UI

**Files:**
- Create: `player/app/src/main/java/com/github/serezhka/airplay/app/ui/MainWindow.java`
- Create: `player/app/src/main/java/com/github/serezhka/airplay/app/ui/VideoSurface.java`
- Create: `player/app/src/main/java/com/github/serezhka/airplay/app/ui/ReceiverViewModel.java`
- Create: `player/app/src/test/java/com/github/serezhka/airplay/app/ui/ReceiverViewModelTest.java`
- Modify: `player/app/src/main/java/com/github/serezhka/airplay/app/PlayerApp.java`

**Interfaces:**
- Consumes: `ReceiverController`, `ReceiverState`, `SettingsStore`, GStreamer video component, and `GstPlayer.setVolume/reset`.
- Produces: waiting and playback screens; commands `toggleFullscreen()`, `setAlwaysOnTop(boolean)`, `setVolume(double)`, `setQuality(Quality)`, `saveReceiverName(String)`, and `disconnect()`.

- [ ] **Step 1: Write failing view-model tests**

Test exact mappings:

```java
assertEquals("等待 iPhone 或 iPad 连接", vm.titleFor(WAITING));
assertEquals("正在连接…", vm.titleFor(CONNECTING));
assertEquals("投屏中", vm.titleFor(PLAYING));
assertEquals("网络波动，正在恢复…", vm.titleFor(RECONNECTING));
assertEquals("连接出现问题，已返回等待状态", vm.titleFor(ERROR));
```

Verify `saveReceiverName("  客厅电脑  ")` persists `客厅电脑`, blank names are rejected without changing settings, volume stays between 0 and 1, and quality offers only `FULL_HD(1920,1080,30)` and `HD(1280,720,30)`.

- [ ] **Step 2: Verify failure**

```powershell
.\gradlew.bat :player:app:test --tests "*ReceiverViewModelTest"
```

Expected: compilation fails because UI/view-model types do not exist.

- [ ] **Step 3: Implement the view model and minimal window**

Use a `CardLayout` with `WAITING_CARD` and `PLAYER_CARD`. Create all Swing components on the EDT. Marshal controller listener updates with `SwingUtilities.invokeLater`. Preserve aspect ratio in `VideoSurface`; unused space is black. Controls are exactly: full screen, always-on-top, volume slider, 1080p/720p selector, receiver-name setting, disconnect, and open-log-directory.

Do not add custom window chrome or animations. `PlayerApp` must construct paths/settings/player/controller/window, start the controller after the window is visible, and stop/reset resources during shutdown.

- [ ] **Step 4: Run UI tests and a local smoke launch**

```powershell
.\gradlew.bat :player:app:test
.\gradlew.bat :player:app:bootRun --args="--spring.main.banner-mode=off"
```

Expected: tests pass; a window shows the waiting copy, receiver name, and connection instructions without requiring a sender.

- [ ] **Step 5: Repeat real-device playback through the UI**

Expected: iPhone and iPad connections switch to the playback card; portrait/landscape preserves aspect ratio; full screen, topmost, volume and disconnect work; a second connection is rejected while the first is active.

- [ ] **Step 6: Commit**

```powershell
git add player/app/src
git commit -m "feat: add receiver desktop window"
```

---

### Task 7: Add Privacy-Safe Diagnostics and Firewall Guidance

**Files:**
- Modify: `player/app/src/main/resources/logback.xml`
- Create: `player/app/src/main/java/com/github/serezhka/airplay/app/diagnostics/NetworkDiagnostics.java`
- Create: `player/app/src/test/java/com/github/serezhka/airplay/app/diagnostics/NetworkDiagnosticsTest.java`
- Modify: `player/app/src/main/java/com/github/serezhka/airplay/app/ui/ReceiverViewModel.java`

**Interfaces:**
- Consumes: current network interfaces, server port bindings, and `AppPaths.logsDirectory()`.
- Produces: bounded rotating log files and `NetworkDiagnostics.Result` containing actionable Chinese messages.

- [ ] **Step 1: Write failing diagnostics tests**

Cover these deterministic inputs through an injected `NetworkSnapshot`:

```java
assertEquals("未发现可用的局域网连接", diagnose(noUpInterfaces).message());
assertEquals("检测到 VPN，若设备无法发现电脑，请允许 VPN 访问本地网络", diagnose(vpnPresent).message());
assertEquals("局域网连接正常", diagnose(privateLan).message());
```

Add a log-content test that sends a synthetic media byte array through logging hooks and asserts the hex/base64/content never appears in the captured log.

- [ ] **Step 2: Verify failure**

```powershell
.\gradlew.bat :player:app:test --tests "*NetworkDiagnosticsTest"
```

Expected: compilation fails because diagnostics types do not exist.

- [ ] **Step 3: Implement diagnostics and rotating logs**

Configure logs at `<airplay.home>/logs/receiver.log`, rotate daily or at 10 MB, retain five files, and log only timestamps, protocol stages, formats, dimensions, codec names, state transitions, exception types/messages, and port numbers. Do not log request bodies or media buffers.

On Windows firewall or bind failures, show a message explaining that Windows may request permission and that the user should allow access on private networks. Do not silently create elevated firewall rules.

- [ ] **Step 4: Run tests**

```powershell
.\gradlew.bat :player:app:test
```

Expected: `BUILD SUCCESSFUL`, including the media-payload non-disclosure assertion.

- [ ] **Step 5: Commit**

```powershell
git add player/app/src
git commit -m "feat: add receiver diagnostics"
```

---

### Task 8: Produce the Self-Contained Windows x64 ZIP

**Files:**
- Modify: `player/app/build.gradle`
- Create: `packaging/windows/runtime-modules.txt`
- Create: `packaging/windows/package.ps1`
- Create: `packaging/windows/smoke-test.ps1`
- Create: `THIRD_PARTY_NOTICES.md`
- Create: `README.zh-CN.md`

**Interfaces:**
- Consumes: application JAR, Java 17 `jlink`, pinned GStreamer MSVC x86_64 distribution, licenses, and app resources.
- Produces: `dist/iOS投屏-win-x64/iOS投屏.exe` (or `iOS投屏.cmd` only if an `.exe` launcher cannot be produced reproducibly) and `dist/iOS投屏-win-x64.zip`.

- [ ] **Step 1: Write the failing package smoke test**

`smoke-test.ps1` must extract the ZIP to a new temporary directory and assert:

```powershell
$required = @(
  'app',
  'runtime\java\bin\javaw.exe',
  'runtime\gstreamer\1.0\msvc_x86_64\bin\gstreamer-1.0-0.dll',
  'LICENSE',
  'THIRD_PARTY_NOTICES.md',
  'README.zh-CN.md'
)
```

It must launch with `-Dairplay.smokeTest=true`, wait up to 20 seconds for `logs/receiver.log` to contain `Receiver ready`, then stop the process and fail if any separately installed Java or GStreamer path appears in the log.

- [ ] **Step 2: Verify smoke test fails before packaging**

```powershell
.\packaging\windows\smoke-test.ps1 -ZipPath .\dist\iOS投屏-win-x64.zip
```

Expected: failure because the ZIP does not exist.

- [ ] **Step 3: Implement deterministic packaging**

Pin the GStreamer archive URL and SHA-256 inside `package.ps1`; download to `build-cache/`, verify the hash before extraction, and fail on mismatch. Use `jlink` with modules listed in `runtime-modules.txt`:

```text
java.base
java.desktop
java.logging
java.management
java.naming
java.net.http
java.security.jgss
java.xml
jdk.crypto.ec
jdk.unsupported
```

Build the boot JAR, create the Java runtime, copy only required GStreamer runtime/plugins after confirming ALAC, AAC, H.264, WASAPI, appsrc, converters and parsers pass `gst-inspect-1.0`, add licenses/docs, create a launcher that sets `airplay.home` to its own directory, and produce the ZIP with a stable root directory.

- [ ] **Step 4: Document use and troubleshooting**

`README.zh-CN.md` must contain only the shipped workflow: unzip, double-click, approve private-network firewall access, ensure both devices use the same LAN, open Control Center → Screen Mirroring, select the receiver, and use the controls. Include separate remedies for not discovered, picture without sound, sound without picture, stutter, VPN, and DRM black screen.

- [ ] **Step 5: Build and smoke-test the ZIP**

```powershell
.\packaging\windows\package.ps1
.\packaging\windows\smoke-test.ps1 -ZipPath .\dist\iOS投屏-win-x64.zip
```

Expected: package script prints the ZIP path and SHA-256; smoke test prints `PASS: portable package started with bundled runtime`.

- [ ] **Step 6: Commit**

```powershell
git add player/app/build.gradle packaging THIRD_PARTY_NOTICES.md README.zh-CN.md
git commit -m "build: package portable windows receiver"
```

---

### Task 9: Complete the Acceptance Matrix

**Files:**
- Modify: `docs/testing/ios26-compatibility.md`
- Create: `docs/testing/windows-portability.md`

**Interfaces:**
- Consumes: release-candidate ZIP and two clean Windows test environments.
- Produces: signed-off evidence for every design acceptance criterion.

- [ ] **Step 1: Run all automated verification from a clean checkout**

```powershell
.\gradlew.bat clean test
.\packaging\windows\package.ps1
.\packaging\windows\smoke-test.ps1 -ZipPath .\dist\iOS投屏-win-x64.zip
```

Expected: all three commands succeed.

- [ ] **Step 2: Run the 30-minute iPhone acceptance session**

On iPhone 15/iOS 26: connect, show system UI, play local non-DRM video with sound, rotate twice, toggle fullscreen/topmost, adjust volume, switch 1080p→720p→1080p, disconnect, and reconnect. Continue for 30 minutes.

Expected: no crash; picture and computer audio continue; no visibly increasing A/V drift; controls work; reconnect succeeds.

- [ ] **Step 3: Run the 30-minute iPad acceptance session**

Repeat the exact Step 2 sequence on iPad mini/iPadOS 26.

Expected: same result as the iPhone session.

- [ ] **Step 4: Validate Windows 10 and Windows 11 portability**

On one Windows 10 x64 and one Windows 11 x64 environment without separately installed Java/GStreamer, extract the ZIP to a writable folder and complete discovery, connection, video, audio, disconnect and relaunch. Record OS build, package SHA-256 and result in `windows-portability.md`.

- [ ] **Step 5: Record limitations without weakening failures**

List DRM black-screen behavior as an expected limitation. Any failure in discovery, non-DRM audio/video, 30-minute stability, or clean-PC startup remains a release blocker; do not relabel it as a known limitation.

- [ ] **Step 6: Commit acceptance evidence**

```powershell
git add docs/testing
git commit -m "test: record receiver acceptance results"
```

Expected: documentation identifies tested device/OS combinations and the exact ZIP hash.
