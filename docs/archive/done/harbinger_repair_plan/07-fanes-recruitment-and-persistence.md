# Fanes, recruitment and player persistence

[Execution index and dependency register](README.md) · [Live findings](../HARBINGER_LIVE_GAMEPLAY_FINDINGS.md) · [Evidence journal](../HARBINGER_GAMEPLAY_CAMPAIGN.md)

Status: recommended work, not implemented by this document. Priority and evidence type are explicit per task. **Requires** blocks dependent implementation. **Acceptance prerequisites** block a meaningful final gameplay verdict, not independent coding or local unit checks. Both are linked in the central register.

Define personal versus shared progression clearly, make helpers useful without obstructing rituals, and verify state survives the appropriate lifecycle transitions.

## Tasks

<a id="fan-01"></a>

### FAN-01: Unify personal craft credit and station access

**Priority:** Blocking. **Evidence:** Confirmed Bloodwell defect; access policy recommendation.

**Observed basis:** Fane completion produced a Bloodwell that denied its founder for not personally crafting it. Another player correctly unlocked a shared centrifuge only after their own real craft. Teaching-station policies differed.

**Requires:** None.

**Acceptance prerequisites:**

- [RUN-01](01-runtime-and-controls.md#run-01) — Dedicated founding/access must survive ritual effects.

**Unblocks:** [MEM-03](05-weaving-scars-and-teachings.md#mem-03) (implementation), [FAN-02](07-fanes-recruitment-and-persistence.md#fan-02) (implementation), [FAN-03](07-fanes-recruitment-and-persistence.md#fan-03) (implementation), [VAL-01](09-validation-and-execution.md#val-01) (implementation)

**Recommended code work:** Use the existing personal-craft milestone as the baseline contract. Credit successful cardinal Bloodwell completion through the same canonical path as its intended craft; do not grant access on mere recipe detection or failed completion. Expose one shared access query/reason to station menus and teaching exceptions. For old saves recover founder credit only from trustworthy Fane ownership/completion evidence, not proximity to someone else's well. Keep server checks on every open/action.

**Recommended gameplay behavior:** Recommended default: communal ownership/storage does not silently grant all personal mastery. Explain that rule on denial and show how to earn it. A supervised first-lesson station exception must be explicit and local to that lesson rather than an accidental global unlock.

**Acceptance:** Founder completes the normal Fane, opens resulting well, saves/reloads; nonmember and member without personal credit remain correctly constrained. Member earns actual craft and uses shared station. Full-inventory/drop completion and interrupted rite do not lose or falsely award credit.

**Current code/resource entry points:** [ConsecratedBloodwellBlock.java](../../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/ConsecratedBloodwellBlock.java), [ConsecratedBloodwellBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/ConsecratedBloodwellBlockEntity.java), [HarbingerCardinalRiteEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java), [FoundingFaneSavedData.java](../../src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/FoundingFaneSavedData.java)

<a id="fan-02"></a>

### FAN-02: Make member onboarding and permissions explicit

**Priority:** High. **Evidence:** Observed onboarding gap; recommended route.

**Observed basis:** Two real clients joined and retained membership, but the tested shared Hermit display had already been spent. Nonleaders saw actions that the server rejected. Broad natural NPC availability was not established.

**Requires:**

- [DIA-01](02-dialogue-and-rewards.md#dia-01) — Any starter entitlement needs trustworthy reward claims.
- [FAN-01](07-fanes-recruitment-and-persistence.md#fan-01) — Membership guidance must match the settled personal/shared access rules.

**Acceptance prerequisites:** None.

**Unblocks:** [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Keep membership and personal initiation separate. Add or expose an existing legitimate per-player starter route through Hermit dialogue after another player consumes the site display. Recommended fallback: one verified initiation entitlement per eligible uninitiated player, tied to the actual quest, not an infinitely harvestable heart or a degree grant. Derive enabled member controls from the same role permissions enforced server-side; recheck on execution to handle changed membership.

**Recommended gameplay behavior:** Tell recruits whether they still need initiation, personal craft mastery or a role assignment. Give a route to the next legitimate requirement instead of a dead-end communal station.

**Acceptance:** Two fresh accounts encounter one spent site, both complete intended onboarding, join and use appropriate actions; nonleader disband/recall failures are explained or disabled. Test leave/rejoin, leader change if supported, stale screen and restart; no repeat starter farming.

**Current code/resource entry points:** [FoundingFaneSavedData.java](../../src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/FoundingFaneSavedData.java), [DialogueEventHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java), [HarbingerAssignmentLedgerItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java)

<a id="fan-03"></a>

### FAN-03: Keep recruited helpers out of ritual interaction lanes

**Priority:** High. **Evidence:** Confirmed interference; navigation tuning.

**Observed basis:** Recruited NPCs intercepted Projection repair and opened conversations. Their roles did help later rites; broad spawn scarcity was not proven.

**Requires:**

- [FAN-01](07-fanes-recruitment-and-persistence.md#fan-01) — Use consistent owner/member/access state for role assignment.

**Acceptance prerequisites:**

- [RUN-01](01-runtime-and-controls.md#run-01) — Dedicated helper/rite test needs safe particle paths.

**Unblocks:** [RIT-02](04-crafting-rites-and-armor.md#rit-02) (acceptance), [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Constrain assigned helper positioning using the resolved anchor/support layout. Prefer safe interaction-lane spacing and explicit ritual-use routing while an active Projection is held, keeping ordinary NPC conversation possible outside that action. Do not make all nearby NPCs noncollidable or disable their AI permanently. Show role/assignment state from the actual Fane data and provide a reliable way to locate a missing instructor.

**Recommended gameplay behavior:** Helpers should reduce pressure without becoming obstacles. Keep manual recruitment and role choice meaningful; clearly explain Anchor, Attendant and Warden behavior.

**Acceptance:** Run a live repair with an NPC between player and anchor, a moving Warden, different assignments, a blocked route and rejoin/restart. Confirm roles work, anchors can be repaired, and deliberate NPC conversation remains accessible. Repeat without test NoAI/teleport assistance.

**Current code/resource entry points:** [FoundingFaneSavedData.java](../../src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/FoundingFaneSavedData.java), [BloodProjectionInteractionEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/event/BloodProjectionInteractionEvents.java), [HarbingerCardinalRiteEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java)

<a id="fan-04"></a>

### FAN-04: Classify durable Chamber state and safe death recovery

**Priority:** High. **Evidence:** Observed state loss; consequences need reproduction.

**Observed basis:** Chamber visit flags vanished on death while degree and capacity survived; plain save/reload worked. Actual post-death chair behavior was not verified. Vanished conjured tools aggravated late-path recovery.

**Requires:** None.

**Acceptance prerequisites:**

- [RIT-01](04-crafting-rites-and-armor.md#rit-01) — The natural Throne/Chamber route must be constructible.
- [END-01](08-qliphoth-and-endings.md#end-01) — Recovery must respect completed-path permissions without trapping the player.

**Unblocks:** [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Reproduce death and the actual chair revisit before labeling every missing visit flag a bug. Separate permanent earned attunement/return eligibility from temporary visit/seat context. Copy or reconstruct only durable eligibility from existing authoritative progression; rebind the new player entity and discard stale seat/entity references. Do not copy all persistent data indiscriminately or duplicate dropped inventory. Define restoration of an earned Staff independently from illegal path re-entry using END-01.

**Recommended gameplay behavior:** Death should retain earned progression and provide a clear legitimate return/reconjuration route, while preserving intended equipment/resource loss.

**Acceptance:** Death inside/outside Chamber, respawn at Throne, actual chair return, logout inside, dimension change and dedicated restart. Compare inventory/drop counts and earned flags. Confirm a never-attuned player does not gain access from the recovery logic.

**Current code/resource entry points:** [ChamberOfWillManager.java](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberOfWillManager.java), [ChamberOfWillEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/ChamberOfWillEvents.java), [InitiatoryDegreeEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/degree/InitiatoryDegreeEvents.java)

## Completion record

When executed, record changed files, focused checks, live reproduction result, exact assists still required, save compatibility outcome, and downstream task IDs now unblocked. Keep tests of preserved broken saves separate from fresh-route acceptance. Update the relevant reference/wiki guidance only after behavior is implemented; this proposal does not redefine current mechanics.
