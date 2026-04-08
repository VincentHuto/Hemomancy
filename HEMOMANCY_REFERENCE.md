# Hemomancy — Complete Mod Reference

> **Minecraft Version:** 1.20.1 (Forge)
> **Last Updated:** 2026-04-07

<!-- Texture base paths (relative from project root) -->
<!-- Items:  src/main/resources/assets/hemomancy/textures/item/ -->
<!-- Blocks: src/main/resources/assets/hemomancy/textures/block/ -->
<!-- Entity: src/main/resources/assets/hemomancy/textures/entity/ -->
<!-- GUI:    src/main/resources/assets/hemomancy/textures/gui/ -->
<!-- Effects:src/main/resources/assets/hemomancy/textures/mob_effect/ -->
<!-- Armor:  src/main/resources/assets/hemomancy/textures/models/armor/ -->
<!-- MnA:    src/main/resources/assets/hemomancy/textures/mna/ -->

Hemomancy is a blood magic mod built around the *quality* of blood manipulation rather than just quantity. It covers topics of gore, magic, exaggerated biology, fungi, secret societies, and cosmic horror. The power to control blood is the result of a **special fungal infection** — a sentient extraterrestrial fungus that deliberately broke off from a larger hive-mind organism (itself the physical manifestation of an outer-god-type entity) and landed on the Minecraft world, slowly taking hold.

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Core Player Capabilities](#2-core-player-capabilities)
3. [The Harbinger Path (Hematic Order)](#3-the-harbinger-path-hematic-order)
4. [The Unstained Path (Anti-Hemomancy)](#4-the-unstained-path-anti-hemomancy)
5. [Mutual Exclusion of Paths](#5-mutual-exclusion-of-paths)
6. [Blood Manipulations](#6-blood-manipulations)
7. [Blood Tendency (Kinship) System](#7-blood-tendency-kinship-system)
8. [Vascular System](#8-vascular-system)
9. [Skill Tree](#9-skill-tree)
10. [Bloodlines](#10-bloodlines)
11. [Morphlings](#11-morphlings)
12. [Runes & Spores](#12-runes--spores)
13. [Items & Materials](#13-items--materials)
14. [Tools & Weapons](#14-tools--weapons)
15. [Armor Sets](#15-armor-sets)
16. [Functional Blocks & Tile Entities](#16-functional-blocks--tile-entities)
17. [Decorative & Building Blocks](#17-decorative--building-blocks)
18. [Recipe Systems](#18-recipe-systems)
19. [Mob Entities](#19-mob-entities)
20. [Projectile & Blood Construct Entities](#20-projectile--blood-construct-entities)
21. [Status Effects & Potions](#21-status-effects--potions)
22. [World Generation & Biomes](#22-world-generation--biomes)
23. [Structures](#23-structures)
24. [Villagers & Professions](#24-villagers--professions)
25. [Mod Compatibility (MnA / Curios / JEI)](#25-mod-compatibility)
26. [GUIs & Overlays](#26-guis--overlays)
27. [Advancements](#27-advancements)
28. [Keybindings](#28-keybindings)
29. [Commands](#29-commands)
30. [Known WIP / Incomplete Systems](#30-known-wip--incomplete-systems)

---

## 1. Getting Started

1. **Find Gourd Seeds** ![Gourd Seeds](src/main/resources/assets/hemomancy/textures/item/gourd_seeds.png) — obtained from breaking grass (advancement: *Strange Seeds*).
2. **Discover a Blood Temple** — a naturally generating structure containing a **Mortal Display** pedestal.
3. **Activate the Blood Temple** — click the Mortal Display to awaken your blood, enabling the mod's features (advancement: *The First Awakening*). This activates your `IBloodVolume` capability (`active = true`).
4. **Obtain the Liber Sanguinum** ![Liber Sanguinum](src/main/resources/assets/hemomancy/textures/item/liber_sanguinum.png) — the mod's guide book (entity model: ![](src/main/resources/assets/hemomancy/textures/entity/liber_sanguinum.png)), crafted using a structure recipe (bookshelf + Sanguine Formation ![Sanguine Formation](src/main/resources/assets/hemomancy/textures/item/sanguine_formation.png)). (advancement: *Sanctum Sanguinium*).
5. **Craft Befouling Ash** ![Befouling Ash](src/main/resources/assets/hemomancy/textures/item/befouling_ash.png) — a key ingredient for blood structure recipes (advancement: *Ashen Beginnings*).

From here the player can pursue the **Harbinger Path** (blood magic) or eventually diverge to the **Unstained Path** (anti-blood purification).

---

## 2. Core Player Capabilities

All player-attached Forge capabilities, registered in `CapabilityInit`:

| Capability | Interface | Purpose |
|---|---|---|
| Blood Volume | `IBloodVolume` | Current/max blood, active state, bloodline link, trickle/auto-draw settings |
| Blood Tendency | `IBloodTendency` | 8-axis alignment scores (kinship with blood tendencies) |
| Vascular System | `IVascularSystem` | Health state of 7 vein sections |
| Known Manipulations | `IKnownManipulations` | Unlocked blood manipulations, selected manip, vein locations |
| Equipped Morphling | `IEquippedMorphling` | Currently equipped morphling for the Living Staff |
| Rune | `IRune` | Rune slot / rune binder state |
| Rune Item Handler | `IRunesItemHandler` | Inventory for rune binder contents |
| Initiatory Degree | `IInitiatoryDegree` | Harbinger rank (0–7) |
| Unstained Progress | `IUnstainedProgress` | Purification path state (purity, clarity, flags) |
| Earthen Vein Location | `IEarthenVeinLoc` | Block capability for earthen vein blocks |

---

## 3. The Harbinger Path (Hematic Order)

The default/primary progression. The player embraces hemomancy and rises through the ranks of a secret society called **The Hematic Order** (a.k.a. "The Harbingers").

### 3.1 Blood Volume

- **Interface:** `IBloodVolume`
- **Default:** 0 current / 5,000 max, `active = false`
- Activated by clicking a Blood Temple's Mortal Display
- Blood is spent to cast manipulations and power rituals
- Can be expanded via the Capacity skill (+500 per level)
- Stored in Blood Gourds for portable use
- Has **trickle donation** and **auto-draw** settings for Bloodline pool interaction

### 3.2 Initiatory Degrees

Progression through **Cardinal Rites** — multiblock blood rituals. Each rite advances the player to the next degree:

| Degree | Title | Cardinal Rite |
|--------|-------|---------------|
| 0 | Uninitiated | *(starting state)* |
| 1 | Neophyte of the Crimson Veil | `sanguine_initiation` |
| 2 | Votary of the Hematic Covenant | `votary_rite` |
| 3 | Initiate of the Scarlet Sanctum | `initiate_rite` |
| 4 | Adept of the Sanguine Brotherhood | `adept_rite` |
| 5 | Illuminatus of the Crimson Lodge | `illuminatus_rite` |
| 6 | Sanctified of the Bloodline Covenant | `sanctified_rite` |
| 7 | Archon of the Hematic Order | `archon_rite` |

Cardinal Rites have:
- A blood cost
- A rite type (`CardinalRiteType`)
- A multiblock pattern
- An item result
- A casting duration (tick-based, tracked via `ActiveCardinalRite`)
- Boundary enforcement (player must stay in range)
- Unwilling sacrifice processing

### 3.3 Cardinal Rite Casting Flow

Managed by `CardinalRiteEvents`:
1. Player initiates the rite at the correct multiblock
2. An `ActiveCardinalRite` is created, tracking the caster UUID, center position, recipe, duration, and rite size
3. Each tick: particles spawn, boundary checked, sacrifices processed
4. On completion: degree awarded, Unstained progress reset (if any), chat message sent

---

## 4. The Unstained Path (Anti-Hemomancy)

The divergent/opposing path. The player abandons blood magic in pursuit of purification and enlightenment, guided by **Unstained Zealot** NPCs.

### 4.1 Entry Requirements

- Player must have reached at least **Degree 2 (Votary)** before an Unstained Zealot will offer the choice
- The Zealot directs the player to bring **Hemolytic Solution** ![Hemolytic Solution](src/main/resources/assets/hemomancy/textures/item/hemolytic_solution.png) to an **Unstained Podium** block

### 4.2 Phase 1: Purity (0–100)

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

### 4.3 Phase 2: Clarity (0–100)

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

### 4.4 HUD

Unstained players see a dedicated gauge overlay (top-right corner) with:
- Silver **Purity** bar
- Teal **Clarity** bar (only after clarity unlocked)
- Stage labels for each

---

## 5. Mutual Exclusion of Paths

The two paths are **mutually exclusive**:
- **Starting Unstained** → resets Harbinger degree to 0
- **Completing a Harbinger degree rite** → resets all Unstained progress (purity → 0, clarity → 0, clarityUnlocked → false, begunPurification → false)
- Message: *"Your purification has been undone by the blood rite."*

---

## 6. Blood Manipulations

Blood manipulations are abilities fueled by blood. Lore-wise, they are dormant memories everyone has access to — unlocked via **Hematic Memory** items.

### 6.1 Manipulation Properties

Each manipulation has:
- **Name** — registry ID
- **Blood cost** — drained from the player's blood volume (modified by Efficiency skill and purity penalty)
- **XP cost** — additional experience cost
- **Alignment level** — required tendency alignment
- **Type** — `QUICK`, `CHARGED`, `PASSIVE`, or `CONTINUOUS`
- **Rank** — `HUMILIS`, `MEDIOCRITAS`, `SUMMA`, `MAGISTER`, `PERFECTUS`
- **Tendency** — which of the 8 blood tendencies it belongs to
- **Vein Section** — which vein section takes strain when cast
- **Cooldown** — tick-based cooldown between uses
- **ManipLevel** — manipulations level up with use

### 6.2 Registered Manipulations

| Name | Cost | Type | Rank | Tendency | Vein Section | Cooldown |
|------|------|------|------|----------|-------------|----------|
| `venous_travel` | 1000 | Continuous | Mediocritas | Ferric | Right Arm | 20t |
| `blood_shot` | 100 | Quick | Humilis | Animus | Head | 10t |
| `deadly_gaze` | 100 | Quick | Humilis | Animus | Head | 20t |
| `blood_needle` | 100 | Quick | Humilis | Animus | Head | 10t |
| `blood_rush` | 100 | Passive | Humilis | Animus | Body | 60t |
| `blood_cloud` | 300 | Quick | Summa | Animus | Head | 40t |
| `blood_aneurysm` | 400 | Quick | Summa | Animus | Body | 40t |
| `activation_potential` | 200 | Quick | Mediocritas | Ductilis | Body | 30t |
| `sanguine_ward` | 10 | Continuous | Mediocritas | Ductilis | Body | 20t |
| `ferric_transmutation` | 1000 | Quick | Summa | Ferric | Body | 20t |
| `conjure_blade` | 1000 | Quick (Conjuration) | Mediocritas | Ferric | Right Arm | 40t |
| `blood_absorption` | 1000 | Quick (Conjuration) | Mediocritas | Ferric | Right Arm | 40t |
| `blood_projection` | 1000 | Quick (Conjuration) | Mediocritas | Ferric | Right Arm | 40t |
| `summon_avatar` | 500 | Quick | Summa | Animus | Body | 100t |
| `summon_thrall` | 500 | Quick | Mediocritas | Animus | Body | 60t |
| `crimson_flame_conjuration` | 150 | Quick | Humilis | Animus | Right Arm | 15t |

### 6.3 Manipulation Tree

Manipulations are organized in a visual **Manipulation Tree** (displayed on the Skill Tree screen alongside the skill tree). Entries are defined in `ManipulationTreeInit` with parent-child relationships. Each node shows whether the player has learned it.

---

## 7. Blood Tendency (Kinship) System

The player has alignment scores across **8 blood tendencies**. These represent the player's affinity with different aspects of blood magic and determine which manipulations they can effectively use.

| Tendency | Concepts | Particle Color | Enzyme Item |
|----------|----------|---------------|-------------|
| **Animus** | Life, Regen, Living tools | Red (255,0,0) | ![](src/main/resources/assets/hemomancy/textures/item/vivacious_enzyme.png) Vivacious Enzyme |
| **Flammeus** | Fire, Heat, The Nether | Orange (255,100,0) | ![](src/main/resources/assets/hemomancy/textures/item/fervent_enzyme.png) Fervent Enzyme |
| **Ductilis** | Lightning, Speed, Nervous Energy | Yellow (255,255,0) | ![](src/main/resources/assets/hemomancy/textures/item/neurotic_enzyme.png) Neurotic Enzyme |
| **Lux** | Light, Flight, AOEs, Flashiness | White (255,255,255) | ![](src/main/resources/assets/hemomancy/textures/item/incandescent_enzyme.png) Incandescent Enzyme |
| **Mortem** | Death, Decay, Withering | Dark Green (0,58,0) | ![](src/main/resources/assets/hemomancy/textures/item/ruinous_enzyme.png) Ruinous Enzyme |
| **Congeatio** | Cold, Ice, Water | Blue (0,100,255) | ![](src/main/resources/assets/hemomancy/textures/item/frigid_enzyme.png) Frigid Enzyme |
| **Ferric** | Iron, Barbs, Solidity, Unchanging | Gray (53,53,53) | ![](src/main/resources/assets/hemomancy/textures/item/ferric_enzyme.png) Ferric Enzyme |
| **Tenebris** | Darkness, Stealth, The End | Purple (70,0,110) | ![](src/main/resources/assets/hemomancy/textures/item/umbral_enzyme.png) Umbral Enzyme |

Enzymes are obtained using a **Living Syringe** on mobs (primed with blood vials), then processed in a **Vial Centrifuge** to extract enzymes and Hematic Iron Powder.

---

## 8. Vascular System

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

## 9. Skill Tree

Opened from the **Dendritic Distributor** block. Has two tabs: **Skills** and **Manipulations**.

Skills cost **skill points** (earned from using manipulations) and many require a **minimum initiatory degree**:

| Skill | ID | Max Lvl | SP Cost | Req. Degree | Effect | Parent |
|-------|----|---------|---------|-------------|--------|--------|
| Base | 0 | 1 | — | — | Root node, unlocked by default | — |
| Capacity | 1 | 5 | 1 | — | +500 max blood volume per level | Base |
| Efficiency | 2 | 5 | 1 | — | -8% manipulation cost per level (multiplicative, ~34% at max) | Base |
| Last Wind | 3 | 3 | 2 | 2 | +2 blood regen/tick when below 10% blood | Capacity |
| Dynamic Use | 4 | 3 | 2 | 2 | +10% manipulation power when tendency matches | Efficiency |
| Feeding Frenzy | 5 | 3 | 3 | 3 | +25% blood gained from kills | Last Wind |
| Hemostasis | 6 | 3 | — | — | -10% blood lost when taking damage per level | Efficiency |
| Sanguine Surge | 7 | 3 | — | — | +1 passive blood regen/tick per level | Hemostasis |
| Crimson Mastery | 8 | 3 | — | — | +15% manipulation damage/effectiveness per level | Sanguine Surge |
| Vital Link | 9 | 3 | 4 | 5 | +10% chance to heal on dealing manipulation damage | Feeding Frenzy |
| Iron Will | 10 | 3 | 3 | 3 | 10% damage reduction per level when blood < 15% | Last Wind |
| Blood Flow | 11 | 5 | 2 | 3 | -5% manipulation cooldowns per level | Hemostasis |
| Coagulation | 12 | 3 | 3 | 4 | +15% chance to block incoming bleed effects | Hemostasis |
| Sanguine Reach | 13 | 3 | 3 | 5 | +15% range for ranged blood manipulations | Crimson Mastery |

Skill bonuses are computed in `SkillPointHelper`.

---

## 10. Bloodlines

A multiplayer social system where players form blood-bound groups.

- **Creation:** Use an **Unsigned Ancestral Ledger** — first use signs and creates a bloodline named after the leader
- **Joining:** Another player uses the same signed ledger to join
- **Shared Pool:** Each member contributes 5,000 blood to a communal pool
- **Trickle Donation:** Optionally auto-donate blood to the shared pool at a configurable rate
- **Auto-Draw:** Optionally auto-draw from the shared pool when personal blood falls below a threshold
- **Persistence:** Bloodline data is stored in world-level `BloodlineSavedData`
- **Monitoring:** The **Bloodline Pool Monitor** item shows pool status; the **BloodlinePoolScreen** provides a GUI

---

## 11. Morphlings

Symbiotic parasites derived from the fungal infection. They provide the Living Staff with different attack/ability modes.

### 11.1 Types

| Morphling | Item Class | Effect |
|-----------|-----------|--------|
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_fungal.png) Fungal | `FungalMorphlingItem` | Base fungal morphling |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_leeches.png) Leeches | `LeechesMorphlingItem` | Summons leeches to fight and drain blood |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_chitinite.png) Chitinite | `ChitiniteMorphlingItem` | Chitin shield / defense |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_serpent.png) Serpent | `SerpentMorphlingItem` | Paralyzing serpent projectile |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_pests.png) Pests | `PestsMorphlingItem` | Swarm of pest projectiles |
| Spider | `SpiderMorphlingItem` | Arachnid-themed attacks |

### 11.2 Cultivation

- Start with a **Morphling Polyp** ![Morphling Polyp](src/main/resources/assets/hemomancy/textures/item/morphling_polyp.png) (base form)
- Incubate in a **Morphling Incubator** block with enzymes to grow into specific morphling types
- Store morphlings in a **Morphling Jar** ![Morphling Jar](src/main/resources/assets/hemomancy/textures/item/morphling_jar.png) (6 slots, Uncommon rarity) — they bounce around inside
- The **Living Staff** cycles through equipped morphlings and changes its topper model accordingly

---

## 12. Runes & Spores

### 12.1 Runes

Runes are equippable items stored in a **Rune Binder** ![Rune Binder](src/main/resources/assets/hemomancy/textures/item/rune_binder.png) (18 slots) or **Upgraded Rune Binder** ![Rune Binder Upgraded](src/main/resources/assets/hemomancy/textures/item/rune_binder_upgraded.png) (27 slots). They are crafted at the **Runic Chisel Station**.

| Rune | Tendency |
|------|----------|
| ![](src/main/resources/assets/hemomancy/textures/item/rune_transcendence.png) Rune Transcendence | Lux |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_sol.png) Rune Sol | Flammeus |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_heart.png) Rune Heart | Animus |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_descendence.png) Rune Descendence | Mortem |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_moon.png) Rune Moon | Congeatio |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_eye.png) Rune Eye | Ductilis |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_feral.png) Rune Feral | Ductilis |
| ![](src/main/resources/assets/hemomancy/textures/item/mind_spike.png) Mind Spike | Ductilis |

Each rune has a corresponding **Rune Pattern** item used in crafting.

### 12.2 Functional Spores (Rune-type items)

Special fungal rune items with active effects:

| Item | Tendency | Effect |
|------|----------|--------|
| ![](src/main/resources/assets/hemomancy/textures/item/respergillus.png) Respergillus | Animus | Functional spore |
| ![](src/main/resources/assets/hemomancy/textures/item/talaromyces_minus.png) Talaromyces Minus | Ferric | Functional spore |
| ![](src/main/resources/assets/hemomancy/textures/item/lumina_devorans.png) Lumina Devorans | Tenebris | Functional spore |
| ![](src/main/resources/assets/hemomancy/textures/item/noctifly_agaric.png) Noctifly Agaric | Animus | Grants the Noctifly Agaric (Elytra) effect |

### 12.3 Spores (Passive rune items)

One for each tendency:
![](src/main/resources/assets/hemomancy/textures/item/vivacious_spores.png) Vivacious,
![](src/main/resources/assets/hemomancy/textures/item/ferric_spores.png) Ferric,
![](src/main/resources/assets/hemomancy/textures/item/fervent_spores.png) Fervent,
![](src/main/resources/assets/hemomancy/textures/item/incandescent_spores.png) Incandescent,
![](src/main/resources/assets/hemomancy/textures/item/neurotic_spores.png) Neurotic,
![](src/main/resources/assets/hemomancy/textures/item/ruinous_spores.png) Ruinous,
![](src/main/resources/assets/hemomancy/textures/item/umbral_spores.png) Umbral,
![](src/main/resources/assets/hemomancy/textures/item/frigid_spores.png) Frigid.

---

## 13. Items & Materials

### 13.1 Key Materials

| Item | Purpose |
|------|---------|
| ![](src/main/resources/assets/hemomancy/textures/item/sanguine_formation.png) Sanguine Formation | Catalyst for blood structure recipes |
| ![](src/main/resources/assets/hemomancy/textures/item/befouling_ash.png) Befouling Ash / ![](src/main/resources/assets/hemomancy/textures/item/smouldering_ash.png) Smouldering Ash | Ash trails for rituals and recipes |
| ![](src/main/resources/assets/hemomancy/textures/item/active_befouling_ash.png) Active Befouling / ![](src/main/resources/assets/hemomancy/textures/item/active_smouldering_ash.png) Active Smouldering Ash | Active versions of ash trails |
| ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_scrap.png) Hematic Iron Scrap | Blood-infused iron alloy ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_powder.png) Hematic Iron Powder | Extracted from blood via centrifuge |
| ![](src/main/resources/assets/hemomancy/textures/item/consecrated_copper_ingot.png) Consecrated Copper Ingot | Anti-blood copper, used in Unstained path |
| ![](src/main/resources/assets/hemomancy/textures/item/hemolytic_solution.png) Hemolytic Solution | Anti-blood enzyme solution, starts the Unstained path |
| ![](src/main/resources/assets/hemomancy/textures/item/hemolytic_plating.png) Hemolytic Plating | Silver-based anti-blood plating |
| ![](src/main/resources/assets/hemomancy/textures/item/neutralizing_gasket.png) Neutralizing Gasket | Anti-blood component |
| ![](src/main/resources/assets/hemomancy/textures/item/foul_paste.png) Foul Paste | Crafting ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_rock.png) Blood Rock | Crafting ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/sanguine_conduit.png) Sanguine Conduit | Crafting ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/serpent_scale.png) Serpent Scale | Mob drop |
| ![](src/main/resources/assets/hemomancy/textures/item/swollen_leech.png) Swollen / ![](src/main/resources/assets/hemomancy/textures/item/dried_leech.png) Dried Leech | Mob drops |
| ![](src/main/resources/assets/hemomancy/textures/item/chitinous_husk.png) Chitinous Husk | Mob drop |
| ![](src/main/resources/assets/hemomancy/textures/item/puppeteering_thread.png) Puppeteering Thread | Mob drop |
| ![](src/main/resources/assets/hemomancy/textures/item/bleeding_bulb.png) Bleeding Bulb | Plant-based ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/dicentra_sap.png) Dicentra Sap | Plant-based ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/spore_sac.png) Spore Sac | Fungal ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_crystal_shard.png) Blood Crystal Shard / ![](src/main/resources/assets/hemomancy/textures/item/cleansed_blood_crystal_shard.png) Cleansed Blood Crystal Shard | Crystal materials |
| ![](src/main/resources/assets/hemomancy/textures/item/vivianite_cluster.png) Vivianite Cluster | Mineral material |
| ![](src/main/resources/assets/hemomancy/textures/item/gourd_seeds.png) Gourd Seeds | Plantable, grows gourds |
| ![](src/main/resources/assets/hemomancy/textures/item/dried_gourd.png) Dried Gourd | Gourd processing product |

### 13.2 Blood Storage Items

| Item | Capacity |
|------|----------|
| ![](src/main/resources/assets/hemomancy/textures/item/bloody_flask.png) Bloody Flask | 250 |
| ![](src/main/resources/assets/hemomancy/textures/item/bloody_jug.png) Bloody Jug | 2,500 |
| ![](src/main/resources/assets/hemomancy/textures/item/stabilized_sanguine_formation.png) Stabilized Sanguine Formation | 5,000 |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_gourd_white.png) Blood Gourd White | Simple tier |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_gourd_red.png) Blood Gourd Red | Crimson tier |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_gourd_black.png) Blood Gourd Black | Ashen tier |
| ![](src/main/resources/assets/hemomancy/textures/item/curved_horn.png) Curved Horn | Horn tier |
| ![](src/main/resources/assets/hemomancy/textures/item/bloody_vial.png) Bloody Vial | Holds extracted blood for centrifuging |

> **Blood Gourd 3D models (open/closed):**
>
> | White | Red | Black | Curved Horn |
> |---|---|---|---|
> | ![](src/main/resources/assets/hemomancy/textures/entity/blood_gourd/white.png) ![](src/main/resources/assets/hemomancy/textures/entity/blood_gourd/white_open.png) | ![](src/main/resources/assets/hemomancy/textures/entity/blood_gourd/red.png) ![](src/main/resources/assets/hemomancy/textures/entity/blood_gourd/red_open.png) | ![](src/main/resources/assets/hemomancy/textures/entity/blood_gourd/black.png) ![](src/main/resources/assets/hemomancy/textures/entity/blood_gourd/black_open.png) | ![](src/main/resources/assets/hemomancy/textures/entity/blood_gourd/curved_horn.png) ![](src/main/resources/assets/hemomancy/textures/entity/blood_gourd/curved_horn_open.png) |

### 13.3 Memory Items

| Item | Purpose |
|------|---------|
| ![](src/main/resources/assets/hemomancy/textures/item/hematic_memory.png) Hematic Memory | Base blank memory item |
| ![](src/main/resources/assets/hemomancy/textures/item/lethian_dew.png) Lethian Dew | Memory processing ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/lethian_brew.png) Lethian Brew | Cursed clay jar from the River Lethe — enables forgetting memories |
| ![](src/main/resources/assets/hemomancy/textures/item/fervent_husk.png) Fervent Husk | Memory processing ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_stained_stone.png) Blood Stained Stone | Memory-related item |
| Blood Memory (per manipulation) | One for each registered manipulation — using it teaches the player |

**Memory Textures Gallery:**

| | | | |
|---|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/item/memory_blood_shot.png) Blood Shot | ![](src/main/resources/assets/hemomancy/textures/item/memory_deadly_gaze.png) Deadly Gaze | ![](src/main/resources/assets/hemomancy/textures/item/memory_blood_needle.png) Blood Needle | ![](src/main/resources/assets/hemomancy/textures/item/memory_blood_rush.png) Blood Rush |
| ![](src/main/resources/assets/hemomancy/textures/item/memory_blood_cloud.png) Blood Cloud | ![](src/main/resources/assets/hemomancy/textures/item/memory_blood_aneurysm.png) Blood Aneurysm | ![](src/main/resources/assets/hemomancy/textures/item/memory_activation_potential.png) Activation Potential | ![](src/main/resources/assets/hemomancy/textures/item/memory_sanguine_ward.png) Sanguine Ward |
| ![](src/main/resources/assets/hemomancy/textures/item/memory_venous_travel.png) Venous Travel | ![](src/main/resources/assets/hemomancy/textures/item/memory_ferric_transmutation.png) Ferric Transmutation | ![](src/main/resources/assets/hemomancy/textures/item/memory_living_blade.png) Living Blade | ![](src/main/resources/assets/hemomancy/textures/item/memory_blood_absorption.png) Blood Absorption |
| ![](src/main/resources/assets/hemomancy/textures/item/memory_blood_projection.png) Blood Projection | ![](src/main/resources/assets/hemomancy/textures/item/memory_summon_avatar.png) Summon Avatar | | |

### 13.4 Diagnostic Items

| Item | Purpose |
|------|---------|
| ![](src/main/resources/assets/hemomancy/textures/item/blood_tendency_gauge.png) Blood Tendency Gauge | Inspect current blood tendency alignment |
| ![](src/main/resources/assets/hemomancy/textures/item/vascular_status_gauge.png) Vascular Status Gauge | Inspect vein section health |
| ![](src/main/resources/assets/hemomancy/textures/item/bloodline_pool_monitor.png) Bloodline Pool Monitor | View bloodline shared pool status |
| ![](src/main/resources/assets/hemomancy/textures/item/self_reflection_mirror.png) Self Reflection Mirror | Rune-related inspection |

### 13.5 Miscellaneous

| Item | Purpose |
|------|---------|
| ![](src/main/resources/assets/hemomancy/textures/item/charm_of_vascularium.png) Charm of Vascularium | Enables blood manipulations; equippable accessory (Curios) ![](src/main/resources/assets/hemomancy/textures/entity/model_layer_vasc_charm.png) |
| ![](src/main/resources/assets/hemomancy/textures/item/liber_sanguinum.png) Liber Sanguinum | Guide book |
| ![](src/main/resources/assets/hemomancy/textures/item/unsigned_ancestral_ledger.png) Unsigned Ancestral Ledger | Creates/joins bloodlines |
| ![](src/main/resources/assets/hemomancy/textures/item/engram_stamp.png) Engram Stamp | Engram-related tool |
| ![](src/main/resources/assets/hemomancy/textures/item/vivianite_scalpel.png) Vivianite Scalpel | Vivianite-based tool |
| ![](src/main/resources/assets/hemomancy/textures/item/fungal_spine.png) Fungal Spine | Fungal tool item (unstackable, Uncommon) |
| ![](src/main/resources/assets/hemomancy/textures/item/sanguine_salve.png) Sanguine Salve | Heals 25 blood on use |
| ![](src/main/resources/assets/hemomancy/textures/item/cleansing_hemolymph.png) Cleansing Hemolymph | Blue vial from Hemolymphopoda mobs |
| ![](src/main/resources/assets/hemomancy/textures/item/structure_spawner.png) Structure Spawner | Debug/creative item for spawning structures |
| ![](src/main/resources/assets/hemomancy/textures/item/recycled_enzyme.png) Recycled Enzyme | Generic enzyme fallback |

### 13.6 Banner Patterns

- ![](src/main/resources/assets/hemomancy/textures/item/heart_pattern.png) **Heart Pattern** — Vascularium Crest
- ![](src/main/resources/assets/hemomancy/textures/item/veins_pattern.png) **Veins Pattern** — Vein Border

---

## 14. Tools & Weapons

### 14.1 Tool Tiers

| Tier | Enum |
|------|------|
| Hematic Iron | `HEMATIC_IRON` |
| Living | `LIVING` |

### 14.2 Living Tools (Blood-powered)

All are single-stack, use the `LIVING` tool tier:

| Weapon | Class | Notes |
|--------|-------|-------|
| Living Blade | `LivingBladeItem` | Blood-feeding sword (25 base dmg, +3 speed) |
| Living Axe | `LivingAxeItem` | Blood-feeding axe |
| Living Spear | `LivingSpearItem` | Blood-feeding polearm |
| Living Baghnakh | `LivingBaghnakhItem` | Blood-feeding claw weapon |
| Living Staff | `LivingStaffItem` | Channels morphlings and blood magic |
| Living Syringe | `LivingSyringeItem` | Extracts blood vials from mobs |
| Living Crossbow | `LivingCrossbowItem` | Fires Blood Bolts |
| Sanguis Lancea | `SanguisLanceaItem` | Throwable blood lance (25 base dmg) |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_absorption.png) Blood Absorption | `BloodAbsorptionItem` | Conjured blood-absorbing tool |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_projection.png) Blood Projection | `BloodProjectionItem` | Conjured blood projectile launcher |

> *Note: Living tools (blade, axe, spear, staff, syringe, crossbow, lancea, baghnakh) use 3D entity models rather than flat item textures — see `src/main/resources/assets/hemomancy/textures/entity/` for their model textures:*
>
> ![](src/main/resources/assets/hemomancy/textures/entity/model_living_blade_hand.png) ![](src/main/resources/assets/hemomancy/textures/entity/model_living_axe_hand.png) ![](src/main/resources/assets/hemomancy/textures/entity/model_living_spear_hand.png)

### 14.3 Hematic Iron Weapons

| Weapon | Notes |
|--------|-------|
| ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_sword.png) Hematic Iron Sword | Standard sword tier |
| ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_knapper.png) Hematic Iron Knapper | Specialized knapping tool (42 dmg) |

