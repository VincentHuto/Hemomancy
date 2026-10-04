param([switch]$Update)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$manifestPath = Join-Path $PSScriptRoot 'dependency-bundle.json'
$localJars = @(
    'create-1.21.1-6.0.10.jar', 'buildinggadgets2-1.3.9.jar', 'worldedit-mod-7.3.8.jar',
    'moonlight-neoforge-1.21.1-3.0.20.jar', 'theoneprobe-1.21_neo-12.0.8.jar',
    'dummmmmmy-1.21-2.0.12-neoforge.jar', 'jei-1.21.1-neoforge-19.27.0.340.jar',
    'mna-3.1.11-neoforge.1.21.1.0.jar', 'TerraBlender-neoforge-1.21.1-4.1.0.8.jar'
)
$libRoot = [IO.Path]::GetFullPath((Join-Path $repoRoot '../HutosLib'))
if (-not (Test-Path -LiteralPath (Join-Path $libRoot 'build.gradle'))) {
    throw 'Provision the pinned sibling HutosLib source snapshot described in dependency-bundle.json.'
}
$sourceFiles = & git -C $libRoot ls-files --cached --others --exclude-standard -- src build.gradle settings.gradle gradle.properties
if ($LASTEXITCODE -ne 0) { throw 'Could not inventory HutosLib source.' }
$sourceRows = foreach ($relative in ($sourceFiles | Sort-Object -Unique)) {
    $path = Join-Path $libRoot $relative
    if (Test-Path -LiteralPath $path -PathType Leaf) {
        $relative.Replace('\', '/') + ':' + (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash
    }
}
$sourceDigest = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData(
    [Text.Encoding]::UTF8.GetBytes(($sourceRows -join "`n"))))
$head = & git -C $libRoot rev-parse HEAD
if ($LASTEXITCODE -ne 0) { throw 'Could not read HutosLib revision.' }
$jars = foreach ($name in $localJars) {
    $relative = 'libs/' + $name
    $path = Join-Path $repoRoot $relative
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing provisioned dependency: $relative" }
    [ordered]@{ path = $relative; sha256 = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash }
}
$actual = [ordered]@{ hutosLib = [ordered]@{ revision = $head; sourceSha256 = $sourceDigest }; localJars = @($jars) }
if ($Update) {
    $actual | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $manifestPath -Encoding utf8
    Write-Output 'Recorded the current dependency input snapshot.'
    exit 0
}
$expected = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json
if ($expected.hutosLib.revision -ne $head -or $expected.hutosLib.sourceSha256 -ne $sourceDigest) {
    throw 'HutosLib differs from the pinned source snapshot; a matching version number alone is insufficient.'
}
foreach ($jar in $jars) {
    $pinned = @($expected.localJars | Where-Object { $_.path -eq $jar.path })
    if ($pinned.Count -ne 1 -or $pinned[0].sha256 -ne $jar.sha256) { throw "Dependency digest mismatch: $($jar.path)" }
}
if (@($expected.localJars).Count -ne $jars.Count) { throw 'Dependency manifest contains a stale inventory.' }
Write-Output "Verified $($jars.Count) local jars and the exact HutosLib source snapshot ($head)."
