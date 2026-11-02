# Gameplay source-path coverage

This is the breadth check behind the guides. The 2026-09-25 scan enumerated the Java packages under `src/main/java/com/vincenthuto/hemomancy`, examined names and repeated API patterns in each gameplay family, then traced representative implementations and resources for the high-overlap paths. It does **not** claim a line-by-line review of roughly 3,000 Java files. `src/test`, `src/gameTest`, generated output, `common/testing`, and dormant compatibility packages were excluded at the user's request to focus on actual gameplay code.

## Common code

| Package path | Responsibility found | Cleanup guide or disposition |
| --- | --- | --- |
| [antecedent](../../src/main/java/com/vincenthuto/hemomancy/common/antecedent) | recordings, inquiry, research, items | [Reloads](world-persistence-and-reloads.md), [progression](progression-and-item-state.md); preserve dialogue semantics. |
| [armor](../../src/main/java/com/vincenthuto/hemomancy/common/armor) | set abilities and combat handlers | [Combat](combat-and-powers.md); registry already exists, compare event hooks to it. |
| [block](../../src/main/java/com/vincenthuto/hemomancy/common/block) | direct use, multiblocks, fluids, crops | [Interaction routing](interaction-routing.md), [block entities](block-entities-and-menus.md). |
| [brewing](../../src/main/java/com/vincenthuto/hemomancy/common/brewing) | advanced brews, resolver, progress | [Recipes](recipe-pipelines.md), [item state](progression-and-item-state.md). |
| [capability](../../src/main/java/com/vincenthuto/hemomancy/common/capability) | attachments, access, player state, sync | [Storage index](capability-storage-index.md), [lifecycle](capability-lifecycle-and-observer-sync.md), [event phases](entity-and-player-event-phases.md), [progression](progression-and-item-state.md). |
| [circus](../../src/main/java/com/vincenthuto/hemomancy/common/circus) | pavilion state and performance | [Persistence](world-persistence-and-reloads.md), [worldgen](worldgen-and-encounters.md). |
| [command](../../src/main/java/com/vincenthuto/hemomancy/common/command) | commands and debug/operator controls | Keep as call surface; compare its mutations with the same server domain methods used by gameplay. |
| [damage](../../src/main/java/com/vincenthuto/hemomancy/common/damage) | school attack context and event phases | [Combat](combat-and-powers.md). |
| [data](../../src/main/java/com/vincenthuto/hemomancy/common/data) | datagen and book reload | [Registration](registration-and-content.md), [reloads](world-persistence-and-reloads.md). |
| [effect](../../src/main/java/com/vincenthuto/hemomancy/common/effect) | mob effects and area managers | [Combat](combat-and-powers.md), [visuals](visual-effects.md); compare status ownership before merging hooks. |
| [enchanting](../../src/main/java/com/vincenthuto/hemomancy/common/enchanting) | Scriptorium, Forge, affinities, provenance | [Recipes](recipe-pipelines.md), [item state](progression-and-item-state.md), [reloads](world-persistence-and-reloads.md). |
| [entity](../../src/main/java/com/vincenthuto/hemomancy/common/entity) | mobs, bosses, projectiles, NPCs, animations | [Rendering](entity-rendering-animation.md), [combat](combat-and-powers.md), [worldgen](worldgen-and-encounters.md). |
| [event](../../src/main/java/com/vincenthuto/hemomancy/common/event) | cross-feature event hooks and managers | [Interaction routing](interaction-routing.md), [combat](combat-and-powers.md), [persistence](world-persistence-and-reloads.md). |
| [init](../../src/main/java/com/vincenthuto/hemomancy/common/init) | registries for content, recipes, entities, features | [Registration](registration-and-content.md), [recipes](recipe-pipelines.md), [worldgen](worldgen-and-encounters.md). |
| [item](../../src/main/java/com/vincenthuto/hemomancy/common/item) | items, tools, stack handlers, components | [Item state](progression-and-item-state.md), [interaction routing](interaction-routing.md). |
| [loot](../../src/main/java/com/vincenthuto/hemomancy/common/loot) | global loot modifier | [Registration](registration-and-content.md); audit with loot JSON when content changes. |
| [manipulation](../../src/main/java/com/vincenthuto/hemomancy/common/manipulation) | casting, costs, channels, school powers | [Combat](combat-and-powers.md), [visuals](visual-effects.md), [networking](networking-and-client-projections.md). |
| [menu](../../src/main/java/com/vincenthuto/hemomancy/common/menu) | slot layout, data slots, actions, quick move | [Screens and menus](screens-menus-and-sprites.md), [block entities](block-entities-and-menus.md). |
| [mission](../../src/main/java/com/vincenthuto/hemomancy/common/mission) | NPC assignments and milestones | [Progression](progression-and-item-state.md). |
| [network](../../src/main/java/com/vincenthuto/hemomancy/common/network) | payloads and dispatch | [Networking](networking-and-client-projections.md). |
| [particle](../../src/main/java/com/vincenthuto/hemomancy/common/particle) | particle data/types | [Visuals](visual-effects.md). |
| [recipe](../../src/main/java/com/vincenthuto/hemomancy/common/recipe) | recipe types, schemas, serializers | [Recipes](recipe-pipelines.md). |
| [rite](../../src/main/java/com/vincenthuto/hemomancy/common/rite) | cardinal/Unstained rites, sigils, lasting zones | [Rite lifecycle](rite-lifecycle.md), [persistence](world-persistence-and-reloads.md). |
| [succession](../../src/main/java/com/vincenthuto/hemomancy/common/succession) | residents, profession data, rites | [Rites](rite-lifecycle.md), [reloads](world-persistence-and-reloads.md), [networking](networking-and-client-projections.md). |
| [summon](../../src/main/java/com/vincenthuto/hemomancy/common/summon) | puppet definitions, commands, trial hooks | [Combat](combat-and-powers.md), [interaction routing](interaction-routing.md). |
| [tile](../../src/main/java/com/vincenthuto/hemomancy/common/tile) | machines, storage, transfers | [Block entities](block-entities-and-menus.md), [recipes](recipe-pipelines.md). |
| [vein](../../src/main/java/com/vincenthuto/hemomancy/common/vein) | travel destinations and phases | [Networking](networking-and-client-projections.md), [persistence](world-persistence-and-reloads.md). |
| [world](../../src/main/java/com/vincenthuto/hemomancy/common/world) | folded hallway state/collision | [Worldgen and encounters](worldgen-and-encounters.md), [interaction routing](interaction-routing.md). |
| [worldgen](../../src/main/java/com/vincenthuto/hemomancy/common/worldgen) | features, structures, arenas, encounters | [Worldgen](worldgen-and-encounters.md), [persistence](world-persistence-and-reloads.md). |