### 14.4 Other Weapons

| Weapon | Notes |
|--------|-------|
| Barbed Blade | Sword-class, Living tier, +3 speed, +25 dmg |
| Chitinite Mace | Sword-class, Living tier |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_bolt.png) Blood Bolt | Ammo for Living Crossbow |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_thrall_effigy.png) Blood Thrall Effigy | Summons a Blood Thrall creature (stackable to 16) |
| ![](src/main/resources/assets/hemomancy/textures/item/drudge_electrode.png) Drudge Electrode | Tool item |

---

## 15. Armor Sets

### 15.1 Hematic Iron Armor

Standard blood-infused iron armor set (fire resistant):
- ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_helm.png) Helm, ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_chestplate.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_leggings.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_boots.png) Boots

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/hematic_iron_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/hematic_iron_layer_2.png)

### 15.2 Blood Lust Armor

Special armor with mask variants:
- ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_helm.png) Helm (no mask), ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_helm_tengu.png) Helm (Tengu mask), ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_helm_horned.png) Helm (Horned mask)
- ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_chest.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_legs.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_boots.png) Boots
- Mask items: ![](src/main/resources/assets/hemomancy/textures/item/tengu_mask.png) Tengu Mask, ![](src/main/resources/assets/hemomancy/textures/item/horned_mask.png) Horned Mask (crafting ingredients)

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/blood_lust_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/blood_lust_layer_2.png)

