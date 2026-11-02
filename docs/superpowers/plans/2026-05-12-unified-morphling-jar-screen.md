# Unified Morphling Jar Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the split Morphling Jar viewer/inventory flow with one container screen that keeps item management and the animated morphling selection display together.

**Architecture:** `MorphlingJarMenu` remains the authoritative server container and moves its six jar slots into two vertical columns around a central display. `MorphlingJarScreen` absorbs the animated viewer behavior and renders a procedural green jar/inventory UI without a blit texture. Existing packets continue to open the menu and equip or clear the selected morphling.

**Tech Stack:** NeoForge 1.21.1 container menus, Minecraft client `GuiGraphics`, existing Hemomancy payload packets, PowerShell source guards, `./gradlew.bat build`.

---

### Task 1: Lock Expected Unified Screen Shape

**Files:**
- Verify: `src/main/java/com/vincenthuto/hemomancy/client/screen/item/living/MorphlingJarScreen.java`
- Verify: `src/main/java/com/vincenthuto/hemomancy/common/menu/MorphlingJarMenu.java`
- Verify: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/ItemMorphlingJar.java`
- Verify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`

- [ ] **Step 1: Run the failing source guard**

```powershell
@'
$checks = @()
$screen = Get-Content 'src/main/java/com/vincenthuto/hemomancy/client/screen/item/living/MorphlingJarScreen.java' -Raw
$menu = Get-Content 'src/main/java/com/vincenthuto/hemomancy/common/menu/MorphlingJarMenu.java' -Raw
$jar = Get-Content 'src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/ItemMorphlingJar.java' -Raw
$client = Get-Content 'src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java' -Raw
$checks += @{ Name = 'screen has no texture blit'; Pass = $screen -notmatch 'morphling_jar_gui\.png|graphics\.blit\(GUI' }
$checks += @{ Name = 'screen has unified display'; Pass = $screen -match 'renderMorphlingDisplay' -and $screen -match 'PacketUpdateLivingStaffMorph' }
$checks += @{ Name = 'menu uses side columns'; Pass = $menu -match 'JAR_LEFT_SLOT_X' -and $menu -match 'JAR_RIGHT_SLOT_X' }
$checks += @{ Name = 'item opens server container'; Pass = $jar -notmatch 'MorphlingJarViewerScreen' -and $jar -match 'openMenu' }
$checks += @{ Name = 'keybind opens unified container'; Pass = $client -notmatch 'MorphlingJarViewerScreen\.openScreen' }
$failed = $checks | Where-Object { -not $_.Pass }
$checks | ForEach-Object { Write-Host "$($_.Name): $($_.Pass)" }
if ($failed) { exit 1 }
'@ | powershell -NoProfile -ExecutionPolicy Bypass
```

Expected: FAIL because the old screen still uses `morphling_jar_gui.png`, the open paths still call `MorphlingJarViewerScreen`, and the menu still lays jar slots out as a 3x2 grid.

### Task 2: Reposition Container Slots

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/menu/MorphlingJarMenu.java`

- [ ] **Step 1: Replace the 3x2 jar grid**

Move jar slots 0-2 to the left of the central display and 3-5 to the right. Keep player inventory slots in normal 9x3 plus hotbar form at the bottom of the larger screen.

- [ ] **Step 2: Keep server behavior unchanged**

Do not change quick move, save, validity, or equipped-morphling clearing logic.

### Task 3: Merge Viewer Rendering Into Container Screen

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/item/living/MorphlingJarScreen.java`

- [ ] **Step 1: Remove the GUI texture path**

Delete the blit texture and old blood ring rendering.

- [ ] **Step 2: Add procedural green UI rendering**

Render an upper animated green vein display, a green gradient inventory background, hand-drawn slot wells, and a central swimming morphling display.

- [ ] **Step 3: Add selection input**

Clicking a swimming morphling sends `PacketUpdateLivingStaffMorph(slot)`, clicking the active morphling sends `PacketUpdateLivingStaffMorph(-1)`, and the screen stays open so inventory management remains available.

### Task 4: Unify Open Paths

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/ItemMorphlingJar.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`

- [ ] **Step 1: Right-click opens the server container**

Use the existing `MorphlingJarMenu` provider for normal right-click and shift-right-click.

- [ ] **Step 2: Keybind opens the unified server container**

Send `OpenMorphlingJarPacket` for the jar keybind regardless of shift state.

### Task 5: Documentation And Verification

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`

- [ ] **Step 1: Document the unified jar screen**

Update the morphling and screen docs to say the Morphling Jar now has one unified inventory/selection container.

- [ ] **Step 2: Verify**

Run:

```powershell
./gradlew.bat build
git diff --check
```

Expected: build exits 0, diff check has no actionable whitespace failures.
