# Sound audit, 2026-11-13

A read-only pass over every sound the mod plays or should play. Each row names the moment, what plays now, the event
it should use, a target sound a designer could act on, a length, and search phrases. The search links open Freesound
filtered to CC0. Nothing under `src/` was changed and no audio was downloaded.

The rows were written by eleven agents reading the code, one area each. Nobody listened to anything: a verdict of
`review_by_ear` means only that the file's stats and wiring show no problem. The target sounds and search phrases are
proposals, and the code problems listed below were reported by the agents; three were spot-checked (marked).

## Numbers

| | Count |
|---|---|
| Sound events in `sounds.json` | 173, backed by 102 files |
| Vanilla `SoundEvents` call sites in Java | 907 lines (915 calls) |
| Vanilla calls that should become mod sounds | 761 rows |
| Vanilla calls that can stay vanilla | 35 rows |
| Existing events that borrow another event's or a vanilla file | 76 |
| Existing events whose file is unfit by its stats (mostly stereo on a positional sound) | 14 |
| Existing events with no visible problem, to judge by ear | 83 |
| Silent moments that should have a sound | 325 rows |
| Rows that need a new sound, all told | 1,176 (416 priority 1, 615 priority 2, 145 priority 3) |

Almost every row asks for its own event, so this is about 1,160 sounds if built in full. Priority 1 is the realistic
first target: signature moments and sounds the player hears constantly.

## Files

| File | Covers | Priority 1 rows |
|---|---|---|
| [manip_core.md](manip_core.md) | Projection, Absorption, lash and strands, cast table, Ferric, Flammeus | 43 |
| [manip_schools.md](manip_schools.md) | Congeatio, Tenebris, Mortem, Animus, Lux, Ductilis | 13 |
| [items.md](items.md) | Living tools, vials, microscope, Morphlings, armour | 22 |
| [blocks_stations.md](blocks_stations.md) | Stations, machines, puzzle blocks, block sound types | 28 |
| [rites_world_ui.md](rites_world_ui.md) | Cardinal Rite cues, biomes, music, progression, HUD and screens | 46 |
| [entities_voices.md](entities_voices.md) | Ambient, hurt and death voice design for every creature and NPC | 32 |
| [entities_calls.md](entities_calls.md) | Mob attacks, summons, projectiles, circus | 13 |
| [boss_annetta_vesper_mycophant.md](boss_annetta_vesper_mycophant.md) | Annetta and the Stained Priestess, Vesper, the Mycophant | 52 |
| [saints_velorum_seraphae.md](saints_velorum_seraphae.md) | Velorum, Seraphae | 51 |
| [saints_hemorath_putriciel.md](saints_hemorath_putriciel.md) | Hemorath, Putriciel, the shared Saint kit | 39 |
| [boss_endgame.md](boss_endgame.md) | Evening Star, Phlegethosoma, endgame encounters, Clairaudiograph records | 77 |
| [palette.md](palette.md) | The 73 shared events (blood, bone, glass, spark, voice, UI) | |

Each file lists silent moments first, then existing mod sounds, then vanilla sounds to replace, each sorted by
priority, and ends with the vanilla calls that can stay.

## Where a few sounds fix many rows

- **`ManipulationCastSounds`** gives about 40 manipulations 19 vanilla cues, and `EntityCasts` repeats them with
  different constants. One `manipulation.<id>.cast` lookup shared by player and entity casts fixes every cast at once.
- **One heartbeat set.** The Warden heartbeat sits under four `rite.*` and `mausoleum.*` events and three direct
  calls. One authored `blood.heartbeat.deep` set replaces all seven.
- **Living weapon wake and rest.** Seven items toggle with `BEACON_ACTIVATE` / `BEACON_DEACTIVATE`.
- **Block sound types.** Ten materials (flesh, fungal, bone, vivianite glass, heartwood, lacquer, hematic iron, chitin,
  blood soil, blood crystal) need a break / place / step / hit / fall set each; today they use GRASS, WART_BLOCK,
  SLIME_BLOCK, GLASS and STONE.
- **Summon puppets.** `GroundPuppetEntity` extends Zombie, so five puppets groan like zombies; the hound, vulture and
  spitter use wolf, vex and skeleton voices. Vesper's roster uses the same bodies.
- **Biomes.** None of the 17 biome JSONs has its own ambient loop, mood, additions or music.
- **Mono re-exports.** About 30 positional events play stereo files (all of Vesper and the Mycophant except her
  summon, Abhorrent Thought and Will, the Qliphoth Pome, `rite.culmination`, the bow blade clang). Minecraft plays
  stereo files without distance falloff. Downmixing them is a mechanical fix that needs no new audio.

