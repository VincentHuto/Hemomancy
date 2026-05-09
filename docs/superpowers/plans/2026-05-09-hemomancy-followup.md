# Hemomancy Follow-up Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve inscription presentation, puppeteer usability, summon follow reliability, and blood-route visibility.

**Architecture:** Keep server-owned state authoritative and use NeoForge 1.21 menu/payload/client-render patterns already present in the mod. Blood Echoes become wall-attached inscription blocks while Rite Fragments keep the relic/floor treatment. Puppeteer workflows move from ambiguous clicks into a single screen with small server packets for button actions. Suture visuals reuse the existing client world render event style and receive owner-filtered link snapshots from the server.

**Tech Stack:** Minecraft 1.21.1, NeoForge 21.1.x, Java 21, payload networking, `AbstractContainerMenu`, client `Screen`, world render events.

---

### Task 1: Wall-Attached Blood Echoes

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/block/shared/DiscoveryInscriptionBlock.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/DiscoveryInscriptionPlacement.java`
- Modify: structure after-place callers under `src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/`
- Modify: blockstate/model JSON for `blood_echo_inscription`

- [ ] Add a wall mode to `DiscoveryInscriptionBlock` based on the block instance, with thin horizontal-facing shape, no collision, and support survival check.
- [ ] Add `placeOnInteriorWall(...)` to scan air positions with sturdy horizontal backing blocks.
- [ ] Route Blood Echo structure placements through wall placement and leave Rite Fragments on floors.
- [ ] Update `blood_echo_inscription` model/blockstate to render like a wall plaque/rune plane.

### Task 2: Puppeteer's Spindle Screen

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/menu/PuppeteersSpindleMenu.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/screen/PuppeteersSpindleScreen.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/network/summon/PacketPuppeteersSpindleAction.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ContainerInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/PuppeteersSpindleBlock.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarItem.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java`
- Modify: language file

- [ ] Register a menu type and screen.
- [ ] Open the menu from the spindle block instead of doing item-click workflows directly.
- [ ] Move bind, wind, unlock, select, call, and recall operations into reusable `MarionetteCrossbarItem`/spindle helpers.
- [ ] Add a server packet for screen buttons: `SELECT`, `BIND`, `WIND`, `UNLOCK`, `CALL_OR_RECALL`.
- [ ] Render known summons, selected summon, thread meter, costs, and action buttons.

### Task 3: Veinwing Vulture Follow Reliability

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BoundSummonBehavior.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/VeinwingVultureEntity.java`

- [ ] Keep the existing long-distance teleport protection.
- [ ] Add a flight-aware helper that pulls flying summons toward an owner anchor when idle or too far.
- [ ] Add a closer teleport fail-safe for flying summons if movement cannot close distance for several ticks.

### Task 4: Hematic Suture Visibility

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/data/SutureLinkClientData.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/HematicSutureLinkRenderer.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/common/network/routing/PacketSyncSutureLinks.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/routing/BloodRoutingSavedData.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/HematicSutureNeedleItem.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java`

- [ ] Send owner-filtered link snapshots when binding/cycling routes.
- [ ] Store snapshots client-side and expire stale entries.
- [ ] Render visible lines from the player to sutured blocks when the player holds the needle, plus small glowing target markers.

### Task 5: Verification

- [ ] Run `./gradlew.bat build`.
- [ ] If build fails, fix compile/runtime issues and rerun the same command.
- [ ] Summarize changed behavior and verification output.
