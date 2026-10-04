# Hemomancy testing

## Priority repair verification (2026-10-02)

The latest complete `alphaCheck build` gate passed 2,423 JVM tests (zero failures/errors/skips), all 705 required GameTests and resource/package checks (`build/priority-loom-preview-complete-gate.log`, exit 0), including the NPC, ordeal, activation, Communion and read-only Loom preview helpers below. A fresh minimal packaged client/server replay of Hemomancy SHA-256 `6FC3AAB7323E25E14DDD6C9C10757A29E690EEDCCE65A3C6FA80D6856308883E`, HutosLib 7.4.0 and TerraBlender 4.1.0.8 passed connected render/recipe setup, saving all dimensions and two normal exits. Explicit UNKNOWN category registration removed 61 reproduced machine-recipe warnings without changing effective categories; two unreferenced HutosLib backup PNGs are excluded from packaging and unused Ray uniform metadata is removed. Immutable archive checks preserve all other resources, recipes and shader code. Evidence: `build/priority-minimal-corrected-accepted.log`; red checks and configuration assists are recorded in the repair record. Visual/reload/locale and natural gameplay acceptance remain separate.


A fresh D0 Normal Survival journey has begun in seed-42 `PriorityNaturalSurvival_20261003`, using no gameplay commands or preparation helper. Native mining/walking collected three logs, then normal inventory/table clicks crafted twelve planks, a table, sticks and wooden pickaxe/axe. Saved Minecraft statistics and final inventory agree; rank stayed D0/NONE and the client exited normally. The modpack's automatic probe note was the only initial item, and the driver's fresh-world settings enable cheats although none were used. Evidence: `build/priority-natural-survival-acquisition-accepted.log`. This is an acquisition baseline for continuing the natural journey, not promotion, discovery, combat or full progression acceptance. No source changed.

An earned-Spine first Gardens visit passed from a copy of the accepted nine-husk Communion save. Ordinary use charged 500 mL; the normal 2,400-tick projection returned to the original position and queued the unresolved revelation choice. Normal whisper acknowledgements reached the retained decision; native selection set Apotheos pending while retaining D7. Both unresolved and selected choice states survived cold two-client reconnects, with exact saved inventories and owner/member counters preserved and six normal exits. Evidence: `build/priority-gardens-spine-native-accepted.log`. The clone changed only its save name; D7/Bloom acquisition was supplied earlier, and completed endings/later visits/fair combat remain open. No source changed, so the latest full gate above remains applicable.

A corrected fresh two-player Vigil ordeal passed ordinary sealing, all three actual threat waves, native sword/Projection maintenance, both offering consumptions and production completion. Both survivors received effects; only the caster earned Vigil proof and one enzyme. Exact inventories/effects/proof persisted through four normal client exits and a cold two-client reconnect. `build/priority-vigil-ordeal-r3-accepted.log` and `build/priority-vigil-ordeal-r3-station-verified.log` verify saved NBT, recorded waves, intact authored floor and emptied braziers. Rank/blood/floor/offerings/anchors/equipment were supplied; natural acquisition remains open; ordinary staff activation/planting are checked separately below. Prior death and missing-captured-floor attempts are explicitly excluded. The complete `alphaCheck build` gate passed after this test-only fixture correction (`build/priority-vigil-ordeal-r3-complete-gate.log`, exit 0).


Ordinary Living Staff activation/planting and cancellation passed in a separate fresh two-client world. The supplied station began inactive; D0 staff use was refused, while D6 use escrowed exact tagged components and captured the floor/offerings. Two ordinary cancellations returned one staff each, including after a real 50 mL anchor commitment, normal active-rite save and cold reopen. Saved inventories, empty active-rite list, retained offerings/intact floor and absence of stray staff drops passed `build/priority-vigil-activation-accepted.log` and `build/priority-vigil-activation-station-verified.log`; four clients exited normally. Rank/blood/floor/offerings/items were supplied. Full consecration and natural acquisition remain separate; the complete alpha/build gate passed after the new test-only helper (`build/priority-vigil-activation-complete-gate.log`, exit 0).


An actual Creative client in fresh `PriorityLoomReload_20261003` passed ordinary Loom placement, memory/catalyst insertion and output selection, sneak-use catalyst removal with retained memory/no preview, and reinsertion with restored preview. A disposable false-condition overlay removed Blood Shot through normal `/reload`; both current recipe managers and visible result cleared while exact inputs/selection remained. Normal data-pack disable restored both automatically. Four original native captures were inspected at 854�480. `build/priority-loom-preview-native-accepted.log` verifies paired readbacks, unchanged inputs, unpaid/uncommitted state, saved Loom/disabled pack, ordinary inputs and normal client exit 0. The initial controller's incorrect pack-discovery assumption is preserved and excluded; the corrected continuation used the same unchanged world. The helper only reads the exact disposable world. No paid weave, Survival mastery/acquisition, remote multiplayer or broader locale/display acceptance is claimed. The fresh full gate above includes this helper; production jar bytes are unchanged.

A fresh two-client `PriorityCommunion_20261003` case passed all nine unaccelerated production ripenings and ordinary ordered picking/eating, owner-only Communion, full-main-inventory Spine deferral after offhand eating, cold counter/queue/exact inventory persistence, and exactly-one production delivery after native Throw freed a slot. `build/priority-communion-native-accepted.log` verifies the nine bound-stack checkpoints, saved player/Bloom NBT, peer isolation and four normal participating-client exits. D7, bloodline/blood, terrain/Bloom and filler inventory were supplied; Peaceful/day excluded combat. No ripe husk, tick/RNG acceleration, consumption or reward proof was assigned. The earlier cold observer preconnection stop is excluded with its logs preserved; an unchanged fresh observer passed against the same host. Natural rank/resource acquisition and rite-created Bloom acceptance remain open. See the priority repair record for the controlled setup, request-write race correction and checkpoint evidence.

A subsequent normally activated Vigil completed in one rite: actual staff escrow, twelve ordinary 50 mL anchor payments/600 mL commitment, inscription, ordinary Anchor claim, all required support nodes, all three threat waves, both offering consumptions, production rewards and exact staff return. Four normal client exits and a fresh two-client reconnect preserved inventories/components, effects, caster-only proof and D6/D0. `build/priority-vigil-full-consecration-verified.log`, `build/priority-vigil-full-activation-accepted.log` and `build/priority-vigil-full-activation-station-verified.log` verify recorded payments, actual saved NBT/entity regions and authored station. No setup helper was reapplied; rank/bloodline/floor/offerings, equipment and some preparation positions remain assisted. Natural acquisition/balance remain open. Only evidence/scripts/documentation changed for that Vigil follow-up; its then-current gate was the activation gate. The latest complete refresh is the Loom preview gate above.

Ordinary two-player First Separation Alchemist claiming passed with a full host inventory: exact pending syringe/eight-vial rack stayed out of temporary guided inventory despite newly freed temporary space, original personal inventories restored on normal interruption, the queue survived cold restart, and ordinary THROW of two supplied barrier stacks allowed exact delivery. Guest inventory and lack of quest/reward proof stayed unchanged; all four clients exited 0. Evidence: `build/priority-npc-reward-native-accepted.log`. Eligibility/rank, inventories, terrain and teachers were supplied. The guarded preparation helper compiled after the then-current full gate and was exercised natively; production jar bytes did not change in that step. The latest full gate includes it. Other quest rewards and natural completion remain open.

The later `build/priority-chamber-coop-final-gate.log` failed in `threeAnchorHelpersRepairTwoActualRings` (supplied helper alive but missing from the entity index). The fixture awaited the station chunk while its third helper occupied the adjacent chunk. Setup now boundedly awaits each actual helper chunk and UUID index before the unchanged repair/payment assertions. All 50 temporary diagnostic copies passed (755 required tests total); they were removed, and the normal full gate passed again in `build/priority-anchor-helper-readiness-final-gate.log` (exit 0).

A subsequent two-player guided Chamber server-loss check passed against the then-current production jar `F9E225FFC384803D21864CA765192B29C831BFD5FCD9B2A958C04E7B39436359`: both visits were active at a successful real save and abrupt host termination, fresh processes resumed the same personal cells, a second active-pair save succeeded, and normal stops restored exact independent pre-entry typed inventory/equipment. Evidence: `build/priority-chamber-crash-coop-r3-accepted.log`. Eligibility, teachers/platform and inventories were supplied. Test-only baseline/world-guard additions compiled separately after the full gate and were exercised in this run. The separate Harbinger equipment container was empty; populated-container restoration remains untested. Partial-write recovery remains open. The repair record preserves two excluded crash attempts and the timestamp-controller correction.

`alphaCheck build` passed 2,423 JVM tests (zero failures/errors/skips), all 705 required combined GameTests, strict resource-log verification, packaged asset-path/PNG-header/font-license checks and build after the pome ownership/lifecycle-sync and guided Chamber wording corrections (`build/priority-anchor-helper-readiness-final-gate.log`, exit 0; earlier gates remain preserved). Two actual clients reproduced one gesture toggling station consent twice; the handler now ignores the duplicate empty-main-hand offhand fallback while preserving empty-offhand use with an occupied main hand. The loaded regression failed before correction and passed afterward; a fresh native replay toggled consent once in both directions and verified connected effects/proof isolation. Four clients exited normally, and `build/priority-vigil-native-evidence/verify_vigil.py` passed. Supplied D6/inscription and explicit production completion callbacks isolate these checks; natural 1,200-tick ordeal, nine-husk Communion and additional multiplayer recovery modes remain open. A subsequent two-client pome check passed owner-only picking, exact bound Drop refusal and owner-only one-husk eating/progress synchronization. A supplied foreign-owned copy reproduced server refusal with client-predicted inventory loss; its native assertion failed. Ownership refusal now runs on both sides, retaining server-only messaging and progression. Post-fix native refusal retained the exact foreign stack and zero guest progress; the owner completed a second timed consumption. Cold replay exposed an additional unsynchronized client counter. Shared degree lifecycle sync now sends existing pome progress at login, dimension change and respawn. The loaded save/reload lifecycle regression and full gate passed. A fresh two-client Survival replay received owner progress two immediately, kept the guest at zero with the exact foreign stack, and stopped normally. Both native verifiers passed against snapshots and saved personal progress; all eight recorded pome-review client exits were zero. Natural acquisition and nine-husk Communion remain separate. The earlier `priority-vigil-claim-green.log` failed despite its filename and is not accepted evidence. Activation, Vulture, Beacon, Vitric and claimed-Will fixtures now keep their actors/targets inside appropriately sized existing templates. Historical removal callers remain unproven. A 50-case diagnostic run reproduced Obscured's tick-65 failure before melee contact; its unchanged movement/actual-hit requirements now wait within the original 80-tick timeout. All 50 corrected diagnostic melee cases passed; that stress run failed overall on the separately corrected formation footprint collision. Temporary duplicate cases and retry annotation fields were removed before the final normal gate. See the priority repair record for logs and limits. The owner selected the approved migration list: Chitinite requires the Living Syringe; Hemomancy Armadillo, Desiccant, Mortarbound and Naeglerophaeon allow ordinary vials. This supersedes the historical red alpha result below.

The final jar excludes test distillation recipes and GameTest classes. An isolated packaged NeoForge 21.1.219 server containing only Hemomancy, HutosLib 7.4.0 and TerraBlender 4.1.0.8 started, saved all dimensions and stopped normally, including the updated routing artifact. The isolated packaged client also created render atlases and joined that server through loopback. Packaged baking and screenshot-based appearance remain distinct acceptance checks; later resource corrections are recorded below. Exact artifact hashes, repaired findings, fixture failures and outstanding acceptance are in [the repair record](validation/PRIORITY_FINDINGS_2026_10_02.md). Further Chamber recovery and reward cases, packaged client visuals/reload and natural Survival journeys remain open.

A fresh two-player guided Chamber check used ordinary Mnemonist dialogue and Begin input with explicitly supplied D3/active blood and distinct tagged inventories. Both entered separate active areas and moved temporary copies into different personal crafting inputs. Guest client stop restored only that guest; the host remained active with its original snapshot. A fresh guest process rejoined outside, and the host later returned on the normal guided timer. Exact stopped NBT preserves both original inventories/components, D3/NONE and no attunement, without temporary copies. All three clients exited normally; `build/priority-chamber-multiplayer-evidence/verify_chamber.py` passed. This covers connected guest interruption/reconnect, not simultaneous server loss or partial-write recovery. See the repair record for checkpoints, snapshots and limits.

Two actual connected development clients exercised supplied shared-Fane membership and an ordinary held Projection input. The acting member alone earned covenant proof and supplied 1,017 mL; the nearby member remained uncredited. Recorded status and stopped advancement JSON passed the ownership verifier. Both clients stopped normally. A misleading machine-mastery warning led to an additional red/green loaded routing regression and full alpha/build pass. Native replay then verified ordinary Use without that warning, persisted proof for the previously uncredited host, two normal exits and an unchanged dependency jar. See the repair record for fixtures and exact scope.

A resource-only packaged follow-up corrected seven obsolete Forge composite/OBJ loader and parent identifiers. `jar` passed and a fresh packaged client completed model baking and connected without those seven failures. Other resource warnings remain. This step has its own recorded artifact hash and native logs; the subsequent full gate above includes the correction. See the repair record's model-loader evidence.

Naeglerophaeon's custom entity geometry then moved outside vanilla's block-model scan without changing its bytes. The intended red regression became green (three focused resource tests plus `jar`). A packaged client connected without model-load parse failures, and a supplied nearby NoAI boss caused its custom cubes/bone groups to load successfully. Blockstate/texture warnings and visual/combat acceptance remain; exact logs, histograms and latest jar hash are in the repair record.

Two later blockstate tests reproduced missing Engram downward variants and the invisible Abocipher emitter definition. Empty-model fallbacks preserve their existing placement/render rules. Both tests and `jar` passed; all 624 Engram states resolve once. A packaged bake removed the 105 blockstate warnings. Texture/invalid-path warnings remain, and this bake-only run did not repeat connected gameplay. The subsequent full gate above includes these corrections.

Fresh opt-in `PriorityScars_20261002` supplied D6, three Resonance ranks and seven known/active scars. Native Effigy screenshots were inspected at effective GUI scales 1 and 2: all seven occupied sockets fit clear of Prepare. Ordinary Prepare input persisted all seven selected IDs in the stopped station chunk; normal exit was 0 and the evidence verifier passed. Finished-pattern motif/blood preparation and natural acquisition are separate. See the repair record for screenshots, snapshots, saved NBT and exact scope.

