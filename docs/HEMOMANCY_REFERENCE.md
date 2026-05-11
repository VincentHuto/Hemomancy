# Hemomancy - Developer Reference

> **Last audited:** 2026-05-11
> **Mod ID / package:** `hemomancy` / `com.vincenthuto.hemomancy`
> **Target:** Minecraft `1.21.1`, NeoForge `21.1.219`, Java `21`
> **Version:** `6.0.1-neoforge.1.21.1.0`

This is the canonical developer reference for Hemomancy. Current code and data are authoritative when older prose, design notes, or screenshots disagree. Use [LORE_REFERENCE.md](LORE_REFERENCE.md) for tone, faction beliefs, cosmology, and character/narrative context.

Hemomancy is a NeoForge blood magic mod built around the *quality* of blood manipulation rather than just quantity. Its public fiction frames blood magic as a sacred inheritance of the Hematic Order, while the deeper biological truth is fungal blood-memory/infection tied to a slow cosmic reproductive cycle. Keep that moral grayness intact: Harbingers are taboo and dangerous but not simple villains, and the Unstained are not simple heroes.

**Status legend:** `Implemented` means present in the current NeoForge 1.21.1 runtime path. `Partial` means a playable or compiled spine exists with explicit remaining work. `Dormant` means source/design is preserved but excluded or unregistered. `Planned` means design/lore intent without active runtime behavior.

**Recently audited systems:** attachments/capabilities, NeoForge payload networking, Blood Structure/Cardinal Rite degree gates, Qliphoth Communion and Apotheos gating, direct blood routing, puppeteer summon trials, Mycelial Crucible/Lantern, White Humor Purification, Blood Moon sync, machine access gating, Field Notes/Liber discovery, MnA/Curios dormant compat, and focused test coverage.

<!-- Texture base paths from this docs/ file -->
<!-- Items:   ../src/main/resources/assets/hemomancy/textures/item/ -->
<!-- Blocks:  ../src/main/resources/assets/hemomancy/textures/block/ -->
<!-- Entity:  ../src/main/resources/assets/hemomancy/textures/entity/ -->
<!-- GUI:     ../src/main/resources/assets/hemomancy/textures/gui/ -->
<!-- Effects: ../src/main/resources/assets/hemomancy/textures/mob_effect/ -->
<!-- Armor:   ../src/main/resources/assets/hemomancy/textures/models/armor/ -->
<!-- MnA:     ../src/main/resources/assets/hemomancy/textures/mna/ -->


---

## Table of Contents

