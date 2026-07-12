[CmdletBinding()]
param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path,
    [string]$JdkRoot = "C:\codex-worktrees\portable-airplay\build-cache\jdk17\jdk-17.0.19+10",
    [string]$SourceGStreamerRoot = "C:\codex-worktrees\portable-airplay\dev-home\runtime\gstreamer\1.0\msvc_x86_64",
    [string]$PackageName = "ios-airplay-win-x64"
)

$ErrorActionPreference = "Stop"

function Reset-Directory {
    param([string]$Path)
    if (Test-Path $Path) {
        Remove-Item -LiteralPath $Path -Recurse -Force
    }
    New-Item -ItemType Directory -Path $Path | Out-Null
}

$projectRootPath = (Resolve-Path $ProjectRoot).Path
$distRoot = Join-Path $projectRootPath "dist"
$packageRoot = Join-Path $distRoot $PackageName
$javaRuntimeTarget = Join-Path $packageRoot "runtime\java"
$gstRuntimeTarget = Join-Path $packageRoot "runtime\gstreamer\1.0\msvc_x86_64"
$jarPath = Join-Path $projectRootPath "player\app\build\libs\java-airplay-server-1.0.7.jar"
$zipPath = Join-Path $distRoot ($PackageName + ".zip")

if (-not (Test-Path $JdkRoot)) { throw "JDK not found: $JdkRoot" }
if (-not (Test-Path $SourceGStreamerRoot)) { throw "GStreamer runtime not found: $SourceGStreamerRoot" }

$env:JAVA_HOME = $JdkRoot
$env:PATH = (Join-Path $JdkRoot "bin") + ";" + $env:PATH

Push-Location $projectRootPath
try {
    & .\gradlew :player:app:bootJar :player:app:test :player:gstreamer:test :server:test :lib:test
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed." }
}
finally {
    Pop-Location
}

if (-not (Test-Path $jarPath)) { throw "Application jar not found: $jarPath" }

Reset-Directory $distRoot
New-Item -ItemType Directory -Path $packageRoot | Out-Null
New-Item -ItemType Directory -Path (Join-Path $packageRoot "app") | Out-Null
New-Item -ItemType Directory -Path (Join-Path $packageRoot "logs") | Out-Null
New-Item -ItemType Directory -Path (Join-Path $packageRoot "cache") | Out-Null
New-Item -ItemType Directory -Path (Join-Path $packageRoot "runtime\gstreamer\1.0") -Force | Out-Null

Copy-Item -LiteralPath $jarPath -Destination (Join-Path $packageRoot "app\java-airplay-server-1.0.7.jar")
Copy-Item -LiteralPath (Join-Path $projectRootPath "LICENSE") -Destination (Join-Path $packageRoot "LICENSE")
Copy-Item -LiteralPath (Join-Path $projectRootPath "README.md") -Destination (Join-Path $packageRoot "UPSTREAM_README.md")

Copy-Item -LiteralPath $JdkRoot -Destination $javaRuntimeTarget -Recurse
Copy-Item -LiteralPath $SourceGStreamerRoot -Destination $gstRuntimeTarget -Recurse

$launcher = @'
@echo off
setlocal
set "APP_HOME=%~dp0"
if "%APP_HOME:~-1%"=="\" set "APP_HOME=%APP_HOME:~0,-1%"
pushd "%APP_HOME%"
"%APP_HOME%\runtime\java\bin\java.exe" -Djava.net.preferIPv4Stack=true -Dairplay.home="%APP_HOME%" -jar "%APP_HOME%\app\java-airplay-server-1.0.7.jar"
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%
'@
Set-Content -LiteralPath (Join-Path $packageRoot "Start AirPlay.cmd") -Value $launcher -Encoding ASCII

$debugLauncher = @'
@echo off
setlocal
set "APP_HOME=%~dp0"
if "%APP_HOME:~-1%"=="\" set "APP_HOME=%APP_HOME:~0,-1%"
pushd "%APP_HOME%"
"%APP_HOME%\runtime\java\bin\java.exe" -Djava.net.preferIPv4Stack=true -Dairplay.home="%APP_HOME%" -jar "%APP_HOME%\app\java-airplay-server-1.0.7.jar"
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%
'@
Set-Content -LiteralPath (Join-Path $packageRoot "Start AirPlay Debug.cmd") -Value $debugLauncher -Encoding ASCII

$stopLauncher = @'
@echo off
setlocal
set "APP_HOME=%~dp0"
if "%APP_HOME:~-1%"=="\" set "APP_HOME=%APP_HOME:~0,-1%"
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$appHome = [System.IO.Path]::GetFullPath('%APP_HOME%');" ^
  "$pidPath = Join-Path $appHome 'airplay.pid';" ^
  "$stopped = $false;" ^
  "if (Test-Path $pidPath) { $pidValue = (Get-Content $pidPath -Raw).Trim(); if ($pidValue) { $process = Get-Process -Id $pidValue -ErrorAction SilentlyContinue; if ($process) { Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue; $stopped = $true } }; Remove-Item $pidPath -Force -ErrorAction SilentlyContinue }" ^
  "if (-not $stopped) { $targets = Get-CimInstance Win32_Process | Where-Object { ($_.Name -eq 'java.exe' -or $_.Name -eq 'javaw.exe') -and $_.CommandLine -and $_.CommandLine -match 'java-airplay-server-1\.0\.7\.jar' -and $_.CommandLine -match [regex]::Escape($appHome) }; $targets | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue } }"
exit /b %ERRORLEVEL%
'@
Set-Content -LiteralPath (Join-Path $packageRoot "Stop AirPlay.cmd") -Value $stopLauncher -Encoding ASCII

$readme = @'
# iPhone / iPad screen mirroring to Windows

How to use:

1. Extract the whole zip to a normal folder.
2. Double-click `Start AirPlay.cmd`.
3. A visible console window will stay open while the receiver is running. Do not close it during use.
4. To fully exit the receiver, double-click `Stop AirPlay.cmd` or close the console window.
5. If Windows asks about firewall access on first launch, allow private networks.
6. Keep the iPhone / iPad and the PC on the same local network.
7. Open Control Center on the iPhone / iPad, tap Screen Mirroring, and choose `srzhka`.

Common issues:

- Cannot find srzhka
  Make sure the app is already running, both devices are on the same LAN, and VPN is off.

- Want to close the app completely
  Disconnecting screen mirroring only returns the receiver to standby. Use `Stop AirPlay.cmd` to exit it fully.

- Picture but no sound
  Disconnect and reconnect once, then confirm the current default Windows playback device can output sound.

- Sound but no picture
  Reconnect once. Some protected content may stay black.

- Stutter
  Prefer 5GHz Wi-Fi and keep the phone close to the router.

- Need logs or startup details
  Run `Start AirPlay Debug.cmd`. Log file: `logs\receiver.log`
'@
Set-Content -LiteralPath (Join-Path $packageRoot "README.txt") -Value $readme -Encoding UTF8

if (Test-Path $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}
Compress-Archive -Path $packageRoot -DestinationPath $zipPath -Force

$hash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash
Write-Host "ZIP: $zipPath"
Write-Host "SHA256: $hash"
