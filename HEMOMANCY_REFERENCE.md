# Hemomancy — Complete Mod Reference

> **Minecraft Version:** 1.20.1 (Forge)
> **Last Updated:** 2026-04-14

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
31. [Configuration](#31-configuration)
32. [Networking & Packets](#32-networking--packets)
33. [Sound Events](#33-sound-events)
34. [Particle Types](#34-particle-types)

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
| Visceral Organs | `IVisceralOrgans` | Tracks extracted/modified organs (Spleen, Liver, Lungs, Kidneys, Heart) for the Visceral Mirror ritual system |

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

The divergent/opposing path. The player abandons blood magic in pursuit of purification and enlightenment, guided by **Unstained Zealot** NPCs and the silent patronage of **Our Lady of Lethe**.

### 4.1 Our Lady of Lethe — Patron of the Unstained

The Unstained revere a mysterious figure known only as **Our Lady of Lethe** (sometimes whispered as *"The Lady of the Forgotten Waters"* or *"She Who Absolves"*). She is described in Unstained scripture as:

- A **tall woman** with **white hair** that cascades like flowing water
- Clad in **white robes** that shimmer faintly with silver thread
- Eyes of **liquid silver** that see through all deception and corruption
- Skin of **pale blue**, as though touched by the waters of the River Lethe itself

Her origins are unknown — some Unstained texts suggest she is a being from before the fungal infection that birthed hemomancy, a guardian spirit of purity who was driven into hiding when blood magic first took root. Others believe she is a manifestation of the world's immune response to the alien fungus, a living antibody in humanoid form.

**Connection to the Lethean Poppies:**
The **Lethean Poppies** that grow across the world are said to bloom wherever Our Lady once walked. The dew they produce — **Lethean Dew** — carries her essence: the power of forgetting. Just as the mythological River Lethe washed away memories, the lethean poppies help players *forget* their blood manipulations, severing the ties that bind them to hemomancy. The Unstained believe that harvesting and refining these poppies is a sacred act of devotion to their patron.

**Tears of Lethe** are distilled from Lethean Dew at an Altar of Cleansing, concentrating Our Lady's blessing into a single potent draught. When offered at her altar, these tears trigger a powerful purification — a one-time gift from the Lady herself.

**The Lethe Icon** is an exceedingly rare relic depicting Our Lady, said to have been carved by the first Unstained from pale silver found at the bottom of a forgotten river. Those who possess it are considered to be under her direct protection.

### 4.2 Entry Requirements

- Player must have reached at least **Degree 2 (Votary)** before an Unstained Zealot will offer the choice
- The Zealot directs the player to bring **Hemolytic Solution** ![Hemolytic Solution](src/main/resources/assets/hemomancy/textures/item/hemolytic_solution.png) to an **Unstained Podium** block

### 4.3 Phase 1: Purity (0–100)

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
| **Tears of Lethe on Altar of Cleansing** | +25.0 | One-time blessing from Our Lady of Lethe |
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

### 4.4 Phase 2: Clarity (0–100)

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

### 4.5 HUD

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

| Name | Cost | Type | Rank | Tendency | Vein Section | Cooldown | Description |
|------|------|------|------|----------|-------------|----------|-------------|
| `venous_travel` | 1000 | Continuous | Mediocritas | Ferric | Right Arm | 20t | Teleport to saved Earthen Vein locations (vein network fast travel) |
| `blood_shot` | 100 | Quick | Humilis | Animus | Head | 10t | Fires a single tracking blood shot projectile in the look direction |
| `deadly_gaze` | 100 | Quick | Humilis | Animus | Head | 20t | Raycasts 100 blocks; launches the targeted entity upward with blood claw FX |
| `blood_needle` | 100 | Quick | Humilis | Animus | Head | 10t | Fires a spread of 10–20 blood needle projectiles with random scatter |
| `blood_rush` | 100 | Passive | Humilis | Animus | Body | 60t | Summons a Wretched Will and grants Blood Rush effect (+20% move/attack speed) |
| `blood_cloud` | 300 | Quick | Summa | Animus | Head | 40t | Launches a Blood Cloud Carrier projectile that deploys an AoE blood cloud |
| `blood_aneurysm` | 400 | Quick | Summa | Animus | Body | 40t | AoE blood particle burst around the caster (visual/damage) |
| `activation_potential` | 200 | Quick | Mediocritas | Ductilis | Body | 30t | AoE lightning bolt to all entities within 5 blocks, dealing 5 damage each |
| `sanguine_ward` | 10 | Continuous | Mediocritas | Ductilis | Body | 20t | Passive damage reduction shield (logic handled in ManipEvents on hurt) |
| `ferric_transmutation` | 1000 | Quick | Summa | Ferric | Body | 20t | Converts a held Gold Ingot into an Iron Ingot |
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
| `sanguine_mending` | 150 | Quick | Humilis | Ferric | Right Arm | 30t | Repairs the held item by 50 durability using blood |
| `vital_reservoir` | 50 | Quick | Mediocritas | Mortem | Heart | 60t | Converts 10 XP levels into 1000 blood volume |
| `hemorrhage` | 100 | Quick | Humilis | Mortem | Right Arm | 20t | Targets the closest living entity within 8 blocks and applies Wither II (6s) |
| `exsanguinate` | 300 | Quick | Mediocritas | Mortem | Right Arm | 50t | Executes a weakened target (≤30% HP) within 10 blocks: deals 1.5× their current HP as damage and restores 600 blood to the caster |
| `void_shroud` | 100 | Quick | Humilis | Tenebris | Body | 20t | Grants the caster Invisibility for 5 seconds |
| `blood_eclipse` | 300 | Quick | Mediocritas | Tenebris | Head | 45t | Forward cone attack (18 blocks, 30° half-angle): applies Blindness II (5s) + Weakness I (6s) + 0.5 heart shadow damage |
| `sanguine_excavation` | 400 | Quick | Mediocritas | Ferric | Right Arm | 40t | Flood-fill mines a cluster of matching blocks at look target (base 9 blocks, scales with Sanguine Reach) |
| `umbral_step` | 300 | Quick | Mediocritas | Tenebris | Left Leg | 40t | Teleports to the targeted block (range 24, scales with Sanguine Reach) — destination must be dark (light ≤ 7) |

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

Opened from the **Dendritic Distributor** block. Has six tabs:
- **Skills** — panning/zoomable blood skill tree with skill nodes
- **Manipulations** — panning/zoomable manipulation tree with manipulation nodes
- **Crafting** — sidebar listing blood structure recipes grouped by tier (Basic/Advanced/Expert) with degree gating (0/2/4)
- **Runes** — sidebar listing chisel station recipes grouped by tier (1/2/3) with degree gating (4/4/5)
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
| Rune Affinity | 15 | 3 | 3 | 4 | Enhances attunement strength when runes are equipped | Crimson Mastery |
| Rune Resonance | 16 | 3 | 3 | 4 | Amplifies passive effects of equipped runes | Rune Affinity |
| Rune Mastery | 17 | 3 | 4 | 5 | Maximizes rune power and synergy | Rune Resonance |

Skill bonuses are computed in `SkillPointHelper`.

**Skill Wiring Status** (which skills are actually hooked into gameplay events):

| Skill | Wired? | Where Called |
|-------|--------|-------------|
| Capacity | ✅ Yes | `BloodVolumeEvents` — adds flat bonus to max blood volume each tick |
| Efficiency | ✅ Yes | `BloodManipulation.performAction()` — multiplies manipulation blood cost |
| Manip Slots | ✅ Yes | `KnownManipulationEvents` — expands active manipulation slot count |
| Last Wind | ✅ Yes | `BloodVolumeEvents` — passive blood regen when below 10% threshold |
| Dynamic Use | ⚠️ Helper only | `SkillPointHelper.getDynamicUseMultiplier()` exists but no event caller found |
| Feeding Frenzy | ✅ Yes | `BloodVolumeEvents` — multiplies blood gained from kills |
| Hemostasis | ⚠️ Helper only | `SkillPointHelper.getHemostasisMultiplier()` exists but no event caller found |
| Sanguine Surge | ⚠️ Helper only | `SkillPointHelper.getSanguineSurgeRegen()` exists but no event caller found |
| Crimson Mastery | ✅ Yes | `PyreticForgeManip` — scales items smelted per cast |
| Vital Link | ⚠️ Helper only | `SkillPointHelper.getVitalLinkChance()` exists but no event caller found |
| Iron Will | ⚠️ Helper only | `SkillPointHelper.getIronWillReduction()` exists but no event caller found |
| Blood Flow | ⚠️ Helper only | `SkillPointHelper.getBloodFlowMultiplier()` exists but no event caller found |
| Coagulation | ⚠️ Helper only | `SkillPointHelper.getCoagulationChance()` exists but no event caller found |
| Sanguine Reach | ✅ Yes | `BloodLampManip`, `CrimsonFlameConjurationManip`, `UmbralStepManip`, `SanguineExcavationManip` — scales range |
| Rune Affinity | ⚠️ Helper only | Helper exists; rune deepening amount scaling not yet wired |
| Rune Resonance | ⚠️ Helper only | Helper exists; rune synergy effects not yet wired |
| Rune Mastery | ⚠️ Helper only | Helper exists; rune power maximization not yet wired |

> **Note:** Skills marked "⚠️ Helper only" have their bonus calculations fully implemented in `SkillPointHelper` but need to be wired into the relevant event handlers (damage events, regen ticks, cooldown calculations, etc.) to have actual gameplay effects.

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

| Morphling | Item Class | Preferred Tendency | Effect / Notes |
|-----------|-----------|-------------------|----------------|
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_fungal.png) Fungal | `FungalMorphlingItem` | Animus | Mycorrhizal Mending (passive fungal regeneration). Base morphling type |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_leeches.png) Leeches | `LeechesMorphlingItem` | Animus | Sanguine Siphon (blood drain on hit). Summons leeches to fight and drain blood |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_chitinite.png) Chitinite | `ChitiniteMorphlingItem` | Ferric | Chitinous Bulwark (+4 armor toughness). Chitin shield / defense |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_serpent.png) Serpent | `SerpentMorphlingItem` | Mortem | Serpentine Guile (+15% move speed, +10% attack speed). Paralyzing serpent projectile |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_pests.png) Pests | `PestsMorphlingItem` | Mortem | Verminous Aura (pest-based AoE effect). Swarm of pest projectiles |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_spider.png) Spider | `SpiderMorphlingItem` | Tenebris | Arachnid Anastomosis (spider-vein healing). Arachnid-themed attacks |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_bat.png) Bat | `BatMorphlingItem` | Tenebris | Echoic Perception (nearby entities glow, radius scales with amplifier). Maturity: Sonar Shriek → Membrane Glide → Nightwing Frenzy |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_moth.png) Moth | `MothMorphlingItem` | Lux | Luminous Dissipation (knockback resistance). Maturity: Dustwing Trail → Phototaxis Pulse → Cocoon Rebirth |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_tick.png) Tick | `TickMorphlingItem` | Mortem | Hemorrhagic Venom (AoE damage aura to nearby hostiles). Maturity: Engorge → Blood Fever → Pandemic Burst |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_urchin.png) Urchin | `UrchinMorphlingItem` | Ferric | Spined Barricade (passive thorns + armor bonus). Maturity: Spine Lash → Tidal Anchor → Calcareous Shell |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_centipede.png) Centipede | `CentipedeMorphlingItem` | Congeatio | Venomous Resilience (poison immunity + speed boost). Maturity: Burrowing Strike → Segmented Defense → Myriapod Swarm |
| ![](src/main/resources/assets/hemomancy/textures/item/morphling_mole.png) Mole | `MoleMorphlingItem` | Ferric | Burrower's Instinct (mining speed + underground regen/night vision). Maturity: Burrow Sense → Earthen Bulwark → Seismic Slam |

