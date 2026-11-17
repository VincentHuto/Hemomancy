# 01. Living blood, clotting, containers and mint loops

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P04). |
| T2 | Built (P04 and B06). B06: CB-13 (150 mL an enzyme), CB-14 (25% and 10%), CB-17 (100 mL, Animus 1, Flammeus 1), the Morphic Nectar scrap recipe (1 powder). |
| T3 | Built (P04). B06 added `BloodLossFormationChanceTest`. |
| T4 | Built (P04, B02, B06). B02 rewrote the clinical GameTest onto the worn gourd. B06: a suture draws only from the worn gourd, with a GameTest. |
| T5 | Built (P04 and B06). B06: stamps rounded to the minute, a Flask distils by its fresh share, tests for both mission gifts and the mob-drop jug. |
| T6 | Built (B06). Spike placers in a chunk attachment; owner checks on husk, lead-in and pick-up. |
| T7 | Built (B06). Vital Effusion and Pyretic Forge; `catalyst_count` 3 for the ice chain. |
| T8 | Built (B06). `KIN_DRAWING` link state, source `blood_routing_kin`. |
| T9 | Built (B06). `CheapBloodInfusionRules.yieldShare`; `CheapBloodInfusionHelperTest` is JUnit now. |
| T10 | Built (B07). The pour split (`CoagulationRules.pour`), age blending, rite-hold pause, courier age, suture revive at 4:1, three `living_blood` server keys, Gauge and bar lines, the Liber page and three Alchemist lines. Overflow that does not fit as fresh blood also goes to the clot. |
| T11 | Built (B06 and B08). B06: the price sheet. B08: six crafts repriced (Hematic Memory, Charm of Vascularium, Morphling Jar, Sanguine Conduit, Self-Reflection Mirror, Vitality Chalice); the other 63 kept, each with its reason in the sheet. |
| T12 | Built (P04 and B06). B06 recorded the 2026-11-14 answers and defaults in `docs/CORE_LOOP.md`. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot. One premise below is out of date: the Rib gourd grows 0.125 mL a tick
since P03 (CB-2), not 0.25.

## Summary

