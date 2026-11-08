# Morphling strains: current stats

The eight Brined Court house strains as built (Brined Court Tracks 1, 2 and 4). This page is the current stats reference and replaces `docs/archive/done/CURRENT_MORPHLING_STATS_AND_ABILITIES.md`, which describes the first-attempt strains and stays as history. Mechanics overview: `docs/HEMOMANCY_REFERENCE.md` §16. Lore: `docs/LORE_REFERENCE.md`, "Morphling strains".

A Morphling is a fungal strain that has convergently grown an animal's body solution without being that animal, and lives with a partner organism suited to its house's water. Each section below opens with what the strain is (fungus, convergent form, partner, look), then its numbers.

Source of truth: the `<Strain>Rules` classes in `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/` (one per strain) and `MorphlingBalanceBands`. `MorphlingBalanceBandsTest` checks every ladder against balance rules 1, 2 and 4; `MorphlingStrainLadderRulesTest` pins the per-stage numbers below. The design is `docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md`, "Strain powers and appearance". These are first-pass numbers, to be tuned in play inside the bands (decision M8).

Stage lists read Unfed / Fledgling / Developing / Mature / Apex / Primal. A proc is two seconds unless stated.

## Stages

| Level | Stage | Gives | Reached by |
| --- | --- | --- | --- |
| 0 | Unfed | the passive at its lowest value; tint only | a fresh strain |
| 1 | Fledgling | passive; tint only | first incubation |
| 2 | Developing | first reactive power; small growths | incubation after 50 mL bonding blood (wild-bound strains start here) |
| 3 | Mature | second reactive power; a visible organism | incubation after 100 mL bonding blood (clears `WildBound`) |
| 4 | Apex | signature power; the full silhouette | incubation after 200 mL bonding blood |
| 5 | Primal | Primal power; oversized or replacement anatomy | an Apex strain thrown into Morphic Nectar after Apotheos (Degree 8) |

Bonding blood is absorbed through equipped upkeep and resets at each stage. The Incubator raises one stage per feeding: the preferred enzyme counts in full, the secondary at 75%, any other at 50%.

## Shared rules

