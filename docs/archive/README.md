# docs/archive

History, not guidance. Nothing in here is authoritative: when an archived file disagrees with `docs/HEMOMANCY_REFERENCE.md`,
`docs/LORE_REFERENCE.md`, a live topic doc in `docs/` or the code, the archived file is wrong. Archived files are not
updated when mechanics change. Go to a file here only for a contract table, a coordinate, a ruling number or the reasoning
behind a decision that the live docs summarise.

Only the markdown in this folder is tracked by git (`.gitignore`). Evidence images, generated output and the concept-art
folders stay on the machine that made them.

| Folder | What it holds |
| --- | --- |
| `done/` | The pre-October archive: finished plans, audits, design briefs, the consolidation map, release checklist, deferred-ideas ledger (`DEFERRED_IDEAS.md`), the first-attempt Morphling stats and the casting-animation concept art. Moved here whole from `docs/done`. |
| `tracks/` | Completed work-track suites, one folder per feature: `brined_court/` (11 tracks + the working design spec), `mausoleums/` (12 files; `12-next-pass.md` sums up pass 1), `possession/`, `heart_node/`, `npc_trade/`, `circus/`. Each track file keeps its Status block and the rulings it recorded. The live summary of each is the matching topic doc in `docs/`. |
| `audits/` | Dated read-only audits and handoffs: Cardinal Rites (2026-10-12), Harbinger progression (2026-09-10 snapshot, 2026-10-02 disposition), the manipulation balance plan (passes 1 and 2 landed) and visual catalogue, the mob-drop catalogue and consolidation handoff, the dictation refactor check. |
| `plans/` | Design briefs and implementation packets that have been built or superseded: the Alembic upgrade packet (superseded by `docs/STATION_UPGRADES.md`), the Harbinger progression redesign, the Chamber of Will and Mnemonist brief, the Fungal Gardens dynasties plan, and `endgame/` (the four 2026-10-27 V1/V2 brainstorms that produced the D8 split; both versions kept on purpose). |
| `concept/` | Brainstorm material, concept art, generator output and editorial exports: `Hemo Concept/` (huddles and plates), `Underwater faction ideas/` (the Brined Court's ancestors), `output/` (icon and HUD generator output; `brined-strain-icons-16x16/export_hud_sprites.py` still exports the Morphling HUD sprites), `antecedent-fresh-draft/` and `antecedent-text-export/`, `OrchestratorReports/`. Git-ignored wholesale. |

Specs and plans written by the superpowers skills stay where the skills put them, in `docs/superpowers/specs|plans/`,
named by date. Session ledgers stay in `.superpowers/sdd/` (local only).

## Adding to the archive

Move a file here when its work is finished or abandoned and the live docs carry what still matters. Fix the links that
pointed at it (`README.md`, `AGENTS.md`, the wiki, the reference docs, Java comments) in the same change, and give the
file a line in the table above if it starts a new folder.
