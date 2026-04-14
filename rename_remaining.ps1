Set-Location "C:\Users\Vince\Desktop\My MC Mods\CURRENT\Hemomancy"

# 1. Rename texture files
$texDir = "src\main\resources\assets\hemomancy\textures"
Get-ChildItem -Path $texDir -Recurse -File | Where-Object { $_.Name -match '^rune_' } | ForEach-Object {
    $newName = $_.Name -replace '^rune_', 'scar_'
    Rename-Item $_.FullName $newName
    Write-Host "Renamed texture: $($_.Name) -> $newName"
}

# 2. Rename generated resource files
$genDir = "src\generated\resources"
Get-ChildItem -Path $genDir -Recurse -File | Where-Object { $_.Name -match '^rune_' } | ForEach-Object {
    $content = Get-Content -Path $_.FullName -Raw
    $content = $content -replace 'hemomancy:item/rune_', 'hemomancy:item/scar_'
    $content = $content -replace 'hemomancy:block/rune_', 'hemomancy:block/scar_'
    $content = $content -replace 'hemomancy:rune_', 'hemomancy:scar_'
    Set-Content -Path $_.FullName -Value $content -NoNewline

    $newName = $_.Name -replace '^rune_', 'scar_'
    Rename-Item $_.FullName $newName
    Write-Host "Renamed generated: $($_.Name) -> $newName"
}

# 3. Rename scar_station generated files
Get-ChildItem -Path $genDir -Recurse -File | Where-Object { $_.Name -match 'scar_station' } | ForEach-Object {
    $content = Get-Content -Path $_.FullName -Raw
    $content = $content -replace 'scar_station', 'scar_station'
    Set-Content -Path $_.FullName -Value $content -NoNewline

    $newName = $_.Name -replace 'scar_station', 'scar_station'
    Rename-Item $_.FullName $newName
    Write-Host "Renamed chisel: $($_.Name) -> $newName"
}

# 4. Check for rune_eye.png, rune_feral.png in textures (may have been missed)
Get-ChildItem -Path $texDir -Recurse -File | Where-Object { $_.Name -match 'rune' } | ForEach-Object {
    Write-Host "STILL HAS RUNE: $($_.FullName)"
}

Write-Host "Done!"