### 15.3 Barbed Armor

Defensive barbed armor set:
- ![](src/main/resources/assets/hemomancy/textures/item/barbed_helm.png) Helm, ![](src/main/resources/assets/hemomancy/textures/item/barbed_chestplate.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/barbed_leggings.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/barbed_boots.png) Boots
- Barbed Shield ![](src/main/resources/assets/hemomancy/textures/entity/barbed_shield/model_barbed_shield.png)

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/barbed_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/barbed_layer_2.png)

### 15.4 Chitinite Armor

Insectoid/chitin-based armor:
- ![](src/main/resources/assets/hemomancy/textures/item/chitinite_helm.png) Helm, ![](src/main/resources/assets/hemomancy/textures/item/chitinite_chestplate.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/chitinite_leggings.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/chitinite_boots.png) Boots
- Chitinite Shield ![](src/main/resources/assets/hemomancy/textures/entity/chitinite_shield/model_chitinite_shield.png)
- Chitinite Arm Banner (dyeable, 16 colors)

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/chitinite_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/chitinite_layer_2.png)

### 15.5 Unstained Armor

Anti-blood zealot armor (for the Unstained path):
- ![](src/main/resources/assets/hemomancy/textures/item/unstained_helm.png) Helm, ![](src/main/resources/assets/hemomancy/textures/item/unstained_chestplate.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/unstained_leggings.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/unstained_boots.png) Boots

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/unstained_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/unstained_layer_2.png)

