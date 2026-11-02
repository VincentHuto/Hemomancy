# Track 3: the Heart-node screen

**Status (2026-10-29): built, uncommitted.** What later tracks code against:
- `HeartNodeSettings`: bit layout unchanged from Track 1's stub (`SPREAD` bit 0 … `EDGE_WARNING` bit 6, intensity 0–4, default 2, `SHOW_STRANGERS` off), now with `with(bit, on)` / `withIntensity`; pinned by `HeartNodeSettingsTest`. **Track 4 reads `HeartNodeClientState.Envelope.settings()`**, which already arrives through `HeartNodeEnvelopePacket`; a settings change calls `HeartNodeEnvelopeSync.syncAround` so it lands within a tick or two.
- `HeartNodeFootprint.chainedFrom(synapse)`: the lobe centre (heart or earlier synapse) a connected synapse's thread runs from; null when cut off. The screen draws its threads with it, and Track 4 can too.
- Payloads (protocol 24): `PacketOpenHeartNodeScreen` (clientbound, `common/network/heartnode`; node, owner and name, synapses with facing and connected, settings, `plantedAt`, respawn-bound, waiting caches, `readOnly`) and `HeartNodeSettingsPacket` (serverbound; `accept` checks `ServerboundGuards.nearBlockEntity` at block reach and the node's owner, then `HeartNodeService.updateSettings`).
- `common/world/HeartNodeService`: `open(viewer, node)` (owner editable, creative op read-only, others get `not_yours`; a node with no entry says `no_sanctum`), `snapshot`, `updateSettings`.
- Caches are now queryable: `HeartNodeSavedData.addCache` / `removeCache` / `caches(owner)` (NBT `Caches`); `HeartNodeEvents.onDrops` records, `FruitingBodyCacheBlock.onRemove` forgets, the snapshot drops records whose loaded block is gone. Caches made before this pass aren't listed.
- Edge warning: `HeartNodeEvents.onEdgeTick` (every `EDGE_CHECK_TICKS` 10, `HeartNodeRules.warnsLeaving`, gap `EDGE_WARNING_TICKS` 200), session map `EDGE_WATCH` cleared through `TransientServerStateEvents`.
- Screen: `client/screen/tile/functional/HeartNodeScreen` (300×212, no blur so the world shows; map left, state right, 7 toggles + Growth slider + Reset in a 3×3 grid; sends at once, then at most every 5 ticks, and flushes on close). Chrome: `ScreenDrawUtils` border/accents, `VeinBackgroundRenderer.Style.MYCELIAL` (new `Palette.MYCELIAL`, `Speckles.Scattered.SPORE`). In `ScreenChromeReview` as `heart_node`.
- Tests: `HeartNodeSettingsTest`, new cases in `HeartNodeRulesTest` (time to next stage, leave-line rule), `HeartNodeSavedDataTest` (caches), `HeartNodeFootprintTest` (`chainedFrom`); GameTests `HeartNodeScreenGameTests` (owner opens, stranger refused, stale cache dropped; stranger / out-of-reach / wrong-block packets ignored, rules unchanged with everything off).
- Review: `HeartNodeScreenScenes` from `HeartNodeClientReview` (leave line, screen opened through the block, each toggle flipped and checked 5 ticks later, saved bits; captures `heart-node-8-screen`, `heart-node-9-<n>-<toggle>`). **Not launched yet: needs a client run.**

**Deviations (review these):**
- No Liber line: there is still no Apotheos section in the book (Tracks 1 and 2 found the same). HR §5.6d and `wiki/World-Content.md` carry the screen.
- The operator read-only path is not covered by a GameTest (it needs a real op entry); it is one branch in `HeartNodeService.open`.
- The map draws every synapse in the record; withered synapse blocks have no record, so "active or withered" shows as chained vs cut off (cut-off synapses still hold their lobe, per Track 2).
- Toggles are vanilla buttons labelled `Name: ON/OFF`, and the slider notches are named Hint / Lace / Felt / Thick / Carpet. "Show strangers" is labelled **Strangers**, "edge warning" **Leave line**, "motes" **Spores**.
- Until Track 4 lands, only the synced bits change; the toggle captures show the screen over an unchanged world.

**Depends on:** Track 1's `HeartNodeEntry.settings` field and `HeartNodeEnvelopePacket` (the settings ride it to the client). Track 4 reads the settings when it draws, so agree the bit layout here first and pin it in a test.

**Goal:** right-clicking your own heart-node opens a screen that shows the state of your sanctum and lets you set how it looks: the spread, its edge, the synapse threads, the ambient spores, and whether strangers see any of it. It is a control panel, not an inventory. No slots, no menu container.

## Current state
- `HeartNodeBlock` has no use action.
- The pattern to follow is `ConsecratedBloodwellBlock.useWithoutItem` → `PacketHandler.sendToPlayer(player, new PacketOpenBloodlinePoolScreen())` → a clientbound handler opens the screen. The block never names a client class (`CommonClientIsolationTest`).
- Screen chrome comes from `ScreenDrawUtils` and `VeinBackgroundRenderer` with a per-screen `Style` (`ScreenChromeGuardrailTest`). New screens are added to `ScreenChromeReview`.

## Target

### Settings
**`HeartNodeSettings`** (record, `common/world`), stored in the owner's `HeartNodeEntry` with a codec / NBT pair, and sent as a compact bit set plus one byte of intensity:

| Setting | Type | Default | What it does |
| --- | --- | --- | --- |
| `spread` | on/off | on | Track 4's ground layer at all. |
| `intensity` | 0–4 | 2 | Spread opacity and vein density, from a hint at 0 to a thick carpet at 4. |
| `edge` | on/off | on | The feathered frond line along the envelope's edge (the "where does my sanctum end" read). |
| `threads` | on/off | on | The glowing hyphal threads from the heart to each synapse along the ground. |
| `pulse` | on/off | on | The heartbeat wave that rolls outward through the spread. |
| `motes` | on/off | on | Ambient spore motes drifting up from the spread (client particles, `HemoParticleData`). |
| `showStrangers` | on/off | off | Whether other players see the spread at all. When off, they see nothing but the blocks, which keeps a personal place hidden. When on, they see a dimmer, desaturated version with no threads. |
| `edgeWarning` | on/off | on | A short chat-bar line when the owner crosses out of their envelope ("You leave the sanctum."), at most once per 10 s. |

Each setting is purely presentational, except `edgeWarning`, which is a message. **No setting changes a rule:** benefits, privacy, cache and respawn never toggle. Keeping the rules unchangeable keeps the settings safe to send from the client.

`HeartNodeSettingsTest` pins the defaults, the bit layout, the intensity clamp, and a round trip through NBT and the packet encoding.

### Opening
- `HeartNodeBlock.useWithoutItem`: the owner (server side) gets `PacketOpenHeartNodeScreen(pos)`. A stranger gets `message.hemomancy.heart_node.not_yours` and nothing opens. Creative operators may open anyone's (read-only, Save disabled), which helps support.
- Client: `ClientboundHeartNodeHandlers.open(pos)` in `client/network`, registered as `ClientPayloadHandlers::dispatch`. It reads the player's own envelope from `HeartNodeClientState` (Track 1) and opens `HeartNodeScreen`.

### Saving
- **`HeartNodeSettingsPacket(pos, bits, intensity)`** (serverbound). Validate with `ServerboundGuards`: the player is in reach of `pos` (block-interaction distance), the block at `pos` is a heart-node, and its entry's owner is the player. Clamp `intensity`. Then call `HeartNodeService.updateSettings(player, settings)`, which writes the entry, marks the saved data dirty and resyncs the envelope to everyone near it (strangers' relation decides what they draw).
- The screen sends on each toggle, debounced client-side to one packet per 250 ms, so the world updates live behind it.

