# Hemomancy Progress Report

## Major Repository Changes Since April 17, 2026

**Reporting period:** April 17-August 17, 2026  
**Branch:** `neo-1.21.1`  
**Scope:** Committed work through August 17, with current uncommitted work identified separately.

## Executive Summary

Since April 17, Hemomancy has progressed from a newly compiling NeoForge port with several disconnected systems into a much more integrated, progression-focused mod. Encounters, NPC assignments, crafting, powers, equipment, structures, world spaces, and interfaces now connect into recognizable player journeys.

The period contains 1,005 commits, including 874 non-merge commits. The net comparison touches 8,306 files with approximately 497,000 additions and 124,000 deletions. Generated resources, asset migrations, and large model files heavily inflate those raw figures, so the implementation milestones below are more useful than commit or line counts alone.

The strongest overall progress is the move from isolated mechanics toward connected play:

- Harbinger progression now has assignments, chapter gates, specialists, and an endgame.
- Cardinal Rites became staged ritual encounters rather than simple recipes.
- Vesper, the Mycophant, Annetta, and the Saints provide distinct encounter arcs.
- Morphlings, scars, manipulations, living weapons, and armor now form interacting loadout systems.
- Hematic Memory now has distinct Noetic and Thelemic expressions with different acquisition, activation, body routing, and progression roles while sharing one loadout architecture.
- New biomes, structures, NPCs, machines, and authoring tools support those systems.

## Items and Equipment

### Living Arsenal

- The **Living Staff** became the central long-term weapon and blood-utility interface.
- Added or completed temporary Living Staff forms:
  - Living Blade
  - Living Axe
  - Living Spear
  - Living Baghnakh
  - Living Crossbow
  - Living Torch
  - Living Flail
  - Living Sickle
- Added blood-costed form switching, misplaced-staff recovery, inventory duplication protection, and automatic restoration when selecting another manipulation.
- Added **Living Weapon Grafts** for the seven standard forms, including recipes, unlock tracking, Artificer assignments, JEI support, and fitting visuals.
- Added the Vesper-memory **Living Sickle** with two combat modes:
  - Short Reap melee and radial spin attacks.
  - Blood Hook projectile attacks that wound and pull targets.
- Added or expanded:
  - Living Syringe
  - Blood Absorption hand tool
  - Blood Projection hand tool
  - Blood Bolts
  - Sanguis Lancea
  - Annetta's Sanguis Lancea

### Armor and Shields

- Added the **Hematic Armature** upgrade path so earlier armor remains useful.
- Expanded Blood Lust progression through:
  - Barbed
  - Chitinite
  - Prismatic
  - Edacious Blood Lust
  - Sheolic Blood Lust
  - Phantasmal Blood Lust
- Added or substantially implemented:
  - Silent Archon Vestments
  - Covenant Mantle
  - Crown of Sacred Marrow
  - Vestment of the Final Molt
  - Lantern Tick Helmet
  - Venous Strider Sabatons
  - Hemolymphopoda Headpiece
  - Barbed, Chitinite, and Unstained shields
- Armor-set abilities were moved into the manipulation radial with cooldown and tooltip feedback.
- Silent Archon received:
  - Physical intangibility with matching offensive restrictions.
  - Silent Severance.
  - Monolithic Dislocation.
  - Silent Slipping through blocks.
  - Custom translucent armor and player-body rendering.

### Morphlings

The Morphling system was consolidated around eight primary strains:

| Morphling | Primary implementation |
|---|---|
| Deadman's Purse | Feed banking, transfusion, and overkill blood storage |
| Gravecap | Fungal networks, sporulation, and Cordyceps-style attacks |
| Witch's Ear | Echo perception, gliding, and night pursuit |
| Lumenlace | Camouflage, ink defense, and low-light behavior |
| Bootlace | Climbing, silk tethering, and web nests |
| Irontooth | Burrow sensing, defense, and seismic attacks |
| Emberfang | Venom, constriction, and ambush behavior |
| Winter Shroud | Cryptobiotic defense, cleansing, and survival |

Supporting implementations include:

- Morphling Polyps and Jars.
- Identity and migration data.
- Mutation layers derived from wild specimen ecology.
- Developing, Mature, Apex, and Primal stages.
- Hunger, metabolism, feeding timestamps, and husbandry.
- Morphic Nectar mutation presentation.
- Morphling Cradle hosting.
- Equipped-Morphling recovery after player death.
- Puppet-interference penalties for range and blood upkeep.

### Memory and Progression Items

