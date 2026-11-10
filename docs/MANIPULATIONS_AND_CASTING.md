# Manipulations and Casting

Status (2026-11-01): 123 registered manipulations (108 active, 15 retired) across eight schools, two balance passes landed, casting animations v2 and the Cast Outpouring committed in `18dc1ec92` (2026-10-19); cast impacts, the body-anchored basics and the infusion vein overlay are built but uncommitted and unreviewed in game.

## What it is

Blood manipulations are the abilities a Harbinger fuels with blood. Each registered entry has a temper (school), a secondary temper, a rank, a cast type (Quick, Charged, Continuous, Passive), a cost and a vascular section it draws from (`HEMOMANCY_REFERENCE.md` §8.1). The eight tempers are Animus, Flammeus, Ductilis, Lux, Mortem, Congeatio, Ferric and Tenebris (§9). Ranks run Humilis (D0), Mediocritas (D1), Summa (D3), Magister (D5), Perfectus (D6).

**Roster after pass 2** (`docs/archive/audits/MANIPULATION_BALANCE_PLAN.md` §11.2, primary temper by rank):

| Temper | Total | H | M | S | Ma | P |
|---|---|---|---|---|---|---|
| Animus | 23 | 7 | 7 | 6 | 2 | 1 |
| Ductilis | 15 | 4 | 5 | 2 | 2 | 2 |
| Ferric | 15 | 5 | 4 | 3 | 2 | 1 |
| Mortem | 15 | 4 | 4 | 3 | 2 | 2 |
| Flammeus | 13 | 5 | 5 | 1 | 1 | 1 |
| Tenebris | 10 | 2 | 5 | 1 | 1 | 1 |
| Congeatio | 9 | 2 | 3 | 2 | 1 | 1 |
| Lux | 8 | 1 | 4 | 1 | 1 | 1 |

Every school has at least one entry at every rank, except that Lux's only Summa entry is a Saint canon memory, which no longer counts toward coverage (D-19, 2026-11-06; see Open questions). Pass 1 (2026-10-09) normalised arrow projectile damage, repriced Sanguine Ward and Endless Hour, buffed Funeral Bell and Prismatic Reproof, fixed Glacial Grasp, re-homed the Blood Needle family to Ferric and Rebuke/Impressment to Ductilis, and added six manipulations with bespoke geometry (Searing Lash, Cruor Javelin, Umbral Rend, Fruiting Contagion, Radiant Wake, Lodestone Collapse). Pass 2 (2026-10-10) added the 25% charge floor, server-observed charge clamp, Crimson Mastery coverage, one shared boss predicate, validation before payment, the Animus re-rank and per-school tuning (plan §10, §11; `HEMOMANCY_REFERENCE.md` top notes).

**School combat layer.** Every DIRECT school hit applies the school's state through `SchoolStates` (Pressure, Necrosis, Lodestone, Searing, Rime, Disrupted, Illuminated, Obscured) and `SchoolCombatEvents.prepare`/`commit` consume them for the payoffs (Aneurysm, Funeral Bell, Vitric Combustion, Lodestone Collapse, Umbral Rend and the rest; `wiki/School-Combat.md`, plan §4 and §10).

**Drudge and entity casts.** A manipulation attaches its Drudge behaviour with `setDrudgeAction(...)` (work in `DrudgeActions`) and what a Will or Vesper does when it casts with `setEntityCast(EntityCasts::x)`; `EntityManipulationEffects.cast` applies only the shared rules (`AGENTS.md`).

**Engram weaving.** The Somatic Loom teaches a memory by unfurling an eight-pointed engram on the ground; the weaver threads one fragment per recipe enzyme home with a Living Staff, colours never sharing a node, with scars, valves, prisms and false fragments by rank. Spans (1 enzyme = 3 spans, 192 per vial) are spent only at completion and no weave can kill. The player's guide is [`engram-weaving/README.md`](engram-weaving/README.md); the design is `docs/superpowers/specs/2026-10-15-somatic-loom-engram-weaving-design.md`, which superseded the combat half of the 2026-10-11 witnessed weave (setup, tiers, echo casts and the Cortical dome stayed).

