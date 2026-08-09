# Current Morphling Strains: Stats and Abilities

> Implementation-grounded reference for the eight canonical Morphling strains currently registered in Hemomancy.
>
> Last verified against the current source: **August 9, 2026**.

The former twelve-animal prototype roster is retired. Old morphling IDs and assets may still exist for save migration, but the playable strain roster is:

1. Deadman's Purse
2. Gravecap
3. Witch's Ear
4. Foxfire
5. Bootlace
6. Irontooth
7. Emberfang
8. Winter Shroud

## Shared Maturity Progression

| Stage | Effective enzyme power | Progression effect |
|---|---:|---|
| **Unfed** | 0 | Base equipped passive |
| **Fledgling** | 10 | Stronger base passive |
| **Developing** | 30 | First specialized power |
| **Mature** | 60 | Second specialized power |
| **Apex** | 100 | Third specialized power |
| **Primal** | Apex plus Morphic Nectar after Apotheos/Degree 8 | Capstone active or passive power |

Incubator feeding advances exactly one stage. The first Unfed → Fledgling cycle is immediate; the Morphling must then absorb **50 / 100 / 200 blood** through successful equipped upkeep at Fledgling / Developing / Mature before its next incubation. Stage advancement resets this blood bond, and the Incubator leaves enzyme units it does not need for the next threshold in place.

Enzyme effectiveness depends on blood tendency:

| Enzyme relationship | Effective power granted |
|---|---:|
| Preferred tendency | 100% |
| Secondary tendency | 75% |
| Any other tendency | 50% |

Wild-bound Morphlings cannot progress beyond **Developing** until properly incubated. Hunger is enabled by default for Mature+ cultivated strains. Hungry and Starving states do not reduce passive strength; Starving instead retains its configured blood drain and Morphic Strain consequences.

## Base Stats and Attributes

The stage values in this table are ordered:

**Unfed / Fledgling / Developing / Mature / Apex / Primal**

| Morphling | Preferred / secondary tendency | Equipped passive and stage scaling |
|---|---|---|
| **Deadman's Purse** | Animus / Congeatio | **Sanguine Siphon:** restores **1 / 1.5 / 2 / 2.5 / 3 / 3.5 mL blood** per two-second effect proc, subject to the passive-income circulation cap. |
| **Gravecap** | Mortem / Animus | **Mycorrhizal Mending:** heals **0.5 / 0.75 / 1 / 1.25 / 1.5 / 1.75 HP** per two-second effect proc. Two HP equal one heart. |
| **Witch's Ear** | Ductilis / Tenebris | **Echoic Perception:** reveals living entities in a **16 / 20 / 24 / 28 / 32 / 36-block radius**. This operates from Unfed even though its maturity tooltip lists it at Developing. |
| **Foxfire** | Lux / Ductilis | **Luminous Dissipation:** adds **10 / 20 / 30 / 40 / 50 / 60 percentage points of knockback resistance**. |
| **Bootlace** | Tenebris / Lux | **Arachnid Anastomosis:** repairs **0.5 / 0.75 / 1 / 1.25 / 1.5 / 1.75 vascular health** to every damaged body section per two-second effect proc. |
| **Irontooth** | Ferric / Mortem | **Burrower's Instinct:** adds **15 / 30 / 45 / 60 / 75 / 90% block-breaking speed**. Below Y=50 it also heals **0.5 / 0.75 / 1 / 1.25 / 1.5 / 1.75 HP** per proc and grants Night Vision. |
| **Emberfang** | Flammeus / Ductilis | **Serpentine Guile:** movement speed is **+15 / +30 / +45 / +45 / +45 / +45%** and attack speed is **+10 / +20 / +30 / +30 / +30 / +30%**. Its passive amplifier is capped at 2. |
| **Winter Shroud** | Congeatio / Ferric | **Venomous Resilience:** grants poison immunity and **+5 / +10 / +15 / +20 / +25 / +30% movement speed**. |

## Abilities by Maturity Stage

### Deadman's Purse

**Preferred tendency:** Animus  
**Secondary tendency:** Congeatio  
**Role:** Blood generation, emergency healing, and group sustain

