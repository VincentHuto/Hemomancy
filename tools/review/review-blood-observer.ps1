param([ValidatePattern('^[a-z0-9_-]+$')][string]$Label = 'casts')

$ErrorActionPreference = 'Stop'
$bloodRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$bloodProcesses = @(Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {
    $_.CommandLine -match 'net.neoforged.devlaunch.Main' -and $_.CommandLine.Contains($bloodRoot)
})
if ($bloodProcesses.Count -ne 1) { throw 'Start the primary blood multiplayer review first, with no other client from this checkout running.' }

Push-Location -LiteralPath $bloodRoot
try {
    # Prepare launch arguments only. Rebuilding a dependency JAR under a live client invalidates its class loader.
    & .\gradlew.bat prepareClientRun -I tools/review/animus-mortem-review.init.gradle -PbloodObserver "-PbloodReviewLabel=$Label" --max-workers=1 --no-daemon
    if ($LASTEXITCODE -ne 0) { throw 'Could not prepare the observer launch arguments.' }
    Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class BloodObserverLaunchArgs {
    [DllImport("shell32.dll", SetLastError=true)] public static extern IntPtr CommandLineToArgvW([MarshalAs(UnmanagedType.LPWStr)] string command, out int count);
    [DllImport("kernel32.dll")] public static extern IntPtr LocalFree(IntPtr memory);
}
'@
    $bloodCount = 0
    $bloodPointer = [BloodObserverLaunchArgs]::CommandLineToArgvW($bloodProcesses[0].CommandLine, [ref]$bloodCount)
    try {
        $bloodArguments = for ($bloodIndex = 0; $bloodIndex -lt $bloodCount; $bloodIndex++) {
            [Runtime.InteropServices.Marshal]::PtrToStringUni([Runtime.InteropServices.Marshal]::ReadIntPtr($bloodPointer, $bloodIndex * [IntPtr]::Size))
        }
    } finally { [void][BloodObserverLaunchArgs]::LocalFree($bloodPointer) }
    $bloodExecutable = $bloodArguments[0]
    $bloodLaunchArguments = $bloodArguments[1..($bloodArguments.Count - 1)]
    Set-Location -LiteralPath build/blood-observer-client
    & $bloodExecutable @bloodLaunchArguments
    exit $LASTEXITCODE
} finally { Pop-Location }
