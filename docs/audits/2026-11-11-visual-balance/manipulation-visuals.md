# Manipulation visuals against the Riposte standard

Audit date: 2026-11-11. Read-only, from source. Nothing was launched, so every grade is what the code draws, not what was seen in game. Source was being edited while this was written; cite the files and expect small drift. The standard is [MANIPULATION_VISUALS.md](../../MANIPULATION_VISUALS.md); the older catalogue in `docs/archive/audits/MANIPULATION_VISUAL_CATALOGUE.md` (snapshot 2026-10-09) was used as a starting map and every row here was re-checked against current code.

All paths below are relative to `src/main/java/com/vincenthuto/hemomancy/` unless they start with `src/` or `docs/`.

## Summary

`common/init/ManipulationInit.java` registers **115** manipulations (more than the expected 108: the core-loop work added `blood_lash`, `living_land`, `venous_cling`, `tendon_line`, `conjure_pickaxe`, `lode_draw` and `hematic_hewing`).

| Score | Meaning | Count |
|---|---|---|
| A | At the Riposte bar: a bespoke geometry class with a named timeline, layered custom geometry in the school material, motion with follow-through | 43 |
| B | Custom toolkit, but thin or single-layer (a branch in a shared school tier, golden-angle scatter, no named phases) | 53 |
| C | Generic or near-generic: a shared form, or a few streams and cards that would read as any spell | 17 |
| D | Vanilla particles or nothing distinctive | 2 |

Three B rows also carry vanilla particles (flagged **V**): the Ferric construct entities `EntityIronWall`, `EntityIronSpike` and `EntityIronPillar` emit `ParticleTypes.SQUID_INK` and `ParticleTypes.ASH` (6 each on `src/test/resources/architecture/vanilla-particle-allow-list.txt`, "pending the vanilla particle sweep (A-33)"). No file under `common/manipulation/`, `client/render/world/` or `client/render/outpouring/` names a vanilla particle (`ManipulationVanillaParticleSourceTest` keeps it that way).

Every A grade is source-only. The 2026-10-09 and 2026-10-19 rebuilds are all still marked "pending in-game review" in the old catalogue and nothing in this audit could change that.

**The ten worst focal offenders** (worst score weighted by how focal and how often cast):

1. `sanguine_marionette` (D, Ductilis Humilis channel): the only manipulation still drawn by the legacy `VisceralMesh` switch in `ManipulationVisualRenderer` (`MARIONETTE_TETHER`, `MARIONETTE_ORDER`).
2. `axonal_transduction` (C, Ductilis Perfectus capstone): in the world it leaves only gold `HemoParticleData.glow` motes (`ductilis/AxonalTransductionManager.java`); the travel itself is a screen-space view (`client/screen/overlay/AxonalTravelView.java`).
3. `exsanguinate` (C, Mortem Mediocritas attack): the shared `MORTEM_BURST` eight-streak burst plus a HutosLib tendril.
4. `cryogenic_pulse` (C, Congeatio Humilis attack): draws exactly the Glacial Grasp `ICE` clot scatter (`ThermalGeometry` case `STILLNESS,ICE,CRYOGENIC_PULSE`).
5. `thread_ripper` (C, Ductilis Summa charge): three parallel ribbons that part (`FerricDuctilisGeometry` case `THREAD`).
6. `tendon_line` (C, Animus Humilis traversal): a good cord, but `client/render/world/TendonLineRenderer.java` draws it only for the local player, so nobody else sees the line.
7. `crimson_tithe` (C, Saint canon): one clot card and a spiral (`AnimusMortemGeometry` case `DEBT`, five lines).
8. `unclosing_eye` (C, Saint canon): one `EYE` card with a red pupil card (`LuxUmbraGeometry` case `EYE`).
9. `bloom_of_rot` (C, Saint canon): seven wisp cards over rot patches (`AnimusMortemGeometry` case `BLOOM`).
10. `osseous_bloom` (C, Congeatio Summa): eight clots, sharing its branch with `FROZEN_VEINS` (`ThermalGeometry` case `BONE`).

Right behind them are the Perfectus and Magister capstones that only reach B: `synaptic_storm`, `eclipse_well`, `white_verdict`, `sanguine_tempest`, `ironhearted`, `absolute_stillness`.

## How a manipulation reaches the screen (current source)

- Server code calls `common/manipulation/ManipulationVisuals.burst(...)`, `attached(...)` or `replay(...)`. Each sends a `ManipulationVisualPacket(form, entityId, from, to, radius, ticks, count)` near the midpoint. `Form` is an ordinal-encoded enum; new forms are appended.
- `client/render/world/ManipulationVisualRenderer.java` turns each packet into a cue and offers it to the tiers in this order: body-anchored casts (`bodyAnchored`: `BloodRushGeometry`, `SanguineWardGeometry`, `SanguineMendingGeometry`, `WeaponConjureGeometry`, `CellHandGeometry`, `AvatarManifestGeometry`), then `FerricDuctilisGeometry`, `LodestoneCollapseGeometry`, `LodestoneFieldGeometry`, `SearingLashGeometry`, `CruorJavelinGeometry`, the Saint reliquaries, `RadiantWakeGeometry`, `FruitingContagionGeometry`, `UmbralRendGeometry`, `CastImpactGeometry`, `HematicRiposteGeometry`, `PuppetStringsGeometry`, `VesperRecallGeometry`, `BloodShotGeometry`, `FuneralBellGeometry`, `CrimsonCoronationGeometry`, `UmbralTearGeometry`, `BloodAneurysmGeometry`, `PhoenixDebtGeometry`, `NerveCommandGeometry`, `DeadlyGazeGeometry`, then the shared school tiers `AnimusMortemGeometry`, `ThermalGeometry`, `LuxUmbraGeometry`, then the legacy `VisceralMesh` switch (only `MARIONETTE_TETHER`, `MARIONETTE_ORDER`, `ORE` remain).
- Every landed direct school hit also gets the shared `CAST_IMPACT` (`common/manipulation/impact/CastImpacts.landed`, drawn by `CastImpactGeometry`: flash, delivery shape, school flourish). Strands force their delivery through `CastImpacts.deliveredAs`.
- Every cast also gets the shared cast outpouring (`client/render/outpouring/*`): blood leaves the vascular section, veins light, marks fade. `OutpouringRules.woundOnly` steps it down for Riposte, Coronation, the avatar family and Staff weapon forms.
- Held Projection and Absorption shapings get the `CELL_HAND` stream from palm to real target (`CellHandGeometry`) as their baseline, whatever the shaping.
- Strand shapings (`common/manipulation/verb/strand/Strands.java`: `LASH`, `JET`, `SPRAY`, `PLANT`, `BRAND`) are drawn by `client/render/world/StrandRenderer.java` from `StrandClientState`, with school variants in `StrandSchoolGeometry.java` and accents (`crack`, `pierce`, `drench`, `eruption`, `brandSet`) on `STRAND_ACCENT`.

