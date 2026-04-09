# Hemomancy × Mana and Artifice — Compatibility Brainstorm

> **Date:** 2026-04-08 (Revised — 8 features now implemented)
> **Status:** Brainstorming / Feature Planning — Partially Implemented
> **Goal:** Make playing with both Hemomancy and Mana and Artifice (MnA) together more fun, synergistic, and rewarding.

### Design Principle: "Why Does This Need MnA?"

Every feature in this document must pass a simple test: **Does this feature only make sense because MnA exists?** If a feature could (or should) eventually exist in base Hemomancy without MnA being installed, it doesn't belong here. Cross-mod features must leverage MnA-specific systems — the spell crafting system, the Occulus progression, faction mechanics, constructs, runeforging, manaweaving, affinities, ritual circles, wands, etc. — in ways that wouldn't work without them.

---

## Current Integration Summary

Before brainstorming new features, here is what already exists:

| Category | What Exists |
|----------|-------------|
| **Faction** | The Harbingers — custom faction with blood-red manaweave, Mark of Blood token, Tome of the Impending End grimoire, Horn of the Impending End, custom HarbingersMana casting resource |
| **Spell Components** | Blood Binding (root), Sanguine Transmutation (mana→blood), Sanguine Fertility (breeding buff), **Sanguine Offering (blood→mana, `ComponentBloodToMana`)**, **Blood Loss (`ComponentBloodLoss`)**, **Blood Rush (`ComponentBloodRush`)**, **Hemolysis (`ComponentHemolysis`)**, **Conjure Sanguilith (`ComponentSummonSanguilith`)** |
| **Manipulations** | Sanguine Transfusion (blood→mana, inverse of Sanguine Transmutation) |
| **Rituals** | Ritual of the Weeping Wound (drains 1000 blood → Mote of Blood) |
| **Items** | Befouled Vinteum Dust, Foul Vinteum Ingot, Mana Infused Memory Blank, Living Infused Thread, Mote of Blood, Blood Shot Occulus, Living Thread Armor (4 pieces) |
| **Armor** | Living Thread set (Hood, Robes, Leggings, Boots) — dyeable, mana-repairable, 3pc set bonus: +500 max mana, +50% mana regen |
| **Crafting** | Runic Anvil: Living Infused Thread + Mage Armor → Living Thread armor pieces |
| **Block/Tile** | Broken Mana Trapezohedron — provides mana regen aura, recharges constructs, charges pedestal items, charges sigils |
| **Entity** | Sanguilith — blood-themed summoned monster, uses MnA summon/target utilities |
| **Guidebook** | 12+ MnA guidebook entries covering all cross-mod content (updated with new components) |
| **VFX** | Animated bloody border overlay on Hemomancy spell icons (HemoSpellIconCompositor) |
| **Cross-System: Blood Tithe** | `BloodTitheHandler` — Harbinger faction members casting blood-affinity spells have 25% (configurable) of mana cost converted to blood cost. Hooks into `CalculatingManaCostEvent`. |
| **Cross-System: Spell ↔ Manip Combos** | `ManipComboHelper` + `BloodTitheHandler` — Casting blood-affinity MnA spells grants **Arcane Resonance** (reduces next manipulation's blood cost). Using manipulations grants **Sanguine Clarity** (reduces next spell's mana cost). |
| **Effects (Combo)** | Arcane Resonance (marker buff, reduces blood cost), Sanguine Clarity (marker buff, reduces mana cost) |
| **Config** | `HemoMnAConfig` — Full server config for all cross-mod mechanics: conversion ratios, Blood Tithe settings, Living Thread armor values, Trapezohedron radius, combo durations/reductions, Sanguilith scaling |
| **Spell Textures** | blood_loss.png, blood_rush.png, hemolysis.png, summon_sanguilith.png (all in `textures/mna/`) |
| **MnA Recipes** | Component recipe JSONs for Blood Loss, Blood Rush, Hemolysis, Summon Sanguilith (in `data/hemomancy/recipes/components/`) |

---

## New Feature Ideas

### 1. New Spell Components

These are MnA spell system components that players slot into MnA's spell crafting UI. They inherently require MnA because they extend `SpellEffect` / `PotionEffectComponent` and are only meaningful within MnA's composable spell system (shapes + components + modifiers). The value is being able to **compose** these with other MnA shapes and modifiers — something that blood manipulations alone can't do.

#### 1a. `ComponentBloodToMana` — "Sanguine Offering" ✅ IMPLEMENTED
- **Status:** Implemented in `compat/mna/spell/ComponentBloodToMana.java`. Extends `SpellEffect` with MAGNITUDE attribute (50–500). Conversion ratio configurable via `HemoMnAConfig.BLOOD_TO_MANA_RATIO`.
- **Concept:** The inverse of `ComponentManaToBlood`. Drains the target's (or caster's) Hemomancy blood volume and converts it into mana for the caster.
- **MnA Justification:** Exists as a spell component, so it can be composed with any MnA shape (projectile, touch, self, AoE zone) and modified by MnA modifiers (amplify, extend, etc.). The existing manipulation `SanguineTransfusionManip` is self-only and fixed-cost. As a spell component, this becomes far more flexible — e.g., pair it with a beam shape to drain enemy blood into your mana at range, or combine it with an AoE zone shape to create a mana-leeching field.
- **Attributes:** Magnitude (how much blood to drain, 50–500), Affinity: Blood
- **Tag:** FRIENDLY (self-cast) or HARMFUL (target drain — drains target blood, converts to caster mana)