### 15.6 Crown of Sacred Marrow

Special artifact helmet (`MarrowCrownArmorItem`), uses `MARROW_CROWN` tier.

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/marrow_crown_layer_1.png)

---

## 16. Functional Blocks & Tile Entities

| Block | Tile Entity | Purpose |
|-------|-------------|---------|
| **Mortal Display** | `MortalDisplayBlockEntity` | Activates blood magic when clicked in a Blood Temple ![](src/main/resources/assets/hemomancy/textures/entity/model_floating_heart.png) |
| **Scrying Podium** | `ScryingPodiumBlockEntity` | Opens the Charm/Gourd slot screen for equipping the Charm of Vascularium and Blood Gourds |
| **Visceral Artificial Recaller** | `VisceralRecallerBlockEntity` | Crafting station for creating Hematic Memories using enzymes, blank memories, and catalysts |
| **Vial Centrifuge** | `VialCentrifugeBlockEntity` | Spins down Bloody Vials into enzymes and Hematic Iron Powder ![](src/main/resources/assets/hemomancy/textures/entity/model_centrifuge_arms.png) |
| **Juiceinator** | `JuicinatorBlockEntity` | Squeezes items to extract blood (requires fire below) |
| **Runic Chisel Station** | `ChiselStationBlockEntity` | Crafts runes from patterns and blanks |
| **Morphling Incubator** | `MorphlingIncubatorBlockEntity` | Grows Morphling Polyps into specific morphling types with enzymes |
| **Fungal Podium** | `FungalPodiumBlockEntity` | Fungal-related interaction station |
| **Fungal Implantation Pylon** | `FungalImplantationPylonBlockEntity` | Sporic implantation station ![](src/main/resources/assets/hemomancy/textures/entity/fungal_implantation_pylon/fungal_implantation_pylon.png) |
| **Dendritic Distributor** | `DendriticDistributorBlockEntity` | Opens the Skill Tree / Manipulation Tree screen |
| **Unstained Podium** | `UnstainedPodiumBlockEntity` | Where Hemolytic Solution / Consecrated Copper are used for the Unstained path |
| **Semi-Sentient Construct** | `SemiSentientConstructBlockEntity` | Blood construct-related block |
| **Earthen Vein** | `EarthenVeinBlockEntity` | Vein location marker for teleportation (Venous Travel) ![](src/main/resources/assets/hemomancy/textures/entity/earthen_vein/model_earthen_vein.png) |
| **Iron Brazier** | `IronBrazierBlockEntity` | Decorative/functional brazier |
| **Suspended Blood Crystal** | `SuspendedBloodCrystalBlockEntity` | Floating blood crystal display ![](src/main/resources/assets/hemomancy/textures/entity/model_suspended_blood_crystal.png) |
| **Suspended Vivianite** | `SuspendedVivianiteBlockEntity` | Floating vivianite display ![](src/main/resources/assets/hemomancy/textures/entity/model_suspended_vivianite.png) |
| **Humane Idol** | `HumaneIdolBlockEntity` | Idol block |
| **Serpentine Idol** | `SerpentineIdolBlockEntity` | Idol block |
| **Engram Block** | — | Translucent engram |
| **Filler Block** | `FillerBlockEntity` | Indestructible filler for multiblocks |
| **Bog Body** | — | Decorative translucent body block |

