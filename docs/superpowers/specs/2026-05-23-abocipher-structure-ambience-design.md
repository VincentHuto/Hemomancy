# Abocipher Structure Ambience Design

## Goal

Add ambient Abocipher glyph particles around naturally generated Blood Temple and Harbinger Outpost structures. The effect should feel like blood-memory residue in Harbinger spaces: quiet, occult, and alive, without implying that Harbingers are simple villains or that the structures are apocalypse cult sites.

## Recommended Approach

Use a hidden technical marker block placed by each structure's `afterPlace` hook. This keeps the ambience tied to generated structures while allowing randomized emitter positions and per-structure tuning.

The marker should not be visible, collide with players, drop loot, or appear in normal player-facing workflows. It exists only to host a lightweight block entity that emits client-side Abocipher particles while loaded.

## Architecture

- Add an invisible `abocipher_emitter` block under Hemomancy's normal block registration.
- Add an `AbocipherEmitterBlockEntity` registered in `BlockEntityInit`.
- Give the emitter a client-side ticker. The server-side ticker is unnecessary unless future behavior needs state changes.
- Store a small profile on the block entity so Blood Temple and Harbinger Outpost emitters can use different ranges and densities.
- Place emitters from `BloodTempleStructure.afterPlace(...)` and `HarbingerOutpostStructure.afterPlace(...)` after the existing inscription/NPC placement logic computes the full structure bounding box.

## Placement

Blood Temple:

- Place one emitter near the interior center of the structure.
- Randomize its offset slightly from the calculated center so repeated temples do not feel identical.
- Keep the radius modest and density sparse.

Harbinger Outpost:

- Place two or three emitters inside the structure.
- Randomize positions around interior quadrants using the structure bounding box.
- Use a slightly wider radius and slightly higher density than the Blood Temple, suggesting an inhabited Order site rather than a single hermit's final rite.

Emitters should only be placed once per structure generation, guarded by the same center-chunk checks already used for NPC and inscription placement.

## Particle Behavior

The client ticker should spawn `ParticleInit.abocipher` particles with:

- Randomized positions within the emitter radius.
- A small upward velocity and slight horizontal drift.
- A low spawn chance per tick rather than a fixed burst every tick.
- Distance checks against the local client player so unloaded or distant anchors do not create unnecessary particles.

The existing Abocipher particle already drifts upward, rotates, fades, and renders full-bright, so the emitter only needs to choose where and how often to spawn it.

## Data And State

The block entity should persist:

- Profile type, such as `blood_temple` or `harbinger_outpost`.
- A variant seed generated during `afterPlace`, used to keep each emitter's randomized timing and drift stable after chunk reloads.

If the block entity has no valid profile, it should fall back to a conservative default rather than failing.

## Retroactive Worlds

This design affects newly generated structures. Existing Blood Temples and Harbinger Outposts in already-explored worlds will not gain emitters automatically.

If retroactive support becomes important later, add a separate migration or debug command that scans known structure bounds and places missing emitters. Do not add continuous client or server structure scanning for the initial feature.

## Testing

- Run `./gradlew.bat build`.
- Use `runClient` or the structure spawner/debug workflow to place or locate Blood Temple and Harbinger Outpost structures.
- Verify emitters are invisible, non-colliding, and non-dropping.
- Verify Abocipher particles appear in both structures with distinct density/radius profiles.
- Verify no old `net.minecraftforge.*` APIs or `SimpleChannel` patterns are introduced.