| Stage | Stats and powers granted |
|---|---|
| **Unfed** | Sanguine Siphon restores 1 mL blood per proc. |
| **Fledgling** | Sanguine Siphon increases to 1.5 mL per proc. |
| **Developing** | **Feed Banking:** melee damage stores borrowed blood at **6 times the damage dealt**. |
| **Mature** | **Blood Transfusion:** at 30% health or lower, spends 200 mL to heal 6 HP; 10-second cooldown. Kill banking also begins here in code, storing up to 160 mL based on victim maximum health. Feed Banking rises to 7.2 times damage. |
| **Apex** | The tooltip formally unlocks **Overkill Banking**, although kill banking already operates at Mature. Feed Banking rises to 8.4 times damage, kill-bank generation improves, and Blood Transfusion heals 9 HP. |
| **Primal** | **Hemophage Covenant:** spend 450 mL; 60-second cooldown; 20-second duration; 15 seconds of Morphic Strain. Attacks heal injured players within 12 blocks for up to 2.5 HP and produce up to 35 mL each in borrowed reserve and personal blood. Feed Banking rises to 9.6 times damage and Blood Transfusion heals 12 HP. |

### Gravecap

**Preferred tendency:** Mortem  
**Secondary tendency:** Animus  
**Role:** Regeneration, area denial, ally healing, and bonus loot

| Stage | Stats and powers granted |
|---|---|
| **Unfed** | Mycorrhizal Mending heals 0.5 HP per proc. |
| **Fledgling** | Mycorrhizal Mending heals 0.75 HP per proc. |
| **Developing** | **Sporulation:** when hurt, releases Wither and Slowness spores around the wearer. Base radius is 5 blocks with a 3-second cooldown. At this stage, Wither lasts 2 seconds and Slowness lasts 1.5 seconds. |
| **Mature** | **Mycorrhizal Network:** periodically heals injured allied players within a base 8-block radius for 0.5 HP each, before Hyphal Cultivation bonuses. Sporulation grows to a 6-block radius. |
| **Apex** | **Cordyceps Burst:** kills inflict Poison II for 5 seconds and Slowness for 4 seconds around the corpse, then reroll the victim's loot table for bonus drops. Base radius is 6 blocks before skill bonuses. |
| **Primal** | **Primal Mycorrhiza:** kills against creatures with at least 20 maximum HP can create a fungal patch. It heals allies for 3 HP, grants Mycorrhizal Mending II, applies Weakness I and Slowness II to hostiles, and has a skill-scaled chance to produce a Spore Sac. It has a 30-second cooldown and applies 11 seconds of base Morphic Strain. |

Hyphal Cultivation increases Gravecap's support radii, healing, and Spore Sac chance.

### Witch's Ear

**Preferred tendency:** Ductilis  
**Secondary tendency:** Tenebris  
**Role:** Detection, aerial control, and dark-environment pursuit

| Stage | Stats and powers granted |
|---|---|
| **Unfed** | Echoic Perception reveals living entities within 16 blocks. |
| **Fledgling** | Echoic Perception radius increases to 20 blocks. |
| **Developing** | The tooltip formally unlocks **Echoic Perception**, but the effect already operates from Unfed. Radius increases to 24 blocks; there is no separate new code path at this stage. |
| **Mature** | **Membrane Glide:** sneaking while airborne grants Slow Falling. Fall damage is completely cancelled when one quarter of the fall distance is no greater than 3, equivalent to falls of up to 12 blocks. |
| **Apex** | **Night-State Pursuit:** below light level 4, grants Strength II and Speed I. Echoic Perception reaches 32 blocks. |
| **Primal** | **Echothesis:** spend 260 mL; 25-second cooldown; 8 seconds of Morphic Strain. Living entities within 36 blocks glow for 11 seconds. Below light level 5, the wearer gains Strength II and Speed II for 9 seconds while nearby monsters receive Darkness for 5 seconds. |

### Foxfire

**Preferred tendency:** Lux  
**Secondary tendency:** Ductilis  
**Role:** Knockback resistance, concealment, disruption, and death prevention