**Casting presentation** (all 2026-10-19):

- *Casting animations v2*: 40 school-by-rank keyframe tracks in both views, 31 bespoke styles, an aim layer owned by purpose, and the Unstained Still Art lane (five clarity stages, endings). Six layers: base track, cast type, rank scope, aim layer, bespoke override, secondary accent (`wiki/Casting-Animations.md`).
- *Cast Outpouring*: the caster's own blood leaves the manipulation's vascular section, shader veins light under the skin, the blood turns into the school's material, scales with rank and leaves fading client-only marks. Nothing detached floats behind the caster. Replaced hand veins, rank constructs and Lux wings.
- *Cast impacts*: one `CAST_IMPACT` per landed DIRECT school hit, hooked last in `SchoolCombatEvents.commit`; shape by delivery (MELEE, PROJECTILE, AREA, TETHER), flourish by school, 10-tick life, one per target per 5 ticks unless heavy. Uncommitted.
- *Body-anchored basics*: Blood Rush, Sanguine Ward, Sanguine Mending, the Living Arsenal conjure (one class for all `conjure_*`), the cell hands (Blood Absorption/Projection) and the avatar manifest, drawn from a `CasterFrame`. Uncommitted.
- *Infusion veins* (2026-10-20): blood infusion gets a vein overlay growing from the impact point as a sore and a separate melt shell. Uncommitted.

The visual bar is [`MANIPULATION_VISUALS.md`](MANIPULATION_VISUALS.md): a focal effect is its own geometry class with a named timeline, layered shell/sinew/knots/business end/motion anatomy, white base tint plus exactly two accents, rooted on the body and aimed with the head, with a separate impact burst; particles are accents and geometry carries the effect.

## Where it lives

| Piece | Path |
|---|---|
| Registry, tree | `common/init/ManipulationInit`, `ManipulationTreeInit` |
| Implementations | `common/manipulation/<temper>/` (`animus`, `congeatio`, `ductilis`, `ferric`, `flammeus`, `lux`, `mortem`, `tenebris`), plus `saint/`, `stillarts/`, `family/` |
| Cast pipeline | `common/manipulation/ManipulationCastingRules`, `ManipulationChargeClock`, `ManipulationRankGates`, `ManipulationReactiveEvents` |
| Targeting | `common/manipulation/ManipulationCombatHelper` (`canHarm`, `Reach`, `canHarmFor`, `isHostileTarget`) |
| Drudge, entity casts | `common/manipulation/DrudgeActions`, `EntityCasts`, `EntityManipulationEffects` |
| School states | `common/damage/SchoolCombatEvents`, `SchoolStates`, `SchoolStateRules` |
| Cast impacts | `common/manipulation/impact/CastImpactRules`, `CastImpacts`; `client/render/world/CastImpactGeometry` |
| Visual cues | `common/manipulation/ManipulationVisuals` (forms) to `client/render/world/ManipulationVisualRenderer` (tier order, `bodyAnchored`) |
| Reference and basics geometry | `client/render/world/HematicRiposteGeometry`, `BloodRushGeometry`, `SanguineWardGeometry`, `SanguineMendingGeometry`, `WeaponConjureGeometry`, `CellHandGeometry`, `CellHandStreams`, `AvatarManifestGeometry`; rules classes beside each manipulation |
| Hand and weapon anchors | `client/render/item/harbinger/FirstPersonPalm`, `HeldWeaponPose` |
| Casting tracks | `client/player/tracks/` (`TrackAuthoring`, `CastingTracks`, `AimLayers`, eight `<School>Tracks`, `StyleTracks`, `PerfectusTracks`, `GameplayTracks`, `ChannelTracks`, `StillArtTracks`, `StillArtEndings`); model in `client/player/CastingTrack`, `CastingTrackRules`, `CastingPlayback`, `CastingPlayerPose` |
| Outpouring | `client/render/outpouring/` (`OutpouringRules`, `OutpouringRenderer`, `CastVeinsLayer`, `OutpouringMarks`, `OutpouringScreenPasses`, ...), bridge `client/render/world/OutpouringBrushes`, server `common/manipulation/outpouring/` |
| Engram weaving | rules `common/tile/harbinger/crafting/engram/` (`EngramBoard`, `EngramAim`, ...); client `client/render/tile/harbinger/crafting/engram/` and `shaders/core/world/engram_sigil.*` |
| Infusion veins | `common/event/BloodFeedStyle`, `BloodInfusionManager`; `client/render/world/BloodStructureFeedWarpRenderer`; `shaders/core/world/blood_infusion_veins.*`, `blood_structure_warp.*` |
| Review | `/hemocastpreview`; two-client casting harness (`tools/oneoff/casting-animation-review.init.gradle`, `-PcastingRun`, `-PcastingIntegrationOnly`, `-PcastingBasicsOnly`, `-PcastingImpactsOnly`; `CastingClientAcceptance`); `outpouringReviewClient`, `infusionReviewClient` and `runLoomReviewClient` rows in `gradle/review-clients.gradle` |
| Tests | GameTests `ManipulationBalancePassGameTests`, `ManipulationBalancePass2*GameTests`, `CastingAnimationGameTests` (`casting_animation_validation`), `SomaticLoomWeavingGameTests`, `EntityCastCharacterisationGameTests`, `BloodInfusionGameTests`; JVM `CastingTrackRulesTest`, `CastingTracksTest`, `client/render/outpouring/*Test`, `ManipulationVanillaParticleSourceTest`, `BalancePassVisualSourceTest` |
| Docs | `HEMOMANCY_REFERENCE.md` §8, §9, "Casting presentations (2026-09-16)"; `wiki/Blood-Systems.md`, `wiki/Casting-Animations.md`, `wiki/Casting-Animation-Validation.md`, `wiki/School-Combat.md`; archived audits in `archive/audits/MANIPULATION_BALANCE_PLAN.md` and `MANIPULATION_VISUAL_CATALOGUE.md` |

