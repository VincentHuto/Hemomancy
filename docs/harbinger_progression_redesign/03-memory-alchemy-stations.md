# Phase 3: memories, alchemy, and station lessons

[Index](HARBINGER_PROGRESSION_REDESIGN.md) · Depends on Phases 1 and 2 for coherent sample and cylinder lessons.

## Outcome

2026-10-01 Distributor Apply follow-up: Stopping the final running Thelemic memory now sends its changed state immediately through existing tracking-and-self synchronization; periodic active-reserve sync cannot correct that case. A registered-player packet regression failed before correction and passed with retained knowledge/reserves, no Apply cost and no redundant repeat update. Loaded NBT/copy-path coverage also retains legacy Noetic/mixed pattern names, order, preferred selection and an empty intermediate slot, then Applies both restored patterns. All 99 clinical cases passed. This is not a new native cold restart or second connected-client check; Phase 8 records integrated status and broad limits.

Final validation for that follow-up passed 694 integrated required tests, runtime resource checks and 99 clinical cases. One of 2,407 JVM cases still fails on unchanged sampling policy, so full alpha remains red. The preceding Vulture actor-tick failure did not recur; no Vulture fix is claimed.

2026-10-01 Distributor server-validation follow-up: Save/Overwrite previously checked learned Noetic IDs but accepted any recognized Thelemic key from stale equipped data. Both now filter against the actual player's knowledge before costs or storage mutation. The loaded Save regression failed before correction; the first green run passed 96 clinical cases. Expanded packet-handler coverage passed all 97 clinical cases, rejecting remote Save/Rename/Apply, unknown Noetic/Thelemic Apply and over-capacity Apply without changing the saved pattern, equipment, selection, active Thelemic memory, reserve or costs. Normal learned Thelemic Save still pays 100 mL and 25 raw XP without consuming reserve. These are supplied-state server fixtures, not real multiplayer or natural progression acceptance. Phase 8 records the integrated failures and replay; broad acceptance remains open.

2026-10-01 instruction readability follow-up: Eleven dense station/scar pages opt out of the existing over-text decal layer without changing the theme or authored instructions. The codec regression failed before correction; 21 focused JVM cases then passed. Native Scriptorium first and last spreads are unobscured at 1280x900 in the retained disposable review, superseding the stain-overlap issue below. Final alpha passed 694 required runtime tests and resource verification; one of 2,407 JVM cases retains the unchanged sampling-policy failure. Other pages/scales and broad acceptance remain open; Phase 8 records evidence.

2026-10-01 native Scriptorium follow-up: The teacher choice earned the previously missing Liber page with only `DIALOGUE` source; cold reload retained access. The supplied teacher/existing D6 review rank are assisted, not natural D3 arrival. Native review caught optional teaching moving into Quest Work because of `lesson` in its new event ID. Existing taught-event naming restored Lore, with a captured red regression, 96 passing dialogue JVM cases, 94 passing clinical tests and corrected native Lore navigation. Final integrated runtime/resource checks passed 694 required tests; full JVM status remains red on one sampling-policy case among 2,406. Four leaves fit, but first-spread stain overlap remains a visual acceptance issue. Phase 8 records captures and limits.

2026-10-01 operation-page discovery: The existing D3 Mnemonist Scriptorium choice now unlocks its registered Liber construction/operation page through validated server dialogue. No station ownership, enchantment, kit claim or Main proof is required. Refinement/compounding and binding/refilling pages accompany existing Condenser/Athanor personal eligibility, with the same shared sync/login repair as their upgrade lessons. Loaded regressions failed before wiring; the focused green run passed 112 JVM cases, 94 clinical GameTests and 35 station GameTests. Previously visited Scriptorium conversations can be repeated; no old visit is inferred from rank. Native page presentation and natural acquisition remain open; Phase 8 records integrated evidence.