> **Note:** The older morphlings (Fungal, Leeches, Chitinite, Serpent, Pests, Spider) apply their signature status effect as a passive but do **not yet have named maturity-tier reactive abilities** like the newer morphlings (Bat, Moth, Tick, Urchin, Centipede, Mole). Adding maturity ability names and implementations for these 6 original morphlings is a planned task.

### 11.2 Cultivation

- Start with a **Morphling Polyp** ![Morphling Polyp](src/main/resources/assets/hemomancy/textures/item/morphling_polyp.png) (base form)
- Incubate in a **Morphling Incubator** block with enzymes to grow into specific morphling types
- Store morphlings in a **Morphling Jar** ![Morphling Jar](src/main/resources/assets/hemomancy/textures/item/morphling_jar.png) (6 slots, Uncommon rarity) — they bounce around inside
- The **Living Staff** cycles through equipped morphlings and changes its topper model accordingly

### 11.3 Maturity System

Each morphling has a **maturity level** (1–4) that determines its power and which reactive abilities it has:

| Maturity | Name | Description |
|----------|------|-------------|
| 1 | Nascent | Base form — passive effect only (the morphling's signature status effect) |
| 2 | Developing | First reactive ability unlocked (typically a triggered defensive response) |
| 3 | Mature | Second reactive ability unlocked (more powerful utility/combat mechanic) |
| 4 | Apex | Third reactive ability unlocked (powerful signature ability with longer cooldown) |

Each morphling type has a **preferred tendency** and **secondary tendency** — feeding the corresponding enzymes during incubation accelerates maturity. The passive effect's amplifier scales with maturity level.

---

## 12. Runes & Spores

### 12.1 Runes

Runes are equippable items stored in a **Rune Binder** ![Rune Binder](src/main/resources/assets/hemomancy/textures/item/rune_binder.png) (18 slots) or **Upgraded Rune Binder** ![Rune Binder Upgraded](src/main/resources/assets/hemomancy/textures/item/rune_binder_upgraded.png) (27 slots). They are crafted at the **Runic Chisel Station**. Rune crafting requires **Initiatory Degree 4 (Adept)** minimum.

Runes are organized in **three tiers** by `deepenAmount` — how strongly they shift tendency alignment per equipped slot:

**Tier 1 Runes (deepenAmount = 1) — Basic, available at Degree 4:**

| Rune | Tendency | Effect |
|------|----------|--------|
| ![](src/main/resources/assets/hemomancy/textures/item/mind_spike.png) Mind Spike | Ductilis | Deepens Ductilis tendency alignment when equipped |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_transcendence.png) Rune Transcendence | Lux | Deepens Lux tendency alignment when equipped |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_sol.png) Rune Sol | Flammeus | Deepens Flammeus tendency alignment when equipped |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_heart.png) Rune Heart | Animus | Deepens Animus tendency alignment when equipped |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_descendence.png) Rune Descendence | Mortem | Deepens Mortem tendency alignment when equipped |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_moon.png) Rune Moon | Congeatio | Deepens Congeatio tendency alignment when equipped |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_eye.png) Rune Eye | Ductilis | Deepens Ductilis tendency alignment when equipped |
| ![](src/main/resources/assets/hemomancy/textures/item/rune_feral.png) Rune Feral | Ductilis | Deepens Ductilis tendency alignment when equipped |
| Rune Thorn | Ferric | Deepens Ferric tendency alignment when equipped |
| Rune Shade | Tenebris | Deepens Tenebris tendency alignment when equipped |

**Tier 2 Runes (deepenAmount = 2) — Advanced, available at Degree 4:**

| Rune | Tendency |
|------|----------|
| Rune Pyre | Flammeus |
| Rune Marrow | Animus |
| Rune Blight | Mortem |
| Rune Rime | Congeatio |
| Rune Flux | Ductilis |
| Rune Halo | Lux |
| Rune Anvil | Ferric |
| Rune Veil | Tenebris |

**Tier 3 Runes (deepenAmount = 3) — Expert, available at Degree 5:**

| Rune | Tendency |
|------|----------|
| Rune Phoenix | Flammeus |
| Rune Ichor | Animus |
| Rune Wither | Mortem |
| Rune Glacier | Congeatio |
| Rune Chimera | Ductilis |
| Rune Corona | Lux |
| Rune Crucible | Ferric |
| Rune Oblivion | Tenebris |

> **Rune Mechanic:** All standard runes extend `ItemRune` and share the same core mechanic: when equipped in a Rune Binder slot, they deepen the player's Blood Tendency alignment toward their assigned tendency by a fixed amount (set per rune via `deepenAmount`). This shifts which manipulations the player has strongest affinity with. Individual gameplay bonuses beyond tendency alignment are not yet implemented for standard runes.

Each rune has a corresponding **Rune Pattern** item used in crafting.

### 12.2 Functional Spores (Rune-type items)

Special fungal rune items with active effects (extend `ItemFungalRune`):

| Item | Tendency | Effect |
|------|----------|--------|
| ![](src/main/resources/assets/hemomancy/textures/item/respergillus.png) Respergillus | Animus | Fungal spore with custom 3D render when equipped (specific effect TBD) |
| ![](src/main/resources/assets/hemomancy/textures/item/talaromyces_minus.png) Talaromyces Minus | Ferric | Grants Haste (Dig Speed) effect while equipped in Rune Binder |
| ![](src/main/resources/assets/hemomancy/textures/item/lumina_devorans.png) Lumina Devorans | Tenebris | Fungal spore with custom 3D render when equipped (specific effect TBD) |
| ![](src/main/resources/assets/hemomancy/textures/item/noctifly_agaric.png) Noctifly Agaric | Animus | Grants the Noctifly Agaric (Fungal Elytra) flight effect |

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
| ![](src/main/resources/assets/hemomancy/textures/item/sanguine_quintessence.png) Stabilized Sanguine Formation | 5,000 |
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
| ![](src/main/resources/assets/hemomancy/textures/item/lethean_dew.png) Lethean Dew | Memory processing ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/lethean_brew.png) Lethean Brew | Cursed clay jar from the River Lethe — enables forgetting memories |
| ![](src/main/resources/assets/hemomancy/textures/item/fervent_husk.png) Fervent Husk | Memory processing ingredient |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_stained_stone.png) Blood Stained Stone | Memory-related item |
| Blood Memory (per manipulation) | One for each registered manipulation — using it teaches the player |