**Foundation**
1. [Getting Started](#1-getting-started)
2. [Core Architecture & Player Capabilities](#2-core-architecture--player-capabilities)
3. [Configuration](#3-configuration)
4. [Networking & Packets](#4-networking--packets)

**Paths & Progression**
5. [The Harbinger Path (Hematic Order)](#5-the-harbinger-path-hematic-order)
6. [The Unstained Path (Anti-Hemomancy)](#6-the-unstained-path-anti-hemomancy)
7. [Mutual Exclusion of Paths](#7-mutual-exclusion-of-paths)

**Hematic Body & Blood Systems**
8. [Blood Manipulations](#8-blood-manipulations)
9. [Blood Tendency (Kinship) System](#9-blood-tendency-kinship-system)
10. [Vascular System](#10-vascular-system)
11. [Skill Tree](#11-skill-tree)
12. [Bloodlines](#12-bloodlines)
13. [Scars & Spores](#13-scars--spores)
14. [Status Effects & Potions](#14-status-effects--potions)

**Unstained Systems**
15. [Unstained Systems](#15-unstained-systems)

**Companions, Summons & Automation**
16. [Morphlings](#16-morphlings)
17. [Puppeteering & Summons](#17-puppeteering--summons)
18. [Drudge System](#18-drudge-system)
19. [Direct Blood Routing & Servitors](#19-direct-blood-routing--servitors)

**Content Catalogs**
20. [Items & Materials](#20-items--materials)
21. [Tools & Weapons](#21-tools--weapons)
22. [Armor Sets](#22-armor-sets)
23. [Functional Blocks & Block Entities](#23-functional-blocks--block-entities)
24. [Decorative & Building Blocks](#24-decorative--building-blocks)
25. [Recipe Systems](#25-recipe-systems)
26. [Mob Entities](#26-mob-entities)
27. [Projectile & Blood Construct Entities](#27-projectile--blood-construct-entities)

**World, Client & Appendices**
28. [World Generation & Biomes](#28-world-generation--biomes)
29. [Structures](#29-structures)
30. [Villagers & Professions](#30-villagers--professions)
31. [GUIs & Overlays](#31-guis--overlays)
32. [Advancements](#32-advancements)
33. [Keybindings](#33-keybindings)
34. [Commands](#34-commands)
35. [Sound Events](#35-sound-events)
36. [Particle Types](#36-particle-types)
37. [Mod Compatibility](#37-mod-compatibility)
38. [Known WIP / Incomplete Systems](#38-known-wip--incomplete-systems)

---
## 1. Getting Started

1. **Find Gourd Seeds** ![Gourd Seeds](../src/main/resources/assets/hemomancy/textures/item/gourd_seeds.png) — obtained from breaking grass (advancement: *Strange Seeds*).
2. **Discover a Blood Temple** — a naturally generating structure containing a **Mortal Display** pedestal.
3. **Activate the Blood Temple** — click the Mortal Display to awaken your blood, enabling the mod's features (advancement: *The First Awakening*). This activates your `IBloodVolume` capability (`active = true`).
4. **Obtain the Liber Sanguinum** ![Liber Sanguinum](../src/main/resources/assets/hemomancy/textures/item/liber_sanguinum.png) — the mod's guide book (entity model: ![](../src/main/resources/assets/hemomancy/textures/entity/liber_sanguinum.png)), crafted using a structure recipe (bookshelf + Sanguine Formation ![Sanguine Formation](../src/main/resources/assets/hemomancy/textures/item/sanguine_formation.png)). (advancement: *Sanctum Sanguinium*).
5. **Craft Befouling Ash** ![Befouling Ash](../src/main/resources/assets/hemomancy/textures/item/befouling_ash_trail.png) — a key ingredient for blood structure recipes (advancement: *Ashen Beginnings*).

From here the player can pursue the **Harbinger Path** (blood magic) or eventually diverge to the **Unstained Path** (anti-blood purification).

---

## 2. Core Architecture & Player Capabilities

### 2.1 Source-of-Truth Files

| Area | Current source of truth |
|---|---|
| Mod metadata, Minecraft/NeoForge/Java versions | `gradle.properties`, `build.gradle`, `src/main/resources/META-INF/neoforge.mods.toml` |
| Entrypoint and event-bus wiring | `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java` |
| Registries | `src/main/java/com/vincenthuto/hemomancy/common/init/*Init.java` |
| Player/block/item state | `HemoAttachmentTypes`, `HemoCapabilityKeys`, `HemoCapabilityRegistrar`, `HemoCapabilityAccess` |
| Networking | `common/network/PacketHandler.java` and packet classes implementing `CustomPacketPayload` |
| Generated client assets | `common/data/gen/DataGeneration.java` plus `src/generated/resources` |
| Runtime datapack content | `src/main/resources/data/hemomancy/**` |
| Optional integrations | `compat/**`, with MnA and Curios excluded from compilation until compatible NeoForge 1.21.1 deps exist |

### 2.2 Runtime Wiring

`Hemomancy.java` registers the main DeferredRegisters, config specs, capability registration, payload registration, creative-tab population, item-inquiry reload listener, and HutosLib book serializer setup. Keep NeoForge 1.21 APIs in new work: `net.neoforged.*`, `DeferredHolder`, attachments/capabilities, and payload-based networking.

`build.gradle` includes `../HutosLib` as a composite build through `settings.gradle`, uses local jars for TerraBlender, GeckoLib, and JEI, and excludes `compat/mna/**` and `compat/curios/**` from main compilation. `runData` currently enables blockstates, item models, and language generation; server recipes/tags/loot providers are intentionally commented out.

### 2.3 Player & Block State

All player-attached NeoForge attachments and exposed capabilities are registered through `HemoAttachmentTypes`, `HemoCapabilityKeys`, and `HemoCapabilityRegistrar`. The old provider-style capability-init pattern is not used on the current NeoForge 1.21.1 branch.

| Capability | Interface | Purpose |
|---|---|---|
| Blood Volume | `IBloodVolume` | Current/max blood, active state, bloodline link, trickle/auto-draw settings, direct-routing bloodline opt-in, blood debt tracking (Hemorath encounter) |
| Known Still Arts | `IKnownStillArts` | Set of Still Arts granted by Our Lady / Unstained rites; selected art; synced via `KnownStillArtsServerPacket` |
| Known Summons | `IKnownSummons` | Permanently unlocked puppeteer summon shapes; synced via `KnownSummonsServerPacket` and refreshed on progress-screen open |
| Liber Knowledge | `IBookKnowledge` | Player-owned Liber Sanguinum / Liber Immaculatus unlock state, memo discovery, and entry visibility |
| Skill Progress | `SkillProgress` | Per-player Harbinger skill points and unlocked skill ids |
| Blood Tendency | `IBloodTendency` | 8-axis alignment scores (kinship with blood tendencies) |
| Vascular System | `IVascularSystem` | Health state of 7 vein sections |
| Known Manipulations | `IKnownManipulations` | Unlocked blood manipulations, selected manip, vein locations |
| Equipped Morphling | `IEquippedMorphling` | Currently equipped morphling for the Living Staff |
| Scar | `IScar` | Scar slot / scar binder state |
| Scar Item Handler | `IScarsItemHandler` | Inventory for scar binder contents |
| Initiatory Degree | `IInitiatoryDegree` | Harbinger rank (0–8) |
| Unstained Progress | `IUnstainedProgress` | Purification path state (purity, clarity, flags) |
| White Humor Volume | `IWhiteHumorVolume` | Unstained/pallid reservoir for purified lymph. Used by Pale Humor Flasks and Pallid Retort white humor storage. |
| Earthen Vein Location | `IEarthenVeinLoc` | Block capability for earthen vein blocks |
| Visceral Organs | `IVisceralOrgans` | Tracks extracted/modified organs (Spleen, Liver, Lungs, Kidneys, Heart) for the Visceral Mirror ritual system |

Block and item storage use the same attachment/capability style. Current block attachments cover blood volume, blood tendency, and white humor volume; item capabilities cover portable blood volume and scar metadata.

### 2.4 Data Generation & Focused Tests

`runData` writes into `src/generated/resources`, with `src/main/resources` taking precedence on duplicates. The active providers are `HemoBlockStateProvider`, `HemoItemModelProvider`, and `HemoLanguageProvider`; server recipe/tag/loot providers remain commented in `DataGeneration.java`.

Focused unit-style tests now exist under `src/test/java` for newer gameplay rules and serialization seams, including direct blood routing, blood container transfer, Mycelial Lantern automation, puppeteer summon trials, morphic nectar/primal morphlings, blood gourd tier stats, crude-memory model data, manipulation rank gates, known-manipulation grants, dialogue starter memory choice, loot data, and overlay rendering rules. Use `./gradlew.bat build` for docs-adjacent safety checks only if code or generated data changes.

---

## 3. Configuration

Hemomancy registers active server, client, and common config specs through `HemoConfig`. `HemoServerConfig` and `HemoClientConfig` contain the active gameplay/client options; `HemoCommonConfig` is currently an empty reserved common spec. `HemoMnAConfig` remains in source as the conditional MnA config target, but it is dormant and not registered while MnA compat is excluded from compilation.

### 3.1 Server Config (`HemoServerConfig`)

**Blood Volume** (`blood_volume`):

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `bloodRegenEnabled` | Boolean | `true` | — | Whether passive blood regeneration is enabled |
| `bloodRegenRate` | Double | `1.0` | 0.1–100.0 | Blood restored per regen tick |
| `bloodRegenInterval` | Int | `20` | 1–1200 | Ticks between each regen tick (20 = 1 second) |
| `bloodDrainOnDamageEnabled` | Boolean | `true` | — | Whether taking damage drains blood |
| `bloodDrainPerDamage` | Double | `5.0` | 0.1–500.0 | Blood drained per point of damage |
| `bloodGainOnKillEnabled` | Boolean | `true` | — | Whether kills grant blood |
| `bloodGainPerKill` | Double | `25.0` | 1.0–1000.0 | Base blood gained per kill |
| `bloodGainBossMultiplier` | Double | `5.0` | 1.0–50.0 | Multiplier for boss entity kills |

**Blood Tendency** (`blood_tendency`):

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `tendencyShiftOnKillEnabled` | Boolean | `true` | — | Whether kills shift tendency alignment |
| `tendencyShiftAmount` | Double | `1.0` | 0.1–100.0 | Tendency gained per relevant kill |
| `tendencyShiftOnManipUse` | Double | `0.5` | 0.0–50.0 | Tendency gained when using a manipulation of that tendency |

**Vascular System** (`vascular_system`):

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `vascularDegradationOnDamageEnabled` | Boolean | `true` | — | Whether damage degrades vascular sections |
| `vascularDamagePerHit` | Double | `0.5` | 0.01–50.0 | Vascular health lost per damage point |
| `vascularDegradationOnManipEnabled` | Boolean | `true` | — | Whether manipulations strain vein sections |
| `vascularManipStrain` | Double | `1.0` | 0.01–50.0 | Vascular health lost per manipulation use |
| `vascularPassiveHealEnabled` | Boolean | `true` | — | Whether vascular sections heal over time |
| `vascularHealRate` | Double | `0.1` | 0.01–10.0 | Vascular health restored per heal tick |
| `vascularHealInterval` | Int | `100` | 1–6000 | Ticks between heal ticks |
| `vascularDebuffsEnabled` | Boolean | `true` | — | Whether damaged sections apply debuffs |

**Bloodline** (`bloodline`):

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `bloodlinePoolEnabled` | Boolean | `true` | — | Whether members contribute to shared pool |
| `bloodlinePoolContributionRate` | Double | *(see code)* | — | Blood per tick donated to pool |
| `bloodlinePoolContributionInterval` | Int | *(see code)* | — | Ticks between pool contributions |
| `bloodlinePoolMinBloodThreshold` | Double | *(see code)* | — | Minimum blood before donating stops |
| `bloodlineAutoDrawMaxRate` | Double | `2.0` | 0.1–100.0 | Max blood/tick auto-drawn from pool |
| `bloodlineHealEnabled` | Boolean | `true` | — | Whether nearby members heal each other |
| `bloodlineHealAmount` | Double | `1.0` | 0.1–20.0 | Health restored per heal tick |
| `bloodlineHealInterval` | Int | `40` | 1–6000 | Ticks between bloodline heal ticks |
| `bloodlineHealHealthThreshold` | Double | `0.5` | 0.05–1.0 | Health % below which healing activates |
| `bloodlineHealRange` | Double | `32.0` | 1.0–256.0 | Max distance for bloodline healing |

**Morphling** (`morphling`):

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `morphlingPassiveDrainEnabled` | Boolean | `true` | — | Whether equipped morphlings drain blood |
| `morphlingDrainRate` | Double | `0.5` | 0.01–100.0 | Blood drained per drain tick |
| `morphlingDrainInterval` | Int | `60` | 1–6000 | Ticks between drain ticks |

### 3.2 Client Config (`HemoClientConfig`)

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `location` | Int | `0` | 0–3 | Blood Volume HUD position: 0=Top Left, 1=Top Right, 2=Bottom Left, 3=Bottom Right |

| `render_layers.renderBloodGourdLayer` | Boolean | `true` | true/false | Renders blood gourds and curved horns equipped in the gourd slot |
| `render_layers.renderVasculariumCharmLayer` | Boolean | `true` | true/false | Renders the Charm of Vascularium on the player model |
| `render_layers.renderMorphlingJarLayer` | Boolean | `true` | true/false | Renders equipped morphling jars on the player model |
| `render_layers.renderEquippedMorphlingLayer` | Boolean | `true` | true/false | Renders the equipped morphling on the player arm in third person |
| `render_layers.renderEquippedMorphlingHandLayer` | Boolean | `true` | true/false | Renders the equipped morphling on the player hand in first person |
| `render_layers.renderMorphlingMutationLayer` | Boolean | `true` | true/false | Renders morphling mutation overlays and model attachments |

### 3.3 MnA Cross-Mod Config (`HemoMnAConfig`)

Preserved for MnA compat, but **not currently registered** because the MnA dependency and `Hemomancy.java` registration block are commented out on the NeoForge 1.21.1 branch. See §37.1 for the dormant compat status.

**Conversion** (`conversion`):

| Key | Default | Description |
|-----|---------|-------------|
| `manaToBloodRatio` | `5.0` | Blood per 1 mana consumed (Sanguine Transmutation) |
| `bloodToManaRatio` | `0.2` | Mana per 1 blood consumed (Sanguine Offering) |

**Blood Tithe** (`blood_tithe`):

| Key | Default | Description |
|-----|---------|-------------|
| `bloodTitheEnabled` | `true` | Enable Blood Tithe for Harbinger faction |
| `bloodTitheManaReduction` | `0.25` | % of mana cost converted to blood (0.0–1.0) |
| `bloodTitheBloodPerMana` | `5.0` | Blood drained per 1 mana replaced |

**Living Thread Armor** (`living_thread_armor`):

| Key | Default | Description |
|-----|---------|-------------|
| `setBonus3pcMaxMana` | `500.0` | Bonus max mana from 3-piece set |
| `setBonus3pcManaRegen` | `0.5` | Mana regen multiplier (+50%) from 3-piece set |

**Trapezohedron** (`trapezohedron`):

| Key | Default | Description |
|-----|---------|-------------|
| `effectRadius` | `8` | Broken Mana Trapezohedron aura radius (blocks) |

**Spell ↔ Manipulation Combos** (`spell_manip_combos`):

| Key | Default | Description |
|-----|---------|-------------|
| `comboSystemEnabled` | `true` | Enable the alternating combo system |
| `arcaneResonanceDuration` | `100` | Ticks of blood cost reduction after casting MnA blood spell |
| `arcaneResonanceBloodReduction` | `0.25` | % blood cost reduction during Arcane Resonance |
| `sanguineClarityDuration` | `100` | Ticks of mana cost reduction after using manipulation |
| `sanguineClarityManaReduction` | `0.20` | % mana cost reduction during Sanguine Clarity |

**Sanguilith** (`sanguilith`):

| Key | Default | Description |
|-----|---------|-------------|
| `healthPerMagnitude` | `10.0` | Bonus HP per magnitude above 1 |
| `maxSummons` | `2` | Max active Sanguliliths per player |

---

## 4. Networking & Packets

All packets are registered in `PacketHandler.registerChannels()` using the NeoForge 1.21 payload API. Each packet implements `CustomPacketPayload` with a static `TYPE` and `STREAM_CODEC`, then registers through `playToClient`, `playToServer`, or `playBidirectional`. Old channel constants are not used.

| Payload Area | Examples | Direction Pattern |
|--------------|----------|-------------------|
| Player state sync | Blood volume, blood tendency, vascular system, degree, Unstained progress, Liber knowledge | Mostly server → client |
| Manipulations and Still Arts | Selected manip/art, use key packets, cooldowns, vein teleport, avatar tracking | Client → server plus sync responses |
| Scars, binders, morphlings, summons | Scar inventories, gourd sync, morphling jar/staff actions, equipped morphling sync, known-summon sync | Bidirectional / mixed |
| Rites and machines | Cardinal rite activation/sync, crafting rings, centrifuge/loom buttons, SSC screen | Mixed |
| Dialogue and world events | NPC dialogue, Qliphoth blooms, Blood Moon, particles, structure placement | Mixed |

Notable packets:
- `PacketUnlockSkill` / `PacketSyncSkills` — Skill tree progression through payload registration in `PacketHandler`
- `PacketSyncActiveRites` — Cardinal rite boundary sync for client-side rendering
- `PacketSyncDegree` / `PacketSyncUnstainedProgress` — Path progression sync
- `KnownSummonsRequestPacket` / `KnownSummonsServerPacket` — Puppeteer summon unlock sync, refreshed on login/respawn/dimension change/screen open/unlock
- `PacketPuppeteersSpindleAction` — Server-side spindle screen action packet. Selects summons, binds slotted crossbars, and calls/recalls using the crossbar currently inside the open spindle container.
- `SyncTrackingAvatarPacket` — Blood Avatar visual state sync to all nearby players
- `TeleportToVeinPacket` — Venous Travel teleportation
- `OpenDialoguePacket` / `DialogueOptionPacket` — Full NPC dialogue system (Harbinger Hermit, Alchemist, Vicar, Mnemonist, Unstained Zealot, Acolyte, Fungal Whisper, Ancestral Communion)
- `PlaceStructurePacket` — Debug structure spawner

Direct Blood Routing adds no dedicated payload. Link state persists in `BloodRoutingSavedData`, node visuals are server-tick/particle driven, and source drains reuse existing player volume sync (`BloodVolumeServerPacket`) after transfer.

---

## 5. The Harbinger Path (Hematic Order)

The default/primary progression. The player embraces hemomancy and rises through the ranks of a secret society called **The Hematic Order** (a.k.a. "The Harbingers").

### 5.1 Blood Volume

- **Interface:** `IBloodVolume`
- **Default:** 0 current / 5,000 max, `active = false`
- Activated by clicking a Blood Temple's Mortal Display
- Blood is spent to cast manipulations and power rituals
- Can be expanded via the Capacity skill (+500 per level)
- Stored in Blood Gourds for portable use; equipped gourds receive overflow blood from valid blooded kills after the player is topped off
- Direct emergency restores (`blood_rock`, `bloody_flask`, `vitality_chalice`) apply **Blood Drunkenness** for 3 minutes, stacking to amplifier 3 and adding +15%/+30%/+45%/+60% manipulation blood cost; amplifier 3 also increases manipulation cooldowns by 25%
- Has **trickle donation** and **auto-draw** settings for Bloodline pool interaction
- Has a **blood routing opt-in** flag used by sanctum-only direct routing when a bloodline member allows their membership to authorize shared-pool machine links
- Has **Blood Debt Tracking** for the Hemorath saint encounter: `addDamage(amount)`, `addBloodSpend(amount)`, `consumeDebt()`, `getBloodDebt()`, `resetBloodDebt()` — debt accumulates from manipulation casts and direct damage during the Hemorath fight, then is collected on fight resolution

### 5.2 Initiatory Degrees

Progression through **Cardinal Rites** — multiblock blood rituals. Each rite advances the player to the next degree:

| Degree | Title | Cardinal Rite |
|--------|-------|---------------|
| 0 | Uninitiated | *(starting state)* |
| 1 | Neophyte of the Crimson Veil | `sanguine_initiation` |
| 2 | Votary of the Hematic Covenant | `votary_rite` |
| 3 | Initiate of the Scarlet Sanctum | `initiate_rite` |
| 4 | Adept of the Sanguine Brotherhood | `sanguine_brotherhood` |
| 5 | Illuminatus of the Crimson Lodge | `illuminatus_rite` |
| 6 | Sanctified of the Bloodline Covenant | `sanctified_rite` |
| 7 | Archon of the Hematic Order | `archon_rite` |
| 8 | Apotheos of the Hematic Order | `apotheos_rite` *(requires Qliphoth Communion — gate enforced in `BloodCraftingKeyPressPacket` before rite start and re-checked in `CardinalRiteEvents` before completion, using the player's `IInitiatoryDegree` capability)* |

Cardinal Rites have:
- A blood cost
- A rite form (`CardinalRiteType`), which controls structure size, cast duration, and boundary behavior
- An explicit `required_degree` JSON field, interpreted as Initiatory Degree for Harbinger rites or Unstained stage for Unstained rites
- A multiblock pattern
- A `rankup` boolean on Harbinger degree-advancement rites, used by the Rites tab to highlight degree rites with a slow red/gold name glow
- An item result
- A casting duration (tick-based, tracked via `ActiveCardinalRite`)
- Boundary enforcement (player must stay in range)
- Unwilling sacrifice processing

### 5.3 Cardinal Rite Casting Flow

Managed by `CardinalRiteEvents`:
1. Player initiates the rite at the correct multiblock
2. An `ActiveCardinalRite` is created, tracking the caster UUID, center position, recipe, duration, and rite size
3. Each tick: particles spawn, boundary checked, sacrifices processed
4. On completion: degree awarded, Unstained progress reset (if any), chat message sent

`BloodCraftingKeyPressPacket` validates the explicit `required_degree` before activation through `RecipeDegreeGates`, then performs the rank-up redundancy check. If a rite's `rankup` flag is true and the caster is already at or above the rank it grants, the server refuses to start the rite so players do not spend materials or time on redundant degree-up rituals. `CardinalRiteEvents` re-checks the same gate before completion so saved/active rites cannot finish after a player loses access. The same packet also accepts structure-spawner-placed rite structures by scanning the matched multiblock pattern rather than assuming the clicked block is the rite origin.

### 5.4 Harbinger NPC Dialogue System

Four Harbinger NPC types provide lore and gameplay hints through the `DialogueTree` framework. All dialogue trees are fully implemented and degree-gated.

**Harbinger Hermit** (`HarbingerHermitDialogueTrees`) — one-of-a-kind NPC found at the starting Blood Temple. Acts as the player's first guide.

| Degree State | Content |
|---|---|
| No blood (pre-initiation) | Offers lore about the Mortal Display, explains his duty as eternal keeper, presents the option to claim the heart and begin hemomancy |
| Degree 0 (uninitiated) | Congratulates the player, offers guidance about the Rite of Sanguine Initiation, drops the Rite Hint item on farewell (triggering `hermit_farewell_die` → kills the hermit) |
| Degree 1 Neophyte | Acknowledges first step; hints toward Votary Rite and manipulation lore |
| Degree 2 Votary | Guidance on blood tendencies and the Somatic Loom; hints toward Scarlet Sanctum |
| Degree 3 Initiate | Points toward Sanguine Brotherhood rite |
| Degree 4 Adept | **Scar lore branch** — explains scars as literal mind-maps of new venous/neural pathways, Cerebral Scarring Station usage; hints toward Crimson Lodge |
| Degree 5 Illuminatus | Reveals Bloodline Covenant system; hints toward Bloodline Covenant rite |
| Degree 6 Sanctified | Final hint — points toward the Rite of the Hematic Order |
| Degree 7 Archon | Kneels before the player: "Archon of the Hematic Order. You are the blood incarnate." |
| Degree 8 Apotheos | Speechless reverence; final words from the keeper who was never given instructions beyond the seventh degree |

**Harbinger Alchemist** (`HarbingerAlchemistDialogueTrees`) — found at Harbinger Outposts. Focuses on machines and crafting systems; dismisses purifying players coldly.

| Degree | Content |
|---|---|
| Uninitiated | Politely refuses: machines require initiation |
| Neophyte | Introduces the Ghastly Alembic; overview of the Outpost machine chain |
| Votary | Explains the Vial Centrifuge and blood tendency separation; **introduces Blood Structure crafting** (recipes unlock by explicit degree/stage gates) |
| Initiate | Reveals the Somatic Loom and explains memory weaving |
| Adept | Introduces the Cerebral Scarring Station (surgical instrument) and Chisel Station (rune encoding) |
| Illuminatus | Reveals higher-degree Blood Structure patterns, including conduit-scale machinery and Morphling Incubator lore |
| Sanctified | Describes the "final synthesis" — all machines as one unified process |
| Archon | Defers to the player's mastery; "I have nothing left to teach" |
| Apotheos | Awe and vertigo: "I built machines to process blood. The machines were always pointing at something. I understand now." Reflects that the player was the product the machines were building toward |
| Purifying | Cold dismissal: "I have no time to teach someone who won't make use of my knowledge" |
| Clarity | Ignores the player entirely |

**Harbinger Vicar** (`HarbingerVicarDialogueTrees`) — found at Harbinger Outposts. Keeper of faction history and doctrine; delivers gravitas and hidden truths at high degrees.

| Degree | Lore Branch |
|---|---|
| Uninitiated | Who the Harbingers are; purpose of the Outpost |
| Neophyte | The Hematic Covenant as a body of rites/wisdom; Votary degree hints |
| Votary | Seven blood tendencies (Fungal, Umbral, Incandescent, Ferric, Vivacious, Ruinous, Neurotic) and their role |
| Initiate | History of the **Scarlet Sanctum**, founded by Archon Erythravane in the Second Age; **Saints lore branch** — directs player toward Trial Chambers and Hallowed Residuum extraction |
| Adept | History of the **Sanguine Brotherhood** — shared blood pools born from war necessity |
| Illuminatus | The **Crimson Lodge**: documented the link between hemomancy and the mycelial network; kept secret to be "arrived at independently"; **Founding Sanctum branch** — explains Sanguine Quintessence and sanctum consecration; degree hint toward Bloodline Covenant rite |
| Sanctified | The **Hematic Order** as a state of being, not a rank; the blood "becomes indistinguishable from the blood of the world"; degree hint toward Archon rite |
| Archon | Hidden lore: *"The Hematic Order never had seven degrees. There have always been eight. The eighth degree is silence."* |
| Apotheos | Speechless reverence; the Covenant was "always meant to be outlived — it is a ladder; what you have become is what was always at the top of it" |
| Purifying | Stern warning; grieves the loss of blood power; urges return before path completes |

**Harbinger Mnemonist** (`HarbingerMnemonistDialogueTrees`) - found at Harbinger Outposts. Patient, quietly unsettling mentor for blood-memory practice; focuses on crude memories, active manipulation slots, the Mnemonic Reliquary, and Somatic Loom memory weaving.

| Degree / State | Content |
|---|---|
| Degree 0 / uninitiated | Explains blood-memory as inheritance, but refuses practical manipulation teaching before Sanguine Initiation |
| Degree 1 Neophyte | Explains crude memories, auto-equipped starter practices, and active manipulation slots; offers a one-time starter crude memory choice if eligible |
| Degree 2 Votary | Introduces the Mnemonic Reliquary as deliberate loadout management |
| Degree 3+ Initiate and above | Explains the Somatic Loom as a move from scraped echoes to deliberate weaving, and clarifies rank-gated full memories |
| Degree 5+ Illuminatus and above | Includes normal Harbinger recruit/expel options when the player has a valid bloodline |
| Purifying / Clarity | Will answer questions, but does not offer the starter crude-memory reward |

The one-time Mnemonist starter reward is tracked in persistent player data as `hemomancy.mnemonist_starter_memory_claimed`. Eligible Degree 1+ Harbingers choose exactly one item from `crude_memory_blood_shot`, `crude_memory_blood_rush`, or `crude_memory_deadly_gaze`. The reward is an item, not direct learning; using the crude memory shard performs the actual teach/auto-equip flow. If the player already knows the chosen manipulation or the grant fails, the claimed flag is not set. Dialogue event IDs are `mnemonist_grant_crude_blood_shot`, `mnemonist_grant_crude_blood_rush`, and `mnemonist_grant_crude_deadly_gaze`.

**Item inquiry dialogue:** Detailed "ask about held item" responses are now data-driven through `ItemInquiryLoader`. Files live under `data/hemomancy/dialogue_inquiry/<npc_id>/<item_namespace>/<item_path>.json`; supported NPC IDs currently include `alchemist`, `vicar`, `mnemonist`, `zealot`, and `guardian`. Entries may be simple line lists or ordered conditional branches using `min_degree`, `max_degree`, `min_purity`, and `max_purity`. Current authored count: 137 inquiry entries, including Mnemonist entries for all crude memories, key full memories, the Mnemonic Reliquary, and the Somatic Loom. The dialogue UI also uses dedicated 48x48 portrait textures for Harbinger and Unstained NPCs (`*_portrait.png`) instead of deriving portraits from the full entity texture.

### 5.5 Fungal Whisper Events

At higher degrees (4–7), the ancient fungal consciousness begins intruding into the player's mind. These are delivered via the `FungalWhisperDialogueTrees` + `FungalWhisperEvents` system — pop-up dialogues from the anonymous `???` speaker with the FUNGAL dialogue theme.

| Degree | Tone | Key Revelations |
|---|---|---|
| 4 Adept | Subliminal / barely perceptible | Itching blood, earthy smell, world-filaments flashing briefly — seeds of doubt only |
| 5 Illuminatus | Clearer intrusions | *"The blood you command... it was not always blood."* Hints of spores, hyphae beneath the surface; the crimson tide was a forest once |
| 6 Sanctified | Direct fungal revelations | *"The first Archons did not discover hemomancy. They were infected by it."* Erythromycelium as original organism; hemomancers as fruiting bodies of one mycelial web |
| 7 Archon | Full truth | *"You have reached the apex of what the infection permits."* The Hematic Order as a reproductive strategy; each degree a stage of sporulation; *"There is no Hematic Order. There never was."* |

Each degree has 3 variant whispers (indices 0–2) for variety. Some variants include branching "What was that?" / "Who are you?" follow-up nodes. A `whisper_truth_acknowledged` event fires when the Archon-tier truth is accepted.

At Archon (Degree 7), **3–5 Fungal Whispers** fire before the Fungal Spine event triggers. The whispers are somewhat disabled during development to allow testing.

### 5.6 The Fungal Spine and The Realm Beyond

After completing Qliphoth Communion by eating all nine pomes from a single bloom and then completing the Rite of Apotheos, a **Fungal Spine** item tears free from the player's back and drops into the world. Using it transports the player's consciousness to the Fungal Dimension.

**The Fungal Dimension:**
- A vast sphere of flesh, meat, and pulsing biology — the local "surface" of the fourth-dimensional Fungal Entity
- Enormous hyphae tendrils arc into the sky; bulb-nodes at their tips eventually break off like spores falling back toward the world
- The world and moon are visible in the sky, suggesting the space exists just above Earth
- The player has no physical body here — they are an astral/consciousness projection
- Everything here is hostile, even to an Archon
- Fungal Whispers occur almost constantly, nearly harassing in frequency
- The player keeps their Fungal Spine and can use it to return to the overworld
- Digging to the bottom of the space and "puncturing" the core severs the connection temporarily (ejecting the player)
- May contain **morphic pools** or podiums as place-based anchors, but the portable **Fungal Spine** is the primary player-owned travel key. See §5.9 for the Archon choice fork behaviour.

**Player Choice at the End:**
- Stay silent and simply return; remain an Archon and tell no one — choice stamped as `hemomancy:archon_choice_made = "silent"` in persistent data
- Continue deeper into the eldritch truth toward the true 8th Degree (transcendence) — choice stamped as `hemomancy:archon_choice_made = "apotheos"`; `apotheos_rite` is now unblocked in combination with the Qliphoth Communion flag
- The Archon may draw a Fungal Spine at any time to return or revisit; the podium delegates to the same helper but is no longer the core dependency

> **Status: Partial.** Spawn placement, dimension-exclusive mob population, safe return placement, and the Archon first-exit choice fork are implemented. `FungalPodiumBlock.use()` fires `FungalWhisperDialogueTrees.coreWitnessDialogue()` on the first Degree-7 exit attempt, stamps `hemomancy:archon_choice_made`, and then delegates to `performReturnTravel()`. Remaining WIP is terrain feature population depth and broader dimension content.

### 5.7 The Founding Sanctum (Degree 5)

At **Degree 5 (Illuminatus)**, a Harbinger can perform a founding rite that consecrates an area around their chosen base as a **Harbinger Sanctum**.

- Covers a **5×5 chunk area** centered on the founding location
- All Harbingers present in the sanctum receive enhanced effects: stronger regeneration, lower cooldowns, more potent blood manipulations
- Intended to encourage collective settlement and cooperative play
- A crafting material called **Quintessence** is granted by the Illuminatus rite and is required for the founding ritual

> **Status: Partial.** Buff application logic is functional (`FoundingSanctumEvents` applies Damage Boost, Regeneration, and Damage Resistance to qualifying players within the sanctum radius). The Sanguine Quintessence item is registered, produced by the Exsanguination cardinal rite, and required as a placed catalyst at the sanctum heart. Sanctum locations persist through `FoundingSanctumSavedData`, and Blood Moon boundary sealing is wired. Remaining WIP is boundary detection confirmation and full gameplay tuning.

### 5.8 The Saints System (Degree 3–4)

At around **Degree 3–4**, the Harbinger Vicar and/or the player's own research direct them to seek out **ancient Saints** — Hemomancers from the world's deep past whose power grew so extraordinary that they were entombed rather than buried.

**Structure:** Each saint has a **Trial Chamber** structure containing:
1. An entrance with a locking mechanism that seals the player inside upon entry
2. The trial itself — a unique puzzle/survival challenge (blood slowly sapped throughout)
3. A gateway that opens only upon trial completion
4. An inner sarcophagus chamber with the saint's corpse

**Extraction vs. Combat:**
- Saint Sarcophagi persist their saint type, corpus state, extraction attempts, and whether a peaceful sample was already yielded.
- If the player's **dominant blood tendency** matches the saint's thematic affinity → peaceful Consecrated Syringe extraction.
- If tendencies do not align → the saint rejects the player and awakens, beginning the saint-specific boss fight.
- Smearing **Foul Paste** on a sarcophagus deliberately triggers that saint's boss fight even after peaceful extraction.
- Boss victories yield the matching Hallowed Residuum directly; peaceful samples become Hallowed Residuum through the Vial Centrifuge.

**Reward (two output types):** Each saint yields two things from their blood:
1. **Hallowed Residuum** (`hallowed_residuum_<saint>`) — extracted by processing a Consecrated Syringe in the Vial Centrifuge. Serves as the catalyst currency for the Somatic Loom's Canon Memory recipes.
2. **Canon Memory** (via Somatic Loom) — placing the Hallowed Residuum as the loom's catalyst and aligning the loom's tendencies to match the saint's pair unlocks that saint's unique SUMMA-rank blood manipulation.

**Saint → Canon Memory → Fungal Scar Family (at-a-glance):**

| Saint | Tendencies | Loom Recipe (mortem true etc.) | Canon Memory | Related Fungal Scar |
|-------|-----------|-------------------------------|--------------|----------------------|
| **Hemorath** | MORTEM + ANIMUS | `mortem: true`, `animus: true` + Residuum of Hemorath | Crimson Tithe | Talaromyces Minus |
| **Seraphae** | LUX + DUCTILIS | `lux: true`, `ductilis: true` + Residuum of Seraphae | Unclosing Eye | Noctifly Agaric / Anastocordyceps nexus / Antiphonomyces resonans |
| **Putriciel** | MORTEM + FLAMMEUS | `mortem: true`, `flammeus: true` + Residuum of Putriciel | Bloom of Rot | Respergillus / Sanguiflora cadens / Saprovitta vestigium |
| **Velorum** | CONGEATIO + TENEBRIS | `congeatio: true`, `tenebris: true` + Residuum of Velorum | Endless Hour | Lumina Devorans / Thanomyces resurgens |

> The older saint-residuum + vanilla-catalyst incubator recipes for fungal scars have been replaced by Mycelial Crucible cultivation recipes. Hallowed Residuum still matters for Canon Memories and Saint rewards; scar growth now keys off the recipe tendency, blood cost, and aligned enzymes.

There are **four Saints** in total; which one a player encounters first is partially randomized.

#### Known Saints

| Saint | Trial Type | Boss Mechanic | Thematic Tendency |
|-------|-----------|---------------|------------------|
| **Hemorath** | Four-basin blood-filling puzzle (fill each basin to correct level, monsters spawn throughout; wrong levels set you back) | Hybrid blood-debt/overload fight. Blood magic spent near the active fight increases the player's debt while also feeding Hemorath's absorbed-blood meter; enough absorbed blood triggers an exsanguination collapse and awards Hallowed Residuum. | MORTEM + ANIMUS (iron permanence, death/life) |
| **Seraphae, the Chain Saint** | Light/containment trial room (WIP) | `SeraphaeEntity`: containment integrity mechanic. Fragments, anchors, and CONDENSING hits increase integrity until Seraphae's bound radiance is chained again; the fight is containment, not execution. | LUX + DUCTILIS (witness, light, neural) |
| **Putriciel** | Absolution-window victory condition — players must deal damage during brief periodic absolution cycles (opens every 300 ticks, lasts 80 ticks); requires 5 successful absolution hits. Rot nova pulses Wither+fire to the whole arena. | `PutricielEntity`: `DATA_ABSOLVED` synched flag; `openAbsolutionWindow()` / `endAbsolutionWindow()` cycle; `hurt()` increments absolution counter during window; ordinary lethal damage outside the intended condition is clamped so the reward cannot be bypassed. | MORTEM + FLAMMEUS (absolution, rot-fire) |
| **Velorum** | Martyrdom resistance — gains brief Resistance I on every hit, creating attack-rhythm windows. Frost nova roots players. Veil of darkness blinds (Nausea for blood-active players). Silence drain strips blood from nearby Harbingers at low HP (≤25%). | `VelorumEntity`: `DATA_MARTYRDOM` synched flag; `fireFrostNova()`, `fireVeilOfDarkness()`, `fireSilenceDrain()` per-tick methods; martyrdom Resistance in `hurt()`, with the synced martyrdom visual/state cleared after the resistance window expires. | CONGEATIO + TENEBRIS (martyrdom, silence, frozen dark) |

> **Status: Partial.** The shared sarcophagus encounter spine is implemented for all four saints: peaceful aligned extraction, unaligned rejection/awakening, Foul Paste forced awakening, saint-specific boss dispatch, Consecrated Syringe tagging, and direct boss residuum rewards. Hemorath's basin/altar/gate trial remains the first complete trial flow. Seraphae, Putriciel, and Velorum have boss AI implemented and registered, but bespoke Trial Chamber rooms, world placement tuning, models/textures/GeckoLib animations, and final balance are still WIP.

### 5.9 Qliphoth Communion (Degree 7 → 8 Prerequisites)

Qliphoth Communion is the multi-step prerequisite chain that unlocks the Rite of Apotheos. It is **fully implemented**. The five stages are:

**Stage 1 — Monolith Shatter**
An Archon (Degree 7) interacts with their **Sanguine Monolith** twice (`SHATTER_INTERACTION_THRESHOLD = 2`). On the second interaction the monolith explodes, drops a **Qliphoth Seed** (`hemomancy:qliphoth_seed`), and fires `FungalWhisperDialogueTrees.postMonolithShatter()` — the Entity comments on what was hidden inside. Clients receive `SpawnMonolithShatterBurstPacket`; `SanguineMonolithShatterRenderer` renders black triangular shards plus a fast black core/shell orb blast from the monolith center.

**Stage 2 — Bloom of the Qliphoth Rite**
The player places the Qliphoth Seed as a catalyst item within the multiblock pattern of the **Bloom of the Qliphoth** cardinal rite (Degree 7 Grand rite, blood cost 1200, uses `nether_wart_block`, `soul_soil`, `blood_wood_log`, `polished_venous_stone`, and `engram_block` as pattern blocks). The rite consumes the seed. On completion `CardinalRiteEvents.completeBloomOfQliphoth()`:
- Places a `QliphothBloomBlock` (1×1×8 multiblock) at the rite center
- Registers the bloom in `QliphothBloomSavedData` (overworld SavedData) with owner UUID, center position, dimension, and 3-chunk radius
- Fires `FungalWhisperDialogueTrees.postBloom()`

The bloom and its invisible filler shell are protected from ordinary player breaking. `QliphothBloomEvents` cancels break attempts against the bloom or any filler attached to it, and `FillerBlock` does not forward filler removal into destroying a Qliphoth Bloom. Intentional cleanup is via the Rite of Cult Pruning.

**Stage 3 — Qliphoth Pome Drops (and Tree Growth)**
`QliphothBloomEvents.onLevelTick()` runs every 40 ticks. Each tick it may attempt `trySpawnPome()` for each bloom (1-in-80 chance). Each pome is tagged:
- `hemomancy:bloom_origin` (Long) — bloom center as `BlockPos.asLong()`
- `hemomancy:husk_index` (Int, 0–8) — ordinal index of the nine Qliphoth husks

The nine husks in order: *Nahemoth, Samael, Gamaliel, Harab Serapel, Golachab, Thagirion, A'arab Zaraq, Satariel, Ghagiel*. Each drop fires `FungalWhisperDialogueTrees.pomeDropped(huskIndex, offerMemo)` to the online bloom owner even if the Qliphoth Communion memo is already known; the memo capture option is only offered when appropriate. Pomes are invulnerable (fire/lava/void) and never despawn (`lifespan = Integer.MAX_VALUE`). A bloom produces exactly 9 pomes then ceases (`MAX_POMES_PER_BLOOM = 9` in `QliphothBloomSavedData`).

After each `incrementPomesDropped()` call, `CardinalRiteEvents.syncQliphothBlooms()` is called so the client receives the updated `pomesDropped` count and can advance the tree's visual growth stage. The tree progresses through 9 visual stages tied to the pome count (see rendering below).

**Tree Visual Growth Stages**

The `QliphothBloomRenderer` reads `bloom.getPomesDropped()` and passes it as a `stage` integer into each draw method. Stage helpers compute per-component fractions:

| Pomes Dropped (stage) | Trunk height | Root length | Branches | Sub-branches | Canopy floaters | Apex black-hole orb |
|---|---|---|---|---|---|---|
| 0 | 25% | 15% | — | — | — | — |
| 1 | 40% | 36% | — | — | — | — |
| 2 | 55% | 57% | — | — | — | — |
| 3 | 70% | 79% | — | — | — | — |
| 4 | 85% | 100% | — | — | — | — |
| 5 | 100% | 100% | — | — | — | — |
| 6 | 100% | 100% | 40% length | — | — | — |
| 7 | 100% | 100% | 70% length | ✓ | — | — |
| 8 | 100% | 100% | 100% | ✓ | ✓ | — |
| 9 | 100% | 100% | 100% | ✓ | ✓ | ✓ |

Implementation: `trunkHeightFrac(stage)`, `rootLengthFrac(stage)`, `branchLengthFrac(stage)` in `QliphothBloomRenderer`. The `pomesDropped` count is stored in `QliphothBloomClientData.BloomEntry` and synced via `PacketSyncQliphothBlooms`.

**Stage 4 — Qliphoth Communion Achieved**
`QliphothPomeItem.trackCommunionProgress()` tracks per-bloom consumption in the player's `IInitiatoryDegree` capability (`pome_communion_progress`, keyed by bloom origin Long). When the ninth pome from a single bloom is consumed:
- `IInitiatoryDegree#setQliphothCommunionDone(true)` is set on the player
- `FungalWhisperDialogueTrees.qliphothCommunion()` fires the nine-shell completion whisper
- HUD pome progress is immediately synced with `PacketSyncPomeProgress`

Creative-spawned / untagged pomes do not have a real bloom origin, so they use a synthetic test origin and still advance the same capability path. Their husk message is inferred from the player's current total pome count before consumption, so creative testing still displays the correct `[huskname]` message for the next pome in order.

**Stage 5 — Rite of Apotheos Unlocked**
`BloodCraftingKeyPressPacket` (server-side rite activation) checks `IInitiatoryDegree#isQliphothCommunionDone()` before allowing the `apotheos_rite` to begin. `CardinalRiteEvents.completeRite()` repeats the same check before granting Degree 8, so old active rites or alternate completion paths cannot bypass the gate. If absent, the player receives: *"The Eighth Degree remains sealed. Consume all nine Qliphoth husks from a single bloom."* If present (and degree ≥ 7), the rite proceeds normally.

When degree rites actually advance the player to Degrees 5, 6, and 7, `FungalWhisperDialogueTrees.spineGrowth(degree)` fires one-shot bodily hints that the Fungal Spine is growing. On successful Degree 8 advancement, `CardinalRiteEvents` plays wet flesh sounds, drops `fungal_spine` behind the player, and opens `FungalWhisperDialogueTrees.fungalSpineEmerged()` with usage guidance.

**Key fields serialized inside the player's `IInitiatoryDegree` capability:**

| Key | Type | Meaning |
|-----|------|---------|
| `pome_communion_done` | Boolean | Communion completed; Apotheos rite now accessible |
| `pome_communion_progress` | CompoundTag | Per-bloom pome consumption counters (keys = bloom origin Long as String) |
| `pome_empowerment_expiry` | Long | Game-time tick when pome manipulation discount expires (0 = none) |
| `pome_total_consumed` | Int | Total pome counter for HUD display, capped at 9 |
| `hemomancy:archon_choice_made` | String | `"silent"` or `"apotheos"` — set when Archon resolves the Fungal Dimension choice fork |

---

## 6. The Unstained Path (Anti-Hemomancy)

The divergent/opposing path. The player abandons blood magic in pursuit of purification and enlightenment, guided by **Unstained Zealot** NPCs and the silent patronage of **Our Lady of Still Waters**.

### 6.1 Our Lady of Still Waters — Patron of the Unstained

The Unstained revere a mysterious figure known only as **Our Lady of Still Waters** (sometimes whispered as *"The Lady of the Forgotten Waters"* or *"She Who Absolves"*). She is described in Unstained scripture as:

- A **tall woman** with **white hair** that cascades like flowing water
- Clad in **white robes** that shimmer faintly with silver thread
- Eyes of **liquid silver** that see through all deception and corruption
- Skin of **pale blue**, as though touched by the still waters

Her origins are unknown — some Unstained texts suggest she is a being from before the fungal infection that birthed hemomancy, a guardian spirit of purity who was driven into hiding when blood magic first took root. Others believe she is a manifestation of the world's immune response to the alien fungus, a living antibody in humanoid form.

**Connection to the Lethean Poppies:**
The **Lethean Poppies** that grow across the world are said to bloom wherever Our Lady once walked. The dew they produce — **Lethean Dew** — carries her essence: the power of forgetting. Just as the mythological River Lethe washed away memories, the lethean poppies help players *forget* their blood manipulations, severing the ties that bind them to hemomancy. The Unstained believe that harvesting and refining these poppies is a sacred act of devotion to their patron.

**Tears of Silthmere** are distilled from Lethean Dew at an Altar of Cleansing, concentrating Our Lady's blessing into a single potent draught. When offered at her altar, these tears trigger a powerful purification — a one-time gift from the Lady herself.

**The Pallid Icon** is an exceedingly rare relic depicting Our Lady, said to have been carved by the first Unstained from pale silver found at the bottom of a forgotten river. Those who possess it are considered to be under her direct protection.

### 6.2 Unstained NPC Dialogue System

Two Unstained NPC types guide the player through the purification journey. All dialogue trees are fully implemented.

**Unstained Zealot** (`ZealotDialogueTrees`) — recruiter; the NPC who first offers the path.

| Player State | Dialogue Branch |
|---|---|
| No blood at all | Dismisses gently: "You bear no mark of the crimson arts." |
| Active blood, Degree 0-1 | Full plea with concerned sadness: the Church treats early infection as something that can still be healed |
| Active blood, Degree 2-4 | Full plea with increasing caution: the Zealot still offers help, but questions the player's motives and attachment to the Covenant |
| Active blood, Degree 5 (Illuminatus) | Full plea with hesitant disdain: aid is still offered, but the Church assumes the request may be manipulation or regret arriving late |
| Active blood, Degree 6+ | Refuses recruitment; the Church believes the stain has rooted too deeply for ordinary hemolytic cure |
| Active blood, Degree 0-5 (plea) | Explains the hemolytic rites, offers craft-hemolytic info branch, `zealot_accept_purification` / `zealot_accept_church` / `zealot_reject_help` outcomes |
| Already on purification path — Corrupted | "Continue your work at the podium, and the stain shall lift." |
| Purity 25–49 (Tainted) | Silver Ward info branch |
| Purity 50–74 (Cleansing) | Altar of Cleansing info branch |
| Purity 75–99 (Absolved) | Clarity Rite info branch |
| Clarity unlocked | Verdigris info branch |
| Enlightened | Final reverence: the journey complete |

**Unstained Acolyte** (`AcolyteDialogueTrees`) — found at Unstained temples; provides stage-aware guidance and tasks.

| Stage | Dialogue Content |
|---|---|
| Not on path | Gentle introduction to the Unstained way; "Who are you?" branch |
| Corrupted (0–24) | Explains how to purify; gives task: gather Ghost Pipe (`acolyte_task_gather_ghost_pipe`) |
| Tainted (25–49) | **Our Lady of Still Waters lore** (3 lines about the Lady's nature); tasks: wreath offering / hemolytic offering |
| Cleansing (50–74) | **Silver Veil lore** (inner layer of purity shielding the soul); task: consecration |
| Absolved (75–99) | Explains the Clarity path (3-line clarity branch) |
| Purified (100, pre-Clarity) | Explains how to unlock Clarity |
| Clarity phase | Verdigris lore; task: chalice offering (`acolyte_task_chalice`) |
| Enlightened | Ultimate reverence: "The Lady weeps for joy." |

### 6.3 Entry Requirements

- Standard path: the player finds a Blood Temple, activates blood control, then later finds an Unstained Church seeking cure or healing
- Unstained Zealots offer purification to blood-active players from **Degree 0 through Degree 5 (Illuminatus)**, with tone shifting from concerned sadness to wary disdain as degree rises
- **Degree 6+** Harbingers are not accepted by the normal Church route; the Church treats them as too deeply rooted in the blood-memory infection for ordinary cure
- The Zealot directs the player to bring **Hemolytic Solution** ![Hemolytic Solution](../src/main/resources/assets/hemomancy/textures/item/hemolytic_solution.png) to an **Unstained Podium** block

### 6.4 Phase 1: Purity (0–100)

Initiated by using Hemolytic Solution at the Unstained Podium:
- Sets `begunPurification = true`, grants 5.0 starting purity
- **Resets Harbinger degree to 0**

As purity rises, blood magic becomes increasingly penalized:

| Stage | Purity ≥ | Blood Magic Penalty |
|-------|----------|---------------------|
| Corrupted | 0 | None (1.0× cost) |
| Tainted | 25 | +10% cost (1.10×) |
| Cleansing | 50 | +25% cost (1.25×) |
| Absolved | 75 | +50% cost (1.50×) |
| Purified | 100 | **Completely blocked** |

- **Silver Ward** resistance scales linearly: `purity / 100`

#### Purity Sources

**Combat:**

| Source | Purity Gained | Condition |
|--------|---------------|-----------|
| **Killing a Hemomancy mob** | +2.0 | Any entity tagged `hemomancy_mob` |
| **Killing an Undead mob** | +0.5 | Any MobType.UNDEAD (zombies, skeletons, phantoms, etc.) |
| **Killing a Hostile mob** | +0.25 | Any other MobCategory.MONSTER |
| **Flawless kill bonus** | +0.5 extra | Added to any kill reward if player hasn't taken damage in last 5 seconds |

**Survival & Exploration:**

| Source | Purity Gained | Condition |
|--------|---------------|-----------|
| **Hemolytic Solution on Podium** | +10.0 | Each use (first use grants +5.0 and begins path) |
| **Tears of Silthmere on Altar of Cleansing** | +25.0 | One-time blessing from Our Lady of Still Waters |
| **Lethean Poppy Wreath on Altar** | +5.0 | Repeatable offering at the Altar of Cleansing |
| **Completing an Advancement** | +1.5 | Any advancement (boss kills, exploration, progression) |
| **Picking up XP orbs** | +0.1 | Requires active **Hemolysis** effect |
| **Sleeping through the night** | +3.0 | Requires active **Hemolysis** effect; natural wake only |
| **Hemolysis effect tick** | +0.01/tick | Passive gain while Hemolysis is active (very slow) |

**Farming & Mercy:**

| Source | Purity Gained | Condition |
|--------|---------------|-----------|
| **Breeding animals** | +0.3 | Creating life — any successful breeding |
| **Planting crops/saplings/flowers** | +0.05 | Placing a block tagged as crop, sapling, or flower |
| **Healing a tamed animal** | +0.2 | When a tamed pet heals (feeding a wolf, etc.) |
| **Natural self-healing (no blood)** | +0.1 | Healing 2+ hearts with blood volume empty or inactive |

**Restraint & Discipline:**

| Source | Purity Gained | Condition |
|--------|---------------|-----------|
| **Blood magic abstinence** | +0.5 | Every 5 minutes without using any blood manipulation |
| **Empty blood renunciation** | +0.15/min | Blood volume is zero or inactive (abandoned hemomancy) |

*Kill rewards are not gated by Hemolysis — any player on the Unstained path earns purity from kills. XP and sleep rewards require the Hemolysis effect. Abstinence timer resets whenever a blood manipulation is used.*

### 6.5 Phase 2: Clarity (0–100)

Unlocked after reaching Purified (purity = 100) and using **Consecrated Copper** at the Unstained Podium:
- Sets `clarityUnlocked = true`
- **Permanently disables blood magic** (`BloodVolume.active = false`)

| Stage | Clarity ≥ |
|-------|-----------|
| Awakened | 0 |
| Discerning | 25 |
| Vigilant | 50 |
| Resolute | 75 |
| Enlightened | 100 |

- **Verdigris Aura** (anti-blood field) scales linearly: `clarity / 100`
- Reaching 100 clarity = **Enlightenment**, the final state

### 6.6 Unstained Progression Level (`getPlayerUnstainedLevel`)

To gate Unstained cardinal rites the same way Harbinger degree gates Harbinger rites, the full purity → clarity path is divided into **8 numbered levels** by `HemoCapabilityAccess.getPlayerUnstainedLevel(Player)`:

| Level | Milestone | Condition |
|-------|-----------|-----------|
| 0 | Not begun | `!hasBegunPurification()` |
| 1 | Begun | `hasBegunPurification()`, purity < 25 |
| 2 | Tainted | purity ≥ 25 |
| 3 | Cleansing | purity ≥ 50 |
| 4 | Absolved | purity ≥ 75 |
| 5 | Purified | `isPurified()` (purity ≥ 100) |
| 6 | Discerning | clarity ≥ 25 |
| 7 | Vigilant | clarity ≥ 50 |
| 8 | Enlightened | `isEnlightened()` (clarity ≥ 100) |

These levels are compared against each Unstained recipe's explicit `required_degree` value through `RecipeDegreeGates`. The field intentionally mirrors Harbinger degree gates for shared tooling, but Unstained recipes are cataloged separately from Harbinger Blood Structure and Cardinal Rite recipes in §15.

---

## 7. Mutual Exclusion of Paths

The two paths are **mutually exclusive**. Resets are handled by `PathMutualExclusionHelper`:
- **Starting Unstained** (Hemolytic Solution at podium) → resets Harbinger degree to 0, resets Pome Communion
- **Completing a Harbinger degree rite** → resets all Unstained progress (purity → 0, clarity → 0, clarityUnlocked → false, begunPurification → false, **all KnownStillArts cleared**)
- **Unlocking Clarity** (Consecrated Copper at podium) → `enforceHarbingerResetOnClarity()` is called; if any Harbinger degree was held, it is stripped at the moment clarity is confirmed
- Message: *"Your purification has been undone by the blood rite."* / *"The Hematic Order falls silent within you."*

---

## 8. Blood Manipulations

Blood manipulations are abilities fueled by blood. Lore-wise, they are dormant memories everyone has access to. They can now be learned through two paths: crude memory shards for early, weak starter echoes, and full **Blood Memory** items for the refined midgame-and-later memory-weaving path.

### 8.1 Manipulation Properties

Each manipulation has:
- **Name** — registry ID
- **Blood cost** — drained from the player's blood volume (modified by Efficiency skill, purity penalty, and Blood Drunkenness)
- **XP cost** — additional experience cost
- **Alignment level** — required tendency alignment
- **Type** — `QUICK`, `CHARGED`, `PASSIVE`, or `CONTINUOUS`
- **Rank** — `HUMILIS`, `MEDIOCRITAS`, `SUMMA`, `MAGISTER`, `PERFECTUS`
- **Tendency** — which of the 8 blood tendencies it belongs to
- **Vein Section** — which vein section takes strain when cast
- **Cooldown** — tick-based cooldown between uses
- **ManipLevel** — manipulations level up with use

Shared degree gates for manipulation ranks are centralized in `ManipulationRankGates` and used by full Blood Memory items, crude memory shards, and the manipulation progress UI:

| Rank | Required Initiatory Degree |
|------|----------------------------|
| `HUMILIS` | 0 |
| `MEDIOCRITAS` | 1 |
| `SUMMA` | 3 |
| `MAGISTER` | 5 |
| `PERFECTUS` | 6 |

### 8.2 Registered Manipulations

`ManipulationInit` currently registers 44 blood manipulations. The catalog below tracks active registry entries and their developer-facing gameplay role.

| Name | Cost | Type | Rank | Tendency | Vein Section | Cooldown | Description |
|------|------|------|------|----------|-------------|----------|-------------|
| `venous_travel` | 1000 | Continuous | Mediocritas | Ferric | Right Arm | 20t | Teleport to saved Earthen Vein locations (vein network fast travel) |
| `blood_shot` | 100 | Quick | Humilis | Animus | Head | 10t | Fires a single tracking blood shot projectile in the look direction |
| `deadly_gaze` | 100 | Quick | Humilis | Animus | Head | 20t | Raycasts 100 blocks; launches the targeted entity upward with blood claw FX |
| `blood_needle` | 100 | Quick | Humilis | Animus | Head | 10t | Fires a spread of 10–20 blood needle projectiles with random scatter |
| `blood_rush` | 100 | Passive | Humilis | Animus | Body | 60t | Summons a Wretched Will and grants Blood Rush effect (+20% move/attack speed) |
| `blood_cloud` | 300 | Quick | Summa | Animus | Head | 40t | Launches a Blood Cloud Carrier projectile that deploys an AoE blood cloud |
| `blood_aneurysm` | 400 | Quick | Summa | Animus | Body | 40t | Targets nearest enemy in 10 blocks: deals 8 magic damage + launches target upward, then bursts for 3 splash damage to all entities within 4 blocks of the target. Both values scale with Crimson Mastery. |
| `vital_effusion` | 350 | Quick | Humilis | Animus | Body | 20t | Bonemeal-accelerates growable blocks near the targeted surface in a small area |
| `activation_potential` | 200 | Quick | Mediocritas | Ductilis | Body | 30t | AoE lightning bolt to all entities within 5 blocks, dealing 5 damage each |
| `sanguine_ward` | 10 | Continuous | Mediocritas | Ductilis | Body | 20t | Passive damage reduction shield (logic handled in ManipEvents on hurt) |
| `hemolymphal_pulse` | 400 | Quick | Humilis | Ductilis | Head | 20t | Blood-sense pulse that applies Glowing to nearby living entities for 15 seconds |
| `ferric_transmutation` | 1000 | Quick | Summa | Ferric | Body | 20t | **Sanguine Alloy** — saturates the caster's blood with ferrous compounds for 90s: grants Strength II (iron-enriched blood hits harder) + Sanguine Siphon II (accelerated blood regeneration). Memory item display name: "Memory Sanguine Alloy". |
| `conjure_blade` | 1000 | Quick (Conjuration) | Mediocritas | Ferric | Right Arm | 40t | Conjures a Living Blade into empty main hand |
| `blood_absorption` | 1000 | Quick (Conjuration) | Mediocritas | Ferric | Right Arm | 40t | Conjures a Blood Absorption tool into empty main hand |
| `blood_projection` | 1000 | Quick (Conjuration) | Mediocritas | Ferric | Right Arm | 40t | Conjures a Blood Projection launcher into empty main hand |
| `summon_avatar` | 500 | Quick | Summa | Animus | Body | 100t | Toggles the Blood Avatar form (visual transformation synced to all players) |
| `summon_thrall` | 500 | Quick | Mediocritas | Animus | Body | 60t | Two-step: (1) raycast to spawn a Blood Thrall at source block, (2) raycast again to set its destination — thrall then works autonomously |
| `crimson_flame_conjuration` | 150 | Quick | Humilis | Animus | Right Arm | 15t | Places Crimson Flames on the targeted block face (range 16, scales with Sanguine Reach) |
| `blood_lamp` | 75 | Quick | Humilis | Lux | Left Arm | 10t | Places an invisible light block (level 15) at the targeted surface (range 16, scales with Sanguine Reach) |
| `crimson_sight` | 250 | Quick | Mediocritas | Lux | Head | 60t | Grants Night Vision (60s) and applies Glowing to all mobs within 32 blocks (30s) |
| `crimson_harvest` | 200 | Quick | Humilis | Ductilis | Left Leg | 60t | Bone-meals all growable blocks in a 5×5 area around the caster |
| `hemosynthesis` | 200 | Quick | Humilis | Lux | Body | 40t | Converts blood into food — restores 4 hunger and 4.0 saturation |
| `pyretic_forge` | 350 | Quick | Mediocritas | Flammeus | Body | 30t | Smelts held items in-hand using blood heat (base 8 items, scales with Crimson Mastery) |
| `sanguine_ignition` | 125 | Quick | Humilis | Flammeus | Body | 25t | AoE fire pulse in 5-block radius: sets targets alight for 4s and deals 1 heart ignition damage |
| `vitric_combustion` | 500 | Quick | Summa | Flammeus | Body | 60t | Long-range (22 blocks) targeted blood explosion (8 hearts damage, 8s fire, knockback) — range scales with Sanguine Reach |
| `glacial_grasp` | 125 | Quick | Humilis | Congeatio | Left Arm | 20t | Freezes water in a 7×7 area into Frosted Ice (on-demand Frost Walker) |
| `cryogenic_pulse` | 150 | Quick | Humilis | Congeatio | Body | 30t | AoE cryo burst in 5-block radius: 1.5 hearts damage + Slowness III (3s) + Mining Fatigue I (4s) |
| `glacial_bastion` | 350 | Quick | Mediocritas | Congeatio | Left Arm | 50t | Projects a 3-wide × 3-high ice wall at the targeted location (range 20, scales with Sanguine Reach) |
| `glacial_circulation` | 175 | Quick | Humilis | Congeatio | Body | 100t | Chills blood for 90s: grants Fire Resistance + Slowness I. Works everywhere (unlike GlacialGrasp's water dependency). The tradeoff: fire immunity at the cost of movement speed. |
| `osseous_bloom` | 600 | Quick | Summa | Congeatio | Body | 60t | Crystallisation burst in 6-block radius: deals 25% of each target's **current** HP as freeze damage (punishes full-health targets hardest) + Slowness IV for 4s. Scales with Crimson Mastery. Best as an opener, not a finisher. |
| `sanguine_mending` | 150 | Quick | Humilis | Ferric | Right Arm | 30t | Repairs the held item by 50 durability using blood |
| `vital_reservoir` | 50 | Quick | Mediocritas | Mortem | Heart | 60t | Converts 10 XP levels into 1000 blood volume |
| `hemorrhage` | 100 | Quick | Humilis | Mortem | Right Arm | 20t | Targets the closest living entity within 8 blocks and applies Wither II (6s) |
| `exsanguinate` | 300 | Quick | Mediocritas | Mortem | Right Arm | 50t | Executes a weakened target (≤30% HP) within 10 blocks: deals 1.5× their current HP as damage and restores 600 blood to the caster |
| `void_shroud` | 100 | Quick | Humilis | Tenebris | Body | 20t | **Dash-stealth** — grants Invisibility + Speed II + Night Vision for 5 seconds. Designed as a repositioning tool; pairs with `umbral_step` (shroud first, then teleport through shadow). |
| `blood_eclipse` | 300 | Quick | Mediocritas | Tenebris | Head | 45t | Forward cone attack (18 blocks, 30° half-angle): applies Blindness II (5s) + Weakness I (6s) + 0.5 heart shadow damage |
| `sanguine_excavation` | 400 | Quick | Mediocritas | Ferric | Right Arm | 40t | Flood-fill mines a cluster of matching blocks at look target (base 9 blocks, scales with Sanguine Reach) |
| `vascular_dowsing` | 500 | Quick | Humilis | Ferric | Right Arm | 20t | Scans nearby ore blocks and reveals them to the caster with ore-colored dust particles |
| `ferric_resonance` | 600 | Quick | Mediocritas | Ferric | Right Arm | 20t | Repairs the damaged held tool or weapon by 50 durability using blood |
| `umbral_step` | 300 | Quick | Mediocritas | Tenebris | Left Leg | 40t | Teleports to the targeted block (range 24, scales with Sanguine Reach) — destination must be dark (light ≤ 7) |
| `crimson_tithe` | 400 | Quick | Summa | Mortem | Heart | 100t | **Canon Memory (Hemorath)** — stores 500 blood as a debt; if not repaid within 30s, the caster is charged double and takes 6 magic damage. Gambling mechanic: high-risk short-term power. |
| `unclosing_eye` | 350 | Quick | Summa | Lux | Head | 120t | **Canon Memory (Seraphae)** — applies Glowing to ALL living entities in 32 blocks (including the caster), strips Invisibility from any target that has it, grants Night Vision 30s. Anti-stealth weapon; total mutual exposure. Feedback reports concealments dissolved. |
| `bloom_of_rot` | 500 | Quick | Summa | Mortem | Body | 80t | **Canon Memory (Putriciel)** — 8-block AoE: applies Wither II (10s) + Poison I (10s) + Slowness III (10s) to all entities; caster also receives Poison I (5s). |
| `endless_hour` | 600 | Quick | Summa | Congeatio | Body | 200t | **Canon Memory (Velorum)** — absorbs all incoming damage for 10s (Absorption V + Resistance IV), then repays the full accumulated damage when the effect expires. |

### 8.3 Memory Learning and Early Starter Flow

Full Blood Memory items (`BloodMemoryItem`) teach a manipulation only when the player meets that manipulation rank's shared degree gate. They remain the deliberate, refined path for advanced `HUMILIS`, remaining `MEDIOCRITAS`, `SUMMA`, saint canon memories, scar-catalyst alternatives, and later ranks.

Crude memory shards (`CrudeMemoryShardItem`) are the early "scraped echo" path. They do not require the Mnemonic Reliquary: using one teaches the manipulation, auto-equips it into the first empty equipped-manipulation slot, and syncs `KnownManipulationServerPacket`. If all active slots are full, the crude memory still teaches the manipulation but never replaces the player's existing loadout.

Sanguine Initiation now grants the two core utility manipulations at Degree 1 and auto-equips them if slots are open:

| Manipulation | Role |
|--------------|------|
| `blood_absorption` | Utility tool conjuration for early blood collection |
| `blood_projection` | Utility launcher/projection practice |

Existing Degree 1+ Harbinger saves are backfilled on login if they lack either utility manipulation.

Current crude memory shard items:

| Item | Manipulation | Lane |
|------|--------------|------|
| `crude_memory_blood_shot` | `blood_shot` | Starter Animus |
| `crude_memory_blood_rush` | `blood_rush` | Starter Animus |
| `crude_memory_deadly_gaze` | `deadly_gaze` | Starter Animus |
| `crude_memory_crimson_harvest` | `crimson_harvest` | Ductilis starter |
| `crude_memory_sanguine_mending` | `sanguine_mending` | Ferric starter |
| `crude_memory_blood_lamp` | `blood_lamp` | Lux starter |
| `crude_memory_hemorrhage` | `hemorrhage` | Mortem starter |
| `crude_memory_glacial_grasp` | `glacial_grasp` | Congeatio starter |
| `crude_memory_sanguine_ignition` | `sanguine_ignition` | Flammeus starter |
| `crude_memory_void_shroud` | `void_shroud` | Tenebris starter |

Harbinger outpost loot now favors these crude starter memories in early danger/exploration rewards instead of over-granting full memory items. The Mnemonic Reliquary remains the Degree 2 deliberate loadout-management tool, and the Somatic Loom remains the Degree 3 refined memory-weaving station.

### 8.4 Manipulation Tree

Manipulations are organized in a visual **Manipulation Tree** (displayed on the Skill Tree screen alongside the skill tree). Entries are defined in `ManipulationTreeInit` with parent-child relationships. Each node shows whether the player has learned it.

---

## 9. Blood Tendency (Kinship) System

The player has alignment scores across **8 blood tendencies**. These represent the player's affinity with different aspects of blood magic and determine which manipulations they can effectively use.

| Tendency | Concepts | Particle Color | Enzyme Item |
|----------|----------|---------------|-------------|
| **Animus** | Life, Regen, Living tools | Red (255,0,0) | ![](../src/main/resources/assets/hemomancy/textures/item/vivacious_enzyme.png) Vivacious Enzyme |
| **Flammeus** | Fire, Heat, The Nether | Orange (255,100,0) | ![](../src/main/resources/assets/hemomancy/textures/item/fervent_enzyme.png) Fervent Enzyme |
| **Ductilis** | Lightning, Speed, Nervous Energy | Yellow (255,255,0) | ![](../src/main/resources/assets/hemomancy/textures/item/neurotic_enzyme.png) Neurotic Enzyme |
| **Lux** | Light, Flight, AOEs, Flashiness | White (255,255,255) | ![](../src/main/resources/assets/hemomancy/textures/item/incandescent_enzyme.png) Incandescent Enzyme |
| **Mortem** | Death, Decay, Withering | Dark Green (0,58,0) | ![](../src/main/resources/assets/hemomancy/textures/item/ruinous_enzyme.png) Ruinous Enzyme |
| **Congeatio** | Cold, Ice, Water | Blue (0,100,255) | ![](../src/main/resources/assets/hemomancy/textures/item/frigid_enzyme.png) Frigid Enzyme |
| **Ferric** | Iron, Barbs, Solidity, Unchanging | Gray (53,53,53) | ![](../src/main/resources/assets/hemomancy/textures/item/ferric_enzyme.png) Ferric Enzyme |
| **Tenebris** | Darkness, Stealth, The End | Purple (70,0,110) | ![](../src/main/resources/assets/hemomancy/textures/item/umbral_enzyme.png) Umbral Enzyme |

Enzymes are obtained using a **Living Syringe** on mobs (now rack-fed via **Vial Rack** storage), then processed in a **Vial Centrifuge** to extract enzymes and Hematic Iron Powder.

---

## 10. Vascular System

The player's vascular system has **7 sections** that take strain from manipulation use and damage:

| Section | Enum Value |
|---------|-----------|
| Head | `HEAD` |
| Left Arm | `LEFTARM` |
| Heart | `HEART` |
| Body | `BODY` |
| Right Arm | `RIGHTARM` |
| Left Leg | `LEFTLEG` |
| Right Leg | `RIGHTLEG` |

- Sections degrade through health states when strained (healthy → stressed → clotted → dead)
- Degraded sections apply debuffs
- Sections heal passively when the player is well-fed
- The **Vascular Status Gauge** item and **VascularViewScreen** let the player inspect their vein health

---

## 11. Skill Tree

Opened from the **Dendritic Distributor** block. Has six tabs:
- **Skills** — panning/zoomable blood skill tree with skill nodes
- **Manipulations** — panning/zoomable manipulation tree with manipulation nodes
- **Crafting** — sidebar listing blood structure recipes grouped by tier (Basic/Advanced/Expert) with degree gating (0/2/4)
- **Scars** — sidebar listing scar station recipes grouped by tier (1/2/3) with degree gating (4/4/5)
- **Rites** — sidebar listing cardinal rite recipes
- **Materials** — panning/zoomable catalogue of mod items and blocks

Skills cost **skill points** (earned from using manipulations) and many require a **minimum initiatory degree**:

| Skill | ID | Max Lvl | SP Cost | Req. Degree | Effect | Parent |
|-------|----|---------|---------|-------------|--------|--------|
| Base | 0 | 1 | — | — | Root node, unlocked by default | — |
| Capacity | 1 | 5 | 1 | — | +500 max blood volume per level | Base |
| Efficiency | 2 | 5 | 1 | — | -8% manipulation cost per level (multiplicative, ~34% at max) | Base |
| Manip Slots | 14 | 5 | 2 | 1 | +1 active manipulation slot per level | Base |
| Last Wind | 3 | 3 | 2 | 2 | +2 blood regen/tick when below 10% blood | Capacity |
| Sanguine Surge | 7 | 3 | 2 | 2 | +1 passive blood regen/tick per level | Capacity |
| Dynamic Use | 4 | 3 | 2 | 2 | +10% manipulation power when tendency matches | Efficiency |
| Hemostasis | 6 | 3 | 2 | 2 | -10% blood lost when taking damage per level | Efficiency |
| Feeding Frenzy | 5 | 3 | 3 | 3 | +25% blood gained from kills | Last Wind |
| Iron Will | 10 | 3 | 3 | 3 | 10% damage reduction per level when blood < 15% | Last Wind |
| Blood Flow | 11 | 5 | 2 | 3 | -5% manipulation cooldowns per level | Hemostasis |
| Coagulation | 12 | 3 | 3 | 4 | +15% chance to block incoming bleed effects | Hemostasis |
| Crimson Mastery | 8 | 3 | 3 | 4 | +15% manipulation damage/effectiveness per level | Dynamic Use |
| Vital Link | 9 | 3 | 4 | 5 | +10% chance to heal on dealing manipulation damage | Feeding Frenzy |
| Sanguine Reach | 13 | 3 | 3 | 5 | +15% range for ranged blood manipulations | Crimson Mastery |
| Scar Affinity | 15 | 3 | 3 | 4 | Opens the mind to cerebral scarring; +10% scar effect potency per level | Crimson Mastery |
| Scar Resonance | 16 | 3 | 3 | 4 | The bond between scar and blood deepens; +1 equippable scar slot per level | Scar Affinity |
| Scar Mastery | 17 | 3 | 4 | 5 | Scarred pathways fully colonised; scar effects last 20% longer per level | Scar Resonance |

Skill bonuses are computed in `SkillPointHelper`.

**Skill Wiring Status** (which skills are actually hooked into gameplay events):

| Skill | Wired? | Where Called |
|-------|--------|-------------|
| Capacity | ✅ Yes | `BloodVolumeEvents` — adds flat bonus to max blood volume each tick |
| Efficiency | ✅ Yes | `BloodManipulation.performAction()` — multiplies manipulation blood cost |
| Manip Slots | ✅ Yes | `KnownManipulationEvents` — expands active manipulation slot count |
| Last Wind | ✅ Yes | `BloodVolumeEvents` — passive blood regen when below 10% threshold |
| Dynamic Use | ✅ Yes | `BloodManipulation` — divides effective blood cost by multiplier when tendency matches |
| Feeding Frenzy | ✅ Yes | `BloodVolumeEvents` — multiplies blood gained from kills |
| Hemostasis | ✅ Yes | `BloodVolumeEvents` — multiplies blood drained when taking damage |
| Sanguine Surge | ✅ Yes | `BloodVolumeEvents` — adds passive blood regen per tick |
| Crimson Mastery | ✅ Yes | `PyreticForgeManip` — scales items smelted per cast |
| Vital Link | ✅ Yes | `KnownManipulationEvents` — chance to heal player on dealing manipulation damage |
| Iron Will | ✅ Yes | `BloodVolumeEvents.onPlayerDamaged` — reduces incoming damage by `getIronWillMultiplier()` when blood is below `getIronWillThreshold()` (default 15% of max blood) |
| Blood Flow | ✅ Yes | `BloodManipulation` — multiplies effective cooldown of manipulations |
| Coagulation | ✅ Yes | `BloodLossEffect` — chance to block incoming bleed effect ticks |
| Sanguine Reach | ✅ Yes | `BloodLampManip`, `CrimsonFlameConjurationManip`, `UmbralStepManip`, `SanguineExcavationManip` — scales range |
| Scar Affinity | ✅ Yes | `ScarEntityEventHandler.checkScarSynergy` — synergy attribute modifier amount multiplied by `getScarAffinityMultiplier()`; modifier removed and re-added every 20 ticks so level changes take effect immediately |
| Scar Resonance | ✅ Yes | `ScarEntityEventHandler.getEffectiveScarSlotMax()` — returns `SCAR_SLOT_MAX + getScarResonanceSlots()`; used as upper bound in all scar combat loops (`onLivingHurt`, `onEntityKilledByPlayer`, `checkScarSynergy`) |
| Scar Mastery | ✅ Yes | `ItemScar.onPlayerAttack`, `onPlayerDefend`, `onPlayerKill`, `applyTierThreeTickEffect` — all triggered effect durations multiplied by `getScarMasteryDurationMultiplier()` |

---

## 12. Bloodlines

A multiplayer social system where players form blood-bound groups.

- **Creation:** Use an **Unsigned Ancestral Ledger** — first use signs and creates a bloodline named after the leader
- **Joining:** Another player uses the same signed ledger to join
- **Shared Pool:** Each member contributes 5,000 blood to a communal pool
- **Trickle Donation:** Optionally auto-donate blood to the shared pool at a configurable rate
- **Auto-Draw:** Optionally auto-draw from the shared pool when personal blood falls below a threshold
- **Direct Routing Contribution:** Sanctum-only routing can draw from the shared pool when the linked player enables bloodline mode. The current implementation requires the linked player to be the bloodline leader or to have their routing opt-in enabled before the pool is used.
- **Member Expulsion:** Bloodline progenitors can expel member players through `BloodlinePoolScreen` (server-validated via `PacketKickBloodlinePlayer`)
- **Persistence:** Bloodline data is stored in world-level `BloodlineSavedData`
- **Monitoring:** The **Bloodline Pool Monitor** item shows pool status; the **BloodlinePoolScreen** provides a GUI

---

## 13. Scars & Spores

### 13.1 Scars

Scars are equippable items stored in a **Scar Binder** ![Scar Binder](../src/main/resources/assets/hemomancy/textures/item/scar_binder.png) (18 slots) or **Scar Binder Upgraded** ![Scar Binder Upgraded](../src/main/resources/assets/hemomancy/textures/item/scar_binder_upgraded.png) (27 slots). They are crafted at the **Cerebral Scarring Station**. Scar crafting requires **Initiatory Degree 4 (Adept)** minimum.

Scars are organized in **three tiers** by `deepenAmount` — how strongly they shift tendency alignment per equipped slot. The current NeoForge branch also gives standard scars real passive/combat effects through `ItemScar`: attribute modifiers, persistent effects, blood upkeep, max-blood modifiers, and event hooks for attack/defense/kill/tick behavior. `Scar Affinity` scales synergy modifiers, `Scar Resonance` increases effective combat slots, and `Scar Mastery` extends triggered scar effect durations.

**Tier 1 Scars (deepenAmount = 1) — Basic, available at Degree 4:**

| Scar | Tendency | Effect |
|------|----------|--------|
| ![](../src/main/resources/assets/hemomancy/textures/item/mind_spike.png) Mind Spike | Ductilis | Legacy override scar / mind spike slot behavior |
| ![](../src/main/resources/assets/hemomancy/textures/item/scars/scar_heart.png) Scar of the Heart | Animus | +2 Max Health |
| ![](../src/main/resources/assets/hemomancy/textures/item/scars/scar_pyre.png) Scar of the Pyre | Flammeus | +1 Attack Damage, -1 Armor |
| ![](../src/main/resources/assets/hemomancy/textures/item/scars/scar_feral.png) Scar of the Feral | Ductilis | +5% Attack Speed, -1 Armor |
| ![](../src/main/resources/assets/hemomancy/textures/item/scars/scar_halo.png) Scar of the Halo | Lux | +1 Armor Toughness, -5% Movement Speed; blinds attackers |
| ![](../src/main/resources/assets/hemomancy/textures/item/scars/scar_blight.png) Scar of Blight | Mortem | +1 Attack Damage; poison backtracks onto wearer after kills |
| ![](../src/main/resources/assets/hemomancy/textures/item/scars/scar_rime.png) Scar of Rime | Congeatio | +5% Movement Speed, -5% Attack Speed; slows struck foes |
| ![](../src/main/resources/assets/hemomancy/textures/item/scars/scar_thorn.png) Scar of the Thorn | Ferric | +1 Armor, -5% Movement Speed; reflects 1 thorns damage |
| ![](../src/main/resources/assets/hemomancy/textures/item/scars/scar_shade.png) Scar of the Shade | Tenebris | +5% Movement Speed, -1 Attack Damage; invisibility in darkness |

**Tier 2 Scars (deepenAmount = 2) — Advanced, available at Degree 4:**

| Scar | Tendency | Effect |
|------|----------|--------|
| Scar of Marrow | Animus | +4 Max Health, -5% Movement Speed; heals wearer on kill |
| Scar of Sol | Flammeus | +2 Attack Damage, -2 Armor; briefly ignites attackers |
| Scar of Flux | Ductilis | +10% Attack Speed, -2 Armor; grants Haste on kill |
| Scar of the Veil | Lux | +2 Armor Toughness, -10% Movement Speed; blinds + marks attackers with Glowing |
| Scar of Withering | Mortem | +2 Attack Damage, -2 Max Health; poisons struck foes |
| Scar of the Glacier | Congeatio | +10% Movement Speed, -10% Attack Speed; slows struck and nearby foes |
| Scar of the Anvil | Ferric | +2 Armor, +1 Armor Toughness, -10% Movement Speed; reflects 2 thorns damage |
| Scar of the Moon | Tenebris | +10% Movement Speed, -2 Attack Damage; invisibility in darkness and when struck in darkness |

**Tier 3 Scars (deepenAmount = 3) — Expert, available at Degree 5 (planned: move gate to Degree 6):**

| Scar | Tendency | Effect |
|------|----------|--------|
| Scar of the Phoenix | Animus | +6 Max Health, -10% Movement Speed; heals on kill and regenerates when gravely wounded |
| Scar of the Corona | Flammeus | +3 Attack Damage, +0.3 Knockback Resistance, -3 Armor; ignites attackers |
| Scar of the Chimera | Ductilis | +15% Attack Speed, -3 Armor, -4 Max Health; Haste/Speed/Strength on kill |
| Scar of Transcendence | Lux | +2 Armor Toughness, -15% Movement Speed; blinds/marks attackers and grants Resistance in bright light |
| Scar of Oblivion | Mortem | +3 Attack Damage, -4 Max Health; withers struck foes |
| Scar of Descendence | Congeatio | +15% Movement Speed, -15% Attack Speed, -2 Attack Damage; slows struck/nearby foes and grants slow fall |
| Scar of the Crucible | Ferric | +3 Armor, +2 Armor Toughness, -15% Movement Speed, -5% Attack Speed; reflects 3 thorns damage |
| Scar of the Eye | Tenebris | +15% Movement Speed, -3 Attack Damage; invisibility in darkness and when struck |

> **Scar Mechanic:** All standard scars extend `ItemScar`; when equipped in a valid Scar Binder slot they deepen the player's Blood Tendency alignment and apply their configured modifiers/effects. A stale `scar_ichor` recipe/lang/model entry still exists in resources, but `ItemInit` does **not** currently register `scar_ichor`; the active Animus tier-3 scar is `scar_phoenix`.

Each scar has a corresponding **Scar Pattern** item used in crafting.

### 13.2 Functional Fungal Scars (Scar-type items)

Special fungal scar items with active effects extend `ItemFungalScar`, render as rotating 3D scar items on the player, have rare rarity/foil visuals, and occupy the dedicated fungal scar slot (`ScarType.FUNGAL`, slot 0). The current implementation uses the **Mycelial Crucible**, not the Morphling Incubator, and the old four-scar incubator plan has been superseded.

The basic saint-linked set remains tuned at 1,200 blood / 1,200 ticks / 2,000 enzyme-power threshold:

| Item | Tendency | Active Effect |
|------|----------|---------------|
| ![](../src/main/resources/assets/hemomancy/textures/item/noctifly_agaric.png) Noctifly Agaric | Animus | Grants the `fungal_elytra` effect while equipped; glide support is maintained by `ScarEntityEventHandler.onGlideTick()` |
| ![](../src/main/resources/assets/hemomancy/textures/item/respergillus.png) Respergillus | Animus | Grants Water Breathing while equipped |
| ![](../src/main/resources/assets/hemomancy/textures/item/talaromyces_minus.png) Talaromyces Minus | Ferric | Grants Haste while worn and enables shift-mining ore vein mining through `VeinMinerHelper` |
| ![](../src/main/resources/assets/hemomancy/textures/item/lumina_devorans.png) Lumina Devorans | Tenebris | Grants Night Vision, Strength, and Resistance while equipped |

The new advanced set is also registered and has live event handlers for the non-tooltip effects:

| Item | Tendency | Active Effect | Cultivation Cost |
|------|----------|---------------|------------------|
| **Saprovitta vestigium** | Flammeus | **Feeding Wake** — movement leaves a brief damaging blood-fungal trail (1.5 magic damage pulses every 6 ticks while moving) | 1,200 blood / 1,200 ticks / 2,000 enzyme power |
| **Antiphonomyces resonans** | Ductilis | **Crawling Choir** — 20% chance for a successful blood manipulation to echo-cast at no extra blood cost | 2,400 blood / 2,400 ticks / 3,000 enzyme power |
| **Sanguiflora cadens** | Mortem | **Vein Orchard** — 30% chance on kill to bloom blood resources at the death site (Spore Sac, sometimes Hematic Iron Scrap) | 2,400 blood / 2,400 ticks / 3,000 enzyme power |
| **Thanomyces resurgens** | Congeatio | **Split Husk** — prevents death once, drains all active blood, reforms at 25% health, 15-minute per-stack cooldown | 2,400 blood / 2,400 ticks / 3,000 enzyme power |
| **Anastocordyceps nexus** | Lux | **Latching Vein** — striking an enemy tethers nearby foes for 6 seconds; tethered targets share 20% of damage taken | 2,400 blood / 2,400 ticks / 3,000 enzyme power |

**Mycelial Crucible recipe format** (`data/hemomancy/recipe/fungal_scar/*.json`):
```json
{
  "type": "hemomancy:fungal_scar_cultivation",
  "tendency": "LUX",
  "blood_cost_phase1": 2400,
  "phase1_duration": 2400,
  "maturation_threshold": 3000,
  "immature_result": { "id": "hemomancy:immature_fungal_scar" },
  "result": { "id": "hemomancy:anastocordyceps_nexus" }
}
```

### 13.3 Spore Cultures (Enzyme Fruiting)

Aligned spores are now functional reusable culture items for the **Mycelial Lantern** enzyme-fruiting loop. Each is registered as an uncommon item that stacks to 16 and is crafted shapelessly from the matching enzyme + `spore_sac` + `hyphal_substrate`. The culture remains in the Lantern while blood is converted into the matching enzyme output.

One culture exists for each enzyme/tendency vocabulary pair:
![](../src/main/resources/assets/hemomancy/textures/item/vivacious_spores.png) Vivacious,
![](../src/main/resources/assets/hemomancy/textures/item/fervent_spores.png) Fervent,
![](../src/main/resources/assets/hemomancy/textures/item/neurotic_spores.png) Neurotic,
![](../src/main/resources/assets/hemomancy/textures/item/incandescent_spores.png) Incandescent,
![](../src/main/resources/assets/hemomancy/textures/item/ruinous_spores.png) Ruinous,
![](../src/main/resources/assets/hemomancy/textures/item/frigid_spores.png) Frigid,
![](../src/main/resources/assets/hemomancy/textures/item/ferric_spores.png) Ferric,
![](../src/main/resources/assets/hemomancy/textures/item/umbral_spores.png) Umbral.

The JSON recipes live at `data/hemomancy/recipe/<spore_id>.json`, e.g. `vivacious_spores.json` combines `vivacious_enzyme`, `spore_sac`, and `hyphal_substrate` into `vivacious_spores`.

### 13.4 Mycelial Crucible & Immature Fungal Scar Cultures

The **Mycelial Crucible** (`MycelialCrucibleBlockEntity`) is the current fungal-scar cultivation station. It has 8 slots:

- Center (slot 0): finished fungal scar seed for Phase 1, or `immature_fungal_scar` for Phase 2
- Enzyme slots (1–4): aligned `EnzymeItem` / `RecycledEnzymeItem`; only matching tendency contributes
- Output (5): immature culture or finished scar
- Blood input (6): Bloody Flask or Blood Gourd
- Flask output (7): empty/cured flask return

**Phase 1 — Implantation:** The center scar plus aligned enzymes start a timed cultivation run. The crucible deducts the recipe's flat blood cost, then drains 1.5 blood/tick for the recipe duration. On completion it consumes the center/enzymes and outputs the single consolidated `immature_fungal_scar`.

**Phase 2 — Maturation:** The immature culture stores `Tendency`, `MatureThreshold`, `MatureProgress`, and `TargetScarId` in `DataComponents.CUSTOM_DATA`. Feeding aligned enzymes advances `MatureProgress`; when progress reaches the threshold, the crucible converts it into the target `ItemFungalScar`. Progress is preserved on the item stack, and blood shortages pause the process rather than resetting it.

`Hyphal Substrate` is registered as a supporting crafting ingredient, and `immature_fungal_scar` uses one model/texture with dynamic translated names such as `item.hemomancy.immature_scar.anastocordyceps_nexus`.

> **Design status:** The extractor / harvested Fungal Gardens scar plan has been replaced for now by crucible cultivation. The deeper Apotheos-tier fungal scar concept remains open-ended design space, but the implemented fourth scar family is already live through `ItemFungalScar` + `MycelialCrucible`.

---

## 14. Status Effects & Potions

Each effect has a corresponding potion, splash potion, lingering potion, and tipped arrow variant:

| Effect | Category | Color | Notable Mechanic |
|--------|----------|-------|-----------------|
| ![](../src/main/resources/assets/hemomancy/textures/mob_effect/blood_binding.png) **Blood Binding** | Harmful | Dark red | Immobilizes target |
| ![](../src/main/resources/assets/hemomancy/textures/mob_effect/blood_loss.png) **Blood Loss** | Harmful | Red | -15% movement speed |
| ![](../src/main/resources/assets/hemomancy/textures/mob_effect/blood_rush.png) **Blood Rush** | Beneficial | Red | +20% move speed, +10% attack speed |
| ![](../src/main/resources/assets/hemomancy/textures/mob_effect/hemolysis.png) **Hemolysis** | Neutral | Pink | Blood destruction effect |
| ![](../src/main/resources/assets/hemomancy/textures/mob_effect/fungal_elytra.png) **Noctifly Agaric** (Fungal Elytra) | Beneficial | — | Grants elytra flight ![](../src/main/resources/assets/hemomancy/textures/models/armor/fungal_elytra.png) |
| ![](../src/main/resources/assets/hemomancy/textures/mob_effect/sanguine_fertility.png) **Sanguine Fertility** | Beneficial | 0xCC3344 | Fertility/growth effect |
| **Arachnid Anastomosis** | Beneficial | 0x8B0000 | Spider-vein healing |
| **Mycorrhizal Mending** | Beneficial | 0x7B4F2A | Fungal regeneration |
| **Sanguine Siphon** | Beneficial | 0x8B0000 | Blood drain on hit |
| **Chitinous Bulwark** | Beneficial | 0x556B2F | +4 armor toughness |
| **Serpentine Guile** | Beneficial | 0x2E8B57 | +15% move speed, +10% attack speed |
| **Verminous Aura** | Beneficial | 0x4A3728 | Pest-based area effect |
| **Echoic Perception** | Beneficial | — | Bat morphling effect — nearby entities glow (radius scales with amplifier) |
| **Luminous Dissipation** | Beneficial | — | Moth morphling effect — knockback resistance |
| **Hemorrhagic Venom** | Beneficial | — | Tick morphling effect — AoE damage aura to nearby hostiles |
| **Spined Barricade** | Beneficial | — | Urchin morphling effect — passive thorns + armor bonus |
| **Venomous Resilience** | Beneficial | — | Centipede morphling effect — poison immunity + speed |
| **Burrower's Instinct** | Beneficial | — | Mole morphling effect — mining speed + underground regen/night vision |
| **Arcane Resonance** | Beneficial | 0x8800AA | MnA combo marker — next blood manipulation costs less blood (granted by blood-affinity MnA spells) |
| **Sanguine Clarity** | Beneficial | 0xAA0022 | MnA combo marker — next MnA spell costs less mana (granted by using blood manipulations) |
| **Morphic Strain** | Harmful | Fungal green | Primal morphling drawback. Modest max-health and movement-speed reduction after successful Primal powers. |

---

## 15. Unstained Systems

This section collects the reusable anti-hemomancy systems. The Unstained path section explains how a player enters and advances through Purity/Clarity; this section tracks the powers, rites, purification mechanics, client surfaces, and data anchors that support that path.

### 15.1 Still Arts

Still Arts are the Clarity-phase counterpart to Blood Manipulations, but they are not crafted Hematic Memories. They are granted by Our Lady of Still Waters and by Unstained rites as the player's silvery vital humor becomes stable enough to carry them.

Implementation spine:
- Registry: `StillArtInit.STILL_ARTS` (`hemomancy:still_arts`)
- Art definition: `common/unstained/stillarts/StillArt`
- Player state: `IKnownStillArts` / `KnownStillArts`, exposed through `HemoCapabilityAccess.getKnownStillArts(player)`
- Sync and use packets: `KnownStillArtsServerPacket`, `UpdateSelectedStillArtPacket`, `UseStillArtKeyPacket`
- Client selection: `RadialChooseStillArtScreen`, opened from the existing charm/radial key after Clarity is unlocked

Current basic Still Arts:

| Art | Required Clarity Stage | Role |
|-----|------------------------|------|
| Silver Rebuke | Awakened | Short-range pale knockback and slowing rebuke |
| Lethean Mute | Awakened | Silences hostile bodies through weakness and confusion |
| Still Pulse | Discerning | Brief defensive stillness and area slowing |
| Pale Diagnosis | Discerning | Reveals nearby suspicious or afflicted bodies |
| Memory Shear | Vigilant | Cuts a monster's immediate hostile fixation and disorients it |
| Absolving Step | Vigilant | Purging step that clears fire, poison, and wither while lunging forward |
| Quietus Bell | Resolute | Protective bell pulse that weakens surrounding hostiles |
| Autoimmune Edge | Enlightened | Dangerous pale backlash against nearby living bodies |

The Rite of Clarity (Consecrated Copper at the Unstained Podium) directly grants **Silver Rebuke** as the first Still Art via `KnownStillArtEvents.grantArt(player, StillArtInit.silver_rebuke)`. The remaining arts are granted by **advancements** through `StillArtRewardTable` — `KnownStillArtEvents.onAdvancementEarned` maps specific Unstained advancements to their eligible `EnumClarityStage` and calls `grantArtsForStage()`, which grants all arts whose required stage is ≤ the earned stage:

| Advancement | Clarity Stage | Arts Granted |
|---|---|---|
| `hemomancy/clarity_awakened` | AWAKENED | Silver Rebuke, Lethean Mute |
| `hemomancy/discerning` | DISCERNING | Still Pulse, Pale Diagnosis |
| `hemomancy/vigilant` | VIGILANT | Memory Shear, Absolving Step |
| `hemomancy/resolute_stage` | RESOLUTE | Quietus Bell |
| `hemomancy/enlightened_seeker` | ENLIGHTENED | Autoimmune Edge |

On login, `playerLoggedIn` calls `grantEligibleArts()` to backfill any arts the player should already have based on current `IUnstainedProgress` clarity. The radial selection screen (`RadialChooseStillArtScreen`) is opened from the existing charm/radial key once Clarity is unlocked.

### 15.2 Unstained Cardinal Rites

All Unstained rites have `bloodCost: 0` — they draw from purity/clarity rather than the blood reservoir.

**Purity-Phase Rites (levels 0–5):**

| Rite | File | Rite Form | Required Stage | Effect |
|------|------|-----------|----------------|--------|
| Rite of Lethean Baptism | `lethean_baptism` | Minor | 0 | Begins the Unstained path; sets `begunPurification = true`, grants starting purity |
| Rite of Still Waters | `still_waters` | Minor | 1 (Begun) | Creates a 5-min zone (16 block radius) reducing magic damage by 30% |
| Rite of Pale Consecration | `pale_consecration` | Lesser | 2 (Tainted) | 10-min zone that sears and slows hostile mobs entering the consecrated ground |
| Rite of the Silver Veil | `silver_veil` | Lesser | 2 (Tainted) | Grants Silver Ward effect (30 min, amplifier 1) to the caster |
| Rite of Silthmere's Remembrance | `silthmeres_remembrance` | Greater | 5 (Purified) | Bursts +5 purity and refreshes Silver Ward for all Unstained within 32 blocks |
| Rite of the Lethean Tide | `lethean_tide` | Greater | 3 (Cleansing) | Forcibly ends an active Blood Moon; grants the caster +10 purity |
| Rite of Clarity Ascension | `clarity_ascension` | Greater | 5 (Purified) | Unlocks the clarity phase (`clarityUnlocked = true`); requires full purity enforced in handler |
| Rite of the Closed Vein | `closed_vein` | Minor | 5 (Purified) | Reusable, non-breaking rite; clears Blood Loss, grants Silver Ward, slows nearby hostiles, and grants Lethean Mute after Clarity |
| Rite of the Lethe Covenant | `lethe_covenant` | Grand | 8 (Enlightened) | Establishes a Lethe Covenant domain: 5 chunks, 30 min. Halves spawns, shields Silver Ward from bleed, passively grows purity for Unstained inside |
| Rite of Lethean Judgment | `lethean_judgment` | Grand | 8 (Enlightened) | Offensive: applies Hemolysis (amp 2, 30 s) and disrupts vascular system of all blood-active players within 16 blocks |

**Clarity-Phase Rites (levels 6–8):**

| Rite | File | Rite Form | Required Stage | Effect |
|------|------|-----------|----------------|--------|
| Rite of the Silver Dawn | `silver_dawn` | Greater | 6 (Discerning) | Converts blood-faction blocks to cleansed equivalents in 8-block radius; grants Verdigris Aura (amp 2, 10 min) and +5 clarity |
| Rite of Antiseptic Ground | `antiseptic_ground` | Lesser | 6 (Discerning) | Reusable, non-breaking rite; creates a 15-min antiseptic ground zone and grants Still Pulse + Pale Diagnosis |
| Rite of Glass Lungs | `glass_lungs` | Lesser | 7 (Vigilant) | Reusable, non-breaking rite; clears poison/wither/fire, grants clean breath and slow falling, and grants Memory Shear + Absolving Step |
| Rite of the Pale Vigil | `pale_vigil` | Greater | 7 (Vigilant) | Bursts +10 clarity, Silver Ward (amp 2, 30 min), and Verdigris Aura (amp 2, 30 min) to all clarity-bearing Unstained within 40 blocks. Grants `ADV_VIGILANT`. |
| Rite of Moon-Washed Copper | `moon_washed_copper` | Greater | 7 (Vigilant) | Reusable, non-breaking rite; grants Verdigris Aura/Silver Ward, +5 clarity (+10 at night), Quietus Bell, and Autoimmune Edge if Enlightened |
| Rite of the Lethean Font | `lethean_font` | Grand | 8 (Enlightened) | Pinnacle Unstained rite. Opens a Lethe Covenant domain spanning 8 chunks for 1 hour. Bursts +20 clarity, Silver Ward (amp 3), and Verdigris Aura (amp 3) for 1 hour to all clarity-bearers within 50 blocks. Grants `ADV_ENLIGHTENED_SEEKER`. |

### 15.3 Unstained Crafting & Recipe Data

Unstained crafting is kept apart from the Harbinger Blood Structure/Cardinal Rite catalogs even when it reuses the same serializer or pattern-matching helper. The player-facing gates, resources, and fiction belong to the Purity/Clarity path rather than blood reservoir progression.

| Recipe lane | Data/type | Gate | Notes |
|---|---|---|---|
| Unstained Blood Structure recipes | `data/hemomancy/recipe/blood_structure/` entries with `unstained: true` | `HemoCapabilityAccess.getPlayerUnstainedLevel(player)` via `RecipeDegreeGates` | Examples include `unstained_pillar.json` (Glowstone Dust on Hematic Iron Block -> Unstained Podium, stage 1) and `pallid_retort.json` (Pale Distillate on Cauldron -> Pallid Retort, stage 2, `bloodCost: 0`). |
| Unstained Cardinal Rites | `data/hemomancy/recipe/cardinal_rite/` | numbered Unstained stage 0-8 | See §15.2. These rites set `bloodCost: 0` and spend purity/clarity semantics instead of blood. |
| White Humor Purification | `data/hemomancy/recipe/white_humor_purification/`, `white_humor_purification` | physical White Humor pool charges | See §15.4. Dropped items transform while submerged in charged White Humor sources. |
| Pallid Retort distillation | `data/hemomancy/recipe/distillation/` entries with `pallid: true` | Pallid Retort station access | Includes Ghost Pipe -> Pale Distillate, Lethean Dew/Brew, Hemolytic Solution, Consecrated Copper Ingot, Pale Silver, Pallid Infusion, and still-water draughts. |
| Unstained vanilla crafting | `data/hemomancy/recipe/` shaped/shapeless recipes | material/tool progression | Includes Lethean Chalice, Lethean Poppy Wreath, Pale Distillate, Tears of Silthmere, Cleansed Stone, Pallid Lantern, Pale Silver blocks/items, Hemolytic Plating, Unstained armor/tools, Verdigris Censer, Pallid Icon, and Tome of the Unstained. |

### 15.4 White Humor Purification

White Humor Purification is an Unstained in-world recipe system handled by `WhiteHumorPurificationRecipe`, `WhiteHumorPurificationEvents`, and persisted pool charge data in `WhiteHumorPoolSavedData`.

The player creates a pool by using a **Pale Humor Flask** on a replaceable block. This places a `white_humor` source block, returns an empty cured clay flask when not in creative mode, and resets that source to **32 purification charges**. Dropped item entities sitting in White Humor check for `hemomancy:white_humor_purification` recipes. Matching stacks keep extended lifetime while submerged, accumulate purification progress, then transform once their recipe's `transform_time` is reached and a charged source block is found within a 2-block search radius.

Each transformed item consumes one source charge. Large stacks are split by the nearest source's remaining charges: the transformed result entity is spawned, the original stack shrinks by the transformed count, and the source is removed when its charges are spent. If no charged source is available, progress holds at completion until one is available.

Clean Unstained witness blocks within 4 blocks accelerate the process. Bloomed Lethean Poppies count as 2 progress bonus; white/light gray/gray candles and unwaxed copper/cut copper/stairs/slabs count as 1. Every 80 item ticks, one participating witness may absorb the shed taint: Lethean Poppies become dormant, candles darken toward black, and copper advances one oxidation step.

| Input | Output | Transform Time |
|---|---|---|
| Blood Crystal Shard | Cleansed Blood Crystal Shard | 300 ticks |
| Hematic Iron Block | Pale Silver Block | 600 ticks |
| Venous Stone | Cleansed Stone | 240 ticks |
| Infested Venous Stone | Cleansed Stone | 260 ticks |
| Sanguine Glass | Cleansed Sanguine Glass | 240 ticks |
| Sanguine Pane | Cleansed Sanguine Pane | 240 ticks |

The Liber Immaculatus documents this diegetically under `books/liberimmaculatus/sacred_tools/pages/white_humor_purification.json`. JEI displays the recipe category as **White Humor Purification** and notes that each source purifies 32 items.

### 15.5 Unstained HUD

Unstained players see a dedicated top-right reliquary orb overlay (`UnstainedGaugeOverlay`). It only renders once `hasBegunPurification()` is true.

The overlay is built from **layered PNG textures** in `assets/hemomancy/textures/gui/unstained_overlay/`:

| Layer | Count | Purpose |
|-------|-------|---------|
| `orb_purity_0` – `orb_purity_20` | 21 frames | Orb fill color — washes from blood-red (0) toward silver-white (20) as purity rises |
| `halo_0` – `halo_10` | 11 frames | Glow halo around the diamond frame; appears at full purity, intensifies with clarity |
| `diamond_clarity_0` – `diamond_clarity_10` | 11 frames | Faceted diamond frame — brightens through 11 clarity stages |
| `diamond_purified` | 1 | Static frame used when purity is complete but clarity not yet unlocked |
| `pips_purity_0` – `pips_purity_4` | 5 frames | Angular purity stage pips along the bottom V of the diamond frame |
| `pips_clarity_0` – `pips_clarity_4` | 5 frames | Same pip geometry, verdigris-colored for the clarity phase |

Animated fluid fills the interior of the orb (circle geometry using inverse binary search for constant-area fill):
- A **meniscus line** with a sine-wave animation renders at the fluid surface; amplitude is reduced as the orb fills
- **Blood particle sprites** rise through the fluid while purity < 100%; they fade out as purity increases
- The fluid color transitions from blood-red to white across the purity range
- When `clarityUnlocked = true` the orb shows full white fill and the halo+diamond tracks the clarity step instead

Text rendered to the right of the orb:
- Stage title (e.g. "Cleansing") — color lerps from red to pale white as purity rises; verdigris when clarity is active
- Percentage line (e.g. "Purity 54%")

The overlay sits in the top-right corner (`screenWidth - 34, centerY = 54`). Position is not configurable (right-side only).

### 15.6 Unstained Materials, Facilities, and Data Anchors

Unstained implementation crosses several catalogs, so this section is the system map rather than a duplicate item/block list.

| Area | Primary anchors |
|---|---|
| Player state | `IUnstainedProgress`, `IKnownStillArts`, `IWhiteHumorVolume`, `HemoCapabilityAccess.getPlayerUnstainedLevel(player)` |
| Entry and progression blocks | `UnstainedPodiumBlockEntity`, `AltarOfCleansingBlockEntity`, `PallidRetortBlockEntity` |
| Purifying resources | Hemolytic Solution, Lethean Dew, Lethean Brew, Tears of Silthmere, Pale Humor Flask, Pale Distillate, Consecrated Copper, Pale Silver, Pallid Infusion |
| Cleansed building palette | Cleansed Blood Crystal, Cleansed Stone, Cleansed Sanguine Glass/Pane, Pallid Lantern, Pale Silver Bells, oxidized copper, white/gray candles, Lethean Poppy witness blocks |
| Unstained NPC/world anchors | Unstained Church, Unstained Zealot, Unstained Acolyte, Unstained Guardian, Our Lady whisper dialogue, Spectral Companion entity shell |
| Data paths | `data/hemomancy/recipe/blood_structure/` (`unstained: true` entries), `data/hemomancy/recipe/cardinal_rite/`, `data/hemomancy/recipe/white_humor_purification/`, `data/hemomancy/recipe/distillation/` (`pallid: true` entries), `data/hemomancy/books/liberimmaculatus/`, `data/hemomancy/dialogue_inquiry/zealot/`, `data/hemomancy/dialogue_inquiry/guardian/` |
| Client surfaces | `UnstainedGaugeOverlay`, `UnstainedProgressScreen`, `RadialChooseStillArtScreen`, `StillArtCooldownOverlay`, `WhiteHumorBarWidget`, `UnstainedRiteBoundaryRenderer` |

Keep Harbinger and Unstained systems mutually exclusive: completing a Harbinger degree rite clears Unstained progress and known Still Arts, while Clarity disables blood magic and strips remaining Harbinger degree state.

---

## 16. Morphlings

Symbiotic parasites derived from the fungal infection. They provide the Living Staff with different attack/ability modes.

### 16.1 Types

| Morphling | Item Class | Preferred / Secondary Tendency | Base Effect | Maturity Abilities (Developing → Mature → Apex) |
|-----------|-----------|-------------------------------|-------------|--------------------------------------------------|
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_fungal.png) Fungal | `FungalMorphlingItem` | Mortem / Animus | Mycorrhizal Mending (passive health regeneration) | Sporulation (AoE toxic spores when hit) → Mycorrhizal Network (heal nearby allies) → Cordyceps Burst (kills explode, poison foes + bonus loot) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_leeches.png) Leeches | `LeechesMorphlingItem` | Animus / Congeatio | Sanguine Siphon (passive blood volume refill) | Life Steal (heal from melee damage dealt) → Blood Transfusion (emergency heal using blood volume) → Sanguine Frenzy (missing-HP bonus damage + execute weakened targets) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_chitinite.png) Chitinite | `ChitiniteMorphlingItem` | Ferric / Congeatio | Chitinous Bulwark (passive armor toughness) | Carapace Thorns (reflect melee damage back) → Ablative Plating (regenerating Absorption shield) → Ironhide (invulnerability + thorn burst on heavy hit) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_serpent.png) Serpent | `SerpentMorphlingItem` | Ductilis / Flammeus | Serpentine Guile (move and attack speed) | Venom Strike (Poison on melee hit) → Constrict (3 hits roots & crushes target with Wither) → Ambush Predator (sneak 3s for lethal poison first-strike) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_pests.png) Pests | `PestsMorphlingItem` | Flammeus / Tenebris | Verminous Aura (AoE pest damage aura to nearby hostiles) | Swarm Retaliation (tracking pest projectiles hunt your attacker) → Infest (kills spawn pests targeting nearby foes) → Plague Burst (AoE Wither + damage at low health) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_spider.png) Spider | `SpiderMorphlingItem` | Tenebris / Lux | Arachnid Anastomosis (vascular/spider-vein healing) | Wall Climbing (cling to walls, arrest downward velocity) → Silk Tether (spawn temporary cobweb to break falls) → Web Cocoon (root & Poison attacker when struck) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_bat.png) Bat | `BatMorphlingItem` | Tenebris / Ductilis | Echoic Perception (nearby entities glow, radius scales with maturity) | Sonar Shriek (Darkness & Slow attacker on hit) → Membrane Glide (slow falling & reduced fall damage) → Nightwing Frenzy (Strength II + Speed I in darkness) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_moth.png) Moth | `MothMorphlingItem` | Lux / Ductilis | Luminous Dissipation (knockback resistance) | Dustwing Trail (blind hostiles while sprinting) → Phototaxis Pulse (flash blinds attacker + nearby hostiles on hit) → Cocoon Rebirth (prevent death by spending blood, 10 min cooldown) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_tick.png) Tick | `TickMorphlingItem` | Mortem / Tenebris | Hemorrhagic Venom (AoE damage aura to nearby hostiles) | Engorge (Resistance on kill from feeding) → Blood Fever (Speed near wounded hostiles) → Pandemic Burst (AoE Wither + Weakness on heavy hit) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_urchin.png) Urchin | `UrchinMorphlingItem` | Ferric / Congeatio | Spined Barricade (passive thorns + armor bonus) | Spine Lash (thorns + slow melee attackers) → Tidal Anchor (periodic knockback pulse vs. nearby hostiles) → Calcareous Shell (Resistance II after heavy hit, 20 s cooldown) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_centipede.png) Centipede | `CentipedeMorphlingItem` | Congeatio / Ferric | Venomous Resilience (poison immunity + speed boost) | Burrowing Strike (Weakness on hit to simulate armor bypass) → Segmented Defense (Regeneration to offset heavy hits) → Myriapod Swarm (Invisibility + Speed III escape at low HP) |
| ![](../src/main/resources/assets/hemomancy/textures/item/morphling_mole.png) Mole | `MoleMorphlingItem` | Ferric / Mortem | Burrower's Instinct (mining speed + underground regen/night vision) | Burrow Sense (reveal entities underground via Glowing) → Earthen Bulwark (Resistance when taking damage underground) → Seismic Slam (shockwave attack while sneaking+jumping underground) |

### 16.2 Cultivation

- Start with a **Morphling Polyp** ![Morphling Polyp](../src/main/resources/assets/hemomancy/textures/item/morphling_polyp.png) (base form)
- Incubate in a **Morphling Incubator** block with enzymes to grow into specific morphling types
- Store morphlings in a **Morphling Jar** ![Morphling Jar](../src/main/resources/assets/hemomancy/textures/item/morphling_jar.png) (6 slots, Uncommon rarity) — they bounce around inside
- The **Living Staff** cycles through equipped morphlings and changes its topper model accordingly

### 16.3 Maturity System

**Current implementation note:** Morphling maturity is now a five-stage system: `Unfed -> Fledgling -> Developing -> Mature -> Apex -> Primal` in code-facing terminology, with player-facing Primal treated as maturity level `5`. Incubator feeding and enzyme power still mature a morphling only up to **Apex**. **Primal** is a nectar-only capstone state and is backed by the stack marker `Primalized`, so it cannot be reached by simply adding more enzyme power.

Each morphling has a **maturity level** (1–5) that determines its power and which reactive abilities it has:

| Maturity | Name | Description |
|----------|------|-------------|
| 1 | Nascent | Base form — passive effect only (the morphling's signature status effect) |
| 2 | Developing | First reactive ability unlocked (typically a triggered defensive response) |
| 3 | Mature | Second reactive ability unlocked (more powerful utility/combat mechanic) |
| 4 | Apex | Third reactive ability unlocked (powerful signature ability with longer cooldown) |
| 5 | Primal | Nectar-transformed Apex form. Unlocks a late-game active or loop-defining capstone power. |

Each morphling type has a **preferred tendency** and **secondary tendency** — feeding the corresponding enzymes during incubation accelerates maturity. The passive effect's amplifier scales with maturity level.

### 16.3.1 Primal Morphlings

Primal morphlings are the true fourth player-facing capstone above Apex. To primalize a morphling, throw an **Apex** morphling item into a pool of **Morphic Nectar** while a nearby/throwing `ServerPlayer` has completed **Apotheos** (`IInitiatoryDegree >= 8`). The special Primal transform path runs before normal Morphic Nectar recipes. It refuses non-Apex morphlings, already-Primal morphlings, and players below Apotheos degree 8.

The transform preserves the item stack's morphling identity, existing custom data, feedings/enzyme power, cooldowns, and nectar mutation marker. Primal powers intentionally fill late-game gameplay roles rather than acting as simple numeric upgrades. Existing Apex powers still count Primal morphlings as Apex-or-better for compatibility.

Successful Primal powers apply **Morphic Strain** as the main fungal drawback alongside blood cost and cooldown. Morphic Strain is lighter than Hematic Strain: it modestly reduces max health and movement speed, uses fungal visual feedback, and is capped before it becomes a hard lockout.

| Morphling | Primal Role | Primal Power |
|---|---|---|
| Fungal | Corpse ecology / fungal resource engine | **Primal Mycorrhiza** seeds temporary communion patches from elite kills, healing allies, weakening hostiles, and rarely converting remains into fungal scar or morphic crafting resources. Cradle mode emits a sanctuary pulse. |
| Leeches | Blood economy overdrive | **Hemophage Covenant** links nearby bloodline allies; linked damage returns capped healing/blood to the owner or bloodline pool. |
| Chitinite | Boss tank / counterburst bank | **Primal Carapace** enters a shell stance that stores incoming damage briefly, then releases ferric shards in a ring or cone. Cradle mode fortifies ritual areas. |
| Serpent | Priority-target assassination | **Sovereign Venom** marks one target; repeated hits escalate poison into paralysis and then hemotoxic rupture. Strong against elites/bosses, poor for trash clearing. |
| Pests | Swarm commander / area denial | **Vermin Crown** builds a swarm counter from kills, then releases stored swarms as autonomous hunters. Cradle mode patrols and harasses hostiles. |
| Spider | Terrain control / vertical lair | **Web of Red Thread** tethers to blocks, pulls or roots entities, and can lace temporary climbable web-lines for allies. |
| Bat | Recon / night raid planning | **Echothesis** sends an active pulse that reveals living blood signatures through terrain, pings important block entities, and grants a short darkness combat window. |
| Moth | Rare reset / luminous rescue | **Chrysalis of Last Light** is an active or lethal-trigger cocoon that cleanses debuffs, prevents death, and disorients nearby hostiles. It has a very long cooldown and heavy blood/strain cost. |
| Tick | Attrition epidemic | **Hemorrhagic Season** infects wounded hostiles with a spreading bleed/wither marker; infected deaths refresh and spread the outbreak. |
| Urchin | Ritual anchor / bastion | **Reefheart Bastion** roots the player, grants resistance and knockback immunity, and reflects damage in pulses. Cradle mode becomes a defensive ward. |
| Centipede | Danger traversal / de-aggro | **Hundredfold Molt** sheds a decoy husk, grants brief invulnerability/invisibility/speed, and clears poison, wither, and slowness. |
| Mole | Excavation / domain utility | **Deep Tremor Sense** maps nearby ores, entities, and caves; charged use releases a tunneling shockwave. In the Fungal Gardens, it can reveal morphic pools or buried fungal features. |

### 16.3.2 Morphic Nectar Mutation Display

Any item transformed through Morphic Nectar receives the `MorphicNectarMutated` stack marker via `MorphicNectarMutationRules.markMutated`. Mutated items gain a tooltip line (`Morphic Nectar-mutated`) and a client-side inventory decorator. Generic nectar-mutated items use a dark organic green frame. Primal morphlings use a stronger red/green/yellow animated tendril overlay rendered procedurally over the item slot.

The Primal decorator is intentionally not a static frame: it uses a small set of animated, high-resolution procedural tendrils drawn with the GUI render type so the item reads as actively writhing while staying legible at inventory scale. The current tuning uses 5 tendrils, 42 curve samples, 3 layered passes, a 1.25 px body width, and trimmed endpoints to avoid a filled-in slot mask.

### 16.4 Morphling Cradle

The **Morphling Cradle** (`MorphlingCradleBlockEntity`) is an owner-bound support station for hosting a single morphling item outside the staff.

- Supports **floor / wall / ceiling** placement (`AttachFace` + `FACING` state)
- Only the bound owner can swap/remove the hosted morphling
- Applies hosted morphling support effects to the owner and valid bloodline members in range
- Uses staged upkeep/action blood costs, with fallback draw from owner bloodline pool when enabled
- Can leech nearby valid hostile targets into a cradle blood buffer and redistribute that blood to nearby cradles / owner blood volume
- Recognizes Primal maturity (`level 5`) but only cradle-suitable morphlings gain special Primal area behavior: Fungal, Leeches, Chitinite, Pests, and Urchin.

---

## 17. Puppeteering & Summons

The puppeteer summon system is a Harbinger-side control-tool path rather than another blood manipulation category.

- **Control tool:** `marionette_crossbar` / **Marionette Crossbar**. It stores a stable crossbar UUID, selected summon name, and up to 256 thread charge. Use calls or recalls the selected summon; sneak-use cycles known summons. The item bar is always visible on crossbars and acts as the thread meter: full at 256 thread, empty at 0, using a crimson/red color ramp even though the item is not damageable.
- **Station:** `puppeteers_spindle` / **Puppeteer's Spindle**. It is a persistent block entity with two menu slots: a crossbar slot and a thread feeder slot. Placed thread is consumed immediately into the spindle's internal `threadBuffer` at 1 buffer per item count, capped at 512. A slotted Marionette Crossbar automatically draws from that buffer until the crossbar reaches its 256-thread cap. Binding, summon selection, and call/recall preparation are controlled from the spindle screen/packets and operate on the slotted crossbar rather than the player's first inventory crossbar.
- **Spindle rendering/UI:** The placed spindle stores horizontal facing, faces the placing player, and renders through `PuppeteersSpindleRenderer` / `PuppeteersSpindleModel` with a custom item renderer instead of appearing as a venous stone brick cube. Its screen uses a vein-pattern background, styled slot frames, crossbar/thread meters, summon list, and themed buttons/tooltips.
- **Unlock economy:** Direct "unlock next summon with Sanguine Quintessence" spindle use is retired. `sanguine_quintessence` is now the held catalyst for instant Blood Crafting puppeteer trial recipes; defeating the spawned unbound trial boss permanently grants that summon shape to the trial caster.
- **Thread economy and tether:** Summoning spends the definition's `threadSummonCost`; active summons drain `threadUpkeepPerMinute` from their owning crossbar every minute. Each bound summon renders a red thread back to its owner. If the matching crossbar is not equipped in either hand, the summon and thread flicker/fade for 100 ticks (5 seconds); re-equipping the crossbar stabilizes the summon, while failing to do so unravels it. If upkeep cannot be paid, the crossbar's active summons still unravel immediately.
- **Anti-stockpile rule:** active summon cap is calculated from the player's `skill_puppet_skein` level and checks active bound summons by owner, not by crossbar. Carrying extra crossbars cannot exceed the learned cap.
- **Skills:** `skill_puppet_skein` increases active summon cap, `skill_living_sinew` increases summon health/damage, and `skill_far_tether` increases command range.
- **Harbinger UI:** `HarbingerProgressScreen` includes a `SUMMONS` tab. It groups summons by degree, shows degree-locked, recipe-locked, trial-required, and known states, reports base and skill-modified stats, and creates a client-only preview entity for the selected summon. Preview render failures fall back to an icon/text placeholder rather than crashing the screen.

Puppeteer trial recipes unlock when the matching degree is obtained and are also re-awarded on login for existing saves. The recipe `required_degree` is sourced from the matching `PuppeteerSummonDefinition.requiredDegree()` at runtime so the Blood Crafting gate cannot drift from the summon definition gate. Trial bosses reuse the summon definitions as unbound hostile versions with default boss tuning: 1.5x health, 1.25x damage, no owner/crossbar upkeep, no active-summon cap accounting, and caster-only unlock credit on death.

Current summon definitions:

| Summon | Degree | Role | Base HP | Base Damage | Thread Call | Upkeep | Trial Blood |
|---|---:|---|---:|---:|---:|---:|---:|
| `veinwing_vulture` | 2 | Fast flying striker | 14 | 4 | 28 | 18/min | 500 |
| `marrow_spitter` | 3 | Ranged support | 22 | 5 | 38 | 12/min | 750 |
| `gorebound_hulk` | 4 | Slow heavy bruiser | 55 | 9 | 56 | 8/min | 1100 |

Legacy summon/test entities (`enthralled_doll`, `wretched_will`, and `blood_thrall`) remain mechanically unchanged by this pass.

---

## 18. Drudge System

**Status:** `Implemented` for the persistent Drudge entity, SSC birthing/refill loop, memory execution spine, direct-routing tender behavior, rogue-state rules, and interaction controls.

The Drudge is a persistent, player-owned semi-organic construct that holds a single **Blood Memory** (`BloodManipulation`) and executes it autonomously within a leash radius anchored to a **Semi-Sentient Construct (SSC)** block. Unlike the Blood Thrall (a transient courier), the Drudge is a long-term servant that "learns a job" and keeps doing it.

### Entity: `DrudgeEntity`

- **Class:** `common/entity/npc/DrudgeEntity`
- **Registry ID:** `hemomancy:drudge`
- **Extends:** `PathfinderMob implements OwnableEntity`

**Synched data (server→client):**
| Field | Type | Purpose |
|-------|------|---------|
| `DATA_OWNER_UUID` | `Optional<UUID>` | UUID of the Harbinger who birthed this Drudge |
| `DATA_HOME_POS` | `Optional<BlockPos>` | World position of the bound SSC |
| `DATA_BLOOD_CHARGE` | `float` | Current internal blood reserve (0–3 000 mL) |
| `DATA_IS_ROGUE` | `boolean` | Whether the Drudge has turned hostile |
| `DATA_PASSIVE_MODE` | `boolean` | Passive = auto-fires; Commanded = electrode-only |

**Attributes:**
- Health: 20 HP, Speed: 0.22, Armor: 4, Attack: 3, Follow Range: 32

**Blood economy:** The Drudge has an internal blood pool (`bloodCharge`, max 3 000 mL). The SSC refills it at 50 mL/tick when the Drudge is within 3 blocks of the SSC. The Drudge does **not** draw from the player's `IBloodVolume` cap in real time.

**Direct routing tender behavior:** Drudges near their SSC scan nearby saved Suture links every 40 ticks and can feed linked machines through `DrudgeTenderSource`. A successful tender action spends 20 internal blood charge, respects the linked source contract and target request, and does not create blood or act as bulk storage.

**Action cost:** Each manipulation fires at `cost × DRUDGE_ACTION_COST_MULTIPLIER` (default 1.5×) and a cooldown of `cooldown × DRUDGE_COOLDOWN_MULTIPLIER` (default 2×).

### AI Goal Stack

| Priority | Goal | Condition |
|----------|------|-----------|
| 1 | `DrudgeReturnToSSCGoal` | Blood charge below threshold OR outside leash range |
| 2 | `DrudgeExecuteMemoryGoal` | Has memory + sufficient charge + (Passive or electrode signal) |
| 3 | `MeleeAttackGoal` | Rogue mode only |
| 4 | `WaterAvoidingRandomStrollGoal` | Not rogue, within leash range |
| 5 | `RandomLookAroundGoal` | Always |

### Drudge Memory Behavior

`DrudgeExecuteMemoryGoal` dispatches the installed manipulation's registered `DrudgeAction` from `ManipulationInit`. Actions use the Drudge's current block position as the work origin and `drudgeWorkRadius` as the search radius. Blood charge and cooldown are only consumed when the action returns `true`; unsupported or unregistered actions do not fire.

| Manipulation | Drudge behavior |
|--------------|-----------------|
| `venous_travel` | Unsupported; cannot be used by Drudges |
| `blood_shot` | Ranged strike against the nearest hostile |
| `deadly_gaze` | Launches and heavily strikes the nearest hostile |
| `summon_avatar` | Unsupported; cannot be used by Drudges |
| `blood_needle` | Fires a five-hit needle volley at the nearest hostile |
| `blood_cloud` | Applies Wither to nearby hostiles |
| `blood_rush` | Grants Speed II to the Drudge and nearby player allies for 10 seconds |
| `blood_aneurysm` | Damages nearby hostiles and applies Nausea |
| `vital_effusion` | Bonemeal-accelerates nearby growable blocks around the Drudge |
| `ferric_transmutation` | Spawns one iron ingot at the Drudge's position |
| `activation_potential` | Grants Regeneration II to nearby player allies for 5 seconds |
| `sanguine_ward` | Grants Resistance I to nearby player allies for 10 seconds |
| `hemolymphal_pulse` | Applies Glowing to all nearby living entities |
| `conjure_blade` | Unsupported; cannot be used by Drudges |
| `blood_absorption` | Unsupported; cannot be used by Drudges |
| `blood_projection` | Unsupported; cannot be used by Drudges |
| `summon_thrall` | Unsupported; cannot be used by Drudges |
| `crimson_flame_conjuration` | Ignites the nearest hostile for 6 seconds |
| `sanguine_mending` | Repairs up to 100 durability on the most-damaged armor piece of the nearest player ally |
| `hemosynthesis` | Heals the most-wounded nearby player ally for 4 HP |
| `blood_lamp` | Places a torch at a nearby dark, supported air block |
| `crimson_harvest` | Bonemeal-accelerates nearby growable blocks in the work radius |
| `glacial_grasp` | Freezes, heavily slows, and damages the nearest hostile |
| `sanguine_excavation` | Mines the block the Drudge is facing, dropping normal block drops |
| `vascular_dowsing` | Unsupported; cannot be used by Drudges |
| `ferric_resonance` | Unsupported; cannot be used by Drudges |
| `pyretic_forge` | Ignites and damages all hostiles in the work radius |
| `umbral_step` | Teleports the Drudge to a random dark valid spot within the work radius |
| `crimson_sight` | Applies Glowing to nearby hostiles |
| `vital_reservoir` | Damages the nearest hostile for 2 magic damage and refills 200 Drudge blood charge, unless near full |
| `cryogenic_pulse` | Slows all hostiles in the work radius |
| `glacial_circulation` | No registered Drudge action; currently does not fire |
| `glacial_bastion` | Raises a ring of packed ice around the Drudge |
| `osseous_bloom` | No registered Drudge action; currently does not fire |
| `sanguine_ignition` | Ignites and damages the nearest hostile |
| `vitric_combustion` | Ignites and heavily fire-damages all hostiles in the work radius |
| `void_shroud` | Cloaks the Drudge with Invisibility for 30 seconds |
| `blood_eclipse` | Blinds and magic-damages all hostiles in the work radius |
| `hemorrhage` | Applies Wither to the nearest hostile |
| `exsanguinate` | Deals 20% max-health magic damage to the nearest hostile and heals the Drudge for half that amount |
| `crimson_tithe` | Unsupported; cannot be used by Drudges |
| `unclosing_eye` | Applies long-duration Glowing to all nearby living entities |
| `bloom_of_rot` | Applies Wither II and Poison I to all hostiles in the work radius |
| `endless_hour` | Grants the Drudge Absorption III and Resistance III for 20 seconds |

### Rogue State

A Drudge goes Rogue when:
1. Blood charge reaches 0 and it cannot reach its SSC within `DRUDGE_ROGUE_TIMEOUT_TICKS` (default 200 ticks = 10 s).
2. It drifts more than `leashRadius + 6` blocks from its SSC home.

In Rogue state: targets players (priority) then monsters; the equipped memory is **dropped on death** so it is not lost.

### Interaction Summary

| Action | Result |
|--------|--------|
| Right-click Drudge (empty hand) | Toggle Passive/Commanded mode + status readout |
| Right-click Drudge (with Blood Memory) | Install the memory |
| Shift+right-click Drudge (empty hand) | Retrieve installed memory |
| DSD shift+right-click on Drudge | Dissolve the Drudge, refund 1 500 mL to player |
| Drudge Electrode (ON mode) + attack swing | Send "fire now" signal to all owned Drudges within 16 blocks |
| Drudge Electrode (ON mode) + right-click SSC | Birth a new Drudge (degree gate + 3 000 mL cost) |

### Acquisition: Birthing via SSC + Electrode

1. Place an SSC with blood available.
2. Hold the Drudge Electrode in ON mode and right-click the SSC.
3. Degree gate: player must be Illuminatus (Degree ≥ 3, configurable).
4. Blood cost: 3 000 mL drained from player.
5. SSC cap: max 3 Drudges per SSC (configurable). Attempt beyond cap returns a flavour message.
6. Spawns a Drudge at the SSC position, bound to it, at half charge.

### SSC as Hub: `SemiSentientConstructBlockEntity`

The SSC now implements `IBloodTile` so it can hold its own blood volume (max 30 000 mL, refillable by Dendritic Distributors or other sources). Every 10 ticks it scans for nearby Drudges whose `homePos` matches its position and refills their `bloodCharge` at 50 mL per tick-scan (= 500 mL per second at 20 TPS).

Right-clicking the SSC with an empty hand now displays the status of all bound Drudges and the SSC's own blood level.

### Config Keys (`HemoServerConfig`, section `drudge`)

| Key | Default | Description |
|-----|---------|-------------|
| `drudgeLeashRadius` | 24 | Max blocks from SSC before Drudge returns |
| `drudgeMaxPerSSC` | 3 | Max Drudges per SSC |
| `drudgeBirthCost` | 3000.0 | mL cost to birth a Drudge |
| `drudgeRogueTimeoutTicks` | 200 | Ticks stuck before going Rogue |
| `drudgeActionCostMultiplier` | 1.5 | Blood cost multiplier for Drudge actions |
| `drudgeCooldownMultiplier` | 2 | Cooldown multiplier for Drudge actions |
| `drudgeRequiredDegree` | 3 | Minimum degree to birth a Drudge |
| `drudgeWorkRadius` | 12 | Radius (blocks) for target scanning |

### Textures

Located in `assets/hemomancy/textures/entity/drudge/`:
- `model_drudge_grey.png` — Default (tame) texture
- `model_drudge_red.png` — Rogue texture (applied when `isRogue() == true`)
- Additional palette variants: purple, green, yellow, blue, brown (available for future use)

### Items Involved

| Item | Role |
|------|------|
| `drudge_electrode` (`DrudgeElectrodeItem`) | ON mode + SSC click = birth; ON mode + swing = signal |
| `dsd` (`DSDItem`) | Shift+right-click Drudge = dissolve + 1 500 mL refund |
| Blood Memory items (`BloodMemoryItem`) | Install into Drudge to assign its task |

---

## 19. Direct Blood Routing & Servitors

Direct Blood Routing is the no-basin automation model for blood-fed machines. It intentionally avoids a NeoForge blood fluid, external pipe compatibility, and new bulk blood storage blocks. Links persist owner, mode, bloodline permission, and target working reserve in `BloodRoutingSavedData`; they do not persist blood.

**Core routing API:**
- `IBloodSourceContract` models a permitted source contract that can validate ownership, range, and maximum draw rate.
- Current source contracts are `EquippedGourdSource`, `LinkedPlayerSource`, `BloodlineSource`, `ThrallCourierSource`, and `DrudgeTenderSource`.
- `IBloodRoutingTarget` lets a machine request only current recipe demand or a capped working reserve.
- `BloodRoutingHelper` performs pull-based transfer, source priority, safety floors, bloodline checks, and target sync.
- Existing `IBloodTile` / `IBloodReservoirContainer` reservoirs remain valid targets. If a block entity does not implement `IBloodRoutingTarget`, routing fills only toward `BloodRoutingRules.DEFAULT_WORKING_RESERVE` (600 blood), not the whole reservoir.

**Hematic Suture Needle:**
- Registry item: `hematic_suture_needle`; class: `HematicSutureNeedleItem`.
- Degree 3+ can bind a blood-capable block entity or a `HematicSutureNodeBlockEntity` to the player in nearby mode.
- Sneak-use on the player's own bound link cycles modes: nearby -> sanctum -> sanctum + bloodline -> nearby. Sanctum mode requires Degree 5 and the link position to be inside the owner's Founding Sanctum radius.
- Sneak-use in air toggles the player's `IBloodVolume#isBloodRoutingOptInEnabled()` flag for bloodline routing permission.

**Source priority and limits:**
- Nearby links require the bound player to be online, alive, active in `IBloodVolume`, Degree 3+, in the same level, and within `BloodRoutingHelper.NEARBY_RANGE` (16 blocks).
- Sanctum links require Degree 5+ and a link position inside the owner's Founding Sanctum.
- Routing ticks every 10 ticks with a default pass budget of 100 blood (`DEFAULT_MAX_RATE_PER_TICK` 10 x interval).
- Source order is: open equipped Blood Gourd first (scar slot, main hand, then offhand, using that gourd's tier transfer rate), then owner blood at up to 80 blood per pass while staying above the 50% safety floor, then optional bloodline pool at up to 60 blood per pass.
- Bloodline draw only works in sanctum mode with bloodline mode enabled. The linked player must belong to a valid bloodline, the shared pool must contain blood, and the linked player must be the bloodline leader or have their routing opt-in enabled.

**Hematic Suture Node:**
- Registry block: `hematic_suture_node`; block entity: `HematicSutureNodeBlockEntity`.
- Optional visible anchor for longer or clearer sanctum infrastructure. Machines can still be bound directly for simple setups.
- Holds no blood capability and no persistent reservoir; it emits subtle red dust routing particles when it moves blood.
- Every routing interval it attempts to feed adjacent linked targets from the same saved link budget.

**Servitor behavior:**
- `BloodThrallEntity` can bind a direct-routing source/node, physically carry a capped amount of blood, and deposit into a destination reservoir. It draws through the same linked source contracts, so it cannot duplicate blood or bypass safety limits.
- `DrudgeTenderSource` lets Drudges near their Semi-Sentient Construct tend nearby linked machines. A Drudge scans saved Suture links around its SSC, spends internal charge only when routing succeeds, and does not generate or bulk-store blood for machines.

---

## 20. Items & Materials

### 20.1 Key Materials

| Item | Purpose |
|------|---------|
| ![](../src/main/resources/assets/hemomancy/textures/item/sanguine_formation.png) Sanguine Formation | Catalyst for blood structure recipes |
| ![](../src/main/resources/assets/hemomancy/textures/item/befouling_ash_trail.png) Befouling Ash / ![](../src/main/resources/assets/hemomancy/textures/item/smouldering_ash_trail.png) Smouldering Ash / ![](../src/main/resources/assets/hemomancy/textures/item/virid_salis_trail.png) Virid Salis | Ash trails for rituals and recipes; Virid Salis is the Unstained-aligned green salt-ash |
| ![](../src/main/resources/assets/hemomancy/textures/item/active_befouling_ash.png) Active Befouling / ![](../src/main/resources/assets/hemomancy/textures/item/active_smouldering_ash.png) Active Smouldering Ash | Active versions of ash trails |
| ![](../src/main/resources/assets/hemomancy/textures/item/hematic_iron_scrap.png) Hematic Iron Scrap | Blood-infused iron alloy ingredient |
| ![](../src/main/resources/assets/hemomancy/textures/item/hematic_iron_powder.png) Hematic Iron Powder | Extracted from blood via centrifuge |
| ![](../src/main/resources/assets/hemomancy/textures/item/chalybeate_sclerite.png) Chalybeate Sclerite | Ferric deep-ocean material nonlethally knapped from retracted Chalybeate Snails with any HutosLib `ItemKnapper`. Distills to Hematic Iron Powder and can substitute for Ferric Enzyme in the Ferric Spores recipe. |
| ![](../src/main/resources/assets/hemomancy/textures/item/consecrated_copper_ingot.png) Consecrated Copper Ingot | Anti-blood copper, used in Unstained path |
| ![](../src/main/resources/assets/hemomancy/textures/item/hemolytic_solution.png) Hemolytic Solution | Anti-blood enzyme solution, starts the Unstained path |
| ![](../src/main/resources/assets/hemomancy/textures/item/hemolytic_plating.png) Hemolytic Plating | Silver-based anti-blood plating |
| ![](../src/main/resources/assets/hemomancy/textures/item/neutralizing_gasket.png) Neutralizing Gasket | Anti-blood component |
| ![](../src/main/resources/assets/hemomancy/textures/item/foul_paste.png) Foul Paste | Crafting ingredient |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_rock.png) Blood Rock | Crafting ingredient |
| ![](../src/main/resources/assets/hemomancy/textures/item/sanguine_conduit.png) Sanguine Conduit | Crafting ingredient / covenant anchor. **Block form gated behind Degree 5 (Illuminatus).** Right-clicking a surface places the block only when `IInitiatoryDegree.getDegreeNumber() >= 5`; below that degree the item shows the locked placement message and fails placement. In-air right-click opens the Harbinger skill tree at any degree. **Right-clicking the placed block also opens the Harbinger skill tree.** The placed block has a minimal `SanguineConduitBlockEntity` whose BER (`SanguineConduitBlockRenderer`) draws a slow, dim pulsing crimson ring expanding outward — a quiet mark of covenant presence. Registered in `ItemInit` as `ItemSanguineConduit`, which extends `BlockItem` for `BlockInit.sanguine_conduit`; `BlockInit.shouldSkipAutoBlockItem()` skips the placed block so no duplicate generic `BlockItem` overwrites the custom item on reload. Tooltip changes at Degree 5 to reveal the planting mechanic. |
| ![](../src/main/resources/assets/hemomancy/textures/item/sanguine_quintessence.png) Sanguine Quintessence | Rare Harbinger catalyst produced by the Exsanguination cardinal rite. Used as the placed catalyst for Founding Sanctum and as the held catalyst for puppeteer trial Blood Crafting recipes. |
| ![](../src/main/resources/assets/hemomancy/textures/item/serpent_scale.png) Serpent Scale | Mob drop |
| ![](../src/main/resources/assets/hemomancy/textures/item/swollen_leech.png) Swollen / ![](../src/main/resources/assets/hemomancy/textures/item/dried_leech.png) Dried Leech | Mob drops |
| ![](../src/main/resources/assets/hemomancy/textures/item/chitinous_husk.png) Chitinous Husk | Mob drop |
| ![](../src/main/resources/assets/hemomancy/textures/item/puppeteering_thread.png) Puppeteering Thread | Mob drop and puppeteer fuel. The Puppeteer's Spindle consumes it into its 512-thread internal buffer at 1 item count = 1 buffer point, then transfers that thread into slotted Marionette Crossbars up to their 256-thread cap. |
| ![](../src/main/resources/assets/hemomancy/textures/item/bleeding_bulb.png) Bleeding Bulb | Plant-based ingredient |
| ![](../src/main/resources/assets/hemomancy/textures/item/dicentra_sap.png) Dicentra Sap | Plant-based ingredient |
| ![](../src/main/resources/assets/hemomancy/textures/item/spore_sac.png) Spore Sac | Fungal ingredient |
| ![](../src/main/resources/assets/hemomancy/textures/item/hyphal_substrate.png) Hyphal Substrate | Mycelial Crucible support ingredient for fungal scar cultivation |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_crystal_shard.png) Blood Crystal Shard / ![](../src/main/resources/assets/hemomancy/textures/item/cleansed_blood_crystal_shard.png) Cleansed Blood Crystal Shard | Crystal materials |
| ![](../src/main/resources/assets/hemomancy/textures/item/vivianite_cluster.png) Vivianite Cluster | Mineral material |
| ![](../src/main/resources/assets/hemomancy/textures/item/gourd_seeds.png) Gourd Seeds | Plantable, grows gourds |
| ![](../src/main/resources/assets/hemomancy/textures/item/dried_gourd.png) Dried Gourd | Gourd processing product |

### 20.2 Blood Storage Items

| Item | Role | Capacity | Flow | Kill Siphon | Passive |
|------|------|----------|------|-------------|---------|
| ![](../src/main/resources/assets/hemomancy/textures/item/bloody_flask.png) Bloody Flask | Cheap infusion | 2,500 | Instant use | None | Blood Drunkenness |
| ![](../src/main/resources/assets/hemomancy/textures/item/bloody_jug.png) Bloody Jug | Cheap infusion | 5,000 | Instant use | None | Blood Drunkenness |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_gourd_white_open.png) Blood Gourd White | Steady Vessel | 1,000 | 1.0 ml/tick | 75% | None |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_gourd_red_open.png) Blood Gourd Red | Combat Siphon | 1,800 | 3.0 ml/tick | 125% | None |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_gourd_black_open.png) Blood Gourd Black | Deep Reservoir | 3,500 | 0.75 ml/tick | 100% | None |
| ![](../src/main/resources/assets/hemomancy/textures/item/curved_horn.png) Curved Horn | Burst Vessel | 1,500 | 5.0 ml/tick | 50% | None |
| ![](../src/main/resources/assets/hemomancy/textures/entity/blood_gourd/hemorath_rib.png) Hemorath Rib | Living Marrow | 5,000 | 2.5 ml/tick | 125% | +0.25 ml/tick |
| ![](../src/main/resources/assets/hemomancy/textures/item/bloody_vial.png) Bloody Vial | Sample container | N/A | Syringe/rack use | None | Centrifuge workflow |
| ![](../src/main/resources/assets/hemomancy/textures/item/vial_rack.png) Vial Rack | Sample rack | 8 vials | Syringe/rack use | None | Centrifuge workflow |

Direct blood restores are emergency infusions: they restore blood immediately, apply a 60-tick use cooldown, and build Blood Drunkenness. Sampled Bloody Vials and Vial Racks remain extraction/centrifuge containers and are not drinkable infusion fuel. Blood Gourds avoid the mismatch penalty because their reserve is bonded through the equipped gourd/scar slot rather than carried as loose foreign blood.

Acquisition: Venous Stone has a rare 2.5% global loot modifier chance to shed a `blood_rock` when mined. Bloody Jugs have a rare 2% killed-by-player global loot modifier chance from the curated `hemomancy:bloody_jug_drop_candidates` entity tag, currently blood-drunk puppeteers, crimson does, cruor fiends, hemojellies, hemolymphopoda, thirsters, and venous striders.

> **Blood Gourd 3D models (open/closed):**
>
> | White | Red | Black | Curved Horn |
> |---|---|---|---|
> | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_gourd/white.png) ![](../src/main/resources/assets/hemomancy/textures/entity/blood_gourd/white_open.png) | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_gourd/red.png) ![](../src/main/resources/assets/hemomancy/textures/entity/blood_gourd/red_open.png) | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_gourd/black.png) ![](../src/main/resources/assets/hemomancy/textures/entity/blood_gourd/black_open.png) | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_gourd/curved_horn.png) ![](../src/main/resources/assets/hemomancy/textures/entity/blood_gourd/curved_horn_open.png) |

### 20.3 Memory Items

| Item | Purpose |
|------|---------|
| ![](../src/main/resources/assets/hemomancy/textures/item/hematic_memory.png) Hematic Memory | Base blank memory item |
| ![](../src/main/resources/assets/hemomancy/textures/item/lethean_dew.png) Lethean Dew | Memory processing ingredient |
| ![](../src/main/resources/assets/hemomancy/textures/item/lethean_brew.png) Lethean Brew | Cursed clay jar from the River Lethe — enables forgetting memories |
| ![](../src/main/resources/assets/hemomancy/textures/item/fervent_husk.png) Fervent Husk | Memory processing ingredient |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_stained_stone.png) Blood Stained Stone | Memory-related item |
| Blood Memory (per manipulation) | One for each registered manipulation — using it teaches the player |
| Crude Memory Shards | Early starter memories that teach and auto-equip weak manipulations without needing the Mnemonic Reliquary; current set covers `blood_shot`, `blood_rush`, `deadly_gaze`, `crimson_harvest`, `sanguine_mending`, `blood_lamp`, `hemorrhage`, `glacial_grasp`, `sanguine_ignition`, and `void_shroud` |
| **Canon Memory: Crimson Tithe** | Saint manipulation memory (Hemorath) — obtained via Somatic Loom with Hallowed Residuum of Hemorath |
| **Canon Memory: Unclosing Eye** | Saint manipulation memory (Seraphae) — obtained via Somatic Loom with Hallowed Residuum of Seraphae |
| **Canon Memory: Bloom of Rot** | Saint manipulation memory (Putriciel) — obtained via Somatic Loom with Hallowed Residuum of Putriciel |
| **Canon Memory: Endless Hour** | Saint manipulation memory (Velorum) — obtained via Somatic Loom with Hallowed Residuum of Velorum |

**Memory Textures Gallery:**

> **Note:** Memory items use a 2-layer model system — the base `hematic_memory.png` is overlaid with a unique per-manipulation overlay from `textures/item/memories/memory_*_overlay.png`.

| | | | |
|---|---|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_shot_overlay.png) Blood Shot | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_deadly_gaze_overlay.png) Deadly Gaze | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_needle_overlay.png) Blood Needle | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_rush_overlay.png) Blood Rush |
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_cloud_overlay.png) Blood Cloud | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_aneurysm_overlay.png) Blood Aneurysm | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_activation_potential_overlay.png) Activation Potential | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_sanguine_ward_overlay.png) Sanguine Ward |
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_venous_travel_overlay.png) Venous Travel | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_ferric_transmutation_overlay.png) Sanguine Alloy *(item id: memory_ferric_transmutation)* | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_living_blade_overlay.png) Living Blade | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_absorption_overlay.png) Blood Absorption |
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_projection_overlay.png) Blood Projection | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_summon_avatar_overlay.png) Summon Avatar | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_crimson_flame_conjuration_overlay.png) Crimson Flame Conjuration | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_lamp_overlay.png) Blood Lamp |
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_crimson_sight_overlay.png) Crimson Sight | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_crimson_harvest_overlay.png) Crimson Harvest | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_hemosynthesis_overlay.png) Hemosynthesis | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_pyretic_forge_overlay.png) Pyretic Forge |
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_vital_effusion_overlay.png) Vital Effusion | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_hemolymphal_pulse_overlay.png) Hemolymphal Pulse | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_vascular_dowsing_overlay.png) Vascular Dowsing | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_ferric_resonance_overlay.png) Ferric Resonance |
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_glacial_grasp_overlay.png) Glacial Grasp | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_sanguine_mending_overlay.png) Sanguine Mending | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_vital_reservoir_overlay.png) Vital Reservoir | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_sanguine_excavation_overlay.png) Sanguine Excavation |
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_umbral_step_overlay.png) Umbral Step | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_summon_thrall_overlay.png) Summon Thrall | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_cryogenic_pulse_overlay.png) Cryogenic Pulse | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_glacial_bastion_overlay.png) Glacial Bastion |
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_sanguine_ignition_overlay.png) Sanguine Ignition | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_vitric_combustion_overlay.png) Vitric Combustion | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_void_shroud_overlay.png) Void Shroud | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_eclipse_overlay.png) Blood Eclipse |
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_hemorrhage_overlay.png) Hemorrhage | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_exsanguinate_overlay.png) Exsanguinate | Memory Glacial Circulation *(overlay texture pending)* | Memory Osseous Bloom *(overlay texture pending)* |