- Hematic Memories and Crude Memory Shards.
- Memory Thread and the physical Somatic Loom weaving interaction.
- Memory of Vesper.
- Mnemonic Blueprint and Mnemonic Folio.
- Harbinger Assignment Ledger.
- Covenant Waybill.
- Book of Observances.
- Field Notes and prewritten memos.
- Monolith Fragment and Monolith-Imbued Cloth.
- Qliphoth Seed and Qliphoth Pomes.
- Saint relics.
- Stained Church and Mason's Respite maps.

### Hematic Memory: Noetic and Thelemic Powers

The memory overhaul established **Hematic Memory** as the shared category for repeatable instruction preserved in blood. It has two mechanically distinct expressions:

| Axis | Noetic Memory | Thelemic Memory |
|---|---|---|
| Core idea | An intelligible pattern understood and projected as a blood manipulation | Embodied will learned by the body as a repeatable physical answer |
| Primary specialist | Mnemonist | Alchemist, with later Mnemonist integration |
| Acquisition | Hematic Memory items, Somatic Loom weaving, discoveries, and recognition routes | Prepared tincture doses made with tissue, enzymes, blood vessels, and the Ghastly Alembic |
| Use model | Player deliberately casts the selected manipulation | Player activates a bodily instruction; it triggers only when its useful physical action occurs |
| Resource model | Manipulation-specific blood cost and casting rules | Finite prepared reserve, per-trigger blood cost, vascular strain, cooldown, and optional overexertion |
| Body relationship | Can route through a matching active cerebral scar and receive tier-based strain reduction | Assigned to one of five vascular sections; dead sections block activation and competing memories replace each other within that section |
| Loadout | Uses shared mnemonic capacity | Uses the same shared mnemonic capacity |
| Interface | Shared manipulation radial, Reliquary, and Dendritic Distributor | Same radial and stations, with tincture icons, reserve meter, pulse states, and vascular cracking |
| Persistence | Learned manipulation knowledge and existing manipulation identifiers remain authoritative | Learned knowledge survives death; prepared reserve, active state, cooldown, empowerment, and arming are cleared on death |

Expression, tendency, rank, vascular section, and acquisition route are independent axes. A Ferric Noetic Memory is not the same kind of power as a Ferric-associated Thelemic Memory, and neither becomes a separate class or exclusive progression lane.

The specialist mnemonic is **“Mneme keeps. Nous knows. Thelēma acts.”** Noetic practice is the understood and projected expression; Thelemic practice is the embodied and enacted expression.

#### Noetic Memories

Conventional active blood manipulations are formally classified as **Noetic Memories**. The change provides a real runtime classification through `HematicMemoryExpression.NOETIC`; it is not merely renamed dialogue.

Major implementation points:

- Blank Hematic Memories are prepared from Sanguine Formation, Blood-Stained Stone, and Neurotic Enzyme.
- The Somatic Loom turns a blank memory, catalysts, stored tendency enzymes, projected blood, and the orb-weaving interaction into a learned Noetic manipulation.
- Noetic Memories retain their normal registry names, manipulation controls, recipes, and serialized slot strings.
- They occupy the same mnemonic capacity as Thelemic Memories but do not consume tincture reserve or claim an active vascular section.
- Active cerebral scars can route a Noetic Memory whose primary tendency matches the scar.
- Matching cerebral-scar tiers reduce Noetic strain by 5% per tier, up to the current tier-three ceiling.
- Fixed mechanical controls such as Blood Absorption and Blood Projection are excluded from Noetic scar-routing proofs and saved mnemonic patterns.
- The D6 Vein Mason progression explicitly requires a non-mechanical Noetic Memory matching an active cerebral scar, then a changed Effigy loadout and a second matching Noetic cast.

**Conductive Mark** is the first dual-recognition Noetic formula. At Degree 3, either a recorded Ductilis/Neurotic tissue result or completion of the first Somatic Loom weave converges on one recognition advancement and the same existing memory recipe. The two scholarly routes do not create duplicate items, recipes, currencies, or powers.

Representative Noetic powers added or expanded during the period include:

- Black Veil Covenant
- Blood Eclipse Mantle
- Cauterizing Rebuke
- Conductive Mark
- Ferric Resonance
- Glacial Circulation
- Glacial Rampart
- Gloam Laceration
- Grave Debt
- Hematic Beacon
- Hematic Flare
- Hemolymphal Pulse
- Insatiable Hunger
- Iron Retort
- Lumen Suture
- Osseous Bloom
- Prismatic Reproof
- Sanguine Magnetism
- Scalding Updraft
- Synaptic Jolt
- Umbral Reversal
- Vascular Dowsing
- Vital Effusion

#### Thelemic Memories

