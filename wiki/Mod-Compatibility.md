# Mod Compatibility

Hemomancy integrates with several popular mods to enhance gameplay. All integrations are **optional** — the mod works standalone.

---

## Required Dependencies

### HutosLib
**Version:** 7.3.5+
**Required:** Yes

**What It Provides:**
- Core utility library
- Particle systems
- Dialogue tree framework (used for Item Inquiry)
- Book/codex framework (used for Field Notes)
- Shared code between VincentHuto mods

**Installation:**
- Download from CurseForge or Modrinth
- Place in mods folder alongside Hemomancy

### GeckoLib
**Version:** 4.8.4+
**Required:** Yes

**What It Provides:**
- Entity animation system
- Used for mob animations
- Model rendering support

### TerraBlender
**Version:** 4.1.0.8+
**Required:** Yes

**What It Provides:**
- Biome integration framework
- Allows Hemomancy biomes to generate alongside other mods
- Essential for worldgen

---

## Optional Integrations

### Mana and Artifice

**Status:** Planned, partially implemented (dormant in NeoForge 1.21.1 build)

When both mods are installed, Hemomancy adds deep integration:

#### Harbinger Faction
- Harbingers become a custom MnA faction
- Own faction reputation system
- Faction-specific mana resource bar
- Unique faction benefits

#### Spell-Blood Synergy

**Blood Tithe**
- Pay spell mana costs partially in blood
- Converts blood volume to mana
- Toggleable system
- Efficient for blood-rich builds

**Blood Loss Component**
- Custom spell component
- Damages target's blood volume (if they have capability)
- Harbinger-specific offensive option

**Blood Rush Component**
- Spell component
- Temporarily boosts caster's blood regeneration
- Costs mana to accelerate blood recovery

**Hemolysis Component**
- Ruptures blood cells
- Damage + debuff combo
- Effective against organic targets

**Mana↔Blood Conversion**
- `BloodToMana` component — spend blood, gain mana
- `ManaToBlood` component — spend mana, gain blood
- Two-way resource exchange

#### Sanguilith Summon
- Summon Sanguilith (blood turret) via MnA spell
- Uses MnA summoning framework
- Costs both mana and blood

#### Combo Effects

**Arcane Resonance**
- When casting MnA spell that aligns with your blood tendency
- Spell gets power boost
- Mana cost slightly reduced
- Synergy between magical paths

**Sanguine Clarity**
- When using blood manipulation while mana is full
- Manipulation gets efficiency boost
- Blood cost slightly reduced
- Rewards resource management

#### Configuration
All MnA integration features are tunable in `HemoMnAConfig`:
- Enable/disable individual components
- Adjust conversion rates
- Tune Blood Tithe costs
- Balance combo effects