#### 1b. `ComponentBloodLoss` — "Hemorrhage" ✅ IMPLEMENTED
- **Status:** Implemented in `compat/mna/spell/ComponentBloodLoss.java`. Extends `PotionEffectComponent`, icon at `textures/mna/blood_loss.png`, recipe JSON at `data/hemomancy/recipes/components/blood_loss.json`.
- **Concept:** Applies the existing Blood Loss effect (movement speed debuff) via the MnA spell system.
- **MnA Justification:** Blood Loss exists as a potion/effect in base Hemomancy, but applying it via spells lets players combine it with MnA shapes and modifiers that don't exist in Hemomancy — e.g., attach it to a Rune shape to create a persistent trap that slows enemies, or use a Chain modifier to spread it to multiple targets, or combine it with projectile shape for ranged application. These delivery mechanisms are MnA-exclusive.
- **Attributes:** Duration (60–300 ticks), Magnitude (1–3), Affinity: Blood
- **Tag:** HARMFUL

#### 1c. `ComponentBloodRush` — "Crimson Surge" ✅ IMPLEMENTED
- **Status:** Implemented in `compat/mna/spell/ComponentBloodRush.java`. Extends `PotionEffectComponent`, icon at `textures/mna/blood_rush.png`, recipe JSON at `data/hemomancy/recipes/components/blood_rush.json`.
- **Concept:** Applies the Blood Rush effect (+move speed, +attack speed) via spells.
- **MnA Justification:** Same reasoning as 1b — composability with MnA shapes. Cast on allies at range with projectile shape, create a sigil/rune that buffs anyone who steps on it, or combine with zone shape to create a "blood rally point" area buff. None of these delivery options exist in base Hemomancy.
- **Attributes:** Duration (100–600 ticks), Magnitude (1–3), Affinity: Blood
- **Tag:** FRIENDLY

#### 1d. `ComponentHemolysis` — "Blood Destruction" ✅ IMPLEMENTED
- **Status:** Implemented in `compat/mna/spell/ComponentHemolysis.java`. Extends `PotionEffectComponent`, icon at `textures/mna/hemolysis.png`, recipe JSON at `data/hemomancy/recipes/components/hemolysis.json`.
- **Concept:** Applies Hemolysis effect via spells.
- **MnA Justification:** Same composability argument. Additionally, when combined with MnA's spell modifier system, the DoT behavior can be tuned (extended, amplified) in ways that Hemomancy's potion system doesn't support. Pair with AoE/zone shapes for area denial.
- **Attributes:** Duration (40–200 ticks), Magnitude (1–4), Affinity: Blood
- **Tag:** HARMFUL

#### 1e. `ComponentSummonSanguilith` — "Conjure Sanguilith" ✅ IMPLEMENTED
- **Status:** Implemented in `compat/mna/spell/ComponentSummonSanguilith.java`. Extends `SpellEffect`, icon at `textures/mna/summon_sanguilith.png`, recipe JSON at `data/hemomancy/recipes/components/summon_sanguilith.json`. Requires Harbinger faction. Health scaling configurable via `HemoMnAConfig.SANGUILITH_HEALTH_PER_MAGNITUDE`.
- **Concept:** Summons a Sanguilith entity at the target location via spell casting.
- **MnA Justification:** The Sanguilith already exists as an MnA-dependent entity (uses `SummonUtils`, MnA particles, MnA sounds). But currently there's no way to summon it via a spell — you'd need to trigger it through code. Making it a proper spell component means it integrates into MnA's summon ecosystem alongside Animated Constructs and other faction summons. Players can customize it with MnA modifiers (longer duration, more damage, etc.).
- **Attributes:** Duration (200–600 ticks for summon lifetime), Magnitude (damage scaling), Affinity: Blood
- **Tag:** HARMFUL

### 2. New Spell Shapes

Spell shapes are a fundamentally MnA-only concept — they define *how* a spell is delivered. These can't exist without MnA's spell system.

#### 2a. `ShapeBloodPulse` — "Sanguine Pulse"
- **Concept:** A short-range AoE burst shape that radiates outward from the caster, themed with blood particles. Unlike MnA's standard pulse shapes, this one costs blood volume in addition to mana — the more blood spent, the larger the radius.
- **MnA Justification:** Extends MnA's `SpellShape` API. The dual-resource cost (mana from MnA + blood from Hemomancy) is something that can't exist without both mods. Any spell component can be attached to this shape, not just blood ones — you could use a standard MnA fire effect with a blood pulse delivery.
- **Mechanics:** 3–8 block radius (scales with blood spent), affects all entities in range.
- **Visual:** Red blood cell particles radiating outward.

