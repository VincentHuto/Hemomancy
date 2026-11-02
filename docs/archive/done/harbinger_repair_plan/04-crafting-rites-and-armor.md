# Construction, Cardinal Rites and armor

[Execution index and dependency register](README.md) · [Live findings](../HARBINGER_LIVE_GAMEPLAY_FINDINGS.md) · [Evidence journal](../HARBINGER_GAMEPLAY_CAMPAIGN.md)

Status: recommended work, not implemented by this document. Priority and evidence type are explicit per task. **Requires** blocks dependent implementation. **Acceptance prerequisites** block a meaningful final gameplay verdict, not independent coding or local unit checks. Both are linked in the central register.

Preserve ritual preparation, animated supports and physical upgrades while removing prerequisite cycles, inaccessible interactions and misleading completion rules.

## Tasks

<a id="rit-01"></a>

### RIT-01: Remove the D6 construction cycle and expose craft targets

**Priority:** Blocking. **Evidence:** Confirmed defects.

**Observed basis:** The production Monolith required D7 while D6 Throne progression depended on it. Monolith/Throne target cells were enclosed. Mining the Monolith with an iron pick yielded no block item.

**Requires:** None.

**Acceptance prerequisites:** None.

**Unblocks:** [MAT-04](03-materials-and-machines.md#mat-04) (acceptance), [FAN-04](07-fanes-recruitment-and-persistence.md#fan-04) (acceptance), [VAL-01](09-validation-and-execution.md#val-01) (implementation)

**Recommended code work:** Recommended graph repair: make the infrastructure Monolith craft available at D6 while retaining D7-only lore rewards/shattering where intended. Expose its required interaction cell and the Throne target by minimally exchanging frame positions with the same material counts, or support a reachable frame face through the existing pattern search. Validate all supported rotations; do not rely on an impossible centre ray. Add the missing shipped Monolith loot table in singular loot_table/blocks and verify processResources output. Server datagen providers are not all enabled, so editing only a generator is insufficient.

**Recommended gameplay behavior:** Preserve the 175/8,000 costs initially: gradual Throne payment already worked with a refill. Show the feed target and interruption grace clearly. Allow investment to be relocated through intended mining rather than destroyed.

**Acceptance:** At earned D6 with normal resources, construct both unmodified production recipes from reachable positions, refill during the 8,000 feed, collect items, mine/re-place the Monolith, and bind/use the Throne. Repeat rotated frames, failed interruption and reload. No recipe override or crafted-stat grant.

**Current code/resource entry points:** [sanguine_monolith.json](../../src/main/resources/data/hemomancy/recipe/blood_structure/sanguine_monolith.json), [covenant_throne.json](../../src/main/resources/data/hemomancy/recipe/blood_structure/covenant_throne.json), [BloodCraftingPatternSearchRules.java](../../src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodCraftingPatternSearchRules.java), [RecipeDegreeGates.java](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/RecipeDegreeGates.java), [SanguineMonolithBlock.java](../../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineMonolithBlock.java)

<a id="rit-02"></a>

### RIT-02: Make rite requirements and repair readiness agree

**Priority:** High. **Evidence:** Confirmed usability and state-reporting failures.

**Observed basis:** D5 required a sigil labeled Optional; symmetric floor orientation produced confusing socket rejection; totals exceeded capacity while anchors stayed dry or at 49/50, sometimes with Reservoir still stocked.

**Requires:** None.

**Acceptance prerequisites:**

- [RUN-01](01-runtime-and-controls.md#run-01) — Dedicated repair/rite acceptance must survive its particle startup.
- [FAN-03](07-fanes-recruitment-and-persistence.md#fan-03) — Test helper-obstructed repair as well as empty arenas.

**Unblocks:** [RIT-03](04-crafting-rites-and-armor.md#rit-03) (implementation), [VAL-01](09-validation-and-execution.md#val-01) (implementation), [VAL-03](09-validation-and-execution.md#val-03) (implementation)

**Recommended code work:** Derive labels, seal eligibility and failure reasons from the same resolved rite requirements. Resolve rotation consistently with occupied sockets, or show an explicit orientation before accepting offerings. Trace per-anchor rounding, reach, transfer caps and Reservoir eligibility; fix the real missing transfer rather than lowering the threshold globally. Clamp display totals to their actual meaning and separately show missing/damaged anchors. Render the resolved support node order used by collision/validation.

**Recommended gameplay behavior:** The player should know which anchor or offering blocks progress and how to reach it. Make required sigils visibly required. Explain whether the reservoir repairs automatically and under which conditions.

**Acceptance:** Replay D5, Fane, Vigil, Bloom and Apotheos setup/repair with ordinary aim, every supported orientation, one far anchor, one broken anchor, and a 49/50 case with stored reserve. Seal only when the displayed conditions are all satisfied; a failed stroke identifies the expected next target.

**Current code/resource entry points:** [HarbingerCardinalRiteEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java), [BloodProjectionInteractionEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/event/BloodProjectionInteractionEvents.java), [dominion_lesser.json](../../src/main/resources/data/hemomancy/cardinal_rite_floor/dominion_lesser.json)

<a id="rit-03"></a>

### RIT-03: Clarify timed ordeals and measure support dominance

**Priority:** Medium. **Evidence:** Observed behavior; proposed encounter design.

**Observed basis:** Bastion often did most killing, timed threats vanished, and Lens/counterfeit failures sometimes advanced anyway. These are not automatically code defects: the intended success contract needs to be explicit.

**Requires:**

- [RIT-02](04-crafting-rites-and-armor.md#rit-02) — Readiness and support targeting must be reliable before interpreting encounter contribution.

**Acceptance prerequisites:**

- [COM-04](06-morphlings-and-puppets.md#com-04) — Include functioning companion builds before final encounter balance sign-off.
- [END-01](08-qliphoth-and-endings.md#end-01) — Completed-path gates must not confound the late-rite sample.

**Unblocks:** [VAL-02](09-validation-and-execution.md#val-02) (implementation)

**Recommended code work:** For each wave/profile define whether success means survival, kills, a timed interaction, or repair. Bind transition predicates and UI to that contract. Prefer keeping existing defensive survival waves, but make optional bonus objectives pay bonuses only when completed; required Lens/omen objectives should retry or fail explicitly, not time out into success. Record kill source, timeout reason, anchor loss, player contribution and support resource usage before changing damage.

**Recommended gameplay behavior:** Keep preparation rewarding. Different supports should solve different problems; do not nerf Bastion solely because it kills during a defense rite. Compare its sustained benefit with the costs and opportunities of other supports.

**Acceptance:** Normal iron and upgraded builds, same wave seed/layout, no helper then intended helpers; active combat and passive protection controls. Exercise deliberate Lens failure, no kills, successful objective and boundary repair. Confirm authored success/failure text and rewards match each outcome.

**Current code/resource entry points:** [HarbingerCardinalRiteEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java), [QliphothBloomEvents.java](../../src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/QliphothBloomEvents.java)

<a id="rit-04"></a>

### RIT-04: Complete armor guidance and diagnose restraint damage

**Priority:** High. **Evidence:** Working upgrade chain with unresolved damage and late blockers.

**Observed basis:** Paid Hematic/Chitinite upgrades worked. Remaining restrained kept hurting the player; the exact source was not established. Partial/full-set wording disagreed, and advanced progression hit missing lesson/cornerstone access.

**Requires:** None.

**Acceptance prerequisites:**

- [DIA-02](02-dialogue-and-rewards.md#dia-02) — The advanced chain needs a normally received cornerstone.
- [DIA-03](02-dialogue-and-rewards.md#dia-03) — Consecration and inspection lessons must remain available.
- [MAT-04](03-materials-and-machines.md#mat-04) — Natural branch cost cannot be accepted from supplied rare reagents.

**Unblocks:** [END-04](08-qliphoth-and-endings.md#end-04) (acceptance), [VAL-01](09-validation-and-execution.md#val-01) (implementation)

**Recommended code work:** Instrument the actual restraint damage source and collision before changing it. If intentional upgrade payment, bound it to the active operation and explain exit; if suffocation/filler collision, fix placement or collision without removing intended health costs. Ensure no continued debit after completion or failed eligibility. Reconcile Armature preview, worn-item matching and NPC inspection through existing upgrade rules.

**Recommended gameplay behavior:** Preview resulting piece, health/blood cost, reagent and exit control. Explain Barbed retaliation, Chitinite endurance and Prismatic evasion with actual current modifiers and source hints. Keep partial-set inspection wording consistent with what it accepts.

**Acceptance:** Normal craft and all four pieces for one branch, partial versus full set, idle completed restraint, held sneak exit, interrupted upgrade, death and restart. Continue through the actual advanced cornerstone/consecration steps rather than granting those kits.

**Current code/resource entry points:** [HematicArmatureBlockEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/HematicArmatureBlockEntity.java), [ArmatureRestraintEntity.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/utility/ArmatureRestraintEntity.java), [ArmatureUpgradeRules.java](../../src/main/java/com/vincenthuto/hemomancy/common/recipe/ArmatureUpgradeRules.java), [DialogueEventHandler.java](../../src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java)

## Completion record

When executed, record changed files, focused checks, live reproduction result, exact assists still required, save compatibility outcome, and downstream task IDs now unblocked. Keep tests of preserved broken saves separate from fresh-route acceptance. Update the relevant reference/wiki guidance only after behavior is implemented; this proposal does not redefine current mechanics.