**Memory Textures Gallery:**

> **Note:** Memory items use a 2-layer model system — the base `hematic_memory.png` is overlaid with a unique per-manipulation overlay from `textures/item/memories/memory_*_overlay.png`.

| | | | |
|---|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_shot_overlay.png) Blood Shot | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_deadly_gaze_overlay.png) Deadly Gaze | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_needle_overlay.png) Blood Needle | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_rush_overlay.png) Blood Rush |
| ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_cloud_overlay.png) Blood Cloud | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_aneurysm_overlay.png) Blood Aneurysm | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_activation_potential_overlay.png) Activation Potential | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_sanguine_ward_overlay.png) Sanguine Ward |
| ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_venous_travel_overlay.png) Venous Travel | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_ferric_transmutation_overlay.png) Ferric Transmutation | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_living_blade_overlay.png) Living Blade | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_absorption_overlay.png) Blood Absorption |
| ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_projection_overlay.png) Blood Projection | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_summon_avatar_overlay.png) Summon Avatar | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_crimson_flame_conjuration_overlay.png) Crimson Flame Conjuration | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_lamp_overlay.png) Blood Lamp |
| ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_crimson_sight_overlay.png) Crimson Sight | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_crimson_harvest_overlay.png) Crimson Harvest | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_hemosynthesis_overlay.png) Hemosynthesis | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_pyretic_forge_overlay.png) Pyretic Forge |
| ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_glacial_grasp_overlay.png) Glacial Grasp | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_sanguine_mending_overlay.png) Sanguine Mending | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_vital_reservoir_overlay.png) Vital Reservoir | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_sanguine_excavation_overlay.png) Sanguine Excavation |
| ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_umbral_step_overlay.png) Umbral Step | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_summon_thrall_overlay.png) Summon Thrall | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_cryogenic_pulse_overlay.png) Cryogenic Pulse | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_glacial_bastion_overlay.png) Glacial Bastion |
| ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_sanguine_ignition_overlay.png) Sanguine Ignition | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_vitric_combustion_overlay.png) Vitric Combustion | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_void_shroud_overlay.png) Void Shroud | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_blood_eclipse_overlay.png) Blood Eclipse |
| ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_hemorrhage_overlay.png) Hemorrhage | ![](src/main/resources/assets/hemomancy/textures/item/memories/memory_exsanguinate_overlay.png) Exsanguinate | | |

> **Memory Overlay System:** All memory items now use a layered model system — each memory has a unique overlay texture (`textures/item/memories/memory_*_overlay.png`) composited on top of the base Hematic Memory texture. This provides visual distinction for each manipulation's memory item without requiring fully separate textures.

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
| ![](src/main/resources/assets/hemomancy/textures/item/debug_showcase_spawner.png) Debug Showcase | Creative-mode debug item (`DebugShowcaseItem`) — right-click to spawn a complete showcase area containing every Hemomancy feature organized into 4 sections: (1) All items in labeled chests, (2) All blocks placed on platforms, (3) All mob entities in fenced pens, (4) All blood structures and cardinal rites as placed patterns. |

### 13.6 Unstained Materials (Our Lady of Lethe)

| Item | Purpose |
|------|---------|
| Tears of Lethe | Distilled from Lethean Dew — used at the Altar of Cleansing for a one-time purity boost (+25) |
| ![](src/main/resources/assets/hemomancy/textures/item/lethean_poppy_wreath.png) Lethean Poppy Wreath | Woven from Lethean Poppies — repeatable altar offering (+5 purity) |
| ![](src/main/resources/assets/hemomancy/textures/item/silver_chalice.png) Silver Chalice | A ritual vessel of the Unstained — offered at the Altar of Cleansing for clarity (+5) |
| Tome of the Unstained | A book of Unstained scripture describing Our Lady of Lethe and the path of purification |
| Icon of Our Lady | A rare relic depicting Our Lady of Lethe — carved from pale silver, grants her protection |
| ![](src/main/resources/assets/hemomancy/textures/item/pale_silver_ingot.png) Pale Silver Ingot | A refined metal sacred to the Unstained, used in crafting Unstained equipment |
| Lethean Extract | Concentrated essence from Lethean Poppies, a crafting ingredient for Unstained recipes |

### 13.7 Food Items

| Item | Purpose |
|------|---------|
| ![](src/main/resources/assets/hemomancy/textures/item/gourd_slice.png) Gourd Slice | Edible gourd food item |
| ![](src/main/resources/assets/hemomancy/textures/item/gourd_stew.png) Gourd Stew | Stew crafted from gourd and other ingredients |
| Roasted Gourd Seeds | Smelted/smoked/campfire-cooked gourd seeds (3 cooking methods) |

### 13.8 Organ Echo Items

Produced by the **Visceral Mirror** ritual (requires Degree 3+). Spectral imprints of the player's organs — bound to the player (dissolve if placed in non-player inventory), only one per organ type can exist at a time. Organ "Tier" indicates risk level and degree requirement for extraction:

| Item | Organ | Tier | Notes |
|------|-------|------|-------|
| Echo of Spleen | `SPLEEN` | 3 | Governs blood volume and filtration |
| Echo of Liver | `LIVER` | 3 | Metabolizes toxins and purifies the blood |
| Echo of Lungs | `LUNGS` | 3 | Oxygenates blood and sustains vital rhythm |
| Echo of Kidneys | `KIDNEYS` | 3 | Filters impurities and maintains humoral balance |
| Echo of Heart | `HEART` | 4 | The seat of circulation and will — highest risk, requires Degree 4+ |

> **⚠️ WIP:** The organ extraction ritual flow is fully implemented (Visceral Mirror → cycle organs → confirm extraction → produce Echo items). However, **organ modification tiers and gameplay effects for each extracted organ are still being designed**. Currently, Organ Echoes are produced and player-bound but do not grant bonuses. Planned effects include: Spleen → blood capacity bonus, Liver → poison/potion resistance, Lungs → underwater breathing/stamina, Kidneys → faster blood regeneration, Heart → manipulation power boost with high risk.

### 13.9 Banner Patterns

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

---

## 15. Armor Sets

### 15.1 Hematic Iron Armor

Standard blood-infused iron armor set (fire resistant):
- ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_helm.png) Helm, ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_chestplate.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_leggings.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/hematic_iron_boots.png) Boots
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Hematic Iron Scrap
- **Set Bonus (4 pieces):** Passive blood regeneration — +2 blood/second while wearing full set

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/hematic_iron_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/hematic_iron_layer_2.png)

### 15.2 Blood Lust Armor

Special armor with mask variants:
- ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_helm.png) Helm (no mask), ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_helm_tengu.png) Helm (Tengu mask), ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_helm_horned.png) Helm (Horned mask)
- ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_chest.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_legs.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/blood_lust_boots.png) Boots
- Mask items: ![](src/main/resources/assets/hemomancy/textures/item/tengu_mask.png) Tengu Mask, ![](src/main/resources/assets/hemomancy/textures/item/horned_mask.png) Horned Mask (crafting ingredients)
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Hematic Iron Scrap
- **Set Bonus (4 pieces):** Lifesteal — 10% of direct melee damage dealt heals the player

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/blood_lust_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/blood_lust_layer_2.png)

### 15.3 Barbed Armor

Defensive barbed armor set:
- ![](src/main/resources/assets/hemomancy/textures/item/barbed_helm.png) Helm, ![](src/main/resources/assets/hemomancy/textures/item/barbed_chestplate.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/barbed_leggings.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/barbed_boots.png) Boots
- Barbed Shield ![](src/main/resources/assets/hemomancy/textures/entity/barbed_shield/model_barbed_shield.png)
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Chitinous Husk
- **Set Bonus (4 pieces):** Thorns — attackers take 2 damage and receive Blood Loss effect (3 seconds)

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/barbed_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/barbed_layer_2.png)

### 15.4 Chitinite Armor

Insectoid/chitin-based armor:
- ![](src/main/resources/assets/hemomancy/textures/item/chitinite_helm.png) Helm, ![](src/main/resources/assets/hemomancy/textures/item/chitinite_chestplate.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/chitinite_leggings.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/chitinite_boots.png) Boots
- Chitinite Shield ![](src/main/resources/assets/hemomancy/textures/entity/chitinite_shield/model_chitinite_shield.png)
- Chitinite Arm Banner (dyeable, 16 colors)
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Chitinous Husk
- **Set Bonus (4 pieces):** +2.0 Armor Toughness (via attribute modifier) and 25% projectile damage reduction

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/chitinite_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/chitinite_layer_2.png)

### 15.5 Unstained Armor