## Rulings that bind

User rulings:

- The Hematic Riposte arm is the gold-standard visual; reuse its layered recipe when upgrading any focal manipulation, rite, boss or impact effect (`MANIPULATION_VISUALS.md`).
- Custom Hemomancy/HutosLib particles and geometry first; vanilla particles only as a deliberate fallback. No vanilla `ParticleTypes` in `common/manipulation/` (`ManipulationVanillaParticleSourceTest`), none in the Outpouring, impacts or Still Arts.
- No rings, funnels or "vacuum cleaner" mechanical shapes on blood effects; prefer organic behaviour (gravity, capillaries, drips). The first cell-hand use geometry was rejected for this.
- Blood streams read as fluid with travelling bulges, a sheath, a dark underside and a highlight; constant-width thin ribbons are "stiff wires".
- Every hand visual lines up with the real first-person palm while moving (`FirstPersonPalm`); conjures coat the real weapon's faces in its real held pose (`HeldWeaponPose`).
- Infusion veins grow slowly from the impact point as a sore, not a star of spokes; they spill onto neighbours, go dark before the break, and the melt shell is a separate overlay. "Lava lamp" contour veins and a stiff radial tree with glowing cracks were both rejected.
- Casting v2 goal: readable, school-distinct casting in both views, less restrained than the 2026-09 revision but never disjointed (no torso off the waist, no arms off the shoulders).
- Outpouring Revision 1: reuse the mod's toolkit (the first build's own material and mark shaders were "blocky"); veins must look like the floor-shader veins at screen resolution; no copper, brass, bronze or silver on the Harbinger lane; nothing detached behind the caster.
- Engram weaving must be fun, challenging and "alien, magical, visceral" in the style of the Cardinal floor membrane; no weave can kill.

Architecture rules (`AGENTS.md`):

