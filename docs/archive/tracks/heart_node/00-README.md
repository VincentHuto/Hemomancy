# Heart-node rework — work paths

Date: 2026-10-29. The overnight Section 3 build (HR §5.6d; `HeartNodeRules`, `HeartNodeSavedData`, `HeartNodeBlock` / `HeartNodeBlockEntity`, `FruitingBodyCacheBlock`, `HeartNodeEvents`) made the heart-node a small fixed sanctum: an 8-block circle, eight calcified-hyphae ribs it grows around itself, a Regeneration and spore bonus, break and place privacy, a forced respawn and a death cache. It works, but it doesn't play like a Fane. It can't grow, it builds its own walls out of real blocks, and the node is a hand-written cube model.

**The new shape:** the heart-node is the Apotheos's **personal Fane** in the Gardens. It stands in for the Consecrated Bloodwell as the heart of a Soft Envelope. **Mycelial Synapses** extend that envelope the way Hematic Stakes extend a Founding Fane. Only the planter benefits: it is a personal spot, never a bloodline's. A **Heart-node screen** sets what it shows. Its edge is not a bubble: a living mycelial layer **spreads over the ground**, drawn by a shader in the style of the mausoleum ward but organic.

The heart-node does not replace the Founding Fane. An Apotheos keeps their bloodline's Founding Fane in the Overworld (or any other dimension it was founded in) and has a heart-node in the Gardens as well. The two never share range, members, buffs or data.

Each file below is one independent track. A session takes one track, reads this README first, and leaves the others alone.

| # | Track | File | Depends on |
| --- | --- | --- | --- |
| 1 | The heart-node as a personal Fane: footprint, saved data, owner-only effects, rib removal | [01-personal-fane-model.md](01-personal-fane-model.md) | — |
| 2 | Mycelial Synapse: the Blood Structure that extends the envelope | [02-mycelial-synapse.md](02-mycelial-synapse.md) | 1 (footprint API) |
| 3 | The Heart-node screen: visual toggles and the sanctum's state (built 2026-10-29) | [03-heart-node-screen.md](03-heart-node-screen.md) | 1 (settings live in its saved entry) |
| 4 | The mycelial spread: a ground-creeping shader instead of a bubble (built 2026-10-29) | [04-mycelial-spread-shader.md](04-mycelial-spread-shader.md) | 1 (sync payload); reads 3's settings |
| 5 | Set pieces: a generator-authored heart-node and synapse, planting and withering presentation (built 2026-10-29) | [05-heart-node-set-pieces.md](05-heart-node-set-pieces.md) | — |

Track 1 lands first, or at least its `HeartNodeFootprint` API and the `HeartNodeEnvelopePacket` record do: Tracks 2, 3 and 4 code against them. Tracks 4 and 5 are client-heavy and can run in parallel with 2 and 3 once 1's records exist. Track 5 owns the block models; Track 2 owns the synapse's block class and recipe and uses a placeholder model until 5 lands.

## What stays
- `HeartNodeBlock` / `HeartNodeBlockEntity` (owner, stage), the shapeless recipe (bleeding heart, 4 spore sacs, conscious mass).
- The planting refusals in `HeartNodeEvents.onPlace` (HIGH priority): Apotheos only, the Gardens dimension only, network ground only. The "crowds another node" check becomes an envelope-overlap check (Track 1).
- `plant()` at LOWEST priority: the forced respawn, withering the planter's previous node, one node per player.
- The respawn clear through `HeartNodeSavedData.takeRemovedSpawn` at login.
- The fruiting-body death cache (`FruitingBodyCacheBlock` / BE, `LivingDropsEvent` LOWEST, `cacheSpot`), now triggered by a death anywhere inside the owner's envelope rather than within 8 blocks of the node.
- Break and place privacy for strangers. Strangers are kept from editing the sanctum; they get nothing from it.

## What goes
- The rib ring: `RIB_RADIUS`, `RIBS`, `RIB_HEIGHT`, `ribBase`, the growth loop that places `calcified_hyphae` and `infected_cap` blocks, `GROW_TICKS` as a rib timer (Track 1). Ribs already placed in worlds stay as ordinary blocks; nothing deletes player-visible blocks.
- `FOOTPRINT_RADIUS` / `FOOTPRINT_HEIGHT` as the sanctum's only extent (Track 1 replaces them with the envelope).
- `HeartNodeSavedData.crowds` (2× radius) as the overlap rule (Track 1).
- The hand-written cube `models/block/heart_node.json` and its single texture (Track 5).

## Shared rules for every track
- Read `AGENTS.md` and `docs/architecture/ARCHITECTURE_STANDARDS.md` first. Lang keys only; particles through `HemoParticleData`; render types as static constants or `SeededRenderTypes`; world-space drawing from `WorldEffectDispatcher`; `AnimationTime` for animation; HUD through `GuiLayerRegistrar`; nothing under `common/` names a client class.
- Payloads go through `PacketHandler` (bump `PROTOCOL_VERSION`, rewrite `src/test/resources/network/payload-registry.txt`), and serverbound claims are checked with `ServerboundGuards`. A payload decodes, validates and calls one service method.
- Tracks 1, 3 and 5 each add payloads. Parallel sessions will collide on `PROTOCOL_VERSION` and the registry file: bump by one from whatever is on disk when you land, and regenerate the registry file rather than merging it by hand.
- Blood paid for a synapse goes through the Blood Structure feed (`BloodAccounting.pay` via `BloodStructureFeedManager`), never a direct drain.
- Lore: the Gardens are a 3D projection of the Fungal Entity's memory, and only an Apotheos travels there in body. The heart-node is a scar the Apotheos opens in it, a place the Gardens remember as theirs. No copper, brass or bronze. Hematic iron, bone, flesh and mycelium only.
- 16 px per block texel density. Set-piece models come from a `tools/model_export/` generator (`.bbmodel` and preview PNG included). Hand-written JSON models are not acceptable.
- Frozen fixtures (`registry-ids.txt`, `creative-tab-order.txt`), the item-less block list in `RegistrationGameTests` and `BlockInit.ITEMLESS_BLOCKS` change with every new block.
- Every track ends with client proof: Track 1 sets up `heartNodeReviewClient` (see below), and each later track adds its scenes to it.
- Update HR §5.6d, the Apotheos Liber entries and any teacher lines with the mechanics. The `wiki/` page for the Gardens too, if one exists.
- No commits; leave work in the tree.

## Review client
Track 1 adds `heartNodeReviewClient` as a row in `gradle/review-clients.gradle` (driver `HeartNodeClientReview` under `src/gameTest`, the same operator-inbox pattern as `possessionReviewClient` and `adipocereCrustReviewClient`). It makes an Apotheos test player in the Gardens, plants a node on network ground and captures it. Track 2 adds a synapse chain, Track 3 opens the screen and flips each toggle, Track 4 captures the spread at several growth stages, by day and in the dark, as owner and as stranger, and Track 5 captures the planting and withering beats. Screenshots go to `build/heart-node-review/screenshots`.

## Ledger from the first pass
`.superpowers/sdd/OVERNIGHT_ROADMAP.md` (Section 3) and the Section 3 entry in `docs/superpowers/OVERNIGHT_REPORT_2026-10-28.md` record the review findings that were fixed: overlap takeover, respawn wipe, plant ordering, cache breakability. Their tests (`HeartNodeRulesTest`, `HeartNodeSavedDataTest`, the heart-node GameTests) must stay meaningful. Rewrite them where the rule changes; don't just delete them.