Anti-blood zealot armor (for the Unstained path):
- ![](src/main/resources/assets/hemomancy/textures/item/unstained_helm.png) Helm, ![](src/main/resources/assets/hemomancy/textures/item/unstained_chestplate.png) Chestplate, ![](src/main/resources/assets/hemomancy/textures/item/unstained_leggings.png) Leggings, ![](src/main/resources/assets/hemomancy/textures/item/unstained_boots.png) Boots
- **Stats:** Defense 3/6/8/3 (20 total), Toughness 3.0, KB Resist 0.1, Durability ×37, Enchantability 15
- **Repair:** Chitinous Husk (placeholder — should be Pale Silver Ingot or Consecrated Copper)
- **Set Bonus (4 pieces):** Immunity to Blood Loss and Hemolysis effects (auto-removed on tick)

> Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/unstained_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/unstained_layer_2.png)

### 15.6 Crown of Sacred Marrow

Special artifact helmet (`MarrowCrownArmorItem`), uses `MARROW_CROWN` tier.
- **Stats:** Same as Hematic Iron (Defense 3/6/8/3, Toughness 3.0, KB Resist 0.1)
- **Repair:** Hematic Iron Scrap
- **Artifact Bonus:** +10% melee damage (via attribute modifier) when blood volume is above 50%

> **Note:** All armor sets share identical base stat distributions (equivalent to Netherite-tier defense/toughness) but each has a unique set bonus implemented in `ArmorSetBonusHandler`. The Marrow Crown is an artifact helmet with its own standalone bonus that doesn't require a full set.

---

## 16. Functional Blocks & Tile Entities

| Block | Tile Entity | Purpose |
|-------|-------------|---------|
| **Mortal Display** | `MortalDisplayBlockEntity` | Activates blood magic when clicked in a Blood Temple ![](src/main/resources/assets/hemomancy/textures/entity/model_floating_heart.png) |
| **Scrying Podium** | `ScryingPodiumBlockEntity` | Opens the Charm/Gourd slot screen for equipping the Charm of Vascularium and Blood Gourds |
| **Visceral Artificial Recaller** | `VisceralRecallerBlockEntity` | Crafting station for creating Hematic Memories using enzymes, blank memories, and catalysts |
| **Vial Centrifuge** | `VialCentrifugeBlockEntity` | Spins down Bloody Vials into enzymes and Hematic Iron Powder. Reworked with new 3D stand model (`CentrifugeStandModel`), custom block entity renderer (`VialCentrifugeRenderer`), and `VialCentrifugeBlockItem` with custom item renderer. ![](src/main/resources/assets/hemomancy/textures/entity/model_centrifuge_stand.png) ![](src/main/resources/assets/hemomancy/textures/entity/model_centrifuge_arms.png) |
| **ghastly_alembic** | `GhastlyAlembicBlockEntity` | Squeezes items to extract blood (requires fire below). Has 4 slots: Input (slot 0), Flask (slot 1, fills Cured Clay Flasks into Bloody Flasks), Result (slot 2), and **Catalyst (slot 3)** — an optional catalyst ingredient that modifies or enhances the recipe output. Hopper access: top → input, bottom → result, sides → flask + catalyst. Renders via custom `GhastlyAlembicRenderer` (3D entity model `GhastlyAlembicModel`, facing-aware). |
| **Runic Chisel Station** | `ChiselStationBlockEntity` | Crafts runes from patterns and blanks |
| **Morphling Incubator** | `MorphlingIncubatorBlockEntity` | Grows Morphling Polyps into specific morphling types with enzymes. Has 7 slots: Center/polyp (slot 0), 4 enzyme/catalyst slots (1–4), Output (slot 5), Blood Flask/Gourd (slot 6). Craft time: 200 ticks base; enzyme feeding: 100 + 60 per item. Blood cost: 0.5/tick. Uses `IncubatorRecipe` system with 13 recipes (one per morphling type). JEI-integrated. Renders via custom `MorphlingIncubatorRenderer` (3D entity model). |
| **Fungal Podium** | `FungalPodiumBlockEntity` | Fungal-related interaction station |
| **Fungal Implantation Pylon** | `FungalImplantationPylonBlockEntity` | Sporic implantation station ![](src/main/resources/assets/hemomancy/textures/entity/fungal_implantation_pylon/fungal_implantation_pylon.png) |
| **Dendritic Distributor** | `DendriticDistributorBlockEntity` | Opens the Skill Tree / Manipulation Tree screen |
| **Unstained Podium** | `UnstainedPodiumBlockEntity` | Where Hemolytic Solution / Consecrated Copper are used for the Unstained path |
| **Altar of Cleansing** | `AltarOfCleansingBlockEntity` | Sacred altar of Our Lady of Lethe — grants one-time purity boost with Tears of Lethe; accepts Lethean Poppy Wreaths and Silver Chalices for repeatable offerings |
| **Semi-Sentient Construct** | `SemiSentientConstructBlockEntity` | Blood construct-related block |
| **Earthen Vein** | `EarthenVeinBlockEntity` | Vein location marker for teleportation (Venous Travel) ![](src/main/resources/assets/hemomancy/textures/entity/earthen_vein/model_earthen_vein.png) |
| **Iron Brazier** | `IronBrazierBlockEntity` | Decorative/functional brazier |
| **Suspended Blood Crystal** | `SuspendedBloodCrystalBlockEntity` | Floating blood crystal display ![](src/main/resources/assets/hemomancy/textures/entity/model_suspended_blood_crystal.png) |
| **Suspended Cleansed Blood Crystal** | `SuspendedCleansedBloodCrystalBlockEntity` | Floating cleansed blood crystal display (purified variant with random time offset animation) ![](src/main/resources/assets/hemomancy/textures/entity/model_suspended_cleansed_blood_crystal.png) |
| **Suspended Vivianite** | `SuspendedVivianiteBlockEntity` | Floating vivianite display ![](src/main/resources/assets/hemomancy/textures/entity/model_suspended_vivianite.png) |
| **Mnemonic Reliquary** | `MnemonicReliquaryBlockEntity` | Animated decorative/lore reliquary with opening/closing lid. Tracks open count, syncs lid angle (lerped). Has custom 3D block entity renderer and item renderer. Opened via `MnemonicReliquaryMenu`. Currently atmospheric/decorative — no inventory slots or crafting function yet. Planned: may serve as memory storage or manipulation bookmark container. ![](src/main/resources/assets/hemomancy/textures/entity/model_mnemonic_reliquary.png) |
| **Humane Idol** | `HumaneIdolBlockEntity` | Idol block |
| **Serpentine Idol** | `SerpentineIdolBlockEntity` | Idol block |
| **Engram Block** | — | Translucent engram |
| **Filler Block** | `FillerBlockEntity` | Indestructible filler for multiblocks |
| **Bog Body** | — | Decorative translucent body block |
| **Visceral Mirror** | `VisceralMirrorBlockEntity` | Ritualistic mirror for organ extraction — gaze into your reflection to extract and modify organs (Spleen, Liver, Lungs, Kidneys, Heart). Requires degree 3+. Cycle organs (right-click) → confirm extraction (sneak right-click). Produces Organ Echo items. |

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

### 17.3 Anti-Blood / Unstained

- ![](src/main/resources/assets/hemomancy/textures/block/hemolytic_plating_block.png) Hemolytic Plating Block
- Cleansed Stone — pale, smooth stone found in Unstained temples
- Lethe Lantern — softly glowing lantern sacred to Our Lady of Lethe

### 17.4 Glass & Panes

| | | | |
|---|---|---|---|
| ![](src/main/resources/assets/hemomancy/textures/block/sanguine_glass.png) Sanguine Glass | ![](src/main/resources/assets/hemomancy/textures/block/sanguine_pane.png) Sanguine Pane | ![](src/main/resources/assets/hemomancy/textures/block/vivianite_glass.png) Vivianite Glass | ![](src/main/resources/assets/hemomancy/textures/block/vivianite_pane.png) Vivianite Pane |
| ![](src/main/resources/assets/hemomancy/textures/block/cleansed_sanguine_glass.png) Cleansed Sanguine Glass | ![](src/main/resources/assets/hemomancy/textures/block/cleansed_sanguine_pane.png) Cleansed Sanguine Pane | | |

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
- Ghost Pipe (myco-heterotrophic, Night Vision effect, **Unstained** — ghastly_alembic → Lethean Extract)
- Sarcodes (myco-heterotrophic, Regeneration effect — ghastly_alembic → Dicentra Sap, brews Potion of Blood Rush)
- Rafflesia (parasitic, Confusion effect — ghastly_alembic → Spore Sac, brews Potion of Hemolysis)

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
| `ghastly_alembic_recipe_type` | `GhastlyAlembicSerializer` | Ghastly Alembic | Squeezes items for blood/extracts. Has 4 slots: input, flask (auto-fills Cured Clay Flasks), result, and catalyst (modifies output) |
| `recaller_recipe_type` | `RecallerRecipeSerializer` | Visceral Recaller | Creating Hematic Memories |
| `incubator_recipe_type` | `IncubatorRecipeSerializer` | Morphling Incubator | Growing Morphling Polyps into specific morphlings using enzyme catalysts. 13 recipes for all morphling types (bat, centipede, chitinite, fungal, leeches, mole, moth, pests, serpent, spider, tick, urchin). JEI-integrated via `IncubatorRecipeCategory`. |
| `blood_structure_recipe` | `BloodStructureRecipeSerializer` | In-world structure | Structure crafting (hit structure with catalyst + blood) |
| `cardinal_rite_recipe` | `CardinalRiteRecipeSerializer` | Multiblock | Cardinal Rites for degree advancement |
| Morphling Jar Upgrade | `CopyMorphlingJarRecipe.Serializer` | Crafting | Upgrading morphling jars |
| Blood Gourd Upgrade | `CopyBloodGourdRecipe.Serializer` | Crafting | Upgrading blood gourds |
| Blood Gourd Fill | `FillBloodGourdRecipe.Serializer` | Crafting | Filling gourds with blood |