The bodily powers originally implemented internally as Muscle Memories are formally presented as **Thelemic Memories**. Compatibility names such as the Java `musclememory` package and persistent `muscle_memory:<id>` keys remain unchanged to protect saves and networking.

Thelemic state separates six facts that the earlier report incorrectly collapsed together:

1. **Learned:** permanent knowledge of the bodily instruction.
2. **Reserve:** prepared tincture duration currently stored for that memory.
3. **Equipped:** occupying one shared mnemonic and radial slot.
4. **Active:** enabled in its assigned vascular section and draining reserve.
5. **Triggered:** a useful action successfully paid blood and strain and produced the response.
6. **Armed:** the next valid trigger may use the overexerted response.

Selecting, activating, and triggering are separate operations. Merely selecting or activating a Thelemic Memory does not spend its per-action blood cost. Payment occurs only when the relevant action is valid and the response commits.

Eight Thelemic Memories were implemented, each with flask and jug tinctures:

| Thelemic Memory | Section | Tendencies | Normal trigger and response | Overexerted response | Signature Morphling |
|---|---|---|---|---|---|
| Sanguine Fists | Arms | Flammeus / Animus | A valid direct melee hit adds damage | Additional damage and stronger knockback | Emberfang |
| Laboring Arms | Arms | Ferric / Ductilis | Breaking a sufficiently hard block with the correct tool activates increased break speed | Raises the break-speed bonus from 20% to 40% | Irontooth adds a resistant-block bonus |
| Coursing Legs | Legs | Ductilis / Flammeus | Ground sprinting triggers every 12 blocks and activates increased movement speed | Raises movement speed from 15% to 30% and adds step height | Emberfang banks attack momentum |
| Hushed Gait | Legs | Tenebris / Ductilis | Moving without sprinting in very low light suppresses step, swim, and splash events | Deep hush also suppresses landing sound | Bootlace suppresses shorter landings |
| Predatory Eyes | Head | Lux / Tenebris | In darkness, identifies and marks nearby moving hostiles | Expands the scan and marks up to eight targets | Witch's Ear permits broader no-line-of-sight tracking behavior |
| Second Pulse | Heart | Animus / Mortem | Reduces damage that would push the player below 30% health | Increases the reduction to 50%, capped at 10 damage | Deadman's Purse |
| Enduring Viscera | Body | Congeatio / Mortem | Refunds hunger when exhaustion consumes the last unsaturated food point | Refunds two food points and strengthens the active response | Winter Shroud reduces exhaustion |
| Carrion Metabolism | Body | Mortem / Animus | Metabolizes tagged carrion and adds food and saturation | Grants a stronger refill and temporary Absorption | Gravecap is fed by the response |

Each memory has its own blood cost, vascular strain, and cooldown profile. The Arms, Legs, Head, Heart, and Body sections remain the only injury authority. Because two memories compete for Arms, two for Legs, and two for Body, a player may learn all eight but cannot run both memories assigned to the same section simultaneously.

#### Tincture Preparation and Reserve

- Every Thelemic Memory has a flask and jug recipe.
- A flask requires a 2,500-blood Bloody Flask, holds three doses, and returns a Cured Clay Flask after the last dose.
- A jug requires a 5,000-blood Bloody Jug, holds six doses, and returns a Cured Clay Jug after the last dose.
- Each dose permanently teaches an unknown Thelemic Memory and adds five minutes of reserve.
- Reserve is capped at thirty minutes per memory and drains only while that memory is active.
- Failed Alembic preparation is transactional: formation, enzyme, blood, and vessel are committed only when the output can complete.
- Legacy `Primed` state migrates into reserve rather than invalidating older saves.

#### Activation, Vascular Damage, and Overexertion

- One Thelemic Memory may be active per vascular section.
- Activating a second memory assigned to the same section replaces the first.
- A dead vascular section blocks activation.
- Normal manipulation-use toggles the selected Thelemic Memory.
- Sneak-use arms its next valid trigger for ten seconds.
- A valid overexerted trigger attempts to pay twice the normal blood and 2.5 times the normal vascular strain.
- Invalid actions do not consume arming.
- If enhanced payment is unavailable, the system falls back to the ordinary response instead of deleting the prepared use.
- Morphling resonance modifies costs, strain, or the signature response through the same transactional trigger service.

The radial communicates these states directly: full-color tincture icons indicate reserve, grayscale indicates an empty reserve, active memories pulse, armed memories pulse faster, and vascular damage appears as progressively denser cracks. The tooltip reports expression, section, reserve, blood cost, strain, and the current blocking reason.

#### Shared Noetic/Thelemic Loadouts

