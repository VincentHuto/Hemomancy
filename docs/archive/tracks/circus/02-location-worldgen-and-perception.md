# Circus Location, World Generation, and Perception

## World-generated location

**SETTLED:** The Circus is a world-generated structure, not a random event placed into already occupied terrain. The design must not materialize a tent inside a player's home.

Use the existing Harbinger jigsaw structure pipeline as the model: registered structure type, configured structure JSON, structure set, biome tag, template pool, NBT pieces, and placement checks. `HarbingerOutpostStructure` is the closest social-location precedent; `HarbingerVoyagerWreckStructure` demonstrates a specialist-group location with custom placement and authored loot.

### Physical layer

The generated blocks represent what exists without collective projection:

- a road-worn pavilion or ring;
- dark red cloth and irregular colorful scraps;
- blackened hematic frames, poles, hooks, and hanging Crossbars;
- a Puppeteer's Spindle or equivalent practice anchor;
- pelts, bones, bone-white face plates, practical masks, and visible thread;
- enough solid collision and navigation to remain playable when effects are disabled.

Do not build the entire impossible spectacle into permanent blocks. The physical layer must still read as a place whose inhabitants are collectively seeing more than an outsider can.

### Placement requirements

- Generate only during world generation or through an explicit administrator/test command.
- Use terrain, biome, spacing, separation, and footprint checks comparable to current structures.
- Keep major pieces inside the structure bounding box and avoid destructive post-generation expansion.
- Provide a locate/test route for development.
- Validate new chunks; an existing explored region is not evidence that placement is broken.

**IMPLEMENTED INITIAL SELECTION:** The first pavilion uses one 33×12×33 physical template inside a 49×49 sampled footprint, `beard_thin` terrain adaptation, 48-chunk spacing, and 16-chunk separation. Its biome tag covers level land in plains, forests, taigas, savannas, sparse jungle, and badlands while excluding oceans and swamps. The five-block maximum sampled surface variation remains the final terrain gate. Revisit these values only after fresh-chunk placement evidence shows a concrete density or fit problem.

## Perception model

**SETTLED:** Circus members collectively manifest a shared mental state until it becomes local reality. Acclimation makes the site more coherent, not progressively noisier.

Use a small ordered state model rather than free-form per-effect flags:

| State | Player-facing result |
| --- | --- |
| **Unaware** | The physical pavilion is visible, but inhabitants look disordered and the site does not read as a complete circus. |
| **Disturbed** | Red heat-haze, contradictory silhouettes, scraps of impossible color, displaced sounds, and brief false movement appear. |
| **Acclimating** | Figures, cloth, lighting, duplicated motion, and implied interior space begin agreeing with one another. |
| **Attuned** | The shared spectacle appears stable and solid-looking, while retaining impossible movement and geometry. |

These names are documentation terms, not required public enum names. The implementation may use a compact integer or existing player-state facility. Do not create an extensible perception framework for one location.

### Implemented state rules

- Acclimation is a persisted per-player value from 0-1000 and does not decay away from the pavilion.
- Passive exposure awards one point per 80 ticks while inside a generated pavilion. Performer introductions, the attention act, pact, and controlled acts award one-time milestones.
- Stage thresholds are Unaware 0-149, Disturbed 150-499, Acclimating 500-999, and Attuned 1000.
- Unaware has no added particles or audio. Disturbed uses stronger motion instability and false silhouettes; Acclimating adds agreeing cloth, lights, and performer echoes; Attuned stabilizes a fuller ring of manifested figures and pavilion dressing. These world-space cues, particles, sounds, and the remaining screen edge are visible only to that player.
- The server owns the score and sends a compact score/active packet to that player. Shared server entities do not move or multiply based on an observer's stage.
- Exit, login, logout, respawn, and dimension-change paths clear or replace active client presentation. Live reconnect and two-client checks remain required.
- An abandoned active pavilion keeps its owner for a 200-loaded-tick grace period, then returns to idle without granting encounter credit. The same player can immediately retry after a failed controlled act.
- Pavilion ownership, completed route, outcome, and phase survive a `SavedData` save/load round trip; controlled performers persist their owning player and route lethality across entity reload; an intruding player cannot replace the active owner.

## Rendering and environment

Reuse the visual grammar and low-level seams already proven by `FaneBoundaryRenderer`, `FaneBoundaryClientData`, `BlackVeilRenderer`, and `SpawnBlackVeilPacket`. Reuse does not mean making the Circus a Fane or Black Veil mechanically.

The effect stack should be staged:

- boundary heat-haze and sparse red instability for early exposure;
- localized lights, sound displacement, false silhouettes, and motion echoes during acclimation;
- richer cloth/lighting overlays and coherent manifested figures when attuned;
- selective collision or server-side manifestations only where gameplay explicitly requires physical reality.

Do not represent all stages with a full-screen shader alone. The location needs world-space cues that multiplayer observers can understand, and effects must remain readable with shaders disabled.

## Reality boundary

**SETTLED:** Manifestations can become physically real; they are not all harmless hallucinations.

**IMPLEMENTED BOUNDARY:** The current slice does not permanently transform victims. Combat-capable manifestations are ordinary server entities and clean up with their attempt. Liberation alone authors a persistent site outcome: the destroyed carousel plus conservative cobweb ruin placed only into air. Until a later feature closes its own permanence rules:

- keep permanent transformation out of the first pass;
- use ordinary server-side entities for anything that attacks, blocks movement, drops loot, or persists;
- use client-only presentation only for effects with no mechanical consequence;
- never let a client illusion determine damage, loot, route state, or progression.

## Acceptance criteria

- The structure cannot spawn through a runtime event inside an existing player build.
- The physical pavilion remains navigable without post-processing.
- Different players can receive different presentation states without disagreeing about real collision, enemies, or rewards.
- Entering, leaving, dying, changing dimensions, disconnecting, and server restart cannot leave stale visual or audio effects. Automated checks cover server-side attempt cleanup and persistence; presentation and real restart/rejoin remain live checks.
- Acclimation trends from instability toward coherence.
- Fresh-chunk worldgen, dedicated-server tracking, multiplayer divergence, and shader-disabled presentation receive live validation.