### 18.1 Blood Structure Crafting

An in-world system: build a specific block structure, then hit a particular block with a catalyst item while spending blood. The structure transforms into the desired output.

| Recipe | Blood Cost | Held Item | Hit Block | Result |
|--------|-----------|-----------|-----------|--------|
| Liber Sanguinum | 100 | Sanguine Formation | Bookshelf | Liber Sanguinum |
| Hematic Iron Block | *(see JSON)* | *(see JSON)* | Iron Block | Hematic Iron Block |
| Living Staff | 150 | Sanguine Formation | Iron Bars | Living Staff |
| Morphling Incubator | 200 | Morphling Polyp | Hematic Iron Block | Morphling Incubator |
| Semi-Sentient Construct | 250 | Befouling Ash | Conscious Mass | Semi-Sentient Construct |
| Unstained Podium | 50 | Glowstone Dust | Hematic Iron Block | Unstained Podium |

> Recipes are in `data/hemomancy/recipes/blood_structure/`. Each recipe defines a multiblock `pattern` with `key` mapping characters to blocks, plus `heldItem`, `hitBlock`, `bloodCost`, and `result`.

### 18.2 Cardinal Rite Recipes

Specific cardinal rite recipes include degree advancement rites (section 3.2) plus utility rites. Rites come in four tiers based on complexity and cost:

**Degree Advancement Rites:**

| Rite | Blood Cost | Tier | Degree → | Description |
|------|-----------|------|----------|-------------|
| Sanguine Initiation | 100 | Minor | 0 → 1 | Basic initiation awakening hematic potential |
| Rite of the Votary | 250 | Minor | 1 → 2 | Binds the practitioner deeper into the Covenant |
| Rite of the Scarlet Sanctum | 500 | Lesser | 2 → 3 | Grants formal entry into the Scarlet Sanctum |
| Adept Rite | *(see JSON)* | Lesser | 3 → 4 | Fourth rite of the Hematic Order |
| Rite of the Crimson Lodge | 2000 | Greater | 4 → 5 | Illuminates the inner secrets of the Crimson Lodge |
| Rite of the Bloodline Covenant | 3000 | Greater | 5 → 6 | Consecrates the practitioner to the Bloodline Covenant |
| Rite of the Hematic Order | 5000 | Grand | 6 → 7 | Crowns the practitioner as Archon — the highest rank |

**Utility Rites:**

| Rite | Blood Cost | Tier | Description |
|------|-----------|------|-------------|
| **Bloodline Founding** | 500 | Lesser | Binds the caster's essence into a new bloodline, producing a presigned ancestral ledger |
| **Bloodline Recall** | 750 | Lesser | Reconjures a lost ancestral ledger from the caster's blood memory |
| **Vascular Mending** | 800 | Lesser | Floods the caster's vascular system with purified blood, fully healing all 7 vein sections |
| **Hematic Fortification** | 500 | Lesser | Strengthens the caster's connection to the blood arts |
| **Hematic Unbinding** | 1000 | Lesser | Dissolves the caster's bloodline, freeing all members and returning shared blood |
| **Crimson Beacon** | 600 | Lesser | Anchors the caster's dying essence to this location — respawn point on fatal blow |
| **Hungering Earth** | 750 | Lesser | Feeds blood into the earth, converting terrain into blood-touched stone and ash |
| **Exsanguination** | 500 | Lesser | Drains the lifeblood of a creature within the ritual circle, crystallizing their essence into Sanguine Quintessence |
| **Sanguine Attunement** | 300 | Minor | Purges and resets the caster's blood tendency alignments to a blank slate |
| **Scarlet Summons** | 2000 | Greater | Teleports all online bloodline members to the rite location (cost scales with members) |
| **Sanguine Dominion** | 3500 | Greater | Claims the surrounding land as a Blood Domain — reduced manip cost, bleeding curse on enemies, empowered blood blocks |
| **Eternal Covenant** | 4000 | Greater | Permanently expands the caster's maximum blood volume (one-time only) |
| **Lethe's Shadow** | 5000 | Grand | Strips Unstained purification from a nearby player — a blasphemous assault on Our Lady's path |
| **Ancestral Communion** | 5000 | Grand | Opens a channel to the ancient fungal consciousness, receiving cryptic lore about hemomancy's origins |

### 18.3 Plant & Fungi Recipes

Plants and fungi found in hemomancy biomes serve as ingredients across multiple crafting systems:

**ghastly_alembic Processing:**

| Input | Output | Count | Cook Time |
|-------|--------|-------|-----------|
| Infected Fungus | Foul Paste | 2 | 100 |
| Stinkhorn Fungus | Foul Paste | 2 | 100 |
| Ghost Pipe | Lethean Extract | 1 | 150 |
| Sarcodes | Dicentra Sap | 2 | 120 |
| Rafflesia | Spore Sac | 2 | 120 |
| Puffball Fungus | Spore Sac | 2 | 120 |
| Lethean Poppy | Lethean Dew | 2 | 150 |

**Crafting Recipes:**

| Recipe | Type | Ingredients | Output |
|--------|------|-------------|--------|
| Lethean Poppy Wreath | Shapeless | 4× Lethean Poppy + String | 1 |
| Lethean Extract | Shapeless | Lethean Dew + Consecrated Copper Ingot | 1 |
| Tears of Lethe | Shapeless | Lethean Extract + Silver Chalice | 1 |
| Pale Silver Ingot | Shapeless | Iron Ingot + Lethean Extract | 1 |
| Spore Sac | Shapeless | Puffball Fungus + Hyphae | 2 |
| Foul Paste (fungi) | Shapeless | Infected Fungus + Stinkhorn Fungus + Bone Meal | 3 |

**Brewing Recipes (Awkward Potion + Plant → Potion):**

Only blood-faction plants brew into hemomancy potions. Unstained plants (Puffball Fungus, Lethean Poppy, Ghost Pipe) deliberately do not brew blood-positive effects — their uses are in ghastly_alembic processing and Unstained crafting chains.

| Plant Ingredient | Result Potion |
|-----------------|---------------|
| Bleeding Heart | Potion of Sanguine Siphon |
| Infected Fungus | Potion of Mycorrhizal Mending |
| Stinkhorn Fungus | Potion of Blood Binding |
| Rafflesia | Potion of Hemolysis |
| Sarcodes | Potion of Blood Rush |

### 18.4 Food Recipes

| Recipe | Type | Notes |
|--------|------|-------|
| Gourd Slice | Crafting | Sliced from gourd block |
| Gourd Stew | Crafting | Stew from gourd + ingredients |
| Roasted Gourd Seeds | Smelting | Gourd seeds in furnace |
| Roasted Gourd Seeds | Smoking | Gourd seeds in smoker |
| Roasted Gourd Seeds | Campfire Cooking | Gourd seeds on campfire |

---

## 19. Mob Entities

### 19.1 Hostile / Monster Mobs

| Entity | Texture | Category | Notes |
|--------|---------|----------|-------|
| **Fargone** | ![](src/main/resources/assets/hemomancy/textures/entity/fargone/model_fargone.png) | Monster | Standard mosquito esk blood monster |
| **Thirster** | ![](src/main/resources/assets/hemomancy/textures/entity/thirster/model_thirster.png) | Monster | Blood-thirsting mob |
| **Abhorent Thought** | | Monster | Large (1.5×3.25), eldritch thought entity |
| **Erythromycelium Eruptus** | ![](src/main/resources/assets/hemomancy/textures/entity/erythromycelium_eruptus/model_erythromycelium_eruptus.png) | Monster | Large fungal eruption mob (1.5×3.0) |
| **Blood Drunk Puppeteer** | ![](src/main/resources/assets/hemomancy/textures/entity/blood_drunk_puppeteer/model_blood_drunk_puppeteer.png) | Monster | Human-sized, controls dolls |
| **Enthralled Doll** | ![](src/main/resources/assets/hemomancy/textures/entity/enthralled_doll/model_enthralled_doll.png) | Monster | Small (0.5×0.5), controlled by puppeteer |
| **Chthonian** | ![](src/main/resources/assets/hemomancy/textures/entity/chthonian/model_chthonian.png) | Monster | Underground termite/ant creature |
| **Chthonian Queen** | ![](src/main/resources/assets/hemomancy/textures/entity/chthonian_queen/model_chthonian_queen.png) | Monster | Boss variant of Chthonian |
| **Lump of Thought** | ![](src/main/resources/assets/hemomancy/textures/entity/lump_of_thought/model_lump_of_thought.png) | Monster | Sentient thought blob |
| **Morphling Polyp** (mob) | ![](src/main/resources/assets/hemomancy/textures/entity/morphling_polyp/model_morphling_polyp.png) | Monster | Wild morphling mob |
| **Dessicant** | | Monster | Desiccating creature (ON_GROUND spawn) |
| **Cruor Fiend** | | Monster | Blood-fueled fiend (ON_GROUND spawn) |
| **Void Drinker** | | Monster | Void-aligned blood drainer (ON_GROUND spawn) |
| **Frozen Clot** | | Monster | Ice-blood clot creature (ON_GROUND spawn) |
| **Abyssal Siphon** | | Monster | Large (1.2×0.6) deep-sea blood siphon (ON_GROUND spawn) |
| **Synapse Hound** | | Monster | Neural creature (ON_GROUND spawn) |
| **Myelin Borer** | | Monster | Burrowing neural parasite (ON_GROUND spawn) |