Fresh opt-in `PriorityLedger_20261002` supplied canonical ending states. Ordinary ledger-item use transmitted pending Apotheos as incomplete, and completed Apotheos/Silent Archon as complete. All three 854x480 captures were inspected: pending has its rite instruction; both endings show 12/12 and distinct completion descriptions. Server/client readback, screenshots and normal exit 0 passed the verifier. These are supplied-state display checks, not natural ending completion.

Fresh `PriorityChamberCrash_20261002` entered a guided visit through ordinary Mnemonist dialogue, with supplied component-bearing inventory. After temporary crafting movement, a guarded save/flush persisted an active inside-Chamber checkpoint. Abrupt process termination skipped logout. A fresh process resumed the active visit; ordinary stop then restored exact original inventory/components and D3/NONE without temporary copies or attunement. The NBT/state/exit verifier passed. This covers abrupt loss after a complete checkpoint; the later ordinary-bed Dream run is separate. Deferred rewards, multiplayer and partial-write recovery are distinct acceptance cases.

Fresh `PriorityDream_20261002` used supplied D2/inventory/ordinary bed and normal sleep input. First completed sleep recorded one failed attempt; second entered a Dream without setting flags or calling entry directly. Temporary crafting/drop checks retained the copy, then the 2,400-tick timer returned the player automatically. Exact original components/counts/slots and D2/NONE survived normal stop, with no guided completion or attunement. The native state/NBT verifier passed. Natural acquisition, deferred rewards and multiplayer remain separate.

Fresh `PriorityDreamRewards_20261002` entered an actual Dream by ordinary bed sleep, then a guarded fixture supplied First Separation quest proofs and invoked production reward claiming after entry. The syringe and initialized eight-vial rack remained pending despite temporary inventory room. Timer return restored original component-bearing inventory, cleared crafting, and delivered each reward once. Normal stop and cold reopen preserved exact components and an empty queue without duplicates; both exits were 0 and the native verifier passed. This supplies quest eligibility rather than naturally completing or claiming through NPC dialogue. Other reward paths and multiplayer ownership remain separate. The opt-in helper compiled; production code/jar did not change in that reward follow-up. The latest complete gate above includes the helper.

The earlier disposable development-client pass exercised ordinary Mnemonist dialogue entry, temporary cursor/crafting/armor/offhand inventory input, logout return and fresh-process reopening. Stopped NBT retained exact custom item components/counts; client slots matched the entry state and D3 stayed unchanged. Both client launches stopped normally. At that checkpoint, crash recovery, inside-Chamber persisted reconnect and Dream mode were open; the later native cases above address those stated scenarios. Deferred gifts and real multiplayer require their own evidence. The empty seven-socket Effigy rendered without Prepare overlap. A 60-second one-player/25-NoAI-NPC JFR profile found four marker-handler samples among 2,257 runnable server samples; it did not demonstrate a performance defect. See the repair record for assists, scripts and measurement limits.

A later packaged resource correction restored 32 canonical item PNGs byte for byte from their retained authored archive, corrected five texture maps without geometry changes, and appended the Hemorath Rib sprite to the existing Minecraft block atlas while preserving its six prior sources. Explicit packaging exclusions keep raw samples/notes/backups and one corrupt unused archive out of runtime resources, retaining source files and the font license. A fresh minimal bake removed all 47 Hemomancy missing-texture warnings and all 72 logged invalid paths; the packaged gate checks 80 invalid paths found across the jar. Four resource regressions and the final complete gate passed. Immutable artifact/provenance/geometry/license checks passed in `verify_model_textures.py`; review found no remaining actionable issue. Two HutosLib invalid archive paths, font/frame metadata and earlier recipe-category warnings remain separate. This bake had no listening server and does not establish appearance or connected gameplay. See the repair record for failed intermediate checks, final artifact hash and acceptance limits.

The subsequent metadata pass removed two rejected index-4 entries from each four-frame thread animation and replaced the earlier duplicate middle-dot mapping in four Fane blackletter sizes with a zero placeholder. Installed Minecraft bytecode and a before/after verifier establish unchanged effective frame sequence/timing, surviving glyph mappings/metrics and PNG bytes. Both resource regressions failed at the intended defects then passed; a fresh minimal packaged bake removed all four invalid-frame and four duplicate-codepoint warnings, with earlier resource repairs intact. Review found no actionable issue. Four unused-frame notices, two HutosLib invalid archive paths and the earlier recipe-category warnings remain separate. Evidence and the immutable artifact are under `build/priority-metadata-native-evidence/`; this is bake-only evidence without visual or connected-gameplay acceptance.

## Circus Observation Modes

On 2026-10-01, `runClinicalGameTestServer test --tests '*Circus*' verifyGameTestResourceLog --console=plain` passed 130 clinical, 66 focused JVM and 700 combined runtime cases plus the resource-log gate, exit 0 (`build/circus-observation-verified-current.log`). The new real-structure-query fixture confirms spectator/dead ticks earn neither discovery nor passive acclimation; living D0/inactive-blood observation remains valid without rank credit, and later spectator inspection retains earned proof without extra points.

The confirmed red run reproduced spectator credit. Earlier compile errors and the first green attempt's nullable D0 enum assertion were fixture mistakes, corrected without weakening the checks. Phase 8 records all four terminal logs, structure/reference cleanup, review and supplied-metadata/direct-tick scope. No native generation/travel/restart, multiplayer or full JVM/alpha rerun is claimed; the sampling-policy mismatch remains unresolved.

## Native Earned Staff Death Recovery

On 2026-10-01, separate `EarnedStaffDeath_20261001` preserved the original formation-earned Staff world. Following recovery of an inherited dead checkpoint, a fresh ordinary fall caused 28 damage and actual death. Dead cold login retained the bond/recall knowledge; normal Respawn and mapped Conjure Staff recalled one base Staff from an empty inventory before corpse pickup. Another cold restart and final stopped NBT retained exact Staff inventory, known/equipped memories, 3,782 handled blood, gourd equipment and D3.

Three clients stopped normally, exit 0: `build/staff-native-death-current.log`, `build/staff-native-dead-reopen-current.log`, and `build/staff-native-recalled-cold-current.log`. Saved NBT/snapshots/screenshots use `staff-native-`; Phase 8 records supplied terrain/travel, inherited assists, and the first wrong-selection utility cast. No production change or suite gate rerun occurred. This covers base Staff environmental-death/Respawn/recall/cold recovery, not natural acquisition, transformed forms/fittings, fair combat or multiplayer.

## Cylinder Operation Resume

On 2026-10-01, `runScriptoriumGameTestServer` passed all 20 required cases (`build/cylinder-resume-final-current.log`, terminal exit 0). The new both-media test serializes paid capture/application mid-operation into fresh block entities, checking exact components, blood costs, remaining tick boundaries, ordinary consumption, maintenance counts and no replay after completed-state reload. Earlier runs exposed the helper's extra tick and reused test machine; both fixture errors were corrected without production changes. Phase 8 records all four terminal logs and review. This is supplied serialization/server-tick coverage, not chunk-unload or cold-process acceptance. No full JVM/resource/alpha rerun is claimed.

## Native First Draws Death And Respawn

On 2026-10-01, separate `PendingDrawsDeath_20261001` copied the stopped earned five-vial pending checkpoint. Ordinary forward input off the raised fixture caused actual fall death (28 damage), not a kill/manual-clone command. Dead ticks and a fresh-client death-screen login retained all five pending vials with an empty inventory. Normal Respawn delivered exactly five once at living D1. Offhand swap and position-assisted ordinary pickups recovered all 36 original stone stacks/components without replacement items. Another cold restart retained exact recovered inventory, pending zero and no collection proof or rank change.

Three clients stopped normally, exit 0: `build/first-draws-native-death-current.log`, `build/first-draws-native-dead-reopen-current.log`, `build/first-draws-native-respawn-cold-current.log`. Saved dead/recovered NBT backups and live client/server snapshots use `build/draws-native-`; screenshots use `draws-death-`. Phase 8 records exact artifacts, supplied D1/active blood/terrain/inventory, fall and pickup assists, and the initial eight-stack pickup before the sweep. This is actual assisted death/Respawn/cold recovery of First Draws, not natural acquisition, combat, other rewards, ordinary Chamber input, native Creative/partial-stack coverage or multiplayer. No source or suite gate reran; unresolved full-alpha sampling policy remains separate.

## Native First Draws Cold Recovery

On 2026-10-01, actual Alchemist briefing in fresh supplied-D1 Survival `PendingDrawsNative_20261001` kept five vials pending with all 36 main slots full. Cold restart retained the claim and exact inventory. Normal offhand swap freed one slot; ordinary ticks delivered exactly five and cleared the remainder. The inventory visibly synchronized, repeated First Draws dialogue showed progress without another gift, and another cold restart retained the exact delivered stacks/components, pending zero, no samples/species and D1. The complete offer fit at 1280x900/GUI scale 3. A stopped pending copy is preserved for later recovery checks.

All three clients stopped normally, exit 0, in `build/first-draws-native-{pending,reopen,delivered-cold}-current.log`. Snapshots/backups use `build/draws-native-` and screenshots use `draws-native-`; Phase 8 records exact files, the obstructed initial tooltip capture and corrected signed-NBT offhand inspection. Supplied rank/active blood, terrain, protection and full stone inventory isolate delivery; they are not natural acquisition. Actual death, ordinary Chamber-input deferral, native Creative/partial-stack cases, other scales and multiplayer remain open. No source or suite gate reran; the preceding focused/loaded evidence and unresolved full-alpha policy failure remain historical.

## Loaded Pending-Gift Lifecycle

On 2026-10-01, all four new real-entry Dream/guided pending-gift return/recovery checks and all seven existing loaded-entry checks passed individually in disposable `ChamberPendingGifts_20261001` (`build/chamber-pending-gifts-loaded-current.log`, client stopped normally, terminal exit 0). The original Chamber review save was preserved. Headless opt-in control ran 140 cases with exactly eleven absent-destination failures (`build/chamber-pending-gifts-destination-control-current.log`, expected exit 1); ordinary unflagged replay passed all 129 clinical cases (`build/chamber-pending-gifts-loaded-headless-current.log`, exit 0). Compilation/catalog checks passed and review found no actionable issues. All required sessions are terminal.

The new cases check actual entry snapshots and dimension transfer, deferred deliveries despite free temporary space, explicit legacy Memory migration, exact inventory/crafting/cursor/equipment restoration, later delivery and no duplicates or degree change. Claims/proofs, timer shortening, direct retry hooks and interrupted transfer remain supplied fixture assists. Natural keyboard gameplay, connected-client inventory sync, cold recovery and multiplayer remain open. Production code did not change after the preceding focused correction. No full alpha/JVM/resource rerun is claimed; see Phase 8 and the eleven commands under Loaded Chamber Entry.

## Observational Pending-Gift Recovery

On 2026-10-01, twelve Dream/guided cases reproduced pending-claim loss through actual interrupted-snapshot recovery. The six delivery paths now defer while `ChamberVisitService.isObservational`: First Draws, First Separation briefing/tools, Communion Spine, Vesper Memory and first Tendril. Inventory-carrying timed-chair/attuned/admin controls still deliver. Explicit legacy Memory migration remains before deferral.

Final `runClinicalGameTestServer test --tests '*FirstDraws*' --tests '*FirstSeparation*' --tests '*ClinicalBlood*' --tests '*ChamberVisit*' --tests '*Qliphoth*' --tests '*Vesper*' --tests '*Mycophant*' verifyGameTestResourceLog --console=plain` passed 129 clinical, 248 focused JVM and 700 combined required runtime cases plus resources (`build/chamber-pending-gifts-verified-current.log`, terminal exit 0). The corrected red run had exactly twelve intended failures; the initial red run also exposed a fixture count error, corrected before production edits. Review found no actionable issues; all sessions are terminal. Tests supply interrupted visit/earned-claim state, then check untouched temporary inventory, exact entry snapshot restoration, later item/component delivery, repeat refusal and unchanged degree. Actual loaded/native entry/exit, live login ordering, client sync, cold recovery, natural progression and multiplayer remain open; see Phase 8. The full-alpha result below predates this focused correction and remains red.

## Current Full Alpha

After the supply-recovery fixes, `alphaCheck --console=plain` ran 2,408 JVM cases with one failure in `BloodProfileMigrationTest.shipsExactlyTheApprovedRestrictions` and stopped before runtime tasks (`build/progression-recovery-alpha-current.log`, exit 1). `alphaCheck --continue --console=plain` repeated that failure but passed all 700 required combined runtime cases and the strict resource-log verifier (`build/progression-recovery-alpha-continue-current.log`, exit 1 solely on `:test`). Both sessions are terminal. Clinical is opt-in; its 116-case evidence at that refresh was separate, not a fresh alpha pass. The subsequent observational correction above passed 129 clinical cases. The profile/fixture policy decision is pending: Armadillo, Desiccant, Mortarbound, Naeglerophaeon and Chitinite differ. No restriction or assertion was changed. Full alpha remains red; Phase 8 records exact IDs, logs and remaining recovery/native acceptance work.

## First Separation Briefing Recovery

Three actual-callback delivery fixtures failed before the pending-count fix among 115 clinical cases (`build/separation-briefing-delivery-red-current.log`, exit 1). Initial verification passed clinical/JVM checks but failed the combined Activation Potential damage test with both damage measurements zero; combat code was left unchanged. Review then caught a stale purification/clarity callback that could mark briefing without its supply claim; its 116-case clinical regression failed first. Final `runClinicalGameTestServer test --tests '*FirstSeparation*' --tests '*ClinicalBlood*' verifyGameTestResourceLog --console=plain` passed 116 clinical, 10 focused JVM and 700 combined runtime cases plus resources (`build/separation-briefing-delivery-verified-current.log`, exit 0). All four sessions are terminal. Loaded coverage includes NBT reload, cleared dead inventory, `restoreFrom(..., false)`, partial/Creative recovery, one-time helper calls and no sample/reward/rank proof. Phase 8 preserves both red runs and the initial combined failure; native synchronization, wording fit, death/cold recovery, natural progression, multiplayer and full alpha remain open.

## First Separation Tool Recovery

Three actual-dialogue callback fixtures failed overflow-drop assertions before the fix (`build/separation-tool-delivery-red-current.log`, 112 clinical cases, exit 1). An initial green run's dead-player snapshot had no free space; review caught that it could not isolate the alive guard. After clearing the separate dead snapshot's inventory, final `runClinicalGameTestServer test --tests '*FirstSeparation*' --tests '*ClinicalBlood*' --tests '*DialogueReward*' verifyGameTestResourceLog --console=plain` passed 112 clinical, 10 focused JVM and 700 combined runtime cases plus resource checks (`build/separation-tool-delivery-verified-current.log`, exit 0). All sessions are terminal. Tests cover supplied earned proof, callback claiming, NBT reload, dead/free-space refusal, `restoreFrom(..., false)`, partial/Creative recovery, duplicate refusal, exact rack components and unchanged D2. Source-contract checks are separate from loaded behavior. Native synchronization, wording fit, death/cold recovery, natural progression, multiplayer and full alpha remain open; Phase 8 preserves both verification runs and review's fixture correction.