| Rule | Value |
| --- | --- |
| Home bonus (unless a power says otherwise) | +50% durations and rates, +25% radii (`MorphlingHomeBonus`) |
| Primal cost / cooldown band | 250-450 mL, 12-90 s |
| Death-save cost / cooldown | 500-650 mL, shared Last Rite 10 min |
| Reactive cooldown bands | Developing 3-15 s, Mature 8-20 s, Apex 20-60 s |
| Giver floors | Shared Table and Colony 50% health, Breakwater 30% |
| Morphling healing of the host | draws the shared circulation window, 1 bandwidth unit per HP (`MorphlingHealing`) |
| Upkeep | configured baseline; Puppet Interference x1.25; Sun-Fed waives it at home unless Puppet Interference is active (the puppets pay their side: 75% tether range and x1.5 thread upkeep, HEMOMANCY_REFERENCE §17) |
| Bloodline ally | anything `ManipulationCombatHelper.allied` counts for the host (teammates, the host's pets and puppets, impressed mobs) |
| Hostile | a target the host may harm (`canHarm`) that is a monster or is hunting the host |
| Still / anchored | standing still: under 0.06 blocks of drift per tick and not jumping; anchored: still while sneaking (`MorphlingAnchorState`) |

## Home conditions

| Strain | Home |
| --- | --- |
| Sunburst | daytime with open sky, or in water within 5 blocks below a sky-lit air block in daytime |
| Holdfast | stone-type block below or beside at feet or head height, or submerged |
| Mermaid's Purse | sprint-swimming, in water in a deep-ocean or Pelagic (open, twilight, midnight, carrion, hydrothermal) biome, or an ally within 8 blocks |
| Dead Man's Rope | submerged, or holding at least one strand |
| Ink Cap | light 7 or lower at the eyes, or submerged |
| Hushbell | biome temperature below 0.15, submerged below sea level, or still for 1 s |
| Marrowplume | a bone block within 8 blocks (4 vertically), or a kill in the last 30 s |
| Kinbud | hot biome (temperature 1.5+), the Nether, burning or in lava, Hydrothermal Depths, or magma, fire, campfire or lava within 4 blocks |

## Sunburst (Heliophyta donans)

Sunburst lichen, the orange crust on coastal rock, partnered with a green lichen alga (*Trebouxia*); it converges on the lettuce sea slug, which keeps stolen chloroplasts alive in ruffled flaps. Gold-orange crust across the shoulders and back that fans into fronds and glows in sunlight. Sustenance, rescue of others and light. Kept flat and crusted so it never reads like Kinbud's flowing orange.

| Stage | Power | Numbers |
| --- | --- | --- |
| Passive | Sun-Fed (home only) | upkeep waived; per proc 0.25 / 0.35 / 0.5 / 0.6 / 0.75 / 1 hunger with matching saturation; with a full bar, 1 / 1.5 / 2 / 2.5 / 3 / 3.5 mL blood in daylight (circulation income) |
| Developing | Shared Table | ally within 8 at 30% health gets 2 / 3 / 4 / 5 HP (Dev / Mature / Apex / Primal), giver never below 50%; 15 s (10 s home). Alone: host at 30% gets 4 hunger and full saturation |
| Mature | Lantern Fronds | still 2 s; host and allies within 12 (15 home) get Water Breathing and Night Vision, refreshed while still and 3 s after; no cooldown |
| Apex | Bloom (home only) | still 3 s; field patch radius 5 for 20 s: Regeneration I and 0.5 hunger per 2 s; 60 s |
| Primal | Arbor | 400 mL; anchored up to 20 s, moving uproots; host and allies within 12: Regeneration II, +1 saturation per second, one harmful effect cleansed every 4 s; at home +5 mL/s blood; 90 s, 15 s Morphic Strain |

## Holdfast (Hapterophyta tenax)

Rock tripe, a lichen fixed to rock by one holdfast, partnered with encrusting coralline red algae that cement themselves to stone; it converges on the limpet, which clamps down and returns to the same spot, and on the kelp holdfast. Knotted, root-like fists, stone grey-brown flecked with coralline pink. It grips rather than plates.

| Stage | Power | Numbers |
| --- | --- | --- |
| Passive | Grip | knockback resistance 0.1 / 0.2 / 0.3 / 0.4 / 0.5 / 0.6 (total capped at 1.0); stone-type breaking speed +10 / 20 / 30 / 40 / 50 / 60% |
| Developing | Cling | against a wall: no sliding down, fall distance cleared; submerged: currents do not push; no cooldown |
| Mature | Brace | hit while sneaking: Resistance I 3 s (II at home), no knockback; allies within 3 behind: Resistance I; 15 s |
| Apex | Breakwater | anchored 2 s: Resistance I and knockback immunity; allies within 5 knockback-immune, 25% of their damage moved to the host at 2 mL per HP, stopping at 30% host health |
| Primal | The Holding | 350 mL; host and hostiles within 8 (10 home) pinned 4 s, allies Resistance II; release: 6 magic damage and a launch to each pinned hostile; 30 s, 9 s Morphic Strain |

## Mermaid's Purse (Sanguibursa communis)

Bird's nest fungus for the purse pouches (small cups holding egg-like packets), partnered with blood-digesting gut bacteria as vampire bats rely on; it converges on the sailfish (raising the sail to herd prey and taking turns to strike), the marlin's bill and the bluefin tuna's speed. Cobalt back, silver flank with a crimson shimmer, a sail that flushes at speed.

| Stage | Power | Numbers |
| --- | --- | --- |
| Passive | Purse Siphon | 1 / 1.5 / 2 / 2.5 / 3 / 3.5 mL per proc (circulation income); swim speed 0 / 5 / 10 / 15 / 20 / 25% in water, x1.5 at home, never past 35%, and never past 35% together with the full Prismatic or Phantasmal Bloodlust set |
| Developing | Feed Banking | strike damage x6 into borrowed blood (x7.2 / 8.4 / 9.6 Mature / Apex / Primal); sprint-swimming x1.5 |
| Mature | School's Share | host or ally within 8 at 30%: 200 mL (banked first) heals the lowest 6 / 9 / 12 HP; 10 s; kill banking up to 160 mL |
| Apex | Raise the Sail | sprint-swimming: Dolphin's Grace; allies within 6 behind: Dolphin's Grace (Speed I on land); bill strike within 2 s of a sprint-swim: +4 damage, double banking, once per 8 s |
| Primal | Shared Wake | 450 mL; 20 s: strikes heal injured group members within 12 up to 2.5 HP and bank up to 35 mL; 60 s, 15 s Morphic Strain |

+35% is a ceiling on the total, not only on the strain's share. The full Prismatic (or Phantasmal Bloodlust) set already gives x1.35, so a Purse worn inside the set adds no swim speed; out of the set it gives its own 0-35%. Raise the Sail's Dolphin's Grace has no amplifier, so a second source adds nothing. Healing the host (School's Share alone, Marrow Feeding, Kinbud regrowth) claims the circulation window, so a low-degree host can get less than the listed heal in one second.

## Dead Man's Rope (Funiculospora nexilis)

