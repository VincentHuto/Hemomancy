# Harbinger repair once-over

All 36 tasks remain **open for Vince's review**. This handoff separates implemented repairs and focused evidence from the natural campaigns and subjective judgments that have not been completed. Do not interpret a compiled change, supplied-state GameTest or assisted replay as final acceptance.

The detailed chronology, failed attempts and exact assists are in [PROGRESS.md](PROGRESS.md). Original worlds were preserved; replay copies and evidence are under `build/harbinger-repair`. No task closure, commit or staging is part of this pass.

| Open task | Addressed in this repair pass | Once-over / remaining evidence |
|---|---|---|
| RUN-01 | Data-only particle payloads remove dedicated client-factory loading; two-client crash replays and assisted rite restarts | Natural restart and visual parity |
| RUN-02 | Morph buffers retain distinct foil consumers; repeated awakened morph cycling survives | Hands, third person and enchanted appearance |
| RUN-03 | Distinct default controls, independent selection and binding hints | Native held menus and migrated custom bindings |
| DIA-01 | Root choices retain reward events; delivery acknowledgment and claim handling | Remaining NPC/full-inventory reward matrix |
| DIA-02 | Block dialogue context validation and cornerstone delivery | Replaced/stale context and full-inventory replay |
| DIA-03 | Unfinished Vicar and Alchemist lessons remain available at later degrees | Late armor and completed-Chamber counsel |
| DIA-04 | Readable dialogue, portraits and durable deferred whispers; pending-choice reconnect replay | Other screen scales, combat/loom/scar deferral and visual comfort |
| MAT-01 | Four once-only initiation flasks, clear extraction teaching; live paid utilities and reload | Natural raw-material acquisition, first machine/memory pacing and recovery |
| MAT-02 | Physical vial conservation, normal syringe lifespan and atomic completion/retry | Automation and natural syringe pickup |
| MAT-03 | Alembic capacity, container boundaries, recipe outputs and stopped reasons | Native status and crystal growth/harvest |
| MAT-04 | Empty Mausoleum biome route restored to existing temperate biomes | Multi-seed availability, natural rare loot and acquisition cost |
| RIT-01 | D6 Monolith gate, exposed Monolith/Throne interaction cells and block loot | Normal rotated construction and harvesting |
| RIT-02 | Shared readiness/checklist, actual ring targeting, payment boundaries and finale load wait | Remaining orientations, distant anchors and natural support |
| RIT-03 | Trace/defeat objectives cannot win by timeout; ordinary survival remains valid | Live readability, retry cost and support contribution |
| RIT-04 | Dynamic filler collision stops restraint suffocation; live idle/four Hematic upgrades and reload; paid preview | Preview, interruptions and advanced branch materials |
| MEM-01 | Explicit Loom output selection and first-lesson credit | Fresh first weave/learn/cast pacing |
| MEM-02 | Persisted recipe/commitments; original paid failure replay completed | Other recipes, partial strands and dedicated unload |
| MEM-03 | Carving rejection preserves trace; supervised first lesson, grid and modifier preview | Fresh reward route, accounting and other patterns |
| MEM-04 | Activation, overexertion, stop threshold, recovery and Fortification guidance | Natural repetitions/downtime; no global strain increase or tutorial threshold change |
| MEM-05 | First active scar set persists; second credited route must actually differ | Natural referral, matching casts, alternate builds and reconnect |
| COM-01 | Morphling identity/bond writeback and stale menu protection; earned feeding replay | Death, Jar switching and dedicated identity matrix |
| COM-02 | Primal activation dispatches through actual creature state | Active-strain/Gravecap gameplay matrix |
| COM-03 | Spindle roles, shared body limit, recall economics and partner/echo guidance | Native commands and intended active support builds |
| COM-04 | Comparable-trial recorder/reporter with baseline checks, sample count and spread | Five real trials per matchup/build; no unsupported tier rebalance |
| FAN-01 | Canonical founder credit/recovery, personal mastery query and limited supervised scar access | Broader shared-station authority matrix |
| FAN-02 | Personal spent-temple oath, legacy link recovery, live second-player initiation/restart and member role controls | Leader change, leave/rejoin, stale screen and repeat rewards |
| FAN-03 | Projection routing, resolved stations, actual rings and affordable helper payment | Natural moving helpers, blocked routes and instructor discovery |
| FAN-04 | Durable Chamber eligibility; dedicated death/attuned return and drop accounting | Throne respawn, recovery conjuration and never-attuned control |
| END-01 | Shared pending/completed path permissions and preflight gates | Both saved endings: ordinary casting, side rites, puppets and recovery |
| END-02 | Capture last fruit metadata, reject duplicate/out-of-order bound husks, clear only matching fruit; conservative old wait recovery | Full natural nine-fruit lifecycle, origin unload and preserved-save recovery |
| END-03 | Tree messages distinguish growth, pending consumption, chosen branch and completion; deferred choice retained | First travel/return, pruning and both branch presentations |
| END-04 | Encounter/recovery source audit and explicit anchor/absorption/retry instructions | Natural Vesper attempts, downed reconnect, graft and post-victory recovery; difficulty unchanged |
| VAL-01 | Preserved-copy replays, stale-client guards, evidence journal and this checkpoint-by-checkpoint review | Complete fresh core route without progression grants has not been run |
| VAL-02 | Separate controlled-trial reporting and explicit functional/balance boundaries | Natural economics, advanced armor/Anchorite and balance campaign has not been completed |
| VAL-03 | Two actual clients: particle paths, rite restart, death/return, second-player initiation and membership UI | Complete shared journey through both endings has not been run |
| VAL-04 | Inventoried all 14 Observances; five novitiate and nine purification hooks covered; locked-entry prerequisites | Full unassisted Unstained route, death/reconnect and pruning remains |

For the first once-over, prioritize ordinary controls, the starter blood loop, one saved paid weave, Armature entry/exit, a second recruit at a spent temple, and one pome harvest/eat. Those touch the confirmed progression blockers most directly. Then check the advanced lessons and ending presentation with your intended builds.

Balance results remain separate. [Trial recording instructions](../../tools/harbinger_acceptance/README.md) cover fixed starting conditions, active/passive controls, five-run samples, assists and evidence. No empty worksheet or supplied fixture is counted as a played campaign.

Repository verification limitation: `git diff --check` encountered unreadable object `e6d3cae2d97ae7c53eab05ab63e3d9d98f9debd1`. No Git-object repair or checkout reset was attempted. Compilation, JVM tests and runtime checks are recorded independently in the progress log.