- Both expressions use one radial and one mnemonic capacity.
- Before Reliquary instruction, a newly learned Thelemic Memory auto-equips into the first free shared slot.
- Knowledge learned after capacity is full remains permanent but stays outside the field radial until the loadout is rearranged.
- The Degree 3 **Mnemonic Reliquary** groups both expressions by tendency and displays their expression and, for Thelemic entries, vascular section.
- The Degree 5 **Dendritic Distributor** saves, previews, names, overwrites, and applies mixed Noetic/Thelemic patterns.
- Applying a pattern deactivates active Thelemic Memories omitted from that pattern and restores either expression as the selected radial entry.
- Distributor save and overwrite operations retain their 100-blood and 25-raw-XP cost; applying and renaming are free.
- Legacy manipulation slot strings and `muscle_memory:<id>` keys remain the save and network contract.

#### Distinct Progression Roles

- **Body Answers** follows First Separation and introduces the Alchemist's empirical Thelemic route through Sanguine Fists, tissues, enzymes, and a Bloody Flask.
- Drinking the first Sanguine Fists dose completes the assignment and unlocks the remaining flask, jug, and vessel formula catalogue.
- **The Woven Vessel** introduces the Mnemonist's Noetic route through blank-memory preparation and the Somatic Loom; it also provides a skip-path fallback for the Thelemic recipe catalogue without granting a free Thelemic power or reserve.
- The D5 Vein Mason journey uses a Thelemic Memory to induce a controlled Varicose state. Ordinary physical damage and Noetic casting deliberately do not satisfy that proof.
- The D6 Vein Mason journey instead tests Noetic routing through cerebral scars before and after an Effigy loadout change.
- These routes are complementary rather than class locks: early embodied instruction remains useful beside later perceptual and conceptual projection.

### Utility and Diagnostic Items

- Curor Lens for inspecting health, blood, tendencies, and scars.
- Tendon Line for biological climbing and tethered traversal.
- Constrictor Cord.
- Scale Grip death-protection token.
- Blood Chum.
- Hearty Compass.
- Humoral Barometer.
- Vascular Status Gauge.
- Bloodline Pool Monitor.
- Self-Reflection Mirror.
- Vascularium Charm.
- Vascular Poultice.
- Sanguine Salve.
- Witness Organ.
- Void Eye Organ.
- Husk Effigies.
- Vein Spider.
- Structure Scanner.
- Scar-pattern preparation items.
- Specimen Jars with exact entity-data capture and release.

### Unstained Equipment and Materials

- Unstained armor and shield.
- Unstained Warhammer.
- Absolution Dagger.
- Silthmere Glaive.
- Pale Silver Pickaxe.
- Consecrated Syringe.
- Draught of Still Waters and Draught of Still Mercy.
- Pale Distillate, Pallid Infusion, and related consumables.
- Consecrated Copper and Pale Silver progression.
- Cleansing Hemolymph and Hemolytic solutions.

## Blocks and Machines

### Crafting and Processing Stations

| Block | Implementation |
|---|---|
| Hematic Armature | Restrains the player while upgrading worn armor; supports base, Vicar, and Monolithic machine tiers |
| Somatic Loom | Uses enzymes, catalysts, memories, and projected blood in a physical orb-wrangling minigame |
| Vial Centrifuge | Converts sampled Blood Vials into tendency enzymes and returns empty containers |
| Ghastly Alembic | Extracts blood and processes items, flasks, catalysts, and Blood Gourds |
| Morphling Incubator | Grows Polyps into Morphling strains using enzymes and blood |
| Mycelial Crucible | Grows immature fungal cultures into specific fungal scars |
| Mycelial Lantern | Passively produces enzymes from cultures and blood |
| Puppeteer's Spindle | Stores Puppeteering Thread charge and prepares a Marionette Crossbar |
| Dendritic Distributor | Manages higher-degree synaptic loadouts |
| Mnemonic Reliquary | Groups Noetic and Thelemic Memories and equips both through one shared mnemonic capacity |
| Dictation Table | Converts discoveries into virtual Field Notes using blood or experience |

### Equipment and Biological Stations

| Block | Implementation |
|---|---|
| Scarlet Vanity | Manages Morphlings, Blood Gourds, Charms, and Living Staff fittings |
| Cerebral Scarring Station | Creates cerebral scars from patterns |
| Mason's Effigy | Prepares multi-scar loadout patterns |
| Fungal Implantation Pylon | Installs, replaces, or removes the player's cultivated fungal scar |
| Morphling Cradle | Hosts an owner-bound Morphling with blood routing and aura effects |
| Specimen Jar | Captures, displays, transports, and releases supported creatures |
| Visceral Mirror | Extracts organ echoes from the player |
| Scrying Podium | Displays blood, circulation, manipulation, cost, slot, and tendency diagnostics |