These rulings close the free Formation and scrap loops, cut a handful of yields and prices, tighten who may touch tanks,
vessels and drips, and replace "any fill makes the whole tank fresh" with a clot-revive rule the owner wrote.
The three largest pieces are the clot-revive rule (T10, eight open choices), the Formation recipe pass (T11, 69 files
once LP-1 is closed) and diminishing returns on Blood Rock, Flask and Jug (T9).
Half of the owner's clot design is already built: clot takes room in the tank today (`BlockBloodVolume.getMaxBloodVolume`
is capacity less clot). The new part is the revive split and the end of the free whole-tank restamp.
A double Rib tick across worn slots is not possible (one gourd slot, slot 6). The real leak is elsewhere: every gourd
in the pack ticks through `inventoryTick`, so five carried Rib gourds make 25 mL a second.
Blood Drunkenness already stacks on every Rock, Flask and Jug use, but it raises cast cost. It does not cut what the
vessel gives, which is what CL-6 asks for.
The Iron Brazier's bowl holds one item, so "ice 4:1" needs a small design. A scab spike has no record of who placed it.
Morphic Nectar turns scrap into 2 powder, a second scrap loop the sheet missed. FX-3 has nothing to route.
Counts: 27 rows, BUILD 20, KEEP 5, DONE 0, MOOT 1, CONFLICT 0, UNCLEAR 0, OWNER 1, PARKED 0.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| LP-1 | Remove both free Formation recipes outright: the shapeless craft and the Alembic distillation of blood-stained stone. | BUILD | `recipe/sanguine_formation.json` (foul paste, bleeding bulb, redstone make 2) and `recipe/distillation/blood_stained_stone.json` (1 stone gives 2). These are the only two recipes whose result is a Formation. `recipe/blood_stained_stone_from_venous_stone.json` spends a Formation and stays. | T1 |
| LP-2 | "Refer to LP-1": the gourd fill stays, because LP-1 closes the free Formations. This is my reading. | KEEP | Four `hemomancy:blood_gourd_fill` recipes (white, red, black, curved horn). `FillBloodGourdRecipe.assemble` adds a hard-coded 200 mL for 4 Formations. After LP-1 it is a lossy sink: 4 Formations cost 320 mL or more at a Condenser and give 200 mL that never clots. | NQ-01-1 |
| CQ-6, LP-3 | New design. Clot takes room in the vessel. While a vessel holds clot, 50% (a config value) of blood projected in goes to reviving the clot. | BUILD | `BlockBloodVolume`: clot already takes room (`getMaxBloodVolume`). `fill` and `addBloodVolume` call `stampFed`, which restamps the whole tank. `settle(held)` restamps a rite-held station every read. `ThrallCourierSource.transferTo` calls `fill`, so a courier restamps. No revive path exists and no clot value is in config. | T10 |
| LP-4 | Sutured buffers follow the same rule. Perhaps a cheaper revive cost so suturing is worth it. | BUILD | `BloodRoutingHelper.keepBufferFresh` calls `BlockBloodVolume.freshen` every 200 ticks at no cost while the link is `DRAWING`. `linkStatus` gives `DRAWING` anywhere in the owner's Fane at D5. | T10 |
| LP-5 | Distil scrap to 1 powder. | BUILD | `recipe/distillation/hematic_iron_scrap.json` count 3. `recipe/brazier_transmutation/hematic_iron_scrap.json` brazes powder and coal back to 1 scrap for 40 mL. Also found: `recipe/morphic_nectar/hematic_iron_scrap_to_powder.json` gives 2 powder. | T2 |
| LP-8 | Formation drop from blood loss on mobs: 0.02% a tick. | BUILD | `BloodLossEffect.applyEffectTick`: `random.nextDouble() > 0.999`, which is 0.1% a tick, on any mob with blood. | T3 |
| LP-15 | Coal and charcoal give 1 Flammeus Temper. | BUILD | `data/hemomancy/temper_catalyst/flammeus.json`: coal 3, charcoal 3. | T2 |
| CL-5 | As built: 5 minutes fresh, then 10% a minute, none fresh after an hour. | KEEP | `CoagulationRules`: `FRESH_WINDOW_TICKS` 6000, `CLOT_SHARE_PER_MINUTE` 0.10, `FULL_CLOT_TICKS` 72000. | T12 |
| CL-6 | Flask and Jug clot. Blood Rock does not. All three give less and less when several are used quickly. | BUILD | `ItemInit`: `blood_rock` 250 mL (no clot), `bloody_flask` 2500 and `bloody_jug` 5000 (clot). `BloodyFlaskItem.use` gives the full fresh share every time. `CheapBloodInfusionRules` adds Blood Drunkenness (3 minutes, levels 0 to 3, cast cost +15% a level) and a 60-tick cooldown per item. Nothing reduces the yield. | T9 |
| CL-7 | As built: owner offline or out of range, the machine stalls and its buffer clots. | KEEP | `BloodRoutingHelper.linkStatus` returns `OWNER_OFFLINE`, `OWNER_DEAD`, `OUT_OF_REACH` and so on. `routeLinkedTarget` then draws nothing. CN-9 adds one exception. | T12 |
| CL-8 | As built: owner and bloodline only. The backlash idea is for later, not now. | KEEP | `BloodDrawRights.permits`. `HemoServerConfig.FOREIGN_RESERVOIR_DRAW` (`foreignReservoirDraw`, default false). | T12 |
| CN-9 | A bloodline member may stand in for an offline owner at a sutured machine. | BUILD | `BloodRoutingHelper.linkStatus`: `findOwner` null gives `OWNER_OFFLINE` with no fallback. `drawFromLinkedSources` only ever charges the owner. | T8 |
| CQ-3 | A scab spike drips only from tanks its placer may draw. | BUILD | `ScabDrip.sourceAt` and `fromTank` take from any reservoir above with no rights check. `BloodScabPointedDripstoneBlock` has no block entity and stores no placer. Spikes also grow and rise on their own. | T6 |
| CQ-4 | Only the owner and bloodline may empty, pick up or fill a specimen vessel. | BUILD | `SpecimenVessels.useEmptyHand` (husk taken with no check) and `leadIn` (no check on an owned empty vessel). Sampling already asks `BloodDrawRights` in `maySample`. The file changed on 2026-11-13 (joined vats). The gap is still there. | T6 |
| CQ-5 | Give the bone-meal and flint brazier recipes a Shaping. | BUILD | `recipe/brazier_transmutation/bone_meal.json` and `flint.json` have no `shaping` field. The other ten name one. The answer does not say which Shaping. | T7 |
| FX-1 | Only the worn gourd gets its effects. A double tick must not be possible. | BUILD | Worn double tick: not possible. `HarbingerEquipmentType.GOURD` is slot 6 only, and `HarbingerEquipmentContainer.tick` calls `onWornTick` once per worn stack. But `BloodGourdItem.inventoryTick` also runs `tickBloodTransfer` for every gourd in the pack, so carried gourds generate and feed too. | T4 |
| FX-2 | Stamp `bottled_at` on every flask creation path. | BUILD | `BloodyFlaskItem.stampBottled` is called by `GhastlyAlembicBlockEntity.tryDrainBloodIntoFlask` and by `inventoryTick` in a player's pack. Loot (`loot_modifiers/bloody_jugs_from_blood_mobs.json`, two chest tables, `loot_table/entities/thirster.json`) and the two mission gifts create unstamped vessels. No crafting recipe makes one. | T5 |
| FX-3 | Route the Mycelial Lantern's drain through `BloodAccounting`. | MOOT | `MycelialLanternBlockEntity.serverTick` spends the Lantern's own tank. `BloodAccounting.pay` takes a player. `BloodAccountingGuardrailTest` already lists the Lantern's two tank calls as allowed, like every other station. No player's blood is drained there. | NQ-01-17 |
| CN-12 | Only a worn gourd gives regeneration, so two cannot stack. | BUILD | Same code as FX-1. `EnumBloodGourdTiers.RIB` generates 0.25 mL a tick into the gourd, worn or carried. An open gourd, worn or carried, feeds the player. | T4 |
| CB-13 | Lantern enzyme price 150 to 200 mL. | BUILD | Eight `recipe/enzyme_fruiting/*.json`: `duration` 2400, `blood_per_tick` 0.25, so 600 mL. Lantern tank is 1,200 (`MAX_BLOOD`). | T2 |
| CB-14 | Rendering stays free. Yields cut to 10% to 25%. | BUILD | Ten `recipe/rendering/*.json` carry a `chance`: four at 0.5 (blaze powder, scrap, packed ice, spider eye), six at 0.25. `spore_sac.json` has no chance and is not a vanilla reagent. | T2 |
| CB-16 | Tincture jug costs 4000 for 6 doses. | BUILD | Eight `recipe/distillation/tincture_*_jug.json`: `blood_cost` 5000, 6 doses. Flask recipes are 2500. Bottling plain blood into a jug is a different number (`AlembicVesselRules`, 5000) and is not part of this. | T2 |
| CB-17 | `memory_blood_rush` costs 100 mL and 2 enzymes. | BUILD | `recipe/memory_weaving/memory_blood_rush.json`: blood 350, seven enzymes (one of each but Lux). A second recipe, `memory_blood_rush_scarred.json`, is already 100 mL with Animus 1 and Lux 1. The answer does not say which two enzymes. | T2 |
| CB-18 | Ice chain to 4:1. Keep the other brazier yields and Pyretic Forge's 15 mL smelting. | BUILD | `packed_ice.json` (1 ice, 30 mL) and `blue_ice.json` (1 packed ice, 60 mL). `BrazierTransmutationRecipe` has no input count and `IronBrazierBlockEntity` seats one item (`copyWithCount(1)`). `BrazierTransmutationRite.SMELTING_COST` is 15. | T7 |
| CB-28 | Rebalance the Formation recipes after LP-1. | BUILD | 71 recipe files name `sanguine_formation` (counted). 2 make Formations and go in T1. 69 spend them. I could not tell which 4 the sheet counts as already rebalanced. | T11 |
| RV-47 | Keep: a drip-made Condenser takes the tank owner's crystallization when online, 0 otherwise. | KEEP | `SanguineCondenserBlockEntity` lines 101 to 110 (owner from the drip, crystallization 0 when offline). `VenousBasinBlock.feedingBasin` picks the basin for a gutter. I read only its first lines. | T12 |
| PR-15 | Play: the five-minute window, the colour drain, the clot band, scraping speed, the Gauge readout. | OWNER | Nothing to check in code. Play after T10, since T10 changes what the window means. | none |

