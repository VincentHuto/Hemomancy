# Phase 1: early progression and assignment ownership

[Index](../HARBINGER_PROGRESSION_REDESIGN.md) · Depends on the player-journey contract.

## Outcome

A new player meets the specialists through connected practical work. D1 and D2 remain completable without finishing a catalogue, acquiring rare specimens, or undertaking optional vocation projects.

## Read before editing

- [First Bloodcraft](../../src/main/java/com/vincenthuto/hemomancy/common/mission/vicar/FirstBloodcraftAssignment.java) and FirstBloodcraftLedgerProgress in the same directory.
- [First Separation](../../src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/FirstSeparationAssignment.java), its ledger progress, ConcentratedBlood, ClinicalBloodKnowledge, and ClinicalBloodProgress.
- [Chapter milestones](../../src/main/java/com/vincenthuto/hemomancy/common/mission/shared/HarbingerChapterMilestone.java) and HarbingerChapterProgression.
- [Vial interaction](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodVialItem.java).
- DialogueEventHandler, EarlyInitiationDialogue, the four Harbinger dialogue factories, HarbingerAssignmentLedgerItem, its open packet/screen, and current assignment categorization.

## Instructions

1. Preserve D0 attachment and D1 initiation, their supplies, core manipulation grants, and interruption behavior. Do not restore old sanguine_initiation/votary rank recipes from stale documentation.
2. Keep First Bloodcraft's four proofs: 500 ml actually absorbed, formation projected, Venous Stone projected, Liber OR Hematic Iron structure crafted. The Vicar owns promotion; the Artificer owns the Hematic Iron lesson. Referral text must not imply both structures are mandatory.
3. Introduce First Draws as a Side assignment available from active-blood D1. Five successful creature-sampling events must cover at least three common animal species. Use registered profiles/eligible entities, not display-name comparisons. Do not count self-samples, repeated inspection of one vial, injection, or rejected sampling.
4. Grant five empty vials once, with full-inventory delivery and persistent claim protection. Show the ordinary replacement crafting route. Count samples on successful collection; retain the filled vials for D2 work. Do not confiscate the five samples as payment.
5. Track this limited D1 fieldwork without lowering the global D2 clinical lesson or injection gate. A D1 player can collect while microscopy, ordinary injection lessons, and clinical storage lessons remain at their current stages.
6. Introduce supported specimen/flora cataloguing with a concrete container demonstration. Issue two starter Specimen Jars once when the supported jar lesson is introduced; retain later catalogue rewards as additional supplies, avoiding duplicate early grants to already taught players. Do not require unsupported creatures or plants to fit in jars.
7. At D2, recognize usable samples brought from First Draws. The existing markSampleAcquired path currently only records after briefing: update onboarding recognition so old inventory samples do not leave the ledger falsely incomplete. They still must undergo the assigned spin.
8. Preserve the owner/spin UUID proof on separation output. Generic possession of an enzyme, another player's output, or a fabricated client action must not satisfy it.
9. Keep the separation claim's Living Syringe and initialized Vial Rack. D3 provides advanced instruction, not first legal ownership.
10. Make the First Distillation step explicit in the ledger before Concentrated Blood and sleep. The current FirstSeparationLedgerProgress does not expose a distinct distillation field; reconcile its screen/packet with the actual two-part gate rather than merely changing a sentence.
11. Keep personal Alembic output recovery as evidence. Claiming separation alone, operating a hopper, or just opening the machine must not promote.
12. Preserve special Concentrated Blood direct injection, pending-rest persistence, replacement when eligible, and promotion only after completed sleep. Do not require a Living Syringe for self-injection or an optional ordinary-injection course.
13. Teach D2 bloodwood using Dead Bush only. Put the source and clearance/blood requirements in the recipe hint or lesson.

## Classification

| Activity | Classification |
|---|---|
| First Bloodcraft | Main |
| First Draws | Side |
| Separation and distillation sequence | Main; one rank certification, with both prerequisites visible |
| Red Taxonomy, Living Bestiary, enzyme records | Catalogue |
| Full armor sets, storage equipment, recording | Vocation |
| Fungal Gardens survey | Side |

Use the existing ledger's categories and progress serialization. Do not create a separate quest journal.

## Migration and cleanup

- Existing D2+ saves retain their First Bloodcraft certification. Preserve the current migration rather than demanding a second structure.
- Honor existing separation claims, owned syringe/rack, pending Concentrated Blood rest, and completed distillation.
- Keep relevant lesson/reward keys stable. Add only the state needed for First Draws and any missing ledger proof.
- Replace contradictory numbers and promotion instructions across dialogue, hints, ledger, Liber entries, and wiki in the same slice.

## Acceptance

- [ ] Fresh D1 completes either permitted structure route and advances exactly once.
- [ ] Five successful samples from three eligible species complete First Draws; five copies/inspections of one vial do not.
- [ ] D1 sampling works with an empty vial, while syringe-only targets and ordinary injection remain correctly restricted.
- [ ] D1 samples are recognized at D2 briefing and usable in the assigned centrifuge.
- [ ] Wrong-player/wrong-spin enzymes fail; assigned personally recovered output succeeds.
- [ ] Separation alone and distillation alone cannot advance D2.
- [ ] Completed sleep after eligible injection advances D3; interrupted sleep does not.
- [ ] Death/relog/restart and full inventories do not lose proof or duplicate supplies.

Extend FirstBloodcraftLedgerProgressTest, FirstSeparationLedgerProgressTest, FirstSeparationSpinProofTest, ClinicalBloodProgressTest, ClinicalBloodProgressionGameTests, BloodInjectionGameTests, and DistillationExtractionGameTests as appropriate. Verify behavior in registered server tests rather than relying only on source-text assertions.