### Ritual and Progression Blocks

- **Iron Brazier:** consolidated absorption for memories, scars, scar patterns, and progression rites.
- **Cardinal Focus:** ritual-medium presentation and interaction.
- **Covenant Throne:** covenant binding, progression state, and seating.
- **Mortal Display:** Hermit's heart and temple-oath prerequisite.
- **Sanguine Monolith / Crimson Lodestone:** degree-gated lore and eventual shattering into Qliphoth materials.
- **Qliphoth Bloom:** owner-bound multiblock tree, blood aura, Pome lifecycle, Vesper access, and Silent-path pruning.
- **Sanguine Vigil:** covenant-state support.
- **Blood Trial Altar:** ritual and ordeal infrastructure.
- **Offering Gate:** offering and ritual-floor infrastructure.
- **Saint Sarcophagus:** Saint encounter infrastructure.
- **Sanguine Omen:** blood-trial and progression marker.
- **Consecrated Bloodwell:** exposes bloodline-pool blood to staff absorption and projection.
- **Warp Chair:** multiblock binding, seating, rendering, and travel support.

### Building and Environmental Blocks

- Venous Stone walls, slabs, stairs, bricks, cracked, chiseled, gilded, and infested variants.
- Hematic Iron bars, chains, door, trapdoor, pillars, and chiseled blocks.
- Pale Silver bars, chains, and bells.
- Cleansed Stone and Hemolytic Plating.
- Pallid Lantern.
- Sanguine and Vivianite glass and panes.
- Blood Wood logs, leaves, and planks.
- Hyphae, infected stems, caps, and fruiting caps.
- Erythrocytic Dirt and Mycelium.
- Bleeding Heart.
- Devil's Tooth.
- Infected Fungus, Stinkhorn, Puffball, Ghost Pipe, Sarcodes, and Rafflesia.
- Lethean/Silthmere Poppy and wreath.
- Gourd, stem, tap, and blood-storage behavior.
- Smouldering Ash, Befouling Ash, and Virid Salis trails.
- Blood Crystal growth.
- Suspended Blood Crystal, Cleansed Crystal, and Vivianite displays.
- Earthen Veins, Hematic Sutures, and blood-routing nodes.
- Abocipher Emitters for structure ambience.
- Experimental Non-Euclidean Hallway blocks.

## Entities and Encounters

### Bosses

| Boss | Major implementation |
|---|---|
| Saint Seraphae | Containment integrity, anchors, fragments, staged states, models, and emissive rendering |
| Saint Hemorath | Saint framework integration and trial content |
| Saint Putriciel | Entity and shared Saint-framework implementation |
| Saint Velorum | Entity and shared Saint-framework implementation |
| Vesper, the Crowned Refusal | Phase-one Silent-path fight with missiles, hazards, adds, weapons, and tendency defenses |
| Vesper, the Evening Star | Phase-two flight, rage, stronger attacks, transition presentation, and Memory reward |
| The Mycophant | Apotheos-route fungal boss with flames, debuffs, summons, claw pressure, and transformation reward |
| Annetta Knowles | Branching Harbinger-versus-mercy encounter |
| Stained Priestess | Annetta's aggressive second Harbinger phase |
| Latent Annetta Infection | Mercy-route boss fought with cured Annetta as support |

### Wildlife and Ecology

- Hematic Burrower.
- Scarlet Serpent.
- Tooth Pecks.
- Verdigris Moth.
- Blood Lantern Jelly.
- Chalybeate Snail.
- Mnemonic Whale.
- Prism Cuttle.
- Venom-Rib Centipede.
- Desiccant.
- Lantern Tick.
- Bloodlicker.
- Brined Votary.
- Bog Revenant.
- Expanded integration for Barbed Urchins, Crimson Does, Venous Striders, and existing fauna.
- Wild Morphling Polyps with biome-derived layers and specimen capture.

### Wills and Supernatural Hostiles

- Rogue Will entities and anchors.
- Faltering and broken phases.
- Absorb, Redirect, Commandeer, and banishment interactions.
- Sanctuary checks.
- Equipment and weapon control.
- Hidden hive-attention attribution.
- Blood-Drunk Puppeteer tuning.
- Enthralled Doll ownership and string rendering.
- Unsettled Ichor.
- Awakened Ichorian Sigils.
- Humanity Sprites for Cardinal Rite finales.

### Summons and Controlled Entities

- Gorebound Hulk.
- Marrow Spitter.
- Mnemonist Puppet.
- Veinwing Vulture.
- Ground Puppet.
- Bound Puppeteer infrastructure.
- Pale Intercession / Our Lady's Manifestation.
- Phantasmal Echo.
- Blood Thrall.
- Arbor of Will.
- Armature Restraint.
- Covenant Throne seat.

