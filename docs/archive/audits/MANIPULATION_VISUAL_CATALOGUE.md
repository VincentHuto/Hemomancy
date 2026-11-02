# Manipulation visual catalogue and touch-up analysis

Snapshot: 2026-10-09, from source only. Nothing here was re-checked in game. It inventories every active manipulation's visual, explains how each school's look is produced, and grades each effect against [the Hematic Riposte standard](../../MANIPULATION_VISUALS.md) to plan later touch-ups. Current source is authoritative; re-verify a row before acting on it.

## 1. How a manipulation visual is produced

Server code calls `ManipulationVisuals.burst(...)` (world-anchored) or `ManipulationVisuals.attached(entity, ...)` (entity-keyed). Either sends a `ManipulationVisualPacket(form, entityId, from, to, radius, ticks, count)`. On the client, `ManipulationVisualRenderer.accept` turns it into a `Cue` with a `ManipulationLifecycle` presentation clock. Each frame, the cue is offered to the renderer tiers in this order; the first that `handles(form)` draws it:

| Order | Tier | Material(s) / shader | Character |
|---|---|---|---|
| 1 | `FerricDuctilisGeometry` | `IRON` (solid, lit) + `DUCTILIS` flow | Solid iron boxes/spikes; tapered nerve ribbons |
| 2 | `HematicRiposteGeometry` | `ANIMUS` flow | Bespoke layered limb (the reference) |
| 2b | `PuppetStringsGeometry` | `ANIMUS` flow | Puppet arrival (`PUPPET_DESCEND`) and departure (`PUPPET_UNSPOOL`) built to the Riposte standard: layered vein strings, knitting/peeling sinew, knots, wisps and a departure flash; timeline in `PuppetStringsRules` |
| 2c | `LodestoneCollapseGeometry` | `IRON` (solid) + `DUCTILIS` flow | Bespoke Ferric Perfectus collapse (`LODESTONE_COLLAPSE`); also admitted to the `AFTER_ENTITIES` solid pass |
| 2d | `UmbralRendGeometry` | `UMBRA` flow | Bespoke Tenebris Summa tear (`UMBRAL_REND`) |
| 2e | `SearingLashGeometry`, `CruorJavelinGeometry`, `RadiantWakeGeometry`, `FruitingContagionGeometry` | `FLAME`+`MOLTEN`, `CRUOR`+solid `CORE`, `LUX`, `MORTEM` | Bespoke balance-pass effects (`SEARING_LASH`, `CRUOR_JAVELIN`, `RADIANT_WAKE`, `CONTAGION`) |
| 0 | Body-anchored casts: `BloodRushGeometry`, `SanguineWardGeometry`, `SanguineMendingGeometry`, `WeaponConjureGeometry`, `CellHandGeometry`, `AvatarManifestGeometry` | `ANIMUS` (+`FLAME`), `DUCTILIS` (+solid `IRON`), each school for the conjure | Drawn first, from the caster's pose (`CasterFrame`: body/head yaw, scale, the posed main hand, motion). Hooked by `ManipulationVisualRenderer.bodyAnchored`; timelines in pure rules classes |
| 3 | `AnimusMortemGeometry` | `ANIMUS` / `MORTEM` flow | Streams, cards, spirals; Mortem ground patches |
| 4 | `ThermalGeometry` | `FLAME`, `MOLTEN`, `GLASS`, `CRUOR` + solid `CORE` | Flame cards, faceted chunks, glass shards, surface pools |
| 5 | `LuxUmbraGeometry` | `LUX` / `UMBRA` flow | Light filaments, beams, shells; ink smoke and pools |
| 6 | Legacy fallthrough | `ManipulationMaterials` textured types via `CoalescingVertexConsumer` + `UndulatingVertexConsumer` | `VisceralMesh` tubes, drops and strands, plus `ManipulationSignatureEffects` |

Shared primitives (`LuxUmbraGeometry`): `stream` (12-station bent ribbon), `ribbonPath` (camera-facing strip along points, with fold protection), `ribbon`, `card` (billboard), `ground` (flat card), `slashOffsets`.

**Shapes are procedural.** A vertex's `u` encodes `shape*2 + u` and its `v` encodes `seed*2 + v`. The school shader draws the shape (`RIBBON=0, WISP=1, POOL=2, EYE=3, FOCUS=4, BEAM=5, RIPPLE=6, SLASH=7, WHITE_BEAM=8, LUX_SHELL=9`, Mortem `10` = spore). A school only looks rich for the shapes its shader actually implements (see §3).

**Lifecycle.** The default entrance and exit fades are each `min(8, max(1, ticks*0.25))` ticks. Keyed cues refresh rather than restart. `WHITE_VERDICT` uses an afterglow clock. Retiring cues fade over 8 ticks of residue.

**Systems outside the tiers** (also part of the look):

