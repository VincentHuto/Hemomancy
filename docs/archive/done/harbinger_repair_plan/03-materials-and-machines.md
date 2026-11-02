# Materials, extraction and machines

[Execution index and dependency register](README.md) · [Live findings](../HARBINGER_LIVE_GAMEPLAY_FINDINGS.md) · [Evidence journal](../HARBINGER_GAMEPLAY_CAMPAIGN.md)

Status: recommended work, not implemented by this document. Priority and evidence type are explicit per task. **Requires** blocks dependent implementation. **Acceptance prerequisites** block a meaningful final gameplay verdict, not independent coding or local unit checks. Both are linked in the central register.

Fix conservation and impossible transfers before changing economic rates. Keep demonstrated working distillation and crafting behavior. Numerical tuning below is a proposal to measure, not a balance result.

## Tasks

<a id="mat-01"></a>

### MAT-01: Give the starter economy a practical taught blood source

**Priority:** High. **Evidence:** Measured pacing concern; proposed tuning.

**Observed basis:** Zero starting blood, about 1/second regeneration, 1,000 utility conjuration cost, and roughly 10 blood from a 10-HP cow make the first useful actions expensive. Full natural resource pacing was not measured.

**Requires:**

- [DIA-01](02-dialogue-and-rewards.md#dia-01) — Any introductory supply must use reliable once-only reward delivery.

**Acceptance prerequisites:**

- [MAT-02](03-materials-and-machines.md#mat-02) — Container duplication and sample yields must be stable before tuning the loop.
- [MAT-03](03-materials-and-machines.md#mat-03) — Flask transfer must work to measure the intended economy.

**Unblocks:** [VAL-01](09-validation-and-execution.md#val-01) (implementation)

**Recommended code work:** Model the complete D1 bill from current recipes, conjuration costs and penalties, including the cost needed to access extraction in the first place. Preferred initial intervention: a one-time, claim-safe starter supply or temporary first-use allowance tied to actual initiation, sized to the taught first loop plus one recovery attempt. Reuse the existing reward/progression hook. Do not globally multiply animal blood yield or remove tool costs without measuring later effects.

**Recommended gameplay behavior:** Teach a repeatable income source immediately after the starter supply. Proposed target for testing: first useful tool and first bloodcraft within 5-10 minutes of active instructed play after initiation, excluding Hermit search. Treat that range as a design target, not a measured player average.

**Acceptance:** Fresh D0 player, no supplied rocks/raw materials after initialization, record time and every income/expense through first machine and memory. Repeat after one death. Verify no repeat-claim farming and no downstream trivialization of Throne or rites.

**Current code/resource entry points:** [ManipulationInit.java](../../src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java), [DialogueEventHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java), [BloodVolumeEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java)

<a id="mat-02"></a>

### MAT-02: Conserve vials and make the syringe reward recoverable

**Priority:** High. **Evidence:** Confirmed defects; yield policy review.

**Observed basis:** Eight samples loaded into the centrifuge while eight empty rack vials remained. A dropped syringe had zero lifespan. Two- versus eight-sample batches both returned 250 blood, with different enzyme/powder yields.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [MAT-01](03-materials-and-machines.md#mat-01) (acceptance), [COM-04](06-morphlings-and-puppets.md#com-04) (acceptance), [VAL-01](09-validation-and-execution.md#val-01) (implementation), [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Trace the rack insertion path through LivingSyringeItemHandler and VialCentrifugeBlockEntity. Transfer each physical container once; if the centrifuge retains a vial, clear that rack entry rather than manufacturing an empty replacement. On rejection leave both inventories unchanged. Correct the syringe item entity lifespan so intended rewards survive normal pickup. Make batch outputs derive from one documented recipe/yield rule, including any deliberate rounding; retain observed rates until a controlled economic comparison justifies a change.

**Recommended gameplay behavior:** Explain opposing slot requirements, partial batches, sample species and both sampling controls. Keep harmless repeated sampling unless a measured design decision calls for a cooldown; do not introduce animal injury merely to make the interface harder.

**Acceptance:** Count all containers across player/rack/machine for 2 and 8 samples, partial capacity, repeated insertion, automation if supported, and save/reload. Compare cow/goat outputs and processing time. Drop/reward a syringe with room and full inventory; verify pickup and bounded normal despawn.

**Current code/resource entry points:** [LivingSyringeItem.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingSyringeItem.java), [LivingSyringeItemHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/item/itemhandler/LivingSyringeItemHandler.java), [VialCentrifugeBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/VialCentrifugeBlockEntity.java), [VialCentrifugeStartupRules.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/VialCentrifugeStartupRules.java)

<a id="mat-03"></a>

### MAT-03: Unify Alembic storage, flask transfer and crystal guidance

**Priority:** High. **Evidence:** Confirmed transfer defect; harvest policy choice.

**Observed basis:** The 2,000 tank cannot pay a 2,500 flask fill. Near-full processing still says Distilling. Mature iron-pick harvest gave a crystal, conflicting with the Silk Touch expectation; earlier growth gave shards. Ordinary distillation and tinctures worked.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [MAT-01](03-materials-and-machines.md#mat-01) (acceptance), [MEM-04](05-weaving-scars-and-teachings.md#mem-04) (acceptance), [VAL-01](09-validation-and-execution.md#val-01) (implementation), [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Preferred minimal transfer fix: raise Alembic capacity to at least one full 2,500-blood flask while keeping flask value and existing recipe costs unchanged; derive transfer checks and UI scale from the same capacity/amount definitions. Check incoming container transfers for overflow before committing. Preserve stored amounts on load. Report concrete stopped reasons: tank full, output blocked, missing heat/input. Audit the crystal loot path and decide whether current normal-pick whole-crystal harvest is intentional before editing drops.

**Recommended gameplay behavior:** Present tank distillation, direct flask recipe inputs, and crystal growth as separate supported uses. Recommended harvest policy is to retain the observed mature whole-crystal reward unless an authored requirement clearly demands Silk Touch, then align instructions and JEI to that decision. Do not silently nerf existing farms while fixing tank arithmetic.

**Acceptance:** Test at 1,999/2,000/2,499/2,500 and capacity, full output slot, empty/full containers, no heat, and restart. Re-run paste and Sanguine Fists recipes. Grow each crystal stage naturally with accelerated waiting only; harvest with hand, iron pick, Silk Touch and Fortune where supported; verify exact drops and regrowth.

**Current code/resource entry points:** [GhastlyAlembicBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/GhastlyAlembicBlockEntity.java), [GhastlyAlembicScreen.java](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/GhastlyAlembicScreen.java), [GhastlyAlembicMenu.java](../../src/main/java/com/vincenthuto/hemomancy/common/menu/tile/crafting/GhastlyAlembicMenu.java), [BloodCrystalBlock.java](../../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/rite/BloodCrystalBlock.java), [SuspendedBloodCrystalBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/rite/SuspendedBloodCrystalBlockEntity.java)

<a id="mat-04"></a>

### MAT-04: Restore and measure natural rare-material routes

**Priority:** High. **Evidence:** Confirmed empty biome tag; incomplete acquisition evidence.

**Observed basis:** Mausoleum locate failed near the journey and its biome tag was empty. Quintessence and armor rare reagents were supplied; their costs and availability are not validated.

**Requires:** None.

**Acceptance prerequisites:**

- [RIT-01](04-crafting-rites-and-armor.md#rit-01) — The construction route must be reachable before a full natural cost audit.
- [DIA-03](02-dialogue-and-rewards.md#dia-03) — Material guidance must remain available at the degree where the player returns.

**Unblocks:** [RIT-04](04-crafting-rites-and-armor.md#rit-04) (acceptance), [COM-04](06-morphlings-and-puppets.md#com-04) (acceptance), [VAL-01](09-validation-and-execution.md#val-01) (implementation)

**Recommended code work:** Populate the Mausoleum biome tag with intentional existing eligible biomes and verify its structure/structure-set wiring and shipped loot tables. Prefer fixing the missing route over adding a new currency or blanket drop increases. Trace every Fane/armor catalyst to a natural source, minimum degree, required dimension and encounter prerequisites; identify cycles and ensure alternatives actually exist in runtime resources.

**Recommended gameplay behavior:** Add discoverable source clues to the corresponding NPC inquiry/book entry. For every rare drop document expected attempts and whether the item is consumed once, per piece, or per retry. Recommend pity/recovery changes only after observing repeat attempts.

**Acceptance:** Fresh chunks on multiple seeds: real locating/discovery and structure entry, actual chest or mob drop, then one Fane catalyst and one complete armor branch acquired without item grants. Record search time and unavailable sources separately from combat failures.

**Current code/resource entry points:** [mausoleum.json](../../src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/mausoleum.json), [mausoleum.json](../../src/main/resources/data/hemomancy/worldgen/structure/mausoleum.json), [mausoleum_treasure.json](../../src/main/resources/data/hemomancy/loot_table/chests/mausoleum_treasure.json), [ArmatureUpgradeRecipe.java](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/ArmatureUpgradeRecipe.java)

## Completion record

When executed, record changed files, focused checks, live reproduction result, exact assists still required, save compatibility outcome, and downstream task IDs now unblocked. Keep tests of preserved broken saves separate from fresh-route acceptance. Update the relevant reference/wiki guidance only after behavior is implemented; this proposal does not redefine current mechanics.
