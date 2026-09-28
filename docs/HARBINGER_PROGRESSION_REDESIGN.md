# Harbinger progression cleanup: implementation instructions

Date: 2026-09-28

Status: agreed direction, source-audited implementation packet; gameplay changes are not implemented by this document.

## Purpose and authority

Implement the user's revised progression as a connected course of practice across the Vicar, Alchemist, Artificer, and Mnemonist. Preserve the existing promotion spine. Give each degree one defining achievement; specialist projects should provide depth without becoming four mandatory checklists per degree.

This packet consolidates the user's draft, the Codex/Claude comparison, and the user's acceptance of the corrected combined progression. It supersedes the conflicting recommendations in the pasted analyses for this cleanup. It does not make every statement in older design documents authoritative.

Current code and loaded resources describe what exists. The instructions here describe the target. A documentation-only source review cannot establish survival pacing, natural structure availability, multiplayer behavior, or successful migration. Those have explicit acceptance checks below.

## Read and execute in this order

1. [Player journey and lore contract](harbinger_progression_redesign/00-player-journey.md).
2. [Phase 1: early progression, assignments, and teaching ownership](harbinger_progression_redesign/01-early-progression.md).
3. [Phase 2: ordinary wax and specialty ambergris](harbinger_progression_redesign/02-cylinders.md).
4. [Phase 3: memories, alchemy, and station lessons](harbinger_progression_redesign/03-memory-alchemy-stations.md).
5. [Phase 4: equipment, staff, gourds, and Cardinal rites](harbinger_progression_redesign/04-equipment-and-rites.md).
6. [Phase 5: Chamber visits and scars](harbinger_progression_redesign/05-chamber-and-scars.md).
7. [Phase 6: field research, cultivation, and the Circus](harbinger_progression_redesign/06-research-and-cultivation.md).
8. [Phase 7: founding, covenant mastery, and endings](harbinger_progression_redesign/07-covenant-and-endings.md).
9. [Phase 8: documentation reconciliation and integrated acceptance](harbinger_progression_redesign/08-validation-and-handoff.md).

Work inline in the current checkout. Do not create a worktree, stage unrelated files, commit, or push as a side effect of following this packet. Preserve the existing dirty progression work and the user's comparison document.

## Decisions fixed by this packet

| Area | Target |
|---|---|
| D1 to D2 | First Bloodcraft, returned to the Vicar. Preserve the existing four proofs and Liber OR Hematic Iron completion rule. Teach both routes without making both mandatory. |
| D2 to D3 | Alchemist's claimed First Separation plus personally recovered distillation, then Concentrated Blood injection and completed sleep. |
| D3 to D4 | First deliberate weave: The Woven Vessel. |
| D4 to D5 | First scar/effigy cycle: The Vein-Mason. |
| D5 to D6 | Founded bloodline plus first usable Fane: A Covenant Written in Place. |
| D6 to D7 | Chamber return, bound Covenant Throne, successful Covenant Vigil: The Living Covenant. |
| Living Staff | Teach at D2; preserve the existing D1 recipe gate. |
| Living Syringe | Preserve the existing D2 separation reward. Teach its advanced use and restricted specimens at D3. Do not confiscate or delay existing access. |
| First gourd | Keep Scarlet Vanity and Pallid Vessel at D3; Crimson D4, Ashen D5, Horn D6. |
| Memory stations | Loom and Reliquary D3, deeper practice D4. No player-authored spell editor. |
| Scriptorium / Forge | Keep existing D3/D5/D7 tiers. Introduce the Scriptorium at D3 and fuller Forge work at D4; do not raise existing use gates. |
| Lantern / Morphlings | Lantern remains usable at D3, formal cultivation lesson D4. Morphling handling D4; incubation D5. |
| Chamber | Early D1-D2 bed dreams remain; first guided interpretation D3; fuller seat lesson D4; longer supported practice D5; independent attunement rite D6. No failed-roll requirement for intentional visits. |
| Field journeys | Overworld Fungal Gardens D2; Voyagers D3; Circus D4; formal Deep Dark/Phlegethontic commissions D5; paired Vagrant Mind research D6. All are optional research/specialist branches. |
| D5 identity | Illuminatus of the Crimson Lodge. Vicar is a role whose responsibilities the founder can now exercise. |
| Kit ownership | Artificer teaches and grants the Armature Consecration Kit at D5. Retain the legacy registry ID and claim compatibility. |
| Cylinders | New ordinary Wax Cylinder for routine recording and ordinary Forge patterns; existing Ambergris Cylinder retained for ancient records and master-pattern work. |
| Endings | Silent Archon remains D7; only Apotheosis grants D8. No ending-specific victory reward merely for reaching D7. |

The exact sample threshold and guided-visit duration below are implementation defaults: five samples from at least three common species, and a two-minute guided glimpse. They make the accepted design concrete without adding rank requirements.

## Source-audit findings that constrain implementation

