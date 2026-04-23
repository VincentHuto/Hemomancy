$tileDir = "C:\Users\Vince\Desktop\My MC Mods\NeoForge\NeoForge Attempts\1.21.1\Hemomancy\src\main\java\com\vincenthuto\hemomancy\common\tile"

Get-ChildItem -Path $tileDir -Filter "*.java" -Recurse | ForEach-Object {
    $file = $_.FullName
    $lines = Get-Content $file
    $modified = $false

    $newLines = $lines | ForEach-Object {
        $line = $_

        # Replace 'void load(CompoundTag X, HolderLookup.Provider' with 'void loadAdditional'
        if ($line -match 'void load\(CompoundTag (\w+), HolderLookup\.Provider') {
            $line = $line -replace 'void load\(CompoundTag', 'void loadAdditional(CompoundTag'
            $modified = $true
        }
        # Replace 'super.load(X, registries)' with 'super.loadAdditional(X, registries)'
        elseif ($line -match 'super\.load\(\w+, registries\)') {
            $line = $line -replace 'super\.load\(', 'super.loadAdditional('
            $modified = $true
        }
        $line
    }

    if ($modified) {
        Set-Content -Path $file -Value $newLines
        Write-Host "Updated: $($_.Name)"
    }
}
Write-Host "Done."

