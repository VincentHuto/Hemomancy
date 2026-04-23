$tileDir = "C:\Users\Vince\Desktop\My MC Mods\NeoForge\NeoForge Attempts\1.21.1\Hemomancy\src\main\java\com\vincenthuto\hemomancy\common\tile"

Get-ChildItem -Path $tileDir -Filter "*.java" -Recurse | ForEach-Object {
    $file = $_.FullName
    $lines = Get-Content $file
    $hlImport = "import net.minecraft.core.HolderLookup;"

    $hlCount = ($lines | Where-Object { $_ -eq $hlImport }).Count

    if ($hlCount -gt 1) {
        # Remove all HolderLookup import lines
        $noHL = $lines | Where-Object { $_ -ne $hlImport }

        # Find where to insert it (before first 'import net.minecraft.' line)
        $insertIdx = -1
        for ($i = 0; $i -lt $noHL.Count; $i++) {
            if ($noHL[$i] -match '^import net\.minecraft\.') {
                $insertIdx = $i
                break
            }
        }

        if ($insertIdx -ge 0) {
            $result = @()
            $result += $noHL[0..($insertIdx-1)]
            $result += $hlImport
            $result += $noHL[$insertIdx..($noHL.Count-1)]
        } else {
            $result = $noHL
        }

        Set-Content -Path $file -Value $result
        Write-Host "Fixed: $($_.Name) (had $hlCount imports)"
    }
}
Write-Host "Done."

