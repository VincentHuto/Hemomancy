$tileDir = "C:\Users\Vince\Desktop\My MC Mods\NeoForge\NeoForge Attempts\1.21.1\Hemomancy\src\main\java\com\vincenthuto\hemomancy\common\tile"

Get-ChildItem -Path $tileDir -Filter "*.java" -Recurse | ForEach-Object {
    $file = $_.FullName
    $content = Get-Content $file -Raw
    $modified = $false

    # Fix saveAdditional signature
    if ($content -match 'void saveAdditional\(CompoundTag (\w+)\)' -and $content -notmatch 'void saveAdditional\(CompoundTag \w+, HolderLookup') {
        $param = $Matches[1]
        $content = $content -replace "void saveAdditional\(CompoundTag $param\)", "void saveAdditional(CompoundTag $param, HolderLookup.Provider registries)"
        $content = $content -replace "super\.saveAdditional\($param\)", "super.saveAdditional($param, registries)"
        $modified = $true
    }

    # Fix load signature
    if ($content -match 'void load\(CompoundTag (\w+)\)' -and $content -notmatch 'void load\(CompoundTag \w+, HolderLookup') {
        $param = $Matches[1]
        $content = $content -replace "void load\(CompoundTag $param\)", "void load(CompoundTag $param, HolderLookup.Provider registries)"
        $content = $content -replace "super\.load\($param\)", "super.load($param, registries)"
        $modified = $true
    }

    # Fix getUpdateTag() method signature (override)
    if ($content -match 'CompoundTag getUpdateTag\(\)' -and $content -notmatch 'CompoundTag getUpdateTag\(HolderLookup') {
        $content = $content -replace 'CompoundTag getUpdateTag\(\)', 'CompoundTag getUpdateTag(HolderLookup.Provider registries)'
        $content = $content -replace 'super\.getUpdateTag\(\)', 'super.getUpdateTag(registries)'
        $modified = $true
    }

    # Fix handleUpdateTag signature
    if ($content -match 'void handleUpdateTag\(CompoundTag (\w+)\)' -and $content -notmatch 'void handleUpdateTag\(CompoundTag \w+, HolderLookup') {
        $param = $Matches[1]
        $content = $content -replace "void handleUpdateTag\(CompoundTag $param\)", "void handleUpdateTag(CompoundTag $param, HolderLookup.Provider registries)"
        $content = $content -replace "super\.handleUpdateTag\($param\)", "super.handleUpdateTag($param, registries)"
        $modified = $true
    }

    # Fix onDataPacket signature
    if ($content -match 'void onDataPacket\(Connection (\w+), ClientboundBlockEntityDataPacket (\w+)\)' -and $content -notmatch 'void onDataPacket\(Connection \w+, ClientboundBlockEntityDataPacket \w+, HolderLookup') {
        $p1 = $Matches[1]
        $p2 = $Matches[2]
        $content = $content -replace "void onDataPacket\(Connection $p1, ClientboundBlockEntityDataPacket $p2\)", "void onDataPacket(Connection $p1, ClientboundBlockEntityDataPacket $p2, HolderLookup.Provider lookupProvider)"
        $content = $content -replace "super\.onDataPacket\($p1, $p2\)", "super.onDataPacket($p1, $p2, lookupProvider)"
        $modified = $true
    }

    # Add HolderLookup import if modified and not already present
    if ($modified -and $content -notmatch 'import net\.minecraft\.core\.HolderLookup') {
        # Insert before first 'import net.minecraft.' line
        $content = $content -replace '(import net\.minecraft\.)', "import net.minecraft.core.HolderLookup;`r`nimport net.minecraft."
    }

    if ($modified) {
        Set-Content -Path $file -Value $content -NoNewline
        Write-Host "Updated: $($_.Name)"
    }
}
Write-Host "Done."

