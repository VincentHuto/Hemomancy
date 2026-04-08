# Hemomancy × Mana and Artifice — Compatibility Brainstorm

> **Date:** 2026-04-08
> **Status:** Brainstorming / Feature Planning
> **Goal:** Make playing with both Hemomancy and Mana and Artifice (MnA) together more fun, synergistic, and rewarding.

---

## Current Integration Summary

Before brainstorming new features, here is what already exists:

| Category | What Exists |
|----------|-------------|
| **Faction** | The Harbingers — custom faction with blood-red manaweave, Mark of Blood token, Tome of the Impending End grimoire, Horn of the Impending End, custom HarbingersMana casting resource |
| **Spell Components** | Blood Binding (root), Sanguine Transmutation (mana→blood), Sanguine Fertility (breeding buff) |
| **Manipulations** | Sanguine Transfusion (blood→mana, inverse of Sanguine Transmutation) |
| **Rituals** | Ritual of the Weeping Wound (drains 1000 blood → Mote of Blood) |
| **Items** | Befouled Vinteum Dust, Foul Vinteum Ingot, Mana Infused Memory Blank, Living Infused Thread, Mote of Blood, Blood Shot Occulus, Living Thread Armor (4 pieces) |
| **Armor** | Living Thread set (Hood, Robes, Leggings, Boots) — dyeable, mana-repairable, 3pc set bonus: +500 max mana, +50% mana regen |
| **Crafting** | Runic Anvil: Living Infused Thread + Mage Armor → Living Thread armor pieces |
| **Block/Tile** | Broken Mana Trapezohedron — provides mana regen aura, recharges constructs, charges pedestal items, charges sigils |
| **Entity** | Sanguilith — blood-themed summoned monster, uses MnA summon/target utilities |
| **Guidebook** | 12 MnA guidebook entries covering all cross-mod content |
| **VFX** | Animated bloody border overlay on Hemomancy spell icons (HemoSpellIconCompositor) |

---

## New Feature Ideas

### 1. New Spell Components

#### 1a. `ComponentBloodToMana` — "Sanguine Offering"
- **Concept:** The inverse of `ComponentManaToBlood`. Drains the target's (or caster's) Hemomancy blood volume and converts it into mana for the caster.
- **Why:** Currently the mana↔blood conversion only goes one direction as a spell component. Having both directions as spell components (not just manipulation + spell) makes the system more flexible for spell crafting.
- **Attributes:** Magnitude (how much blood to drain, 50–500), Affinity: Blood
- **Tag:** FRIENDLY (self-cast) or HARMFUL (target drain)
- **Interaction:** If cast on an enemy, drains their blood and converts to caster's mana — creates a "mana vampire" playstyle.

#### 1b. `ComponentBloodLoss` — "Hemorrhage"
- **Concept:** Applies the existing Blood Loss effect (movement speed debuff) via the MnA spell system.
- **Why:** Blood Loss is already a Hemomancy effect but has no spell component equivalent. This is a natural addition alongside Blood Binding.
- **Attributes:** Duration (60–300 ticks), Magnitude (1–3), Affinity: Blood
- **Tag:** HARMFUL

#### 1c. `ComponentBloodRush` — "Crimson Surge"
- **Concept:** Applies the Blood Rush effect (+move speed, +attack speed) via spells. Self-buff or team-buff.
- **Why:** Blood Rush is an existing beneficial effect that would be great as a castable buff in MnA spell compositions.
- **Attributes:** Duration (100–600 ticks), Magnitude (1–3), Affinity: Blood
- **Tag:** FRIENDLY

#### 1d. `ComponentHemolysis` — "Blood Destruction"
- **Concept:** Applies Hemolysis effect via spells — a DoT (damage-over-time) that works by destroying the target's blood.
- **Why:** Creates a unique blood-magic themed DoT that interacts with the Hemomancy blood system.
- **Attributes:** Duration (40–200 ticks), Magnitude (1–4), Affinity: Blood
- **Tag:** HARMFUL

