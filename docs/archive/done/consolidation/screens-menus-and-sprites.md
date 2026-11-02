# Screens, menu geometry, and sprites

## Ownership map

The server menu type is registered in [ContainerInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/ContainerInit.java); its screen is registered in [ClientEvents](../../src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java). A block entity or item opens the menu. The menu owns slots, `ContainerData`, validation, button actions, and quick-move routing. The screen draws and hit-tests what that menu exposes. For a new machine, all four links need to be traceable.

| Screen family | Current examples | Shared part and important exception |
| --- | --- | --- |
| Animated blood machine | [Enzymatic Scriptorium](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/EnzymaticScriptoriumScreen.java), [Resonant Forge](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/ResonantForgeScreen.java), [Cabinet](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/PhlebotomistsCabinetScreen.java) | All use [VeinBackgroundRenderer](../../src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/VeinBackgroundRenderer.java) and the blood inventory panel; dimensions and controls differ. |
| Compact processing | [Ghastly Alembic](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/GhastlyAlembicScreen.java), [Pallid Retort](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/PallidRetortScreen.java), [Centrifuge](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/VialCentrifugeScreen.java), [Crucible](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/MycelialCrucibleScreen.java) | Repeated panel, progress/status, bars, labels, and tooltips; blood, pallid, and fungal material choices differ. |
| Dialogue | [DialogueScreen](../../src/main/java/com/vincenthuto/hemomancy/client/screen/dialogue/DialogueScreen.java), [DialogueLayout](../../src/main/java/com/vincenthuto/hemomancy/client/screen/dialogue/DialogueLayout.java), [DialogueThemeStyle](../../src/main/java/com/vincenthuto/hemomancy/client/screen/dialogue/DialogueThemeStyle.java) | Already has a layout and theme system. It uses `blitSprite` resources under [dialogue sprites](../../src/main/resources/assets/hemomancy/textures/gui/sprites/dialogue), including material/flat style variants. Keep this distinct from inventory-panel texture coordinates. |
| Progression and radial views | [HarbingerProgressScreen](../../src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/HarbingerProgressScreen.java), [UnstainedProgressScreen](../../src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/unstained/UnstainedProgressScreen.java), [GenericRadialMenu](../../src/main/java/com/vincenthuto/hemomancy/client/screen/radial/GenericRadialMenu.java) | Shared viewport/navigation or control geometry may be useful; layout semantics are unlike machine slots. |

## Concrete repetition and boundaries

### Alembic/Retort geometry, from the current menu and screen code

Coordinates below are relative to each screen's `leftPos, topPos`; slot entries are the menu's slot origins. This is the first concrete layout contract to preserve during extraction.

| Element | Ghastly Alembic | Pallid Retort |
| --- | --- | --- |
| Screen/craft area | `176×176`; upper panel height `86` | `176×176`; upper panel height `86` |
| Ingredient / catalyst | `(44,32)` / `(8,8)` | `(44,32)` / `(8,8)` |
| Result / flask / flask output | `(134,32)` / `(155,58)` / `(134,58)` | same coordinates, different slot restrictions |
| Extra ingredient slots | tincture blood `(8,32)`; second catalyst `(8,58)` when tier allows | none |
| Player inventory | nine columns starting `(8,94)`, rows at `94,112,130`; hotbar at `y=152` | same |
| Inventory panel | panel anchor is first player slot minus `(5,6)`, so `(3,88)`; BLOODY material | `(3,88)`; PALLID material |
| Heat / progress / fluid bar | heat starts `(44,50)`; arrow `(68,36)`; fluid bar `(158,16)` size `10×38` | same bounds; white-humor widget |
| Title / inventory label | centered title at `y=4`; inventory label at `y=93` | same |
| Status / hover | status at `(36,56)`; advanced feedback at `(36,66)`; advanced-cost hover `x=68..130`, `y=34..43` | heated/no-coolant status at `(36,56)` or `(40,56)`; fluid-bar hover from widget bounds |

Both menu files still contain introductory comments describing three container slots and inventory indexes from an older layout. The constants and `addSlot` calls are authoritative: Alembic has seven machine slots and Retort has five. Correct those comments when editing gameplay code; do not derive a helper from them.

[InventoryPanelTextures](../../src/main/java/com/vincenthuto/hemomancy/client/screen/util/InventoryPanelTextures.java) already defines BLOODY, FUNGAL, MORPHIC, and PALLID panels at 172×86. The Scriptorium is 216×214 and Forge 220×224; compact machines are often 176 wide with different heights. A shared **machine screen layout contract** should return the inventory panel anchor, craft-area bounds, title/status bounds, bar bounds, and slot decoration positions from one screen-space coordinate system. Preserve each screen's actual width and artwork.

The Alembic and Retort screens both use 176×176 surfaces and related bar/recipe tooltips. Their menus share initial slot positions but have different extra slots and fluid meanings. That is a good first pair for extracting panel and label placement, with distinct palette and bar providers. The Scriptorium's [menu](../../src/main/java/com/vincenthuto/hemomancy/common/menu/tile/crafting/EnzymaticScriptoriumMenu.java) has 47 data values and custom click buttons; it should not be pushed into a generic furnace layout.

The [Stage 3 transaction audit](stage-3-distillation/README.md) records the Alembic/Retort output-extraction and recipe-ledger differences. Keep those menu actions out of a visual layout helper.

The Scriptorium uses independent PNG icons under [scriptorium textures](../../src/main/resources/assets/hemomancy/textures/gui/scriptorium); one icon is treated as an 18-pixel source while most are 16. Dialogue uses named sprites and `.png.mcmeta` metadata. Record source size, destination bounds, hover bounds, and tooltip bounds together so a sprite change cannot quietly shift the clickable area.

## First migration slice

1. Produce a screen/menu geometry table for the Alembic and Retort: image size, every slot, inventory origin, title, status, bar, icon, click target, and tooltip rectangle.
2. Extract shared panel/bar/tooltip positioning where numbers match. Keep menu `quickMoveStack` and machine validation server-side.
3. Review all machine screens at actual GUI scales. Use the established animated red veins, recessed dark-red slots, crimson borders, pale-red sentence-case labels, recognizable icons, and tooltips for blood machines; preserve Pallid/Fungal treatments where their identity is intentional.
4. Check screen registration, slot alignment, text clipping, hover feedback, and inventory-panel seams with the live client. A PNG path or code-level rectangle check cannot prove the screen feels properly spaced.