#### 2b. `ShapeBloodLink` — "Hemomantic Tether"  
- **Concept:** A beam-type shape that creates a visible blood tether between caster and target. Spell effects are applied continuously while the tether holds. The tether consumes blood volume per tick to maintain.
- **MnA Justification:** This is a new spell delivery mechanism (channeled tether) that feeds into MnA's shape system. Any MnA component can be applied through it — e.g., tether + healing = continuous healing beam that costs blood; tether + damage = sustained blood-drain beam. The tether is a new shape type that enriches MnA's spell design space.
- **Mechanics:** Maintains connection up to 12 blocks, breaks on line-of-sight loss or distance. Drains blood per tick.

### 3. New MnA Rituals

MnA rituals use MnA's ritual circle system with chalk patterns, reagents on pedestals, and specific MnA-registered ritual effects. These must produce results that specifically bridge both mod systems.

#### 3a. Ritual of Sanguine Convergence
- **Concept:** An MnA ritual that uses the ritual circle to permanently bind a player's MnA casting resource to their Hemomancy blood pool — creating a "hybrid resource" where mana regeneration is boosted proportional to current blood volume, and blood regeneration is boosted proportional to current mana. The bond persists until removed by a counter-ritual.
- **MnA Justification:** This fundamentally links MnA's casting resource system (`ICastingResource`) with Hemomancy's blood volume capability. It can only work with both systems present. The ritual uses MnA's ritual circle, chalk patterns, and pedestal-based reagents. The effect modifies `HarbingersMana` regeneration rates based on `IBloodVolume` state — neither system alone can do this.
- **Requirements:** MnA ritual circle with blood chalk pattern, Mote of Blood × 4 on pedestals, Greater Mote of Blood as focus, Harbinger faction membership required.
- **Effect:** +1% mana regen per 100 blood volume stored. +0.5% blood regen per 100 mana stored. Stacks with other modifiers.

#### 3b. Ritual of the Arcane Crucible
- **Concept:** An MnA ritual that transmutes MnA-exclusive materials into Hemomancy cross-mod items that can't be created any other way. Specifically: converts Vinteum + mana + blood sacrifice into "Foul Vinteum Catalyst" — a higher-tier crafting material needed for the most powerful cross-mod equipment.
- **MnA Justification:** Replaces the old "Ritual of the Iron Covenant" — this version specifically requires MnA ritual infrastructure (circle, pedestals, chalk) and consumes MnA materials (Vinteum, Chimerite) alongside Hemomancy blood. The output is a cross-mod material that has no base-Hemomancy equivalent.
- **Requirements:** MnA ritual circle, Vinteum Ingot on pedestal, Chimerite on pedestal, Hematic Iron Scrap on pedestal, 1500 blood cost. Must be Harbinger faction.
- **Output:** Foul Vinteum Catalyst × 1 — used in advanced Runic Anvil and Manaweaving recipes.

#### 3c. Ritual of the Mana Wound
- **Concept:** An MnA ritual that creates a persistent "mana wound" zone at the ritual site. Within this zone, any MnA spell cast has a chance to also apply a random Hemomancy effect (Blood Loss, Blood Rush, Hemolysis) to its targets as a free rider. The zone lasts for a configurable duration.
- **MnA Justification:** This literally modifies how MnA spells behave in a localized area — it hooks into MnA's spell resolution system to inject Hemomancy effects. This can only function when both mod systems are active. It uses MnA's ritual system to create the zone and MnA's spell event hooks to modify spell behavior.
- **Requirements:** MnA ritual circle, Befouled Vinteum Dust, Engram blocks (lit), 2000 blood cost.
- **Effect:** 16-block radius zone lasting 10 minutes. 25% chance per spell cast in zone to apply a random Hemomancy debuff (on harmful spells) or buff (on friendly spells).

### 4. New Items & Crafting (MnA Crafting Systems)

These items are created through MnA-specific crafting systems (Manaweaving, Runic Anvil, Runeforging) and interact with MnA mechanics. They can't exist without MnA's crafting infrastructure.

#### 4a. Hemomantic Wand Core
- **Concept:** A wand core material for MnA's wand crafting system, made from Living Infused Thread + Foul Vinteum Ingot via the Manaweaving Altar.
- **MnA Justification:** MnA wands are a core MnA system with cores, handles, and gems. This adds a blood-themed core that only works within MnA's wand crafting. Wands with this core get a unique mechanic: spells cast with Blood affinity components consume slightly less mana but also drain a small amount of blood. The dual-resource mechanic only exists because both resource systems are present.
- **Effect:** Blood affinity spells cost 15% less mana but also drain 25 blood per cast. Non-blood spells work normally.