## Plan

### T1. Remove the two free Formation recipes (size S)

- Rulings: LP-1 (and LP-2 stays as it is)
- Change: delete `sanguine_formation.json` and `distillation/blood_stained_stone.json`. Move both to `removed_assets/`
  and add them to its `MANIFEST.txt`. Keep `blood_stained_stone_from_venous_stone.json`: it spends a Formation and the
  Mnemonist's lines teach it. After this a Formation comes only from a Condenser (100 to 150 mL), the key (300 mL), a
  neglected tank (400 mL), wound drops at D1 to D3, mob loot and the LP-8 drop.
- Files: `src/main/resources/data/hemomancy/recipe/sanguine_formation.json`,
  `src/main/resources/data/hemomancy/recipe/distillation/blood_stained_stone.json`, `removed_assets/MANIFEST.txt`
- Tests: add a JVM data test that no recipe file has `hemomancy:sanguine_formation` as its result (beside
  `CondenserRecipeSafetyResourceTest`, using `support/ResourceReader`). No test I found names either recipe. JEI and
  the recipe-unlock advancements were not checked for these two ids.
- Docs: `docs/HEMOMANCY_REFERENCE.md` (the "Living blood" section and the Alembic table near line 5334),
  `docs/CORE_LOOP.md` "Rulings that bind", `docs/archive/tracks/core_loop/03-living-blood.md` ("Loot and the shapeless recipe stay" is
  now wrong), `wiki/Blood-Systems.md`
- Depends on: nothing

### T2. Number edits in data (size M)

- Rulings: LP-5, LP-15, CB-13, CB-14, CB-16, CB-17
- Change:
  - LP-5: `distillation/hematic_iron_scrap.json` count 3 to 1.
  - LP-15: `temper_catalyst/flammeus.json` coal 3 to 1, charcoal 3 to 1.
  - CB-13: all eight `enzyme_fruiting` recipes to 150 mL an enzyme (`blood_per_tick` 0.0625, `duration` stays 2400).
    150 is the low end of the range. 200 would be 0.0833.
  - CB-14: rendering `chance` 0.5 to 0.25 (four files) and 0.25 to 0.10 (six files).
  - CB-16: eight `tincture_*_jug.json` `blood_cost` 5000 to 4000.
  - CB-17: `memory_blood_rush.json` blood 350 to 100, enzymes to Animus 1 and Flammeus 1 (its Temper and its
    secondary, see NQ-01-18).
- Files: the recipe files named above under `src/main/resources/data/hemomancy/recipe/`, and
  `src/main/resources/data/hemomancy/temper_catalyst/flammeus.json`