## Code problems found in passing

These are not sourcing work. See "Follow-up" at the end for what was fixed the same day and which reports were wrong.

- `BloodStructureCrafting.java:140` and `BloodStructureFeedManager.java:186` play the "not enough blood" refusal with
  `playLocalSound` on a server level, which does nothing. Probably silent in game. (First one spot-checked.)
  `BloodConstructEntity` does the same for its expiry and dissolve.
- `SomaticLoomBlockEntity.java:1032` plays `BEACON_POWER_SELECT` on every Living Staff charge with no tick gate.
  (Spot-checked: the call is unconditional; whether the caller runs every tick was not traced.)
- `UnstainedPodiumBlock.java:93` plays a zombie moan to nearby players on every interaction. (Spot-checked.)
- `CloudEntityBlood` thunder plays at volume 10000; Sanguis Lancea's return plays at volume 10.
- `ChewWoodGoal` plays the zombie door bang every 8 ticks while a termite chews.
- Velorum and Seraphae play a hit sound from `onHitTaken` and again from `getHurtSound` for the same blow.
- Putriciel's body fire plays from two places at once (`PutricielEntity:470` and `:1453`), and he has hurt and
  ambient sounds although the design calls him silent.
- A Stained Priestess plunge sets off 9 to 11 thorns that play two sounds each, so about 20 sounds in a few ticks.
- Brushing the Verdigris Moth plays `AXE_SCRAPE` twice, and also during the brush cooldown.
- Cruor Javelin plays its throw sound after its hit in the same tick.
- `sclerite_block` uses the COPPER sound type on blood-side content.
- `BiomeInit.fungalGardens` sets the nether wastes loop, but `fungal_gardens.json` has no loop and End music.
- `entity.mycophant.teleport` now plays when she splits; her 9.86 s death file outlasts her 6 s yield scene.

## Constraints for replacements

- The three `antecedent.*` records are played by the Clairaudiograph by resource id and decoded by the spectrograph.
  A replacement must keep the lengths (73 s, 30 s, 30 s) and cue windows, including the Severed Record anomaly at
  55 to 68 s.
- Positional sounds are mono OGG Vorbis. Music and non-positional UI may be stereo.
- No bell, brass or copper timbres on Harbinger content. `ManipulationCastSounds` gives Funeral Bell `BELL_BLOCK`
  today.
- Keep a credits file (source, author, licence per file) from the first sound added; the repo has none.

## Sources

Sonniss GameAudioGDC bundles (royalty-free, no attribution, no reposting the raw files) for quality, Freesound CC0
for specific gaps. Freesound and OpenGameArt disallow automated access in `robots.txt`, so files were not fetched by
script; a Freesound API key is the sanctioned way to automate candidate pulls.

## Follow-up, 2026-11-13

Done after the audit, with the JVM test suite green:

- The 17 stereo positional files were downmixed to mono in place (same length and sample rate). The stereo originals
  are in `removed_assets/` under the same paths. The three boss music files stay stereo.
- The two "not enough blood" refusals now reach the player (`playNotifySound`), using `rite.cancel` at low volume
  instead of the Enderman scream, which `BloodStructureFloorlessSourceTest` already bans from the completion.
- The Somatic Loom's Living Staff charge sound plays every 8 ticks, as the Effigy's does.
- Velorum and Seraphae return no hurt sound; `onHitTaken` already sounds each blow.
- Putriciel's ambient sound is silent in fire stages; the stage tick paces the body fire.
- Brushing a Verdigris Moth plays the brush scrape only when nothing sheds.
- `sclerite_block` uses the METAL sound type.

Reports that were wrong or are not bugs:

- `BloodConstructEntity` calls `playLocalSound` from code that also runs on the client, so its cues do play.
- The Unstained Podium's `playSound(player, ...)` is the normal both-sides call; everyone hears it. Only the zombie
  moan itself is the problem, and that is sourcing work.
- Sanguis Lancea's return at volume 10 and the Blood Cloud's thunder at 10000 copy vanilla's trident and lightning.
- Cruor Javelin's hit and throw play in the same tick, so their order is not audible.

Left as design or sourcing work: the termite's door bang, the Priestess thorn stack, Putriciel having a voice at all,
the Mycophant event name and death length, and the `BiomeInit` / biome JSON disagreement.

## Regenerating

`tools/sound_audit/inventory.py` rebuilds the raw inventory (events with ffprobe stats, vanilla call sites by slice).
`tools/sound_audit/render.py` merges `tools/sound_audit/out/*.json` into this folder and fails loudly on any call or
event without a row. `tools/` is untracked, so the scripts and the JSON are local.