#### 4b. Mote of Mana
- **Concept:** A crystallized mana essence created via the Ritual of the Weeping Wound's inverse — an MnA ritual that sacrifices massive mana (not blood) to produce a crystal usable in Hemomancy's Visceral Recaller as a special catalyst.
- **MnA Justification:** Created through MnA's ritual system by consuming MnA's mana resource. The output item serves as a bridge ingredient: it slots into Hemomancy's Visceral Recaller as a catalyst that reduces the blood cost of crafting recipes. This creates a genuine cross-mod crafting loop — MnA players can invest their mana to ease Hemomancy crafting, and vice versa with Mote of Blood.
- **Crafting:** MnA ritual circle, 5000 mana sacrificed, Vinteum block as focus.
- **Use:** Visceral Recaller catalyst that reduces blood cost by 50% for one recipe.

#### 4c. Blood-Infused Construct Capabilities
- **Concept:** New construct capability items for MnA's Construct system, crafted via MnA's runeforging system using Hemomancy materials.
- **MnA Justification:** MnA Constructs are MnA's animated companion system with modular capabilities. The Broken Mana Trapezohedron already recharges them. These new capabilities are specifically MnA Construct modules that add blood-themed behavior — they plug into MnA's `ConstructCapability` system and can only exist on MnA Constructs.
- **Capabilities:**
  - **Blood Reservoir Module:** (Runeforged from Hematic Iron + Mote of Blood) Construct gains a blood reservoir that slowly fills from defeated enemies. Nearby Harbinger faction players can drain it to refill their blood volume. Uses MnA's `ConstructCapability.STORE_MANA` pattern but for blood.
  - **Hemomantic Plating:** (Runeforged from Chitinite Plating + Foul Vinteum Ingot) Construct gains life-steal on attacks — heals itself by draining target blood. Interacts with Hemomancy's NOBLOOD entity tag.
  - **Sanguine Casting Core:** (Runeforged from Mana Infused Memory Blank + Greater Mote of Blood) Construct can cast one Hemomancy blood manipulation per 60 seconds (configured at runeforging time). Uses MnA's construct AI targeting + Hemomancy's manipulation system.

#### 4d. Arcane Living Staff
- **Concept:** An upgraded Living Staff created via Manaweaving Altar that can function as both a Hemomancy manipulation focus AND an MnA spell wand.
- **MnA Justification:** MnA wands and Hemomancy's Living Staff are separate item systems. This hybrid item bridges them — it renders with morphling toppers like the Living Staff but can also be used to cast MnA spells from the Harbinger grimoire. Requires Manaweaving (MnA crafting) and Living Staff + Hemomantic Wand Core as inputs. This item literally cannot function without both mod systems.
- **Crafting:** Manaweaving Altar — Living Staff + Hemomantic Wand Core + Foul Vinteum Catalyst.
- **Mechanics:** Right-click cycles morphlings (Hemomancy), left-click uses equipped morphling attack, spell casting via Harbinger grimoire keybind.

### 5. Enhanced Faction Features — The Harbingers

All of these are MnA faction features that use MnA-specific systems (Occulus, faction progression, raids, sanctums, manaweaving).

#### 5a. Faction Tasks / Occulus Progression
- **Concept:** The Harbingers faction should have unique Occulus tasks that specifically involve both MnA and Hemomancy activities. The Occulus is MnA's progression GUI — tasks here unlock higher faction tiers and rewards.
- **MnA Justification:** The Occulus task system is entirely MnA's. `HarbingersFaction.getOcculusTaskPrompt()` currently returns a generic prompt. Custom tasks would integrate Hemomancy milestones into MnA's progression framework — something that can only happen when both mods are present.
- **Examples:**
  - "Cast 50 spells using Blood affinity components" (uses MnA spell tracking + Hemomancy affinity)
  - "Perform the Ritual of the Weeping Wound" (MnA ritual + Hemomancy blood cost)
  - "Reach Blood Affinity level 5 in the Occulus" (MnA affinity progression + Hemomancy-themed)
  - "Craft Living Thread Armor via the Runic Anvil" (MnA crafting + Hemomancy materials)

#### 5b. Harbinger Faction Raid Enemies
- **Concept:** When the Harbinger faction horn is sounded (or a rival faction attacks Harbinger territory), blood-themed enemies appear using MnA's faction raid system.
- **MnA Justification:** Faction raids are an MnA system (`FactionRaidEvent`, raid wave spawning, raid sound/music). The Harbingers faction needs its own raid roster to feel complete within MnA's faction ecosystem. Raid mobs would be Hemomancy entities (Blood Golems, Fungal creatures) registered through MnA's raid mob system.

#### 5c. Harbinger Sanctum Structure
- **Concept:** A custom MnA "sanctum" multiblock structure for the Harbingers. Currently `getSanctumStructure` returns MnA's generic council circle.
- **MnA Justification:** Every MnA faction has a unique sanctum structure (Council Circle, Demon Pit, etc.) that's used for faction-specific rituals and progression. The Harbingers need one built from Hemomancy blocks (venous stone, engrams, blood stained glass) but registered through MnA's structure system. Players discover and activate it through MnA's sanctum mechanics.