- Tests: `TemperCatalystDataTest` (add coal and charcoal at 1. Its "one reagent at 3" rule still holds through blaze
  powder), `RenderingRecipeResourceTest` (add the 10% and 25% bands), `AlembicReservoirTinctureGameTests` (pins a jug
  `getBloodCost() == 5000`), `MemoryWeavingRecipeResourceShapeTest`, `gametest/journey/HemoJourneyFixtures.java` line
  557 (hands out 3 powder for the first distillation. Found by search, not read in full),
  `DistillationExtractionGameTests`, `MycelialLanternAutomationRulesTest`
- Docs: `docs/HEMOMANCY_REFERENCE.md` (recipe tables, Lantern, tinctures, Temper catalysts),
  `docs/MANIPULATIONS_AND_CASTING.md` for the Temper catalyst number, `wiki/Blood-Systems.md`
- Depends on: NQ-01-14 for the Morphic Nectar half of LP-5. LP-15 and CB-14 may overlap chunk 04 (cast economy and
  Temper).

### T3. Blood-loss Formation drop to 0.02% (size S)

- Rulings: LP-8
- Change: in `BloodLossEffect.applyEffectTick` the mob roll becomes 0.0002 a tick, as a named constant. A 1,000-tick
  bleed (Blood Shot, Bolt, Needle, Bullet) then gives about an 18% chance instead of 63%, if the mob lives that long.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/effect/BloodLossEffect.java`
- Tests: a JVM test on the constant if it moves to a rules class. Otherwise none practical for a 0.02% roll.
- Docs: `docs/HEMOMANCY_REFERENCE.md`, `docs/MANIPULATIONS_AND_CASTING.md`
- Depends on: nothing

### T4. Only the worn gourd works (size S)

- Rulings: FX-1, CN-12
- Change: `BloodGourdItem.inventoryTick` stops calling `tickBloodTransfer`. Generation (Rib, 0.25 mL a tick) and the
  open-gourd feed run only from `onWornTick`, which the one gourd slot calls. `BloodRoutingHelper.findOpenEquippedGourd`
  stops reading the main hand and off hand, so a suture draws only from the worn gourd (NQ-01-11). Pouring a gourd in a
  machine slot, filling one at the Gourdvine Tap and the crafting refill are not effects and stay. Tooltip and lang
  say a gourd works only when worn.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/BloodGourdItem.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/routing/BloodRoutingHelper.java`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: new GameTest in `LivingBloodGameTests`: a Rib gourd in the pack gains nothing, a worn one gains 0.25 mL a tick,
  an open carried gourd does not feed the player. `BloodAccountingGuardrailTest` keeps its `BloodRoutingHelper` count.
- Docs: `docs/HEMOMANCY_REFERENCE.md` line 1089 ("carry it or equip it", "hand use opens a carried vessel"),
  `wiki/Blood-Systems.md`, `docs/CORE_LOOP.md` "Rulings that bind"
- Depends on: nothing

### T5. Stamp every flask and jug when it is made (size S)

- Rulings: FX-2
- Change: stamp in three more places. One global loot modifier stamps any clotting vessel in generated loot (mob
  drops, chests). The two mission gifts (`EarlyInitiation`, `BodyAnswersAssignment.giveBriefingSupplies`) stamp before
  giving. `BloodContainerTransfer.fillReservoirFromSlot` stamps an unstamped vessel before it reads its fresh share, so
  a hopper cannot feed a machine an ageless flask. After this an unstamped vessel exists only where Creative put it.
  Note: in the Alembic's output slot a new flask joins the stack and takes the older stamp, and flasks bottled at
  different ticks do not stack. Not changed here.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodyFlaskItem.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/tile/BloodContainerTransfer.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/mission/vicar/EarlyInitiation.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/BodyAnswersAssignment.java`,
  `src/main/resources/data/neoforge/loot_modifiers/global_loot_modifiers.json`. The class that registers loot modifier
  types was not opened.
- Tests: GameTests in `LivingBloodGameTests`: a looted jug carries a stamp, a hopper-fed unstamped flask is stamped
  before it pours. The Vicar's four-flask gift keeps its one-time claim (`AGENTS.md` documentation contract).
- Docs: `docs/HEMOMANCY_REFERENCE.md` "Carried blood (CL-6)"
- Depends on: nothing

### T6. Draw rights for scab spikes and specimen vessels (size M)

- Rulings: CQ-3, CQ-4
- Change:
  - CQ-3: record who placed a hanging spike, keyed by its root position, when a player places it. `ScabDrip.sourceAt`
    asks `BloodDrawRights.mayDraw(level, placer, tank)` before it takes a drop. A spike with no placer (grown by a
    drip, raised from the floor, world-made) draws only from tanks nobody owns. Where the record lives is NQ-01-15.
  - CQ-4: `SpecimenVessels.useEmptyHand` (husk) and `leadIn` ask `BloodDrawRights.mayDraw` and refuse with
    `message.hemomancy.living_blood.not_yours`. So do the sneak pick-up in `SpecimenJarBlock.useWithoutItem` and the
    sneak branch in `SpecimenVatBlock.useWithoutItem` (line 253, not read). A vessel nobody owns stays open to all.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/world/farm/ScabDrip.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/BloodScabPointedDripstoneBlock.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/menagerie/SpecimenVessels.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/SpecimenJarBlock.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/SpecimenVatBlock.java`
- Tests: `FarmingGameTests` (a stranger's spike under an owned tank stays dry, the owner's drips), `MenagerieGameTests`
  (a stranger cannot take a husk, lead a creature in or pick the jar up, kin can), `BloodDrawRightsTest`
