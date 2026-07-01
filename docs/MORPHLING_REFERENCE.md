# Morphling Reference

This table reflects the current Morphling implementation in code.

## Shared Progression

| Stage | Requirement | Notes |
|---|---:|---|
| Unfed | 0 power | Base equipped passive only |
| Fledgling | 10 power | Base equipped passive only |
| Developing | 30 power | First unique ability |
| Mature | 60 power | Second unique ability |
| Apex | 100 power | Third unique ability |
| Primal | Apex + Morphic Nectar after Apotheos | Primal active or primal passive unlock |

Preferred enzymes grant full power, secondary enzymes grant 75% power, and other enzymes grant 50% power. Wild-bound Morphlings cap at Developing until properly incubated or primalized.

All Morphlings currently have no passive blood upkeep in code: `getBloodCost()` returns `0`. The meaningful blood costs are on Primal abilities.

## Morphlings

| Morphling | Tendencies / Passive | Developing | Mature | Apex | Primal |
|---|---|---|---|---|---|
| Fungal | `Mortem` / `Animus`; Mycorrhizal Mending | Sporulation on hurt: nearby enemies get Wither and Slowness | Mycorrhizal Network heals nearby allies | Cordyceps Burst on kill: poisons/slows enemies and rolls bonus loot | Primal Mycorrhiza: elite kills trigger a healing fungal patch, ally healing, enemy debuffs, and possible fungal scar drops |
| Leeches | `Animus` / `Congeatio`; Sanguine Siphon | Life steal on attack, scaling from 15% to 25% | Emergency blood transfusion at low health; spends blood to heal | Sanguine Frenzy: bonus damage based on missing health, plus execute-style exsanguination | Hemophage Covenant: costs `450` blood, `1200t` cooldown; attacks heal allies and refill blood |
| Chitinite | `Ferric` / `Congeatio`; Chitinous Bulwark | Reflects 20% to 40% melee damage | Periodic absorption plating | Heavy hits trigger Ironhide, brief invulnerability, and thorn burst | Primal Carapace: costs `500` blood, `900t` cooldown; grants Resistance III, absorption, stores damage, then bursts |
| Serpent | `Ductilis` / `Flammeus`; Serpentine Guile | Venom Strike applies Poison on hit | Constrict after repeated hits: roots and Withers target | Ambush Predator after sneaking: strong opener with Poison, Darkness, and bonus damage | Sovereign Venom: costs `420` blood, `700t` cooldown; marks target and escalates venom effects over repeated hits |
| Pests | `Flammeus` / `Tenebris`; Verminous Aura | Swarm Retaliation summons tracking pests when hurt | Infest kill spawns pest swarm | Plague Burst at low health: Wither and magic damage AoE | Vermin Crown: stores swarm charges from kills; active costs `300` blood, `500t` cooldown, and releases pests |
| Spider | `Tenebris` / `Lux`; Arachnid Anastomosis | Wall climbing and fall control | Silk Tether cancels falls with temporary web | Web Cocoon roots and poisons attacker | Web of Red Thread: costs `250` blood, `160t` cooldown; pulls target or launches player if no target |
| Cuttlefish | `Lux` / `Ductilis`; Luminous Dissipation | Sepia Wake while sprinting blinds enemies | Chromatophore Flash blinds/slows attackers | Ink Mantle Reprieve prevents lethal damage by spending blood | Last-Light Mantle: costs `750` blood, `12000t` cooldown; cleanse, health floor, invulnerability, regeneration, absorption, and enemy blind/nausea |
| Tick | `Mortem` / `Tenebris`; Hemorrhagic Venom | Engorge on kill grants Resistance | Blood Fever grants Speed near wounded enemies | Pandemic Burst on heavy damage: Wither and Weakness AoE | Hemorrhagic Season: costs `420` blood, `700t` cooldown; punishes wounded enemies with Wither/Weakness and spreads on kills |
| Centipede | `Congeatio` / `Ferric`; Venomous Resilience | Burrowing Strike weakens targets | Segmented Defense grants regeneration after large hits | Myriapod Swarm at low health: invisibility and Speed III | Hundredfold Molt: costs `300` blood, `700t` cooldown; cleanses debuffs and grants invisibility, Speed IV, and Resistance V |
| Bat | `Tenebris` / `Ductilis`; Echoic Perception | Sonar Shriek debuffs attacker with Darkness and Slowness | Membrane Glide gives Slow Falling and reduces fall damage | Nightwing Frenzy in darkness: Strength II and Speed I | Echothesis: costs `260` blood, `500t` cooldown; reveals entities in a large radius, grants darkness buffs, and blinds monsters |
| Urchin | `Ferric` / `Congeatio`; Spined Barricade | Spine Lash reflects 25% to 45% melee damage and slows attacker | Tidal Anchor knocks nearby hostiles away | Calcareous Shell on heavy hit: Resistance II and self Slowness | Reefheart Bastion: costs `480` blood, `900t` cooldown; Resistance III, Slowness V, Strength I, thorn burst, and knockback |
| Mole | `Ferric` / `Mortem`; Burrower's Instinct | Burrow Sense underground reveals nearby entities | Earthen Bulwark underground grants Resistance | Seismic Slam underground deals AoE damage and knockback | Deep Tremor Sense: costs `320` blood, `600t` cooldown; Haste III, Night Vision, entity reveal, nearby monster damage, and nectar-fluid highlighting |