#### 5d. Harbinger-Specific Manaweaving Recipes
- **Concept:** Manaweaving altar recipes that require the Harbinger faction and produce enhanced Hemomancy items.
- **MnA Justification:** The Manaweaving Altar is MnA's faction-locked crafting station — recipes require specific faction membership and mana expenditure. These recipes leverage MnA's manaweaving infrastructure to create items that bridge both systems:
  - Manaweave + Blood Gourd + Vinteum → **Arcane Blood Gourd** (stores blood AND mana, allows paying either resource for either mod's costs)
  - Manaweave + Morphling Jar + Chimerite → **Resonant Morphling Jar** (morphling effects also grant a small MnA affinity XP bonus in the corresponding element)
  - Manaweave + Hematic Memory Blank + Mana Infused Memory Blank → **Dual Memory** (teaches both a blood manipulation AND grants spell XP toward learning the corresponding spell component)

### 6. Cross-System Mechanics

These mechanics specifically bridge MnA and Hemomancy's separate systems in ways that only function when both are present.

#### 6a. Blood Affinity ↔ Tendency Synergy
- **Concept:** When a player has high Blood affinity in MnA's Occulus AND high blood tendency levels in Hemomancy, they gain passive bonuses that affect both systems:
  - High Blood affinity (MnA) + High ANIMUS tendency (Hemomancy) → blood-affinity spells also heal the caster for a small amount
  - High Blood affinity (MnA) + High FERRIC tendency (Hemomancy) → blood-affinity spells deal bonus damage to armored targets
  - Having any tendency above 5 → +5% reduced mana cost for Blood affinity spells per tendency above 5
- **MnA Justification:** Reads MnA's affinity system (`IPlayerMagic`) and Hemomancy's tendency system (`IBloodKinship`) simultaneously. The bonuses modify MnA spell behavior based on Hemomancy progression. Neither system alone has enough information to produce these effects.
- **Implementation:** Forge tick event that checks both capabilities, applies attribute modifiers.

#### 6b. Tendency ↔ Affinity Mapping for Spell Power
- **Concept:** Map Hemomancy's 8 blood tendencies to MnA's affinities so that investing in Hemomancy tendencies boosts corresponding MnA spell power:
  - ANIMUS (Life) → WATER (healing/life)
  - FLAMMEUS (Fire) → FIRE
  - DUCTILIS (Lightning/Speed) → WIND
  - LUX (Light) → ARCANE
  - MORTEM (Death) → ENDER
  - CONGEATIO (Cold/Ice) → ICE
  - FERRIC (Iron/Solidity) → EARTH
  - TENEBRIS (Darkness) → BLOOD
- **MnA Justification:** This modifies MnA's spell damage/effect calculations based on Hemomancy's tendency values. It hooks into MnA's spell resolution to apply bonuses. The mapping is meaningless without MnA's affinity system as the target.
- **Effect:** Each tendency point above 3 gives +2% power to spells of the corresponding MnA affinity.

#### 6c. Blood Tithe — Spell Modifier ✅ IMPLEMENTED (as event handler)
- **Status:** Implemented in `compat/mna/spell/BloodTitheHandler.java` as a Forge event handler (hooks into `CalculatingManaCostEvent` and `SpellCastEvent`) rather than as a `SpellModifier`. This was chosen because it's simpler and automatically applies to all Harbinger faction members casting blood-affinity spells without requiring players to manually slot a modifier. Configurable via `HemoMnAConfig` (enable/disable, mana reduction %, blood-per-mana ratio).
- **Concept:** A new MnA spell modifier (slotted into spells via the spell crafting UI) that replaces a percentage of the spell's mana cost with blood cost.
- **MnA Justification:** This is a `SpellModifier` that lives inside MnA's spell crafting system. Players add it to spells in the crafting UI like any other modifier. It hooks into MnA's spell cost calculation to divert part of the cost to Hemomancy's blood pool. This is a spell modifier — an MnA-exclusive concept.
- **Mechanics:** Tier 1: 25% of mana cost paid in blood instead. Tier 2: 50%. Tier 3: 75%. Blood-to-mana ratio: 5 blood per 1 mana replaced. Requires Harbinger faction.

#### 6d. Spell → Manipulation Combos ✅ IMPLEMENTED
- **Status:** Implemented across `BloodTitheHandler.onSpellCast()` (grants Arcane Resonance after blood-affinity spell cast) and `ManipComboHelper.onManipulationUsed()` (grants Sanguine Clarity after manipulation use, consumes Arcane Resonance). Two new marker effects added: `ArcaneResonanceEffect` and `SanguineClarityEffect`. Durations and reduction percentages configurable via `HemoMnAConfig`. Integration in `BloodManipulation.performAction()` checks for Arcane Resonance to reduce blood cost.
- **Concept:** Casting certain MnA spells with Blood affinity components triggers a brief "Arcane Resonance" buff that reduces the blood cost and cooldown of the next Hemomancy manipulation used within 5 seconds. Conversely, using certain high-rank Hemomancy manipulations grants a brief "Sanguine Clarity" buff that reduces the mana cost of the next MnA spell.
- **MnA Justification:** This combo system reads MnA's spell cast events and Hemomancy's manipulation use events. It creates a gameplay loop that alternates between both mod systems — cast MnA spell → use Hemomancy manipulation at reduced cost → cast MnA spell at reduced cost → repeat. The combo mechanic bridges both systems' event buses.

### 7. New Blocks & Structures (MnA Infrastructure)

These blocks extend MnA's block/tile entity systems or create structures using MnA's multiblock framework.

#### 7a. Blood-Corrupted Mana Pedestal
- **Concept:** A Hemomancy variant of MnA's pedestal (`PedestalBlock` / `PedestalTile`) that can hold MnA items and slowly corrupts them with blood, converting MnA materials into cross-mod variants.
- **MnA Justification:** This is a variant of MnA's own pedestal system. It extends `PedestalBlock`, interacts with MnA's pedestal-scanning logic (used by the Broken Mana Trapezohedron already), and processes MnA-specific items. Regular Vinteum Dust → Befouled Vinteum Dust. Chimerite → Blood-Infused Chimerite. The pedestal interaction model is MnA's.
- **Function:** Place MnA items, feed blood (right-click with blood gourd or drain from player), wait for corruption timer. Uses MnA's pedestal rendering with a blood-tinted shader.

#### 7b. Hemomantic Ley Line Node
- **Concept:** A block that generates a zone where both MnA spell power and Hemomancy manipulation power are enhanced. Crafted from Broken Mana Trapezohedron + Mote of Blood + Engrams.
- **MnA Justification:** The enhancement applies to MnA spells (via MnA's spell damage modifier hooks) AND Hemomancy manipulations simultaneously. The node scans for nearby MnA pedestals (like the Trapezohedron already does) and uses them as amplifiers. Without MnA, this block would only affect manipulations — the spell power boost is the cross-mod value.
- **Effect:** +15% spell power and +15% manipulation potency within 16 blocks. Each nearby pedestal with a Mote of Blood adds +3%.

### 8. New Entities / Companions (MnA Companion Systems)

#### 8a. Blood Construct
- **Concept:** A Hemomancy-themed MnA Construct (`Construct` entity) that uses blood as fuel instead of mana.
- **MnA Justification:** MnA Constructs are MnA's modular animated companion system with capabilities, AI tasks, and fuel systems. A blood-fueled variant extends MnA's `Construct` class but replaces the mana fuel mechanic with Hemomancy's blood volume. It uses MnA's construct workbench for configuration and capability assignment, MnA's construct AI for behavior, but blood for power. This cannot exist without MnA's entire construct framework.
- **Unique Mechanics:**
  - Fueled by blood (player right-clicks to donate blood, or construct drains defeated enemies)
  - Can equip Hemomancy morphlings for passive effects in combat
  - Compatible with all standard MnA construct capabilities PLUS the new blood-themed ones (4c)
  - Built at MnA's Construct Workbench using Hemomancy materials

### 9. Quality-of-Life & Polish

#### 9a. Harbinger Mana HUD Fix
- **Concept:** The HarbingersMana GUI currently has a TODO noting the texture isn't loading properly. Fix this so the resource bar matches the Harbinger blood-red theme.
- **MnA Justification:** This is an existing bug in MnA-specific code. `HarbingersManaGui.getTexture()` returns a Hemomancy texture but MnA isn't loading it correctly.
- **Implementation:** Debug the texture loading path and ensure `harbingers_resource_bars.png` is properly registered with MnA's `CastingResourceGuiRegistry`.

#### 9b. Occulus Task Integration
- **Concept:** The `HarbingersFaction.getOcculusTaskPrompt()` currently returns a generic translatable key. Replace with Hemomancy-specific tasks using MnA's Occulus task system.
- **MnA Justification:** The Occulus is MnA's progression UI. This is purely MnA faction integration work.

#### 9c. Cross-Mod Advancements
- **Concept:** Add advancements that trigger when players complete milestones requiring both mods:
  - "Blood Pact" — Join the Harbingers faction (MnA faction system)
  - "Arcane Hemomancer" — Cast a spell (MnA) using 3+ blood components simultaneously
  - "Living Wardrobe" — Equip full Living Thread armor (Runic Anvil crafted)
  - "Dual Practitioner" — Have 100+ max mana (MnA) while at Initiatory Degree 3+ (Hemomancy)
  - "Convergence" — Cast a spell and use a manipulation within 3 seconds of each other
- **MnA Justification:** Each advancement requires checking both MnA and Hemomancy state. They celebrate cross-mod milestones.

#### 9d. JEI Integration for Cross-Mod Recipes
- **Concept:** Add JEI recipe categories for MnA-specific cross-mod recipes:
  - Manaweaving recipes that produce Hemomancy items (Living Infused Thread, etc.)
  - Runic Anvil recipes for Living Thread armor
  - Blood ↔ Mana conversion ratios (spell component / manipulation)
  - Runeforging recipes for blood construct capabilities
- **MnA Justification:** These are MnA crafting systems. JEI categories help players discover cross-mod recipes that use MnA infrastructure.

#### 9e. Config Options ✅ IMPLEMENTED
- **Status:** Implemented in `config/HemoMnAConfig.java`. Registered as a Forge server config when MnA is present. Covers: blood↔mana conversion ratios, Blood Tithe settings, Living Thread armor set bonus values, Trapezohedron effect radius, Spell↔Manipulation combo settings (enable/disable, durations, reduction percentages), and Sanguilith summon tuning.
- **Concept:** Add server config options for all cross-mod mechanics:
  - Blood ↔ Mana conversion ratios (spell & manipulation)
  - Living Thread armor set bonus values
  - Broken Mana Trapezohedron effect radii
  - Blood Tithe modifier ratios
  - Tendency ↔ Affinity mapping bonuses
  - Ley Line Node boost percentages
- **MnA Justification:** All of these tune mechanics that bridge both mod systems.

### 10. Wild / Ambitious Ideas

#### 10a. Blood Affinity Full Progression Track
- **Concept:** Currently Blood exists as an MnA affinity (`Affinity.BLOOD`). Expand this so Blood affinity has a full progression track in the Occulus with unique tiers, unlockables, and Hemomancy-specific rewards at each tier.
- **MnA Justification:** The Occulus progression system is entirely MnA's. Adding a fleshed-out Blood affinity track means Hemomancy content unlocks through MnA's progression framework — new spell components at tier 2, Blood Tithe modifier at tier 3, enhanced Sanguilith at tier 4, etc.

#### 10b. Hemomantic Enchantments via Runeforging
- **Concept:** Add Hemomancy-themed enchantments applied exclusively through MnA's runeforging system (not vanilla enchanting table):
  - **Sanguine Edge** — Melee attacks drain blood from targets and feed the attacker's blood pool
  - **Crimson Ward** — Armor piece reduces Blood Tithe modifier's blood cost by 10% per piece
  - **Hemolytic Barbs** — When hit, attacker receives Blood Loss effect
- **MnA Justification:** Runeforging is MnA's enchantment system — these enchantments can only be applied through MnA's runeforging anvil with MnA-specific materials (rune patterns + cross-mod reagents). They're not available through any vanilla or base-Hemomancy system.

#### 10c. Dual-Resource Spell System
- **Concept:** Spells crafted specifically in the Harbinger grimoire can be configured to "dual fuel" — they consume both mana and blood simultaneously, but at 60% of each individual cost and with +25% spell power.
- **MnA Justification:** The Harbinger grimoire is already a faction-specific MnA item. This adds a unique faction perk that modifies how MnA's spell cost system works specifically for Harbinger grimoire users. It reads both MnA casting resource and Hemomancy blood volume during spell resolution.

#### 10d. Construct Familiar System
- **Concept:** A Harbinger-exclusive upgrade to MnA's Construct system where the Blood Construct (8a) becomes a "familiar" that shares a soul-bond with the player. The familiar gains XP from the player's Hemomancy activities and unlocks construct capabilities automatically based on the player's blood tendency progression.
- **MnA Justification:** Extends MnA's Construct progression system with a Hemomancy-driven advancement path. The familiar's capabilities are determined by both MnA construct workbench configuration AND Hemomancy tendency levels. Deep integration of both mod's progression systems.

---

## Features Intentionally NOT Included

The following ideas were considered and rejected because they don't genuinely require MnA:

| Rejected Idea | Why It Belongs in Base Hemomancy |
|---------------|----------------------------------|
| Erythromycelium spread ritual | Fungal spreading is a core Hemomancy mechanic. It should work without MnA. |
| Blood gourd capacity upgrades | Blood gourd progression should be base Hemomancy content. |
| New Hemomancy effects/potions | Effects are Hemomancy's system. Adding them as MnA spell components (section 1) is different from adding the effects themselves. |
| Morphling incubation speed boosts | Morphling system is entirely base Hemomancy. |
| Blood dimension / plane | A dimension should be Hemomancy core content. If MnA integration is desired, do it via portal-access gating. |
| Initiatory degree advancement via ritual | Degree advancement via cardinal rites is core Hemomancy progression. MnA rituals should grant bonuses that complement degrees, not replace the progression path. |
| Enhanced blood manipulation power | Manipulation power scaling is base Hemomancy. Cross-mod should grant small bonuses (section 6), not be the primary upgrade path. |

---

## Priority Recommendations

Based on implementation difficulty and gameplay impact, here's a suggested priority order:

### High Priority (Quick wins, high impact)
1. **9a. Harbinger Mana HUD Fix** — Bug fix, small code change
2. ~~**1b–1d. New PotionEffectComponents** (Blood Loss, Blood Rush, Hemolysis)~~ ✅ Done
3. **9b. Occulus Task Integration** — Small code change in HarbingersFaction
4. ~~**9e. Config Options**~~ ✅ Done — `HemoMnAConfig.java`
5. **9c. Cross-Mod Advancements** — JSON-only, no code needed

### Medium Priority (Moderate effort, great synergy)
6. ~~**1a. ComponentBloodToMana**~~ ✅ Done
7. ~~**1e. ComponentSummonSanguilith**~~ ✅ Done
8. ~~**6c. Blood Tithe Modifier**~~ ✅ Done — implemented as event handler in `BloodTitheHandler`
9. **5d. Harbinger Manaweaving Recipes** — JSON recipe definitions using MnA infrastructure
10. ~~**6d. Spell → Manipulation Combos**~~ ✅ Done — `ManipComboHelper` + `BloodTitheHandler`
11. **9d. JEI Integration** — Follows existing JEI pattern

### Lower Priority (Ambitious, high effort)
12. **2a–2b. New Spell Shapes** — More complex MnA API work
13. **3a–3c. New MnA Rituals** — Each genuinely bridges both systems
14. **4a–4d. New Cross-Mod Items** — Requires MnA crafting integration + models/textures
15. **5a–5c. Faction Enhancements** — Significant MnA integration work
16. **6a–6b. Cross-System Synergy** — Deep integration of affinity + tendency systems

### Stretch Goals (Long-term)
17. **7a–7b. MnA-Extended Blocks** — Pedestal variant, Ley Line Node
18. **8a. Blood Construct** — Full construct variant, major effort
19. **10a–10d. Wild Ideas** — Major API work, deep integration

---

## Implementation Notes

### Patterns to Follow
- **PotionEffect spell components** follow `ComponentBloodLoss.java` / `ComponentBloodRush.java` / `ComponentHemolysis.java` — extend `PotionEffectComponent`, pass effect supplier + attribute pairs (DURATION, MAGNITUDE), override `SoundEffect()`, `getAffinity()` (return `Affinity.BLOOD`), `SpawnParticles()`, `getRequiredXPForRote()`, `getComponentTags()`. Very minimal code per component.
- **Custom logic spell components** follow `ComponentBloodToMana.java` — extend `SpellEffect` directly, override `applyEffect()` with full custom logic. Use `MAGNITUDE` attribute for scaling.
- **Summon spell components** follow `ComponentSummonSanguilith.java` — extend `SpellEffect`, use `SummonUtils` for entity spawning, gate behind faction check via `HarbingerEventHandler.isPlayerHarbinger()`.
- **Cross-system event handlers** follow `BloodTitheHandler.java` — use `@SubscribeEvent` on Forge event bus, hook into MnA events like `CalculatingManaCostEvent` and `SpellCastEvent`. Register on the Forge bus when MnA is loaded.
- **Manipulation-side combo integration** follows `ManipComboHelper.java` — called from `BloodManipulation.performAction()` when MnA is loaded. Keep in `compat/mna/spell/` package to avoid class-loading issues.
- **Cross-mod config** follows `HemoMnAConfig.java` — separate `ForgeConfigSpec` registered only when MnA is present.
- **New spell shapes** extend MnA's `SpellShape` API — study MnA's existing shapes for patterns.
- **New spell modifiers** extend MnA's modifier system — study existing modifiers for the pattern.
- **New items** go in `MnAPluginItemInit.java` using the `MNAITEMS` DeferredRegister.
- **Registration** happens in `Hemomancy.java` constructor (already has conditional MnA loading).
- **Spell registration** via `MnAPluginSpellInit.registerSpellBits()`.
- **Ritual registration** via `MnAPluginRitualInit.registerRitualEffects()`.
- **Component recipe JSONs** go in `data/hemomancy/recipes/components/` — one JSON per component.
- **Construct capabilities** register through MnA's `ConstructCapability` system.
- **Runeforging recipes** use MnA's runeforging recipe format.
- **Manaweaving recipes** use MnA's manaweaving recipe format.
- **Guidebook entries** go in `src/main/resources/assets/hemomancy/mna_guide/en_us.json`.
- **Animated icons** use `HemoSpellIconCompositor.borderedIcon()` for bloody border effect.
- **Lang keys** must be added to `en_us.json` for all new items/effects.

### Textures Needed
Spell component icons (16×16) are stored in `textures/mna/`. Already created: `blood_loss.png`, `blood_rush.png`, `hemolysis.png`, `summon_sanguilith.png`. The HemoSpellIconCompositor will auto-generate the animated bloody border overlay. Future components still need icons for: spell shapes, any additional components.

### Testing Considerations
- MnA must be present as a dependency (build.gradle already has it)
- All new components need null-safety checks for capabilities
- Blood volume interactions need `isActive()` / `isFull()` / `wouldOverstrain()` checks
- Cross-mod tick events should be gated behind MnA loaded check
- Blood Tithe handler and combo system tested via `CalculatingManaCostEvent` / `SpellCastEvent` hooks — verify event firing order
- Arcane Resonance and Sanguine Clarity effects must be consumed (removed) after use to prevent stacking
- Sanguilith summon component should respect `HemoMnAConfig.SANGUILITH_MAX_SUMMONS` to prevent spam
- Construct capabilities need testing with MnA's construct workbench
- Runeforging/Manaweaving recipes need testing in MnA's crafting UIs