---

## 17. Decorative & Building Blocks

### 17.1 Venous Stone Family

A full block family with variants:

| | | |
|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/block/venous_stone.png) Venous Stone | ![](src/main/resources/assets/hemomancy/textures/block/polished_venous_stone.png) Polished Venous Stone | ![](src/main/resources/assets/hemomancy/textures/block/polished_venous_stone_bricks.png) Polished Venous Stone Bricks |
| ![](src/main/resources/assets/hemomancy/textures/block/chiseled_polished_venous_stone.png) Chiseled Polished | ![](src/main/resources/assets/hemomancy/textures/block/cracked_polished_venous_stone_bricks.png) Cracked Bricks | ![](src/main/resources/assets/hemomancy/textures/block/gilded_venous_stone.png) Gilded Venous Stone |
| ![](src/main/resources/assets/hemomancy/textures/block/infested_venous_stone.png) Infested Venous Stone | | |

- Venous Stone, Slab, Stairs
- Polished Venous Stone, Slab, Stairs
- Polished Venous Stone Bricks, Slab, Stairs
- Chiseled Polished Venous Stone
- Cracked Polished Venous Stone Bricks
- Gilded Venous Stone
- Infested Venous Stone

### 17.2 Hematic Iron Family

| | | |
|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/block/hematic_iron_block.png) Hematic Iron Block | ![](src/main/resources/assets/hemomancy/textures/block/hematic_iron_pillar.png) Hematic Iron Pillar | ![](src/main/resources/assets/hemomancy/textures/block/chiseled_hematic_iron_block.png) Chiseled Hematic Iron |

- Hematic Iron Block
- Hematic Iron Pillar (rotatable)
- Chiseled Hematic Iron Block

### 17.3 Anti-Blood

- ![](src/main/resources/assets/hemomancy/textures/block/hemolytic_plating_block.png) Hemolytic Plating Block

### 17.4 Glass & Panes

| | | | |
|---|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/block/sanguine_glass.png) Sanguine Glass | ![](src/main/resources/assets/hemomancy/textures/block/sanguine_pane.png) Sanguine Pane | ![](src/main/resources/assets/hemomancy/textures/block/vivianite_glass.png) Vivianite Glass | ![](src/main/resources/assets/hemomancy/textures/block/vivianite_pane.png) Vivianite Pane |

### 17.5 Wood & Organic

| | | |
|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/block/blood_wood_log.png) Blood Wood Log | ![](src/main/resources/assets/hemomancy/textures/block/blood_wood_planks.png) Blood Wood Planks | ![](src/main/resources/assets/hemomancy/textures/block/conscious_mass.png) Conscious Mass |

- Blood Wood Log (rotatable pillar)
- Blood Wood Planks
- Conscious Mass (wart-block sound)

### 17.6 Fungal / Plant Blocks