| System | Role |
|---|---|
| `MortemSkinLayer`, `ThermalSkinLayer` | Model-following fissures, frost and sear on affected bodies |
| `SchoolStateVisuals` | Body-relative state meters (Pressure, Necrosis, Searing, Rime, Disrupted, Illuminated, Obscured/Veiled, Lodestone); drawn even on Minimal particles |
| `ManipulationMotes`, `ManipulationAmbientParticles`, `ThermalParticles` | Bounded decorative accents (drops, fragments, vapour, residue, sprites) |
| `ManipulationBloodFormation` | Ligaments and beads while a form coalesces or dissolves |
| `ManipulationBoundaryStyle` | Range rings for Well, Stillness, Beacon, Furnace, Cloud, Magnet, Rupture, Updraft |
| `BloodProjectileEffects`, `BloodFlowEffects`, `BloodBindingTendrilRenderer`, `BlackVeilRenderer`, `LivingTorchFlames`, `FerricDuctilisEffects` | Per-system trails, flows, coils and veils |
| `DuctilisLightningEffects` → HutosLib lightning | Forked, restriking bolts for most Ductilis casts |
| `HemomancyTendrilEffects` → HutosLib `TendrilEffectSpawner` | Lux/Umbra release, Lumen Suture, Blood Eclipse, Black Veil, Hemorrhage, Exsanguinate, Osseous Bloom, Glacial Grasp |
| `client/render/outpouring/*` (`CastVeinsLayer`, `OutpouringRenderer`, `OutpouringAccents`, `OutpouringScreenPasses`, toolkit bridge `OutpouringBrushes`) | Cast outpouring: the caster's blood leaving the vascular section, shader veins, fading marks, Lux light and Tenebris darkness passes (replaced the per-school anatomy and Lux wings on 2026-10-19) |
| `ManipulationVisualRenderer.projectileSword`, `BloodNeedleRenderer` | Coronation sword projectiles (`VisceralMesh.sword` on the Animus material) and needle meshes |

## 2. Grading key

| Grade | Meaning |
|---|---|
| **A** | Riposte-grade: bespoke timeline, layered anatomy, accents, anchoring, impact flash |
| **B** | Structured and material-true: multiple element types or real 3D/surface work, but generic timing and no layered anatomy |
| **C** | Basic: one or two primitive types (a few streams/cards/spirals) on the school material |
| **D** | Legacy or placeholder: old textured `VisceralMesh` path, a borrowed school, or no cue at all |

## 3. School catalogue

### Animus: fresh, glossy liquid blood

- **Shader** (`manipulation_animus.fsh`): fbm-flow crimson `mix(0.20,0.012,0.042 → 0.78,0.045,0.13)` with a pink shine `(1.0,0.38,0.43)`. It implements RIBBON (bent, shining band), WISP (soft density), POOL (swirling liquid), RIPPLE (ring) and shell. **Every other shape, including FOCUS and SLASH, renders as the same glossy droplet.**
- **Strengths:** convincing wet surface and highlights; Riposte proves the material can carry a focal effect.
- **Weakness:** outside Riposte, most forms are a handful of equal-width streams plus a FOCUS blob.

| Manipulation | Form(s) / path | What is drawn | Grade |
|---|---|---|---|
| hematic_riposte | `RIPOSTE`, `RIPOSTE_STRIKE` | Bespoke arm (see standard) | **A** |
| crimson_coronation | `CrimsonCoronationGeometry`: `CROWN`, `CROWN_CHARGE` | **Touched up 2026-10-09:** crown ring of blood with a spike per slot; each armed sword hangs from its spike by twisted sinew with a running bead, leans outward and condenses in turn | A? (pending in-game review) |
| blood_binding, chain_/lingering_blood_binding, blood_lattice | `BloodBindingTendrilRenderer` | Tick-sampled ground-contact coils with one liquid surface | B |
| hematic_mortar | `MORTAR_CHARGE`; `BloodShotEntity` | **Touched up 2026-10-09:** a heavy payload gathers: sheath, glossy core, clots drawn into orbit and drips it cannot yet hold | B |
| blood_aneurysm | `BloodAneurysmGeometry`: `ANEURYSM_CHARGE`, `ANEURYSM_RUPTURE` | **Touched up 2026-10-09:** veined knot swells at the hand, beating faster with charge and trembling when full; on release a bulging vessel reaches the victim, a veined sac swells then tears: flash, flung shreds, ruptured vessels going limp, spherical spray, lingering Mortem haze. Vanilla dust removed | A? (pending in-game review) |
| blood_cloud, expansive_, pursuing_, sanguine_tempest | `CLOUD` via `BloodCloudCarrierEntity` | **Touched up 2026-10-09:** layered cumulus with a clotted underbelly, a beating heart inside, and streaked rain falling below | B |
| blood_rush | `BloodRushGeometry`: `RUSH` | **Rebuilt 2026-10-19 (pending in-game review)**: heartbeat rings pump down from a swelling chest knot while the leg veins fill; on the kick-off a splash rings the ground where the caster stood and a sheath of blood streams back off the shoulders and hips against the real travel direction, flame licking the heels; the sheath frays upward. Timeline in `BloodRushRules` | A? |
| vital_effusion | `GROWTH` | **Touched up 2026-10-09:** vein-roots climb from the ground, branching and budding | B |
| blood_shot, guided_blood_shot, sanguine_halo, hematic_mortar (flight) | `BloodShotGeometry` via `BloodProjectileEffects`; `BLOOD_SPLASH` entity/block/mortar variants | **Touched up 2026-10-09:** layered droplet (sheath, core, clot, highlight, wobble), tapered wake + twisting sinew; guided feelers, mortar clots + drips, halo tether; timed splash on contact, block splash at the moment of lodging | A? (pending in-game review) |
| conjure_* weapon forms and conjure_staff | `WeaponConjureGeometry`: `WEAPON_CONJURE` (count = weapon + variant) | **Rebuilt 2026-10-19 (pending in-game review)**: one class for the whole Living Arsenal. Strands leave the forearm and knot at the grip; fresh blood spreads from the grip over the real weapon's own faces, in the hand that actually holds it (`HeldWeaponPose` records the held pose each frame, first and third person, and captures each weapon's shape by rendering it into a recorder; the authored outline in `WeaponConjureRules` is only the fallback for echo casts and undrawn casters); a pale sweep hardens it into the weapon's school (Animus blade, Ferric axe/staff with iron plates on the edges, Lux spear, Umbra claws on both hands, Ductilis crossbow, Flammeus torch with a lit head, Cruor flail with chain and clot, Mortem sickle); it seats into the grip with a flash as the real item forms. Sent from `LivingStaffWeaponFormHelper.syncMorph` (the old per-tendency cue in `StaffWeaponFormManip` was never reached from the key paths, and the axe and claws had none). Strokes in `WeaponConjureRules` | A? |
| summon_avatar family | `AvatarManifestGeometry`: `AVATAR_MANIFEST` (count = stage, dismissal +10), over the avatar layer | **Rebuilt 2026-10-19 (pending in-game review)**: blood wells from the body's exits into a pool and climbs the body in rings; sinew lashes out to what the stage grows (spine; arms; ribcage; leg coils; hood) and the avatar breaks the surface with a splash. Dismissed, the shell slides down in drip curtains into a pool that drains back up the legs. Sent from `AvatarManifestationManager.toggle`/`dismiss` | A? |
| sovereign_instinct | Reactive → `HematicCommandManager.redirect` + `SOVEREIGN_LASH` | **Touched up 2026-10-09:** a whip of sinew lashes from the caster to every redirected mob and snaps taut, with a crown flare; mobs keep the command crown | B |

