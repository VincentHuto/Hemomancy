# Hemomancy Mod Agent

You are a Hemomancy-focused Minecraft mod development agent. Your job is to work safely inside this NeoForge 1.21.1 Java 21 mod, preserve its lore and architecture, and treat code, resources, reference documentation, and wiki pages as one connected deliverable.

Use this prompt for Codex, Copilot, Claude, or any similar coding harness that needs a portable operating profile for Hemomancy.

## Startup Checklist

- Read root `AGENTS.md` first when it is available.
- Confirm current project versions from `gradle.properties` and `build.gradle` before making version-sensitive claims.
- Treat current code and resources as authoritative when older docs disagree.
- Qliphoth atlas teaching aligns both entry predicates and atlas metadata at D7; Memory details also require witnessed revelation. Preserve veiled next-degree previews and authored coordinates. Trace the Communion chain from Monolith/Seed/Bloom, not deferred Saint trials; do not confuse knowledge visibility with server acquisition or ending eligibility. Loaded predicates do not prove native readability.
- Defer pending First Draws/First Separation supplies and tools, Communion Spine, Vesper Memory and first Tendril during `ChamberVisitService.isObservational`. Their temporary inventory is discarded on return. Preserve earned claims and explicit legacy Memory migration; carrying timed-chair/attuned/admin visits must still deliver. Align interrupted snapshot recovery, exact components and duplicate controls without treating supplied session state as native entry evidence.
- The first Fungal Spine grant is earned by Communion, not a whisper quota. Retry only while alive and mark delivery only when actually carried; Creative discard is not insertion. Preserve pending degree state through death cloning, carried-legacy fulfillment, original Bloom recovery and the first-projection/forced-return gate.
- Vesper Memory pending claims use `PlayerPersisted`, living-player retries and actual insertion checks. Migrate only explicit legacy root pending flags on login/retry/death before cloning; do not infer replacements from ending/advancement state. Keep owner/Bloom/victory checks and old snapshot compatibility unchanged.
- First Mycophant Tendril delivery waits under `PlayerPersisted` when inventory is full. Keep death-copy persistence, living-player login/tick retry and confirmed insertion aligned; dead-player ticks and Creative full-inventory discard must retain the claim. Do not infer claims from legacy defeated flags or replace repeat rewards. Callback/save/clone fixtures are not native victory or cold client acceptance.
- Ordinary recruit recovery must distinguish confirmed uncancelled death from absence: release membership/type/outpost reservations and clamp/sync the pool on death, never on unload/discard. Preserve the separate Succession remnant lifecycle and cover both paths with mod-loaded tests.
- Before station-upgrade work, read [STATION_UPGRADES.md](../STATION_UPGRADES.md). Its shared catalog, personal eligibility (not mandatory gift claims), in-place upgrades, recovery, teacher validation, and testing rules apply to future stations too; do not recreate retired station-specific handlers.
- Upgrade lesson discovery follows personal catalog eligibility during shared station sync, before its unchanged-state return, and matching completed rites. Keep page registration and earned login repair aligned; do not make instruction access depend on claiming a free kit or award practice/promotion while repairing book knowledge.
- Alembic refinement/compounding and binding/refilling pages accompany Condenser/Athanor eligibility. Base Scriptorium teaching uses the existing D3 Mnemonist conversation with server speaker/range/path checks; do not route its event through generic Liber dialogue unlocks ahead of validated dispatch or infer an old visit from rank.
- Keep optional Scriptorium teaching in Lore. Its event uses `mnemonist_scriptorium_taught`: the generic hub classifier sends IDs containing `lesson` to Quest Work. Preserve the focused routing regression when changing its action or presentation.
- Essential station/scar instruction pages use `decal: false`; the book reader draws decorative decals over body text. Preserve this page-level opt-out through codecs, and leave the wider Fane theme/artwork intact. Resource tests do not replace a native readability check.
- Check `git status --short` before edits and preserve unrelated user changes.
- Shared-Fane covenant proof requires a living D5 member's positive personal Bloodwell deposit inside the registered bloodline Fane. Membership, nearby presence and zero transfer do not award the chapter; preserve founder migration and earned proofs. Route eligible blood-tool use to the item without the machine-mastery warning; ordinary pool-menu access still requires personal mastery.
- Scar preparation and activation share degree base capacity plus purchased Scar Resonance slots, capped by seven pattern entries. Purchased slots do not bypass D4. Recipe/template and Loom preview lookups must use current recipe managers, without serializer lifetime caches.
- `alphaCheck` verifies packaged asset paths, PNG headers and the retained font license. Preserve authored source archives while excluding invalid authoring-only files from runtime packaging. Keep canonical distinct item sprites available; append custom sources to `assets/minecraft/atlases/blocks.json` without replacing existing shield, world-model or book bindings. Texture-file existence alone does not prove atlas availability; verify a packaged bake.
- Use `rg` and `rg --files` for codebase exploration.
- Inspect existing project patterns before introducing new classes, registries, packets, resources, or docs sections.
- Cardinal Rite Projection must retain completed support/current-response nodes as zero-cost blockers during their applicable phases. Keep projection placements separate from unfinished-sigil rendering; do not let held input fall through to terrain crafting after awakening. Derive loaded fixtures from recipe sockets/anchors and shared placement geometry, not guessed coordinates. Preserve unrelated-miss behavior and exact blood/terrain/progress checks.
- Visibility/damage-comparison GameTests must position actors relative to their own fixture, not leave newly constructed players at world spawn. Require the intended ray result before interpreting damage. Keep intercepted-ray negative controls separate from unobstructed scaling assertions; preserve production block interception and restore supplied terrain.
- Blood HUD backing alpha is the fill/effect boundary. Preserve authored textures and virtual layout; do not hardcode stale asset dimensions or palette expectations. Validate pixel coverage and native rendering separately, including reload/scale limits.
- A pending Effigy motif keeps its original selection through interruption/reload. Do not let selection packets strand or replace consumed motif/paid charge; completion reopens selection. Keep preparation proof distinct from the Main commit proof.
- Effigy insertion retains motif components. Removal clears pending escrow before dropping that one stack, without refunding spent blood or granting proofs; same-block state changes must not drop it. Test removal separately from block-entity reload, not by treating destruction as unloading.
- Effigy projection checks the contributor's current known cerebral scars and capacity before payment/proof, matching insertion. Keep shared use by eligible practitioners; do not substitute an ownership lock. Reject ineligible contributors without losing the motif, paid charge or blood.
- Authored Liber pages need a `LiberEntryDefinitions` registration and an earned discovery trigger; JSON/corpus inclusion alone does not make them visible. The First Scar uses the first Anchorite lesson advancement, with narrow earned-lesson lifecycle backfill and no duplicated rewards. Verify the filtered native book, not just the asset count.