## First Draws Supply Recovery

New loaded Survival-full, Creative-full and partial-insertion fixtures failed their intended assertions before the fix (`build/first-draws-delivery-red-current.log`, exit 1). Final `runClinicalGameTestServer test --tests '*FirstDraws*' --tests '*FirstSeparation*' --tests '*ClinicalBlood*' verifyGameTestResourceLog --console=plain` passed 109 clinical, 12 focused JVM and 700 combined runtime cases plus resource checks (`build/first-draws-delivery-final-current.log`, exit 0). All required sessions are terminal. Tests exercise NBT reload, dead-player retention, actual `restoreFrom(..., false)`, D1 retry, exact partial remainder and duplicate refusal. No D2 lesson gate or sample proof is awarded by delivery. Review found no actionable issues. The later teacher/ledger wording edit parsed as JSON but was not rendered in this pass; native death/cold recovery, live inventory synchronization, natural progression, multiplayer and full alpha remain open.

## Qliphoth Atlas Teaching

The subsequent itemless-Bloom icon correction has its own red/green evidence: two expected empty-icon failures among 106 clinical cases, then a no-recipe regression exposing the missed true flag. Final `runClinicalGameTestServer test --tests '*Material*' --tests '*RecipeMap*' --tests '*QliphothBookTeachingTest' --tests '*ArchonEndingDialogueTest' verifyGameTestResourceLog --console=plain` passed 106 clinical, 62 focused JVM and 700 combined runtime cases plus resource checks (`build/qliphoth-icon-verified-current.log`, exit 0). Bloom stays itemless; its Materials icon is a Pome with `hasRecipe=false`, and its rite icon is the Seed medium. Native `qliphoth-icon-native-bloom.png` confirms a rendered proxy, complete text and no crafting section at 1280x900/GUI scale 3/100% zoom. The client stopped normally (`build/qliphoth-icon-native-current.log`, exit 0), retaining exact saved inventory and degree. Dark-icon contrast and native rite-icon rendering remain open; Phase 8 preserves both failed runs and review's corrected finding.

The expanded `MaterialsAtlasMetadataSourceTest` failed on the old Spine creature-drop description, then passed through direct Java execution. `test --tests '*Material*' --tests '*QliphothBookTeachingTest' --tests '*ArchonEndingDialogueTest' compileGameTestJava --console=plain` passed all 26 focused JVM cases (`build/qliphoth-atlas-teaching-current.log`, exit 0). New loaded `qliphothAtlasTeachingFollowsArchonRevelation` verifies actual five-entry predicates/metadata: D5 hidden, D6 veiled preview, D7 Communion teaching, Memory withheld until revelation returns, and D8 knowledge retention without reward. `runClinicalGameTestServer verifyGameTestResourceLog --console=plain` passed all 105 clinical and 700 combined runtime cases plus resource checks (`build/qliphoth-atlas-runtime-current.log`, exit 0). Clinical remains opt-in; direct legacy source execution is separate from JUnit selection.

Review found no actionable issues and scoped diff checks passed. No server acquisition/reward/ending eligibility changed. A subsequent native review in disposable `QliphothAtlasNative_20261001` confirmed all five complete detail descriptions fit at 1280x900, GUI scale 3, atlas zoom 100%. The client stopped normally (`build/qliphoth-atlas-native-current.log`, exit 0); saved inventory and degree state match the source checkpoint exactly. Bloom's blank icon, nearby dark icons, crossing legacy traces, native concealment/previews and other scales remain unresolved or unverified. Phase 8 records captures and assists; this is not full visual or natural-progression acceptance. Full JVM/alpha was not rerun; the historical sampling-policy failure remains unresolved.

## Pending First Spine Delivery

The first 104-case red clinical run reproduced dead-player delivery but missed the Creative hazard because its fixture called degree login without the actual delivery tick. After correcting it, unchanged production failed both expected regressions (`build/fungal-spine-delivery-red-retry-current.log`, exit 1). Final `runClinicalGameTestServer test --tests '*Qliphoth*' --tests '*InitiatoryDegreeBloomIdentityTest' --tests '*ArchonEndingDialogueTest' verifyGameTestResourceLog --console=plain` passed 104 clinical, 55 focused JVM and 700 combined runtime cases plus resource checks (`build/fungal-spine-delivery-final-current.log`, exit 0). The unchanged legacy Spine source contract also passed directly through Java. Clinical remains opt-in.

Delivery requires a living player and an actually carried Spine, preserving pending Communion through save/load and death cloning, Creative-full retry, carried-legacy fulfillment and duplicate refusal. Supplied Communion/death-state fixtures are not natural fruit acquisition, ordinary native death, cold-client pending delivery or multiplayer evidence. Phase 8 records logs and scope. Full JVM/alpha was not rerun; the historical sampling-policy failure remains unresolved.

## Vesper Memory Recovery

Three clinical regressions first failed for legacy pending-claim migration, Creative-full discard and death before retry (`build/vesper-memory-recovery-red-current.log`, exit 1). After correction, `runClinicalGameTestServer test --tests '*Vesper*' --tests '*ArchonEndingDialogueTest' --tests '*InitiatoryDegreeBloomIdentityTest' verifyGameTestResourceLog --console=plain` passed all 102 clinical cases, 181 focused JVM cases, 700 combined runtime cases and resource checks (`build/vesper-memory-recovery-final-current.log`, exit 0). Coverage includes explicit old-claim migration, slot-opening delivery, serialization, death cloning and duplicate refusal. The clinical namespace remains opt-in; supplied claims/death events are not native victory, ordinary death or cold-client persistence.

The legacy `EndgameBossRewardResourceTest` main failed its stale direct-Mycophant-delivery string assertion, then passed via direct Java execution after alignment with pending delivery after arena exit. It is separate from focused JUnit selection. Phase 8 records review and compatibility limits. Full JVM/alpha was not rerun; the historical sampling-policy failure remains unresolved.

## Mycophant Tendril Recovery

`runClinicalGameTestServer` passed all 100 required cases after the unique Tendril fix (`build/mycophant-tendril-delivery-green-current.log`, exit 0). Its new regression first failed the expected full-inventory pending-claim assertion (`mycophant-tendril-delivery-red-current.log`, exit 1). Coverage includes blocked login/tick delivery, opening a slot, saved-data reload, death cloning and duplicate refusal. The clinical namespace remains opt-in. Direct victory callbacks and entity save/clone checks do not prove native boss combat, ordinary death or cold-client persistence; Phase 8 records the boundary.

Review then reproduced Creative full-inventory false insertion and dead-player pre-respawn delivery losses; both now retain the claim. Final replay passed the expanded 100 clinical cases, 16 focused Mycophant/ending/Bloom JVM cases, 700 combined runtime cases and the resource gate (`build/mycophant-tendril-final-current.log`, exit 0). An earlier integrated attempt had one Activation Potential zero-damage failure, skipped resource verification and ended exit 1; unchanged replay passed without a claimed fix. Phase 8 records all red logs and review scope. Full JVM/alpha was not rerun, and the historical sampling-policy failure remains unresolved.

## Completed Native Apotheos

`ApotheosNativeFinal_20261001` continued a disposable copy of the assisted floor-repair checkpoint through remaining supports, normal seal, all six actual waves, native anchor repairs, response Seal, offering procession and culmination. It earned D8/Apotheos and the advancement, restored the exact escrowed Staff and normally collected one Quintessence. Cold restart retained exact completed inventory/degree and no active rite. A supplied Vicar acknowledged Apotheos and still offered unfinished Annetta work; the quest was left untouched. Journal audit excludes direct damage/kill, journey advancement, state/phase edit, tick-rate and game-mode forcing commands. Protection, high-damage sword, terrain/preparation and position/aim remain explicit assists. Two preceding operator-interrupted response failures were preserved with D7/pending, one Staff and no D8. All four clients ended exit 0; Phase 8 lists logs, snapshots, screenshots and typed-NBT evidence. No source/test/full gate reran; natural pacing, fair combat, multiplayer and broad acceptance remain open.

## Native Spine Recovery

`SpineRecoveryNative_20261001` is a disposable copy of the assisted Communion world. Explicitly clearing only its Spine simulates loss; a fresh client retained the absence. Normal empty-handed sneak-use at the original living nine-husk Bloom returned one Spine, with repeat use before/after another cold restart refusing duplicates. Typed NBT retained the recovered checkpoint's exact inventory/components and degree. Three clients ended exit 0 in `build/spine-recovery-native-{loss,reclaim,retained}-current.log`. A misplaced conduit control is excluded, so exact original inventory through setup is not claimed. The overlong undecided action-bar hint was shortened; the existing source contract failed before the change and passed afterward through direct Java execution, and native `spine-recovery-native-short-hint.png` fits at 1280x900. Phase 8 records snapshots and limits. No full gate reran; natural death/acquisition, full-inventory native delivery, multiplayer and endings remain open.

The Spine follow-up also passed both focused Qliphoth teaching/Bloom-identity JVM cases in `build/spine-recovery-teaching-jvm-current.log`, terminal exit 0. This is not a full alpha or loaded runtime replay.

## Native Broken-Floor Sigil Input

An assisted Apotheos client paid 5 mL through ordinary held Projection at a lowered Bastion marker and retained it across a fresh client process. The first repair attempt ended in an actual Rogue Will death and is excluded. A separate protected retry repaired the floor with two ordinary stone placements, retained the partial payment, paid the remaining 45 mL at the raised marker and awakened Bastion. Completed-marker overhold retained stone/progress; regeneration prevents claiming an isolated exact blood cost from the native snapshot. All three client logs ended exit 0. Final saved assertions retain Inscription, all anchors paid, Bastion 6/6, zero instability, one escrowed Staff and D7/pending without D8. Phase 8 records artifacts and assists; natural pacing, fair combat, multiplayer and completed endings remain open. No source/test/full gate reran.

## Sigil Target Lifetime And Visibility Fixtures

The support blocker test now checks ordeal, final still interval and release at culmination. `awakenedResponseBlocksOnlyItsCurrentWave` checks a completed response through 65 production casts, then releases its old location during the next interval/non-response wave. Phase transitions are supplied callbacks, not natural timing. Vitric's charge-comparison fixture now uses a clear ray above its own structure instead of world spawn, retaining its original damage assertions; a supplied-wall negative control retains block interception. The full 700-case runtime run passed these checks but had a separate preexisting-Blackstone covenant-fixture failure and skipped resource verification. JVM validation completed 2,408 cases with the unchanged sampling-policy failure. Phase 8 records exact logs and final replay.

Final fixture-isolated replay passed all 700 required runtime cases and resource checks (`build/sigil-lifetime-final-isolated-resource-current.log`, exit 0). Covenant setup now uses the existing clean template and an asserted-empty supplied Y=40 platform, preserving placement guards and action/restore checks; template selection alone did not fix its repeated ground collision. Direct Liber/Hematic Iron output checks now use the established clean-template/entity-loaded wait, keeping exact delta/advancement assertions. Their earlier capture-failure cause is not independently proved. No production behavior or final JVM result changed; native timing, multiplayer and fair endings remain open.

## Broken-Floor Sigil Repair

The completed-sigil loaded fixture also tests an unfinished Bastion above a supplied two-block depression. The lowered marker accepts 5 mL; repairing its floor retains that payment and lets the raised marker accept the remaining 45 mL, completing exactly one node. Repeated input then costs zero and adds no instability. `build/sigil-broken-floor-recovery-current.log` passed 698 required runtime cases and resource checks, exit 0. No production change or JVM rerun occurred. This is supplied server terrain/aim evidence, not native visual/input, cold-recovery or multiplayer acceptance; Phase 8 records scope and the preceding sampling-policy JVM failure.

## Completed Sigil Projection Blockers

`awakenedSigilRetainsItsProjectionBlocker` uses the loaded Apotheos recipe's resolved Bastion placement, an awakened shape, and an ordinary stone backdrop. It requires handled/zero payment, then 65 production Projection calls without blood, terrain or progress changes; an unrelated miss must still permit ordinary Projection. `fullyPaidShapeKeepsItsGroundMarkersAsBlockers` covers the pure target-index rule. Phase 8 records red/green evidence and exact integrated results. These supplied-state checks do not prove native broken-floor behavior or fair completed endings.

Final unchanged server/resource replay passed 698 required cases and resource checks (`build/awakened-sigil-blocker-resource-repeat-current.log`, exit 0). Full JVM validation completed 2,408 cases with the unchanged sampling-policy failure; all five virtual-targeting cases passed. Its server run had an existing Vitric block-hit/zero-damage failure that did not recur on replay. The initial full attempt had a corrected missing assertion import, not JVM test execution. Completed-response and phase-transition removal remain direct-test gaps; full alpha is not green.

## Native Unvisited Survey Referral

Fresh Survival/Normal `FungalSurveyUnvisited_20261001` (seed 42061003) retained no visit/report data through normal supplied-D2 Vicar teaching and cold client restart. Normal Quest Work navigation rendered the complete unvisited Overworld/specimen/non-consuming inspection lesson at 1280x900. Normal ledger use stayed Side 0/3 beside Main First Separation 0/8; read-only saved NBT retained D2/full health and exact inventory across restart. Rank, active blood and teacher were assisted; terrain, travel, specimens and proof were not supplied. Both client logs, `build/fungal-survey-unvisited-native-current.log` and `build/fungal-survey-unvisited-reopen-current.log`, exited 0 without operator errors. Phase 8 records captures, snapshots, startup-resource caveats and limits. No production source or automated-suite result changed; natural pacing, other scales and multiplayer remain open.

## Founder Reception Late Conflicts

Two blood-injection GameTests exercise canonical and personal recruit conflicts acquired during a founder ceremony, then a clean full-duration retry with exact binding/starter rewards. Initial immediate-conflict coverage passed all 91 focused cases in `build/founder-reception-conflict-current.log`; the final fixture injects conflict at tick 199 and checks cancellation past completion time plus the retry's independent duration. Production guards are unchanged. Phase 8 records final integrated evidence and cleanup. Supplied detached profiles, game-time delays and explicit ceremony callbacks are not real multiplayer or ten-second wall-clock reception acceptance.

Final `build/founder-reception-conflict-alpha-current.log` passed 697 integrated required tests, resource verification and 91 focused injection cases. One of 2,407 JVM cases still fails on unchanged sampling policy; overall exit 1. Both new fixtures belong to the integrated namespace.

## Native Visited Survey Referral