### Projectiles

- Blood Chum.
- Constrictor Cord.
- Hemolytic Vial.
- Living Flail head with terrain interaction and recovery.
- Living Sickle Blood Hook.
- Vesper Scutes.
- Shared carried-weapon projectile support.

## Rites and Magic

### Degree-Advancement Rites

- Sanguine Initiation.
- Votary Rite.
- Initiate Rite.
- Illuminatus Rite.
- Sanctified Rite.
- Archon Rite.
- Apotheos Rite.
- Clarity Ascension for the Unstained route.

These rites are tied to chapter progression and reject redundant rank-ups.

### Covenant and Settlement Rites

- Founding Fane.
- Bloodline Founding.
- Bloodline Recall.
- Covenant Vigil.
- Crimson Beacon.
- Crimson Lodge consecration.
- Sanguine Dominion.
- Sanguine Fervor.
- Eternal Covenant.
- Persistent bloodline and fane state.

### Utility and Transformation Rites

- Ancestral Communion.
- Antiseptic Ground.
- Closed Vein.
- Exsanguination.
- Glass Lungs.
- Hematic Fortification.
- Hematic Unbinding.
- Hungering Earth.
- Moon-Washed Copper.
- Vascular Mending.
- Ashen, Crimson, and Pallid vessel rites.
- Horn of Culmination.
- Scarlet Summons.
- Brazier-based item, memory, and scar absorption.

### Qliphoth and Endgame Rites

- Chamber of Will.
- Bloom of the Qliphoth.
- Severed Qliphoth and Vesper ordeal lifecycle.
- Qliphoth Bloom ownership and saved state.
- First-visit projection and forced return.
- Fungal revelation and Silent-versus-Apotheos outcome tracking.

### Puppeteer Trials

Four dedicated Cardinal ordeals replaced ordinary crafting acquisition:

- Gorebound Hulk trial.
- Marrow Spitter trial.
- Mnemonist Puppet trial.
- Veinwing Vulture trial.

The system includes learned-shape tracking, caster ownership, ally roles, failure handling, and Puppet and Manipulation commands.

### Unstained Rites

- Still Waters.
- Pale Consecration.
- Pale Vigil.
- Lethean Baptism.
- Lethean Font.
- Lethean Judgment.
- Lethean Tide.
- Silver Dawn.
- Silver Veil.
- Silthmere's Remembrance.
- Severed Covenant.
- Unstained preflight validation and separate saved-data lifecycle.

### Cardinal Rite Runtime

The Cardinal Rite framework now handles:

- Authored ritual-floor definitions.
- Station matching and foundation validation.
- Offering consumption and bloodletting.
- Medium requirements.
- Planted Living Staff escrow.
- Boundary growth and player leashes.
- Pillars and socket geometry.
- Animated Ichorian sigils.
- Threat waves and allies.
- Instability and cancellation.
- Failure cleanup.
- Offering processions.
- Daemon emergence.
- Humanity-sprite finales.
- Persistent ritual and covenant state.

## Manipulations and Scars

### Noetic Blood Manipulations

- Black Veil Covenant
- Blood Eclipse Mantle
- Cauterizing Rebuke
- Conductive Mark
- Ferric Resonance
- Glacial Circulation
- Glacial Rampart
- Gloam Laceration
- Grave Debt
- Hematic Beacon
- Hematic Flare
- Hemolymphal Pulse
- Insatiable Hunger
- Iron Retort
- Lumen Suture
- Osseous Bloom
- Prismatic Reproof
- Sanguine Magnetism
- Scalding Updraft
- Synaptic Jolt
- Umbral Reversal
- Vascular Dowsing
- Vital Effusion

These are the projected, actively cast **Noetic** expression of Hematic Memory. Shared work added entity casting, tendency-aware hits, status ownership, Drudge casting, Will casting, cost gates, cerebral-scar routing, and synchronized mixed-expression loadouts.

### Fungal Scars

The cultivated roster was consolidated to:

- Antiphonomyces Resonans
- Cryostroma Perdurans
- Noctifly Agaric
- Oculiflora Reticularis
- Putrivora Resolvens
- Rhizovitta Communis
- Saprovitta Vestigium
- Talaromyces Minus

This included recipes, cultures, migration from retired scars, passive blood-income routing, reveal rendering, and Mycelial Crucible progression.

### Cerebral Scars

