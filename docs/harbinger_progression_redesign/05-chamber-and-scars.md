# Phase 5: staged Chamber practice and the scar chapter

[Index](../HARBINGER_PROGRESSION_REDESIGN.md) · Keep both promotion dependencies intact.

## Outcome

The Chamber gradually becomes intelligible and controllable. The Anchorite remains the D4 teacher whose scar/effigy cycle opens D5. Neither progression path depends on a failed nightly chance roll.

## Read before editing

- [Chamber visit rules](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberVisitRules.java), ChamberVisitService, ChamberVisitMode, ChamberVisitEvents.
- [Seat interaction](../../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/WarpChairBlock.java), WarpChairBlockEntity and its pairing/recovery behavior.
- ChamberOfWillManager, ChamberProgressionRules, safety/boundary rules, Chamber client sync and overlay.
- [Vein-Mason assignments](../../src/main/java/com/vincenthuto/hemomancy/common/mission/cicatrix_anchorite/VeinMasonAssignments.java), VeinMasonScarLesson, Anchorite/Vicar/Mnemonist dialogue, HarbingerAdvancementGranter.
- [Earlier Chamber brief](../CHAMBER_OF_WILL_MNEMONIST_LORE_IMPLEMENTATION_BRIEF.md), as lore context only where consistent with this packet.

## Chamber instructions

1. Preserve D1-D2 spontaneous bed dreams. Current first-dream chances rise 35% to 65% to guaranteed after repeated failures; later dreams use 25%. Do not invent a D5 75% mandatory seat gate.
2. Preserve normal Concentrated Blood sleep completion taking precedence over an unrelated dream on that wake. The D2 promotion must not be swallowed by Chamber travel.
3. Add a D3 guided glimpse begun through an explicit Mnemonist lesson. Default duration is two minutes. It is the first guided understanding of the Chamber, not necessarily the player's first visit.
4. Route the glimpse through the existing visit lifecycle. Use an explicit bounded guided mode or equally clear existing-service extension; do not call ADMIN access, permanently attune the player, generate a second Chamber, or restore items through a separate ad hoc teleport handler.
5. The guided glimpse should be safe and observational, with the same protections against carrying away dream inventory or transferring items that its presentation promises. Store the original return context and preserve real inventory/equipment.
6. Mark the guided lesson completed only after a valid return. An interrupted pre-entry/failed-start attempt must remain retryable. A legitimate early dream/seat visit changes the dialogue's framing, not eligibility to learn.
7. Keep current D3 chair availability. Formal D4 instruction explains direct sneak-use and the completed-sleep route. Current timed chair budgets are 5 minutes at D3, 10 at D4, and 20 at D5; preserve these unless measured playtesting justifies a separate balance adjustment.
8. D5 instruction explains greater stability and the recruited Mnemonist's support. Do not add mandatory recruitment or a new free repeatable guided transport system.
9. D6 Chamber rite remains the independent attunement route. D3 glimpse, D4 seat use, and D5 support never grant the rite's permanent attunement flag.
10. Preserve legitimate old return/chair/attunement advancements and existing constructions. The current Living Covenant checks a general return proof: early returns may already satisfy it. Do not reset them or silently change the chapter to require a new D6-specific return.
11. Check logout, death, dimension transfer, interrupted sleep, missing destination, reload, full inventory, and clone handling. Preserve one private cell per owner and existing safety/return logic. Reject overlapping visits/projections.

## Scar instructions

1. Keep the Masons Respite directive and map at D4. The Anchorite is remote and should not be replaced by an outpost scar vendor.
2. Teach the complete sequence: existing low-tier tendency-appropriate pattern, scar crafting, burning the scar into known memory, preparing the Effigy loadout, and committing that loadout.
3. Keep the actual first Effigy loadout as the Main proof. Dialogue, owning a pattern, or merely crafting a scar cannot substitute.
4. Teach cerebral scars as learned and committed bodily routes, not ordinary equipped scar trinkets. Do not restore the obsolete implantation-pylon acquisition story.
5. Offer continuation at D5 to deepen alignment and loadout choices, without moving the first scar chapter out of D4.
6. Keep later fungal scars and their existing cultivation distinct from ordinary tendency instruction. Do not leak the complete endgame explanation into the first Anchorite visit.

## Acceptance

- [ ] D2 injection/sleep promotes correctly even when early dreams are eligible.
- [ ] D3 guided entry is explicit, bounded, safe, and retryable after failure.
- [ ] Guided return restores exact item state and original context; it does not grant attunement.
- [ ] D3 chair owners retain access; D4 lesson accurately describes both controls.
- [ ] D6 rite works independently of the Mnemonist's presence.
- [ ] Existing early return evidence survives and does not itself complete the Throne/Vigil proofs.
- [ ] Chamber construction survives progression changes, visits, and server restart.
- [ ] A fresh D4 can complete the entire scar cycle and then start the Illuminatus rite.
- [ ] Incorrect owner, missing materials, or interrupted scar operations do not grant proof or lose unique supplies.

Use ChamberVisitRulesTest, Chamber progression/safety tests, VeinMasonAssignmentsTest, and mod-loaded visit/assignment coverage. Require live client inspection of entry, overlay, safe forced return, and both seat controls; unit tests cannot establish that presentation.