Normal Vicar interaction in disposable `FungalSurveyReferral_20261001` recognized the retained early visit, rendered both referral paragraphs at 1280x900 and delivered the normal assignment ledger. The survey remained D2 Side 1/3 beside Main First Separation 0/8. Saved typed NBT retained the original startup note plus exactly that ledger and unchanged unreported survey; the D0 source world was untouched. The client stopped exit 0 in `build/fungal-survey-referral-native-current.log`. Rank/active blood/teacher were supplied. Phase 8 distinguishes the initial blood-tendency capture from accepted survey evidence; unvisited lead, natural advancement/teacher discovery, native replacement-teacher report and multiplayer are unverified. No source/full gate changed or reran.

## Native Survey Mode Isolation

Fresh disposable `FungalSurveyMode_20261001`, Survival/Normal seed 42061002, isolated the new observer guard in native play. A no-proof D0/inactive-blood baseline remained without a visit during spectator travel inside confirmed Overworld Gardens; Survival observation then recorded `Visited` without referral or proof injection. Cold client restart retained the unreported visit, rank, inactive blood, full health and exact inventory. Both native sessions ended exit 0 (`build/fungal-survey-mode-native-current.log`, `build/fungal-survey-mode-reopen-current.log`). Phase 8 lists inspected captures, snapshots and existing far-chunk canopy warnings. Travel/mode switching were assisted; no broader generation/performance, natural acquisition or multiplayer acceptance is claimed. No source changed or full gate reran; the preceding 695 runtime/resource checks passed while sampling policy keeps JVM alpha red.

## Survey Observation And Teacher Recovery

`fungalSurveyRejectsSpectatorVisitAndResumesWithAnotherAlchemist` failed before excluding spectator discovery from the survey recorder. It then verifies supplied D0/inactive-blood Survival visit evidence, unfinished persisted-data restoration, removed/remote teacher rejection and a non-consuming report through a different nearby living Alchemist, retaining exact specimen counts/components and D2. Final `build/fungal-survey-resumption-alpha-current.log` passed 695 required runtime cases and resource verification; the sole failure among 2,407 JVM cases remains sampling policy, overall exit 1. Direct biome arguments and persisted-data copying are fixtures, not natural discovery/death-clone/native restart acceptance. Phase 8 also qualifies the older pre-fix native visit attribution because spectator travel preceded Survival harvesting; its natural plant/drop evidence remains valid.

## Distributor Apply State Sync

The registered-player `distributorApplySynchronizesOmittedThelemicMemory` failed with zero captured packets before conditional muscle-state sync. Its initial detached-player fixture and first fix attempt did not exercise NeoForge tracking delivery; Phase 8 distinguishes those logs. The corrected green run passed all 99 clinical cases in `build/distributor-apply-sync-green-registered-current.log`, including immediate stopped state, retained knowledge/reserves, free Apply, no repeat update and legacy/mixed pattern NBT restoration/re-application. These loaded checks do not establish a second real client, native GUI response or a new cold-world restart; Phase 8 records integrated status and broader limits.

Final `build/distributor-apply-sync-alpha-current.log` passed 694 integrated required tests, resource checks and 99 clinical cases. The sole failure among 2,407 JVM tests remains sampling policy; overall exit 1. The preceding Vulture timing failure did not recur, and no fix is claimed for it.

## Distributor Learned-Memory Validation

`distributorSaveRequiresLearnedThelemicMemory` failed before Save/Overwrite's knowledge filter received the actual player; stale unlearned Thelemic keys previously spent blood/XP. The corrected first run passed 96 clinical cases. Expanded production packet-handler tests passed 97 cases in `build/distributor-known-memory-alpha-current.log`, covering normal paid Save, remote Save/Rename/Apply and unknown/over-capacity Apply with record, equipment, activation, reserve and cost retention. The same alpha retained one sampling-policy JVM failure among 2,407 and a Vulture actor-tick fixture failure among 694 integrated tests; resource verification did not run. Phase 8 records the unchanged replay and limits. Supplied-state handler tests are not real multiplayer acceptance.

## Dense Instruction Page Decals

Eleven station/scar instruction pages explicitly disable the existing over-text decal layer. The new production-codec round-trip regression failed before correction; all 21 focused binding, Vein-Mason teaching and station-upgrade JVM cases passed afterward (`build/progression-book-decals-green-current.log`). Native Scriptorium first/last spreads are unobscured at 1280x900, superseding the overlap issue below; other pages/scales are unverified. Normal shutdown preserved all original typed stacks and the earned `DIALOGUE` source. Final alpha passed 694 required runtime tests and resource verification, but one of 2,407 JVM cases still fails on unchanged sampling policy (`build/progression-book-decals-alpha-current.log`). Overall exit 1; Phase 8 records captures, assists and limits.

## Native Scriptorium Teaching

Ordinary teacher selection in disposable `EffigyPresentation_20261001` unlocked the Scriptorium page without a knowledge override. Saved source was `DIALOGUE`; all four leaves appeared in the filtered book, and cold restart retained access without repeating teaching. The existing review rank was D6, so exact D3 arrival is not native evidence. A new event ID unintentionally routed this optional topic into Quest Work; the hub regression failed before changing its ID to `mnemonist_scriptorium_taught`. Fixed native captures show the topic in Lore, and `build/scriptorium-lore-routing-green-fixed-current.log` passed 96 dialogue JVM and 94 clinical cases. Both client sessions stopped exit 0 and retained all original typed inventory stacks. Final alpha passed 694 required runtime tests and resource verification, but one of 2,406 JVM cases still fails on sampling policy (`build/scriptorium-teaching-final-alpha-current.log`). First-spread blood-stain overlap, other scales, natural acquisition and broad acceptance remain open. Phase 8 lists exact captures, excluded setup attempts and assists.

## Machine Operation Lesson Discovery

Two new clinical cases verify the existing D3 Mnemonist Scriptorium choice unlocks a registered page without Main proof or repeat rewards, rejecting early, remote, wrong and absent teachers. Extended station coverage checks refinement/compounding and binding/refilling page discovery alongside Condenser/Athanor eligibility. After captured loaded failures, `build/station-operation-discovery-green-current.log` passed 112 focused JVM cases, 94 clinical and 35 station GameTests. Initial alpha in `build/station-operation-discovery-alpha-current.log` retained the sampling-policy JVM failure among 2,405 cases and a claimed-Will puppet-slot failure among 694 runtime tests; resource verification did not run. Unchanged replay passed 694/694 and resource verification in `build/station-operation-discovery-server-repeat-current.log`. New clinical cases are focused-namespace coverage, not additions to the combined count. No puppet code changed and its failure cause is unproven. Phase 8 records native/natural-acquisition limits.

## Station Upgrade Lesson Discovery

Two loaded regressions in `build/station-lesson-discovery-red-loaded-current.log` failed for undefined filtered-book pages and missing earned login repair. Seven upgrade pages now register and unlock on personal catalog eligibility during shared station sync, without a kit claim. Green verification passed 18 focused JVM cases and 35 station GameTests in `build/station-lesson-discovery-green-current.log`; coverage includes rank-only/gifted-kit/inactive-blood rejection, earlier practice, no claims/items/promotion and login backfill. Integrated `alphaCheck --continue` passed 694 required GameTests and resource verification, but one of 2,405 JVM cases still fails on unchanged sampling policy (`build/station-lesson-discovery-alpha-current.log`, exit 1). Native presentation, actual connected-player login and base-operation pages are not accepted by these fixtures. Phase 8 records the initially excluded test namespace and remaining scope.

## Scar Teaching Presentation

`build/scar-page-pagination-jvm-current.log` passed resource processing and nine focused Vein-Mason/book cases after shortening the First Scar page without removing its actions/costs/gates. Native review in `build/scar-page-pagination-native-current.log` verified three leaves, no orphan-only leaf, and the full revised Anchorite Effigy lesson at 1280x900; normal stop exited 0. Teacher branch access used a temporarily revoked copied milestone and a supplied NPC, then restored that milestone; it is not earned progression. All 36 original typed inventory stacks survived plus the review book. Phase 8 lists captures and remaining other-scale, earned-discovery, multiplayer and broad acceptance limits. No full suite reran; preceding sampling-policy/Vitric alpha failures and the passing unchanged server/resource replay remain the recorded integrated evidence.

## First Scar Discovery

`scarLessonUnlocksRegisteredLiberPracticePage` verifies first-lesson advancement unlock of a registered Liber page without preparation/Main proof. `earnedScarLessonBackfillsPracticePageWithoutNewRewards` checks earned-lesson login repair, retained other knowledge, newcomer exclusion and repeat safety without items/promotion. Both failed before wiring; all 92 clinical tests passed afterward. Full alpha retained one of 2,405 JVM failures (sampling policy) and one intermittent Vitric failure among 690 combined tests. Unchanged replay passed 690/690 plus resource verification. Logs: `build/scar-page-discovery-red-current.log`, `build/scar-page-discovery-alpha-current.log`, `build/scar-page-discovery-server-repeat-current.log`. Native corrected filtered-book review reached all four scar-page leaves at 1280x900 and stopped exit 0 in `build/scar-page-discovery-native-fixed-current.log`, preserving all 36 original stacks plus one supplied book. Page access was assisted; the two-word fourth-leaf orphan, Anchorite preview and broader acceptance remain open. Phase 8 distinguishes excluded pre-fix cover captures from actual page evidence.

## Shared Effigy Contributor Eligibility

`effigyProjectionRejectsUnknownScarsWithoutLosingPaidWork` and `effigyProjectionRejectsInsufficientCapacityWithoutLosingPaidWork` establish a real 200 mL partial motif, then reject a second player's ineligible projection without spending blood, changing escrow, emitting output or granting proof. Making that guest eligible permits the same shared work to finish for exactly the remaining 300/800 mL, with one original pattern and preparation-only credit. Both failed before the charging guard; green passed all 90 clinical tests. Final `alphaCheck runClinicalGameTestServer --continue --console=plain` passed 690 combined/90 clinical GameTests and resource verification, with one of 2,405 JVM cases retaining the unchanged sampling-policy failure. Updated teacher/Liber resource assertions passed. Logs: `build/effigy-contributor-red-current.log`, `build/effigy-contributor-green-current.log`, `build/effigy-contributor-alpha-current.log`. This is server-fixture evidence, not real multiplayer or native feedback/pagination acceptance; Phase 8 records those limits.

## Effigy break recovery

`breakingChargedEffigyReturnsItsExactMotifOnce` uses real destruction after 200 mL partial charging and a same-block facing change. It requires exactly one returned motif with its original components, cleared escrow, no blood refund/proof and no duplicate on repeated removal/recovery. The new assertion failed before the removal hook. Green passed 88 clinical tests; final `alphaCheck runClinicalGameTestServer --continue --console=plain` passed 690 combined/88 clinical GameTests and resource verification, retaining only the separate sampling-policy failure among 2,405 JVM cases. Logs: `build/effigy-break-red-current.log`, `build/effigy-break-green-current.log`, `build/effigy-break-alpha-current.log`. Final resource processing and nine focused Vein-Mason/book JVM cases passed in `build/effigy-break-final-teaching-current.log`, including the recovery-text requirements. The reload fixture replaces only its block entity, not the block. Native recovery/pagination, actual restart, multiplayer and broader acceptance remain open; see Phase 8.

## Pending Effigy interruption

`chargedEffigySelectionSurvivesInterruptionAndReload` verifies that a partly paid motif cannot lose/change selection, retains its 200 mL charge through a block-entity save/load round trip, spends only the remaining 300 mL, ejects one original Heart pattern and grants preparation rather than Main commit proof. Completion reopens selection. The loaded red run failed at clearing selection; the green run passed 87 clinical and six focused Vein-Mason JVM cases. Final `alphaCheck runClinicalGameTestServer --continue --console=plain` passed 690 combined/87 clinical GameTests and resource verification, with one of 2,405 JVM cases still failing on sampling policy. Logs: `build/effigy-selection-red-loaded-current.log`, `build/effigy-selection-green-current.log`, `build/effigy-selection-alpha-current.log`. Phase 8 distinguishes this round trip from native/cold-world/block-break recovery; those and broader acceptance remain open.

## Authored blood HUD mask

The vessel pixel tests use current backing alpha and frame geometry, not obsolete dimensions or a required shoulder palette. They cover full visible-window fill, volume boundaries, mask containment, degree/Pome colour, all authored Apotheos frames and empty masks. A new coverage case failed before the renderer correction. Final focused vessel/gourd selection passed 13 unique cases in `build/blood-fill-mask-green-current.log`. The full run in `build/blood-fill-mask-alpha-current.log` passed 690 combined/86 clinical GameTests and resource verification; 2,405 JVM cases retained one restriction-policy failure. Native D1 full/half/near-empty, D7 full and animated D8 fit at 1280x900 in a disposable copy; the low capture was 85 mL after regeneration, not exact zero. Native log ended exit 0. Phase 8 lists artifacts and assists; resource reload, other GUI scales/positions and live gourd variants remain open.

## Pending-rest death cloning

`concentratedBloodRequiresOwnRewardAndCompletedSleep` now extends actual vial injection through same-GameProfile `restoreFrom(original, false)`, retaining pending rest, charm, degree and both promotion proofs. Wake events still reject interruption and award D3 once; pending rest rejects repeat injection. Final `runBloodInjectionGameTestServer verifyGameTestResourceLog --continue --console=plain` passed 89 focused injection and 690 combined tests plus resource verification, exit 0, in `build/concentrated-blood-death-clone-final-current.log`. Phase 8 records the initial differing-UUID fixture and unsupported old-object NBT assertion failures. No production behavior changed; native death/bed timing, cold recovery and broad acceptance remain open. The separate two-case profile JVM audit retained one restriction failure; no full JVM suite reran.

## Pome-window services

Native review on 2026-10-01 validated the undecided D7 reaction, full greeting and topic hub at 1280x900 after ordinary consumption of a supplied unbound Pome. Normal church-map selection spawned its map; position-assisted pickup and normal stop saved all 16 original stacks unchanged plus one map. The original world was preserved. Log: `build/vicar-pome-native-current.log`, exit 0. Phase 8 lists captures and excludes the mis-aimed after-expiry attempt. Other ending-state presentation, natural acquisition, multiplayer and fair endings remain open; no suite reran during this review.

`pomeEmpoweredVicarRetainsOrdinaryEndingServices` compares the real Vicar's normal and empowered dialogue across all five ending states, preserving root options, service nodes, valid targets and player degree/path. It first failed in `build/vicar-pome-services-red-current.log`; after the greeting-only fix, all 690 combined GameTests and resource verification passed in `build/vicar-pome-services-green-current.log`. The focused `ArchonEndingDialogueTest` and `VicarCovenantGuidanceTest` cases passed 9/9 in `build/vicar-pome-services-jvm-current.log`. Native presentation and full ending acceptance remain open. The full JVM suite was not rerun; its preceding serial result retains three HUD/profile failures.

