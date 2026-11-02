# Qliphoth, path decisions and ending recovery

[Execution index and dependency register](README.md) · [Live findings](../HARBINGER_LIVE_GAMEPLAY_FINDINGS.md) · [Evidence journal](../HARBINGER_GAMEPLAY_CAMPAIGN.md)

Status: recommended work, not implemented by this document. Priority and evidence type are explicit per task. **Requires** blocks dependent implementation. **Acceptance prerequisites** block a meaningful final gameplay verdict, not independent coding or local unit checks. Both are linked in the central register.

Separate the nine-pome pending decision from completed endings. Preserve each branch identity, existing saves and authored rewards while repairing lifecycle and recovery failures.

## Tasks

<a id="end-01"></a>

### END-01: Centralize pending and completed path permissions

**Priority:** Blocking. **Evidence:** Confirmed defects; recommended branch policy.

**Observed basis:** Completed Silent Archon D7 could not cast Blood Shot; D8 Apotheos could not learn new puppets; Silent Fortification charged four anchors and rejected only at final completion.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [DIA-04](02-dialogue-and-rewards.md#dia-04) (acceptance), [RIT-03](04-crafting-rites-and-armor.md#rit-03) (acceptance), [MEM-04](05-weaving-scars-and-teachings.md#mem-04) (acceptance), [MEM-05](05-weaving-scars-and-teachings.md#mem-05) (acceptance), [COM-03](06-morphlings-and-puppets.md#com-03) (acceptance), [FAN-04](07-fanes-recruitment-and-persistence.md#fan-04) (acceptance), [END-03](08-qliphoth-and-endings.md#end-03) (implementation), [END-04](08-qliphoth-and-endings.md#end-04) (acceptance), [VAL-03](09-validation-and-execution.md#val-03) (implementation), [VAL-04](09-validation-and-execution.md#val-04) (acceptance)

**Recommended code work:** Introduce one small gameplay permission helper around the existing degree/path state and operation type, used by BloodManipulation, UseManipKeyPacket where applicable, PuppeteerTrialRiteController, cardinal start and completion, and Staff recovery. Do not infer completion from degree >=8 or pome count alone. Preflight before taking offerings/blood/Staff; revalidate completion for legitimate state changes and restore escrow on cancellation according to paid/spent rules. Preserve known/learned powers and all existing path IDs and serialized states.

**Recommended gameplay behavior:** Recommended policy: unresolved nine-pome choice restricts branch actions as authored but retains the tools/recovery required to resolve it. Completed Apotheos and Silent restore ordinary permitted crafting, casting and side lessons; neither can replay the other ending or obtain duplicate unique rewards. Keep Unstained mutual exclusion. Any deliberately permanent lost power must be explicitly named before commitment rather than accidentally inherited from a pome check.

**Acceptance:** Table-test each operation at <9, nine before choice, Apotheos pending, Silent pending, completed Apotheos, completed Silent, and Unstained/purifying. Live both saved endings: basic cast, Staff recovery after death, new puppet trial and full Fortification. Wrong-path finale remains denied before payment; repeat final reward remains denied.

**Current code/resource entry points:** [BloodManipulation.java](../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java), [UseManipKeyPacket.java](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UseManipKeyPacket.java), [PuppeteerTrialRiteController.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/PuppeteerTrialRiteController.java), [HarbingerCardinalRiteEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java), [InitiatoryDegree.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/degree/InitiatoryDegree.java)

<a id="end-02"></a>

### END-02: Commit pome consumption to its source tree safely

**Priority:** Blocking. **Evidence:** Confirmed lifecycle defect.

**Observed basis:** Two actual last-item consumptions increased player count but left the tree awaiting consumption; the workaround kept a duplicate stack item alive.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [END-03](08-qliphoth-and-endings.md#end-03) (implementation), [END-04](08-qliphoth-and-endings.md#end-04) (acceptance), [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Capture origin, husk index, owner and tainted/bound state before any shrink/consumption. Validate against the correct bloom lifecycle, then update player progress and tree state as one server action and synchronize both. Use the existing origin/husk identity to prevent a duplicate notification from advancing the same husk twice. Do not derive a missing bloom identity from global pome count. For old stuck saves, reconcile only when matching authoritative owner/origin/progress proves that specific consumption; leave ambiguous cases intact with an explicit repair diagnostic.

**Recommended gameplay behavior:** Each real harvest/eat should advance the visible tree exactly once. Explain why foreign, tainted or out-of-order fruit behaves differently. Preserve current branch-choice timing.

**Acceptance:** One-item and stacked cases; actual nine harvests/eats from one tree with no item modifier; unload/reload origin chunk, log out between pick/eat, wrong owner, duplicate notification and tainted fruit. Verify one communion and no extra fruit/reward. Test a copy of the preserved stuck tree for conservative recovery.

**Current code/resource entry points:** [QliphothPomeItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/QliphothPomeItem.java), [QliphothPomeRules.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/QliphothPomeRules.java), [QliphothBloomSavedData.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/QliphothBloomSavedData.java), [QliphothBloomBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/QliphothBloomBlockEntity.java)

<a id="end-03"></a>

### END-03: Clarify tree care, travel and revelation timing

**Priority:** High. **Evidence:** Observed ambiguity; preserve working lore beats.

**Observed basis:** Bloom, shatter, harvest and travel had strong payoff, but lifecycle stalls, overlapping voice screens and unclear Gardens landing/return made the branching journey hard to read.

**Requires:**

- [END-01](08-qliphoth-and-endings.md#end-01) — Status/actions must use the agreed branch permission matrix.
- [END-02](08-qliphoth-and-endings.md#end-02) — Display a lifecycle that actually advances.
- [DIA-04](02-dialogue-and-rewards.md#dia-04) — Use its safe pending-dialogue presentation instead of a second queue.

**Acceptance prerequisites:**

- [DIA-03](02-dialogue-and-rewards.md#dia-03) — Contextual Monolith guidance must reflect completed chapters.

**Unblocks:** [VAL-01](09-validation-and-execution.md#val-01) (implementation)

**Recommended code work:** Derive tree status from actual bloom state: growing, awaiting harvest, awaiting consumption, choice pending, severed or completed. Present one matching action instead of competing generic reminders. Keep FungalGardenTravelHelper checks and dialogue state aligned with END-01; show usable landing/return affordances and persist the choice prompt rather than auto-answering it. Distinguish Silent Living Arsenal severing from separate Unstained pruning interactions.

**Recommended gameplay behavior:** Before an irreversible branch action show the relevant consequence and return/recovery route using authored language. Retain the eerie revelation; schedule it through the pending-dialogue behavior rather than interrupting active crafting. Do not invent a generic horticulture system for an already authored nine-husk sequence.

**Acceptance:** Fresh tree lifecycle, actual first travel, normal return, reconnect before choice, both legitimate branch decisions, failed and repeated pruning attempts. A server restart must not repeat completed lore rewards or lose a pending choice. Check the Monolith guidance after an already completed Chamber visit.

**Current code/resource entry points:** [QliphothBloomBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/QliphothBloomBlockEntity.java), [QliphothBloomSavedData.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/QliphothBloomSavedData.java), [FungalGardenTravelHelper.java](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/FungalGardenTravelHelper.java), [FungalWhisperDialogueTrees.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/FungalWhisperDialogueTrees.java), [SanguineMonolithDialogueTrees.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/SanguineMonolithDialogueTrees.java)

<a id="end-04"></a>

### END-04: Retest Vesper difficulty and the complete reward loop

**Priority:** High. **Evidence:** Assisted completion; natural combat remains unverified.

**Observed basis:** Normal baseline died in about 23 seconds; Resistance V/Saturation and later Strength X covered actual anchors, phase two, absorption, return and reward. That is not evidence for a fair natural victory or a mandatory boss nerf.

**Requires:** None.

**Acceptance prerequisites:**

- [END-01](08-qliphoth-and-endings.md#end-01) — Completed and pending recovery/casting rules must work.
- [END-02](08-qliphoth-and-endings.md#end-02) — Reach the branch through actual fruit progression.
- [RUN-02](01-runtime-and-controls.md#run-02) — The graft reward must not introduce a client crash.
- [RIT-04](04-crafting-rites-and-armor.md#rit-04) — Intended armor preparation must be available.
- [COM-04](06-morphlings-and-puppets.md#com-04) — If companions are included, their functioning build contribution must be measured.

**Unblocks:** [VAL-02](09-validation-and-execution.md#val-02) (implementation)

**Recommended code work:** Use the existing encounter and damage instrumentation to measure telegraphs, reachable attack windows, anchor vulnerability, player DPS, phase transitions and resource recovery. Correct reproducible hitbox/targeting problems first; tune damage/HP only after a competent natural route establishes the gap. Preserve the distinct phases, downed absorption condition, return and unique memory graft. Guard against double rewards across death/reconnect at the finish.

**Recommended gameplay behavior:** Teach what makes an anchor vulnerable and when to absorb the downed boss. Recovery after a loss must be feasible with the actual branch inventory and powers. State intended solo preparation without requiring undocumented helper or immunity tricks.

**Acceptance:** Fresh copied pre-fight state with legitimately acquired intended loadout, no resistance/strength/health/blood grants during the scored fight. Multiple active attempts through real player controls; record causes of loss and each phase. Then death/retry, disconnect at downed state, absorption, return, graft and safe awakened morphing. Assisted mechanics-only attempts remain separately labeled.

**Current code/resource entry points:** [ChamberOfWillManager.java](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberOfWillManager.java), [LivingStaffItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java), [LivingStaffMorphRenderer.java](../../src/main/java/com/vincenthuto/hemomancy/client/render/item/harbinger/LivingStaffMorphRenderer.java)

## Completion record

When executed, record changed files, focused checks, live reproduction result, exact assists still required, save compatibility outcome, and downstream task IDs now unblocked. Keep tests of preserved broken saves separate from fresh-route acceptance. Update the relevant reference/wiki guidance only after behavior is implemented; this proposal does not redefine current mechanics.