## Client code

| Package path | Responsibility found | Cleanup guide or disposition |
| --- | --- | --- |
| [data](../../src/main/java/com/vincenthuto/hemomancy/client/data) | transient client projections | [Networking](networking-and-client-projections.md). |
| [event](../../src/main/java/com/vincenthuto/hemomancy/client/event) | screen/renderer registration, overlays, input | [Screens](screens-menus-and-sprites.md), [rendering](entity-rendering-animation.md), [networking](networking-and-client-projections.md). |
| [item](../../src/main/java/com/vincenthuto/hemomancy/client/item) | client item extensions | [Registration](registration-and-content.md), [rendering](entity-rendering-animation.md). |
| [model](../../src/main/java/com/vincenthuto/hemomancy/client/model) | entity, tile, armor, item geometry | [Rendering and animation](entity-rendering-animation.md). |
| [morphling](../../src/main/java/com/vincenthuto/hemomancy/client/morphling) | mutation attachment visuals | [Rendering](entity-rendering-animation.md). |
| [particle](../../src/main/java/com/vincenthuto/hemomancy/client/particle) | custom particles and factories | [Visuals](visual-effects.md). |
| [player](../../src/main/java/com/vincenthuto/hemomancy/client/player) | casting, pose, injection playback | [Rendering](entity-rendering-animation.md), [networking](networking-and-client-projections.md). |
| [render](../../src/main/java/com/vincenthuto/hemomancy/client/render) | entity, world, block, item passes | [Rendering](entity-rendering-animation.md), [visuals](visual-effects.md). |
| [rite](../../src/main/java/com/vincenthuto/hemomancy/client/rite) | planting/impact state | [Rites](rite-lifecycle.md), [visuals](visual-effects.md). |
| [screen](../../src/main/java/com/vincenthuto/hemomancy/client/screen) | machine, dialogue, tree, radial UI | [Screens, menus, and sprites](screens-menus-and-sprites.md). |
| [sound](../../src/main/java/com/vincenthuto/hemomancy/client/sound) | music, playback, analysis | Treat audio lifecycle as its own future audit; [Clairaudiograph screen](../../src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/ClairaudiographScreen.java) is the UI consumer. |
| [vein](../../src/main/java/com/vincenthuto/hemomancy/client/vein) | travel client state and effects | [Networking](networking-and-client-projections.md), [visuals](visual-effects.md). |

## Runtime resource families checked

[Recipe JSON](../../src/main/resources/data/hemomancy/recipe), [worldgen JSON](../../src/main/resources/data/hemomancy/worldgen), [tags](../../src/main/resources/data/hemomancy/tags), [loot tables](../../src/main/resources/data/hemomancy/loot_table), [dialogue inquiry](../../src/main/resources/data/hemomancy/dialogue_inquiry), [inscriptions](../../src/main/resources/data/hemomancy/discovery_inscription), [sigils](../../src/main/resources/data/hemomancy/ichorian_sigil), [GUI textures](../../src/main/resources/assets/hemomancy/textures/gui), and [GUI sprites](../../src/main/resources/assets/hemomancy/textures/gui/sprites) were included in the path inventory. A Java-only cleanup can miss these contracts.
