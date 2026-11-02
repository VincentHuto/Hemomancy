# Circus Implementation Workpacks

This sequence delivers the smallest coherent Circus pass first. A later workpack may begin only when its listed gates and dependencies are closed.

## WP-1 — Canon and source synchronization

**Status:** Completed 2026-09-01. The canonical lore snapshot now names six artificial bodies, and the lore/mechanics references record the settled sect, layered-reality, natural-animal, reuse, and worldgen boundaries.

**Depends on:** Nothing.

**Deliver:**

- Reconcile current lore/mechanics references with the six live puppet definitions.
- Add the settled Circus identity, layered-reality rule, release boundary, and natural-animal distinction to canonical references only where they are genuinely settled.
- Keep every candidate and gate labeled as such.

**Complete when:** Canon, mechanics reference, this suite, translations, and live registrations do not contradict one another about roster, faction status, or release scope.

## WP-2 — Physical location and worldgen shell

**Status:** In progress. The structure type, biome tag, structure set, template pool, generated 33×12×33 physical pavilion, 49×49 terrain sampler, and focused placement/resource checks are implemented. Automated checks cover dry land, five-block maximum surface variation, template bounds/palette, and an unobstructed entrance aisle. Fresh-chunk placement, visual fit, and navigation still require a live world check before this workpack is complete.

**Depends on:** Select biome list, rarity/spacing, footprint, terrain policy, and initial piece layout.

**Deliver:**

- A registered jigsaw structure using current structure patterns.
- Physical pavilion templates, pool, structure/structure-set data, biome tag, and locate/test support.
- A stable site anchor and bounding area for later perception and encounter systems.

**Complete when:** Fresh chunks place the structure only on valid terrain, pieces stay bounded, the physical site is navigable without effects, and existing structures/player builds are not runtime-overwritten.

## WP-3 — Discovery and Mnemonist introduction

**Status:** In progress. Degree 4 is selected. Server-owned discovery, early-discovery acknowledgment, Mnemonist introduction branches, and a reissuable Circus waybill using the existing lodestone implementation are wired and covered by focused branch/resource checks. Live structure detection, save/reload persistence, dialogue presentation, and waybill guidance still require an in-game check.

**Depends on:** WP-2. Degree 4 timing is settled.

**Deliver:**

- Server-authoritative discovery state for the specific Circus location or location class.
- An authored Mnemonist lead at the chosen degree.
- Separate undiscovered and already-discovered dialogue paths.
- A recoverable way to find or return to the site consistent with existing map/waybill conventions.

**Complete when:** Discovery survives save/load, early explorers receive acknowledgment, repeat dialogue does not grant duplicate rewards, and no parallel reputation system exists.

## WP-4 — Acclimation and layered presentation

**Status:** Implemented pending live presentation checks. Acclimation persists per player from 0-1000, advances one point per 80 pavilion ticks, awards the four performer, spider, pact, and act milestones once, and gates Ringmaster dialogue at 500. Per-view screen edges, particles, sounds, world-space silhouettes, cloth, lights, and performer echoes progress from Disturbed instability through Acclimating agreement to a stable Attuned spectacle. Login/logout/respawn/dimension changes clear active presentation. Live two-client, reconnect, shader-disabled, and visual-fit checks remain.

**Depends on:** WP-2; settle exposure time, decay, persistence, route influence, and stage thresholds.

**Deliver:**

- One compact server-owned acclimation value/state.
- Area tracking and synchronization for the viewing player.
- Staged heat-haze, light, sound, silhouette, cloth, and manifestation presentation.
- Cleanup on exit, death, dimension change, logout, and server stop.

**Complete when:** Two players can see different presentation stages while agreeing on real entities/collision, state moves from unstable to coherent, and no stale client effect survives lifecycle transitions.

## WP-5 — First mechanical Circus vertical slice

**Status:** Implemented through the trained peacock-spider attention act before route commitment and the existing troupe/Crossbar behavior seams.

**Depends on:** WP-3 and WP-4.

**Deliver:**

- Use the implemented Scarlet Mummer as the mechanical teaching anchor.
- Add one authored performance or specialist interaction using existing Crossbar, Spindle, staff, dialogue, and target-control seams.
- Teach attention/decoy behavior without adding a new progression framework.

**Complete when:** The first visit demonstrates a meaningful Circus mechanic, ownership and cleanup follow existing puppet rules, and the experience remains understandable without accepting a permanent route.

## WP-6 — One candidate creature

