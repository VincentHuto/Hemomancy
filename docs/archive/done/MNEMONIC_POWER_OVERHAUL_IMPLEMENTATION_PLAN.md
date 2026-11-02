# Unified Mnemonic Power Overhaul — Implementation Tracker

## Purpose

This tracker combines `MUSCLE_MEMORY_UNIFIED_RADIAL_IMPLEMENTATION.md` and
`HEMOMANCY_MNEMONIC_TAXONOMY_IMPLEMENTATION_BRIEF.md` into one implementation
sequence for the current `neo-1.21.1` checkout. The first document remains the
mechanical design record; the second remains the taxonomy, lore, and dialogue
design record. Neither is rewritten as though its proposals were always canon.

The overhaul keeps one Hematic Memory system. Conventional manipulation
memories are **Noetic Memories**; embodied, tincture-prepared powers are
**Thelemic Memories**. They share the radial, memorization capacity, Reliquary,
and Dendritic Distributor.

## Protected Compatibility Boundaries

- [x] Work in the current checkout and preserve unrelated dirty work.
- [x] Preserve `muscle_memory:<id>` loadout keys and all existing registry,
  recipe, attachment, NBT, packet, manipulation, and legacy slot identifiers.
- [x] Preserve migration from legacy `Primed` NBT.
- [x] Keep Blood Absorption, Blood Projection, and sickle control exemptions.
- [x] Do not add a second radial, mnemonic XP meter, ninth tendency, potion-stat
  catalogue, or irreversible early class choice.
- [ ] Do not call the overhaul release-validated until live visual, combat,
  persistence, progression, and multiplayer checks pass.

## Rename Risk Matrix

| Surface | Action | Compatibility rule |
|---|---|---|
| UI, dialogue, tooltips, Liber, advancements | Muscle Memory -> Thelemic Memory | Player-facing only |
| Formal manipulation classification | Noetic Memory | `manipulation` remains the practical verb/control |
| Java package/class names | Retain unless runtime-only | Avoid needless migration risk |
| `muscle_memory:<id>` and `MUSCLE_MEMORY` | Retain | Persistent save/network contract |
| `Primed` NBT | Read and migrate | Never rewrite old saves in place |
| Historical design documents | Retain and mark superseded | Preserve design evidence |

## Work Group 1 — Shared Runtime Taxonomy

Dependencies: existing mixed `MemorySlotRef` loadouts.

- [x] Add runtime-only `HematicMemoryExpression` with `NOETIC` and `THELEMIC`.
- [x] Expose expression from the shared memory-entry abstraction.
- [x] Map manipulation entries to Noetic and bodily entries to Thelemic.
- [x] Add compatibility tests proving storage and NBT are unchanged.

Acceptance: consumers can display expression without parsing names or
tendencies, and old loadouts deserialize identically.

## Work Group 2 — Thelemic State, Vessels, and Atomic Preparation

Current status: **WIP requiring stabilization**. The dirty tree already contains
learned/reserve/equipped/active/triggered/armed state, legacy migration, flask
and jug doses, reserve caps, Alembic vessel rules, and transactional helpers.

- [x] Keep learned knowledge through death; clear prepared/active/cooldown/
  empowered/armed state.
- [x] Five-minute doses, thirty-minute cap, three-dose flask, six-dose jug.
- [x] Require 2,500-blood Bloody Flask and 5,000-blood Bloody Jug.
- [x] Return cured flask/jug after the last dose.
- [x] Validate Alembic inputs and output capacity before ingredient commit.
- [x] Finish player-facing reserve/prepared/active terminology.
- [x] Re-run all sixteen tincture recipe, model, dose, and transaction audits.

Acceptance: failed preparation consumes no formation, enzyme, blood, or vessel;
death and legacy migration preserve exactly the intended state.

## Work Group 3 — Unified Radial and Communication Vocabulary

Current status: **WIP requiring stabilization**. The combined radial, tincture
icons, reserve color, pulses, crack overlays, dead-section blocking, edge meter,
and mixed shared capacity exist in the dirty tree.

- [x] Full color with reserve; grayscale without reserve.
- [x] Neutral inactive background, slow active pulse, faster armed pulse.
- [x] Fine/dense/maximum cracks for varicose/clotted/dead sections.
- [x] Edge reserve meter and exact duration tooltip.
- [x] Selection is distinct from activation; normal use toggles and sneak-use arms.
- [x] Add expression, section, reserve, blood cost, strain, and blocked reason to
  the tooltip.
- [x] Verify combined visual-state tests, not just isolated effects.

Acceptance: one radial communicates memory class, readiness, activity, risk,
and body damage without requiring the detailed Vascular Status screen.

## Work Group 4 — Trigger Transactions and Overexertion

Current status: **WIP requiring stabilization**. All eight trigger families,
Morphling resonance, Sanguine Reserve integration, ten-second arming, 2x blood,
2.5x strain, and ordinary fallback exist.

