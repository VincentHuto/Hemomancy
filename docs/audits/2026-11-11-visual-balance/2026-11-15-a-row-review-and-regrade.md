# Addendum, 2026-11-15: the A-row review pack and the re-grade of the stale rows

This adds to [manipulation-visuals.md](manipulation-visuals.md); it does not edit it. Built in build 2 package B19
(chunk 09 T7, rulings VX-17, VX-1, PR-7, NQ-09-6). Grades use the audit's scale: A is at the Riposte bar (a bespoke
geometry class with a named timeline, layered custom geometry in the school material, motion with follow-through).

## What was captured

| Pack | How | Where |
| --- | --- | --- |
| The three lashes and the eight rebuilt held forms, first and third person at play distance | `runStrandReviewClient`, new world `StrandReviewB19`, all 50 scenes, 191 stills | `build/strand-client-review/screenshots/`; one still per form on `build/build2/B19-review/strand-held-forms-sheet.png` |
| The same from a second player's view | `tools/review/review-onlooker.ps1` (host and observer, bare java), 32 stills from beside and in front | `build/onlooker-observer/screenshots/`; a sheet on `build/build2/B19-review/onlooker-sheet.png` |
| Every visual form on a stand, one at a time | `runClient -I tools/review/manipulation-visceral-review.init.gradle -PvisceralRoster` | `build/visceral-review-client/screenshots/` |

Not captured: the 43 A rows cast one by one through the casting harness (`tools/review/casting-animation-review.init.gradle`
with `CastingClientAcceptance`). It runs a server and two clients as three Gradle runs at once, and this build allows
one Gradle command at a time. The roster shows every form's look, but not the cast that sends it. The A rows are
still owed a casting-harness sitting (VX-17, PR-7).

The strand stills carry the vanilla movement tutorial toast and the advancement toasts; the strand scene does not hide
the GUI.

## Re-grade of the 18 stale rows

Graded from source and from the strand stills. "Held" is the hold-to-channel form rebuilt on 2026-11-12; "R cast" is
the quick cast, which the rework did not touch.

| Manipulation | Audit said | Now | Why |
| --- | --- | --- | --- |
| blood_lash | A | B | The tap, orb and thrown whip are layered and timed (`LashRules`), but in a still the orb and line read as one thin red stroke and the crack is a small flash. Needs the owner's look in motion. |
| searing_lash (lash half) | A | B | Same lash body in Flammeus; the heat build reads as a hot red streak along the floor. The R cast (`SearingLashGeometry`) stays A. |
| gloam_laceration (lash) | B, batch 9 quick cut | B | Three dark tails with smoke at the mark; faint to others by design. Batch 9 stands for the R cut. |
| blood_needle_fan | A | A (R cast) | Held spray cut. The R cast is untouched. |
| blood_needle_lance (held) | A | C | The hovering volley is a ring of thin barbed needles that barely reads at play distance in the still. No draw-in timeline beyond the count. |
| cruor_javelin (held) | A | B | Three states (drawn, flight with a frost wake, stuck) in layered cruor glass, but the drawn shaft is dark and small against a body. |
| glacial_grasp (held) | B, batch 4 | B | Five cruor fingers closing on a timeline (`GlacialGraspRules.formed`) with frost knuckles; close to the upgrade the audit asked for. Batch 4 keeps only the R cast split. |
| hematic_flare (held) | B, batch 8 | B | A soft cone, a white core and a heat ribbon that reddens; reads as a thin line from the side. Batch 8 stands for the R cast `FLARE`. |
| prismatic_reproof | B, batch 8 | B (R cast) | Held spray cut. NQ-09-2 ruled: reuse the prism art on the R cast. |
| blood_eclipse (held) | B, batch 9 spray | B | The carried dome: twelve turning ribs and a pale rim. Strong and clear in third person. The spray member is dropped (VX-1, 2026-11-14). |
| crimson_flame_conjuration (held) | B, batch 10 plant | B | Painted fire reads boldly as a wall of flame along the aim; the shared `ERUPTION` ring under it is plain. The plant member is dropped. The R cast entry stands. |
| synaptic_jolt (held) | B, batch 11 | C | The live conductors draw only HutosLib bolts, faint in a still. NQ-09-2 ruled a fresh design for it. |
| conductive_mark | B, batch 11 | B (R cast) | Held brand cut. Batch 11 stands. |
| hemorrhage, insatiable_hunger, grave_debt | B, batch 5 Mortem brand | B (R casts) | Held brand cut from all three. The brand member is dropped (VX-1); batch 5 keeps the R casts. |
| ferric_spikes (held) | B with vanilla, batch 1 | B | The steered run raises real Iron Spikes in a line; batch 1 (2026-11-14) swapped their particles. Player-cast spikes skip the standing particles, so only the dissolve shows. |
| tendon_line | C, batch 7 | B | `TendonLineRenderer` draws every tracked line: a three-layer cord with a wound sinew and a rooted clot, slack and taut pulses. The visibility gap is closed. |

Touched after the audit with no known change of look: `ThermalGeometry`, `CellHandStreams` and the Sculpting classes of
Blood Binding, Furnace Veins, Pyretic Forge, Carrion Communion and Eclipse Snuff. Recheck each at the start of its batch.

## Seen from a second player

- Read clearly: the Blood Lash, Hematic Flare's beam (a hard white line), Blood Eclipse's dome, the drawn javelin, the
  Tendon Line haul, and the needle volley seen from the front (a ring of barbed needles round the head).
- Faint: Gloam's lash (by design), Synaptic Jolt's conductors on a creature.
- Not seen: Blood Eclipse's field of dark from outside (`onlooker-30` and `31` show the caster and nothing round them).
- Not in the onlooker scenes: Glacial Grasp's hand, Crimson Flame's painted fire, the Ferric blade run and the Searing
  Lash. They are seen only from the caster's side.

## What changed in the shared tiers on the same day

VX-4 and VX-5 (batch 13) were built in the same package: the Animus and Umbra shaders gained their own focus, slash and
wisp shapes, and the shared tiers seed each cue per cast. The roster stills are the first look at both.

## For the owner

The eight held forms and three lashes are ungraded by the owner. Any upgrade to them is a new decision after this
re-grade (section 5 of `OPEN_QUESTIONS.md`, "VX-1 (held forms)").