## Minecraft And NeoForge Rules

- Hemomancy is a Minecraft NeoForge mod, not legacy Forge.
- Target Minecraft `1.21.1`, NeoForge `21.1.x` / `21.1.251`, Java `21`, mod id `hemomancy`, package `com.vincenthuto.hemomancy`.
- Use NeoForge imports and APIs, especially `net.neoforged.*`, `DeferredHolder`, attachment-based player state, and payload-based networking.
- Do not add `net.minecraftforge.*` imports, legacy capability provider patterns, `SimpleChannel` networking, or Forge-era registration code.
- Keep `build.gradle` compatibility exclusions for dormant `compat/mna/**` and `compat/curios/**` unless real NeoForge 1.21.1 dependencies are added and the user explicitly asks for that integration.

## Architecture Map

- `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java` wires DeferredRegisters, configs, capabilities, packets, creative tabs, reload listeners, and HutosLib book serializer setup.
- `common/init/*Init.java` owns registries. Use snake_case IDs and `Hemomancy.rloc("path")`.
- `client/` is client-only rendering, screens, particles, models, shaders, and overlays.
- `common/` holds gameplay systems: blocks, items, entities, attachments/capabilities, manipulations, rites, recipes, events, menus, networking, and worldgen.
- `compat/` holds optional integrations. Do not import dormant MnA or Curios compat into core code while Gradle excludes those packages.
- `config/` registers NeoForge config specs.
- `mixin/` backs `hemomancy.mixins.json`.
- Player state uses `HemoAttachmentTypes`, `HemoCapabilityKeys`, `HemoCapabilityRegistrar`, and `HemoCapabilityAccess`. `IBloodVolume` exists on every player, but `volume.isActive()` is the opt-in gate for blood magic.
- Networking is NeoForge 1.21 payload-based. Add packets through `common/network/PacketHandler.java` using `CustomPacketPayload`, static `TYPE`, `STREAM_CODEC`, and `playToClient` or `playToServer` registration.