| | | | |
|---|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/block/hyphae.png) Hyphae | ![](src/main/resources/assets/hemomancy/textures/block/hyphae_block.png) Hyphae Block | ![](src/main/resources/assets/hemomancy/textures/block/infected_stem.png) Infected Stem | ![](src/main/resources/assets/hemomancy/textures/block/infected_cap.png) Infected Cap |
| ![](src/main/resources/assets/hemomancy/textures/block/fruiting_infected_cap.png) Fruiting Infected Cap | ![](src/main/resources/assets/hemomancy/textures/block/erythrocytic_dirt.png) Erythrocytic Dirt | ![](src/main/resources/assets/hemomancy/textures/block/erythrocytic_mycelium_top.png) Erythrocytic Mycelium | ![](src/main/resources/assets/hemomancy/textures/block/bleeding_heart.png) Bleeding Heart |
| ![](src/main/resources/assets/hemomancy/textures/block/infected_fungus.png) Infected Fungus | ![](src/main/resources/assets/hemomancy/textures/block/stinkhorn_fungus.png) Stinkhorn Fungus | ![](src/main/resources/assets/hemomancy/textures/block/lethean_poppy_bloomed.png) Lethean Poppy | |

- Hyphae (cross-block, replaceable)
- Hyphae Block
- Infected Stem
- Infected Cap / Fruiting Infected Cap
- Erythrocytic Dirt
- Erythrocytic Mycelium (spreads, random ticks)
- Bleeding Heart (flower, Absorption effect)
- Infected Fungus (flower, Confusion effect)
- Stinkhorn Fungus (Confusion effect)
- Puffball Fungus (Saturation effect)
- Lethean Poppy (Regeneration effect, random ticks)

All applicable flowers have **potted** variants.

### 17.7 Gourd

| | |
|---|---|
| ![](src/main/resources/assets/hemomancy/textures/block/gourd_side.png) Gourd | ![](src/main/resources/assets/hemomancy/textures/block/gourd_stem.png) Gourd Stem |

- Gourd (pumpkin-like, grows from stem)
- Gourd Stem / Attached Gourd Stem

### 17.8 Ash Trails

| | | | |
|---|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/item/smouldering_ash.png) Smouldering Ash | ![](src/main/resources/assets/hemomancy/textures/item/active_smouldering_ash.png) Active Smouldering | ![](src/main/resources/assets/hemomancy/textures/item/befouling_ash.png) Befouling Ash | ![](src/main/resources/assets/hemomancy/textures/item/active_befouling_ash.png) Active Befouling |

- Smouldering Ash Trail / Active Smouldering Ash Trail
- Befouling Ash Trail / Active Befouling Ash Trail

### 17.9 Misc

- ![](src/main/resources/assets/hemomancy/textures/block/crimson_flames.png) Crimson Flames (special fire block)
- Blood Crystal (modeled block)

---

## 18. Recipe Systems

| Recipe Type | Serializer | Station | Purpose |
|-------------|-----------|---------|---------|
| `chisel_recipe` | `ChiselRecipeSerializer` | Runic Chisel Station | Crafting runes |
| `juiceinator_recipe_type` | `JuiceinatorSerializer` | Juiceinator | Squeezing items for blood |
| `recaller_recipe_type` | `RecallerRecipeSerializer` | Visceral Recaller | Creating Hematic Memories |
| `blood_structure_recipe` | `BloodStructureRecipeSerializer` | In-world structure | Structure crafting (hit structure with catalyst + blood) |
| `cardinal_rite_recipe` | `CardinalRiteRecipeSerializer` | Multiblock | Cardinal Rites for degree advancement |
| Morphling Jar Upgrade | `CopyMorphlingJarRecipe.Serializer` | Crafting | Upgrading morphling jars |
| Blood Gourd Upgrade | `CopyBloodGourdRecipe.Serializer` | Crafting | Upgrading blood gourds |
| Blood Gourd Fill | `FillBloodGourdRecipe.Serializer` | Crafting | Filling gourds with blood |

### 18.1 Blood Structure Crafting

An in-world system: build a specific block structure, then hit a particular block with a catalyst item while spending blood. The structure transforms into the desired output.

---

## 19. Mob Entities

### 19.1 Hostile / Monster Mobs

| Entity | Texture | Category | Notes |
|--------|---------|----------|-------|
| **Fargone** | ![](src/main/resources/assets/hemomancy/textures/entity/fargone/model_fargone.png) | Monster | Standard blood monster |
| **Thirster** | ![](src/main/resources/assets/hemomancy/textures/entity/thirster/model_thirster.png) | Monster | Blood-thirsting mob |
| **Abhorent Thought** | | Monster | Large (1.5×3.25), eldritch thought entity |
| **Erythromycelium Eruptus** | ![](src/main/resources/assets/hemomancy/textures/entity/erythromycelium_eruptus/model_erythromycelium_eruptus.png) | Monster | Large fungal eruption mob (1.5×3.0) |
| **Blood Drunk Puppeteer** | ![](src/main/resources/assets/hemomancy/textures/entity/blood_drunk_puppeteer/model_blood_drunk_puppeteer.png) | Monster | Human-sized, controls dolls |
| **Enthralled Doll** | ![](src/main/resources/assets/hemomancy/textures/entity/enthralled_doll/model_enthralled_doll.png) | Monster | Small (0.5×0.5), controlled by puppeteer |
| **Chthonian** | ![](src/main/resources/assets/hemomancy/textures/entity/chthonian/model_chthonian.png) | Monster | Underground worm creature |
| **Chthonian Queen** | ![](src/main/resources/assets/hemomancy/textures/entity/chthonian_queen/model_chthonian_queen.png) | Monster | Boss variant of Chthonian |
| **Lump of Thought** | ![](src/main/resources/assets/hemomancy/textures/entity/lump_of_thought/model_lump_of_thought.png) | Monster | Sentient thought blob |
| **Morphling Polyp** (mob) | ![](src/main/resources/assets/hemomancy/textures/entity/morphling_polyp/model_morphling_polyp.png) | Monster | Wild morphling mob |

### 19.2 Creature / Ambient Mobs

| Entity | Texture | Category | Notes |
|--------|---------|----------|-------|
| **Leech** | ![](src/main/resources/assets/hemomancy/textures/entity/leech/model_leech_brown.png) | Creature | Small (0.4×0.1) blood-sucking leech |
| **Fungling** | ![](src/main/resources/assets/hemomancy/textures/entity/fungling/model_fungling.png) | Creature | Friendly fungal creature |
| **Chitinite** | ![](src/main/resources/assets/hemomancy/textures/entity/chitinite/model_chitinite.png) | Creature | Iron-shelled insect (1.0×0.3) |
| **Fervent Chitinite** | ![](src/main/resources/assets/hemomancy/textures/entity/fervent_chitinite/model_fervent_chitinite.png) | Creature | Fire variant of Chitinite |
| **Hemolymphopoda** | ![](src/main/resources/assets/hemomancy/textures/entity/hemolymphopoda/model_hemolymphopoda.png) | Ambient | Small (0.9×0.3), drops Cleansing Hemolymph |
| **Barbed Urchin** | ![](src/main/resources/assets/hemomancy/textures/entity/barbed_urchin/model_barbed_urchin.png) | Water Ambient | Underwater iron-barbed urchin |

### 19.3 Summons / Player-controlled

| Entity | Texture | Category | Notes |
|--------|---------|----------|-------|
| **Blood Thrall** | ![](src/main/resources/assets/hemomancy/textures/entity/blood_thrall/blood_thrall.png) | Creature | Small (0.6×0.7), summoned blood transport creature |
| **Unstained Zealot** | ![](src/main/resources/assets/hemomancy/textures/entity/unstained_zealot/unstained_zealot.png) | Creature | NPC that guides Unstained path entry |

### 19.4 Entity Tags

Mobs are tagged by tendency: `FUNGAL_TAG`, `UMBRAL_TAG`, `INCANDESCENT_TAG`, `FERRIC_TAG`, `VIVACIOUS_TAG`, `RUINOUS_TAG`, `NEUROTIC_TAG`, `FERVENT_TAG`, `FRIGID_TAG`.

### 19.5 Spawn Placements

Registered in `EntityInit.commonSetup`:
- Barbed Urchin → `IN_WATER`
- Hemolymphopoda → `ON_GROUND`
- Fargone → `ON_GROUND` (monster rules)
- Abhorent Thought → `ON_GROUND` (monster rules)