Because the cast outpouring and cast impact are shared, "C" below means the spell adds little or nothing on top of those two.

## Per-temper tables

Columns: rank (Humilis, Mediocritas, Summa, Magister, Perfectus), cast type (Quick, Charged, Continuous, Passive), vascular section, score, one line of evidence, one line of upgrade. "Strand" names the strand form a manipulation also has.

### Animus (28)

| Manipulation | Rank / type / section | Score | Evidence | Upgrade and parts to reuse |
|---|---|---|---|---|
| blood_shot | Hum / Q / Head | A | `BloodShotGeometry` (221 lines) via `BloodProjectileEffects`: sheath, core, clot, highlight, tapered wake, twisting sinew; timed `BLOOD_SPLASH` on contact | Verify in game; nothing to build |
| guided_blood_shot | Hum / Q / Head | A | Same geometry with guided feelers | Verify in game |
| hematic_mortar | Med / C / Head | A | `MORTAR_CHARGE` in `AnimusMortemGeometry` (sheath, clots in orbit, drips) plus mortar flight in `BloodShotGeometry` | Verify in game |
| sanguine_halo | Med / Q / Head | B | Halo tether on the `BloodShotGeometry` droplet; no halo body of its own | Give the halo its own ring geometry: `CrimsonCoronationGeometry` ring-and-spike pattern, sinew via `OutpouringBrushes.sinew` |
| blood_binding | Hum / Q / Legs | B | `BloodBindingTendrilRenderer` + `BloodBindingTendrilGeometry`: ground-contact coils, one liquid surface (`ground` only) | Add a translucent sheath, heartbeat sinew and a clot knot on the bound ankle per the anatomy list; reuse `LuxUmbraGeometry.ribbonPath` widths and `HematicRiposteGeometry` sinew timing |
| lingering_blood_binding | Hum / Q / Legs | B | Same coils as blood_binding; the mode is invisible | Per-mode signature: a lingering pool (`ground` with `POOL`) that keeps reaching |
| chain_blood_binding | Hum / Q / Legs | B | Same coils | Per-mode signature: a coil that leaps body to body, reusing `SovereignInstinct`'s whip path from `AnimusMortemGeometry` `SOVEREIGN_LASH` |
| blood_lattice | Med / Q / Legs | B | Same coils | Per-mode signature: a web between the coils, reusing `ThermalGeometry.lattice` logic on the Animus material |
| tendon_line | Hum / Q / Arms | C | `TendonLineRenderer` draws a cord (`OutpouringBrushes.ribbonPath` + `sinew`) for `mc.player` only; `TendonLineShaping` sends Ductilis `THREAD` | Draw for every tracked caster (needs the anchor synced to trackers), add root knot and hand knot `FOCUS` cards; keep it in `OutpouringBrushes` |
| blood_lash | Hum / Q / Arms; strand LASH | A | `StrandRenderer.lash`: wisp sheath, body, hot core, secondary thread, tip knot, speed swish; `crack` accent | Verify in game |
| living_land | Med / Q / Legs | B | `LivingLandRenderer` (vein net on carried blocks, blood cord, socket pool) + shared `ANIMUS_CONJURE` / `ANIMUS_IMPACT` bursts | A bespoke strike burst at the socket (tear, flung clods of blood); reuse `BloodAneurysmGeometry` rupture shreds |
| venous_cling | Hum / Q / Legs | B | `ClingVeinRenderer`: short sinew plus infusion vein net at each contact; subtle by design | Fine as an overlay; low priority |
| hematic_riposte | Med / Q / Arms | A | `HematicRiposteGeometry` + `HematicRiposteRules`: the reference | None |
| summon_avatar | Med / P / Body | A | `AvatarManifestGeometry` (`AVATAR_MANIFEST`, stage in `count`): pool, climbing rings, sinew lash, surface splash, drip-curtain dismissal | Verify in game |
| summon_avatar_arms | Med / P / Body | A | Same, stage 2 | Verify |
| summon_avatar_armor | Sum / P / Body | A | Same, stage 3 | Verify |
| summon_avatar_legs | Sum / P / Body | A | Same, stage 4 | Verify |
| summon_avatar_complete | Mag / P / Body | A | Same, stage 5 | Verify |
| blood_cloud | Sum / Q / Head | B | `CLOUD` in `AnimusMortemGeometry` (8 golden-angle wisp cards, beating core, rain streaks), sent from `CloudEntityBlood` | A layered cloud class: clotted underbelly sheet, forking blood rain that splashes (`BloodShotGeometry` splash), lightning in the Tempest via `OutpouringBrushes.bolt` |
| expansive_blood_cloud | Sum / Q / Head | B | Same cloud, larger | Same class, wider footprint |
| pursuing_blood_cloud | Sum / Q / Head | B | Same cloud | Same class with a trailing wake toward its target |
| sanguine_tempest | Mag / Q / Head | B | Same cloud with faster drift (`count==3`); a Magister cast that looks like Blood Cloud | Highest-value cloud upgrade: rotating wall, bolts (`OutpouringBrushes.bolt`, `DuctilisLightningEffects`), rain sheets |
| blood_rush | Hum / Q / Body | A | `BloodRushGeometry` + `BloodRushRules`: heartbeat rings, leg veins, kick-off splash, sheath streaming back, heel flame | Verify |
| blood_aneurysm | Sum / C / Body | A | `BloodAneurysmGeometry`: swelling veined knot, vessel to victim, sac tears, shreds, spray, Mortem haze | Verify |
| vital_effusion | Hum / Q / Body | B | `GROWTH` in `AnimusMortemGeometry`: four vein roots with a bud each | Layer a pale core and wisp shell on the roots, a heartbeat pulse up them, and buds that open; reuse `VitalEffusionShaping` cue timing |
| conjure_blade | Med / Q / Arms | A | `WeaponConjureGeometry` on the real held weapon via `HeldWeaponPose` | Verify |
| crimson_coronation | Perf / C / Head | A | `CrimsonCoronationGeometry` (`CROWN`, `CROWN_CHARGE`): crown ring, spikes, swords on twisted sinew | Verify |
| crimson_tithe (Saint) | Sum / Q / Heart | C | `DEBT` is one `FOCUS` card and a spiral (`AnimusMortemGeometry`, 5 lines); `TITHE_COLLECT`/`TITHE_RETURN` share the `HUNGER_COLLAPSE` streams; HUD meter | New `CrimsonTitheGeometry`: a blood ledger knot at the heart, threads drawn from each payer, a return surge; reuse `HemorathBloodGeometry` (the Saint's own blood) and `CellHandStreams`-style liquid streams |

### Ductilis (15)

| Manipulation | Rank / type / section | Score | Evidence | Upgrade and parts to reuse |
|---|---|---|---|---|
| sanguine_marionette | Hum / Cont / Legs | D | `HematicCommandManager` sends `MARIONETTE_TETHER` / `MARIONETTE_ORDER`; drawn by the legacy `VisceralMesh.segment`/`drop`, `ring`, `diamond` switch in `ManipulationVisualRenderer` | Port to `PuppetStringsGeometry` (layered vein strings, knots, wisps; endpoints from `PuppeteerThreadEndpointRules`), order markers as nerve glyphs from `NerveCommandGeometry`; then delete the legacy branch |
| deadly_gaze | Hum / C / Head | A | `DeadlyGazeGeometry` (`GAZE_CHARGE`, `GAZE_GRIP`, `GAZE_SLAM`): nerve eye, ink pupil, seizing tendrils, ink pool slam | Verify |
| hematic_rebuke | Med / Q / Head | A | `NerveCommandGeometry` + `NerveCommandRules`: impulse, cranial net, spine pulses; HutosLib bolt on cast | Verify |
| hematic_impressment | Sum / Q / Head | A | Same class, leash and nape sigil | Verify |
| activation_potential | Med / C / Body | B | `DuctilisLightningEffects.activationPotential` (HutosLib bolts with restrike) + `LIGHTNING_CHARGE` (converging ribbons, arcs, nerve ring) | A charged body: nerve ring as one `ribbonPath`, glow core under the ribbons; reuse `StrandSchoolGeometry.arc` for the release |
| synaptic_jolt | Hum / Q / Head; strand SPRAY | B | `DuctilisLightningEffects.synapticJolt` bolts; strand spray is `StrandSchoolGeometry.arcSpray` (A-grade jagged arcs) | Bring the quick cast up to the spray: a hand-anchored arc burst using `StrandSchoolGeometry.arc` and the `NERVE_HIT` fork |
| conductive_mark | Med / Q / Head; strand BRAND | B | `MARK` bbox-fitted nerve paths in `FerricDuctilisGeometry`; bolts; Ductilis brand arc in `StrandRenderer.brand` | Add a glowing sigil at the mark and a pulse that runs the paths; reuse `NerveCommandGeometry` sigil |
| sanguine_ward | Med / Cont / Body | A | `SanguineWardGeometry` + `SanguineWardRules`: counter-wound lattice, knots, crown on full pool | Verify |
| hemolymphal_pulse | Hum / Q / Head | B | `NERVE_PULSE`: one expanding nerve loop over a glow; `hemolymphalPulse` bolts | Layer a second fraying ring and ground crackle; reuse `CastImpactGeometry` `bolts` flourish |
| conjure_crossbow | Med / Q / Arms | A | `WeaponConjureGeometry` | Verify |
| sovereign_instinct | Mag / P / Head | B | `SOVEREIGN_LASH` whip (two ribbons, crown flare) + `COMMAND` crown spiral, 13 lines in `AnimusMortemGeometry` | Give the trigger a caster-side burst: tethers lashing out per redirected mob in `NerveCommandGeometry` style, real crown geometry from `CrimsonCoronationGeometry` |
| axonal_transduction | Perf / P / Head | C | World-side only `HemoParticleData.glow(GOLD)` motes (`AxonalTransductionManager` lines 176, 237); the route is a screen overlay (`AxonalTravelView`) | Bespoke departure and arrival: the body unravels into nerve current down the path and knits at the end; reuse `DuctilisLightningEffects.conductorStrike`, `StrandSchoolGeometry.arc`, `PuppetStringsGeometry` knit/peel timing |
| synaptic_storm | Perf / C / Head | B | `LIGHTNING_CHARGE` charge, then `DuctilisLightningEffects.conductiveArc` hops; no storm body | New `SynapticStormGeometry`: overhead nerve canopy that forks down per hop, with a named charge, strike and fray; reuse HutosLib bolts and `StrandSchoolGeometry.arc` |
| living_circuit | Mag / Cont / Body | B | `CIRCUIT` bbox nerve paths + `livingCircuit` bolts | Glow core and travelling current beads along the paths (`SanguineWardGeometry` bead pattern) |
| thread_ripper | Sum / C / Head | C | `THREAD`/`THREAD_CHARGE` in `FerricDuctilisGeometry`: three parallel ribbons that part, frayed ends | Bespoke severing: taut nerve threads pulled from the target, a snap flash, recoiling frayed ends; reuse `ThreadKnotGeometry` and `PuppetStringsGeometry` unspool |

### Ferric (19)

| Manipulation | Rank / type / section | Score | Evidence | Upgrade and parts to reuse |
|---|---|---|---|---|
| blood_needle | Hum / C / Head | A | `ForgedNeedleGeometry` + `ForgedNeedleRules`: gather ribbons, forged flechettes, field lines, blood wake | Verify |
| blood_needle_fan | Hum / C / Head; strand SPRAY | A | Same charge; strand `StrandSchoolGeometry.ironFan` | Verify |
| blood_needle_lance | Hum / C / Head; strand JET | A | Same charge with barbs; strand `ironJet` | Verify |
| conjure_axe | Med / Q / Arms | A | `WeaponConjureGeometry` | Verify |
| conjure_pickaxe | Med / Q / Arms | A | `WeaponConjureGeometry` | Verify |
| conjure_staff | Med / Q / Arms | A | `WeaponConjureGeometry` / `CellHandGeometry` | Verify |
| blood_absorption | Med / Q / Arms | A | `CellHandGeometry` (447 lines): liquid streams with sheath, slugs, whirl strands | Verify |
| blood_projection | Med / Q / Arms | A | Same | Verify |
| sanguine_mending | Hum / Cont / Arms | A | `SanguineMendingGeometry` + `SanguineMendingRules`: blood along the tool, stitched cracks, riveted | Verify |
| vascular_dowsing | Hum / Cont / Arms | D | `ORE` is the legacy `VisceralMesh.locatorBox`; gouge shaping spawns `HemoParticleData.bloodCell` | Acceptable as UI; if touched, a vein that crawls through the stone to each ore (`LodestoneFieldGeometry` flux lines, infusion vein net) |
| lode_draw | Med / Q / Arms | B | `PacketBloodStructureFeed` warp + `FERRIC_IMPACT` + `BloodFlowVisuals.toward` | Fine for a utility; a crawling iron seam along the lode would lift it |
| hematic_hewing | Sum / Q / Arms | C | Only `FERRIC_IMPACT` bursts at cut blocks (`HewingShaping` line 102) over the cell-hand stream | A cut preview and a blade of blood-iron that sweeps the face; reuse `ForgedNeedleGeometry.flechette` and `FerricDuctilisGeometry.plate` |
| iron_retort | Med / Q / Body | B | `RETORT`: three plates rotated to body yaw (6 lines) | Plates that slam on with a ring flash and blood squeeze, per `LodestoneCollapseGeometry` impact |
| ironhearted | Mag / C / Heart | B | `IRON_HEART`/`IRON_CHARGE`: three chest plates via `IronHeartAttachment` + a ribbon | Bespoke heart cage: plates fold over a beating iron heart; reuse `FerricDuctilisGeometry.plate`, `BloodAneurysmGeometry` heartbeat |
| sanguine_magnetism | Sum / Q / Body | B, V | `MAGNET` field ribbons + `EntityIronPillar` construct; pillar emits vanilla `SQUID_INK` and `ASH` | Sweep the vanilla particles (`HemoParticleData.darkGlow`, `FerricDuctilisGeometry.shatter`); field via `LodestoneFieldGeometry` |
| ferric_rampart | Sum / Q / Body | B, V | `FerricDuctilisGeometry.construct` WALL (chamfered riveted plates, shatter); `EntityIronWall` emits vanilla ink and ash | Sweep the vanilla particles; add the blood gather seam on rise |
| ferric_spikes | Sum / Q / Body; strand PLANT | B, V | Construct SPIKE; strand eruption `StrandSchoolGeometry.ironSpikes`; `EntityIronSpike` emits vanilla ink and ash | Sweep the vanilla particles |
| iron_choir | Mag / Cont / Body | B | `CHOIR`: up to three orbiting chamfered plate shields; `FERRIC_IMPACT` flying plate | A choir body: plates on blood tethers that sing (pulse) in turn; reuse `CrimsonCoronationGeometry` tether pattern |
| lodestone_collapse | Perf / C / Body | A | `LodestoneCollapseGeometry` + `LodestoneCollapseRules` | Verify |

### Flammeus (13)

| Manipulation | Rank / type / section | Score | Evidence | Upgrade and parts to reuse |
|---|---|---|---|---|
| conjure_torch | Med / Q / Arms | A | `WeaponConjureGeometry` + `LivingTorchFlames` | Verify |
| crimson_flame_conjuration | Hum / Q / Arms; strand PLANT | B | `FLAME_CONJURE`: seven small tongues round the body (`ThermalGeometry`); strand plant is the shared ring telegraph | A Flammeus plant: a fire column that erupts from the pour; reuse `SearingLashGeometry` molten core, `PyreFlames`, `ThermalGeometry.flameRibbon` |
| pyretic_forge | Hum / Q / Body | B | `FORGE`: one rotating molten chunk, four ground rings, fire cards (12 lines) | A forge mouth: molten pour into the struck item, hammer-flash sparks; reuse `ThermalGeometry.chunk`, `HemoParticleData.emberSpark` |
| sanguine_ignition | Hum / Q / Body | B | `IGNITION`: golden-angle `flameRibbon` tongues over a pool | Path-following flame tongues with a named flare and gutter phase |
| vitric_combustion | Sum / C / Body | B | `GLASS`/`GLASS_CHARGE`: 30 faceted shards on `ThermalFragments`, flash, smoke | Add a molten core that cracks before the burst and glowing seams in the shards |
| cauterizing_rebuke | Med / Q / Body | B | `CAUTERIZE`: molten seam and short tongues + `ThermalSkinLayer` | A searing hand-press at the wound with a hiss of smoke; reuse `SearingLashGeometry` flare |
| scalding_updraft | Hum / Q / Legs | B | `UPDRAFT` (count = mode): spiral flame tongues up a column | One updraft class with per-mode shapes (blast ring, soar wake, hover cushion) |
| soaring_updraft | Hum / Q / Legs | B | Same, narrow mode | As above |
| suspended_updraft | Med / Q / Legs | B | Same, hover mode | As above |
| expulsive_updraft | Med / Q / Legs | B | Same, blast mode with molten droplets | As above |
| furnace_veins | Mag / Cont / Body | B | `FURNACE`: surface-following molten pools, swaying tongues, smoke | Veins of molten blood running out from the feet across the ground; reuse `HemorathFissureGeometry`-style cracks and `ThermalSurface` |
| phoenix_debt | Perf / P / Heart | A | `PhoenixDebtGeometry`: molten-bone wings, flame primaries, molten heart, armed banked wings | Verify |
| searing_lash | Med / Q / Arms; strand LASH | A | `SearingLashGeometry` + `SearingLashRules`; Flammeus strand lash | Verify |

### Lux (8)

| Manipulation | Rank / type / section | Score | Evidence | Upgrade and parts to reuse |
|---|---|---|---|---|
| conjure_spear | Med / Q / Arms | A | `WeaponConjureGeometry` | Verify |
| hematic_flare | Hum / Q / Head; strand JET | B | `FLARE`: Fresnel light shell, 18 rays, cubic expansion (`LuxUmbraGeometry`) | Two accents and a fray: warm core, crimson undertone on the rays, rays that drift and thin on fade; reuse `RadiantWakeGeometry` filament feathers |
| prismatic_reproof | Med / Q / Head; strand SPRAY | B | `VERDICT` beam with two crimson edge ribbons; strand `prismSpray` is richer | Make the quick cast a split prism like the spray: reuse `StrandSchoolGeometry.prismSpray` |
| hematic_beacon | Med / Q / Body | B | `BEACON`: bobbing gold glow, tall wisp, two streams, ripple; `luxRelease` tendril | A standing light with a crimson root and slow rising motes; reuse `SeraphaeLightGeometry` |
| lumen_suture | Med / Q / Arms | C | `SUTURE` is two streams (4 lines); `LUX_MENDING` five rising streams; `lumenSuture` tendril | Stitches of light that close the wound in turn, like `SanguineMendingGeometry` but on the body; reuse `SanguineMendingRules` timing |
| white_verdict | Perf / C / Head | B | `WHITE_VERDICT`: view-adaptive beam with axial sheets; afterglow | A capstone body: charge halo, beam that scorches a line and leaves ash and a crimson after-image; reuse `SeraphaeRadianceGeometry` |
| radiant_wake | Mag / Cont / Legs | A | `RadiantWakeGeometry` + `RadiantWakeRules` | Verify |
| unclosing_eye (Saint) | Sum / Q / Head | C | `EYE`: one eye card and a red pupil card (8 lines); manip itself only adds a `ManipulationParticles.accent` | A Saint eye: lids that open, a ring of filament lashes, a gaze line to what it reveals; reuse `SeraphaeLightGeometry`/`SeraphaeRadianceGeometry` and `DeadlyGazeGeometry` eye |

### Congeatio (9)

| Manipulation | Rank / type / section | Score | Evidence | Upgrade and parts to reuse |
|---|---|---|---|---|
| conjure_flail | Med / Q / Arms | A | `WeaponConjureGeometry` | Verify |
| glacial_grasp | Hum / Q / Arms; strand PLANT | B | `ICE` clot scatter with surface cracks and frost fingers, `FROST_ADVANCE`, tendril; Congeatio plant eruption has shards | A creeping grasp: frost fingers that close on the target as a hand; reuse `CruorJavelinGeometry` frost strands, `VelorumFrostGeometry` |
| cryogenic_pulse | Hum / Q / Body | C | Shares the `ICE` branch exactly (`case STILLNESS,ICE,CRYOGENIC_PULSE`) | A pulse of its own: an expanding frost ring from the chest that crystallises outward; reuse `ThermalGeometry.cracks`, `VelorumRimeGeometry` |
| glacial_rampart | Med / Q / Arms | C | Real `frozen_cruor` blocks; the visual is the shared `CRUOR_FORM`/`CRUOR_BREAK`/`CRUOR_SURFACE` from `TemporaryIceManager` | A raising wall: frost climbing the face before it sets, shatter on break; reuse `VelorumIceGeometry`, `ThermalFragments` |
| osseous_bloom | Sum / Q / Body | C | `BONE`: eight clots growing out, same branch as `FROZEN_VEINS`; tendril | Bone-white cruor spurs that burst from the ground round each target; reuse `StrandSchoolGeometry.ironSpikes` layout on the cruor material, `ThermalGeometry.clot` |
| absolute_stillness | Perf / Cont / Body | B | `STILLNESS`: suspended clots joined by a frozen lattice | Capstone body: the air sets as glass, time-stopped motes, a rim that cracks at end; reuse `VelorumFrostGeometry` |
| rimebound_sentence | Mag / C / Arms | B | `ThermalGeometry.shackles`: two clot rings and three clot chains to ground anchors | Fine shape; add a named lock moment (snap flash, frost burst) and fray on release |
| cruor_javelin | Med / Q / Arms; strand JET | A | `CruorJavelinGeometry` + `CruorJavelinRules`; strand jet carries `cruorShards` | Verify |
| endless_hour (Saint) | Sum / Q / Body | B | `HOUR`/`HOUR_BREAK`: a clock of twelve clots, frost rim, hands, break scatter | Give it Velorum's own material and a ticking pulse; reuse `VelorumIceGeometry`, `VelorumRimeGeometry` |

### Tenebris (10)

| Manipulation | Rank / type / section | Score | Evidence | Upgrade and parts to reuse |
|---|---|---|---|---|
| conjure_claws | Med / Q / Arms | A | `WeaponConjureGeometry`; `ClawSlashRenderer` on hits | Verify |
| umbral_step | Med / Q / Legs | A | `UmbralTearGeometry` (`TELEPORT`, `UMBRA_ARRIVAL`): seam tears, ink swallows, violet flash | Verify |
| void_shroud | Hum / Q / Body | B | `VEIL`: six smoke cards, two ink bands, one violet glint | Subtle overlay is right here; add a named wrap and unwrap |
| gloam_laceration | Hum / Q / Arms; strand LASH | B | `UMBRA_SLASH`: three staggered slashes with landing flashes; the Tenebris strand lash is A | Bring the quick cut up to `UmbralRendGeometry` lips and blood line |
| blood_eclipse | Med / Q / Head; strand SPRAY | B | HutosLib `bloodEclipse` tendril, `UMBRAL_FIELD` overlay, Umbra spray plumes, `EclipseSnuffShaping` `UMBRA_MIST` | A Tenebris spray of its own (ink tongues rather than plumes) |
| black_veil_covenant | Med / Q / Body | C | `BlackVeilRenderer`: one pool, twelve golden-angle wisps, a boundary ring; `blackVeil` tendril | A veil that hangs: ink curtains falling from a rim, violet seams; reuse `VelorumDarkGeometry`, `UmbralRendGeometry` lips |
| umbral_reversal | Med / Q / Legs | A | `UmbralTearGeometry` | Verify |
| penumbral_drift | Mag / Cont / Legs | B | `VEIL` (as void_shroud) while drifting | A drift wake of ink behind the feet |
| eclipse_well | Perf / C / Head | B | `WELL`: liquid pool, seven wisps, violet rim arcs, ink drawn in; `WELL_CHARGE` is one wisp card | Capstone body: a sinking vortex with arms (`VisceralGeometry.wellPoint` exists), a real charge, a collapse; reuse `UmbralTearGeometry` |
| umbral_rend | Sum / Q / Arms | A | `UmbralRendGeometry` + `UmbralRendRules` | Verify |

### Mortem (13)

| Manipulation | Rank / type / section | Score | Evidence | Upgrade and parts to reuse |
|---|---|---|---|---|
| conjure_sickle | Perf / Q / Arms | A | `WeaponConjureGeometry` | Verify |
| hemorrhage | Hum / Q / Arms; strand BRAND | B | `WOUND` drops into a pool + `MortemSkinLayer` + tendril; shared brand | A Mortem brand: rot-dark knot and hyphae cinching, not the shared one |
| lignum_mortis | Hum / Cont / Arms | C | `BloodFlowVisuals.lignum` to `BloodFlowEffects` (two streams, two cards) | Hyphal roots spreading into the wood as it grows; reuse `FruitingContagionGeometry` threads, `PutricielGrowthGeometry` |
| canopy_mortis | Hum / Cont / Arms | C | Same flow | As above |
| worked_lignum | Med / Cont / Arms | C | Same flow | As above |
| exsanguinate | Med / Q / Arms | C | Shared `MORTEM_BURST` (hot flash, eight streaks, beads) + `exsanguinate` tendril | A drain of its own: blood pulled out of the target in a swelling stream to the hand; reuse `CellHandGeometry` absorption stream and `BloodAneurysmGeometry` vessel |
| insatiable_hunger | Hum / Q / Body; strand BRAND | B | `HUNGER`: four inward spirals and a core wisp; `HUNGER_COLLAPSE` streams; skin layer | Fine for a status; Mortem brand as above |
| grave_debt | Med / Q / Heart; strand BRAND | B | `GRAVE`: grave outline, settling dust; `GRAVE_REFUND` stream | Fine for a status; Mortem brand as above |
| blackhearted | Mag / P / Heart | B | `BLACK_HEART`: beating clot, sheath, clotted veins; shared burst on rupture | A bespoke rupture: the heart tears like `BloodAneurysmGeometry`, black spray |
| funeral_bell | Perf / C / Heart | A | `FuneralBellGeometry`: bell on a tendon rope, toll swing, echo rings, rising souls | Verify |
| carrion_communion | Mag / Cont / Body | C | `COMMUNION`: a pool and three rising streams; `BloodFlowVisuals.connect`; `CarrionDrawShaping` `BONE`/`MORTEM_CONJURE` | Streams of rot-blood drawn from each corpse to the caster, with corpses sagging; reuse `CellHandGeometry` stream, `PutricielSludgeGeometry` |
| fruiting_contagion | Sum / Q / Body | A | `FruitingContagionGeometry` + `FruitingContagionRules` | Verify |
| bloom_of_rot (Saint) | Sum / Q / Body | C | `BLOOM`: seven wisp cards over terrain-following rot patches | A Saint bloom: stalks and caps pushing up through the patch, spore burst; reuse `PutricielGrowthGeometry`, `PutricielSporeGeometry`, `FruitingContagionGeometry` |

## Toolkit parts list

Everything below already exists. New effects should be built from these through the public bridges, not by copying their art into a private shader or class.

### Drawing primitives (client, package `client/render/world`)

| Part | What it draws | How to call it |
|---|---|---|
| `LuxUmbraGeometry` | The shared primitives: `ribbonPath` (camera-facing strip along points, with per-point widths and fold protection), `ribbon`, `stream` (12-station bent ribbon), `card` (billboard), `ground` (flat card), `surface`, `slashOffsets()`; shape ids `RIBBON`, `WISP`, `POOL`, `EYE`, `FOCUS`, `BEAM`, `RIPPLE`, `SLASH`, `WHITE_BEAM`, `LUX_SHELL` | Package-private statics; draw into `LuxUmbraBatch.vertices(Material.X)` so the school shader colours it |
| `LuxUmbraBatch` + `Material` | One batch per frame of school materials: `ANIMUS`, `MORTEM`, `LUX`, `UMBRA`, `DUCTILIS`, `FLAME`, `MOLTEN`, `GLASS`, `CRUOR` | `flows.vertices(Material.ANIMUS)` |
| `ThermalGeometry` | `flameRibbon`, `tongue`, `fire`, `smoke`, `powder`, `shard` (faceted glass), `clot` (cruor core inside frost shell), `chunk` (molten), `blockFaces`, plus private `cracks`, `pool`, `frostFingers`, `lattice`, `shackles` | Package statics; surface pools through `ThermalSurface`, flying pieces through `ThermalFragments` |
| `FerricDuctilisGeometry` | Iron in the solid pass: `plate` (chamfered, riveted), `spike`, `shatter`, construct bodies; Ductilis `ribbon` (tapered nerve), `field`, `gather` | Package statics; iron goes to the solid `AFTER_ENTITIES` pass |
| `ForgedNeedleGeometry` | `flechette` (square-section iron needle with fins, optional barbs), `flightIron`, `flightFlow`, `charge` | Package statics |
| `StrandSchoolGeometry` | `arc` (re-jagging Ductilis arc with hot core and fork), `arcSpray`, `prismSpray` (Lux prism rays), `cruorShards`, `ironJet`, `ironFan`, `ironSpikes` | Package statics, positions relative to the pose |
| `CastImpactGeometry` | The shared landed-hit burst: `flash`, delivery shapes (`melee`, `projectile`, `area`, `tether`, `sweep`), school flourishes (`clots`, `flames`, `shards`, `bolts`, `starburst`, `blobs`, `sparks`, `splinters`, `puff`) and `material(school)` | Fires automatically from `CastImpacts.landed`; `material(EnumBloodTemper)` is the school-to-material map to reuse |
| `CellHandGeometry`, `CellHandStreams` | Liquid blood streams palm to target (sheath, slug-bulging body, dark underside, highlight, braided second stream, whirl); `CellHandStreams.livePalm`, `staffHead`, `along` | `CELL_HAND` cue; `CellHandStreams` is public for anchoring |
| `CasterFrame`, `HeldWeaponPose`, `FirstPersonPalm` | Body yaw, head yaw, scale, crouch and the posed hand, first and third person; the real held weapon's pose and faces | Add the form to `ManipulationVisualRenderer.bodyAnchored` |
| `VisceralGeometry` | Pure maths: `side`, `strandPoint`, `wellPoint`, `bellRadius`, `crownSwordGrowth` | Public statics |
| `BloodStreams` | `stream` and `droplet` for code outside the batch | Public statics with a `MultiBufferSource` |
| `MaterialQuads` | School and rite material vertices with the shape/seed tile encoding; `seedTile` | Required for any new material vertex writer (`MaterialQuadGuardrailTest`) |
| `SchoolRenderTypes`, `SeededRenderTypes` | Cached school materials and seeded holders | Never build a `RenderType` per frame (`RenderTypeCachingGuardrailTest`) |

### Bespoke geometry classes worth borrowing from

`HematicRiposteGeometry` (the reference arm, shell/sinew/knots/claws/trail), `PuppetStringsGeometry` (knitting and peeling vein strings), `NerveCommandGeometry` (impulse, cranial net, nape sigil), `BloodAneurysmGeometry` (swelling knot, tearing sac, shreds), `CrimsonCoronationGeometry` (ring, spikes, sinew-hung swords), `UmbralTearGeometry` and `UmbralRendGeometry` (ink lips, violet cut, void), `LodestoneCollapseGeometry` and `LodestoneFieldGeometry` (iron shards, flux lines, filings), `SearingLashGeometry` (molten-cored whip), `CruorJavelinGeometry` (clot spine, frost strands), `RadiantWakeGeometry` (filament feathers), `FruitingContagionGeometry` (stalks, hyphal threads, spores), `FuneralBellGeometry`, `PhoenixDebtGeometry`, `DeadlyGazeGeometry`, `WeaponConjureGeometry`, `AvatarManifestGeometry`, `SanguineWardGeometry`, `SanguineMendingGeometry`, `BloodRushGeometry`, `BloodShotGeometry`.

The four Saint fight toolkits are in the same package and match the four Saint canon memories: Hemorath (`HemorathBloodGeometry`, `HemorathIronGeometry`, `HemorathFissureGeometry`, `HemorathImpactGeometry`) for Crimson Tithe; Seraphae (`SeraphaeLightGeometry`, `SeraphaeRadianceGeometry`, `SeraphaeTissueGeometry`) for Unclosing Eye; Putriciel (`PutricielGrowthGeometry`, `PutricielSporeGeometry`, `PutricielRotGeometry`, `PutricielSludgeGeometry`) for Bloom of Rot; Velorum (`VelorumIceGeometry`, `VelorumRimeGeometry`, `VelorumFrostGeometry`, `VelorumDarkGeometry`) for Endless Hour.

### Public bridges and server-side senders

| Part | What it does | How to call it |
|---|---|---|
| `client/render/world/OutpouringBrushes` | The public brush bridge for code outside the package: `begin`, `flow(Mat)`, `core`, `iron`, `flush`, `card`, `ribbonPath`, `ground`, `fire`, `smoke`, `tongue`, `clot`, `shard`, `plate`, `ferricQuad`, `sinew`, and particle helpers `flames`, `frost`, `lick`, `blood`, `mist`, `bolt` | Public statics; `TendonLineRenderer` is a working example |
| `common/manipulation/ManipulationVisuals` | `burst`, `attached`, `replay` (retire then resend so recasts replay), `chargeForm`, `endChannel` | Server side; pack seeded variation into `count` (keep it at least 1) |
| `common/manipulation/ManipulationParticles` | `activate`, `hurt`, `impact`, `accent`: bounded authored sprite accents (`ManipulationAmbientParticles` on the client) | Server side |
| `common/particle/HemoParticleData` | `glow`, `darkGlow`, `bloodCell`, `absorbedBloodCell`, `hitGlow`, `ember`, `emberSpark`, `bloodClaw`, `bloodAvatarHit`, `serpent`, `sporiticSpore` and others | Particle options for `sendParticles`; accents only, the geometry carries the effect |
| `PacketHandler.sendClawSlash` | HutosLib claw slash accent | Server side |
| `common/manipulation/ductilis/DuctilisLightningEffects` | HutosLib forked bolts with restrike: `conductorStrike`, `conductorDischarge`, `activationPotential`, `synapticJolt`, `conductiveMark`, `conductiveArc`, `hemolymphalPulse`, `sanguineWard`, `livingCircuit`, `sensoryResponse` | Server side |
| `common/manipulation/HemomancyTendrilEffects` | HutosLib tendrils: `luxRelease`, `umbraRelease`, `lumenSuture`, `bloodEclipse`, `blackVeil`, `hemorrhage`, `exsanguinate`, `osseousBloom`, `glacialGrasp`, plus configs | Server side |
| `common/manipulation/BloodFlowVisuals` | Persistent flows to `BloodFlowEffects`: `connect`, `lignum`, `toward`, `from`, `stop` | Server side |
| `common/manipulation/impact/CastImpacts` | Shared landed-hit cue; `deliveredAs` scope to force a delivery shape | Automatic on direct school hits |
| `client/render/outpouring/*` | Cast outpouring from the vascular section; `OutpouringRules.woundOnly` for spells that put their own blood on the body | Automatic per cast |
| `StrandRenderer` / `StrandClientState` | The five strand forms and their accents | `Strands.lash/jet/spray/plant/brand` in `Shapings` |

### Known gaps in the toolkit itself

- `manipulation_animus.fsh` still implements only shapes 0, 1, 2, 6 and 9 (`src/main/resources/assets/hemomancy/shaders/core/world/manipulation_animus.fsh` lines 24 to 44), so Animus `FOCUS` and `SLASH` still fall back to the glossy droplet. `manipulation_umbra.fsh` still has three shapes. Add new shape indices rather than changing 4 and 7, which Riposte relies on.
- Golden-angle (`i * 2.39996`) scatter is still the default layout across `AnimusMortemGeometry`, `ThermalGeometry`, `LuxUmbraGeometry` and `BlackVeilRenderer`; that is the main reason B rows read alike.
- Seeds still come from `entityId` or `from.hashCode()` in the shared tiers, so repeated casts by one caster look identical outside the bespoke classes.

## Upgrade order and batches

Order: focal and common first, worst score first. Each bespoke class follows the standard: its own `final` static-only geometry class with `handles(Form)` and `draw(...)`, hooked before `AnimusMortemGeometry`, a pure rules class for timing, a source-contract test for the hook and draw order, JVM tests for the rules.

| # | Batch | Members | Size | Notes |
|---|---|---|---|---|
| 1 | Vanilla sweep of the Ferric constructs | ferric_rampart, ferric_spikes, sanguine_magnetism (`EntityIronWall`, `EntityIronSpike`, `EntityIronPillar`) | S | Replace `SQUID_INK`/`ASH` with `HemoParticleData.darkGlow`/`bloodCell` and the existing `FerricDuctilisGeometry.shatter`; lower the three allow-list counts to 0 and drop the rows |
| 2 | Ductilis threads | sanguine_marionette, thread_ripper, axonal_transduction | L | Marionette onto `PuppetStringsGeometry` and delete the legacy `MARIONETTE_*` branch; a new severing class for Thread Ripper; world-side departure and arrival for Axonal Transduction |
| 3 | Saint canon memories | crimson_tithe, unclosing_eye, bloom_of_rot, endless_hour | M | One class each, built from that Saint's own fight geometry (Hemorath, Seraphae, Putriciel, Velorum) |
| 4 | Congeatio clot scatter | cryogenic_pulse, osseous_bloom, glacial_rampart, glacial_grasp, rimebound_sentence (lock moment) | M | Split `case STILLNESS,ICE,CRYOGENIC_PULSE` and `case BONE,FROZEN_VEINS,FROST_CONJURE`; give each its own silhouette |
| 5 | Mortem drains and growth | exsanguinate, carrion_communion, lignum_mortis, canopy_mortis, worked_lignum, a Mortem strand brand (hemorrhage, insatiable_hunger, grave_debt) | M | Drains on the `CellHandGeometry` liquid stream; growth on `FruitingContagionGeometry` hyphae |
| 6 | Perfectus and Magister capstones at B | synaptic_storm, eclipse_well, white_verdict, absolute_stillness, sanguine_tempest, ironhearted | L | The most visible gap left: each is a capstone with a generic body; one bespoke class each |
| 7 | Animus visibility fix and mid-tier | tendon_line (draw for trackers), blood_binding family per-mode, blood_cloud family, vital_effusion, sanguine_halo, living_land strike | M | Tendon Line needs the anchor synced to trackers before the renderer can draw other players' lines |
| 8 | Lux mending and light | lumen_suture, hematic_beacon, hematic_flare, prismatic_reproof quick cast | M | Warm core and crimson undertone accents, `SanguineMendingRules`-style stitching for Suture |
| 9 | Tenebris veils | black_veil_covenant, void_shroud, penumbral_drift, gloam_laceration quick cut, blood_eclipse spray | M | Overlays stay subtle and partly clear; the cut and the covenant are focal and go bold |
| 10 | Flammeus card swarms | sanguine_ignition, the four updrafts, pyretic_forge, crimson_flame_conjuration (and its plant), cauterizing_rebuke, furnace_veins, vitric_combustion | M | Replace golden-angle tongue rings with path-following tongues and named phases |
| 11 | Ductilis bolt family polish | activation_potential, synaptic_jolt quick cast, conductive_mark, hemolymphal_pulse, living_circuit, sovereign_instinct | M | Glow cores under the ribbons, rings as one `ribbonPath`, caster-side moment for Sovereign Instinct |
| 12 | Ferric minor | iron_retort, iron_choir, hematic_hewing, lode_draw, vascular_dowsing (only if wanted; it reads as UI) | S | |
| 13 | Toolkit enablers | Animus `FOCUS`/`SLASH` as new shader indices, Umbra shapes, per-cast seed in the shared tiers | M | Needs a client launch to prove the shaders compile |
| 14 | In-game review of the A rows | all 43 | M | A review pass with the existing review clients (`gradle/review-clients.gradle`, for example the casting and outpouring clients) before more A work stacks on unseen code |

The first three batches clear every D and the ten worst offenders.

## What could not be determined without a client

- Whether any A row actually reads at the Riposte bar in game. All of them are marked pending in-game review and this audit is source-only.
- Whether the strand forms (especially the shared `PLANT` and `BRAND`) read as distinct per school at play distance.
- How much the shared cast outpouring and cast impact already carry the C rows in practice; they may look better or worse than their source suggests.
- First-person intrusion of the body-anchored and strand effects, and how the B rows hold up under Minimal particles.
- Shader shape coverage was read from the `.fsh` files only; driver-specific compile issues need a launch.
- Whether the `NeuralOverloadEffect` vanilla `ELECTRIC_SPARK` (on the allow-list) is ever reached from a manipulation; no manipulation in `common/manipulation/` applies it by name.
