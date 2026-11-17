# Owner answers: the plans

Updated 2026-11-16. This folder holds the plans. The list of what is open is one file,
[../OPEN_QUESTIONS.md](../OPEN_QUESTIONS.md). Nothing in this folder lists open questions any more.

## What is here

| File | What it is |
| --- | --- |
| `01-...md` to `17-...md` | One chunk file per work area, written on 2026-11-13 from your 520 sheet answers. Each has "Rulings reviewed" (one row per sheet id, with the file that was checked), a "Plan" of tasks T1, T2, ... with the exact values, files and tests, and a "Status on 2026-11-14" block that says which tasks are built. |
| [BUILD_LOG_2026-11-14.md](BUILD_LOG_2026-11-14.md) | The record of the overnight build: a morning report, then one section per package (P01 to P18) with what was built, skipped, tested and recorded. |
| [BUILD_LOG_2.md](BUILD_LOG_2.md) | The record of build 2 (2026-11-14 to 2026-11-16): a report at the top, how to verify, the baseline, then one section per package (B01 to B37) with its verification. |

A task id such as "04 T5" in `OPEN_QUESTIONS.md` means task T5 in chunk file 04.

The "New questions" and "Still to do" tables inside each chunk file are the 2026-11-13 snapshot. They were merged,
cut down and moved into `OPEN_QUESTIONS.md` on 2026-11-14 and are kept only as the working behind those rows.

## How the work went

1. **2026-11-11.** Every open question in the repo was compiled into one 520-row sheet.
2. **2026-11-13.** You answered all 520 rows. A review sorted them into the 17 chunks, checked each row against the
   code and wrote the plans here. It found 14 answers the 2026-11-12 work had overtaken and raised 183 new questions;
   another 76 rows had never been asked.
3. **2026-11-14, overnight.** A build ran the tasks that waited on no open question: 18 packages, each implemented and
   independently verified, none reverted. Nothing was committed.
4. **2026-11-14, after.** Every open row was triaged against the code again. The 273 open rows became 102 questions
   for you, 159 choices a builder will make unless you object, and one list of remaining work. The topic docs got
   every ruling from the sheet, and the superseded sheets were archived.
5. **2026-11-14 to 2026-11-16, build 2.** You answered all 102 questions and objected to no default. Build 2 ran 37
   packages (B01 to B37), each verified, none reverted, nothing committed. It built every answered row except the
   ones you held, the PT-85 investigation and a handful it skipped with reasons. The final check was green: the unit
   suite, two combined GameTest runs and the playthrough suite. The report is at the top of
   [BUILD_LOG_2.md](BUILD_LOG_2.md). What is left is 8 questions, 7 defaults, 35 work rows (9 ready) and 180 play and
   tune rows.

## Where each area stands

Counts are rows in `OPEN_QUESTIONS.md` on 2026-11-16, after build 2. "Ready" work can be built today with no answer.

| Chunk | Area | Questions for you | Builder defaults | Work rows | Ready now | Play and tune |
| --- | --- | --- | --- | --- | --- | --- |
| [01](01-living-blood-and-containers.md) | Living blood and containers | 0 | 0 | 0 | 0 | 11 |
| [02](02-ground-network-and-farming.md) | Ground network and farming | 0 | 0 | 0 | 0 | 6 |
| [03](03-menagerie-research-and-teaching.md) | Menagerie and the firsts | 1 | 0 | 1 | 0 | 6 |
| [04](04-cast-economy-and-temper.md) | Cast economy and Temper | 0 | 0 | 0 | 0 | 14 |
| [05](05-verbs-sculpting-and-strands.md) | Verbs, strand skills and cleats | 1 | 0 | 1 | 0 | 13 |
| (no chunk) | Lash and rebuilt held forms | 0 | 0 | 1 | 0 | 7 |
| (no chunk) | HUD and the Bandolier | 0 | 0 | 0 | 0 | 6 |
| [06](06-creatures-and-spawns.md) | Creatures and spawns | 0 | 0 | 0 | 0 | 8 |
| [07](07-bosses.md) | Bosses | 0 | 0 | 1 | 1 | 10 |
| [08](08-saints-and-mausoleums.md) | Saints and mausoleums | 0 | 1 | 5 | 0 | 12 |
| [09](09-visuals-particles-and-texels.md) | Visuals, particles and texels | 1 | 2 | 8 | 2 | 7 |
| [10](10-early-and-mid-progression.md), [11](11-bloodline-and-throne.md) | D0 to D6 journey and bloodlines | 3 | 3 | 5 | 0 | 12 |
| [12](12-endgame-paths.md) | D7 to D8 endgame | 0 | 0 | 0 | 0 | 8 |
| [13](13-rites-and-crafts.md) | Rites and Working crafts | 0 | 0 | 1 | 0 | 8 |
| [14](14-brined-court.md) | Brined Court | 0 | 0 | 4 | 3 | 10 |
| [15](15-circus.md) | Crimson Troupe | 1 | 0 | 1 | 0 | 20 |
| [16](16-npc-dialogue-canon-and-liber.md) | Dialogue, lessons and Liber | 1 | 1 | 2 | 1 | 8 |
| (no chunk) | Sound | 0 | 0 | 3 | 0 | 5 |
| [17](17-architecture-combat-rules-and-housekeeping.md) | Architecture, tests and housekeeping | 0 | 0 | 2 | 2 | 9 |

The lash, the HUD and sound have no chunk file. Their working is in the lash and HUD ledgers under
`.superpowers/sdd/` and in `docs/audits/2026-11-13-sound/`.

## Suggested order for what is left

1. **Commit the tree.** 943 tracked files are changed. The Sculpting rename should start from a clean tree.
2. **Refresh the three stale test snapshots.** Done on 2026-11-14 in the build 2 baseline (`BUILD_LOG_2.md`).
3. **Answer group 1.1 and NQ-05-1** in `OPEN_QUESTIONS.md`, then run the Sculpting rename alone as its own commit.
4. **Build the ready rows.** About 100 rows need no answer. They are small and mostly independent.
5. **Answer NQ-17-1 to NQ-17-3**, then finish the combat refactor alone (17 T12, T13). The price sheet (04 T5c)
   comes after it.
6. **Accept or change the starting numbers** (group 1.4). That frees most of the balance work.
7. **Design answers by area**, as you pick each area up: the clot design, the Circus rebuild, the Saint gifts,
   bloodlines, the sound direction.
8. **Words last.** Lessons, inquiries, Liber pages and the docs-drift pass go stale each time a number or a name
   moves, so they are written once at the end.

## Rules for the build

- A step-level plan is written for a chunk when you pick it. The plans here are task-level.
- One Gradle build at a time. No two sessions build in this tree together.
- Tasks in different chunks that edit the same file are named in each task's "Depends on".
- A new payload bumps `PacketHandler.PROTOCOL_VERSION` (50 today) and rewrites the payload registry file. The ones
  still to come: the leader's confirm (chunk 11), the cleats toggle (chunk 05), the Circus act (chunk 15).
- A shader change needs a client launch to prove it compiles.
- When a row is built or answered, delete it from `OPEN_QUESTIONS.md` and record it in its topic doc.

## Archived

The blank sheet, your answered copy (it was `docs/Open Questions.md`), the conflict sheet, the new-questions sheet,
the never-asked sheet, the first still-to-do list and the old `OPEN_QUESTIONS.md` are in
[../archive/tracks/owner_answers_2026-11/](../archive/tracks/owner_answers_2026-11/). None of them is authoritative.
