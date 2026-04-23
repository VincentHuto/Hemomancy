$tileDir = "C:\Users\Vince\Desktop\My MC Mods\NeoForge\NeoForge Attempts\1.21.1\Hemomancy\src\main\java\com\vincenthuto\hemomancy\common\tile"

Get-ChildItem -Path $tileDir -Filter "*.java" -Recurse | ForEach-Object {
    $file = $_.FullName
    $content = Get-Content $file -Raw

    # Count occurrences of the HolderLookup import
    $count = ([regex]::Matches($content, 'import net\.minecraft\.core\.HolderLookup;')).Count

    if ($count -gt 1) {
        # Remove all occurrences first
        $content = $content -replace 'import net\.minecraft\.core\.HolderLookup;\r?\n', ''
        # Add it back once, before the first 'import net.minecraft.' line
        $content = $content -replace '(import net\.minecraft\.)', "import net.minecraft.core.HolderLookup;`r`nimport net.minecraft."
        Set-Content -Path $file -Value $content -NoNewline
        Write-Host "Fixed duplicate imports in: $($_.Name)"
    }
}
Write-Host "Done."