2026-10-01 atlas follow-up: Blood-Stained Stone now explains its repeatable Venous Stone, Sanguine Formation and redstone craft, with four stones required for a blank. Neurotic Enzyme explains chicken centrifuging, possible Vivacious output, and its impulse/motion identity. The focused source-text regression passed after a captured failure, Java compilation passed, and all 86 clinical GameTests passed. These checks do not establish natural acquisition/pacing or rendered atlas layout. Recipes and gates are unchanged.

The player progresses from using a memory, to weaving one, to managing an intentional set, and finally to preserving multiple patterns. Alchemical and equipment inscription work support that sequence without becoming extra promotion gates.

## Read before editing

- [Blank memory recipe](../../src/main/resources/data/hemomancy/recipe/hematic_memory.json) and [first Blood Shot weave](../../src/main/resources/data/hemomancy/recipe/memory_weaving/memory_blood_shot.json).
- [Chicken blood profile](../../src/main/resources/data/minecraft/blood_profiles/chicken.json) and VialCentrifugeBlockEntity.
- DialogueEventHandler's Woven Vessel turn-in, HarbingerMnemonistDialogueTrees, MnemonicRecipeKnowledge, MnemonicReliquaryProgression, NoeticDiscoveryProgression.
- AdvancedBrewingDialogue/Progress, ClinicalBloodKnowledge, Thelemic memory handling, Dendritic Distributor menu/packets, SynapticLoadoutSlotHelper.
- [Shared station-upgrade contract](../STATION_UPGRADES.md), [StationUpgradeRites](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/StationUpgradeRites.java), StationUpgradeCatalog/Progress/Rules, machine operation rules and station recipes. The former station-specific rite handlers are removed.

## Instructions

1. Make the first directed crude-memory choice a D2 lesson. Preserve D1 loot, already learned crude memories, and earned starter claims. The lesson is about choosing and using a combat/utility memory; Absorption/Projection already exist.
2. At D3, keep the Loom and Reliquary recipes/use available. Teach one complete authored weave before offering a long catalogue of possibilities.
3. Explicitly teach the blank-memory ingredient chain: Neurotic Enzyme from accessible Ductilis samples, Sanguine Formations, and Blood-Stained Stone. Chicken samples allow direct-vial collection and can yield Neurotic Enzyme; teach that a mixed profile may need repeated spins.
4. Preserve the Woven Vessel's existing material bridge: its turn-in checks a blank memory and archive materials, then supplies Bleeding Bulb and Vivacious Enzymes. Do not turn its briefing/reward flag into the promotion proof; the player must actually finish a weave.
5. Keep a fully obtainable D3 route through that first weave without requiring the D4 lantern course, D5 incubation, whale materials, Circus, or Saints.
6. Distinguish the stations in language: Loom weaves memories to learn authored manipulations; Reliquary equips known Noetic/Thelemic memories; Scriptorium inscribes equipment; Forge records/transfers equipment patterns; Distributor stores named loadouts.
7. At D3 explain the current Thelemic preparations and Body Answers work. A basic D2 distillation may already have produced a tincture; recognize it instead of pretending the player has never seen one.
8. Keep Alembic base distillation before promotion, Condenser eligibility at D4 and Athanor at D6. D4 larger-preparation teaching does not invent a fourth tier. Preserve slots, bound-vessel rules, extraction credit, upgrade escrow, and pending reward delivery.
9. Introduce Scriptorium at D3 as an optional project. Its enchanting-table and other material requirements must not block The Woven Vessel. Show the actual current ingredient list.
10. Teach the full Forge cycle at D4 while preserving D3 availability. Offer Precision at D5 and Masterwork at D7 using existing upgrades and progress conditions. D6 adds applied practice, not another tier.
11. At D4 deepen memory selection and preparation rather than re-awarding the first lesson. Keep familiar early recipes accessible.
12. At D5 teach Dendritic Distributor save/apply/rename/overwrite with the actual blood/XP costs and shared Noetic/Thelemic capacity. Keep fixed Absorption/Projection utilities out of saved custom selections and preserve deactivation of omitted running Thelemic memories.
13. Keep Scriptorium tiers D3/D5/D7. Use the actual Rite of the Eightfold Script and Rite of the Palimpsest (formerly the Monolithic Script) names, not a guessed generic Consecrated Scriptorium rite.
14. Follow the shared station-upgrade contract: preserve in-place state, lock cleanup, exact escrow/Staff recovery (not spent blood), persistence, and recipe compatibility. Prior-tier eligibility means personal practice and degree, not accepting an earlier gift. Crafted/gifted kits work but never bypass server eligibility; crafting remains available and warns about unclaimed free kits. Athanor adds the second catalyst slot, not a third.

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