**Saint Canon Memory Overlays (placeholder art — unique textures pending):**

| | | | |
|---|---|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_crimson_tithe_overlay.png) Crimson Tithe | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_unclosing_eye_overlay.png) Unclosing Eye | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_bloom_of_rot_overlay.png) Bloom of Rot | ![](../src/main/resources/assets/hemomancy/textures/item/memories/memory_endless_hour_overlay.png) Endless Hour |

> **Memory Overlay System:** Memory items use a layered model system: the base Hematic Memory texture is composited with per-memory overlays from `textures/item/memories/memory_*_overlay.png`. Most active memories have unique overlays; the gallery calls out remaining pending overlay art where applicable.

### 20.4 Diagnostic Items

| Item | Purpose |
|------|---------|
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_tendency_gauge.png) Blood Tendency Gauge | Inspect current blood tendency alignment |
| ![](../src/main/resources/assets/hemomancy/textures/item/vascular_status_gauge.png) Vascular Status Gauge | Inspect vein section health |
| ![](../src/main/resources/assets/hemomancy/textures/item/bloodline_pool_monitor.png) Bloodline Pool Monitor | View bloodline shared pool status |
| ![](../src/main/resources/assets/hemomancy/textures/item/self_reflection_mirror.png) Self Reflection Mirror | Scar-related inspection |

