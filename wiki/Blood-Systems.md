# Blood Systems

Use this page as the mechanics reference: blood volume, manipulations, tendencies, vascular state, skills, and routing.

---

## Blood Volume

**Blood Volume** is the resource every blood-magic action spends.

### Key Stats
- **Starting Volume:** 1000 mL (after Mortal Display activation)
- **Human Maximum:** ~5000 mL (natural)
- **Extended Maximum:** `5000 + Capacity + Spleen + Eternal Covenant - scars`
- **Average Adult:** 5000 mL

### Maximum Blood Diagnostics

Player maximum blood is resolved server-side by the max-blood ledger. Capacity, Spleen, and Eternal Covenant stack additively, then scar penalties subtract from the final bonus total. The Scrying Podium **Blood** tab shows the computed maximum, net modifier, and exact positive/negative modifier totals; hovering `Mods` lists the source of each modifier.

### Regeneration
- **Base Rate:** Slow passive regeneration over time
- **Enhanced by:**
  - Animus-aligned manipulations
  - Blood Lust armor
  - Founding Fane buffs
  - Charm of Vascularium through Hemomancy's current equipment surfaces
  - Certain foods and potions
- **Reduced by:**
  - Combat damage
  - Overuse of manipulations
  - Certain status effects
  - Environmental hazards

### Blood Flow Diagnostics

The **Scrying Podium** shows server-truth blood flow in `mL/t` rather than estimating regeneration from circulation bandwidth. Its **Blood** tab displays Blood Volume, Vascular Health, positive flow, negative flow, net flow, circulation bandwidth used/cap/available, and source rows with applied/requested rates. Hovering the Blood Flow modifier area opens the full active source breakdown.

Player/body sources such as base regeneration, **Sanguine Surge**, **Last Wind**, and **Mnemonic Candle Aura** stack directly. External passive income such as armor set regeneration, Sanguine Siphon, fungal scar income, Qliphoth Bloom, Morphling Cradle feed, and Open Blood Gourd transfer shares the circulation bandwidth cap where applicable. Active costs, kill rewards, weapon hits, manual draws, and structure feeding are not treated as passive `mL/t` flow.

### Death and Volume
- Configurable: volume may reset on death
- Can be set to partial loss or full reset
- Death changes how much blood you have available.

---

## Blood Manipulations

**Manipulations** are the abilities you trigger, charge, toggle, or keep active with blood.

### Manipulation Types

**QUICK**
- Instant cast
- Single action
- Short cooldown
- Example: Blood Bolt (projectile attack)

**CHARGED**
- Hold to charge
- More powerful when fully charged
- Longer cooldown
- Example: Crimson Lance (charged spear throw)

**PASSIVE**
- Always active once learned
- Constant low cost or free
- Background benefits
- Example: Sanguine Regeneration (constant healing)

**CONTINUOUS**
- Toggle on/off
- Drains blood while active
- Sustained effects
- Example: Blood Barrier (active shield)

### Manipulation Ranks

Manipulations are organized into four power tiers:

**HUMILIS** (Humble)
- Degree 1 (Neophyte) access
- Low blood cost (100-500 mL)
- Basic effects
- Foundation of blood magic

**MEDIOCRITAS** (Moderate)
- Degree 2 (Illuminatus) access
- Medium blood cost (500-1500 mL)
- Enhanced effects
- Specialized applications

**SUMMA** (Superior)
- Degree 3 (Initiate) access
- High blood cost (1500-3500 mL)
- Powerful effects
- Tactical importance

**PERFECTUS** (Perfect)
- Degree 4 (Adept) and higher
- Very high blood cost (3500-7000 mL)
- Elite effects
- Game-changing abilities

### Learning Manipulations

**Method 1: Somatic Loom (Memory Weaving)**
1. Craft **Blank Hematic Memory**
2. Gather tendency-specific **Enzyme** (Vivacious, Fervent, Neurotic, etc.)
3. Collect **Catalyst** materials for the manipulation
4. Place items in **Somatic Loom** following pattern
5. Project blood into the Loom
6. Wrestle colored memory-orbs into shape
7. Receive completed **Hematic Memory** item
8. Use Memory to permanently learn manipulation

