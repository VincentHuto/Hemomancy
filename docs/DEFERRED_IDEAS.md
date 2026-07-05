# Deferred Ideas & Pending Decisions Registry

> **Last updated:** 2026-07-04
> **Purpose:** One place for (A) ideas raised during the power-systems design cycle that have **no owning spec or plan yet**, and (B) **decisions someone must make**, with their default-if-unanswered. Referenced by [POWER_SYSTEMS_AUDIT.md](POWER_SYSTEMS_AUDIT.md) and the plans under `docs/superpowers/plans/`. When an item graduates to a spec/plan, move the link here and mark it graduated; when a decision is made, record the outcome inline — do not delete rows.

## A. Orphaned ideas (no owning spec/plan)

| ID | Idea | Origin | Activation trigger |
|---|---|---|---|
| D-01 | **Magister rank = `CHARGED` casts.** Fill the empty Magister manipulation rank with held, blood-channeled greater forms of known Summa manipulations, unlocked by the deferred "forced manipulation rank-up rituals" — solves the empty rank and the unused `CHARGED` enum together. | [BLOOD_MANIPULATION_EXAMINATION.md](BLOOD_MANIPULATION_EXAMINATION.md) §7; audit §7 Phase 4 | When degree 5–6 content wants a spell-tier payoff; needs its own spec |
| D-02 | **`ManipLevel` payoff curve.** Manipulations already track use-levels with minimal effect; give leveling a visible curve (efficiency/potency per level, shown in the detail panel) — "memories deepen as they are lived." | Examination §7.4 | Cheap win any time; pairs naturally with D-01 |
| D-03 | **Triad tendency Resonance.** One capped efficiency bonus (cost/CD only, ~20–25% max) when worn armor set + equipped morphling + cast manipulation share a tendency. Replaces per-system buff stacking. | Audit §6.2 / Phase 4 | After the morphling reframe lands (needs the one-strain-per-tendency roster) |
| D-04 | **The communal-pool "Return."** Endgame handshakes with the bloodline pool: morphling drinks from the covenant pool when the host runs dry, armor tithes overkill in, manipulations overdraw inside a fane. | Audit §6.3 / Phase 4 | After guardrails' `BorrowedBloodReserve` and the content plans stabilize |
| D-06 | **Sent-Will player-mirroring silhouette.** Proctors visually echo the target player's equipment/build, not just counter it mechanically. | [Wills plan](superpowers/plans/2026-07-03-rogue-hemomancer-wills.md) Task 4 (deferred stretch) | Art/render capacity after Wills Phase A–B ship |
| D-09 | **Claimed Will decay.** Should a Commandeered Will slowly unravel even with upkeep paid (impermanence as theme)? | Wills spec §14 Q7 | Decide after the first bend-layer balance pass |
| D-10 | **Ripeness gating.** `hiveAttention` / Sent-Will kill tracking currently records and gates nothing; could foreshadow or soft-gate Apotheos. | Wills spec §9 / plan Task 8 | Only if Apotheos pacing needs another dial — deliberately inert until then |
| D-11 | **Fungal-scar folk-name fallback.** If the Latin-only scar register ever reads as clutter in playtests, apply the morphling treatment: folk-name primary, binomial subtitle. Current position: Latin-only is load-bearing contrast — do not do this preemptively. | Naming-register conversation; [morphling spec](superpowers/specs/2026-07-02-morphling-fungal-strain-reframe-design.md) §5 | Playtest feedback that scar names blur |
| D-12 | **Morphling final art pass.** The reframe ships with reuse/retint placeholder attachment models flagged "interim" per the asset triage. | [Morphling plan](superpowers/plans/2026-07-03-morphling-fungal-strain-reframe.md) Task 7 | Art capacity after the reskin lands |
| D-13 | **Oculiflora extended reveal sets.** Saint chambers and spore-vein reveals deferred until those features have client-known positions. | [Scar plan](superpowers/plans/2026-07-03-fungal-scar-consolidation.md) Task 6 Step 1 | When structure positions are synced client-side for any other reason |
| D-14 | **Faded Memory recipes.** The `faded_memory` drop registers as a loom catalyst *candidate* with no recipes; authoring its memory-weaving recipes (and possibly a scar-catalyst role) is open content space. | Wills plan Task 8 Step 2 | First content pass after Wills ship |

## B. Pending decisions (ledger — record outcomes here)

| ID | Decision | Owner doc | Default if unanswered | Status |
|---|---|---|---|---|
| D-05 | **Oculiflora "sight → tap":** may the wearer drain a blood/enzyme trickle from revealed fungal terrain? | Scar spec §8; scar plan Task 6 Step 4 | Spec leans **yes**, as a follow-up task after the reveal pass ships clean | OPEN |
| D-08 | **Wills lore canonization:** fold the two Will origins, the Blood Drunk Puppeteer retcon, and commandeering into `LORE_REFERENCE.md` as canon? | Wills plan Task 12 Step 3 | **Yes by default** — the plan canonizes at closeout unless countermanded before Task 12 runs | OPEN (defaults yes) |
| D-15 | **Circulation bandwidth visibility:** invisible cap + debug command, or a player-facing flow meter on the blood HUD? | Audit §8 Q1; [guardrails plan](superpowers/plans/2026-07-03-audit-phase1-guardrails.md) Task 1 Step 4 | Decided 2026-07-04: visible in the Scrying Podium Blood Flow diagnostics as positive/negative/net mL/t plus used/cap/available bandwidth and requested/applied source rows. | DECIDED |
| D-16 | **Hunger scope:** does morphling hunger apply to wild-bound (Developing-capped) morphlings or only Mature+? | Morphling spec §11; plan Task 8 | Mature+ only (gentler early game); hunger flag itself ships **default off** | DECIDED |
| D-17 | **Hunger flag flip:** who runs the balance pass that flips `hungerEnabled` to true? | Morphling plan Task 8 Step 1 | Stays off until a named balance pass; do not flip silently | OPEN |
| D-18 | **Cradle power-budget classification:** second morphling "slot" or covenant infrastructure outside the budget? | Audit §8 Q2 | Infrastructure (it already pays staged blood upkeep) | OPEN (defaults infra) |
| D-19 | **Saint Canon memories in Resonance (D-03):** participate or stay outside? | Audit §8 Q3 | Outside — imprinted, not aligned (they already ignore Dynamic Use) | OPEN (defaults outside) |

## C. Build order (for orientation — details in each plan)

1. [Guardrails mini-plan](superpowers/plans/2026-07-03-audit-phase1-guardrails.md) — shared helpers; erases the content plans' TODO seams.
2. [Fungal Scar Consolidation](superpowers/plans/2026-07-03-fungal-scar-consolidation.md) — landed as the eight-scar roster pass; Oculiflora remains reveal-only and Sanguiflora has moved out of fungal scars ahead of morphling work.
3. [Morphling Fungal-Strain Reframe](superpowers/plans/2026-07-03-morphling-fungal-strain-reframe.md) — **land before public-alpha saves exist** (item renames = migration surface grows with every tester world).
4. [Rogue Hemomancer Wills](superpowers/plans/2026-07-03-rogue-hemomancer-wills.md) — largest/new-system risk; combat now uses the Will entity-cast path while Drudge casting remains isolated to Drudges.