### 20.5 Miscellaneous

| Item | Purpose |
|------|---------|
| ![](../src/main/resources/assets/hemomancy/textures/item/charm_of_vascularium.png) Charm of Vascularium | Enables blood manipulations; its player render layer displays the item stack when equipped |
| ![](../src/main/resources/assets/hemomancy/textures/item/liber_sanguinum.png) Liber Sanguinum | Guide book |
| **Field Notes** | Stack-local memo notebook. Captures fleeting dialogue/memo events into `DataComponents.CUSTOM_DATA` (`Memos`, `RemainingMemos`, `InkPath`). Fresh notes have no prepared pages until filled with field ink. Hematic Field Ink binds the notes to Harbinger memos and Liber Sanguinum dictation; Pale Field Ink binds them to Unstained memos and Liber Immaculatus dictation. Each refill prepares 15 memo captures. Field Notes do not become their own Liber chapter; dictation unlocks normal book pages in the player's `LiberKnowledge` attachment. |
| **Hematic Field Ink** | Harbinger Field Notes refill item crafted from Dicentra Sap, Hematic Iron Powder, a water bottle, and an ink sac. |
| **Pale Field Ink** | Unstained Field Notes refill item crafted from Tears of Silthmere, Pale Distillate, a water bottle, and an ink sac. |
| ![](../src/main/resources/assets/hemomancy/textures/item/unsigned_ancestral_ledger.png) Unsigned Ancestral Ledger | Creates/joins bloodlines |
| **Hematic Suture Needle** | Direct blood routing tool. Degree 3+ binds blood-capable machines or Hematic Suture Nodes to the player; sneak-use on a bound link cycles nearby/sanctum/bloodline modes, and sneak-use in air toggles the player's bloodline routing opt-in. |
| ![](../src/main/resources/assets/hemomancy/textures/item/engram_stamp.png) Engram Stamp | Engram-related tool. Right-click on a solid surface (face-sturdy from above, empty block above) to place an engram block; right-click on an existing engram block to cycle its character. Consumes 1 durability per use. |
| **Scratch-Engraving (no stamp)** | Emergency / early-game method. Hold a sharp shard — `hemomancy:vivianite_cluster`, `minecraft:flint`, `minecraft:quartz`, or `hutoslib:obsidian_flakes` — in the main hand and right-click any solid surface (face-sturdy from above, empty block above). Places a random-character engram block at the cost of **1 heart (2 HP)** of generic damage. Creative players receive the engram without taking damage. Handled by `ScratchEngramHandler` (`@EventBusSubscriber` on `PlayerInteractEvent.RightClickBlock`). |
| ![](../src/main/resources/assets/hemomancy/textures/item/vivianite_scalpel.png) Vivianite Scalpel | Vivianite-based tool |
| ![](../src/main/resources/assets/hemomancy/textures/item/fungal_spine.png) Fungal Spine | Fungal tool item (unstackable, Uncommon) |
| **Qliphoth Seed** | Dropped by the Sanguine Monolith when shattered by a Degree-7 Archon (two interactions). Custom entity `EntityQliphothSeedItem`. Used as a placed catalyst inside the **Bloom of the Qliphoth** rite. One-time per monolith. |
| **Qliphoth Pome** | Edible fruit dropped by the Qliphoth Bloom tree over time (9 total per bloom lifecycle). Each pome tagged with `hemomancy:bloom_origin` + `hemomancy:husk_index` (0–8). On consumption, emits a player-centered black pulse via `SpawnPomePulsePacket`, grants +300 blood, Regeneration II (12 s), Darkness (7 s), and 25% manip cost reduction (3 min). Consuming all nine from one bloom sets `hemomancy:qliphoth_communion = true` and fires the Communion whisper. See §5.9. |
| ![](../src/main/resources/assets/hemomancy/textures/item/sanguine_salve.png) Sanguine Salve | Heals 25 blood on use |
| ![](../src/main/resources/assets/hemomancy/textures/item/cleansing_hemolymph.png) Cleansing Hemolymph | Blue vial from Hemolymphopoda mobs |
| ![](../src/main/resources/assets/hemomancy/textures/item/structure_spawner.png) Structure Spawner | Debug/creative item for spawning structures |
| ![](../src/main/resources/assets/hemomancy/textures/item/recycled_enzyme.png) Recycled Enzyme | Generic enzyme fallback |
| ![](../src/main/resources/assets/hemomancy/textures/item/debug_showcase_spawner.png) Debug Showcase | Creative-mode debug item (`DebugShowcaseItem`) — right-click to spawn a complete showcase area containing every Hemomancy feature organized into 4 sections: (1) All items in labeled chests, (2) All blocks placed on platforms, (3) All mob entities in fenced pens, (4) All blood structures and cardinal rites as placed patterns. |