---

## 20. Projectile & Blood Construct Entities

### 20.1 Blood Constructs

Extend `BloodConstructEntity` (a `PathfinderMob` implementing `IBloodConstruct`). They are summoned by the player and have a limited lifetime (`deathTicks`):

| Entity | Notes |
|--------|-------|
| Blood Cloud (`CloudEntityBlood`) | Area-of-effect blood cloud |
| ![](src/main/resources/assets/hemomancy/textures/entity/iron_pillar/model_iron_pillar.png) Iron Pillar (`EntityIronPillar`) | 0.75×2.8 iron construct |
| ![](src/main/resources/assets/hemomancy/textures/entity/iron_wall/model_iron_wall.png) Iron Wall (`EntityIronWall`) | 1.6×2.8 iron wall construct |
| ![](src/main/resources/assets/hemomancy/textures/entity/iron_spike/model_iron_spike.png) Iron Spike (`EntityIronSpike`) | 1.4×1.5 iron spike trap |
| ![](src/main/resources/assets/hemomancy/textures/entity/wretched_will/modelwretchedwill.png) Wretched Will (`EntityWretchedWill`) | Will-based construct |

### 20.2 Projectiles

| Entity | Texture | Notes |
|--------|---------|-------|
| Directed Blood Orb | | High tracking range (150), main blood projectile |
| Tracking Blood Orb | | Homing blood orb |
| Blood Cloud Carrier | | Delivers blood clouds |
| Tracking Serpent | ![](src/main/resources/assets/hemomancy/textures/entity/crimson_serpent/model_crimson_serpent.png) | Homing snake projectile |
| Tracking Pests | | Homing pest swarm |
| Blood Bolt | ![](src/main/resources/assets/hemomancy/textures/entity/blood_bolt/model_blood_bolt.png) | Crossbow ammo entity |
| Blood Needle | ![](src/main/resources/assets/hemomancy/textures/entity/blood_needle/model_blood_needle.png) | Small fast projectile |
| Blood Bullet | ![](src/main/resources/assets/hemomancy/textures/entity/blood_bullet/model_blood_bullet.png) | Pistol-type projectile |
| Blood Shot | ![](src/main/resources/assets/hemomancy/textures/entity/blood_shot/model_blood_shot.png) | Shotgun-style spread |
| Sanguis Lancea | ![](src/main/resources/assets/hemomancy/textures/entity/sanguis_lancea/model_sanguis_lancea.png) | Thrown spear entity |
| Dark Arrow | | Dark-themed arrow |

### 20.3 Item Entities

| Entity | Notes |
|--------|-------|
| Flying Charm | The Charm of Vascularium flying to the player |
| Morphling Polyp Item | Dropped morphling polyp pickup |

---

## 21. Status Effects & Potions

Each effect has a corresponding potion, splash potion, lingering potion, and tipped arrow variant:

| Effect | Category | Color | Notable Mechanic |
|--------|----------|-------|-----------------|
| ![](src/main/resources/assets/hemomancy/textures/mob_effect/blood_binding.png) **Blood Binding** | Harmful | Dark red | Immobilizes target |
| ![](src/main/resources/assets/hemomancy/textures/mob_effect/blood_loss.png) **Blood Loss** | Harmful | Red | -15% movement speed |
| ![](src/main/resources/assets/hemomancy/textures/mob_effect/blood_rush.png) **Blood Rush** | Beneficial | Red | +20% move speed, +10% attack speed |
| ![](src/main/resources/assets/hemomancy/textures/mob_effect/hemolysis.png) **Hemolysis** | Neutral | Pink | Blood destruction effect |
| ![](src/main/resources/assets/hemomancy/textures/mob_effect/fungal_elytra.png) **Noctifly Agaric** (Fungal Elytra) | Beneficial | — | Grants elytra flight ![](src/main/resources/assets/hemomancy/textures/models/armor/fungal_elytra.png) |
| ![](src/main/resources/assets/hemomancy/textures/mob_effect/sanguine_fertility.png) **Sanguine Fertility** | Beneficial | 0xCC3344 | Fertility/growth effect |
| **Arachnid Anastomosis** | Beneficial | 0x8B0000 | Spider-vein healing |
| **Mycorrhizal Mending** | Beneficial | 0x7B4F2A | Fungal regeneration |
| **Sanguine Siphon** | Beneficial | 0x8B0000 | Blood drain on hit |
| **Chitinous Bulwark** | Beneficial | 0x556B2F | +4 armor toughness |
| **Serpentine Guile** | Beneficial | 0x2E8B57 | +15% move speed, +10% attack speed |
| **Verminous Aura** | Beneficial | 0x4A3728 | Pest-based area effect |

---

## 22. World Generation & Biomes

### 22.1 Custom Biomes (via TerraBlender)

| Biome | Key | Temperature | Precipitation | Notes |
|-------|-----|-------------|---------------|-------|
| **Fungal Gardens** | `fungal_gardens` | 2.0 | None (Nether) | Hyphae tendrils, huge fungi |
| **Fungal Isles** | `fungal_isles` | 2.0 | None (Nether) | Hyphae, huge fungi, small infected fungi |
| **Sporecrown Thicket** | `sporecrown_thicket` | 1.2 | None | Dense fungal overgrowth, hostile spawns (Eruptus, Chthonian, Fargone), crimson particles, dark red fog |

Registered via 3 TerraBlender regions (`TestRegion1/2/3`) with custom surface rules.

> **Custom environment textures:**
>
> | | | | |
> |---|---|---|---|
> | ![](src/main/resources/assets/hemomancy/textures/environment/sun.png) Sun | ![](src/main/resources/assets/hemomancy/textures/environment/moon.png) Moon | ![](src/main/resources/assets/hemomancy/textures/environment/clouds.png) Clouds | ![](src/main/resources/assets/hemomancy/textures/environment/blood_moon_phases.png) Blood Moon Phases |

### 22.2 World Features

| Feature | Notes |
|---------|-------|
| Big Mushgloom | Large mushroom variant |
| Canopy Mushroom / Brown & Red Canopy | Tree-like mushroom features |
| Small Infected Mushroom | Small scattered fungi |
| Fungus Feature | Generic fungal feature |
| Hyphae Feature | Ground-level hyphae spread |
| Hyphae Tendril | Vertical tendril features |
| Bog Body Feature | Generates bog body blocks |

### 22.3 Configured/Placed Features

Managed via `ConfiguredFeatureInit` and `PlacedFeatureInit`:
- `HYPHAE_TENDRIL`, `HUGE_FUNGUS`, `SMALL_INFECTED_FUNGUS`
- `PLACED_INFESTED_VENOUS_STONE_BLOB`, `PLACED_MYCELIUM_BLOB`
- `PLACED_CANOPY_MUSHROOMS_DENSE`
- `PATCH_HYPHAE`, `BLEEDING_HEARTS`, `STINK_HORNS`

---

## 23. Structures

| Structure | Type | Notes |
|-----------|------|-------|
| **Blood Temple** | `BloodTempleStructure` | Contains the Mortal Display; gateway to hemomancy |
| **Unstained Church** | `UnstainedChurchStructure` | Contains the Unstained Podium; gateway to the Unstained path |

---

## 24. Villagers & Professions

| Profession | POI Block | Notes |
|-----------|-----------|-------|
| ![](src/main/resources/assets/hemomancy/textures/entity/villager/profession/hemopothecary.png) **Hemopothecary** | Scrying Podium | Blood-themed villager trader |

- Custom `HemopothecaryProcessor` for village structure integration
- `VillageEvents` for handling village spawning

---

## 25. Mod Compatibility

### 25.1 Mana and Artifice (MnA)

Full integration as a faction + spell system:

**Faction: The Harbingers**
- `HarbingersFaction` — custom faction with blood-red manaweave (RGB 160,0,40) ![Faction Icon](src/main/resources/assets/hemomancy/textures/mna/faction_icon_harbinger.png)
- Token item: ![](src/main/resources/assets/hemomancy/textures/item/mna/mark_of_blood.png) Mark of Blood
- Grimoire: ![](src/main/resources/assets/hemomancy/textures/item/mna/spellbook_h.png) Tome of the Impending End
- Faction Horn: ![](src/main/resources/assets/hemomancy/textures/item/mna/horn_harbinger.png) Horn of the Impending End
- Custom mana resource (`HarbingersMana`) ![Resource Bars](src/main/resources/assets/hemomancy/textures/mna/harbingers_resource_bars.png)

