# Dialogue, rewards and continuing lessons

[Execution index and dependency register](README.md) · [Live findings](../HARBINGER_LIVE_GAMEPLAY_FINDINGS.md) · [Evidence journal](../HARBINGER_GAMEPLAY_CAMPAIGN.md)

Status: recommended work, not implemented by this document. Priority and evidence type are explicit per task. **Requires** blocks dependent implementation. **Acceptance prerequisites** block a meaningful final gameplay verdict, not independent coding or local unit checks. Both are linked in the central register.

Repair the player-facing guidance contract: a visible choice must reach its authored action, a reward message must match delivery, and unfinished lessons must remain available.

## Tasks

<a id="dia-01"></a>

### DIA-01: Preserve dialogue choices and acknowledge real rewards

**Priority:** Blocking. **Evidence:** Confirmed defect.

**Observed basis:** First Blood completed but normal hub dialogue promised rewards without delivery; focused original choices delivered them. Other NPC lesson topics disappeared.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [DIA-02](02-dialogue-and-rewards.md#dia-02) (implementation), [DIA-03](02-dialogue-and-rewards.md#dia-03) (acceptance), [MAT-01](03-materials-and-machines.md#mat-01) (implementation), [MEM-01](05-weaving-scars-and-teachings.md#mem-01) (acceptance), [MEM-03](05-weaving-scars-and-teachings.md#mem-03) (acceptance), [FAN-02](07-fanes-recruitment-and-persistence.md#fan-02) (implementation), [VAL-01](09-validation-and-execution.md#val-01) (implementation), [VAL-04](09-validation-and-execution.md#val-04) (acceptance)

**Recommended code work:** Trace original node and event IDs through DialogueHubFactory and DialogueScreen. Preserve terminal event options when converting a tree to hub presentation. Keep reward eligibility and claim state server-owned in the existing NPC mission handlers. Return a delivery result before showing success; disable only the pending claim interaction and recover on rejection. Repeated packets must not grant twice. Avoid a global replacement quest engine or resetting existing claim flags.

**Recommended gameplay behavior:** The ordinary hub must expose the next useful task, and a claimed reward must either enter inventory or remain recoverable as an intentional drop. Show a specific rejection instead of continuing a success line.

**Acceptance:** Replay the preserved D1 reward failure through the normal hub, with no focused override. Confirm exact kit, claim flag and next topic; repeat click/reopen/relog, full inventory and moving NPC. Exercise Alchemist, Mnemonist and Anchorite task options too.

**Current code/resource entry points:** [DialogueHubFactory.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueHubFactory.java), [DialogueScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/dialogue/DialogueScreen.java), [DialogueEventHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java), [HarbingerAssignmentLedgerItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java)

<a id="dia-02"></a>

### DIA-02: Validate block dialogue and deliver the cornerstone

**Priority:** Blocking. **Evidence:** Confirmed defect.

**Observed basis:** Monolith navigation worked, but Accept the cornerstone showed success without an item or claim; its speaker sentinel -1 failed live-entity validation.

**Requires:**

- [DIA-01](02-dialogue-and-rewards.md#dia-01) — Use its reward acknowledgement and claim behavior instead of building a second inconsistent delivery path.

**Acceptance prerequisites:** None.

**Unblocks:** [RIT-04](04-crafting-rites-and-armor.md#rit-04) (acceptance), [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Give block conversations server-validated context: block position/dimension and the allowed interaction/event, bound to the current nearby Monolith conversation. Prefer extending the existing open/choice payload context rather than treating every negative entity ID as trusted. Reject stale, distant, wrong-block or forged choices. Apply the same single-claim delivery contract as NPC rewards, and avoid breaking the separate disembodied voice route.

**Recommended gameplay behavior:** Accepting the cornerstone produces the actual item once, without implying the Monolith was shattered. An unavailable reward explains its prerequisite.

**Acceptance:** Normal block dialogue acceptance, inventory-full delivery, repeat/replay, block removed between open and click, out-of-range and wrong dimension; verify no arbitrary reward event can be invoked. Continue into the relevant Armature step with the received item.

**Current code/resource entry points:** [DialogueOptionPacket.java](../../src/main/java/com/vincenthuto/hemomancy/common/network/dialogue/DialogueOptionPacket.java), [OpenDialoguePacket.java](../../src/main/java/com/vincenthuto/hemomancy/common/network/dialogue/OpenDialoguePacket.java), [SanguineMonolithBlock.java](../../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineMonolithBlock.java), [SanguineMonolithDialogueTrees.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/SanguineMonolithDialogueTrees.java), [DialogueEventHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java)

<a id="dia-03"></a>

### DIA-03: Keep unfinished lessons after degree advancement

**Priority:** High. **Evidence:** Confirmed defect and misleading text.

**Observed basis:** D6 lost the Vicar consecration-kit lesson offered at D5. Monolith and Alchemist greetings ignored completed or still-unfinished chapters. Armor dialogue claimed a full set although the partial set was accepted.

**Requires:** None.

**Acceptance prerequisites:**

- [DIA-01](02-dialogue-and-rewards.md#dia-01) — Normal hub routing must work before the late-lesson walkthrough can be accepted.

**Unblocks:** [MAT-04](03-materials-and-machines.md#mat-04) (acceptance), [RIT-04](04-crafting-rites-and-armor.md#rit-04) (acceptance), [MEM-04](05-weaving-scars-and-teachings.md#mem-04) (acceptance), [END-03](08-qliphoth-and-endings.md#end-03) (acceptance), [VAL-01](09-validation-and-execution.md#val-01) (implementation)

**Recommended code work:** Resolve task options from actual mission state first and degree eligibility second. Preserve old unfinished tasks at higher degrees; keep completed task history and repeatable counsel separate. Keep missions under their existing NPC owner packages. Align full-set versus first-piece checks between dialogue and handler using the intended current predicate. Query Chamber chapter state for its Monolith reminder.

**Recommended gameplay behavior:** Returning late should not punish the player for exploring another system first. Show the missing action, item source, and degree minimum; distinguish the D4 advancement rite from Hematic Fortification.

**Acceptance:** D5 to D6 transition before/after each kit claim; D7 late return; partial and full armor; already-completed Chamber visit; no duplicate kits and no lost lesson. Confirm normal hub visibility after DIA-01.

**Current code/resource entry points:** [DialogueEventHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java), [SanguineMonolithDialogueTrees.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/SanguineMonolithDialogueTrees.java), [HarbingerCicatrixAnchoriteDialogueTrees.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerCicatrixAnchoriteDialogueTrees.java), [AnchoriteProgressSnapshot.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AnchoriteProgressSnapshot.java)

<a id="dia-04"></a>

### DIA-04: Make guidance readable and defer intrusive whispers

**Priority:** High. **Evidence:** Confirmed presentation problems.

**Observed basis:** Black portraits, missing ledger/Observances icons, clipped text and a vascular view obscured by its model were visible. Unsolicited whispers replaced crafting/preparation screens.

**Requires:** None.

**Acceptance prerequisites:**

- [END-01](08-qliphoth-and-endings.md#end-01) — Required ending decisions must follow the shared pending/completed path policy.

**Unblocks:** [END-03](08-qliphoth-and-endings.md#end-03) (implementation), [VAL-04](09-validation-and-execution.md#val-04) (acceptance)

**Recommended code work:** Fix registered resource paths and layout bounds rather than shortening away authored instructions. Wrap/scroll body and objective text, keep hitboxes aligned, and maintain contrast with the actual background. Queue unsolicited whisper presentation while a crafting/ritual/active-combat interaction is in progress; present a visible pending notification and consume it once on a safe opening. Keep required path decisions pending and persist them across reconnect; do not silently discard or auto-answer.

**Recommended gameplay behavior:** Keep lore, but put the next actionable instruction where it can be read. A new voice must not steal the current crafting input or hide the end of an encounter.

**Acceptance:** Native screenshots at two practical GUI scales, long translated text, hovered objectives, real NPC portraits, vascular labels and Book of Observances icons. Trigger a whisper during Loom, scar carving, rite preparation and boss aftermath; finish current interaction, open the pending dialogue, reconnect and ensure one correct decision remains.

**Current code/resource entry points:** [DialogueScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/dialogue/DialogueScreen.java), [HarbingerAssignmentLedgerScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java), [VascularViewScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/item/VascularViewScreen.java), [FungalWhisperDialogueTrees.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/FungalWhisperDialogueTrees.java)

## Completion record

When executed, record changed files, focused checks, live reproduction result, exact assists still required, save compatibility outcome, and downstream task IDs now unblocked. Keep tests of preserved broken saves separate from fresh-route acceptance. Update the relevant reference/wiki guidance only after behavior is implemented; this proposal does not redefine current mechanics.
