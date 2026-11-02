# Morphling Reference

This table reflects the eight canonical Morphling strains currently registered in code. The older twelve-animal prototype roster is retired.

## Shared Progression

| Stage | Requirement | Notes |
|---|---:|---|
| Unfed | 0 power | Base equipped passive only |
| Fledgling | 10 power | Base equipped passive only |
| Developing | 30 power | First unique ability |
| Mature | 60 power | Second unique ability |
| Apex | 100 power | Third unique ability |
| Primal | Apex + Morphic Nectar after Apotheos | Primal active or primal passive unlock |

Preferred enzymes grant full power, secondary enzymes grant 75% power, and other enzymes grant 50% power. Wild-bound Morphlings cap at Developing until properly incubated.

All canonical Morphlings use the configured baseline passive upkeep even though `getBloodCost()` returns `0`: by default they absorb 0.5 blood every 60 ticks. That successful upkeep builds the current stage's blood bond. Fledgling, Developing, and Mature require 50, 100, and 200 absorbed blood before the next one-stage Incubator advancement; disabling passive upkeep bypasses this gate and does not disable equipped powers.

Morphling hunger is enabled by default for Mature+ cultivated strains. Hungry and Starving states do not reduce passive strength; Starving retains its separately configured blood drain and Morphic Strain consequences.

## Morphlings

| Morphling | Tendencies / Passive | Developing | Mature | Apex | Primal |
|---|---|---|---|---|---|
| Deadman's Purse | `Animus` / `Congeatio`; Deadman's Purse Morphling | Feed Banking stores strike blood as borrowed reserve | Blood Transfusion spends blood for an emergency heal | Overkill Banking stores corpse blood for later casts | Hemophage Covenant shares damage into blood and healing |
| Gravecap | `Mortem` / `Animus`; Gravecap Morphling | Sporulation releases toxic spores when hurt | Mycorrhizal Network heals nearby allies | Cordyceps Burst turns kills into poison bursts and bonus loot | Primal Mycorrhiza seeds healing fungal patches from elite kills |
| Witch's Ear | `Ductilis` / `Tenebris`; Witch's Ear Morphling | Echoic Perception reveals nearby signals | Membrane Glide grants slow falling and reduced fall damage | Night-State Pursuit grants strength in darkness | Echothesis reveals blood signatures and empowers night raids |
| Lumenlace | `Lux` / `Ductilis`; Lumenlace Morphling | Sepia Wake blinds hostiles while sprinting | Low-Light Camouflage hides a still wearer in darkness or water | Ink Mantle Reprieve prevents death by spending blood | Last-Light Mantle cleanses and prevents death at a heavy cost |
| Bootlace | `Tenebris` / `Lux`; Bootlace Morphling | Wall Climbing enables fungal-cord ascent | Silk Tether creates a web to break falls | Web Nest traps nearby hostiles while sneaking | Web of Red Thread pulls, roots, or movement-tethers targets |
| Irontooth | `Ferric` / `Mortem`; Irontooth Morphling | Burrow Sense reveals entities underground | Earthen Bulwark grants resistance when struck underground | Seismic Slam releases an underground shockwave | Deep Tremor Sense maps life, nectar, and tunneling threats |
| Emberfang | `Flammeus` / `Ductilis`; Emberfang Morphling | Venom Strike poisons melee targets | Constrict roots and crushes after repeated hits | Ambush Predator rewards a prepared first strike | Sovereign Venom marks a priority target for escalating venom |
| Winter Shroud | `Congeatio` / `Ferric`; Winter Shroud Morphling | Cryptobiotic Hide rewards stillness or low health | Cold Cleanse sheds poison, wither, weakness, and slowness | Tun Molt creates a low-health escape burst | Cryptobiosis provides a Primal Last Rite survival state |

Only Gravecap, Deadman's Purse, Winter Shroud, Bootlace, and Irontooth are accepted by the Morphling Cradle while Primal.