A slime-mould vein network that links food sources and abandons tubes it no longer needs, with honey fungus as a naming nod, partnered with light-making bacteria (*Aliivibrio*); it converges on salp chains, which link and separate again, and takes its name and trailing strands from the seaweed *Chorda filum*. Translucent violet-blue veins with a pulse travelling down each strand. It must not copy the Siphonophore mob. Never "many-headed" in game: many heads belong to Hydra.

| Stage | Power | Numbers |
| --- | --- | --- |
| Passive | Lateral Strands | reveal living entities within 16 / 20 / 24 / 28 / 32 / 36 blocks (x1.25 at home) |
| Developing | Lifeline | cancels falls up to 12 blocks (24 Mature, any from Apex); strand at the landing point 5 s |
| Mature | Strand Link | empty-hand sneak-use on an ally or a block: one strand up to 16 blocks; a linked player gets vascular repair 0.5-1.75 per section per proc; either side releases; Alone: sneak to reel toward a block anchor |
| Apex | Colony | up to 3 strands; 20% of a linked creature's damage spread over the others, none below 50%; block anchors do not share |
| Primal | Red Rope | 250 mL; looked-at target within 22: a hostile is pulled, pinned 5 s (2 s for bosses, which are not pulled) and given Weakness I; an ally is pulled to the host; 12 s, 6 s Morphic Strain |

## Ink Cap (Atramentaria velata)

Shaggy and common ink caps, which digest themselves into black ink, partnered with melanised black yeasts; it converges on ultra-black deep-sea fish and the sea hare's ink. A tall shaggy cap and veil, ink black with bone-white highlights, always dripping. Concealment and escape, not ambush.

| Stage | Power | Numbers |
| --- | --- | --- |
| Passive | Veil | hostile detection range -10 / 15 / 20 / 25 / 30 / 35%, x1.5 at home, never past 50% |
| Developing | Ink Wake | sprinting or swimming blinds hostiles within 4 for 1.5 s (3 s Primal); 4 s per target |
| Mature | Folded Away (home only) | still 2 s: Invisibility while still; attacking breaks it and locks it out 3 s |
| Apex | Ink Reprieve | lethal damage: 500 mL, health 8, 2 s invulnerability, blinds hostiles within 4 for 3 s, 3 s Invisibility; Last Rite `hemomancy:ink_mantle` |
| Primal | Deliquesce | 300 mL; 6 s Invisibility and Speed II, hostiles drop their target, trail gives Darkness 3 s within 3; sealed (no attacks or items), a swing ends it; 40 s, 8 s Morphic Strain |

## Hushbell (Campanospora silens)

Fungal sclerotia, hardened dormant masses that outlast winter, partnered with cold-loving bacteria; it converges on the tardigrade's tun state and the diving bell spider's air bubble. A folded frost blue-grey bell with a slow breathing pulse, then stillness. Stasis and conservation.

| Stage | Power | Numbers |
| --- | --- | --- |
| Passive | Hush | poison immunity; hunger and air loss 10 / 20 / 30 / 40 / 50 / 60% slower |
| Developing | Cryptobiotic Hide | still or at 45% health: Resistance I; at 30%: Resistance II |
| Mature | Cold Cleanse | every 5 s removes Poison, Wither, Weakness and Slowness |
| Apex | Fold Shut | a hit leaving 40% health or less: 3 s (4 home) Resistance IV, no air loss, pinned and sealed; unfolds with a full cleanse; 30 s |
| Primal | Cryptobiosis | lethal damage: 650 mL, health at least 10, 5 s sealed and invulnerable; unfolding: Resistance III 11 s, Regeneration II 9 s, Absorption III 13 s; Last Rite `hemomancy:cryptobiosis`, 35 s Morphic Strain II |

## Marrowplume (Medullophyta ossivora)

Bleeding tooth fungus for the palette (bone-white flesh beading red) and scarlet caterpillar club for the growth habit, partnered with bone-dissolving bacteria; it converges on the stalked sea lily, anchored on a stalk yet able to let go and crawl away. Red clubs with feathery crowns on white. Remains as a resource: nothing goes to waste. Kept white-based so it never reads like Kinbud.

| Stage | Power | Numbers |
| --- | --- | --- |
| Passive | Marrow Feeding | 0.5 / 0.75 / 1 / 1.25 / 1.5 / 1.75 HP per proc, x1.5 at home |
| Developing | Plume Cloud | when hurt: hostiles within 5 (6 from Mature) get Weakness I 2 s and Slowness I 1.5 s; host heals 0.5 per hostile up to 2; 5 s |
| Mature | Root Network | every 4 s injured allies within 8 heal 0.5; Alone: +0.25 to Marrow Feeding |
| Apex | Fall's Inheritance | kills: 25% (40% Primal) loot reroll and always one bone; not players, bosses or spawner mobs |
| Primal | Whale Fall | kill of 20+ max health: patch radius 6 for 12 s; allies heal 3 once (x1.5 anchored) and get Marrow Feeding II, hostiles inside Weakness I and Slowness II; Spore Sac chance 12% + 3% per Hyphal Cultivation level; 30 s, 11 s Morphic Strain, no blood |