### 19.2 Creature / Ambient Mobs

| Entity | Texture | Category | Notes |
|--------|---------|----------|-------|
| **Leech** | ![](src/main/resources/assets/hemomancy/textures/entity/leech/model_leech_brown.png) | Creature | Small (0.4×0.1) blood-sucking leech |
| **Fungling** | ![](src/main/resources/assets/hemomancy/textures/entity/fungling/model_fungling.png) | Creature | Friendly fungal creature |
| **Chitinite** | ![](src/main/resources/assets/hemomancy/textures/entity/chitinite/model_chitinite.png) | Creature | Iron-shelled Isopod insect (1.0×0.3) |
| **Fervent Chitinite** | ![](src/main/resources/assets/hemomancy/textures/entity/fervent_chitinite/model_fervent_chitinite.png) | Creature | Fire variant of Chitinite |
| **Hemolymphopoda** | ![](src/main/resources/assets/hemomancy/textures/entity/hemolymphopoda/model_hemolymphopoda.png) | Ambient | Small (0.9×0.3), Horseshoe crab drops Cleansing Hemolymph |
| **Barbed Urchin** | ![](src/main/resources/assets/hemomancy/textures/entity/barbed_urchin/model_barbed_urchin.png) | Water Ambient | Underwater iron-barbed urchin |
| **Crimson Doe** | | Creature | Blood-touched deer (ON_GROUND spawn) |
| **Hemojelly** | | Ambient | Blood jelly creature (ON_GROUND spawn) |
| **Venous Strider** | | Ambient | Vein-walking strider (ON_GROUND spawn) |

### 19.3 NPC / Summons / Player-controlled

| Entity | Texture | Category | Notes |
|--------|---------|----------|-------|
| **Blood Thrall** | ![](src/main/resources/assets/hemomancy/textures/entity/blood_thrall/blood_thrall.png) | Creature | Small (0.6×0.7), summoned blood transport creature |
| **Unstained Zealot** | ![](src/main/resources/assets/hemomancy/textures/entity/unstained_zealot/unstained_zealot.png) | Creature | NPC that guides Unstained path entry |
| **Unstained Guardian** | | Creature | NPC that guards Unstained sacred sites |
| **Unstained Acolyte** | | Creature | NPC acolyte of the Unstained faction |
| **Harbinger Hermit** | | Creature | NPC Harbinger recluse; part of the dialogue system (`HarbingerHermitDialogueTrees`) |
| **Spectral Companion** | | Misc | Spectral ally entity |
| **Sanguilith** | | Misc (MnA) | Large (1.5×3.25), blood-themed summoned monster (requires MnA). Summoned via `ComponentSummonSanguilith` spell. Ownable, duration-limited, targets nearby hostiles with swinging melee attacks. Max 4 nearby. Registered in `MnAPluginEntityInit`. Has custom `SanguilithModel` and `SanguilithRenderer`. |

### 19.4 Entity Tags

Mobs are tagged by tendency: `FUNGAL_TAG`, `UMBRAL_TAG`, `INCANDESCENT_TAG`, `FERRIC_TAG`, `VIVACIOUS_TAG`, `RUINOUS_TAG`, `NEUROTIC_TAG`, `FERVENT_TAG`, `FRIGID_TAG`.

### 19.5 Spawn Placements

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

### 19.6 Entity Loot Tables

> **⚠️ CRITICAL WIP:** All entity loot tables in `HemoEntityLootProvider` are **entirely commented out** and the entity loot provider is also commented out in `DataGeneration.java`. This means **no Hemomancy mob currently drops any items** when killed. The intended drops (from the commented code) are:
>
> | Entity | Intended Drop |
> |--------|--------------|
> | Chitinite | Chitinous Husk |
> | Chthonian | Chitinous Husk |
> | Chthonian Queen | Chitinous Husk |
> | Leech | Swollen Leech |
> | Fargone | Sanguine Formation |
> | Thirster | Sanguine Formation |
> | Abhorent Thought | Sanguine Formation |
> | Blood Drunk Puppeteer | Puppeteering Thread |
> | Enthralled Doll | Bleeding Bulb |
> | Lump of Thought | Spore Sac |
> | Fungling | Infected Fungus |
>
> All drops use `LootingEnchantFunction` and `LootItemKilledByPlayerCondition`. These need to be uncommented and the data generator re-enabled.

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
| **Echoic Perception** | Beneficial | — | Bat morphling effect — nearby entities glow (radius scales with amplifier) |
| **Luminous Dissipation** | Beneficial | — | Moth morphling effect — knockback resistance |
| **Hemorrhagic Venom** | Beneficial | — | Tick morphling effect — AoE damage aura to nearby hostiles |
| **Spined Barricade** | Beneficial | — | Urchin morphling effect — passive thorns + armor bonus |
| **Venomous Resilience** | Beneficial | — | Centipede morphling effect — poison immunity + speed |
| **Burrower's Instinct** | Beneficial | — | Mole morphling effect — mining speed + underground regen/night vision |
| **Arcane Resonance** | Beneficial | 0x8800AA | MnA combo marker — next blood manipulation costs less blood (granted by blood-affinity MnA spells) |
| **Sanguine Clarity** | Beneficial | 0xAA0022 | MnA combo marker — next MnA spell costs less mana (granted by using blood manipulations) |

---

## 22. World Generation & Biomes

### 22.1 Custom Biomes (via TerraBlender)

| Biome | Key | Temperature | Precipitation | Notes |
|-------|-----|-------------|---------------|-------|
| **Fungal Gardens** | `fungal_gardens` | 2.0 | None (Nether) | Hyphae tendrils, huge fungi |
| **Fungal Isles** | `fungal_isles` | 2.0 | None (Nether) | Hyphae, huge fungi, small infected fungi |
| **Sporecrown Thicket** | `sporecrown_thicket` | 1.2 | None | Dense fungal overgrowth, hostile spawns (Eruptus, Chthonian, Fargone), crimson particles, dark red fog |
| **Hyphal Spires** | `hyphal_spires` | 0.9 | None | Extreme towering terrain with calcified hyphae, conscious mass patches; high-weirdness / low-erosion zones |
| **Drifting Mycelium** | `drifting_mycelium` | 0.7 | None | Anti-gravity floating islands of fungal terrain; high-continentalness zones with 3D noise creating disconnected landmasses |

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
| **Qliphoth Sanctum** | NBT structure | Dark sanctum used for the Qliphoth-related endgame content; contains Engram Block |
| **Qliphoth Bloom** | NBT structure | Qliphoth Bloom block structure placement |
| **Blood Tower (Core)** | NBT structure | Core segment of the Blood Tower multi-piece structure |
| **Blood Tower (Top 1)** | NBT structure | Top segment of the Blood Tower multi-piece structure |
| **Plains Hemopothecary** | Village structure | Hemopothecary villager house for plains biome villages |
| **Desert Hemopothecary** | Village structure | Hemopothecary villager house for desert biome villages |
| **Taiga Hemopothecary** | Village structure | Hemopothecary villager house for taiga biome villages |
| **Snowy Hemopothecary** | Village structure | Hemopothecary villager house for snowy biome villages |
| **Savanna Hemopothecary** | Village structure | Hemopothecary villager house for savanna biome villages |

> Structure NBT files are in `data/hemomancy/structures/`. The Blood Temple and Unstained Church are registered as Forge structure features with template pools. The hemopothecary village variants are integrated via `HemopothecaryProcessor` and `VillageEvents`.

---

## 24. Villagers & Professions

