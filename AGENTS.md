# AGENTS.md — Hemomancy

## Snapshot
- Hemomancy is a Minecraft **NeoForge** mod for `1.21.1` / `21.1.219`, Java `21`, mod id `hemomancy`, package `com.vincenthuto.hemomancy`.
- Start with `src/main/java/com/vincenthuto/hemomancy/Hemomancy.java`, then verify versions and dependency gates in `gradle.properties`, `build.gradle`, and `settings.gradle`.
- Treat current code/resources as authoritative when docs drift; use `docs/HEMOMANCY_REFERENCE.md` for mechanics/status and `docs/LORE_REFERENCE.md` for tone.
- For the longer contributor checklist, see `docs/agents/hemomancy-mod-agent.md`.

## Architecture map
- `Hemomancy.java` is the wiring hub: it registers `common/init/*Init.java` registries, configs, `HemoCapabilityRegistrar`, `PacketHandler`, reload listeners like `ItemInquiryLoader`, and the HutosLib `BookPlaceboReloadListener` serializer.
- Package split is strict: `client/` = renderers/screens/particles, `common/` = gameplay/content/networking/worldgen, `compat/` = optional integrations, `config/` = NeoForge config specs, `mixin/` = `hemomancy.mixins.json` support.
- Player state is attachment/capability-driven. Define/register through `HemoAttachmentTypes`, `HemoCapabilityKeys`, and `HemoCapabilityRegistrar`; read/write through `HemoCapabilityAccess` only.
- Important state is always present on players, especially `IBloodVolume`; use `volume.isActive()` as the blood-magic opt-in gate instead of capability presence.
- Harbinger and Unstained are mutually exclusive progression paths; changes to degree/purity logic must preserve the reset rules documented in `docs/HEMOMANCY_REFERENCE.md`.
- Networking is NeoForge 1.21 payload-based. Follow `common/network/PacketHandler.java`: each packet uses `CustomPacketPayload`, static `TYPE`, `STREAM_CODEC`, and `playToClient` / `playToServer` registration. Do **not** add legacy `SimpleChannel` code.
- Datagen lives in `common/data/gen/DataGeneration.java`; `runData` currently generates blockstates, item models, and language only. Server recipe/tag/loot providers are intentionally commented out.

## Workflows that matter
- Windows/PowerShell commands from project root: `./gradlew.bat build`, `./gradlew.bat test`, `./gradlew.bat runClient`, `./gradlew.bat runServer`, `./gradlew.bat runData`, `./gradlew.bat gameTestServer`.
- Run `./gradlew.bat alphaCheck` for the combined JVM and dedicated GameTest alpha gate; see `docs/TESTING.md` for the in-game `/hemo test` fixture workflow.
- `settings.gradle` pulls `../HutosLib` as a composite build when present; otherwise Hemomancy resolves `com.vincenthuto.hutoslib:hutoslib` normally.
- `build.gradle` relies on local jars in `libs/` for TerraBlender, JEI, Create, Building Gadgets 2, WorldEdit, and other dev/runtime integrations; do not assume Maven coordinates exist for every dependency.
- `src/generated/resources` is included as a resource source, but `processResources` excludes duplicates so `src/main/resources` stays authoritative.

## Project-specific patterns
- Use NeoForge imports (`net.neoforged.*`), `DeferredHolder`, and `Hemomancy.rloc("snake_case_id")`; do not introduce `net.minecraftforge.*` or Forge-era provider/registration patterns.
- Registries live in `common/init/*Init.java`. Example: `ManipulationInit` holds the blood-manipulation registry and registers entries like `blood_shot` and `vital_effusion` with cost/rank/tendency metadata.
- Manipulation implementations live under `common/manipulation/<tendency>/`; many also define Drudge AI helpers with `setDrudgeAction(...)`. New manipulations usually need the class, a `ManipulationInit` entry, lang/model assets, and related docs.
- For Hemomancy special effects, inspect and prefer the authored Hemomancy/HutosLib visual toolkit before using vanilla particles. Reuse the dynamic glow, cell, tendril, lightning, claw, slash, and related custom effects whenever they can express the intended action; match their color, motion, layering, and intensity to the surrounding blocks, mobs, and items. Vanilla particles are a deliberate fallback for effects the custom suite cannot reasonably express, not the convenience default. This applies especially to bosses, rites, manipulations, impacts, transitions, and other focal moments; do not let otherwise custom visuals become dominated by generic vanilla particle clouds.
- Current resource paths are singular 1.21-style paths already used by this repo: `data/hemomancy/recipe/`, `data/hemomancy/loot_table/`, and `data/hemomancy/dialogue_inquiry/<npc>/<namespace>/<item>.json`. Do not “fix” them to older plural conventions.
- HutosLib owns part of the dialogue/book pipeline, so check library-backed serializers and reload listeners before inventing local replacements.

## Integration and lore guardrails
- `build.gradle` excludes `compat/mna/**` and `compat/curios/**`; do not reference those packages from core code unless the dependency and source-set gate are explicitly re-enabled.
- JEI compat is live via the local jar; HutosLib is required infrastructure, not optional sugar.
- Keep the existing tone: fungal blood-memory/cosmic infection, morally gray Harbingers, non-saintly Unstained, and no God/Satan framing for the Fungal Entity or Pale Lady.
- Preserve established vocabulary: Harbinger content uses terms like `Hematic Order`, `Sanguine Brotherhood`, `Crimson Lodge`; Unstained content uses `Lethean` / still-water language; enzyme item names intentionally do **not** mirror tendency enum names.

## Documentation contract
- If mechanics, progression, recipes, lore, or player-facing behavior change, check whether `docs/HEMOMANCY_REFERENCE.md`, `docs/LORE_REFERENCE.md`, `docs/MNA_COMPATIBILITY_BRAINSTORM.md`, or the relevant `wiki/*.md` pages also need updates before calling the work done.