- First Bloodcraft currently records 500 ml absorbed, Sanguine Formation, Venous Stone, and one eligible blood structure. Do not restore the stale 5000-ml carrying requirement.
- First Separation currently rewards a Living Syringe and initialized Vial Rack. D3 syringe instruction is a lesson move, not a new tool gate.
- ClinicalBloodKnowledge and ClinicalBloodProgress currently start lessons at D2. Add narrow D1 collection tracking without exposing microscopy/injection/storage lessons early.
- D1 direct collection already exists in BloodVialItem.onLeftClickEntity. Do not invent a second sampling interaction.
- The first blank Hematic Memory uses Neurotic Enzyme. Chicken profiles contain Animus and Ductilis and allow loose-vial sampling; centrifuging can produce the required enzyme. Multiple spins may be needed.
- The Woven Vessel briefing already supplies Bleeding Bulb and three Vivacious Enzymes after its current turn-in. Preserve that bridge to the first weave; do not require D4 cultivation first.
- The Alembic's current structure includes Nether Bricks. A D5 formal Nether research commission is not the first permission to visit the Nether. Explain ordinary earlier material acquisition honestly.
- The seat already has a direct sneak-use entry at D3 as well as a completed-sleep path. Do not accidentally replace deterministic access with a nightly lottery.
- Overworld Fungal Gardens generation is explicitly registered. The separate fungal dimension is not the D2 survey destination.
- Ordinary cylinders require coordinated item, machine, menu, recipe, inquiry, renderer, and saved-data handling. Changing only the recipe will not work.
- The existing chapter's Chamber-return flag may already be earned by an early visit. Preserve that evidence by default; do not silently require a second return or grant rite attunement from it.

## Operating rules for every phase

1. Re-read the touched current files and their dirty diff before editing. Earlier session summaries can be stale.
2. Extend the existing assignment/state system. Avoid a parallel quest engine, new generalized framework, or duplicate source of degree truth.
3. Keep server authority, active-blood eligibility, Unstained exclusions, range checks, ownership, and one-time reward rules.
4. Make dialogue, the Assignment Ledger, the Liber, recipe visibility, inquiries, and actual server requirements agree.
5. Support players arriving out of order or having completed earlier work. Teaching later must not remove earlier earned access.
6. Include persistence and retry behavior for every new proof/reward. A full inventory, unavailable NPC, interruption, death, or reload must not consume the only path forward.
7. Update the relevant reference/wiki passages with each implementation phase. Do not publish unimplemented target behavior as current.
8. Run the smallest meaningful checks for the slice, then the integrated checks in Phase 8. Report failures and untested surfaces explicitly.

## Dependency audit

| Dependency | Resolution | Required acceptance evidence |
|---|---|---|
| D1 sampling before syringe | Existing loose-vial interaction and eligible common profiles | D1 collection succeeds without syringe; restricted targets reject correctly |
| D1 collection before clinical lessons | Separate collection evidence from D2 clinical eligibility | Collection persists; injection remains locked |
| Sample collection before jars | Vials for blood; early jar demonstration/supply for supported specimens only | Player is never tasked with a capture whose container is only its reward |
| D3 weave before D4 cultivation | D2 centrifuging supplies Neurotic Enzyme; current Mnemonist supplies first-weave catalyst/enzymes | Fresh D3 can craft a blank and personally weave |
| Staff before gourd rites | D2 Artificer lesson, existing D1 recipe | Staff production and first rite possible before Pallid Vessel |
| Scar before D5 | Anchorite remains the D4 Main chapter | First scar and effigy loadout completes before Illuminatus rite |
| Chamber rite before Archon | D6 independent rite; early dreams do not grant attunement | D6 chapter completable without D7 ingredients or another human player |
| D3 recording before whale expedition | Ordinary Wax Cylinder accepted throughout routine machines | Record, replay, construct Forge, capture/apply ordinary pattern without ambergris |
| Main progression vs optional vocations | Explicit assignment classification and server checks | Skip every optional expedition/Circus/full armor set and still reach both ending choices |
| D5 founding vs recruitment | Found covenant first, then recruit and found site according to current rites | No circular need for a follower who requires an already completed late chapter |
| D7 equipment vs ending choice | Existing outcome gates retained and checked against verified ending state | Neither outcome's exclusive rewards leak to undecided Archons |

## Implementation status

The packet itself has been written and source-reviewed. Each implementation phase remains pending.

| Phase | Status | Evidence / remaining work |
|---|---|---|
| 1 | Pending | No code changes made by this packet |
| 2 | Pending | No code changes made by this packet |
| 3 | Pending | No code changes made by this packet |
| 4 | Pending | No code changes made by this packet |
| 5 | Pending | No code changes made by this packet |
| 6 | Pending | No code changes made by this packet |
| 7 | Pending | No code changes made by this packet |
| 8 | Pending | Runtime acceptance follows implementation |

During implementation, update this table with files changed, commands/results, and remaining live checks. Do not mark a phase complete merely because its prose or tests were written.
