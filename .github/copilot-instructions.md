# COPILOT-INSTRUCTIONS.md — Hemomancy Project Orientation

> Project-level notes for Claude. Read this first whenever working in this repo.
> For deep dives, follow the cross-references — do not duplicate their content here.

## What this project is

**Hemomancy** is a Minecraft NeoForge mod (MC 1.21.1, NeoForge 21.1.x, Java 21) by VincentHuto. It is a blood-magic + fungal-horror mod organized around the *quality* of blood manipulation rather than just quantity. Players can pursue one of two mutually-exclusive paths: the **Harbinger** path (embrace blood magic, ascend through 7+1 degrees of a secret society called the Hematic Order) or the **Unstained** path (purify blood corruption out of yourself with hemolytic solution and copper rituals, guided by Our Lady of Still Waters).

- `mod_id` = `hemomancy`, `mod_version` = `5.0.1`, package root = `com.vincenthuto.hemomancy`
- Main class: `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java`
- Author / dev username in run config: `Huto`
- Build: `./gradlew build` (use `runClient` / `runServer` / `runData` launch configs for testing)

## Authoritative reference docs (always check before answering)

These three documents live at the project root and are the source of truth:

- `LORE_REFERENCE.md` — World lore, factions, characters, cosmology, narrative themes. Read this for *intent* — what something is supposed to mean in the world.
- `HEMOMANCY_REFERENCE.md` — Complete mechanical/code reference: every system, item, block, manipulation, mob, recipe, capability, packet, config key. Read this for *implementation* — what is registered, what is wired, what is WIP.
- `MNA_COMPATIBILITY_BRAINSTORM.md` — Planned and in-progress cross-mod features for Mana and Artifice. Each idea includes an "MnA Justification" explaining why it specifically requires that mod.

There is also `lore talk transcript.txt` — raw transcript of lore conversations between the author and an LLM. Useful for catching tone, but `LORE_REFERENCE.md` is the canonical condensed version.

The reference docs are versioned and dated ("Last Updated: 2026-04-19"); always trust the code over the docs when they conflict, then update the doc.

## Tone the mod aims for (do not flatten this)

This is **morally gray** body-horror cosmic-horror — not edgelord, not heroic. Get this wrong and you'll write content that feels off:

- The Harbingers are **not villains**. They are a found-family secret society who view blood magic as a sacred inheritance, not a curse. Most are warm community members; a few have caused harm and given the Order its taboo reputation.
- The Unstained are **not heroes**. Their methods are brutal — they essentially embalm themselves in formaldehyde-equivalent ("Hemolytic Solution") to purify, becoming biologically immortal but glassy-eyed. Their Guardians beat infected people to death with blunt weapons (no blades — they refuse to spill blood).
- The Fungal Entity is **not evil**. It's a 4-dimensional alien organism whose reproductive cycle happens to use humans. The Hematic Order is, unbeknownst to itself, a sporulation strategy — each degree is a stage of fruiting.
- Our Lady of Still Waters is **not a goddess of good**. She's a localized force of nature analogous to a white blood cell — defensive, not righteous. Her last-stage whispers hint at her becoming autoimmune.
- Common folk view blood magic the way they'd view vultures: discomforting, taboo, but not actually evil.

When writing lore, dialogue, item descriptions, or new content, preserve this gray. Avoid "dark cult vs holy church" framing.

## Code layout

