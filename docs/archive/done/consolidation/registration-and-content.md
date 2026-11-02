# Registration and content creation

## Current paths

- [Hemomancy.java](../../src/main/java/com/vincenthuto/hemomancy/Hemomancy.java) attaches many `DeferredRegister`s to the mod bus. [ItemInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java) splits items among base, handheld, special, and spawn-egg registers; [BlockInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java) splits blocks by shape/render family. [BlockEntityInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java), [EntityInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java), and [ContainerInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/ContainerInit.java) add associated runtime types.
- [BlockInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java) already auto-registers most `BlockItem`s in `onRegisterItems`, using `createItemBlock` for custom subclasses and `shouldSkipAutoBlockItem` for intentional omissions or items registered separately in `ItemInit`. `sanguine_conduit` is skipped by the automatic path because [ItemInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java) registers its gated item manually under the same ID. The split between automatic, custom, manual, and itemless routes is the actual content contract. [Stage 5](stage-5-registration/README.md) maps these decisions and the separate creative-tab route.
- [DataGeneration](../../src/main/java/com/vincenthuto/hemomancy/common/data/gen/DataGeneration.java) covers part of resource generation. `src/main/resources` remains authoritative when it duplicates generated output. A gameplay object may additionally require item/block model, blockstate, language, loot table, mining tag, recipe, creative tab route, and sometimes renderer or spawn egg.

## Overlap and target contract

The repeated responsibility is a **content bundle**, not just calling `register`. A new block's registration should point reviewers to its item decision, block entity/menu if any, resources, drops, mining rules, and tab visibility. A new entity should point to its renderer/model, attributes, spawn rules, loot, localization, and egg decision. Keep registry IDs obvious and stable.

A compact catalog of **auto item**, **custom block item**, and **manual/none** decisions would make the existing `createItemBlock` switch and skip list easier to audit. A new generic block/item registration helper would duplicate the current automation. `DeferredHolder` and `Hemomancy.rloc("snake_case_id")` remain the project pattern.

## First migration slice

1. Inventory one family of blocks and classify each as auto item, custom item, manual `ItemInit` item, or intentionally itemless. Check the `shouldSkipAutoBlockItem` list against explicit `ItemInit` entries.
2. Make the item decision visible beside each block's registration or in a single catalog without changing the registration mechanism. Preserve IDs and registration order constraints.
3. Add a short content checklist in contributor docs; inspect [runtime loot tables](../../src/main/resources/data/hemomancy/loot_table), tags, and assets for the migrated entries. [DataGeneration](../../src/main/java/com/vincenthuto/hemomancy/common/data/gen/DataGeneration.java) currently registers client blockstate/model/language providers and a Circus structure provider; other runtime resources still need explicit review.
4. Keep creative tab routing separate, including WIP content. A registration helper should not silently decide player visibility.

**Avoid:** a second automatic item path, broad registry rewrites, automatic item creation for skipped blocks, or treating generated files as the only resource source.