## Merged Alembic XP

The real pickup/shift-click checks now count each XP orb's per-award value multiplied by its saved merged `Count`. A new loaded test forces vanilla merging across the two actual menu extractions and still requires exactly two XP, unchanged output components/counts and cleared recipe ledgers. Before correction, `build/alembic-merged-xp-red-current.log` failed with `Value:1,Count:2` and total one. Afterward, all 21 focused distillation tests passed. The final unchanged `verifyGameTestResourceLog --console=plain` replay passed 689 combined tests and resource verification, exit 0, in `build/alembic-merged-xp-resource-repeat-current.log`. Intermediate runs failed existing Alembic, Obscured melee or Vitric checks; Phase 8 distinguishes the reproduced accounting defect from unproven individual intermittent causes. No production mechanics changed and no fresh JVM/clinical run occurred; the preceding serial JVM result retains three failures.

## Staff Death Recovery

`livingStaffJourneyUsesRealStructureCraftAndRestoresBond` now extends real formation crafting through `ServerPlayer.restoreFrom(original, false)`, verifying earned bond/recall knowledge, independent attachment state and one recalled Staff. On 2026-10-01, `runGameTestServer --console=plain` passed 688/688 in `build/staff-bond-death-clone-current.log`. The resource-verification task reran that suite and failed one existing Obscured melee check; the Staff check passed again, but resource verification did not execute. Log: `build/staff-bond-death-clone-resource-current.log`. This is death-clone/action coverage, not native key-input or a full lethal-hit/cold-recovery acceptance. Phase 8 separately records normal refusal respawn/re-entry and exact recovery of 13 ordinary dropped stacks with protection and two positioning assists. No production gate changed or fresh JVM run occurred; the preceding serial JVM result retains three failures.

## Ending-choice teaching

Run `./gradlew.bat test --tests '*ArchonEndingDialogueTest' --tests '*SeveredQliphothStateTest' processResources --console=plain` for the returned-revelation wording and pending/proven-state checks. On 2026-10-01, all eight cases passed after two new teaching assertions first failed. Native review of an already-saved decision caught an overlong Silence label; the shortened option and both responses fit at 1280x900. Normal choice in a disposable copy saved D7 `SILENT_PENDING`, no Vesper memory and exact unchanged full inventory, preserving the original undecided world. Cold runtime reopening retained the pending state and exact inventory. Phase 8 records artifacts and excludes the faded-chat capture from receipt-layout evidence. This is not fair combat or completed-ending acceptance. The preceding integrated gate passed 688 combined/86 clinical GameTests and resource verification, with three failures among 2,401 JVM tests.

For the introductory Bloom page, include `--tests '*QliphothBookTeachingTest'` with the book/resource checks. Its assertion rejects the retired divine-prohibition explanation and requires the actual rite, owner and nine-pome teaching without Vesper spoilers. On 2026-10-01, all 11 focused book/dialogue cases passed. Native review confirmed two-leaf pagination at 1280x900, not unobscured readability because existing blood stains cover part of the left leaf. Fresh alpha retained three failures among 2,402 JVM tests and had three intermittent combined failures; an unchanged replay passed 688/688 and resource verification, while clinical passed 86/86. Phase 8 records both runs and their limits.

## Latest integrated gate

The held-weapon wound-entry correction passed 688 combined and 86 clinical GameTests plus resource verification in `build/silent-held-weapon-entry-green-current.log`. That JVM run had 19/2,402 failures while a native Gradle launch overlapped, including sixteen dependency class-loading failures. A serial `test --rerun-tasks --console=plain` replay in `build/silent-held-weapon-entry-jvm-serial-current.log` retained only the same three HUD/profile failures. The loaded entry regression first failed before the fix. Native two-cut/held-Blade entry was verified; the unarmored player subsequently died before shutdown, so arena logout inventory preservation and dropped-item recovery are not claimed. Phase 8 records the complete chronology and assists.

The latest 2026-10-01 integrated replay is `build/carousel-natural-tick-alpha-current.log`: 2,402 JVM tests with the same three HUD/profile failures, 688/688 combined, 86/86 clinical and passing resource verification. The Carousel fixture now waits for its first natural tick before the original three-tick rider-position check and cleans up its temporary ticking tickets on success/failure. This is test readiness, not a production Circus or Main-path change. Phase 8 records the captured zero-tick failure, repeat and unverified timeout-cleanup limit.

## Shared station upgrades

See [STATION_UPGRADES.md](STATION_UPGRADES.md) for gates, offerings, regression requirements, and dated validation limits. Run `./gradlew.bat test --tests 'com.vincenthuto.hemomancy.common.station.*' compileGameTestJava --offline --console=plain` and `./gradlew.bat runStationUpgradeGameTestServer --offline --console=plain`. The dedicated suite covers real activation/ordered completion for both tiers of all four stations, personal eligibility without mandatory prior gift claims, one-time/pending rewards, and non-duplicated offering recovery. It prepopulates anchor/seal state and accelerates ticks; client tooltips, connected-player multiplayer, natural timing, and full restart behavior need separate evidence.

Hemomancy now has two complementary test layers. JVM tests catch fast logic and resource regressions; NeoForge GameTests load the real server registries and exercise player-facing progression fixtures. The GameTest code lives in `src/gameTest`, is available in development runs, and is not included in the release jar.

## Liber reader layout

Run `./gradlew.bat test --tests '*BookResourceBindingTest' --tests '*BookClarityTest' --tests '*MemoBookPresentationTest'` in Hemomancy, and `./gradlew.bat test` in the sibling HutosLib checkout. `BookGeometryTest` covers a short spread, a centered compact leaf, and narrow viewport bounds. The authored-theme test checks both Libers at a 428×240 GUI size.

On 2026-10-01, the disposable `runBookKitReviewClient` review confirmed the Liber Immaculatus cover/contents and long-entry spread at 856×480 with GUI scale 2, a next-arrow turn onto its third leaf, and a bounded single-page cover at 720×480. HutosLib passed 133 JVM tests. Hemomancy passed the focused book checks; its full JVM run passed 2,396 of 2,399 tests, with failures outside the reader changes: `BloodFillPixelsTest.upperFrameShoulderDoesNotCoverFillWithSilverPixels`, `BloodFillPixelsTest.fullFillTopMatchesStaticVesselTextures`, and `BloodProfileMigrationTest.shipsExactlyTheApprovedRestrictions`. This review does not establish multiplayer or new progression behavior.

## Everyday commands

From the project root on Windows:

```powershell
./gradlew.bat test
./gradlew.bat runGameTestServer
./gradlew.bat alphaCheck
```

- `test` runs the native JUnit tests and adapts every legacy `public static void main` contract into an individually reported JUnit dynamic test.

- `runGameTestServer` starts a headless NeoForge server, runs the registered progression scenarios, and exits non-zero if a required scenario fails.
- `alphaCheck` runs both layers in order. Use this before an alpha build or whenever progression, crafting, quests, or rewards change.

## Ledger header fitting

`HarbingerAssignmentLedgerCollapseSourceTest` guards the shared collapsed/expanded progress budget and title hover path. It is a legacy `main` contract, so invoke its compiled class directly for an isolated check or include `LegacyMainTestAdapterTest`; filtering Gradle by its own class name finds no JUnit tests. Native validation must inspect verbose labels such as the D4 Vein-Mason count in collapsed and expanded views, including full-wording hover and separation from the title. The 2026-10-01 1280x900 replay verified that case; other GUI scales/locales remain open.

## Held-sample teacher lessons

The combined server includes `alchemistHeldJarKeepsCurrentDegreeLessons` and `alchemistHeldFloraKeepsCurrentAndUnfinishedLessons`. They exercise loaded D2-D8 dialogue factories, preserving the current greeting/options, research actions, unfinished early assignments and valid merged targets. Native checks must also navigate current lessons while holding the actual specimen/flora. The 2026-10-01 D4 Polyp-jar replay displayed the Lantern lesson through Conversation/Speak Freely; the wild-Morphling option was present, but its body and other native sample/degree combinations remain open. Factory tests do not substitute for event authorization or connected-player checks.

## Combat fixture isolation

For combat fixtures, move supplied players into the test's own world-space area before creating targets. `new ServerPlayer` alone leaves them at shared spawn, where concurrently running fixtures can provide a closer enemy or obstruct a cast. Preserve the production targeting rules; verify the intended target and include a non-target control when selection matters. Capture ray/entity diagnostics after the measured action so probes cannot make chunks ready before the cast.

## Queued field guidance

Voyager vessel bearings run as bounded server-tick chart work. Loaded fixtures check immediate acknowledgement without quest proof, eight-request capacity, duplicate slot retention, ninth-request refusal and real logout-event cleanup/retry. Fixtures must release their queued requests in `finally`; discarding an unregistered test player alone does not post logout. Native checks must separately prove eventual destination accuracy, ongoing world ticks, interruption/retry and delivery without proof. A time budget between vanilla probes is not a hard latency cap: one probe can still wait on worldgen/storage. Other-seed cost, unsupported layouts, complete missing-candidate scans, nested-wait state changes and connected multiplayer need their own evidence.

## Loaded Chamber entry

The default headless clinical world has no Chamber level. Its overlap rejections cannot independently establish the overlap predicates. To register eleven additional destination-required checks, start `./gradlew.bat runBookKitReviewClient -PchamberLoadedValidation=true`, create a disposable world, and run these commands one at a time, waiting for each completion:

```text
/test run loaded_chamber_active_visit
/test run loaded_chamber_fungal_projection
/test run loaded_chamber_already_inside
/test run loaded_chamber_dream_snapshot_return
/test run loaded_chamber_dream_menu_recovery
/test run loaded_chamber_guided_snapshot_return
/test run loaded_chamber_guided_menu_recovery
/test run loaded_chamber_dream_pending_return
/test run loaded_chamber_dream_pending_recovery
/test run loaded_chamber_guided_pending_return
/test run loaded_chamber_guided_pending_recovery
```

Each first requires the actual Chamber `ServerLevel`. The first three check entry rejection and exact preservation of binding/visit data. The dream pair enters through completed-sleep handling with guaranteed eligibility, verifies actual entry snapshots with nonempty crafting inputs and a component-bearing cursor, and checks shortened timer return or outside recovery with another menu open. The guided pair uses real guided entry with an empty cursor and verifies equipped-gourd restoration and completion only on valid return.

All eight restoration cases hold temporary tickets, wait for every origin/private-cell query chunk to be entity-loaded, and prove that each query sees a supplied item probe. A temporary forced anchor keeps the otherwise empty Chamber entity loop running; cleanup removes only an anchor the fixture added. UUID sets must remain unchanged immediately and after both probes receive five natural ticks. Pass/failure listeners clean actors, probes, tickets, and the added anchor. This is a bounded loaded-window check, not crash, cancellation, long-duration, or genuinely unloaded-chunk recovery acceptance. Use disposable worlds and wait for each test before starting another.

The four pending-gift cases additionally create real entry snapshots before freeing a temporary slot and calling production retries. Dream cases cover D1 First Draws; first-guided D7 cases supply all six earned pending claims. No gift may insert during observation, and the explicit legacy Memory claim must still migrate. Timer return or outside recovery must restore the snapshot first, then free-space retries deliver exact counts, initialized rack components and no duplicates without changing degree.

Actors, eligibility, pending claims, timer shortening, direct retry hooks, equipment mutation, interrupted transfer, and temporary chunk forcing are fixture assists, not natural player journeys or connected-client inventory-sync checks. Without the opt-in flag, these generated tests are absent and the clinical suite remains unchanged. `runClinicalGameTestServer -PchamberLoadedValidation=true` is a negative control: all eleven must fail on the absent destination. Do not enable that flag for ordinary alpha runs. See the progression handoff for dated results and remaining live acceptance limits.

## GameTest suite catalog

`gradle/game-tests.gradle` is the source of truth for suite namespaces, focused launcher names, run directories, and inclusion in `alphaCheck`. Each entry must explicitly set `alpha: true`, or `alpha: false` with a reason and a focused launcher. The combined server includes the existing Hemomancy, blood-injection, harpoon, and item-inventory suites plus combat order, owner snapshots, distillation, registration, Unstained zone lifecycle, and recipe codec plumbing. The vanilla `minecraft` template namespace remains enabled for legacy fixtures.

Existing focused encounter, clinical, brewing, Scriptorium, animation, and living-weapon suites remain explicitly opt-in; this change expands the combined gate with consolidation coverage. Fresh-world Phlegethontic validation remains separate because it requires seed/run parameters and validation mixins. Existing focused task names and directories are preserved. Living weapons now have a normal `runLivingWeaponGameTest` task instead of relying on the missing external init script.

`./gradlew.bat verifyGameTestSuites` checks source holders and generators against the catalog, requires explicit gate decisions and exclusion reasons, and rejects undeclared or mismatched launchers. It runs before `test`, `check`, `alphaCheck`, and run preparation. A new namespace therefore fails the gate until its catalog entry is added. Use literal `@GameTestHolder("namespace")` values or `Hemomancy.MOD_ID`; declare generated suites using their generator class.

The guard itself has integration probes:

```powershell
./gradlew.bat -I src/test/gametest-suite-registration.init.gradle -PsuiteProbe=positive verifyGameTestSuites
```

The `missing-decision`, `missing-reason`, `unknown-namespace`, and `unlisted-run` probe modes must fail with the corresponding registration error. These probes alter only the test invocation and create their synthetic source under `build/`.

## Living weapons

```powershell
./gradlew.bat test --tests '*Living*' --tests '*LuxUmbra*' --tests '*Gloam*' --tests '*Thermal*' runLivingWeaponGameTest compileJava processResources
```

The isolated server uses `build/living-weapons-gametest`. Its weapon scenarios cover saved spear charge, failed and uncooled hits, primary and nearby burst damage, ally exclusion, axe swing duration and pool timing, and Blood Bolt impact scaling and bounded chaining. JVM checks cover the hand curves, three-cut geometry, spherical Lux extent, and torch fan motion and visibility.

For visual acceptance, inspect partial and full spear charge in hand, inventory, on the ground, and during the Living Staff morph. Check the axe swing in both hands and from another player, three separate Umbral cuts, and the torch's full fan from both ends. With paired claws, check alternating arms on clicks and held-attack repeats in first person, third person, and from another player, including the left-handed setting. An unrelated offhand item should keep its normal behavior. Shader compilation and server tests do not establish those views.

## Unstained weapon animations