#### 1e. `ComponentSummonSanguilith` — "Conjure Sanguilith"
- **Concept:** Summons a Sanguilith entity at the target location via spell casting. The Sanguilith already exists as an entity but currently has no spell component to summon it.
- **Why:** Gives Harbinger faction mages a blood-themed summon comparable to MnA's other summon spells.
- **Attributes:** Duration (200–600 ticks for summon lifetime), Magnitude (damage scaling), Affinity: Blood
- **Tag:** HARMFUL

### 2. New Spell Shapes

#### 2a. `ShapeBloodPulse` — "Sanguine Pulse"
- **Concept:** A short-range AoE burst shape that radiates outward from the caster in a blood-themed wave. Similar to MnA's existing pulse shapes but themed with blood particles.
- **Why:** The commented-out `ShapePorkPulse` in MnAPluginSpellInit suggests a pulse shape was planned. This would be a blood-themed version.
- **Mechanics:** 3–8 block radius, affects all entities in range, costs blood in addition to mana.
- **Visual:** Red blood cell particles radiating outward.

#### 2b. `ShapeBloodLink` — "Hemomantic Tether"  
- **Concept:** A beam-type shape that creates a visible blood tether between caster and target. Spell effects are applied continuously while the tether holds.
- **Why:** Creates a unique channeling mechanic that bridges Hemomancy's continuous manipulation style with MnA's spell system.
- **Mechanics:** Maintains connection up to 12 blocks, breaks on line-of-sight loss or distance.

### 3. New Rituals

#### 3a. Ritual of Sanguine Ascension
- **Concept:** A grand MnA ritual that permanently increases the caster's Hemomancy initiatory degree by 1 (alternative to the cardinal rite system).
- **Why:** Creates a meaningful bridge between MnA's ritual system and Hemomancy's degree progression. Players who invest in both mod systems can advance through a different path.
- **Requirements:** Mote of Blood × 4 on pedestals, Greater Mote of Blood as focus, 2000 blood cost, Harbinger faction membership.
- **Visual:** Chakra-colored blood particles spiraling upward, engrams lighting in sequence.

#### 3b. Ritual of Erythromycelic Bloom
- **Concept:** An MnA ritual that spreads Hemomancy's erythromycelium infection in a radius around the ritual site.
- **Why:** Connects MnA's ritual system with Hemomancy's worldgen/spread mechanic. Useful for players who want to cultivate the fungal infection for morphling farming.
- **Requirements:** Morphling Polyp, Fungal items, blood cost.
- **Output:** Spreads erythromycelium blocks in a configurable radius.

#### 3c. Ritual of the Iron Covenant
- **Concept:** A ritual that creates a "Foul Vinteum Catalyst" — a new crafting material that's required for the highest-tier cross-mod recipes.
- **Why:** Adds a meaningful crafting chain that requires investment in both mod systems.
- **Requirements:** Foul Vinteum Ingots, Hematic Iron Scraps, Mote of Blood.

### 4. New Items & Crafting Materials

#### 4a. Hemomantic Wand Core
- **Concept:** A wand core material craftable from Living Infused Thread + Foul Vinteum Ingot that can be used in MnA's wand crafting system.
- **Why:** Wands are a core MnA item. Having a Hemomancy-themed core material adds cross-mod crafting depth.
- **Effect:** Wands with this core gain +15% Blood affinity spell power, and spells cast with blood affinity components drain slightly less blood.

#### 4b. Sanguine Vinteum Dust
- **Concept:** A refinement of Befouled Vinteum Dust created by processing it through the Blood Centrifuge.
- **Why:** Connects Hemomancy's centrifuge system with MnA materials, creating a cross-mod crafting chain.
- **Uses:** Required ingredient for advanced cross-mod recipes.

#### 4c. Mote of Mana (Inverse of Mote of Blood)
- **Concept:** A crystallized mana essence created by a reverse ritual — sacrificing massive amounts of mana to create a pure mana crystal that can be used in Hemomancy's Visceral Recaller recipes.
- **Why:** Creates a symmetrical item to the Mote of Blood. MnA players can contribute to Hemomancy crafting, and Hemomancy players can contribute to MnA crafting.