- [x] Ensure armed state is consumed only after a valid payment commits.
- [x] Invalid actions and failed eligibility do not consume arming.
- [x] Enhanced payment failure falls back to the ordinary response.
- [x] Preserve enhanced behavior for all eight Thelemic Memories.
- [x] Recheck partial-payment, recursion, duplicate-hit, and blood/healing loops.

Acceptance: validation precedes every blood, borrowed blood, strain, tendency,
cooldown, armed-state, and effect mutation.

## Work Group 5 — Reliquary and Dendritic Distributor

Current status: **WIP requiring stabilization**. Mixed entries, shared capacity,
auto-equip, referral, placed-block teaching, mixed Distributor storage/preview,
and absent-Thelemic deactivation exist.

- [x] Reliquary recipe and teaching gate at Degree 3.
- [x] Full-capacity Alchemist referral and Mnemonist lesson.
- [x] Woven Vessel and placed-block fallback lessons.
- [x] Distributor costs remain Degree 5, 100 blood, 25 XP, slot-capacity limited.
- [x] Correct stale Degree 4 atlas/reference text.
- [x] Replace manipulation-only wording and show expression/section metadata.

Acceptance: both expressions equip, save, preview, apply, and restore selection
through the same systems with no persistent-key changes.

## Work Group 6 — Recipe Knowledge and Skip-Path Fallbacks

- [x] Body Answers briefing awards supplies and the Sanguine Fists flask formula.
- [x] Drinking Sanguine Fists completes it and awards the remaining flask,
  cured-jug, and all jug formulas.
- [x] Woven Vessel awards the complete catalogue when missing and teaches the
  Reliquary without learning a Thelemic Memory or granting reserve.
- [x] Keep shared Alembics mechanically usable; recipe knowledge controls
  teaching and visibility, not per-player machine authorization.

Acceptance: fresh tincture, skipped-tincture, existing-world, and multiplayer
shared-machine paths all converge without progression dead ends.

## Work Group 7 — Canon, Dialogue, and Epistemic Pathing

- [x] Establish Hematic, Noetic, and Thelemic as the public vocabulary.
- [x] Keep `Mneme keeps. Nous knows. Thelēma acts.` for later specialist teaching.
- [x] Treat tendencies as predictive Harbinger maps, not cosmic elements.
- [x] Add Alchemist empirical and Mnemonist interpretive acknowledgements.
- [x] Implement Conductive Mark dual recognition: Degree 3 + Ductilis record or
  Degree 3 + first weave, converging on one advancement and existing recipe.
- [x] Reserve `Voluntas Sola` and embodied-will inversion for Silent Archon lore.
- [x] Preserve the fungal reinterpretation and complementary progression lanes.

Acceptance: dialogue differs by epistemic route without duplicating the power,
recipe, item, currency, or hard-locking the player into a school.

## Work Group 8 — Player-Facing Language and Documentation

- [x] Replace formal player-facing Muscle Memory wording with Thelemic Memory.
- [x] Classify conventional memories as Noetic where formal classification helps.
- [x] Update ledger, radial, Vascular Status, Reliquary, Distributor, diagnostics,
  advancements, dialogue, Liber, localization, JEI, and reference surfaces.
- [x] Update `LORE_REFERENCE.md` and `HEMOMANCY_REFERENCE.md` to implemented truth.
- [x] Mark old Muscle/Willed dichotomy documents as superseded design history.
- [x] Add source audits preventing formal player-facing regressions while
  allowing compatibility identifiers and historical evidence.

## Verification Record

Focused baseline before this tracker: mixed memory, Thelemic state, dose,
Alembic vessel, distillation consumption, and equip-rule JVM tests passed, but
the initial run used up-to-date Gradle outputs and must be rerun from source.

- [x] Focused JVM tests with `--rerun-tasks` — passed 2026-08-17.
- [x] `gradlew.bat compileJava compileGameTestJava --no-daemon --rerun-tasks` — passed 2026-08-17.
- [ ] `gradlew.bat runGameTestServer --no-daemon` — 79/80 passed; known unrelated
  `defaultfollowmodeacquiresnearbyhostiles` baseline failed.
- [ ] `gradlew.bat alphaCheck --no-daemon` — 1,445/1,446 passed; known unrelated
  `EquippedMorphlingOverlayTest` Lumenlace-bounds baseline failed.
- [ ] Live radial readability and combined grayscale/color/pulse/crack states.
- [ ] Live click/toggle/sneak-arm, vessel/Alembic, mixed Reliquary/Distributor.
- [ ] Old-save, death, relog, tracking, progression, and multiplayer checks.
- [ ] Balance pass for reserve pressure, overexertion risk, emergency survival,
  target volume, mining/travel cadence, and Morphling resonance.

Known unrelated baseline failures must be reported separately. Automated green
checks do not close any live-client or balance gate.
