# Puppetry and Circus Mechanics

## Implemented foundation

**IMPLEMENTED:** Circus work extends the existing puppetry stack. It does not replace it.

- `PuppeteersSpindleBlockEntity` prepares artificial bodies.
- An owner-attuned `MarionetteCrossbarItem` supplies summon charge and remains the reusable medium in Puppetry Cardinal Ordeals.
- `PuppeteerSummonDefinitions`, `PuppeteerSummonFactory`, `BoundPuppeteerSummon`, and `BoundSummonBehavior` are the shared definition, creation, ownership, upkeep, and lifecycle path.
- Cardinal Ordeals unlock a shape only after its manifested trial entity is defeated.
- A faltering Broken Will can be Commandeered, but remains a failed person rather than becoming an artificial puppet shape.

The ordinary ordeal roster contains seven shapes; Succession can add the eighth outcome-specific pattern:

| Shape | Degree | Implemented role |
| --- | ---: | --- |
| Veinwing Vulture | 3 | Flying striker |
| Marrow Juggler (`marrow_spitter`) | 3 | Three-dagger focused volley |
| Gorebound Hulk | 4 | Heavy bruiser |
| Scarlet Mummer | 4 | Attention controller |
| Sanguine Hound | 4 | Pursuit harrier |
| Cinder Bellows | 4 | Telegraphing fire cone |
| Mnemonist Puppet | 5 | Memory echo |
| Ringmaster Pattern | 4 | Low-damage conductor; Succession reward |

The Circus must not introduce a second summon registry, ownership capability, upkeep currency, command surface, or radial control system.

## Scarlet Mummer anchor

**IMPLEMENTED:** The Scarlet Mummer is the clearest current Circus vertical slice. It is a low-damage attention controller with:

- a group attention pulse against threats targeting its owner or allied puppets;
- a maximum of four redirected targets inside an eight-block radius;
- one readable melee evasion during its performance window;
- a narrow hematic performer silhouette, cobra-like gorget, and cuttle-like color language.

New Circus creatures or Ringmaster variants should complement this role rather than duplicating its redirect-and-evade loop or replacing it with generic damage.

## Circus mechanical vocabulary

**SETTLED:** Circus specialization works through advanced uses of existing systems.

| Verb | Intended expression | Boundary |
| --- | --- | --- |
| Redirect attention | Target manipulation, false threat, forced facing, readable aggro control | Must respect immunity and avoid permanent AI corruption. |
| Decoy | Short-lived duplicate, false position, or false wind-up | A visual decoy cannot secretly deal server damage. |
| Displace | Short teleport, evasive swap, entrance/exit movement | Must use safe-position checks and preserve dimension/border rules. |
| Flash | Light burst, color pulse, momentary concealment | Must remain readable and accessibility-conscious. |
| Sound | Echolocation, displaced cues, overwhelming cicada noise | Must not depend on audio alone for required combat information. |
| Manifest | Thought given a temporary server-side body | Uses ordinary entity ownership, cleanup, and persistence rules. |

These verbs are design vocabulary, not a request for a general ability framework.

## Professions through existing equipment

**SETTLED:** Reuse current Living Staff forms and manipulation families before adding equipment.

- A flame eater specializes in the existing torch form and fire kit.
- An animal tamer uses summon commands, targeting, and Crossbar control.
- A juggler uses existing projectile or orbiting visual language where it fits.
- An acrobat uses current mobility, displacement, and safe teleport seams.
- A Ringmaster coordinates multiple bodies and attention but does not receive Vesper's broad ability catalogue.

Only add a new form, topper, or item when it produces a route reward or mechanic that cannot be expressed by an existing form. The proposed Ringmaster staff is the main candidate and remains gated on the hostile-route design.

The implemented Ringmaster Pattern uses this same stack. While active it occupies one body slot and raises the shaped-body cap by one. It relays its owner's Crossbar mode and focus command once to same-owner, same-session bodies within 32 blocks; it never relays a relay, changes ownership, grants free upkeep, or increases damage. The route topper is cosmetic, and Liberation's Thread Ripper is a separate anti-tether manipulation rather than a replacement command surface.

## Perception versus mechanics

Every Circus effect must declare which side owns it:

- **Client presentation:** haze, color, apparent duplicates, impossible posture, false cloth, displaced ambient sound.
- **Server mechanics:** damage, targeting, collision, teleport destination, persistent creature, reward, discovery, acclimation, route state.

A client presentation may communicate a server mechanic, but cannot be its authority. Server entities should not multiply merely because two viewers see different presentation states.

## Mechanical safeguards

- Preserve the owner-attuned Crossbar and current puppet capacity/upkeep behavior.
- Preserve progression preflight before manifestation or resource consumption.
- Use shared safe-position checks for evasions and teleports.
- Release redirected targets and temporary manifestations on death, dismissal, owner loss, dimension separation, cancellation, and server shutdown.
- Do not make puppet damage recursively trigger attention, memory replay, death rewards, or another puppet's special behavior without an explicit rule.
- Respect existing friendly-target exclusions and Peaceful behavior.
- Keep flash and sound effects configurable enough to avoid inaccessible or physically unpleasant presentation.

## Focused verification expected from future work

- Pure rule tests for target eligibility, immunity, safe displacement, state transition, and cleanup.
- Source/resource guards for registered models, renderers, textures, translations, recipes, and structure data.
- GameTests for ownership, Crossbar consumption, capacity, route rewards, persistence, and entity cleanup.
- Live multiplayer checks for different perception states, tracking, audio locality, puppet commands, and disconnect/rejoin.

