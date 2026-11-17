# docs/

How the documentation is laid out, which files are live, and the rules for keeping it that way. Code and resources win
over any doc that drifts; the two reference docs win over every other doc.

## The two references

| File | Role |
| --- | --- |
| [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) | Mechanics and status of every system. Tests read it (`LoreCanonAuthoritySourceTest`, `SettledLoreSynchronizationSourceTest` and others pin lines). |
| [LORE_REFERENCE.md](LORE_REFERENCE.md) | Canon lore, tone and vocabulary. Tests read it too. |

Both are updated in the same change as the mechanics or lore they describe. They are long by design; the topic docs
below are the short way in.

## Topic docs

One file per feature area, kept at `docs/` root in UPPER_SNAKE case. Each one says what the system is, where it lives
(packages, data, generators, review client, GameTests, reference sections), the rulings that bind it, the calls that were
made without the user, what is still open, what proof is owed, and where the history is. They are summaries with
pointers, not a third reference: a number lives in one place, and a topic doc links to it.

| Topic | File | Covers |
| --- | --- | --- |
| The core loop | [CORE_LOOP.md](CORE_LOOP.md) | The 2026-11-09 overhaul: two verbs and Sculptings, strands, living blood, Temper, the menagerie, the ground network, farming and water, the growing Staff, Living Land and cling, the body and the world's answer; the play-test script [CORE_LOOP_PLAYTEST.md](CORE_LOOP_PLAYTEST.md) and the Track 11 ledger [CORE_LOOP_TEACHING_LEDGER.md](CORE_LOOP_TEACHING_LEDGER.md) stay live; the track files, the two build logs and the applied `AGENTS.md` patch moved to [archive/tracks/core_loop/](archive/tracks/core_loop/00-README.md) on 2026-11-15 (CQ-14) |
| Endgame paths | [ENDGAME_PATHS.md](ENDGAME_PATHS.md) | D8 Apotheos and Silentium, the Fungal Gardens body, the heart-node Fane, possession, gaze and grab, adipocere, Visitation |
| Saint mausoleums | [SAINT_MAUSOLEUMS.md](SAINT_MAUSOLEUMS.md) | The four complexes, ward, Chapels, ring door, sarcophagus, set pieces, themes, Saint fights and enrage, Recollections |
| Brined Court | [BRINED_COURT.md](BRINED_COURT.md) | The eight houses, Seats and outposts, Court NPCs, Marimo and Hydra, the Bloom Warden and Kindled Champion, membership and standing |
| Morphling strains | [MORPHLING_STRAINS.md](MORPHLING_STRAINS.md) | The eight house strains' current stats and ladders |
| Pelagic terrain | [PELAGIC_WORLDGEN.md](PELAGIC_WORLDGEN.md), [PELAGIC_ECOLOGY.md](PELAGIC_ECOLOGY.md) | The ocean biomes the Court lives in and what lives there |
| Crimson Troupe | [CIRCUS.md](CIRCUS.md) | The Circus sect: pavilion, discovery, puppetry, the Ringmaster and performers |
| NPC dialogue and trade | [NPC_DIALOGUE_AND_TRADE.md](NPC_DIALOGUE_AND_TRADE.md) | The dialogue hub, routing, inquiries, the trade system, voice passes |
| NPC voices | [NPC_VOICES.md](NPC_VOICES.md) | One sheet per speaker: rhythm, vocabulary, pronouns, contrast-map targets, trade keys |
| Cardinal rites | [CARDINAL_RITES.md](CARDINAL_RITES.md) | Rite lifecycle, purposes and floors, Working crafts, Blood Structures, rite spaces, the Rites & Crafts map |
| Station upgrades | [STATION_UPGRADES.md](STATION_UPGRADES.md) | The shared catalog, progress and rite contract for every upgradeable station |
| Manipulations and casting | [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) | Roster and balance, school combat, targeting, engram weaving, casting animations, outpouring, impacts, infusion |
| Manipulation visuals | [MANIPULATION_VISUALS.md](MANIPULATION_VISUALS.md) | The Hematic Riposte standard every focal visual is graded against |
| Engram weaving | [engram-weaving/README.md](engram-weaving/README.md) | The player's guide to the Somatic Loom puzzle, with screenshots |
| Bosses | [BOSSES.md](BOSSES.md) | Annetta, Vesper, the Mycophant, the shared presentation (bar frames, opening scenes, clocks) |
| Hematic Succession | [HEMATIC_SUCCESSION_DESIGN_SPEC.md](HEMATIC_SUCCESSION_DESIGN_SPEC.md) | The implementation contract for manifested successor NPCs |
| Antecedent recordings | [DEEP_DARK_RECORDING_REWRITE.md](DEEP_DARK_RECORDING_REWRITE.md) | The transcript that must match the shipped OGG recordings and captions |
| Sound | [SOUND.md](SOUND.md) | Sound events, the per-manipulation cast events, the sourcing rules and routine; the credits list [SOUND_CREDITS.md](SOUND_CREDITS.md) |

## Backlog

[OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) is the one list: the questions that need the owner's answer, the choices a
builder will make unless he objects, the remaining work (with what each row waits on), the play-and-tune list and what
is parked. No other file lists open questions. A topic doc's "Open questions" section is a pointer to it.

