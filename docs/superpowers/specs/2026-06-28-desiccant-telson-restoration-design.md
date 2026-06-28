# Desiccant Telson Restoration Design

## Goal

Restore the desert scorpion mob under the correctly spelled `desiccant` id, make it spawn naturally in dry hot overworld biomes, add its Telson drop, and move the Barbed armor preparation recipe/lore from Fargone Proboscis to Telson without removing the proboscis item.

## Scope

- Rename active mob-facing code and resources from the old typo `dessicant` to `desiccant`.
- Keep Fargone Proboscis registered, textured, named, and droppable from Fargones.
- Add `telson` as the Desiccant's stinger-and-bulb reagent.
- Change Aculeate Vitriol to use Telson in place of Fargone Proboscis.
- Update docs and tests that describe dormant mobs, armor preparation materials, drops, and recipe expectations.

## Architecture

The mob remains a standard NeoForge entity registered in `EntityInit`, using the existing scorpion behavior and renderer structure as the starting point. Entity type, spawn egg, language keys, model layer, renderer registration, sounds, texture paths, loot table, biome modifier, and biome tag should use `desiccant` consistently.

The Java classes should be corrected to `DesiccantEntity`, `DesiccantModel`, and `DesiccantRenderer` so future references do not perpetuate the typo. The entity remains a hostile, low-profile desert scorpion with the existing hunger-on-hit behavior.

## Spawning

Desiccants should spawn through a NeoForge `add_spawns` biome modifier using a Hemomancy biome tag. The tag should cover dry hot places: deserts, badlands variants, savanna variants, and comparable vanilla dry/hot biomes.

`DesiccantEntity.canSpawnHere` should be expanded beyond sand/red sand/sandstone so the biome modifier works in badlands and other dry terrain. The accepted surface list should include sand, red sand, sandstone-family surfaces, terracotta, and red sandstone where applicable, while still respecting peaceful difficulty and normal monster spawn rules.

## Items And Loot

`telson` is a new uncommon base item. It needs registry entry, lang entry, item model, texture, and a materials-screen entry if that screen lists armor-prep reagents.

`data/hemomancy/loot_table/entities/desiccant.json` should guarantee Telson for player kills with looting scaling, matching nearby material-drop loot table conventions. Fargone's existing proboscis drop remains intact.

## Recipes And Lore

`aculeate_vitriol.json` changes from:

- Toxicognath
- Fargone Proboscis
- Calcified Blood Spine

to:

- Toxicognath
- Telson
- Calcified Blood Spine

Docs should describe Aculeate Vitriol and Barbed armor as using Telson instead of Fargone Proboscis. Fargone Proboscis remains documented as a Fargone material but no longer as the armor-prep ingredient.

## Testing

Update focused resource/source tests:

- Desiccant is no longer considered dormant.
- Desiccant registration, spawn placement, client wiring, spawn egg, biome modifier, and loot table are expected.
- Telson item assets/lang/registry and Desiccant Telson loot are expected.
- Aculeate Vitriol is expected to consume Telson, not Fargone Proboscis.
- Existing Fargone Proboscis coverage remains where appropriate to prove the item was not removed.

Verification should include the affected Java/resource tests and at least a compile-oriented Gradle test/build command if runtime dependencies permit it.