#### 4d. Blood-Infused Construct Parts
- **Concept:** New construct capability items for MnA's Construct system that are made with Hemomancy materials.
- **Why:** MnA Constructs are already partially supported (Broken Mana Trapezohedron recharges them). Adding blood-themed construct parts deepens the integration.
- **Ideas:**
  - **Blood Reservoir Module:** Construct stores blood and can heal nearby Hemomancers.
  - **Hemomantic Plating:** Construct gains life-steal on attacks.
  - **Fungal Symbiote Core:** Construct slowly regenerates health, themed with erythromycelium particles.

### 5. Enhanced Faction Features — The Harbingers

#### 5a. Faction Tasks / Advancement Tree
- **Concept:** The Harbingers faction should have unique Occulus tasks that specifically involve Hemomancy activities (using manipulations, performing cardinal rites, achieving certain blood tendencies).
- **Why:** Currently `getOcculusTaskPrompt` returns a generic pact ritual. Custom tasks would make the faction progression feel unique.
- **Examples:**
  - "Drain 10,000 total blood using manipulations"
  - "Reach Initiatory Degree 3"
  - "Perform the Ritual of the Weeping Wound"
  - "Align your blood tendency to FERRIC at level 5+"

#### 5b. Harbinger Faction Raid Enemies
- **Concept:** When the Harbinger faction horn is sounded (or a rival faction attacks), blood-themed enemies should appear — Hemomancy mobs like blood-themed entities, fungal creatures, or even corrupted versions of MnA enemies.
- **Why:** Faction raids are a major MnA feature. Having unique Harbinger raid mobs would make the faction feel more alive.

