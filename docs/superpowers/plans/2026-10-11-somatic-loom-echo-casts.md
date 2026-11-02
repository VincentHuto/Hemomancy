# Somatic Loom Echo Casts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** Every castable memory's echo performs a real, recognisable cast at each loop release and at the climax. The echo uses the same client renderers the live manipulation uses, sized to the echo and aimed at the loom's front. The school-gesture fallback is left only for retired, uncastable manipulations.

**Architecture:**
- **Toolkit:** a client-only `EchoFx` toolkit feeds locally built packets into the public client entry points that live casts already use:
  - `ManipulationVisualRenderer.accept`
  - `ManipulationAmbientParticles.accept`
  - `LuxUmbraEffects.accept`
  - `BloodFlowEffects.accept`
  - `FerricDuctilisEffects.accept` (`NerveLinkPacket`)
  - `ClawSlashRenderer.spawn`
  - `BlackVeilRenderer.addVeil`
  - the HutosLib `BoltRenderer`
- **Delays:** a client tick scheduler (`EchoFx.later`) staggers multi-beat casts such as restrikes, slams and collapses.
- **Projectiles and constructs:** these have no world cue to replay, so they use short-lived client-only dummy entities (`EchoDummies`). Each dummy gets a negative id, is removed after a fixed life, and is never sent to the server.
- **Registration:** casts are grouped per school in `client/render/tile/harbinger/crafting/echo/<School>EchoCasts.java`. Each group registers into `EchoCasts`, and each batch shrinks `src/test/resources/echo_cast_pending.txt`.

**Tech Stack:** Java 21, NeoForge 1.21.1 client, JUnit 5 coverage test, native client capture.

**Spec:** `docs/superpowers/specs/2026-10-11-somatic-loom-weave-design.md` ("a real cast for every memory"). **Visual bar:** `docs/MANIPULATION_VISUALS.md` (Hematic Riposte). **Inventory:** the per-manipulation table of server→client cues, with call sites, in the session scratchpad `manipulation_visual_inventory.md`. Its facts are restated per task below wherever a task depends on them.

**Depends on:** Plan 1 (`EchoCast`, `EchoCasts`, `EchoContext`, the coverage test) and Plan 2 (stage-dependent echo scale).

## Global Constraints

- **Client-only.** Echo casts never touch the server, gameplay, capabilities, player position or school state:
  - never call `AxonalTransductionClient.update` or `SyncSchoolCombatS2CPacket`;
  - never `CastingAnimationClientState.accept` for a real player.
- **Particles:**
  - Hemomancy/HutosLib particles and cue renderers only.
  - No vanilla `ParticleTypes`. Where a live cast uses a vanilla particle (vital_effusion HAPPY_VILLAGER, vascular_dowsing dust), the echo substitutes a Hemomancy glow.
- **Keyed cues:**
  - Keyed forms (WELL, BEACON, ORE and CRUOR_SURFACE are keyed by `from`) are always sent first with `ticks = 0` and then fresh, so a looped echo replays instead of refreshing.
  - Body-skin forms (WOUND, HUNGER, GRAVE, ROT_INFECTION, FROZEN_VEINS, BONE, RIMEBOUND, CAUTERIZE) need a live entity, so the echo uses their world-burst counterparts instead.
- **Size.** Every radius and distance scales by `ctx.figure()`, the echo's absolute scale. A base echo's cast stays within the loom's footprint plus about 2 blocks; a Cortical echo's cast is life-size. No cast reaches more than 6 blocks from the loom.
- **Aim.** Casts aim at `ctx.target()`, an imagined body in front of the loom: ahead along the loom's front, at chest height. Self-casts centre on `ctx.feet()` or `ctx.chest()`.
- **Tone.** Each cast is the memory's own signature geometry. Do not substitute a generic burst when a dedicated geometry exists, as listed in the inventory's form → geometry table.
- Do not commit.

## Review Focus

1. A looped echo must replay keyed cues, never leave them refreshed and frozen. Pin this with the WELL and BEACON casts in a coverage assertion that their builders send `ticks = 0` first.
2. A dummy entity must never outlive its echo:
   - pin removal on its life expiry;
   - pin removal on level change.
3. Casts at a paused or off-screen loom must not pile up cues. `fireRelease` already skips paused loops; the 192-cue cap holds.
4. Retired manipulations stay on the gesture fallback. The coverage test reads `ManipulationRetirementRules` and asserts that exactly the retired names stay pending.
5. Nothing reaches the server. A source contract asserts that no `PacketDistributor` or `sendToServer` call appears under `echo/`.

---

### Task 1: The EchoFx toolkit, the extended context and the scheduler

**Files:**
- Modify: `echo/EchoCast.java`. `EchoContext` gains:
  - `feet` (`Vec3`);
  - `figure` (float, the absolute echo scale);
  - derived methods `chest()`, `ahead(double blocks)` and `target()`;
  - `scaled(double)`.
