# airplay-win

Portable AirPlay receiver for Windows 10/11 x64.

This project is a Windows-focused fork based on [serezhka/java-airplay](https://github.com/serezhka/java-airplay), packaged for personal use with a bundled Java runtime, bundled GStreamer runtime, and ready-to-run start/stop scripts.

## Features

- Wireless AirPlay receiver for Windows 10/11 x64
- iPhone / iPad screen mirroring
- Audio playback on the PC
- Portable package, unzip and run
- Visible console startup for easier troubleshooting
- Start and stop scripts included

## Download

Download the latest prebuilt package from:

[Latest Release](https://github.com/jensoncheung11/airplay-win/releases/latest)

Main asset:

- `ios-airplay-win-x64.zip`

## Quick start

1. Download `ios-airplay-win-x64.zip` from the latest release.
2. Extract the whole zip to a normal folder.
3. Double-click `Start AirPlay.cmd`.
4. If Windows asks about firewall access on first launch, allow private networks.
5. Make sure the iPhone / iPad and the PC are on the same local network.
6. On iPhone / iPad, open Control Center -> Screen Mirroring.
7. Select `srzhka`.

To fully stop the receiver, double-click `Stop AirPlay.cmd` or close the console window.

## Included scripts

- `Start AirPlay.cmd`  
  Starts the receiver in a visible console window.

- `Start AirPlay Debug.cmd`  
  Starts the receiver in a visible console window for debugging.

- `Stop AirPlay.cmd`  
  Stops the packaged receiver process.

## Notes

- The current default AirPlay device name is `srzhka`.
- Disconnecting screen mirroring does not fully exit the receiver process. Use `Stop AirPlay.cmd` if you want to close it completely.
- Some DRM-protected video content may stay black.
- For better stability, prefer 5 GHz Wi-Fi and keep the phone close to the router.

## Logs

The packaged build writes logs to:

`logs\receiver.log`

## Common issues

### Cannot find `srzhka`

- Make sure the app is already running.
- Make sure both devices are on the same LAN.
- Turn off VPN or network isolation features.
- If Windows Firewall prompts on first launch, allow private network access.

### Picture but no sound

- Disconnect and reconnect once.
- Confirm the current default Windows playback device can output sound.

### Sound but no picture

- Reconnect once.
- Some protected content may not render.

### App does not close after disconnecting

- This is expected behavior for standby mode.
- Run `Stop AirPlay.cmd` to stop it fully.

## Build from source

This repository includes a Windows packaging script that builds the app, runs tests, and creates a portable zip package.

From the project root:

```powershell
.\packaging\windows\package.ps1
```

Output:

- `dist\ios-airplay-win-x64\`
- `dist\ios-airplay-win-x64.zip`

The packaging script expects:

- a local JDK 17 runtime
- a local GStreamer runtime

The current default package target is Windows x64.

## Tech notes

- Player backend: GStreamer
- Default player mode: Swing window
- Default target resolution: 1920x1080
- Default target fps: 60

## Upstream

This project is based on:

- [serezhka/java-airplay](https://github.com/serezhka/java-airplay)

Thanks to the upstream project for the original AirPlay implementation.