- Anvil
- Blight
- Blood-Honed
- Chimera
- Corona
- Crucible
- Descendence
- Eye
- Feral
- Flux
- Glacier
- Halo
- Heart
- Marrow
- Moon
- Oblivion
- Phoenix
- Pyre
- Rime
- Shade
- Sol
- Thorn
- Transcendence
- Veil
- Wither

Cerebral scars now use definition-backed knowledge, capacity, prepared patterns, tendency families, loadouts, and a circular scar interface.

## NPCs, Assignments, and Dialogue

### Harbinger NPCs

| NPC | Role |
|---|---|
| Vicar | Recruitment, Assignment Ledger, First Bloodcraft, doctrine, chapter gates, covenant guidance, and Degree 8 revelation |
| Alchemist | First Separation, machine instruction, Red Taxonomy, specimens, and biological processing |
| Artificer / Redwright | Hematic Armature, armor fittings, Blood Lust forks, Living Weapon Grafts, and late-game upgrades |
| Cicatrix Anchorite / Vein Mason | Cerebral scars, effigies, patterns, vascular diagnosis, and D5-D6 assignments |
| Mnemonist | Noetic Memory instruction, starter memories, Somatic Loom weaving, mixed-memory Reliquary loadouts, and Noetic discovery routes |
| Hermit | Optional Hermit Road, Sanguine Initiation blueprint, temple oath, and scripted farewell |
| Voyager | Reef, vent, and wreck research aboard the Survey Cog |
| Votary Wayfarer | Junior active-vessel observer |

Existing Harbinger NPCs also received item inquiries and degree-aware dialogue.

### Unstained NPCs

- Unstained Zealot.
- Unstained Guardian.
- Unstained Scout.
- Unstained Acolyte.
- Our Lady whisper and manifestation dialogue.
- Annetta's mercy-route dialogue and encounter state.

### Assignment and Journey Work

- Assignment Ledger categories:
  - Main
  - Side
  - Vocation
  - Catalogue
- D2-D7 chapter milestones.
- Return-ready tracking instead of inventory-only checks.
- First Bloodcraft reward flow.
- First Separation briefing, sample spinning, completion, and rewards.
- Red Taxonomy specimen objectives and rewards.
- Artificer lesson and fitting states.
- Mnemonist referral and Reliquary teaching.
- Vein Mason scar lessons and loadout milestones.
- Covenant Waybill navigation.
- Optional Hermit route rather than a hard dependency.
- Dialogue inquiries gated by player state, stack data, advancement, degree, or path.

## World and Structure Changes

### Biomes and Features

- Erythrocoral Reef biome and region.
- Expanded Fungal Gardens overworld integration.
- Fungal cave carver.
- Deep Ocean Vents.
- Erythrocoral Reef formations.
- Morphic Pools.
- Tooth Geodes.
- Venous Ridges.
- Silver Bells Towers.
- Improved biome-specific spawning and creature ecology.
- Blood Moon player-blood consequences.
- Feature-order corrections between Java bootstrap and biome JSON.

### Exploration Structures

- Broken Church and Annetta encounter scene.
- Harbinger Outpost with Vicar, Alchemist, and Mnemonist placement.
- Harbinger Voyager Wreck variants.
- Active Survey Cog / Voyager Vessel.
- Sanguine Surveyor Bivouac.
- Bog-Body Ossuary Niche.
- Crimson Lodge Annex.
- Mason's Respite.
- Hermitage Remnant.
- Unstained Church and tainted or broken variants.
- Blood Temple ambience improvements.
- Qliphoth Fane.
- Qliphoth Bloom structure.
- Blood Tower segments.
- Mausoleum work.
- Chthonian Termite Mound.
- Village Hemopothecary templates; village-pool injection remains disabled.
- Prototype Saint trial rooms and sarcophagus chambers.

### Instanced and Special Spaces

- Chamber of Will expansion and visit modes.
- Mental Refuge prototype with a custom skybox.
- Mnemonic Lowtide space with water, fog, roots, parchment, ruins, and shader effects.
- Owner-specific Vesper arena.
- Mycophant encounter management.
- Qliphoth projection and return positioning.
- Warp Chair binding and travel support.

## UI, Rendering, and Authoring

### Player Interfaces

- Complete NPC Dialogue Screen rewrite.
- Assignment Ledger screen.
- Harbinger Progress Screen split into tab controllers and views.
- Circular Scar tree.
- Crafting and Rites maps.
- Degree-layer navigation.
- Pan, zoom, drag, marquee selection, and authored node positions.
- Compact recipe inspectors and tooltips.
- Ritual-floor nodes.
- Bestiary entity previews with rotation and zoom.
- Sanguine Diagnosis screen.
- Morphling equipment overlay.
- Unstained Progress Screen.
- Radials for Noetic manipulations, armor abilities, Living Staff forms, and Thelemic Memories.
- Nine-slice scaling metadata for multiple GUI assets.

