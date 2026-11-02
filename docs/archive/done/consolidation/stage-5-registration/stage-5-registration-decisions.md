# Stage 5: block and item registration decisions

This is a source and runtime-resource audit of the 2026-09-25 checkout. It records the item decision behind a registered block and the separate creative-tab decision. No registry ID, block, item, or asset has been changed.

## The active registration route

[Hemomancy](../../../src/main/java/com/vincenthuto/hemomancy/Hemomancy.java) attaches four [ItemInit](../../../src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java) item registers and ten [BlockInit](../../../src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java) block registers to the mod bus. `BlockInit` also subscribes to item `RegisterEvent`. Its `getAllBlockEntriesAsStream()` collects the eight ordinary block registers, then `onRegisterItems` filters `shouldSkipAutoBlockItem`, chooses a subclass in `createItemBlock`, and registers that `BlockItem` under the block's own resource ID. The default branch creates a plain `BlockItem`.

The same block stream feeds the main creative tab in `Hemomancy.buildContents`. It **does not include `POTTEDBLOCKS` or `LIQUIDBLOCKS`**. `BlockInit.getAllBlockEntries()` has a different membership: it includes potted blocks, while the stream used for auto items and the tab does not. ItemInit's four-register stream independently feeds the main tab. These membership rules should be explicit in any future registry catalog; the name `getAllBlockEntriesAsStream` by itself does not tell a caller that potted and liquid blocks are absent.

| Decision | Current route | Examples and reason |
| --- | --- | --- |
| Ordinary block item | Included in the block stream, not skipped, default `new BlockItem` with the block ID. | [Venous stone](../../../src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java) and most placeable blocks. No matching `ItemInit` declaration is needed. |
| Custom block item | Included in the block stream, selected by an identity branch in `createItemBlock`; registered under the block ID. | Mycelial Crucible, Vial Centrifuge, Specimen Jar, Earthen Vein, Saint Sarcophagus, Puppeteer's Spindle, and other blocks with placement or carried-state behavior. The current factory has 19 explicit custom branches. |
| Manual item for a block | Block is skipped from auto item; `ItemInit` owns a distinct or same-ID item. | `sanguine_conduit` uses same-ID [ItemSanguineConduit](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/ItemSanguineConduit.java), whose `useOn` gates placement at degree 5. `lethean_poppy_wreath` uses a same-ID manual `BlockItem` with stack limit 16. `gourd_stem` is placed through `gourd_seeds`; active ash trails have differently named manual `ItemNameBlockItem`s. [BloodStainedStoneItem](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodStainedStoneItem.java) places `placed_blood_stained_stone` under a different item ID. |
| Intentionally no automatic item | Omitted from the block stream or listed in the skip predicate with no ordinary BlockItem replacement. | Potted and liquid blocks are stream exclusions. Attached gourd stem, filler/controller blocks, overgrowth parts, and other internal states are skip-list examples. A skipped block's actual placement/drop behavior must be read from its feature owner. |

## The skip list is several policies in one place

The 13 identity checks in `shouldSkipAutoBlockItem` have different reasons; a cleanup should not turn them into one “itemless” category.

| Current skip entries | Route to preserve |
| --- | --- |
| `gourd_stem`, `attached_gourd_stem` | `gourd_seeds` is an `ItemNameBlockItem` for the stem. The attached stem has no corresponding direct item in ItemInit. |
| `active_smouldering_ash_trail`, `active_befouling_ash_trail` | ItemInit has `active_smouldering_ash` and `active_befouling_ash`, each pointing at its trail block under a different item ID. Both items are also excluded from the main creative stream. |
| `placed_blood_stained_stone` | The separately registered `blood_stained_stone` item places this block with its own use logic. |
| `lethean_poppy_wreath`, `sanguine_conduit` | Same-ID manual ItemInit registrations. Preserve wreath stack size and conduit placement gate. |
| `filler_block`, `warp_chair_filler`, `abocipher_emitter`, `qliphoth_bloom` | No direct manual BlockItem registration found in ItemInit. These need their controller/feature placement route recorded before any item policy changes. |
| `escharian_overgrowth`, `escharian_overgrowth_rim` | No direct manual BlockItem registration found in ItemInit. Keep their world/feature-driven placement distinct from the normal player item path. |