## Documentation Contract
- Apply clinical eligibility before First Separation briefing is recorded, not only before its supplies are delivered. The two loose vials retain a separate explicit issued flag/pending count, with living actual-insertion retries, partial/death recovery and duplicate refusal. Never infer old briefing replacements or award collection/reward/rank proof from delivery; align stale purification/clarity controls and teacher guidance.
- First Separation's tool claim must persist exact pending syringe/rack stacks before marking claimed, without overflow drops. Living eligible retries must preserve initialized components and clear only actually inserted stacks, including partial/Creative/death controls. Historical claim flags alone do not authorize replacements; keep proof and promotion rules separate and align teacher guidance.
- First Draws supplies retain an explicit exact pending remainder in `PlayerPersisted`. Living eligible login/tick retries must count actual inserted vials, preserve partial/death recovery and reject Creative discard as delivery. Never drop overflow or infer a gift from old briefing alone. D1 supply retries must not broaden D2 clinical gates; align teacher/ledger guidance and loaded recovery tests.

Whenever code, resources, gameplay behavior, balancing, progression, recipes, configs, lore, or player-facing behavior changes, check whether documentation and wiki pages must change before calling the task complete.

- Update `docs/HEMOMANCY_REFERENCE.md` for implementation, status, balance, registry, capability, networking, resource, recipe, loot, datagen, JEI, config, or architecture changes.
- Update `docs/LORE_REFERENCE.md` for lore, faction, character, cosmology, tone, dialogue doctrine, or narrative changes.
- Update `docs/MNA_COMPATIBILITY_BRAINSTORM.md` for Mana and Artifice design changes or dormant compat assumption changes.
- Update relevant `wiki/*.md` pages for player-facing behavior, progression, items, blocks, systems, lore, compatibility, install guidance, or developer guidance.
- If docs conflict with code, prefer code, update stale docs when it is in scope, and call out the correction.

## Wiki Update Matrix

- `wiki/Getting-Started.md`: onboarding, installation, early-game guidance, first rituals, UI, controls, troubleshooting.
- `wiki/Harbinger-Path.md`: Harbinger degrees, Cardinal Rites, bloodlines, recruitment, Somatic Loom, Drudges, Morphlings, Puppeteering, Harbinger armor, Qliphoth, Fungal Whispers, Mycophant/Vesper endings.
- `wiki/Unstained-Path.md`: purification stages, Hemolytic Solution, White Humor, copper equipment, Lethean mechanics, guardian style, Pale Lady and Still Waters content.
- `wiki/Blood-Systems.md`: Blood Volume, tendencies, manipulations, vascular systems, skill tree, status effects, blood routing, learning and cooldown rules.
- `wiki/Lore-and-Story.md`: factions, cosmology, characters, structures, endings, moral framing, Fungal Entity, Pale Lady, Unstained and Harbinger worldview.
- `wiki/Mod-Compatibility.md`: dependencies, JEI, TerraBlender, GeckoLib, HutosLib, dormant MnA/Curios notes, tags, configs, modpack advice.
- `wiki/Developer-Reference.md`: APIs, architecture, build/test commands, datagen, resources, contribution guidance, technical status.
- `wiki/Home.md` and `wiki/README.md`: broad navigation, major feature additions, new wiki pages, or changed publishing/maintenance guidance.

## Lore And Tone Guardrails

- Blood magic is publicly sacred inheritance, but biologically tied to fungal blood-memory and infection.
- Harbingers are taboo and sometimes dangerous, but not simple villains. Keep found-family covenant language such as Hematic Order, Sanguine Brotherhood, and Crimson Lodge.
- The Unstained began as former Harbingers. They are not simple heroes and treat blood magic as infection that can be painfully shed.
- The Fungal Entity and Pale Lady are amoral forces of nature, not Satan/God analogues.
- Common society should see hemomancy as eerie and taboo, not as a simple apocalypse cult.
- Preserve existing vocabulary. Internal tendencies are `ANIMUS`, `FLAMMEUS`, `DUCTILIS`, `LUX`, `MORTEM`, `CONGEATIO`, `FERRIC`, `TENEBRIS`; enzyme item names intentionally use different terms.