Run `./gradlew.bat -I tools/living_weapon_validation.init.gradle test --tests '*UnstainedWeapon*' --tests '*LivingAxePlayerPose*' runLivingWeaponGameTest` for pose curves and synchronized swing durations. The dagger should start with its blade pointing down and lift its tip upward and outward ahead of the hilt, the glaive should cross a broad horizontal fan, and the hammer should visibly lift its head before the downstroke while retaining the Living Axe's timing and third-person pose. Check first person and third person, both handedness settings, offhand swings, Annetta's dagger, and return to the resting grip. Confirm the existing hit effects and weapon mechanics during combat.

## Phlegethontic Nether

```powershell
./gradlew.bat test --tests '*Phlegethontic*' compileGameTestJava
./gradlew.bat runGameTestServer
./gradlew.bat runPhlegethonticValidationServer -PphlegethonticSeed=42 -PphlegethonticRun=fresh-unique-name
./gradlew.bat runPhlegethonticValidationServer -PphlegethonticSeed=42 -PphlegethonticRun=reverse-unique-name -PphlegethonticOrder=reverse
```

Use a new run name for every fresh-world measurement. The validation server uses `build/phlegethontic-worlds/<seed>-<run>` and writes `phlegethontic-validation.json` there. It sets the actual world seed, adds a second TerraBlender Nether region and foreign-namespace Nether biome, generates 576 chunks, audits all feature writes, examines an inner 100-chunk sample, and advances 120 ticks with fluid ticking enabled. Its feature timings combine the queue, Basin materialization, and deferred vein completion per generated chunk; they exclude ore-reservation interception overhead and are not whole-generator overhead benchmarks.

`tools/oneoff/compare_phlegethontic_worlds.py` compares region-file block states in two matching runs. It reports river/vein differences separately from high cavern-surface scab changed by vanilla lava and magma decoration. It requires `nbtlib`. The [validation report](phlegethontic-nether-worldgen/VALIDATION.md) records tested seeds, measured targets, and visual/multiplayer evidence.

`PhlegethonticCavernTest` checks domed overhead clearance, tapering before a protected boundary, and continuous overlapping vaults across negative chunk coordinates. For visual regression, compare fresh worlds at the same seed and camera position; loading an old save does not rebuild its roof.

`PhlegethonticGameTests` is explicitly registered by `DevTestHooks`. It covers real codecs and modifier injection, thermal immunity and Blood Loss, multi-block contact cadence, ticking fluid containment with foreign blocks, full-size spawning, home return, clotting, bucket rejection, tether exclusion/escape/collision/lifetime, piercing projectiles, and persistence. The positive control deliberately spills uncontained ichor to prove that the containment tests advance real fluid ticks. Test entities are removed after the batch.

The three existing combat-targeting cases that spawn beyond their original one-block template now use `combat_targeting_room`. Keeping their complete target footprint in loaded chunks avoids generation-grid-dependent entity-query failures.

The isolated `runPhlegethonticReviewClient` opens `build/phlegethontic-client/saves/Phlegethontic Final Review`; `runPhlegethonticObserverClient` joins `localhost:25566` from a separate game directory. These profiles and the `review.json` driver belong to development only. The driver can send commands to its integrated server, capture through Minecraft's screenshot API, hold/release Sneak, and write server player/tether status. Never point these disposable review profiles at a normal save. Compile before launching the clients and avoid rebuilding dependency JARs while either is running.

### D5 Basin commission collection

Run `./gradlew.bat test --tests '*PhlegethonticCollectionTeachingTest' --tests '*PhlegethonticCommissionDialogueTest'` for the rack-ejection/idle-transfer/non-spinning/main-hand wording contract and report/bearing options. The loaded `ClinicalBloodProgressionGameTests.phlegethonticCommissionChecksBothSourcesAndFiveItems` checks both creature sources, invalid samples, five carried items rather than world cup count, D5/teacher authorization, non-consumption and saved completion.

Native disposable `NetherCommissionD5_20261001` sampled a supplied Excoriated through ordinary syringe use, harvested a supplied five-count cluster in Survival, ejected the actual rack, transferred its sample into an idle Centrifuge and removed it without spinning. Personal Centrifuge mastery was earned through ordinary held Projection with supplied formation/offering materials after a supplied machine correctly refused access. Normal D5 Alchemist dialogue gave a bearing and accepted the report. Parsed item NBT matched across sampling, recovery, report and cold restart; the ledger retained D5 Side 3/3 and the rebuilt sample tooltip fit at 1280x900. No filled vial or commission proof was injected. Rank, tools, teacher, creature, plant cluster, footing and protection were assisted. See Phase 8 for logs and full-gate results.

Subsequent untouched-Nether exploration in that seed matched the bearing, generated a five-count Scyphus cluster and spawned wild Bombardiers. Normal Survival harvesting and assisted repositioning onto the original drop increased the five supplied plants to ten; ordinary syringe use sampled a natural Bombardier without harming or modifying it. The recovered loose vial matched its sampled payload and survived cold restart with all ten plants. No Nether blocks, plants, creatures or filled samples were supplied. Travel/location inspection, player repositioning, tools/rank and protection were assisted. Unaided acquisition/travel/combat, natural Excoriated sampling, a new report with these specimens, other seeds/viewports and multiplayer remain open. No full gate was rerun for this runtime-only check.

### D6 Vagrant Mind shared discovery

The clinical suite checks D6/teacher authorization, incomplete-proof refusal, independent paired reports, non-consumption of a carried ganglion and saved-data restoration. Native validation must additionally enter a naturally generated Mind before referral, retain discovery across a full restart, navigate both reports normally, compare full inventory components and inspect the D6 Side ledger without completing Main work.

The 2026-10-01 seed 42061004 replay recorded the natural interior visit and biology at D5; spectator inspection did not grant either. Both survived cold reopening and supported normal Mnemonist/Alchemist reports at supplied D6. The Mnemonist's native bearing matched the natural candidate. Full inventory NBT matched before, between and after reports and after another cold restart; capacity and memory/loadout state were unchanged. The native 1280x900 ledger retained completed Side 4/4 beside Main 0/3. Location finding, travel, rank, teachers and protection were assisted; no End structure, organism, unique item or proof was supplied. Unaided travel/pacing, other seeds/viewports, worst-case synchronous lookup cost, alternate teachers and multiplayer remain open. See Phase 8 for artifacts. No production code changed or full gate was rerun.

### D5 Deep Dark bearing

`DeepDarkCommissionDialogueTest` checks that an unfinished D5 commission offers guidance before or after personal analysis, without unlocking an unproved report. `AntecedentGameTests.deepDarkBearingValidatesTeacherAndGrantsNoAnalysis` exercises packet dispatch with wrong/remote teachers, D4, inactive blood, a valid D5 request and a retired completed commission. Guidance must not change persistent proof or grant personal Ahaematic analysis. The existing prior-analysis report test continues to check non-consumption and saved completion.

Native acceptance must request the bearing through normal Alchemist dialogue, confirm the candidate belongs to Overworld Deep Dark, inspect its coordinate/no-safe-entrance warning and ledger hint, and verify no report/analysis proof appears. Check another-dimension and missing-candidate replies separately. The lookup is synchronous and bounded to 2,048 blocks; one fast result does not establish worst-case responsiveness, unaided travel or multiplayer.

The 2026-10-01 seed 42061004 replay sampled a naturally generated catalyst near that bearing through ordinary Survival syringe use, retained the catalyst and recovered the actual loose sample through an idle Centrifuge without spinning. Normal microscope use at supplied D3 identified it and earned personal analysis. Cold reopening retained that work; normal Alchemist dialogue at later supplied D5 acknowledged it and accepted the report. Parsed full inventory NBT matched after analysis, both cold reopens and report. The native 1280x900 ledger retained D5 Side 2/2 beside Main 0/2. The independent Vigil inquiry remains unfinished. Travel/location finding, rank, protection, tools and teacher/platform were assisted; no catalyst, terrain, filled sample or proof was supplied. Unaided pacing/acquisition, alternate teachers, other seeds/viewports and multiplayer remain open. See Phase 8 for artifacts and excluded navigation attempts. No production source changed or full gate was rerun.

### Escharian Scyphus colonies

Run `./gradlew.bat test --tests '*Escharian*' compileGameTestJava` for the five authored count models and rotations, deterministic count variation, patch budgets, contour connectivity, attachment, 50–70% patch coverage, complete dark-face pile coverage, solid tapered pile dimensions, ground-only rims, surface-following infested foundations and stone transitions, obstruction rejection, legacy configuration, and anchor preference checks. The runtime acceptance test stacks Scyphus from one through five on all six faces in dry and waterlogged states, checks count-aware shapes and drops, rejects a sixth placement, and repeats support-loss and white-rim checks.

Run `./gradlew.bat runPhlegethonticValidationServer -PphlegethonticSeed=20260912 -PphlegethonticRun=UNIQUE` using a new run name. The disposable server tests actual BlockItem placement on all six faces, dry and waterlogged, selection thickness, rotation/mirror, empty-hand loot, replanting, support-loss drops, and white-rim placement rejection. The fresh Nether report scans 576 chunks for plant directions, backing validity, ground rims, and sample ichor proximity. It requires wall colonies, ceiling colonies, and ground piles for this seed. Every generated colony must have infested and regular venous stone nearby, and every ground-pile base must rest on infested venous stone. The original 100-chunk terrain/fluid checks remain.

In the disposable review client, first enter spectator mode and teleport to `(1010,206,1000)` in the Nether. Wait for the area to load; the item-entity drop checks require loaded chunks. Then write `{"sequence":1500,"operation":"escharian"}` to `build/phlegethontic-client/review.json`. This also runs the attachment/harvest checks with the real player, then places six orientation fixtures at X 1000–1020, Y 202, Z 1000 and four production-layout ground piles at X 1000, 1018, 1036, and 1054, Y 200, Z 1020. Inspect from above and below; the cup base must touch the support and its opening face away. Inspect natural colonies against uneven walls and ceilings using coordinates from the validation report. Capture screenshots through the driver's `screenshot` operation. Existing chunks cannot demonstrate the replacement generator.

## Automatic Harbinger, Unstained, and Circus journeys

Launch the isolated client below, create or open a disposable world, grant yourself operator permission, and run one of these development-only commands:

```text
/hemo test journey harbinger run
/hemo test journey harbinger run_to_choice
/hemo test journey harbinger run_main_to_choice
/hemo test journey harbinger run_main_to_rest
/hemo test journey unstained run
/hemo test journey unstained cure run
/hemo test journey unstained novitiate run
/hemo test journey circus run
/hemo test journey circus succession run
/hemo test journey circus liberation run
/hemo test journey run_all
```

`unstained run` is the cure alias. `circus run` executes Succession and Liberation using separate pavilion fixtures. `run_all` executes Harbinger, Unstained cure, Unstained novitiate, Circus Succession, then Circus Liberation. A server-tick runner prepares each existing fixture, invokes the same server gameplay hooks exercised by the fixture GameTests, and advances only after the authoritative journey check passes. It restores the captured player snapshot—including Circus progress, known summons, known manipulations, and inventory—and removes journey-owned blocks, entities, drops, and temporary world state after each successful route.

`/hemo test journey harbinger status` or `/hemo test journey unstained status` reports the route, checkpoint, automation state, and any latched failure. Reissuing the matching `run` resumes the current checkpoint. Exceptions and timeouts stop the runner without advancing; the snapshot and current fixture remain available for inspection. `/hemo test clear` cancels the runner, removes its fixtures, and restores the snapshot. Manual `start`, `next`, and `reset` commands stop automation before taking control.

This automation validates server-side progression, stations, rites, dialogue events, observances, assignments, travel, ceremonies, item use, and pickup hooks. It does not validate mouse/key handling, dialogue or HUD rendering, animations, particles, sound, or other client visuals. Use the manual journey commands for those checks.

`run_to_choice` retains the full route but pauses before the D7 ending response. `run_main_to_choice` skips optional research, equipment, and Hermit Road stages and stops at the same choice. `run_main_to_rest` stops the Main route at D2 before Concentrated Blood injection or sleep, retaining the Alchemist, bed, and original snapshot for actual client input. These pauses synchronize the selected hotbar slot after server-side automation. Completed-night acceptance must use a real bed wake; the ordinary runner's synthetic sleep helper is not evidence of that event. Use `status` and manual `next` to inspect and advance, or `clear` to restore the starting state.

The 2026-10-01 fresh `MainOnlyChoice_20261001` replay, seed 42061005, reached D7 Communion through this Main-only runner without earlier optional route completion. Ordinary Spine use ran the real two-minute projection/forced return, and normal `/hemowhisper` listening through earlier pending messages displayed both response options. Cold reopening retained the undecided choice. Saved NBT/advancement inspection verified empty Bestiary/Antecedent, absent expedition reports and incomplete full taxonomy, enzyme, cultivation and equipment projects; incidental collection/enzyme and ledger-receipt credit was present. Fixtures, synthetic sleep and accelerated rites remain assists. This does not establish natural Main-only play, fair combat or either completed ending. See Phase 8 for logs, screenshots and preserved save state.

## Manual Circus journeys

Use the isolated journey client and an operator account:

```text
/hemo test journey circus succession start
/hemo test journey circus liberation start
/hemo test journey circus status
/hemo test journey circus next
/hemo test journey circus reset
/hemo test clear
```

Each route builds a compact pavilion arena with the Ringmaster, carousel, and all four performers. The checkpoints cover generated-site discovery, all performer introductions, Attuned perception, the neutral attention act, explicit route choice, the remaining three Succession acts, both finale paths, carousel captives and anchors, Ringmaster completion, and route rewards. Fixtures accelerate passive acclimation and protect the operator with temporary resistance during automatic combat, but route choice, act startup, encounter phases, target rules, completion, and rewards still use their production server paths.

The discovery checkpoint deliberately requires a real generated pavilion in manual mode: use the Scarlet Waybill or `/locate structure hemomancy:circus_pavilion`, enter it, then return and advance. Automatic mode marks that already-covered discovery hook because a compact fixture is not a registered worldgen structure. At the Attuned and reward checkpoints, pause automation with the matching manual route if you need to judge overlays, models, animation, particles, sound, Crossbar radial behavior, or Thread Ripper aiming. Those are client observations and cannot honestly be passed by headless automation.

## Manual Harbinger journey

Launch the isolated journey client from the project root:

```powershell
./gradlew.bat runAlphaJourneyClient
```

This profile stores its world and client settings under `run-alpha-journey`, separate from the normal development client. On the first launch, select **Singleplayer**, create or open a world, and enter that world before running commands. A new isolated directory has no world to open until you create one.

Run the checkpoint journey as an operator:

```text
/hemo test journey harbinger start
/hemo test journey harbinger status
/hemo test journey harbinger next
/hemo test journey harbinger reset
/hemo test clear
```