- Create: `echo/EchoFx.java`, with static helpers over the entry points listed in the Architecture.
- Create: `echo/EchoScheduler.java`. It runs on `ClientTickEvent.Post`, keeps a list of `(dueTick, Runnable)` with a cap of 256, and clears on level change.
- Create: `echo/EchoDummies.java`.
  - `spawn(ClientLevel, Entity, int lifeTicks)` assigns a fresh negative id and calls `level.addEntity`.
  - It removes the entity on expiry through `level.removeEntity(id, DISCARDED)` and clears everything on level change.
- Modify: `SomaticLoomEcho.fireRelease` to build the richer context.
- Tests:
  - `EchoContextTest` (JVM) covers `ahead`/`target` geometry and figure scaling.
  - `EchoCastCoverageTest` gains:
    - the retired-names assertion;
    - a "no server send under echo/" source contract;
    - a "keyed forms send ticks=0 first" source contract (it greps `EchoFx.keyed(`).

### Task 2: Animus (24) and Mortem (16)

Each cast follows its live cues, scaled.

**Animus:**
- **blood_shot and guided_blood_shot:** a dummy `BloodShotEntity` from the hands to the target, then a BLOOD_SPLASH (count 1).
- **hematic_mortar:** MORTAR_CHARGE at the hands, then a mortar dummy and BLOOD_SPLASH count 3.
- **sanguine_halo:** five orbiting shots.
- **blood_binding family:** a TEAR blood-flow from the hands to the target, then to chained points. BloodBindingTendril needs real entities, so a flow stands in for the tendril.
- **hematic_riposte:** a RIPOSTE burst at the chest, a claw slash and a RIPOSTE_STRIKE.
- **summon_avatar family:** an ANIMUS_CONJURE swell plus RUSH-coloured accents shaped to the arm, armour, leg or whole figure.
- **blood_cloud family:** a CLOUD burst at the target. The radius and the mode count follow the mode (expansive, pursuing, tempest).
- **blood_rush:** a RUSH burst at the feet, with a forward lunge accent.
- **blood_aneurysm:** ANEURYSM_CHARGE at the hands, then ANEURYSM_RUPTURE from the target to the chest.
- **vital_effusion:** GROWTH sprouts at three points around the feet.
- **conjure_blade:** ANIMUS_CONJURE at the hands.
- **crimson_coronation:** CROWN_CHARGE, then a CROWN with 8 lances at the chest, then one sword dummy and an ANIMUS_IMPACT.

**Mortem:**
- **conjure_axe and conjure_sickle:** MORTEM_CONJURE.
- **hemorrhage:** a TEAR flow plus a MORTEM_BURST at the target.
- **lignum, canopy and worked:** LIGNUM flows to three points ahead, then MORTEM accents.
- **exsanguinate:** an EXTRACTION flow from the target to the chest, then a MORTEM_BURST.
- **insatiable_hunger:** an accent at the target, then HUNGER_COLLAPSE.
- **grave_debt:** a MORTEM_BURST at the target, then GRAVE_REFUND.
- **blackhearted:** BLACK_HEART at the chest, then BLACKHEART_RUPTURE.
- **funeral_bell:** BELL_CHARGE, then a BELL at the feet.
- **carrion_communion:** COMMUNION flows from two points into the chest.
- **fruiting_contagion:** CONTAGION fruiting, then spreading.
- **crimson_tithe:** DEBT at the chest, then TITHE_COLLECT.
- **bloom_of_rot:** a BLOOM at the feet.

### Task 3: Ductilis (17) and Ferric (19)

**Ductilis:**
- **Lightning:** bolts are replayed through `EchoFx.bolt`, using the `DuctilisLightningEffects.config()` colours and a 3-tick restrike via the scheduler. They cover synaptic_jolt, activation_potential, synaptic_storm hops (3 points), hemolymphal_pulse (NERVE_PULSE plus 12 radial bolts, scaled), sanguine_ward (WARD plus 6 ring arcs), conductive_mark (5 ring arcs plus a MARK burst) and living_circuit (`NerveLink` plus CIRCUIT).
- **Commands:** hematic_rebuke and hematic_impressment use NERVE_COMMAND with count 1 or 2 from the target to the eyes. sanguine_marionette uses a MARIONETTE_TETHER, then a MARIONETTE_ORDER. sovereign_instinct uses COMMAND plus three SOVEREIGN_LASH.
- **deadly_gaze:** GAZE_CHARGE, then GAZE_GRIP, then GAZE_SLAM 12 ticks later.
- **thread_ripper:** THREAD_CHARGE, then THREAD.
- **conjure_crossbow:** FERRIC_CONJURE.
- **axonal_transduction:** a gold-glow path along the warp threads to the castle (glow particles), plus an ARC bolt. It never touches player state.

