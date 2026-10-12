# Ferric and Ductilis

Ferric forms blood into rigid iron. Ductilis carries electrical impulses between bodies and conductors. Their combination creates temporary defenses that also carry current.

## Iron defenses

Sanguine Magnetism summons a pillar for six seconds. Its field pulls eligible enemies inward and pins bodies close to the column. When a player's pillar falls, it slams every enemy it pulled that is still within three blocks for 4 damage, or 6 if Lodestoned, scaled by Crimson Mastery. The slam is a Ferric hit, so it applies Lodestone and can shatter three-level Rime. Offensive Ductilis casts can energize summoned iron; an energized pillar also discharges into nearby enemies within five blocks.

Two remembered forms share Magnetism's mastery and selector. Absorb each form's memory and reach the required family level:

| Form | Family level | Behavior |
| --- | --- | --- |
| Ferric Rampart | 1 | Five blocks wide, three high, and three eighths of a block thick. Cardinally oriented plates block bodies. Your own and your allies' projectiles pass through; all other projectiles are destroyed. |
| Ferric Spikes | 2 | Five blades across a five-block line. After a brief blood tell, contact deals 3.5 Ferric damage (scaled by the caster's Crimson Mastery) and applies Lodestone, at most once every ten ticks per target. |

Ferric Rampart lasts ten seconds and costs 350 mL; Ferric Spikes lasts six seconds and costs 450 mL. Both use Magnetism's rank, range and cooldown. Placement needs firm support and clear space. Rejected placement costs no blood and starts no cooldown. Constructs do not replace blocks or drop items. Their saved expiry is absolute, so unloading does not extend their life. Encounter spikes keep their existing behavior.

Iron Choir keeps three interceptions per paid pulse; plates snap toward actual intercepted shots. Each pulse also looses one blood needle (about 3 damage, scaled by Crimson Mastery) at the nearest visible hostile within ten blocks. Iron Retort braces against melee and fractures when consumed. Ironhearted keeps its full-charge requirement, capacity, damage reserve, knockback protection and chest/HUD placement. Mending and conjuration gather blood at the held tool. Vascular Dowsing retains its ore-specific locator. Retired Ferric utilities remain retired.

## Forged needles

The Blood Needle family is Ferric. While charging, blood is drawn from the wrist and forged one needle at a time into square-section iron flechettes with tail fins; lances grow backward barbs and a full charge draws magnetic field lines into the bundle. The iron is solid and lit in flight, trailing a thin blood wake. Hits apply Lodestone like any Ferric strike, and a fully charged Lance pins non-boss targets for a second.

## Nerves and paralysis

Jolt immediately shocks and interrupts its target. Activation Potential discharges radially, with stronger damage and disruption as charge increases. Storm chooses each next target from the previous endpoint. It prioritizes conductive targets, then distance, with deterministic ties. It cannot revisit victims or arc through a wall. Ordinary hops reach five blocks; conductive targets and energized Ferric relays extend hops to eight. Acquisition stays within eighteen blocks of the caster, with at most eight victims.

Storm's dedicated paralysis stops voluntary movement, jumps, attacks, item use and casting, and interrupts existing channels. Camera control, gravity and damage processing continue. Ordinary mobs retain the charge-scaled 10–60 tick lock; players are capped at 20 ticks. Bosses receive reduced movement disruption. A 20-tick recovery period after paralysis ends prevents repeated full locks.

Interruption cancels an already held blood-manipulation charge and requires a fresh key press. Paralysis also blocks Still Arts, alternate casting controls, slime contact attacks and mounted jump releases. It cancels a creeper's natural attack windup; an intentionally lit fuse continues. Automatic defensive passives remain active. Death, logout, login, respawn and dimension changes clear the transient lock.

Hematic Rebuke and Impressment are Ductilis commands: each briefly Disrupts its target and fires a nerve bolt from the caster. Rebuke clamps a crackling, recoiling net over the skull and sends fearful pulses down the spine; Impressment holds a steady nerve leash to a blood-lit sigil at the nape. Conductive Mark identifies a preferred discharge target and retains its Lux interaction. Sanguine Ward shows its remaining protection pool as an electrical sheath. Living Circuit links the actual allied recipients and flashes when a recipient spends the conductive strike. Hemolymphal Pulse reveals blood-bearing bodies and marks wounded targets. Thread Ripper severs narrow nerve filaments while retaining puppet and encounter restrictions.

Deadly Gaze carries intent through the eyes. While it charges, an eye of nerve opens along the aim, fed by optic nerves sheathed in ink; its slit pupil narrows and a faint sightline reaches out as the charge grows. A hit seizes the target in nerve tendrils that wind up its body to a knot above the head, tethered back to the caster's eyes while ink drips from the lifted body. A full charge crackles around the seized body and then slams it into a bursting pool of ink split by violet cracks.

Blood-iron is forged rather than boxed: every plate has chamfered faces that catch the light, and larger plates carry rivets. When a rampart, spike line or pillar expires it shatters, its plates drifting outward, tumbling and falling instead of fading in place.

## Environmental conduction

An offensive Ductilis cast can strike a conductor directly or energize one touched by its victim. Marking, detection and support links do not independently electrify terrain.

Water and blocks in `hemomancy:ductilis_conductors` connect through shared faces. The shipped tag includes iron construction blocks, iron bars, chains, rails, anvils, copper construction variants, lightning rods and Hematic Iron counterparts. Oxidized and waxed copper remain conductive; ores do not. Datapacks can extend the tag.

Each discharge visits at most 256 loaded nodes within twelve blocks of its entry point. Connected material may carry current around an obstacle. Air arcs still require an unobstructed path. The network lasts forty ticks and rechecks connectivity before each ten-tick pulse. A pulse hits at most three eligible touching targets for two base damage and brief movement impairment. Direct hits, marked arcs, relays and network pulses share hit accounting. Caster, team, ownership and PvP protections apply throughout.

Conduction changes no blocks, redstone, oxidation or fluids. Electricity expires before or with its construct, and transient networks clear on source loss, dimension changes and server shutdown.

## Presentation and review

Opaque iron faces write depth and use procedural rust, wet blood, formation and fracture. Nervous electricity uses thin luminous ribbons, branches, travelling impulses and residual haze. HutosLib bolts retain the strike paths; its entity anchors drive moving links. Water traces follow exposed connected fluid surfaces; metal traces follow exposed block shapes. Essential states remain visible with Minimal particles.

Visual forms append to the existing ordinal list. Existing visual and Lux/Umbra flow packet layouts are unchanged. Conduction paths have a separate payload capped at 256 positions. Ongoing marks, guards, paralysis and links replay to new observers and retire when their source or lifetime ends.

The disposable review runs real paid player casts, supported NPC casts, reactive defenses, moving allies, daylight/night, first/third person, Minimal particles, dense targets and resource reload. Its player-control scene holds movement, jump, attack and use inputs during paralysis, rotates the camera, then verifies movement resumes. It requires the existing `VisceralReview` save under `build/visceral-review-client`. It edits only that disposable world and its settings.

```powershell
python tools/validate_ferric_ductilis_shaders.py
.\gradlew.bat -I tools/ferric-ductilis-review.init.gradle runGameTestServer
.\gradlew.bat -I tools/ferric-ductilis-review.init.gradle -PferricReviewLabel=final -PferricMultiplayer -PferricReviewModes=3 runClient
# Once the first client reports FERRIC OBSERVER READY, in another shell:
.\tools\review-ferric-observer.ps1 -Label final
# After both clients exit:
python tools/assemble_ferric_review.py --label final
```

The observer launcher prepares arguments without rebuilding dependency JARs under a running client. Do not run a build concurrently with the review. Captures go to each disposable client's screenshots directory. Frame intervals are recorded in `ferric-final-frame-times.csv`; screenshot capture and a second client affect those timings, so they are not isolated shader costs. The assembly script writes MP4 clips and timing summaries under `build/ferric-ductilis-review`.

Numeric limits are initial tuning defaults. Automated checks do not establish final combat balance or replace player visual acceptance.