## 2026-09-30 shared activation follow-up

A live check found that Thelemic use depended on the last Noetic input type. Selecting Sanguine Marionette, then applying a pattern whose selected entry was Laboring Arms, left Laboring Arms inactive after two ordinary use-key attempts despite available reserve. Selecting quick Blood Absorption before applying the same pattern allowed it to activate. The client now routes input from the shared memory reference: Thelemic entries toggle once on press, and selection changes reset held input rather than retaining the previous Noetic's charge or channel mode.

Server validation now rejects a stale Noetic channel start while Thelemic is selected, stops an existing channel on that selection change, and cancels or rejects the previous Noetic charge pose. Positive charge previews also require Noetic selection. Selection interruption sends zero-duration world-preview cleanup before pose cleanup; canonical charge-form/source/bounds checks still apply, but unequipping or losing activation eligibility does not block cancellation. The review's masked preview rejection test was strengthened to keep the prior Noetic equipped, blood active, and the refresh throttle cleared.

The three stale-channel/pose regressions failed before the fixes; preview cleanup failed separately before its repair. The final focused run passed 31 JVM tests and all 50 clinical GameTests, including existing previews for all 16 registered charged powers. Protected native replays with continuous Sanguine Marionette and charged Hematic Mortar retained as the legacy Noetic selection both toggled Laboring Arms on once during a 45-tick hold, then off on a fresh press, with zero charge and no channel. The charged case was also repeated after cold restart. These assisted checks do not establish the full input/selection matrix or complete Phase 3. Setup interruptions, synchronization timing, and remaining limits are recorded in Phase 8.

## 2026-09-30 shared selection and Distributor follow-up

The disposable first-weave world now has a live shared-capacity check. At D3, drinking supplied Sanguine Fists, Coursing Legs, and Predatory Eyes tinctures filled the four shared slots alongside the earned Blood Shot. Enduring Viscera was learned and prepared without exceeding capacity; its drag into the full Reliquary was rejected. Removing Blood Shot through the screen allowed the Thelemic drag equip. An assisted D5 increase allowed Blood Shot back into the fifth slot, and Laboring Arms replaced the occupied Arms selection without increasing the count.

A Distributor was crafted with normal Projection from a supplied formation and placed normally. Save rejected zero XP, then saved a five-memory mixed pattern after XP was supplied. The pattern excluded all three fixed utilities. Rename and Apply did not spend XP. The Noetic-only second pattern removed a running Laboring Arms when applied, retaining its reserve. Overwrite first showed Confirm without spending or changing the old pattern, then saved the mixed selection for 25 raw XP. Low-blood overwrite and D4 Apply were rejected. Cold reloads retained the original named patterns, then the overwritten pattern, preferred Thelemic selection, remaining reserve, and XP.

These checks used normal screen clicks, drag, text entry, and mapped-key input through the opt-in client driver. Its new source contract failed before those operations existed; the final focused JVM run passed 28 tests. The driver snapshots read server memory/loadout state without injecting it. Rank, tinctures, XP, blood, formation blocks, relocation, and dropped-item recovery were assisted. Two setup-related drowning deaths interrupted the craft replay; the actual crafted item was recovered, not replaced, and only the later completed interactions count. No production gameplay rule changed. Natural acquisition, broader capacity/unknown-memory/distance checks, the full Noetic expression matrix, multiplayer, and the integrated gate remain open; Phase 3 is not complete.