| Profession | POI Block | Notes |
|-----------|-----------|-------|
| ![](src/main/resources/assets/hemomancy/textures/entity/villager/profession/hemopothecary.png) **Hemopothecary** | Scrying Podium | Blood-themed villager trader |

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
- `ComponentBloodToMana` — "Sanguine Offering" — drains target's blood and converts to mana for caster (inverse of ManaToBlood). Magnitude 50–500, Blood affinity. Composable with any MnA shape.
- ![](src/main/resources/assets/hemomancy/textures/mna/blood_loss.png) `ComponentBloodLoss` — applies Blood Loss effect (movement speed debuff) via spells. Duration 60–300t, Magnitude 1–3, HARMFUL
- ![](src/main/resources/assets/hemomancy/textures/mna/blood_rush.png) `ComponentBloodRush` — applies Blood Rush effect (+move/attack speed) via spells. Duration 100–600t, Magnitude 1–3, FRIENDLY
- ![](src/main/resources/assets/hemomancy/textures/mna/hemolysis.png) `ComponentHemolysis` — applies Hemolysis effect (blood destruction DoT) via spells. Duration 40–200t, Magnitude 1–4, HARMFUL
- ![](src/main/resources/assets/hemomancy/textures/mna/summon_sanguilith.png) `ComponentSummonSanguilith` — "Conjure Sanguilith" — summons a Sanguilith at target location. Duration 200–600t (summon lifetime), Magnitude scales damage. Requires Harbinger faction. HARMFUL

**Cross-System Mechanics (Implemented):**
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
| ![](src/main/resources/assets/hemomancy/textures/item/mna/foul_vinteum_ingot.png) Foul Vinteum Ingot | ![](src/main/resources/assets/hemomancy/textures/item/befouled_vinteum_dust.png) Befouled Vinteum Dust | ![](src/main/resources/assets/hemomancy/textures/item/mna/mana_infushed_memory_blank.png) Mana Infused Memory Blank | ![](src/main/resources/assets/hemomancy/textures/item/mna/living_infused_thread.png) Living Infused Thread |
| ![](src/main/resources/assets/hemomancy/textures/item/mna/living_thread_hood.png) Living Thread Hood | ![](src/main/resources/assets/hemomancy/textures/item/mna/living_thread_robes.png) Living Thread Robes | ![](src/main/resources/assets/hemomancy/textures/item/mna/living_thread_leggings.png) Living Thread Leggings | ![](src/main/resources/assets/hemomancy/textures/item/mna/living_thread_boots.png) Living Thread Boots |
| ![](src/main/resources/assets/hemomancy/textures/item/blood_shot_occulus.png) Blood Shot Occulus | ![](src/main/resources/assets/hemomancy/textures/item/mna/mote_blood.png) Mote of Blood | ![](src/main/resources/assets/hemomancy/textures/item/mna/mana_memory_sanguine_transfusion.png) Mana Memory: Sanguine Transfusion | |

> Living Thread Armor model: ![](src/main/resources/assets/hemomancy/textures/models/armor/living_thread_layer_1.png) ![](src/main/resources/assets/hemomancy/textures/models/armor/living_thread_layer_2.png)

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
- Harbinger Mana HUD texture fix (current TODO)

### 25.2 Curios

Curios integration for the Charm of Vascularium and other equippable items (handled in `compat/curios`).

### 25.3 JEI

JEI recipe category support for:
- Chisel Station recipes
- Visceral Recaller recipes
- Blood Structure Crafting recipes
- Morphling Incubator recipes (`IncubatorRecipeCategory`)

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
| ![](src/main/resources/assets/hemomancy/textures/gui/centrifuge_gui.png) Centrifuge | ![](src/main/resources/assets/hemomancy/textures/gui/ghastly_alembic_gui.png) ghastly_alembic | ![](src/main/resources/assets/hemomancy/textures/gui/recaller_gui.png) Recaller |
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
| `GhastlyAlembicScreen` | ghastly_alembic block | ghastly_alembic crafting GUI |
| `VialCentrifugeScreen` | Vial Centrifuge | Centrifuge crafting GUI (reworked with new 3D stand model) |
| `MorphlingIncubatorScreen` | Morphling Incubator | Incubation crafting GUI |
| `MnemonicReliquaryScreen` | Mnemonic Reliquary block | Reliquary viewing GUI — opens animated lid on interaction |
| `SporeImplantScreen` | Fungal Implantation Pylon | Spore implantation GUI |
| `StructureSpawnerScreen` | Structure Spawner item | Debug structure spawning |
| Various radial menus | Living Staff / keybinds | Morphling/manipulation selection |
| Guide/Codex screens | Liber Sanguinum | **⚠️ NON-FUNCTIONAL** — `HemoProgressionScreen.setupEntries()` is entirely commented out, `ENTRIES` list is empty, `EntryScreen.render()` is commented out. The guidebook opens but displays no content. |

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
| **Lady of the Forgotten Waters** | Obtain Tears of Lethe |
| **Path of Purity** | Obtain the Tome of the Unstained |
| **Our Lady of Lethe** | Obtain the Icon of Our Lady (challenge) |
| **Bleeding a Stone** | Craft a ghastly_alembic |

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

- **Entity Loot Tables** — ~~All entity loot tables in `HemoEntityLootProvider` are entirely commented out.~~ **RESOLVED:** 25 entity loot table JSON files exist in `data/hemomancy/loot_tables/entities/` and are loaded automatically by Forge convention. The `HemoEntityLootProvider` data generator remains disabled but is not needed — loot tables work via the JSON files.
- **Progression Codex / Liber Sanguinum** — `HemoProgressionScreen.setupEntries()` is entirely commented out. The `ENTRIES` list is empty, `EntryScreen.render()` is commented out. The guidebook opens but displays no content. Needs entry definitions and page content.
- **Blood Fluid** (`FluidInit`) — Blood as a placeable fluid is entirely commented out / WIP
- **Manipulation Rank Advancement** — Ritual-based forced rank upgrades described as WIP in lore
- **Unstained Zealot Capability Check** — Uses reflection to check for `UnstainedProgressProvider` (suggests it was added incrementally)
- **Skill Effect Wiring** — ~~7 of 13 skills are not wired.~~ **PARTIALLY RESOLVED:** All 18 skills in `SkillPointHelper` have helper methods. Fully wired into event handlers (7): Capacity, Efficiency, Manip Slots, Last Wind, Feeding Frenzy, Crimson Mastery, Sanguine Reach. Still helper-only/not called from events (10): Dynamic Use, Hemostasis, Sanguine Surge, Vital Link, Iron Will, Blood Flow, Coagulation, Rune Affinity, Rune Resonance, Rune Mastery.
- **Loot Modifiers** (`AddItemModifier`) — framework exists, specific loot tables TBD
- **Visceral Organs System** — Organ extraction ritual flow is implemented. Organ modification tiers and gameplay effects for each extracted organ still TBD. See §13.8 for details.
- **Armor Set Bonuses** — ~~No set bonus logic exists.~~ **RESOLVED:** All 5 armor sets now have unique set bonuses implemented in `ArmorSetBonusHandler`: Hematic Iron (blood regen), Blood Lust (lifesteal), Barbed (thorns + Blood Loss), Chitinite (toughness + projectile reduction), Unstained (Blood Loss/Hemolysis immunity). The Marrow Crown artifact has a standalone +10% damage bonus when blood > 50%. See §15 for details.
- **Old Morphling Maturity** — The 6 original morphlings (Fungal, Leeches, Chitinite, Serpent, Pests, Spider) lack named maturity-tier reactive abilities unlike the 6 newer morphlings.
- **Rune Gameplay Effects** — Standard runes only deepen tendency alignment when equipped. Individual gameplay bonuses (e.g., stat boosts, triggered effects) are not yet implemented beyond the Functional Spores.
- **Vial Centrifuge Rework** — New 3D stand model (`CentrifugeStandModel`) and custom item renderer implemented; UI and menu updated. `VialCentrifugeBlockItem` has custom `BlockEntityWithoutLevelRenderer`.
- **Memory Overlay Textures** — All manipulations now have unique overlay textures (`textures/item/memories/memory_*_overlay.png`) for the layered memory item model system. The `HemoItemModelProvider` generates 2-layer models (base `memory_blank` + per-manipulation overlay) for all `BloodMemoryItem` instances.
- **Incubator Recipe System** — Full `IncubatorRecipe` + `IncubatorRecipeSerializer` added with 13 JSON recipes for all morphling types. JEI integration via `IncubatorRecipeCategory`. Recipes stored in `data/hemomancy/recipes/incubator/`.
- **Mnemonic Reliquary** — New functional block with animated lid (open/close), custom 3D block entity renderer (`MnemonicReliquaryRenderer`), item renderer (`MnemonicReliquaryItemRenderer`), block model (`MnemonicReliquaryModel`), menu (`MnemonicReliquaryMenu`), and screen (`MnemonicReliquaryScreen`). Tracks open count and syncs lid angle via block events.
- **Suspended Cleansed Blood Crystal** — Purified variant of the Suspended Blood Crystal with custom block, block entity (random time offset for desynchronized animations), block item with custom renderer, 3D model, and blockstate.
- **Cleansed Sanguine Glass & Pane** — New glass/pane variants added to the block system with blockstates, models, textures, and loot tables.
- **Debug Showcase Item** — Creative-mode testing tool (`DebugShowcaseItem`) that generates an organized showcase of all mod content in 4 sections: items in chests, blocks on platforms, mobs in fenced pens, and multiblock structures placed as patterns.
- **Cardinal Rite Boundary Renderer** — Client-side visual renderer (`CardinalRiteBoundaryRenderer`) for cardinal rite boundaries during active rites.
- **Morphling Item Textures** — All morphling types now have individual item textures and item models (bat, centipede, chitinite, fungal, leeches, mole, moth, pests, serpent, spider, tick, urchin).
- **MnA Compatibility Expansion** — Extensive brainstorming for new cross-mod features documented in `MNA_COMPATIBILITY_BRAINSTORM.md`. Recently implemented: 5 new spell components (Blood Loss, Blood Rush, Hemolysis, Summon Sanguilith, Blood-to-Mana), Blood Tithe handler, Spell ↔ Manipulation combo system, full cross-mod config. Still planned: spell shapes, rituals, faction enhancements, construct system, wand core, manaweaving recipes, runeforging enchantments.
- **GhastlyAlembic Custom Renderer** — `GhastlyAlembicRenderer` now renders the block as a full 3D entity model (`GhastlyAlembicModel`) with facing-aware rotation. Previously was a static block.
- **MorphlingIncubator Custom Renderer** — `MorphlingIncubatorRenderer` now renders the incubator as a full 3D entity model with custom animation.
- **New Monster Mobs (WIP)** — 10 new monster entity types are registered (Dessicant, Cruor Fiend, Void Drinker, Frozen Clot, Abyssal Siphon, Synapse Hound, Myelin Borer) and 3 creature/ambient types (Crimson Doe, Hemojelly, Venous Strider). Spawn rules are registered but specific spawn biomes, AI, drops, and loot tables are still being designed/implemented.
- **New NPC Entities (WIP)** — Unstained Guardian, Unstained Acolyte, Harbinger Hermit, and Spectral Companion entities are registered but their AI, dialogue, and drop content is still being developed.
- **New Manipulation Expansions** — 8 new manipulations added covering new tendencies: Congeatio (Cryogenic Pulse, Glacial Bastion), Flammeus (Sanguine Ignition, Vitric Combustion), Tenebris (Void Shroud, Blood Eclipse), Mortem (Hemorrhage, Exsanguinate). Memory items and overlay textures for these manipulations may still need to be generated.
- **Rune Tier System** — All three tiers of runes now fully registered (10 Tier 1, 8 Tier 2, 8 Tier 3 = 26 total runes) with patterns for all. Individual gameplay bonuses beyond tendency alignment remain unimplemented.
- **HemoItemModelProvider Enhancements** — Data generator now handles `BloodMemoryItem` 2-layer models, `ItemRunePattern` 2-layer models, and properly excludes special blocks (sanguine panes, cleansed sanguine panes, ash trails, engram, filler, crimson flames) from automatic block model generation.

