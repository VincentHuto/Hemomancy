param([ValidatePattern('^[a-z0-9_-]+$')][string]$Label = 'casts')

$ErrorActionPreference = 'Stop'
$ferricRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$ferricProcesses = @(Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {
    $_.CommandLine -match 'net.neoforged.devlaunch.Main' -and $_.CommandLine.Contains($ferricRoot)
})
if ($ferricProcesses.Count -ne 1) { throw 'Start the primary ferric multiplayer review first, with no other client from this checkout running.' }

Push-Location -LiteralPath $ferricRoot
try {
    # Prepare launch arguments only. Rebuilding a dependency JAR under a live client invalidates its class loader.
    & .\gradlew.bat prepareClientRun -I tools/review/ferric-ductilis-review.init.gradle -PferricObserver "-PferricReviewLabel=$Label" --max-workers=1 --no-daemon
    if ($LASTEXITCODE -ne 0) { throw 'Could not prepare the observer launch arguments.' }
    Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class FerricObserverLaunchArgs {
    [DllImport("shell32.dll", SetLastError=true)] public static extern IntPtr CommandLineToArgvW([MarshalAs(UnmanagedType.LPWStr)] string command, out int count);
    [DllImport("kernel32.dll")] public static extern IntPtr LocalFree(IntPtr memory);
}
'@
    $ferricCount = 0
    $ferricPointer = [FerricObserverLaunchArgs]::CommandLineToArgvW($ferricProcesses[0].CommandLine, [ref]$ferricCount)
    try {
        $ferricArguments = for ($ferricIndex = 0; $ferricIndex -lt $ferricCount; $ferricIndex++) {
            [Runtime.InteropServices.Marshal]::PtrToStringUni([Runtime.InteropServices.Marshal]::ReadIntPtr($ferricPointer, $ferricIndex * [IntPtr]::Size))
        }
    } finally { [void][FerricObserverLaunchArgs]::LocalFree($ferricPointer) }
    $ferricExecutable = $ferricArguments[0]
    $ferricLaunchArguments = $ferricArguments[1..($ferricArguments.Count - 1)]
    Set-Location -LiteralPath build/ferric-observer-client
    & $ferricExecutable @ferricLaunchArguments
    exit $LASTEXITCODE
} finally { Pop-Location }
