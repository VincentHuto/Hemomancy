# Saint Reconciliation Pass — Plan

Spec: `docs/superpowers/specs/2026-10-26-saint-reconciliation-design.md`. Execution: four parallel agents, each owning a disjoint set of files. The orchestrator then integrates, builds and tests serially. Nothing is committed.

## Shared rules for every task
- **No Gradle.** Do not run Gradle; the shared build directory collides when two builds run. The orchestrator compiles and tests.
- **Shared files.** Only Task D edits `src/main/resources/assets/hemomancy/lang/en_us.json`. Other tasks list each lang key they need, with its English value, in their final report.
- **Code style.** Match each file's existing style. Follow AGENTS.md: no `Component.literal` English, `Hemomancy.rloc`, the shared test helpers in `src/test/.../support/`, and source-text assertions only under `architecture/`.
- **Tests.** Update or extend the tests and GameTest fixtures that pin anything a task changes, but only in that task's own area. A test that pins another task's files should be reported, not edited.

## Task A — Tendency data and doctrine removal
Files owned:
- `EnumSaintType`, `EnumDoctrineTag` (delete), `HallowedResiduumItem`
- `ManipulationInit` (the four Canon Memory entries only)
- `CrimsonTitheManip`, `UnclosingEyeManip`, `BloomOfRotManip`, `EndlessHourManip` (Javadoc and tendency text only)
- `recipe/saint_relic_*.json`, `recipe/memory_weaving/memory_{crimson_tithe,unclosing_eye,bloom_of_rot,endless_hour}.json`
- Any other code or data that maps a Saint or Canon Memory to tendencies (microscope, materials atlas, JEI, fingerprints)
- Their tests and GameTest fixtures

Work:
1. Set `EnumSaintType` to HEMORATH(ANIMUS, FERRIC), SERAPHAE(LUX, DUCTILIS), PUTRICIEL(MORTEM, FLAMMEUS), VELORUM(CONGEATIO, TENEBRIS). Remove the doctrine fields and accessors, and remove `getBaseSuccessChance` if it is unused.
2. Delete `EnumDoctrineTag` and the residuum doctrine tooltip line.
3. Set the manipulations to: crimson_tithe ANIMUS/FERRIC, unclosing_eye LUX/DUCTILIS, bloom_of_rot MORTEM/FLAMMEUS, endless_hour CONGEATIO/TENEBRIS.
4. Recipes:
   - Loom: crimson_tithe uses animus 1 + ferric 1; unclosing_eye uses lux 1 + ductilis 1.
   - Hemorath relic: bone + vivacious_enzyme + ferric_enzyme.
   - Seraphae relic: already incandescent + neurotic (Lux + Ductilis); verify it.
5. Lang removals for D to apply: `saint_doctrine.*` and `tooltip.hemomancy.hallowed_residuum.doctrine`.

## Task B — Encounter lifecycle, endings, loot, Seraphae hierarchy
Files owned:
- `SaintTrialEntity`, `RisingSaint`, `SaintRise`, `HarbingerSaintEncounterHooks`
- `HemorathEntity`, `PutricielEntity`, `VelorumEntity`, `SeraphaeEntity` (with its fragment and anchor where needed), `HematicCollapseGoal`, `EmptyPulseGoal`
- `SaintSarcophagusBlock`, `SaintSarcophagusBlockEntity`
- `loot_table/entities/{hollow_vessel→hemorath, seraphae, putriciel, velorum}.json`
- `PutricielFrame` (pronoun in the Javadoc only)
- Their tests and GameTest fixtures

Work:
1. **Seraphae extends `SaintTrialEntity`.** Add overridable hooks where the base assumes melee or an HP bar, and delete her duplicated rise code. Her containment behaviour, timings, integrity, bar and save keys must stay exactly as they are.
2. **Tomb return.** The boss remembers its tomb position and the original sarcophagus block state, saved in NBT. Whenever the encounter ends, the sarcophagus is restored there: same Saint, DORMANT, lid closed, not consecrated, `sampleYielded` false. The endings are Hemorath's overload or HP death, Putriciel's fifth absolution, Seraphae's containment and Velorum's death. Each restores the tomb once, and a removed or unloaded boss does not duplicate the restore. Put a generic return line in chat (key `message.hemomancy.saint.returns_to_tomb`, "The sarcophagus closes over %s once more.").
3. **One reward path.** A shared `SaintTrialEntity` method rolls `entities/<id>` once per encounter and replaces the scattered direct residuum drops. Vanilla death loot must not roll a second time.
   - Tables: Hallowed Residuum ×1 for every Saint, plus 1–2 `sanguine_quintessence`.
   - Rename `hollow_vessel.json` to `hemorath.json`.
   - Hemorath keeps inserting into the player's inventory if that is the current behaviour; otherwise the loot drops at the tomb.
4. **No random Saint.** Remove the random roll. An unassigned sarcophagus answers with key `message.hemomancy.saint_sarcophagus.unassigned` ("No Saint rests in this sarcophagus.") until a relic names it.
5. **Velorum defeat line.** Add key `message.hemomancy.velorum.returns_to_stillness`: "Velorum stills. The long dormancy takes him back."
6. Fix `PutricielFrame` "Her" to "Their".

## Task C — Lore and reference docs, wiki
Files owned: `docs/LORE_REFERENCE.md`, `docs/HEMOMANCY_REFERENCE.md`, `docs/MOB_DROP_CATALOGUE.md`, `wiki/*.md`.

Work:
1. Rewrite `LORE_REFERENCE.md` §10 from the uploaded lore document, amended by the spec's rulings: tendencies, titles, Putriciel's pronouns, the endings, the end of the Archon framing for Hemorath, and Velorum without martyrdom.
2. Add the ascension/recognition doctrine and the continuity notes. Keep each Saint's current Appearance paragraph.
3. Fix canon rule :38, the "randomized" lines, doctrine mentions, the per-Saint trial chamber claims (say the chambers are planned), the §25.7.2 loom table, the manipulation tendency table, the Saint tables, loot and titles.
4. Keep release-scope statements.
5. Do not describe the deferred per-Saint overhauls as built.

## Task D — Liber and lang
Files owned: `src/main/resources/data/hemomancy/books/libersanguinium/**`, `LiberEntryDefinitions` and `LiberDiscoveryEvents` (Saint entries only), `en_us.json`, and lang/voice goldens.

Work:
1. **Liber pages.**
   - Rewrite `the_saints.json` and `hemorath.json`.
   - Add `seraphae.json`, `putriciel.json` and `velorum.json` in `cosmic_forces`, with discovery registration like HEMORATH's.
   - Fix `manipulations/pages/saint_memories.json`: the residuum → Centrifuge → Loom route, and the four tendency pairs. Near-time wording for Endless Hour may stay.
   - Move Crimson Tithe off the Mortem (ruinous) enzyme page and onto the Animus/Ferric enzyme pages if those list memories.
2. **Lang.**
   - Title cards: `boss.hemomancy.hemorath.epithet` "The First Saint", seraphae "The Chain Saint", putriciel "The Befouled Saint", velorum "Saint of the Endless Hour".
   - Replace "Hollow Vessel" in player text with Hemorath.
   - Putriciel lines use they/them.
   - Putriciel's rest line: "Putriciel is purified, and will rest for a time."
   - Velorum Mask line: drop "martyr-metal".
   - Rewrite the Vicar `saints_lore` line in the Vicar's voice (`docs/NPC_VOICES.md`). The Saints were sealed by their own people, and the Order inherited and keeps the tombs.
   - Add the keys from Tasks A and B.
   - Update any lang pins or goldens.
