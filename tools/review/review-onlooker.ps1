# Core-loop round two P11: how another player sees a caster (gametest OnlookerClientReview).
#
# One Gradle call prepares both runs and exits (--no-daemon); then the host and the observer start as bare java
# processes, so no Gradle is alive while the two clients run. The host opens a disposable world (an operator op file
# dropped after its startup line), publishes it on port 25581 and logs ONLOOKER_SERVER_READY; the observer then joins
# and grabs a frame each time the host tells it to. Screenshots land in build/onlooker-observer/screenshots (what the
# onlooker sees) and build/onlooker-host/screenshots (unused unless F2 is pressed).
#
#   pwsh -NoProfile -File tools/review/review-onlooker.ps1 [-Only <scene prefix>] [-Java <path to java.exe>]
param(
    [ValidatePattern('^[a-z0-9-]*$')][string]$Only = '',
    [string]$Java = ''
)

$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$hostDir = Join-Path $root 'build\onlooker-host'
$observerDir = Join-Path $root 'build\onlooker-observer'
$moddev = Join-Path $root 'build\moddev'

function Stop-Onlookers {
    Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {
        $_.CommandLine -and ($_.CommandLine -match 'onlooker-host|onlooker-observer|OnlookHost|OnlookWatch')
    } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
}

function Find-Java {
    if ($Java) { return $Java }
    $toolchain = Get-ChildItem -Path (Join-Path $env:USERPROFILE '.gradle\jdks') -Filter 'java.exe' -Recurse -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match 'adoptium-21' -and $_.FullName -match '\\bin\\java\.exe$' } | Select-Object -First 1
    if ($toolchain) { return $toolchain.FullName }
    if (Test-Path 'C:\Program Files\Java\jdk-21\bin\java.exe') { return 'C:\Program Files\Java\jdk-21\bin\java.exe' }
    return 'java'
}

function Start-Client([string]$run, [string]$dir) {
    New-Item -ItemType Directory -Force $dir | Out-Null
    # A first launch shows the accessibility onboarding screen, which would hold up the observer's quick-play join.
    $options = Join-Path $dir 'options.txt'
    if (-not (Test-Path $options)) { Set-Content -Path $options -Value 'onboardAccessibility:false' -Encoding ascii }
    # Start-Process joins its arguments with spaces, so each argument file (the path has spaces) is quoted here.
    $arguments = @("`"@$moddev\${run}RunVmArgs.txt`"", "`"@$moddev\${run}RunClasspath.txt`"", 'net.neoforged.devlaunch.Main',
        "`"@$moddev\${run}RunProgramArgs.txt`"")
    $out = Join-Path $dir 'onlooker-stdout.log'
    return Start-Process -FilePath $script:javaExe -ArgumentList $arguments -WorkingDirectory $dir -PassThru `
        -RedirectStandardOutput $out -RedirectStandardError (Join-Path $dir 'onlooker-stderr.log')
}

function Wait-For([scriptblock]$condition, [int]$seconds, [string]$what) {
    $deadline = (Get-Date).AddSeconds($seconds)
    while (-not (& $condition)) {
        if ((Get-Date) -gt $deadline) { throw "Timed out waiting for $what" }
        Start-Sleep -Seconds 2
    }
}

# 1. Nothing from an earlier run is still holding the game directories.
Stop-Onlookers

Push-Location -LiteralPath $root
try {
    # 2. The one Gradle call: compile, write both runs' argument files and classpaths, then exit.
    & .\gradlew.bat compileGameTestJava prepareOnlookerReviewClientRun prepareOnlookerObserverClientRun `
        writeOnlookerReviewClientClasspath writeOnlookerObserverClientClasspath "-PonlookerOnly=$Only" --max-workers=1 --no-daemon
    if ($LASTEXITCODE -ne 0) { throw 'Could not prepare the onlooker runs.' }
    $script:javaExe = Find-Java

    # 3. The host.
    $journal = Join-Path $hostDir 'operator\journal.jsonl'
    $startups = if (Test-Path $journal) { @(Select-String -Path $journal -Pattern '"kind":"startup"').Count } else { 0 }
    Remove-Item -Recurse -Force (Join-Path $observerDir 'screenshots') -ErrorAction SilentlyContinue
    $hostProcess = Start-Client 'onlookerReviewClient' $hostDir

    # 4. Its startup line, then one op file: open the save if it exists, else create it.
    Wait-For { (Test-Path $journal) -and @(Select-String -Path $journal -Pattern '"kind":"startup"').Count -gt $startups } 300 'the host to start'
    $inbox = Join-Path $hostDir 'operator\inbox'
    New-Item -ItemType Directory -Force $inbox | Out-Null
    $op = if (Test-Path (Join-Path $hostDir 'saves\Onlooker')) { '{"op":"open","name":"Onlooker"}' } else { '{"op":"create","name":"Onlooker","seed":"1"}' }
    Set-Content -Path (Join-Path $inbox 'onlooker.json') -Value $op -Encoding ascii

    # 5. The published world, then the observer.
    $hostLog = Join-Path $hostDir 'logs\latest.log'
    Wait-For { (Test-Path $hostLog) -and (Select-String -Path $hostLog -Pattern 'ONLOOKER_SERVER_READY' -Quiet) } 300 'the host to publish its world'
    $observerProcess = Start-Client 'onlookerObserverClient' $observerDir

    # 6. Both exit; print what each said and the observer's frames.
    $hostProcess.WaitForExit()
    if (-not $observerProcess.WaitForExit(120000)) { Stop-Process -Id $observerProcess.Id -Force }
    Select-String -Path $hostLog -Pattern 'ONLOOKER_REVIEW' | ForEach-Object { $_.Line }
    $observerLog = Join-Path $observerDir 'logs\latest.log'
    if (Test-Path $observerLog) { Select-String -Path $observerLog -Pattern 'ONLOOKER_REVIEW' | ForEach-Object { $_.Line } }
    Get-ChildItem (Join-Path $observerDir 'screenshots') -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
    Get-ChildItem (Join-Path $hostDir 'screenshots') -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
} finally {
    Pop-Location
}
