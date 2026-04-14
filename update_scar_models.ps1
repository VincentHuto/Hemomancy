$modelDir = "C:\Users\Vince\Desktop\My MC Mods\CURRENT\Hemomancy\src\main\resources\assets\hemomancy\models\item"

$scarItems = @(
    "scar_sol", "scar_heart", "scar_descendence", "scar_moon", "scar_eye", "scar_feral",
    "scar_thorn", "scar_shade", "scar_pyre", "scar_marrow", "scar_blight", "scar_rime",
    "scar_flux", "scar_halo", "scar_anvil", "scar_veil", "scar_phoenix", "scar_ichor",
    "scar_wither", "scar_glacier", "scar_chimera", "scar_corona", "scar_crucible",
    "scar_oblivion", "scar_transcendence"
)

foreach ($name in $scarItems) {
    $filePath = Join-Path $modelDir "$name.json"
    if (Test-Path $filePath) {
        $newContent = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "hemomancy:item/scars/scar_blank",
    "layer1": "hemomancy:item/scars/$name"
  }
}
"@
        Set-Content -Path $filePath -Value $newContent -NoNewline
        Write-Host "Updated: $name.json"
    } else {
        Write-Host "MISSING: $filePath"
    }
}

Write-Host "Done! Updated $($scarItems.Count) scar item models."