**Method 2: Field Notes Discovery**
- Some manipulations can be discovered through gameplay
- Experimentation with blood tendencies
- NPC teachings
- Rare drops from infected creatures

**Method 3: Bloodline Teaching**
- High-degree Harbingers can teach lower-degree members
- Requires ritual at Cardinal Altar
- Costs blood from both parties

### Using Manipulations

**Selection:**
- Press `V` (default) to open manipulation wheel
- Move mouse to desired manipulation
- Release to select

**Activation:**
- Depends on manipulation type
- Quick: Single press of manipulation key (`R` default)
- Charged: Hold manipulation key until charged
- Passive: Automatically active
- Continuous: Toggle with manipulation key

**Management:**
- Check Field Notes (`B`) for known manipulations
- Organize by tendency or frequency of use
- Monitor cooldowns via HUD

### Manipulation Diagnostics

The **Scrying Podium** **Manipulations** tab is synced from the server. It shows how many manipulations you know, how many are currently equipped, the current equipped-slot cap, and the selected manipulation's base blood cost versus effective blood cost.

Hover the equipped-slot modifier row to see the active slot formula: `min(9, 3 + degree / 2 + Manip Slots skill)`. Hover the selected manipulation's cost modifier row to see each active discount or surcharge, including skills, manipulation mastery, tendency-match bonuses, purity penalties, potion effects, world effects, rite effects, and equipped tools.

### Synaptic Loadouts

At Degree 5, the **Dendritic Distributor** becomes a Synaptic Loadout station. It stores whole manipulation arrangements as named neural patterns:

- 3 remembered patterns by default
- `skill_synaptic_memory` adds 1 pattern per level, up to 7 total
- Saving or overwriting costs 100 blood and 25 raw XP
- Applying and renaming are free
- Fixed mechanical utilities such as blood absorption and blood projection stay automatic and are not saved inside patterns

Use these patterns for combat, travel, rituals, and fane work. You do not need to relearn the manipulations.

---

## The Eight Tendencies

**Tendency** (also called **Kinship**) represents the quality or affinity of your blood. Each tendency unlocks different manipulations and playstyles.

The **Scrying Podium** **Tendency** tab shows your dominant and latent tendencies, a full tendency-value profile, and rite readiness.

### Animus (Life/Vitality)

**Theme:** Healing, regeneration, vitality, growth

**Color:** Crimson-red with warm undertones

**Associated Enzyme:** Vivacious Enzyme

**Philosophy:** Blood is life itself. Master Animus to sustain yourself and others.

**Key Manipulations:**
- **Sanguine Touch** (Humilis): Heal self or others with touch
- **Vital Surge** (Mediocritas): Temporary health and regeneration boost
- **Life Ward** (Summa): Create healing aura around you
- **Phoenix Blood** (Perfectus): Auto-revive from lethal damage once

**Playstyle:** Support, sustain, endurance

**Synergies:**
- Pairs well with tank builds
- Essential for group play
- Countered by Mortem tendency

---

### Flammeus (Fire/Heat)

**Theme:** Combustion, heat, burning, explosive power

**Color:** Orange-red with flickering highlights

**Associated Enzyme:** Fervent Enzyme

**Philosophy:** Blood burns hot. Ignite it and unleash destruction.

**Key Manipulations:**
- **Ember Touch** (Humilis): Set touch target on fire
- **Flame Bolt** (Mediocritas): Projectile that ignites
- **Conflagration** (Summa): Area-of-effect explosion
- **Solar Flare** (Perfectus): Massive firestorm centered on self

**Playstyle:** Offensive, area damage, crowd control

**Synergies:**
- High damage output
- Environmental destruction
- Countered by Congeatio tendency

---

### Ductilis (Lightning / Nerves)

**Theme:** Iron manipulation, weaponry, metallurgy

**Color:** Silver-gray with metallic sheen

**Associated Enzyme:** Neurotic Enzyme

**Philosophy:** Blood contains iron. Control it, shape it, weaponize it.

**Key Manipulations:**
- **Iron Dart** (Humilis): Launch iron projectile from blood
- **Ferrous Shield** (Mediocritas): Summon iron barrier
- **Blade Storm** (Summa): Create whirling iron weapons
- **Arsenal** (Perfectus): Summon multiple iron constructs

