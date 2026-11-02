# Somatic weaving, scars and Anchorite teachings

[Execution index and dependency register](README.md) · [Live findings](../HARBINGER_LIVE_GAMEPLAY_FINDINGS.md) · [Evidence journal](../HARBINGER_GAMEPLAY_CAMPAIGN.md)

Status: recommended work, not implemented by this document. Priority and evidence type are explicit per task. **Requires** blocks dependent implementation. **Acceptance prerequisites** block a meaningful final gameplay verdict, not independent coding or local unit checks. Both are linked in the central register.

Keep weaving and scar fitting as distinct physical practices. Make output choice, learning, fitting, strain and recovery understandable and persistent.

## Tasks

<a id="mem-01"></a>

### MEM-01: Pin an explicit Loom recipe and protect the first weave

**Priority:** High. **Evidence:** Confirmed defect.

**Observed basis:** Adding enzymes silently changed Blood Shot into 400-blood Halo, which required Blood Shot mastery 4. The first-weave chapter still advanced despite the output being unusable.

**Requires:** None.

**Acceptance prerequisites:**

- [DIA-01](02-dialogue-and-rewards.md#dia-01) — The real first-memory task handoff must work.
- [RUN-01](01-runtime-and-controls.md#run-01) — Dedicated learning from the brazier must be safe.

**Unblocks:** [MEM-02](05-weaving-scars-and-teachings.md#mem-02) (implementation)

**Recommended code work:** Choose a recipe through the existing interaction/UI surface and retain its ID independently of current enzyme reservoir size. Before charging, resolve catalysts, mastery/degree eligibility, base cost and possible drift cost. Keep ordinary crafting of future-use memories possible if intentionally supported, but present that as a deliberate choice; the introductory objective must request and credit the intended usable memory. Do not use list order as hidden selection.

**Recommended gameplay behavior:** Show output, blood price, enzyme minimum and learning prerequisite before payment. Teach Staff preparation and fixed-distance dragging before strands begin drifting. Provision a small explicit tutorial allowance for the demonstrated drift cost, not unlimited free weaving.

**Acceptance:** Stock 1 then 20 matching enzymes; output remains the selected Blood Shot. Deliberately select Halo and see its price/learning lock. Complete the intended first memory, learn it through the brazier and cast it; wrong output cannot falsely finish the lesson.

**Current code/resource entry points:** [SomaticLoomBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/SomaticLoomBlockEntity.java), [SomaticLoomInteractionEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/event/SomaticLoomInteractionEvents.java), [DialogueEventHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java)

<a id="mem-02"></a>

### MEM-02: Persist and recover an active weave

**Priority:** High. **Evidence:** Confirmed defect.

**Observed basis:** A saved phase-2 weave reloaded without the recipe/configuration and failed. Cancellation spent blood/enzymes; output must not duplicate on recovery.

**Requires:**

- [MEM-01](05-weaving-scars-and-teachings.md#mem-01) — Persist the explicit selected-recipe contract rather than the old reservoir-driven recipe inference.

**Acceptance prerequisites:** None.

**Unblocks:** [MEM-05](05-weaving-scars-and-teachings.md#mem-05) (acceptance), [VAL-01](09-validation-and-execution.md#val-01) (implementation), [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Persist the chosen recipe ID, committed inputs, enzyme spent, current strand state and phase in the existing block entity save format. Load inventory before revalidating the saved weave, so recipe refresh cannot overwrite it. Keep spent and available resources separate. On a genuinely missing/changed recipe provide a defined cancellation/recovery result using stored commitments; never invent completion or refund already delivered output. Preserve existing saved worlds with a conservative versioned read path.

**Recommended gameplay behavior:** Players can leave and return to a weaving project. Explain what cancelling loses and what remains in the station before accepting the cancellation.

**Acceptance:** Reload, unload/reload chunk and server restart at uncharged, charged, partly pulled, last strand and completed-output stages. Check exact resources and output count. Test one removed-recipe data reload with an explicit safe failure message.

**Current code/resource entry points:** [SomaticLoomBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/SomaticLoomBlockEntity.java), [SomaticLoomInteractionEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/event/SomaticLoomInteractionEvents.java)

<a id="mem-03"></a>

### MEM-03: Clarify scar carving, learning and fitting

**Priority:** High. **Evidence:** Confirmed teaching friction.

**Observed basis:** The generated Scarring Station rejected the new student until personally crafted; the adjacent Effigy did not. Load Pattern showed only a stencil; empty Carve cleared it with little explanation. Thorn fitting itself worked.

**Requires:**

- [FAN-01](07-fanes-recruitment-and-persistence.md#fan-01) — Use one declared personal-versus-supervised access policy.

**Acceptance prerequisites:**

- [DIA-01](02-dialogue-and-rewards.md#dia-01) — Normal lesson delivery must work.
- [RUN-01](01-runtime-and-controls.md#run-01) — Scar learning uses the brazier path.

**Unblocks:** [MEM-05](05-weaving-scars-and-teachings.md#mem-05) (acceptance), [VAL-01](09-validation-and-execution.md#val-01) (implementation)

**Recommended code work:** Recommend retaining personal-craft mastery as the baseline, with a narrowly scoped supervised first-lesson permission at the generated teaching station if desired; that exception must not silently unlock every shared machine. Reuse the access decision owned by FAN-01. Keep stencil cells distinct from traced cells; invalid Carve should explain and preserve the stencil/inputs. Preview scar modifiers and their active versus learned state through the existing station/Effigy screens.

**Recommended gameplay behavior:** Name the three steps: make the scar, learn it, fit it. Show why the tendency-based first pattern was selected, Thorn's +1 armor/-5% speed, and the 100/500/50 payments before each step. Keep the ritual identity rather than merging everything into one free unlock.

**Acceptance:** Fresh student follows the normal kit and station instruction, deliberately presses Carve before tracing, traces all cells, learns and fits once. Confirm no resource loss on invalid input, expected costs on valid use, and correct active stats after reload.

**Current code/resource entry points:** [ScarStationBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/ScarStationBlockEntity.java), [ScarStationScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/scar/ScarStationScreen.java), [MasonsEffigyBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/MasonsEffigyBlockEntity.java), [MasonsEffigyScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MasonsEffigyScreen.java)

<a id="mem-04"></a>

### MEM-04: Make Thelemic strain training useful rather than repetitive

**Priority:** Medium. **Evidence:** Working mechanics; proposed teaching adjustment.

**Observed basis:** Real distilled Sanguine Fists, roughly 240 practice punches, diagnosis and salve treatment worked. Normal dialogue hid the lesson; completed Silent Fortification failed after preparation.

**Requires:** None.

**Acceptance prerequisites:**

- [RUN-03](01-runtime-and-controls.md#run-03) — Use real radial selection.
- [DIA-03](02-dialogue-and-rewards.md#dia-03) — Keep the lesson visible at later ranks.
- [END-01](08-qliphoth-and-endings.md#end-01) — Fortification must allow the completed branch.
- [MAT-03](03-materials-and-machines.md#mat-03) — Distillation is part of the taught loop.

**Unblocks:** [MEM-05](05-weaving-scars-and-teachings.md#mem-05) (acceptance)

**Recommended code work:** Preserve actual Thelemic strain and treatment hooks in VeinMasonAssignments. Recommended teaching change: introduce the existing overexertion mechanic in a bounded supervised exercise, with a clear stop threshold and recovery kit, instead of silently increasing global strain. Consider a lower tutorial damage threshold only after measuring ordinary play; keep it separate from the actual VARICOSE definition. Always retain earned diagnosis/treatment progress across late return.

**Recommended gameplay behavior:** Describe Thelemic activation, duration/reserve, affected body section and how to stop. Name Hematic Fortification explicitly and explain its permanent 15% reduction. Make the expected treatment reflect the current condition; passive healing can legitimately change the diagnosis before arrival.

**Acceptance:** Normally distill/drink/select/activate, cause measured strain, stop, receive diagnosis, craft/use treatment and finish Fortification at D5 and on a completed late path. No selection, strain or objective setter in the acceptance run; measure repetitions and downtime.

**Current code/resource entry points:** [VeinMasonAssignments.java](../../src/main/java/com/vincenthuto/hemomancy/common/mission/cicatrix_anchorite/VeinMasonAssignments.java), [HarbingerCicatrixAnchoriteDialogueTrees.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerCicatrixAnchoriteDialogueTrees.java), [MuscleMemoryActivationService.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/musclememory/MuscleMemoryActivationService.java), [VascularDiagnosisRules.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/vascular/VascularDiagnosisRules.java), [hematic_fortification.json](../../src/main/resources/data/hemomancy/recipe/cardinal_rite/hematic_fortification.json)

<a id="mem-05"></a>

### MEM-05: Validate D6 Noetic routing and alternative scar builds

**Priority:** High. **Evidence:** Unverified downstream chain; do not invent a confirmed failure.

**Observed basis:** The original campaign did not complete referral, matching Noetic cast, changed Cerebral Scar loadout and second route; its prior D5 step was blocked.

**Requires:** None.

**Acceptance prerequisites:**

- [MEM-04](05-weaving-scars-and-teachings.md#mem-04) — A naturally completed D5 lesson is required for the walkthrough.
- [MEM-03](05-weaving-scars-and-teachings.md#mem-03) — Scar fitting must be understandable and correct.
- [END-01](08-qliphoth-and-endings.md#end-01) — Completed Silent casting must work.
- [MEM-02](05-weaving-scars-and-teachings.md#mem-02) — Obtain the required memories without an unstable weave.

**Unblocks:** [VAL-02](09-validation-and-execution.md#val-02) (implementation)

**Recommended code work:** Follow the existing assignment state machine rather than replacing it. Verify first cast checks the active matching scar route, loadout credit requires a real set change, and the second cast occurs after that change. Expose tendency, routed body section and applicable scar tier in existing loadout/diagnosis UI. Fix only failures reproduced on this route, preserving memory-slot ordering and Noetic/Thelemic identity.

**Recommended gameplay behavior:** The player should understand why a scar changes this particular cast and how the second build differs. Offer one concrete demonstration, then allow alternative matching choices.

**Acceptance:** Complete the normal referral and counsel; cast without a matching scar as a negative control; fit one valid route, cast, swap to a different valid loadout and cast again. Reconnect between steps, test unchanged loadout rejection and claim reward once. Follow with two materially different scar builds.

**Current code/resource entry points:** [VeinMasonAssignments.java](../../src/main/java/com/vincenthuto/hemomancy/common/mission/cicatrix_anchorite/VeinMasonAssignments.java), [ScarNoeticRoutingRules.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarNoeticRoutingRules.java), [SynapticLoadoutActionPacket.java](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/SynapticLoadoutActionPacket.java), [HarbingerCicatrixAnchoriteDialogueTrees.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerCicatrixAnchoriteDialogueTrees.java)

## Completion record

When executed, record changed files, focused checks, live reproduction result, exact assists still required, save compatibility outcome, and downstream task IDs now unblocked. Keep tests of preserved broken saves separate from fresh-route acceptance. Update the relevant reference/wiki guidance only after behavior is implemented; this proposal does not redefine current mechanics.
