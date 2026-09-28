# Phase 8: documentation reconciliation and integrated acceptance

[Index](../HARBINGER_PROGRESSION_REDESIGN.md) · Run focused checks during each phase, then integrate here.

## Documentation to reconcile

| Surface | Required cleanup |
|---|---|
| HEMOMANCY_REFERENCE.md | Correct degree/teacher tables, Main proofs, actual station gates, staff/syringe ownership vs lesson timing, gourds, kit owner/name, cylinder uses and Chamber modes. |
| LORE_REFERENCE.md | Preserve early mystery, D4 scar apprenticeship, D5 founding responsibility, D6 limits of ordinary teachers, and distinct ending responses. Remove obsolete mechanics presented as current lore. |
| LORE_CONSISTENCY_REVIEW.md | Add a dated adopted decision for distributed teaching. Explain that the Vicar retains core bloodcraft/doctrine and specialists own their practical lessons. Preserve resolved cosmology/ending decisions. |
| Harbinger-Path.md | Replace stale first-rank rites, seven-tendency claims, obsolete scar methods and unimplemented progression promises. Show Main vs optional branches and teaching vs access. |
| Getting-Started.md / Clinical-Blood-Tools.md | D1 collection, D2 processing, correct vial controls, syringe reward/instruction timing, Concentrated Blood sleep, ordinary wax. |
| Clairaudiograph.md / Antecedent-Inquiry.md | New ordinary medium, legacy ancient recordings, optional early inquiry vs D5 formal commission; no invented audio features. |
| Blood-Systems.md / Lore-and-Story.md | Accurate memory/station distinctions, eight tendencies, Chamber vs fungal contexts, degree vs NPC role. |
| Liber pages, dialogue/inquiries, Conduit/JEI/material atlas | Match the same requirements, names, costs, recovery routes and spoiler stages as the server. |
| Earlier Chamber/repair/design plans | Add clear dated supersession pointers where contradictory, preserving historical status/evidence. Do not mark old implementation claims newly verified. |

Only edit pages that exist and apply. MNA compatibility is outside this packet's scope. Do not perform blanket rewrites of unrelated references or erase historic design records.

## Source checks

Run narrow searches for the touched obsolete wording and references. Review matches in context; many legacy registry IDs must remain.

- Vicar's Consecration Kit: display text changes; vicars_consecration_kit ID and old claim compatibility remain.
- Seven tendencies / generic Fungal tendency: normal tendency count is eight.
- First Bloodcraft 5000 ml: current proof is 500 ml absorbed; distinguish vessel capacity and other unrelated costs.
- Concentrated Blood grants D2: target/current promotion is D2 -> D3.
- Somatic Loom or Reliquary first unlocked D4: actual gates remain D3.
- Anchorite first available D5: first scar chapter remains D4.
- Chamber first exists D6: distinguish early dreams/seat practice from D6 attunement.
- Fungal Gardens exclusively a dimension: preserve the explicit Overworld biome.
- Any sapling becomes bloodwood: target supports Dead Bush.
- Ordinary cylinders require ambergris: update all ordinary consumers, not ancient/specialty content.
- Saints, Canon acquisition or Drudges mandatory in alpha: keep deferred.

Check resource JSON, recipe IDs, translation keys, new item model/texture references, assignment serialization, packet field order, and every new Markdown link.

## Focused automated checks

Choose tests based on actual changed behavior. Favor existing fixtures and observable outcomes over new tests that merely search source text. Registry-backed interactions need a mod-loaded server.

The suite catalog is [gradle/game-tests.gradle](../../gradle/game-tests.gradle); [TESTING.md](../TESTING.md) explains launchers and run directories. Verify task availability again at execution time.

Typical compile/resource check:

    ./gradlew.bat compileJava compileGameTestJava processResources

Examples of existing focused commands:

    ./gradlew.bat test --tests '*FirstBloodcraftLedgerProgressTest' --tests '*FirstSeparationLedgerProgressTest' --tests '*FirstSeparationSpinProofTest' --tests '*HarbingerChapterMilestoneTest' --tests '*ClinicalBloodProgressTest'
    ./gradlew.bat runClinicalGameTestServer runBloodInjectionGameTestServer runDistillationGameTestServer
    ./gradlew.bat runScriptoriumGameTestServer runAdvancedBrewingGameTestServer
    ./gradlew.bat runAntecedentGameTestServer

