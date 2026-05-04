# Specimen Jar Design

**Date:** 2026-05-04

## Goal

Add a placeable Specimen Jar block/item made from Vivianite Glass and Hematic Iron that captures and displays Hemomancy arthropod mobs.

## Scope

The jar captures only Hemomancy arthropod-themed mobs for now. The first capturable set is `chthonian`, `chthonian_queen`, `chitinite`, `fervent_chitinite`, `hemolymphopoda`, `myelin_borer`, `fargone`, and `tooth_pecks`.

Vanilla arthropods and non-Hemomancy mobs are out of scope.

## Gameplay

An empty Specimen Jar can be placed as a block. Right-clicking a capturable Hemomancy mob with an empty jar stores that exact entity's save data in the item, removes the entity from the world, and gives the player a filled jar. The filled jar can be placed, picked up, and broken while preserving or releasing the stored specimen depending on the interaction.

Placed behavior:

- Empty jars place and drop as empty jars.
- Filled jars place with their captured entity stored in the block entity.
- Shift right-clicking a placed jar picks the jar back up without breaking it and preserves the stored entity.
- Breaking a filled jar releases the exact stored entity at the jar position.
- Creative-mode removal should not duplicate released specimens or item drops beyond normal project conventions.

## Data Model

Captured entity data is stored as item/block-entity NBT under a dedicated `Specimen` compound. The compound includes the entity ID and its saved state from `Entity#save`, with position/UUID adjusted when released so the entity is recreated safely in the current world.

Capturability is controlled by an entity type tag named `hemomancy:specimen_jar_capturable`. This keeps the feature data-driven and easy to extend without touching item logic.

## Rendering

The block entity renderer draws the jar model and, when filled, renders a client-side cached instance of the stored entity inside the jar. The renderer scales the entity to fit within the jar and keeps it still enough to read as a specimen.

The item renderer mirrors the same visual language for held and inventory rendering where custom item rendering is supported. Filled jars should visibly contain the stored specimen.

## Assets And Registration

The feature adds:

- `SpecimenJarBlock`
- `SpecimenJarBlockEntity`
- `SpecimenJarBlockItem`
- `SpecimenJarRenderer`
- `SpecimenJarItemRenderer`
- `SpecimenJarModel`
- blockstate, item model, recipe, loot table, lang, and entity tag data

The implementation follows existing Hemomancy registration patterns in `BlockInit`, `BlockEntityInit`, `ClientEvents`, `LayerEvents` if needed, and the custom block item renderer patterns used by Vial Centrifuge, Morphling Cradle, and Mnemonic Reliquary.

## Testing

The implementation should include automated coverage for the server-side capture/release data helpers where practical, then run the project build. Manual in-game verification should cover capture, empty placement, filled placement, shift-right-click pickup, and break-to-release behavior.

## Documentation

Update `HEMOMANCY_REFERENCE.md` with the Specimen Jar feature, capturable mob scope, and interaction rules.