## Kinbud (Pyrrhiza gemmans)

Budding yeast, the octopus stinkhorn (whose red arms hatch from an egg; reference the organism, never its folk name) and lion's mane mushroom for the mane, partnered with heat-tolerant, sulfur-oxidising vent bacteria; it converges on the freshwater hydra, which buds and regrows its body, and the lion's mane jellyfish's trailing crown. A pale polyp along the spine with an amber-to-crimson mane that ripples like flame and never burns. Stinging and grown regeneration, a preview of Hydra's anatomy.

| Stage | Power | Numbers |
| --- | --- | --- |
| Passive | Cultivated Mending | 10 / 15 / 20 / 25 / 30 / 35% of each non-lethal hit regrown over 10 s, x1.5 at home, never past 50% |
| Developing | Nematocyst Sting | strikes apply Stung (1 damage/s, own damage type, not Poison) for 3 / 4 / 5 / 6 s (Dev to Primal), +50% at home |
| Mature | Grasping Crown | 3 hits on one target within 5 s: pinned 2 s, Wither II 3 s; 8 s before the same target again |
| Apex | Second Head | after hitting or being hit within 5 s: a head buds for 8 s, striking the nearest hostile within 4 once a second for 2 and Stung; 30 s (20 s home); body, not a summon |
| Primal | Kindled | 400 mL; 20 s: all damage taken regrows (cap 20 HP at home, 10 away), two heads at home (one away), Stung 2/s at home; 90 s, 30 s Morphic Strain II |

## Synced active state

`SyncEquippedMorphlingPacket` carries the host's `MorphlingActiveState` bits, Mermaid's Purse's banked-blood fill (bits 24-27, sixteenths) and Dead Man's Rope strands (entity ids and block anchors). The bit list is in `MorphlingActiveState`; it is sent when it changes, not every tick.

## Upkeep

- Every strain draws the configured baseline upkeep: `morphlingDrainRate` 0.5 mL every `morphlingDrainInterval` 60 ticks, about 10 mL a minute (`morphlingPassiveDrainEnabled`, default on). Running dry force-unequips the strain.
- Puppet Interference multiplies final upkeep by 1.25 while any valid same-owner puppet or Commandeered Will is active (HR §16.6). It also cancels Sunburst's Sun-Fed waiver.
- Sun-Fed waives upkeep at home and credits the waived amount to bonding, so a Sunburst kept at home still bonds.
- Hunger (`hungerEnabled`, default on) tracks Fed, Hungry and Starving for Mature and later strains that aren't wild-bound. It doesn't weaken the passive; Starving adds `starvingDrainRate` and can apply Morphic Strain.

## Morphling Cradle

The Cradle hosts any strain below Primal. At Primal it takes only the support strains flagged `cradlePrimal` in `MorphlingStrains`: Sunburst, Holdfast, Mermaid's Purse, Hushbell and Marrowplume. Dead Man's Rope, Ink Cap and Kinbud are refused at Primal.

## Sources

- `common/item/harbinger/morphlings/`: `MorphlingStrains` and `MorphlingStrain` (the table), `HouseMorphlingItem` and the eight `<Strain>MorphlingItem` classes, the eight `<Strain>Rules` classes, `MorphlingBalanceBands`, `MorphlingHomeConditions`, `MorphlingActiveState`, `MorphlingItem`.
- `common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java`, `MorphlingPowerEvents.java`, `MorphlingDamageShare.java`, `MorphlingVisibility.java`.
- `common/event/LastRiteHelper.java` (the two death-saves), `common/effect/StungEffect.java`, `MorphlingLockEffect.java`, `MorphlingStrainEffect.java`.
- `config/HemoServerConfig.java` (upkeep, hunger and bonding settings).
- Text: `assets/hemomancy/lang/en_us.json` (`morphling.hemomancy.<id>.*`, `message.hemomancy.<id>_morphling_item.*`, `bestiary.hemomancy.morphling.<family>.*`), `data/hemomancy/dialogue_inquiry/alchemist/hemomancy/morphling_<id>.json`, the Liber pages `the_hematic_order/pages/morphling_*.json`.
- Tests: `MorphlingStrainsTest`, `MorphlingStrainLadderRulesTest`, `MorphlingBalanceBandsTest`, `MorphlingStrainTextResourceTest`, `MorphlingStrainTableGuardrailTest`; GameTests `MorphlingStrainGameTests`, `MorphlingPowerGameTests`, `MorphlingStrainMatrixGameTests`.