Do not assume the combined suite includes every focused namespace. Current clinical, advanced-brewing, Scriptorium, Antecedent and several encounter suites are opt-in. For a new namespace, update DevTestHooks/catalog/template registration and run verifyGameTestSuites; do not create unregistered tests that never execute.

At integration, run the repository's required progression gate:

    ./gradlew.bat alphaCheck

Run additional focused suites that alphaCheck excludes when this work touches them. Record exact failures and whether the failing code changed. Never convert a partially green run into a full pass claim.

## Runtime acceptance matrix

| Scenario | Required evidence |
|---|---|
| Fresh Main-only player | D0 through D7 without optional catalogue completion, full armor sets, Circus, rare expeditions, Saints, or unavailable teachers. |
| Early scientist | D1 direct-vial samples survive and count at D2; restricted profiles still need syringe; centrifuge and personal extraction proofs work. |
| Out-of-order explorer | Early staff, crude memory, chair, reef/Deep Dark discovery, and station crafting are acknowledged without revoked access or duplicate rewards. |
| D3 memory route | Actual blank memory plus first weave produced before D4 cultivation or D5 incubation; material hints are sufficient without commands. |
| Routine cylinder user | Ordinary audio and basic/Precision Forge work require no ambergris; legacy ancient/master data survive. |
| Chamber learner | Explicit guided visit, direct chair entry, sleeping chair entry, timed return, and independent rite behave as documented. |
| Scar apprentice | D4 scar/effigy sequence is achievable and alone opens the D5 chapter gate. |
| Solo founder | Legitimate bloodline/Fane/recruit route supplies a valid Vigil helper without another human player. |
| Two players | Proof ownership, NPC event validation, distinct Chamber inventories/cells, founder membership, and rite ownership do not cross-credit. |
| Existing save | Old degree/claims, pending rest, completed weave/scars, Fane, Chamber attunement, loadouts, cylinders, and ending flags survive. |
| Recovery | Interrupt, die, log out, restart, fill inventory/output, and retry at each changed state boundary. |
| Endings | From legitimate pre-choice state, separately complete refusal and Apotheosis without cross-granting status/rewards. |

Use disposable test worlds/copies for recovery and endings. Do not delete or overwrite the user's runtime worlds. A fixture may accelerate reaching a late test state; label it as a fixture and separately record the survival acquisition path.

## Live flow review

- Read the first option and next-step text at every rank. The player should know what advances them, what is optional, where to go, and why.
- Check that degree unlocks do not present four equally urgent quest walls. Prefer the Main continuation, one contextual referral, and accessible optional branches.
- Verify cross-NPC referrals acknowledge completed work and shared observations.
- Check that recipe hints show actual catalysts, mediums, costs, floors, required staff, supported containers, and replacement routes.
- Time introductory lessons and trips. Record excessive material grind, repeated empty nights, duplicate travel, or text that reveals more than the player has earned.
- Inspect in-world items, ledger, dialogue, cylinder models and Chamber presentation. Automated source checks cannot approve these views.

## Phase handoff record

For each implementation slice append:

    Phase and date:
    Behavior changed:
    Files changed:
    Save/compatibility handling:
    Tests run and exact result:
    Live scenarios observed:
    Remaining failures or unverified behavior:
    Next phase:

Update the index's implementation status table with this evidence. Keep plan status distinct from implemented status and from live acceptance.

## Completion rule

The cleanup is complete only when the implemented player journey, server gates, saved state, dialogue/ledger/recipes, references, and observed runtime journey agree. If code and automated tests are complete but live checks are unavailable, state that exact limit and leave the live acceptance items open.

## Verification of this documentation packet

This packet was prepared from a read-only gameplay-source audit of the current checkout on 2026-09-28, including its existing uncommitted progression changes. Ten new Markdown files were created. Three narrow .gitignore exceptions make this packet visible to Git despite the existing docs-wide ignore rule. No gameplay code, resources, existing lore/reference pages, or runtime worlds were changed for this request. Current-source checks established the promotion graph, relevant recipe gates, direct-vial path, syringe reward, first-weave ingredient route, Chamber modes, station tiers, kit claim path, and cylinder consumers.

Packet verification passed: 10 Markdown files, 45 local links, eight implementation phases, 22 current recipe gates, all six promotion certifications, and the chicken-profile prerequisite for Neurotic Enzyme. Markdown structure/whitespace checks and the scoped Git whitespace check passed. No Java build, GameTest, fresh-world survival journey, or multiplayer run was performed for this documentation-only request.
