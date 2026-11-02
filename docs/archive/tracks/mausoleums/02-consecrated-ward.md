# Track 2 — The consecrated ward

**Goal:** remove the seal block. Everything past the threshold is held by a **ward**: a visible shader layer over the consecrated stone, a threshold that turns back anyone below the eighth degree, and a hit effect whenever someone tries to break through. The complex is protected by what it is, not by a 3×3 of blocks a player can mine around.

## Current state
- `MausoleumSealBlock` / `MausoleumSealRules`: a 3×3 doorway of unbreakable blocks that open for D8 (`open` flood, 100-tick close, waits for occupants). Tests: `MausoleumSealRulesTest`, GameTests `mausoleumSealOpensJoinedSealsAndCloses`, `mausoleumSealWaitsForItsDoorwayToClear`.
- `MausoleumEvents`: inside any piece of a `hallowed_mausoleums` structure, survival players cannot break or place, explosions and pistons do nothing. This is the real protection and it stays.
- The generator writes nine `mausoleum_seal` blocks in `seal_hall()`.
- Nothing is drawn: a player sees ordinary stone that refuses to break, with a chat line.

## Target
1. **Threshold.** A ward plane across the corridor where the seal hall was. A D8 player walks through. Anyone else is pushed back a block with a flare on the plane where they touched it (server: a `PlayerTickEvent` check against the threshold box; client: the same box drawn as a faint sheet that brightens where the player is near it). Creative and spectator pass.
2. **Ward layer.** Every exposed face of consecrated stone within ~12 blocks of the player carries a shader layer: a slow-breathing consecration glyph pattern in the Saint's ward colour, brightest near the crosshair, almost invisible at the edge of the radius. The effect is quiet by default; it must not wash out Track 7's architecture.
3. **Hit effect.** A refused break, place, explosion or piston push produces a strike: a ring ripples out from the hit face across the ward layer, the face flashes, a low bell-like refusal sound plays, and a few glow motes fall from the face (`HemoParticleData`, never vanilla particles). Repeated hits within a second escalate the flash, not the knockback.
4. **Apotheos burn tie-in.** The existing hallowed-ground burn (`MausoleumEvents.onPlayerTick`) gets its charring visual here: hyphae on the Apotheos blacken (the ward layer's glyphs crawl onto the player model's silhouette) and smoke rises. Keep the drain numbers.

## Design
- **Ward region.** The client cannot query server structure data, so the generator places one hidden **ward anchor** block (`ward_anchor`, no item, no hitbox, inside the threshold wall) per complex. At `MausoleumStructure.afterPlace` the server writes into its block entity the inner pieces' bounding boxes (from the `PiecesContainer`) and the threshold box, synced in the update tag. The client's world renderer collects loaded ward anchors (`ClientSessionEvents` clears the cache on logout, per `ClientSessionCacheTest`).
- **Render.** One `WorldEffectDispatcher` stage, `MausoleumWardRenderer` in `client/render/world/`. Faces come from `BlockShellFaces.emit` over the exposed blocks of the anchor's boxes near the camera (cull by distance first; cache the face set per chunk and rebuild on block change). Material: a `SeededRenderTypes` holder keyed by Saint, whose shader (`assets/hemomancy/shaders/core/world/mausoleum_ward.*`) reads a uniforms record (time, player position, strike origin + age, ward colour). Shared noise goes through `shaders/include/noise/` with `#moj_import`. A shader change needs a client launch to prove it compiles.
- **Strikes.** Server to client: a small clientbound payload `WardStrikePacket(pos, face, kind)` sent through `PacketHandler.sendNear`; registered in `payload-registry.txt` with a `PROTOCOL_VERSION` bump. The client keeps a short list of strikes (position, face, age) that the shader samples.
- **Threshold.** `MausoleumWardRules` (pure, JVM-tested): `mayCross(int degree)`, `pushBack(Vec3 playerPos, AABB threshold)`, strike escalation. `MausoleumEvents` grows the threshold tick; the hit effect fires from its existing cancels.

## Tasks
1. Delete the seal block, rules, tests and GameTests; remove it from `BlockInit`, the itemless lists, the fixtures, the lang, and the generator.
2. Add the ward anchor block + BE, the `afterPlace` writer, the payload, the client cache and renderer, the shader, the threshold events, the rules and their tests.
3. Generator: the seal hall becomes a **threshold hall**: a short vaulted passage (Track 7 kit) with the ward anchor in its wall and a visible line in the floor (a lacquered venous-stone strip) marking where the ward begins.
4. GameTests: a non-D8 fake player placed past the threshold is moved back; a D8 one is not. A refused break sends a strike payload (assert through the sent-packet capture used by other network GameTests).
5. Client proof through Track 0: screenshots of the ward layer at rest, mid-strike, and the threshold flare.

## Acceptance
- No `mausoleum_seal` anywhere in the tree.
- A D4 player cannot enter or dig in past the threshold, and sees why: the sheet, the flash, the sound.
- A D8 player sees the ward layer breathing and walks through.
- The layer costs nothing when no anchor is loaded, and stays under one draw call per Saint when one is.