**Spell Components:**
- ![](src/main/resources/assets/hemomancy/textures/mna/blood_binding.png) `ComponentBloodBinding` — applies Blood Binding effect via spells
- ![](src/main/resources/assets/hemomancy/textures/mna/mana_to_blood.png) `ComponentManaToBlood` — converts MnA mana into Hemomancy blood volume (configurable magnitude, 50–200 mana per cast)
- ![](src/main/resources/assets/hemomancy/textures/mna/sanguine_fertility.png) `ComponentSanguineFertility` — applies Sanguine Fertility via spells

**Manipulations:**
- `SanguineTransfusionManip` — MnA-specific manipulation

**Runic Anvil Integration:**
- Living Infused Thread + Mage Armor → Living Thread armor set (Hood, Robes, Leggings, Boots)
- (3) Set Bonus: +500 Max Mana, +50% Mana Regen

**Additional MnA Items:**

| | | | |
|---|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/item/mna/foul_vinteum_ingot.png) Foul Vinteum Ingot | ![](src/main/resources/assets/hemomancy/textures/item/befouled_vinteum_dust.png) Befouled Vinteum Dust | ![](src/main/resources/assets/hemomancy/textures/item/mna/mana_infushed_memory_blank.png) Mana Infused Memory Blank | ![](src/main/resources/assets/hemomancy/textures/item/mna/living_infused_thread.png) Living Infused Thread |
| ![](src/main/resources/assets/hemomancy/textures/item/mna/living_thread_hood.png) Living Thread Hood | ![](src/main/resources/assets/hemomancy/textures/item/mna/living_thread_robes.png) Living Thread Robes | ![](src/main/resources/assets/hemomancy/textures/item/mna/living_thread_leggings.png) Living Thread Leggings | ![](src/main/resources/assets/hemomancy/textures/item/mna/living_thread_boots.png) Living Thread Boots |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_shot_occulus.png) Blood Shot Occulus | ![](src/main/resources/assets/hemomancy/textures/item/mna/mote_blood.png) Mote of Blood | ![](src/main/resources/assets/hemomancy/textures/item/mna/mana_memory_sanguine_transfusion.png) Mana Memory: Sanguine Transfusion | |

> Living Thread Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/living_thread_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/living_thread_layer_2.png)

**MnA Ritual:**
- Ritual of The Weeping Wound

**MnA Block/Tile/Entity:**
- Custom blocks, tiles, and entities in `compat/mna/block`, `compat/mna/tile`, `compat/mna/entity`

### 25.2 Curios

Curios integration for the Charm of Vascularium and other equippable items (handled in `compat/curios`).

### 25.3 JEI

JEI recipe category support for:
- Chisel Station recipes
- Visceral Recaller recipes
- Blood Structure Crafting recipes
- Morphling Incubator recipes

---

## 26. GUIs & Overlays

### 26.1 HUD Overlays

| Overlay | Location | Shows |
|---------|----------|-------|
| `BloodVolumeOverlay` | Left side | Current/max blood volume bar ![](src/main/resources/assets/hemomancy/textures/gui/blood_bar.png) |
| `UnstainedGaugeOverlay` | Top-right | Purity + Clarity bars ![](src/main/resources/assets/hemomancy/textures/gui/unstained_gauge.png) |
| `EquippedMorphlingOverlay` | — | Currently equipped morphling icon |
| `ManipCooldownOverlay` | — | Active manipulation cooldown timer |

> **Gauge fills:** ![](src/main/resources/assets/hemomancy/textures/gui/blood_fill_tiled.png) Blood fill &nbsp; ![](src/main/resources/assets/hemomancy/textures/gui/unstained_fill_tiled.png) Purity fill &nbsp; ![](src/main/resources/assets/hemomancy/textures/gui/unstained_clarity_fill_tiled.png) Clarity fill

### 26.2 Screens

**Key GUI Textures:**

| | | |
|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/gui/centrifuge_gui.png) Centrifuge | ![](src/main/resources/assets/hemomancy/textures/gui/juiceinator_gui.png) Juiceinator | ![](src/main/resources/assets/hemomancy/textures/gui/recaller_gui.png) Recaller |
| ![](src/main/resources/assets/hemomancy/textures/gui/chisel_station.png) Chisel Station | ![](src/main/resources/assets/hemomancy/textures/gui/rune_binder_gui.png) Rune Binder | ![](src/main/resources/assets/hemomancy/textures/gui/morphling_jar_gui.png) Morphling Jar |
| ![](src/main/resources/assets/hemomancy/textures/gui/tendency_view.png) Tendency View | ![](src/main/resources/assets/hemomancy/textures/gui/vascular_view.png) Vascular View | ![](src/main/resources/assets/hemomancy/textures/gui/fungal_implantation_pylon.png) Spore Implant |

| Screen | Opened From | Purpose |
|--------|------------|---------|
| `CharmGourdScreen` | Scrying Podium | Equip Charm of Vascularium + Blood Gourds |
| `SkillTreeScreen` | Dendritic Distributor | Skill tree + manipulation tree (pan/zoom, animated vein BG) |
| `TendencyViewScreen` | Blood Tendency Gauge | View blood tendency alignments |
| `VascularViewScreen` | Vascular Status Gauge | View vein section health |
| `VascularStatusScreen` | — | Detailed vascular status |
| `BloodlinePoolScreen` | Bloodline Pool Monitor | View/manage bloodline shared pool |
| `JuiceinatorScreen` | Juiceinator block | Juiceinator crafting GUI |
| `VialCentrifugeScreen` | Vial Centrifuge | Centrifuge crafting GUI |
| `MorphlingIncubatorScreen` | Morphling Incubator | Incubation crafting GUI |
| `SporeImplantScreen` | Fungal Implantation Pylon | Spore implantation GUI |
| `StructureSpawnerScreen` | Structure Spawner item | Debug structure spawning |
| Various radial menus | Living Staff / keybinds | Morphling/manipulation selection |
| Guide/Codex screens | Liber Sanguinum | In-game guidebook |

---

## 27. Advancements

| Advancement | Trigger |
|-------------|---------|
| **Strange Seeds** | Find Gourd Seeds from grass |
| **The First Awakening** | Activate a Blood Temple's Mortal Display |
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
| **Unstained** | Obtain Hemolytic Solution |
| **Bleeding a Stone** | Craft a Juiceinator |

---

## 28. Keybindings

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
| Toggle Rune Binder Pickup | Toggle rune pickup mode |

---

## 29. Commands

The `/hemomancy` command tree (via `HemoCommand`) provides:

**Blood Volume:**
- `get blood` — show current blood
- `set blood <amount>` — set blood volume
- `fill blood` — fill to max

**Initiatory Degree:**
- `get degree` — show current degree
- `set degree <number>` — set degree (0–7)

**Unstained Progress:**
- `get unstained` — full overview (purity, clarity, stages)
- `get purity` — show purity value + stage
- `set purity <value>` — set purity (0–100)
- `get clarity` — show clarity value + stage
- `set clarity <value>` — set clarity (0–100)
- `toggle clarity` — toggle clarity unlock
- `reset unstained` — reset all unstained progress to zero
- `max unstained` — max out all unstained progress

---

## 30. Known WIP / Incomplete Systems

- **Blood Fluid** (`FluidInit`) — Blood as a placeable fluid is entirely commented out / WIP
- **Progression Codex** (`HemoProgressionScreen`) — Book entry system exists but entries are commented out
- **Manipulation Rank Advancement** — Ritual-based forced rank upgrades described as WIP in lore
- **Unstained Zealot Capability Check** — Uses reflection to check for `UnstainedProgressProvider` (suggests it was added incrementally)
- **Some skill descriptions** exist in lang but the full implementation of effects like `skill_sanguine_surge`, `skill_crimson_mastery`, `skill_vital_link`, `skill_iron_will`, `skill_blood_flow` need verification for complete gameplay wiring
- **Loot Modifiers** (`AddItemModifier`) — framework exists, specific loot tables TBD

---

*This document should be kept up to date as development continues. Each section maps directly to the codebase structure under `com.vincenthuto.hemomancy`.*