**Playstyle:** Versatile, offense and defense, tactical

**Synergies:**
- Balanced combat approach
- Works with Ferric tendency
- Material-based crafting synergy

---

### Lux (Light/Radiance)

**Theme:** Illumination, purification, holy power

**Color:** Bright crimson with gold highlights

**Associated Enzyme:** Incandescent Enzyme

**Philosophy:** Blood can shine. Let it illuminate the darkness.

**Key Manipulations:**
- **Radiant Pulse** (Humilis): Flash of light, blind enemies
- **Beacon** (Mediocritas): Create floating light source
- **Purifying Ray** (Summa): Beam that damages undead/corrupted
- **Sanctified Blood** (Perfectus): Temporary immunity to darkness

**Playstyle:** Support, anti-undead, utility

**Synergies:**
- Strong against undead and corrupted
- Vision control
- Countered by Tenebris tendency

**Current implemented combat notes:** Lux now has `hematic_flare` as a Humilis true-offense ray: it deals magic damage, applies Glowing, strips Invisibility, and hits concealed enemies harder. `prismatic_reproof` remains the larger cone follow-up, but now always deals base magic damage and doubles its bite against Glowing targets. Lux attacks dynamically oppose Tenebris-school Rogue Wills by their synced Will school.

---

### Mortem (Death/Decay)

**Theme:** Necrosis, withering, death magic

**Color:** Dark crimson with black veins

**Associated Enzyme:** Ruinous Enzyme

**Philosophy:** Blood knows death intimately. Wield that knowledge.

**Key Manipulations:**
- **Wither Touch** (Humilis): Inflict wither effect
- **Necrotic Bolt** (Mediocritas): Projectile that decays
- **Plague Aura** (Summa): Area debuff, damages and weakens
- **Death's Embrace** (Perfectus): Instant kill on low-health target

**Playstyle:** Debuff, damage-over-time, execution

**Synergies:**
- Strong against living creatures
- Combos with damage builds
- Countered by Animus tendency

---

### Congeatio (Ice/Cold)

**Theme:** Freezing, preservation, cold magic

**Color:** Pale blue-red with frost

**Associated Enzyme:** Frigid Enzyme

**Philosophy:** Blood can be frozen, preserved, weaponized as ice.

**Key Manipulations:**
- **Frost Touch** (Humilis): Slow and damage with cold
- **Ice Shard** (Mediocritas): Frozen projectile
- **Glacial Armor** (Summa): Temporary ice shell, high defense
- **Absolute Zero** (Perfectus): Freeze everything in radius

**Playstyle:** Control, defense, slow

**Synergies:**
- Excellent crowd control
- Defensive builds
- Countered by Flammeus tendency

---

### Ferric (Magnetic/Ferrous)

**Theme:** Magnetism, attraction/repulsion, iron control

**Color:** Dark metallic red with magnetic distortion

**Associated Enzyme:** Ferric Enzyme (same name as tendency)

**Philosophy:** Blood iron responds to magnetic force. Command it.

**Key Manipulations:**
- **Magnetic Pull** (Humilis): Draw items/enemies toward you
- **Repulsion Field** (Mediocritas): Push enemies away
- **Ferro-kinesis** (Summa): Control metal objects at range
- **Singularity** (Perfectus): Create magnetic vortex

**Playstyle:** Utility, positioning, environmental control

**Synergies:**
- Works with Ductilis
- Strong battlefield control
- Unique utility options

---

### Tenebris (Shadow/Darkness)

**Theme:** Concealment, stealth, shadow magic

**Color:** Black-red with shadowy wisps

**Associated Enzyme:** Umbral Enzyme

**Philosophy:** Blood flows in darkness. Embrace the shadow.

**Key Manipulations:**
- **Shadow Step** (Humilis): Short-range teleport
- **Veil of Night** (Mediocritas): Invisibility for short time
- **Umbral Chains** (Summa): Bind enemy in shadow
- **Eclipse** (Perfectus): Create area of absolute darkness

**Playstyle:** Stealth, mobility, ambush

**Synergies:**
- Rogue/stealth builds
- Escape and infiltration
- Countered by Lux tendency

