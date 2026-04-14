$src = "C:\Users\Vince\Desktop\My MC Mods\CURRENT\Hemomancy\src\main\resources\assets\hemomancy\textures\item\scars\rune_halo.png"
$dst = "C:\Users\Vince\Desktop\My MC Mods\CURRENT\Hemomancy\src\main\resources\assets\hemomancy\textures\item\scars\scar_halo.png"
if (Test-Path $src) {
    Move-Item $src $dst -Force
    Write-Host "Renamed rune_halo.png to scar_halo.png"
} else {
    Write-Host "rune_halo.png not found (may already be renamed)"
}