## 2026-09-30 first-weave follow-up

A disposable D3 client collected two ordinary chicken samples, spun them in opposed Centrifuge slots with the normal timer, and recovered Neurotic Enzyme. The player crafted four Blood Stained Stones, then a blank Hematic Memory using that recovered enzyme. The Mnemonist's rendered turn-in consumed the archive materials, retained the blank, and supplied one Bleeding Bulb and three Vivacious Enzymes. A personally crafted Loom accepted those inputs; empty-hand selection chose Blood Shot, normal Blood Projection paid its 50 mL, and a held Living Staff captured and returned the drifting strand. The weave produced one Blood Shot memory, spent two enzymes, and retained one. Both the first-weave and finished-vessel advancements, the memory item, and the Loom's remaining enzyme survived a full client shutdown and reopen.

The run exposed a presentation mismatch: the Main blank-memory lesson was under Conversation / Speak Freely. `DialogueHubFactory` now presents Woven Vessel in Quest Work with NOTICE, retaining the recipe-teaching choice and original turn-in node. Indexed vessels do not reoffer the material bridge; general Loom teaching remains in Lore. The regression first failed because the Quest Work topic was absent. The dialogue package then passed 61 JVM tests and the clinical server passed all 43 required GameTests. A read-only review found no issues. A separate fresh D3 client replay opened the corrected topic, reached the original lesson through its normal choice, and learned the blank recipe.

This is an assisted route check, not natural-survival acceptance: rank, active/full blood, ordinary ingredients, a Living Staff and projection item, stationary entities, a safe platform, formation blocks, and player relocations were supplied. Centrifuge and Loom personal-crafting access gates were retained; both machines were crafted through normal Blood Projection before use. The strand drifted normally and its return used normal held-Staff input, with an assisted relocation to match the captured distance. No Lantern, Incubator, ambergris, Circus, Saints, or prepared Neurotic Enzyme was supplied. Natural material acquisition, fair physical movement, multiplayer, interrupted-weave cold reload, wider station/loadout acceptance, and the integrated gate remain open. Full action/results and runtime artifacts are recorded in Phase 8.

## 2026-09-30 interrupted weave and learned-memory follow-up

The same disposable D3 world now has two mid-weave cold-restart checks. A second Blood Shot weave retained its 50 mL payment, blank, bulb, selected recipe, 21 stored enzymes, and unfinished strand after shutdown. Normal held-Staff input completed it after reopening, spending one enzyme without another Projection payment. A third weave spent two enzymes while its strand was held, then stopped unfinished. Reopening retained the paid state and 18 remaining enzymes. Explicit empty-hand use showed the Staff instructions; crouch-use cancelled, and two further crouch-uses recovered the bulb and blank without refunding the spent enzymes.

That replay exposed a chat flood: continuing to hold Projection after payment repeated the full Staff instructions every tick. `SomaticLoomBlockEntity.tryChargeRitualBlood` now quietly rejects further charge attempts only for a valid paid weave. Missing or changed recipes still warn, and explicit help remains available. The new regression failed on repeated feedback before the fix; `runClinicalGameTestServer --console=plain` then passed 44/44. A fixed client held Projection for three seconds without repeating the lesson. A read-only scoped review found no issues.

The original first-weave memory was learned through normal offering, Projection lighting, and Blood Absorption at an Iron Brazier. A personally projected, normally placed Reliquary then showed Blood Shot equipped at 1/4 slots. Ordinary Mnemonist interaction recognized the first weave and awarded the Conductive Mark recipe. The wiki now distinguishes full-memory brazier learning from direct-use crude shards. Additional blanks, bulbs, enzymes, an Absorption item, Reliquary formation blocks, and relocations were supplied for these checks. They do not establish natural acquisition, fair movement, the full shared Noetic/Thelemic selection matrix, Distributor operations, multiplayer, wider station recovery, or a green integrated gate. Phase 3 remains in progress.