### 20.6 Unstained Materials (Our Lady of Still Waters)

| Item | Purpose |
|------|---------|
| Tears of Silthmere | Distilled from Lethean Dew — used at the Altar of Cleansing for a one-time purity boost (+25) |
| ![](../src/main/resources/assets/hemomancy/textures/item/lethean_poppy_wreath.png) Lethean Poppy Wreath | Woven from Lethean Poppies — repeatable altar offering (+5 purity) |
| ![](../src/main/resources/assets/hemomancy/textures/item/silver_chalice.png) Silver Chalice | A ritual vessel of the Unstained — offered at the Altar of Cleansing for clarity (+5) |
| Pale Silver Bell | Handheld Unstained support equipment. Use grants short Silver Ward and weakens/slows nearby hostiles. |
| Lethean Chalice | Reusable still-water vessel. Use clears one harmful effect, extinguishes fire, grants brief regeneration, and adds Verdigris Aura after Clarity. |
| Verdigris Censer | Reusable oxidized-copper support tool. Use grants Verdigris Aura and marks nearby monsters or blood-active bodies with Glowing + Weakness. |
| Pale Humor Flask | Bottled White Humor from the Pallid Retort. Drink to replenish an active Unstained white humor reservoir, use with an Unstained weapon in the off hand to coat it with hemolytic charge, or pour into the world to create a finite White Humor source pool for purification recipes. |
| Tome of the Unstained | A book of Unstained scripture describing Our Lady of Still Waters and the path of purification |
| Icon of Our Lady | A rare relic depicting Our Lady of Still Waters — carved from pale silver, grants her protection |
| ![](../src/main/resources/assets/hemomancy/textures/item/pale_silver_ingot.png) Pale Silver Ingot | A refined metal sacred to the Unstained, used in crafting Unstained equipment |
| The Pale Distillate | Concentrated essence from Lethean Poppies, a crafting ingredient for Unstained recipes |
| ![](../src/main/resources/assets/hemomancy/textures/item/virid_salis_trail.png) Virid Salis | Verdigris-colored salt-ash used as the Unstained counterpart to ritual ash trails. **Harvested** by right-clicking any unwaxed oxidized/weathered/exposed copper block (plain, cut, stairs, or slab) with a vanilla brush — strips one oxidation step, drops 1 Virid Salis, costs 1 brush durability. Handled by `CopperBrushingHandler`. **Warding effect**: when placed as a trail (`hemomancy:virid_salis_trail`), any `Monster` mob that walks across it takes 1 magic damage per second (`ViridSalisTrailHandler`). Blood constructs and blood-type mobs (`IBloodConstruct`, `HematicConstructEntity`, `CruorFiendEntity`, `FrozenClotEntity`, `BloodDrunkPuppeteerEntity`, `ThirsterEntity`, `AbyssalSiphonEntity`, `LeechEntity`, `VenousStriderEntity`) take 2 magic damage per second and receive Slowness II for 3 seconds. **Player effect**: Harbinger players at Initiatory Degree 5 (Perfected) or higher take 1 magic damage per second and receive Slowness I for 3 seconds when crossing the trail. |

### 20.7 Food Items

| Item | Purpose |
|------|---------|
| ![](../src/main/resources/assets/hemomancy/textures/item/gourd_slice.png) Gourd Slice | Edible gourd food item |
| ![](../src/main/resources/assets/hemomancy/textures/item/gourd_stew.png) Gourd Stew | Stew crafted from gourd and other ingredients |
| Roasted Gourd Seeds | Smelted/smoked/campfire-cooked gourd seeds (3 cooking methods) |

### 20.8 Organ Echo Items

Produced by the **Visceral Mirror** ritual (requires Degree 3+). Spectral imprints of the player's organs — bound to the player (dissolve if placed in non-player inventory), only one per organ type can exist at a time. Organ "Tier" indicates risk level and degree requirement for extraction:

| Item | Organ | Tier | Notes |
|------|-------|------|-------|
| Echo of Spleen | `SPLEEN` | 3 | Governs blood volume and filtration |
| Echo of Liver | `LIVER` | 3 | Metabolizes toxins and purifies the blood |
| Echo of Lungs | `LUNGS` | 3 | Oxygenates blood and sustains vital rhythm |
| Echo of Kidneys | `KIDNEYS` | 3 | Filters impurities and maintains humoral balance |
| Echo of Heart | `HEART` | 4 | The seat of circulation and will — highest risk, requires Degree 4+ |

> **Status: Implemented.** Organ extraction ritual (Visceral Mirror → cycle organs → confirm → produce Echo items) and all per-organ gameplay effects are fully implemented in `VisceralOrgansEvents` (player tick + capability check): **Spleen** +1000 max blood per organ level (announces expansion on first reach); **Liver** removes Poison (level 2+) and Wither (level 3+) on tick; **Lungs** grants Water Breathing (100×level ticks) while underwater; **Kidneys** grants Regeneration at (level-1) amplifier normally, **level amplifier** during a Blood Moon (overclocked filtration); **Heart** grants Damage Resistance (capped at Resistance II), **Wither immunity at level 3** (Cardiac Autonomy mastered), and drains 10÷level blood per 2 s tick. **Iron Brazier reagent system is organ-specific:** each organ requires its own reagent type — Heart=`blood_crystal_shard`, Spleen=`vivianite_cluster`, Lungs=`fervent_husk`, Kidneys=`consecrated_copper_ingot`, Liver=`dicentra_sap`. The three reagents must all be the same type; the brazier records the locked organ and validates the echo matches before consuming it. See §20.8 and `IronBrazierBlockEntity`.

### 20.9 Banner Patterns

- ![](../src/main/resources/assets/hemomancy/textures/item/heart_pattern.png) **Heart Pattern** — Vascularium Crest
- ![](../src/main/resources/assets/hemomancy/textures/item/veins_pattern.png) **Veins Pattern** — Vein Border

---

## 21. Tools & Weapons

### 21.1 Tool Tiers

| Tier | Enum |
|------|------|
| Hematic Iron | `HEMATIC_IRON` |
| Living | `LIVING` |

### 21.2 Living Tools (Blood-powered)

All are single-stack, use the `LIVING` tool tier:

| Weapon | Class | Notes |
|--------|-------|-------|
| Living Blade | `LivingBladeItem` | Blood-feeding sword (25 base dmg, +3 speed) |
| Living Axe | `LivingAxeItem` | Blood-feeding axe |
| Living Spear | `LivingSpearItem` | Blood-feeding polearm |
| Living Baghnakh | `LivingBaghnakhItem` | Blood-feeding claw weapon |
| Living Staff | `LivingStaffItem` | Channels morphlings and blood magic |
| Living Syringe | `LivingSyringeItem` | Extracts blood vials from mobs into a loaded Vial Rack (Shift to eject rack) |
| Living Crossbow | `LivingCrossbowItem` | Fires Blood Bolts |
| Sanguis Lancea | `SanguisLanceaItem` | Throwable blood lance (25 base dmg) |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_absorption.png) Blood Absorption | `BloodAbsorptionItem` | Conjured blood-absorbing tool |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_projection.png) Blood Projection | `BloodProjectionItem` | Conjured blood projectile launcher |

> *Note: Living tools (blade, axe, spear, staff, syringe, crossbow, lancea, baghnakh) use 3D entity models rather than flat item textures — see `src/main/resources/assets/hemomancy/textures/entity/` for their model textures:*
>
> ![](../src/main/resources/assets/hemomancy/textures/entity/model_living_blade_hand.png) ![](../src/main/resources/assets/hemomancy/textures/entity/model_living_axe_hand.png) ![](../src/main/resources/assets/hemomancy/textures/entity/model_living_spear_hand.png)

### 21.3 Hematic Iron Weapons

| Weapon | Notes |
|--------|-------|
| ![](../src/main/resources/assets/hemomancy/textures/item/hematic_iron_sword.png) Hematic Iron Sword | Standard sword tier |
| ![](../src/main/resources/assets/hemomancy/textures/item/hematic_iron_knapper.png) Hematic Iron Knapper | Specialized knapping tool (42 dmg) |

### 21.4 Other Weapons

| Weapon | Notes |
|--------|-------|
| Barbed Blade | Sword-class, Living tier, +3 speed, +25 dmg |
| Chitinite Mace | Sword-class, Living tier |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_bolt.png) Blood Bolt | Ammo for Living Crossbow |
| ![](../src/main/resources/assets/hemomancy/textures/item/blood_thrall_effigy.png) Blood Thrall Effigy | Summons a Blood Thrall creature (stackable to 16) |

---

## 22. Armor Sets

### 22.1 Hematic Iron Armor

Standard blood-infused iron armor set (fire resistant):
- ![](../src/main/resources/assets/hemomancy/textures/item/hematic_iron_helm.png) Helm, ![](../src/main/resources/assets/hemomancy/textures/item/hematic_iron_chestplate.png) Chestplate, ![](../src/main/resources/assets/hemomancy/textures/item/hematic_iron_leggings.png) Leggings, ![](../src/main/resources/assets/hemomancy/textures/item/hematic_iron_boots.png) Boots
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Hematic Iron Scrap
- **Set Bonus (4 pieces):** Passive blood regeneration — +2 blood/second while wearing full set

> Armor model: ![](../src/main/resources/assets/hemomancy/textures/models/armor/hematic_iron_layer_1.png) ![](../src/main/resources/assets/hemomancy/textures/models/armor/hematic_iron_layer_2.png)

### 22.2 Blood Lust Armor

Special armor with mask variants:
- ![](../src/main/resources/assets/hemomancy/textures/item/blood_lust_helm.png) Helm (no mask), ![](../src/main/resources/assets/hemomancy/textures/item/blood_lust_helm_tengu.png) Helm (Tengu mask), ![](../src/main/resources/assets/hemomancy/textures/item/blood_lust_helm_horned.png) Helm (Horned mask)
- ![](../src/main/resources/assets/hemomancy/textures/item/blood_lust_chest.png) Chestplate, ![](../src/main/resources/assets/hemomancy/textures/item/blood_lust_legs.png) Leggings, ![](../src/main/resources/assets/hemomancy/textures/item/blood_lust_boots.png) Boots
- Mask items: ![](../src/main/resources/assets/hemomancy/textures/item/tengu_mask.png) Tengu Mask, ![](../src/main/resources/assets/hemomancy/textures/item/horned_mask.png) Horned Mask (crafting ingredients)
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Hematic Iron Scrap
- **Set Bonus (4 pieces):** Lifesteal — 10% of direct melee damage dealt heals the player

> Armor model: ![](../src/main/resources/assets/hemomancy/textures/models/armor/blood_lust_layer_1.png) ![](../src/main/resources/assets/hemomancy/textures/models/armor/blood_lust_layer_2.png)

### 22.3 Barbed Armor

Defensive barbed armor set:
- ![](../src/main/resources/assets/hemomancy/textures/item/barbed_helm.png) Helm, ![](../src/main/resources/assets/hemomancy/textures/item/barbed_chestplate.png) Chestplate, ![](../src/main/resources/assets/hemomancy/textures/item/barbed_leggings.png) Leggings, ![](../src/main/resources/assets/hemomancy/textures/item/barbed_boots.png) Boots
- Barbed Shield ![](../src/main/resources/assets/hemomancy/textures/entity/barbed_shield/model_barbed_shield.png)
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Chitinous Husk
- **Set Bonus (4 pieces):** Thorns — attackers take 2 damage and receive Blood Loss effect (3 seconds)

> Armor model: ![](../src/main/resources/assets/hemomancy/textures/models/armor/barbed_layer_1.png) ![](../src/main/resources/assets/hemomancy/textures/models/armor/barbed_layer_2.png)

### 22.4 Chitinite Armor

Insectoid/chitin-based armor:
- ![](../src/main/resources/assets/hemomancy/textures/item/chitinite_helm.png) Helm, ![](../src/main/resources/assets/hemomancy/textures/item/chitinite_chestplate.png) Chestplate, ![](../src/main/resources/assets/hemomancy/textures/item/chitinite_leggings.png) Leggings, ![](../src/main/resources/assets/hemomancy/textures/item/chitinite_boots.png) Boots
- Chitinite Shield ![](../src/main/resources/assets/hemomancy/textures/entity/chitinite_shield/model_chitinite_shield.png)
- Chitinite Arm Banner (dyeable, 16 colors)
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Chitinous Husk
- **Set Bonus (4 pieces):** +2.0 Armor Toughness (via attribute modifier) and 25% projectile damage reduction

> Armor model: ![](../src/main/resources/assets/hemomancy/textures/models/armor/chitinite_layer_1.png) ![](../src/main/resources/assets/hemomancy/textures/models/armor/chitinite_layer_2.png)

### 22.5 Unstained Armor

Anti-blood zealot armor (for the Unstained path):
- ![](../src/main/resources/assets/hemomancy/textures/item/unstained_helm.png) Helm, ![](../src/main/resources/assets/hemomancy/textures/item/unstained_chestplate.png) Chestplate, ![](../src/main/resources/assets/hemomancy/textures/item/unstained_leggings.png) Leggings, ![](../src/main/resources/assets/hemomancy/textures/item/unstained_boots.png) Boots
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Chitinous Husk (placeholder — should be Pale Silver Ingot or Consecrated Copper)
- **Set Bonus (4 pieces):** Immunity to Blood Loss and Hemolysis effects (auto-removed on tick)

> Armor model: ![](../src/main/resources/assets/hemomancy/textures/models/armor/unstained_layer_1.png) ![](../src/main/resources/assets/hemomancy/textures/models/armor/unstained_layer_2.png)

### 22.6 Crown of Sacred Marrow

Special artifact helmet (`MarrowCrownArmorItem`), uses `MARROW_CROWN` tier.
- **Stats:** Same as Hematic Iron (Defense 3/6/8/3, Toughness 3.0, KB Resist 0.1)
- **Repair:** Hematic Iron Scrap
- **Artifact Bonus:** +10% melee damage (via attribute modifier) when blood volume is above 50%

> **Note:** All armor sets share identical base stat distributions (equivalent to Netherite-tier defense/toughness) but each has a unique set bonus implemented in `ArmorSetBonusHandler`. The Marrow Crown is an artifact helmet with its own standalone bonus that doesn't require a full set.

---

## 23. Functional Blocks & Block Entities

| Block                                | BlockEntity                                | Purpose                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|--------------------------------------|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Mortal Display**                   | `MortalDisplayBlockEntity`                 | Activates blood magic when clicked in a Blood Temple ![](../src/main/resources/assets/hemomancy/textures/entity/model_floating_heart.png)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **Scrying Podium**                   | `ScryingPodiumBlockEntity`                 | Opens the Charm/Gourd slot screen for equipping the Charm of Vascularium and Blood Gourds                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| **Somatic Loom**                     | `SomaticLoomBlockEntity`                   | Crafting station for creating Hematic Memories using enzymes, blank memories, and catalysts                  ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/somatic_loom.png)                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Puppeteer's Spindle**              | `PuppeteersSpindleBlockEntity`             | Harbinger puppeteer control station. Two-slot container: slot 0 accepts a `marionette_crossbar`, slot 1 accepts `puppeteering_thread`. Thread inserted into the feeder slot is consumed immediately into a persistent `threadBuffer` capped at 512, and the slotted crossbar auto-fills from that buffer up to its 256-thread cap. The screen handles summon selection, crossbar binding/attunement, and call/recall preparation for the slotted crossbar. The placed block stores horizontal facing, faces the placer, and renders through `PuppeteersSpindleRenderer` / `PuppeteersSpindleModel` plus a custom block item renderer. |
| **Vial Centrifuge**                  | `VialCentrifugeBlockEntity`                | Spins down Bloody Vials into enzymes and Hematic Iron Powder. Reworked with new 3D stand model (`CentrifugeStandModel`), custom block entity renderer (`VialCentrifugeRenderer`), and `VialCentrifugeBlockItem` with custom item renderer. Accepts **Vial Rack** right-click bulk inserts, and startup now requires at least one processable vial with valid output fit. ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/vial_centrifuge.png)                                                                                                                                                                                                                                   |
| **ghastly_alembic**                  | `GhastlyAlembicBlockEntity`                | Squeezes items to extract blood (requires fire below). Has 4 slots: Input (slot 0), Flask (slot 1, fills Cured Clay Flasks into Bloody Flasks), Result (slot 2), and **Catalyst (slot 3)** — an optional catalyst ingredient that modifies or enhances the recipe output. Hopper access: top → input, bottom → result, sides → flask + catalyst. Renders via custom `GhastlyAlembicRenderer` (3D entity model `GhastlyAlembicModel`, facing-aware)![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/ghastly_alembic.png)   .                                                                                                                           |
> **Ghastly Alembic gourd filling:** The alembic's result/blood output slot also accepts Blood Gourds. When a gourd is placed there, the block entity drains stored blood from its internal tank into the gourd's stack-backed internal blood volume instead of producing bottled blood in that slot.
> **Ghastly Alembic blood seep:** On each configured leak interval, the alembic scans venous stone variants or bone blocks in the surrounding 3x3 floor below it, skipping the center tile occupied by the alembic itself. It first places a fresh Blood Crystal Bud in the first valid open space on top of a trigger tile; only if no placement is possible does it scan existing buds on those trigger tiles and grow the first immature one. If neither placement nor growth can occur, no blood is drained and the next interval starts a fresh search.

| **Cerebral Scarring Station**        | `ScarStationBlockEntity`                   | Crafts scars from patterns and blanks                    ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/scar_station.png)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **Morphling Incubator**              | `MorphlingIncubatorBlockEntity`            | Grows Morphling Polyps into specific morphling types with enzymes. Has 8 slots: Center/polyp (slot 0), 4 enzyme/catalyst slots (1–4), Output (slot 5), Blood Flask/Gourd input (slot 6), and Empty Flask output (slot 7). Craft time: 200 ticks base; enzyme feeding: 100 + 60 per item. Blood cost: 0.5/tick. Bloody Flask transfer is clamped to available player blood capacity (prevents overfill blocking). Uses `IncubatorRecipe` system with 13 recipes (one per morphling type). JEI-integrated. Renders via custom `MorphlingIncubatorRenderer` (3D entity model). ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/morphling_incubator.png) 
| **Mycelial Crucible**                | `MycelialCrucibleBlockEntity`              | Cultivates fungal scars through `FungalScarCultivationRecipe`. Has 8 slots: center scar/immature culture, 4 aligned enzyme slots, output, blood flask/gourd input, and empty flask output. Phase 1 drains the recipe's flat blood cost plus 1.5/tick to produce the consolidated `immature_fungal_scar`; Phase 2 feeds aligned enzymes into the culture's custom-data progress until it matures into its stored target scar. See §13.4. |
| **Mycelial Lantern**                 | `MycelialLanternBlockEntity`               | Degree 5 passive enzyme-fruiting machine. A 1x2x1 multiblock (main block below, `filler_block` above) crafted via Blood Structure recipe. Slots: reusable spore culture (0), blood input for Bloody Flasks/Blood Gourds (1), enzyme output (2), empty flask output (3). Uses a 4,000 blood internal reservoir; each default recipe takes 2,400 ticks at 0.25 blood/tick (600 total) for 1 matching enzyme. Progress pauses without reset when blood or output space is unavailable. Automation: top inserts culture, sides insert blood containers, bottom extracts enzyme/empty containers; culture is not auto-extracted. Rendered by `MycelialLanternRenderer` / `MycelialLanternModel`, with translucent glass rendered after the displayed culture/output item and Blockbench source at `assets/hemomancy/models/block/bbmodel/mycelial_lantern.bbmodel`. |
| **Morphling Cradle**                 | `MorphlingCradleBlockEntity`               | Owner-bound morphling support cradle. Hosts one morphling, runs staged aura/leech logic, and can route blood through internal buffer / owner / bloodline fallback. Supports floor, wall, and ceiling placement. Rendered with custom block entity + item renderers (`MorphlingCradleRenderer`, `MorphlingCradleItemRenderer`). |
| **Specimen Jar**                     | `SpecimenJarBlockEntity`                   | Vivianite glass and Hematic Iron containment jar for Hemomancy arthropod specimens. Empty jars place normally and face the placer. Right-clicking a capturable Hemomancy arthropod with an empty jar stores that exact entity's NBT in the jar item and removes the live mob. Filled jars place with the specimen displayed inside by `SpecimenJarRenderer` / `SpecimenJarItemRenderer`, rotated with the jar's horizontal facing and animated via the renderer's client-only entity copy. Shift-right-clicking a placed jar picks it back up without releasing the specimen; breaking a filled jar releases the stored entity and drops an empty jar. Capturable scope is data-driven by `data/hemomancy/tags/entity_types/specimen_jar_capturable.json` and currently includes Chthonian, Chthonian Queen, Chitinite, Fervent Chitinite, Hemolymphopoda, Myelin Borer, Fargone, and Tooth Pecks. |
| **Fungal Podium**                    | `FungalPodiumBlockEntity`                  | Portal to the Fungal Gardens dimension. Degree 2+ (Votary) required; costs 500 blood. Stores overworld return coordinates in player persistent data. Degree-7 Archons on first exit attempt see the `coreWitnessDialogue()` choice fork instead of teleporting home; subsequent uses proceed directly. See §5.6, §5.9.                                                                                                                                                                                                                                                                                                                                               |
| **Sanguine Monolith** (*The Crimson Lodestone*) | `SanguineMonolithBlockEntity` | 1×2 multiblock (base + filler above) available to Degree 5+ players. Provides degree-gated guidance (degrees 4–7) via `SanguineMonolithDialogueTrees`. The dialogue speaker is displayed as **"The Crimson Lodestone"** (`hemomancy.monolith.lodestone_name`). Each degree includes a `what_are_you` branch that progressively discloses the Monolith's nature: a sealed incubation vessel containing a dormant mycelial fragment built by the Crimson Lodge. At Degree 7 the player can press further for the pre-shatter warning (`press_again` node). At Degree 7 an Archon may interact with it **twice** to shatter it — rendering black shards plus a black orb blast client-side, dropping a **Qliphoth Seed**, and firing `FungalWhisperDialogueTrees.postMonolithShatter()`. The first step of Qliphoth Communion. Custom animated model (`SanguineMonolithModel`). See §5.9 and LORE_REFERENCE §6.5a. |
| **Qliphoth Bloom**                   | `QliphothBloomBlockEntity`                 | 1×1×8 multiblock tree (base + 7 filler blocks) placed by the Bloom of the Qliphoth rite. Stores owner UUID and chunk radius. Effects (Regeneration I, +5 blood/tick) are tick-driven via `QliphothBloomEvents`. Slowly drops 9 Qliphoth Pomes over its lifetime — one per Qliphoth husk (Nahemoth → Ghagiel), with owner whisper alerts on each drop. Registered and synced via `QliphothBloomSavedData`. Player breaking is canceled for the bloom and its filler shell; intended removal is the Rite of Cult Pruning. See §5.9.                                                                                                                                                                                                                       |
| **Fungal Implantation Pylon**        | `FungalImplantationPylonBlockEntity`       | Sporic implantation station ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/fungal_implant.png)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| **Dendritic Distributor**            | `DendriticDistributorBlockEntity`          | Opens the Skill Tree / Manipulation Tree screen                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| **Unstained Podium**                 | `UnstainedPodiumBlockEntity`               | Central interaction block for the Unstained path. Four recognized interaction modes (server-side only, degree-gated at > Illuminatus): **Hemolytic Solution** — first use begins purification (`begunPurification = true`, +5 purity, resets Harbinger degree); subsequent uses add +10 purity per flask while unpurified. **Consecrated Copper Ingot** — requires `isPurified() == true` and `!hasClarityUnlocked()`; performs the Rite of Clarity: sets `clarityUnlocked = true`, disables blood magic permanently (`IBloodVolume.active = false`), grants first Still Art (Silver Rebuke), runs `enforceHarbingerResetOnClarity()`, and syncs both capabilities. **Hemolytic Plating** — requires `hasClarityUnlocked()`; adds +15 clarity per plating while not yet enlightened. **Empty hand** — prints current purity stage + percent; if clarity is unlocked, also prints clarity stage + percent. Scrying Dish item converts the podium into a Scrying Podium. |
| **Altar of Cleansing**               | `AltarOfCleansingBlockEntity`              | Sacred altar of Our Lady of Still Waters — grants one-time purity boost with Tears of Silthmere; accepts Lethean Poppy Wreaths and Silver Chalices for repeatable offerings                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| **Semi-Sentient Construct**          | `SemiSentientConstructBlockEntity`         | Blood construct-related block and Drudge home anchor; nearby Drudges can tend linked direct-routing machines around their SSC without creating blood                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **Hematic Suture Node**              | `HematicSutureNodeBlockEntity`             | Optional direct-routing anchor. Stores its link in `BloodRoutingSavedData`, holds no persistent blood/reservoir, emits red routing particles, and routes adjacent linked machines from the bound source contract                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Earthen Vein**                     | `EarthenVeinBlockEntity`                   | Vein location marker for teleportation (Venous Travel) ![](../src/main/resources/assets/hemomancy/textures/entity/earthen_vein/model_earthen_vein.png)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **Iron Brazier**                     | `IronBrazierBlockEntity`                   | Decorative/functional brazier                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| **Suspended Blood Crystal**          | `SuspendedBloodCrystalBlockEntity`         | Floating blood crystal display ![](../src/main/resources/assets/hemomancy/textures/entity/model_suspended_blood_crystal.png)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| **Suspended Cleansed Blood Crystal** | `SuspendedCleansedBloodCrystalBlockEntity` | Floating cleansed blood crystal display (purified variant with random time offset animation) ![](../src/main/resources/assets/hemomancy/textures/entity/model_suspended_cleansed_blood_crystal.png)                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| **Suspended Vivianite**              | `SuspendedVivianiteBlockEntity`            | Floating vivianite display ![](../src/main/resources/assets/hemomancy/textures/entity/model_suspended_vivianite.png)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **Mnemonic Reliquary**               | `MnemonicReliquaryBlockEntity`             | Animated decorative/lore reliquary with opening/closing lid. Tracks open count, syncs lid angle (lerped). Has custom 3D block entity renderer and item renderer. Opened via `MnemonicReliquaryMenu`. Currently atmospheric/decorative — no inventory slots or crafting function yet. Planned: may serve as memory storage or manipulation bookmark container. ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/mnemonic_reliquary.png)                                                                                                                                                                                                                |
| **Dictation Table**                  | `DictationTableBlockEntity`                | First implementation slice of the memo loop. Holds one Liber Sanguinum or Liber Immaculatus stack and renders an open book while one is inserted. Right-click with Field Notes to dictate captured memo IDs into the player's `LiberKnowledge` attachment, draining player blood with a cost that scales by memo count. Hematic-ink notes can only be dictated into Liber Sanguinum; Pale-ink notes can only be dictated into Liber Immaculatus. Memo entries unlock pages inside the normal book chapters for that player; chapters with zero unlocked pages are hidden entirely. The table is only one discovery source; rites, degree gains, advancement grants, item pickups, and special dialogue events can also unlock Liber pages. |
| **Humane Idol**                      | `HumaneIdolBlockEntity`                    | Idol block                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **Serpentine Idol**                  | `SerpentineIdolBlockEntity`                | Idol block                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **Engram Block**                     | —                                          | Translucent engram. Emits redstone comparator signal 15 when lit (LIT=true), 0 when unlit. `hasAnalogOutputSignal()` returns true.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| **Filler Block**                     | `FillerBlockEntity`                        | Indestructible filler for multiblocks                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| **Bog Body**                         | —                                          | Decorative translucent body block                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **Visceral Mirror**                  | `VisceralMirrorBlockEntity`                | Ritualistic mirror for organ extraction — gaze into your reflection to extract and modify organs (Spleen, Liver, Lungs, Kidneys, Heart). Requires degree 3+. Cycle organs (right-click) → confirm extraction (sneak right-click). Produces Organ Echo items.    ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/mirror.png)                                                                                                                                                                                                                                                                                                                                                                                                        |