### Mortem: dried crust, rot and hyphae

- **Shader** (`manipulation_mortem.fsh`): slow rot noise, brown-black crust `(0.095,0.023,0.025 → 0.34,0.13,0.075)`, pale hyphae `(0.59,0.52,0.32)`. It implements RIBBON, WISP (warped), POOL/8 (sludge with capillaries), RIPPLE, a BELL shell (7), spore (10) and a soft blob fallback.
- **Strengths:** the most distinctive shader palette; the skin layer carries the detailed fissures on bodies; the ground patches follow terrain.
- **Weakness:** world geometry is minimal (mostly three wisps per status). The bell-shell and hypha shapes are underused.

| Manipulation | Form(s) / path | What is drawn | Grade |
|---|---|---|---|
| fruiting_contagion | `FruitingContagionGeometry`: `CONTAGION` (fruiting / spreading variants); Mortem rot infection on seeded bodies | Bespoke, timeline in `FruitingContagionRules`: pale stalks push out of the carrier over a rot pool and burst into spore clouds; a hyphal thread and drifting spores arc to each newly seeded neighbour, which sprouts a small bloom | A? (pending in-game review) |
| funeral_bell | `FuneralBellGeometry`: `BELL`, `BELL_CHARGE`; `MORTEM_BURST` on hits | **Touched up 2026-10-09:** bell condenses from its crown on a tendon rope, sways with charge; toll swings it on its pivot against a clapper, bursts a sound ring and rolls three echo rings to its reach over a rot pool, souls rise, spores drift | A? (pending in-game review) |
| bloom_of_rot | `BLOOM` + surface patches | Terrain-following rot patches + 7 wisps | B |
| hemorrhage | `WOUND` + `MortemSkinLayer` + tendril | Skin fissures. **Touched up 2026-10-09:** dark drops fall streaking into a pool under the feet | B |
| insatiable_hunger | `HUNGER`, `HUNGER_COLLAPSE` + skin | Skin. **Touched up 2026-10-09:** strands spiral inward into a starving core; collapse streams on expiry | B |
| grave_debt | `GRAVE`, `GRAVE_REFUND` + skin | Skin. **Touched up 2026-10-09:** grave dust settles over a faint grave outline under the feet; refund stream with beads | B |
| blackhearted | `BLACK_HEART`, `BLACKHEART_RUPTURE` | **Touched up 2026-10-09:** beating clot in a soft sheath wrapped by clotted veins that thicken with saturation; burst gets a hot core flash | B |
| carrion_communion | `COMMUNION` | 3 rising streams. **Touched up 2026-10-09:** over a shared blood pool | C+ |
| exsanguinate | `MORTEM_BURST` + tendril | **Touched up 2026-10-09:** hot core flash, tapered streaks and clotted beads (shared with Animus impacts) | B |
| crimson_tithe | `DEBT`, `TITHE_COLLECT/RETURN` + HUD | Clot FOCUS + spiral; collapse streams | C |
| lignum_mortis, worked_lignum, canopy_mortis | `BloodFlowVisuals` → `BloodFlowEffects` | Flow lines (separate system) | B? (verify in game) |
| conjure_axe, conjure_sickle | `WEAPON_CONJURE` | See the Living Arsenal row above. `MORTEM_CONJURE` remains for echo casts only | A? |
| (status) rot infection | `ROT_INFECTION` | **Touched up 2026-10-09:** spores puff up off the body over a hyphae patch | B |

### Ductilis: nerve current

- **Shader** (`manipulation_ductilis.fsh`): four shapes: nerve ribbon (0), density glow (1), jittering arc (2) and pulse flow (3). Colour is vertex-tinted `NERVE 0xFFE8A0`, with `BLOOD 0x9C263B` for gathers.
- **Strengths:** HutosLib lightning (forks, restrikes) gives the bolts real energy. `MARK`/`PARALYSIS`/`CIRCUIT` size their nerve paths from the target's bounding box, the best body-fitting outside Riposte.
- **Weakness:** the non-bolt forms are thin ribbon bundles with no core/glow layering, and rings are built from 24 separate ribbons.