### Rendering

- Custom 3D item rendering for living weapons and armor.
- Cardinal Rite fog, stains, boundaries, tendrils, pillars, sigils, and projections.
- Living Ichorian anatomy with synchronized animation.
- Qliphoth Bloom geometry and black-hole shader.
- Vesper weapons, cocoon, transition, flight, and attack animation.
- Apotheos chamber wall, ceiling, and floor shaders.
- Lowtide water, haze, roots, and ruin presentation.
- Morphling mutation layers.
- Specimen Jar entity display.
- Liquid-level rendering for the incubator and distillation machines.
- Abocipher ambience.
- Guide-book animation-state isolation.
- Equipment visibility synchronization.

### Development Tools

- Dialogue editor and Java/JSON import and export.
- Skill-tree editor.
- Scar-tree editor.
- Crafting and Rites map editor.
- Material Atlas editor.
- Tendencies editor.
- Model-export tools.
- Preview and validation routes for Java-backed map data.
- Permanent dialogue-inquiry validation in CI.

## Testing, Documentation, and Technical Work

- Approximately 684 unit-test files and 21 GameTest files were added in the net comparison.
- Added journey fixtures for Harbinger, Artificer, and Unstained progression.
- Added focused checks for:
  - Recipes and degree gates.
  - Advancement progression.
  - Armor upgrades.
  - Entity resources.
  - Models and textures.
  - UI layout and hit testing.
  - Cardinal Rite geometry and lifecycle.
  - World-generation ordering.
  - Morphling rules.
  - Scar migration.
  - Living-arsenal duplication protection.
  - Dialogue inquiries.
- Consolidated generated resources into clearer authoritative paths.
- Expanded release, testing, lore, power-system, and implementation references.
- Performed a broad source-tree de-AI cleanup while preserving gameplay systems.

## Chronological Summary

### Late April

- First successful NeoForge build.
- Saint Seraphae implementation.
- Lethe-to-Silthmere terminology migration.
- Scar redesign.
- NPC item inquiries.
- Initial dialogue tooling.

### May

- Unstained equipment and NPC expansion.
- Ocean and structure world generation.
- Morphling presentation work.
- Boss AI and model passes.
- Hematic Armature.
- Circular skill trees.
- Early Vesper and Mycophant implementation.

### June

- Memory-weaving redesign.
- Expanded creature roster.
- Registry-backed scar loadouts.
- Silent Archon and advanced armor.
- Lowtide and Mental Refuge spaces.
- Material and progression authoring tools.

### July

- Power-system guardrails.
- Fungal-scar and Morphling reframes.
- Rogue Wills.
- New manipulations.
- Living Weapon Grafts.
- Assignment-driven progression.
- Dialogue overhaul.
- Cardinal Rite transformation.

### August

- Scar and recipe-map interface consolidation.
- Harbinger journey cleanup.
- Vesper fight completion.
- Qliphoth presentation.
- Unified Hematic Memory runtime with complementary Noetic and Thelemic expressions.
- Morphling-puppet interaction.
- Morphling death recovery.
- Late-game progression refinement.
- Public Noetic/Thelemic vocabulary and distinct embodied-versus-projected progression proofs.

## Current Work in Progress

The following work is present in the current working tree but is not part of the completed committed milestone set above:

- NPC-specific mission package organization.
- Early Harbinger and Unstained progression repairs.
- Blood Lust lineage-aware upgrade recipes.
- Silent Archon input corrections.
- Armature access and degree-gate adjustments.
- Journey and GameTest updates.
- Minecraft tag-path cleanup.
- Reference documentation updates.
- Final stabilization and live validation of the Noetic/Thelemic radial, tincture preparation, reserve, overexertion, mixed Reliquary/Distributor patterns, persistence, and multiplayer synchronization.

## Discussion Summary

The clearest progress-report framing is:

> Hemomancy has moved from porting and isolated feature restoration into integration. Its major systems now reinforce a connected early game, specialist-driven progression, ritual journey, and endgame choice.

The strongest demonstration candidates are:

1. Cardinal Rite activation and finale.
2. Vesper and the Qliphoth route.
3. Assignment-led Harbinger progression.
4. Morphling growth and combat behavior.
5. Living Staff weapon forms and grafts.
6. A side-by-side Noetic cast and action-triggered Thelemic response using the shared radial and Reliquary.
7. The redesigned dialogue and progression interfaces.

This report summarizes repository history and implementation evidence. It does not represent a fresh full build, live-client visual test, multiplayer test, or complete end-to-end progression run.
