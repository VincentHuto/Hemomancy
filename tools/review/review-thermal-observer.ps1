param([ValidatePattern('^[a-z0-9_-]+$')][string]$Label = 'casts')

$ErrorActionPreference = 'Stop'
$thermalRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$thermalProcesses = @(Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object {
    $_.CommandLine -match 'net.neoforged.devlaunch.Main' -and $_.CommandLine.Contains($thermalRoot)
})
if ($thermalProcesses.Count -ne 1) { throw 'Start the primary thermal multiplayer review first, with no other client from this checkout running.' }

Push-Location -LiteralPath $thermalRoot
try {
    # Prepare launch arguments only. Rebuilding a dependency JAR under a live client invalidates its class loader.
    & .\gradlew.bat prepareClientRun -I tools/review/manipulation-visceral-review.init.gradle -PthermalReview -PthermalObserver "-PthermalObserverLabel=$Label" --max-workers=1 --no-daemon
    if ($LASTEXITCODE -ne 0) { throw 'Could not prepare the observer launch arguments.' }
    Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class ThermalObserverLaunchArgs {
    [DllImport("shell32.dll", SetLastError=true)] public static extern IntPtr CommandLineToArgvW([MarshalAs(UnmanagedType.LPWStr)] string command, out int count);
    [DllImport("kernel32.dll")] public static extern IntPtr LocalFree(IntPtr memory);
}
'@
    $thermalCount = 0
    $thermalPointer = [ThermalObserverLaunchArgs]::CommandLineToArgvW($thermalProcesses[0].CommandLine, [ref]$thermalCount)
    try {
        $thermalArguments = for ($thermalIndex = 0; $thermalIndex -lt $thermalCount; $thermalIndex++) {
            [Runtime.InteropServices.Marshal]::PtrToStringUni([Runtime.InteropServices.Marshal]::ReadIntPtr($thermalPointer, $thermalIndex * [IntPtr]::Size))
        }
    } finally { [void][ThermalObserverLaunchArgs]::LocalFree($thermalPointer) }
    $thermalExecutable = $thermalArguments[0]
    $thermalLaunchArguments = $thermalArguments[1..($thermalArguments.Count - 1)]
    Set-Location -LiteralPath build/thermal-observer-client
    & $thermalExecutable @thermalLaunchArguments
    exit $LASTEXITCODE
} finally { Pop-Location }