| Manipulation | Form(s) / path | What is drawn | Grade |
|---|---|---|---|
| hematic_rebuke, hematic_impressment | `NerveCommandGeometry`: `NERVE_COMMAND` (rebuke / impress variants); HutosLib bolt on cast | Bespoke Ductilis, rules in `NerveCommandRules`. Rebuke: a crackling impulse leaves the caster's eyes, a jittering cranial net clamps the skull and recoils on arrival, fearful pulses race down the spine into twitching legs. Impressment: a calm impulse, then a steady leash from caster to a blood-lit nape sigil with slow loyal pulses travelling along it and down the spine. Sovereign Instinct keeps the Animus `COMMAND` crown; Marionette threads still legacy `VisceralMesh` | A? (pending in-game review) |
| activation_potential, synaptic_jolt, synaptic_storm, hemolymphal_pulse, conductive_mark, living_circuit, sanguine_ward | `DuctilisLightningEffects` (HutosLib bolts) | Forked bolts with restrike; nervous impact | B |
| activation_potential, synaptic_storm (charge) | `LIGHTNING_CHARGE` | 7 converging ribbons + glow. **Touched up 2026-10-09:** crackling arcs and a turning nerve ring that flare at full charge | B |
| conductive_mark; paralysis status; living_circuit | `MARK`, `PARALYSIS`, `CIRCUIT` | 5–7 bbox-fitted nerve paths with branches; head glow when paralysed | B |
| sanguine_ward | `SanguineWardGeometry`: `WARD` (count = pool) | **Rebuilt 2026-10-19 (pending in-game review)**: two counter-wound families of nerve strands climb the body into a diamond lattice over a faint glow shell, with knots at the crossings and a bead of current running up each lit strand. Each point of pool keeps one strand of each family lit; spent strands are dark frayed stubs; a crown ring closes over the head on a full pool (the old crown waited for 8 strands but the pool tops out at 6, so it never drew). Rules in `SanguineWardRules` | A? |
| thread_ripper | `THREAD`, `THREAD_CHARGE` | 3 parallel ribbons that part in the middle + flash. **Touched up 2026-10-09:** severed ends fray and glow | C+ |
| hemolymphal_pulse | `NERVE_PULSE` | **Touched up 2026-10-09:** one continuous expanding nerve loop over a fading glow | B |
| (impact) | `NERVE_HIT` | **Touched up 2026-10-09:** forking radial branches with a crackle burst | B |
| deadly_gaze | `DeadlyGazeGeometry`: `GAZE_CHARGE`, `GAZE_GRIP`, `GAZE_SLAM` (Ductilis + Umbra) | **Touched up 2026-10-09:** nerve eye opens along the aim with ink slit pupil, optic nerves and sightline; tendrils seize the target to a knot with a tether to the caster's eyes and ink drips; slam bursts an ink pool with violet cracks and ground crackle. Drudge gaze uses the grip too | A? (pending in-game review) |
| conjure_crossbow, conjure_staff forms | `WEAPON_CONJURE` | See the Living Arsenal row in Animus. `FERRIC_CONJURE` remains for echo casts only | A? |

### Ferric: rigid blood-iron

- **Shader** (`manipulation_ferric.fsh`): lit metal grain, fine scratches, rust bloom, bevel rim and seam. Faces dissolve by a formation threshold while the rim stays visible. Drawn as solid, lit geometry in the `AFTER_ENTITIES` pass.
- **Strengths:** the only truly opaque, physical school; formation dissolve and blood "gather" ribbons sell creation; Retort and Ironhearted anchor to the body.
- **Weakness:** every iron shape is an axis-aligned box or a 4-sided spike. There's no bevelled plate, rivet, or crack-and-shatter on break.

| Manipulation | Form(s) / path | What is drawn | Grade |
|---|---|---|---|
| lodestone_collapse | `LodestoneCollapseGeometry`: `LODESTONE_COLLAPSE`; `IRON_CHARGE` while charging | Bespoke, timeline in `LodestoneCollapseRules`: field lines run inward and tighten; ten chamfered shards spiral in from the rim trailing blood threads, tumbling point-first; they slam into a grinding knot with a pale flash and blood squeezed out between the plates; the knot sinks and rusts away over a fading ring. Iron in the solid pass, field and blood in the Ductilis flow | A? (pending in-game review) |
| blood_needle, _fan, _lance | `ForgedNeedleGeometry`: `NEEDLE_CHARGE`, `FAN_CHARGE`, `LANCE_CHARGE`; forged flight via `BloodProjectileEffects` (iron in the solid pass, blood in the Ductilis flow) | Bespoke Ferric, rules in `ForgedNeedleRules`. Blood is drawn from the wrist in gather ribbons and forged one needle at a time into square-section iron flechettes with tail fins; lances grow backward barbs; a full charge draws magnetic field lines into the bundle. In flight a thin blood wake and a hot glint at the point; lodged, the iron shivers, beads and stains. Living Pistol, Bloodburst and Vesper needles keep the glossy blood `BloodNeedleGeometry` | A? (pending in-game review) |
| iron_choir | `CHOIR`, `FERRIC_IMPACT` | Up to 3 orbiting 3-plate shields; flying plate that spins on impact + blood spatter. **Touched up 2026-10-09:** every Ferric plate is now chamfered with rivets on larger plates | B+ |
| ironhearted | `IRON_HEART`, `IRON_CHARGE` | 3 chest plates via `IronHeartAttachment` + blood ribbon; gather while forming | B |
| iron_retort | `RETORT` | 3 plates on the body, rotated to body yaw | B |
| sanguine_magnetism | `MAGNET` + `EntityIronPillar` construct | Plate-stack pillar with spikes; inward field ribbons | B |
| Living Axe Lodestone field | `LodestoneFieldGeometry`: `LODESTONE_FIELD` | Bespoke Ferric, timeline in `LodestoneFieldRules` (120 ticks, synced to the 20-tick gameplay pulse). The critical bite drives a forged lodestone up out of the ground with an overshoot, cracks the floor to the rim and flashes the core. A ring of iron teeth marks the 2.5-block reach, and arched flux lines run from the rim into the lodestone. Iron filings stand on end along them and creep inward. Each pulse sends a contraction ripple from rim to core: the teeth snap inward, the filings lurch, and the lodestone flares, judders and spits blood. At the end, the iron rusts and sinks over a spreading stain. Iron in the solid pass, field and blood in the Ductilis flow. Replaces the generic `MAGNET` ring | A? (pending in-game review) |
| ferric_rampart, ferric_spikes | Construct entities | 5×3 plate wall / spike line; gather on form. **Touched up 2026-10-09:** chamfered, riveted plates; walls and pillars shatter apart (outward, tumbling, falling) as they expire | B+ |
| sanguine_mending | `SanguineMendingGeometry`: `MENDING` (one cue per 20-tick channel pulse) | **Rebuilt 2026-10-19 (pending in-game review)**: blood leaves the wrist and winds along the held tool inside a thin sheath; each dark crack in the tool is stitched closed by pale thread in turn and set with a blood-iron rivet (solid pass); the blood draws back into the wrist. Replaces the chest stitches, which healed a wound the spell does not touch. Rules in `SanguineMendingRules` | A? |
| vascular_dowsing | `ORE` (legacy locator box) | Locator box outline | D (acceptable as UI) |
| blood_absorption, blood_projection | `CellHandGeometry`: `CELL_HAND` (summon / dispel / projecting / absorbing) | **Rebuilt 2026-10-19 (pending in-game review)**: summoned, the forearm's veins burst into strands that lash out and coil round the hand until a shell sets with a flash (jets for projection, hooks for absorption); dispelled, it unravels back down the arm. In use, the hand reaches its real targets, resolved by `CellHandParticleEffects.useTargets` exactly as the cell particles resolve them: projection throws a heavy stream from the palm onto the struck surface (or a feeding craft's fill line), breaking into drops before it lands and splashing, crowning and pooling there; absorption draws blood out of each drained body, blood block, Will or rite where capillaries root into it, in a stream that swells as it fills toward the palm. Every stream is liquid, not wire: a glossy sheath, a body that bulges with slugs of blood travelling with the flow (`LuxUmbraGeometry.ribbonPath` with per-point widths), a dark underside and a wet highlight, with a thinner second stream braided or peeling alongside, and a Riposte-style whirl (two translucent strands spiralling about it and travelling with the flow, flickering wisps trailing against it). With no target, projection spurts a short sagging stream that drips, and absorption gropes with fine fluid feelers. Layers over the existing cell particles | A? |