---

## 24. Decorative & Building Blocks

### 24.1 Venous Stone Family

A full block family with variants:

| | | |
|---|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/block/venous_stone.png) Venous Stone | ![](../src/main/resources/assets/hemomancy/textures/block/polished_venous_stone.png) Polished Venous Stone | ![](../src/main/resources/assets/hemomancy/textures/block/polished_venous_stone_bricks.png) Polished Venous Stone Bricks |
| ![](../src/main/resources/assets/hemomancy/textures/block/chiseled_polished_venous_stone.png) Chiseled Polished | ![](../src/main/resources/assets/hemomancy/textures/block/cracked_polished_venous_stone_bricks.png) Cracked Bricks | ![](../src/main/resources/assets/hemomancy/textures/block/gilded_venous_stone.png) Gilded Venous Stone |
| ![](../src/main/resources/assets/hemomancy/textures/block/infested_venous_stone.png) Infested Venous Stone | | |

- Venous Stone, Slab, Stairs
- Polished Venous Stone, Slab, Stairs
- Polished Venous Stone Bricks, Slab, Stairs
- Chiseled Polished Venous Stone
- Cracked Polished Venous Stone Bricks
- Gilded Venous Stone
- Infested Venous Stone

### 24.2 Hematic Iron Family

| | | |
|---|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/block/hematic_iron_block.png) Hematic Iron Block | ![](../src/main/resources/assets/hemomancy/textures/block/hematic_iron_pillar.png) Hematic Iron Pillar | ![](../src/main/resources/assets/hemomancy/textures/block/chiseled_hematic_iron_block.png) Chiseled Hematic Iron |

- Hematic Iron Block
- Hematic Iron Pillar (rotatable)
- Chiseled Hematic Iron Block

### 24.3 Anti-Blood / Unstained

- ![](../src/main/resources/assets/hemomancy/textures/block/hemolytic_plating_block.png) Hemolytic Plating Block
- Cleansed Stone — pale, smooth stone found in Unstained temples
- Pallid Lantern — softly glowing lantern sacred to Our Lady of Still Waters
- Virid Salis Trail — green Unstained salt-ash trail block placed by `hemomancy:virid_salis` / `hemomancy:virid_salis_trail`

### 24.4 Glass & Panes

| | | | |
|---|---|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/block/sanguine_glass.png) Sanguine Glass | ![](../src/main/resources/assets/hemomancy/textures/block/sanguine_pane.png) Sanguine Pane | ![](../src/main/resources/assets/hemomancy/textures/block/vivianite_glass.png) Vivianite Glass | ![](../src/main/resources/assets/hemomancy/textures/block/vivianite_pane.png) Vivianite Pane |
| ![](../src/main/resources/assets/hemomancy/textures/block/cleansed_sanguine_glass.png) Cleansed Sanguine Glass | ![](../src/main/resources/assets/hemomancy/textures/block/cleansed_sanguine_pane.png) Cleansed Sanguine Pane | | |

### 24.5 Wood & Organic

| | | |
|---|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/block/blood_wood_log.png) Blood Wood Log | ![](../src/main/resources/assets/hemomancy/textures/block/blood_wood_planks.png) Blood Wood Planks | ![](../src/main/resources/assets/hemomancy/textures/block/conscious_mass.png) Conscious Mass |

- Blood Wood Log (rotatable pillar)
- Blood Wood Planks
- Conscious Mass (wart-block sound)

### 24.6 Fungal / Plant Blocks

| | | | |
|---|---|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/block/hyphae.png) Hyphae | ![](../src/main/resources/assets/hemomancy/textures/block/hyphae_block.png) Hyphae Block | ![](../src/main/resources/assets/hemomancy/textures/block/infected_stem.png) Infected Stem | ![](../src/main/resources/assets/hemomancy/textures/block/infected_cap.png) Infected Cap |
| ![](../src/main/resources/assets/hemomancy/textures/block/fruiting_infected_cap.png) Fruiting Infected Cap | ![](../src/main/resources/assets/hemomancy/textures/block/erythrocytic_dirt.png) Erythrocytic Dirt | ![](../src/main/resources/assets/hemomancy/textures/block/erythrocytic_mycelium_top.png) Erythrocytic Mycelium | ![](../src/main/resources/assets/hemomancy/textures/block/bleeding_heart.png) Bleeding Heart |
| ![](../src/main/resources/assets/hemomancy/textures/block/infected_fungus.png) Infected Fungus | ![](../src/main/resources/assets/hemomancy/textures/block/stinkhorn_fungus.png) Stinkhorn Fungus | ![](../src/main/resources/assets/hemomancy/textures/block/lethean_poppy_bloomed.png) Lethean Poppy | |

- Hyphae (cross-block, replaceable — crafts into Spore Sac)
- Hyphae Block
- Infected Stem
- Infected Cap / Fruiting Infected Cap
- Erythrocytic Dirt
- Erythrocytic Mycelium (spreads, random ticks)
- Bleeding Heart (flower, Absorption effect — crafts Dicentra Sap, brews Potion of Sanguine Siphon)
- Infected Fungus (flower, Confusion effect — ghastly_alembic → Foul Paste, brews Potion of Mycorrhizal Mending, incubator catalyst for Fungal Morphling)
- Stinkhorn Fungus (Confusion effect — ghastly_alembic → Foul Paste, brews Potion of Blood Binding)
- Puffball Fungus (Saturation effect, **Unstained** — ghastly_alembic → Spore Sac, incubator catalyst for Fungal Morphling)
- Lethean Poppy (Regeneration effect, random ticks, **Unstained** — ghastly_alembic → Lethean Dew, crafts Lethean Poppy Wreath)
- Ghost Pipe (myco-heterotrophic, Night Vision effect, **Unstained** — ghastly_alembic → The Pale Distillate)
- Sarcodes (myco-heterotrophic, Regeneration effect — ghastly_alembic → Dicentra Sap, brews Potion of Blood Rush)
- Rafflesia (parasitic, Confusion effect — ghastly_alembic → Spore Sac, brews Potion of Hemolysis)

All applicable flowers have **potted** variants.

### 24.7 Gourd

| | |
|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/block/gourd_side.png) Gourd | ![](../src/main/resources/assets/hemomancy/textures/block/gourd_stem.png) Gourd Stem |

- Gourd (pumpkin-like, grows from stem)
- Gourd Stem / Attached Gourd Stem

### 24.8 Ash Trails

| | | | | |
|---|---|---|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/item/smouldering_ash_trail.png) Smouldering Ash | ![](../src/main/resources/assets/hemomancy/textures/item/active_smouldering_ash.png) Active Smouldering | ![](../src/main/resources/assets/hemomancy/textures/item/befouling_ash_trail.png) Befouling Ash | ![](../src/main/resources/assets/hemomancy/textures/item/active_befouling_ash.png) Active Befouling | ![](../src/main/resources/assets/hemomancy/textures/item/virid_salis_trail.png) Virid Salis |

- Smouldering Ash Trail / Active Smouldering Ash Trail
- Befouling Ash Trail / Active Befouling Ash Trail
- Virid Salis Trail (Unstained-aligned; currently no active variant)

### 24.9 Misc

- ![](../src/main/resources/assets/hemomancy/textures/block/crimson_flames.png) Crimson Flames (special fire block)
- Blood Crystal (modeled block)

---

## 25. Recipe Systems

This section tracks shared recipe infrastructure and Harbinger-facing recipe catalogs. Unstained Still Arts, Cardinal Rites, White Humor purification, and Unstained structure recipes live in §15 so those lanes stay separate from the blood-magic path.

**Shared and Harbinger-facing recipe types:**

| Recipe Type | Serializer | Station | Purpose |
|-------------|-----------|---------|---------|
| `scar_recipe` | `ScarRecipeSerializer` | Cerebral Scarring Station | Crafting scars |
| `distillation_recipe` | `DistillationRecipeSerializer` | Ghastly Alembic / Pallid Retort | Shared distillation serializer. Ghastly Alembic is the Harbinger station; `pallid: true` routes to the Pallid Retort and is cataloged with Unstained crafting in §15.3. |
| `recaller_recipe_type` | `RecallerRecipeSerializer` | Visceral Recaller | Creating Hematic Memories |
| `incubator_recipe_type` | `IncubatorRecipeSerializer` | Morphling Incubator | Growing Morphling Polyps into specific morphlings using enzyme catalysts (13 morphling recipes). JEI-integrated via `IncubatorRecipeCategory`. Fungal scar crafting has moved out to the Mycelial Crucible. |
| `fungal_scar_cultivation` | `FungalScarCultivationSerializer` | Mycelial Crucible | Two-phase fungal scar cultivation. Phase 1 produces `immature_fungal_scar`; Phase 2 matures the culture with aligned enzymes into one of 9 finished `ItemFungalScar` variants. |
| `enzyme_fruiting` | `EnzymeFruitingRecipeSerializer` | Mycelial Lantern | Reusable aligned spore culture + blood -> matching enzyme. Defaults: 2,400 ticks, 0.25 blood/tick, 600 total blood, output count 1; JSON-tunable per recipe. |
| `blood_structure_recipe` | `BloodStructureRecipeSerializer` | In-world structure | Harbinger structure crafting; Unstained entries that share the serializer are cataloged in §15.3. |
| `puppeteer_trial_recipe` | `PuppeteerTrialRecipeSerializer` | In-world Blood Crafting pattern | Instant summon unlock trials. Uses Blood Structure-style pattern matching, held Sanguine Quintessence catalyst, blood drain, pattern consumption, and unbound hostile summon boss spawn. |
| `cardinal_rite_recipe` | `CardinalRiteRecipeSerializer` | Multiblock | Harbinger Cardinal Rites for degree advancement and blood utility rites; Unstained rites are cataloged in §15.2. |
| Morphling Jar Upgrade | `CopyMorphlingJarRecipe.Serializer` | Crafting | Upgrading morphling jars |
| Blood Gourd Upgrade | `CopyBloodGourdRecipe.Serializer` | Crafting | Upgrading blood gourds |
| Blood Gourd Fill | `FillBloodGourdRecipe.Serializer` | Crafting | Filling gourds with blood |
| Vial Rack | Vanilla shaped recipe | Crafting | 8 Bloody Vials + Hematic Iron Scrap → Vial Rack |

**Unstained recipe lanes:**

| Lane | Data/type | System reference |
|---|---|---|
| Unstained structure recipes | `blood_structure_recipe` entries with `unstained: true` | §15.3 |
| Unstained Cardinal Rites | `cardinal_rite_recipe` entries with `bloodCost: 0` and Unstained `required_degree` gates | §15.2 |
| White Humor Purification | `white_humor_purification`, physical White Humor pools | §15.4 |
| Pallid Retort distillation | `distillation_recipe` entries with `pallid: true` | §15.3 |
| Unstained material/tool crafting | vanilla shaped/shapeless recipes under `data/hemomancy/recipe/` | §15.3 |

Current datapack paths use the 1.21 singular directory names already present in this repository.

**Shared / Harbinger data paths:**

| Content | Path |
|---|---|
| Main recipes | `src/main/resources/data/hemomancy/recipe/` |
| Harbinger Blood Structure recipes | `src/main/resources/data/hemomancy/recipe/blood_structure/` |
| Harbinger Cardinal Rites | `src/main/resources/data/hemomancy/recipe/cardinal_rite/` |
| Puppeteer trials | `src/main/resources/data/hemomancy/recipe/puppeteer_trial/` |
| Enzyme fruiting | `src/main/resources/data/hemomancy/recipe/enzyme_fruiting/` |
| Entity loot | `src/main/resources/data/hemomancy/loot_table/entities/` |
| Item inquiry dialogue | `src/main/resources/data/hemomancy/dialogue_inquiry/<npc>/<namespace>/<item>.json` |

**Unstained data paths:**

| Content | Path |
|---|---|
| Unstained Blood Structure entries | `src/main/resources/data/hemomancy/recipe/blood_structure/` (`unstained: true`) |
| Unstained Cardinal Rites | `src/main/resources/data/hemomancy/recipe/cardinal_rite/` |
| White Humor purification | `src/main/resources/data/hemomancy/recipe/white_humor_purification/` |
| Pallid Retort distillation | `src/main/resources/data/hemomancy/recipe/distillation/` (`pallid: true`) |
| Liber Immaculatus book data | `src/main/resources/data/hemomancy/books/liberimmaculatus/` |
| Zealot and Guardian inquiry dialogue | `src/main/resources/data/hemomancy/dialogue_inquiry/<npc>/<namespace>/<item>.json` |

### 25.1 Harbinger Blood Structure Crafting

An in-world system: build a specific block structure, then hit a particular block with a catalyst item while spending blood. The structure transforms into the desired output.

Harbinger Blood Structure crafting is introduced through the Alchemist dialogue around Votary, but individual recipes are no longer inferred from blood-cost tiers. Each Harbinger JSON carries `required_degree`; `RecipeDegreeGates` compares that value against the player's Initiatory Degree. Blood cost is only the resource cost. Unstained structure recipes that share the serializer are cataloged in §15.3.

The Liber Sanguinum crafting sidebar and the debug Structure Spawner group Harbinger recipes directly by required degree (`No Degree`, `Degree 1`, ..., `Degree 8`) through `RecipeDegreeGates`.

| Recipe | Required Degree/Stage | Blood Cost | Held Item | Hit Block | Result |
|--------|-----------------------|-----------|-----------|-----------|--------|
| Liber Sanguinum | 0 | 100 | Sanguine Formation | Bookshelf | Liber Sanguinum |
| Hematic Iron Block | 0 | *(see JSON)* | *(see JSON)* | Iron Block | Hematic Iron Block |
| Ghastly Alembic / Iron Brazier / Living Staff | 1 | *(see JSON)* | *(see JSON)* | *(see JSON)* | Early Harbinger machinery/tools |
| Vial Centrifuge / Mnemonic Reliquary | 2 | *(see JSON)* | *(see JSON)* | *(see JSON)* | Votary machinery |
| Somatic Loom / Mind Spike / Semi-Sentient Construct | 3 | *(see JSON)* | *(see JSON)* | *(see JSON)* | Initiate machinery |
| Runic Chisel Station / Visceral Mirror | 4 | *(see JSON)* | *(see JSON)* | *(see JSON)* | Adept machinery |
| Dendritic Distributor / Consecrated Bloodwell / Morphling Incubator / Mycelial Lantern | 5 | *(see JSON)* | *(see JSON)* | *(see JSON)* | Crimson Lodge machinery, including passive enzyme fruiting |
| Covenant Throne / Vascular Effigy | 6 | *(see JSON)* | *(see JSON)* | *(see JSON)* | Bloodline Covenant machinery |
| Sanguine Monolith | 7 | *(see JSON)* | *(see JSON)* | *(see JSON)* | Archon machinery |

> Harbinger recipes are in `data/hemomancy/recipe/blood_structure/`. Each recipe defines a multiblock `pattern` with `key` mapping characters to blocks, plus `heldItem`, `hitBlock`, `bloodCost`, `required_degree`, and `result`. Unstained entries in the same folder use `unstained: true` and are documented in §15.3.
> **Mycelial Lantern blood structure:** `blood_structure/mycelial_lantern.json` is Degree 5, costs 2,500 blood, uses `spore_sac` on a `hematic_iron_block`, and builds from Sanguine Glass, brown mushroom blocks, Hematic Iron, Polished Venous Stone, and Copper Block.

### 25.1.1 Puppeteer Trial Blood Crafting

Puppeteer summon unlocks use a Blood Crafting-adjacent recipe type rather than Cardinal Rites. Recipes live under `data/hemomancy/recipe/puppeteer_trial/` and use `type: "hemomancy:puppeteer_trial_recipe"`.

Each trial recipe extends the Blood Structure recipe data shape with summon-specific fields:

- `summon`: summon definition id, e.g. `veinwing_vulture`
- `bloodCost`: blood drained immediately on activation
- `heldItem`: currently `hemomancy:sanguine_quintessence`
- `hitBlock`: currently `hemomancy:conscious_mass`
- `pattern` / `key`: Blood Structure-style multiblock pattern
- `consume_pattern`: whether the matched pattern is cleared on activation
- `required_degree`: present in JSON as a fallback, but runtime gates use the matching `PuppeteerSummonDefinition.requiredDegree()` when the summon definition exists

Activation is instant from `BloodCraftingKeyPressPacket`: match the pattern at the hit block, verify the caster's Harbinger degree, ensure the summon is not already known, consume one Sanguine Quintessence unless the player has instabuild, drain blood, optionally clear the pattern, and spawn an unbound hostile trial version of the summon. Trial boss death grants the summon only to the recorded caster through `KnownSummonEvents.grantSummon` and syncs the known-summons capability.

Current trial recipes:

| Trial Recipe | Required Degree | Blood Cost | Held Catalyst | Hit Block | Pattern Notes |
|--------------|-----------------|-----------:|---------------|-----------|---------------|
| `puppeteer_trial/veinwing_vulture` | 2 | 500 | Sanguine Quintessence | Conscious Mass | Venous Stone + Engram Block ring |
| `puppeteer_trial/marrow_spitter` | 3 | 750 | Sanguine Quintessence | Conscious Mass | Venous Stone + Engram Block + Bone Block |
| `puppeteer_trial/gorebound_hulk` | 4 | 1100 | Sanguine Quintessence | Conscious Mass | Engram Block frame + Gilded Venous Stone core |

`PuppeteerSummonTrialEvents.awardTrialRecipes` grants all trial recipes at or below the player's current degree from degree-grant and login paths, so old saves receive newly eligible trials when the player next logs in.

### 25.2 Harbinger Cardinal Rite Recipes

Specific Harbinger cardinal rite recipes include degree advancement rites (section 5.2) plus blood-path utility rites. Progression access now comes from each recipe JSON's explicit `required_degree`; the `minor`/`lesser`/`greater`/`grand` `CardinalRiteType` remains as a ritual form that controls size, cast time, and boundary behavior.

`RecipeDegreeGates` is the shared helper for Blood Structures and Cardinal Rites. This section covers rite recipes that compare `required_degree` against `IInitiatoryDegree`; Unstained rites compare the same field against `getPlayerUnstainedLevel` and are cataloged in §15.2. The Rites tab groups recipes by required degree/stage rather than by rite form.

**Degree Advancement Rites:** These recipe JSONs set `"rankup": true`, which lets client UI and tooling distinguish degree rites from utility rites. The rank-up target is inferred from the rite ID so a player who already has that degree or higher cannot start a redundant rank-up rite.

| Rite | Blood Cost | Rite Form | Required Degree | Degree -> | Description |
|------|-----------|-----------|-----------------|----------|-------------|
| Sanguine Initiation | 100 | Minor | 0 | 0 -> 1 | Basic initiation awakening hematic potential |
| Rite of the Votary | 250 | Minor | 1 | 1 -> 2 | Binds the practitioner deeper into the Covenant |
| Rite of the Scarlet Sanctum | 500 | Lesser | 2 | 2 -> 3 | Grants formal entry into the Scarlet Sanctum |
| Adept Rite | *(see JSON)* | Lesser | 3 | 3 -> 4 | Fourth rite of the Hematic Order |
| Rite of the Crimson Lodge | 2000 | Greater | 4 | 4 -> 5 | Illuminates the inner secrets of the Crimson Lodge |
| Rite of the Bloodline Covenant | 3000 | Greater | 5 | 5 -> 6 | Consecrates the practitioner to the Bloodline Covenant |
| Rite of the Hematic Order | 5000 | Grand | 6 | 6 -> 7 | Crowns the practitioner as Archon |
| Rite of Apotheos | 7000 | Grand | 7 | 7 -> 8 | Final ascension beyond Archon; requires completed Qliphoth Communion |

**Utility Rites:**

Utility rite `required_degree` values are authored per recipe rather than inferred from their form. For example, Vascular Mending is Degree 1, the Bloodline/Beacon/Hungering Earth cluster is Degree 2, Scarlet Summons and Sanguine Eclipse are Degree 3, Crimson Vessel is Degree 4, Founding Sanctum/Pallid Shadow/Sanguine Dominion are Degree 5, Eternal Covenant is Degree 6, and Ancestral Communion/Bloom of the Qliphoth are Degree 7.

| Rite | Blood Cost | Rite Form | Description |
|------|-----------|-----------|-------------|
| **Bloodline Founding** | 500 | Lesser | Binds the caster's essence into a new bloodline, producing a presigned ancestral ledger |
| **Bloodline Recall** | 750 | Lesser | Reconjures a lost ancestral ledger from the caster's blood memory |
| **Vascular Mending** | 800 | Lesser | Floods the caster's vascular system with purified blood, fully healing all 7 vein sections |
| **Hematic Fortification** | 500 | Lesser | Strengthens the caster's connection to the blood arts |
| **Hematic Unbinding** | 1000 | Lesser | Dissolves the caster's bloodline, freeing all members and returning shared blood |
| **Crimson Beacon** | 600 | Lesser | Anchors the caster's dying essence to this location: respawn point on fatal blow |
| **Hungering Earth** | 750 | Lesser | Feeds blood into the earth, converting terrain into blood-touched stone and ash |
| **Exsanguination** | 500 | Lesser | Drains the lifeblood of a creature within the ritual circle, crystallizing their essence into Sanguine Quintessence |
| **Sanguine Attunement** | 300 | Minor | Purges and resets the caster's blood tendency alignments to a blank slate |
| **Scarlet Summons** | 2000 | Greater | Teleports all online bloodline members to the rite location (cost scales with members) |
| **Sanguine Dominion** | 3500 | Greater | Claims the surrounding land as a Blood Domain: reduced manip cost, bleeding curse on enemies, empowered blood blocks |
| **Eternal Covenant** | 4000 | Greater | Permanently expands the caster's maximum blood volume (one-time only) |
| **Pallid Shadow** | 5000 | Grand | Strips Unstained purification from a nearby player: a blasphemous assault on Our Lady's path |
| **Ancestral Communion** | 5000 | Grand | Opens a channel to the ancient fungal consciousness. Triggers `AncestralCommunionDialogueTrees`: 5 dialogue variants (Origin, The Schism, The Infection, The Harbingers, The True Name) that reveal the fungal origins of hemomancy. Fires `communion_lore_*` events on completion. |
| **Bloom of the Qliphoth** | 1200 | Grand | Degree 7. Plants a Qliphoth Seed (placed as catalyst within the rite pattern), summons a `QliphothBloomBlock` (1x1x8 tree, 3-chunk radius), and starts the Qliphoth Communion chain. See section 5.9. |

### 25.3 Enzyme Fruiting Recipes

`data/hemomancy/recipe/enzyme_fruiting/*.json` defines Mycelial Lantern fruiting recipes. Each recipe names a reusable `culture` ingredient, an output `result`, a `duration`, and `blood_per_tick`.

Default tuning for the eight enzyme recipes is 2,400 ticks, 0.25 blood/tick, 600 total blood, and 1 matching enzyme output. The spore culture item is not consumed; the machine only consumes blood and time. Progress is preserved while paused for insufficient blood or blocked output.

Implemented outputs: `vivacious_enzyme`, `fervent_enzyme`, `neurotic_enzyme`, `incandescent_enzyme`, `ruinous_enzyme`, `frigid_enzyme`, `ferric_enzyme`, and `umbral_enzyme`.

### 25.4 Plant & Fungi Recipes

Plants and fungi found in hemomancy biomes serve as ingredients across multiple crafting systems:

**ghastly_alembic Processing:**

| Input | Output | Count | Cook Time |
|-------|--------|-------|-----------|
| Infected Fungus | Foul Paste | 2 | 100 |
| Stinkhorn Fungus | Foul Paste | 2 | 100 |
| Ghost Pipe | The Pale Distillate | 1 | 150 |
| Sarcodes | Dicentra Sap | 2 | 120 |
| Rafflesia | Spore Sac | 2 | 120 |
| Puffball Fungus | Spore Sac | 2 | 120 |
| Lethean Poppy | Lethean Dew | 2 | 150 |

**Crafting Recipes:**

| Recipe | Type | Ingredients | Output |
|--------|------|-------------|--------|
| Lethean Poppy Wreath | Shapeless | 4× Lethean Poppy + String | 1 |
| The Pale Distillate | Shapeless | Lethean Dew + Consecrated Copper Ingot | 1 |
| Tears of Silthmere | Shapeless | The Pale Distillate + Silver Chalice | 1 |
| Pale Silver Ingot | Shapeless | Iron Ingot + The Pale Distillate | 1 |
| Spore Sac | Shapeless | Puffball Fungus + Hyphae | 2 |
| Foul Paste (fungi) | Shapeless | Infected Fungus + Stinkhorn Fungus + Bone Meal | 3 |
| Befouling Ash | Smelting | Foul Paste | 1 |
| Smouldering Ash | Shapeless | Hematic Iron Powder + Blaze Powder + Gunpowder | 3 |

**Brewing Recipes (Awkward Potion + Plant → Potion):**

Only blood-faction plants brew into hemomancy potions. Unstained plants (Puffball Fungus, Lethean Poppy, Ghost Pipe) deliberately do not brew blood-positive effects — their uses are in ghastly_alembic processing and Unstained crafting chains.

| Plant Ingredient | Result Potion |
|-----------------|---------------|
| Bleeding Heart | Potion of Sanguine Siphon |
| Infected Fungus | Potion of Mycorrhizal Mending |
| Stinkhorn Fungus | Potion of Blood Binding |
| Rafflesia | Potion of Hemolysis |
| Sarcodes | Potion of Blood Rush |

### 25.5 Food Recipes

| Recipe | Type | Notes |
|--------|------|-------|
| Gourd Slice | Crafting | Sliced from gourd block |
| Gourd Stew | Crafting | Stew from gourd + ingredients |
| Roasted Gourd Seeds | Smelting | Gourd seeds in furnace |
| Roasted Gourd Seeds | Smoking | Gourd seeds in smoker |
| Roasted Gourd Seeds | Campfire Cooking | Gourd seeds on campfire |

### 25.6 Faction-Associated Block Palettes (Planning Guardrail)

When drafting new ritual patterns and recipe structures, use faction palettes to avoid repeatedly reusing the same Hemomancy-only block combinations.

| Faction Theme | Vanilla Block Palette (Primary Picks) | Other-Mod / External Palette (When Available) | Pattern Guidance |
|---|---|---|---|
| **Harbinger** | Deepslate Bricks, Polished Basalt, Red Nether Bricks, Iron Bars, Red Stained Glass, Dark Oak | MnA arcane/rune-stone families, engraved occult stone variants | Keep Hematic Iron and Engram blocks as focal accents rather than the entire structure |
| **Fungal** | Mushroom Stem, Brown/Red Mushroom Blocks, Moss, Rooted Dirt, Mud Bricks, Nether Wart Block | MnA nature/verdant stone families, fungal/mycelial block sets from installed mods | Always pair infected blocks with at least two non-Hemomancy organic blocks |
| **Unstained** | Smooth Quartz, Quartz Bricks, Calcite, Diorite, Oxidized/Waxed Copper, White Stained Glass | MnA sanctified/light stone variants, chapel/shrine materials from installed mods | Favor clean, mineral, and water-adjacent palettes; blood-reactive blocks stay minimal |
| **Chitinite / Arthropod** | Dripstone, Pointed Dripstone, Tuff Bricks, Polished Blackstone Bricks, Packed Mud | Earthy/chitin-like carved stone sets, hive/amber/chitin sets from installed mods | Blend shell-like mineral textures with chitin cues; avoid all-blood-block patterns |
| **Neutral / Common Folk** | Stone Bricks, Cobblestone, Andesite, Oak Planks, Bricks, Lanterns | Non-occult builder sets from worldgen/structure mods | Use as baseline for transitional or non-faction ritual spaces |

**Recipe/Rite Planning Rules:**
- Include at least one Vanilla primary block and one non-Hemomancy block in every new ritual multiblock.
- Do not reuse the same core `hitBlock` family across multiple adjacent rite tiers unless intentionally signaling a direct progression upgrade.
- If a Hemomancy-exclusive block is mandatory for function, diversify the surrounding pattern with faction-appropriate Vanilla blocks.

### 25.7 Saint Canon Memory Recipes (Somatic Loom)

Each of the four saints yields a Canon Memory when the player places a Hallowed Residuum into the Somatic Loom's catalyst slot while the loom's tendency alignment matches the saint's paired tendencies (both must reach the TENDENCY_THRESHOLD of 3.0).

| Canon Memory | Ingredient (catalyst slot) | Loom Tendencies Required | Unlocks Manipulation |
|-------------|---------------------------|--------------------------|---------------------|
| `memory_crimson_tithe` | Hallowed Residuum of Hemorath | MORTEM + ANIMUS | Crimson Tithe (SUMMA, MORTEM) |
| `memory_unclosing_eye` | Hallowed Residuum of Seraphae | LUX + DUCTILIS | Unclosing Eye (SUMMA, LUX) |
| `memory_bloom_of_rot` | Hallowed Residuum of Putriciel | MORTEM + FLAMMEUS | Bloom of Rot (SUMMA, MORTEM) |
| `memory_endless_hour` | Hallowed Residuum of Velorum | CONGEATIO + TENEBRIS | Endless Hour (SUMMA, CONGEATIO) |

> These are SUMMA-rank manipulations — the most costly and powerful tier. They are imprinted rather than learned; no blood cost reduction from Dynamic Use applies.

### 25.7.1 Scar-Catalyst Memory Recipes (Somatic Loom)

Five scar items can serve as Somatic Loom catalysts, providing an alternative path to certain memories. These are distinct from the standard routes (different ingredient, and in most cases different tendency combination). They are intended as mid-game rewards for players who have invested in the scar system:

| Memory | Scar Catalyst | Loom Tendencies Required | Notes |
|--------|--------------|--------------------------|-------|
| `memory_blood_rush` | `scar_heart` | ANIMUS + LUX | Heart-scar resonance variant; pushes blood through willpower alone |
| `memory_umbral_step` | `scar_shade` | TENEBRIS | Shade-scar variant; same tendency as ender-eye route, different catalyst |
| `memory_hemorrhage` | `scar_thorn` | MORTEM + FERRIC | Thorn scar pierces — the wound follows the scar |
| `memory_blood_eclipse` | `scar_moon` | CONGEATIO + TENEBRIS | Moon scar harmonizes with the eclipse; overlapping tendency with fermented spider eye route |
| `memory_sanguine_ignition` | `scar_phoenix` | FLAMMEUS + ANIMUS | Phoenix scar kindles blood into flame; requires two tendencies vs the standard single-tendency fire_charge route |

> Recipes live in `data/hemomancy/recipe/memory_weaving/memory_*_scarred.json`. The loom's recipe matcher checks both tendencies AND ingredient, so scar-catalyst and standard-catalyst routes for the same memory coexist without conflict.

### 25.8 Hallowed Residuum Extraction (Vial Centrifuge)

Processing a **Consecrated Syringe** (tagged with a saint type) in the **Vial Centrifuge** yields the corresponding Hallowed Residuum. The syringe is obtained by using an empty Blood Vial on a consecrated Saint Sarcophagus.

| Consecrated Syringe Tag | Output |
|------------------------|--------|
| `HEMORATH` | Hallowed Residuum of Hemorath |
| `SERAPHAE` | Hallowed Residuum of Seraphae |
| `PUTRICIEL` | Hallowed Residuum of Putriciel |
| `VELORUM` | Hallowed Residuum of Velorum |

---

## 26. Mob Entities

> **Design Note — Arthropods as Natural Hemomancers:** In the Hemomancy worldbuilding, arthropods and crustaceans are treated as nature's own blood mages. They do not use blood magic consciously, but the same forces that let Hemomancers harden blood into iron or spin it into chitin are expressed instinctively across the insect and crustacean kingdoms (urchins growing blood spines, Chthonians growing iron mandibles, Chitinites growing hematic-iron shells, etc.). This informs the mod's use of these creatures as source material for crafting and the Morphling system.

### 26.1 Hostile / Monster Mobs

| Entity | Texture | Category | Notes |
|--------|---------|----------|-------|
| **Fargone** | ![](../src/main/resources/assets/hemomancy/textures/entity/fargone/model_fargone.png) | Monster | Standard mosquito esk blood monster |
| **Thirster** | ![](../src/main/resources/assets/hemomancy/textures/entity/thirster/model_thirster.png) | Monster | Blood-thirsting mob |
| **Abhorent Thought** | | Monster | Large (1.5×3.25), eldritch thought entity |
| **Erythromycelium Eruptus** | ![](../src/main/resources/assets/hemomancy/textures/entity/erythromycelium_eruptus/model_erythromycelium_eruptus.png) | Monster | Large fungal eruption mob (1.5×3.0) |
| **Blood Drunk Puppeteer** | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_drunk_puppeteer/model_blood_drunk_puppeteer.png) | Monster | Human-sized, controls dolls |
| **Enthralled Doll** | ![](../src/main/resources/assets/hemomancy/textures/entity/enthralled_doll/model_enthralled_doll.png) | Monster | Small (0.5×0.5), controlled by puppeteer |
| **Chthonian** | ![](../src/main/resources/assets/hemomancy/textures/entity/chthonian/model_chthonian.png) | Monster | Iron-mandible termite creature — actively chews through wood blocks and wooden tools in the area. Spawns in Chthonian Termite Mounds (Savanna biome). Part of the "arthropods as natural hemomancers" theme (they produce hematic iron shells biologically). |
| **Chthonian Queen** | ![](../src/main/resources/assets/hemomancy/textures/entity/chthonian_queen/model_chthonian_queen.png) | Monster | Boss variant of Chthonian; exactly 1 spawns per Termite Mound. Associated with gold (royal). The only gold-connected creature in the mod. |
| **Lump of Thought** | ![](../src/main/resources/assets/hemomancy/textures/entity/lump_of_thought/model_lump_of_thought.png) | Monster | Sentient thought blob |
| **Morphling Polyp** (mob) | ![](../src/main/resources/assets/hemomancy/textures/entity/morphling_polyp/model_morphling_polyp.png) | Monster | Wild morphling mob |
| **Dessicant** | | Monster | Desiccating creature (ON_GROUND spawn) |
| **Cruor Fiend** | | Monster | Blood-fueled fiend (ON_GROUND spawn) |
| **Void Drinker** | | Monster | Void-aligned blood drainer (ON_GROUND spawn) |
| **Frozen Clot** | | Monster | Ice-blood clot creature (ON_GROUND spawn) |
| **Abyssal Siphon** | | Monster | Large (1.2×0.6) deep-sea blood siphon (ON_GROUND spawn) |
| **Synapse Hound** | | Monster | Neural creature (ON_GROUND spawn) |
| **Myelin Borer** | | Monster | Burrowing neural parasite (ON_GROUND spawn) |