- Docs: `docs/CORE_LOOP.md` "Rulings that bind", `docs/HEMOMANCY_REFERENCE.md` (ownership, fed ground, menagerie)
- Depends on: NQ-01-15, NQ-01-19. Overlaps chunk 02 (ground network and farming) and chunk 03 (menagerie).

### T7. Brazier recipes: two Shapings and the ice chain (size M)

- Rulings: CQ-5, CB-18
- Change:
  - CQ-5: add a `shaping` to `bone_meal.json` and `flint.json`. Proposed: Vital Effusion for bone meal, Pyretic Forge
    for flint (NQ-01-12). With nothing selected, held Absorption on a lit brazier then leaves a rite's bone alone.
  - CB-18: the bowl holds one item, so 4:1 cannot be four offerings. Proposed: a new optional recipe field
    `catalyst_count` (default 1). Packed ice asks for 3 more ice in the off hand, blue ice for 3 more packed ice, at
    the same 30 and 60 mL (NQ-01-13). `BrazierTransmutationRite.preflight` checks the count and `complete` spends it.
    The other ten recipes and the 15 mL Pyretic Forge smelting stay.
- Files: `src/main/resources/data/hemomancy/recipe/brazier_transmutation/bone_meal.json`, `flint.json`,
  `packed_ice.json`, `blue_ice.json`, `src/main/java/com/vincenthuto/hemomancy/common/recipe/BrazierTransmutationRecipe.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/BrazierTransmutationRecipeSerializer.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/rite/brazier/BrazierTransmutationRite.java`,
  `src/main/java/com/vincenthuto/hemomancy/compat/jei/BrazierTransmutationRecipeCategory.java`
- Tests: `FirstConversionsDataTest`, `FirstConversionsGameTests` (the bone test near line 431 needs the Shaping
  selected. Add ice with 3 and with 2 in the off hand). The recipe's stream codec changes. It is recipe sync, not a
  `PacketHandler` payload, so I expect no `PROTOCOL_VERSION` bump. Confirm when building.
- Docs: `docs/HEMOMANCY_REFERENCE.md` brazier table, `docs/CORE_LOOP.md` "Rulings that bind", the Liber page that
  teaches first conversions
- Depends on: NQ-01-12, NQ-01-13

### T8. A bloodline member stands in for an offline owner (size M)

- Rulings: CN-9 (CL-7 otherwise stays)
- Change: when a suture's owner is offline, `BloodRoutingHelper` looks for a member of the owner's bloodline within 16
  blocks of the machine, alive, D3 or higher, blood awake. That member is drawn in the owner's place through
  `BloodAccounting.payUpkeep`, with the same 50% floor. The link reads as drawing. A new value is appended to
  `ReservoirReadout.Link` (the ordinal is saved, so append only) so the Gauge and the station bar say kin is feeding
  it. Details I chose are in NQ-01-16.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/routing/BloodRoutingHelper.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/tile/ReservoirReadout.java`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: `LivingBloodGameTests.anOfflineOwnerFeedsNothing` gains a kin-nearby case and a stranger-nearby case.
  `BloodRoutingRulesTest` if the pick rule is pure.
- Docs: `docs/CORE_LOOP.md` "Rulings that bind", `docs/HEMOMANCY_REFERENCE.md` "Owner draw (CL-7)",
  `docs/archive/tracks/core_loop/03-living-blood.md` "Still open"
- Depends on: NQ-01-16. Build before or after T10, not beside it (same file). Overlaps chunk 11 (bloodline and throne).

### T9. Rock, Flask and Jug give less when used quickly (size M)

- Rulings: CL-6
- Change: the clot half is built and stays (Flask and Jug clot, Blood Rock does not). New: the blood a vessel gives is
  multiplied by a factor that falls with recent uses. Proposed, using the Blood Drunkenness level the player has when
  they drink: none 100%, level 0 80%, level 1 60%, level 2 40%, level 3 25%. All three items share the count. The
  lost share is gone. Formations from a clotted share are not reduced. The effect lasts 3 minutes and each use renews
  it, as today. The rule goes in `CheapBloodInfusionRules` and `BloodyFlaskItem.use` applies it. Tooltip and the
  drunkenness message say the vessel gave less.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/CheapBloodInfusionRules.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/BloodyFlaskItem.java`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: `CheapBloodInfusionHelperTest` (it is a `main` method class with no `@Test`. Check it runs under Gradle and
  move the cases to JUnit if not). GameTest in `LivingBloodGameTests`: five Blood Rocks in a row give 250, 200, 150,
  100 and 62.5 mL.
- Docs: `docs/HEMOMANCY_REFERENCE.md` line 4403 ("Direct blood restores are emergency infusions") and "Carried blood
  (CL-6)", `wiki/Blood-Systems.md`, `docs/CORE_LOOP.md` "Rulings that bind"
- Depends on: NQ-01-9, NQ-01-10

### T10. Clot takes room and pours revive it (size L)

