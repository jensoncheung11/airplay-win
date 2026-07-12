# airplay-win

[![License](https://img.shields.io/github/license/jensoncheung11/airplay-win)](./LICENSE)

[简体中文说明](./README.zh-CN.md)

Portable AirPlay receiver for Windows 10/11 x64.

This project is a Windows-focused fork based on [serezhka/java-airplay](https://github.com/serezhka/java-airplay), packaged for personal use with a bundled Java runtime, bundled GStreamer runtime, and ready-to-run start/stop scripts.

## Features

- Wireless AirPlay receiver for Windows 10/11 x64
- iPhone / iPad screen mirroring
- Audio playback on the PC
- Portable package, unzip and run
- Bundled Java runtime
- Bundled GStreamer runtime
- Start, debug, and stop scripts included

## Download

Download the latest prebuilt package from:

- GitHub Releases: [https://github.com/jensoncheung11/airplay-win/releases/latest](https://github.com/jensoncheung11/airplay-win/releases/latest)
- Gitee Releases: [https://gitee.com/jensoncheung1/airplay-win/releases](https://gitee.com/jensoncheung1/airplay-win/releases)

Main asset:

- `ios-airplay-win-x64.zip`

## Quick start

1. Download `ios-airplay-win-x64.zip`
2. Extract the whole zip to a normal folder
3. Double-click `Start AirPlay.cmd`
4. If Windows Firewall prompts on first launch, allow private network access
5. Make sure the iPhone / iPad and the PC are on the same local network
6. Open Control Center on iPhone / iPad
7. Tap Screen Mirroring
8. Select `srzhka`

To fully stop the receiver, double-click `Stop AirPlay.cmd` or close the console window.

## Included scripts

- `Start AirPlay.cmd`
  - Normal startup

- `Start AirPlay Debug.cmd`
  - Startup for troubleshooting and visible output

- `Stop AirPlay.cmd`
  - Fully stops the packaged receiver process

## Logs

Runtime logs are written to:

```text
logs\receiver.log
```

## Common issues

### Cannot find `srzhka`

Check the following:

- The app is already running
- The phone and PC are on the same LAN
- VPN, proxy, or network isolation is turned off
- Windows Firewall has allowed private network access

### Picture but no sound

Try:

- Disconnecting and reconnecting once
- Confirming the current default Windows playback device works normally

### Sound but no picture

Try:

- Reconnecting once
- Note that some DRM-protected content may not render correctly

### The app does not exit after disconnecting mirroring

This is expected behavior.
Disconnecting mirroring only returns the receiver to standby mode.
Use `Stop AirPlay.cmd` if you want to close it completely.

### Mirroring is laggy or unstable

Recommendations:

- Prefer 5 GHz Wi-Fi
- Keep the phone close to the router
- Keep the LAN stable
- Avoid heavy concurrent network transfers

## Build from source

This repository includes a Windows packaging script that builds the app and creates a portable zip package.

From the project root:

```powershell
.\packaging\windows\package.ps1
```

Output:

```text
dist\ios-airplay-win-x64\
dist\ios-airplay-win-x64.zip
```

## Technical notes

- Default player backend: GStreamer
- Default display mode: Swing window
- Default device name: `srzhka`
- Default target resolution: 1920x1080
- Default target frame rate: 60fps

## Upstream

This project is based on:

- [serezhka/java-airplay](https://github.com/serezhka/java-airplay)

Thanks to the upstream project for the original AirPlay implementation.
