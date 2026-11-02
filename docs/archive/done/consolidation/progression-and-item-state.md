# Progression, item state, and rewards

## Progression paths

[ClinicalBloodKnowledge](../../src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/ClinicalBloodKnowledge.java) can update its player attachment, unlock Liber entries, grant advancements, and sync after a lesson. [HarbingerChapterProgression](../../src/main/java/com/vincenthuto/hemomancy/common/mission/shared/HarbingerChapterProgression.java), [VeinMasonAssignments](../../src/main/java/com/vincenthuto/hemomancy/common/mission/cicatrix_anchorite/VeinMasonAssignments.java), [NoeticDiscoveryProgression](../../src/main/java/com/vincenthuto/hemomancy/common/mission/shared/NoeticDiscoveryProgression.java), and [UnstainedObservances](../../src/main/java/com/vincenthuto/hemomancy/common/mission/unstained/UnstainedObservances.java) make related but path-specific decisions. [LiberKnowledgeHelper](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/LiberKnowledgeHelper.java) and [LiberDiscoveryEvents](../../src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/LiberDiscoveryEvents.java) form another discovery route.

The recurring transaction is **recognize condition → mark durable completion → grant item/advancement/book knowledge → send player feedback → sync client**. Put its idempotence rule in one place per mission family. A generic mission engine is unnecessary unless the same state transitions truly recur; NPC ownership, Harbinger/Unstained exclusion, and dialogue timing are domain behavior. Audit any repeated `grantIfNotDone` and `sync` sequence for duplicate rewards after login, reload, or event replay.

## Item state paths

[DataComponentInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/DataComponentInit.java) registers persistent and networked item components, including advanced brew, Scriptorium provenance, resonant pattern, blood sample identification, field-case contents, and living weapon graft data. The values and their codecs live in classes such as [TinctureDoseData](../../src/main/java/com/vincenthuto/hemomancy/common/item/component/TinctureDoseData.java), [LivingWeaponGraftData](../../src/main/java/com/vincenthuto/hemomancy/common/item/component/LivingWeaponGraftData.java), and [FieldCaseContents](../../src/main/java/com/vincenthuto/hemomancy/common/item/component/FieldCaseContents.java).

Item inventories also use handlers: [ScarBinderItemHandler](../../src/main/java/com/vincenthuto/hemomancy/common/item/itemhandler/ScarBinderItemHandler.java), [MorphlingJarItemHandler](../../src/main/java/com/vincenthuto/hemomancy/common/item/itemhandler/MorphlingJarItemHandler.java), and [CharmItemHandler](../../src/main/java/com/vincenthuto/hemomancy/common/item/itemhandler/CharmItemHandler.java). Their storage/slot rules differ; the shared question is where durable contents live and how a handler writes back to the stack. A data component, item capability, and menu cache should never silently become three authorities for one inventory.

## First migration slice

The three legacy `Inventory` handlers share `StackBackedItemHandler`: Living Syringe, Morphling Jar, and Scar Binder. The obsolete Staff inventory and its menu, screen, capability, and opening packet have been removed. This consolidates fixed-capacity loading, mutation tracking, registry lookup, and explicit saving without changing their saved format or Jar backing-stack identity. The Syringe menu flushes on click and close. Charm and Mnemonic Folio storage remain separate. The focused `runItemInventoryGameTestServer` suite covers count-only changes, no-op transfers, legacy component data, explicit reloads, in-place edits, nested-inventory rejection, identity, and menu-close persistence.

1. Record the source of truth for each stateful item: component ID or handler NBT, serializer, client sync, stack-copy behavior, menu owner, and migration needs.
2. Start with one item handler and its menu. Make mutations follow one write-back path; keep slot acceptance rules local.
3. For one mission family, combine repeated reward and sync steps into an idempotent completion method without changing advancement IDs or Liber entry IDs.
4. Verify stack split/copy, save/reload, menu reopen, and repeated completion events. Preserve existing player items and progress.