- Rulings: CQ-6, LP-3, LP-4
- How it works today:
  - Clot takes room: built. An 8,000 mL tank with 1,000 mL of clot accepts fresh blood only up to 7,000.
  - Any feed above 0 restamps the whole tank fresh (`BlockBloodVolume.stampFed`). 1 mL every five minutes keeps 8,000.
  - A sutured buffer at or under its working reserve is restamped free every 200 ticks while the owner is in reach.
  - A Blood Thrall's delivery is a `fill`, so it restamps. A station held by an upgrade rite is restamped on every
    read and comes out fresh.
  - Clot leaves a tank only as Formations: Absorption scrapes 1 per 400 mL, or a Condenser sets it.
- What the owner's design replaces: the free whole-tank restamp as the way to deal with ageing blood, and the free
  sutured restamp. Scraping clot out as Formations stays as the other choice.
- Change, with the defaults I recommend where the answer is silent (each is a new question below):
  - A pure rule in `CoagulationRules`: of a pour into a tank that holds clot, the revive share (config, 0.5) is spent
    on the clot and the rest goes in as fresh blood. Each mL spent wakes 2 mL of clot back to fresh (NQ-01-2). With
    no free room the whole pour revives (NQ-01-8). When the clot is gone the whole pour is fresh.
    Example: 5,000 fresh, 1,000 clot, 2,000 room. Pour 1,000. 500 goes in fresh and 500 wakes all 1,000 clot. Result
    6,500 fresh, no clot.
  - `BlockBloodVolume` gets one feeding entry that applies the rule and says how much it will take. A top-up no longer
    makes old blood new: the tank's age blends by volume (NQ-01-3). `freshen` and `stampFed` go.
  - Projection (`IBloodReservoir.takeProjectedBlood`) and the suture draw use it (NQ-01-4). The Condenser is exempt:
    it wants clot.
  - Sutured buffers: `keepBufferFresh` and `FRESHEN_INTERVAL_TICKS` go. The buffer clots on the same clock and the
    suture's top-up revives it from the owner's blood at a cheaper rate: 1 mL wakes 4 mL (NQ-01-7).
    `BloodRoutingRules.calculateReserveRequest` must count clot, or a buffer whose clot fills its room asks for
    nothing (the round-one bug in `docs/archive/tracks/core_loop/BUILD_LOG.md` line 462).
  - Couriers: a Thrall's blood keeps the age of the tank it came from and does not revive (NQ-01-5).
  - Rite holds: the clock pauses while a station is rite-locked instead of resetting (NQ-01-6).
  - Config: three server keys in `HemoServerConfig`: `clotReviveShare` 0.5, `clotReviveYield` 2.0,
    `suturedClotReviveYield` 4.0. `wiki/Configuration.md` lists them.
  - State per container: nothing new for block tanks. `Clotted`, `LastFed` and `SettledAt` are already saved. A Flask
    or Jug cannot be revived in the hand, because its clot is worked out from one stamp. Pour it into a tank (the
    clot goes in as clot, built) and revive it there.
  - Readout: the Gauge (`ReservoirReadout.lines`) adds a line while a tank holds clot, saying that half of a pour
    wakes the clot. The station bar tooltip (`BloodVolumeBarWidget`) says the same. The clot band already draws and
    will be seen to shrink. The 2026-11-13 HUD shows no tanks, so it is not touched.
  - Teaching: the Liber page `the_hematic_order/pages/living_blood.json` and the Alchemist's living-blood lesson lines
    (`hemomancy.alchemist.craft.living_blood.*`, written to the Alchemist's sheet in `docs/NPC_VOICES.md`).
- Files: `src/main/java/com/vincenthuto/hemomancy/common/tile/CoagulationRules.java`, `BlockBloodVolume.java`,
  `IBloodReservoir.java`, `ReservoirBlood.java`, `ReservoirReadout.java`, `BloodContainerTransfer.java` (same folder),
  `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/routing/BloodRoutingHelper.java`,
  `BloodRoutingRules.java`, `ThrallCourierSource.java` (same folder),
  `src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BloodThrallEntity.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/SanguineCondenserBlockEntity.java`,
  `src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java`,
  `src/main/java/com/vincenthuto/hemomancy/client/screen/widget/BloodVolumeBarWidget.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/command/CoreLoopFixtures.java`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`,
  `src/main/resources/data/hemomancy/books/libersanguinium/the_hematic_order/pages/living_blood.json`
- Tests: `CoagulationRulesTest` (the split, no room, clot runs out, config values), `BlockBloodVolumeTest`
  (`feedingFreshensTheWholeTankButClotStays` and `aLiveLineFreshensWithoutAddingBloodAndOnlyOnceStale` are rewritten),
  `BloodRoutingRulesTest`, `LivingBloodGameTests` (`projectionFreshensTheTank`,
  `aSuturedMachineDrinksFromItsOwnerNearbyAndStallsWhenTheyLeave`, `aStationHeldByAnUpgradeRiteDoesNotClot`, plus a
  courier case and a full-tank revive case), `HardeningGameTests`, `SanguineCondenserGameTests` (still sets clot),
  `BloodAccountingGuardrailTest` counts if a drain call moves, and every playthrough test that fills a tank and waits.
  The thrall keeps its carried age in its own save data, so no payload changes.
- Docs: `docs/CORE_LOOP.md` (rule 2 and "Rulings that bind"), `docs/HEMOMANCY_REFERENCE.md` "Living blood" (the
  sentences on freshening, owner draw and rite holds), `docs/archive/tracks/core_loop/03-living-blood.md`, `docs/STATION_UPGRADES.md`
  if the rite-hold line changes, `docs/CORE_LOOP_PLAYTEST.md`, `wiki/Blood-Systems.md`, `wiki/Configuration.md`
- Depends on: NQ-01-2 to NQ-01-8. T8 touches the same routing file.

### T11. Formation recipe pass (size L)

- Rulings: CB-28
- Count, from the files: 71 name `sanguine_formation`. 2 make Formations and are removed in T1. 69 spend them:
  - 16 tinctures (8 flask, 8 jug), 1 Formation each.
  - 15 Cardinal Rite recipes (4 rites, 11 Working crafts), 1 each as the item in the Focus.
  - 5 Blood Structures, where the Formation is the held item (whether it is spent was not checked).
  - 7 Living Weapon Grafts, 1 each.
  - 4 gourd fills, 4 each for 200 mL.
  - 22 crafting recipes: five at 4 (Charm of Vascularium, Hematic Memory, Morphling Jar, Sanguine Conduit,
    Self-Reflection Mirror), one at 3 (Vitality Chalice), three at 2 (Ganglionic Harness, Hematic Suture Node,
    Terrestrial Speculum), thirteen at 1.
- Change, in three steps:
  1. Write one sheet (a CSV beside the 2026-11-11 audit) with a row per recipe: Formations, other inputs, Degree it
     opens at, blood cost, and what the Formations cost in mL at the Condenser stage a player has at that Degree.
  2. The owner rules the yardstick (NQ-01-20), then each batch in this order: early crafting the D1 to D3 player
     needs (Vicar's kit, Gauge, Hematic Memory, which is 8 Formations with its four stones), then tinctures with
     CB-16, then gourd fills with NQ-01-1, then the rest of crafting, then grafts, then rites and Working crafts with
     chunk 13.
  3. Edit the data and re-pin the tests each batch touches.
- Files: the 69 files under `src/main/resources/data/hemomancy/recipe/`, and
  `src/main/java/com/vincenthuto/hemomancy/common/recipe/FillBloodGourdRecipe.java` if the 200 mL changes
- Tests: `CardinalRitePurposeDataTest`, `WorkingCraftRosterDataTest`, `LivingWeaponGraftRecipeResourceTest`,
  `AlembicReservoirTinctureGameTests`, playthrough tests that craft these items
- Docs: `docs/HEMOMANCY_REFERENCE.md` recipe tables, `docs/CARDINAL_RITES.md`, NPC lines that quote counts (the
  Mnemonist's "eight formations in all"), `wiki/Getting-Started.md`
- Depends on: T1, NQ-01-1, NQ-01-20, T2 (CB-16). Rites and Working crafts overlap chunk 13 (rites and crafts).

### T12. Record the rulings (size S)

- Rulings: CL-5, CL-7, CL-8, RV-47, LP-2, FX-3
- Change: no code. Move each row from "Open questions" to "Rulings that bind" in `docs/CORE_LOOP.md` and the roll-up,
  with the date 2026-11-13. CL-8 notes the backlash idea as a possible later feature. FX-3 is recorded as nothing to
  do, with the reason.
- Files: `docs/CORE_LOOP.md`, `docs/OPEN_QUESTIONS.md`, `docs/archive/tracks/core_loop/03-living-blood.md`
- Tests: none
- Docs: as above
- Depends on: NQ-01-1 and NQ-01-17 confirmed

## Conflicts with recent work

None.

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-01-1 | LP-2 | Does "refer to LP-1" mean the gourd fill stays now that LP-1 is closed, or that it is removed too? | A: stays (4 Formations for 200 mL that never clots, now a loss). B: remove the four fill recipes. | A. It was the recommendation, and after LP-1 the fill only loses blood. |
| NQ-01-2 | CQ-6, LP-3 | What does the revive half of a pour buy? | A: lossless. The half stays in the tank and wakes its own volume of clot. Clot only slows the fill. B: half price. The half is spent and wakes twice its volume, so 1,000 mL of clot costs 500 mL. C: full price. The half is spent and wakes its own volume, which is the same blood as throwing the clot away. | B. It is the only one where reviving is cheaper than new blood and still costs something. |
| NQ-01-3 | CQ-6, LP-3 | Does a top-up still make the old fresh blood new? The answer does not say, and if it does, 1 mL every five minutes still keeps a tank fresh and no clot ever forms. | A: yes, as built. B: no, age blends by volume, so 1 mL into 8,000 changes nothing. C: only a feed of 5% of capacity restamps. | B. One number per tank, no new state. |
| NQ-01-4 | CQ-6, LP-3 | Which feeds revive clot, and which containers? | A: Projection and the suture draw only. Flask and gourd pours, gutters and couriers add fresh blood into free room. The Condenser never revives. B: every feed revives. | A. It is what the answer names. |
| NQ-01-5 | CQ-6 | Blood Thrall deliveries (the unanswered half of CQ-6). | A: the blood keeps the age of the tank it came from and does not revive. B: a delivery counts as fresh feeding. | A. It closes the two-tanks-and-a-courier loop. |
| NQ-01-6 | CQ-6 | A station held by an upgrade rite. | A: its clock pauses, so blood comes out as old as it went in. B: as built, it comes out freshly fed. | A. |
| NQ-01-7 | LP-4 | With the owner in reach, does a full sutured buffer still age and clot? | A: yes. The free restamp goes and the suture revives the clot from the owner at 1 mL per 4 mL. An idle 600 mL buffer then costs its owner about 15 mL a minute. B: keep the free restamp in reach and use the cheaper revive only after a stall. | A, with the yield as the dial. It is "the same rule". |
| NQ-01-8 | CQ-6, LP-3 | A tank with no free room (fresh plus clot equals capacity). | A: the whole pour revives. B: refuse until clot is scraped. | A. Otherwise a full clotted tank cannot be revived at all. |
| NQ-01-9 | CL-6 | The curve and the window for "less and less". | A: 100, 80, 60, 40, 25% over five uses, reset after 3 minutes without a use. B: other numbers. | A as a first value to play. |
| NQ-01-10 | CL-6 | Is Blood Drunkenness the counter, and does it keep its cast cost penalty too? | A: yes to both. One visible effect, shared by Rock, Flask and Jug. B: a separate hidden counter and Drunkenness unchanged. C: the yield cut replaces the cost penalty. | A. The effect already counts uses and the player can see it. |
| NQ-01-11 | FX-1, CN-12 | What may a gourd that is carried, not worn, still do? | A: pour in a machine slot, fill at the Tap, be refilled by crafting. It no longer generates, feeds the player or feeds a suture from the hand. B: nothing at all outside the worn slot. | A. Those three are uses, not effects. |
| NQ-01-12 | CQ-5 | Which Shaping for bone meal and for flint? | A: Vital Effusion for bone meal, Pyretic Forge for flint. B: Carrion Communion for bone meal, Lode Draw for flint. | A. Both are early memories, so the recipes stay early. |
| NQ-01-13 | CB-18 | The bowl holds one item. How is ice 4:1? | A: 3 more of the same in the off hand, spent on completion. B: 1 in 4 succeeds. C: stay 1:1 and charge four times the blood. | A. No dice, and it reuses the catalyst hand. |
| NQ-01-14 | LP-5 | Morphic Nectar turns 1 scrap into 2 powder. With the braze back that is still +1 scrap a cycle. | A: nectar gives 1 powder too. B: leave it. | A. |
| NQ-01-15 | CQ-3 | A spike has no placer today. Where is it kept, and what may a spike with no placer do? | A: saved data keyed by the spike's root. No placer means it drips only from unowned tanks. B: a spike takes the owner of the tank above it and always drips. | A. B makes the check do nothing. |
| NQ-01-16 | CN-9 | Stand-in details. | A: only while the owner is offline, kin within 16 blocks, D3 or higher, kin pays from their own blood with the 50% floor. B: also when the owner is online but away or dead. | A. It is the question as asked. |
| NQ-01-17 | FX-3 | The Lantern spends its own tank, not a player's blood, so there is nothing to route. | A: leave it, the guardrail already lists it. B: build a tank-side ledger for every station. | A. |
| NQ-01-18 | CB-17 | Which two enzymes for `memory_blood_rush`? | A: Animus 1 and Flammeus 1 (its Temper and secondary). B: Animus 2. C: Animus 1 and Lux 1, the same as the scarred recipe. | A. |
| NQ-01-19 | CQ-4 | A stranger can still break another player's vessel, which frees the creature. | A: leave it to claim protection. B: refuse the break for strangers. | A. Breaking blocks is a wider rule than this row. |
| NQ-01-20 | CB-28 | What is a Formation worth, as the yardstick for the pass? | A: 150 mL at D1 to D2 (bare cauldron), 100 mL from D3. B: another figure. | A. It follows `CoagulationRules.condenserMlPerFormation`. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| PR-15 | Play a session and judge the five-minute window, the colour drain, the clot band, scraping speed and the Gauge readout. | Owner | T10 built |
| CQ-6, LP-3, LP-4 | Build the revive rule once its choices are ruled. | Build | NQ-01-2 to NQ-01-8 |
| CL-6 | Build diminishing returns. | Build | NQ-01-9, NQ-01-10 |
| CL-8 | Backlash on a stranger's draw. A possible later feature. | Parked | Owner raises it again |
| CB-28 | The owner reviews the sheet from step 1 and rules each batch. | Owner | T1, T11 step 1 |
| CB-13 | Confirm 150 mL or pick another value up to 200. | Owner | T2 built |
| LP-2 | Confirm the gourd fill stays. | Owner | NQ-01-1 |
| FX-2 | A clotted Flask or Jug still works as a full ingredient (Covenant Mantle, Morphic Tonic, distilling to vials). Not in the ruling. | Later | Owner |
| FX-2 | Flasks bottled at different ticks do not stack. Rounding the stamp to the minute would fix it. | Later | Owner |
| CL-6 | `CheapBloodInfusionHelperTest` is a `main` method with no `@Test`. Check whether it runs at all. | Build | T9 |
| CB-28 | Whether a Blood Structure spends its held Formation, and which 4 recipes the sheet counts as already rebalanced. Not checked. | Build | T11 step 1 |
