# Puppeteer Summon Visuals Design

> **Historical visual pass:** This document covers the original three-model art scope. The canonical gameplay roster now also includes `mnemonist_puppet`; current mechanics and unlock rules live in `docs/HEMOMANCY_REFERENCE.md` §17.

## Context

This visual pass covered the original three Harbinger puppeteer summons: `veinwing_vulture`, `marrow_spitter`, and `gorebound_hulk`. Their gameplay classes extended vanilla Vex, Skeleton, and Zombie classes to reuse movement, targeting, ranged attack, and trial behavior. Their renderers also reused vanilla renderers, so the summons looked like ordinary vanilla mobs despite their custom names, roles, charge economy, and lore.

The goal is to give each puppeteer summon a distinct Hemomancy construct silhouette while preserving existing server behavior, owner/crossbar binding, trial handling, and dismissal rendering.

## Scope

This pass changes client visuals only:

- Add one custom Java `ModelPart` model for each summon.
- Replace vanilla renderer inheritance with custom `MobRenderer` implementations for the three summon entity types.
- Add dedicated PNG texture atlases for the three summons.
- Register new model layer definitions in the existing client layer event.
- Preserve `PuppeteerSummonRenderHelper` dismissal skip/scale behavior.

This pass does not change summon stats, AI, networking, recipes, thread economy, or unlock rules.

## Architecture

The existing entity classes remain intact:

- `VeinwingVultureEntity` continues extending `Vex`.
- `MarrowSpitterEntity` continues extending `Skeleton`.
- `GoreboundHulkEntity` continues extending `Zombie`.

The server continues to treat those inheritance choices as gameplay implementation details. On the client, the renderers will become Hemomancy-specific `MobRenderer` classes that bake custom layers and use dedicated textures:

- `VeinwingVultureModel` and `VeinwingVultureRenderer`
- `MarrowSpitterModel` and `MarrowSpitterRenderer`
- `GoreboundHulkModel` and `GoreboundHulkRenderer`

Each model will expose a stable `ModelLayerLocation` using the summon entity ID. `LayerEvents.registerModelLayers` will register each layer through the existing NeoForge client event path.

## Visual Direction

The summons should not preserve vanilla silhouettes. They are constructs assembled from blood, thread, marrow, bone, and venous material, not natural living mobs.

`Veinwing Vulture` is a small hooked blood-thread familiar. Its silhouette should center on a narrow body core, crescent beak or mask, ribbed vein wings, small talons, and a trailing sinew/thread tail. Animation should use wing pulsing, hover bobbing, and head tracking.

`Marrow Spitter` is not an archer. It is a bowed marrow-frame ranged support construct with a chest reservoir, rib braces, spindly support legs, shoulder tubing, and a forward bone nozzle. The model should make the vanilla bow item visually irrelevant or hidden so the entity reads as a spitting construct.

`Gorebound Hulk` is a slow red burden with legs. It should have a squat heavy clot torso, oversized corded sinew arms, short bracing legs, an engorged back mass, and embedded pale bone or venous-stone plates. Animation should emphasize lurching weight, arm swing, and a subtle breathing pulse.

## Textures

Textures should stand out more than ordinary Hemomancy living mobs because these are summoned constructs. Atlases should use richer organic contrast: crimson and dark red masses, dark venous linework, pale marrow and bone accents, and limited wet highlights.

Texture paths:

- `assets/hemomancy/textures/entity/puppeteer_summon/veinwing_vulture.png`
- `assets/hemomancy/textures/entity/puppeteer_summon/marrow_spitter.png`
- `assets/hemomancy/textures/entity/puppeteer_summon/gorebound_hulk.png`

## Data Flow

No new data sync is needed. The models and renderers use standard entity render inputs plus existing state already exposed through the entity instance. Owner UUID, crossbar UUID, summon name, trial flag, and dismissal ticks remain unchanged.

The summons tab preview should pick up the custom visuals automatically because the renderer registration changes globally for the entity types. Preview entities do not need special model state.

## Testing

Verification should include:

- `./gradlew.bat compileJava`
- `./gradlew.bat build` if compilation succeeds

The implementation should also be checked for resource path typos and missing layer registrations, since these are the most likely failure points for this visual-only change.
