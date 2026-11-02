# Track 5: set pieces (the node, the synapse, the cache, planting and withering)

**Status (2026-10-29): built, uncommitted.** What exists:
- Generators `tools/model_export/heart_node.py` (stages 0–3, `heart_node_heart`, withered twins of all five), `mycelial_synapse.py` (body, withered, `mycelial_synapse_bulb`, six-way blockstate), `fruiting_body_cache.py` (body, `_lid_west`, `_lid_east`; no item model, since the cache has none). Each writes its `.bbmodel`; `--preview` writes `build/<name>_preview.png`. World textures, plus textures the generators derive with the new `station_kit.derive_texture` (`heart_node_flesh` / `_vessel`, the heart's crimson lifted from the near-black crust and bog flesh; `heart_node_withered_*`; `mycelial_synapse_withered_*`). The hand-written `heart_node.json` and `textures/block/heart_node.png` are gone; the cache keeps its old texture as the body's hide.
- `HeartNodeBlock.STAGE` (block state, picks the model). `HeartNodeBlockEntity.refreshStage` also brings an old save's state up to its stage. Outline = knot + heart; collision = the knot only.
- `HeartNodeRules.HEARTBEAT_TICKS` (50) is the one clock: the spread geometry and the screen read it.
- Client: `HeartNodeParts` (part locations and mirrored generator constants, one draw), `HeartNodeBeat` (pure: paces, double thump, planting beats, resting scale), `HeartNodeRenderer`, `MycelialSynapseRenderer` (flare from `HeartNodeSpreadGeometry.flare`, the shader's sum), `FruitingBodyCacheRenderer`, `HeartNodeMomentRenderer` (dispatcher, right after the possession scenes; ticked from `ClientTickEvents`).
- `HeartNodeMomentPacket(pos, HeartNodeMoment, stage)`, protocol 25, sent by `HeartNodeService.announce` from `plant()`, `HeartNodeBlock.onRemove` and the cache's open.
- Whole-sanctum recede: `HeartNodeClientState.wither` / `withering` (keeps envelopes the last packet dropped for 40 ticks, so either arrival order works); `HeartNodeSpreadRenderer` draws withering sanctums with every lobe's front and life scaled by one `sanctumLife`.
- Lang `block.hemomancy.heart_node.desc`, `block.hemomancy.mycelial_synapse.desc` (hover text). HR §5.6d (Set pieces, Moments) and three block-table rows; `wiki/World-Content.md`.
- Tests: `HeartNodeBeatTest`, `HeartNodeMomentPacketTest`, `HeartNodeSetPieceResourceTest`, a flare case in `HeartNodeSpreadGeometryTest`, stage-state checks in the two heart-node maturity GameTests.
- Review: `HeartNodeSetPieceScenes` from `HeartNodeClientReview` (`heart-node-19` … `-26`).

**Fixed on the way:** a fluid washed away a heart-node or synapse that stood beside it (the review caught morphic nectar flowing back into a cleared cell, dropping the new node and withering its sanctum four ticks after planting). Vanilla only keeps fluid out of blocks whose collision box averages at least 0.73 blocks, and neither shape does (the old 10×11×10 node box didn't either). Both blocks now use `forceSolidOn()`; GameTest `heartNodeAndSynapseHoldBackFluids`.

**Decisions (review these):**
- **Paces stay in step with the ground:** quick = twice a pulse, steady = every pulse, slow = every other pulse, so the heart never beats out of time with the spread.
- **The heart's inner glow is block light raised on each thump** (the part drawn at a lifted light), plus a glow mote and blood cells per beat; no new render type.
- **The synapse flare draws a lit copy of the bulb a hair proud of the baked one** only while it flares, so in daylight it's never darker than the block.
- **A synapse's small wither plays only when the client saw it alive and then inert.** One that withered in an unloaded chunk shows withered from the start; the BE has no record of when it went.
- **Owner-broken nodes still drop their item** (vanilla loot, as before). The no-item rule is about the replant path, which removes the block without drops.
- **Cache opening got its own moment** (`CACHE_OPENED`), since opening removes the block at once like withering does.

**Goal:** the heart-node, the Mycelial Synapse and the fruiting-body cache read as authored set pieces at the legacy-dungeon bar (`feedback_structure_quality_bar`), not cubes. The node beats. Planting it and losing it each have a short moment of presentation.

## Current state
- `models/block/heart_node.json` and `fruiting_body_cache.json` are hand-written single-cuboid models with one texture each.
- The synapse doesn't exist yet. Track 2 registers it with a one-cube placeholder.
- Planting says `message.hemomancy.heart_node.planted` and nothing else visible happens. A replaced node withers (`plant()` removes the old block) with no presentation.

## Target

### Generators
One generator per piece in `tools/model_export/`, following `consecrated_bloodwell.py` and `saint_sarcophagus.py`: block model JSON, atlas texture(s) at 16 px per block, blockstate, `.bbmodel` and a preview PNG. Re-run them, never hand-edit their output. Add each generator to the model-export tests where they list generators.

**`heart_node.py`.** A fungal heart grown out of the Gardens' ground:
- a fist-sized crimson heart (the bleeding heart it was made from) half-sunk in a knot of pale hyphal roots;
- the roots splay out over the block's floor and slightly past it, so the Track 4 spread seems to come out of them;
- a cage of thin bone-white fungal ribs closes over the heart (the rib ring's idea, kept at block scale instead of as world blocks);
- one or two small capped fruiting bodies on the roots.
- **Four maturity stages** (blockstate `stage=0..3`, set from Track 1's maturity): 0 a bare heart in a seed knot; 1 roots spread; 2 the rib cage closes; 3 fruiting bodies and a fully closed cage. Each stage is its own model from the same generator.
- Height about 12 px; no collision past the root knot (the block shape follows the root knot only).

**`mycelial_synapse.py`.** A hematic-iron stalk (the iron block it was made from, drawn to a fluted spike) sheathed in hyphae, ending in a Sporite Crystal bulb split into dendrite tips. A ring of roots grips its base and runs out across the attaching face.
- Authored pointing up. The blockstate rotates it to all six `FACING` values (x/y rotations), so the base always grips the attaching face: floor, wall or ceiling.
- A `withered` variant: greyed iron, a dull crystal and dead brown roots.
- Hematic iron and bone only. No copper, brass or bronze anywhere (`feedback_no_copper_on_harbinger_gear`).

**`fruiting_body_cache.py`.** A squat, gilled fruiting body (bracket-fungus layers over a bulbous cap) that is visibly a container: a seam where it splits, the faint shapes of what it holds pressing against the flesh. Opening it (the existing interaction) plays a split animation in the renderer (below).

### Block entity renderers (the parts that move)
Register them through `client/registry` (`registerBlockEntityRenderer`, never `BlockEntityRenderers.register`). Time comes from `AnimationTime`; render types are static or `SeededRenderTypes`.
- **Heart-node:** the heart is drawn by the BER rather than the static model, so it beats. Scale pulses on `HEARTBEAT_TICKS` (Track 4 matches its spread pulse to the same clock: share one constant in `HeartNodeRules`) with a fast double-thump curve from `HemoEasing`. A dim emissive layer brightens on each beat. The beat speeds up when the owner stands in the heart lobe and slows when they're away (the BE learns this from the envelope state on the client). Strangers see a slow, steady beat. Use the custom glow toolkit (`docs/MANIPULATION_VISUALS.md`), not vanilla particles: a faint crimson inner glow and two or three blood-cell motes per beat.
- **Synapse:** the crystal bulb glows and flares when Track 4's pulse reaches it. The client computes the flare time from the synapse's thread length and the pulse speed, so no packet is needed. Withered: no glow.
- **Cache:** the split animation on open; a slow breathing scale while closed.

### Planting
When `plant()` succeeds:
- The server sends `HeartNodeMomentPacket(pos, PLANTED)` to players near the node, or folds it into the envelope packet as a one-shot flag if Track 1 prefers fewer payloads. Either way, bump the protocol once.
- The client plays a ~1.5 s beat:
  1. roots punch outward into the surrounding blocks as Gardens-style tendrils (reuse the existing tendril effect through its public bridge);
  2. a ring of spores bursts at ground level;
  3. the heart gives three hard beats;
  4. Track 4's growth front starts from the node.
- Sound: a wet, deep thump (a vanilla sound pitched down is fine, as other Hemomancy moments do) and a soft fungal crackle.

### Withering
When a node is withered (replanted elsewhere, or broken by its owner):
- Send `WITHERED` to players near the old position. The heart greys and collapses inward over ~1 s, the roots blacken and recede (the spread layer recedes with them through Track 4), and a puff of grey spores rises.
- The block then goes: the existing removal, after the presentation. The server removes the block at once. The client keeps a **ghost** of the last frame at that position for the length of the beat (a short-lived client effect drawn by the dispatcher), so there's no server delay and no item-dupe window.
- Each of that owner's synapses (inert per Track 2) plays a smaller wither (crystal dulls, roots brown) when its chunk is loaded on a client.

### Teaching and lore
- Item and block descriptions (lang): the heart-node ("A heart the Gardens agreed to keep. It remembers you here."), the synapse ("Iron taught to listen. It carries the node's reach to where the crystal pointed."). Follow `docs/LORE_REFERENCE.md` tone; no God or Satan framing.
- HR §5.6d and the block table in HR's block list get the model descriptions in the same style as the Consecrated Bloodwell row.

## Tasks
1. `heart_node.py` with four stages; blockstate by stage; Track 1's maturity drives the stage. If Track 1 hasn't landed, drive it from the existing `stage` field.
2. `mycelial_synapse.py` with six-way blockstate and the withered variant; replace Track 2's placeholder.
3. `fruiting_body_cache.py`.
4. BERs: heart beat, synapse flare, cache split and breath.
5. Planting and withering moments, the payload and the client ghost.
6. Lang, HR and lore text.
7. Review client: each node stage up close; floor, wall and ceiling synapses (active and withered); the cache opened and closed; a planting sequence and a withering sequence captured frame by frame.

## Acceptance
- No hand-written block model remains for these three blocks. Each has a generator, a `.bbmodel` and a preview.
- The node visibly matures through four stages and beats in time with the spread's pulse.
- Synapses grip their face correctly in all six orientations.
- Planting and withering each play their beat on a second client nearby. A withered node leaves no block and no duplicate item behind.
- 16 px per block throughout. No copper, brass or bronze. Custom effects, not vanilla particle clouds.
