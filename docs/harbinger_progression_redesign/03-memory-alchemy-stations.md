# Phase 3: memories, alchemy, and station lessons

[Index](../HARBINGER_PROGRESSION_REDESIGN.md) · Depends on Phases 1 and 2 for coherent sample and cylinder lessons.

## Outcome

The player progresses from using a memory, to weaving one, to managing an intentional set, and finally to preserving multiple patterns. Alchemical and equipment inscription work support that sequence without becoming extra promotion gates.

## Read before editing

- [Blank memory recipe](../../src/main/resources/data/hemomancy/recipe/hematic_memory.json) and [first Blood Shot weave](../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_shot.json).
- [Chicken blood profile](../../src/main/resources/data/minecraft/blood_profiles/chicken.json) and VialCentrifugeBlockEntity.
- DialogueEventHandler's Woven Vessel turn-in, HarbingerMnemonistDialogueTrees, MnemonicRecipeKnowledge, MnemonicReliquaryProgression, NoeticDiscoveryProgression.
- AdvancedBrewingDialogue/Progress, ClinicalBloodKnowledge, Thelemic memory handling, Dendritic Distributor menu/packets, SynapticLoadoutSlotHelper.
- [Scriptorium rites](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/ScriptoriumRites.java), ResonantForgeDialogue/Progress/Rules/Tier and station recipes.

## Instructions

1. Make the first directed crude-memory choice a D2 lesson. Preserve D1 loot, already learned crude memories, and earned starter claims. The lesson is about choosing and using a combat/utility memory; Absorption/Projection already exist.
2. At D3, keep the Loom and Reliquary recipes/use available. Teach one complete authored weave before offering a long catalogue of possibilities.
3. Explicitly teach the blank-memory ingredient chain: Neurotic Enzyme from accessible Ductilis samples, Sanguine Formations, and Blood-Stained Stone. Chicken samples allow direct-vial collection and can yield Neurotic Enzyme; teach that a mixed profile may need repeated spins.
4. Preserve the Woven Vessel's existing material bridge: its turn-in checks a blank memory and archive materials, then supplies Bleeding Bulb and Vivacious Enzymes. Do not turn its briefing/reward flag into the promotion proof; the player must actually finish a weave.
5. Keep a fully obtainable D3 route through that first weave without requiring the D4 lantern course, D5 incubation, whale materials, Circus, or Saints.
6. Distinguish the stations in language: Loom weaves memories to learn authored manipulations; Reliquary equips known Noetic/Thelemic memories; Scriptorium inscribes equipment; Forge records/transfers equipment patterns; Distributor stores named loadouts.
7. At D3 explain the current Thelemic preparations and Body Answers work. A basic D2 distillation may already have produced a tincture; recognize it instead of pretending the player has never seen one.
8. Keep Alembic base distillation before promotion, Condenser eligibility at D3 and Athanor at D5. D4 larger-preparation teaching does not invent a fourth tier. Preserve slots, bound-vessel rules, extraction credit, upgrade escrow, and pending reward delivery.
9. Introduce Scriptorium at D3 as an optional project. Its enchanting-table and other material requirements must not block The Woven Vessel. Show the actual current ingredient list.
10. Teach the full Forge cycle at D4 while preserving D3 availability. Offer Precision at D5 and Masterwork at D7 using existing upgrades and progress conditions. D6 adds applied practice, not another tier.
11. At D4 deepen memory selection and preparation rather than re-awarding the first lesson. Keep familiar early recipes accessible.
12. At D5 teach Dendritic Distributor save/apply/rename/overwrite with the actual blood/XP costs and shared Noetic/Thelemic capacity. Keep fixed Absorption/Projection utilities out of saved custom selections and preserve deactivation of omitted running Thelemic memories.
13. Keep Scriptorium tiers D3/D5/D7. Use the actual Rite of the Eightfold Script and Rite of the Monolithic Script names, not a guessed generic Consecrated Scriptorium rite.
14. Preserve in-place station upgrades, contents, orientation, lock cleanup, cancellation/refund policy, persistence, and recipe compatibility.

## Degree and lesson map

| System | Existing access to retain | Directed teaching |
|---|---|---|
| Crude memories | Existing early acquisition | D2 chosen-memory lesson |
| Living Syringe | D2 separation reward | D3 restricted-specimen practice |
| Loom / Reliquary | D3 | D3 first weave/equip, D4 deeper practice |
| Clairaudiograph | D3 clinical referral chain | D3 optional recording |
| Scriptorium | D3 / D5 / D7 | Same stages, optional projects |
| Resonant Forge | D3 / D5 / D7 | D4 full introduction, D5 Precision, D6 practice, D7 Masterwork |
| Mycelial Lantern | D3 | D4 sustained cultivation; early help if discovered |
| Distributor | D5 | D5 |

## Acceptance

- [ ] Fresh D3 crafts a blank and personally finishes the first weave with ordinary accessible materials.
- [ ] A prior crude-memory learner can take the lesson without duplicate rewards or losing knowledge.
- [ ] Thelemic and Noetic memories share the existing capacity/selection rules.
- [ ] Main progression remains independent of Scriptorium, Forge, full clinical catalogue, and advanced brewing completion.
- [ ] Station upgrades preserve inventory, stored blood, relevant patterns, and recovery after interruption/restart.
- [ ] Distributor operations validate server-side and preserve legitimate old patterns.

Extend existing memory acquisition/weaving, ClinicalBloodProgressionGameTests, EnzymaticScriptoriumGameTests, ResonantForgeGameTests, and brewing tests. Verify the D3 ingredient path in a mod-loaded runtime, not just recipe JSON.