```
src/main/java/com/vincenthuto/hemomancy/
├── Hemomancy.java                   # @Mod entrypoint; registers all DeferredRegisters; conditional MnA + Curios setup
├── ClientProxy.java / ServerProxy.java
├── client/                           # All Dist.CLIENT code: render, model, screen, particle, morphling client logic
├── common/
│   ├── init/                         # *Init classes — DeferredRegisters for items, blocks, entities, manipulations, etc.
│   ├── block/ tile/                  # Blocks and their BlockEntities
│   ├── item/                         # All items (organized by subfolder when many variants)
│   ├── entity/                       # Mobs, projectiles, blood constructs
│   ├── capability/                   # IBloodVolume, IBloodTendency, IVascularSystem, IInitiatoryDegree, IUnstainedProgress, etc.
│   ├── manipulation/                 # BloodManipulation base + per-tendency subpackages (animus, ferric, lux, ...)
│   ├── rite/                         # Cardinal Rite logic (degree advancement + utility rites)
│   ├── recipe/                       # Custom recipe types (scar, distillation, recaller, incubator, blood_structure, cardinal_rite)
│   ├── event/                        # Forge event handlers (ArmorSetBonusHandler, CommonEvents, …)
│   ├── encounter/ saint/             # Encounter / Saints system content
│   ├── network/                      # PacketHandler + ~60 packets across 8 channels
│   ├── menu/ loot/ effect/           # Standard subsystems
│   └── worldgen/ worldevent/         # Features, biomes (TerraBlender), structures, blood moons
├── compat/
│   ├── mna/                          # Mana and Artifice integration (faction, spell components, items, rituals, blocks, entities)
│   ├── curios/                       # Curios slot for Charm of Vascularium
│   └── jei/                          # JEI recipe categories
├── config/                           # HemoConfig (root), HemoServerConfig, HemoClientConfig, HemoCommonConfig, HemoMnAConfig
└── mixin/core, mixin/util/           # Mixins: hemomancy.mixins.json + hemomancy.mna.mixins.json
```

`src/main/resources/`
- `assets/hemomancy/textures/{item,block,entity,gui,mob_effect,environment,models/armor,mna,memories,scars}/` — textures (the reference doc embeds many of these inline as image links — useful for quick visual lookup).
- `data/hemomancy/{recipe,structure,loot_table,tags,advancement}/` — datapack content. Recipes, loot tables, and advancements use the 1.21 singular folder names in this repo.
- `META-INF/accesstransformer.cfg` — AT entries (touch carefully).
- `hemomancy.mixins.json`, `hemomancy.mna.mixins.json` — mixin configs.

`src/generated/resources/` — output of the data generator (re-run with `runData`). Some providers (`HemoEntityLootProvider`, parts of `DataGeneration`) are intentionally disabled because their outputs are now hand-authored JSON; check the WIP section of `HEMOMANCY_REFERENCE.md` before re-enabling.

## Naming and style conventions to match

- Registry IDs are `snake_case`. Java classes are `PascalCase`. Init classes are `XxxInit` (not `XxxRegistry`).
- Manipulations live in `common/manipulation/<tendency>/` and extend `BloodManipulation`. Each defines cost, type (QUICK/CHARGED/PASSIVE/CONTINUOUS), rank (HUMILIS→PERFECTUS), tendency, vein section, cooldown, and an action lambda.
- Tendencies are named in **Latin-flavored neutral terms** internally: `Animus, Flammeus, Ductilis, Lux, Mortem, Congeatio, Ferric, Tenebris`. The corresponding **enzyme** items use a different vocabulary (Vivacious, Fervent, Neurotic, Incandescent, Ruinous, Frigid, Ferric, Umbral). Don't conflate them — both vocabularies are intentional and need to stay consistent.
- Lore prose leans **archaic, ecclesiastical, Latinate** for Harbinger material ("Liber Sanguinum", "Sanguine Initiation", "Crimson Lodge"); **clean, sacramental, Anglo-Saxon-flavored** for Unstained material ("Tears of Silthmere", "Pallid Icon", "Lethean Dew", "Our Lady of Still Waters").
- Resource locations: use `Hemomancy.rloc("path")` rather than constructing `new ResourceLocation(MOD_ID, ...)` directly.
- Blood costs in the hundreds for Humilis-rank manipulations, low thousands for Mediocritas/Summa, 5000+ for Grand rites. Match the existing economy.
- Cardinal Rite naming: degree-advancement rites are named after the rank they unlock ("Rite of the Crimson Lodge" → Illuminatus). Utility rites get descriptive names ("Vascular Mending", "Crimson Beacon").

## Mod compatibility — load-order conditional

`Hemomancy.java` only registers MnA, Curios, and the related event handlers if `ModList.get().isLoaded("...")` is true. **Never import a compat class from non-compat code at load time** — gate it behind a check or keep it inside the `compat/` package, otherwise the mod will crash without the optional dep present.

