[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ZipPath
)

$ErrorActionPreference = "Stop"

$zipResolved = (Resolve-Path $ZipPath).Path
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("portable-airplay-smoke-" + [guid]::NewGuid().ToString("N"))
$extractRoot = Join-Path $tempRoot "extract"
New-Item -ItemType Directory -Path $extractRoot -Force | Out-Null

try {
    Expand-Archive -LiteralPath $zipResolved -DestinationPath $extractRoot -Force
    $packageDir = Get-ChildItem -LiteralPath $extractRoot -Directory | Select-Object -First 1
    if (-not $packageDir) {
        throw "No package directory found after extraction."
    }

    $javaExe = Join-Path $packageDir.FullName "runtime\java\bin\java.exe"
    $jarPath = Join-Path $packageDir.FullName "app\java-airplay-server-1.0.7.jar"
    $logPath = Join-Path $packageDir.FullName "logs\receiver.log"
    $startScript = Join-Path $packageDir.FullName "Start AirPlay.cmd"
    $stopScript = Join-Path $packageDir.FullName "Stop AirPlay.cmd"

    if (-not (Test-Path $javaExe)) { throw "Missing bundled java: $javaExe" }
    if (-not (Test-Path $jarPath)) { throw "Missing app jar: $jarPath" }
    if (-not (Test-Path $startScript)) { throw "Missing start script: $startScript" }
    if (-not (Test-Path $stopScript)) { throw "Missing stop script: $stopScript" }

    Start-Process -FilePath $startScript -WorkingDirectory $packageDir.FullName | Out-Null

    $deadline = (Get-Date).AddSeconds(20)
    do {
        Start-Sleep -Milliseconds 500
        $process = Get-CimInstance Win32_Process | Where-Object {
            ($_.Name -eq 'java.exe' -or $_.Name -eq 'javaw.exe') -and
            $_.CommandLine -and
            $_.CommandLine -match 'java-airplay-server-1\.0\.7\.jar' -and
            $_.CommandLine -match [regex]::Escape($packageDir.FullName)
        } | Select-Object -First 1
        if (Test-Path $logPath) {
            $content = Get-Content $logPath -Raw -ErrorAction SilentlyContinue
            if ($content -match "Started PlayerApp") {
                break
            }
        }
    } while ((Get-Date) -lt $deadline -and -not $process)

    if (-not $process) {
        throw "Timed out waiting for packaged app startup log."
    }

    Start-Process -FilePath $stopScript -WorkingDirectory $packageDir.FullName -Wait -WindowStyle Hidden
    Start-Sleep -Seconds 2

    $remaining = Get-CimInstance Win32_Process | Where-Object {
        ($_.Name -eq 'java.exe' -or $_.Name -eq 'javaw.exe') -and
        $_.CommandLine -and
        $_.CommandLine -match 'java-airplay-server-1\.0\.7\.jar' -and
        $_.CommandLine -match [regex]::Escape($packageDir.FullName)
    }

    if ($remaining) {
        throw "Stop script did not terminate packaged app."
    }

    Write-Host "PASS: portable package started with bundled runtime and stop script exited it cleanly"
}
finally {
    Get-Process java,javaw -ErrorAction SilentlyContinue |
        Where-Object { $_.Path -like "$tempRoot*" } |
        Stop-Process -Force -ErrorAction SilentlyContinue

    if (Test-Path $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}
