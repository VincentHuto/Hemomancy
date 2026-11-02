# Track 1: the heart-node as a personal Fane

**Status (2026-10-29): built, uncommitted.** What later tracks code against:
- `common/world/LobeEnvelope` (pure geometry; `FaneFootprint` delegates, pinned by `FaneFootprintTest`). `canAddLobe` takes a vertical band too: a lobe chains only within the band of the lobe it touches (the Fane passes `UNBOUNDED`).
- `HeartNodeFootprint`, `HeartNodeEntry`, `HeartNodeRelation`, `HeartNodeSettings` (a stub with the bit layout from Track 3's table in row order, `SPREAD` = bit 0 … `EDGE_WARNING` = bit 6, intensity 0–4, default 2; Track 3 owns the accessors, test and screen).
- `HeartNodeSavedData`: `entry`, `nodeOf`, `footprint`, `footprintAt`, `envelopes`, `plant(owner, pos, gameTime)` (returns the replaced entry, synapses included, so Track 2 can wither them), `remove` (the same), `wouldOverlap(planter, pos, radius)`, `strangerTo`, `ownerStrength`, `insideOwnEnvelope`, `addSynapse` (chain + budget + no foreign overlap), `removeSynapse`, `setSettings`.
- `HeartNodeEnvelopePacket` (`common/network/heartnode`, protocol 23) carries `settingsBits` and `intensity` separately. `HeartNodeEnvelopeSync` diff-sends every 10 ticks and on login, respawn and dimension change; `syncAround(level, pos)` resends at once (Track 3's settings change should call it). Client: `HeartNodeClientState` (`envelopes`, `own`, `at`).
- `HeartNodeBlockEntity.plant(owner, gameTime)`, `stage()`, `plantedAt()`, `refreshStage(level)`.
- Review: `heartNodeReviewClient` / `HeartNodeClientReview`. Not yet launched: needs a client run.
- There were no Apotheos Liber pages or teacher lines about the heart-node to update; only HR §5.6d changed.

**Goal:** the heart-node becomes the centre of a Soft Envelope, the way a Consecrated Bloodwell is for a Founding Fane, with Mycelial Synapses (Track 2) as its lobes. Every benefit, protection and record belongs to the planter alone. The rib ring goes.

## Current state
- `HeartNodeRules`: `FOOTPRINT_RADIUS` 8, `FOOTPRINT_HEIGHT` 6, `inFootprint(node, pos)` (XZ circle plus a height band), the rib constants and `ribBase`, `GROW_TICKS` 100, `BONUS_TICKS` 40, `BONUS_SPORES` 4, `CACHE_SEARCH` 8.
- `HeartNodeSavedData` (per level): owner → node position, a removed map for the login respawn clear, `crowds(planter, pos)` (another node within 2× the radius), `strangerTo(actor, pos)`, `takeRemovedSpawn`.
- `HeartNodeBlockEntity`: `owner`, `stage`, `growth`; `serverTick` grows a rib every `GROW_TICKS` until eight are standing (`growRib` places `calcified_hyphae` and caps it with `infected_cap`).
- `HeartNodeEvents.onPlayerTick`: an Apotheos owner inside the footprint gets Regeneration I and +4 spore load every 40 ticks.
- The Founding Fane: `FaneFootprint(heart, stakes, legacyCenter)`, `HEART_RADIUS` 40, `STAKE_RADIUS` 32, `canAddStake(pos, budget)` (connects by overlap, budget 3–12 from members), `effectStrength(pos)` tapering from the heart, `FoundingFaneSavedData`, `FoundingFaneEvents` (buffs every 40 ticks, boundary sync), `PacketSyncFaneBoundaries`. Its distance is horizontal only.

## Target

### Shared envelope geometry
`FaneFootprint` holds the overlap, containment and taper rules as private statics with fixed radii. Extract them into a pure **`LobeEnvelope`** helper in `common/util` (or `common/world`, next to the footprints):
- `contains(heart, lobes, pos, heartRadius, lobeRadius, verticalBand)`
- `canAddLobe(heart, lobes, pos, heartRadius, lobeRadius, budget)`: chains by overlap to the heart or to an existing lobe, refuses a lobe too far from any of them, and refuses once the budget is spent.
- `strength(heart, lobes, pos, heartRadius, lobeRadius)`: 1 at the heart, tapering through the lobes the way `FaneFootprint.effectStrength` does now.
- `maxRadius(...)`

`FaneFootprint` delegates to it with its own constants and an unlimited vertical band, so Fane behaviour does not change by one block. `FaneFootprintTest` (and every Fane GameTest) must stay green without edits. That is the proof the extraction is a pure refactor.

### The heart-node footprint
**`HeartNodeFootprint(owner, heart, synapses)`** in `common/world`, built on `LobeEnvelope`. Starting values, all in `HeartNodeRules`, to tune in the review client:

| Value | Start | Why |
| --- | --- | --- |
| `HEART_RADIUS` | 12 | A Gardens chamber, not a county; the Gardens are dense and layered. |
| `SYNAPSE_RADIUS` | 8 | Each synapse adds a lobe; chaining reaches out along a tunnel or across a cavern. |
| `SYNAPSE_BUDGET` | 6 | Fixed: there's no bloodline to scale with. Room for one bigger chamber or a short corridor of lobes. |
| `VERTICAL_BAND` | 8 | Up or down from the heart or synapse that claims the cell, so one envelope never claims the layer above or below. The Gardens are a 3D projection, and the Fane's horizontal-only rule would swallow whole stacked caverns. |

- `contains(pos)`, `strength(pos)`, `canAddSynapse(pos)`, `withSynapse` / `withoutSynapse`, `withoutHeart`. Records and immutable, like `FaneFootprint`.
- `overlapsAnother(otherFootprint)` and `wouldOverlap(pos, radius)`: a node or synapse may not be planted where its lobe would intersect **another owner's** envelope. This replaces `crowds`. It keeps the overlap-takeover fix from the first review: a planter can never claim a cell already inside someone else's sanctum.

### Saved data
`HeartNodeSavedData` keeps one **`HeartNodeEntry`** per owner instead of a bare position: `heart`, `synapses` (ordered list), `settings` (Track 3's `HeartNodeSettings`, defaulted here so Track 3 only fills it in), `plantedAt` (game time, used by the spread's growth in Track 4).
- NBT: keep the existing per-owner key layout and add `Synapses` and `Settings` beside the position. Loading an old save gives an entry with no synapses and default settings. No player data migration is needed: this is level data, and the old format is a strict subset. A load test in `HeartNodeSavedDataTest` pins it with an old-format tag.
- `footprint(owner)`, `footprintAt(pos)` (whose envelope a cell sits in, or none), `strangerTo(actor, pos)` (now: inside someone else's envelope, not inside their 8-block circle), `addSynapse(owner, pos)`, `removeSynapse(owner, pos)`, `envelopes()` for the sync.
- `plant` withers the planter's previous node **and removes its synapse records**. The synapse blocks left in the world go inert and drop themselves (Track 2 handles the blocks). `remove` (the node broken) does the same.

### Owner-only effects (the personal rule)
- **Benefits** (`onPlayerTick`): every 40 ticks the owner, if alive, an Apotheos, in the Gardens and inside their own envelope, gets Regeneration I and spore load scaled by `strength(pos)`: full in the heart lobe, tapering to half at the far edge of the outermost synapse. Nobody else gets anything, whether bloodline member, ally or Brotherhood. There is no "member" concept; write the check as `entry.owner().equals(player.getUUID())` and test it with a second player standing on the heart.
- **Death cache**: an owner who dies anywhere inside their envelope leaves a fruiting-body cache at `cacheSpot` near the death (not near the node). A death outside it drops normally. Strangers never make a cache.
- **Respawn**: unchanged (forced respawn on the node). The node is the respawn point, not a synapse.
- **Privacy**: strangers can't break or place inside the envelope (creative bypass kept). Synapses are inside by definition. Explosions inside the envelope don't break envelope blocks (`ExplosionEvent.Detonate`, filter the affected list), extending the node's existing explosion-proofing to the sanctum. Pistons pushing into or out of the envelope are refused as in `MausoleumEvents`.
- **No Fane interplay**: `FoundingFaneEvents` never reads heart-node data, and `HeartNodeEvents` never reads `FoundingFaneSavedData`. Planting a heart-node does not need, touch or count against a Founding Fane, its stake budget or its bloodline. A guardrail test asserts neither class names the other's saved data.

### Rib removal
- Delete the rib constants, `ribBase`, `growRib` and the rib branch of `serverTick`. `stage` stays: it now counts the node's maturity for the model (Track 5) and the spread (Track 4), rising from 0 to 3 over `MATURE_TICKS` (start: 3 minutes) from `plantedAt`.
- Ribs already in worlds are left alone. They are ordinary blocks the owner can break.
- The rib GameTests are replaced by maturity GameTests (stage rises, no blocks are placed).

### Sync to the client
**`HeartNodeEnvelopePacket`** (clientbound): a list of `{owner, heart, synapses[], settingsBits, relation, plantedAt, stage}`, where `relation` is `OWNER` or `STRANGER`.
- Sent to a player on entering the Gardens and whenever an envelope within `maxRadius + 64` changes. Model it on `FoundingFaneEvents.syncFaneBoundaries`, but send only envelopes near the player.
- Owner sync for a player's own node rides the same packet, so `PlayerStateSync` has nothing new to send.
- Client side: a `HeartNodeClientState` cache of envelopes, cleared on logout and dimension change (client session clears).

Bump `PROTOCOL_VERSION` and rewrite the payload registry.

## Tasks
1. `LobeEnvelope` extraction with `FaneFootprint` delegating; Fane tests green unchanged.
2. `HeartNodeFootprint` and `HeartNodeRules` values; `HeartNodeFootprintTest` covering containment, the vertical band, taper, chaining, budget and cross-owner overlap.
3. `HeartNodeEntry` saved data, the old-format load test, synapse records cleared on replant and removal.
4. Owner-only benefits scaled by strength; the cache follows the envelope; privacy across the envelope, including explosions and pistons.
5. Rib removal and maturity stages; replace the rib GameTests.
6. `HeartNodeEnvelopePacket`, the sync service and `HeartNodeClientState`; protocol bump.
7. `heartNodeReviewClient` row and driver (plant, capture, check the packet arrived with the right relation for owner and stranger).
8. HR §5.6d rewrite, Liber and teacher lines.

## Acceptance
- A second player standing on the heart of someone else's node gets no Regeneration, no spore load, no cache and no respawn. They can't break or place inside the envelope.
- The owner's benefit tapers with distance and stops at the envelope's edge and outside the vertical band.
- A node can't be planted where its 12-block heart lobe would touch another owner's envelope.
- Founding Fane behaviour is byte-for-byte unchanged (its tests are untouched and green).
- Old saves load. A node planted before the rework keeps its owner, position and respawn.