### 26.2 Creature / Ambient Mobs

| Entity | Texture | Category | Notes |
|--------|---------|----------|-------|
| **Leech** | ![](../src/main/resources/assets/hemomancy/textures/entity/leech/model_leech_brown.png) | Creature | Small (0.4×0.1) blood-sucking leech |
| **Fungling** | ![](../src/main/resources/assets/hemomancy/textures/entity/fungling/model_fungling.png) | Creature | Friendly fungal creature |
| **Chitinite** | ![](../src/main/resources/assets/hemomancy/textures/entity/chitinite/model_chitinite.png) | Creature | Iron-shelled Isopod insect (1.0×0.3) |
| **Fervent Chitinite** | ![](../src/main/resources/assets/hemomancy/textures/entity/fervent_chitinite/model_fervent_chitinite.png) | Creature | Fire variant of Chitinite |
| **Hemolymphopoda** | ![](../src/main/resources/assets/hemomancy/textures/entity/hemolymphopoda/model_hemolymphopoda.png) | Ambient | Small (0.9×0.3), Horseshoe crab drops Cleansing Hemolymph |
| **Barbed Urchin** | ![](../src/main/resources/assets/hemomancy/textures/entity/barbed_urchin/model_barbed_urchin.png) | Water Ambient | Underwater iron-barbed urchin |
| **Chalybeate Snail** | ![](../src/main/resources/assets/hemomancy/textures/entity/chalybeate_snail/model_chalybeate_snail.png) | Water Ambient | Slow vent-field grazer with defensive retraction. Does not use ordinary biome spawning; `DeepOceanVentFeature` places persistent 2-5 clusters around valid hydrothermal vent floors. Retracted, off-cooldown snails can be nonlethally harvested with any HutosLib `ItemKnapper` for Chalybeate Sclerites. |
| **Crimson Doe** | | Creature | Blood-touched deer (ON_GROUND spawn) |
| **Hemojelly** | | Ambient | Blood jelly creature (ON_GROUND spawn) |
| **Venous Strider** | | Ambient | Vein-walking strider (ON_GROUND spawn) |

### 26.3 NPC / Summons / Player-controlled

| Entity | Texture | Category | Notes |
|--------|---------|----------|-------|
| **Blood Thrall** | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_thrall/blood_thrall.png) | Creature | Small (0.6×0.7), summoned blood transport creature. Can bind direct-routing sources/nodes, carry a capped amount, and deposit into target reservoirs without duplicating source blood. |
| **Blood Drunk Puppeteer** | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_drunk_puppeteer/model_blood_drunk_puppeteer.png) | Monster | Rare blood-themed hostile in dark/spooky and fungal biomes; summons four bonded Enthralled Dolls, drops Puppeteering Thread, and is a bloody-jug drop candidate |
| **Enthralled Doll** | | Monster | Puppeteer-bound support minion. Summoned dolls follow/assist their puppeteer and vanish without loot if the owner is gone |
| **Unstained Zealot** | ![](../src/main/resources/assets/hemomancy/textures/entity/unstained_zealot/unstained_zealot.png) | Creature | NPC that guides Unstained path entry |
| **Unstained Guardian** | | Creature | NPC that guards Unstained sacred sites |
| **Unstained Acolyte** | | Creature | NPC acolyte of the Unstained faction |
| **Harbinger Hermit** | | Creature | NPC Harbinger recluse; full degree 0–7 dialogue (`HarbingerHermitDialogueTrees`). Drops Rite Hint item on farewell. Invulnerable until player chooses "Farewell" option. |
| **Harbinger Alchemist** | | Creature | NPC machine expert found at Harbinger Outposts; full degree 0–7 dialogue (`HarbingerAlchemistDialogueTrees`). Teaches crafting stations, dismisses purifying players. |
| **Harbinger Vicar** | | Creature | NPC doctrine keeper found at Harbinger Outposts; full degree 0–7 dialogue (`HarbingerVicarDialogueTrees`). Delivers faction history lore; reveals secret "8th degree" at Archon. |
| **Harbinger Mnemonist** | ![](../src/main/resources/assets/hemomancy/textures/entity/harbinger_mnemonist/harbinger_mnemonist.png) | Creature | NPC blood-memory mentor found at Harbinger Outposts; full degree-gated dialogue (`HarbingerMnemonistDialogueTrees`). Teaches crude memories, active manipulation slots, the Mnemonic Reliquary, and Somatic Loom progression. Gives eligible Degree 1+ Harbingers one starter crude memory item; purifying/Clarity players may inquire but cannot claim. |
| **Annetta Knowles (The Stained Priestess)** | | Boss / NPC | Separate Unstained boss arc with a full two-route encounter, implemented in `entity/boss/annetta/`. She spawns in COWERING state inside a `BrokenChurchStructure` (see §29), with a ToothPecks Specimen Jar placed beside her and Devil's Tooth decorations around the scene. Her renderer still uses `textures/entity/blank.png` — dedicated model/texture/GeckoLib animations are WIP. `AnnettaKnowlesEntity` has four states: **COWERING** (hiding, dialogue only), **PHASE_ONE** (Harbinger-route boss fight), **CURED_SUPPORT** (Unstained-route ally phase), **RESOLVED** (post-encounter).<br><br>**Harbinger route** (interact while holding a ToothPecks Specimen Jar): the jar shatters, Annetta is bitten, and she transitions to PHASE_ONE. Boss bar: PURPLE, NOTCHED_10. Stats: 350 HP, 7 ATK, 0.26 SPD, 0.8 KB resist, 8 armor. Phase abilities: ① Silver Aura (every 60t, 6-block radius, 3 magic damage + Weakness II to blood-active players) ② Hemolytic Vial throw (every 90t, projectile applies Weakness + Mining Fatigue) ③ Hair-and-Nails Slash at ≤50% HP (every 70t, 5-block AoE, 5 damage + Slowness III). When she would die: if the player holds `annettas_sanguis_lancea`, she mutates into **`StainedPriestessEntity`** (Phase 2 — see below). Harbinger-route drops: `annettas_sanguis_lancea` + hematic_iron_scrap ×4 (if Phase 2 not triggered).<br><br>**Unstained route** (interact while holding a Draught of Still Mercy and `clarityUnlocked == true`): Annetta drinks the draught, transitions to CURED_SUPPORT, and **`LatentAnnettaInfectionEntity`** spawns as a separate boss (the latent infection made physical). In CURED_SUPPORT mode Annetta moves toward the infection entity and applies slow/debuffs near it; she also heals nearby Unstained players every 80t. When the `LatentAnnettaInfectionEntity` dies, it calls `annetta.markResolvedAfterCure()`, transitioning Annetta to RESOLVED state. Unstained-route drops (from LatentAnnettaInfection): `annettas_absolution_dagger` + pale_silver_ingot ×3. |
| **Stained Priestess (`StainedPriestessEntity`)** | | Boss | Phase 2 of the Harbinger-route Annetta encounter. Stats: 420 HP, 12 ATK, 0.32 SPD, 0.9 KB resist, 10 armor. Boss bar: WHITE, NOTCHED_10. Phase abilities: ① Blood Lances (every 70t, fires `SanguisLanceaEntity` projectile in look direction + 2 angled variants) ② Lunge attack (every 100t, moves rapidly toward target and strikes) ③ Blood Pressure Bloom (every 85t, 7-block AoE, 6 magic damage + Slowness to all nearby). Melee hits drain 300 blood from blood-active players (`BLOOD_DRAIN = 300`). Drops: `annettas_sanguis_lancea` + hematic_iron_scrap ×4. |
| **Latent Annetta Infection (`LatentAnnettaInfectionEntity`)** | | Boss | Final challenge of the Unstained-route Annetta encounter: the latent infection given physical form. Stats: 360 HP, 10 ATK, 0.27 SPD, 0.85 KB resist, 8 armor. Boss bar: WHITE, NOTCHED_10. Abilities: ① Infection Bloom (every 70t, MYCELIUM particle burst, Sculk Shrieker sound, 6-block AoE, 5 magic damage + Confusion + Slowness) ② Pressure Spike (every 110t, SOUL_FIRE_FLAME particles, 9-block AoE, 4 indirect magic damage + Weakness). Melee hits apply Poison I. On death: if a linked `AnnettaKnowlesEntity` is in CURED_SUPPORT within 32 blocks, calls `markResolvedAfterCure()`. Drops: `annettas_absolution_dagger` + pale_silver_ingot ×3. |
| **Spectral Companion** | | Misc | Spectral ally entity |
| **Sanguilith** | | Misc (MnA, dormant) | Large (1.5×3.25), blood-themed summoned monster from the dormant MnA compat source. `ComponentSummonSanguilith` summons an ownable, duration-limited melee attacker with a max of 4 nearby. Authored in `MnAPluginEntityInit` with custom `SanguilithModel` and `SanguilithRenderer`, but not compiled/registered while MnA compat is excluded on the current NeoForge 1.21.1 branch. |

### 26.4 Entity Tags

Mobs are tagged by tendency: `FUNGAL_TAG`, `UMBRAL_TAG`, `INCANDESCENT_TAG`, `FERRIC_TAG`, `VIVACIOUS_TAG`, `RUINOUS_TAG`, `NEUROTIC_TAG`, `FERVENT_TAG`, `FRIGID_TAG`. Chalybeate Snails are Ferric-aligned and included in `specimen_jar_capturable`.

### 26.5 Spawn Placements

Registered in `EntityInit.commonSetup`:
- Barbed Urchin → `IN_WATER`
- Hemolymphopoda → `ON_GROUND`
- Fargone → `ON_GROUND` (monster rules)
- Abhorent Thought → `ON_GROUND` (monster rules)
- Dessicant → `ON_GROUND` (monster rules)
- Cruor Fiend → `ON_GROUND` (monster rules)
- Void Drinker → `ON_GROUND` (monster rules)
- Frozen Clot → `ON_GROUND` (monster rules)
- Abyssal Siphon → `ON_GROUND` (monster rules)
- Synapse Hound → `ON_GROUND` (monster rules)
- Myelin Borer → `ON_GROUND` (monster rules)
- Crimson Doe → `ON_GROUND`
- Hemojelly → `ON_GROUND`
- Venous Strider → `ON_GROUND`
- Chalybeate Snail -> no ordinary biome spawn placement; spawned persistently by `DeepOceanVentFeature`

### 26.6 Entity Loot Tables

> **Status: Implemented in resources.** Entity drops are hand-authored JSON now. The disabled `HemoEntityLootProvider` generator remains stale/commented, but the live loot tables are the JSON files under `src/main/resources/data/hemomancy/loot_table/entities/` (1.21 singular `loot_table` path). Current count: **39 entity loot tables**.

Notable implemented drop families:

| Entity / Family | Drop Theme |
|-----------------|------------|
| Chitinite / Fervent Chitinite / Chthonian / Chthonian Queen | Chitinous Husk, with Chthonian Queen also rolling Ferric Enzyme |
| Leech / Blood aquatic or arthropod mobs | Blood/hemolymph materials such as Swollen Leech or Cleansing Hemolymph |
| Fargone / Thirster / Abhorent Thought / Lump of Thought / Morphling Polyp | Sanguine Formation / fungal ingredients depending on mob |
| Blood Drunk Puppeteer / Enthralled Doll | Puppeteering Thread from the puppeteer; puppeteer-summoned dolls are support minions and do not create extra loot |
| Chalybeate Snail | Killing gives only a rare small Hematic Iron Scrap fallback; reliable Chalybeate Sclerites come from knapper harvesting while retracted |
| Saint and boss entities | Direct/special boss rewards are handled in entity code or matching loot JSON depending on encounter |

Do not re-enable `HemoEntityLootProvider` unless the current JSON values are first ported back into the provider.

---

## 27. Projectile & Blood Construct Entities

### 27.1 Blood Constructs

Extend `BloodConstructEntity` (a `PathfinderMob` implementing `IBloodConstruct`). They are summoned by the player and have a limited lifetime (`deathTicks`):

| Entity | Notes |
|--------|-------|
| Blood Cloud (`CloudEntityBlood`) | Area-of-effect blood cloud |
| ![](../src/main/resources/assets/hemomancy/textures/entity/iron_pillar/model_iron_pillar.png) Iron Pillar (`EntityIronPillar`) | 0.75×2.8 iron construct |
| ![](../src/main/resources/assets/hemomancy/textures/entity/iron_wall/model_iron_wall.png) Iron Wall (`EntityIronWall`) | 1.6×2.8 iron wall construct |
| ![](../src/main/resources/assets/hemomancy/textures/entity/iron_spike/model_iron_spike.png) Iron Spike (`EntityIronSpike`) | 1.4×1.5 iron spike trap |
| ![](../src/main/resources/assets/hemomancy/textures/entity/wretched_will/modelwretchedwill.png) Wretched Will (`EntityWretchedWill`) | Will-based construct |

### 27.2 Projectiles

| Entity | Texture | Notes |
|--------|---------|-------|
| Directed Blood Orb | | High tracking range (150), main blood projectile |
| Tracking Blood Orb | | Homing blood orb |
| Blood Cloud Carrier | | Delivers blood clouds |
| Tracking Serpent | ![](../src/main/resources/assets/hemomancy/textures/entity/crimson_serpent/model_crimson_serpent.png) | Homing snake projectile |
| Tracking Pests | | Homing pest swarm |
| Blood Bolt | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_bolt/model_blood_bolt.png) | Crossbow ammo entity |
| Blood Needle | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_needle/model_blood_needle.png) | Small fast projectile |
| Blood Bullet | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_bullet/model_blood_bullet.png) | Pistol-type projectile |
| Blood Shot | ![](../src/main/resources/assets/hemomancy/textures/entity/blood_shot/model_blood_shot.png) | Shotgun-style spread |
| Sanguis Lancea | ![](../src/main/resources/assets/hemomancy/textures/entity/sanguis_lancea/model_sanguis_lancea.png) | Thrown spear entity |
| Dark Arrow | | Dark-themed arrow |

### 27.3 Item Entities

| Entity | Notes |
|--------|-------|
| Flying Charm | The Charm of Vascularium flying to the player |
| Morphling Polyp Item | Dropped morphling polyp pickup |

---

## 28. World Generation & Biomes

### 28.1 Custom Biomes (via TerraBlender)

| Biome | Key | Temperature | Precipitation | Notes |
|-------|-----|-------------|---------------|-------|
| **Fungal Gardens** | `fungal_gardens` | 2.0 | None (Nether) | Hyphae tendrils, huge fungi |
| **Fungal Isles** | `fungal_isles` | 2.0 | None (Nether) | Hyphae, huge fungi, small infected fungi |
| **Sporecrown Thicket** | `sporecrown_thicket` | 1.2 | None | Dense fungal overgrowth, hostile spawns (Eruptus, Chthonian, Fargone), crimson particles, dark red fog |
| **Hyphal Spires** | `hyphal_spires` | 0.9 | None | Extreme towering terrain with calcified hyphae, conscious mass patches; high-weirdness / low-erosion zones |
| **Drifting Mycelium** | `drifting_mycelium` | 0.7 | None | Anti-gravity floating islands of fungal terrain; high-continentalness zones with 3D noise creating disconnected landmasses |

The Fungal Gardens dimension uses a datapack `multi_noise` biome source in `data/hemomancy/dimension/fungal_gardens.json`. Its climate noise is intentionally tuned at a higher horizontal frequency so the dimension's fungal biomes appear as shorter, more varied patches rather than enormous single-biome regions. Its terrain density is intentionally high-relief: `continental_shape`, `erosion_shape`, and `fungal_noise_settings` amplify mid-scale rises, basins, and eroded ridges so the ground does not collapse into broad uniform shelves. Water is also meant to appear as real fungal seas and lowland basins: `fungal_noise_settings` uses sea level 32 and `continental_shape` avoids an excessive positive landmass bias. `morphic_pool` is a fungal dimension surface feature shared across the fungal dimension biomes; it gets two placement attempts per chunk in the same feature step as the other visible fungal terrain features, scans around the ocean-floor heightmap for actual fungal terrain, and carves shallow morphic nectar basins through the dimension's fungal surface palette, including `mycelium_erythrocytic_dirt`. The dimension is carved by datapack configured carvers (`fungal_cave`, `fungal_cave_large`, `fungal_canyon`); the two cave carvers use the registered `hemomancy:dry_fungal_cave` carver, which keeps vanilla-style cave branching but widens and densifies it into frequent spaghetti/Swiss-cheese tunnels while only applying strict fluid-adjacency protection near sea level, preventing exposed underwater air scars without overwhelming chunk generation. `fungal_canyon` is kept rare and deep. `#minecraft:overworld_carver_replaceables` is extended with the dimension's custom fungal stone/surface blocks. The dimension is visually dim: `dimension_type/fungal_gardens.json` uses low ambient light, the Fungal Gardens / Fungal Isles biome fog colors are darkened, and `FungalSkyBoxRenderer` tints the spore skybox down so the custom Earth, moon, and star field remain readable without washing out the realm. Its fungal biomes use End music (`minecraft:music.end`) with cave mood ambience rather than Nether ambient loops or additions. Sky-reaching hyphae tendrils are intentionally common; each tendril now chooses a varied endpoint height, with many stopping in the lower or middle sky and only rare strands approaching the ceiling, so the horizon reads as an uneven alien mycelial forest instead of a uniform set of build-limit cables. Open ground is broken up by `venous_ridge`, a sparse low surface feature that lays smoother organic ribs of infested stone, hyphae, conscious mass, and hemorrhagic crust across dry fungal terrain; some runs begin partially embedded and rise through the ground like exposed roots. Sparse canopy mushrooms are also shared into more fungal dimension biomes so big silhouettes appear outside only the dense thickets. Registered overworld biome hooks still live in the 3 TerraBlender regions (`TestRegion1/2/3`) with custom surface rules.

> **Custom environment textures:**
>
> | | | | |
> |---|---|---|---|
> | ![](../src/main/resources/assets/hemomancy/textures/environment/sun.png) Sun | ![](../src/main/resources/assets/hemomancy/textures/environment/moon.png) Moon | ![](../src/main/resources/assets/hemomancy/textures/environment/clouds.png) Clouds | ![](../src/main/resources/assets/hemomancy/textures/environment/blood_moon_phases.png) Blood Moon Phases |

### 28.1.1 Blood Moons

Blood Moons are a world event distinct from normal nights, with their own moon texture phases (`blood_moon_phases.png`) and a client-side vein/tendril sky overlay.

**Frequency:** Natural trigger checks once per night at tick 12542 and currently has a **1-in-7 chance** to start a 11900-tick Blood Moon. A command can force one for testing; the **Rite of the Sanguine Eclipse** (Greater rite, Degree 3+) also manually triggers one — see §25.2 Harbinger Cardinal Rite Recipes.

**Effects while active:**
- Harbingers / active Hemomancers: **Strength II and Night Vision**
- Non-blood-magic players: **Weakness I** + **passive blood drain** (50 blood per effect interval, ~every 2 s) while their blood is active — the tide pulls at the uninitiated
- Thirsters and Fargones spawn near players within the Blood Moon encounter cap via direct Blood Moon event spawns, not biome spawn lists; placement allows open night sky and non-colliding ground clutter, avoids bright block-lit areas, checks full mob clearance, and only counts successful world insertion
- **Somatic Loom** ritual blood cost reduced by **25%** during a Blood Moon (applied in `SomaticLoomBlockEntity.startRitual()`; parallel to the manipulation discount in `BloodManipulation`)
- **Founding Sanctum** barrier: hostile mobs (non-player `Monster`) that enter a consecrated sanctum boundary during a Blood Moon take 4 magic damage and are knocked outward every effect interval (handled in `FoundingSanctumEvents.onLevelTick()`)
- **Kidneys** organ (if extracted): regeneration amplifier increases by +1 during a Blood Moon (overclocked filtration under pressure) — see §20.8 Organ Echo Items
- Clients render the red Blood Moon phase texture and the `BloodMoonVeinSkyRenderer` tendril overlay when `PacketSyncBloodMoon` marks the event active; the RGB-only Blood Moon phase sheet is drawn additively so its black background texels do not appear as a visible square at dawn/dusk

**Lore significance:** Blood Moons represent the Pale Lady expending a burst of power to push back the fungal infection for another cycle. The moon appearing full and blood-red is her doing. After such a night, the moon may appear dim or new — she is recovering. See [LORE_REFERENCE.md](LORE_REFERENCE.md) §9 for the full cosmological explanation.

> **Status: Implemented.** `BloodMoonEvents` handles natural trigger, commands, gameplay effects, mob spawning, and client sync. Blood drain for uninitiated, loom discount, and sanctum mob-sealing are all implemented. Ritual trigger via the **Rite of the Sanguine Eclipse** is implemented.

### 28.2 World Features

| Feature | Notes |
|---------|-------|
| Big Mushgloom | Large mushroom variant |
| Canopy Mushroom / Brown & Red Canopy | Tree-like mushroom features |
| Small Infected Mushroom | Small scattered fungi |
| Fungus Feature | Generic fungal feature |
| Hyphae Feature | Ground-level hyphae spread |
| Hyphae Tendril | Vertical tendril features |
| Bog Body Feature | Generates bog body blocks |
| Deep Ocean Vent Feature | Rare code-generated basalt/magma hydrothermal vent field in deep ocean biome tag; spawns persistent Chalybeate Snails and provides atmosphere/hazards rather than direct ore nodes |

### 28.3 Configured/Placed Features

Managed via `ConfiguredFeatureInit` and `PlacedFeatureInit`:
- `HYPHAE_TENDRIL`, `VENOUS_RIDGE`, `HUGE_FUNGUS`, `SMALL_INFECTED_FUNGUS`
- `PLACED_INFESTED_VENOUS_STONE_BLOB`, `PLACED_MYCELIUM_BLOB`
- `PLACED_CANOPY_MUSHROOMS_DENSE`, `PLACED_CANOPY_MUSHROOMS_SPARSE`
- `PATCH_HYPHAE`, `BLEEDING_HEARTS`, `STINK_HORNS`
- `DEEP_OCEAN_VENT` / `deep_ocean_vent`: placed with `RarityFilter.onAverageOnceEvery(96)`, `HEIGHTMAP_OCEAN_FLOOR`, and a `surface_structures` biome modifier against `#hemomancy:deep_ocean_vent_spawnlist` (`deep_ocean`, `deep_cold_ocean`, `deep_lukewarm_ocean`)

---

## 29. Structures

| Structure | Type | Notes |
|-----------|------|-------|
| **Broken Church** | `BrokenChurchStructure` | Jigsaw-based overworld structure (registered in `StructureInit`). Spawns Annetta Knowles in COWERING state at `afterPlace()` by scanning the bounding box floor for a valid air-over-solid position. The corner scene includes: a **ToothPecks Specimen Jar** placed 1 block east of Annetta (facing her); three **Devil's Tooth** blocks nearby; and a scatter of random Hemolytic Plating or Bone Blocks within a 7×7 area as environmental debris. |
| **Blood Temple** | `BloodTempleStructure` | Contains the Mortal Display; gateway to hemomancy |
| **Harbinger Outpost** | `HarbingerOutpostStructure` | Harbinger exploration structure. Its `afterPlace()` hook spawns outpost NPCs: Vicar/Alchemist guidance and one **Harbinger Mnemonist** in the unused opposite corner from one Alchemist, giving the structure an early manipulation teacher without requiring NBT edits. Chest loot now favors crude memory starter rewards over overly generous early full-memory rewards. |
| **Sanguine Surveyor Bivouac** | `SanguineSurveyorBivouacStructure` | Harbinger camp structure that can appear in the Nether’s Crimson Forest and in Hemomancy’s fungal Nether biomes. Uses placeholder discovery inscriptions (`hemomancy:random/surveyor_log`) which are replaced at generation time with one of the Surveyor Log inscriptions. |
| **Bog-Body Ossuary Niche** | `BogBodyOssuaryNicheStructure` | Small Harbinger burial/cache niche intended for swamp biomes. Uses placeholder discovery inscriptions (`hemomancy:random/ossuary_memo`) which are replaced at generation time with one of the Ossuary Memo inscriptions. Placement is more tolerant of water depth than other overworld structures. |
| **Crimson Lodge Annex** | `CrimsonLodgeAnnexStructure` | Rare Harbinger “hall-camp” structure that implies ongoing covenant life (bunks, table, lectern, pantry). Uses placeholder discovery inscriptions (`hemomancy:random/lodge_minutes`) which are replaced at generation time with one of the Lodge Minutes inscriptions. |
| **Unstained Church** | `UnstainedChurchStructure` | Contains the Unstained Podium; gateway to the Unstained path |
| **Qliphoth Sanctum** | NBT structure | Dark sanctum used for the Qliphoth-related endgame content; contains Engram Block |
| **Qliphoth Bloom** | NBT structure | Qliphoth Bloom block structure placement |
| **Blood Tower (Core)** | NBT structure | Core segment of the Blood Tower multi-piece structure |
| **Blood Tower (Top 1)** | NBT structure | Top segment of the Blood Tower multi-piece structure |
| **Saint Trial Chamber (Hemorath)** | NBT structure (WIP) | Locked dungeon for the First Saint — four blood-basin puzzle, blood-sapping room, inner sarcophagus chamber. Unlocks once all four basins are filled to the correct level. See §5.8. |
| **Chthonian Termite Mound** | Feature/Structure (WIP) | Savanna biome structure. Always spawns with exactly 1 Chthonian Queen and a variable population of Chthonians. Contains a small loot chest (iron, gold, minerals). Chthonians will chew nearby wood. Spawn rate should be tuned (currently slightly over-common). |
| **Plains Hemopothecary** | Village structure | Hemopothecary villager house for plains biome villages |
| **Desert Hemopothecary** | Village structure | Hemopothecary villager house for desert biome villages |
| **Taiga Hemopothecary** | Village structure | Hemopothecary villager house for taiga biome villages |
| **Snowy Hemopothecary** | Village structure | Hemopothecary villager house for snowy biome villages |
| **Savanna Hemopothecary** | Village structure | Hemopothecary villager house for savanna biome villages |

> Structure NBT files are in `data/hemomancy/structure/`. Worldgen structures are registered via `StructureInit` and the datapack `worldgen/structure*` JSONs. The hemopothecary village variants are integrated via `HemopothecaryProcessor` and `VillageEvents`.

---

## 30. Villagers & Professions

| Profession | POI Block | Notes |
|-----------|-----------|-------|
| ![](../src/main/resources/assets/hemomancy/textures/entity/villager/profession/hemopothecary.png) **Hemopothecary** | Scrying Podium | Blood-themed villager trader |

**Hemopothecary Trade Listings:**

| Level | Sells | Price (Emeralds) |
|-------|-------|-----------------|
| 1 | Befouling Ash | 1 |
| 1 | Fungal Morphling | 1 |
| 1 | Recycled Enzyme | 1 |
| 1 | Bloody Flask | 1 |
| 2 | Fervent Husk | 2 |
| 3 | Barbed Helm | 3 |
| 4 | Foul Paste | 4 |
| 5 | Heart Pattern | 5 |

- Custom `HemopothecaryProcessor` for village structure integration
- `VillageEvents` for handling village spawning

---

## 31. GUIs & Overlays

### 31.1 HUD Overlays

| Overlay | Location | Shows |
|---------|----------|-------|
| `BloodVolumeOverlay` | Left side | Current/max blood volume bar plus a small two-lobed equipped blood gourd indicator that reads only the Charm/Gourd slot and tints white/red/black by gourd variant ![](../src/main/resources/assets/hemomancy/textures/gui/blood_bar.png) |
| `UnstainedGaugeOverlay` | Top-right | Purity + Clarity bars ![](../src/main/resources/assets/hemomancy/textures/gui/unstained_gauge.png) |
| `EquippedMorphlingOverlay` | Next to `BloodVolumeOverlay` | Currently equipped morphling icon only. It appears on the right side of a left-anchored blood bar, or on the left side of a right-anchored blood bar, vertically centered with the bar. No text/backplate. |
| `ManipCooldownOverlay` | — | Active manipulation cooldown timer |

> **Gauge fills:** ![](../src/main/resources/assets/hemomancy/textures/gui/blood_fill_tiled.png) Blood fill &nbsp; ![](../src/main/resources/assets/hemomancy/textures/gui/unstained_fill_tiled.png) Purity fill &nbsp; ![](../src/main/resources/assets/hemomancy/textures/gui/unstained_clarity_fill_tiled.png) Clarity fill


### 31.1a Reusable Screen Widgets

- `BloodVolumeBarWidget`: reusable vertical blood reservoir bar with frame, animated red fill, meniscus, highlights, bubbles, tick marks, `Bounds`, and tooltip rendering. Used by internal blood reservoir screens including Ghastly Alembic, Vial Centrifuge, Morphling Incubator, Mycelial Crucible, and Mycelial Lantern.
- `WhiteHumorBarWidget`: dedicated Unstained counterpart with pale silver-blue fill and tooltip rendering. Extracted from `PallidRetortScreen` so future White Humor machines can reuse the same visual language without forcing a generic fluid widget yet.

### 31.2 Screens

**Key GUI Textures:**

|                                                                                                             | |                                                                                                          |
|-------------------------------------------------------------------------------------------------------------|---|----------------------------------------------------------------------------------------------------------|
| ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/vial_centrifuge_screen.png) Vial Centrifuge | ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/ghastly_alembic_screen.png) Ghastly Alembic | ![](../src/main/resources/assets/hemomancy/textures/gui/recaller_gui.png) Recaller |
| ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/scar_station_screen.png) Scarring Station | ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/scar_viewer_full_screen.png)Scar Binder | ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/jar%20full.png) Morphling Jar              |
| ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/tendency%20screen.png)Tendency View     |  ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/vasc_screen.png)Vascular View | ![](../src/main/resources/assets/hemomancy/textures/ref%20doc%20images/fungal_implant_screen.png) Spore Implant |

| Screen | Opened From | Purpose |
|--------|------------|---------|
| `CharmGourdScreen` | Scrying Podium | Equip Charm of Vascularium + Blood Gourds |
| `HarbingerProgressScreen` | Dendritic Distributor | Harbinger progress suite (Skills/Manipulations/Crafting/Scars/Rites/Materials), now tab-controller modularized; Skills overlay includes rank title text |
| `TendencyViewScreen` | Blood Tendency Gauge | View blood tendency alignments |
| `VascularViewScreen` | Vascular Status Gauge | View vein section health |
| `VascularStatusScreen` | — | Detailed vascular status |
| `BloodlinePoolScreen` | Bloodline Pool Monitor | View/manage bloodline shared pool |
| `GhastlyAlembicScreen` | ghastly_alembic block | ghastly_alembic crafting GUI |
| `PallidRetortScreen` | Pallid Retort | Unstained distillation GUI with crystalline background and reusable `WhiteHumorBarWidget` for the internal White Humor reservoir. |
| `VialCentrifugeScreen` | Vial Centrifuge | Centrifuge crafting GUI (reworked with new 3D stand model) |
| `MorphlingIncubatorScreen` | Morphling Incubator | Incubation crafting GUI |
| `MycelialLanternScreen` | Mycelial Lantern | Fungal/amber enzyme-fruiting GUI. Uses `BloodVolumeBarWidget`, centered reusable culture slot, blood input/empty flask slots under the bar, progress lane to enzyme output, and hover tooltip for the internal blood reservoir. |
| `UnstainedProgressScreen` | Self Reflection Mirror | Unstained progress + shared Rites/Crafting/Materials tab controller stack |
| `MnemonicReliquaryScreen` | Mnemonic Reliquary block | Reliquary viewing GUI — opens animated lid on interaction |
| `SporeImplantScreen` | Fungal Implantation Pylon | Spore implantation GUI |
| `StructureSpawnerScreen` | Structure Spawner item | Debug structure spawning |
| Various radial menus | Living Staff / keybinds | Morphling/manipulation selection |
| Guide/Codex screens | Liber Sanguinum | **Partially functional** — `HemoProgressionScreen.setupEntries()` is still commented out in Java (renderer), but the HutosLib JSON book framework is wired and the Liber Sanguinum's data folder (`data/hemomancy/books/sanctumsanguinium/`) now has a `manipulations/` chapter (ordinality 7) with 10 pages covering all 8 tendencies + overview + Canon Memories. |
| Guide/Codex screens | Liber Immaculatus (Unstained book) | **Populated** — `data/hemomancy/books/liberimmaculatus/` now has 4 chapters (intro, sacred_tools, our_lady, the_path), 3 pages each. Covers Hemolytic Solution mechanics, Our Lady of Still Waters lore, purity/clarity stage descriptions. |

---

## 32. Advancements

### 32.1 Shared / Early Game

| Advancement | Trigger |
|-------------|---------|
| **Strange Seeds** | Find Gourd Seeds from grass |
| **The First Awakening** | Activate a Blood Temple's Mortal Display (programmatic) |
| **Ashen Beginnings** | Craft Befouling Ash |
| **Sanctum Sanguinium** | Obtain the Liber Sanguinum |
| **Iron in the Blood** | Create first Hematic Iron Block via blood structure recipe |
| **Bottled Vitality** | Obtain a Blood Gourd |
| **An Extension of Oneself** | Craft a Living Staff |
| **Inherited Memory** | Obtain any Hematic Memory |
| **Scarlet Tradition** | Obtain the Charm of Vascularium |
| **Armed to the Veins** | Equip full Hematic Iron armor |
| **Cultivator** | Obtain a Morphling Jar |
| **The Blood Remembers** | Obtain a Living Blade |
| **Old Habits** | Obtain any enzyme |
| **Bleeding a Stone** | Craft a Ghastly Alembic |

### 32.2 Harbinger Path (programmatic + item triggers)

All degree advancements are granted via `HarbingerAdvancementGranter.grantDegree()` inside the `DEGREE_RITE_PATHS` completion block of `CardinalRiteEvents`. They chain from `the_first_awakening`.

| Advancement | JSON key | Frame | Trigger |
|-------------|----------|-------|---------|
| **Neophyte of the Crimson Veil** | `degree_1_neophyte` | task | Degree 1 rite (programmatic) |
| **Votary of the Hematic Covenant** | `degree_2_votary` | task | Degree 2 rite (programmatic) |
| **Initiate of the Scarlet Sanctum** | `degree_3_initiate` | task | Degree 3 rite (programmatic) |
| **Adept of the Sanguine Brotherhood** | `degree_4_adept` | goal | Degree 4 rite (programmatic) |
| **Illuminatus of the Crimson Lodge** | `degree_5_illuminatus` | goal | Degree 5 rite (programmatic) |
| **Sanctified of the Bloodline Covenant** | `degree_6_sanctified` | goal | Degree 6 rite (programmatic) |
| **Archon of the Hematic Order** | `degree_7_archon` | challenge | Degree 7 rite (programmatic) |
| **Apotheos of the Hematic Order** | `degree_8_apotheos` | challenge | Degree 8 rite (programmatic) |

**Order function milestones** — branches off the degree chain:

| Advancement | JSON key | Parent | Trigger |
|-------------|----------|--------|---------|
| **Blood Is Bound** | `blood_is_bound` | `degree_3_initiate` | Bloodline founding rite succeeds (programmatic) |
| **A Lodge of Crimson** | `crimson_lodge_consecrated` | `degree_5_illuminatus` | Crimson Lodge rite completes (programmatic) |
| **This Ground Is Ours** | `founding_sanctum_established` | `degree_5_illuminatus` | Founding Sanctum first consecration (programmatic) |
| **The Covenant Cannot Be Unmade** | `eternal_covenant_sealed` | `degree_6_sanctified` | Eternal Covenant rite completes (programmatic) |

**Endgame / revelation milestones:**

| Advancement | JSON key | Parent | Trigger |
|-------------|----------|--------|---------|
| **Voices in the Vein** | `voices_in_the_vein` | `degree_7_archon` | Ancestral Communion rite (programmatic) |
| **The Blood Beneath the Blood** | `the_blood_beneath_the_blood` | `degree_7_archon` | Obtain Fungal Spine (inventory_changed) |

**Mastery side branches:**