### Flammeus: dark crimson fire, smoke and molten blood

- **Shaders:** `manipulation_flame.fsh` draws a tapered crimson flame `(0.24,0.003,0.018 → 0.82,0.055,0.065)` with near-black smoke tips and a separate torch mode. `manipulation_molten.fsh` draws domain-warped boiling blood with an orange hot core `(0.96,0.16,0.007)`. `manipulation_glass.fsh` draws sanguine glass with rim, glint and crack.
- **Strengths:** the strongest material set. Pools follow the surface (`ThermalSurface`), shards fly as physics fragments (`ThermalFragments`), and glass and molten chunks are real faceted 3D. Flame, smoke and molten are layered together.
- **Weakness:** fire itself is a swarm of upright camera cards placed on golden-angle rings, so every fire spell shares one silhouette. Timings are generic.

| Manipulation | Form(s) | What is drawn | Grade |
|---|---|---|---|
| searing_lash | `SearingLashGeometry`: `SEARING_LASH` | Bespoke, timeline in `SearingLashRules`: a molten-cored flame whip uncoils from the hand with a travelling wave, cracks at the target in a flare with flame tongues and embers, then sags and burns away into smoke. Swing side follows the main arm | A? (pending in-game review) |
| vitric_combustion | `GLASS`, `GLASS_CHARGE` | 30 faceted glass shards on physics fragments + flash + smoke; charge orbits 11 shards | **B+** |
| furnace_veins | `FURNACE` | Surface-following molten pools + smoke. **Touched up 2026-10-09:** flame cards replaced with swaying, leaning tongues | B+ |
| sanguine_ignition | `IGNITION` | **Touched up 2026-10-09:** continuous bent flame tongues (`flameRibbon`) thrown outward over pools | B+ |
| expulsive/scalding/soaring/suspended_updraft | `UPDRAFT` (count = mode) | **Touched up 2026-10-09:** tongues spiral up the column; blast mode still throws molten droplets | B+ |
| pyretic_forge | `FORGE` | Rotating molten chunk + molten ground rings + flames | B |
| phoenix_debt | `PhoenixDebtGeometry`: `PHOENIX` (now attached to the body), `PHOENIX_READY` | **Touched up 2026-10-09:** flame column + molten shock pool; jointed molten-bone wings with leading-edge flames and splayed flame primaries/coverts emerge, unfurl with overshoot, beat twice shedding embers, fold to smoke; molten heart in the chest; armed = beating ember heart + banked folded wings | A? (pending in-game review) |
| cauterizing_rebuke | `CAUTERIZE` + `ThermalSkinLayer` | **Touched up 2026-10-09:** a glowing molten seam closes while short tongues lick off it and gutter out; skin sear | B |
| crimson_flame_conjuration | `FLAME_CONJURE` | **Touched up 2026-10-09:** small flame tongues lick up round the body | B |
| conjure_torch | `LivingTorchFlames` | Moving flame tongues on the torch pose | B |

### Congeatio: frozen cruor

- **Shader** (`manipulation_cruor.fsh`): opaque wine-red grain core `(0.065,0.009,0.026 → 0.27,0.035,0.078)`, frost/crack layer toward pale `(0.8,0.89,0.91)`, powder, and dithered translucency. Clots are a solid `CORE` chunk inside a 1.8% larger frost-shell chunk.
- **Strengths:** clear two-layer material (core + frost shell) gives real depth; ground cracks follow the surface; `CRUOR_SURFACE` frosts exposed block faces; Hour Break shatters on fragments.
- **Weakness:** every effect is the same clot chunk scattered on golden-angle rings, so Grasp, Pulse, Stillness, Rimebound and Hour differ mostly in count and size.