`journey harbinger start` captures the player's pre-journey state, resets the player to the starting conditions, and prepares the first checkpoint. At each checkpoint, perform the requested gameplay action, use `journey harbinger status` to inspect the current requirements, then use `journey harbinger next` to verify them and advance. The route runs from Sanguine Initiation through Degree 8 Apotheos. It includes the First Remnant and Vicar report, every rank-up rite, First Separation, The Body Answers, Red Taxonomy, Living Bestiary, Hyphae discovery, Enzyme Mastery, First Culture, Woven Vessel, first Noetic mark recognition, all five Artificer assignments, the Vein-Mason D4-D6 route, Founding Fane, Living Covenant, and Qliphoth Communion. Fixtures supply the exact machine, ingredients, entities, or loadout needed for the next real gameplay trigger; they do not directly award the checkpoint outcome.

The Three Answers also includes its optional Barbed research correspondence. Capture the supplied Barbed Urchin, Desiccant, and Venom-Rib Centipede in separate hotbar jars, ask the marked Alchemist to record each filled jar, then claim the research reward. Weight of the Frame continues after the Archon rite through a real Monolithic Armature upgrade, Edacious inspection, Bloodburst activation, and fitting. After that, consume the nine supplied same-bloom Qliphoth pomes, choose the Eighth Degree in the opened fungal revelation, and invoke the prepared Apotheos grand station. Grand ceremonies are accelerated only after their real station match and activation succeeds.

After the Sanctified rite, run `journey harbinger next` once to enter the Chamber of Will through the real Degree-6 rite visit and again to return. Right-click the supplied Covenant Throne once, then advance to the Covenant Vigil. Invoke the prepared real Vigil with the Living Staff, run `journey harbinger next` to fill its owned anchors, enter inscription, assign the marked Vicar through the bloodline ally service, and fast-complete the ordeal, then run `journey harbinger next` once more to verify both the Vigil and Living Covenant milestones. This deliberately skips the 60-second combat ordeal while retaining station matching, rite activation, helper eligibility, completion rewards, and chapter closure. The dedicated GameTest server does not load the Chamber dimension, so its transition remains a live-client check; throne binding, Vigil activation/completion, helper rewards, and exact restoration of respawn and Chamber flags are automated. Any original bloodline, per-dimension Fane records, respawn binding, Chamber attunement flags, Muscle Memory state, recipe-book knowledge, Living Bestiary catalogue, Artificer persistent assignment keys, full initiatory-degree state, and base blood tendency are restored on reset/clear. If verification fails, remain at that checkpoint, correct the unmet requirement, and run `journey harbinger next` again. After all checkpoints pass and the journey reports `complete`, run `journey harbinger next` once more to remove the fixtures and restore the captured state automatically.

The route performs the real Living Staff blood-structure craft and all Artificer, Mnemonist, Alchemist, Vicar, and Vein-Mason progression used by the chosen Barbed/Edacious path. Discovery coverage includes a loaded blood-echo inscription, item-pickup discovery, dialogue, rite, degree, and advancement-backed Liber unlocks exercised naturally by the route. It does not enumerate all 30 authored inscriptions or alternate armor forks; those share the tested loaders and trigger paths. The current dedicated server suite registers 146 required GameTests. The remaining completion gate is the live-client smoke pass, especially dialogue screens, keybind-driven actions, the Chamber transition, rendering, and the full command-to-command operator flow.

`journey harbinger reset` removes journey-owned fixture output, clears active potion effects acquired during the run (including Blood Drunkenness), and restarts at the first checkpoint while retaining the original snapshot. `/hemo test clear` exits either journey, removes its fixtures, and restores the snapshot captured by its `start` command. Run it before returning to other manual testing. `alphaCheck` remains the automated JVM and dedicated GameTest gate; this isolated client workflow is the operator-driven complement, not a replacement.

The controller stores the fixture dimension with its origin. Invoking `next`, `reset`, or `clear` after traveling to another dimension still operates on the original fixture level; stage transitions and resets return the player there safely. `status` inspects that stored level without moving the player. If the saved dimension is unavailable, the command reports its exact resource key instead of touching the current dimension.

## Manual UNSTAINED journey

Use the same isolated client and an operator account:

```text
/hemo test journey unstained cure start
/hemo test journey unstained novitiate start
/hemo test journey unstained start
/hemo test journey unstained status
/hemo test journey unstained next
/hemo test journey unstained reset
/hemo test clear
```

The cure route proceeds from genuine blood suppression through degree-scaled Lethean Baptism, treatment Observances, full Purity, irreversible Closed Vein cleansing, Clarity preparation, and the Clarity Ascension pledge. The novitiate route exercises all five healthy service vows with the real Retort, Condenser, consecration, protective rite, Podium, and pledge actions. Both routes retain snapshot restoration; the original `journey unstained start` remains a cure-route alias. Post-pledge checkpoints continue through Glass Lungs, the Pale Vigil, Moon-Washed Copper, the Pale Watch, Resolute Still Arts, Enlightenment, and Lethean Font.

The fixtures shorten passive Purity and Clarity accumulation by positioning the player immediately before the next proof. They do not claim the proof: the player must still use the real Podium, complete the real rite, work the real Condenser, interact with the correct NPC dialogue, surrender the required offering, and receive the real milestone reward. Failed verification stays on the current checkpoint. A failed fixture transition latches the completed checkpoint so retrying `journey unstained next` does not consume or perform it twice.

## Field mycology smoke test

1. Find or place each Red Taxonomy plant and break it in Survival. Infected Fungus, Stinkhorn Fungus, Puffball Fungus, Sarcodes, Rafflesia, and Devil's Tooth must drop themselves. Bleeding Heart must drop one to three Bleeding Bulbs normally and the plant itself with Silk Touch. Repeat representative breaks with explosions and confirm the declared survival/decay behavior.
2. Break potted Ghost Pipe, Sarcodes, and Lethean Poppy. Each must return both the flower pot and its contained plant.
3. Submit a first unique Red Taxonomy specimen to the Alchemist with no sampling kit. Confirm exactly one empty Blood Vial is delivered, then submit a repeat and a second unique specimen and confirm neither duplicates that first-submission vial. Four unique specimens may still complete the optional catalogue.
4. Confirm the Alchemist and Assignment Ledger explain the Bleeding Bulb, Foul Paste, and Spore Sac lanes, including the Fungling sample to Infected Fungus route.
5. Craft Infested Wood from one log and one Foul Paste. Leave air above it at brightness 7 or lower and wait for random growth; only Infected Fungus, Hyphae, or Stinkhorn Fungus may appear. Occupy the block above and confirm the growth never replaces it.
6. Distill Devil's Tooth and confirm it produces two Foul Paste. Use JEI to follow Foul Paste into Infested Wood/Befouling Ash and Spore Sac into spores/Hyphal Substrate.

## In-game scenario commands

## Chamber progression and perspective checks

Use `/hemo chamber theme next [player]`, `previous`, and `cycle` to preview normal themes without player-eligibility filtering; use `set <theme>` to select any registered preview, including `vesper_fight` and `mycophant_nursery`, and `reset` to clear it. Use `/hemo chamber size set <radius> [player]` to override the player's accessible chamber radius from 3 through 10; `/hemo chamber size reset [player]` returns to progression sizing. The Orb of Perspective must never select either encounter theme.

For a live progression pass, enter the Chamber and move through tier radii 4, 6, 8, and 10. Confirm each newly unlocked band appears in the same server tick, the previous Sporitic Crystal corners move outward without replacing blocks substituted by the player, and placement, movement clamp, item rescue, safe return, and client border all follow the same radius. Repeat a progression change outside the Chamber, then enter and confirm heartbeat/login recovery builds the missing band.

Craft the Orb with `MEM / EBE / MEM` (Monolith Fragment, Echo Shard, Blood Crystal Shard). Throw it beyond the platform and below floor Y minus 3. Verify stable progression-filtered cycling, persistence after logout/reload, rejection outside the owner's cell and during both encounters, one activation per toss, inventory-first return, and a beside-owner return with normal pickup delay when inventory is full. Also test two separated player cells and logout immediately after throwing.

Development client/server runs add the following operator-only commands beneath the existing `/hemo` root:

```text
/hemo test list
/hemo test setup <scenario>
/hemo test verify <scenario>
/hemo test run <scenario>
/hemo test run_all
/hemo test status
/hemo test clear
```

`setup` prepares a fixture without immediately checking it, which leaves room to interact with a GUI, NPC, structure, or item manually. `verify` checks the prepared outcome. `run` performs setup and verification immediately for state-driven scenarios. `clear` removes the active fixture and only clears equipment/state owned by that fixture.

`run_all` clears any active fixture, then runs every registered scenario in catalogue order. Each scenario is cleaned up before the next begins. It continues after failures, prints each result, and ends with a passed/total summary plus the failed scenario ids.

The initial catalog is:

- `blood_structure_locked` — degree 5 must not satisfy the degree-6 Covenant Throne recipe.
- `blood_structure_unlocked` — degree 6 must satisfy the same recipe.
- `artificer_assignment_ready` — a briefed player wearing complete Hematic Iron makes the Worn Vow fitting available.
- `ArtificerProgressionGameTests` covers ordered D2 inspection, all three recorded fork reagents and real set responses, Blood Lust and Living Arsenal gameplay hooks, and all four D7 material/registered-ability routes against loaded registries.
- `artificer_reward_claimed` — repeated reward-claim marking remains idempotent.

- `uninitiated_cannot_pass_bloodcraft_degree_gate` — a Degree-0 player is rejected by the loaded degree-6 Covenant Throne bloodcraft gate.
- `sanguine_initiation_recipe_loaded` — the Sanguine Initiation recipe is available from the loaded server registry.
- `sanguine_initiation_degree_mapping` — Sanguine Initiation retains its Degree-1 rank-up mapping and registered Sanguine Conduit reward.

These are deliberately narrow pilots. They prove the harness through crafting locks, progression boundaries, assignment readiness, and reward claim state before more expensive end-to-end scenarios are added.

## Adding a scenario

1. Add one `HemoTestScenario` to `HemoTestScenarioCatalog` with a stable snake-case id, a focused setup action, one verification action, and cleanup limited to the fixture's own state.
2. Add a matching method to `HarbingerPilotGameTests` (or a new focused GameTest class) so it runs headlessly.
3. If manual interaction is useful, keep setup and verification separate so `/hemo test setup` can pause at the exact gameplay boundary under test.
4. Add or update a JVM contract for pure rules and resource shape. GameTests should cover integration that genuinely requires loaded registries, recipes, advancements, attachments, blocks, entities, or server ticks.
5. Run `./gradlew.bat alphaCheck`.

Avoid sharing mutable fixture state between tests. Prefer throwaway players and explicit cleanup, and assert observable outcomes rather than implementation details when the real gameplay API is available.

## Phlegethontic Bombardier ecology

1. Locate newly generated Phlegethontic Basin terrain and confirm Bombardiers spawn only with Escharian Overgrowth directly beneath them.
2. Approach a camouflaged specimen. Confirm it remains visible and outlineable, rises into its warning pose, and does not become angry or attack.
3. Strike one specimen. Confirm only Bombardiers bound to the same connected outcropping retaliate.
4. Check the flame from its abdominal nozzle at the sweep edge, behind the creature, and behind solid cover. Confirm the 30-tick wind-up, final eight-tick aim lock, four discrete pulses, and 16-tick sweep.
5. Observe cooling both on and away from Overgrowth. It should recharge in 120 ticks on habitat and 240 ticks elsewhere while trying to return to its outcropping.
6. Reload once during anger and once during cooling. Confirm anger persists and an interrupted wind-up or firing state resumes as cooling rather than duplicating an attack.
7. Snapshot the outcropping before firing, pathfinding, grazing, and cooling. Confirm no block state changes and no terrain ignition.
8. Compare the spray beside a Flammeus manipulation. Confirm the shared pinkish-red and black-tipped flame language, with no vanilla-orange fallback.
9. Inspect `latest.log` for missing entity models, textures, biome modifiers, loot tables, or client-only classloading errors.

The dedicated `PhlegethonticBombardierGameTests` batch covers colony isolation, wind-up safety, front/rear cone selection, cooling transition, and the no-mutation invariant.

## Unstained progression smoke test

1. Begin purification and talk to an Acolyte, Zealot, and Guardian as their Observances become available. Verify the ledger groups all nine assignments under the correct directing office and the Book of Observances is only granted once and survives relog/death.
2. Reopen the matching NPC dialogue with each required offering. Verify it is consumed once, the reward is granted once, and the journal marks the assignment complete.
3. At Clarity 49, verify Glass Lungs is blocked; at 50 it may start and yields a Lethean Chalice. At Clarity 74, verify Moon-Washed Copper is blocked; at 75 it yields a Pale Silver Bell.
4. Verify a Still Art cannot be learned below its declared Clarity stage, including through a rite reward, then verify stage advancement backfills it.
5. Place a Stillwater Condenser beside source water and within four blocks of Ghost Pipe. Below 50 Purity its menu must remain locked. At 50, open its two-slot screen, insert glass bottles, and verify the water/Ghost Pipe indicators and progress channel produce Lethean Dew. A Verdigris Lattice within five blocks must light the lattice indicator, halve processing time, and double output.
6. Stand near a Verdigris Lattice as an Unstained player and verify Resistance. Spawn a tagged Hemomancy creature and verify Weakness and Slowness after a random tick.
7. Confirm no active recipe, dialogue inquiry, or registered item references `pale_silver_pickaxe` or `verdigris_censer`.

## Cicatrix Anchorite D4-D6 smoke test

1. Complete the existing D4 scar lesson, lose an unlearned issued pattern, and verify replacement is offered once no matching template remains in inventory.
2. At D5, use a Thelemic Memory to enter Varicose and confirm physical damage and Noetic casting do not satisfy the milestone. Receive diagnosis, inspect exact health and routed-memory tooltips, then use Salve or Poultice and complete Hematic Fortification.
3. Claim the tier-two reward twice and verify only one reward is granted. Confirm a previously completed Fortification receives automatic credit.
4. At D6, receive the referral, obtain Mnemonist counsel, cast a non-mechanical Noetic Memory matching an active cerebral scar, commit a different Effigy set, and cast another matching Noetic Memory.
5. Relog with active scars and verify effective alignment is unchanged before and after the relog. Remove and re-equip scars and verify saved base alignment never drifts.
6. Inspect the separate collapsible D5 and D6 ledger cards and both return-ready toasts. Confirm the tier-three reward warns that Deep Inscription remains required.

## Hematic Succession validation (2026-09-17)

Run the isolated server suite and affected JVM tests:

```powershell
.\gradlew.bat runSuccessionGameTestServer test --tests '*SuccessionLedgerTest' --tests '*Bloodline*' --tests '*CardinalRite*' --tests '*HarbingerRecruitment*' --tests '*BloodSample*' --tests '*BloodInjection*' assemble --no-daemon
```