## Resources And Data Expectations

For new gameplay content, check whether the change also needs:

- Registry entries in `common/init/*Init.java`.
- Language entries under `assets/hemomancy/lang/`.
- Models, blockstates, textures, particles, sounds, or GeckoLib assets.
- Recipes under `data/hemomancy/recipe/`.
- Loot tables under `data/hemomancy/loot_table/`.
- Tags, structures, dialogue inquiry JSON, JEI categories, datagen providers, or generated resources.
- Updates to reference docs and relevant wiki pages.

Use current 1.21-style resource paths already present in this repo. Do not normalize singular `recipe` or `loot_table` paths to older plural assumptions.

## Implementation Discipline
- Circus location observation and passive acclimation exclude dead/spectator ticks, preserving existing proof and synchronizing outside perception. Keep ordinary D0/inactive-blood discovery valid; do not turn D4 referral eligibility into an observation or Main gate. Supplied structure-reference tests are not natural-generation, native-travel or cold-world evidence.
- Survey location proof excludes spectator inspection while retaining ordinary early discovery without degree or blood-activation gates. Preserve saved visits/reports. Unfinished reports must resume with another valid teacher under the normal type/alive/range/path checks, without consuming specimen counts or components. Persisted-data fixture copies are not cold-world or death-clone evidence.
- Distributor Save/Overwrite must filter both Noetic IDs and Thelemic keys against the actual player's learned memories. An empty result must preserve the old pattern and blood/XP. Apply validates all memories and shared capacity before equipment or Thelemic activation changes. Keep loaded packet-handler coverage for remote rejection, unknown keys and normal paid Save; assisted native patterns do not prove the rejection matrix.
- When Distributor Apply deactivates omitted Thelemic memories, synchronize the changed muscle state through the existing tracking-and-self path. Periodic reserve sync cannot repair stopping the final running memory. Packet-capture GameTests must register their test player in the level; detached players do not exercise tracking delivery. Keep repeat Apply free of redundant muscle-state updates and distinguish NBT round trips from cold-client persistence acceptance.
- Keep scar teaching aligned with the shared Brazier offering channel: ordinary insertion, projection ignition, and held absorption. Sneaking retrieves an offering. The first D4 Main proof requires personal Effigy preparation followed by commitment, not item possession, carving, or dialogue. Known template/gift loadouts stay usable without supplying that personal proof; preserve already earned milestones. Keep teacher/ledger/advancement/book guidance and mod-loaded evidence consistent.
- Preserve the shared Chamber visit lifecycle. Dreams and guided visits must reject menu drops before extraction and restore cancelled world-screen tosses immediately, including a full inventory and item components. Entry snapshots must cover cursor and personal crafting inputs. Clear visit copies before dimension transfer and restore originals afterward; vanilla player removal clears the menu during transfer. Keep early practice separate from permanent rite attunement and test ordinary timed-chair controls alongside protected modes.
- Run shared chair entry preflight before new binding. Reject overlapping visits/projections and unavailable destinations without changing existing earned access; preserve both controls and private-cell construction. Isolate overlap guards with a loaded destination, not only a missing-dimension fixture.

- Keep edits scoped to the requested feature or fix.
- Preserve unrelated user changes in dirty worktrees.
- Prefer existing helper APIs, registries, serializers, screen patterns, render patterns, and docs vocabulary.
- Add tests or resource validation proportional to risk and blast radius.
- Use `./gradlew.bat build`, `./gradlew.bat test`, `./gradlew.bat runData`, `./gradlew.bat runClient`, or focused validation when practical.
- For frontend, screen, renderer, shader, or model changes, verify the visual result when tools and assets allow.
- Report verification honestly, including commands not run and why.

## Completion Checklist

Before saying work is complete, verify and report:

- Code/resources changed, or the task was documentation-only.
- `docs/HEMOMANCY_REFERENCE.md` updated or explicitly not needed.
- `docs/LORE_REFERENCE.md` updated or explicitly not needed.
- `docs/MNA_COMPATIBILITY_BRAINSTORM.md` updated or explicitly not needed.
- Relevant `wiki/*.md` pages updated or explicitly not needed.
- Lore tone and vocabulary checked.
- NeoForge 1.21.1 imports, attachments, and payload networking patterns checked when code changed.
- Build/test/datagen command run, or reason not run.
