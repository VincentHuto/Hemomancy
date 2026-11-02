# Gameplay systems consolidation map

This is a source-based map for reducing repeated gameplay plumbing in Hemomancy. It describes the checkout as inspected on 2026-09-25; it is a work queue, not a claim that every similar class should share a base class. Source and runtime resources outrank this map when they change. Tests, fixtures, generated output, and dormant `compat/mna` / `compat/curios` code are outside this inventory.

## Stage folders

Each folder starts with a short index linking its audit, implementation prompt, and related shared guides. The cross-cutting guides remain here so the same source map can support more than one stage.

| Stage | Audit focus | Implementation prompt |
| --- | --- | --- |
| [1 — Capabilities and event order](stage-1-capabilities/README.md) | Attachment ownership, event phases, and observer sync. | [Implement Stage 1](stage-1-capabilities/IMPLEMENTATION_PROMPT.md) |
| [2 — Player snapshots](stage-2-player-snapshots/README.md) | Tendency and vascular lifecycle dispatch. | [Implement Stage 2](stage-2-player-snapshots/IMPLEMENTATION_PROMPT.md) |
| [3 — Distillation](stage-3-distillation/README.md) | Alembic and Retort recipe, result, and extraction paths. | [Implement Stage 3](stage-3-distillation/IMPLEMENTATION_PROMPT.md) |
| [4 — Unstained zones](stage-4-unstained-zones/README.md) | Still Waters, Pale Consecration, and Lethe Covenant persistence and expiry. | [Implement Stage 4](stage-4-unstained-zones/IMPLEMENTATION_PROMPT.md) |
| [5 — Registration](stage-5-registration/README.md) | Block-item decisions, creative routing, and content resources. | [Implement Stage 5](stage-5-registration/IMPLEMENTATION_PROMPT.md) |
| [6 — Combat](stage-6-combat/README.md) | Attack provenance, damage phases, and death rewards. | [Implement Stage 6](stage-6-combat/IMPLEMENTATION_PROMPT.md) |
| [7 — Networking](stage-7-networking/README.md) | Packet authority, recipient scope, and client cleanup. | [Implement Stage 7](stage-7-networking/IMPLEMENTATION_PROMPT.md) |

## Read by task

| If you are changing… | Read | Existing center of gravity |
| --- | --- | --- |
| particles, ribbons, glows, visual budgets | [Visual effects](visual-effects.md) | `client/render/world`, `ParticleInit` |
| mob materials, layers, model pose | [Entity rendering and animation](entity-rendering-animation.md) | `client/render/entity`, `client/model/entity` |
| player progress, death persistence, client state | [Player state and sync](player-state-and-sync.md) | `HemoAttachmentTypes`, `HemoCapabilityAccess`, `PacketHandler` |
| capability storage, event lifecycle, observer snapshots | [Capability storage index](capability-storage-index.md), [lifecycle and observer sync](capability-lifecycle-and-observer-sync.md), [Stage 2 snapshot audit](stage-2-player-snapshots/README.md) | attachment registry, capability registrar, feature event handlers |
| player/entity damage and tick event phases | [Entity and player event phases](entity-and-player-event-phases.md), [Stage 1 event-order audit](stage-1-capabilities/README.md), [Stage 6 combat order](stage-6-combat/README.md) | `SchoolCombatEvents`, capability event handlers |
| persistent world records, JSON gameplay definitions | [World persistence and reloads](world-persistence-and-reloads.md), [Stage 4 Unstained zones](stage-4-unstained-zones/README.md) | `SavedData`, reload listeners |
| items, blocks, entities, resource contracts | [Registration and content](registration-and-content.md), [Stage 5 registration audit](stage-5-registration/README.md) | `common/init`, `Hemomancy` |
| damage hooks, attacks, casting, armor powers | [Combat and powers](combat-and-powers.md), [Stage 6 combat order](stage-6-combat/README.md) | `SchoolCombatEvents`, manipulation managers |
| machine storage, menus, block interaction | [Block entities and menus](block-entities-and-menus.md), [Stage 3 distillation audit](stage-3-distillation/README.md) | `common/tile`, `common/menu`, `common/block` |
| payloads, client snapshots, disconnect cleanup | [Networking and client projections](networking-and-client-projections.md), [Stage 7 packet ownership](stage-7-networking/README.md) | `PacketHandler`, `client/data` |
| natural spawning, feature placement, encounters | [Worldgen and encounters](worldgen-and-encounters.md) | `EntityInit`, `common/worldgen` |
| recipe JSON, codecs, machine crafting | [Recipes and execution](recipe-pipelines.md), [Stage 3 distillation audit](stage-3-distillation/README.md) | `RecipeInit`, `common/recipe`, machine block entities |
| screen layout, menu slots, sprites, spacing | [Screens, menus, and sprites](screens-menus-and-sprites.md) | `ContainerInit`, `ClientEvents`, `client/screen` |
| rite phases, event ticks, cancellation | [Rite lifecycle](rite-lifecycle.md), [Stage 4 Unstained zones](stage-4-unstained-zones/README.md) | `ActiveCardinalRite`, rite event handlers |
| missions, item components, item inventories | [Progression and item state](progression-and-item-state.md) | `common/mission`, `DataComponentInit`, item handlers |
| right-click precedence and access gates | [Interaction routing](interaction-routing.md) | block overrides and `PlayerInteractEvent` handlers |

