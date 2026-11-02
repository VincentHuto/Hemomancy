# Morphlings and puppeteering

[Execution index and dependency register](README.md) · [Live findings](../HARBINGER_LIVE_GAMEPLAY_FINDINGS.md) · [Evidence journal](../HARBINGER_GAMEPLAY_CAMPAIGN.md)

Status: recommended work, not implemented by this document. Priority and evidence type are explicit per task. **Requires** blocks dependent implementation. **Acceptance prerequisites** block a meaningful final gameplay verdict, not independent coding or local unit checks. Both are linked in the central register.

Restore creature progression and actives before ranking tiers. Treat persistent biological companions and replaceable thread-funded summons as different build choices.

## Tasks

<a id="com-01"></a>

### COM-01: Persist one morphling identity and earned bond

**Priority:** Blocking. **Evidence:** Confirmed defect.

**Observed basis:** Equipped HUD reached 50/50 while the Jar copy stayed 0/50, blocking normal Incubator growth after removal.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [COM-02](06-morphlings-and-puppets.md#com-02) (implementation), [COM-04](06-morphlings-and-puppets.md#com-04) (implementation)

**Recommended code work:** Define the authoritative equipped creature record and its association with the actual owning Jar slot/identity. Write earned bond and stage back through the real item handler before unequip, replacement, insertion and save; synchronize views after the server mutation, not instead of persisting it. Reject stale UI writes that would overwrite newer bond. Review jar copying/stack splitting and clone handling so a single update neither duplicates creatures nor attaches progress to another specimen.

**Recommended gameplay behavior:** Visible growth must survive handling the pet. Keep the same creature name/identity and earned progress across Jar and Incubator views.

**Acceptance:** Earn 50 bond by real upkeep, remove, reinsert and feed normally into the next stage. Repeat hotbar move, jar switch, two identical strains, death/recovery, logout and dedicated restart. Reopen the preserved failure in a copy: recover only progress supported by a matching authoritative record, never blindly grant a tier.

**Current code/resource entry points:** [EquippedMorphling.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/EquippedMorphling.java), [EquippedMorphlingEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java), [MorphlingJarItemHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/itemhandler/MorphlingJarItemHandler.java), [ItemMorphlingJar.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/ItemMorphlingJar.java), [CopyMorphlingJarRecipe.java](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/CopyMorphlingJarRecipe.java)

<a id="com-02"></a>

### COM-02: Dispatch Primal abilities with the creature state

**Priority:** Blocking. **Evidence:** Confirmed defect.

**Observed basis:** Seven ordinary Staff Primal uses did not produce their advertised effects/costs; the call passed the Staff stack into the creature-stage check. Gravecap is an on-kill case, not one of those seven.

**Requires:**

- [COM-01](06-morphlings-and-puppets.md#com-01) — Resolve the actual current creature record rather than another stale Jar copy.

**Acceptance prerequisites:**

- [RUN-02](01-runtime-and-controls.md#run-02) — Awakened Staff transitions must survive the Primal test setup.
- [RUN-01](01-runtime-and-controls.md#run-01) — Observer/server ability effects need dedicated safety.

**Unblocks:** [COM-04](06-morphlings-and-puppets.md#com-04) (implementation), [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Pass the authoritative equipped morphling stack/state to its activation implementation, while retaining the Staff as the triggering tool where needed. Validate stage, ownership, cooldown, target and blood on the server before committing cost or durability. Distinguish a rejected/no-op activation from a successful one. Keep Gravecap on its separate kill hook and verify owner/kill attribution independently.

**Recommended gameplay behavior:** Give a specific unmet requirement on rejection and clear feedback on success. Each Primal should demonstrate its authored purpose before any price or effect change.

**Acceptance:** All seven active strains: below stage, valid stage, low blood, cooldown, invalid target and valid target, compare advertised and actual effects/costs. Verify no effect or debit on denial. Test Gravecap against a valid credited kill and an excluded kill. Include actual awakened Staff after RUN-02.

**Current code/resource entry points:** [LivingStaffItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java), [EquippedMorphlingEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java), [IEquippedMorphling.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/IEquippedMorphling.java)

<a id="com-03"></a>

### COM-03: Explain puppet roles, preparation and recall economics

**Priority:** Medium. **Evidence:** Measured baseline; recommended UI and teaching.

**Observed basis:** Marrow and Hulk protected well against one zombie; Ringmaster and undeveloped Mnemonist did poorly in the passive-owner setup. A recalled damaged Vulture returned at full 14 HP for 28 thread.

**Requires:** None.

**Acceptance prerequisites:**

- [END-01](08-qliphoth-and-endings.md#end-01) — Learning new shapes must work on completed paths.
- [RUN-03](01-runtime-and-controls.md#run-03) — Native command-wheel interaction is part of this acceptance.
- [RUN-01](01-runtime-and-controls.md#run-01) — The real dedicated trial must survive startup.

**Unblocks:** [COM-04](06-morphlings-and-puppets.md#com-04) (implementation)

**Recommended code work:** Expose role, call cost, upkeep period, available body slots and required prepared memory/echo conditions from PuppeteerSummonDefinitions through the Spindle and command UI. Document the existing full-health resummon as an explicit rule unless later measurements justify changing it. Preserve preparation versus unlock distinction. Add helpful denial text for missing trial knowledge and insufficient fuel without bypassing either gate.

**Recommended gameplay behavior:** Teach Ringmaster with an actual partner body and Mnemonist with an actual echo loadout. Do not promise higher-degree forms are universal damage upgrades. Show how recall trades thread/time for a fresh body.

**Acceptance:** Earn one trial normally, prepare/call, issue native commands, recall damaged body and account for exact thread. Demonstrate Ringmaster support with a partner, Mnemonist echoes, and Mummer attention control with an active player. Check default protection still works for early forms.

**Current code/resource entry points:** [PuppeteerSummonDefinitions.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerSummonDefinitions.java), [PuppeteersSpindleBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/PuppeteersSpindleBlockEntity.java), [PuppeteersSpindleScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/item/PuppeteersSpindleScreen.java), [PuppeteerCrossbarCommands.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerCrossbarCommands.java), [MarionetteCrossbarItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarItem.java)

<a id="com-04"></a>

### COM-04: Measure tier value with matched companion builds

**Priority:** Medium. **Evidence:** Validation and conditional tuning.

**Observed basis:** Initial morphling trials had unequal starting HP/effects. Puppet results were single zombies, passive owners and partly different arenas; they do not justify blanket buffs or nerfs.

**Requires:**

- [COM-01](06-morphlings-and-puppets.md#com-01) — Growth state must be stable.
- [COM-02](06-morphlings-and-puppets.md#com-02) — Active power must actually execute.
- [COM-03](06-morphlings-and-puppets.md#com-03) — Intended support/echo builds must be prepared correctly.

**Acceptance prerequisites:**

- [MAT-02](03-materials-and-machines.md#mat-02) — Stable material yields are needed for companion upkeep economics.
- [MAT-04](03-materials-and-machines.md#mat-04) — Rare material availability affects acquisition comparisons.

**Unblocks:** [RIT-03](04-crafting-rites-and-armor.md#rit-03) (acceptance), [END-04](08-qliphoth-and-endings.md#end-04) (acceptance), [VAL-02](09-validation-and-execution.md#val-02) (implementation)

**Recommended code work:** Create repeatable comparison fixtures that reset owner HP, armor, food, effects, blood, geometry, target, tier and loadout between cases. Record effective damage, owner damage prevented, control uptime, companion survival, blood/thread per minute, call downtime and acquisition cost. Include active abilities and their cooldowns. Adjust one parameter family at a time only after a reproducible weakness is established.

**Recommended gameplay behavior:** Judge each role against its purpose: sustained modifiers/growth for morphlings, field presence and prepared utility for puppets. Retain useful low-tier options; a higher tier should deliver its advertised niche without making all earlier choices obsolete.

**Acceptance:** At least five comparable trials per relevant matchup/build as an initial diagnostic, reporting spread rather than a single time. Use single melee, ranged pressure, group threat and one representative late encounter. Separate passive-owner controls from intended active builds and record natural acquisition pacing separately.

**Current code/resource entry points:** [PuppeteerSummonDefinitions.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/summon/puppeteer/PuppeteerSummonDefinitions.java), [EquippedMorphlingEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java), [LivingStaffItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java)

## Completion record

When executed, record changed files, focused checks, live reproduction result, exact assists still required, save compatibility outcome, and downstream task IDs now unblocked. Keep tests of preserved broken saves separate from fresh-route acceptance. Update the relevant reference/wiki guidance only after behavior is implemented; this proposal does not redefine current mechanics.