The current run passed **17 required succession GameTests** and **312 JVM tests in 68 classes**, and produced the mod jar. The server suite checks the real effigy manufacturing path, normal activation and staff escrow, creation/death/restoration, reserve and history retention, counterfeit rejection, failed restoration retry, early legacy refusal, remnant reissue/proximity, serialized rite continuation, competing workplace reservations, displacement/replacement, changed offerings, dissolution, all five profession trees, consent/donation access, self-sampling, all five malformed attacks, and committed-effigy loot. Pure ledger tests also cover identity locks and remnant generations across serialization.

Server reports: `build/succession-final-check.log`; JVM reports: `build/reports/tests/test/index.html`. These checks do not simulate an operating-system crash in the middle of Minecraft's world save. Normal rite serialization/reload is covered; abrupt multi-file save failure is not.

Live review runs use disposable `build/succession-client` and `build/succession-observer` directories. `runSuccessionReviewClient` enables an explicit fixture assist in `src/gameTest`; `runSuccessionObserverClient` connects to localhost:25568. The review setup grants D5, a bloodline/fane, five example residents, and a prepared but unperformed rite. It is not evidence of earned progression. Production activation is then used to run the full timed rite. Do not rebuild the shared development class directory while either review client is running; finish compilation first, then launch the observer directly from the generated JVM/program argument files. A second Gradle invocation also rewrites the included HutosLib jar, so merely excluding compilation is insufficient.

The unrelated broad client resource warnings for old missing models/textures remain separate from succession results. Full long-session playtesting, every professional assignment from start to reward, and power-loss recovery are not claimed by this focused suite.


Live acceptance evidence is stored under `build/succession-client/screenshots/` and `build/succession-observer/screenshots/`. A real timed activation completed with six authoritative residents from a five-resident assisted baseline and zero remaining locks. The observer connected as `SucObserver`, received phase/progress updates and effects, and rendered the five malformed professional variants. The host received their fractured subtitles and combat effects. A clean disconnect wrote `hemomancy_succession.dat`; reopening retained all six resident records and the generated resident entity. The Residents view was inspected after correcting a menu-blur issue. The three in-world offering labels were inspected after correcting their horizontal rendering scale; the final capture is `build/succession-client/screenshots/succession-offering-labels-final.png`. Final packaging and that client review are recorded in `build/succession-visual-final.log`. This is focused two-client presentation validation, not an exhaustive multiplayer progression playthrough.


## Clinical Blood Tools progression (2026-09-17)

Run `./gradlew.bat runClinicalGameTestServer runBloodInjectionGameTestServer` for the clinical progression and existing specimen-machine suites. Their worlds live under `build/clinical-gametest` and `build/blood-injection-gametest`.

The clinical suite covers D1 First Separation availability, held-vial and teacher validation, one-time recipe/Liber rewards, real microscope completion and interruption, duplicate and previously identified source handling, successful versus rejected Cabinet transfers, crafting/referral prerequisites, D2/D3 mnemonic boundaries, safe inquiry hints, and rejection of untaught injection. Existing injection fixtures explicitly know Borrowed Physiology so their transaction and animation checks continue to exercise injection mechanics.

Verification for this change: 31 focused JUnit tests passed; five directly invoked legacy assignment/atlas checks passed; all 5 clinical and all 71 blood-tool server GameTests passed. Changed JSON parsed, clinical inquiry translations resolved, and `git diff --check` passed. This is focused validation, not a full `alphaCheck` run.

Live checks remain: review NPC quest tabs and attention markers, ledger scrolling and new cards, Liber page layout, JEI hide/show after each lesson and after reconnecting, and the D1-to-D3 sequence with two clients. Check an existing save by showing a filled vial, reexamining an already identified sample, and completing a manual Cabinet deposit/withdrawal. Verify progress after death, relogging, and changing dimension. Expanded ledger step titles and descriptions must fit their portrait-offset cards; hover truncated lines to verify the full wording. The D2 Gardens survey specifically inspects Infected Fungus and Puffball Fungus without consuming them. Its native 1280x900 report/restart/title-fit evidence is recorded in the progression Phase 8 handoff, not a sign-off for all cards, locales or viewports.


### Automatic rite helper travel (2026-09-17)

The follow-up helper travel run passed 22 succession GameTests and 308 focused JVM tests (`*CardinalRite*`, `*Bloodline*`, `*Succession*`), with compilation and `assemble`. Evidence: `build/helper-travel-final.log` and `build/reports/tests/test/index.html`.

Five added server tests cover exact three-helper gathering and distinct stations; a player replacing an NPC and leaving during preparation; competing rites and caster membership departure; persisted entity return and Bloodspent exclusion; and travel to/from a fane in another dimension with the same UUID. Cross-dimension fixtures use distinct workplace coordinates so earlier saved test residents cannot own their stations. Existing termination paths return helpers on completion, cancellation, collapse, and caster death; disconnect expiry now explicitly returns them too. Temporary chunk tickets expire, and missing loaded entities are never reconstructed as substitutes. This follow-up did not repeat live client or two-client visual review.


## Antecedent Inquiry

The [September 19 documentation audit](WORKTREE_REFERENCE_AUDIT_2026-09-19.md) maps the complete Git-visible dirty checkout to subsystem documentation. Documentation/link checks are separate from the dated runtime results below; no fresh full-suite pass is implied.

Run `./gradlew.bat runAntecedentGameTestServer` for the isolated `antecedent_validation` namespace. It covers both specimen provenances, microscope completion and blood exclusions, unknown/legacy preservation, Vanity socket item conservation and component persistence, ancient machine save/stop behavior, a complete personal replay with an interrupted observer, fixture delay/removal, all template rotations, and twenty generated city starts covering all center variants and rotations with no neighboring-room overlap.

Run `./gradlew.bat test --tests '*SpectrogramAnalysisTest' --tests '*AntecedentResearchTest'` for silence, frequency-band discrimination, pitch timing, opposite-phase stereo, evidence persistence, and continuous observation windows.

`./gradlew.bat runAntecedentReviewClient` launches an opt-in disposable client under `build/antecedent-client`. `GameplayCampaignDriver` supplies normal UI operations; `antecedent-review.json` accepts fixture setup, play, ordinary playback, microscope, gallery, entrance, archive, mute, reload, and spectrum inspection. Fixture assists are not evidence of natural survival progression. Do not rebuild the composite HutosLib jar while a review client is running: the live resource pack holds its ZIP open.

Live acceptance should include at least GUI scales 2–4, resource-pack replacement and reload, two simultaneously playing machines, a remote listener entering during playback, mixed mute settings, survival travel through the city, death/dimension/save recovery, and the Fungal epilogue competing with other screens. Inspect captions and the 55–68-second silent interval independently from the fictional trace.

### Antecedent validation record (2026-09-17)

Final focused verification passed all **10 Antecedent GameTests** and **7 JVM tests**, followed by successful packaging. The audio tests include complete-frame waveform peaks at 96 kHz. Command: `./gradlew.bat runAntecedentGameTestServer test --tests '*SpectrogramAnalysisTest' --tests '*AntecedentResearchTest' build --console=plain`. Evidence: `build/antecedent-final-result.log`; artifact: `build/libs/hemomancy-6.0.1-neoforge.1.21.1.0.jar`.

The disposable client rendered ordinary creature playback, the Severed Record, the Incertae microscope view, and the Vigil entrance, gallery, and archive. At 65 seconds of muted Severed playback, decoded spectrum energy was exactly zero while the separate unresolved trace and specimen response remained visible. Evidence: `build/antecedent/live-review-before-pack.log` and `build/antecedent-client/screenshots/clairaudiograph-silent-verified.png`.

A resource pack replaced every Pig ambient variant with a 1 kHz tone, then a 2 kHz tone. The strongest displayed analysis band changed from approximately 963 Hz to 1931 Hz after a resource reload, consistent with the logarithmic band centers. Captures and measured results are in `build/antecedent-client/screenshots/spectrograph-pack-1000hz.png`, `spectrograph-pack-2000hz.png`, and `build/antecedent/spectrum-pack-*.txt`. This verifies analysis of the resolved resource audio and reload invalidation. Decoding the shipped 73-second Severed recording also confirmed zero PCM amplitude in the sampled silent interval and final recorded second; see `build/antecedent/audio-verification.json`.

The broader repository checks are not green: `build/antecedent-validation.log` records 2093 JVM tests with one failure in `MorphlingLumenlaceRenameResourceTest`, caused by an existing non-UTF-8 documentation file. `build/antecedent-runtime-validation.log` records 476 GameTests with 12 failures outside the Antecedent namespace, including existing clinical, rite-helper, combat, worldgen, and barrier-fixture checks. Those failures were not suppressed, and their unrelated files were left intact. A clean-checkout baseline comparison was not performed.

The live checks used fixture assistance. They do not establish an uninterrupted survival playthrough, remote multiplayer behavior, every GUI scale, or epilogue arbitration with other screens. The recordings use original synthetic voices; a full listening and performance-quality review remains a separate acceptance step.

### Chamber terrain and brick restoration (2026-09-18)

`runAntecedentGameTestServer` passed all ten required tests after adding `MixinVigilTerrain`. The twenty-city test compares Minecraft's Beardifier density with and without the appended Vigil, sampling inside and around its bounds across all center variants and rotations. It failed before the fix because the Vigil changed density, then passed with only the Vigil excluded from terrain carving. Evidence: `build/antecedent-lava-validation.log`. Reservation, fixed rotation/placement, and unrelated city pieces retain their existing behavior.

The subsequent wall-material restoration regenerated `vigil.nbt` and compared decoded NBT before and after: 9,606 authored positions retained, 2,993 masonry replacements, and unchanged air, fixtures, block-entity data, and dimensions. It retained the terrain mixin. This material-only check did not repeat the full server suite. New natural terrain appearance, nearby natural lava, and survival traversal still require a fresh client worldgen review; existing chunks are not repaired by either change.


### Compact Vigil and unchanged city generation (2026-09-19)

The four-piece annex replaces the advance reservation. `runAntecedentGameTestServer` passed all **10 required tests**; `VigilLayoutTest` and `AntecedentResearchTest` passed **5 JVM tests**. Evidence: `build/antecedent-compact-validation.log` and `build/test-results/test/`. `assemble` also passed (`build/antecedent-compact-package.log`); the release jar contains all four current NBT pieces and the updated mixin configuration.

The twenty-seed sweep covers all three center templates and four rotations. It compares every vanilla piece's complete serialized data with an otherwise identical city generated without the annex hook. All twenty vanilla layouts matched. Three accepted a complete compact Vigil; seventeen skipped it because no safe attachment fit. This is a small regression sample, not a measured worldwide spawn rate. Accepted pieces preserve authored city blocks, remain above the bedrock margin, match the navigable composite including rotated block states and block-entity data, and leave surrounding Beardifier density unchanged. Occupying accepted sites eventually makes the search skip placement.

`python tools/oneoff/antecedent/check_vigil.py` checks all 5,186 authored blocks across four lossless template partitions, 503 reachable player positions, and access to every lectern, chest, bell, and vessel. Rotation tests cover the compact controller's fixtures, save/load, client layout tag, and legacy fallback. Existing generated sites retain their old coordinates.

Fresh client review of natural attachments and terrain appearance remains outstanding. The earlier assisted client captures document the original larger chamber, not this compact layout. These changes apply to newly generated cities and do not repair existing chunks.


### Reproducible natural Vigil location (2026-09-19)

Default/Normal world seed **42** accepts an Ancient City at chunk **(-132, 100)** with a compact Vigil entrance at **(-2073, -45, 1562)** (standing position). Teleport: `/tp @s -2072.5 -45 1562.5`. Portal bounding-box center: **(-2112, -37, 1596)**. Use the current mod build and fresh chunks.

`normalWorldSeedHasNaturallyPlacedVigil` uses the Normal overworld noise generator, seed-specific noise, the registered Ancient City random-spread placement, and the structure's real biome predicate. This is separate from the earlier forced-start layout sweep, whose coordinates were not natural-world examples. The natural search found this annex at its second biome-valid city; all **11 Antecedent GameTests passed**. Evidence: `build/antecedent-natural-seed.log` (`NATURAL_VIGIL`). This verifies natural structure selection and annex placement data, not a live client visual review. Biome/worldgen mods or datapacks that change city placement can alter the result.


### Self-contained modular Vigil (2026-09-19)

The current generator uses six four-module arrangements and scans exposed floor edges throughout the city. It supersedes the compact layout's 64-block portal restriction and the earlier natural seed example above. Existing generated structures remain unchanged.

`python tools/oneoff/antecedent/check_modular_vigil.py` verifies all six serialized arrangements: 4,744 authored positions each, four non-overlapping modules equivalent to their composite, 451 reachable standing positions, and all five lecterns, two chests, bell, vessel, and fixture identities retained. The dedicated server suite places all six layouts in all four rotations and checks controller origins, fixtures, the guaranteed recording, saved versions, and client layout tags. The original compact rotation/save test remains in the suite for compatibility.

The same twenty-city regression sample now accepts a complete Vigil in **20/20** cities, compared with **3/20** before this change. Every vanilla piece's serialized data remains identical to the unmodified generation baseline, and terrain-density checks still pass. This is a regression sample, not a promised universal spawn rate. Some entrances lie outside the old portal radius. Impossible height bounds reject the complete site.

Normal-world seed **42** now selects a modular Vigil at entrance **(741, -45, -835)**, in the city at chunk **(51, -47)**. Teleport in fresh chunks: `/tp @s 741.5 -45 -834.5`. Portal center: **(816, -37, -756)**. The natural search uses normal noise, random-spread placement and actual biome rules. Changed mod/datapack worldgen can change this result.

Final verification passed **12 required GameTests**, **6 JVM tests**, and **assemble**. The release jar contains six layout definitions, all 24 module templates, and six matching composites. Validation evidence: `build/antecedent-modular-validation.log`; final packaging/check evidence: `build/antecedent-modular-final.log`. Client appearance and a continuous survival playthrough of the modular sites remain unverified. Vicar dialogue and the recognition journal now direct players to enclosed wool-lined entrances along the city's roads instead of behind the portal.


### Vigil archive doorway correction (2026-09-19)

The modular archive now uses a one-block-wide, two-block-high doorway with deepslate-brick jambs and a lintel. Both door halves face into the archive; the warning sign faces approaching players from the gallery side, supported by that lintel. Fixture coordinates and saved layout versions remain unchanged. Existing generated blocks are not rewritten.

`check_modular_vigil.py` now rejects missing jambs/lintels and misplaced or reversed signs, in addition to testing routes. It failed on the previous open doorway, then passed all six corrected arrangements with 447 reachable positions each and all interactive content accessible. Runtime/packaging evidence: `build/antecedent-door-validation.log`.