| Stage | Stats and powers granted |
|---|---|
| **Unfed** | Luminous Dissipation adds 10 percentage points of knockback resistance. |
| **Fledgling** | Knockback resistance bonus increases to 20 percentage points. |
| **Developing** | **Sepia Wake:** while sprinting, blinds hostiles within 4 blocks for 1.5 seconds. The duration scales with later maturity. |
| **Mature** | **Low-Light Camouflage:** after remaining still for 2 seconds while in water or at light level 7 or lower, grants short-duration Invisibility that is refreshed while conditions remain valid. |
| **Apex** | **Ink Mantle Reprieve:** on projected lethal damage, spend 500 mL, set health to 8 HP, and gain 2 seconds of invulnerability. Requires its armed Last Rite and has a 10-minute cooldown. |
| **Primal** | **Last-Light Mantle:** spend 750 mL; cleanse all effects; restore health to at least 12 HP; gain 4 seconds of invulnerability, Regeneration II for 8 seconds, and Absorption III for 12 seconds. Hostiles within 8 blocks receive Blindness and Nausea for 8 seconds. Requires its armed Last Rite, has a 10-minute cooldown, and applies 35 seconds of Morphic Strain II. |

At Primal maturity, Sepia Wake's blindness lasts 3 seconds and Luminous Dissipation supplies 60 percentage points of knockback resistance.

### Bootlace

**Preferred tendency:** Tenebris  
**Secondary tendency:** Lux  
**Role:** Vascular repair, climbing, fall arrest, and battlefield control

| Stage | Stats and powers granted |
|---|---|
| **Unfed** | Arachnid Anastomosis repairs 0.5 vascular health to every damaged body section per proc. |
| **Fledgling** | Vascular repair increases to 0.75 per section per proc. |
| **Developing** | **Wall Climbing:** arrests downward motion while colliding horizontally with a wall and clears accumulated fall distance. |
| **Mature** | **Silk Tether:** cancels fall damage and places a temporary web at the landing point. The current implementation does not impose a maximum fall distance. |
| **Apex** | **Web Nest:** while sneaking, traps nearby hostiles within 4 blocks in temporary webs and applies Slowness III for 3 seconds. It has a 5-second cooldown. |
| **Primal** | **Web of Red Thread:** spend 250 mL; 8-second cooldown; 6 seconds of Morphic Strain. A looked-at target within 22 blocks is pulled toward the wearer, effectively rooted for 5 seconds, and afflicted with Poison II for 5 seconds. With no target, the wearer instead leaps forward and gains Slow Falling for 4 seconds. |

### Irontooth

**Preferred tendency:** Ferric  
**Secondary tendency:** Mortem  
**Role:** Underground sustain, detection, combat speed, and shockwaves

| Stage | Stats and powers granted |
|---|---|
| **Unfed** | Burrower's Instinct grants 15% block-breaking speed. Below Y=50 it grants Night Vision and heals 0.5 HP per proc. |
| **Fledgling** | Block-breaking speed increases to 30%; underground healing increases to 0.75 HP per proc. |
| **Developing** | **Burrow Sense:** below Y=50, reveals nearby living entities. Radius begins at 16 blocks and increases with maturity. |
| **Mature** | **Earthen Bulwark:** being struck below Y=50 grants Resistance I for 3 seconds; 15-second cooldown. The response can also shed Chalybeate Sclerite, subject to its separate cooldown. |
| **Apex** | **Seismic Slam:** sneak-jumping below Y=50 deals 6 magic damage and launches hostile creatures within 6 blocks. It has a 20-second cooldown. Block-breaking speed reaches +75% and Burrow Sense reaches 32 blocks. |
| **Primal** | **Deep Tremor Sense:** spend 320 mL; 30-second cooldown; 9 seconds of Morphic Strain. Grants Haste III for 15 seconds and Night Vision for 16 seconds, reveals living entities within 30 blocks for 9 seconds, deals 7 magic damage and launches monsters within 8 blocks, and marks nearby Morphic Nectar deposits with particles. Passive block-breaking speed reaches +90% and ordinary Burrow Sense reaches 40 blocks. |

Burrower's Instinct uses Minecraft 1.21.1's player block-break-speed attribute, matching its mining-speed description.

### Emberfang

**Preferred tendency:** Flammeus  
**Secondary tendency:** Ductilis  
**Role:** Speed, poison, repeated-hit control, and assassination