## Crimson Troupe school expansion

**IMPLEMENTED:** Personal professor instruction gates new Cardinal Ordeals. Existing known shapes satisfy instruction without granting practical mastery or rewards. Seven professors cover the ordinary shapes; the Threadkeeper inspects Crossbars and records reports, while the Ringmaster retains the outcome-specific conductor lesson. Harbinger Main progression remains independent.

Five 25x25 camps (air, blades, pageantry, embers and memory) cover the whole faculty. Newly generated pavilions use a 65x65 school compound around the preserved central arena. The original template remains available for saved pieces. Abandoned stages provide investigation evidence. Camps use spacing 24/separation 8; the pavilion's existing spacing 48/separation 16 remains unchanged. Each selected rotated template is checked across its full footprint for dry, clear terrain with at most three blocks of variation.

The Scarlet Waybill points to the next eligible untaught professor on normal use and searches for an unfinished pavilion on sneak-use. If all eligible instruction is known, normal use also searches for an unfinished pavilion. Completed nearby sites are excluded from the bounded fallback search; it may report no result when the bounded search cannot find another suitable site.

Instruction unlocks recipe knowledge; defeating the existing Cardinal Ordeal unlocks the body. Practicals use the player's own known bodies, existing commands, charge and upkeep. A five-minute local examination spawns two live rehearsal threats, then cleans them up on expiry, departure, dimension change, logout, or loss of the professor. Saved orphan threats are removed after a restart. Rehearsal threats and stage bodies award no drops or XP.

An Empty Ring proves inspection, call, active-body command and recall using one Crossbar. The seven practicals observe aerial pursuit and recall, a focused three-dagger volley, two attention diversions, a guarded Hulk strike and five seconds of Guard, a ruptured Hound's Cur strike, two threats in one Bellows breath, and a successful Mnemonist replay. The Wrong Audience requires two accounts and the Mummer/Juggler practicals. The Missing Understudy requires stage, loft, quarters and carousel records plus the retired professor's testimony. The carousel account states that severing its captives kills them.

Guest acts repeat a passed practical for four Enthralling Filaments once per Minecraft day. Partial delivery remains in personal NBT; Threadkeepers deliver only the exact remainder. Clone handling preserves instruction, practicals, investigation facts and reserved rewards. Milestones award 100 for An Empty Ring, 100 per practical and 150 per investigation, respecting the existing 1000 acclimation cap.

Cinder Bellows uses the shared summon catalogue: Degree 4, 30 HP, attack attribute 4, speed .24, call 48 and upkeep 16/minute. Its 900 mL Dominion Lesser ordeal consumes its frame after manifestation and preserves the Crossbar. The coil-and-lung body has a 20/40/40-tick inhale/breathe/recovery cycle, with four two-base-damage pulses in a five-block, sixty-degree cone. It respects ally filtering and line of sight and never places fire. The Juggler retains `marrow_spitter` IDs and a 35-tick attack cadence; three daggers share the original attack attribute. The Hulk retains stats and adds a readable strike wind-up.

Succession and Liberation keep their existing conditions, rewards and captive outcomes. New faculty are excluded from the original four-performer finale cast. Other camps retain instruction after a pavilion outcome. Decorative masks and rigging have recipes; investigation records have no survival recipe or drops.

Native assets and new NBT templates are rebuilt by `tools/circus/build_troupe_assets.py`; it never rewrites the legacy pavilion. `tools/circus/write_troupe_language.py` merges school text. Java models, portable Blockbench files and UV atlases are synchronized. Runtime GameTests and JVM model checks are separate from native client, fresh natural generation and multiplayer acceptance.

## Tangible pavilion manifestations

**IMPLEMENTED:** Shared attention gains material consequence through the existing personal acclimation score. Site presence and nearby active performers contribute at most one point per 80 eligible ticks. Discovery remains pavilion-specific and independent of blood activation or degree.

| Score | Personal experience |
| --- | --- |
| 0–149 | Ragged canvas and uniforms, mime-like peaceful acts, faint heat shimmer. |
| 150–499 | Fleeting decor, partial costumes, displaced noises and sparse harmless act effects. |
| 500–999 | Lower galleries, half-step stairs and seating support the player; higher architecture remains translucent. |
| 1000 | Upper gallery, domed canopy and ornate uniforms become coherent and privately solid. |

Private geometry uses server-authorized masks, acknowledged revisions and the same rotated authored cells for drawing, collision and targeting. It does not affect ordinary entities or become an obtainable block. Cells occupied during activation are deferred; terrain and fluid conflicts suppress them. Existing harmful attacks retain ordinary visibility and damage.

Succession commits the site's upward public reveal through its existing completion guard. Only loaded dry air is filled, occupied cells wait, and each placed or obstructed node is saved as processed so it cannot be farmed by repeated reconstruction. Real blocks then behave normally for everyone. Liberation withdraws private architecture and recovers current support to checked real floor. This adds no route, lesson, reward or degree requirement. Completed older sites are not retroactively populated.

Newly generated expanded pavilions retain ragged physical roof strips. The legacy template and all already-generated blocks remain intact. Public realization is confined to the completed pavilion; the five camps keep their personal stages. Client refraction can be disabled with `circusDistortion` without hiding collision surfaces.