### 30.1 Unstained Expansion — Planned Features

The Unstained faction is being expanded with deeper lore around **Our Lady of Lethe** as their patron. Planned and in-progress features:

- **Altar of Cleansing** — functional block that grants a one-time +25 purity boost when Tears of Lethe are offered. Also accepts Lethean Poppy Wreaths (repeatable +5 purity) and Silver Chalices (+5 clarity). Will eventually be placed in every Unstained temple structure.
- **Unstained Temple Structure Expansion** — the Unstained temple structure should be expanded to include an Altar of Cleansing, Lethe Lanterns, Cleansed Stone blocks, and more atmospheric elements befitting a shrine to Our Lady.
- **Our Lady of Lethe NPC / Apparition** — a potential future entity: a spectral manifestation of Our Lady that appears briefly at the altar during the blessing, or as a rare encounter near Lethean Poppy fields. Description: tall woman, white hair, white robes, silver eyes, pale blue skin.
- **Unstained Dialogue Expansion** — Zealot dialogues should reference Our Lady of Lethe more directly, with lore about the River Lethe, the meaning of forgetting, and the significance of the poppies.
- **Lethean Crafting Recipes** — implemented recipes:
  - ✅ Tears of Lethe = Lethean Extract + Silver Chalice (crafting)
  - ✅ Lethean Poppy Wreath = 4× Lethean Poppy + String (crafting)
  - ✅ Lethean Extract = Lethean Dew + Consecrated Copper Ingot (crafting)
  - ✅ Pale Silver Ingot = Iron Ingot + Lethean Extract (crafting)
  - Lethe Lantern = Pale Silver Ingot + Lethean Extract + Glowstone (crafting) — planned
  - Cleansed Stone = Stone + Hemolytic Solution (crafting) — planned
- **Unstained Advancement/Achievement Tree** — a dedicated Unstained branch of the advancement tree tracking:
  - Begin the Unstained path
  - Receive the Altar's blessing
  - Reach each purity stage (Tainted → Cleansing → Absolved → Purified)
  - Unlock clarity
  - Reach each clarity stage (Awakened → Discerning → Vigilant → Resolute → Enlightened)
  - Collect all Unstained materials
- **Silver Ward / Verdigris Aura Visual Indicators** — particle effects and visual indicators for active Unstained bonuses, potentially with Our Lady's motifs (silver droplets, pale blue mist).

---

## 31. Configuration

Hemomancy has three config files plus a conditional MnA config.

### 31.1 Server Config (`HemoServerConfig`)

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

### 31.2 Client Config (`HemoClientConfig`)

| Key | Type | Default | Range | Description |
|-----|------|---------|-------|-------------|
| `location` | Int | `0` | 0–3 | Blood Volume HUD position: 0=Top Left, 1=Top Right, 2=Bottom Left, 3=Bottom Right |

### 31.3 MnA Cross-Mod Config (`HemoMnAConfig`)

Only registered when Mana and Artifice is present. See §25.1 for details on what each system does.

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

## 32. Networking & Packets

All packets are registered in `PacketHandler.registerChannels()` across 8 `SimpleChannel` instances:

| Channel | ID | Purpose | Packet Count |
|---------|----|---------|-------------|
| `CHANNELBLOODTENDENCY` | `bloodtendencychannel` | Blood tendency sync (client ↔ server) + tendency view GUI | 3 |
| `CHANNELVASCULARSYSTEM` | `vascularsystemchannel` | Vascular system sync (client ↔ server) + vascular view GUI | 3 |
| `CHANNELBLOODVOLUME` | `bloodvolumechannel` | Blood volume sync, blood draw, skill tree, bloodline pool, cardinal rites, degree sync, unstained progress, structure spawner, dialogue | 16 |
| `CHANNELKNOWNMANIPS` | `knownmanipulationchannel` | Manipulation sync, use/cast packets, cooldowns, vein teleport, avatar tracking, centrifuge start, equip manipulation | 14 |
| `CHANNELPARTICLES` | `particlechannel` | Blood flask, avatar hit, blood claw, and living tool break particle spawning | 4 |
| `CHANNELRUNES` | `runechannel` | Elytra flight, spore/rune/normal inventory open, rune sync, gourd sync, horn animation, gourd toggle, chisel station packets | 11 |
| `CHANNELMORPHLINGJAR` | `morphlingjarchannel` | Jar toggle pickup, open jar, toggle jar message, living staff open, staff morph update, morph key change, equipped morphling sync | 7 |
| `CHANNELRUNEBINDER` | `runebinderchannel` | Open rune binder, toggle binder message | 2 |

**Total:** ~60 registered packets across 8 channels.

Notable packets:
- `PacketUnlockSkill` / `PacketSyncSkills` — Skill tree progression (via `CHANNELBLOODVOLUME`)
- `PacketSyncActiveRites` — Cardinal rite boundary sync for client-side rendering
- `PacketSyncDegree` / `PacketSyncUnstainedProgress` — Path progression sync
- `SyncTrackingAvatarPacket` — Blood Avatar visual state sync to all nearby players
- `TeleportToVeinPacket` — Venous Travel teleportation
- `OpenDialoguePacket` / `DialogueOptionPacket` — Unstained Zealot conversation system
- `PlaceStructurePacket` — Debug structure spawner

---

## 33. Sound Events

Registered in `SoundInit`:

| Sound Event | Registry Key | Notes |
|-------------|-------------|-------|
| Abhorent Thought Ambient | `entity.abhorent_thought.ambient` | Idle sound for the Abhorent Thought mob |
| Abhorent Thought Hurt | `entity.abhorent_thought.hurt` | Damage sound for the Abhorent Thought mob |
| Abhorent Thought Death | `entity.abhorent_thought.death` | Death sound for the Abhorent Thought mob |

> **Note:** Only 3 custom sounds are currently registered. Most entities and interactions use vanilla Minecraft sounds (e.g., `SoundEvents.BOOK_PAGE_TURN`, `SoundEvents.ANVIL_USE`, `SoundEvents.EXPERIENCE_ORB_PICKUP`, etc.). Custom sounds for other mobs, manipulations, and rituals are a planned expansion.

---

## 34. Particle Types

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

*This document should be kept up to date as development continues. Each section maps directly to the codebase structure under `com.vincenthuto.hemomancy`.*