**Ferric:**
- **The needle family:** each variant uses its own charge form (NEEDLE_CHARGE, FAN_CHARGE or LANCE_CHARGE), then needle dummies: 5, 9 in a fan, or 3 piercing.
- **conjure_staff, blood_absorption and blood_projection:** a FERRIC_CONJURE at the hands. They have no live world cue; the conjure form stands in.
- **sanguine_mending:** MENDING.
- **vascular_dowsing:** ORE locators at three points below the loom, sent through `EchoFx.keyed`.
- **iron_retort:** RETORT, then FERRIC_IMPACT.
- **ironhearted:** IRON_CHARGE, then IRON_HEART.
- **sanguine_magnetism:** MAGNET at the target, then LODESTONE_COLLAPSE, with a pillar dummy.
- **ferric_rampart and ferric_spikes:** wall and spike dummies, with FERRIC_IMPACT.
- **iron_choir:** CHOIR plus three FERRIC_IMPACT intercepts.
- **lodestone_collapse:** IRON_CHARGE, then LODESTONE_COLLAPSE.

### Task 4: Lux (12), Tenebris (11), Flammeus (13) and Congeatio (11)

**Lux:**
- **conjure_spear:** FERRIC_CONJURE.
- **hematic_flare:** a lux flow to the target, then FLARE.
- **prismatic_reproof:** three VERDICT beams.
- **hematic_beacon:** keyed BEACON plus a lux flow.
- **lumen_suture:** SUTURE flow, then LUX_MENDING.
- **white_verdict:** VERDICT_CHARGE, then WHITE_VERDICT.
- **radiant_wake:** RADIANT_WAKE standing, then gliding, plus FLARE.
- **unclosing_eye:** an EYE burst, lasting 60 ticks.

**Tenebris:**
- **conjure_claws:** a claw slash.
- **umbral_step and umbral_reversal:** TELEPORT at the feet, then UMBRA_ARRIVAL 2 blocks aside.
- **void_shroud and penumbral_drift:** VEIL.
- **gloam_laceration:** UMBRA_SLASH plus a claw.
- **blood_eclipse:** three umbra flows in a cone.
- **black_veil_covenant:** a small `addVeil` (r 2·figure, 60 ticks) plus 4 surface flows.
- **eclipse_well:** WELL_CHARGE, then a keyed WELL.
- **umbral_rend:** UMBRAL_REND.

**Flammeus:**
- **conjure_torch:** FLAME_CONJURE.
- **crimson_flame_conjuration:** FLAME_CONJURE at a point ahead.
- **pyretic_forge:** FORGE.
- **sanguine_ignition and cauterizing_rebuke:** IGNITION. cauterizing_rebuke also gets a CAUTERIZE-coloured accent.
- **vitric_combustion:** GLASS_CHARGE, then GLASS.
- **Updraft variants:** UPDRAFT, using counts 1 to 4 per variant. expulsive_updraft also gets an IGNITION.
- **furnace_veins:** FURNACE.
- **phoenix_debt:** PHOENIX_READY, then PHOENIX.
- **searing_lash:** SEARING_LASH.

**Congeatio:**
- **conjure_flail:** FROST_CONJURE.
- **glacial_grasp:** ICE plus FROST_ADVANCE.
- **cryogenic_pulse:** CRYOGENIC_PULSE.
- **glacial_rampart:** CRUOR_FORM at five points in a short wall, then CRUOR_BREAK. Do not use CRUOR_SURFACE: it needs real frozen_cruor blocks.
- **osseous_bloom:** a BONE world burst at three points.
- **absolute_stillness:** STILLNESS.
- **rimebound_sentence:** ICE_CHARGE, then a RIMEBOUND world burst.
- **cruor_javelin:** CRUOR_JAVELIN.
- **endless_hour:** HOUR, then HOUR_BREAK.

### Task 5: Coverage close-out and native gallery

- Shrink `echo_cast_pending.txt` to exactly the retired names: blood_lamp, crimson_harvest, hemosynthesis, vital_reservoir, sanguine_excavation, ferric_resonance, glacial_bastion, blood_eclipse_mantle, crimson_sight, glacial_circulation, ferric_transmutation, vigil_of_glass, hematic_ballast, summon_thrall and venous_travel.
- `EchoGalleryReview` (gameTest source set, `-Dhemomancy.echoGallery`) releases each school's casts in turn before a fixed camera. It uses a context at figure 0.42 and at figure 1.0, and captures one screenshot per four casts into `build/loom-client-review/screenshots/echo-*.png`.
- Review the captures. Fix any cast that is invisible, oversized or generic.
- Docs: `HEMOMANCY_REFERENCE.md` §25.7 rendering notes, and the validation note.