**Current implemented combat notes:** Tenebris now has `gloam_laceration` as a Humilis ambush slash: it applies Blood Loss and Weakness, deals magic damage, hits harder while the caster is invisible or standing in natural/synthetic darkness, and uses a three-line tapered claw ribbon visual instead of generic glow motes. `void_shroud`, `black_veil_covenant`, `umbral_step`, and `blood_eclipse` remain the core stealth/darkness chain. Tenebris attacks dynamically oppose Lux-school Rogue Wills by their synced Will school.

---

## Tendency Alignment

**Alignment** measures how attuned your blood is to each tendency. Higher alignment = more powerful manipulations of that tendency.

### Building Alignment

**Method 1: Use Manipulations**
- Using a manipulation increases alignment with its tendency
- More powerful manipulations give more alignment
- Passive gain over time with frequent use

**Method 2: Consume Enzymes**
- Each enzyme corresponds to a tendency
- Drinking/using enzyme directly boosts alignment
- Quick but resource-intensive

**Method 3: Environmental Exposure**
- Certain biomes/areas boost specific tendencies
- Fungal areas boost Mortem and Tenebris
- Bright areas boost Lux
- Cold areas boost Congeatio

**Method 4: Equipment**
- Armor and items can boost tendency alignment
- Scars provide passive tendency bonuses
- Founding Fane can be tuned to specific tendencies

### Alignment Levels

- **0-25:** Novice: Basic effectiveness
- **26-50:** Practiced: Improved effectiveness
- **51-75:** Aligned: Strong effectiveness
- **76-99:** Mastered: Very strong effectiveness
- **100:** Perfected: Maximum effectiveness

### Specialization vs Generalization

**Specialist Build:**
- Focus on 1-2 tendencies
- Max alignment in chosen tendencies
- Very powerful in niche
- Limited versatility

**Generalist Build:**
- Spread across 3-4 tendencies
- Medium alignment in all
- Versatile toolkit
- Less specialized power

**Recommendation:** Start as a generalist. Specialize after you know which tendencies and manipulations you actually use.

---

## Vascular System

The **Vascular System** represents your blood's internal structure and efficiency.

### Vein Sections

Manipulations are organized into **vein sections**: metaphorical regions of your circulatory system:

- **Heart Section**: Core vitality and power (Animus, Mortem)
- **Artery Section**: Offensive and projection (Flammeus, Lux)
- **Capillary Section**: Utility and manipulation (Ductilis, Ferric)
- **Vein Section**: Defensive and control (Congeatio, Tenebris)

### Vascular Health

- **Damaged Vascular System**: Reduces manipulation effectiveness
- **Healthy Vascular System**: Normal operation
- **Enhanced Vascular System**: Improved manipulation power

**Maintain health by:**
- Avoiding overuse of manipulations
- Using Animus healing
- Vascular Mending rite (Cardinal Altar)
- Proper rest and recovery

---

## Skill Tree

The **Skill Tree** provides passive bonuses and unlocks. See the full tree in Field Notes.

### Key Skills

**Blood Efficiency Branch**
- Reduce manipulation costs
- Increase blood regeneration
- Increase volume capacity

**Tendency Mastery Branch**
- Boost specific tendency alignments
- Unlock advanced manipulation variants
- Cross-tendency synergies

**Combat Branch**
- Increase damage of offensive manipulations
- Reduce cooldowns
- Improve targeting

**Defense Branch**
- Strengthen blood barriers and shields
- Reduce incoming damage
- Improve healing effectiveness

**Utility Branch**
- Extend manipulation durations
- Increase range
- Multi-target capabilities

**Scar Branch**
- **Scar Affinity**: Reduce scar application pain
- **Scar Resonance**: Scars boost manipulations more
- **Scar Mastery**: Extend scar-focused progression and mastery behavior

### Skill Points

**Earning Points:**
- Degree advancement
- Completing rituals
- Discovering new manipulations
- Achievements and milestones

**Spending Points:**
- Open Field Notes skill tree
- Select desired skill
- Confirm purchase (irreversible)
- Skill activates immediately

---

## Advanced Techniques

### Manipulation Combos

Combine manipulations for enhanced effects:

**Flame + Iron = Molten Projectiles**
- Use Ductilis to create iron
- Apply Flammeus to superheat it
- Launch as devastating projectile