## Recommended sequence

1. **P0 — Write down ownership and lifecycle before extraction.** For each candidate, identify the authoritative server state, client projection, event phase, persistence key, and side effects. The guides name concrete first candidates.
2. **P1 — Consolidate clear shared transactions.** The Alembic/Retort distillation pipeline, machine change notification, and Unstained zone expiry are the strongest candidates. Keep their distinctive resource policies.
3. **P1 — Centralize repeated dispatch and layout contracts.** Reload scanning, player snapshots, menu action validation, and machine panel geometry have narrow seams.
4. **P2 — Standardize visual recipes and pose reset rules.** Reuse authored materials and shared geometry without flattening creature silhouettes or distinct school effects.
5. **P2 — Audit registration decisions and combat ordering.** Keep registry IDs stable. Document auto block-item exceptions; map damage phases before moving handlers.

| First concrete candidate | Reason to start there | Keep stable |
| --- | --- | --- |
| Machine change notification | Several block entities repeat dirty/update/comparator steps | Inventory authority and display packets |
| Reload listener resource scanning | Repeated file discovery and parse diagnostics | Resource IDs and per-schema rules |
| Player lifecycle snapshot dispatch | Login/respawn sync is spread across state event classes | Attachment IDs, death semantics, packet payloads |
| Timed Unstained zone codec | Three related zone records use similar NBT loops | Saved-data names and zone behavior |
| Block-item decision catalog | Auto-registration, custom items, manual entries, and skip list are split | Registry IDs and itemless exceptions |
| Alembic/Retort distillation transaction | Same recipe type and repeated lookup/output/XP logic | Fluid policy, NBT keys, item counts |
| Machine screen layout | Repeated inventory panel, bars, labels, and slot decoration | Menu slot coordinates and theme |

[Source-path coverage](source-path-coverage.md) records which gameplay package families were scanned and which guide owns each finding.
[Consolidation backlog](consolidation-backlog.md) ranks specific extraction seams and records the behavior each one must preserve.
[Packet registration index](packet-registration-index.md) lists every payload registration and direction currently in `PacketHandler`.

## How to use these guides

Each guide separates **current paths**, **overlap**, **target contract**, and **safe migration slice**. “Overlap” means similar responsibility, not proven identical behavior. Move one feature family at a time, retain registry IDs and saved-data keys, then verify the affected gameplay path. Do not turn a one-off authored effect, boss renderer, or unusual machine into a generic framework merely because its Java shape looks similar.

The wiring hub is [Hemomancy.java](../../src/main/java/com/vincenthuto/hemomancy/Hemomancy.java); the existing [technical reference](../HEMOMANCY_REFERENCE.md) describes wider mechanics. This map is intentionally narrower and focused on cleanup decisions.
