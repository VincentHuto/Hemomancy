# Sound

## What it is

Every sound the mod plays: the events in `sounds.json`, the vanilla `SoundEvents` calls still in code, and the silent
moments that should have a sound. Most moments still borrow a vanilla sound. The 2026-11-13 audit
([audits/2026-11-13-sound/README.md](audits/2026-11-13-sound/README.md)) has one row per moment with a target sound and
search phrases; it is a dated snapshot and is not edited.

## Where it lives

- `common/init/SoundInit.java` registers the events; `assets/hemomancy/sounds.json` defines them; files are under
  `assets/hemomancy/sounds/`; subtitles are `subtitle.hemomancy.*` keys in `en_us.json`.
- **Cast events.** Each manipulation that sounds on cast has one `manipulation.<id>.cast` event, listed in
  `ManipulationCastSounds.CAST_EVENTS` and registered as `SoundInit.MANIPULATION_CAST`. `ManipulationCastSounds` plays it for player casts (the ids in `PLAYER_CASTS`; the rest
  sound from their own code) and `EntityCasts` plays it for a Will's or Vesper's cast, through `playEntityCast`. In
  `sounds.json` each event aliases the vanilla cue it used before, with that cue's volume and pitch, so a sourced file
  replaces the cue with a data change only. All share the subtitle `subtitle.hemomancy.manipulation.cast`.
  `ManipulationCastSoundsTest` and `EntityCastGuardrailTest` pin this.
- **Credits.** [SOUND_CREDITS.md](SOUND_CREDITS.md) has one row per file under `assets/hemomancy/sounds/`.
  `SoundCreditsTest` fails on a file with no row, a row with no file, or a sourced row with no allowed licence.
- **Audit tools.** `tools/sound_audit/` (local, untracked): `inventory.py` rebuilds the raw inventory,
  `render.py` the audit files, `candidates/audition.html` plays the 763 CC0 candidate clips.
- `removed_assets/` (untracked) keeps every file a sound change replaced, under its old path.

## Sourcing routine

1. Re-run `python tools/sound_audit/inventory.py`, so the audit rows point at today's lines.
2. Take the owner's pick for the event (UN-44, from `audition.html`). Only Freesound CC0 and Sonniss GDC files may go
   in (UN-18).
3. Fetch the original file. A Sonniss file is always edited before it goes in (trimmed, layered or processed), never
   committed raw, because the repository is public.
4. Downmix a positional sound to mono OGG Vorbis. Music and non-positional UI may stay stereo.
5. Trim to the length the moment needs, at the pitch the code plays it (a cue played at pitch 0.6 lasts 1/0.6 times
   its file).
6. Name it after its event (`entity/<mob>/<cue>N.ogg`), point the event at it in `sounds.json`, and drop the vanilla
   alias.
7. Add its row to [SOUND_CREDITS.md](SOUND_CREDITS.md) in the same change.
8. Check the timbre rule below before it goes in.

## Rulings that bind

- **UN-11 A (2026-11-14).** Priority 1 only, shared sets first, then by area. Shared sets first: palette, heartbeat,
  living weapon wake and rest, block types, puppet voices, casts.
- **UN-13 A (2026-11-14).** No bell, brass or copper timbres on Harbinger content. Funeral Bell and Velorum's toll
  become struck iron. Copper stays for Unstained. Not built yet: Funeral Bell's cast event still aliases the vanilla
  bell until its iron file arrives.
- **UN-18 A (2026-11-14).** Sound files come from Sonniss GDC bundles and Freesound CC0, every file with a credits row.
  Sonniss files go in edited, never raw, because the repo is on GitHub.
- **UN-14 A (2026-11-14).** Putriciel has no voice. His body gets non-vocal sounds of his own (mould tearing, a sac
  creak). Not built yet (Sound S5).
- **UN-12 (2026-11-14, accepted default).** One `manipulation.<id>.cast` event per manipulation, read by player casts and
  by `EntityCasts`, each aliasing today's vanilla cue until its file arrives. An entity's cast of a manipulation that
  has a player cue uses the player's cue. Built in B35.
- **UN-16 (2026-11-14, accepted default).** `entity.mycophant.teleport` is now `entity.mycophant.split`. The death file
  is trimmed so it ends with the 6 s yield scene; the untrimmed file is in `removed_assets/`. The quiet yield cue is a
  Sound S5 row. Built in B35.
- **UN-17 (2026-11-14, accepted default).** A termite chews with one quiet wood knock a second, not the zombie's door
  bang every 8 ticks. On a Stained Priestess plunge only the first and last thorn of the line sound. Built in B35.
- **UN-70 (2026-11-14, accepted default).** A credits file with one row per shipped file (file, source, author, licence,
  link) and a test that every file has a row. Files that predate it read "source not recorded" for the owner to fill
  in. Built in B35.
- **UN-36 (2026-11-14, accepted default).** The seven same-day fixes of 2026-11-13 stay as built: 17 positional files
  downmixed to mono, the "not enough blood" refusal audible through `rite.cancel`, the Loom's Staff charge every 8
  ticks, no doubled hurt sound on Velorum and Seraphae, Putriciel's ambient silent in fire stages, one moth brush
  scrape, `sclerite_block` on METAL.

## Calls made without the user

- B35: the shared cast subtitle reads "Blood manipulation cast".
- B35: the death file is cut to 3.6 s, not 6 s. The code plays it at pitch 0.6, so 3.6 s of file lasts 6 s in game. The
  last 0.8 s fades out.
- B35: the four thorns of the ring round the plunging spear are silent too; the line's first and last thorn and the
  plunge's own impact carry the move.
- B35: the termite's knock is vanilla `block.wood.hit` at volume 0.3, pitch 1.3 to 1.6, every 20 ticks.
- B35: two doubled cues the audit marked as droppable were dropped: the flint-and-steel over Crimson Flame's TNT
  priming (TNT plays its own primed sound) and Umbral Rend's cast-table sweep on top of its own sweep (its own sweep now
  plays `manipulation.umbral_rend.cast`).
- B35: entity casts that had no sound (Blood Shot, Blood Needle, Deadly Gaze) now play the player's cue; Blood Cloud's
  entity cast plays `manipulation.blood_cloud.cast` (the ghast shot at volume 1) instead of level event 1016 (the ghast
  shot at volume 10).

## Open questions

The open sound rows are under Sound in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md): Sound S3 to S6 (the
work of UN-11, UN-13, UN-14 and UN-18, whose answers are recorded above) and UN-44 (listening to the candidates).

## Proof owed and known gaps

- Nobody has listened to the cast events since the change. They alias the same vanilla cues, so a player cast should
  sound as before.
- 110 of the 121 files in `assets/hemomancy/sounds/` have no recorded source, including eight `.mp3` files under
  `entity/samples/` that no event plays.

## History

- 2026-11-13: the sound audit and its same-day fixes (UN-36).
- 2026-11-14: the owner answered UN-11, UN-13, UN-14 and UN-18 and accepted UN-12, UN-16, UN-17, UN-36 and UN-70.
- 2026-11-16: B35 built the cast events, the Mycophant rename and trim, the termite and thorn thinning, the two dropped
  doubles, and the credits file with its test.