**Ice + Shadow = Frozen Ambush**
- Use Tenebris to conceal
- Use Congeatio to freeze when revealed
- Escape with Shadow Step

**Life + Light = Holy Healing**
- Combine Animus and Lux
- Enhanced healing with purification
- Effective against undead

### Blood Routing

**Blood Routing** allows direct transfer of blood between sources:

**From Vial to Self**
- Quick refill in combat
- Emergency reserve
- Portable blood storage

**From Self to Structure**
- Fuel rituals
- Power blood machines
- Activate constructs

**From Structure to Structure**
- Automate blood networks
- Create blood infrastructure
- Advanced base building

### Drudge Actions

> **Post-alpha WIP:** Drudge automation is documented for development testing and is not first-release progression.

Teach your Drudges specific manipulations:

1. Learn manipulation yourself
2. Craft Drudge Core with memory
3. Assign to Drudge via ritual
4. Drudge can now use that manipulation

**Strategic Uses:**
- Defensive Drudges (barriers, healing)
- Offensive Drudges (projectiles, debuffs)
- Utility Drudges (light, telekinesis)

---

## Blood Constructs

**Blood Constructs** are temporary entities created from your blood.

### Types

**Sanguilith** (dormant MnA compatibility content)
- Floating turret
- Auto-targets enemies
- Shoots blood projectiles
- Duration-based

**Blood Wall**
- Physical barrier
- Blocks movement
- Takes damage for you
- Can be shaped

### Creation
- Requires specific manipulation
- High blood cost
- Cooldown applies
- Multiple constructs possible (depending on degree)

---

## Status Effects

### Positive Effects

**Sanguine Vitality**
- Enhanced blood regeneration
- Increased health
- From Animus manipulations

**Iron Will**
- Reduced manipulation costs
- Increased effectiveness
- From Ductilis alignment

**Radiant**
- Glow effect
- Damage to nearby undead
- From Lux manipulations

### Negative Effects

**Exsanguination**
- Rapid blood loss
- Weakened manipulations
- From damage or overuse

**Withered**
- Reduced healing
- Damage over time
- From Mortem attacks

**Frozen**
- Slowed movement
- Cannot use manipulations
- From Congeatio attacks

**Corrupted**
- Fungal infection active
- Random effects
- From high-degree side effects

---

## Tips for Blood Management

1. **Monitor Your Volume**: Never drain completely. Keep 500-1000 mL reserve.

2. **Carry Vials**: Pre-filled blood vials are emergency refills.

3. **Know Your Costs**: High-rank manipulations can drain you instantly. Plan accordingly.

4. **Regeneration Stacking**: Combine multiple regen sources for fast recovery.

5. **Tendency Focus**: Pick 2-3 tendencies and master them. Spreading thin weakens you.

6. **Manipulation Loadouts**: Customize which manipulations you have quick access to.

7. **Cooldown Awareness**: Don't spam. Cooldowns exist for balance.

8. **Practice Combos**: Some manipulations become stronger when used together.

9. **Environment Matters**: Use terrain and situation to your advantage.

10. **Upgrade Path**: Progress through degrees to unlock higher-rank manipulations.

---

## Integration with Other Systems

### With Mana and Artifice
Mana and Artifice compatibility is dormant in the NeoForge 1.21.1 branch. The design target includes blood/mana conversion, Blood Tithe, resonance combo effects, and Harbinger faction support, but those features are not active until a compatible MnA dependency exists and the excluded compat source is re-enabled.

See: **[[Mod Compatibility]]**

### With Curios
Curios compatibility is dormant in the NeoForge 1.21.1 branch. The Charm of Vascularium remains a Hemomancy item, but the dedicated Curios slot integration is not active until compatible Curios support is restored.

---

## Next Steps

- **[[Harbinger Path]]**: Learn how to progress through degrees
- **[[Advanced Mechanics]]**: Master morphlings and puppeteering; review post-alpha Drudge WIP notes
- **[[World Content]]**: Find enzymes, catalysts, and rare materials

---

*"Blood is not merely a resource. It is memory, identity, power, and price. Master it, and you master yourself. Lose control, and it will consume you."*

*From the Liber Sanguinum, Chapter on Fundamentals*