- "A caster chooses targets through `ManipulationCombatHelper`: `canHarm(player, target)` is the strict rule, `canHarm(caster, target, Reach.X)` keeps an older filter's verdicts with a named preset (moving a site to `Reach.STRICT` is a pending decision, G-2b or F-16), and `canHarmFor(source, target)` serves constructs, summons, projectiles and effect holders. A `Monster.class` gather passes `canHarm` in its own arguments (`CombatTargetingGuardrailTest`)."
- "Manipulation implementations live under `common/manipulation/<temper>/`; many also define Drudge AI helpers with `setDrudgeAction(...)`, and what an entity (a Will, Vesper) does when it casts one is attached the same way with `setEntityCast(EntityCasts::x)`; `EntityManipulationEffects.cast` only applies the shared rules (`EntityCastGuardrailTest`)."
- "Particle data is built through `common/particle/HemoParticleData`. A particle that carries only a colour uses `ColorParticleOptions`... `ParticleDataGuardrailTest` lists the other data classes and rejects per-factory `createData` helpers."
- "Render types are built once: a static constant, a school material from `SchoolRenderTypes`..., or a `SeededRenderTypes` holder keyed by seed... Never build a `RenderType` per call or per frame; `RenderTypeCachingGuardrailTest` rejects it."
- "School and rite material vertices (U = shape tile, V = seed tile, alpha × 255) are written through `client/render/world/MaterialQuads`; wrap a seed with `MaterialQuads.seedTile`, never `floorMod(seed, 127)` (`MaterialQuadGuardrailTest`). Ferric keeps its own 128-tile constant until decision V-3b."
- "Client animation time comes from `client/render/AnimationTime`: `partialTick()` (reads 1 under `/tick freeze`), `partialTickWhileFrozen()`, `gameTime(level[, partialTick])` and `age(entity, partialTick)`... Wall-clock animation is listed until decision A-4 (`AnimationTimeGuardrailTest`)."
- "Third-person player poses apply through `client/player/PlayerAnimationRegistry.THIRD_PERSON`, in its listed priority order (staff planting, offset reset, casting, then the first claiming pose of microscope, vial injection, torch breath, Unstained swing, living-weapon stroke, then the warp chair); `MixinHumanoidModel` calls only the registry (`PlayerAnimationGuardrailTest`)."
- Visual timing never gates gameplay: the server owns windows, damage and cooldowns (`MANIPULATION_VISUALS.md`, casting v2 spec §3).
- A player pays blood only through `BloodAccounting.pay` or `payUpkeep` (`AGENTS.md`); rank gates through `ManipulationRankGates`, family mastery through `ManipulationFamilyRegistry` (plan §8).
- Plan §9, answered 2026-11-06 (D-19): Animus stays the larger default school; Thread Ripper is a Circus-only reward (no weaving recipe; the Ringmaster's Liberation route is its only source); Saint canon memories do not count toward school tier coverage; the Sanguine Tempest's extra Neurotic enzyme is removed, so its recipe is the Blood Cloud baseline plus its stage.
- Mind Broken adds a 15% cost contribution (`mind_broken`, EFFECT) to every manipulation (D-3, 2026-11-06).

## Calls made without the user

- **Balance pass 1 deviations** (plan §10): Endless Hour kept overflow-only deferral and lost its stacked buffs instead of the two options proposed; Sanguine Marionette stayed a Blood Binding form; Blood Cloud was not moved and Fruiting Contagion filled Mortem's Summa; Living Circuit falls back to the caster (reversing an earlier GameTest-pinned decision); the six new memory icons are placeholder 8x8 glyphs.
- **Balance pass 2 rulings** (plan §11.3): Gloam ambush is `isInvisible() || dark`; the charge clamp tolerance is `elapsed + max(2, ceil(0.15 × elapsed))`, so a player can shave about 13% off long charges; kill alignment splits across blood-profile tempers and unprofiled mobs give nothing; Thread Ripper never unravels tamed animals or mounts and returns a Drudge's memory; stale key-downs expire; Endless Hour settles through a dedicated `endless_hour_debt` damage type that Lethe Silver Ward, Still Waters and the Sheolic Bastion no longer catch; the Eclipse Well pulse is PERIODIC. The Tempest weaving recipe gained a Neurotic enzyme (+1) rather than a swap; the owner removed it on 2026-11-06 (D-19).
- **Casting v2 builder's calls** (spec §16): Java-authored tracks, a waist pivot capped at ±14°, purpose as a full-weight aim layer, Still Arts as a lane on the existing packet, the `StillArtInit` vanilla-particle sweep, and the Tier A bespoke set. The local quick-cast flick was dropped during the build (memory ledger). The five "(user)" decisions in §16 were never answered in writing.
- **Outpouring** (spec "Timeline"): quick casts peak on arrival with a 1 to 2 tick swell; continuous channels ramp over 10 to 20 ticks; passive and staff-form toggles get a reduced outpouring; cancel draws the blood back. The user did review the first build live (Revision 1), so the per-school direction there is the user's.
- **Cast impacts** (spec header: "Every call below is mine"): the delivery order, the 5-tick per-target throttle, the 10-tick timeline, the per-school flourishes, Congeatio's flash on the LUX material, and the post-capture position and size fixes.
- **Body-anchored basics** (memory, 2026-10-19): the six geometries, the `bodyAnchored` hook and `CasterFrame`, the conjure cue moved to `LivingStaffWeaponFormHelper.syncMorph`, the Sanguine Ward crown now closing at a 6 pool (the old crown waited for 8 and never drew), and Sanguine Mending stitching the tool rather than the chest (catalogue rows marked "Rebuilt 2026-10-19"). The user corrected the cell-hand shapes, the palm alignment and the weapon match in three follow-ups.
- **Infusion veins** (memory, 2026-10-20): the three `BloodFeedStyle` values, the glided `shownOrigin`, the 3x3x3 neighbour spill with a 2 to 7 px fade, and the melt shell hiding while fed; the look itself followed the user's rejections.
- **Engram weaving** (spec 2026-10-15): the {8/3} star geometry, 25 nodes and 56 edges, size by loom stage and the ground drape were the builder's; the four sections of the design were approved in conversation.

## Open questions

- Plan §9 (both passes): do moved families need a migration note? (The rest of §9 was answered on 2026-11-06, D-19.)
- Lux has no Summa entry once Saint memories stop counting (the Unclosing Eye was its only one). Add a Lux Summa manipulation, or re-rank one of its four Mediocritas entries?
- Casting v2 spec §16 user decisions: how far from the restrained revision; keep the quick-cast flick (since dropped); Tier B scope (Magister channel loops, ground-touch continuous casts); keep the Still Art ground ring; whether to adopt an animation library.
- Pending architecture decisions (`architecture/CONSOLIDATION_PROGRESS.md` Decisions table):
  - G-1 / F-18: explicit damage stages and one death-save stage (changes blood lost per hit; needs an ordering test).
  - F-16: attribute AoE damage from armour, Still Arts, Sheolic and Saprovitta and respect allies and PvP; the sites now name their `Reach` preset and approval moves them to `STRICT`.
  - G-2b: the caster's own puppets stop matching `Monster.class` filters (`allies = false` in `Reach.MONSTER_FILTER`; Drudges would need their owner resolved).
  - G-2c: one boss definition (`ManipulationReactiveEvents.isBoss`, `ConstrictorCordEntity.isBossTarget` and `ThreadRipperManip.protectedBody` disagree).
  - F-13: scar upkeep becomes reserve-protected (one flag on `ScarDefinition.UPKEEP`).
  - F-15: living weapons check `isActive()` and stop wiping the reserve (the `BLOOD_PRICE` sites in Axe, Blade, Crossbow and Tool).
  - A-4: which wall-clock animations should freeze under `/tick freeze` (per animation; 29 client classes listed).
  - V-3b: Ferric's 128 seed-tile constant against the shared 127.
  - V-3c: Outpouring surface marks round their alpha by hand; approving writes them through `MaterialQuads`.
- Catalogue enabler 1 (`MANIPULATION_VISUAL_CATALOGUE.md` §5): real FOCUS and SLASH shapes in the Animus shader are deferred because changing the fallback would alter Hematic Riposte; add new indices instead.

## Proof owed and known gaps

- Nothing in either balance pass has been playtested; every number in plan §2, §10 and §11.1 is a starting value. Pass 2 deferred items (§11.4) include `mayAlter` gaps in Glacial Rampart and Rimebound Sentence, the second boss list in `ConstrictorCordEntity`, Congeatio's vanilla x5 freeze bonus, unsaved `EntityIronPillar.pulled`, and several GameTests that use fallback contexts or skip positive paths.
- Every "A? (pending in-game review)" row in the visual catalogue, including the six balance-pass effects and the 2026-10-19 rebuilt basics, has been seen only through the review harnesses, not in play.
- Casting v2 (`wiki/Casting-Animation-Validation.md`): the night and WIDE captures passed in the close-out; still open are the crouch waist pivot and a few small items under "Final: minor (deferred)" in `.superpowers/sdd/2026-10-19-casting-animations-v2/progress.md`, plus a side or rear camera review and Animus and Flammeus Perfectus first-person fists sitting low.
- Cast impacts, the basics and the infusion veins are uncommitted; the `-PcastingImpactsOnly` and `-PcastingBasicsOnly` harness runs are the only evidence. Blood Rush's effect particles linger into the following harness scenes (a fixture artifact, not a bug).
- The Outpouring real-effect hook is built but off (`cast_outpouring.realEffectMinRank` default -1).
- Shader changes (outpouring veins, `blood_infusion_veins`, `engram_sigil`) need a client launch to prove they compile; offline validators miss driver errors (`AGENTS.md`).
- The engram weaving ledger lists deferred minors (`.superpowers/sdd/2026-10-15-somatic-loom-engram-weaving/progress.md`); several loom files carry CR CR LF line endings.
- `HEMOMANCY_REFERENCE.md` §8.2 was resynced in both passes, but the catalogue's "Still Arts" accents and the uncommitted work are described only in the wiki and specs.

## History

| Date | Event | Source |
|---|---|---|
| 2026-09-07 | Earlier manipulation balance pass | `HEMOMANCY_REFERENCE.md` "Manipulation balance pass, 7 September 2026" |
| 2026-09-09, 09-16 | Formation/established/dissolution visual lifecycle; `CastingClips` 40 three-pose tracks and the restrained-motion revision | `HEMOMANCY_REFERENCE.md` §8 intro and "Casting presentations"; `wiki/Casting-Animation-Validation.md` |
| 2026-10-09 | Hematic Riposte standard written; visual catalogue graded; balance pass 1 built (plan §10); tier 1 to 3 touch-ups implemented in source | [`MANIPULATION_VISUALS.md`](MANIPULATION_VISUALS.md), [`archive/audits/MANIPULATION_VISUAL_CATALOGUE.md`](archive/audits/MANIPULATION_VISUAL_CATALOGUE.md), [`archive/audits/MANIPULATION_BALANCE_PLAN.md`](archive/audits/MANIPULATION_BALANCE_PLAN.md) |
| 2026-10-09 to 10-10 | Balance pass 2 built (plan §11; `superpowers/plans/2026-10-09-manipulation-balance-pass-2.md`) | plan §11 |
| 2026-10-11 | Somatic Loom witnessed weave designed (tiers, echo casts, Cortical dome) | `superpowers/specs/2026-10-11-somatic-loom-weave-design.md` |
| 2026-10-15 | Engram Weaving replaces the loom fight | `superpowers/specs/2026-10-15-somatic-loom-engram-weaving-design.md`, [`engram-weaving/README.md`](engram-weaving/README.md) |
| 2026-10-19 | Casting animations v2 and the Cast Outpouring built and committed (`18dc1ec92`); Outpouring Revision 1 after live review | `superpowers/specs/2026-10-19-casting-animations-v2-design.md`, `2026-10-19-harbinger-cast-outpouring-design.md`, `wiki/Casting-Animations.md` |
| 2026-10-19 | Cast impacts and the body-anchored basics built, uncommitted | `superpowers/specs/2026-10-19-cast-impacts-design.md`, catalogue "Rebuilt 2026-10-19" rows |
| 2026-10-20 | Infusion vein overlay and melt shell built, uncommitted; `infusionReviewClient` | `gradle/review-clients.gradle`, `shaders/core/world/blood_infusion_veins.*` |
| 2026-10-23 | Architecture consolidation records the pending decisions above | [`architecture/CONSOLIDATION_PROGRESS.md`](architecture/CONSOLIDATION_PROGRESS.md) |