Bump `PROTOCOL_VERSION` once for both packets and rewrite `payload-registry.txt`.

### The screen
`client/screen/block/HeartNodeScreen` (or wherever the station screens live; follow the package rules). Layout, 16 px per block art scale, about 256×200:
- **Left: the sanctum diagram.** A top-down map of the envelope: the heart lobe and each synapse lobe as soft mycelial circles, threads between them, the player's position as a mote, and north marked. Drawn with the screen's own vertex buffer (no world render). Hovering a synapse shows its coordinates, facing and whether it is active or withered.
- **Right: state.** Owner, maturity stage (0–3, with the time to the next), synapses used / budget, the envelope's widest reach in blocks, the respawn bound here (yes/no), caches waiting (count, with coordinates on hover, from the saved data; Track 1 makes cache positions queryable), and the benefits active in the heart lobe now (Regeneration I, spore load per pulse at full strength).
- **Bottom: toggles.** The table above as toggle buttons with tooltips. `intensity` is a five-notch slider. A Reset button restores the defaults.
- Chrome: `ScreenDrawUtils` borders and corner accents; `VeinBackgroundRenderer` with a new `Style` in pale mycelial greys with crimson veins (Gardens palette, not the bloodline pool's), so it reads as a Gardens screen. Text is lang keys under `screen.hemomancy.heart_node.*`.
- Add it to `ScreenChromeReview`.

### Teaching
- Liber, Apotheos section: one line saying the node can be opened to tend the sanctum's look.
- HR §5.6d: the settings table and the "settings never change rules" sentence.

## Tasks
1. `HeartNodeSettings` + test; the settings field in the entry (if Track 1 left a default stub, fill it in).
2. Open packet, client handler, use action, stranger refusal.
3. Settings packet, guards, `HeartNodeService.updateSettings`, resync; protocol bump.
4. `HeartNodeScreen`: diagram, state panel, toggles, slider, reset; chrome style; `ScreenChromeReview` entry.
5. `edgeWarning` message, server side and rate-limited, on a UUID-keyed session map cleared through `TransientServerStateEvents`.
6. A GameTest for the guard: a stranger's settings packet is ignored, and so is one sent from out of reach.
7. Review client: open the screen, flip each toggle, and capture the screen plus the world behind it after each flip.

## Acceptance
- Only the owner can open the screen (and creative operators, read-only). Only the owner's packets from within reach change anything.
- Every toggle visibly changes the world within a quarter second (proved in the review captures with Track 4's renderer; until then, by the synced bits arriving).
- With `showStrangers` off, a second client sees no spread, edge, threads or motes.
- No toggle changes benefits, privacy, respawn or the cache.