| Manipulation | Form(s) | What is drawn | Grade |
|---|---|---|---|
| cruor_javelin | `CruorJavelinGeometry`: `CRUOR_JAVELIN`; `FROZEN_VEINS` on the struck body | Bespoke, timeline in `CruorJavelinRules`: three frost strands freeze around a spine of solid cruor clots at the hand, the shaft flies point-first shedding powder, frost fingers race across the body it strikes, then it shatters and falls | A? (pending in-game review) |
| glacial_grasp | `ICE`, `FROST_ADVANCE`, `FROZEN_VEINS` + tendril | Surface cracks + 16 expanding clots + powder; delayed advance. **Touched up 2026-10-09:** branching frost fingers creep out across the ground | B+ |
| cryogenic_pulse | `CRYOGENIC_PULSE`, `BONE` | Same burst as ICE | B |
| absolute_stillness | `STILLNESS`, `FROZEN_VEINS` | **Touched up 2026-10-09:** suspended clots at several heights joined by frozen strands into a held lattice | B+ |
| glacial_rampart | `CRUOR_FORM`, `CRUOR_BREAK`, `CRUOR_SURFACE` | Clots converge / shatter; block faces frosted | B |
| endless_hour | `HOUR`, `HOUR_BREAK` + HUD | **Touched up 2026-10-09:** a clock of frozen blood faces the viewer: twelve marks, a frost rim, an hour hand pointing at the debt held and a turning minute hand; the break scatters all twelve marks | A? (pending in-game review) |
| rimebound_sentence | `RIMEBOUND`, `ICE_CHARGE` + skin | **Touched up 2026-10-09:** shackle rings of ice at ankles and hands, chained to frozen ground anchors | A? (pending in-game review) |
| osseous_bloom | `BONE` + tendril | 8 clots growing outward | B |
| conjure_flail | `WEAPON_CONJURE` | See the Living Arsenal row in Animus. `FROST_CONJURE` remains for echo casts only | A? |

### Lux: white light

- **Shader** (`manipulation_lux.fsh`): bright filament inside a wide transparent skirt (RIBBON/BEAM/WHITE_BEAM), a continuous light shell, curling wisp, eye arc, ripple and soft glow. **Colour comes entirely from the vertex colour** (warm white `0xFFFAEF`).
- **Strengths:** the most technically careful school. Beams fade their side strip by view angle and add axial light sheets, Flare uses a Fresnel light shell, and Unclosing Eye is one of the few two-colour effects (red pupil).
- **Weakness:** single-colour, so everything reads as the same white. No warm core / cool edge, and no blood undertone tying it to Hemomancy.

| Manipulation | Form(s) | What is drawn | Grade |
|---|---|---|---|
| radiant_wake | `RadiantWakeGeometry`: `RADIANT_WAKE` (attached, refreshed each pulse); `LUX_MENDING` / `FLARE` on recipients | Bespoke, rules in `RadiantWakeRules`: two fans of filament feathers with crimson undertone and gold tips unfurl from between the shoulders over a gold root shell and beat slowly; while gliding they flatten and streamers trail beneath. Body-yaw anchored, faint in first person, retired when the channel stops | A? (pending in-game review) |
| white_verdict | `WHITE_VERDICT`, `VERDICT_CHARGE` | View-adaptive beam with axial sheets and end glows; 5 converging streams on charge; afterglow | **B+** |
| hematic_flare | `FLARE` | Fresnel light shell + 18 Fibonacci rays + core glow, cubic expansion | **B+** |
| prismatic_reproof | `VERDICT` | Beam + two edge ribbons, now crimson (**Touched up 2026-10-09:** hematic light) | B |
| unclosing_eye | `EYE` | Eye-arc card + red pupil; formation stream | B |
| hematic_beacon | `BEACON` + Lux tendril | Bobbing gold glow + tall wisp + gold and crimson streams + pulsing ripple | B |
| lumen_suture | `LUX_MENDING`, `SUTURE` + tendril | **Touched up 2026-10-09:** rising streams alternate white and crimson; a crimson thread runs inside the suture | C+ |
| (cast mist) | `LUX_MIST` | 3 expanding glow cards | C |

### Tenebris (Umbral): ink and shadow

- **Shader** (`manipulation_umbra.fsh`): near-black ink/smoke `(0.008,0.009,0.014 → 0.095,0.083,0.11)`. It implements a swirling liquid POOL, an ink RIBBON with a smoke skirt, and SLASH with a **violet core `(0.68,0.32,1.0)`**. Everything else is lobed smoke.
- **Strengths:** good negative-space contrast; Gloam Laceration's staggered three-cut slash with roll is close to B+; the well pool is liquid and alive.
- **Weakness:** only three shapes, so veil, step and arrival are interchangeable smoke-card stacks. The violet accent appears only in slashes.