| Advancement | JSON key | Parent | Trigger |
|-------------|----------|--------|---------|
| **Scars of the Mind** | `scars_of_the_mind` | `degree_4_adept` | Obtain Scar Binder or Scar Binder Upgraded (inventory_changed) |
| **The Land Bleeds for You** | `sanguine_domain` | `degree_5_illuminatus` | Sanguine Dominion rite (programmatic) |

### 32.3 Unstained Path (programmatic)

All granted via `UnstainedAdvancementGranter.grantIfNotDone()` from `UnstainedMilestoneHandler` (tick-based threshold checks) and `CardinalRiteEvents` (clarity ascension rite, altar of cleansing).

| Advancement | JSON key | Frame | Condition |
|-------------|----------|-------|-----------|
| **Unstained** | `unstained` | task | Obtain Hemolytic Solution |
| **Lady of the Forgotten Waters** | `lady_of_forgotten_waters` | goal | Obtain Tears of Silthmere |
| **Path of Purity** | `path_of_purity` | task | Obtain Tome of the Unstained |
| **Our Lady of Still Waters** | `our_lady_of_still_waters` | challenge | Obtain Icon of Our Lady |
| **Blessed by the Altar** | `blessed_by_the_altar` | goal | Use Altar of Cleansing (programmatic) |
| **Tainted** | `tainted` | task | Purity ≥ 25 (programmatic) |
| **Cleansing** | `cleansing` | task | Purity ≥ 50 (programmatic) |
| **Absolved** | `absolved` | goal | Purity ≥ 75 (programmatic) |
| **Purified** | `purified` | challenge | Purity = 100 (programmatic) |
| **Clarity Awakened** | `clarity_awakened` | challenge | Clarity unlocked (programmatic) |
| **Discerning** | `discerning` | task | Clarity ≥ 25 (programmatic) |
| **Vigilant** | `vigilant` | goal | Clarity ≥ 50 (programmatic) |
| **Resolute** | `resolute_stage` | goal | Clarity ≥ 75 (programmatic) |
| **Enlightened** | `enlightened_seeker` | challenge | Clarity = 100 (programmatic) |

---

## 33. Keybindings

All under the "Hemomancy" category:

| Key | Action |
|-----|--------|
| Use Manipulation | Cast the selected quick manipulation |
| Use Quick Manipulation | Alternative quick-cast |
| Use Continuous Manipulation | Toggle continuous manipulation |
| Cycle Known Manipulations | Cycle through unlocked manipulations |
| Activate Blood Construct | Activate blood construct ability |
| Blood Formation | Trigger blood formation |
| Blood Draw | Draw blood |
| Open Morphling Jar | Open jar viewer |
| Open Morphling Jar Viewer | Open detailed jar viewer |
| Toggle Gourd Open/Closed | Toggle blood gourd state |
| Toggle Scar Binder Pickup | Toggle scar pickup mode |

---

## 34. Commands

The `/hemo` command tree (via `HemoCommand`, permission level 2) provides:

**Blood Volume:**
- `blood get` — show current blood
- `blood set <amount>` — set blood volume
- `blood setmax <amount>` — set maximum blood volume
- `blood fill` — fill to max
- `blood activate` — toggle blood capability active state

**Initiatory Degree:**
- `degree get` — show current degree
- `degree set <number>` — set degree (0–8)
- `qliphoth pome reset` — reset pome progress and reseal the Qliphoth Communion gate

**Unstained Progress:**
- `unstained get` — full overview (purity, clarity, stages)
- `unstained begin` — toggle begun purification
- `unstained purity get` / `unstained purity set <value>` — read or set purity (0–100)
- `unstained clarity unlock` — toggle clarity unlock
- `unstained clarity get` / `unstained clarity set <value>` — read or set clarity (0–100)
- `unstained reset` — reset all unstained progress to zero
- `unstained max` — max out all unstained progress

**Other Debug/Admin:**
- `skills get` / `skills setpoints <amount>` / `skills reset`
- `organs get` / `organs set <organ> <level>` / `organs reset`
- `bloodmoon summon` / `bloodmoon cancel`
- `slots get` / `slots equip <manip>` / `slots unequip <manip>`

---

## 35. Sound Events

Registered in `SoundInit`:

| Sample Sound Event | Registry Key | Notes |
|-------------|-------------|-------|
| Abhorent Thought Ambient | `entity.abhorent_thought.ambient` | Idle sound for the Abhorent Thought mob |
| Crimson Doe Ambient | `entity.crimson_doe.ambient` | Ambient sound for the Crimson Doe creature |
| Chthonian Queen Death | `entity.chthonian_queen.death` | Death sound for the Chthonian Queen |
| Synapse Hound Hurt | `entity.synapse_hound.hurt` | Damage sound for the Synapse Hound monster |

> **Status: Implemented.** `SoundInit` currently registers **83 custom sound events** spanning item, creature, aquatic, arthropod, monster, and boss categories. Vanilla sounds are still used in many interactions where dedicated custom audio has not yet been authored.

---

## 36. Particle Types

Registered in `ParticleInit`:

| Particle | Registry Key | Data Class | Factory | Visual Purpose |
|----------|-------------|------------|---------|---------------|
| Serpent | `serpent` | `SerpentParticleData` | `SerpentParticleFactory` | Tracking Serpent projectile trail effect |
| Hit Glow | `hit_glow` | `HitColorParticleData` | `HitGlowParticleFactory` | Colored glow effect on entity hits and manipulation impacts |
| Blood Avatar Hit | `blood_avatar_hit` | `BloodAvatarHitParticleData` | `BloodAvatarHitParticleFactory` | Blood Avatar melee hit splash effect |
| Blood Cell | `blood_cell` | `BloodCellData` | `BloodCellParticleFactory` | Blood cell floating effect (used in blood volume visuals, gourds, rituals) |
| Blood Claw | `blood_claw` | `BloodClawData` | `BloodClawParticleFactory` | Claw-slash blood effect (Deadly Gaze, melee manipulation hits) |
| Absorbed Blood Cell | `absorbed_blood_cell` | `AbsorbedBloodCellData` | `AbsrobedBloodCellParticleFactory` | Blood being absorbed/drawn into the player (blood draw, gourd filling) |

> The mod also makes heavy use of HutosLib particles (`GlowParticleFactory` with `ParticleColor`) for manipulation-specific effects (crimson glows, ice crystals, flame sparks, etc.). These are not registered in Hemomancy's `ParticleInit` but are spawned via `ServerLevel.sendParticles()` in each manipulation's `getAction()` method.

---

## 37. Mod Compatibility

### 37.1 Mana and Artifice (MnA)

> **Status: `Dormant`.** MnA compat source is preserved as the design/implementation target, but it is **not compiled or registered** in the current branch. `build.gradle` excludes `src/main/java/com/vincenthuto/hemomancy/compat/mna/**`, the MnA dependency is commented because no NeoForge 1.21.1 build is available, and the `Hemomancy.java` MnA imports/registration block is commented out. Treat this section as dormant compat until MnA publishes a compatible build and the source exclusion is removed.

Designed integration as a faction + spell system:

**Faction: The Harbingers**
- `HarbingersFaction` — custom faction with blood-red manaweave (RGB 160,0,40) ![Faction Icon](../src/main/resources/assets/hemomancy/textures/mna/faction_icon_harbinger.png)
- Token item: ![](../src/main/resources/assets/hemomancy/textures/item/mna/mark_of_blood.png) Mark of Blood
- Grimoire: ![](../src/main/resources/assets/hemomancy/textures/item/mna/spellbook_h.png) Tome of the Impending End
- Faction Horn: ![](../src/main/resources/assets/hemomancy/textures/item/mna/horn_harbinger.png) Horn of the Impending End
- Custom mana resource (`HarbingersMana`) ![Resource Bars](../src/main/resources/assets/hemomancy/textures/mna/harbingers_resource_bars.png)

**Spell Components:**
- ![](../src/main/resources/assets/hemomancy/textures/mna/blood_binding.png) `ComponentBloodBinding` — applies Blood Binding effect via spells
- ![](../src/main/resources/assets/hemomancy/textures/mna/mana_to_blood.png) `ComponentManaToBlood` — converts MnA mana into Hemomancy blood volume (configurable magnitude, 50–200 mana per cast)
- ![](../src/main/resources/assets/hemomancy/textures/mna/sanguine_fertility.png) `ComponentSanguineFertility` — applies Sanguine Fertility via spells
- `ComponentBloodToMana` — "Sanguine Offering" — drains target's blood and converts to mana for caster (inverse of ManaToBlood). Magnitude 50–500, Blood affinity. Composable with any MnA shape.
- ![](../src/main/resources/assets/hemomancy/textures/mna/blood_loss.png) `ComponentBloodLoss` — applies Blood Loss effect (movement speed debuff) via spells. Duration 60–300t, Magnitude 1–3, HARMFUL
- ![](../src/main/resources/assets/hemomancy/textures/mna/blood_rush.png) `ComponentBloodRush` — applies Blood Rush effect (+move/attack speed) via spells. Duration 100–600t, Magnitude 1–3, FRIENDLY
- ![](../src/main/resources/assets/hemomancy/textures/mna/hemolysis.png) `ComponentHemolysis` — applies Hemolysis effect (blood destruction DoT) via spells. Duration 40–200t, Magnitude 1–4, HARMFUL
- ![](../src/main/resources/assets/hemomancy/textures/mna/summon_sanguilith.png) `ComponentSummonSanguilith` — "Conjure Sanguilith" — summons a Sanguilith at target location. Duration 200–600t (summon lifetime), Magnitude scales damage. Requires Harbinger faction. HARMFUL

**Cross-System Mechanics (dormant until MnA compat is re-enabled):**
- **Blood Tithe** (`BloodTitheHandler`): Harbinger faction members casting blood-affinity spells have a configurable percentage of mana cost converted to blood cost instead (default 25%). Blood drained at 5 blood per 1 mana replaced. Hooks into `CalculatingManaCostEvent`.
- **Spell → Manipulation Combos** (`ManipComboHelper` + `BloodTitheHandler`): Casting blood-affinity MnA spells grants **Arcane Resonance** (reduces next manipulation's blood cost). Using Hemomancy manipulations grants **Sanguine Clarity** (reduces next spell's mana cost). Creates an alternating gameplay loop between both mod systems.

**Cross-Mod Config** (`HemoMnAConfig`):
- Blood ↔ Mana conversion ratios
- Blood Tithe enable/disable, mana reduction %, blood-per-mana ratio
- Living Thread armor set bonus values
- Trapezohedron effect radius
- Spell ↔ Manipulation combo enable/disable, durations, reduction percentages
- Sanguilith summon health scaling and max summon count

**Manipulations:**
- `SanguineTransfusionManip` — MnA-specific manipulation

**Runic Anvil Integration:**
- Living Infused Thread + Mage Armor → Living Thread armor set (Hood, Robes, Leggings, Boots)
- (3) Set Bonus: +500 Max Mana, +50% Mana Regen

**Additional MnA Items:**

| | | | |
|---|---|---|---|
| ![](../src/main/resources/assets/hemomancy/textures/item/mna/foul_vinteum_ingot.png) Foul Vinteum Ingot | ![](../src/main/resources/assets/hemomancy/textures/item/befouled_vinteum_dust.png) Befouled Vinteum Dust | ![](../src/main/resources/assets/hemomancy/textures/item/mna/mana_infushed_memory_blank.png) Mana Infused Memory Blank | ![](../src/main/resources/assets/hemomancy/textures/item/mna/living_infused_thread.png) Living Infused Thread |
| ![](../src/main/resources/assets/hemomancy/textures/item/mna/living_thread_hood.png) Living Thread Hood | ![](../src/main/resources/assets/hemomancy/textures/item/mna/living_thread_robes.png) Living Thread Robes | ![](../src/main/resources/assets/hemomancy/textures/item/mna/living_thread_leggings.png) Living Thread Leggings | ![](../src/main/resources/assets/hemomancy/textures/item/mna/living_thread_boots.png) Living Thread Boots |
| ![](../src/main/resources/assets/hemomancy/textures/item/mna/blood_shot_occulus.png) Blood Shot Occulus | ![](../src/main/resources/assets/hemomancy/textures/item/mna/mote_blood.png) Mote of Blood | ![](../src/main/resources/assets/hemomancy/textures/item/mna/mana_memory_sanguine_transfusion.png) Mana Memory: Sanguine Transfusion | |

> Living Thread Armor model: ![](../src/main/resources/assets/hemomancy/textures/models/armor/living_thread_layer_1.png) ![](../src/main/resources/assets/hemomancy/textures/models/armor/living_thread_layer_2.png)

**MnA Ritual:**
- Ritual of The Weeping Wound

**MnA Block/Tile/Entity:**
- Custom blocks, tiles, and entities in `compat/mna/block`, `compat/mna/tile`, `compat/mna/entity`

**Planned / Brainstormed Features** (see `MNA_COMPATIBILITY_BRAINSTORM.md` for full details — each feature includes an "MnA Justification" explaining why it specifically requires Mana and Artifice):
- New spell shapes: Sanguine Pulse (dual mana+blood cost AoE), Hemomantic Tether (channeled tether draining blood per tick)
- New MnA rituals: Sanguine Convergence (permanently links mana regen to blood volume), Arcane Crucible (transmutes MnA materials with blood sacrifice), Mana Wound (zone that adds Hemomancy effects to MnA spells)
- Tendency ↔ Affinity mapping: Hemomancy tendencies boost corresponding MnA spell affinities
- Harbinger faction: Occulus tasks, sanctum structure, manaweaving recipes, raid mobs — all use MnA faction infrastructure
- Blood Construct: MnA Construct variant fueled by blood instead of mana, built at MnA's workbench
- Blood-Infused Construct Capabilities: runeforged modules for MnA Constructs
- Hemomantic Wand Core, Arcane Living Staff, Mote of Mana — crafted via MnA systems (manaweaving, runeforging)
- Hemomantic enchantments via MnA runeforging
- Cross-mod advancements, JEI integration for MnA crafting recipes
- Harbinger Mana HUD texture (`textures/mna/harbingers_resource_bars.png`) and resource hook (`HarbingersMana` implementing `ICastingResourceGuiProvider`) are authored in dormant compat source and should be treated as port targets until MnA is re-enabled

### 37.2 Curios

**Status: `Dormant`.** Curios integration for the Charm of Vascularium and other equippable items is preserved in `compat/curios`, but the current NeoForge 1.21.1 branch does not compile/register it. `build.gradle` comments the Curios dependency and `Hemomancy.java` has the Curios registration block commented out pending a compatible Curios NeoForge build.

### 37.3 JEI

**Status: `Partial`.** JEI is currently supplied by a local `libs/jei-1.21.1-neoforge-19.27.0.340.jar` while the old Maven dependency lines remain commented. Recipe category support exists/planned for:
- Chisel Station recipes
- Visceral Recaller recipes
- Blood Structure Crafting recipes (Harbinger entries; Unstained entries share infrastructure but are documented in §15.3)
- Morphling Incubator recipes (`IncubatorRecipeCategory`)
- Mycelial Crucible recipes (`MycelialCrucibleRecipeCategory`)
- Morphic Nectar recipes (`MorphicNectarRecipeCategory`)
- White Humor Purification recipes (`WhiteHumorPurificationRecipeCategory`; Unstained-only, see §15.4)
- Enzyme Fruiting recipes have `EnzymeFruitingRecipeCategory` authored, but `JEIPlugin` does not currently register the type/category/catalyst/recipes yet.

### 37.4 HutosLib

HutosLib is still the required shared runtime library (`com.vincenthuto.hutoslib:hutoslib`, currently `7.3.5`), but local development now uses a Gradle composite build. `settings.gradle` includes `../HutosLib` and substitutes the Maven module with the local project, so Hemomancy builds directly against the workspace HutosLib source when that sibling checkout is present.

---

## 38. Known WIP / Incomplete Systems

This section is a maintenance rollup, not a changelog. It uses the status legend from the top of this reference and only calls out systems whose state is easy to misread from older notes.

| Status | Systems |
|--------|---------|
| Implemented | Entity loot JSONs, all 21 skill effects, visceral organs, armor set bonuses, morphling maturity powers, standard scar effects, incubator recipes, fungal scar cultivation, Blood Moon mechanics, Chthonian termite mound behavior, deep ocean vent fields and Chalybeate Snail ecology, major NPC dialogue trees, early crude memory learning, Mycelial Lantern enzyme fruiting, direct blood routing, puppeteer spindle container/render pass, puppeteer trial Blood Crafting recipes |
| Partial | Progression/Liber Java renderer, Founding Sanctum tuning, Saints rooms/world placement/art, Fungal Dimension terrain/content, Annetta dedicated art/rendering and final combat polish, JEI display wiring for Mycelial Lantern |
| Dormant | MnA and Curios compat source/config while their NeoForge 1.21.1 dependencies are unavailable and source exclusions remain active |
| Planned | Direct-routing polish, forced manipulation rank-up rituals, fungal coral reef biomes, Harbinger exploration vessels, optional Our Lady apparition encounter, Spectral Companion summon flow, remaining Unstained Church palette/decor polish |

- **Entity Loot Tables** - `Implemented`: 39 entity loot table JSON files exist in `data/hemomancy/loot_table/entities/` (1.21 singular path) and are loaded automatically by vanilla/NeoForge datapack convention. The `HemoEntityLootProvider` data generator remains disabled but is not needed - loot tables work via the JSON files.
- **Progression Codex / Liber Sanguinum / Liber Immaculatus** — `HemoProgressionScreen.setupEntries()` is still commented out (Java renderer WIP). However, the HutosLib JSON book framework is wired: the `sanctumsanguinium` book folder now has normal lore/mechanics chapters, and the `liberimmaculatus` book folder has 4 chapters (intro, sacred_tools, our_lady, the_path) with 12 pages covering the full Unstained path. The Field Notes / memo slice is implemented: `memo_capture:<id>` dialogue events write memo IDs into Field Notes, and the Dictation Table dictates those IDs into the player's `LiberKnowledge` attachment rather than into the Liber item stack. Field Notes are now ink-bound: Hematic Field Ink captures Harbinger memos for Liber Sanguinum, while Pale Field Ink captures Unstained memos for Liber Immaculatus. `LiberKnowledge` stores `KnownMemos`, `UnlockedLiberEntries`, and per-entry discovery sources, syncs to clients with `PacketSyncLiberKnowledge`, and can be granted through `LiberKnowledgeHelper` by memos, advancements, rites, item pickups, degree changes, dialogue, or other future triggers. `LiberEntryDefinitions` is the central code-side page map: it lists visible book entries and maps initial rites, Harbinger degree advancements, Unstained milestones, selected item pickups, and `liber_unlock:<entry_id>` dialogue events to normal book page IDs. The Liber items now behave like personal viewers/keys: borrowed books show the reader's own unlocked pages, not the owner's. Legacy stack data is migrated into the player attachment when an old Liber is used or placed for dictation. `MemoBookFilter` treats `LiberEntryDefinitions` as the source of visible pages for both Liber books; pages not mapped by a definition remain hidden, and chapters with zero unlocked pages are omitted. Current memos include `first_rite_notes`, `pale_lady_notes`, and Harbinger fungal whisper memos (`fungal_whisper_adept`, `fungal_whisper_illuminatus`, `fungal_whisper_sanctified`, `fungal_whisper_archon`, `fungal_whisper_truth`, `qliphoth_communion`) that unlock the Hyphae, Entity, Truth, and Qliphoth Liber Sanguinum pages through Hematic Field Ink. Remaining WIP: re-enable `setupEntries()` if that older renderer is revived, move entry definitions to data-driven JSON if desired, and author more entry definitions across existing chapters.
- **Blood Fluid** (`FluidInit`) — Legacy/placeable blood fluid remains commented out and is superseded by the current direct-routing design; it is not part of the active automation plan.
- **Manipulation Rank Advancement** — Ritual-based forced rank upgrades described as WIP in lore
- **Skill Effect Wiring** — **Implemented:** All 21 skills in `SkillPointHelper` have helper methods and are fully wired into event handlers. Iron Will wired in `BloodVolumeEvents.onPlayerDamaged`; Scar Affinity/Resonance/Mastery wired in `ScarEntityEventHandler` and `ItemScar`; puppeteer summon cap/health/damage/range are wired through the Marionette Crossbar and bound summon behavior.
- **Loot Modifiers** (`AddItemModifier`) — framework exists; specific loot targets are not yet assigned.
- **Visceral Organs System** — **Implemented:** All 5 organ effects are fully implemented in `VisceralOrgansEvents`: **Spleen** (+1000 max blood per level, announces capacity expansion on first reach); **Liver** (removes Poison at level 2+, Wither at level 3+); **Lungs** (Water Breathing while underwater); **Kidneys** (Regeneration at level-1 amplifier; amplifier +1 during a Blood Moon); **Heart** (Damage Resistance capped at Resistance II; Wither immunity at level 3 — Cardiac Autonomy fully mastered; blood drain 10÷level per 2 s). **Iron Brazier** reagent system is organ-specific. See §20.8.
- **Armor Set Bonuses** — **Implemented:** All 5 armor sets now have unique set bonuses implemented in `ArmorSetBonusHandler`: Hematic Iron (blood regen), Blood Lust (lifesteal), Barbed (thorns + Blood Loss), Chitinite (toughness + projectile reduction), Unstained (Blood Loss/Hemolysis immunity). The Marrow Crown artifact has a standalone +10% damage bonus when blood > 50%. See §22 for details.
- **Morphling Maturity** — **Implemented:** All 12 morphlings now have named maturity-tier reactive abilities (Developing → Mature → Apex) and secondary tendencies defined. See §16.1.
- **Scar Gameplay Effects** — **Implemented:** All standard scars now have full triggered effect implementations. Effect durations respect `getScarMasteryDurationMultiplier()`.
- **Vial Centrifuge Rework** — New 3D stand model (`CentrifugeStandModel`) and custom item renderer implemented; UI and menu updated. `VialCentrifugeBlockItem` has custom `BlockEntityWithoutLevelRenderer`.
- **Memory Overlay Textures** — `Partial`: active memories use the layered memory item model system (`memory_blank` + per-memory overlay), and most manipulations have unique overlay textures under `textures/item/memories/`. Known pending art remains called out in the memory gallery above.
- **Incubator Recipe System** — Full `IncubatorRecipe` + `IncubatorRecipeSerializer` added with 13 JSON recipes for all morphling types. JEI integration via `IncubatorRecipeCategory`. Recipes stored in `data/hemomancy/recipe/incubator/`.
- **Fungal Scar Cultivation** — **Implemented:** `MycelialCrucibleBlockEntity`, `FungalScarCultivationRecipe`, and `FungalScarCultivationSerializer` now support the two-phase fungal scar flow. Nine recipes live in `data/hemomancy/recipe/fungal_scar/`; all use the consolidated `immature_fungal_scar` culture item with target metadata and aligned-enzyme maturation.
- **Mycelial Lantern / Enzyme Fruiting** — **Implemented:** `MycelialLanternBlockEntity`, `EnzymeFruitingRecipe`, `EnzymeFruitingRecipeSerializer`, eight spore culture items, eight enzyme-fruiting JSON recipes, Blood Structure recipe, menu/screen, block entity renderer, item renderer, and Blockbench source are present. **Remaining polish:** `EnzymeFruitingRecipeCategory` exists but `JEIPlugin` is not yet registering it, so JEI display/catalyst wiring still needs a pass.
- **Direct Blood Routing** — **Implemented:** `HematicSutureNeedleItem`, `HematicSutureNodeBlockEntity`, `BloodRoutingSavedData`, `IBloodSourceContract`, `IBloodRoutingTarget`, and `BloodRoutingHelper` provide pull-based machine feeding without a basin, fluid, or bulk storage block. Current behavior supports nearby personal/gourd links, Degree 5 sanctum links, optional bloodline-pool draw with leader/opt-in checks, Blood Thrall courier draw/deposit, and Drudge tendering around an SSC.
- **Puppeteer Spindle and Trial Unlocks** — **Implemented:** `PuppeteersSpindleBlockEntity`, `PuppeteersSpindleMenu`, `PuppeteersSpindleScreen`, `PacketPuppeteersSpindleAction`, `PuppeteersSpindleRenderer`, and `PuppeteersSpindleItemRenderer` provide the two-slot spindle workflow, persistent 512-thread buffer, slotted crossbar filling/binding, themed screen, custom block model, and facing-aware placement. `PuppeteerTrialRecipe`, `PuppeteerTrialRecipeSerializer`, and `PuppeteerSummonTrialEvents` provide the Sanguine Quintessence Blood Crafting trial unlock path for Veinwing Vulture, Marrow Spitter, and Gorebound Hulk.
- **Mnemonic Reliquary** — New functional block with animated lid (open/close), custom 3D block entity renderer (`MnemonicReliquaryRenderer`), item renderer (`MnemonicReliquaryItemRenderer`), block model (`MnemonicReliquaryModel`), menu (`MnemonicReliquaryMenu`), and screen (`MnemonicReliquaryScreen`). Tracks open count and syncs lid angle via block events.
- **Suspended Cleansed Blood Crystal** — Purified variant of the Suspended Blood Crystal with custom block, block entity (random time offset for desynchronized animations), block item with custom renderer, 3D model, and blockstate.
- **Cleansed Sanguine Glass & Pane** — New glass/pane variants added to the block system with blockstates, models, textures, and loot tables.
- **Debug Showcase Item** — Creative-mode testing tool (`DebugShowcaseItem`) that generates an organized showcase of all mod content in 4 sections: items in chests, blocks on platforms, mobs in fenced pens, and multiblock structures placed as patterns.
- **Cardinal Rite Boundary Renderer** — Client-side visual renderer (`CardinalRiteBoundaryRenderer`) for cardinal rite boundaries during active rites.
- **Morphling Item Textures** — All morphling types now have individual item textures and item models (bat, centipede, chitinite, fungal, leeches, mole, moth, pests, serpent, spider, tick, urchin).
- **MnA Compatibility Expansion** — Extensive brainstorming and dormant compat source are documented in `MNA_COMPATIBILITY_BRAINSTORM.md` and `compat/mna/**`. Current NeoForge 1.21.1 branch excludes MnA compat from compilation because no compatible MnA build is available; `Hemomancy.java` registration is commented. Treat spell components, Blood Tithe, Spell ↔ Manipulation combo, and `HemoMnAConfig` as preserved design/port targets rather than active runtime features until compat is re-enabled.
- **GhastlyAlembic Custom Renderer** — `GhastlyAlembicRenderer` now renders the block as a full 3D entity model (`GhastlyAlembicModel`) with facing-aware rotation. Previously was a static block.
- **MorphlingIncubator Custom Renderer** — `MorphlingIncubatorRenderer` now renders the incubator as a full 3D entity model with custom animation.
- **Morphling Incubator Blood Flask Transfer Fix** — Bloody Flask absorption now clamps to available player blood capacity instead of requiring full flask fit. Empty flasks are routed to the dedicated incubator flask output slot.
- **New Monster Mobs** — `Partial`: all 10 monster/creature additions (Dessicant, Cruor Fiend, Void Drinker, Frozen Clot, Abyssal Siphon, Synapse Hound, Myelin Borer, Crimson Doe, Hemojelly, Venous Strider) have AI goals, spawn placements, biome modifier JSONs, and loot table JSONs implemented. GeckoLib animation state machines are stubs awaiting final model work.
- **New NPC Entities Dialogue** — `Partial`: full dialogue trees are implemented for the main Harbinger and Unstained NPCs, including Zealot, Acolyte, Guardian item/ambient dialogue, Scout, and Our Lady whisper events. Guardian/Scout/Acolyte renderers and church spawning are active. Spectral Companion is registered with AI/rendering, but its player-facing summon flow remains WIP.
- **Fungal Whisper System** — `FungalWhisperDialogueTrees` and `FungalWhisperEvents` deliver degree-gated (4–7, with degree 8 using the Archon-tier whisper set) intrusive fungal consciousness whispers. 12 variants across 4 tiers progressively reveal that hemomancy is a fungal infection masquerading as blood magic. High-degree players receive whispers on random intervals. Additional one-shot event dialogues: `postMonolithShatter()` (Entity comments on the seed hiding inside), `postBloom()` (acknowledgment of first fruiting), `pomeDropped(index, offerMemo)` (per-husk drop announcement; always delivered to the online bloom owner, with memo capture only when still relevant), `qliphothCommunion()` (nine-shell completion), `coreWitnessDialogue()` (Archon dimension choice fork). Whisper nodes now include Hematic Field Notes memo capture options where appropriate; ordinary high-tier whispers unlock Entity/Hyphae knowledge, while truth, communion, and core-witness moments unlock Truth or Qliphoth pages.
- **Ancestral Communion Dialogue** — `AncestralCommunionDialogueTrees` provides 5 unique lore-revelation dialogues for the Grand Rite of Ancestral Communion (degree 7). Variants: The Origin, The Schism, The Infection, The Harbingers, The True Name.
- **Harbinger Outpost NPCs** — Harbinger Alchemist, Vicar, and Mnemonist are implemented with degree-gated dialogue trees. The Alchemist covers machine lore, the Vicar covers faction history/doctrine, and the Mnemonist covers crude memories, active manipulation slots, Mnemonic Reliquary loadout management, Somatic Loom memory weaving, and the one-time Degree 1+ starter crude-memory choice. Entities are registered with textures, lang keys, client render hooks, dialogue handlers, and outpost `afterPlace()` spawning.
- **Scar Tier System** — All three standard tiers are registered through `ItemInit` with active gameplay effects. Current active set is Mind Spike + 24 standard scars (8 tendencies × 3 tiers) + 9 fungal scars. Resource cleanup still needed: `scar_ichor` recipe/lang/model data exists but the item is not registered in `ItemInit`.
- **HemoItemModelProvider Enhancements** — Data generator now handles `BloodMemoryItem` 2-layer models, `ItemScarPattern` 2-layer models, and properly excludes special blocks (sanguine panes, cleansed sanguine panes, ash trails, engram, filler, crimson flames) from automatic block model generation.
- **Saints System** — **Partial:** Four canon Saints exist: Hemorath, Seraphae the Chain Saint, Putriciel, and Velorum. The shared sarcophagus spine and boss dispatch are implemented, and Hemorath's trial is the first complete trial flow. Bespoke Trial Chamber rooms/world placement for Seraphae, Putriciel, and Velorum remain WIP. Boss models/textures/GeckoLib animations are stub/placeholder. See §5.8.
- **Founding Sanctum** — **Partial:** Buff application, Sanguine Quintessence, catalyst requirement, sanctum persistence, and Blood Moon sealing are implemented. Sanctum boundary detection and full gameplay tuning remain WIP. See §5.7.
- **Blood Moon Mechanics** — **Implemented:** `BloodMoonEvents` handles natural trigger, commands, gameplay effects, mob spawning, Somatic Loom discount, sanctum sealing, organ synergy, ritual trigger, and client sync/rendering. See §28.1.1.
- **Fungal Dimension** — **Partial:** Fungal Spine access, safe travel placement, dimension mob spawning, and the Archon first-exit choice fork are implemented. Terrain feature population and broader dimension content remain WIP. See §5.6.
- **Annetta Knowles / Stained Priestess** — **Partial:** The two-route encounter is wired through `AnnettaKnowlesEntity`, `StainedPriestessEntity`, `LatentAnnettaInfectionEntity`, and `BrokenChurchStructure`. Dedicated entity model, texture, GeckoLib animations, fuller Phase 1 biological combat identity, and Sanguis Lancea rendering remain WIP. See §26.3 and LORE_REFERENCE §11.
- **Chthonian Termite Mound** — **Implemented:** Savanna structure, guaranteed queen spawn, loot chest, wood-chewing behavior, wooden tool degradation, tuned spawn rate, and spawn placements are present. See §29.
- **Deep Ocean V1: Chalybeate Snail and Vent Fields** - **Implemented:** `deep_ocean_vent` is a code-generated hydrothermal vent feature registered through feature bootstrap/data JSON and added to deep ocean biomes via `neoforge:add_features`. It builds basalt/smooth basalt/deepslate/blackstone/magma vent fields with restrained Hemomancy organic accents, then spawns persistent `chalybeate_snail` clusters. The snail has defensive retraction, Ferric/specimen-jar tags, a spawn egg, renderer/model/texture, subtitles/sounds, and a nonlethal HutosLib `ItemKnapper` harvest path for `chalybeate_sclerite` with a saved 6000-tick cooldown. Fungal coral reefs and Harbinger exploration vessels remain future ocean hooks.
- **Ghost Pipes as Unstained Material** — **Implemented:** Ghost Pipe is registered as a plant/potted plant and now has a Pallid Retort distillation recipe into Pale Distillate (`distillation/ghost_pipe.json`, `pallid: true`).
- **Cleansed Stone and Pallid Lantern** — **Implemented:** `cleansed_stone.json` crafts Cleansed Stone from Stone + Hemolytic Solution, and `pallid_lantern.json` crafts Pallid Lantern from Pale Silver Ingot + Pale Humor Flask + Glowstone Dust. Both are registered blocks and used by Unstained recipes/advancements.

### 38.1 Unstained Expansion - Current Status

The Unstained faction has moved from mostly planned design notes into a broad implemented gameplay path. This rollup keeps the remaining planned pieces visible without leaving completed systems mislabeled.

| System | Status | Code/data anchors |
|---|---|---|
| Altar of Cleansing | `Implemented` | `AltarOfCleansingBlock`, `AltarOfCleansingBlockEntity`, `altar_of_cleansing` block/entity registration, modeled item/BER, inquiry dialogue, and Unstained Church NBT entries. Tears of Silthmere grant the one-time altar blessing; Lethean Poppy Wreaths and Lethean Brew grant repeatable purity; Silver Chalices and Pallid Icons grant clarity once unlocked. |
| Unstained temple/church expansion | `Partial` | `UnstainedChurchStructure` spawns one Zealot, two Guardians, three-to-five Acolytes, and rite-fragment inscriptions; `unstained_church.nbt` includes the Altar of Cleansing and Unstained Podium. Pallid Lantern/cleansed decorative density is still a tuning/art pass. |
| Our Lady presence | `Partial` | `OurLadyWhisperEvents` and `OurLadyWhisperDialogueTrees` now deliver purity/clarity-stage whispers with memo capture. A physical Our Lady apparition/entity remains a future concept; `SpectralCompanionEntity` exists as an Unstained summon shell, not the Lady herself. |
| Unstained dialogue expansion | `Implemented` | Zealot, Acolyte, Guardian, Scout, and Our Lady whisper dialogue trees exist. Unstained inquiry data covers Zealot/Guardian item dialogue, and the church structure now spawns the relevant NPCs. |
| Lethean / pale crafting recipes | `Implemented` | Tears of Silthmere, Lethean Poppy Wreath, Pale Distillate, Pale Silver, Cleansed Stone, Pallid Lantern, Pale Field Ink, Lethean Chalice, Pallid Icon, Verdigris Censer, Unstained armor/tools, Pallid Retort distillation, and White Humor purification recipes are present under `data/hemomancy/recipe/`. Ghost Pipe now has a Pallid Retort recipe into Pale Distillate. |
| Unstained advancement branch | `Implemented` | `UnstainedAdvancementGranter` and `UnstainedMilestoneHandler` grant altar, purity-stage, and clarity-stage advancements. JSONs exist for `unstained`, `path_of_purity`, `blessed_by_the_altar`, `tainted`, `cleansing`, `absolved`, `purified`, `clarity_awakened`, `discerning`, `vigilant`, `resolute_stage`, `enlightened_seeker`, and `lady_of_forgotten_waters`. |
| Silver Ward / Verdigris Aura indicators | `Implemented` | `SilverWardEffect` spawns ambient END_ROD particles and reduces hemomancy-mob damage; `VerdigrisAuraEffect` spawns SCRAPE ring particles and weakens hemomancy mobs. `UnstainedProgressScreen` exposes toggles through `PacketToggleUnstainedBonus`, and `PacketSyncUnstainedProgress` syncs toggle state. |
| Still Arts | `Implemented` | `StillArtInit`, `IKnownStillArts`, `KnownStillArtEvents`, Still Art packets, radial screen, cooldown overlay, and advancement-backed grants are active. See §15.1. |
| Unstained Cardinal Rites | `Implemented` | Purity and clarity rites are authored under `data/hemomancy/recipe/cardinal_rite/` and handled by Unstained rite event systems. See §15.2. |
| White Humor purification | `Implemented` | Recipe type/serializer, placed White Humor pools, finite pool charges, witness-block acceleration, JEI category, and Liber Immaculatus page are present. See §15.4. |

Remaining work should now be tracked as polish/expansion rather than baseline implementation: finish any desired Unstained Church block-palette pass, decide whether Our Lady ever receives a visible apparition entity, wire any Spectral Companion summoning flow if it becomes player-facing, and continue balancing purity/clarity rewards.

---
