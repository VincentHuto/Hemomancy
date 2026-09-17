# Clairaudiograph

The Clairaudiograph carves a creature's call from one Blood Vial onto one Ambergris Cylinder in **80 ticks (four seconds)**. Identification is optional. Place both items in the two slots, choose a call, and press Carve. The blood is used only at completion; its original vessel remains, with sample and identification fields cleared. Names and other container customization survive.

## Crafting

Machine: five Hematic Iron Scraps, one Echo Shard, and one Blood Vial. Pattern: ` I ` / `VEI` / `III` (I = scrap, V = vial, E = shard).

Two cylinders: one Mnemonic Ambergris, one Active Befouling Ash, one vanilla honeycomb, and one paper, shapeless. Honeycomb supplies the wax. No research currency or identification gate is added.

## Listening and redstone

- Preview is private and limited to once per second per player at a machine, including closing/reopening its menu.
- Play broadcasts through the Blocks mixer within 32 blocks, with linear distance attenuation.
- Loop off: a redstone rising edge plays once; constant power does not retrigger.
- Loop on: redstone repeats while powered. GUI Play starts a manual loop that continues without power.
- A falling edge stops a redstone loop. It leaves a one-shot or manual loop alone.
- Stop silences only this machine and cancels repeats. Continued power does not restart it.
- A new Play during the server's playback interval is ignored. Clients also ignore repeats while their previous instance is still playing, preventing overlapping audio.
- Removing the cylinder, breaking the block, leaving its range/chunk/dimension, or disconnecting clears its local sound.

Recordings preserve sound-event identity, not an exact waveform. Events can choose different audio variants. Replay intervals are scheduling values, independent of pitch; they are not measured sound durations. New listeners hear the next loop iteration or explicit trigger. Audio timing and visual presentation still require in-game verification.

## Persistence and missing data

Inputs and completed recordings survive saving. Incomplete carving cancels on unload, restart, or catalogue reload without consuming inputs. Playback never resumes simply because a chunk loads powered. Both slots lock during carving; no automation capability is exposed.

Unknown kinds or invalid recording values stay on the cylinder as unreadable data. Missing source entities display Unknown Specimen, but a retained catalogue association and registered sound can still authorize playback. Missing sounds or removed catalogue entries make the voice unavailable without erasing it. The client reports absent sound assets locally.

## Datapacks

Definitions live in `data/<namespace>/clairaudiograph_sounds/*.json`. Each declares `entity` and a `sounds` array with `kind`, `sound`, optional `pitch` (default 1, finite 0.5–2), and optional `replay_interval_ticks` (default 100, 20–12000). At most 64 choices are accepted per source. Kinds: ambient, hurt, death, step, attack, warning, celebration, special.

Normal pack replacement applies to matching file paths. For distinct paths claiming one entity, the lexically first valid file wins and duplicates are diagnosed. Invalid definitions are rejected; missing registered sounds are omitted with a diagnostic. Reload replaces the catalogue and invalidates open-menu selections. Playback checks the exact source/event/kind/pitch association, including edited cylinder components.

The initial catalogue has 13 sources: pig, cow, sheep, wolf, cat, polar bear, fox, goat, allay, warden, Crimson Doe, Fungling, and Vesper the Crowned Refusal.

## Assets and live acceptance

Editable models and their exporter are under `models/*/bbmodel/` and `tools/model_export/clairaudiograph.py`. The cylinder uses the existing crimson glint pass. The machine renders a rotating cylinder, advancing stylus and moving feed accent.

Still verify in a client: inventory/hand/dropped/mounted glint, model orientation, narrow-screen readability and translations, vanilla and Hemomancy audio, Blocks volume, attenuation, two same-sound machines stopped independently, chunk/dimension/reconnect cleanup, missing resource-pack audio, and saved recording replay after restart. Dedicated-server tests cannot establish sound or shader appearance.

## Verification (2026-09-15)

- Seven focused JVM tests pass (recording codec, invalid values, retained unknown kind, catalogue parsing and bounds).
- All 46 required tests in the specimen dedicated-server suite pass, including eight Clairaudiograph tests covering actual components, vessel conservation, recipes, slot limits, cancellation, stale source requests, forged recordings, redstone, break drops, reload and retained missing-entity playback.
- Resource logs contain no Clairaudiograph recipe/catalogue loading errors.
- The full JVM run completed 2,043 tests with two unrelated failures: `MorphlingLumenlaceRenameResourceTest` encounters a non-UTF-8 byte in `docs/phlegethontic-nether-worldgen/orcadian-horseman/README.md`; `LegacyMainTestSuiteContractTest` expects 361 legacy tests but discovers 360. Those files were not changed.
- Live audio, multiplayer delivery and rendering checks listed above remain unverified.