- **Mana and Artifice**: Adds the Harbingers as a custom MnA faction with their own mana resource bar, spell components (BloodLoss, BloodRush, Hemolysis, ManaToBlood/BloodToMana, Sanguilith summon, etc.), Blood Tithe (mana cost partially paid in blood), and the Spell↔Manipulation combo loop (Arcane Resonance / Sanguine Clarity). All tunable via `HemoMnAConfig`.
- **Curios**: Charm of Vascularium slot.
- **JEI**: Custom recipe categories for Chisel Station, Visceral Recaller, Blood Structure, Morphling Incubator.
- **Create / Ponder / Flywheel**: dev-time only (compileOnly / runtimeOnly), no runtime integration.
- **HutosLib (`com.vincenthuto:hutoslib`)**: required runtime library — provides particles, dialogue tree framework (`DialogueTree`), book/codex framework (`BookPlaceboReloadListener`), and other shared utilities.
- **GeckoLib**: used for entity animations.
- **TerraBlender**: required for the custom Nether biomes (Fungal Gardens / Isles) and overworld biomes (Sporecrown Thicket, Hyphal Spires, Drifting Mycelium).

## Things that are easy to break

- **The two paths are mutually exclusive.** Starting Unstained zeroes Harbinger degree; completing a degree rite zeroes purity/clarity/clarityUnlocked/begunPurification. Any new system that grants degree or purity must respect this.
- **`active = false` on `IBloodVolume`** is the gating flag. The capability exists on every player; it's only "on" after the Mortal Display click. Don't assume a player has blood just because they have the capability.
- **Memory items use a 2-layer model** (base `hematic_memory.png` + per-manip overlay at `memories/memory_<name>_overlay.png`). When adding a new manipulation, the overlay texture and the model entry in `HemoItemModelProvider` both need to be added.
- **Skill wiring**: `SkillPointHelper` has the math for every skill, but a few skills (Iron Will, Scar Affinity, Scar Resonance, Scar Mastery) currently have no event-handler caller. If asked "does skill X do anything," check the wiring table in `HEMOMANCY_REFERENCE.md` §9 before answering.
- **Fungal Whisper events** are *somewhat disabled during development* per the reference. Don't assume they fire on every degree-up.
- **Entity loot tables** now live as hand-authored JSON in `data/hemomancy/loot_table/entities/` — the data-generator provider is intentionally commented out. Don't re-enable it without porting the JSON values into the provider first.

## Active WIP areas (do not assume these are finished)

Check `HEMOMANCY_REFERENCE.md` §38 for the full list, but the big ones:

- **Fungal Dimension** — endgame post-Archon astral-projection dimension. Terrain, alien creatures, exit mechanics still in progress.
- **Saints system** — only Hemorath and the Chain Saint are designed; Saints 3 & 4 TBD; trial chamber gen WIP.
- **Annetta Knowles (Stained Priestess)** — two-route Broken Church encounter is wired with dedicated Java models/textures; final animation, projectile rendering, and combat polish remain WIP.
- **Blood Moons** — frequency and ritual trigger designed; full gameplay effects partially wired.
- **Founding Sanctum** — Degree-5 area-consecration system; buffs work, boundary detection still being tuned.
- **Visceral Organs** — extraction ritual and per-organ gameplay effects are implemented; check the reference before changing tuning.
- **Liber Sanguinum guidebook** — opens but renders no content (`HemoProgressionScreen.setupEntries()` is commented out).
- **Blood as a placeable fluid** (`FluidInit`) — commented out.

## When working in this repo

- **Read the reference docs before making lore/mechanics claims.** They are detailed and current; answering from general Minecraft mod knowledge will get details wrong.
- **Match existing naming and tone.** New manipulations, items, and rites need to fit the Latinate/ecclesiastical Harbinger vocabulary or the sacramental Unstained vocabulary, not generic fantasy.
- **Preserve moral grayness.** No new content should make either faction unambiguously good or evil.
- **The mod is fully opt-in.** "Natural infection" exists in the lore but is not a player mechanic, and shouldn't become one without a strong design reason.
- **When updating systems, also update the reference docs** so the audit dates and "WIP" markers stay honest.
- **Use the workspace folder as the live repo.** This is the user's actual mod source, not a sandbox copy. Edits persist.