`Hemomancy.shouldShowBlockInCreativeTab` adds a third decision after registration: it rejects blocks whose `asItem()` is `AIR`, several internal states, and entries in `isWipBlock`. The WIP tab accepts its own explicit block list. For WIP content, the explicit WIP acceptance and exclusion from the main tab must change together. ItemInit manual items go through `shouldShowItemInCreativeTab` and `isWipItem`, so a same-ID manual block item may be visible through the **item** stream while its block is excluded from the **block** stream. Creative visibility is not proof of registration or of a loot drop.

## A content bundle crosses more than two registries

| Example | Source and runtime links observed | Audit outcome |
| --- | --- | --- |
| Ordinary [venous stone](../../../src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java) | Auto BlockItem; runtime [blockstate](../../../src/main/resources/assets/hemomancy/blockstates/venous_stone.json), [block model](../../../src/main/resources/assets/hemomancy/models/block/venous_stone.json), [item model](../../../src/main/resources/assets/hemomancy/models/item/venous_stone.json), [loot table](../../../src/main/resources/data/hemomancy/loot_table/blocks/venous_stone.json), English name, and Minecraft pickaxe tag. | A standard block still needs independent resource and mining/drop checks. |
| Custom [Mycelial Crucible](../../../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/MycelialCrucibleBlock.java) | Custom BlockItem branch; [block entity](../../../src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java), [menu](../../../src/main/java/com/vincenthuto/hemomancy/common/init/ContainerInit.java), renderer and screen in [ClientEvents](../../../src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java), blockstate/model/item model, language, and pickaxe/stone-tool tags. | No `data/hemomancy/loot_table/blocks/mycelial_crucible.json` was found. Its `onRemove` drops contents and removes fillers, but has no block-item drop override in that class. Check actual survival break/drop behavior before treating the content bundle as complete. |
| Manual [Sanguine Conduit](../../../src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/ItemSanguineConduit.java) | Same-ID manual item, auto-item skip, explicit creative block exclusion, runtime [loot table](../../../src/main/resources/data/hemomancy/loot_table/blocks/sanguine_conduit.json) dropping `hemomancy:sanguine_conduit`, models, localization, and mining tags. | The manual item is a gameplay gate, not a duplicate implementation of ordinary BlockItem registration. |

The Crucible finding is a **source/resource gap**, not a live drop test. More generally, missing `data/hemomancy/loot_table/blocks/<id>.json` can be intentional for internal or temporary blocks; inspect the block's drop override and intended player acquisition before adding a table.

## Smallest useful consolidation

1. Create a reviewable registration decision catalog keyed by block ID: owning block register, auto/custom/manual/none item route, actual item ID, `ItemInit` declaration if manual, and main/WIP/hidden creative route. Populate the 13 skips and 19 custom-factory branches first. This is documentation or a source-adjacent declaration, not a second registration system.
2. For each new placeable block, check the runtime blockstate, block model, item model, language key, loot/drop rule, and mining tags independently. For a machine, include BlockEntityInit, ContainerInit, ClientEvents screen/renderer, and any packet or recipe resources. Keep datagen outputs and committed runtime resources distinguishable.
3. Resolve the Mycelial Crucible drop question with a focused survival interaction before deciding whether to add a loot table. Do not infer that dropping inventory contents also drops the machine block.
4. Only after the catalog is stable, consider replacing the growing `createItemBlock` identity chain with a small explicit factory map. Preserve registry IDs, custom item classes, manual placement gates, and tab routing. A broad rewrite of DeferredRegisters would add risk without addressing the missing decisions.

This Stage 5 pass did not run a client, break blocks in a world, or change gameplay code.
