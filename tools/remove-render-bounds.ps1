$srcDir = "C:\Users\Vince\Desktop\My MC Mods\NeoForge\NeoForge Attempts\1.21.1\Hemomancy\src\main\java\com\vincenthuto\hemomancy\common\tile"

Get-ChildItem -Path $srcDir -Filter "*.java" -Recurse | ForEach-Object {
    $file = $_.FullName
    $lines = Get-Content $file

    if (-not ($lines | Select-String -Pattern "getRenderBoundingBox" -SimpleMatch)) {
        return
    }

    # Find and remove the @Override + getRenderBoundingBox method block
    $newLines = [System.Collections.Generic.List[string]]::new()
    $i = 0
    $modified = $false

    while ($i -lt $lines.Count) {
        $line = $lines[$i]

        # Check if the next non-blank line (or this line) starts a getRenderBoundingBox override
        if ($line -match '^\s*@Override\s*$' -and $i + 1 -lt $lines.Count -and $lines[$i + 1] -match 'getRenderBoundingBox') {
            # Skip @Override and the method body
            $i += 2  # skip @Override and method signature
            # Skip method body (find matching closing brace)
            $depth = 0
            while ($i -lt $lines.Count) {
                if ($lines[$i] -match '\{') { $depth++ }
                if ($lines[$i] -match '\}') {
                    if ($depth -le 1) {
                        $i++  # skip closing brace
                        break
                    }
                    $depth--
                }
                $i++
            }
            $modified = $true
        }
        # Also handle case where @Override and method are on same conceptual group (single-line)
        elseif ($line -match 'getRenderBoundingBox' -and $line -notmatch '//') {
            # Check if the previous line was @Override
            if ($newLines.Count -gt 0 -and $newLines[$newLines.Count - 1] -match '^\s*@Override\s*$') {
                $newLines.RemoveAt($newLines.Count - 1)
            }
            # Skip the return statement and closing brace if it's a simple one-liner method
            # Find method end
            $depth = 0
            $started = $false
            while ($i -lt $lines.Count) {
                if ($lines[$i] -match '\{') { $depth++; $started = $true }
                if ($lines[$i] -match '\}' -and $started) {
                    $depth--
                    if ($depth -le 0) {
                        $i++
                        break
                    }
                }
                $i++
            }
            $modified = $true
        } else {
            $newLines.Add($line)
            $i++
        }
    }

    if ($modified) {
        Set-Content -Path $file -Value $newLines
        Write-Host "Removed getRenderBoundingBox from: $($_.Name)"
    }
}
Write-Host "Done."