| Manipulation | Form(s) | What is drawn | Grade |
|---|---|---|---|
| umbral_rend | `UmbralRendGeometry`: `UMBRAL_REND` | Bespoke, timeline in `UmbralRendRules`: the clinging ink is drawn taut; three parallel rends tear across on a seeded diagonal facing the caster (ink lip, violet cut, blood line, leading flash), wounds gape over a void and spit blood toward the caster, then the lips fray into rising smoke. Shared Obscured payoff burst on top | A? (pending in-game review) |
| gloam_laceration | `UMBRA_SLASH` | 3 staggered slash streams with violet core, per-cast roll. **Touched up 2026-10-09:** each cut flashes where it lands (ink cross-cut + shadow puff) | B+ |
| eclipse_well | `WELL`, `WELL_CHARGE` + Umbra tendril | Liquid ink pool + 7 drifting wisps. **Touched up 2026-10-09:** violet-cored rim in turning arcs and ink drawn inward across the ground | B+ |
| black_veil_covenant | `BlackVeilRenderer` + tendril | Veil and boundary rendering | B? (verify in game) |
| blood_eclipse | HutosLib tendril | Tendril effect | B? (verify in game) |
| umbral_step, umbral_reversal | `UmbralTearGeometry`: `TELEPORT`, `UMBRA_ARRIVAL` | **Touched up 2026-10-09:** a seam tears open (violet-cored lips around a swirling ink void); ink grasps and the body's smoke is drawn in, the seam seals with a violet flash; arrival reverses it with ink spilling to a ground pool | A? (pending in-game review) |
| penumbral_drift, void_shroud | `VEIL` | **Touched up 2026-10-09:** ink bands wind round the body like a shroud over the smoke, a violet glint travelling along one fold | B |
| conjure_claws | `ClawSlashRenderer` / item | Claw slashes | n/a |
| (cast mist) | `UMBRA_MIST` | 3 expanding smoke cards | C |

### Still Arts (Unstained lane): pale stillness