#### 5c. Harbinger Sanctum Structure
- **Concept:** A custom MnA "sanctum" structure for the Harbingers (currently `getSanctumStructure` returns MnA's council circle). This should be a blood temple-like multiblock.
- **Why:** Every MnA faction has a sanctum. The Harbingers deserve their own, themed with Hemomancy blocks (venous stone, engrams, etc.).

#### 5d. Harbinger-Specific Manaweaving Recipes
- **Concept:** Manaweaving altar recipes that require the Harbinger faction and produce Hemomancy items.
- **Why:** The guidebook already references `manaweaving/living_infused_thread`. More manaweaving recipes would integrate deeper into MnA's crafting system.
- **Ideas:**
  - Manaweave + Blood Gourd → Enhanced Blood Gourd (larger capacity)
  - Manaweave + Living Staff → Arcane Living Staff (cast spells + manipulations)
  - Manaweave + Morphling Jar → Empowered Morphling Jar (holds more morphlings)

### 6. Cross-System Mechanics

#### 6a. Blood Affinity Synergy
- **Concept:** When a player has high Blood affinity in MnA AND high blood tendency levels in Hemomancy, they gain passive bonuses:
  - Reduced mana cost for blood-affinity spells
  - Reduced blood cost for manipulations
  - Chance for spells with blood affinity to not consume blood volume
- **Why:** Rewards players who invest in both systems simultaneously.
- **Implementation:** Check both capabilities in a tick event, apply modifiers.

#### 6b. Tendency ↔ Affinity Mapping
- **Concept:** Map Hemomancy's 8 blood tendencies to MnA's affinities:
  - ANIMUS (Life) → WATER (healing/life)
  - FLAMMEUS (Fire) → FIRE
  - DUCTILIS (Lightning/Speed) → WIND
  - LUX (Light) → ARCANE
  - MORTEM (Death) → ENDER
  - CONGEATIO (Cold/Ice) → ICE
  - FERRIC (Iron/Solidity) → EARTH
  - TENEBRIS (Darkness) → ENDER/BLOOD
- **Why:** This mapping would allow tendency-based bonuses to apply to corresponding MnA spells, creating deep synergy.
- **Effect:** Having a high tendency in X gives +10% power to spells with the corresponding MnA affinity.

#### 6c. Blood Cost as Spell Modifier
- **Concept:** Add a new spell modifier that lets players pay part of a spell's mana cost in blood instead of mana.
- **Why:** This would be a unique Harbinger faction perk — their spells can be fueled by both blood and mana, giving them flexibility.
- **Mechanics:** A "Blood Tithe" modifier that replaces X% of mana cost with blood cost (configurable ratio).

#### 6d. Manipulation → Spell Triggers
- **Concept:** Certain blood manipulations could trigger follow-up spell effects, or vice versa. For example:
  - Casting Blood Binding via manipulation also creates a weak Slow spell sigil.
  - Using Blood Rush manipulation also briefly boosts spell casting speed.
- **Why:** Creates unique combo gameplay that rewards using both systems in combat.

### 7. New Blocks & Structures

#### 7a. Blood-Corrupted Mana Pedestal
- **Concept:** A Hemomancy variant of MnA's pedestal that can hold items and slowly infuses them with blood, converting certain MnA items into Hemomancy variants.
- **Why:** Pedestals are central to MnA's crafting. A blood-corrupted version creates a new crafting pathway.
- **Function:** Place a regular Vinteum Dust → slowly becomes Befouled Vinteum Dust. Place Mage Armor → slowly corrupts into Living Thread Armor base.

#### 7b. Hemomantic Ley Line Node
- **Concept:** A block that generates a "blood ley line" zone — within this zone, both spell power and manipulation power are enhanced.
- **Why:** Creates a territorial control mechanic. Players build their base around these nodes for maximum power.
- **Crafting:** Broken Mana Trapezohedron + Mote of Blood + Engrams.

#### 7c. Arcane Blood Altar
- **Concept:** A multiblock structure that combines MnA's ritual circle mechanics with Hemomancy's cardinal rite system. Allows performing enhanced versions of either mod's rituals.
- **Why:** A true fusion crafting station that requires mastery of both mods.

### 8. New Entities / Companions

#### 8a. Blood Construct
- **Concept:** A Hemomancy-themed MnA Construct variant — an animated blood golem that serves as a companion. Unlike regular constructs that run on mana, this one runs on blood.
- **Why:** Constructs are one of MnA's most beloved features. A blood-themed variant would be iconic.
- **Unique Capabilities:**
  - Drains blood from enemies it defeats to sustain itself
  - Can perform basic blood manipulations
  - Carries a morphling and applies its effects in combat

#### 8b. Empowered Sanguilith
- **Concept:** An upgraded version of the existing Sanguilith that can be summoned when wearing full Living Thread armor. It's larger, deals more damage, and has blood-drain on hit.
- **Why:** Leverages the existing Sanguilith but adds a progression tier.

### 9. Quality-of-Life & Polish

#### 9a. Harbinger Mana HUD Fix
- **Concept:** The HarbingersMana GUI currently has a TODO noting the texture isn't loading properly. Fix this so the resource bar matches the Harbinger blood-red theme.
- **Implementation:** Debug `HarbingersManaGui.getTexture()` and ensure the `harbingers_resource_bars.png` texture is properly loaded and rendered.

#### 9b. Occulus Task Integration
- **Concept:** The `HarbingersFaction.getOcculusTaskPrompt()` currently returns a generic translatable. Replace with Hemomancy-specific tasks.
- **Implementation:** Create actual Hemomancy-themed Occulus task prompts and progression.

#### 9c. Cross-Mod Advancements
- **Concept:** Add advancements that trigger when players complete cross-mod milestones:
  - "Blood Pact" — Join the Harbingers faction
  - "Arcane Hemomancer" — Cast a spell with all 3 blood components (Binding + Transmutation + Fertility)
  - "Living Wardrobe" — Equip full Living Thread armor
  - "Sanguilith Summoner" — Summon your first Sanguilith
  - "Dual Practitioner" — Reach Initiatory Degree 3 while having 100+ max mana

#### 9d. JEI Integration for Cross-Mod Recipes
- **Concept:** Add JEI recipe categories for:
  - Manaweaving recipes that produce Hemomancy items
  - Runic Anvil recipes for Living Thread armor
  - Blood ↔ Mana conversion ratios
- **Why:** Players need to be able to discover cross-mod recipes in JEI.

#### 9e. Config Options
- **Concept:** Add server config options for all cross-mod mechanics:
  - Blood ↔ Mana conversion ratios
  - Living Thread armor set bonus values
  - Broken Mana Trapezohedron effect radii
  - Blood affinity synergy bonus percentages
- **Why:** Modpack authors need to be able to tune cross-mod balance.

### 10. Wild / Ambitious Ideas

#### 10a. Blood Affinity as Full MnA Affinity
- **Concept:** Currently Blood is treated as an MnA affinity via `Affinity.BLOOD`. Expand this so Blood affinity has its own full progression track in the Occulus, with unique tiers and unlockables.
- **Why:** Would make the blood magic path feel like a true equal to MnA's core affinities.

#### 10b. Hemomantic Enchantments via Runeforging
- **Concept:** Add Hemomancy-themed enchantments that can be applied via MnA's runeforging system:
  - **Sanguine Edge** — Melee attacks drain blood from targets
  - **Crimson Ward** — Armor reduces blood cost of manipulations
  - **Hemolytic Barbs** — Reflected damage also applies Blood Loss effect
- **Why:** Runeforging is one of MnA's core crafting systems. Having Hemomancy enchantments available through it creates deep integration.

#### 10c. Blood Dimension / Plane
- **Concept:** A pocket dimension accessible only to Harbinger faction members at high initiatory degree — a realm of pure blood magic where unique resources and enemies exist.
- **Why:** The ultimate endgame content for players who master both mod systems.

#### 10d. Dual-Resource Spell System
- **Concept:** Spells crafted in the Harbinger grimoire could have a "dual fuel" mode where they consume both mana and blood simultaneously, but at reduced individual costs and with boosted power.
- **Why:** Rewards investment in both resource systems with tangible combat power.

---

## Priority Recommendations

Based on implementation difficulty and gameplay impact, here's a suggested priority order:

### High Priority (Quick wins, high impact)
1. **9a. Harbinger Mana HUD Fix** — Bug fix, small code change
2. **1b–1d. New PotionEffectComponents** (Blood Loss, Blood Rush, Hemolysis) — Follow existing ComponentBloodBinding pattern exactly
3. **9c. Cross-Mod Advancements** — JSON-only, no code needed
4. **9e. Config Options** — Uses existing HemoServerConfig pattern
5. **9b. Occulus Task Integration** — Small code change in HarbingersFaction

### Medium Priority (Moderate effort, great synergy)
6. **1a. ComponentBloodToMana** — Follows existing ComponentManaToBlood pattern with reversed logic
7. **1e. ComponentSummonSanguilith** — Sanguilith entity already exists, just needs spell component wrapper
8. **5d. Harbinger Manaweaving Recipes** — JSON recipe definitions
9. **6a. Blood Affinity Synergy** — New tick event handler
10. **9d. JEI Integration** — Follows existing JEI pattern

### Lower Priority (Ambitious, high effort)
11. **2a–2b. New Spell Shapes** — More complex MnA API work
12. **3a–3c. New Rituals** — Moderate complexity
13. **4a–4d. New Items** — New models/textures needed
14. **5a–5c. Faction Enhancements** — Significant MnA integration work
15. **6b–6d. Cross-System Mechanics** — Deep integration

### Stretch Goals (Long-term)
16. **7a–7c. New Blocks/Structures**
17. **8a–8b. New Entities**
18. **10a–10d. Wild Ideas**

---

## Implementation Notes

### Patterns to Follow
- **New spell components** should follow `ComponentBloodBinding.java` (for PotionEffect-based) or `ComponentManaToBlood.java` (for custom logic).
- **New items** go in `MnAPluginItemInit.java` using the `MNAITEMS` DeferredRegister.
- **Registration** happens in `Hemomancy.java` constructor (already has conditional MnA loading).
- **Spell registration** via `MnAPluginSpellInit.registerSpellBits()`.
- **Ritual registration** via `MnAPluginRitualInit.registerRitualEffects()`.
- **Guidebook entries** go in `src/main/resources/assets/hemomancy/mna_guide/en_us.json`.
- **Animated icons** use `HemoSpellIconCompositor.borderedIcon()` for bloody border effect.
- **Lang keys** must be added to `en_us.json` for all new items/effects.

### Textures Needed
All new spell components need 16×16 icons in `textures/mna/`. The HemoSpellIconCompositor will auto-generate the animated bloody border overlay.

### Testing Considerations
- MnA must be present as a dependency (build.gradle already has it)
- All new components need null-safety checks for capabilities
- Blood volume interactions need `isActive()` / `isFull()` / `wouldOverstrain()` checks
- Cross-mod tick events should be gated behind MnA loaded check