| Stage | Stats and powers granted |
|---|---|
| **Unfed** | Serpentine Guile grants +15% movement speed and +10% attack speed. |
| **Fledgling** | Serpentine Guile increases to +30% movement speed and +20% attack speed. |
| **Developing** | **Venom Strike:** melee hits apply Poison I for 3 seconds. Serpentine Guile reaches its cap of +45% movement speed and +30% attack speed. |
| **Mature** | **Constrict:** three hits against the same target within 5 seconds root it for 2 seconds and apply Wither II for 3 seconds. Venom Strike lasts 5 seconds. |
| **Apex** | **Ambush Predator:** after sneaking for at least 3 seconds, the next attack applies Poison III for 10 seconds, Darkness for 4 seconds, and 6 magic damage. It has an 8-second cooldown. Ordinary Venom Strike becomes Poison II for 7 seconds. |
| **Primal** | **Sovereign Venom:** mark a looked-at target within 24 blocks for 20 seconds at a cost of 420 mL; 35-second cooldown and 11 seconds of Morphic Strain. The first hit applies Poison III for 8 seconds. The second roots for 4 seconds and applies Weakness II for 6 seconds. The third deals the lesser of 18 magic damage or 18% of maximum health, then applies Wither II for 7 seconds. |

### Winter Shroud

**Preferred tendency:** Congeatio  
**Secondary tendency:** Ferric  
**Role:** Poison immunity, resilience, cleansing, and last-breath survival

| Stage | Stats and powers granted |
|---|---|
| **Unfed** | Venomous Resilience grants poison immunity and +5% movement speed. |
| **Fledgling** | **Cryptobiotic Hide already begins here in code:** standing still or falling to 45% health or lower grants Resistance. At 30% health or lower, the resistance amplifier increases by one. Movement speed reaches +10%. |
| **Developing** | The tooltip formally unlocks **Cryptobiotic Hide**, although its code path begins at Fledgling. Its normal resistance becomes Resistance II, or Resistance III at 30% health or lower. |
| **Mature** | **Cold Cleanse:** periodically removes Poison, Wither, Weakness, and Slowness. Movement speed reaches +20%. |
| **Apex** | **Tun Molt:** when incoming damage leaves the wearer at 40% health or lower, cleanse harmful effects and gain Invisibility for 4 seconds, Speed II for 5 seconds, Resistance II for 3 seconds, and at least 1 second of invulnerability. It has a 30-second cooldown. |
| **Primal** | **Cryptobiosis:** spend 650 mL; cleanse harmful effects; restore health to at least 10 HP; gain 5 seconds of invulnerability, Resistance III for 11 seconds, Regeneration II for 9 seconds, and Absorption III for 13 seconds. Requires its armed Last Rite, has a 10-minute cooldown, and applies 35 seconds of Morphic Strain II. |

## Upkeep and Configuration Caveats

- All eight canonical Morphlings use the configured baseline upkeep despite returning a strain blood cost of `0`. The default is 0.5 blood every 60 ticks, or approximately 10 blood per minute.
- Positive strain blood costs scale that baseline; running out of blood still force-unequips the Morphling.
- `morphlingPassiveDrainEnabled` defaults to `true`.
- `hungerEnabled` defaults to `true`.
- The legacy `husbandryStageQuota` is retained for configuration compatibility but no longer caps maturity; strain-specific husbandry progress still drives its existing side rewards.
- Blood-bond defaults are 50 at Fledgling, 100 at Developing, and 200 at Mature. Disabling passive upkeep bypasses the bond gate.
- Disabling passive drain does not stop `onEquippedTick` powers; passive dispatch remains on the configured Morphling interval.
- Hungry and Starving states do not lower the maturity-scaled passive amplifier. Starving can still apply its configured blood drain and Morphic Strain.

## Morphling Cradle Compatibility

Only the following Primal strains are currently accepted by the Morphling Cradle:

- Gravecap
- Deadman's Purse
- Winter Shroud
- Bootlace
- Irontooth

Witch's Ear, Foxfire, and Emberfang are not currently accepted by the Cradle.

## Primary Implementation Sources

- `src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`
- `src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java`
- `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/MorphlingItem.java`
- `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/DeadmansPurseMorphlingItem.java`
- `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/GravecapMorphlingItem.java`
- `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/WitchsEarMorphlingItem.java`
- `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/FoxfireMorphlingItem.java`
- `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/BootlaceMorphlingItem.java`
- `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/IrontoothMorphlingItem.java`
- `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/EmberfangMorphlingItem.java`
- `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/WinterShroudMorphlingItem.java`
- `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java`
- `src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java`
