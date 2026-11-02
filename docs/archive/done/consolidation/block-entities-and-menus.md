# Block entities, interactions, and menus

## Current paths

- [EnzymaticScriptoriumBlock](../../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/EnzymaticScriptoriumBlock.java) routes empty-hand and item interactions and drops contents on removal. [EnzymaticScriptoriumBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/EnzymaticScriptoriumBlockEntity.java) owns inventory, validation, persistence, menu creation, and a `sync()` method that marks dirty and sends a block update.
- [PhlebotomistsCabinetBlock](../../src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/PhlebotomistsCabinetBlock.java) also routes interactions, while [PhlebotomistsCabinetBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/PhlebotomistsCabinetBlockEntity.java) has distinct specimen storage, viewer/open state, comparator notification, display projection, and menu ownership.
- [VialCentrifugeBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/VialCentrifugeBlockEntity.java), [GhastlyAlembicBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/GhastlyAlembicBlockEntity.java), [MycelialCrucibleBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/MycelialCrucibleBlockEntity.java), and [StillwaterCondenserBlockEntity](../../src/main/java/com/vincenthuto/hemomancy/common/tile/unstained/crafting/StillwaterCondenserBlockEntity.java) each implement combinations of `ContainerData`, NBT load/save, update tags/packets, and processing state.

## Overlap and target contract

Separate five jobs: **interaction routing**, **server inventory/recipe transaction**, **saved state**, **menu data**, and **client display state**. A shared change-notification helper can define when to call `setChanged`, `sendBlockUpdated`, and comparator updates. Use explicit options rather than sending full NBT on every insignificant animation tick. A menu should expose only data the client needs; the server remains the authority for recipe cost and item movement.

Storage shapes differ: the cabinet's specimen semantics and display snapshot are not a normal processing machine. Share plumbing only when inventory and sided automation contracts match. Keep machine-specific slot validation and progress rules next to the machine.

The clearest machine pair is the Alembic and Retort: they share a distillation recipe type, result/XP bookkeeping, and several menu slot coordinates while differing in resource fluid and extra modes. Their common execution seam and extraction differences are detailed in the [Stage 3 transaction audit](stage-3-distillation/README.md). [BloodContainerTransfer](../../src/main/java/com/vincenthuto/hemomancy/common/tile/BloodContainerTransfer.java) already centralizes gourd/reservoir transfer math; prefer extending its contract when a new machine needs the same transfer, rather than copying fill/drain arithmetic into another block entity. White-humor containers may need a separate policy.

## First migration slice

1. Compare `sync()`/`contentsChanged()` paths across the Scriptorium, cabinet, and one crafting machine. Record whether each mutation affects save data, comparator, menu, or world renderer.
2. Extract one notification utility or small interface for matching calls, then migrate two machines. Retain special cabinet display behavior.
3. Standardize slot change and recipe completion around one server transaction per machine: validate, consume, produce, mark dirty, notify.
4. Check item insertion/extraction, unload/reload, menu reopen, and client display. A packet arriving correctly does not prove inventory conservation.

**Avoid:** a deep universal machine superclass, duplicate serialized inventory in both attachments and block-entity NBT, or client-side recipe decisions.