[owner_answers/](owner_answers/00-README.md) holds the plans behind that list: seventeen chunk files written from the
owner's answers of 2026-11-13 (one per work area, each with its rulings, its tasks and a "Status on 2026-11-14" block),
and [BUILD_LOG_2026-11-14.md](owner_answers/BUILD_LOG_2026-11-14.md), the record of the overnight build that followed.

[audits/2026-11-02-cleanup/README.md](audits/2026-11-02-cleanup/README.md) is the consolidated ledger of the
2026-11-02 whole-mod cleanup audit (eleven read-only reports in the same folder: progression, dead code, dialogue, books,
animation and effects, ecology, materials, visuals, systems, resources and lang, runtime, docs drift). It is worked from
in order; a row that needs a call moves into OPEN_QUESTIONS.md, a fixed row is struck. Its owner calls were answered on
2026-11-13.

[audits/2026-11-06-circus/README.md](audits/2026-11-06-circus/README.md) is the same kind of ledger for the Crimson
Troupe (five reports: quests, bosses, puppetry, worldgen, tests and docs). Its owner calls (D-29 to D-46) were answered
on 2026-11-13; what they left open is in OPEN_QUESTIONS.md under the Crimson Troupe.

[audits/2026-11-11-visual-balance/](audits/2026-11-11-visual-balance/) holds the visual and balance audit of
2026-11-11 (its `tuning-sheet.csv` lists the tunable numbers). The 520-row answer sheet compiled from it and from every
other open list, the owner's answered copy, and the review sheets that followed are archived in
[archive/tracks/owner_answers_2026-11/](archive/tracks/owner_answers_2026-11/); their content now lives in the topic
docs' rulings and in OPEN_QUESTIONS.md.

[audits/2026-11-13-sound/README.md](audits/2026-11-13-sound/README.md) is the sound audit: one row per sound moment
(every `sounds.json` event, every vanilla `SoundEvents` call, and the silent moments that need a sound) with a target
sound and library search phrases, in eleven area files plus a shared palette. It is a dated snapshot; what has been
built since and the rulings are in [SOUND.md](SOUND.md), and the remaining rows are in OPEN_QUESTIONS.md under Sound.

## Engineering docs

| Folder or file | Role |
| --- | --- |
| [architecture/ARCHITECTURE_STANDARDS.md](architecture/ARCHITECTURE_STANDARDS.md) | The architecture rules and the index of every guardrail test. Read before adding a system. |
| `architecture/` (the rest) | The 2026-10-21 audit, map, proposal, roadmap, session rules and the consolidation ledger (`CONSOLIDATION_PROGRESS.md`, whose Decisions table holds each architecture decision and its state). `evidence/` has the subsystem reports and the source-text impact map. |
| [TESTING.md](TESTING.md) | Current test state first (counts, known failing tests, flakes), then the suites, GameTest launchers, review clients and the `/hemo test` fixture workflow. |
| [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md) | The player-centric Harbinger D0 to D8 GameTest suite: what each band covers, how to run it, the harness API, how to add a scenario, the rulings the probes led to and each probe's status. |
| [DEPENDENCY_PROVISIONING.md](DEPENDENCY_PROVISIONING.md) | Pinned jars, the HutosLib snapshot, clean packaged acceptance. |
| [agents/hemomancy-mod-agent.md](agents/hemomancy-mod-agent.md) | The portable agent profile; `AGENTS.md` at the repo root is the short form. |
| `validation/` | Dated validation reports the references link to. Append-only. |
| `superpowers/specs/`, `superpowers/plans/` | Design specs and implementation plans written by the planning skills, named by date. A spec is the record of what was decided when; it is not updated afterwards. |

## Archive

[archive/](archive/README.md) holds finished work-track suites, old audits and plans, and concept material. Nothing in it
is authoritative. See its README for the folder map.

## Keeping it clean

- **A change to mechanics or lore updates the reference docs and the affected topic doc in the same change**, plus the
  wiki page if a player would notice (see the last bullet of `AGENTS.md`).
- **A new feature area gets a topic doc** when its first plan is written, with the headings used here: What it is, Where
  it lives, Rulings that bind, Calls made without the user, Open questions, Proof owed and known gaps, History. Add it to
  the table above.
- **Work-track suites** (one folder of numbered track files) are a planning tool. When the tracks are built, lift the
  rulings, open items and proof owed into the topic doc, then move the folder to `archive/tracks/`.
- **Open questions live in one place only:** `OPEN_QUESTIONS.md`. A topic doc's "Open questions" section is a pointer
  paragraph that names the ids belonging to it. A session that gets an answer records it under "Rulings that bind" in the
  topic doc with the date and deletes the row from `OPEN_QUESTIONS.md`. A session that builds a row deletes it there too.
  A new question goes into `OPEN_QUESTIONS.md` with a recommendation, never into a new sheet.
- **"Calls made without the user"** is for decisions a session made because the user was away or said "go on your
  guess". They stay listed until the user has seen the result in a client and either kept or changed them.
- **Dated snapshots** (audits, validation reports, overnight reports) are never edited after the fact. A later state
  goes in the topic doc, with a line saying what superseded the snapshot.
- **Brainstorm docs keep every version.** A "try again" writes a `_V2` file beside the first; both are kept and archived
  together.
- Markdown under `docs/` is tracked by git everywhere; only the archive's images, generated output and concept-art
  folders are ignored (`.gitignore`).