**Status:** In progress. The Luminal Cicada is selected and implemented as a non-hostile forest ambient mob. It seeks exposed log faces, clings and buzzes, flashes visibly at four blocks on a five-second cooldown, drops no death loot, and participates in the existing Specimen Jar capture/release path. Automated behavior/resource checks pass; live movement, sound, flash readability, spawning, rendering, and jar fit remain to be verified.

**Depends on:** The Luminal Cicada ecology, role, habitat, representation, reward, and accessibility decisions are closed for the first pass.

**Default priority:** Choose whichever candidate reuses the most existing behavior without duplicating a live role. Do not implement all three to establish the faction.

**Deliver:** One complete creature slice: behavior, model/texture, spawn or venue placement, bestiary/language, sound, loot or non-loot ecology, and focused tests.

**Complete when:** The creature does something recognizable before combat, bloodcraft meaningfully changes or explains its behavior, and it does not crowd an existing ecological or summon niche.

## WP-7 — Relationship routes and rewards

**Status:** Implemented. Neutral, warning-and-confirmed Succession or Liberation, one pre-completion repair, per-player prerequisites, site-global initiator ownership, deterministic first completion, and duplicate-safe rewards are server authoritative. The spent repair is no longer offered in dialogue.

**Depends on:** Close route trigger, warning, reversibility, multiplayer ownership, repair, and both reward designs.

**Deliver:**

- Neutral entry and explicit cooperation/hostility transitions.
- Persisted server-authoritative route state.
- Route-aware dialogue and encounter behavior.
- Distinct cooperative and antagonistic rewards with duplicate protection.

**Complete when:** Both routes are legible, neither silently triggers, multiplayer credit is deterministic, and rejecting puppetry does not award another puppet.

## WP-8 — Ringmaster encounter

**Status:** Implemented mechanically. The neutral attention act now proves cooperation before the warning-and-confirmed pact choice; Succession then offers the remaining three controlled acts with begin/midpoint/completion feedback. Rafter/carousel/descent phases, real troupe pressure, manifested bat aggregate, route-specific lethality, clean retry, persistent site outcome, Ringmaster Pattern, Thread Ripper, and wearable topper are wired. Automated full-route journeys cover Succession and Liberation at Degree 4, including fixture snapshot restoration and site cleanup. Focused GameTests also cover stale player-state rejection, controlled-performer ownership through entity reload, death/failure retry, the 200-tick absent-owner reset, active-site contention, and pavilion outcome persistence through a `SavedData` round trip. Combat feel, visuals, real multiplayer, and real restart/rejoin still require live validation.

**Depends on:** WP-7 plus Ringmaster identity, phase, arena, failure/reset, and reward gates.

**Deliver:** A focused midgame culmination using specialized variants of established Circus verbs and puppet roles.

**Complete when:** The encounter is not a Vesper duplicate, failure cannot corrupt route/progression state, owned/trial bodies clean up reliably, and each route reaches its intended result.

## WP-9 — Carousel

**Status:** Implemented as a troupe-linked arena and Liberation mechanism. The three captives are the failed initiates riding the three scarred horses; severing each rider exposes that horse's anchor, and all three anchors must be broken to destroy the carousel.

**Delivered:** A centered structure entity, direct-export Java/Blockbench model, authored base and glow textures, three rotating and bobbing horse puppets, ambient motion, ALERT windup, and target-scoped horse-arc damage.

**Remaining live checks:** Confirm pavilion fit, animation readability, sound balance, collision, and multiplayer target synchronization in fresh generated structures.

## Verification matrix

| Area | Automated evidence | Live evidence |
| --- | --- | --- |
| Worldgen | Registration/resource checks and placement-rule tests | Fresh chunks across intended terrain |
| Discovery | Persistence and dialogue-branch tests | Early discovery, normal introduction, return visit |
| Acclimation | State-transition and packet/source tests | Two clients at different stages; reconnect and dimension change |
| Puppetry | Existing summon rules plus focused GameTests | Crossbar commands, tracking, dismissal, starvation |
| Creatures | Pure behavior rules and resource guards | Navigation, animation, sound, crowd performance |
| Routes | Transition, duplicate reward, initiator ownership, active-site contention, absent-owner reset, persisted outcome, and automated full-route journeys | Cooperative and hostile live runs; real disconnect/rejoin and two-client contention |
| Ringmaster/carousel | Encounter-state, failure/retry, and cleanup GameTests | Combat feel, arena bounds, visual readability |

Do not treat a JVM/source test as proof of visuals, worldgen fit, combat feel, multiplayer synchronization, or persistence across a real server restart.