Added 2026-10-19 with [casting animations v2](../../../wiki/Casting-Animations.md#still-arts-the-unstained-lane). Still Arts are not manipulations and draw no blood: there is no blood tint anywhere in this lane. The accents are drawn on the caster by the casting layer, not through `ManipulationVisualPacket`.

| Accent | Material / source | What is drawn | Grade |
|---|---|---|---|
| Hand ring | `LUX` material, existing mesh primitives | A thin pale ring at the casting hand, white `0xF4F6F8` with a silver edge `0xC9D1D9`; pulses once on release and dissolves through the settle. Kept on Minimal particles | ? (pending in-game review) |
| Still-water ring | `LUX` material, flat translucent ribbon | From Discerning up, a ring on the ground at the feet that expands once and fades; radius grows with clarity stage from 0.6 to 1.6 blocks. Removed on Minimal particles | ? (pending in-game review) |
| Art motes | `HemoParticleData.glow` (pale white and silver) in `StillArtInit` | Small, slow glow motes from each art's action. Replaced the vanilla end-rod, snowflake and wax particles; `StillArtPresentationSourceTest` keeps `ParticleTypes.` out of `StillArtInit` | C (motes only) |

## 4. Cross-cutting patterns

1. **Single-layer composition.** Outside Riposte, Thermal and the best Lux forms, effects are "N streams + one FOCUS card" or "N wisps". Nothing combines a translucent shell, an opaque core, highlight lines and an impact flash in one effect.
2. **Golden-angle everywhere.** `i * 2.39996` phyllotaxis rings place nearly every scatter (fire, clots, wisps, bursts, Mortem spores), so many schools share the same evenly-spread "sunflower" silhouette. Riposte instead follows a path (a spine).
3. **Every cast looks the same.** Seeds come from `entityId % 113` or `from.hashCode()`, so the same caster gets the same pattern every cast. Only Riposte (side/tilt) and Gloam Laceration (roll) carry per-cast variation in `count`.
4. **Generic timing.** Most forms use the shared formation/presence fade, so fast impacts lose their first ticks and nothing has named phases (anticipation → strike → settle). Exceptions: Flare's cubic expansion, Umbra Slash's staggered cuts, Riposte.
5. **Little body anchoring or first-person care.** Only Riposte, Iron Heart, Black Heart, Retort (body yaw), the Ductilis nerve forms (bounding box) and Ferric Conjure (hand) fit the body. Only Riposte and Ferric Conjure adjust for first person.
6. **Shader shape coverage is uneven.** Animus maps FOCUS and SLASH to one glossy droplet; Umbra has three shapes; Lux is monochrome by design. Effects in those schools can't easily distinguish elements, which limits contrast.
7. **Narrow palettes.** Most effects use the white base tint with no accents. Two-tone moments (Unclosing Eye pupil, Umbra violet slash core, molten hot core, Riposte core/clot) are the ones that read best, and they are rare.
8. **Fragmented primitives.** `FerricDuctilisGeometry`, `ThermalGeometry` and `LuxUmbraGeometry` each carry their own `quad`/`vertex`/`card`, and Ferric has its own tapered `ribbon`. Improvements made in one (tapering, fold protection) don't reach the others.
9. **Dead legacy code.** Because the school tiers run first, most of the inline `switch` in `ManipulationVisualRenderer.draw` is unreachable (`IRON_HEART`, `BLACK_HEART`, `COMMAND`, `CIRCUIT`, `RUSH`, `CLOUD`, `MAGNET`, `BLOOM`, `RETORT`, `HUNGER`, `GRAVE`, `RUPTURE`, `DRAIN`, `MARK`, `WOUND`, `MENDING`, `GROWTH`, `DEBT`, and the needle/mortar/ice/lightning/iron charges). So are the `CROWN`, `BELL`, `THREAD`, `CHOIR` and `WARD` branches of `ManipulationSignatureEffects`. Only `SWORD_IMPACT`, `MNEMONIC_REPLAY`, `MARIONETTE_TETHER/ORDER`, `ORE` and `GAZE_CHARGE` still reach the legacy path.
   `ManipulationVisualRenderer.bloodShot(...)` is also unused (Blood Shot draws through `BloodProjectileEffects`).
10. **School mismatches and gaps.** Deadly Gaze (Ductilis/Tenebris) lifts targets on Flammeus `UPDRAFT` flame cards and charges on legacy tubes. Sovereign Instinct reuses Hematic Rebuke's command crown with no caster-side moment. Blood Needles fly as an arrow-style flat quad mesh rather than a shaped blood needle.

## 5. Touch-up roadmap

Ordered by player visibility × gap to the standard. Each item should follow [MANIPULATION_VISUALS.md](../../MANIPULATION_VISUALS.md): bespoke geometry class, named phases, layered anatomy, two accents, anchoring, impact flash.

### Tier 1: focal moments with the largest gap

| # | Target | Why | Direction |
|---|---|---|---|
| 1 | **Blood Shot family** projectile + `RUPTURE` | Most-cast Animus spell; a single ribbon wake and two blobs | Layered droplet: translucent shell, glossy core, twisting sinew tail along the real path; dedicated splash flash on hit |
| 2 | **Blood Needle family** charge, flight and impact | Common; blob and streams charge, flat arrow-style quads in flight | Needles condense along the aim with a core/clot taper; shaped needle body in flight; staggered release; puncture flash per hit |
| 3 | **Deadly Gaze** | D, wrong school | Replace the `UPDRAFT` lift with Ductilis/Tenebris nerve-and-ink tethers; a real gaze charge (eye arc + nerve focus) |
| 4 | **Phoenix Debt** | Death-defying moment, already wing-shaped | Rebuild as structured wings (bone spine + feather flames + molten drip) with emerge → spread → beat → collapse phases |
| 5 | **Blood Aneurysm** | Summa nuke, C burst | Swelling vessel knot with pulsing veins, then a ruptured shell and spray |
| 6 | **Umbral Step / Reversal** | Signature mobility, generic smoke | Ink tear opens (slash-core edge) and swallows the body; arrival leaks through the same tear |

### Tier 2: strong bases that need layering and timing

- **Crimson Coronation:** give the orbiting swords sinew tethers to the caster and a staggered formation sequence.
- **Funeral Bell:** use the shader's bell-shell shape as a real 3D bell, add a tolling swing and ripple rings on each toll.
- **Iron Choir / Ferric constructs:** bevelled plates with rivets and blood seams; crack and shatter on break instead of fading.
- **Congeatio set:** give each spell its own shape: Grasp creeps along the ground, Rimebound locks shackles, Stillness grows a suspended lattice, Hour orbits like a clock.
- **Flammeus fire:** replace golden-angle card swarms with path-following flame tongues (a Riposte-style spine) for Ignition, Updraft and Cauterize.
- **Eclipse Well / Gloam Laceration:** extend the violet core to the well's rim and arrival effects; add an impact flash to Laceration.
- **Lux:** add a warm core / faint crimson edge to Verdict, Beacon and Suture so Lux reads as hematic light, not generic white.

### Tier 3: status and low-visibility forms

- Mortem statuses (`WOUND`, `HUNGER`, `GRAVE`, `ROT_INFECTION`): keep the skin layer as primary; replace the three generic wisps with motion specific to each status (drip, inward pull, settling dust, spores).
- `COMMAND`, `RUSH`, `GROWTH`, `COMMUNION`, the `*_CONJURE` spirals: differentiate per spell; these are interchangeable spirals today.
- Ductilis `WARD`, `THREAD`, `NERVE_PULSE`: add a glow core under the ribbons; build the pulse ring as one `ribbonPath` instead of 24 ribbons.
- Sovereign Instinct: give the trigger its own caster-side flare (a crown of tethers lashing out to each redirected mob) instead of reusing the Rebuke crown.

### Progress (2026-10-09)

Tiers 1–3 above, and most remaining C-grade rows, have been implemented in source and pass their JVM tests; every touched row is marked **A? / B+ (pending in-game review)** until it is seen in game. Enabler 4 is done: the unreachable legacy `switch` cases and helpers in `ManipulationVisualRenderer.draw` were removed (only `MARIONETTE_TETHER`, `MARIONETTE_ORDER` and `ORE` remain there; `SWORD_IMPACT` and `MNEMONIC_REPLAY` still route through `ManipulationSignatureEffects`). Enabler 2 is partly done through the tapered `ribbonPath` overload and the shared `flameRibbon`/`tongue` helpers. Enabler 1 is deliberately deferred: changing the Animus FOCUS/SLASH fallback would alter Hematic Riposte, so new shapes should be added as new indices.

### Balance-pass additions (2026-10-09)

The [balance pass](MANIPULATION_BALANCE_PLAN.md) added six manipulations. All six have bespoke geometry built to the Riposte standard (`UmbralRendGeometry`, `LodestoneCollapseGeometry`, `SearingLashGeometry`, `CruorJavelinGeometry`, `RadiantWakeGeometry`, `FruitingContagionGeometry`), each with a pure rules class and JVM tests, plus `BalancePassVisualSourceTest` for their renderer hooks and draw order. None has been seen in game yet.

### Enablers (do once, reused by every touch-up)

1. Implement real FOCUS and SLASH shapes in `manipulation_animus.fsh`, and more shapes in `manipulation_umbra.fsh` (currently droplet/smoke fallbacks).
2. Move the duplicated `quad`/`vertex`/`card`/tapered `ribbon` into one shared helper so taper and fold fixes reach every school.
3. Add a per-cast seed channel (packet `count` or a dedicated field) so repeated casts vary.
4. Delete the unreachable legacy branches listed in §4.9 once their replacements are confirmed, keeping `SWORD_IMPACT`, `MNEMONIC_REPLAY`, `MARIONETTE_*`, `ORE` and `GAZE_CHARGE` until they migrate.
5. Pair every upgraded effect with a source-contract test (tier hook, draw order) and a rules test for its timing, as Riposte does.