#### Why This Integration?
Each MnA feature has specific design justification documented in [MNA_COMPATIBILITY_BRAINSTORM.md](https://github.com/VincentHuto/Hemomancy/blob/main/docs/MNA_COMPATIBILITY_BRAINSTORM.md). The integration aims to create meaningful mechanical and narrative synergy, not just "cross-mod recipes."

**Note:** This integration is currently dormant because Mana and Artifice hasn't updated to NeoForge 1.21.1 yet. Code exists but is excluded from build.

---

### Curios API

**Status:** Planned, partially implemented (dormant in NeoForge 1.21.1 build)

When Curios is installed:

#### Charm of Vascularium
- Unique curio item
- Fits in dedicated Curios slot
- Doesn't use armor/accessory slot

**Effects:**
- Passive blood regeneration boost
- Enhanced tendency alignment rate
- Reduced blood loss from damage
- Quality-of-life for blood mages

**Obtaining:**
- Craftable with blood-infused materials
- Rare drop from high-degree Harbinger NPCs
- Reward from certain rituals

**Why Curios?**
Provides a non-armor slot for hemomancy enhancement, freeing up equipment choices while still offering blood-specific benefits.

**Note:** Currently dormant until Curios updates to NeoForge 1.21.1.

---

### Just Enough Items (JEI)

**Status:** Integrated (via local JAR)

Hemomancy provides JEI integration for custom crafting systems:

#### Recipe Categories

**Chisel Station**
- Shows scar carving recipes
- Input: Blank scar + chisel
- Output: Specific scar types
- Visual pattern guides

**Visceral Recaller**
- Shows respawn point recipes
- Input: Blood + materials
- Output: Bound recall point

**Blood Structure**
- Shows multi-block structure recipes
- Input: Components + ritual items
- Output: Functional structure

**Morphling Incubator**
- Shows creature mutation recipes
- Input: Creature + blood + fungal materials
- Output: Morphling type
- Time requirements shown

**Somatic Loom** (Memory Weaving)
- Shows manipulation learning recipes
- Input: Blank memory + enzyme + catalyst
- Output: Hematic memory for specific manipulation
- Complex patterns visualized

#### Usage Tips
- Press `R` on item to see recipes using it
- Press `U` on item to see how to craft it
- Bookmark frequently-used recipes
- JEI search supports tendency names and manipulation names

---

## Modpack Integration Tips

### For Modpack Creators

#### Balance Considerations
- Blood volume is a limited resource; avoid mods that trivialize regeneration
- Tendencies are meant to specialize; avoid mods that grant all tendencies easily
- Degree progression should feel earned; don't add shortcuts
- Fungal biomes are intentionally limited; respect worldgen balance

#### Config Recommendations
- **Multiplayer:** Consider disabling pvp blood drain if not desired
- **Difficulty:** Adjust blood costs and regeneration for pack difficulty
- **Death:** Choose death penalty (full reset, partial, or none) based on pack hardcore level
- **Blood Moons:** Tune frequency to match pack's event density

#### Recipe Integration
Hemomancy supports recipe modification via datapacks:
- Add custom ritual recipes
- Modify manipulation costs
- Change enzyme/catalyst requirements
- Create pack-specific blood items

#### Cross-Mod Synergies

**With Combat Mods:**
- Blood manipulations work well with combat overhaul mods
- Guardian combat style fits tactical combat systems
- Morphlings can integrate with pet/companion mods

**With Magic Mods:**
- Blood tendency system parallels elemental magic systems
- Resource management (blood) complements mana systems
- Ritual systems pair well with other ritual magic mods

**With Exploration Mods:**
- Fungal biomes add to dimension/biome diversity
- Harbinger Outposts are natural dungeon content
- Blood Moon events work with event/adventure mods

**With Tech Mods:**
- Blood routing system can integrate with fluid systems
- Blood machines fit tech mod aesthetics
- Automation potential for blood generation

#### Known Incompatibilities
- **None currently documented** for NeoForge 1.21.1
- Report issues if found

#### Server Performance
- Founding Fanes with many stakes: slight TPS impact
- Blood routing networks: negligible impact
- Morphling incubators: minimal impact
- Large rituals: temporary particle load

---

## Planned Future Integrations

These are being considered for future updates:

### Create Mod
- Blood as fluid in Create systems
- Mechanical blood processing
- Automation of blood generation
- Rotation-powered rituals

**Status:** Dev-time dependency only; no runtime integration yet

### Botania
- Mana↔Blood conversion systems
- Blood flowers that generate mana
- Runic altar blood rituals
- Cross-mod aesthetic synergy

**Status:** Design phase

### Ars Nouveau
- Blood-based spell crafting
- Tendency-spell alignments
- Glyph system integration

**Status:** Concept stage

---

## Compatibility Testing

### Verified Compatible With:
*(To be updated as community tests)*
- NeoForge 21.1.x baseline mods
- Most worldgen mods (via TerraBlender)
- Most combat mods
- Most GUI/HUD mods

### Potential Issues:
- Mods that drastically change player capabilities may need config tuning
- Mods that replace health systems may conflict with blood volume
- Mods that override death mechanics may need coordination

**Report compatibility issues on [GitHub Issues](https://github.com/VincentHuto/Hemomancy/issues)**

---

## Developer Integration

### For Mod Developers

Want to integrate your mod with Hemomancy?

#### Capability Access
Hemomancy uses NeoForge attachments/capabilities:
```java
// Access blood volume
IBloodVolume volume = player.getData(HemoAttachmentTypes.BLOOD_VOLUME);

// Check if player has active blood
if (volume.isActive()) {
    float current = volume.getBloodLevel();
    volume.setBloodLevel(current - cost);
}
```

#### API (Planned)
A formal API is planned for future versions. Until then, capabilities are the integration point.

#### Documentation
See [HEMOMANCY_REFERENCE.md](https://github.com/VincentHuto/Hemomancy/blob/main/docs/HEMOMANCY_REFERENCE.md) for technical details on:
- Capability system
- Packet structure
- Registry objects
- Event hooks

#### Contact
- **GitHub:** [VincentHuto/Hemomancy](https://github.com/VincentHuto/Hemomancy)
- **Issues:** For integration questions or requests

---

## Configuration Files

Hemomancy provides extensive config options:

### File Locations
- **Server Config:** `serverconfig/hemomancy-server.toml`
- **Client Config:** `clientconfig/hemomancy-client.toml`
- **Common Config:** `defaultconfigs/hemomancy-common.toml`
- **MnA Config:** `serverconfig/hemomancy-mna.toml` (when MnA present)

### Key Options

#### Server Config
- Blood volume settings (starting, max, regeneration)
- Manipulation costs and cooldowns
- Degree requirements
- Death penalties
- PvP blood drain
- Ritual costs
- NPC recruitment limits
- Blood moon frequency

#### Client Config
- HUD position and visibility
- Particle density
- Sound volume
- Screen effects intensity
- GUI scaling
- Keybinds

#### Common Config
- Worldgen toggles (biomes, structures)
- Feature generation rates
- Mob spawn rates
- Loot table modifications

#### MnA Config (when available)
- Blood Tithe conversion rates
- Component costs
- Faction reputation gains
- Combo effect strengths
- Enable/disable specific integrations

### Config Changes
- Most changes apply immediately
- Some require world restart
- Server configs sync to clients automatically

---

## Next Steps

- **[[Getting Started]]** — Install and set up Hemomancy
- **[[Developer Reference]]** — Technical documentation for integration
- **[[Home]]** — Return to main wiki page

---

*"Magic is not isolated. Blood flows through all things. The connections between systems — be they biological, mechanical, or mystical — reveal deeper truths about the nature of power."*

— From cross-mod design philosophy notes
